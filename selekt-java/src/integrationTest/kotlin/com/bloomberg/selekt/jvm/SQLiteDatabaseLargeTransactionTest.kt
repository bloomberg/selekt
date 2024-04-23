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
import com.bloomberg.selekt.SQLiteJournalMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.Locale
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CREATE_TABLE_SQL = "CREATE TABLE 'Foo' (bar TEXT)"
private const val INSERT_SQL = "INSERT INTO 'Foo' VALUES (?)"

private val text = "a".repeat(1_000)
private val args = arrayOf(text)

@Timeout(value = 1L, unit = TimeUnit.HOURS)
internal class SQLiteDatabaseLargeTransactionTest {
    private val file = createTempFile("test-large-transaction", ".db").toFile().apply { deleteOnExit() }

    private val database = openOrCreateDatabase(
        file,
        SQLiteJournalMode.WAL.databaseConfiguration.copy(
            evictionDelayMillis = 50L,
            timeBetweenEvictionRunsMillis = 100L
        ),
        null
    ).apply {
        pragma("journal_mode", SQLiteJournalMode.WAL)
    }

    @BeforeEach
    fun setUp() {
        assertEquals(SQLiteJournalMode.WAL.toString(), database.pragma("journal_mode").uppercase(Locale.US))
        database.exec(CREATE_TABLE_SQL)
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
        WeightedTask(4) { delay(ThreadLocalRandom.current().nextLong(80L, 150L)) },
        WeightedTask(4) {
            transact {
                repeat(1_000) {
                    exec(INSERT_SQL, args)
                }
            }
        },
        WeightedTask(2) { exec(INSERT_SQL, args) },
        WeightedTask(1) {
            transact {
                exec("DROP TABLE 'Foo'")
                exec(CREATE_TABLE_SQL)
            }
        },
        WeightedTask(12) { query("SELECT * FROM 'Foo'", emptyArray()).use {} }
    )

    @Test
    fun largeTransactions(): Unit = runBlocking {
        coroutineScope {
            repeat(3) {
                launch(Dispatchers.IO) {
                    repeat(4_000) { tasks.randomTask()(database) }
                }
            }
        }
    }
}
