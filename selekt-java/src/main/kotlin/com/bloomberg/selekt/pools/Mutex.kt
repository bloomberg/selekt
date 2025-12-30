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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import java.util.concurrent.locks.LockSupport

/**
 * A non-fair and non-reentrant lock.
 */
internal class Mutex {
    @Suppress("unused")
    @Volatile
    private var isLocked = 0

    @Suppress("unused")
    @Volatile
    private var head: Waiter? = null

    @Volatile
    private var isCancelled = 0

    @Suppress("unused")
    @Volatile
    private var tail: Waiter? = null

    private class Waiter(val thread: Thread) {
        companion object {
            val nextUpdater: AtomicReferenceFieldUpdater<Waiter, Waiter> = AtomicReferenceFieldUpdater.newUpdater(
                Waiter::class.java,
                Waiter::class.java,
                "next"
            )
        }

        @Suppress("unused")
        @Volatile
        var next: Waiter? = null
    }

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
        LockSupport.unpark(headUpdater[this]?.thread)
    }

    fun cancel() = isCancelledUpdater.compareAndSet(this, 0, 1).also {
        attemptUnparkWaiters()
    }

    fun isCancelled() = isCancelled != 0

    /**
     * Best effort to unpark all waiting threads.
     */
    fun attemptUnparkWaiters() = forEachWaiter {
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
        addWaiter(thread)
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
        pollHead()
        return true
    }

    private fun removeThisWaiterNotifyingNext() {
        isThisHead().also {
            check(removeWaiter(Thread.currentThread())) { "Failed to remove waiter." }
            if (it) {
                LockSupport.unpark(headUpdater[this]?.thread)
            }
        }
    }

    private fun isThisHead() = headUpdater[this]?.thread === Thread.currentThread()

    private fun addWaiter(thread: Thread) {
        val waiter = Waiter(thread)
        while (true) {
            val currentTail = tailUpdater[this]
            if (currentTail == null) {
                if (headUpdater.compareAndSet(this, null, waiter)) {
                    tailUpdater.compareAndSet(this, null, waiter)
                    return
                }
            } else {
                val next = Waiter.nextUpdater[currentTail]
                if (next != null) {
                    tailUpdater.compareAndSet(this, currentTail, next)
                    continue
                }
                if (Waiter.nextUpdater.compareAndSet(currentTail, null, waiter)) {
                    tailUpdater.compareAndSet(this, currentTail, waiter)
                    return
                }
            }
        }
    }

    private fun pollHead(): Waiter? {
        while (true) {
            val currentHead = headUpdater[this] ?: return null
            val next = Waiter.nextUpdater[currentHead]
            if (currentHead != headUpdater[this]) {
                continue
            }
            if (headUpdater.compareAndSet(this, currentHead, next)) {
                if (next == null) {
                    tailUpdater.compareAndSet(this, currentHead, null)
                }
                Waiter.nextUpdater[currentHead] = null
                return currentHead
            }
        }
    }

    private fun removeWaiter(thread: Thread): Boolean {
        while (true) {
            val currentHead = headUpdater[this] ?: return false
            if (currentHead.thread === thread) {
                return removeHead()
            }
            var previous = currentHead
            var current = Waiter.nextUpdater[previous]
            while (current != null) {
                if (current.thread === thread) {
                    return unlinkWaiter(previous, current)
                }
                previous = current
                current = Waiter.nextUpdater[current]
            }
            return false
        }
    }

    private fun removeHead(): Boolean {
        val currentHead = headUpdater[this] ?: return false
        val next = Waiter.nextUpdater[currentHead]
        if (headUpdater.compareAndSet(this, currentHead, next)) {
            if (next == null) {
                tailUpdater.compareAndSet(this, currentHead, null)
            }
            Waiter.nextUpdater[currentHead] = null
            return true
        }
        return false
    }

    private fun unlinkWaiter(prev: Waiter, current: Waiter): Boolean {
        val next = Waiter.nextUpdater[current]
        if (Waiter.nextUpdater.compareAndSet(prev, current, next)) {
            if (next == null) {
                tailUpdater.compareAndSet(this, current, prev)
            }
            Waiter.nextUpdater[current] = null
            return true
        }
        return false
    }

    private inline fun forEachWaiter(block: (Thread) -> Unit) {
        var current = headUpdater[this]
        while (current != null) {
            block(current.thread)
            current = Waiter.nextUpdater[current]
        }
    }

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

        val headUpdater: AtomicReferenceFieldUpdater<Mutex, Waiter> = AtomicReferenceFieldUpdater.newUpdater(
            Mutex::class.java,
            Waiter::class.java,
            "head"
        )

        val tailUpdater: AtomicReferenceFieldUpdater<Mutex, Waiter> = AtomicReferenceFieldUpdater.newUpdater(
            Mutex::class.java,
            Waiter::class.java,
            "tail"
        )

        fun cancellationError(): Nothing = error("Mutex received cancellation signal.")
    }
}
