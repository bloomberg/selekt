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

package com.bloomberg.selekt.jdbc.statement

import java.nio.file.Path
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class JdbcMaxRowsTest {
    @TempDir
    lateinit var tempDir: Path

    private lateinit var url: String

    @BeforeEach
    fun setUp() {
        url = "jdbc:sqlite:${tempDir.resolve("max-rows.db")}?poolSize=1"
        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE entries (id INTEGER PRIMARY KEY)")
                repeat(10) { index ->
                    statement.executeUpdate("INSERT INTO entries VALUES (${index + 1})")
                }
            }
        }
    }

    @Test
    fun limitInLiteralDoesNotBypassMaximumRows() {
        assertMaximumRows("SELECT id, 'LIMIT 9' FROM entries ORDER BY id")
    }

    @Test
    fun limitInCommentDoesNotBypassMaximumRows() {
        assertMaximumRows("SELECT id FROM entries ORDER BY id -- LIMIT 9")
    }

    @Test
    fun nestedLimitDoesNotBypassMaximumRows() {
        assertMaximumRows(
            "SELECT id FROM entries WHERE id IN (SELECT id FROM entries LIMIT 9) ORDER BY id"
        )
    }

    @Test
    fun preparedQueryPreservesParametersAndEnforcesMaximumRows() {
        DriverManager.getConnection(url).use { connection ->
            connection.prepareStatement("SELECT id FROM entries WHERE id >= ? ORDER BY id").use { statement ->
                statement.setInt(1, 1)
                statement.maxRows = MAXIMUM_ROWS
                statement.executeQuery().use { resultSet ->
                    assertRows(resultSet)
                }
            }
        }
    }

    @Test
    fun scrollableResultCannotReachRowsPastMaximum() {
        DriverManager.getConnection(url).use { connection ->
            connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            ).use { statement ->
                statement.maxRows = MAXIMUM_ROWS
                statement.executeQuery("SELECT id FROM entries ORDER BY id").use { resultSet ->
                    assertTrue(resultSet.last())
                    assertEquals(MAXIMUM_ROWS, resultSet.row)
                    assertEquals(MAXIMUM_ROWS, resultSet.getInt(1))
                    assertFalse(resultSet.next())
                    assertTrue(resultSet.isAfterLast)
                    assertTrue(resultSet.previous())
                    assertEquals(MAXIMUM_ROWS, resultSet.row)
                }
            }
        }
    }

    private fun assertMaximumRows(sql: String) {
        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use { statement ->
                statement.maxRows = MAXIMUM_ROWS
                statement.executeQuery(sql).use { resultSet ->
                    assertRows(resultSet)
                }
            }
        }
    }

    private fun assertRows(resultSet: ResultSet) {
        repeat(MAXIMUM_ROWS) { index ->
            assertTrue(resultSet.next())
            assertEquals(index + 1, resultSet.getInt(1))
        }
        assertFalse(resultSet.next())
        assertFalse(resultSet.next())
    }

    private companion object {
        const val MAXIMUM_ROWS = 3
    }
}
