/*
 * Copyright 2020 Bloomberg Finance L.P.
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

import android.database.SQLException
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SQLDatabaseMemoryTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    private lateinit var database: SQLDatabase

    @Before
    fun setUp() {
        database = SQLDatabase("file::memory:", SQLite, SQLiteJournalMode.MEMORY.databaseConfiguration, null)
    }

    @After
    fun tearDown() {
        database.run {
            close()
            assertFalse(isOpen())
        }
    }

    @Test
    fun delete(): Unit = database.transact {
        database.run {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            val values = ContentValues().apply { put("bar", 42) }
            val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals(1L, rowId)
            assertEquals(1, delete("Foo", "bar=?", arrayOf("42")))
            query(false, "Foo", emptyArray(), "", emptyArray(), null, null, null, null).use {
                assertFalse(it.moveToFirst())
                assertEquals(0, it.count)
            }
        }
    }

    @Test
    fun deleteAll(): Unit = database.transact {
        database.run {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            arrayOf(42, 43, 44, 45).forEachIndexed { index, it ->
                val values = ContentValues().apply { put("bar", it) }
                val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
                assertEquals((index + 1).toLong(), rowId)
            }
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
                assertEquals(4, it.count)
            }
            assertEquals(4, delete("Foo", "", emptyArray()))
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
                assertEquals(0, it.count)
            }
        }
    }

    @Test
    fun exec() {
        database.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
    }

    @Test(expected = SQLException::class)
    fun execInvalidSQL() {
        database.exec("NOT SQL", emptyArray())
    }

    @Test
    fun insertIntWithOnConflict(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            val values = ContentValues().apply { put("bar", 42) }
            val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals(1L, rowId)
        }
    }

    @Test
    fun insertStringWithOnConflict(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
            val values = ContentValues().apply { put("bar", "xyz") }
            val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals(1L, rowId)
        }
    }

    @Test
    fun insertFloatAsIntWithOnConflict(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            val values = ContentValues().apply { put("bar", 42.0f) }
            val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals(1L, rowId)
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
                assertEquals(1, it.count)
                assertTrue(it.moveToFirst())
                assertEquals(42, it.getInt(0))
            }
        }
    }

    @Test
    fun insertIntAsFloatWithOnConflict(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar FLOAT)", emptyArray())
            val values = ContentValues().apply { put("bar", 42) }
            val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals(1L, rowId)
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
                assertEquals(1, it.count)
                assertTrue(it.moveToFirst())
                assertEquals(42, it.getFloat(0).roundToInt())
            }
        }
    }

    @Test
    fun insertFloatWithOnConflictGetAsInt(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar Float)", emptyArray())
            val values = ContentValues().apply { put("bar", 42.0f) }
            val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals(1L, rowId)
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
                assertEquals(1, it.count)
                assertTrue(it.moveToFirst())
                assertEquals(42, it.getInt(0))
            }
        }
    }

    @Test
    fun insertIntWithOnConflictMultipleTimes(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            arrayOf(42, 43, 44, 45).forEachIndexed { index, it ->
                val values = ContentValues().apply { put("bar", it) }
                val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
                assertEquals((index + 1).toLong(), rowId)
            }
        }
    }

    @Test
    fun insertBlobWithOnConflict(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar BLOB)", emptyArray())
            val values = ContentValues().apply { put("bar", ByteArray(1) { 42 }) }
            val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals(1L, rowId)
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
                assertEquals(1, it.count)
                assertTrue(it.moveToFirst())
                val blob = it.getBlob(0)
                assertArrayEquals(ByteArray(1) { 42 }, blob)
                run { assertSame(blob, it.getBlob(0)) }
            }
        }
    }

    @Test(expected = IllegalStateException::class)
    fun execAfterDatabaseHasClosed(): Unit = database.run {
        close()
        assertFalse(isOpen())
        exec("CREATE TABLE 'Foo' (bar BLOB)", emptyArray())
    }

    @Test
    fun query(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
                assertEquals(1, it.count)
                assertEquals(0, it.columnIndex("bar"))
                assertTrue(it.moveToFirst())
                assertEquals(42, it.getInt(0))
                assertFalse(it.moveToNext())
            }
        }
    }

    @Test
    fun rawQuery(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            query("SELECT * FROM 'Foo'", emptyArray()).use {
                assertEquals(1, it.count)
                assertEquals(0, it.columnIndex("bar"))
                assertTrue(it.moveToFirst())
                assertEquals(42, it.getInt(0))
                assertFalse(it.moveToNext())
            }
        }
    }

    @Test
    fun updateWithOnConflict(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            update(
                "Foo", ContentValues().apply { put("bar", 43) }, "bar=?", arrayOf("42"),
                ConflictAlgorithm.REPLACE
            )
            query("SELECT * FROM 'Foo'", emptyArray()).use {
                assertTrue(it.moveToFirst())
                assertEquals(43, it.getInt(0))
                assertFalse(it.moveToNext())
            }
        }
    }

    @Test
    fun version() {
        database.version = 42
        assertEquals(42, database.version)
    }

    @Test
    fun directReadInsideTransaction(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
                assertEquals(1, it.count)
            }
        }
    }
}
