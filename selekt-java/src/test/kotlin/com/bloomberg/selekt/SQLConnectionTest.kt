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

import java.util.stream.Stream
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
import org.mockito.kotlin.argumentCaptor
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.kotlin.never

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
    fun exceptionInConstruction(): Unit = sqlite.run {
        whenever(busyTimeout(any(), any())) doThrow IllegalStateException()
        assertFailsWith<IllegalStateException> {
            SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null)
        }
    }

    @Test
    fun constructionChecksNull(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 0L
            0
        }
        assertFailsWith<IllegalStateException> {
            SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null)
        }
    }

    @Test
    fun prepareChecksNull(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 0L
            0
        }
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).apply {
            assertFailsWith<IllegalStateException> {
                prepare("INSERT INTO Foo VALUES (?)")
            }
        }
    }

    @Test
    fun isAutoCommit1(): Unit = sqlite.run {
        whenever(getAutocommit(any())) doReturn 1
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertTrue(it.isAutoCommit)
        }
    }

    @Test
    fun isAutoCommit2(): Unit = sqlite.run {
        whenever(getAutocommit(any())) doReturn 2
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertTrue(it.isAutoCommit)
        }
    }

    @Test
    fun isAutoCommitFalse(): Unit = sqlite.run {
        whenever(getAutocommit(any())) doReturn 0
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFalse(it.isAutoCommit)
        }
    }

    @Test
    fun interrupt(): Unit = sqlite.run {
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.interrupt()
            verify(this@run, times(1)).interrupt(eq(DB))
        }
    }

    @Test
    fun isInterruptedFalse(): Unit = sqlite.run {
        whenever(isInterrupted(any())) doReturn false
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFalse(it.isInterrupted)
        }
    }

    @Test
    fun isInterruptedTrue(): Unit = sqlite.run {
        whenever(isInterrupted(any())) doReturn true
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertTrue(it.isInterrupted)
        }
    }

    @Test
    fun setProgressHandler(): Unit = sqlite.run {
        val handler = SQLProgressHandler { 0 }
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.setProgressHandler(100, handler)
            verify(this@run, times(1)).progressHandler(eq(DB), eq(100), eq(handler))
        }
    }

    @Test
    fun clearProgressHandler(): Unit = sqlite.run {
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.setProgressHandler(0, null)
            verify(this@run, times(1)).progressHandler(eq(DB), eq(0), isNull())
        }
    }

    @Test
    fun closeResetsProgressHandler(): Unit = sqlite.run {
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).close()
        verify(this@run, times(1)).progressHandler(eq(DB), eq(0), isNull())
    }

    @Test
    fun checkpointDefault(): Unit = sqlite.run {
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.checkpoint()
            verify(this@run, times(1)).walCheckpointV2(eq(DB), isNull(), eq(SQLCheckpointMode.PASSIVE()))
        }
    }

    @Test
    fun prepareChecksArgumentCount(): Unit = sqlite.run {
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            SQL_OK
        }
        whenever(bindParameterCount(any())) doReturn 1
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFailsWith<IllegalArgumentException> {
                it.executeForChangedRowCount("SELECT * FROM 'Foo' WHERE bar=?", emptyArray<Any>())
            }
        }
    }

    @Test
    fun connectionRejectsUnrecognisedColumnType(): Unit = sqlite.run {
        whenever(columnType(any(), any())) doReturn -1
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFailsWith<IllegalStateException> {
                it.executeForCursorWindow("INSERT INTO Foo VALUES (42)", emptyArray<Int>(), mock())
            }
        }
    }

    @Test
    fun executeForLastInsertedRowIdChecksDone(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1L, it.executeForLastInsertedRowId("INSERT INTO Foo VALUES (42)"))
        }
    }

    @Test
    fun executeForLastInsertedRowIdChecksChanges(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_DONE
        whenever(changes(any())) doReturn 0
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1L, it.executeForLastInsertedRowId("INSERT INTO Foo VALUES (42)"))
        }
    }

    @Test
    fun executeForChangedRowCountChecksDone(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(changes(any())) doReturn 0
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1, it.executeForChangedRowCount("INSERT INTO Foo VALUES (42)"))
        }
    }

    @Test
    fun executeForInt(): Unit = sqlite.run {
        val value = 7
        val statement = 43L
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = statement
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(columnInt(any(), any())) doReturn value
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(value, it.executeForInt("SELECT * FROM Foo LIMIT 1"))
        }
        verify(this, times(1)).columnInt(eq(statement), eq(0))
    }

    @Test
    fun executeForLong(): Unit = sqlite.run {
        val value = 7L
        val statement = 43L
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = statement
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(columnInt64(any(), any())) doReturn value
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(value, it.executeForLong("SELECT * FROM Foo LIMIT 1"))
        }
        verify(this, times(1)).columnInt64(eq(statement), eq(0))
    }

    @Test
    fun executeForString(): Unit = sqlite.run {
        val text = "hello"
        val statement = 43L
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = statement
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(columnText(any(), any())) doReturn text
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(text, it.executeForString("SELECT * FROM Foo LIMIT 1"))
        }
        verify(this, times(1)).columnText(eq(statement), eq(0))
    }

    @Test
    fun executeForBlobReadOnly(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(blobOpen(any(), any(), any(), any(), any(), any(), any())) doAnswer {
            (it.arguments[6] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        SQLConnection("file::memory:", this, databaseConfiguration, SQL_OPEN_READONLY, CommonThreadLocalRandom, null).use {
            assertTrue(it.executeForBlob("main", "Foo", "bar", 42L).readOnly)
        }
    }

    @Test
    fun batchExecuteForChangedRowCountSequenceChecksDone(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(changes(any())) doReturn 0
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1, it.executeBatchForChangedRowCount("INSERT INTO Foo VALUES (42)", sequenceOf(emptyArray())))
        }
    }

    @Test
    fun batchExecuteForChangedRowCountEmptyArrayChecksDone(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(changes(any())) doReturn 0
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(0, it.executeBatchForChangedRowCount("INSERT INTO Foo VALUES (42)", emptyArray()))
        }
    }

    @Test
    fun batchExecuteForChangedRowCountWithRangeChecksDone(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(bindParameterCount(any())) doReturn 1
        whenever(step(any())) doReturn SQL_ROW
        whenever(changes(any())) doReturn 0
        val bindArgs = (1..4).map { i -> arrayOf<Any?>(i) }.toTypedArray()
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1, it.executeBatchForChangedRowCount("INSERT INTO Foo VALUES (?)", bindArgs, 1, 3))
        }
    }

    @Test
    fun batchExecuteForChangedRowCountWithRangeSuccess(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(bindParameterCount(any())) doReturn 1
        whenever(step(any())) doReturn SQL_DONE
        whenever(totalChanges(any())) doReturn 10 doReturn 12
        val bindArgs = (1..4).map { i -> arrayOf<Any?>(i) }.toTypedArray()
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(2, it.executeBatchForChangedRowCount(
                "INSERT INTO Foo VALUES (?)",
                bindArgs,
                1,
                3
            ))
        }
    }

    @Test
    fun batchExecuteForChangedRowCountIterableChecksDone(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(changes(any())) doReturn 0
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(-1, it.executeBatchForChangedRowCount("INSERT INTO Foo VALUES (42)", listOf(emptyArray())))
        }
    }

    @Test
    fun batchExecuteForChangedRowCountStreamChecksDone(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(changes(any())) doReturn 0
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertEquals(
                -1,
                it.executeBatchForChangedRowCount(
                    "INSERT INTO Foo VALUES (42)",
                    Stream.of(emptyArray<Any?>())
                )
            )
        }
    }

    @Test
    fun connectionChecksWindowAllocation(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(columnCount(any())) doReturn 1
        whenever(columnType(any(), any())) doReturn -1
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFailsWith<IllegalStateException>("Failed to allocate a window row.") {
                it.executeForCursorWindow("SELECT * FROM Foo", emptyArray<Int>(), mock())
            }
        }
    }

    @Test
    fun connectionChecksSqlColumnType(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(step(any())) doReturn SQL_ROW
        whenever(columnCount(any())) doReturn 1
        whenever(columnType(any(), any())) doReturn -1
        val cursorWindow = mock<ICursorWindow> {
            whenever(it.allocateRow()) doReturn true
        }
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFailsWith<IllegalStateException>("Unrecognised column type for column 0.") {
                it.executeForCursorWindow("SELECT * FROM Foo", emptyArray<Int>(), cursorWindow)
            }
        }
    }

    @Test
    fun releaseMemory(): Unit = sqlite.run {
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.releaseMemory()
            verify(this@run, times(1)).databaseReleaseMemory(any())
        }
    }

    @Test
    fun setTransactionListenerRegistersHooks(): Unit = sqlite.run {
        val listener = mock<SQLTransactionListener>()
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.setTransactionListener(listener)
            verify(this@run, times(1)).commitHook(eq(DB), eq(true), any())
        }
    }

    @Test
    fun setTransactionListenerToNullUnregistersHooks(): Unit = sqlite.run {
        val listener = mock<SQLTransactionListener>()
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.setTransactionListener(listener)
            it.setTransactionListener(null)
            verify(this@run, times(1)).commitHook(eq(DB), eq(true), any())
            verify(this@run, times(1)).commitHook(eq(DB), eq(false), isNull())
        }
    }

    @Test
    fun setTransactionListenerReplacingListenerStillCallsHook(): Unit = sqlite.run {
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.setTransactionListener(mock<SQLTransactionListener>())
            it.setTransactionListener(mock<SQLTransactionListener>())
            verify(this@run, times(2)).commitHook(eq(DB), eq(true), any())
            verify(this@run, never()).commitHook(eq(DB), eq(false), any())
        }
    }

    @Test
    fun closeUnregistersHooksWhenListenerSet(): Unit = sqlite.run {
        val listener = mock<SQLTransactionListener>()
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.setTransactionListener(listener)
        }
        verify(this, times(1)).commitHook(eq(DB), eq(false), isNull())
    }

    @Test
    fun closeDoesNotUnregisterHooksWhenNoListenerSet(): Unit = sqlite.run {
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {}
        verify(this, never()).commitHook(any(), eq(false), any())
    }

    @Test
    fun nativeCommitListenerDelegatesToCommitListener(): Unit = sqlite.run {
        val commitListenerCaptor = argumentCaptor<SQLCommitListener>()
        val listener = mock<SQLTransactionListener>()
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.setTransactionListener(listener)
            verify(this@run).commitHook(eq(DB), eq(true), commitListenerCaptor.capture())
            val result = commitListenerCaptor.firstValue.onCommit()
            verify(listener, times(1)).onCommit()
            assertEquals(0, result)
        }
    }

    @Test
    fun nativeCommitListenerDelegatesToRollbackListener(): Unit = sqlite.run {
        val commitListenerCaptor = argumentCaptor<SQLCommitListener>()
        val listener = mock<SQLTransactionListener>()
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.setTransactionListener(listener)
            verify(this@run).commitHook(eq(DB), eq(true), commitListenerCaptor.capture())
            commitListenerCaptor.firstValue.onRollback()
            verify(listener, times(1)).onRollback()
        }
    }

    @Test
    fun nativeCommitListenerHandlesNullCommitListener(): Unit = sqlite.run {
        val commitListenerCaptor = argumentCaptor<SQLCommitListener>()
        val listener = mock<SQLTransactionListener>()
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.setTransactionListener(listener)
            verify(this@run).commitHook(eq(DB), eq(true), commitListenerCaptor.capture())
            it.setTransactionListener(null)
            val result = commitListenerCaptor.firstValue.onCommit()
            commitListenerCaptor.firstValue.onRollback()
            assertEquals(0, result)
            verify(listener, never()).onCommit()
            verify(listener, never()).onRollback()
        }
    }

    @Test
    fun databaseConfig(): Unit = sqlite.run {
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.databaseConfig(1010, 1)
            verify(this@run, times(1)).databaseConfig(eq(DB), eq(1010), eq(1))
        }
    }

    @Test
    fun executeForForwardCursorStreamsRows(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        var stepCount = 0
        whenever(step(any())) doAnswer { if (stepCount++ < 2) SQL_ROW else SQL_DONE }
        whenever(columnCount(any())) doReturn 1
        whenever(columnType(any(), any())) doReturn ColumnType.INTEGER.sqlDataType
        whenever(columnName(any(), any())) doReturn "id"
        whenever(columnInt64(any(), any())) doReturn 99L
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use { conn ->
            val cursor = conn.executeForForwardCursor("SELECT * FROM Foo", emptyArray())
            assertTrue(cursor.moveToNext())
            assertEquals(99L, cursor.getLong(0))
            assertTrue(cursor.moveToNext())
            assertFalse(cursor.moveToNext())
            cursor.close()
        }
    }

    @Test
    fun executeForForwardCursorReleasesStatementOnClose(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(columnCount(any())) doReturn 1
        whenever(columnName(any(), any())) doReturn "id"
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use { conn ->
            val cursor = conn.executeForForwardCursor("SELECT * FROM Foo", emptyArray())
            cursor.close()
            verify(this@run, times(1)).resetAndClearBindings(eq(43L))
        }
    }

    @Test
    fun executeForForwardCursorInvokesAdditionalOnClose(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(columnCount(any())) doReturn 1
        whenever(columnName(any(), any())) doReturn "id"
        var additionalClosed = false
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use { conn ->
            val cursor = conn.executeForForwardCursor("SELECT * FROM Foo", emptyArray()) {
                additionalClosed = true
            }
            assertFalse(additionalClosed)
            cursor.close()
            assertTrue(additionalClosed)
        }
    }

    @Test
    fun executeForForwardCursorCloseIsIdempotent(): Unit = sqlite.run {
        whenever(openV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 42L
            0
        }
        whenever(prepareV2(any(), any(), any())) doAnswer {
            (it.arguments[2] as LongArray)[0] = 43L
            0
        }
        whenever(columnCount(any())) doReturn 1
        whenever(columnName(any(), any())) doReturn "id"
        var closeCount = 0
        SQLConnection("file::memory:", this, databaseConfiguration, 0, CommonThreadLocalRandom, null).use { conn ->
            val cursor = conn.executeForForwardCursor("SELECT * FROM Foo", emptyArray()) {
                closeCount++
            }
            cursor.close()
            cursor.close()
            assertEquals(1, closeCount)
        }
    }

    private companion object {
        const val DB = 1L
    }
}
