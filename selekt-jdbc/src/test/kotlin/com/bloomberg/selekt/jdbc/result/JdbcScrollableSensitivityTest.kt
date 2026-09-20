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

package com.bloomberg.selekt.jdbc.result

import java.nio.file.Path
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class JdbcScrollableSensitivityTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun scrollSensitiveRefillObservesInterveningWrite() {
        val url = "jdbc:sqlite:${tempDir.resolve("scroll-sensitive.db")}?poolSize=2&cursorWindowSize=2"
        DriverManager.getConnection(url).use { reader ->
            reader.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE entries (id INTEGER PRIMARY KEY, value TEXT NOT NULL)")
                repeat(6) { index ->
                    statement.executeUpdate("INSERT INTO entries VALUES (${index + 1}, 'row${index + 1}')")
                }
            }

            reader.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY).use { statement ->
                statement.executeQuery("SELECT id, value FROM entries ORDER BY id").use { resultSet ->
                    assertEquals(ResultSet.TYPE_SCROLL_SENSITIVE, resultSet.type)
                    assertTrue(resultSet.next())
                    assertEquals("row1", resultSet.getString("value"))

                    updateFifthRow(url)

                    assertTrue(resultSet.absolute(5))
                    assertEquals(5, resultSet.getInt("id"))
                    assertEquals("changed", resultSet.getString("value"))
                }
            }
        }
    }

    private fun updateFifthRow(url: String) {
        DriverManager.getConnection(url).use { writer ->
            writer.createStatement().use {
                assertEquals(1, it.executeUpdate("UPDATE entries SET value = 'changed' WHERE id = 5"))
            }
        }
    }
}
