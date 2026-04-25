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

package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val databaseConfiguration = DatabaseConfiguration(
    evictionDelayMillis = 5_000L,
    maxConnectionPoolSize = 1,
    maxSqlCacheSize = 5,
    timeBetweenEvictionRunsMillis = 5_000L
)

private const val DB = 1L

internal class SQLConnectionFactoryTest {
    @Test
    fun closeThenMakePrimaryThrows() {
        SQLConnectionFactory("", mock(), mock(), mock(), mock()).apply {
            close()
            assertFailsWith<IllegalStateException> {
                makePrimaryObject()
            }
        }
    }

    @Test
    fun closeThenMakeThrows() {
        SQLConnectionFactory("", mock(), mock(), mock(), mock()).apply {
            close()
            assertFailsWith<IllegalStateException> {
                makeObject()
            }
        }
    }

    @Test
    fun interruptWithNoConnections() {
        SQLConnectionFactory("", mock(), mock(), mock(), null).interrupt()
    }

    @Test
    fun interruptCallsInterruptOnAllConnections() {
        val sqlite: SQLite = mock()
        whenever(sqlite.openV2(any(), any(), any())).doAnswer {
            (it.arguments[2] as LongArray)[0] = DB
            0
        }
        val factory = SQLConnectionFactory("file::memory:", sqlite, databaseConfiguration, CommonThreadLocalRandom, null)
        factory.makePrimaryObject().use {
            factory.interrupt()
            verify(sqlite, times(1)).interrupt(eq(DB))
        }
    }

    @Test
    fun interruptDoesNotCallDestroyedConnections() {
        val sqlite: SQLite = mock()
        whenever(sqlite.openV2(any(), any(), any())).doAnswer {
            (it.arguments[2] as LongArray)[0] = DB
            0
        }
        val factory = SQLConnectionFactory("file::memory:", sqlite, databaseConfiguration, CommonThreadLocalRandom, null)
        val connection = factory.makePrimaryObject()
        factory.destroyObject(connection)
        factory.interrupt()
        verify(sqlite, times(0)).interrupt(any())
    }

    @Test
    fun isInterruptedWithNoConnections() {
        assertFalse(SQLConnectionFactory("", mock(), mock(), mock(), null).isInterrupted)
    }

    @Test
    fun isInterruptedReturnsTrueWhenConnectionIsInterrupted() {
        val sqlite: SQLite = mock()
        whenever(sqlite.openV2(any(), any(), any())).doAnswer {
            (it.arguments[2] as LongArray)[0] = DB
            0
        }
        whenever(sqlite.isInterrupted(any())) doReturn true
        val factory = SQLConnectionFactory("file::memory:", sqlite, databaseConfiguration, CommonThreadLocalRandom, null)
        factory.makePrimaryObject().use {
            assertTrue(factory.isInterrupted)
        }
    }

    @Test
    fun isInterruptedReturnsFalseWhenConnectionIsNotInterrupted() {
        val sqlite: SQLite = mock()
        whenever(sqlite.openV2(any(), any(), any())).doAnswer {
            (it.arguments[2] as LongArray)[0] = DB
            0
        }
        whenever(sqlite.isInterrupted(any())) doReturn false
        val factory = SQLConnectionFactory("file::memory:", sqlite, databaseConfiguration, CommonThreadLocalRandom, null)
        factory.makePrimaryObject().use {
            assertFalse(factory.isInterrupted)
        }
    }

    @Test
    fun setProgressHandlerWithNoConnections() {
        SQLConnectionFactory("", mock(), mock(), mock(), null).setProgressHandler(100, SQLProgressHandler { 0 })
    }

    @Test
    fun setProgressHandlerCallsAllConnections() {
        val sqlite: SQLite = mock()
        whenever(sqlite.openV2(any(), any(), any())).doAnswer {
            (it.arguments[2] as LongArray)[0] = DB
            0
        }
        val handler = SQLProgressHandler { 0 }
        val factory = SQLConnectionFactory("file::memory:", sqlite, databaseConfiguration, CommonThreadLocalRandom, null)
        factory.makePrimaryObject().use {
            factory.setProgressHandler(100, handler)
            verify(sqlite, times(1)).progressHandler(eq(DB), eq(100), eq(handler))
        }
    }

    @Test
    fun clearProgressHandlerCallsAllConnections() {
        val sqlite: SQLite = mock()
        whenever(sqlite.openV2(any(), any(), any())).doAnswer {
            (it.arguments[2] as LongArray)[0] = DB
            0
        }
        val factory = SQLConnectionFactory("file::memory:", sqlite, databaseConfiguration, CommonThreadLocalRandom, null)
        factory.makePrimaryObject().use {
            factory.setProgressHandler(0, null)
            verify(sqlite, times(1)).progressHandler(eq(DB), eq(0), isNull())
        }
    }

    @Test
    fun setProgressHandlerDoesNotCallDestroyedConnections() {
        val sqlite: SQLite = mock()
        whenever(sqlite.openV2(any(), any(), any())).doAnswer {
            (it.arguments[2] as LongArray)[0] = DB
            0
        }
        val factory = SQLConnectionFactory("file::memory:", sqlite, databaseConfiguration, CommonThreadLocalRandom, null)
        val connection = factory.makePrimaryObject()
        factory.destroyObject(connection)
        factory.setProgressHandler(100, SQLProgressHandler { 0 })
        verify(sqlite, times(0)).progressHandler(any(), any(), any())
    }
}
