/*
 * Copyright 2020 Bloomberg Finance L.P.
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
import com.bloomberg.selekt.CancellationSignal
import com.bloomberg.selekt.SimpleSQLQuery
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SQLiteTraceEventMode
import java.io.File
import java.util.stream.Stream
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.io.TempDir

internal class SQLiteDatabaseMemoryTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var database: SQLiteDatabase

    @BeforeEach
    fun setUp() {
        database = SQLiteDatabase.createInMemoryDatabase()
    }

    @AfterEach
    fun tearDown() {
        database.run {
            close()
            assertFalse(isOpen)
        }
    }

    @Test
    fun journalMode(): Unit = database.run {
        assertEquals(SQLiteJournalMode.MEMORY, journalMode)
    }

    @Test
    fun vacuum(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar INT)")
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        }
        database.vacuum()
    }

    @Test
    fun version() {
        database.version = 42
        assertEquals(42, database.version)
    }

    @Test
    fun configurationAndVacuumWrappers() {
        database.pageSize = 4_096
        database.setForeignKeyConstraintsEnabled(true)
        database.setForeignKeyConstraintsEnabled(false)
        database.incrementalVacuum()
        database.incrementalVacuum(1)
    }

    @Test
    fun cancellableAndDefaultedQueryWrappers() {
        database.exec("CREATE TABLE Foo (bar INTEGER)")
        database.batch("INSERT INTO Foo VALUES (?)", Stream.of(arrayOf<Any?>(42)))

        database.query(false, "Foo", arrayOf("bar"), null, null).close()
        database.query(
            distinct = false,
            table = "Foo",
            columns = arrayOf("bar"),
            selection = null,
            selectionArgs = null,
            cancellationSignal = CancellationSignal()
        ).close()
        database.query("SELECT * FROM Foo", emptyArray(), CancellationSignal()).close()
        database.query(SimpleSQLQuery("SELECT * FROM Foo"), CancellationSignal()).close()
    }

    @Test
    fun tracedInMemoryAndDeleteDatabaseStaticWrappers() {
        SQLiteDatabase.createInMemoryDatabase(SQLiteTraceEventMode()).close()
        val first = File(tempDir, "first.db").apply { writeText("") }
        assertTrue(SQLiteDatabase.deleteDatabase(first))
        val second = File(tempDir, "second.db").apply { writeText("") }
        assertTrue(SQLiteDatabase.Companion.deleteDatabase(second))
    }
}
