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

import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class IExternalSQLiteTest {
    private val statement = 0xCAFEL
    private val statementHandle = StatementHandle(statement)
    private val db = 0xDEADL
    private val dbHandle = DatabaseHandle(db)
    private val blob = 0xBEEFL
    private val blobHandle = BlobHandle(blob)

    private val sqlite = mock<IExternalSQLite> {
        on { bindText(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
        on { bindInt(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
        on { bindInt64(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
        on { bindDouble(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
        on { bindNull(any<Long>(), any()) }.thenReturn(SQL_OK)
        on { bindBlob(any<Long>(), any(), any(), any()) }.thenReturn(SQL_OK)
        on { bindRow(any<Long>(), any()) }.thenCallRealMethod()
    }

    @Test
    fun `bindRow with string`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf("hello")))
        verify(sqlite).bindText(statement, 1, "hello")
    }

    @Test
    fun `bindRow with int`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(42)))
        verify(sqlite).bindInt(statement, 1, 42)
    }

    @Test
    fun `bindRow with null`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(null)))
        verify(sqlite).bindNull(statement, 1)
    }

    @Test
    fun `bindRow with long`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(Long.MAX_VALUE)))
        verify(sqlite).bindInt64(statement, 1, Long.MAX_VALUE)
    }

    @Test
    fun `bindRow with double`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(3.14)))
        verify(sqlite).bindDouble(statement, 1, 3.14)
    }

    @Test
    fun `bindRow with blob`() {
        val blob = byteArrayOf(1, 2, 3)
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(blob)))
        verify(sqlite).bindBlob(statement, 1, blob, 3)
    }

    @Test
    fun `bindRow with empty args`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, emptyArray()))
        verify(sqlite, never()).bindText(any<Long>(), any(), any())
        verify(sqlite, never()).bindInt(any<Long>(), any(), any())
        verify(sqlite, never()).bindNull(any<Long>(), any())
    }

    @Test
    fun `bindRow with mixed types`() {
        val blob = byteArrayOf(9)
        assertEquals(
            SQL_OK,
            sqlite.bindRow(statement, arrayOf("text", 42, null, Long.MAX_VALUE, 3.14, blob))
        )
        verify(sqlite).bindText(statement, 1, "text")
        verify(sqlite).bindInt(statement, 2, 42)
        verify(sqlite).bindNull(statement, 3)
        verify(sqlite).bindInt64(statement, 4, Long.MAX_VALUE)
        verify(sqlite).bindDouble(statement, 5, 3.14)
        verify(sqlite).bindBlob(statement, 6, blob, 1)
    }

    @Test
    fun `bindRow throws on unsupported type`() {
        assertFailsWith<IllegalArgumentException> {
            sqlite.bindRow(statement, arrayOf(Any()))
        }
    }

    @Test
    fun `bindRow stops on error`() {
        whenever(sqlite.bindInt(any<Long>(), any(), any())).thenReturn(1)
        val result = sqlite.bindRow(statement, arrayOf<Any?>("ok", 42, "never"))
        assertEquals(1, result)
        verify(sqlite).bindText(statement, 1, "ok")
        verify(sqlite).bindInt(statement, 2, 42)
        verify(sqlite, never()).bindText(eq(statement), eq(3), any<String>())
    }

    @Test
    fun `bindRow with StatementHandle delegates to long binders`() {
        val sqlite = handleAwareMock()
        val blob = byteArrayOf(7)
        assertEquals(SQL_OK, sqlite.bindRow(statementHandle, arrayOf("text", 42, blob)))
        verify(sqlite).bindText(statement, 1, "text")
        verify(sqlite).bindInt(statement, 2, 42)
        verify(sqlite).bindBlob(statement, 3, blob, 1)
    }

    @Test
    fun `bindRowTyped with long statement binds all mapped types`() {
        val sqlite = handleAwareMock()
        val blob = byteArrayOf(9)
        val tags = byteArrayOf(1, 2, 3, 4, 4, 0)
        val ints = intArrayOf(7, 0, 0, 0, 0, 0)
        val longs = longArrayOf(0, 8, 0, 0, 0, 0)
        val doubles = doubleArrayOf(0.0, 0.0, 2.5, 0.0, 0.0, 0.0)
        val objects = arrayOfNulls<Any>(6).also {
            it[3] = "txt"
            it[4] = blob
        }
        assertEquals(SQL_OK, sqlite.bindRowTyped(statement, tags, ints, longs, doubles, objects, tags.size))
        verify(sqlite).bindInt(statement, 1, 7)
        verify(sqlite).bindInt64(statement, 2, 8)
        verify(sqlite).bindDouble(statement, 3, 2.5)
        verify(sqlite).bindText(statement, 4, "txt")
        verify(sqlite).bindBlob(statement, 5, blob, 1)
        verify(sqlite).bindNull(statement, 6)
    }

    @Test
    fun `bindRowTyped with StatementHandle binds all mapped types`() {
        val sqlite = handleAwareMock()
        val blob = byteArrayOf(11)
        val tags = byteArrayOf(1, 2, 3, 4, 4, 0)
        val ints = intArrayOf(17, 0, 0, 0, 0, 0)
        val longs = longArrayOf(0, 18, 0, 0, 0, 0)
        val doubles = doubleArrayOf(0.0, 0.0, 12.5, 0.0, 0.0, 0.0)
        val objects = arrayOfNulls<Any>(6).also {
            it[3] = "typed"
            it[4] = blob
        }
        assertEquals(SQL_OK, sqlite.bindRowTyped(statementHandle, tags, ints, longs, doubles, objects, tags.size))
        verify(sqlite).bindInt(statement, 1, 17)
        verify(sqlite).bindInt64(statement, 2, 18)
        verify(sqlite).bindDouble(statement, 3, 12.5)
        verify(sqlite).bindText(statement, 4, "typed")
        verify(sqlite).bindBlob(statement, 5, blob, 1)
        verify(sqlite).bindNull(statement, 6)
    }

    @Test
    fun `newDatabaseHandle default wraps pointer`() {
        val sqlite = mock<IExternalSQLite> {
            on { newDatabaseHandle(any()) }.thenCallRealMethod()
        }
        val handle = sqlite.newDatabaseHandle(db)
        assertEquals(db, handle.pointer)
    }

    @Test
    fun `newStatementHandle default wraps pointer`() {
        val sqlite = mock<IExternalSQLite> {
            on { newStatementHandle(any()) }.thenCallRealMethod()
        }
        val handle = sqlite.newStatementHandle(statement)
        assertEquals(statement, handle.pointer)
    }

    @Test
    fun `newBlobHandle default wraps pointer`() {
        val sqlite = mock<IExternalSQLite> {
            on { newBlobHandle(any()) }.thenCallRealMethod()
        }
        val handle = sqlite.newBlobHandle(blob)
        assertEquals(blob, handle.pointer)
    }

    @Test
    fun `bindBlob with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { bindBlob(any<Long>(), any(), any(), any()) }.thenReturn(SQL_OK)
            on { bindBlob(any<StatementHandle>(), any(), any(), any()) }.thenCallRealMethod()
        }
        val blob = byteArrayOf(1, 2, 3)
        sqlite.bindBlob(statementHandle, 1, blob, blob.size)
        verify(sqlite).bindBlob(statement, 1, blob, blob.size)
    }

    @Test
    fun `bindDouble with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { bindDouble(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { bindDouble(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.bindDouble(statementHandle, 1, 2.5)
        verify(sqlite).bindDouble(statement, 1, 2.5)
    }

    @Test
    fun `bindInt with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { bindInt(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { bindInt(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.bindInt(statementHandle, 1, 42)
        verify(sqlite).bindInt(statement, 1, 42)
    }

    @Test
    fun `bindInt64 with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { bindInt64(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { bindInt64(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.bindInt64(statementHandle, 1, Long.MAX_VALUE)
        verify(sqlite).bindInt64(statement, 1, Long.MAX_VALUE)
    }

    @Test
    fun `bindNull with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { bindNull(any<Long>(), any()) }.thenReturn(SQL_OK)
            on { bindNull(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        sqlite.bindNull(statementHandle, 1)
        verify(sqlite).bindNull(statement, 1)
    }

    @Test
    fun `bindParameterCount with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { bindParameterCount(any<Long>()) }.thenReturn(3)
            on { bindParameterCount(any<StatementHandle>()) }.thenCallRealMethod()
        }
        assertEquals(3, sqlite.bindParameterCount(statementHandle))
        verify(sqlite).bindParameterCount(statement)
    }

    @Test
    fun `bindParameterIndex with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { bindParameterIndex(any<Long>(), any()) }.thenReturn(2)
            on { bindParameterIndex(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        assertEquals(2, sqlite.bindParameterIndex(statementHandle, ":v"))
        verify(sqlite).bindParameterIndex(statement, ":v")
    }

    @Test
    fun `bindText with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { bindText(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { bindText(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.bindText(statementHandle, 1, "hi")
        verify(sqlite).bindText(statement, 1, "hi")
    }

    @Test
    fun `bindZeroBlob with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { bindZeroBlob(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { bindZeroBlob(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.bindZeroBlob(statementHandle, 1, 10)
        verify(sqlite).bindZeroBlob(statement, 1, 10)
    }

    @Test
    fun `clearBindings with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { clearBindings(any<Long>()) }.thenReturn(SQL_OK)
            on { clearBindings(any<StatementHandle>()) }.thenCallRealMethod()
        }
        sqlite.clearBindings(statementHandle)
        verify(sqlite).clearBindings(statement)
    }

    @Test
    fun `blobBytes with BlobHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { blobBytes(any<Long>()) }.thenReturn(16)
            on { blobBytes(any<BlobHandle>()) }.thenCallRealMethod()
        }
        assertEquals(16, sqlite.blobBytes(blobHandle))
        verify(sqlite).blobBytes(blob)
    }

    @Test
    fun `blobClose with BlobHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { blobClose(any<Long>()) }.thenReturn(SQL_OK)
            on { blobClose(any<BlobHandle>()) }.thenCallRealMethod()
        }
        sqlite.blobClose(blobHandle)
        verify(sqlite).blobClose(blob)
    }

    @Test
    fun `blobOpen with DatabaseHandle delegates`() {
        val holder = LongArray(1)
        val sqlite = mock<IExternalSQLite> {
            on { blobOpen(any<Long>(), any(), any(), any(), any(), any(), any()) }.thenReturn(SQL_OK)
            on { blobOpen(any<DatabaseHandle>(), any(), any(), any(), any(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.blobOpen(dbHandle, "main", "t", "c", 1L, 1, holder)
        verify(sqlite).blobOpen(db, "main", "t", "c", 1L, 1, holder)
    }

    @Test
    fun `blobRead with BlobHandle delegates`() {
        val dst = ByteArray(5)
        val sqlite = mock<IExternalSQLite> {
            on { blobRead(any<Long>(), any(), any(), any(), any()) }.thenReturn(SQL_OK)
            on { blobRead(any<BlobHandle>(), any(), any(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.blobRead(blobHandle, 0, dst, 0, 5)
        verify(sqlite).blobRead(blob, 0, dst, 0, 5)
    }

    @Test
    fun `blobReopen with BlobHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { blobReopen(any<Long>(), any()) }.thenReturn(SQL_OK)
            on { blobReopen(any<BlobHandle>(), any()) }.thenCallRealMethod()
        }
        sqlite.blobReopen(blobHandle, 2L)
        verify(sqlite).blobReopen(blob, 2L)
    }

    @Test
    fun `blobWrite with BlobHandle delegates`() {
        val src = ByteArray(5)
        val sqlite = mock<IExternalSQLite> {
            on { blobWrite(any<Long>(), any(), any(), any(), any()) }.thenReturn(SQL_OK)
            on { blobWrite(any<BlobHandle>(), any(), any(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.blobWrite(blobHandle, 0, src, 0, 5)
        verify(sqlite).blobWrite(blob, 0, src, 0, 5)
    }

    @Test
    fun `busyTimeout with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { busyTimeout(any<Long>(), any()) }.thenReturn(SQL_OK)
            on { busyTimeout(any<DatabaseHandle>(), any()) }.thenCallRealMethod()
        }
        sqlite.busyTimeout(dbHandle, 5000)
        verify(sqlite).busyTimeout(db, 5000)
    }

    @Test
    fun `changes with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { changes(any<Long>()) }.thenReturn(3)
            on { changes(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        assertEquals(3, sqlite.changes(dbHandle))
        verify(sqlite).changes(db)
    }

    @Test
    fun `closeV2 with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { closeV2(any<Long>()) }.thenReturn(SQL_OK)
            on { closeV2(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        sqlite.closeV2(dbHandle)
        verify(sqlite).closeV2(db)
    }

    @Test
    fun `commitHook with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { commitHook(any<Long>(), any(), anyOrNull()) }.thenReturn(SQL_OK)
            on { commitHook(any<DatabaseHandle>(), any(), anyOrNull()) }.thenCallRealMethod()
        }
        sqlite.commitHook(dbHandle, true, null)
        verify(sqlite).commitHook(db, true, null)
    }

    @Test
    fun `databaseConfig with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { databaseConfig(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { databaseConfig(any<DatabaseHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.databaseConfig(dbHandle, 1, 1)
        verify(sqlite).databaseConfig(db, 1, 1)
    }

    @Test
    fun `databaseHandle with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { databaseHandle(any<Long>()) }.thenReturn(db)
            on { databaseHandle(any<StatementHandle>()) }.thenCallRealMethod()
        }
        assertEquals(db, sqlite.databaseHandle(statementHandle))
        verify(sqlite).databaseHandle(statement)
    }

    @Test
    fun `databaseReadOnly with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { databaseReadOnly(any<Long>(), any()) }.thenReturn(0)
            on { databaseReadOnly(any<DatabaseHandle>(), any()) }.thenCallRealMethod()
        }
        sqlite.databaseReadOnly(dbHandle, "main")
        verify(sqlite).databaseReadOnly(db, "main")
    }

    @Test
    fun `databaseReleaseMemory with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { databaseReleaseMemory(any<Long>()) }.thenReturn(0)
            on { databaseReleaseMemory(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        sqlite.databaseReleaseMemory(dbHandle)
        verify(sqlite).databaseReleaseMemory(db)
    }

    @Test
    fun `errorCode with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { errorCode(any<Long>()) }.thenReturn(SQL_OK)
            on { errorCode(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        assertEquals(SQL_OK, sqlite.errorCode(dbHandle))
        verify(sqlite).errorCode(db)
    }

    @Test
    fun `errorMessage with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { errorMessage(any<Long>()) }.thenReturn("ok")
            on { errorMessage(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        assertEquals("ok", sqlite.errorMessage(dbHandle))
        verify(sqlite).errorMessage(db)
    }

    @Test
    fun `exec with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { exec(any<Long>(), any()) }.thenReturn(SQL_OK)
            on { exec(any<DatabaseHandle>(), any()) }.thenCallRealMethod()
        }
        sqlite.exec(dbHandle, "SELECT 1")
        verify(sqlite).exec(db, "SELECT 1")
    }

    @Test
    fun `expandedSql with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { expandedSql(any<Long>()) }.thenReturn("SELECT 1")
            on { expandedSql(any<StatementHandle>()) }.thenCallRealMethod()
        }
        assertEquals("SELECT 1", sqlite.expandedSql(statementHandle))
        verify(sqlite).expandedSql(statement)
    }

    @Test
    fun `extendedErrorCode with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { extendedErrorCode(any<Long>()) }.thenReturn(0)
            on { extendedErrorCode(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        sqlite.extendedErrorCode(dbHandle)
        verify(sqlite).extendedErrorCode(db)
    }

    @Test
    fun `extendedResultCodes with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { extendedResultCodes(any<Long>(), any()) }.thenReturn(0)
            on { extendedResultCodes(any<DatabaseHandle>(), any()) }.thenCallRealMethod()
        }
        sqlite.extendedResultCodes(dbHandle, 1)
        verify(sqlite).extendedResultCodes(db, 1)
    }

    @Test
    fun `finalize with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { finalize(any<Long>()) }.thenReturn(SQL_OK)
            on { finalize(any<StatementHandle>()) }.thenCallRealMethod()
        }
        sqlite.finalize(statementHandle)
        verify(sqlite).finalize(statement)
    }

    @Test
    fun `getAutocommit with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { getAutocommit(any<Long>()) }.thenReturn(1)
            on { getAutocommit(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        assertEquals(1, sqlite.getAutocommit(dbHandle))
        verify(sqlite).getAutocommit(db)
    }

    @Test
    fun `interrupt with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { interrupt(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        sqlite.interrupt(dbHandle)
        verify(sqlite).interrupt(db)
    }

    @Test
    fun `isInterrupted with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { isInterrupted(any<Long>()) }.thenReturn(0)
            on { isInterrupted(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        sqlite.isInterrupted(dbHandle)
        verify(sqlite).isInterrupted(db)
    }

    @Test
    fun `key with DatabaseHandle delegates`() {
        val key = byteArrayOf(1, 2)
        val sqlite = mock<IExternalSQLite> {
            on { key(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { key(any<DatabaseHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.key(dbHandle, key, key.size)
        verify(sqlite).key(db, key, key.size)
    }

    @Test
    fun `keyConventionally with DatabaseHandle delegates`() {
        val key = byteArrayOf(3, 4)
        val sqlite = mock<IExternalSQLite> {
            on { keyConventionally(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { keyConventionally(any<DatabaseHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.keyConventionally(dbHandle, key, key.size)
        verify(sqlite).keyConventionally(db, key, key.size)
    }

    @Test
    fun `lastInsertRowId with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { lastInsertRowId(any<Long>()) }.thenReturn(7L)
            on { lastInsertRowId(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        assertEquals(7L, sqlite.lastInsertRowId(dbHandle))
        verify(sqlite).lastInsertRowId(db)
    }

    @Test
    fun `prepareV2 with DatabaseHandle delegates`() {
        val holder = LongArray(1)
        val sqlite = mock<IExternalSQLite> {
            on { prepareV2(any<Long>(), any(), any(), any()) }.thenReturn(SQL_OK)
            on { prepareV2(any<DatabaseHandle>(), any(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.prepareV2(dbHandle, "SELECT 1", 8, holder)
        verify(sqlite).prepareV2(db, "SELECT 1", 8, holder)
    }

    @Test
    fun `progressHandler with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { progressHandler(any<DatabaseHandle>(), any(), anyOrNull()) }.thenCallRealMethod()
        }
        sqlite.progressHandler(dbHandle, 100, null)
        verify(sqlite).progressHandler(db, 100, null)
    }

    @Test
    fun `rawKey with DatabaseHandle delegates`() {
        val key = byteArrayOf(5, 6)
        val sqlite = mock<IExternalSQLite> {
            on { rawKey(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { rawKey(any<DatabaseHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.rawKey(dbHandle, key, key.size)
        verify(sqlite).rawKey(db, key, key.size)
    }

    @Test
    fun `rekey with DatabaseHandle delegates`() {
        val key = byteArrayOf(7, 8)
        val sqlite = mock<IExternalSQLite> {
            on { rekey(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
            on { rekey(any<DatabaseHandle>(), any(), any()) }.thenCallRealMethod()
        }
        sqlite.rekey(dbHandle, key, key.size)
        verify(sqlite).rekey(db, key, key.size)
    }

    @Test
    fun `reset with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { reset(any<Long>()) }.thenReturn(SQL_OK)
            on { reset(any<StatementHandle>()) }.thenCallRealMethod()
        }
        sqlite.reset(statementHandle)
        verify(sqlite).reset(statement)
    }

    @Test
    fun `resetAndClearBindings with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { resetAndClearBindings(any<Long>()) }.thenReturn(SQL_OK)
            on { resetAndClearBindings(any<StatementHandle>()) }.thenCallRealMethod()
        }
        sqlite.resetAndClearBindings(statementHandle)
        verify(sqlite).resetAndClearBindings(statement)
    }

    @Test
    fun `sql with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { sql(any<Long>()) }.thenReturn("SELECT 1")
            on { sql(any<StatementHandle>()) }.thenCallRealMethod()
        }
        assertEquals("SELECT 1", sqlite.sql(statementHandle))
        verify(sqlite).sql(statement)
    }

    @Test
    fun `statementBusy with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { statementBusy(any<Long>()) }.thenReturn(0)
            on { statementBusy(any<StatementHandle>()) }.thenCallRealMethod()
        }
        sqlite.statementBusy(statementHandle)
        verify(sqlite).statementBusy(statement)
    }

    @Test
    fun `statementReadOnly with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { statementReadOnly(any<Long>()) }.thenReturn(1)
            on { statementReadOnly(any<StatementHandle>()) }.thenCallRealMethod()
        }
        assertEquals(1, sqlite.statementReadOnly(statementHandle))
        verify(sqlite).statementReadOnly(statement)
    }

    @Test
    fun `statementStatus with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { statementStatus(any<Long>(), any(), any()) }.thenReturn(5)
            on { statementStatus(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        }
        assertEquals(5, sqlite.statementStatus(statementHandle, 1, false))
        verify(sqlite).statementStatus(statement, 1, false)
    }

    @Test
    fun `step with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { step(any<Long>()) }.thenReturn(100)
            on { step(any<StatementHandle>()) }.thenCallRealMethod()
        }
        assertEquals(100, sqlite.step(statementHandle))
        verify(sqlite).step(statement)
    }

    @Test
    fun `totalChanges with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { totalChanges(any<Long>()) }.thenReturn(10)
            on { totalChanges(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        assertEquals(10, sqlite.totalChanges(dbHandle))
        verify(sqlite).totalChanges(db)
    }

    @Test
    fun `traceV2 with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { traceV2(any<DatabaseHandle>(), any()) }.thenCallRealMethod()
        }
        sqlite.traceV2(dbHandle, 1)
        verify(sqlite).traceV2(db, 1)
    }

    @Test
    fun `transactionState with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { transactionState(any<Long>()) }.thenReturn(0)
            on { transactionState(any<DatabaseHandle>()) }.thenCallRealMethod()
        }
        sqlite.transactionState(dbHandle)
        verify(sqlite).transactionState(db)
    }

    @Test
    fun `walAutoCheckpoint with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { walAutoCheckpoint(any<Long>(), any()) }.thenReturn(SQL_OK)
            on { walAutoCheckpoint(any<DatabaseHandle>(), any()) }.thenCallRealMethod()
        }
        sqlite.walAutoCheckpoint(dbHandle, 1000)
        verify(sqlite).walAutoCheckpoint(db, 1000)
    }

    @Test
    fun `walCheckpointV2 with DatabaseHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { walCheckpointV2(any<Long>(), anyOrNull(), any()) }.thenReturn(SQL_OK)
            on { walCheckpointV2(any<DatabaseHandle>(), anyOrNull(), any()) }.thenCallRealMethod()
        }
        sqlite.walCheckpointV2(dbHandle, null, 0)
        verify(sqlite).walCheckpointV2(db, null, 0)
    }

    @Test
    fun `columnBlob with StatementHandle delegates`() {
        val data = byteArrayOf(9, 10)
        val sqlite = mock<IExternalSQLite> {
            on { columnBlob(any<Long>(), any()) } doReturn data
            on { columnBlob(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        assertEquals(data.toList(), sqlite.columnBlob(statementHandle, 0)!!.toList())
        verify(sqlite).columnBlob(statement, 0)
    }

    @Test
    fun `columnCount with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { columnCount(any<Long>()) }.thenReturn(4)
            on { columnCount(any<StatementHandle>()) }.thenCallRealMethod()
        }
        assertEquals(4, sqlite.columnCount(statementHandle))
        verify(sqlite).columnCount(statement)
    }

    @Test
    fun `columnDouble with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { columnDouble(any<Long>(), any()) }.thenReturn(3.14)
            on { columnDouble(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        assertEquals(3.14, sqlite.columnDouble(statementHandle, 0))
        verify(sqlite).columnDouble(statement, 0)
    }

    @Test
    fun `columnInt with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { columnInt(any<Long>(), any()) }.thenReturn(99)
            on { columnInt(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        assertEquals(99, sqlite.columnInt(statementHandle, 0))
        verify(sqlite).columnInt(statement, 0)
    }

    @Test
    fun `columnInt64 with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { columnInt64(any<Long>(), any()) }.thenReturn(Long.MIN_VALUE)
            on { columnInt64(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        assertEquals(Long.MIN_VALUE, sqlite.columnInt64(statementHandle, 0))
        verify(sqlite).columnInt64(statement, 0)
    }

    @Test
    fun `columnName with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { columnName(any<Long>(), any()) }.thenReturn("col")
            on { columnName(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        assertEquals("col", sqlite.columnName(statementHandle, 0))
        verify(sqlite).columnName(statement, 0)
    }

    @Test
    fun `columnText with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { columnText(any<Long>(), any()) }.thenReturn("val")
            on { columnText(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        assertEquals("val", sqlite.columnText(statementHandle, 0))
        verify(sqlite).columnText(statement, 0)
    }

    @Test
    fun `columnType with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { columnType(any<Long>(), any()) }.thenReturn(1)
            on { columnType(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        assertEquals(1, sqlite.columnType(statementHandle, 0))
        verify(sqlite).columnType(statement, 0)
    }

    @Test
    fun `columnValue with StatementHandle delegates`() {
        val sqlite = mock<IExternalSQLite> {
            on { columnValue(any<Long>(), any()) }.thenReturn(0xFACEL)
            on { columnValue(any<StatementHandle>(), any()) }.thenCallRealMethod()
        }
        assertEquals(0xFACEL, sqlite.columnValue(statementHandle, 0))
        verify(sqlite).columnValue(statement, 0)
    }

    @Test
    fun `withScopedArena default invokes block`() {
        val sqlite = mock<IExternalSQLite> {
            on { withScopedArena<Int>(any()) }.thenCallRealMethod()
        }
        assertEquals(42, sqlite.withScopedArena { 42 })
    }

    private fun handleAwareMock() = mock<IExternalSQLite> {
        on { bindText(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
        on { bindInt(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
        on { bindInt64(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
        on { bindDouble(any<Long>(), any(), any()) }.thenReturn(SQL_OK)
        on { bindNull(any<Long>(), any()) }.thenReturn(SQL_OK)
        on { bindBlob(any<Long>(), any(), any(), any()) }.thenReturn(SQL_OK)
        on { bindRow(any<StatementHandle>(), any()) }.thenCallRealMethod()
        on { bindRowTyped(any<Long>(), any(), any(), any(), any(), any(), any()) }.thenCallRealMethod()
        on { bindRowTyped(any<StatementHandle>(), any(), any(), any(), any(), any(), any()) }.thenCallRealMethod()
        on { bindText(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        on { bindInt(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        on { bindInt64(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        on { bindDouble(any<StatementHandle>(), any(), any()) }.thenCallRealMethod()
        on { bindNull(any<StatementHandle>(), any()) }.thenCallRealMethod()
        on { bindBlob(any<StatementHandle>(), any(), any(), any()) }.thenCallRealMethod()
    }
}
