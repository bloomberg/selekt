/*
 * Copyright 2020 Bloomberg Finance L.P.
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

import com.bloomberg.commons.LinkedDeque
import com.bloomberg.commons.withTryLock
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.concurrent.GuardedBy
import kotlin.concurrent.withLock

internal class CommonObjectPool<K : Any, T : IKeyedObject<K>>(
    private val factory: IObjectFactory<T>,
    private val executor: ScheduledExecutorService,
    private val configuration: PoolConfiguration
) : IObjectPool<K, T> {
    init {
        require(configuration.maxTotal > 1) { "Pool configuration must allow at least two objects." }
    }

    private val maxSecondaryTotal = configuration.maxTotal - 1

    private val isClosed = AtomicBoolean(false)

    private val lock = ReentrantLock(true)
    private val primaryAvailable = lock.newCondition()
    private val secondaryAvailable = lock.newCondition()

    @GuardedBy("lock")
    private var idlePrimaryObject: PooledObject<T>? = null
    @GuardedBy("lock")
    private val idleSecondaryObjects = LinkedDeque<PooledObject<T>>()
    @GuardedBy("lock")
    private var primaryExists = false
    @GuardedBy("lock")
    private var secondaryCount = 0
    @GuardedBy("lock")
    private var future: ScheduledFuture<*>? = null
    @GuardedBy("lock")
    private var tag = Long.MIN_VALUE

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        evict()
    }

    override fun borrowPrimaryObject(): T {
        lock.withLock {
            while (!isClosed.get()) {
                idlePrimaryObject?.let {
                    idlePrimaryObject = null
                    return it.obj
                }
                if (!primaryExists) {
                    primaryExists = true
                    attemptScheduleEviction()
                    return@withLock Unit
                }
                do {
                    primaryAvailable.awaitUninterruptibly()
                } while (idlePrimaryObject == null && primaryExists)
            }
            null
        }?.let {
            return factory.makePrimaryObject()
        }
        error("Pool is closed.")
    }

    /**
     * Poll for a non-primary object, else make a non-primary object, else acquire the primary object if it is available
     * and has no waiters, else make the primary object, else wait for a non-primary object to become available.
     */
    @Suppress("Detekt.ComplexMethod", "Detekt.ReturnCount")
    override fun borrowObject(key: K): T {
        lock.withLock {
            while (!isClosed.get()) {
                idleSecondaryObjects.pollFirst {
                    it.obj.matches(key)
                }?.let {
                    return it.obj
                }
                idleSecondaryObjects.pollFirst()?.let {
                    return it.obj
                }
                if (secondaryCount < maxSecondaryTotal) {
                    ++secondaryCount
                    attemptScheduleEviction()
                    return@withLock Tier.SECONDARY
                }
                if (!lock.hasWaiters(primaryAvailable)) {
                    idlePrimaryObject?.let {
                        idlePrimaryObject = null
                        return it.obj
                    }
                    if (!primaryExists) {
                        primaryExists = true
                        return@withLock Tier.PRIMARY
                    }
                }
                do {
                    secondaryAvailable.awaitUninterruptibly()
                } while (idleSecondaryObjects.isEmpty && secondaryCount == maxSecondaryTotal)
            }
            null
        }?.let {
            return when (it) {
                Tier.SECONDARY -> factory.makeObject()
                Tier.PRIMARY -> factory.makePrimaryObject()
            }
        }
        error("Pool is closed.")
    }

    override fun gauge() = factory.gauge().run {
        PoolGauge(createdCount - destroyedCount)
    }

    override fun returnObject(obj: T) {
        lock.withLock {
            if (obj.isPrimary) {
                returnPrimaryObject(obj)
            } else {
                returnSecondaryObject(obj)
            }
        }
        if (isClosed.get()) {
            evict()
        }
    }

    @GuardedBy("lock")
    private fun returnPrimaryObject(obj: T) {
        idlePrimaryObject = PooledObject(obj, tag)
        primaryAvailable.signal()
    }

    @GuardedBy("lock")
    private fun returnSecondaryObject(obj: T) {
        idleSecondaryObjects.putFirst(PooledObject(obj, tag))
        secondaryAvailable.signal()
    }

    internal fun evict() {
        lock.withTryLock {
            if (isClosed.get()) {
                factory.close()
                primaryAvailable.signalAll()
                secondaryAvailable.signalAll()
            }
            if (!primaryExists && secondaryCount == 0) {
                cancelScheduledEviction()
                return@evict
            }
            evictions()
        }?.forEach {
            factory.destroyObject(it.obj)
        }
    }

    @GuardedBy("lock")
    private fun evictions(): List<PooledObject<T>> {
        fun PooledObject<T>.isIdle() = this@isIdle.tag - this@CommonObjectPool.tag < 0L
        fun shouldRemove(it: PooledObject<T>) = isClosed.get() ||
            it.isIdle() && future?.isCancelled == false
        return sequence {
            val iterator = idleSecondaryObjects.reverseMutableIterator()
            iterator.forEach {
                if (shouldRemove(it)) {
                    iterator.remove()
                    --secondaryCount
                    secondaryAvailable.signal()
                    yield(it)
                } else {
                    return@forEach
                }
            }
            idlePrimaryObject?.let {
                if (shouldRemove(it)) {
                    idlePrimaryObject = null
                    primaryExists = false
                    primaryAvailable.signal()
                    yield(it)
                }
            }
        }.toList().also { ++tag }
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
        ++tag
        future = executor.scheduleAtFixedRate(
            ::evict,
            configuration.evictionDelayMillis,
            configuration.evictionIntervalMillis,
            TimeUnit.MILLISECONDS
        )
    }
}

private enum class Tier {
    PRIMARY,
    SECONDARY
}
