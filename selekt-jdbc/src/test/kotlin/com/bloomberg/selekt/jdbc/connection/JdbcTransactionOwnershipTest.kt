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

import com.bloomberg.selekt.SQLTransactionListener
import java.nio.file.Files
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively

internal class JdbcTransactionOwnershipTest {
    @Test
    fun logicalConnectionsOnSameThreadDoNotShareTransaction() {
        val url = newDatabaseUrl("same-thread")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                first.autoCommit = false
                first.createStatement().use {
                    assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('first')"))
                }
                assertEquals(0, rowCount(second), "A second connection must not see uncommitted rows")
                val failure = assertFailsWith<SQLException> {
                    second.createStatement().use {
                        it.executeUpdate("INSERT INTO test(value) VALUES ('second')")
                    }
                }
                assertTrue(failure.message.orEmpty().contains("owned by another JDBC connection"))
                first.rollback()
                second.createStatement().use {
                    assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('second')"))
                }
                assertEquals(1, rowCount(second))
            }
        }
    }

    @Test
    fun everyWriteExecutionPathRejectsAnotherSameThreadOwner() {
        val url = newDatabaseUrl("write-paths")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                first.autoCommit = false
                first.createStatement().use {
                    assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('first')"))
                }
                assertOwnedByAnotherConnection {
                    second.createStatement().use {
                        it.execute("INSERT INTO test(value) VALUES ('execute')")
                    }
                }
                assertOwnedByAnotherConnection {
                    second.prepareStatement("INSERT INTO test(value) VALUES (?)").use {
                        it.setString(1, "prepared-execute")
                        it.execute()
                    }
                }
                assertOwnedByAnotherConnection {
                    second.createStatement().use {
                        it.addBatch("INSERT INTO test(value) VALUES ('statement-batch')")
                        it.executeBatch()
                    }
                }
                assertOwnedByAnotherConnection {
                    second.prepareStatement("INSERT INTO test(value) VALUES (?)").use {
                        it.setString(1, "prepared-batch")
                        it.addBatch()
                        it.executeBatch()
                    }
                }
                assertEquals(0, rowCount(second))
                first.rollback()
            }
        }
    }

    @Test
    fun commitOnAnotherConnectionDoesNotCommitOwnersTransaction() {
        val url = newDatabaseUrl("cross-commit")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                first.autoCommit = false
                first.createStatement().use {
                    it.executeUpdate("INSERT INTO test(value) VALUES ('first')")
                }
                second.autoCommit = false
                second.commit()
                assertEquals(0, rowCount(second))
                first.commit()
                assertEquals(1, rowCount(second))
            }
        }
    }

    @Test
    fun rollbackOnAnotherConnectionDoesNotRollbackOwnersTransaction() {
        val url = newDatabaseUrl("cross-rollback")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                first.autoCommit = false
                first.createStatement().use {
                    it.executeUpdate("INSERT INTO test(value) VALUES ('first')")
                }
                second.autoCommit = false
                second.rollback()
                assertEquals(0, rowCount(second))
                first.commit()
                assertEquals(1, rowCount(second))
            }
        }
    }

    @Test
    fun rollbackToSavepointKeepsTransactionOwnership() {
        val url = newDatabaseUrl("savepoint-ownership")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                first.autoCommit = false
                first.createStatement().use {
                    it.executeUpdate("INSERT INTO test(value) VALUES ('outer')")
                }
                val savepoint = first.setSavepoint("nested")
                first.createStatement().use {
                    it.executeUpdate("INSERT INTO test(value) VALUES ('inner')")
                }
                first.rollback(savepoint)
                assertEquals(0, rowCount(second))
                assertOwnedByAnotherConnection {
                    second.createStatement().use {
                        it.executeUpdate("INSERT INTO test(value) VALUES ('second')")
                    }
                }
                first.commit()
                assertEquals(1, rowCount(second))
            }
        }
    }

    @Test
    fun rawTransactionControlIsConnectionOwned() {
        val url = newDatabaseUrl("raw-transaction")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                first.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).use {
                    it.execute("BEGIN IMMEDIATE")
                    assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('first')"))
                }
                assertOwnedByAnotherConnection {
                    second.createStatement().use {
                        it.execute("INSERT INTO test(value) VALUES ('second')")
                    }
                }
                assertEquals(0, rowCount(second))
                first.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).use {
                    it.execute("ROLLBACK")
                }
                second.createStatement().use {
                    assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('second')"))
                }
            }
        }
    }

    @Test
    fun transactionStartedThroughInternalSessionIsConnectionOwned() = assertTimeoutPreemptively(
        Duration.ofSeconds(5)
    ) {
        val url = newDatabaseUrl("unwrapped-session")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                first.autoCommit = false
                val jdbcConnection = first as JdbcConnection
                jdbcConnection.withSession {
                    beginImmediateTransaction()
                    exec("INSERT INTO test(value) VALUES ('first')")
                }
                assertEquals(0, rowCount(second))
                assertOwnedByAnotherConnection {
                    second.createStatement().use {
                        it.executeUpdate("INSERT INTO test(value) VALUES ('second')")
                    }
                }
                first.rollback()
                second.createStatement().use {
                    assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('second')"))
                }
                assertEquals(1, rowCount(second))
            }
        }
    }

    @Test
    fun transactionOnBeginRejectsReentrantWriteFromAnotherConnection() = assertTimeoutPreemptively(
        Duration.ofSeconds(5)
    ) {
        val url = newDatabaseUrl("listener-reentrant-other")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                val jdbcConnection = first as JdbcConnection
                jdbcConnection.withSession {
                    beginImmediateTransactionWithListener(object : SQLTransactionListener {
                        override fun onBegin() = assertOwnedByAnotherConnection {
                            second.createStatement().use {
                                it.executeUpdate("INSERT INTO test(value) VALUES ('second')")
                            }
                        }

                        override fun onCommit() = Unit

                        override fun onRollback() = Unit
                    })
                }
                jdbcConnection.withSession { endTransaction() }
                second.createStatement().use {
                    assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('after-rollback')"))
                }
            }
        }
    }

    @Test
    fun transactionOnBeginCanReenterOwningConnection() = assertTimeoutPreemptively(Duration.ofSeconds(5)) {
        val url = newDatabaseUrl("listener-reentrant-owner")
        createSchema(url)
        DriverManager.getConnection(url).use { connection ->
            val jdbcConnection = connection as JdbcConnection
            jdbcConnection.withSession {
                beginImmediateTransactionWithListener(object : SQLTransactionListener {
                    override fun onBegin() {
                        connection.createStatement().use {
                            assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('listener')"))
                        }
                    }

                    override fun onCommit() = Unit

                    override fun onRollback() = Unit
                })
                setTransactionSuccessful()
                endTransaction()
            }
            assertEquals(1, rowCount(connection))
        }
    }

    @Test
    fun failedTransactionListenerReleasesOwnership() = assertTimeoutPreemptively(Duration.ofSeconds(5)) {
        val url = newDatabaseUrl("listener-failure")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                val jdbcConnection = first as JdbcConnection
                val failure = assertFailsWith<IllegalStateException> {
                    jdbcConnection.withSession {
                        beginImmediateTransactionWithListener(object : SQLTransactionListener {
                            override fun onBegin(): Unit = error("listener failed")

                            override fun onCommit() = Unit

                            override fun onRollback() = Unit
                        })
                    }
                }
                assertEquals("listener failed", failure.message)
                jdbcConnection.withSession { assertFalse(inTransaction) }
                second.createStatement().use {
                    assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('after-failure')"))
                }
            }
        }
    }

    @Test
    fun transactionCompletionCallbacksRejectAnotherConnection() = assertTimeoutPreemptively(Duration.ofSeconds(5)) {
        val url = newDatabaseUrl("listener-completion")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                val listener = object : SQLTransactionListener {
                    override fun onBegin() = Unit

                    override fun onCommit() = assertSecondConnectionCannotWrite(second)

                    override fun onRollback() = assertSecondConnectionCannotWrite(second)
                }
                val jdbcConnection = first as JdbcConnection
                jdbcConnection.withSession {
                    beginImmediateTransactionWithListener(listener)
                    setTransactionSuccessful()
                    endTransaction()
                    beginImmediateTransactionWithListener(listener)
                    endTransaction()
                }
                second.createStatement().use {
                    assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('after-completion')"))
                }
            }
        }
    }

    @Test
    fun transactionOwnershipAlternatesAfterCommitAndRollback() {
        val url = newDatabaseUrl("alternating-owners")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                first.autoCommit = false
                second.autoCommit = false
                var committedRows = 0
                listOf(
                    Triple(first, second, false),
                    Triple(second, first, false),
                    Triple(first, second, true),
                    Triple(second, first, true)
                ).forEachIndexed { index, (owner, observer, commit) ->
                    owner.createStatement().use {
                        assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('round-$index')"))
                    }
                    assertEquals(committedRows, rowCount(observer))
                    if (commit) {
                        owner.commit()
                        ++committedRows
                    } else {
                        owner.rollback()
                    }
                    assertEquals(committedRows, rowCount(observer))
                }
            }
        }
    }

    @Test
    fun closingConnectionRollsBackItsTransaction() {
        val url = newDatabaseUrl("close-rollback")
        createSchema(url)
        DriverManager.getConnection(url).also { connection ->
            connection.autoCommit = false
            connection.createStatement().use {
                assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('uncommitted')"))
            }
            connection.close()
        }
        DriverManager.getConnection(url).use { connection ->
            assertEquals(0, rowCount(connection))
            connection.createStatement().use {
                assertEquals(1, it.executeUpdate("INSERT INTO test(value) VALUES ('after-close')"))
            }
        }
    }

    @Test
    fun transactionRejectsSequentialThreadHandoff() {
        val url = newDatabaseUrl("thread-handoff")
        createSchema(url)
        DriverManager.getConnection(url).use { connection ->
            DriverManager.getConnection(url).use { observer ->
                connection.autoCommit = false
                connection.createStatement().use {
                    it.executeUpdate("INSERT INTO test(value) VALUES ('main')")
                }
                val failure = AtomicReference<Throwable?>()
                Thread {
                    runCatching(connection::rollback).onFailure(failure::set)
                }.apply { start() }.join()
                assertTrue(failure.get() is SQLException)
                assertTrue(failure.get()?.message.orEmpty().contains("owned by another thread"))
                assertEquals(0, rowCount(observer))
                connection.rollback()
                assertEquals(0, rowCount(connection))
            }
        }
    }

    @Test
    fun writerOnAnotherThreadWaitsForTransactionOwner() {
        val url = newDatabaseUrl("other-thread")
        createSchema(url)
        DriverManager.getConnection(url).use { first ->
            DriverManager.getConnection(url).use { second ->
                first.autoCommit = false
                first.createStatement().use {
                    it.executeUpdate("INSERT INTO test(value) VALUES ('first')")
                }
                val started = CountDownLatch(1)
                val completed = CountDownLatch(1)
                val failure = AtomicReference<Throwable?>()
                val writer = Thread {
                    started.countDown()
                    runCatching {
                        second.createStatement().use {
                            it.executeUpdate("INSERT INTO test(value) VALUES ('second')")
                        }
                    }.onFailure(failure::set)
                    completed.countDown()
                }.apply { start() }
                assertTrue(started.await(1, TimeUnit.SECONDS))
                assertFalse(completed.await(100, TimeUnit.MILLISECONDS))
                first.rollback()
                writer.join(2_000)
                assertFalse(writer.isAlive)
                assertNull(failure.get())
                assertEquals(1, rowCount(second))
            }
        }
    }

    private fun createSchema(url: String) {
        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use {
                it.execute("CREATE TABLE test (id INTEGER PRIMARY KEY, value TEXT NOT NULL)")
            }
        }
    }

    private fun rowCount(connection: Connection): Int = connection.createStatement().use { statement ->
        statement.executeQuery("SELECT COUNT(*) FROM test").use { result ->
            assertTrue(result.next())
            result.getInt(1)
        }
    }

    private fun assertOwnedByAnotherConnection(block: () -> Unit) {
        val failure = assertFailsWith<SQLException>(block = block)
        assertTrue(failure.message.orEmpty().contains("owned by another JDBC connection"), failure.message)
    }

    private fun assertSecondConnectionCannotWrite(connection: Connection) = assertOwnedByAnotherConnection {
        connection.createStatement().use {
            it.executeUpdate("INSERT INTO test(value) VALUES ('listener-reentry')")
        }
    }

    private fun newDatabaseUrl(name: String): String {
        val path = Files.createTempFile("selekt-jdbc-$name-", ".db").also {
            it.toFile().deleteOnExit()
        }
        return "jdbc:sqlite:$path?journalMode=WAL&busyTimeout=1000&poolSize=2"
    }
}
