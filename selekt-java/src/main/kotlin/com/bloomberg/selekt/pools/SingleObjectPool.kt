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

import java.sql.SQLTimeoutException
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder
import javax.annotation.concurrent.GuardedBy
import javax.annotation.concurrent.ThreadSafe

/**
 * @since 0.12.1
 */
@ThreadSafe
class SingleObjectPool<K : Any, T : IPooledObject<K>> @JvmOverloads constructor(
    private val factory: IObjectFactory<T>,
    private val executor: ScheduledExecutorService,
    private val evictionDelayMillis: Long,
    private val evictionIntervalMillis: Long,
    private val borrowWaitTimeoutMillis: Long = -1L
) : IObjectPool<K, T> {
    init {
        require(borrowWaitTimeoutMillis >= -1L) { "Borrow wait timeout must be non-negative or -1." }
    }

    private val mutex = Mutex()
    private val active = AtomicInteger()
    private val objectCount = AtomicInteger()
    private val pending = AtomicInteger()
    private val borrowCount = LongAdder()
    private val borrowWaitNanos = LongAdder()
    private val borrowTimeoutCount = LongAdder()
    private val evictionCount = LongAdder()

    private var retainObject = false

    @JvmSynthetic
    internal fun retainUntilClose() {
        retainObject = true
    }

    @GuardedBy("mutex")
    private var obj: T? = null

    @GuardedBy("mutex")
    private var canEvict = false

    @GuardedBy("mutex")
    private var future: Future<*>? = null

    private val isClosed: Boolean
        get() = mutex.isCancelled()

    override fun close() {
        if (!mutex.cancel()) {
            return
        }
        evict()
    }

    override fun borrowObject(): T {
        val startNanos = System.nanoTime()
        pending.incrementAndGet()
        try {
            if (borrowWaitTimeoutMillis < 0L) {
                mutex.lock()
            } else if (!mutex.tryLock(TimeUnit.MILLISECONDS.toNanos(borrowWaitTimeoutMillis))) {
                borrowTimeoutCount.increment()
                throw SQLTimeoutException("Timed out waiting for the primary database connection.", "HYT00")
            }
            return acquireObject().also {
                active.set(1)
                recordBorrow(startNanos)
            }
        } finally {
            pending.decrementAndGet()
        }
    }

    override fun borrowObject(key: K) = borrowObject()

    fun borrowObjectOrNull(): T? = if (mutex.tryLock(0L, true)) {
        val startNanos = System.nanoTime()
        acquireObject().also {
            active.set(1)
            recordBorrow(startNanos)
        }
    } else {
        null
    }

    override fun returnObject(obj: T) {
        active.set(0)
        canEvict = false
        if (isClosed) {
            processCloseThenUnlock()
        } else {
            mutex.unlock()
        }
    }

    override fun clear(priority: Priority) {
        executor.execute { evict(priority) }
    }

    @JvmSynthetic
    internal fun evict(priority: Priority? = null) = mutex.run {
        when {
            isClosed -> withTryLock {
                factory.close()
                evictions(null)
            }.also {
                attemptUnparkWaiters()
            }
            priority.isHigh() -> withTryLock {
                if (retainObject) {
                    obj?.releaseMemory()
                }
                evictions(priority)
            }
            else -> withTryLock(0L, false) {
                if (priority != null) {
                    obj?.releaseMemory()
                }
                evictions(priority)
            }
        }
    }?.let {
        evictionCount.increment()
        factory.destroyObject(it)
    }

    @GuardedBy("mutex")
    private fun acquireObject(): T {
        return obj ?: runCatching {
            canEvict = false
            factory.makePrimaryObject()
        }.getOrElse {
            mutex.unlock()
            throw it
        }.also {
            obj = it
            objectCount.set(1)
            attemptScheduleEviction()
        }
    }

    @GuardedBy("mutex")
    private fun attemptScheduleEviction() {
        if (retainObject || evictionIntervalMillis < 0L || isClosed) {
            return
        }
        future = executor.scheduleWithFixedDelay(
            ::evict,
            evictionDelayMillis,
            evictionIntervalMillis,
            TimeUnit.MILLISECONDS
        )
    }

    @GuardedBy("mutex")
    private fun cancelScheduledEviction() {
        future?.let {
            future = null
            it.cancel(false)
        }
    }

    @GuardedBy("mutex")
    private fun evictions(priority: Priority?): T? {
        val mayRemove = !retainObject || isClosed
        return (if (mayRemove && obj?.let { it shouldBeRemovedAt priority } == true) obj else null)?.also {
            obj = null
            objectCount.set(0)
            cancelScheduledEviction()
        }.also {
            canEvict = canEvict || priority == null
        }
    }

    private fun processCloseThenUnlock() {
        try {
            factory.close()
            evictions(null)
        } finally {
            mutex.unlock()
        }?.let(factory::destroyObject)
    }

    private infix fun T.shouldBeRemovedAt(priority: Priority?) =
        canEvict && (priority != null || !future!!.isCancelled) || isClosed || priority.isHigh()

    internal fun metrics() = ObjectPoolMetrics(
        active = active.get(),
        idle = (objectCount.get() - active.get()).coerceAtLeast(0),
        pending = pending.get(),
        borrowCount = borrowCount.sum(),
        borrowWaitNanos = borrowWaitNanos.sum(),
        borrowTimeoutCount = borrowTimeoutCount.sum(),
        evictionCount = evictionCount.sum()
    )

    private fun recordBorrow(startNanos: Long) {
        borrowCount.increment()
        borrowWaitNanos.add((System.nanoTime() - startNanos).coerceAtLeast(0L))
    }
}
