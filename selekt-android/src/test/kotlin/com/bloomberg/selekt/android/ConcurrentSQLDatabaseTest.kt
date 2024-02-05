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

import com.bloomberg.selekt.commons.deleteDatabase
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ConcurrentSQLDatabaseTest {
    private val file = createTempFile("test-concurrent-sql-database", ".db").toFile().apply { deleteOnExit() }

    private val database = SQLDatabase(file.absolutePath, SQLite, SQLiteJournalMode.WAL.databaseConfiguration,
        ByteArray(32) { 0x42 })

    @BeforeEach
    fun setUp() {
        database.exec("PRAGMA journal_mode=${SQLiteJournalMode.WAL}")
    }

    @AfterEach
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
    fun readWhileWrite(): Unit = database.run {
        val hasFinished = AtomicBoolean(false)
        exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
        assertEquals(1L, insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE))
        transact {
            // Ensure the transaction has acquired the write connection.
            assertEquals(2L, insert("Foo", ContentValues().apply { put("bar", 43) }, ConflictAlgorithm.REPLACE))
            thread {
                query("SELECT * FROM Foo", emptyArray()).use {
                    assertEquals(1, it.count)
                }
                hasFinished.set(true)
            }.join()
        }
        assertTrue(hasFinished.get())
    }
}
