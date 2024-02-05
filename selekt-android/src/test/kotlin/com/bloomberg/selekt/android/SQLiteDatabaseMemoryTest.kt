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
import com.bloomberg.selekt.SQLiteJournalMode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class SQLiteDatabaseMemoryTest {
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
}
