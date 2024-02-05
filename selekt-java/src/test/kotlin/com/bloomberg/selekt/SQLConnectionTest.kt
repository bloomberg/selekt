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

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val databaseConfiguration = DatabaseConfiguration(
    evictionDelayMillis = 5_000L,
    maxConnectionPoolSize = 1,
    maxSqlCacheSize = 5,
    timeBetweenEvictionRunsMillis = 5_000L
)

internal class SQLConnectionTest {
    @Mock lateinit var sqlite: SQLite

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(sqlite.openV2(any(), any(), any())).thenAnswer {
            requireNotNull(it.arguments[2] as? LongArray)[0] = DB
            0
        }
    }

    @Test
    fun exceptionInConstruction() {
        whenever(sqlite.busyTimeout(any(), any())) doThrow IllegalStateException()
        assertFailsWith<IllegalStateException> {
            SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null)
        }
    }

    @Test
    fun constructionChecksNull() {
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 0L
            0
        }
        assertFailsWith<IllegalStateException> {
            SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null)
        }
    }

    @Test
    fun prepareChecksNull() {
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 0L
            0
        }
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).apply {
            assertFailsWith<IllegalStateException> {
                prepare("INSERT INTO Foo VALUES (?)")
            }
        }
    }

    @Test
    fun isAutoCommit1() {
        whenever(sqlite.getAutocommit(any())) doReturn 1
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertTrue(it.isAutoCommit)
        }
    }

    @Test
    fun isAutoCommit2() {
        whenever(sqlite.getAutocommit(any())) doReturn 2
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertTrue(it.isAutoCommit)
        }
    }

    @Test
    fun isAutoCommitFalse() {
        whenever(sqlite.getAutocommit(any())) doReturn 0
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFalse(it.isAutoCommit)
        }
    }

    @Test
    fun checkpointDefault() {
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.checkpoint()
            verify(sqlite, times(1)).walCheckpointV2(eq(DB), isNull(), eq(SQLCheckpointMode.PASSIVE()))
        }
    }

    @Test
    fun prepareChecksArgumentCount() {
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer<Any> {
            (it.arguments[2] as LongArray)[0] = 42L
            SQL_OK
        }
        whenever(sqlite.bindParameterCount(any())) doReturn 1
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFailsWith<IllegalArgumentException> {
                it.executeForChangedRowCount("SELECT * FROM 'Foo' WHERE bar=?", emptyArray<Any>())
            }
        }
    }

    @Test
    fun connectionRejectsUnrecognisedColumnType() {
        whenever(sqlite.columnType(any(), any())) doReturn -1
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFailsWith<IllegalStateException> {
                it.executeForCursorWindow("INSERT INTO Foo VALUES (42)", emptyArray<Int>(), mock())
            }
        }
    }

    @Test
    fun executeForLastInsertedRowIdChecksDone() {
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_ROW
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1L, it.executeForLastInsertedRowId("INSERT INTO Foo VALUES (42)"))
        }
    }

    @Test
    fun executeForLastInsertedRowIdChecksChanges() {
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_DONE
        whenever(sqlite.changes(any())) doReturn 0
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1L, it.executeForLastInsertedRowId("INSERT INTO Foo VALUES (42)"))
        }
    }

    @Test
    fun executeForChangedRowCountChecksDone() {
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_ROW
        whenever(sqlite.changes(any())) doReturn 0
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1, it.executeForChangedRowCount("INSERT INTO Foo VALUES (42)"))
        }
    }

    @Test
    fun executeForInt() {
        val value = 7
        val statement = 43L
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = statement
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_ROW
        whenever(sqlite.columnInt(any(), any())) doReturn value
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(value, it.executeForInt("SELECT * FROM Foo LIMIT 1"))
        }
        verify(sqlite, times(1)).columnInt(eq(statement), eq(0))
    }

    @Test
    fun executeForLong() {
        val value = 7L
        val statement = 43L
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = statement
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_ROW
        whenever(sqlite.columnInt64(any(), any())) doReturn value
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(value, it.executeForLong("SELECT * FROM Foo LIMIT 1"))
        }
        verify(sqlite, times(1)).columnInt64(eq(statement), eq(0))
    }

    @Test
    fun executeForString() {
        val text = "hello"
        val statement = 43L
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = statement
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_ROW
        whenever(sqlite.columnText(any(), any())) doReturn text
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(text, it.executeForString("SELECT * FROM Foo LIMIT 1"))
        }
        verify(sqlite, times(1)).columnText(eq(statement), eq(0))
    }

    @Test
    fun executeForBlobReadOnly() {
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.blobOpen(any(), any(), any(), any(), any(), any(), any())) doAnswer Answer {
            (it.arguments[6] as LongArray)[0] = 43L
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_ROW
        SQLConnection("file::memory:", sqlite, databaseConfiguration, SQL_OPEN_READONLY, CommonThreadLocalRandom, null).use {
            assertTrue(it.executeForBlob("main", "Foo", "bar", 42L).readOnly)
        }
    }

    @Test
    fun batchExecuteForChangedRowCountChecksDone() {
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_ROW
        whenever(sqlite.changes(any())) doReturn 0
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1, it.executeForChangedRowCount("INSERT INTO Foo VALUES (42)", sequenceOf(emptyArray<Int>())))
        }
    }

    @Test
    fun connectionChecksWindowAllocation() {
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_ROW
        whenever(sqlite.columnCount(any())) doReturn 1
        whenever(sqlite.columnType(any(), any())) doReturn -1
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFailsWith<IllegalStateException>("Failed to allocate a window row.") {
                it.executeForCursorWindow("SELECT * FROM Foo", emptyArray<Int>(), mock())
            }
        }
    }

    @Test
    fun connectionChecksSqlColumnType() {
        whenever(sqlite.openV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer Answer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(sqlite.step(any())) doReturn SQL_ROW
        whenever(sqlite.columnCount(any())) doReturn 1
        whenever(sqlite.columnType(any(), any())) doReturn -1
        val cursorWindow = mock<ICursorWindow>().apply {
            whenever(allocateRow()) doReturn true
        }
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFailsWith<IllegalStateException>("Unrecognised column type for column 0.") {
                it.executeForCursorWindow("SELECT * FROM Foo", emptyArray<Int>(), cursorWindow)
            }
        }
    }

    @Test
    fun releaseMemory() {
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.releaseMemory()
            verify(sqlite, times(1)).databaseReleaseMemory(any())
        }
    }

    private companion object {
        const val DB = 1L
    }
}
