/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.pools

import com.bloomberg.selekt.annotations.Generated
import com.bloomberg.selekt.commons.LinkedDeque
import com.bloomberg.selekt.commons.withLockInterruptibly
import com.bloomberg.selekt.commons.withTryLock
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.concurrent.GuardedBy
import kotlin.concurrent.withLock
import kotlin.jvm.Throws

class CommonObjectPool<K : Any, T : IPooledObject<K>>(
    private val factory: IObjectFactory<T>,
    private val executor: ScheduledExecutorService,
    private val configuration: PoolConfiguration,
    private val otherPool: SingleObjectPool<K, T>
) : IObjectPool<K, T> {
    init {
        require(configuration.maxTotal > 0) { "Pool configuration must allow at least one object." }
    }

    private val isClosed = AtomicBoolean(false)

    private val lock = ReentrantLock(true)
    private val available = lock.newCondition()

    @GuardedBy("lock")
    private val idleObjects = LinkedDeque<T>()
    @GuardedBy("lock")
    private var count = 0
    @GuardedBy("lock")
    private var future: Future<*>? = null
    @GuardedBy("lock")
    private var tag = false

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        signalAllInterruptibly()
        evictInterruptibly()
    }

    override fun borrowObject() = internalBorrowObject { null }

    override fun borrowObject(key: K) = internalBorrowObject {
        idleObjects.pollFirst {
            it.matches(key)
        }
    }

    @Suppress("Detekt.ReturnCount")
    @Generated
    @Throws(InterruptedException::class)
    private inline fun internalBorrowObject(preferred: () -> T?): T {
        lock.withLockInterruptibly {
            while (!isClosed.get()) {
                preferred()?.let {
                    return it
                }
                idleObjects.pollFirst()?.let {
                    return it
                }
                if (count < configuration.maxTotal) {
                    attemptScheduleEviction()
                    ++count
                    return@withLockInterruptibly Unit
                }
                otherPool.borrowObjectOrNull()?.let { return it }
                do {
                    available.await()
                } while (idleObjects.isEmpty && count == configuration.maxTotal)
            }
            null
        }?.let {
            return factory.makeObject()
        }
        error("Pool is closed.")
    }

    override fun returnObject(obj: T) {
        lock.withLock {
            obj.tag = tag
            idleObjects.putFirst(obj)
            available.signal()
        }
        if (isClosed.get()) {
            evictInterruptibly()
        }
    }

    internal fun evict() {
        lock.withTryLock(0L, TimeUnit.MILLISECONDS) {
            if (isClosed.get()) {
                factory.close()
                available.signalAll()
            }
            if (count == 0) {
                cancelScheduledEviction()
                return@evict
            }
            evictions()
        }?.forEach {
            factory.destroyObject(it)
        }
    }

    @GuardedBy("lock")
    private fun evictions(): List<T> {
        fun T.isIdle() = this@isIdle.tag != this@CommonObjectPool.tag
        fun shouldRemove(it: T) = it.isIdle() && future?.isCancelled == false || isClosed.get()
        return sequence {
            val iterator = idleObjects.reverseMutableIterator()
            iterator.forEach {
                if (shouldRemove(it)) {
                    iterator.remove()
                    --count
                    available.signal()
                    yield(it)
                } else {
                    return@forEach
                }
            }
        }.toList().also { tag = !tag }
    }

    @GuardedBy("lock")
    private fun cancelScheduledEviction() = future?.apply {
        future = null
        cancel(false)
    }

    @GuardedBy("lock")
    private fun attemptScheduleEviction() {
        if (future?.isCancelled == false || configuration.evictionIntervalMillis < 0L || isClosed.get()) {
            return
        }
        tag = !tag
        future = executor.scheduleAtFixedRate(
            ::evict,
            configuration.evictionDelayMillis,
            configuration.evictionIntervalMillis,
            TimeUnit.MILLISECONDS
        )
    }

    private fun signalAllInterruptibly() {
        try {
            lock.withLockInterruptibly {
                available.signalAll()
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun evictInterruptibly() {
        try {
            evict()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
