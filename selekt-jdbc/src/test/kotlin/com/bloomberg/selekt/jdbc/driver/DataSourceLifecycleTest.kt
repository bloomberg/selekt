/*
 * Copyright 2026 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.jdbc.driver

import java.sql.SQLException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

internal class DataSourceLifecycleTest {
    @Test
    fun closeWaitsForInFlightConnectionCreation() {
        val lifecycle = DataSourceLifecycle()
        val creationStarted = CountDownLatch(1)
        val allowCreation = CountDownLatch(1)
        val creationFinished = CountDownLatch(1)
        val closeAttempted = CountDownLatch(1)
        val cleanupStarted = CountDownLatch(1)
        val closeFinished = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()
        val creationThread = thread(name = "data-source-creation") {
            runCatching {
                lifecycle.withOpenDataSource {
                    creationStarted.countDown()
                    check(allowCreation.await(5, SECONDS))
                }
            }.onFailure {
                failure.compareAndSet(null, it)
            }
            creationFinished.countDown()
        }
        assertTrue(creationStarted.await(5, SECONDS))
        val closeThread = thread(name = "data-source-close") {
            closeAttempted.countDown()
            runCatching {
                lifecycle.close {
                    cleanupStarted.countDown()
                }
            }.onFailure {
                failure.compareAndSet(null, it)
            }
            closeFinished.countDown()
        }
        assertTrue(closeAttempted.await(5, SECONDS))
        assertFalse(cleanupStarted.await(200, MILLISECONDS))
        allowCreation.countDown()
        assertTrue(creationFinished.await(5, SECONDS))
        assertTrue(cleanupStarted.await(5, SECONDS))
        assertTrue(closeFinished.await(5, SECONDS))
        creationThread.join()
        closeThread.join()
        assertNull(failure.get())
        assertTrue(lifecycle.isClosed)
    }

    @Test
    fun connectionCreationQueuedBehindCloseIsRejected() {
        val lifecycle = DataSourceLifecycle()
        val cleanupStarted = CountDownLatch(1)
        val allowCleanup = CountDownLatch(1)
        val creationAttempted = CountDownLatch(1)
        val creationFinished = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()
        val closeThread = thread(name = "data-source-close") {
            lifecycle.close {
                cleanupStarted.countDown()
                check(allowCleanup.await(5, SECONDS))
            }
        }
        assertTrue(cleanupStarted.await(5, SECONDS))
        val creationThread = thread(name = "data-source-creation") {
            creationAttempted.countDown()
            failure.set(runCatching { lifecycle.withOpenDataSource {} }.exceptionOrNull())
            creationFinished.countDown()
        }
        assertTrue(creationAttempted.await(5, SECONDS))
        assertFalse(creationFinished.await(200, MILLISECONDS))
        allowCleanup.countDown()
        assertTrue(creationFinished.await(5, SECONDS))
        closeThread.join()
        creationThread.join()
        val thrown = failure.get()
        assertTrue(thrown is SQLException)
        assertEquals("DataSource is closed", thrown.message)
    }

    @Test
    fun closeRunsCleanupOnlyOnce() {
        val lifecycle = DataSourceLifecycle()
        val cleanups = AtomicInteger()
        assertTrue(lifecycle.close(cleanups::incrementAndGet))
        assertFalse(lifecycle.close(cleanups::incrementAndGet))
        assertEquals(1, cleanups.get())
        assertTrue(lifecycle.isClosed)
    }
}
