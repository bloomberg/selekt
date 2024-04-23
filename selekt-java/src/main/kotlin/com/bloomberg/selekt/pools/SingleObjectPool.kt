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

import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.annotation.concurrent.GuardedBy
import javax.annotation.concurrent.ThreadSafe

@ThreadSafe
class SingleObjectPool<K : Any, T : IPooledObject<K>>(
    private val factory: IObjectFactory<T>,
    private val executor: ScheduledExecutorService,
    private val evictionDelayMillis: Long,
    private val evictionIntervalMillis: Long
) : IObjectPool<K, T> {
    private val mutex = Mutex()

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
        mutex.lock()
        return acquireObject()
    }

    override fun borrowObject(key: K) = borrowObject()

    fun borrowObjectOrNull() = if (mutex.tryLock(0L, true)) {
        acquireObject()
    } else {
        null
    }

    override fun returnObject(obj: T) {
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
            attemptScheduleEviction()
        }
    }

    @GuardedBy("mutex")
    private fun attemptScheduleEviction() {
        if (evictionIntervalMillis < 0L || isClosed) {
            return
        }
        future = executor.scheduleAtFixedRate(
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
        return (if (obj?.let { it shouldBeRemovedAt priority } == true) obj else null)?.also {
            obj = null
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
        }?.let {
            factory.destroyObject(it)
        }
    }

    private infix fun T.shouldBeRemovedAt(priority: Priority?) =
        canEvict && (priority != null || !future!!.isCancelled) || isClosed || priority.isHigh()
}
