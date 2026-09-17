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

package com.bloomberg.selekt.android

import com.bloomberg.selekt.ColumnType
import com.bloomberg.selekt.CancellationSignal
import com.bloomberg.selekt.DatabaseConfiguration
import com.bloomberg.selekt.OperationCancelledException
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SimpleSQLQuery
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val ROW_COUNT = 25
private const val WINDOW_SIZE = 4

internal class SQLDatabasePagedCursorTest {
    private lateinit var database: SQLDatabase

    @BeforeEach
    fun setUp() {
        database = SQLDatabase(
            "file::memory:",
            SQLite,
            SQLiteJournalMode.MEMORY.databaseConfiguration.copy(cursorWindowSize = WINDOW_SIZE),
            null
        )
        database.exec("CREATE TABLE 'Foo' (bar INT, baz TEXT)", emptyArray())
        database.batch(
            "INSERT INTO 'Foo' VALUES (?, ?)",
            (0 until ROW_COUNT).map { arrayOf<Any?>(it, "row$it") }
        )
    }

    @AfterEach
    fun tearDown() {
        database.run {
            close()
            assertFalse(isOpen())
        }
    }

    @Test
    fun androidCursorPlatformDefaultsRemainUnbounded() {
        assertEquals(DatabaseConfiguration.UNBOUNDED_CURSOR_WINDOW_SIZE, SQLite.defaultCursorWindowSize)
        assertEquals(
            DatabaseConfiguration.UNBOUNDED_CURSOR_WINDOW_BYTE_SIZE,
            SQLite.defaultCursorWindowByteSize
        )
    }

    @Test
    fun countIsTheWholeResultSet() {
        database.query(SimpleSQLQuery("SELECT bar FROM Foo")).use {
            assertEquals(ROW_COUNT, it.count)
        }
    }

    @Test
    fun scansForwardAcrossEveryWindow() {
        database.query(SimpleSQLQuery("SELECT bar, baz FROM Foo ORDER BY bar")).use { cursor ->
            (0 until ROW_COUNT).forEach {
                assertTrue(cursor.moveToNext())
                assertEquals(it.toLong(), cursor.getLong(0))
                assertEquals("row$it", cursor.getString(1))
            }
            assertFalse(cursor.moveToNext())
        }
    }

    @Test
    fun refillsInsteadOfRetainingTheCompleteResult(): Unit = database.run {
        query(SimpleSQLQuery("SELECT bar, baz FROM Foo ORDER BY bar")).use { cursor ->
            exec("UPDATE Foo SET baz=? WHERE bar=?", arrayOf<Any?>("changed", 20))
            assertTrue(cursor.moveToPosition(20))
            assertEquals("changed", cursor.getString(1))
        }
    }

    @Test
    fun cancellationRemainsActiveForRefills(): Unit = database.run {
        val signal = CancellationSignal()
        query("SELECT bar FROM Foo ORDER BY bar", emptyArray(), signal).use { cursor ->
            assertTrue(cursor.moveToPosition(20))
            signal.cancel()
            assertFailsWith<OperationCancelledException> { cursor.getLong(0) }
        }
    }

    @Test
    fun rejectsARowLargerThanTheWindowBeforeCopyingIt() {
        SQLDatabase(
            "file::memory:",
            SQLite,
            SQLiteJournalMode.MEMORY.databaseConfiguration.copy(cursorWindowByteSize = 128),
            null
        ).use { boundedDatabase ->
            boundedDatabase.exec("CREATE TABLE oversized (value BLOB)", emptyArray())
            boundedDatabase.exec("INSERT INTO oversized VALUES (?)", arrayOf(ByteArray(256)))
            val failure = assertFailsWith<IllegalStateException> {
                boundedDatabase.query(SimpleSQLQuery("SELECT value FROM oversized"))
            }
            assertTrue(failure.message.orEmpty().contains("forward-only"))
        }
    }

    @Test
    fun scansBackwardAcrossEveryWindow() {
        database.query(SimpleSQLQuery("SELECT bar, baz FROM Foo ORDER BY bar")).use { cursor ->
            assertTrue(cursor.moveToLast())
            for (row in ROW_COUNT - 1 downTo 0) {
                assertEquals(row.toLong(), cursor.getLong(0))
                assertEquals("row$row", cursor.getString(1))
                assertEquals(row > 0, cursor.moveToPrevious())
            }
        }
    }

    @Test
    fun seeksRandomlyAcrossWindows() {
        database.query(SimpleSQLQuery("SELECT bar FROM Foo ORDER BY bar")).use { cursor ->
            listOf(24, 0, 13, 7, 23, 1, 18).forEach {
                assertTrue(cursor.moveToPosition(it))
                assertEquals(it.toLong(), cursor.getLong(0))
            }
        }
    }

    @Test
    fun readsEveryColumnTypeFromARefilledWindow() {
        database.exec("CREATE TABLE 'Bar' (i INTEGER, r REAL, t TEXT, b BLOB, n INTEGER)", emptyArray())
        database.batch(
            "INSERT INTO 'Bar' VALUES (?, ?, ?, ?, ?)",
            (0 until ROW_COUNT).map { arrayOf<Any?>(it, 0.5 + it, "t$it", byteArrayOf(it.toByte()), null) }
        )
        database.query(SimpleSQLQuery("SELECT i, r, t, b, n FROM Bar ORDER BY i")).use {
            assertTrue(it.moveToPosition(20))
            assertEquals(ColumnType.INTEGER, it.type(0))
            assertEquals(20L, it.getLong(0))
            assertEquals(20.5, it.getDouble(1))
            assertEquals("t20", it.getString(2))
            assertContentEquals(byteArrayOf(20), it.getBlob(3))
            assertTrue(it.isNull(4))
            assertNull(it.getString(4))
        }
    }

    @Test
    fun readsAnEmptyResultSet() {
        database.query(SimpleSQLQuery("SELECT bar FROM Foo WHERE bar < 0")).use {
            assertEquals(0, it.count)
            assertFalse(it.moveToFirst())
        }
    }

    @Test
    fun readsAResultSetSmallerThanTheWindow() {
        database.query(SimpleSQLQuery("SELECT bar FROM Foo WHERE bar < 2 ORDER BY bar")).use {
            assertEquals(2, it.count)
            assertTrue(it.moveToFirst())
            assertEquals(0L, it.getLong(0))
            assertTrue(it.moveToNext())
            assertEquals(1L, it.getLong(0))
            assertFalse(it.moveToNext())
        }
    }
}
