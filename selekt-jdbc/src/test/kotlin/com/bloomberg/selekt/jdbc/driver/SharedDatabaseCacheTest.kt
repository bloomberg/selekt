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

import com.bloomberg.selekt.SQLDatabase
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class SharedDatabaseCacheTest {
    private val scheduler = ScheduledThreadPoolExecutor(1).apply {
        removeOnCancelPolicy = true
    }
    private val caches = mutableListOf<SharedDatabaseCache>()

    @AfterEach
    fun tearDown() {
        caches.forEach(SharedDatabaseCache::close)
        scheduler.shutdownNow()
        scheduler.awaitTermination(5, TimeUnit.SECONDS)
    }

    private fun cache(
        maxIdleEntries: Int = DEFAULT_MAX_IDLE_JDBC_DATABASES,
        idleTimeoutMillis: Long = JDBC_DATABASE_IDLE_TIMEOUT_MILLIS
    ) = SharedDatabaseCache(maxIdleEntries, idleTimeoutMillis, scheduler).also(caches::add)

    @Test
    fun reusesIdleDatabaseBeforeTimeout() {
        val database = mock<SQLDatabase>()
        val cache = cache(idleTimeoutMillis = 60_000L)
        val first = cache.acquire("database") { database }
        first.releaseConnection()

        val second = cache.acquire("database") { fail("Idle database was not reused") }

        assertSame(first, second)
        second.releaseConnection()
        verify(database, never()).close()
    }

    @Test
    fun releasesDatabaseAfterIdleTimeout() {
        val database = mock<SQLDatabase>()
        val cache = cache(idleTimeoutMillis = 20L)
        val shared = cache.acquire("database") { database }

        shared.releaseConnection()

        assertTrue(waitUntil { !shared.isOpen() }, "Database did not expire")
        assertEquals(0, cache.size)
        verify(database).close()
    }

    @Test
    fun releasesDatabaseImmediatelyWhenRequested() {
        val database = mock<SQLDatabase>()
        val cache = cache(idleTimeoutMillis = 60_000L)
        val shared = cache.acquire("database", DatabaseIdlePolicy.RELEASE_IMMEDIATELY) { database }

        shared.releaseConnection()

        assertFalse(shared.isOpen())
        assertEquals(0, cache.size)
        verify(database).close()
    }

    @Test
    fun retainsDatabaseUntilCacheIsClearedWhenRequested() {
        val database = mock<SQLDatabase>()
        val cache = cache(maxIdleEntries = 0, idleTimeoutMillis = 0L)
        val shared = cache.acquire("database", DatabaseIdlePolicy.RETAIN_UNTIL_CLEARED) { database }

        shared.releaseConnection()

        assertTrue(shared.isOpen())
        assertEquals(1, cache.size)
        verify(database, never()).close()

        cache.clear()
        assertFalse(shared.isOpen())
        verify(database).close()
    }

    @Test
    fun evictsLeastRecentlyUsedIdleDatabaseAtCapacity() {
        val firstDatabase = mock<SQLDatabase>()
        val secondDatabase = mock<SQLDatabase>()
        val cache = cache(maxIdleEntries = 1, idleTimeoutMillis = 60_000L)
        val first = cache.acquire("first") { firstDatabase }
        first.releaseConnection()

        val second = cache.acquire("second") { secondDatabase }
        second.releaseConnection()

        assertFalse(first.isOpen())
        assertTrue(second.isOpen())
        assertEquals(1, cache.size)
        verify(firstDatabase).close()
        verify(secondDatabase, never()).close()
    }

    @Test
    fun clearingCacheDoesNotCloseActiveConnection() {
        val database = mock<SQLDatabase>()
        val cache = cache()
        val shared = cache.acquire("database") { database }

        cache.clear()

        assertEquals(0, cache.size)
        assertTrue(shared.isOpen())
        verify(database, never()).close()
        shared.releaseConnection()
        assertFalse(shared.isOpen())
        verify(database).close()
    }

    private fun waitUntil(condition: () -> Boolean): Boolean {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (System.nanoTime() < deadline) {
            if (condition()) {
                return true
            }
            Thread.sleep(10L)
        }
        return condition()
    }
}
