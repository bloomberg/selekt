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

import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MutexTest {
    @Test
    fun negativeNanos() {
        Mutex().let {
            assertFailsWith<IllegalArgumentException> {
                it.tryLock(-1L)
            }
        }
    }

    @Test
    fun lockThenTryLock() {
        Mutex().apply {
            lock()
            assertFalse(tryLock(0L))
        }
    }

    @Test
    fun tryLockZeroNanos() {
        assertTrue(Mutex().tryLock(0L))
    }

    @Test
    fun tryLockWithCancellation() {
        Mutex().apply { cancel() }.let {
            assertFailsWith<IllegalStateException> {
                it.tryLock(0L, true)
            }
        }
    }

    @Test
    fun tryLockWithoutCancellation() {
        assertTrue(Mutex().apply { cancel() }.tryLock(0L, false))
    }

    @Test
    fun tryLockFails() {
        val lock = Mutex()
        thread { lock.lock() }.join()
        assertFalse(lock.tryLock(1L, false))
    }

    @Test
    fun cancelThenLock() {
        Mutex().apply {
            assertTrue(cancel())
            assertFailsWith<IllegalStateException> {
                lock()
            }
        }
    }

    @Test
    fun cancelWhileWaiting() {
        val lock = Mutex()
        thread { lock.lock() }.join()
        thread {
            Thread.sleep(100L)
            lock.cancel()
        }
        assertFailsWith<IllegalStateException> {
            lock.lock()
        }
    }

    @Test
    fun mutexCancels() {
        Mutex().let {
            assertFalse(it.isCancelled())
            assertTrue(it.cancel())
            assertTrue(it.isCancelled())
        }
    }

    @Test
    fun mutexCancelsOnce() {
        Mutex().let {
            assertTrue(it.cancel())
            assertFalse(it.cancel())
        }
    }

    @Test
    fun lockThenInterrupt() {
        Mutex().apply {
            lock()
        }.let {
            Thread.currentThread().interrupt()
            assertFailsWith<InterruptedException> {
                it.lock()
            }
        }
    }

    @Test
    fun interruptThenTryLock() {
        Mutex().apply {
            val thread = Thread.currentThread().apply { interrupt() }
            assertFailsWith<InterruptedException> {
                tryLock(0L, false)
            }
            assertFalse(thread.isInterrupted)
        }
    }

    @Test
    fun lockInterrupts() {
        Mutex().apply {
            val thread = Thread.currentThread().apply { interrupt() }
            assertFailsWith<InterruptedException> {
                lock()
            }
            assertFalse(thread.isInterrupted)
        }
    }

    @Test
    fun interruptTrumpsCancellationWhenLocking() {
        Mutex().apply {
            cancel()
            Thread.currentThread().interrupt()
            assertFailsWith<InterruptedException> {
                lock()
            }
        }
    }

    @Test
    fun interruptTrumpsCancellationWhenTrying() {
        Mutex().apply {
            cancel()
            Thread.currentThread().interrupt()
            assertFailsWith<InterruptedException> {
                tryLock(0L, true)
            }
        }
    }

    @Test
    fun tryLockRespectsTimeout() {
        Mutex().apply {
            lock()
            val intervalNanos = TimeUnit.MILLISECONDS.toNanos(100L)
            val start = System.nanoTime()
            assertFalse(tryLock(intervalNanos, false))
            val duration = System.nanoTime() - start
            assertTrue(duration >= intervalNanos)
            assertTrue(duration <= intervalNanos + TimeUnit.MILLISECONDS.toNanos(300L))
        }
    }

    @Test
    fun interruptWaiterThenUnlockUnparksNext() {
        Mutex().apply {
            lock()
            val first = thread { lock() }
            val second = thread {
                Thread.sleep(100L)
                lock()
            }
            Thread.sleep(500L)
            first.interrupt()
            unlock()
            second.join()
        }
    }

    @Test
    fun interruptedWaiterExits() {
        Mutex().apply {
            lock()
            val first = thread {
                lock()
                Thread.interrupted() // So .join() does not throw.
            }
            Thread.sleep(100L)
            first.interrupt()
            first.join()
        }
    }

    @Test
    fun contention() {
        val executor = Executors.newFixedThreadPool(3)
        try {
            Mutex().apply {
                arrayOf({
                    lock()
                    unlock()
                }, {
                    lock()
                    unlock()
                }, {
                    if (tryLock(TimeUnit.MILLISECONDS.toNanos(100L))) {
                        unlock()
                    }
                }).map {
                    executor.submit {
                        repeat(100_000) {
                            it()
                        }
                    }
                }.forEach {
                    it.get()
                }
            }
        } finally {
            executor.shutdownNow()
        }
    }
}
