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

package com.bloomberg.selekt.jdbc.connection

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class JdbcReadOnlyEnforcementTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun statementExecuteQueryRejectsDmlReturning() = withReadOnlyDatabase("statement-returning") { connection ->
        connection.createStatement().use { statement ->
            listOf(
                "INSERT INTO audit_t(v) VALUES (7) RETURNING v",
                "UPDATE audit_t SET v = 8 WHERE v = 1 RETURNING v",
                "DELETE FROM audit_t WHERE v = 1 RETURNING v"
            ).forEach { sql ->
                assertReadOnlyFailure { statement.executeQuery(sql).use { } }
            }
        }
        assertEquals(listOf(1, 2), values(connection))
    }

    @Test
    fun preparedStatementExecuteQueryRejectsDmlReturning() = withReadOnlyDatabase("prepared-returning") { connection ->
        listOf(
            "INSERT INTO audit_t(v) VALUES (?) RETURNING v",
            "UPDATE audit_t SET v = ? WHERE v = 1 RETURNING v",
            "DELETE FROM audit_t WHERE v = ? RETURNING v"
        ).forEach { sql ->
            connection.prepareStatement(sql).use { statement ->
                statement.setInt(1, 7)
                assertReadOnlyFailure { statement.executeQuery().use { } }
            }
        }
        assertEquals(listOf(1, 2), values(connection))
    }

    @Test
    fun executeQueryRejectsWritablePragmaAndCteWrite() = withReadOnlyDatabase("pragma-cte") { connection ->
        connection.createStatement().use { statement ->
            assertReadOnlyFailure { statement.executeQuery("PRAGMA user_version = 42").use { } }
            assertReadOnlyFailure {
                statement.executeQuery(
                    "WITH candidate(v) AS (VALUES (9)) " +
                        "INSERT INTO audit_t(v) SELECT v FROM candidate RETURNING v"
                ).use { }
            }
            statement.executeQuery("PRAGMA user_version").use { resultSet ->
                assertTrue(resultSet.next())
                assertEquals(0, resultSet.getInt(1))
            }
        }
        assertEquals(listOf(1, 2), values(connection))
    }

    @Test
    fun executeQueryRejectsVirtualTableCreation() = withReadOnlyDatabase("virtual-table") { connection ->
        connection.createStatement().use { statement ->
            assertReadOnlyFailure {
                statement.executeQuery("CREATE VIRTUAL TABLE blocked_vec USING vec1(embedding)").use { }
            }
            statement.executeQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE name = 'blocked_vec'"
            ).use { resultSet ->
                assertTrue(resultSet.next())
                assertEquals(0, resultSet.getInt(1))
            }
        }
    }

    @Test
    fun readQueriesRemainAvailable() = withReadOnlyDatabase("reads") { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT v FROM audit_t ORDER BY v").use { resultSet ->
                assertTrue(resultSet.next())
                assertEquals(1, resultSet.getInt(1))
            }
        }
        connection.prepareStatement("SELECT v FROM audit_t WHERE v = ?").use { statement ->
            statement.setInt(1, 2)
            statement.executeQuery().use { resultSet ->
                assertTrue(resultSet.next())
                assertEquals(2, resultSet.getInt(1))
                assertFalse(resultSet.next())
            }
        }
    }

    @Test
    fun readOnlyConnectionCannotAttachAnotherDatabase() {
        val attached = File(tempDir, "attached.db")
        DriverManager.getConnection("jdbc:sqlite:${attached.absolutePath}").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE confidential(value TEXT NOT NULL)")
                statement.executeUpdate("INSERT INTO confidential(value) VALUES ('secret')")
            }
        }
        val escapedPath = attached.absolutePath.replace("'", "''")
        withReadOnlyDatabase("attach-target") { connection ->
            connection.createStatement().use { statement ->
                assertReadOnlyFailure { statement.execute("ATTACH DATABASE '$escapedPath' AS secret") }
                assertReadOnlyFailure {
                    statement.executeQuery("/* audit */ ATTACH DATABASE '$escapedPath' AS secret").use { }
                }
            }
            connection.prepareStatement("-- audit\r\nATTACH DATABASE '$escapedPath' AS secret").use { statement ->
                assertReadOnlyFailure { statement.execute() }
            }
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA database_list").use { resultSet ->
                    while (resultSet.next()) {
                        assertFalse(resultSet.getString("name").equals("secret", ignoreCase = true))
                    }
                }
            }
        }
    }

    private fun withReadOnlyDatabase(name: String, block: (Connection) -> Unit) {
        val url = "jdbc:sqlite:${File(tempDir, "$name.db").absolutePath}"
        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("CREATE TABLE audit_t(v INTEGER NOT NULL)")
                statement.executeUpdate("INSERT INTO audit_t(v) VALUES (1), (2)")
            }
        }
        DriverManager.getConnection(url).use { connection ->
            connection.isReadOnly = true
            block(connection)
        }
    }

    private fun values(connection: Connection): List<Int> = connection.createStatement().use { statement ->
        statement.executeQuery("SELECT v FROM audit_t ORDER BY v").use { resultSet ->
            buildList {
                while (resultSet.next()) {
                    add(resultSet.getInt(1))
                }
            }
        }
    }

    private fun assertReadOnlyFailure(block: () -> Unit) {
        val failure = assertFailsWith<SQLException>(block = block)
        assertTrue(failure.message.orEmpty().contains("readonly", ignoreCase = true), failure.message)
    }
}
