/*
 * Copyright 2020 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.pools

import com.bloomberg.selekt.commons.LinkedDeque
import com.bloomberg.selekt.commons.forEachCatching
import com.bloomberg.selekt.commons.withLockInterruptibly
import com.bloomberg.selekt.commons.withTryLock
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.concurrent.GuardedBy
import javax.annotation.concurrent.ThreadSafe
import kotlin.concurrent.withLock
import kotlin.jvm.Throws

@ThreadSafe
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
        signalAll()
        evict()
    }

    override fun borrowObject() = internalBorrowObject { null }

    override fun borrowObject(key: K) = internalBorrowObject {
        idleObjects.pollFirst {
            it.matches(key)
        }
    }

    @Suppress("Detekt.ReturnCount")
    @Throws(InterruptedException::class)
    private inline fun internalBorrowObject(preferred: () -> T?): T {
        lock.withLockInterruptibly {
            while (!isClosed.get()) {
                preferred()?.let {
                    return it
                }
                idleObjects.pollLast()?.let {
                    return it
                }
                if (count < configuration.maxTotal) {
                    ++count
                    attemptScheduleEviction()
                    return@withLockInterruptibly Unit
                }
                otherPool.borrowObjectOrNull()?.let { return it }
                do {
                    available.await()
                } while (idleObjects.isEmpty && count == configuration.maxTotal)
            }
            null
        }?.let {
            return runCatching { factory.makeObject() }.getOrElse {
                lock.withLock {
                    --count
                    available.signal()
                }
                throw it
            }
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
            evict()
        }
    }

    override fun clear(priority: Priority) {
        executor.execute { evict(priority) }
    }

    @JvmSynthetic
    internal fun evict(priority: Priority? = null) {
        @GuardedBy("lock")
        fun evictions() = run {
            if (isClosed.get()) {
                factory.close()
                available.signalAll()
            }
            if (count == 0) {
                cancelScheduledEviction()
                return@run emptyList()
            }
            if (priority != null) {
                idleObjects.reverseMutableIterator().forEach {
                    it.releaseMemory()
                }
            }
            evictions(priority)
        }
        if (isClosed.get() || priority.isHigh()) {
            lock.withTryLock(::evictions)
        } else {
            lock.withTryLock(0L, TimeUnit.MILLISECONDS, ::evictions)
        }?.destroyEach()
    }

    @GuardedBy("lock")
    private fun evictions(priority: Priority?): List<T> {
        return sequence {
            val iterator = idleObjects.reverseMutableIterator()
            iterator.forEach {
                if (it shouldBeRemovedAt priority) {
                    iterator.remove()
                    --count
                    available.signal()
                    yield(it)
                } else {
                    return@forEach
                }
            }
        }.toList().also {
            if (priority == null) {
                tag = !tag
            }
        }
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
        future = executor.scheduleAtFixedRate(
            ::evict,
            configuration.evictionDelayMillis,
            configuration.evictionIntervalMillis,
            TimeUnit.MILLISECONDS
        )
    }

    private fun signalAll() {
        lock.withTryLock {
            available.signalAll()
        }
    }

    private infix fun T.shouldBeRemovedAt(
        priority: Priority?
    ) = (this@shouldBeRemovedAt.tag != this@CommonObjectPool.tag).let {
        it && (priority != null || !future!!.isCancelled) || isClosed.get() || priority.isHigh()
    }

    private fun Iterable<T>.destroyEach() {
        forEachCatching {
            factory.destroyObject(it)
        }.firstOrNull()?.let {
            throw it
        }
    }
}
