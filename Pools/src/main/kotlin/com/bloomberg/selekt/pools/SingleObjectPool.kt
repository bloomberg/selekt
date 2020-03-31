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

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.concurrent.GuardedBy
import com.bloomberg.commons.InterruptibleSemaphore
import com.bloomberg.commons.withTryAcquisition

internal class SingleObjectPool<K : Any, T : IKeyedObject<K>>(
    private val factory: IObjectFactory<T>,
    private val executor: ScheduledExecutorService,
    private val evictionDelayMillis: Long,
    private val evictionIntervalMillis: Long
) : IObjectPool<K, T> {
    private val isClosed = AtomicBoolean(false)

    private val semaphore = InterruptibleSemaphore(1)
    @GuardedBy("semaphore")
    private var obj: T? = null
    @GuardedBy("semaphore")
    private var canEvict = false
    @GuardedBy("semaphore")
    private var future: ScheduledFuture<*>? = null

    override fun close() {
        if (isClosed.getAndSet(true)) {
            return
        }
        semaphore.interruptWaiters()
        evict()
    }

    override fun borrowObject(key: K) = borrowPrimaryObject()

    @Suppress("Detekt.UnconditionalJumpStatementInLoop")
    override fun borrowPrimaryObject(): T {
        while (!isClosed.get()) {
            try {
                semaphore.acquire()
            } catch (_: InterruptedException) {
                continue
            }
            if (isClosed.get()) {
                semaphore.release()
                break
            }
            canEvict = false
            return obj ?: factory.makePrimaryObject().also {
                obj = it
                attemptScheduleEviction()
            }
        }
        error("Pool is closed.")
    }

    override fun gauge() = factory.gauge().run {
        PoolGauge(createdCount - destroyedCount)
    }

    override fun returnObject(obj: T) {
        semaphore.release()
        if (isClosed.get()) {
            evict()
        }
    }

    internal fun evict() {
        semaphore.withTryAcquisition {
            if (isClosed.get()) {
                factory.close()
            }
            (if (isClosed.get() || canEvict && future?.isCancelled == false) obj else null)?.also {
                obj = null
                cancelScheduledEviction()
            }.also {
                canEvict = true
            }
        }?.let {
            factory.destroyObject(it)
        }
    }

    @GuardedBy("semaphore")
    private fun attemptScheduleEviction() {
        if (future?.isCancelled == false || evictionIntervalMillis < 0L || isClosed.get()) {
            return
        }
        canEvict = true
        future = executor.scheduleAtFixedRate(
            ::evict,
            evictionDelayMillis,
            evictionIntervalMillis,
            TimeUnit.MILLISECONDS
        )
    }

    @GuardedBy("semaphore")
    private fun cancelScheduledEviction() = future?.let {
        future = null
        it.cancel(false)
    }
}
