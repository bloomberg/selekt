/*
 * Copyright 2021 Bloomberg Finance L.P.
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

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MutexTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    @Test
    fun negativeNanos() {
        Mutex().let {
            assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
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
            assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
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
            assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
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
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
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
            assertThatExceptionOfType(InterruptedException::class.java).isThrownBy {
                it.lock()
            }
        }
    }

    @Test
    fun interruptThenTryLock() {
        Mutex().apply {
            val thread = Thread.currentThread().apply { interrupt() }
            assertThatExceptionOfType(InterruptedException::class.java).isThrownBy {
                tryLock(0L, false)
            }
            assertFalse(thread.isInterrupted)
        }
    }

    @Test
    fun lockInterrupts() {
        Mutex().apply {
            val thread = Thread.currentThread().apply { interrupt() }
            assertThatExceptionOfType(InterruptedException::class.java).isThrownBy {
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
            assertThatExceptionOfType(InterruptedException::class.java).isThrownBy {
                lock()
            }
        }
    }

    @Test
    fun interruptTrumpsCancellationWhenTrying() {
        Mutex().apply {
            cancel()
            Thread.currentThread().interrupt()
            assertThatExceptionOfType(InterruptedException::class.java).isThrownBy {
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

    @Test(timeout = 2_000L)
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

    @Test(timeout = 2_000L)
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

    @Test(timeout = 10_000L)
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
