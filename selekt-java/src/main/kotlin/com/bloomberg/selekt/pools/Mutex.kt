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

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import java.util.concurrent.locks.LockSupport

/**
 * A non-fair and non-reentrant lock.
 */
internal class Mutex {
    @Suppress("unused")
    @Volatile
    private var isLocked = 0

    @Volatile
    private var isCancelled = 0
    private val waiters = ConcurrentLinkedQueue<Thread>()

    fun lock() {
        when {
            Thread.interrupted() -> throw InterruptedException()
            isCancelled() -> cancellationError()
            internalTryLock() -> return
        }
        check(awaitLock(Long.MIN_VALUE, true)) { "Failed to acquire lock." }
    }

    fun tryLock(
        nanos: Long,
        isCancellable: Boolean = true
    ): Boolean {
        require(nanos >= 0L) { "Nanos must be non-negative." }
        if (Thread.interrupted()) {
            throw InterruptedException()
        } else if (isCancellable && isCancelled()) {
            cancellationError()
        }
        return awaitLock(nanos, isCancellable)
    }

    fun unlock() {
        isLockedUpdater[this] = 0
        LockSupport.unpark(waiters.peek())
    }

    fun cancel() = isCancelledUpdater.compareAndSet(this, 0, 1).also {
        attemptUnparkWaiters()
    }

    fun isCancelled() = isCancelled != 0

    /**
     * Best effort to unpark all waiting threads.
     */
    fun attemptUnparkWaiters() = waiters.forEach {
        LockSupport.unpark(it)
    }

    inline fun <R> withTryLock(
        block: () -> R
    ): R? = if (internalTryLock()) {
        try {
            block()
        } finally {
            unlock()
        }
    } else {
        null
    }

    inline fun <R> withTryLock(
        nanos: Long,
        isCancellable: Boolean,
        block: () -> R
    ): R? = if (tryLock(nanos, isCancellable)) {
        try {
            block()
        } finally {
            unlock()
        }
    } else {
        null
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun internalTryLock() = isLockedUpdater.compareAndSet(this, 0, 1)

    /**
     * @param intervalNanos the maximum time to wait for the lock in nanoseconds; negative indicates indefinitely.
     * @param isCancellable true if the wait can be cancelled, else false.
     * @return true if the thread succeeded in acquiring the lock, else false.
     */
    @Suppress("Detekt.CommentOverPrivateFunction", "Detekt.ComplexMethod", "Detekt.ReturnCount")
    private fun awaitLock(
        intervalNanos: Long,
        isCancellable: Boolean
    ): Boolean {
        val thread = Thread.currentThread()
        if (!waiters.add(thread)) {
            return false
        }
        var remainingNanos = intervalNanos
        val deadlineNanos = System.nanoTime() + intervalNanos
        while (!(isThisHead() && internalTryLock())) {
            when {
                intervalNanos < 0L -> LockSupport.park(this)
                intervalNanos == 0L -> {
                    removeThisWaiterNotifyingNext()
                    return false
                }
                else -> LockSupport.parkNanos(this, remainingNanos)
            }
            if (Thread.interrupted()) {
                removeThisWaiterNotifyingNext()
                throw InterruptedException()
            } else if (isCancellable && isCancelled()) {
                removeThisWaiterNotifyingNext()
                cancellationError()
            } else if (intervalNanos > 0L) {
                remainingNanos = (deadlineNanos - System.nanoTime()).also {
                    if (it <= 0L) {
                        removeThisWaiterNotifyingNext()
                        return false
                    }
                }
            }
        }
        waiters.remove()
        return true
    }

    private fun removeThisWaiterNotifyingNext() {
        isThisHead().also {
            check(waiters.remove(Thread.currentThread())) { "Failed to remove waiter." }
            if (it) {
                LockSupport.unpark(waiters.peek())
            }
        }
    }

    private fun isThisHead() = waiters.peek() === Thread.currentThread()

    private companion object {
        // Reduce the risk of "lost unpark" due to class loading.
        @Suppress("unused")
        private val ensureLoaded: Class<*> = LockSupport::class.java

        val isLockedUpdater: AtomicIntegerFieldUpdater<Mutex> = AtomicIntegerFieldUpdater.newUpdater(
            Mutex::class.java,
            "isLocked"
        )

        val isCancelledUpdater: AtomicIntegerFieldUpdater<Mutex> = AtomicIntegerFieldUpdater.newUpdater(
            Mutex::class.java,
            "isCancelled"
        )

        fun cancellationError(): Nothing = error("Mutex received cancellation signal.")
    }
}
