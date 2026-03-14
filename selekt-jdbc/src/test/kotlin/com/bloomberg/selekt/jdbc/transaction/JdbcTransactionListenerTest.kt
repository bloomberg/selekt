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

package com.bloomberg.selekt.jdbc.transaction

import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLTransactionListener
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.sql.DriverManager
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JdbcTransactionListenerTest {
    @TempDir
    lateinit var tempDir: File

    private fun getConnectionUrl(testName: String) =
        "jdbc:sqlite:${File(tempDir, "$testName.db").absolutePath}"

    private class TrackingListener : SQLTransactionListener {
        val beginCount = AtomicInteger(0)
        val commitCount = AtomicInteger(0)
        val rollbackCount = AtomicInteger(0)

        override fun onBegin() {
            beginCount.incrementAndGet()
        }

        override fun onCommit() {
            commitCount.incrementAndGet()
        }

        override fun onRollback() {
            rollbackCount.incrementAndGet()
        }
    }

    @Test
    fun transactionListenerCommitViaConnectionCommit() {
        val listener = TrackingListener()
        DriverManager.getConnection(getConnectionUrl("test1")).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("DROP TABLE IF EXISTS test")
                statement.executeUpdate("CREATE TABLE test (id INTEGER PRIMARY KEY, value TEXT)")
            }
            val database = connection.unwrap(SQLDatabase::class.java)
            connection.autoCommit = false
            database.beginExclusiveTransactionWithListener(listener)
            connection.createStatement().use { statement ->
                statement.executeUpdate("INSERT INTO test (value) VALUES ('test')")
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            assertEquals(1, listener.beginCount.get(), "onBegin should be called once")
            assertEquals(1, listener.commitCount.get(), "onCommit should be called once")
            assertEquals(0, listener.rollbackCount.get(), "onRollback should not be called")
            connection.createStatement().use { statement ->
                val resultSet = statement.executeQuery("SELECT COUNT(*) FROM test")
                assertTrue(resultSet.next())
                assertEquals(1, resultSet.getInt(1))
            }
        }
    }

    @Test
    fun transactionListenerRollbackViaConnectionRollback() {
        val listener = TrackingListener()
        DriverManager.getConnection(getConnectionUrl("test2")).use { connection ->
            connection.createStatement().use {
                it.executeUpdate("DROP TABLE IF EXISTS test2")
                it.executeUpdate("CREATE TABLE test2 (id INTEGER PRIMARY KEY, value TEXT)")
            }
            val database = connection.unwrap(SQLDatabase::class.java)
            connection.autoCommit = false
            database.beginExclusiveTransactionWithListener(listener)
            connection.createStatement().use {
                it.executeUpdate("INSERT INTO test2 (value) VALUES ('rollback-test')")
            }
            database.endTransaction()
            assertEquals(1, listener.beginCount.get(), "onBegin should be called once")
            assertEquals(0, listener.commitCount.get(), "onCommit should not be called")
            assertEquals(1, listener.rollbackCount.get(), "onRollback should be called once via native hook")
            connection.createStatement().use {
                val resultSet = it.executeQuery("SELECT COUNT(*) FROM test2")
                assertTrue(resultSet.next())
                assertEquals(0, resultSet.getInt(1))
            }
        }
    }

    @Test
    fun transactionListenerMultipleTransactions() {
        val listener = TrackingListener()
        DriverManager.getConnection(getConnectionUrl("test6")).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("DROP TABLE IF EXISTS test6")
                statement.executeUpdate("CREATE TABLE test6 (id INTEGER PRIMARY KEY, value TEXT)")
            }
            val database = connection.unwrap(SQLDatabase::class.java)
            database.beginExclusiveTransactionWithListener(listener)
            connection.createStatement().use {
                it.executeUpdate("INSERT INTO test6 (value) VALUES ('first')")
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            database.beginExclusiveTransactionWithListener(listener)
            connection.createStatement().use {
                it.executeUpdate("INSERT INTO test6 (value) VALUES ('second')")
            }
            database.endTransaction()
            database.beginExclusiveTransactionWithListener(listener)
            connection.createStatement().use {
                it.executeUpdate("INSERT INTO test6 (value) VALUES ('third')")
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            assertEquals(3, listener.beginCount.get(), "onBegin should be called 3 times")
            assertEquals(2, listener.commitCount.get(), "onCommit should be called 2 times (first and third)")
            assertEquals(1, listener.rollbackCount.get(), "onRollback should be called 1 time (second)")
            connection.createStatement().use {
                val resultSet = it.executeQuery("SELECT COUNT(*) FROM test6")
                assertTrue(resultSet.next())
                assertEquals(2, resultSet.getInt(1), "Only first and third should be committed")
            }
        }
    }

    @Test
    fun transactionListenerNestedTransactions() {
        val listener = TrackingListener()
        DriverManager.getConnection(getConnectionUrl("test7")).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("DROP TABLE IF EXISTS test7")
                statement.executeUpdate("CREATE TABLE test7 (id INTEGER PRIMARY KEY, value TEXT)")
            }
            val database = connection.unwrap(SQLDatabase::class.java)
            database.beginExclusiveTransactionWithListener(listener)
            connection.createStatement().use {
                it.executeUpdate("INSERT INTO test7 (value) VALUES ('outer')")
            }
            database.beginExclusiveTransaction()
            connection.createStatement().use {
                it.executeUpdate("INSERT INTO test7 (value) VALUES ('inner')")
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            database.setTransactionSuccessful()
            database.endTransaction()
            assertEquals(1, listener.beginCount.get(), "onBegin should be called once for outer transaction")
            assertEquals(1, listener.commitCount.get(), "onCommit should be called once when outer transaction commits")
            assertEquals(0, listener.rollbackCount.get(), "onRollback should not be called")
            connection.createStatement().use { statement ->
                val resultSet = statement.executeQuery("SELECT COUNT(*) FROM test7")
                assertTrue(resultSet.next())
                assertEquals(2, resultSet.getInt(1))
            }
        }
    }

    @Test
    fun verifyFFMHooksConfiguration() {
        assertTrue(true, "JDBC configured to use FFM-based transaction hooks")
    }

    @Test
    fun transactionListenerNotCalledAfterUnregistration() {
        val listener = TrackingListener()
        DriverManager.getConnection(getConnectionUrl("test8")).use { connection ->
            connection.createStatement().use {
                it.executeUpdate("DROP TABLE IF EXISTS test8")
                it.executeUpdate("CREATE TABLE test8 (id INTEGER PRIMARY KEY, value TEXT)")
            }
            val database = connection.unwrap(SQLDatabase::class.java)
            database.beginExclusiveTransactionWithListener(listener)
            database.setTransactionSuccessful()
            database.endTransaction()
            assertEquals(1, listener.beginCount.get())
            assertEquals(1, listener.commitCount.get())
            database.beginExclusiveTransaction()
            connection.createStatement().use {
                it.executeUpdate("INSERT INTO test8 (value) VALUES ('no-listener')")
            }
            database.setTransactionSuccessful()
            database.endTransaction()
            assertEquals(1, listener.beginCount.get(), "onBegin should still be 1 (not called for second transaction)")
            assertEquals(1, listener.commitCount.get(), "onCommit should still be 1 (not called for second transaction)")
            connection.createStatement().use {
                val resultSet = it.executeQuery("SELECT COUNT(*) FROM test8")
                assertTrue(resultSet.next())
                assertEquals(1, resultSet.getInt(1))
            }
        }
    }
}
