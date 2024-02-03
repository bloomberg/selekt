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
import android.database.sqlite.SQLiteException
import com.bloomberg.selekt.commons.times
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SimpleSQLQuery
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.kotlin.mock
import java.util.Locale
import java.util.stream.Stream
import kotlin.io.path.createTempFile
import kotlin.math.roundToInt
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@Suppress("ArrayInDataClass")
internal data class SQLInputs(
    val journalMode: SQLiteJournalMode,
    val key: ByteArray?
) {
    override fun toString() = "$journalMode-${if (key != null) "keyed" else "not-keyed"}"
}

private fun createFile(
    input: SQLInputs
) = createTempFile("test-sql-database-$input-", ".db").toFile()

internal class SampleSQLArgumentsProvider : ArgumentsProvider {
    override fun provideArguments(
        context: ExtensionContext
    ): Stream<out Arguments> = (SQLiteJournalMode.entries.filter { it != SQLiteJournalMode.MEMORY } *
        arrayOf(ByteArray(32) { 0x42 }, null)).map {
        SQLInputs(it.first, it.second)
    }.map { Arguments.of(it) }.stream()
}

internal class SQLDatabaseTest {
    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun delete(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        assertEquals(1, it.delete("Foo", "bar=?", arrayOf("42")))
        it.query(false, "Foo", emptyArray(), "", emptyArray(), null, null, null, null).use { cursor ->
            assertFalse(cursor.moveToFirst())
            assertEquals(0, cursor.count)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun deleteAll(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        arrayOf(42, 43, 44, 45).forEachIndexed { index, value ->
            val values = ContentValues().apply { put("bar", value) }
            val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals((index + 1).toLong(), rowId)
        }
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(4, cursor.count)
        }
        assertEquals(4, it.delete("Foo", "", emptyArray()))
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(0, cursor.count)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun exec(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun execInvalidSQL(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        assertFailsWith<SQLException> {
            it.exec("NOT SQL", emptyArray())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun executeCompileStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        val statement = it.compileStatement("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        statement.execute()
        val rowId = it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun executeInsertCompileStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val statement = it.compileStatement("INSERT INTO 'Foo' VALUES (42)", emptyArray())
        val rowId = statement.executeInsert()
        assertEquals(1L, rowId)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun executeInsertNumberCompileStatementThrows(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        val statement = it.compileStatement("INSERT INTO 'Foo' VALUES (?)", arrayOf(mock<Number>()))
        assertFailsWith<IllegalArgumentException> {
            statement.executeInsert()
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun executeUpdateDeleteCompileStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        val statement = it.compileStatement("DELETE FROM 'Foo' WHERE bar=42", emptyArray())
        val count = statement.executeUpdateDelete()
        assertEquals(1, count)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun execAfterDatabaseHasClosed(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.close()
        assertFalse(it.isOpen())
        assertFailsWith<IllegalStateException> {
            it.exec("CREATE TABLE 'Foo' (bar BLOB)", emptyArray())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun executeStatementsOnAnotherThread(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        val statement = it.compileStatement("CREATE TABLE 'Foo' (bar INT)")
        Thread {
            it.transact { statement.execute() }
        }.run {
            isDaemon = true
            start()
            join(1_000L)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun simpleQueryForStringCompileStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        val statement = it.compileStatement("PRAGMA journal_mode", emptyArray())
        assertEquals(inputs.journalMode.name, statement.simpleQueryForString()?.uppercase(Locale.US))
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun simpleQueryForNullStringCompileStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        val statement = it.compileStatement("SELECT null", emptyArray())
        assertNull(statement.simpleQueryForString())
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertByteWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42.toByte()) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertDoubleWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar DOUBLE)", emptyArray())
        val values = ContentValues().apply { put("bar", 42.0) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertIntWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertLongWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42L) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertShortWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42.toShort()) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertStringWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        val values = ContentValues().apply { put("bar", "xyz") }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertFloatAsIntWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42.0f) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.getInt(0) in 41..42)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertIntAsFloatWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar FLOAT)", emptyArray())
        val values = ContentValues().apply { put("bar", 42) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals(42, cursor.getFloat(0).roundToInt())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertFloatWithOnConflictGetAsInt(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar Float)", emptyArray())
        val values = ContentValues().apply { put("bar", 42.0f) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals(42, cursor.getInt(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertIntWithOnConflictMultipleTimes(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        arrayOf(42, 43, 44, 45).forEachIndexed { index, value ->
            val values = ContentValues().apply { put("bar", value) }
            val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
            assertEquals((index + 1).toLong(), rowId)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertBlobWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar BLOB)", emptyArray())
        val values = ContentValues().apply { put("bar", ByteArray(1) { 42 }) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            val blob = cursor.getBlob(0)
            assertContentEquals(ByteArray(1) { 42 }, blob)
            run { assertSame(blob, cursor.getBlob(0)) }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertNullWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        val values = ContentValues().apply { putNull("bar") }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertJapaneseText(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        val text = "日本語"
        it.exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        val values = ContentValues().apply { put("bar", text) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals(text, cursor.getString(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertBlobGetByColumnName(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar BLOB)")
        val values = ContentValues().apply { put("bar", ByteArray(1) { 42 }) }
        val rowId = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(1L, rowId)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            val index = cursor.columnIndex("bar")
            assertTrue(index > -1)
            val blob = cursor.getBlob(index)
            assertContentEquals(ByteArray(1) { 42 }, blob)
            run { assertSame(blob, cursor.getBlob(0)) }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertAndGetBlobWithStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar BLOB)")
        val statement = it.compileStatement("INSERT INTO Foo VALUES (?)")
        statement.bindBlob(1, ByteArray(1) { 42 })
        assertEquals(1L, statement.executeInsert())
        it.query(SimpleSQLQuery("SELECT * FROM Foo")).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            val index = cursor.columnIndex("bar")
            assertTrue(index > -1)
            val blob = cursor.getBlob(index)
            assertContentEquals(ByteArray(1) { 42 }, blob)
            run { assertSame(blob, cursor.getBlob(0)) }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertDoubleAsCompiledStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        val value = 42.0
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        val statement = it.compileStatement("INSERT INTO 'Foo' VALUES (?)")
        statement.bindDouble(1, value)
        assertEquals(1L, statement.executeInsert())
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals(value, cursor.getDouble(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertIntAsCompiledStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        val value = 42
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        val statement = it.compileStatement("INSERT INTO 'Foo' VALUES (?)")
        statement.bindInt(1, value)
        assertEquals(1L, statement.executeInsert())
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals(value, cursor.getInt(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertNullAsCompiledStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar TEXT)")
        val statement = it.compileStatement("INSERT INTO 'Foo' VALUES (?)")
        statement.bindString(1, "knock me out")
        statement.bindNull(1)
        assertEquals(1L, statement.executeInsert())
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertStringAsCompiledStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        val text = "greetings"
        it.exec("CREATE TABLE 'Foo' (bar TEXT)")
        val statement = it.compileStatement("INSERT INTO 'Foo' VALUES (?)")
        statement.bindString(1, text)
        assertEquals(1L, statement.executeInsert())
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals(text, cursor.getString(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertStringAsCompiledStatementWithTableBind(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar TEXT)")
        assertFailsWith<SQLiteException> {
            it.compileStatement("INSERT INTO ? VALUES (?)")
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun clearCompiledStatement(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar TEXT)")
        val statement = it.compileStatement("INSERT INTO 'Foo' VALUES (?)")
        statement.bindString(1, "abc")
        statement.clearBindings()
        assertEquals(1L, statement.executeInsert())
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun closeCompiledStatementClears(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar TEXT)")
        val statement = it.compileStatement("INSERT INTO 'Foo' VALUES (?)")
        statement.bindString(1, "abc")
        statement.close()
        assertEquals(1L, statement.executeInsert())
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun upsertString(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar TEXT PRIMARY KEY, count INT DEFAULT 0)")
        val values = ContentValues().apply { put("bar", "hello") }
        val id = it.insert("Foo", values, ConflictAlgorithm.REPLACE)
        assertEquals(id, it.upsert("Foo", values, arrayOf("bar"), "count=count+1"))
        it.query(false, "Foo", arrayOf("count"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun query(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertEquals(0, cursor.columnIndex("bar"))
            assertTrue(cursor.moveToFirst())
            assertEquals(42, cursor.getInt(0))
            assertFalse(cursor.moveToNext())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun queryMultiple(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT, xyz INT)", emptyArray())
        it.insert("Foo", ContentValues().apply {
            put("bar", 42)
            put("xyz", 43)
        }, ConflictAlgorithm.REPLACE)
        it.query(false, "Foo", arrayOf("bar", "xyz"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertEquals(0, cursor.columnIndex("bar"))
            assertEquals(1, cursor.columnIndex("xyz"))
            assertTrue(cursor.moveToFirst())
            assertEquals(42, cursor.getInt(0))
            assertEquals(43, cursor.getInt(1))
            assertFalse(cursor.moveToNext())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun queryDistinct(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        repeat(2) { _ ->
            it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        }
        it.query(true, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun queryAll(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertEquals(1, cursor.count)
            assertEquals(0, cursor.columnIndex("bar"))
            assertTrue(cursor.moveToFirst())
            assertEquals(42, cursor.getInt(0))
            assertFalse(cursor.moveToNext())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun queryTableWithEmptyName(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        assertFailsWith<SQLException> {
            it.query(false, "", emptyArray(), "", emptyArray(), null, null, null, null).use {}
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun queryQueryNoArgs(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        it.query(SimpleSQLQuery("SELECT * FROM 'Foo'")).use { cursor ->
            assertEquals(1, cursor.count)
            assertEquals(0, cursor.columnIndex("bar"))
            assertTrue(cursor.moveToFirst())
            assertEquals(42, cursor.getInt(0))
            assertFalse(cursor.moveToNext())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun queryQueryWithArgs(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        it.query(SimpleSQLQuery("SELECT * FROM 'Foo' WHERE bar=?", arrayOf(42))).use { cursor ->
            assertEquals(1, cursor.count)
            assertEquals(0, cursor.columnIndex("bar"))
            assertTrue(cursor.moveToFirst())
            assertEquals(42, cursor.getInt(0))
            assertFalse(cursor.moveToNext())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun rawQuery(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        it.query("SELECT * FROM 'Foo'", emptyArray()).use { cursor ->
            assertEquals(1, cursor.count)
            assertEquals(0, cursor.columnIndex("bar"))
            assertTrue(cursor.moveToFirst())
            assertEquals(42, cursor.getInt(0))
            assertFalse(cursor.moveToNext())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun rawQueryWithPadding(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        it.query("          SELECT   *   FROM    'Foo'          ", emptyArray()).use { cursor ->
            assertEquals(1, cursor.count)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun queryThenAlterThenQueryIgnoresSchemaChange(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.query("SELECT * FROM 'Foo'", emptyArray()).use { cursor ->
            assertEquals(1, cursor.columnCount)
        }
        it.exec("ALTER TABLE 'Foo' ADD COLUMN xyz INT")
        it.query("SELECT * FROM 'Foo'", emptyArray()).use { cursor ->
            assertEquals(1, cursor.columnCount)
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun queryByChar(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar TEXT)")
        it.insert("Foo", ContentValues().apply { put("bar", "x") }, ConflictAlgorithm.REPLACE)
        assertFailsWith<IllegalArgumentException> {
            it.query("SELECT * FROM 'Foo' WHERE bar=?", arrayOf('x')).close()
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertAsQuery(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.query("INSERT INTO 'Foo' VALUES (42)", emptyArray()).close()
        it.query("SELECT * FROM 'Foo'", emptyArray()).use { cursor ->
            assertEquals(1, cursor.count)
            assertTrue(cursor.moveToFirst())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun updateWithOnConflict(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        it.update(
            "Foo", ContentValues().apply { put("bar", 43) }, "bar=?", arrayOf("42"),
            ConflictAlgorithm.REPLACE
        )
        it.query("SELECT * FROM 'Foo'", emptyArray()).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(43, cursor.getInt(0))
            assertFalse(cursor.moveToNext())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun simpleQueryForLong(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        val value = 42L
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(value))
        it.compileStatement("SELECT * FROM 'Foo' LIMIT 1").use { statement ->
            assertEquals(value, statement.simpleQueryForLong())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertBigInt(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar BIGINT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", Long.MAX_VALUE) },
            ConflictAlgorithm.REPLACE)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(Long.MAX_VALUE, cursor.getLong(0))
            assertFalse(cursor.moveToNext())
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun version(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.version = 42
        assertEquals(42, it.version)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun directReadInsideTransaction(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.transact {
            exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
                assertEquals(1, cursor.count)
            }
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun readConnectionIsReturnedToReadPool(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.query("SELECT date()", emptyArray())
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun secureDeleteIsFast(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        assertEquals(2, it.pragma("secure_delete").toInt())
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun batchInsert(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        assertEquals(2, it.batch("INSERT INTO 'Foo' VALUES (?)", sequenceOf(arrayOf(42), arrayOf(43))))
    }

    @ParameterizedTest
    @ArgumentsSource(SampleSQLArgumentsProvider::class)
    fun insertOneHundredUnboundThenOneBound(
        inputs: SQLInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        inputs.key
    ).destroy {
        it.pragma("journal_mode", inputs.journalMode)
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        repeat(100) { i -> it.exec("INSERT INTO 'Foo' VALUES ($i)") }
        it.exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(200))
        it.query("SELECT * FROM 'Foo'", emptyArray()).use { cursor ->
            repeat(100) { i ->
                assertTrue(cursor.moveToNext())
                assertEquals(i, cursor.getInt(0))
            }
            assertTrue(cursor.moveToNext())
            assertEquals(200, cursor.getInt(0))
        }
    }
}
