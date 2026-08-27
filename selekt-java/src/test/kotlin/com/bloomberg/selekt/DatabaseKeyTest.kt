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

package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

private const val KEY_POINTER = 0x5EC4E7L
private const val KEY_SIZE = 32

internal class DatabaseKeyTest {
    @Test
    fun closeFreesTheSecretExactlyOnce() {
        val sqlite = mock<IExternalSQLite>()
        DatabaseKey(sqlite, KEY_POINTER, KEY_SIZE).close()
        verify(sqlite, times(1)).freeSecret(eq(KEY_POINTER), eq(KEY_SIZE))
    }

    @Test
    fun closeIsIdempotent() {
        val sqlite = mock<IExternalSQLite>()
        DatabaseKey(sqlite, KEY_POINTER, KEY_SIZE).apply {
            close()
            close()
        }
        verify(sqlite, times(1)).freeSecret(eq(KEY_POINTER), eq(KEY_SIZE))
    }

    @Test
    fun useAfterCloseThrows() {
        val sqlite = mock<IExternalSQLite>()
        val key = DatabaseKey(sqlite, KEY_POINTER, KEY_SIZE).apply { close() }
        assertFailsWith<IllegalStateException> {
            key.use { _, _ -> }
        }
    }

    @Test
    fun useDoesNotFreeTheSecretWhileActionIsRunning() {
        val sqlite = mock<IExternalSQLite>()
        val key = DatabaseKey(sqlite, KEY_POINTER, KEY_SIZE)
        key.use { pointer, size ->
            assertEquals(KEY_POINTER, pointer)
            assertEquals(KEY_SIZE, size)
            verify(sqlite, never()).freeSecret(eq(KEY_POINTER), eq(KEY_SIZE))
        }
    }

    @Test
    fun retainKeepsTheSecretAliveAcrossOneClose() {
        val sqlite = mock<IExternalSQLite>()
        val key = DatabaseKey(sqlite, KEY_POINTER, KEY_SIZE)
        key.retain()
        key.close()
        verify(sqlite, never()).freeSecret(eq(KEY_POINTER), eq(KEY_SIZE))
        key.release()
        verify(sqlite, times(1)).freeSecret(eq(KEY_POINTER), eq(KEY_SIZE))
    }

    @Test
    fun concurrentUseAndCloseFreesTheSecretExactlyOnce() {
        val sqlite = mock<IExternalSQLite>()
        val key = DatabaseKey(sqlite, KEY_POINTER, KEY_SIZE)
        val threadCount = 8
        val iterationsPerThread = 500
        val startLatch = CountDownLatch(1)
        val unexpectedFailures = AtomicInteger(0)
        val workers = List(threadCount) {
            thread(start = false) {
                startLatch.await()
                repeat(iterationsPerThread) {
                    try {
                        key.use { _, _ -> }
                    } catch (@Suppress("SwallowedException") e: IllegalStateException) {
                        // Expected once the key has been destroyed by the racing close().
                    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                        unexpectedFailures.incrementAndGet()
                    }
                }
            }
        }
        val closer = thread(start = false) {
            startLatch.await()
            key.close()
        }
        (workers + closer).forEach { it.start() }
        startLatch.countDown()
        (workers + closer).forEach { it.join() }
        assertEquals(0, unexpectedFailures.get())
        verify(sqlite, times(1)).freeSecret(eq(KEY_POINTER), eq(KEY_SIZE))
    }
}
