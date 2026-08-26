/*
 * Copyright 2026 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android.room

import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.jupiter.SelektTestExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(SelektTestExtension::class)
internal class SelektSQLiteDriverTest {
    @Test
    fun createDriverWithoutKey() {
        val connection = createSelektSQLiteDriver().open(":memory:")
        connection.use {
            val statement = connection.prepare("SELECT 1")
            statement.use {
                assertTrue(statement.step())
                assertEquals(1L, statement.getLong(0))
            }
        }
    }

    @Test
    fun createDriverWithKey() {
        val key = ByteArray(32) { 0x42 }
        val connection = createSelektSQLiteDriver(key = key).open(":memory:")
        connection.use {
            val statement = connection.prepare("SELECT 1")
            statement.use {
                assertTrue(statement.step())
                assertEquals(1L, statement.getLong(0))
            }
        }
    }

    @Test
    fun createDriverWithDifferentJournalMode() {
        val connection = createSelektSQLiteDriver(journalMode = SQLiteJournalMode.TRUNCATE).open(":memory:")
        connection.use {
            val statement = connection.prepare("PRAGMA journal_mode")
            statement.use {
                assertTrue(statement.step())
                assertEquals("memory", statement.getText(0).lowercase())
            }
        }
    }

    @Test
    fun prepareAndExecuteStatement() {
        val connection = createSelektSQLiteDriver().open(":memory:")
        connection.use {
            val createTable = connection.prepare("CREATE TABLE test (id INTEGER PRIMARY KEY, value TEXT)")
            createTable.use {
                assertFalse(createTable.step())
                createTable.reset()
            }

            val insert = connection.prepare("INSERT INTO test (id, value) VALUES (?, ?)")
            insert.use {
                insert.bindLong(1, 1)
                insert.bindText(2, "hello")
                assertFalse(insert.step())
                insert.reset()
            }

            val select = connection.prepare("SELECT id, value FROM test WHERE id = ?")
            select.use {
                select.bindLong(1, 1)
                assertTrue(select.step())
                assertEquals(1L, select.getLong(0))
                assertEquals("hello", select.getText(1))
            }
        }
    }

    @Test
    fun driverHasConnectionPool() {
        val driver = createSelektSQLiteDriver()
        assertTrue(driver.hasConnectionPool)
    }

    @Test
    fun multipleConnections() {
        val driver = createSelektSQLiteDriver()
        val connection1 = driver.open(":memory:")
        val connection2 = driver.open(":memory:")
        connection1.use {
            connection2.use {
                val stmt1 = connection1.prepare("SELECT 1")
                val stmt2 = connection2.prepare("SELECT 2")

                stmt1.use {
                    assertTrue(stmt1.step())
                    assertEquals(1L, stmt1.getLong(0))
                }

                stmt2.use {
                    assertTrue(stmt2.step())
                    assertEquals(2L, stmt2.getLong(0))
                }
            }
        }
    }
}
