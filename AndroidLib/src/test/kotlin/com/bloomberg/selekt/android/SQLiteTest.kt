/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.android

import android.database.sqlite.SQLiteAbortException
import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException
import android.database.sqlite.SQLiteBlobTooBigException
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteDatatypeMismatchException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import android.database.sqlite.SQLiteMisuseException
import android.database.sqlite.SQLiteOutOfMemoryException
import android.database.sqlite.SQLiteReadOnlyDatabaseException
import android.database.sqlite.SQLiteTableLockedException
import com.bloomberg.selekt.commons.deleteDatabase
import com.bloomberg.selekt.ColumnType
import com.bloomberg.selekt.NULL
import com.bloomberg.selekt.Pointer
import com.bloomberg.selekt.SQLOpenOperation
import com.bloomberg.selekt.SQL_ABORT
import com.bloomberg.selekt.SQL_AUTH
import com.bloomberg.selekt.SQL_BUSY
import com.bloomberg.selekt.SQL_CANT_OPEN
import com.bloomberg.selekt.SQL_CONSTRAINT
import com.bloomberg.selekt.SQL_CORRUPT
import com.bloomberg.selekt.SQL_DONE
import com.bloomberg.selekt.SQL_FULL
import com.bloomberg.selekt.SQL_IO_ERROR
import com.bloomberg.selekt.SQL_LOCKED
import com.bloomberg.selekt.SQL_MISMATCH
import com.bloomberg.selekt.SQL_MISUSE
import com.bloomberg.selekt.SQL_NOMEM
import com.bloomberg.selekt.SQL_NOT_A_DATABASE
import com.bloomberg.selekt.SQL_NOT_FOUND
import com.bloomberg.selekt.SQL_OK
import com.bloomberg.selekt.SQL_OPEN_CREATE
import com.bloomberg.selekt.SQL_OPEN_READONLY
import com.bloomberg.selekt.SQL_OPEN_READWRITE
import com.bloomberg.selekt.SQL_READONLY
import com.bloomberg.selekt.SQL_ROW
import com.bloomberg.selekt.SQL_TOO_BIG
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.After
import org.junit.Assert
import org.junit.Test

import org.junit.Before
import org.junit.Rule
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import java.io.File
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal inline fun Pointer.useConnection(block: (Pointer) -> Unit) = try {
    block(this)
} finally {
    assertEquals(SQL_OK, SQLite.closeV2(this))
}

internal fun prepareStatement(connection: Long, sql: String): Long {
    val holder = LongArray(1)
    SQLite.prepareV2(connection, sql, holder)
    return holder.first().also { assertNotEquals(NULL, it) }
}

internal inline fun Pointer.usePreparedStatement(block: (Pointer) -> Unit) = try {
    block(this)
} finally {
    assertEquals(SQL_OK, SQLite.finalize(this))
}

private inline fun Pair<Pointer, Pointer>.useTwoConnections(block: Pair<Pointer, Pointer>.() -> Unit) = try {
    block(this)
} finally {
    assertEquals(SQL_OK, SQLite.closeV2(first))
    assertEquals(SQL_OK, SQLite.closeV2(second))
}

internal class SQLiteTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    private val file = File.createTempFile("test-sqlite", ".db").also { it.deleteOnExit() }

    private var db: Pointer = NULL

    private val key = ByteArray(32) { 0x42 }
    private val otherKey = ByteArray(32) { 0x43 }

    @Before
    fun setUp() {
        db = openConnection()
    }

    @After
    fun tearDown() {
        try {
            assertEquals(SQL_OK, SQLite.closeV2(db))
        } finally {
            if (file.exists()) {
                assertTrue(deleteDatabase(file))
            }
        }
    }

    @Test
    fun openSuccessfully() {
        assertNotEquals(NULL, db)
    }

    @Test
    fun keySuccessfully() {
        assertEquals(SQL_OK, SQLite.key(db, key))
    }

    @Test
    fun keyLateThenVacuumFails() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)")
        assertEquals(SQL_OK, SQLite.key(db, key))
        assertThatExceptionOfType(SQLiteDatabaseCorruptException::class.java).isThrownBy {
            SQLite.exec(db, "VACUUM")
        }
    }

    @Test
    fun openThenRekeySuccessfully() {
        assertEquals(SQL_OK, SQLite.rekey(db, otherKey))
    }

    @Test
    fun keyThenRekeySuccessfully() {
        SQLite.key(db, key)
        assertEquals(SQL_OK, SQLite.rekey(db, otherKey))
    }

    @Test(expected = SQLiteDatabaseCorruptException::class)
    fun keyConnectionThenNoKeyAnotherConnection() {
        SQLite.key(db, key)
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)")
        openConnection().useConnection {
            SQLite.exec(it, "INSERT INTO 'Foo' VALUES (43)")
        }
    }

    @Test(expected = SQLiteDatabaseCorruptException::class)
    fun keyConnectionThenKeyIncorrectlyAnotherConnection() {
        SQLite.key(db, key)
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)")
        openConnection().useConnection {
            SQLite.key(it, otherKey)
            SQLite.exec(it, "SELECT * FROM 'Foo'")
        }
    }

    @Test
    fun keyConnectionThenKeyCorrectlyAnotherConnection() {
        SQLite.key(db, key)
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)")
        openConnection().useConnection {
            SQLite.key(it, ByteArray(32) { 0x42 })
            SQLite.exec(it, "SELECT * FROM 'Foo'")
        }
    }

    @Test
    fun keyConnectionFirstThenClose() {
        openConnection().useConnection {
            SQLite.key(it, key)
            SQLite.exec(it, "CREATE TABLE 'Foo' (bar INT)")
            SQLite.exec(it, "INSERT INTO 'Foo' VALUES (42)")
        }
        SQLite.key(db, key)
        SQLite.exec(db, "SELECT * FROM 'Foo'")
    }

    @Test
    fun wasteKeyedConnectionThenUseDifferentKey() {
        openConnection().useConnection {
            SQLite.key(it, key)
        }
        SQLite.key(db, otherKey)
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
    }

    @Test
    fun softHeapLimitIsPositive() {
        assertEquals(8 * 1_024 * 1_024L, SQLite.softHeapLimit64())
    }

    @Test
    fun hardHeapLimitIsPositive() {
        assertEquals(0L, SQLite.hardHeapLimit64())
    }

    @Test
    fun memoryUsed() {
        assertTrue(sqlite.memoryUsed() >= 0L)
    }

    @Test
    fun closeConnectionTwiceDoesNotThrow() {
        openConnection().let {
            SQLite.closeV2(it)
            assertThatExceptionOfType(SQLiteMisuseException::class.java).isThrownBy {
                SQLite.closeV2(it)
            }
        }
    }

    @Test
    fun closeConnectionThenUse() {
        openConnection().let {
            SQLite.closeV2(it)
            assertThatExceptionOfType(SQLiteMisuseException::class.java).isThrownBy {
                SQLite.exec(it, "CREATE TABLE 'Foo' (bar INT)")
            }
        }
    }

    @Test
    fun useConnectionThenCloseThenUse() {
        openConnection().let {
            SQLite.exec(it, "CREATE TABLE 'Foo' (bar INT)")
            prepareStatement(it, "INSERT INTO Foo VALUES (42)").usePreparedStatement { _ ->
                SQLite.closeV2(it)
                assertThatExceptionOfType(SQLiteMisuseException::class.java).isThrownBy {
                    prepareStatement(it, "INSERT INTO Foo VALUES (43)").usePreparedStatement { }
                }
            }
        }
    }

    @Test
    fun secureDeleteFast() {
        prepareStatement(db, "PRAGMA secure_delete").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(0, SQLite.columnInt(it, 0))
        }
        assertEquals(SQL_OK, SQLite.exec(db, "PRAGMA secure_delete=FAST"))
        prepareStatement(db, "PRAGMA secure_delete=FAST").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(2, SQLite.columnInt(it, 0))
        }
    }

    @Test
    fun bindSingleQuote() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)")
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?)").usePreparedStatement {
            assertEquals(SQL_OK, SQLite.bindText(it, 1, "'"))
            assertEquals(SQL_DONE, SQLite.step(it))
        }
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals("'", SQLite.columnText(it, 0))
        }
    }

    @Test
    fun bindTextWithSingleQuotes() {
        val text = "He said, 'I hope we'll see you at church next Sunday,' but they didn't promise!"
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)")
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?)").usePreparedStatement {
            assertEquals(SQL_OK, SQLite.bindText(it, 1, text))
            assertEquals(SQL_DONE, SQLite.step(it))
        }
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(text, SQLite.columnText(it, 0))
        }
    }

    @Test
    fun bindJsonTextContainingSingleQuotes() {
        val text = "{\"h'llo\":\"w'rld\",\"es'''''cape\":\"m'e'e'e\"}"
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)")
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?)").usePreparedStatement {
            assertEquals(SQL_OK, SQLite.bindText(it, 1, text))
            assertEquals(SQL_DONE, SQLite.step(it))
        }
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(text, SQLite.columnText(it, 0))
        }
    }

    @Test
    fun bindElidesPoorSelfInjectionAttempt() {
        val text = "'); DROP TABLE Foo; SELECT date("
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)")
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?)").usePreparedStatement {
            assertEquals(SQL_OK, SQLite.bindText(it, 1, text))
            assertEquals(SQL_DONE, SQLite.step(it))
        }
        repeat(2) {
            prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
                assertEquals(SQL_ROW, SQLite.step(it))
                assertEquals(text, SQLite.columnText(it, 0))
            }
        }
    }

    @Test
    fun bindNull() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)")
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?)").usePreparedStatement {
            assertEquals(SQL_OK, SQLite.bindNull(it, 1))
            assertEquals(SQL_DONE, SQLite.step(it))
        }
    }

    @Test
    fun insertBindParameterCount() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?)").usePreparedStatement {
            assertEquals(1, SQLite.bindParameterCount(it))
        }
    }

    @Test
    fun columnTypeBlob() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar BLOB)"))
        assertEquals(SQL_OK, SQLite.exec(db, "INSERT INTO 'Foo' VALUES (x'0500')"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(ColumnType.BLOB.sqlDataType, SQLite.columnType(it, 0))
        }
    }

    @Test
    fun columnTypeInt() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        assertEquals(SQL_OK, SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(ColumnType.INTEGER.sqlDataType, SQLite.columnType(it, 0))
        }
    }

    @Test
    fun columnTypeIntAsBlob() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar BLOB)"))
        assertEquals(SQL_OK, SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(ColumnType.INTEGER.sqlDataType, SQLite.columnType(it, 0))
        }
    }

    @Test
    fun columnTypeLowercaseInt() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar int)"))
        assertEquals(SQL_OK, SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(ColumnType.INTEGER.sqlDataType, SQLite.columnType(it, 0))
        }
    }

    @Test
    fun columnTypeFloat() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar FLOAT)"))
        assertEquals(SQL_OK, SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42.42)"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(ColumnType.FLOAT.sqlDataType, SQLite.columnType(it, 0))
        }
    }

    @Test
    fun columnTypeNull() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)"))
        assertEquals(SQL_OK, SQLite.exec(db, "INSERT INTO 'Foo' VALUES (NULL)"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(ColumnType.NULL.sqlDataType, SQLite.columnType(it, 0))
        }
    }

    @Test
    fun columnTypeText() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)"))
        assertEquals(SQL_OK, SQLite.exec(db, "INSERT INTO 'Foo' VALUES ('abc')"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_ROW, SQLite.step(it))
            assertEquals(ColumnType.STRING.sqlDataType, SQLite.columnType(it, 0))
        }
    }

    @Test
    fun lastInsertRowId() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        arrayOf(42, 43, 44, 45).forEachIndexed { index, it ->
            assertEquals(SQL_OK, SQLite.exec(db, "INSERT INTO 'Foo' VALUES ($it)"))
            assertEquals((index + 1).toLong(), SQLite.lastInsertRowId(db))
        }
    }

    @Test
    fun prepareV2ThenFinalize() {
        prepareStatement(db, "CREATE TABLE 'Foo' (bar INT)").usePreparedStatement {
            Assert.assertNotEquals(NULL, this)
        }
    }

    /*
    @Test
    fun prepareV2ThenFinalizeTwice() {
        prepareStatement(db, "CREATE TABLE 'Foo' (bar INT)").let {
            Assert.assertNotEquals(NULL, this)
            SQLite.finalize(it)
            assertThatExceptionOfType(SQLiteException::class.java).isThrownBy { SQLite.finalize(it) }
        }
    }
     */

    @Test
    fun prepareWildcardThenColumnCount() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(1, SQLite.columnCount(it))
        }
    }

    @Test
    fun alterTableThenPreparedStatementColumnCount() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(1, SQLite.columnCount(it))
        }
        assertEquals(SQL_OK, SQLite.exec(db, "ALTER TABLE 'Foo' ADD COLUMN xyz INT"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(2, SQLite.columnCount(it))
        }
    }

    @Test
    fun alterTableWithinPreparedStatement() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(1, SQLite.columnCount(it))
            assertEquals(SQL_OK, SQLite.exec(db, "ALTER TABLE 'Foo' ADD COLUMN xyz INT"))
            assertEquals(SQL_DONE, SQLite.step(it))
            assertEquals(2, SQLite.columnCount(it), "Prepared statement was not recompiled after step.")
        }
    }

    @Test
    fun prepareExplicitThenColumnCount() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT 'bar' FROM 'Foo'").usePreparedStatement {
            assertEquals(1, SQLite.columnCount(it))
        }
    }

    @Test
    fun prepareExplicitUnescapedThenColumnName() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT bar FROM 'Foo'").usePreparedStatement {
            assertEquals("bar", SQLite.columnName(it, 0))
        }
    }

    @Test
    fun prepareExplicitQuoteEscapedThenColumnName() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT 'bar' FROM 'Foo'").usePreparedStatement {
            assertEquals("'bar'", SQLite.columnName(it, 0))
        }
    }

    @Test
    fun prepareExplicitAccentEscapedThenColumnName() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT `bar` FROM 'Foo'").usePreparedStatement {
            assertEquals("bar", SQLite.columnName(it, 0))
        }
    }

    @Test
    fun prepareExplicitSquareBracketEscapedThenColumnName() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT [bar] FROM 'Foo'").usePreparedStatement {
            assertEquals("bar", SQLite.columnName(it, 0))
        }
    }

    @Test(expected = SQLiteBindOrColumnIndexOutOfRangeException::class)
    fun bindColumnOutOfBounds() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT ? FROM 'Foo'").usePreparedStatement {
            SQLite.bindText(it, 2, "bar")
        }
    }

    @Test
    fun databaseHandle() {
        prepareStatement(db, "CREATE TABLE 'Foo' (bar INT)").usePreparedStatement {
            assertEquals(db, SQLite.databaseHandle(it))
        }
    }

    @Test
    fun prepareWriteToReadOnly() {
        val dbHolder = LongArray(1)
        SQLite.openV2(file.absolutePath, SQL_OPEN_READONLY, dbHolder)
        dbHolder[0].let {
            try {
                prepareStatement(it, "CREATE TABLE 'Foo' (bar INT)").usePreparedStatement {}
            } finally {
                SQLite.closeV2(it)
            }
        }
    }

    @Test(expected = SQLiteException::class)
    fun preparePragmaStatement() {
        prepareStatement(db, "PRAGMA ?").usePreparedStatement {}
    }

    @Test
    fun databaseReadOnly() {
        openConnection(SQL_OPEN_READONLY).useConnection {
            assertNotEquals(0, SQLite.databaseReadOnly(it, "main"))
        }
    }

    @Test
    fun databaseNotReadOnly() {
        assertEquals(0, SQLite.databaseReadOnly(db, "main"))
    }

    @Test
    fun databaseReadOnlyWrongName() {
        openConnection(SQL_OPEN_READONLY).useConnection {
            assertEquals(-1, SQLite.databaseReadOnly(it, "foo"))
        }
    }

    @Test
    fun deleteDatabaseFileThenInsert() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)")
        assertTrue(deleteDatabase(file))
        assertThatExceptionOfType(SQLiteReadOnlyDatabaseException::class.java).isThrownBy {
            SQLite.exec(db, "INSERT INTO 'Foo' VALUES (43)")
        }
    }

    @Test
    fun deleteDatabaseFileThenQuery() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)")
        assertTrue(deleteDatabase(file))
        assertThatCode {
            prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
                assertNotEquals(0, SQLite.statementReadOnly(it))
                assertEquals(SQL_ROW, SQLite.step(it))
            }
        }.doesNotThrowAnyException()
    }

    @Test
    fun deleteDatabaseFileThenBeginImmediateTransaction() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        SQLite.exec(db, "INSERT INTO 'Foo' VALUES (42)")
        assertTrue(deleteDatabase(file))
        assertThatCode {
            SQLite.exec(db, "BEGIN IMMEDIATE TRANSACTION")
        }.doesNotThrowAnyException()
    }

    @Test
    fun statementReadOnly() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertNotEquals(0, SQLite.statementReadOnly(it))
        }
    }

    @Test
    fun statementNotReadOnly() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (42)").usePreparedStatement {
            assertEquals(0, SQLite.statementReadOnly(it))
        }
    }

    @Test
    fun vacuumStatementNotReadOnly() {
        prepareStatement(db, "vacuum").usePreparedStatement {
            assertEquals(0, SQLite.statementReadOnly(it))
        }
    }

    @Test
    fun pragmaJournalModeStatementReadOnly() {
        prepareStatement(db, "PRAGMA journal_mode=wal").usePreparedStatement {
            assertEquals(0, SQLite.statementReadOnly(it))
        }
    }

    @Test
    fun statementSql() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT bar FROM 'Foo' WHERE bar=?").usePreparedStatement {
            assertEquals("SELECT bar FROM 'Foo' WHERE bar=?", SQLite.sql(it))
        }
    }

    @Test
    fun statementExpandedSql() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT bar FROM 'Foo' WHERE bar=?").usePreparedStatement {
            SQLite.bindInt(it, 1, 42)
            assertEquals("SELECT bar FROM 'Foo' WHERE bar=42", SQLite.expandedSql(it))
        }
    }

    @Test
    fun journalPragmaStatementNotReadOnly() {
        prepareStatement(db, "PRAGMA journal_mode").usePreparedStatement {
            assertEquals(0, SQLite.statementReadOnly(it))
        }
    }

    @Test(expected = SQLiteReadOnlyDatabaseException::class)
    fun writeToReadOnly() {
        openConnection(SQL_OPEN_READONLY).useConnection {
            SQLite.exec(it, "CREATE TABLE 'Foo' (bar INT)")
        }
    }

    @Test
    fun getAutocommitInitial() {
        assertNotEquals(0, SQLite.getAutocommit(db), "Database must be in autocommit mode initially.")
    }

    @Test
    fun getAutocommitInTransaction() {
        assertEquals(SQL_OK, SQLite.exec(db, "BEGIN IMMEDIATE TRANSACTION"))
        assertEquals(0, SQLite.getAutocommit(db), "Database must not be in autocommit mode when transacting.")
    }

    @Test
    fun getAutocommitAfterRollback() {
        assertEquals(SQL_OK, SQLite.exec(db, "BEGIN IMMEDIATE TRANSACTION"))
        assertEquals(SQL_OK, SQLite.exec(db, "ROLLBACK"))
        assertNotEquals(0, SQLite.getAutocommit(db), "Database must be in autocommit mode.")
    }

    @Test
    fun beginIsReadOnly() {
        prepareStatement(db, "BEGIN").usePreparedStatement {
            assertEquals(1, SQLite.statementReadOnly(it))
        }
    }

    @Test
    fun busyTimeout() {
        assertEquals(SQL_OK, SQLite.busyTimeout(db, 2_500))
    }

    @Test
    fun errorCode() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_DONE, SQLite.step(it))
            assertEquals(SQL_DONE, SQLite.errorCode(db))
        }
    }

    @Test
    fun errorMessage() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_DONE, SQLite.step(it))
            assertEquals("no more rows available", SQLite.errorMessage(db))
        }
    }

    @Test
    fun exceptionForErrorOk() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_OK, SQL_OK, "")
        }
    }

    @Test
    fun exceptionForError() {
        assertThatExceptionOfType(SQLiteException::class.java).isThrownBy {
            SQLite.throwSQLException(Int.MIN_VALUE, Int.MIN_VALUE, "")
        }
    }

    @Test
    fun exceptionForErrorAbort() {
        assertThatExceptionOfType(SQLiteAbortException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_ABORT, SQL_ABORT, "")
        }
    }

    @Test
    fun exceptionForErrorAuth() {
        assertThatExceptionOfType(SQLiteCantOpenDatabaseException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_AUTH, SQL_AUTH, "")
        }
    }

    @Test
    fun exceptionForErrorBusy() {
        assertThatExceptionOfType(SQLiteDatabaseLockedException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_BUSY, SQL_BUSY, "")
        }
    }

    @Test
    fun exceptionForErrorCantOpen() {
        assertThatExceptionOfType(SQLiteCantOpenDatabaseException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_CANT_OPEN, SQL_CANT_OPEN, "")
        }
    }

    @Test
    fun exceptionForErrorConstraint() {
        assertThatExceptionOfType(SQLiteConstraintException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_CONSTRAINT, SQL_CONSTRAINT, "")
        }
    }

    @Test
    fun exceptionForErrorCorrupt() {
        assertThatExceptionOfType(SQLiteDatabaseCorruptException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_CORRUPT, SQL_CORRUPT, "")
        }
    }

    @Test
    fun exceptionForErrorFull() {
        assertThatExceptionOfType(SQLiteFullException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_FULL, SQL_FULL, "")
        }
    }

    @Test
    fun exceptionForErrorIO() {
        assertThatExceptionOfType(SQLiteDiskIOException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_IO_ERROR, SQL_IO_ERROR, "")
        }
    }

    @Test
    fun exceptionForErrorLocked() {
        assertThatExceptionOfType(SQLiteTableLockedException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_LOCKED, SQL_LOCKED, "")
        }
    }

    @Test
    fun exceptionForErrorMisuse() {
        assertThatExceptionOfType(SQLiteMisuseException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_MISUSE, SQL_MISUSE, "")
        }
    }

    @Test
    fun exceptionForErrorNoMemory() {
        assertThatExceptionOfType(SQLiteOutOfMemoryException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_NOMEM, SQL_NOMEM, "")
        }
    }

    @Test
    fun exceptionForErrorNotDatabase() {
        assertThatExceptionOfType(SQLiteDatabaseCorruptException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_NOT_A_DATABASE, SQL_NOT_A_DATABASE, "")
        }
    }

    @Test
    fun exceptionForErrorNotFound() {
        assertThatExceptionOfType(SQLiteCantOpenDatabaseException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_NOT_FOUND, SQL_NOT_FOUND, "")
        }
    }

    @Test
    fun exceptionForErrorReadOnly() {
        assertThatExceptionOfType(SQLiteReadOnlyDatabaseException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_READONLY, SQL_READONLY, "")
        }
    }

    @Test
    fun exceptionForErrorMismatch() {
        assertThatExceptionOfType(SQLiteDatatypeMismatchException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_MISMATCH, SQL_MISMATCH, "")
        }
    }

    @Test
    fun exceptionForErrorTooBig() {
        assertThatExceptionOfType(SQLiteBlobTooBigException::class.java).isThrownBy {
            SQLite.throwSQLException(SQL_TOO_BIG, SQL_TOO_BIG, "")
        }
    }

    @Test
    fun extendedErrorCode() {
        SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_DONE, SQLite.step(it))
            assertEquals(SQL_DONE, SQLite.extendedErrorCode(db))
        }
    }

    @Test
    fun databaseReleaseMemory() {
        assertTrue(SQLite.databaseReleaseMemory(db) >= 0)
    }

    @Test
    fun releaseMemory() {
        assertTrue(SQLite.releaseMemory(Int.MAX_VALUE) >= 0)
    }

    @Test
    fun valueFreeDup() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)"))
        prepareStatement(db, "SELECT bar FROM 'Foo' WHERE bar=?").usePreparedStatement {
            val p = SQLite.columnValue(it, 1)
            assertNotEquals(0L, p)
            val q = SQLite.valueDup(p)
            try {
                assertNotEquals(0, q)
                assertNotEquals(p, q)
            } finally {
                SQLite.valueFree(q)
            }
        }
    }

    @Test
    fun valueFromBind() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT, xyz INT)"))
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?, 1)").usePreparedStatement {
            SQLite.bindInt(it, 1, 42)
            val q = SQLite.valueDup(SQLite.columnValue(it, 1))
            val r = SQLite.valueDup(SQLite.columnValue(it, 2))
            try {
                assertEquals(0, SQLite.valueFromBind(q))
                assertEquals(0, SQLite.valueFromBind(r))
            } finally {
                SQLite.valueFree(q)
                SQLite.valueFree(r)
            }
        }
    }

    @Test
    fun walCheckpointPassive() {
        assertEquals(SQL_OK, SQLite.walCheckpointV2(db, null, 0))
    }

    @Test
    fun walAutoCheckpointFileSizeDefault() {
        assertEquals(SQL_OK, SQLite.exec(db, "PRAGMA journal_mode=WAL"))
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)"))
        assertEquals(SQL_OK, SQLite.exec(db, "BEGIN IMMEDIATE TRANSACTION"))
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?)").usePreparedStatement {
            repeat(1_000) { _ ->
                SQLite.bindText(it, 1, "a".repeat(10_000))
                assertEquals(SQL_DONE, SQLite.step(it))
                assertEquals(SQL_OK, SQLite.reset(it))
                assertEquals(SQL_OK, SQLite.clearBindings(it))
            }
        }
        assertEquals(SQL_OK, SQLite.exec(db, "END"))
        val walJournal = File(file.path + "-wal")
        assertTrue(walJournal.exists() && walJournal.canRead())
        assertTrue(walJournal.length() > 1e7)
        assertTrue(file.length() > 1e7)
    }

    @Test
    fun walAutoCheckpointNone() {
        assertEquals(SQL_OK, SQLite.exec(db, "PRAGMA journal_mode=WAL"))
        assertEquals(SQL_OK, SQLite.exec(db, "PRAGMA page_size=4096"))
        assertEquals(SQL_OK, SQLite.walAutoCheckpoint(db, 0))
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)"))
        assertEquals(SQL_OK, SQLite.exec(db, "BEGIN IMMEDIATE TRANSACTION"))
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?)").usePreparedStatement {
            repeat(1_000) { _ ->
                SQLite.bindText(it, 1, "a".repeat(10_000))
                assertEquals(SQL_DONE, SQLite.step(it))
                assertEquals(SQL_OK, SQLite.reset(it))
                assertEquals(SQL_OK, SQLite.clearBindings(it))
            }
        }
        assertEquals(SQL_OK, SQLite.exec(db, "END"))
        val walJournal = File(file.path + "-wal")
        assertTrue(walJournal.exists() && walJournal.canRead())
        assertTrue(walJournal.length() > 1e7)
        assertEquals(4096, file.length())
    }

    @Test
    fun deleteFileThenWrite() {
        assertTrue(file.delete())
        assertThatExceptionOfType(SQLiteDiskIOException::class.java).isThrownBy {
            SQLite.exec(db, "CREATE TABLE 'Foo' (bar INT)")
        }
    }

    @Test
    fun deleteFileThenBeginImmediateTransaction() {
        assertTrue(file.delete())
        assertThatExceptionOfType(SQLiteDiskIOException::class.java).isThrownBy {
            SQLite.exec(db, "BEGIN IMMEDIATE TRANSACTION")
        }
    }

    @Test
    fun transactionStateNone() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)"))
        assertEquals(0, SQLite.transactionState(db))
    }

    @Test
    fun transactionStateRead() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)"))
        assertEquals(SQL_OK, SQLite.exec(db, "BEGIN TRANSACTION"))
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(SQL_DONE, SQLite.step(it))
        }
        assertEquals(1, SQLite.transactionState(db))
    }

    @Test
    fun transactionStateWrite() {
        assertEquals(SQL_OK, SQLite.exec(db, "CREATE TABLE 'Foo' (bar TEXT)"))
        assertEquals(SQL_OK, SQLite.exec(db, "BEGIN TRANSACTION"))
        assertEquals(SQL_OK, SQLite.exec(db, "INSERT INTO 'Foo' VALUES ('abc')"))
        assertEquals(2, SQLite.transactionState(db))
    }

    @Test
    fun threadsafe() {
        assertEquals(2, SQLite.threadsafe())
    }

    @Test
    fun closeRollsBackAutomatically(): Unit = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")
        exec(db, "INSERT INTO 'Foo' VALUES (42)")
        openConnection().useConnection {
            exec(it, "BEGIN TRANSACTION")
            exec(it, "INSERT INTO 'Foo' VALUES (43)")
        }
        exec(db, "BEGIN TRANSACTION")
    }

    @Test
    fun blobOpenClose(): Unit = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar BLOB)")
        exec(db, "INSERT INTO 'Foo' VALUES (x'42')")
        val holder = longArrayOf(0L)
        assertEquals(
            SQL_OK,
            blobOpen(db, "main", "Foo", "bar", 1L, 0, holder),
            errorMessage(db)
        )
        try {
            assertNotEquals(0L, holder.first())
        } finally {
            assertEquals(SQL_OK, blobClose(holder.first()))
        }
    }

    @Test
    fun blobBytes(): Unit = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar BLOB)")
        exec(db, "INSERT INTO 'Foo' VALUES (x'42')")
        val holder = longArrayOf(0L)
        assertEquals(
            SQL_OK,
            blobOpen(db, "main", "Foo", "bar", 1L, 0, holder),
            errorMessage(db)
        )
        try {
            assertEquals(1, blobBytes(holder.first()))
        } finally {
            assertEquals(SQL_OK, blobClose(holder.first()), errorMessage(db))
        }
    }

    @Test
    fun blobRead(): Unit = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar BLOB)")
        exec(db, "INSERT INTO 'Foo' VALUES (x'42')")
        val holder = longArrayOf(0L)
        assertEquals(
            SQL_OK,
            blobOpen(db, "main", "Foo", "bar", 1L, 0, holder),
            errorMessage(db)
        )
        try {
            val buffer = byteArrayOf(0)
            assertEquals(SQL_OK, blobRead(holder.first(), 0, buffer, 0, 1))
            assertEquals(0x42, buffer.first())
        } finally {
            assertEquals(SQL_OK, blobClose(holder.first()), errorMessage(db))
        }
    }

    @Test
    fun blobModify(): Unit = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar BLOB)")
        exec(db, "INSERT INTO 'Foo' VALUES (x'42')")
        val holder = longArrayOf(0L)
        assertEquals(
            SQL_OK,
            blobOpen(db, "main", "Foo", "bar", 1L, 1, holder),
            errorMessage(db)
        )
        try {
            assertEquals(SQL_OK, blobWrite(holder.first(), 0, byteArrayOf(0x43), 0, 1), errorMessage(db))
            val buffer = byteArrayOf(0)
            assertEquals(SQL_OK, blobRead(holder.first(), 0, buffer, 0, 1), errorMessage(db))
            assertEquals(0x43, buffer.first())
        } finally {
            assertEquals(SQL_OK, blobClose(holder.first()), errorMessage(db))
        }
    }

    @Test
    fun blobWrite(): Unit = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar BLOB)")
        prepareStatement(db, "INSERT INTO 'Foo' VALUES (?)").usePreparedStatement {
            bindZeroBlob(it, 1, 2)
            assertEquals(SQL_DONE, step(it))
        }
        val holder = longArrayOf(0L)
        assertEquals(
            SQL_OK,
            blobOpen(db, "main", "Foo", "bar", 1L, 1, holder),
            errorMessage(db)
        )
        try {
            assertEquals(SQL_OK, blobWrite(holder.first(), 0, byteArrayOf(0x42, 0x43), 0, 2), errorMessage(db))
            val buffer = byteArrayOf(0, 0)
            assertEquals(SQL_OK, blobRead(holder.first(), 0, buffer, 0, 2), errorMessage(db))
            assertEquals(0x42, buffer.first())
            assertEquals(0x43, buffer[1])
        } finally {
            assertEquals(SQL_OK, blobClose(holder.first()), errorMessage(db))
        }
    }

    @Test
    fun blobReopen(): Unit = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar BLOB)")
        exec(db, "INSERT INTO 'Foo' VALUES (x'42')")
        exec(db, "INSERT INTO 'Foo' VALUES (x'43')")
        val holder = longArrayOf(0L)
        assertEquals(
            SQL_OK,
            blobOpen(db, "main", "Foo", "bar", 1L, 1, holder),
            errorMessage(db)
        )
        try {
            assertEquals(SQL_OK, blobReopen(holder.first(), 2), errorMessage(db))
            val buffer = byteArrayOf(0)
            assertEquals(SQL_OK, blobRead(holder.first(), 0, buffer, 0, 1), errorMessage(db))
            assertEquals(0x43, buffer.first())
        } finally {
            assertEquals(SQL_OK, blobClose(holder.first()), errorMessage(db))
        }
    }

    @Test
    fun blobWriteOnReadOnlyConnection(): Unit = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar BLOB)")
        exec(db, "INSERT INTO 'Foo' VALUES (x'42')")
        openConnection(SQL_OPEN_READONLY).useConnection {
            assertNotEquals(0, SQLite.databaseReadOnly(it, "main"))
            val holder = longArrayOf(0L)
            assertThatExceptionOfType(SQLiteReadOnlyDatabaseException::class.java).isThrownBy {
                blobOpen(it, "main", "Foo", "bar", 1L, 1, holder)
            }
        }
    }
    @Test
    fun blobReadOnReadOnlyConnection(): Unit = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar BLOB)")
        exec(db, "INSERT INTO 'Foo' VALUES (x'42')")
        openConnection(SQL_OPEN_READONLY).useConnection {
            assertNotEquals(0, SQLite.databaseReadOnly(it, "main"))
            val holder = longArrayOf(0L)
            assertEquals(
                SQL_OK,
                blobOpen(db, "main", "Foo", "bar", 1L, 0, holder),
                errorMessage(db)
            )
            try {
                val buffer = byteArrayOf(0)
                assertEquals(SQL_OK, blobRead(holder.first(), 0, buffer, 0, 1))
                assertEquals(0x42, buffer.first())
            } finally {
                assertEquals(SQL_OK, blobClose(holder.first()), errorMessage(db))
            }
        }
    }

    @Test
    fun twoReadTransactions() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")
        exec(db, "INSERT INTO 'Foo' VALUES (42)")

        openTwoConnections(SQL_OPEN_READONLY, SQL_OPEN_READONLY).useTwoConnections {
            listOf(
                "BEGIN TRANSACTION",
                "SELECT * FROM Foo",
                "COMMIT TRANSACTION"
            ).forEach {
                exec(first, it)
                exec(second, it)
            }
        }
    }

    @Test
    fun twoReadTransactionsDifferentCommitOrder() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")
        exec(db, "INSERT INTO 'Foo' VALUES (42)")

        openTwoConnections(SQL_OPEN_READONLY, SQL_OPEN_READONLY).useTwoConnections {
            listOf(
                "BEGIN TRANSACTION",
                "SELECT * FROM Foo"
            ).forEach {
                exec(first, it)
                exec(second, it)
            }

            "COMMIT TRANSACTION".let {
                exec(second, it)
                exec(first, it)
            }
        }
    }

    @Test
    fun twoWrites() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")

        openTwoConnections(SQL_OPEN_READWRITE, SQL_OPEN_READWRITE).useTwoConnections {
            exec(first, "INSERT INTO 'Foo' VALUES (42)")
            exec(second, "INSERT INTO 'Foo' VALUES (1337)")
        }
    }

    @Test
    fun oneWriteTransactionAndWriteOnAnotherConnection() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")

        openTwoConnections(SQL_OPEN_READWRITE, SQL_OPEN_READWRITE).useTwoConnections {
            exec(first, "BEGIN TRANSACTION")
            exec(first, "INSERT INTO 'Foo' VALUES (42)")

            assertThatExceptionOfType(SQLiteDatabaseLockedException::class.java).isThrownBy {
                exec(second, "INSERT INTO 'Foo' VALUES (1337)")
            }

            exec(first, "COMMIT TRANSACTION")
        }
    }

    @Test
    fun writeInTwoTransactions() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")

        openTwoConnections(SQL_OPEN_READWRITE, SQL_OPEN_READWRITE).useTwoConnections {
            exec(first, "BEGIN TRANSACTION")
            exec(first, "INSERT INTO 'Foo' VALUES (42)")

            exec(second, "BEGIN TRANSACTION")

            assertThatExceptionOfType(SQLiteDatabaseLockedException::class.java).isThrownBy {
                exec(second, "INSERT INTO 'Foo' VALUES (1337)")
            }

            exec(first, "COMMIT TRANSACTION")
            exec(second, "COMMIT TRANSACTION")
        }
    }

    @Test
    fun writeInOneTransactionAndReadInAnother() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")

        openTwoConnections(SQL_OPEN_READWRITE, SQL_OPEN_READONLY).useTwoConnections {
            exec(first, "BEGIN TRANSACTION")
            exec(first, "INSERT INTO 'Foo' VALUES (42)")

            exec(second, "BEGIN TRANSACTION")
            exec(second, "SELECT * FROM Foo")

            /*
             * An attempt to execute COMMIT might also result in an SQLITE_BUSY return code if an another thread or process
             * has an open read connection. When COMMIT fails in this way, the transaction remains active and the COMMIT can
             * be retried later after the reader has had a chance to clear.
             *
             * See https://www.sqlite.org/lang_transaction.html
             */
            assertThatExceptionOfType(SQLiteDatabaseLockedException::class.java).isThrownBy {
                exec(first, "COMMIT TRANSACTION")
            }
            exec(second, "COMMIT TRANSACTION")
            exec(first, "COMMIT TRANSACTION")
        }
    }

    @Test
    fun isBeginReadOrWrite() = openConnection(SQL_OPEN_READONLY).useConnection { connection ->
        fun Int.isReadOnly() = this != 0
        prepareStatement(connection, "BEGIN IMMEDIATE").usePreparedStatement { statement ->
            assertFalse(SQLite.statementReadOnly(statement).isReadOnly())
        }
        prepareStatement(connection, "BEGIN EXCLUSIVE").usePreparedStatement { statement ->
            assertFalse(SQLite.statementReadOnly(statement).isReadOnly())
        }
        prepareStatement(connection, "BEGIN DEFERRED").usePreparedStatement { statement ->
            assertTrue(SQLite.statementReadOnly(statement).isReadOnly())
        }
        prepareStatement(connection, "BEGIN").usePreparedStatement { statement ->
            assertTrue(SQLite.statementReadOnly(statement).isReadOnly())
        }
        prepareStatement(connection, "BEGIN TRANSACTION").usePreparedStatement { statement ->
            assertTrue(SQLite.statementReadOnly(statement).isReadOnly())
        }
    }

    @Test
    fun connectionStatus() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")
        val holder = IntArray(2) { 0 }
        assertEquals(SQL_OK, databaseStatus(db, 3, false, holder))
        assertNotEquals(0, holder[0])
        assertNotEquals(0, holder[1])
    }

    @Test
    fun statementStatusRun() = SQLite.run {
        prepareStatement(db, "BEGIN TRANSACTION").usePreparedStatement {
            assertEquals(SQL_DONE, step(it))
            assertEquals(1, statementStatus(it, 6, false))
        }
    }

    @Test
    fun statementBusy() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")
        exec(db, "INSERT INTO 'Foo' VALUES (42)")
        exec(db, "INSERT INTO 'Foo' VALUES (43)")
        prepareStatement(db, "SELECT * FROM 'Foo'").usePreparedStatement {
            assertEquals(0, statementBusy(it))
            assertEquals(SQL_ROW, step(it))
            assertEquals(1, statementBusy(it))
        }
    }

    @Test
    fun changes() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")
        exec(db, "INSERT INTO 'Foo' VALUES (42)")
        exec(db, "INSERT INTO 'Foo' VALUES (43)")
        assertEquals(1, changes(db))
    }

    @Test
    fun totalChanges() = SQLite.run {
        exec(db, "CREATE TABLE 'Foo' (bar INT)")
        exec(db, "INSERT INTO 'Foo' VALUES (42)")
        exec(db, "INSERT INTO 'Foo' VALUES (43)")
        assertEquals(2, totalChanges(db))
    }

    @Test
    fun analysisLimit(): Unit = SQLite.run {
        assertEquals(SQL_OK, exec(db, "PRAGMA analysis_limit=100"))
        openConnection().useConnection {
            prepareStatement(it, "PRAGMA analysis_limit").usePreparedStatement { s ->
                assertEquals(0, columnInt(s, 1))
            }
        }
    }

    @Test
    fun keywordCount(): Unit = SQLite.run {
        assertEquals(145, keywordCount())
    }

    private fun openConnection(flags: SQLOpenOperation = SQL_OPEN_READWRITE or SQL_OPEN_CREATE): Pointer {
        val holder = LongArray(1)
        assertEquals(SQL_OK, SQLite.openV2(file.absolutePath, flags, holder))
        return holder.first().also { assertNotEquals(NULL, it) }
    }

    private fun openTwoConnections(firstFlags: SQLOpenOperation, secondFlags: SQLOpenOperation): Pair<Pointer, Pointer> =
        openConnection(firstFlags) to openConnection(secondFlags)
}
