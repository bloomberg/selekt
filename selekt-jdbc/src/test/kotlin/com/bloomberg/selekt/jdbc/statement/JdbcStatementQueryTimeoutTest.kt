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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLTimeoutException
import java.sql.Statement
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class JdbcStatementQueryTimeoutTest {
    @TempDir
    lateinit var tempDir: File

    private fun url(): String = "jdbc:sqlite:${File(tempDir, "timeout.db").absolutePath}"

    private val runawaySql = """
        WITH RECURSIVE r(n) AS (
            SELECT 1 UNION ALL SELECT n + 1 FROM r WHERE n < 100000000
        )
        SELECT count(*) FROM r
    """.trimIndent()

    private val runawayUpdateSql = """
        UPDATE sink SET value = (
            WITH RECURSIVE r(n) AS (
                SELECT 1 UNION ALL SELECT n + 1 FROM r WHERE n < 100000000
            )
            SELECT count(*) FROM r
        )
    """.trimIndent()

    private fun ResultSet.drain() {
        @Suppress("Detekt.UnconditionalJumpStatementInLoop")
        while (next()) { continue }
    }

    private fun openStatement(block: (Statement) -> Unit) {
        DriverManager.getConnection(url()).use { connection ->
            connection.createStatement().use(block)
        }
    }

    private fun openConnection(block: (Connection) -> Unit) {
        DriverManager.getConnection(url()).use(block)
    }

    private fun createSink(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.executeUpdate("CREATE TABLE sink (value INTEGER)")
            statement.executeUpdate("INSERT INTO sink VALUES (0)")
        }
    }

    private fun assertDefaultTimeoutCancellation(statement: Statement, operation: () -> Unit) {
        assertEquals(0, statement.queryTimeout)
        val started = CountDownLatch(1)
        val done = AtomicBoolean(false)
        val canceller = thread(name = "JDBC-M-05-canceller") {
            started.await(5, TimeUnit.SECONDS)
            Thread.sleep(100)
            while (!done.get()) {
                statement.cancel()
                Thread.sleep(50)
            }
        }
        try {
            val start = System.nanoTime()
            started.countDown()
            val thrown = assertFailsWith<SQLException> { operation() }
            val elapsedMs = (System.nanoTime() - start) / 1_000_000
            assertTrue(
                generateSequence<Throwable>(thrown) { it.cause }.any { it is SQLTimeoutException },
                "Expected SQLTimeoutException in cause chain, got ${thrown::class.simpleName}: ${thrown.message}"
            )
            assertTrue(
                elapsedMs < 15_000,
                "cancel() should abort within seconds; took ${elapsedMs}ms"
            )
        } finally {
            done.set(true)
            canceller.join(5_000)
        }
    }

    @Test
    fun setQueryTimeoutAbortsLongRunningQuery() {
        openStatement { statement ->
            statement.queryTimeout = 1
            val start = System.nanoTime()
            val thrown = assertFailsWith<SQLException> {
                statement.executeQuery(runawaySql).use { it.drain() }
            }
            val elapsedMs = (System.nanoTime() - start) / 1_000_000
            assertTrue(
                thrown is SQLTimeoutException,
                "Expected SQLTimeoutException, got ${thrown::class.simpleName}: ${thrown.message}"
            )
            assertTrue(
                elapsedMs in 500..15_000,
                "Query should abort near the 1s deadline; took ${elapsedMs}ms"
            )
        }
    }

    @Test
    fun cancelAbortsLongRunningQueryWithDefaultTimeout() {
        openStatement { statement ->
            assertDefaultTimeoutCancellation(statement) {
                statement.executeQuery(runawaySql).use { it.drain() }
            }
        }
    }

    @Test
    fun cancelAbortsPreparedLongRunningQueryWithDefaultTimeout() {
        openConnection { connection ->
            connection.prepareStatement(runawaySql).use { statement ->
                assertDefaultTimeoutCancellation(statement) {
                    statement.executeQuery().use { it.drain() }
                }
            }
        }
    }

    @Test
    fun cancelAbortsLongRunningExecuteQueryWithDefaultTimeout() {
        openStatement { statement ->
            assertDefaultTimeoutCancellation(statement) {
                assertTrue(statement.execute(runawaySql))
                statement.resultSet.use { it.drain() }
            }
        }
    }

    @Test
    fun cancelAbortsPreparedLongRunningExecuteQueryWithDefaultTimeout() {
        openConnection { connection ->
            connection.prepareStatement(runawaySql).use { statement ->
                assertDefaultTimeoutCancellation(statement) {
                    assertTrue(statement.execute())
                    statement.resultSet.use { it.drain() }
                }
            }
        }
    }

    @Test
    fun cancelAbortsLongRunningUpdateWithDefaultTimeout() {
        openConnection { connection ->
            createSink(connection)
            connection.createStatement().use { statement ->
                assertDefaultTimeoutCancellation(statement) {
                    statement.executeUpdate(runawayUpdateSql)
                }
            }
        }
    }

    @Test
    fun cancelAbortsPreparedLongRunningUpdateWithDefaultTimeout() {
        openConnection { connection ->
            createSink(connection)
            connection.prepareStatement(runawayUpdateSql).use { statement ->
                assertDefaultTimeoutCancellation(statement) {
                    statement.executeUpdate()
                }
            }
        }
    }

    @Test
    fun cancelAbortsLongRunningExecuteUpdateWithDefaultTimeout() {
        openConnection { connection ->
            createSink(connection)
            connection.createStatement().use { statement ->
                assertDefaultTimeoutCancellation(statement) {
                    statement.execute(runawayUpdateSql)
                }
            }
        }
    }

    @Test
    fun cancelAbortsPreparedLongRunningExecuteUpdateWithDefaultTimeout() {
        openConnection { connection ->
            createSink(connection)
            connection.prepareStatement(runawayUpdateSql).use { statement ->
                assertDefaultTimeoutCancellation(statement) {
                    statement.execute()
                }
            }
        }
    }

    @Test
    fun cancelAbortsLongRunningBatchWithDefaultTimeout() {
        openConnection { connection ->
            createSink(connection)
            connection.createStatement().use { statement ->
                statement.addBatch(runawayUpdateSql)
                assertDefaultTimeoutCancellation(statement) {
                    statement.executeBatch()
                }
            }
        }
    }

    @Test
    fun cancelAbortsPreparedLongRunningBatchWithDefaultTimeout() {
        openConnection { connection ->
            createSink(connection)
            connection.prepareStatement(runawayUpdateSql).use { statement ->
                statement.addBatch()
                assertDefaultTimeoutCancellation(statement) {
                    statement.executeBatch()
                }
            }
        }
    }

    @Test
    fun zeroQueryTimeoutMeansNoWatchdog() {
        openStatement { statement ->
            statement.queryTimeout = 0
            statement.executeQuery("SELECT 1").use { resultSet ->
                assertTrue(resultSet.next())
                assertNotNull(resultSet.getObject(1))
            }
        }
    }

    @Test
    fun watchdogIsDisarmedAfterSuccessfulQuery() {
        openStatement { statement ->
            statement.queryTimeout = 5
            statement.executeQuery("SELECT 1").use { it.drain() }
            Thread.sleep(50)
            statement.executeQuery("SELECT 2").use { resultSet ->
                assertTrue(resultSet.next())
                assertEquals(2, resultSet.getInt(1))
            }
        }
    }
}
