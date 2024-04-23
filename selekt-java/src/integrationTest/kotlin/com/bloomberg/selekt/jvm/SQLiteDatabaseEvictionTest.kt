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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.assertFalse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.Locale
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val body = "a".repeat(1_000_000)

@Timeout(value = 1L, unit = TimeUnit.HOURS)
internal class SQLiteDatabaseEvictionTest {
    private val file = createTempFile("test-eviction", ".db").toFile().apply { deleteOnExit() }

    private val database = openOrCreateDatabase(
        file,
        SQLiteJournalMode.WAL.databaseConfiguration.copy(
            evictionDelayMillis = 25L,
            timeBetweenEvictionRunsMillis = 50L
        ),
        null
    ).apply {
        pragma("journal_mode", SQLiteJournalMode.WAL)
    }

    @BeforeEach
    fun setUp() {
        assertEquals(SQLiteJournalMode.WAL.toString(), database.pragma("journal_mode").uppercase(Locale.US))
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
    fun evictions(): Unit = runBlocking {
        val values = ContentValues().apply { put("body", body) }
        coroutineScope {
            repeat(400) {
                database.exec("CREATE TABLE 'Foo' (body TEXT)")
                repeat(100) {
                    database.insert("Foo", values, ConflictAlgorithm.REPLACE)
                }
                delay(51L + ThreadLocalRandom.current().nextLong(25L))
                database.exec("DROP TABLE 'Foo'")
            }
        }
    }
}
