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
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private const val DB_POINTER = 0xDEADB00BL
private const val STMT_POINTER = 0xCAFEBABEL
private const val BLOB_POINTER = 0xFEEDF00DL

private val DATABASE_HANDLE = DatabaseHandle(DB_POINTER)
private val STATEMENT_HANDLE = StatementHandle(STMT_POINTER)
private val BLOB_HANDLE = BlobHandle(BLOB_POINTER)

internal class SQLiteTest {
    private val externalSqlite = mock<IExternalSQLite> {
        on { errorMessage(any<Long>()) }.thenReturn("")
        on { errorMessage(any<DatabaseHandle>()) }.thenReturn("")
    }
    private val sqlite = SQLite(externalSqlite)

    @Test
    fun `newDatabaseHandle delegates to IExternalSQLite`() {
        whenever(externalSqlite.newDatabaseHandle(DB_POINTER)) doReturn DATABASE_HANDLE
        assertEquals(DATABASE_HANDLE, sqlite.newDatabaseHandle(DB_POINTER))
    }

    @Test
    fun `newStatementHandle delegates to IExternalSQLite`() {
        whenever(externalSqlite.newStatementHandle(STMT_POINTER)) doReturn STATEMENT_HANDLE
        assertEquals(STATEMENT_HANDLE, sqlite.newStatementHandle(STMT_POINTER))
    }

    @Test
    fun `newBlobHandle delegates to IExternalSQLite`() {
        whenever(externalSqlite.newBlobHandle(BLOB_POINTER)) doReturn BLOB_HANDLE
        assertEquals(BLOB_HANDLE, sqlite.newBlobHandle(BLOB_POINTER))
    }

    @Test
    fun `bindBlob with StatementHandle succeeds`() {
        val blob = byteArrayOf(1, 2, 3)
        whenever(externalSqlite.bindBlob(STATEMENT_HANDLE, 1, blob, 3)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.bindBlob(STATEMENT_HANDLE, 1, blob))
        verify(externalSqlite).bindBlob(STATEMENT_HANDLE, 1, blob, 3)
    }

    @Test
    fun `bindBlob with StatementHandle throws on error`() {
        val blob = byteArrayOf(1)
        whenever(externalSqlite.bindBlob(any<StatementHandle>(), any(), any(), any())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.bindBlob(STATEMENT_HANDLE, 1, blob) }
    }

    @Test
    fun `bindDouble with StatementHandle succeeds`() {
        whenever(externalSqlite.bindDouble(STATEMENT_HANDLE, 1, 3.14)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.bindDouble(STATEMENT_HANDLE, 1, 3.14))
    }

    @Test
    fun `bindInt with StatementHandle succeeds`() {
        whenever(externalSqlite.bindInt(STATEMENT_HANDLE, 1, 42)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.bindInt(STATEMENT_HANDLE, 1, 42))
    }

    @Test
    fun `bindInt64 with StatementHandle succeeds`() {
        whenever(externalSqlite.bindInt64(STATEMENT_HANDLE, 1, Long.MAX_VALUE)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.bindInt64(STATEMENT_HANDLE, 1, Long.MAX_VALUE))
    }

    @Test
    fun `bindNull with StatementHandle succeeds`() {
        whenever(externalSqlite.bindNull(STATEMENT_HANDLE, 1)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.bindNull(STATEMENT_HANDLE, 1))
    }

    @Test
    fun `bindParameterCount with StatementHandle delegates`() {
        whenever(externalSqlite.bindParameterCount(STATEMENT_HANDLE)) doReturn 3
        assertEquals(3, sqlite.bindParameterCount(STATEMENT_HANDLE))
    }

    @Test
    fun `bindParameterIndex with StatementHandle delegates`() {
        whenever(externalSqlite.bindParameterIndex(STATEMENT_HANDLE, ":p")) doReturn 2
        assertEquals(2, sqlite.bindParameterIndex(STATEMENT_HANDLE, ":p"))
    }

    @Test
    fun `bindText with StatementHandle succeeds`() {
        whenever(externalSqlite.bindText(STATEMENT_HANDLE, 1, "hello")) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.bindText(STATEMENT_HANDLE, 1, "hello"))
    }

    @Test
    fun `bindZeroBlob with StatementHandle succeeds`() {
        whenever(externalSqlite.bindZeroBlob(STATEMENT_HANDLE, 1, 8)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.bindZeroBlob(STATEMENT_HANDLE, 1, 8))
    }

    @Test
    fun `bindRow with StatementHandle and array succeeds`() {
        whenever(externalSqlite.bindRow(eq(STATEMENT_HANDLE), any())) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.bindRow(STATEMENT_HANDLE, arrayOf("x")))
    }

    @Test
    fun `bindRow with StatementHandle and ParameterRow succeeds`() {
        val row = ParameterRow(1).apply { setObject(0, "text") }
        whenever(externalSqlite.bindRowTyped(
            eq(STATEMENT_HANDLE), any(), any(), any(), any(), any(), any()
        )) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.bindRow(STATEMENT_HANDLE, row))
    }

    @Test
    fun `blobBytes with BlobHandle delegates`() {
        whenever(externalSqlite.blobBytes(BLOB_HANDLE)) doReturn 512
        assertEquals(512, sqlite.blobBytes(BLOB_HANDLE))
    }

    @Test
    fun `blobClose with BlobHandle succeeds`() {
        whenever(externalSqlite.blobClose(BLOB_HANDLE)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.blobClose(BLOB_HANDLE))
    }

    @Test
    fun `blobOpen with DatabaseHandle succeeds`() {
        val holder = LongArray(1)
        whenever(externalSqlite.blobOpen(DATABASE_HANDLE, "main", "t", "c", 1L, 0, holder)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.blobOpen(DATABASE_HANDLE, "main", "t", "c", 1L, 0, holder))
    }

    @Test
    fun `blobOpen with DatabaseHandle throws on error`() {
        val holder = LongArray(1)
        whenever(externalSqlite.blobOpen(
            any<DatabaseHandle>(), any(), any(), any(), any(), any(), any()
        )) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.blobOpen(DATABASE_HANDLE, "main", "t", "c", 1L, 0, holder) }
    }

    @Test
    fun `blobRead with BlobHandle succeeds`() {
        val dest = ByteArray(4)
        whenever(externalSqlite.blobRead(BLOB_HANDLE, 0, dest, 0, 4)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.blobRead(BLOB_HANDLE, 0, dest, 0, 4))
        verify(externalSqlite).blobRead(BLOB_HANDLE, 0, dest, 0, 4)
    }

    @Test
    fun `blobReopen with BlobHandle succeeds`() {
        whenever(externalSqlite.blobReopen(BLOB_HANDLE, 2L)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.blobReopen(BLOB_HANDLE, 2L))
    }

    @Test
    fun `blobWrite with BlobHandle succeeds`() {
        val src = byteArrayOf(9, 8, 7)
        whenever(externalSqlite.blobWrite(BLOB_HANDLE, 0, src, 0, 3)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.blobWrite(BLOB_HANDLE, 0, src, 0, 3))
        verify(externalSqlite).blobWrite(eq(BLOB_HANDLE), eq(0), same(src), eq(0), eq(3))
    }

    @Test
    fun `busyTimeout with DatabaseHandle succeeds`() {
        whenever(externalSqlite.busyTimeout(DATABASE_HANDLE, 5000)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.busyTimeout(DATABASE_HANDLE, 5000))
    }

    @Test
    fun `busyTimeout with DatabaseHandle throws on error`() {
        whenever(externalSqlite.busyTimeout(any<DatabaseHandle>(), any())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.busyTimeout(DATABASE_HANDLE, 5000) }
    }

    @Test
    fun `changes with DatabaseHandle delegates`() {
        whenever(externalSqlite.changes(DATABASE_HANDLE)) doReturn 7
        assertEquals(7, sqlite.changes(DATABASE_HANDLE))
    }

    @Test
    fun `clearBindings with StatementHandle succeeds`() {
        whenever(externalSqlite.clearBindings(STATEMENT_HANDLE)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.clearBindings(STATEMENT_HANDLE))
    }

    @Test
    fun `closeV2 with DatabaseHandle succeeds`() {
        whenever(externalSqlite.closeV2(DATABASE_HANDLE)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.closeV2(DATABASE_HANDLE))
    }

    @Test
    fun `closeV2 with DatabaseHandle throws on error`() {
        whenever(externalSqlite.closeV2(any<DatabaseHandle>())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.closeV2(DATABASE_HANDLE) }
    }

    @Test
    fun `columnBlob with StatementHandle delegates`() {
        whenever(externalSqlite.columnBlob(STATEMENT_HANDLE, 0)) doReturn null
        assertNull(sqlite.columnBlob(STATEMENT_HANDLE, 0))
    }

    @Test
    fun `columnCount with StatementHandle delegates`() {
        whenever(externalSqlite.columnCount(STATEMENT_HANDLE)) doReturn 4
        assertEquals(4, sqlite.columnCount(STATEMENT_HANDLE))
    }

    @Test
    fun `columnDouble with StatementHandle delegates`() {
        whenever(externalSqlite.columnDouble(STATEMENT_HANDLE, 0)) doReturn 2.71
        assertEquals(2.71, sqlite.columnDouble(STATEMENT_HANDLE, 0))
    }

    @Test
    fun `columnInt with StatementHandle delegates`() {
        whenever(externalSqlite.columnInt(STATEMENT_HANDLE, 0)) doReturn 99
        assertEquals(99, sqlite.columnInt(STATEMENT_HANDLE, 0))
    }

    @Test
    fun `columnInt64 with StatementHandle delegates`() {
        whenever(externalSqlite.columnInt64(STATEMENT_HANDLE, 0)) doReturn Long.MIN_VALUE
        assertEquals(Long.MIN_VALUE, sqlite.columnInt64(STATEMENT_HANDLE, 0))
    }

    @Test
    fun `columnName with StatementHandle delegates`() {
        whenever(externalSqlite.columnName(STATEMENT_HANDLE, 0)) doReturn "id"
        assertEquals("id", sqlite.columnName(STATEMENT_HANDLE, 0))
    }

    @Test
    fun `columnText with StatementHandle delegates`() {
        whenever(externalSqlite.columnText(STATEMENT_HANDLE, 0)) doReturn "hello"
        assertEquals("hello", sqlite.columnText(STATEMENT_HANDLE, 0))
    }

    @Test
    fun `columnType with StatementHandle delegates`() {
        whenever(externalSqlite.columnType(STATEMENT_HANDLE, 0)) doReturn 1
        assertEquals(1, sqlite.columnType(STATEMENT_HANDLE, 0))
    }

    @Test
    fun `commitHook with DatabaseHandle succeeds`() {
        val listener = mock<SQLCommitListener>()
        whenever(externalSqlite.commitHook(DATABASE_HANDLE, true, listener)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.commitHook(DATABASE_HANDLE, true, listener))
    }

    @Test
    fun `commitHook with DatabaseHandle throws on error`() {
        val listener = mock<SQLCommitListener>()
        whenever(externalSqlite.commitHook(any<DatabaseHandle>(), any(), any<SQLCommitListener>())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.commitHook(DATABASE_HANDLE, true, listener) }
    }

    @Test
    fun `databaseConfig with DatabaseHandle succeeds`() {
        whenever(externalSqlite.databaseConfig(DATABASE_HANDLE, 1001, 1)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.databaseConfig(DATABASE_HANDLE, 1001, 1))
    }

    @Test
    fun `databaseHandle with StatementHandle delegates`() {
        whenever(externalSqlite.databaseHandle(STATEMENT_HANDLE)) doReturn DB_POINTER
        assertEquals(DB_POINTER, sqlite.databaseHandle(STATEMENT_HANDLE))
    }

    @Test
    fun `databaseReleaseMemory with DatabaseHandle delegates`() {
        whenever(externalSqlite.databaseReleaseMemory(DATABASE_HANDLE)) doReturn 0
        assertEquals(0, sqlite.databaseReleaseMemory(DATABASE_HANDLE))
    }

    @Test
    fun `errorCode with DatabaseHandle delegates`() {
        whenever(externalSqlite.errorCode(DATABASE_HANDLE)) doReturn SQL_ERROR
        assertEquals(SQL_ERROR, sqlite.errorCode(DATABASE_HANDLE))
    }

    @Test
    fun `errorMessage with DatabaseHandle delegates`() {
        whenever(externalSqlite.errorMessage(DATABASE_HANDLE)) doReturn "msg"
        assertEquals("msg", sqlite.errorMessage(DATABASE_HANDLE))
    }

    @Test
    fun `exec with DatabaseHandle succeeds`() {
        whenever(externalSqlite.exec(DATABASE_HANDLE, "BEGIN")) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.exec(DATABASE_HANDLE, "BEGIN"))
    }

    @Test
    fun `exec with DatabaseHandle throws on error`() {
        whenever(externalSqlite.exec(any<DatabaseHandle>(), any())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.exec(DATABASE_HANDLE, "BEGIN") }
    }

    @Test
    fun `expandedSql with StatementHandle delegates`() {
        whenever(externalSqlite.expandedSql(STATEMENT_HANDLE)) doReturn "SELECT 1"
        assertEquals("SELECT 1", sqlite.expandedSql(STATEMENT_HANDLE))
    }

    @Test
    fun `extendedErrorCode with DatabaseHandle delegates`() {
        whenever(externalSqlite.extendedErrorCode(DATABASE_HANDLE)) doReturn 267
        assertEquals(267, sqlite.extendedErrorCode(DATABASE_HANDLE))
    }

    @Test
    fun `extendedResultCodes with DatabaseHandle delegates`() {
        whenever(externalSqlite.extendedResultCodes(DATABASE_HANDLE, 1)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.extendedResultCodes(DATABASE_HANDLE, 1))
    }

    @Test
    fun `finalize with StatementHandle succeeds`() {
        whenever(externalSqlite.finalize(STATEMENT_HANDLE)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.finalize(STATEMENT_HANDLE))
    }

    @Test
    fun `finalize with StatementHandle throws on error`() {
        whenever(externalSqlite.finalize(any<StatementHandle>())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.finalize(STATEMENT_HANDLE) }
    }

    @Test
    fun `getAutocommit with DatabaseHandle delegates`() {
        whenever(externalSqlite.getAutocommit(DATABASE_HANDLE)) doReturn 1
        assertEquals(1, sqlite.getAutocommit(DATABASE_HANDLE))
    }

    @Test
    fun `interrupt with DatabaseHandle delegates`() {
        sqlite.interrupt(DATABASE_HANDLE)
        verify(externalSqlite).interrupt(DATABASE_HANDLE)
    }

    @Test
    fun `isInterrupted with DatabaseHandle delegates`() {
        whenever(externalSqlite.isInterrupted(DATABASE_HANDLE)) doReturn 1
        assertEquals(true, sqlite.isInterrupted(DATABASE_HANDLE))
        whenever(externalSqlite.isInterrupted(DATABASE_HANDLE)) doReturn 0
        assertEquals(false, sqlite.isInterrupted(DATABASE_HANDLE))
    }

    @Test
    fun `keyConventionally with DatabaseHandle succeeds`() {
        val key = byteArrayOf(1, 2, 3)
        whenever(externalSqlite.keyConventionally(DATABASE_HANDLE, key, 3)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.keyConventionally(DATABASE_HANDLE, key))
    }

    @Test
    fun `lastInsertRowId with DatabaseHandle delegates`() {
        whenever(externalSqlite.lastInsertRowId(DATABASE_HANDLE)) doReturn 99L
        assertEquals(99L, sqlite.lastInsertRowId(DATABASE_HANDLE))
    }

    @Test
    fun `prepareV2 with DatabaseHandle succeeds`() {
        val holder = LongArray(1)
        whenever(externalSqlite.prepareV2(DATABASE_HANDLE, "SELECT 1", 8, holder)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.prepareV2(DATABASE_HANDLE, "SELECT 1", holder))
    }

    @Test
    fun `prepareV2 with DatabaseHandle throws on error`() {
        val holder = LongArray(1)
        whenever(externalSqlite.prepareV2(any<DatabaseHandle>(), any(), any(), any())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.prepareV2(DATABASE_HANDLE, "SELECT 1", holder) }
    }

    @Test
    fun `progressHandler with DatabaseHandle delegates`() {
        val handler = mock<SQLProgressHandler>()
        sqlite.progressHandler(DATABASE_HANDLE, 100, handler)
        verify(externalSqlite).progressHandler(DATABASE_HANDLE, 100, handler)
    }

    @Test
    fun `reset with StatementHandle succeeds`() {
        whenever(externalSqlite.reset(STATEMENT_HANDLE)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.reset(STATEMENT_HANDLE))
    }

    @Test
    fun `resetAndClearBindings with StatementHandle succeeds`() {
        whenever(externalSqlite.resetAndClearBindings(STATEMENT_HANDLE)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.resetAndClearBindings(STATEMENT_HANDLE))
    }

    @Test
    fun `resetAndClearBindings with StatementHandle throws on error`() {
        whenever(externalSqlite.resetAndClearBindings(any<StatementHandle>())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.resetAndClearBindings(STATEMENT_HANDLE) }
    }

    @Test
    fun `sql with StatementHandle delegates`() {
        whenever(externalSqlite.sql(STATEMENT_HANDLE)) doReturn "SELECT ?"
        assertEquals("SELECT ?", sqlite.sql(STATEMENT_HANDLE))
    }

    @Test
    fun `statementBusy with StatementHandle delegates`() {
        whenever(externalSqlite.statementBusy(STATEMENT_HANDLE)) doReturn 1
        assertEquals(1, sqlite.statementBusy(STATEMENT_HANDLE))
    }

    @Test
    fun `statementReadOnly with StatementHandle delegates`() {
        whenever(externalSqlite.statementReadOnly(STATEMENT_HANDLE)) doReturn 1
        assertEquals(1, sqlite.statementReadOnly(STATEMENT_HANDLE))
    }

    @Test
    fun `statementStatus with StatementHandle delegates`() {
        whenever(externalSqlite.statementStatus(STATEMENT_HANDLE, 1, false)) doReturn 5
        assertEquals(5, sqlite.statementStatus(STATEMENT_HANDLE, 1, false))
    }

    @Test
    fun `step with StatementHandle returns SQL_DONE`() {
        whenever(externalSqlite.step(STATEMENT_HANDLE)) doReturn SQL_DONE
        assertEquals(SQL_DONE, sqlite.step(STATEMENT_HANDLE))
    }

    @Test
    fun `step with StatementHandle returns SQL_ROW`() {
        whenever(externalSqlite.step(STATEMENT_HANDLE)) doReturn SQL_ROW
        assertEquals(SQL_ROW, sqlite.step(STATEMENT_HANDLE))
    }

    @Test
    fun `step with StatementHandle throws on error`() {
        whenever(externalSqlite.step(any<StatementHandle>())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.step(STATEMENT_HANDLE) }
    }

    @Test
    fun `stepWithoutThrowing with StatementHandle delegates`() {
        whenever(externalSqlite.step(STATEMENT_HANDLE)) doReturn SQL_DONE
        assertEquals(SQL_DONE, sqlite.stepWithoutThrowing(STATEMENT_HANDLE))
    }

    @Test
    fun `throwSQLException with DatabaseHandle throws`() {
        whenever(externalSqlite.errorCode(DATABASE_HANDLE)) doReturn SQL_ERROR
        whenever(externalSqlite.extendedErrorCode(DATABASE_HANDLE)) doReturn SQL_ERROR
        whenever(externalSqlite.errorMessage(DATABASE_HANDLE)) doReturn "test error"
        assertFailsWith<SQLException> { sqlite.throwSQLException(DATABASE_HANDLE) }
    }

    @Test
    fun `totalChanges with DatabaseHandle delegates`() {
        whenever(externalSqlite.totalChanges(DATABASE_HANDLE)) doReturn 10
        assertEquals(10, sqlite.totalChanges(DATABASE_HANDLE))
    }

    @Test
    fun `traceV2 with DatabaseHandle delegates`() {
        sqlite.traceV2(DATABASE_HANDLE, 1)
        verify(externalSqlite).traceV2(DATABASE_HANDLE, 1)
    }

    @Test
    fun `walAutoCheckpoint with DatabaseHandle succeeds`() {
        whenever(externalSqlite.walAutoCheckpoint(DATABASE_HANDLE, 1000)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.walAutoCheckpoint(DATABASE_HANDLE, 1000))
    }

    @Test
    fun `walAutoCheckpoint with DatabaseHandle throws on error`() {
        whenever(externalSqlite.walAutoCheckpoint(any<DatabaseHandle>(), any())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.walAutoCheckpoint(DATABASE_HANDLE, 1000) }
    }

    @Test
    fun `walCheckpointV2 with DatabaseHandle succeeds`() {
        whenever(externalSqlite.walCheckpointV2(DATABASE_HANDLE, "main", 0)) doReturn SQL_OK
        assertEquals(SQL_OK, sqlite.walCheckpointV2(DATABASE_HANDLE, "main", 0))
    }

    @Test
    fun `walCheckpointV2 with DatabaseHandle throws on error`() {
        whenever(externalSqlite.walCheckpointV2(any<DatabaseHandle>(), any(), any())) doReturn SQL_ERROR
        assertFailsWith<SQLException> { sqlite.walCheckpointV2(DATABASE_HANDLE, "main", 0) }
    }
}
