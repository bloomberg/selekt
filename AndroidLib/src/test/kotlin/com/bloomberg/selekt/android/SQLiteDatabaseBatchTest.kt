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

import com.bloomberg.selekt.annotations.Experimental
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.commons.deleteDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(Experimental::class)
internal class SQLiteDatabaseBatchTest {
    private val file = createTempFile("test-sql-database-batch", ".db").toFile().also { it.deleteOnExit() }

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
    fun batchSequenceInsert(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        assertEquals(2, batch("INSERT INTO 'Foo' VALUES (?)", sequenceOf(arrayOf(42), arrayOf(43))))
    }

    @Test
    fun batchSequenceInsertWithCommonArray(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        val args = Array(1) { 0 }
        assertEquals(2, batch("INSERT INTO 'Foo' VALUES (?)", sequence {
            args[0] = 42
            yield(args)
            args[0] = 43
            yield(args)
        }))
        query("SELECT * FROM 'Foo'", null).use {
            assertEquals(2, it.count)
            assertTrue(it.moveToFirst())
            assertEquals(42, it.getInt(0))
            assertTrue(it.moveToNext())
            assertEquals(43, it.getInt(0))
        }
    }

    @Test
    fun batchSequenceInsertBooleanTrueThrows(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        assertThrows<IllegalArgumentException> {
            batch("INSERT INTO 'Foo' VALUES (?)", sequenceOf(arrayOf(true)))
        }
    }

    @Test
    fun batchSequenceInsertBooleanFalseThrows(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        assertThrows<IllegalArgumentException> {
            batch("INSERT INTO 'Foo' VALUES (?)", sequenceOf(arrayOf(false)))
        }
    }

    @Test
    fun batchSequenceInsertAnyFails(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar TEXT)", emptyArray())
        assertThrows<IllegalArgumentException> {
            batch("INSERT INTO 'Foo' VALUES (?)", sequenceOf(arrayOf(Any())))
        }
    }

    @Test
    fun batchRequiresUpdate(): Unit = database.run {
        assertThrows<IllegalArgumentException> {
            batch("SELECT * FROM Foo", sequence { })
        }
    }
}
