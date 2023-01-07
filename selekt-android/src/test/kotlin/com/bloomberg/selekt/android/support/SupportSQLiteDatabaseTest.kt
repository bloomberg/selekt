/*
 * Copyright 2022 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android.support

import android.content.ContentValues
import android.database.MatrixCursor
import android.database.sqlite.SQLiteTransactionListener
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.android.ConflictAlgorithm
import com.bloomberg.selekt.android.SQLiteDatabase
import com.bloomberg.selekt.annotations.DelicateApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.only
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val CONFLICT_REPLACE = 5

@DelicateApi
internal class SupportSQLiteDatabaseTest {
    @Mock lateinit var database: SQLiteDatabase
    private lateinit var supportDatabase: SupportSQLiteDatabase

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        supportDatabase = database.asSupportSQLiteDatabase()
    }

    @Test
    fun beginTransaction() {
        supportDatabase.beginTransaction()
        verify(database, times(1)).beginExclusiveTransaction()
    }

    @Test
    fun beginNonExclusiveTransaction() {
        supportDatabase.beginTransactionNonExclusive()
        verify(database, times(1)).beginImmediateTransaction()
    }

    @Test
    fun beginTransactionWithListener() {
        val listener = mock<SQLiteTransactionListener>()
        supportDatabase.beginTransactionWithListener(listener)
        verify(database, times(1)).beginExclusiveTransactionWithListener(eq(listener.asSQLTransactionListener()))
    }

    @Test
    fun beginTransactionWithListenerNonExclusive() {
        val listener = mock<SQLiteTransactionListener>()
        supportDatabase.beginTransactionWithListenerNonExclusive(listener)
        verify(database, times(1)).beginImmediateTransactionWithListener(eq(listener.asSQLTransactionListener()))
    }

    @Test
    fun close() {
        supportDatabase.close()
        verify(database, times(1)).close()
    }

    @Test
    fun compileStatement() {
        val sql = "INSERT INTO Foo VALUES (?)"
        whenever(database.compileStatement(eq(sql))) doReturn mock()
        supportDatabase.compileStatement(sql)
        verify(database, times(1)).compileStatement(same(sql))
    }

    @Test
    fun delete() {
        val table = "Foo"
        val whereClause = "x = ?"
        val whereArgs = arrayOf(42)
        supportDatabase.delete(table, whereClause, whereArgs)
        verify(database, times(1)).delete(same(table), same(whereClause), same(whereArgs))
    }

    @Test
    fun disableWriteAheadLogging() {
        whenever(database.journalMode) doReturn SQLiteJournalMode.WAL
        supportDatabase.disableWriteAheadLogging()
        verifyNoMoreInteractions(database)
        assertTrue(supportDatabase.isWriteAheadLoggingEnabled)
    }

    @Test
    fun enableWriteAheadLoggingFalse() {
        whenever(database.journalMode) doReturn SQLiteJournalMode.DELETE
        assertFalse(supportDatabase.enableWriteAheadLogging())
        verify(database, times(1)).journalMode
    }

    @Test
    fun enableWriteAheadLoggingTrue() {
        whenever(database.journalMode) doReturn SQLiteJournalMode.WAL
        assertTrue(supportDatabase.enableWriteAheadLogging())
        verify(database, times(1)).journalMode
    }

    @Test
    fun endTransaction() {
        supportDatabase.endTransaction()
        verify(database, times(1)).endTransaction()
    }

    @Test
    fun execSQL() {
        val sql = "CREATE TABLE Foo (x INT)"
        supportDatabase.execSQL(sql)
        verify(database, times(1)).exec(same(sql))
    }

    @Test
    fun execSQLWithBindArguments() {
        val sql = "INSERT INTO Foo VALUES (?)"
        val args = arrayOf(42)
        supportDatabase.execSQL(sql, args)
        verify(database, times(1)).exec(same(sql), same(args))
    }

    @Test
    fun insert() {
        val table = "Foo"
        val conflictAlgorithm = CONFLICT_REPLACE
        val values = ContentValues()
        supportDatabase.insert(table, conflictAlgorithm, values)
        verify(database, times(1)).insert(same(table), same(values), same(ConflictAlgorithm.REPLACE))
    }

    @Test
    fun inTransactionFalse() {
        whenever(database.isTransactionOpenedByCurrentThread) doReturn false
        assertFalse(supportDatabase.inTransaction())
        verify(database, times(1)).isTransactionOpenedByCurrentThread
    }

    @Test
    fun inTransactionTrue() {
        whenever(database.isTransactionOpenedByCurrentThread) doReturn true
        assertTrue(supportDatabase.inTransaction())
        verify(database, times(1)).isTransactionOpenedByCurrentThread
    }

    @Test
    fun isDatabaseIntegrityOkMainFalse() {
        whenever(database.integrityCheck(eq("main"))) doReturn false
        val cursor = MatrixCursor(arrayOf("_", "name", "file")).apply {
            addRow(arrayOf("_", "main", "main.db"))
        }
        whenever(database.query(eq("PRAGMA database_list"), anyOrNull())) doReturn cursor
        assertFalse(supportDatabase.isDatabaseIntegrityOk)
        verify(database, times(1)).integrityCheck(eq("main"))
    }

    @Test
    fun isDatabaseIntegrityOkMainTrue() {
        whenever(database.integrityCheck(eq("main"))) doReturn true
        val cursor = MatrixCursor(arrayOf("_", "name", "file")).apply {
            addRow(arrayOf("_", "main", "main.db"))
        }
        whenever(database.query(eq("PRAGMA database_list"), anyOrNull())) doReturn cursor
        assertTrue(supportDatabase.isDatabaseIntegrityOk)
        verify(database, times(1)).integrityCheck(eq("main"))
    }

    @Test
    fun isDatabaseIntegrityOkMainAndAttachedFalse() {
        whenever(database.integrityCheck(eq("main"))) doReturn true
        whenever(database.integrityCheck(eq("foo"))) doReturn false
        val cursor = MatrixCursor(arrayOf("_", "name", "file")).apply {
            addRow(arrayOf("_", "main", "main.db"))
            addRow(arrayOf("_", "foo", "foo.db"))
        }
        whenever(database.query(eq("PRAGMA database_list"), anyOrNull())) doReturn cursor
        assertFalse(supportDatabase.isDatabaseIntegrityOk)
        verify(database, times(1)).integrityCheck(eq("main"))
        verify(database, times(1)).integrityCheck(eq("foo"))
    }

    @Test
    fun isDatabaseIntegrityOkMainAndAttachedTrue() {
        whenever(database.integrityCheck(eq("main"))) doReturn true
        whenever(database.integrityCheck(eq("foo"))) doReturn true
        val cursor = MatrixCursor(arrayOf("_", "name", "file")).apply {
            addRow(arrayOf("_", "main", "main.db"))
            addRow(arrayOf("_", "foo", "foo.db"))
        }
        whenever(database.query(eq("PRAGMA database_list"), anyOrNull())) doReturn cursor
        assertTrue(supportDatabase.isDatabaseIntegrityOk)
        verify(database, times(1)).integrityCheck(eq("main"))
        verify(database, times(1)).integrityCheck(eq("foo"))
    }

    @Test
    fun isDbLockedByCurrentThreadFalse() {
        whenever(database.isConnectionHeldByCurrentThread) doReturn false
        assertFalse(supportDatabase.isDbLockedByCurrentThread)
        verify(database, times(1)).isConnectionHeldByCurrentThread
    }

    @Test
    fun isDbLockedByCurrentThreadTrue() {
        whenever(database.isConnectionHeldByCurrentThread) doReturn true
        assertTrue(supportDatabase.isDbLockedByCurrentThread)
        verify(database, times(1)).isConnectionHeldByCurrentThread
    }

    @Test
    fun isOpenFalse() {
        whenever(database.isOpen) doReturn false
        assertFalse(supportDatabase.isOpen)
        verify(database, times(1)).isOpen
    }

    @Test
    fun isOpenTrue() {
        whenever(database.isOpen) doReturn true
        assertTrue(supportDatabase.isOpen)
        verify(database, times(1)).isOpen
    }

    @Test
    fun isReadOnly() {
        assertFalse(supportDatabase.isReadOnly)
        verifyNoMoreInteractions(database)
    }

    @Test
    fun isWriteAheadLoggingEnabledTrue() {
        whenever(database.journalMode) doReturn SQLiteJournalMode.WAL
        assertTrue(supportDatabase.isWriteAheadLoggingEnabled)
        verify(database, times(1)).journalMode
    }

    @Test
    fun maximumSize() {
        whenever(database.maximumSize) doReturn 42L
        assertEquals(42L, supportDatabase.maximumSize)
        verify(database, times(1)).maximumSize
    }

    @Test
    fun needUpgradeFalse() {
        whenever(database.version) doReturn 42
        assertFalse(supportDatabase.needUpgrade(41))
        verify(database, times(1)).version
    }

    @Test
    fun needUpgradeTrue() {
        whenever(database.version) doReturn 42
        assertTrue(supportDatabase.needUpgrade(43))
        verify(database, times(1)).version
    }

    @Test
    fun pageSize() {
        whenever(database.pageSize) doReturn 42L
        assertEquals(42L, supportDatabase.pageSize)
        verify(database, times(1)).pageSize
    }

    @Test
    fun path() {
        val path = "/tmp/foo.db"
        whenever(database.path) doReturn path
        assertSame(path, supportDatabase.path)
        verify(database, times(1)).path
    }

    @Test
    fun queryString() {
        val sql = "SELECT * FROM 'Foo'"
        supportDatabase.query(sql)
        verify(database, times(1)).query(same(sql), isNull())
    }

    @Test
    fun queryStringWithBindArguments() {
        val sql = "SELECT * FROM 'Foo' WHERE id = ?"
        val args = arrayOf(42)
        supportDatabase.query(sql, args)
        verify(database, times(1)).query(same(sql), same(args))
    }

    @Test
    fun queryWithSupportQuery() {
        supportDatabase.query(mock<SupportSQLiteQuery>())
        verify(database, times(1)).query(any())
    }

    @Test
    fun queryWithSupportQueryAndCancellation() {
        supportDatabase.query(mock<SupportSQLiteQuery>(), mock())
        verify(database, times(1)).query(any())
    }

    @Test
    fun setForeignKeyConstraintsEnabledFalse() {
        supportDatabase.setForeignKeyConstraintsEnabled(false)
        verify(database, times(1)).setForeignKeyConstraintsEnabled(eq(false))
    }

    @Test
    fun setForeignKeyConstraintsEnabledTrue() {
        supportDatabase.setForeignKeyConstraintsEnabled(true)
        verify(database, times(1)).setForeignKeyConstraintsEnabled(eq(true))
    }

    @Test
    fun setLocale() {
        assertFailsWith<UnsupportedOperationException> {
            supportDatabase.setLocale(Locale.ROOT)
        }
    }

    @Test
    fun setMaxSqlCacheSize() {
        assertFailsWith<UnsupportedOperationException> {
            supportDatabase.setMaxSqlCacheSize(42)
        }
    }

    @Test
    fun setMaximumSize() {
        supportDatabase.maximumSize = 42L
        verify(database, times(1)).setMaximumSize(eq(42L))
    }

    @Test
    fun setPageSize() {
        supportDatabase.pageSize = 42L
        verify(database, times(1)).pageSize = eq(42L)
    }

    @Test
    fun setTransactionSuccessful() {
        supportDatabase.setTransactionSuccessful()
        verify(database, times(1)).setTransactionSuccessful()
    }

    @Test
    fun setVersion() {
        supportDatabase.version = 42
        verify(database, times(1)).version = eq(42)
    }

    @Test
    fun update() {
        val table = "Foo"
        val conflictAlgorithm = CONFLICT_REPLACE
        val values = ContentValues()
        val whereClause = "x = ?"
        val whereArgs = arrayOf(42)
        supportDatabase.update(table, conflictAlgorithm, values, whereClause, whereArgs)
        verify(database, times(1)).update(
            same(table), same(values), same(whereClause), same(whereArgs), same(ConflictAlgorithm.REPLACE))
    }

    @Test
    fun version() {
        val version = 42
        whenever(database.version) doReturn version
        assertEquals(version, supportDatabase.version)
        verify(database, only()).version
    }

    @Test
    fun yieldIfContendedSafely() {
        supportDatabase.yieldIfContendedSafely()
        verify(database, times(1)).yieldTransaction()
    }

    @Test
    fun yieldIfContendedSafelyAfterDelay() {
        val pauseMillis = 42L
        supportDatabase.yieldIfContendedSafely(pauseMillis)
        verify(database, times(1)).yieldTransaction(eq(pauseMillis))
    }
}
