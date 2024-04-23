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

package com.bloomberg.selekt.jvm

import com.bloomberg.selekt.commons.deleteDatabase
import com.bloomberg.selekt.ConflictAlgorithm
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLiteJournalMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.assertFalse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Timeout(value = 1L, unit = TimeUnit.HOURS)
internal class SQLiteDatabaseThunderingHerdTest {
    private val file = createTempFile("test-thundering-herd", ".db").toFile().apply { deleteOnExit() }

    private val database = openOrCreateDatabase(
        file,
        SQLiteJournalMode.WAL.databaseConfiguration.copy(
            evictionDelayMillis = 100L,
            timeBetweenEvictionRunsMillis = 250L
        ),
        null
    ).apply {
        pragma("journal_mode", SQLiteJournalMode.WAL)
    }

    @BeforeEach
    fun setUp() {
        assertEquals(SQLiteJournalMode.WAL.toString(), database.pragma("journal_mode").uppercase(Locale.US))
        database.exec("CREATE TABLE 'Foo' (bar INT)")
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

    private val tasks = listOf(
        WeightedTask(2) {
            val values = ContentValues()
            transact {
                for (i in 0 until 100) {
                    values.put("bar", i)
                    insert("Foo", values, ConflictAlgorithm.REPLACE)
                }
            }
        },
        WeightedTask(1) { delete("Foo", "", emptyArray()) },
        WeightedTask(7) { query("SELECT * FROM 'Foo'", emptyArray()).use {} }
    )

    @Test
    fun thunder(): Unit = runBlocking {
        coroutineScope {
            repeat(1_000) {
                repeat(10) {
                    launch(Dispatchers.IO) { tasks.randomTask()(database) }
                }
                delay(500L)
            }
        }
    }
}
