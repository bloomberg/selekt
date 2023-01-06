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

package com.bloomberg.selekt.android

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import com.bloomberg.selekt.commons.deleteDatabase
import com.bloomberg.selekt.annotations.Experimental
import com.bloomberg.selekt.SQLTransactionListener
import com.bloomberg.selekt.SQLiteAutoVacuumMode
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.annotations.DelicateApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@DelicateApi
internal class SQLiteDatabaseWALTest {
    private val file = createTempFile("test-sql-database-wal", ".db").toFile().apply { deleteOnExit() }

    private val database = SQLiteDatabase.openOrCreateDatabase(file, SQLiteJournalMode.WAL.databaseConfiguration,
        ByteArray(32) { 0x42 })

    @BeforeEach
    fun setUp() {
        database.exec("PRAGMA journal_mode=${SQLiteJournalMode.WAL}")
    }

    @AfterEach
    fun tearDown() {
        database.run {
            try {
                if (isOpen) {
                    close()
                }
                assertFalse(isOpen)
            } finally {
                if (file.exists()) {
                    assertTrue(deleteDatabase(file))
                }
            }
        }
    }

    @Test
    fun deleteDatabase() {
        SQLiteDatabase.deleteDatabase(file)
        assertFalse(file.exists())
    }

    @Test
    fun journalMode(): Unit = database.run {
        assertEquals(SQLiteJournalMode.WAL, journalMode)
    }

    @Test
    fun maximumPageCount(): Unit = database.run {
        setMaxPageCount(42L)
        assertEquals(42L, maxPageCount)
    }

    @Test
    fun pageCount(): Unit = database.run {
        assertEquals(1, pageCount)
    }

    @Test
    fun pageSizeDefault(): Unit = database.run {
        assertEquals(4_096L, pageSize)
    }

    @Test
    fun setPageSize(): Unit = database.run {
        setPageSizeExponent(16)
        vacuum()
        assertEquals(4_096L, pageSize)
    }

    @Test
    fun setPageSizeThrows(): Unit = database.run {
        assertThrows<IllegalArgumentException> {
            setPageSizeExponent(17)
        }
    }

    @Test
    fun setMaximumSize(): Unit = database.run {
        val size = 65_536L
        setMaximumSize(size)
        assertEquals(size, maximumSize)
    }

    @Test
    fun integrityCheckMain(): Unit = database.run {
        assertTrue(integrityCheck("main"))
    }

    @Test
    fun integrityCheckIllegal(): Unit = database.run {
        assertThrows<SQLiteException> {
            integrityCheck("foo")
        }
    }

    @Test
    fun vacuum(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        delete("Foo", null, null)
        vacuum()
        assertEquals(SQLiteJournalMode.WAL, journalMode)
    }

    @Test
    fun fullAutoVacuum(): Unit = database.run {
        autoVacuum = SQLiteAutoVacuumMode.FULL
        vacuum()
        assertSame(SQLiteAutoVacuumMode.FULL, autoVacuum)
    }

    @Test
    fun incrementalAutoVacuum(): Unit = database.run {
        assertSame(SQLiteAutoVacuumMode.INCREMENTAL, autoVacuum)
    }

    @Test
    fun noneAutoVacuum(): Unit = database.run {
        autoVacuum = SQLiteAutoVacuumMode.NONE
        vacuum()
        assertSame(SQLiteAutoVacuumMode.NONE, autoVacuum)
    }

    @Test
    fun secureDeleteIsFast(): Unit = database.run {
        query("PRAGMA secure_delete", null).use {
            assertTrue(it.moveToFirst())
            assertEquals(2, it.getInt(0))
        }
    }

    @Test
    fun version() = database.run {
        version = 42
        assertEquals(42, version)
    }

    @Test
    fun update(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT PRIMARY KEY, count INT)")
        exec("INSERT INTO 'Foo' VALUES ('x', 42)")
        update("Foo", ContentValues().apply { put("count", 43) }, "bar = ?", arrayOf("x"), ConflictAlgorithm.REPLACE)
        query(false, "Foo", arrayOf("count"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertEquals(43, it.getInt(0))
        }
    }

    @Test
    fun updateMultipleValues(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT PRIMARY KEY, count INT)")
        exec("INSERT INTO 'Foo' VALUES ('x', 42)")
        update("Foo", ContentValues().apply {
            put("bar", "y")
            put("count", 43)
        }, "bar = ?", arrayOf("x"), ConflictAlgorithm.REPLACE)
        query(false, "Foo", null, "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertEquals("y", it.getString(0))
            assertEquals(43, it.getInt(1))
        }
    }

    @Test
    fun updateEmptyWhere(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT PRIMARY KEY, count INT)")
        exec("INSERT INTO 'Foo' VALUES ('x', 42)")
        update("Foo", ContentValues().apply { put("count", 43) }, null, null, ConflictAlgorithm.REPLACE)
        query(false, "Foo", arrayOf("count"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertEquals(43, it.getInt(0))
        }
    }

    @Test
    fun updateEmptyWhereMultiple(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT PRIMARY KEY, count INT)")
        exec("INSERT INTO 'Foo' VALUES ('x', 42)")
        exec("INSERT INTO 'Foo' VALUES ('y', 43)")
        update("Foo", ContentValues().apply { put("count", 44) }, null, null, ConflictAlgorithm.REPLACE)
        query(false, "Foo", arrayOf("count"), "", emptyArray(), null, null, null, null).use {
            assertEquals(2, it.count)
            assertTrue(it.moveToFirst())
            assertEquals(44, it.getInt(0))
            assertTrue(it.moveToNext())
            assertEquals(44, it.getInt(0))
        }
    }

    @OptIn(Experimental::class)
    @Test
    fun upsertRejectsEmptyValues(): Unit = database.run {
        assertThrows<IllegalArgumentException> {
            upsert("Foo", ContentValues(), arrayOf("bar"), "")
        }
    }

    @OptIn(Experimental::class)
    @Test
    fun upsertRejectsEmptyColumns(): Unit = database.run {
        assertThrows<IllegalArgumentException> {
            upsert("Foo", ContentValues().apply { put("bar", "hello") }, emptyArray(), "")
        }
    }

    @OptIn(Experimental::class)
    @Test
    fun upsertString(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT PRIMARY KEY, count INT DEFAULT 0)")
        val values = ContentValues().apply { put("bar", "hello") }
        val id = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(id, upsert("Foo", values, arrayOf("bar"), "count=count+1"))
        query(false, "Foo", arrayOf("count"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertEquals(1, it.getInt(0))
        }
    }

    @OptIn(Experimental::class)
    @Test
    fun upsertAsInsert(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT PRIMARY KEY, count INT DEFAULT 0)")
        val values = ContentValues().apply {
            put("bar", "hello")
            put("count", 42)
        }
        assertEquals(1L, upsert("Foo", values, arrayOf("bar"), "count=count+1"))
        query(false, "Foo", null, null, emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertEquals("hello", it.getString(0))
            assertEquals(42, it.getInt(1))
        }
    }

    @Test
    fun transactionAsQuery(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        repeat(10) { i ->
            query("BEGIN TRANSACTION", emptyArray()).use {
                assertFalse(it.moveToFirst())
            }
            try {
                assertTrue(isTransactionOpenedByCurrentThread)
                exec("INSERT INTO 'Foo' VALUES (42)")
            } finally {
                query("END", emptyArray()).use {
                    assertFalse(it.moveToFirst())
                }
            }
            assertFalse(isTransactionOpenedByCurrentThread)
            query("SELECT * FROM Foo", emptyArray()).use {
                assertEquals(i + 1, it.count)
            }
        }
    }

    @Test
    fun beginTransactionAsQuery(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        repeat(10) { i ->
            query("BEGIN TRANSACTION", emptyArray()).use {
                assertFalse(it.moveToFirst())
            }
            try {
                assertTrue(isTransactionOpenedByCurrentThread)
                exec("INSERT INTO 'Foo' VALUES (42)")
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
            assertFalse(isTransactionOpenedByCurrentThread)
            query("SELECT * FROM Foo", emptyArray()).use {
                assertEquals(i + 1, it.count)
            }
        }
    }

    @Test
    fun endTransactionAsQuery(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        repeat(10) { i ->
            beginExclusiveTransaction()
            try {
                assertTrue(isTransactionOpenedByCurrentThread)
                exec("INSERT INTO 'Foo' VALUES (42)")
            } finally {
                query("END TRANSACTION", emptyArray()).use {
                    assertFalse(it.moveToFirst())
                }
            }
            assertFalse(isTransactionOpenedByCurrentThread)
            query("SELECT * FROM Foo", emptyArray()).use {
                assertEquals(i + 1, it.count)
            }
        }
    }

    @Test
    fun transactionWithListenerWillRollbackWithOnCommitThrow() = database.run {
        exec("CREATE TABLE Foo (bar INT)")
        val listener = mock<SQLTransactionListener>().apply {
            whenever(onCommit()) doThrow IllegalStateException("Bad")
        }
        beginExclusiveTransactionWithListener(listener)
        try {
            exec("INSERT INTO Foo VALUES (?)", arrayOf(42))
            setTransactionSuccessful()
        } finally {
            assertThrows<IllegalStateException> {
                endTransaction()
            }
        }
        verify(listener, times(1)).onCommit()
        verify(listener, times(1)).onRollback()
        query("SELECT * FROM Foo", null).use {
            assertFalse(it.moveToFirst())
        }
    }

    @Test
    fun execUpsertBoundStringThrows(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT PRIMARY KEY, count INT DEFAULT 0)")
        exec("INSERT INTO 'Foo' VALUES ('hello', 0)")
        assertThrows<SQLiteException> {
            exec("INSERT INTO 'Foo' VALUES ('hello', 0) ON CONFLICT DO UPDATE SET ?", arrayOf("count=count+1"))
        }
    }
}
