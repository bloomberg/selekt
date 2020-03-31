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
import android.database.sqlite.SQLiteException
import com.bloomberg.commons.deleteDatabase
import com.bloomberg.commons.times
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SimpleSQLQuery
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@Suppress("ArrayInDataClass")
internal data class SQLInputs(
    val journalMode: SQLiteJournalMode,
    val key: ByteArray?
) {
    override fun toString() = "$journalMode;${if (key != null) "keyed" else "not-keyed"}"
}

@RunWith(Parameterized::class)
internal class SQLDatabaseTest(private val inputs: SQLInputs) {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    private val file = File.createTempFile("test-sql-database", ".db").also { it.deleteOnExit() }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun initParameters(): Iterable<SQLInputs> = (SQLiteJournalMode.values().filter { it != SQLiteJournalMode.MEMORY } *
            arrayOf(ByteArray(32) { 0x42 }, null)).map {
            SQLInputs(it.first, it.second)
        }
    }

    private val database = SQLDatabase(file.absolutePath, SQLite, inputs.journalMode.databaseConfiguration, inputs.key)

    @Before
    fun setUp() {
        database.pragma("journal_mode", inputs.journalMode)
    }

    @After
    fun tearDown() {
        database.run {
            try {
                if (isOpen()) {
                    close()
                }
                assertFalse(isOpen())
            } finally {
                assertTrue(deleteDatabase(file))
            }
        }
    }

    @Test
    fun delete(): Unit = database.run {
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
    fun deleteAll(): Unit = database.run {
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

    @Test
    fun exec() {
        database.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
    }

    @Test(expected = SQLException::class)
    fun execInvalidSQL() {
        database.exec("NOT SQL", emptyArray())
    }

    @Test
    fun executeCompileStatement(): Unit = database.run {
        val statement = compileStatement("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        statement.execute()
        val rowId = insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @Test
    fun executeInsertCompileStatement(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val statement = compileStatement("INSERT INTO 'Foo' VALUES (42)", emptyArray())
        val rowId = statement.executeInsert()
        assertEquals(1L, rowId)
    }

    @Test
    fun executeUpdateDeleteCompileStatement(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        val statement = compileStatement("DELETE FROM 'Foo' WHERE bar=42", emptyArray())
        val count = statement.executeUpdateDelete()
        assertEquals(1, count)
    }

    @Test(expected = IllegalStateException::class)
    fun execAfterDatabaseHasClosed(): Unit = database.run {
        close()
        assertFalse(isOpen())
        exec("CREATE TABLE 'Foo' (bar BLOB)", emptyArray())
    }

    @Test
    fun executeStatementsOnAnotherThread(): Unit = database.run {
        val statement = compileStatement("CREATE TABLE 'Foo' (bar INT)")
        Thread {
            transact { statement.execute() }
        }.let {
            it.isDaemon = true
            it.start()
            it.join(1_000L)
        }
    }

    @Test
    fun simpleQueryForStringCompileStatement(): Unit = database.run {
        val statement = compileStatement("PRAGMA journal_mode", emptyArray())
        assertEquals(inputs.journalMode.name, statement.simpleQueryForString()?.toUpperCase(Locale.US))
    }

    @Test
    fun simpleQueryForNullStringCompileStatement(): Unit = database.run {
        val statement = compileStatement("SELECT null", emptyArray())
        assertNull(statement.simpleQueryForString())
    }

    @Test
    fun insertIntWithOnConflict(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42) }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @Test
    fun insertStringWithOnConflict(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        val values = ContentValues().apply { put("bar", "xyz") }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @Test
    fun insertFloatAsIntWithOnConflict(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42.0f) }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertTrue(it.getInt(0) in 41..42)
        }
    }

    @Test
    fun insertIntAsFloatWithOnConflict(): Unit = database.run {
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
    fun insertFloatWithOnConflictGetAsInt(): Unit = database.run {
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
    fun insertIntWithOnConflictMultipleTimes(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        arrayOf(42, 43, 44, 45).forEachIndexed { index, it ->
            val values = ContentValues().apply { put("bar", it) }
            val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals((index + 1).toLong(), rowId)
        }
    }

    @Test
    fun insertBlobWithOnConflict(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar BLOB)", emptyArray())
        val values = ContentValues().apply { put("bar", ByteArray(1) { 42 }) }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            val blob = it.getBlob(0)
            Assert.assertArrayEquals(ByteArray(1) { 42 }, blob)
            run { assertSame(blob, it.getBlob(0)) }
        }
    }

    @Test
    fun insertNullWithOnConflict(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        val values = ContentValues().apply { putNull("bar") }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertTrue(it.isNull(0))
        }
    }

    @Test
    fun insertJapaneseText(): Unit = database.run {
        val text = "日本語"
        exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        val values = ContentValues().apply { put("bar", text) }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertEquals(text, it.getString(0))
        }
    }

    @Test
    fun insertBlobGetByColumnName(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar BLOB)")
        val values = ContentValues().apply { put("bar", ByteArray(1) { 42 }) }
        val rowId = insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            val index = it.columnIndex("bar")
            assertTrue(index > -1)
            val blob = it.getBlob(index)
            Assert.assertArrayEquals(ByteArray(1) { 42 }, blob)
            run { assertSame(blob, it.getBlob(0)) }
        }
    }

    @Test
    fun insertAndGetBlobWithStatement(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar BLOB)")
        val statement = compileStatement("INSERT INTO Foo VALUES (?)")
        statement.bindBlob(1, ByteArray(1) { 42 })
        assertEquals(1L, statement.executeInsert())
        query(SimpleSQLQuery("SELECT * FROM Foo")).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            val index = it.columnIndex("bar")
            assertTrue(index > -1)
            val blob = it.getBlob(index)
            Assert.assertArrayEquals(ByteArray(1) { 42 }, blob)
            run { assertSame(blob, it.getBlob(0)) }
        }
    }

    @Test
    fun insertAsCompiledStatement(): Unit = database.run {
        val text = "greetings"
        exec("CREATE TABLE 'Foo' (bar TEXT)")
        val statement = compileStatement("INSERT INTO 'Foo' VALUES (?)")
        statement.bindString(1, text)
        assertEquals(1L, statement.executeInsert())
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertEquals(text, it.getString(0))
        }
    }

    @Test
    fun insertAsCompiledStatementWithTableBind(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT)")
        assertThatExceptionOfType(SQLiteException::class.java).isThrownBy {
            compileStatement("INSERT INTO ? VALUES (?)")
        }
    }

    @Test
    fun upsertString(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT PRIMARY KEY, count INT DEFAULT 0)")
        val values = ContentValues().apply { put("bar", "hello") }
        insert("Foo", values, ConflictAlgorithm.REPLACE)
        upsert("Foo", values, arrayOf("bar"), "count=count+1")
        query(false, "Foo", arrayOf("count"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            assertEquals(1, it.getInt(0))
        }
    }

    @Test
    fun query(): Unit = database.run {
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
    fun queryMultiple(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT, xyz INT)", emptyArray())
        insert("Foo", ContentValues().apply {
            put("bar", 42)
            put("xyz", 43)
        }, ConflictAlgorithm.REPLACE)
        query(false, "Foo", arrayOf("bar", "xyz"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
            assertEquals(0, it.columnIndex("bar"))
            assertEquals(1, it.columnIndex("xyz"))
            assertTrue(it.moveToFirst())
            assertEquals(42, it.getInt(0))
            assertEquals(43, it.getInt(1))
            assertFalse(it.moveToNext())
        }
    }

    @Test
    fun queryDistinct(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        repeat(2) {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        }
        query(true, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertEquals(1, it.count)
        }
    }

    @Test
    fun queryAll(): Unit = database.run {
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

    @Test(expected = SQLException::class)
    fun queryTableWithEmptyName(): Unit = database.run {
        query(false, "", emptyArray(), "", emptyArray(), null, null, null, null).use {}
    }

    @Test
    fun queryQueryNoArgs(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        query(SimpleSQLQuery("SELECT * FROM 'Foo'")).use {
            assertEquals(1, it.count)
            assertEquals(0, it.columnIndex("bar"))
            assertTrue(it.moveToFirst())
            assertEquals(42, it.getInt(0))
            assertFalse(it.moveToNext())
        }
    }

    @Test
    fun queryQueryWithArgs(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        query(SimpleSQLQuery("SELECT * FROM 'Foo' WHERE bar=?", arrayOf(42))).use {
            assertEquals(1, it.count)
            assertEquals(0, it.columnIndex("bar"))
            assertTrue(it.moveToFirst())
            assertEquals(42, it.getInt(0))
            assertFalse(it.moveToNext())
        }
    }

    @Test
    fun rawQuery(): Unit = database.run {
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
    fun rawQueryWithPadding(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        query("          SELECT   *   FROM    'Foo'          ", emptyArray()).use {
            assertEquals(1, it.count)
        }
    }

    @Test
    fun queryThenAlterThenQueryIgnoresSchemaChange(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        query("SELECT * FROM 'Foo'", emptyArray()).use {
            assertEquals(1, it.columnCount)
        }
        exec("ALTER TABLE 'Foo' ADD COLUMN xyz INT")
        query("SELECT * FROM 'Foo'", emptyArray()).use {
            assertEquals(1, it.columnCount)
        }
    }

    @Test
    fun insertAsQuery(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        query("INSERT INTO 'Foo' VALUES (42)", emptyArray()).close()
        query("SELECT * FROM 'Foo'", emptyArray()).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
        }
    }

    @Test
    fun updateWithOnConflict(): Unit = database.run {
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
    fun insertBigInt(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar BIGINT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", Long.MAX_VALUE) },
            ConflictAlgorithm.REPLACE)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertTrue(it.moveToFirst())
            assertEquals(Long.MAX_VALUE, it.getLong(0))
            assertFalse(it.moveToNext())
        }
    }

    @Test
    fun version() = database.run {
        version = 42
        assertEquals(42, version)
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

    @Test
    fun readConnectionIsReturnedToReadPool(): Unit = database.run {
        query("SELECT date()", emptyArray())
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
    }

    @Test
    fun secureDeleteIsFast(): Unit = database.run {
        assertEquals(2, pragma("secure_delete").toInt())
    }
}
