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

import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.driver.SelektDriver
import java.io.File
import java.sql.BatchUpdateException
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class JdbcCheckpointTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun statementExecuteReturnsCheckpointResult() = withManualConnection { connection ->
        connection.createStatement().use { statement ->
            assertTrue(statement.execute(CHECKPOINT))
            assertCheckpointResult(assertNotNull(statement.resultSet))
        }
    }

    @Test
    fun preparedExecuteReturnsCheckpointResult() = withManualConnection { connection ->
        connection.prepareStatement(CHECKPOINT).use { statement ->
            assertTrue(statement.execute())
            assertCheckpointResult(assertNotNull(statement.resultSet))
        }
    }

    @Test
    fun statementUpdateDoesNotBeginTransaction() = withManualConnection { connection ->
        connection.createStatement().use { it.executeUpdate(CHECKPOINT) }
    }

    @Test
    fun preparedUpdateDoesNotBeginTransaction() = withManualConnection { connection ->
        connection.prepareStatement(CHECKPOINT).use { it.executeUpdate() }
    }

    @Test
    fun statementBatchRejectsCheckpoint() = withManualConnection { connection ->
        connection.createStatement().use {
            it.addBatch(CHECKPOINT)
            assertFailsWith<BatchUpdateException> { it.executeBatch() }
        }
    }

    @Test
    fun preparedBatchRejectsCheckpoint() = withManualConnection { connection ->
        connection.prepareStatement(CHECKPOINT).use {
            it.addBatch()
            assertFailsWith<BatchUpdateException> { it.executeBatch() }
        }
    }

    @Test
    fun checkpointAcceptsSchemaAndComments() = withManualConnection { connection ->
        listOf(
            "; ; /* maintenance */ PRAGMA main.wal_checkpoint(TRUNCATE); ;",
            "PRAGMA/* keyword */wal_checkpoint/* mode */(TRUNCATE)",
            "PRAGMA-- keyword\r\nmain-- schema\n.-- dot\nwal_checkpoint(TRUNCATE)",
            "-- maintenance\nPrAgMa /* name */ main /* schema */ . wal_checkpoint = TRUNCATE",
            """PRAGMA "main"."wal_checkpoint"(TRUNCATE)""",
            "PRAGMA [main].[wal_checkpoint](TRUNCATE)",
            "PRAGMA `main`.`wal_checkpoint`(TRUNCATE)",
            "PRAGMA 'main'.'wal_checkpoint'(TRUNCATE)"
        ).forEach { sql ->
            connection.createStatement().use { statement ->
                assertTrue(statement.execute(sql), sql)
                assertCheckpointResult(assertNotNull(statement.resultSet))
            }
            connection.prepareStatement(sql).use { statement ->
                assertTrue(statement.execute(), sql)
                assertCheckpointResult(assertNotNull(statement.resultSet))
            }
        }
    }

    @Test
    fun checkpointTokensCannotCrossStatementBoundaries() {
        listOf(
            "PRAGMA; wal_checkpoint(TRUNCATE)",
            "PRAGMA main; .wal_checkpoint(TRUNCATE)",
            "PRAGMA main.; wal_checkpoint(TRUNCATE)",
            "PRAGMA main /* schema */; /* next statement */ .wal_checkpoint(TRUNCATE)"
        ).forEach { sql -> assertFalse(sql.isWalCheckpoint(), sql) }
    }

    @Test
    fun checkpointNamesFollowSqliteTokenization() {
        listOf(
            ";;PrAgMa\n\"main\"\uFEFF.\nwal_checkpoint\uFEFF(TRUNCATE)",
            "PRAGMA -- still commented\rwal_checkpoint(TRUNCATE)",
            "PRAGMA wal_checkpo\u0131nt(TRUNCATE)",
            "PRAGMA wal_chec\u212Apoint(TRUNCATE)"
        ).forEach { sql -> assertFalse(sql.isWalCheckpoint(), sql) }
    }

    @Test
    fun trailingStatementsAreNotExecuted() = withManualConnection { connection ->
        connection.createStatement().use { statement ->
            assertTrue(statement.execute("$CHECKPOINT; PRAGMA user_version=23"))
            assertCheckpointResult(assertNotNull(statement.resultSet))
            assertTrue(statement.execute("SELECT 42; $CHECKPOINT"))
            statement.resultSet.use {
                assertTrue(it.next())
                assertEquals(42, it.getInt(1))
            }
        }
        connection.prepareStatement("$CHECKPOINT; PRAGMA user_version=23").use { statement ->
            assertTrue(statement.execute())
            assertCheckpointResult(assertNotNull(statement.resultSet))
        }
        connection.createStatement().use { statement ->
            statement.executeUpdate("PRAGMA user_version=23; $CHECKPOINT")
            assertTrue((connection as JdbcConnection).withSession { inTransaction })
            connection.rollback()
            statement.executeQuery("PRAGMA user_version").use {
                assertTrue(it.next())
                assertEquals(0, it.getInt(1))
            }
        }
    }

    @Test
    fun checkpointDoesNotCommitActiveTransaction() = withManualConnection { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate("INSERT INTO entries VALUES (2)")
            assertFailsWith<SQLException> { statement.execute(CHECKPOINT) }
            connection.prepareStatement(CHECKPOINT).use { prepared ->
                assertFailsWith<SQLException> { prepared.execute() }
            }
            assertTrue((connection as JdbcConnection).withSession { inTransaction })
            connection.rollback()
            statement.executeQuery("SELECT count(*) FROM entries").use {
                assertTrue(it.next())
                assertEquals(1, it.getInt(1))
            }
        }
    }

    @Test
    fun checkpointHonoursReadOnlyConnection() = withManualConnection { connection ->
        connection.isReadOnly = true
        connection.createStatement().use {
            assertFailsWith<SQLException> { it.execute(CHECKPOINT) }
        }
        connection.prepareStatement(CHECKPOINT).use {
            assertFailsWith<SQLException> { it.execute() }
        }
    }

    @Test
    fun otherPragmaWritesRemainTransactional() = withManualConnection { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate("PRAGMA user_version=23")
            assertTrue((connection as JdbcConnection).withSession { inTransaction })
            connection.rollback()
            statement.executeQuery("PRAGMA user_version").use {
                assertTrue(it.next())
                assertEquals(0, it.getInt(1))
            }
        }
    }

    private fun withManualConnection(block: (Connection) -> Unit) {
        val url = "jdbc:sqlite:${File(tempDir, "checkpoint.db").absolutePath}"
        assertNotNull(SelektDriver().connect(url, Properties())).use { connection ->
            connection.createStatement().use {
                it.execute("PRAGMA journal_mode=WAL")
                it.executeUpdate("CREATE TABLE entries (id INTEGER PRIMARY KEY)")
                it.executeUpdate("INSERT INTO entries VALUES (1)")
            }
            connection.autoCommit = false
            block(connection)
            assertFalse(connection.autoCommit)
            assertFalse((connection as JdbcConnection).withSession { inTransaction })
        }
    }

    private fun assertCheckpointResult(result: ResultSet) = result.use {
        assertTrue(it.next())
        assertEquals(0, it.getInt(1), "Checkpoint must not be busy")
        assertEquals(0, it.getInt(2), "TRUNCATE must remove WAL frames")
        assertEquals(0, it.getInt(3))
        assertFalse(it.next())
    }

    private companion object {
        const val CHECKPOINT = "PRAGMA wal_checkpoint(TRUNCATE)"
    }
}
