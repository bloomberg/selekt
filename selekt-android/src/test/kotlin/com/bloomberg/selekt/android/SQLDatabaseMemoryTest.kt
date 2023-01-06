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

import android.database.SQLException
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SQLDatabaseMemoryTest {
    private lateinit var database: SQLDatabase

    @BeforeEach
    fun setUp() {
        database = SQLDatabase("file::memory:", SQLite, SQLiteJournalMode.MEMORY.databaseConfiguration, null)
    }

    @AfterEach
    fun tearDown() {
        database.run {
            close()
            assertFalse(isOpen())
        }
    }

    @Test
    fun delete(): Unit = database.transact {
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

    @Test
    fun deleteAll(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        arrayOf(42, 43, 44, 45).forEachIndexed { index, value ->
            val values = ContentValues().apply { put("bar", value) }
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

    @Test
    fun exec() {
        database.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
    }

    @Test
    fun execInvalidSQL() {
        assertFailsWith<SQLException> {
            database.exec("NOT SQL", emptyArray())
        }
    }

    @Test
    fun insertIntWithOnConflict(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42) }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @Test
    fun insertLongWithOnConflict(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42L) }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @Test
    fun insertStringWithOnConflict(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        val values = ContentValues().apply { put("bar", "xyz") }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @Test
    fun insertFloatAsIntWithOnConflict(): Unit = database.transact {
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

    @Test
    fun insertIntAsFloatWithOnConflict(): Unit = database.transact {
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

    @Test
    fun insertFloatWithOnConflictGetAsInt(): Unit = database.transact {
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

    @Test
    fun insertIntWithOnConflictMultipleTimes(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        arrayOf(42, 43, 44, 45).forEachIndexed { index, value ->
            val values = ContentValues().apply { put("bar", value) }
            val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals((index + 1).toLong(), rowId)
        }
    }

    @Test
    fun insertBlobWithOnConflict(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar BLOB)", emptyArray())
        val values = ContentValues().apply { put("bar", ByteArray(1) { 42 }) }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            val blob = it.getBlob(0)
            assertContentEquals(ByteArray(1) { 42 }, blob)
            run { assertSame(blob, it.getBlob(0)) }
        }
    }

    @Test
    fun insertEmptyBlobWithOnConflict(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar BLOB)", emptyArray())
        val values = ContentValues().apply { put("bar", byteArrayOf()) }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            val blob = it.getBlob(0)
            assertNull(blob)
        }
    }

    @Test
    fun execAfterDatabaseHasClosed(): Unit = database.run {
        close()
        assertFalse(isOpen())
        assertFailsWith<IllegalStateException> {
            exec("CREATE TABLE 'Foo' (bar BLOB)", emptyArray())
        }
    }

    @Test
    fun queryInt(): Unit = database.transact {
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

    @Test
    fun queryLong(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", 42L) }, ConflictAlgorithm.REPLACE)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertEquals(0, it.columnIndex("bar"))
            assertTrue(it.moveToFirst())
            assertEquals(42L, it.getLong(0))
            assertFalse(it.moveToNext())
        }
    }

    @Test
    fun queryMaxLong(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", Long.MAX_VALUE) }, ConflictAlgorithm.REPLACE)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertEquals(0, it.columnIndex("bar"))
            assertTrue(it.moveToFirst())
            assertEquals(Long.MAX_VALUE, it.getLong(0))
            assertFalse(it.moveToNext())
        }
    }

    @Test
    fun queryMinLong(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", Long.MIN_VALUE) }, ConflictAlgorithm.REPLACE)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertEquals(0, it.columnIndex("bar"))
            assertTrue(it.moveToFirst())
            assertEquals(Long.MIN_VALUE, it.getLong(0))
            assertFalse(it.moveToNext())
        }
    }

    @Test
    fun rawQuery(): Unit = database.transact {
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

    @Test
    fun updateWithOnConflict(): Unit = database.transact {
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

    @Test
    fun version() {
        database.version = 42
        assertEquals(42, database.version)
    }

    @Test
    fun directReadInsideTransaction(): Unit = database.transact {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
        }
    }
}
