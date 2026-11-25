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
import com.bloomberg.selekt.jdbc.driver.SelektDataSource
import java.sql.BatchUpdateException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.times

internal class JdbcPreparedCheckpointCacheTest {
    private lateinit var source: SelektDataSource
    private lateinit var connection: JdbcConnection

    @BeforeEach
    fun setUp() {
        source = SelektDataSource().apply {
            databasePath = ":memory:"
            journalMode = "MEMORY"
            maxPoolSize = 1
        }
        connection = (source.connection as JdbcConnection).apply { autoCommit = false }
    }

    @AfterEach
    fun tearDown() {
        try {
            connection.close()
        } finally {
            source.close()
        }
    }

    @Test
    fun checkpointIsClassifiedOnceAcrossExecutionsAndPoolReuse() {
        verifySingleClassification("/* maintenance */ PRAGMA main.'wal_checkpoint'(TRUNCATE)") { statement ->
            assertTrue(statement.execute())
            assertNotNull(statement.resultSet).use { assertTrue(it.next()) }
            statement.executeQuery().use { assertTrue(it.next()) }
            statement.executeUpdate()
            assertFalse(connection.withSession { inTransaction })
            statement.addBatch()
            assertFailsWith<BatchUpdateException> { statement.executeBatch() }
        }
    }

    @Test
    fun nonCheckpointIsClassifiedOnceAndRemainsTransactional() {
        verifySingleClassification("PRAGMA user_version=23; PRAGMA wal_checkpoint(TRUNCATE)") { statement ->
            assertFalse(statement.execute())
            assertTrue(connection.withSession { inTransaction })
            connection.rollback()
            statement.executeUpdate()
            assertTrue(connection.withSession { inTransaction })
            connection.rollback()
        }
    }

    @Test
    fun updateIsClassifiedOnceAcrossExecutionsAndPoolReuse() {
        connection.createStatement().use { statement ->
            statement.executeUpdate("CREATE TABLE entries (id INTEGER PRIMARY KEY)")
            statement.executeUpdate("INSERT INTO entries VALUES (1)")
        }
        connection.commit()
        verifySingleClassification("UPDATE entries SET id=1 WHERE id=1") { statement ->
            assertFalse(statement.execute())
            assertTrue(connection.withSession { inTransaction })
            connection.rollback()
            assertEquals(1, statement.executeUpdate())
            assertTrue(connection.withSession { inTransaction })
            connection.rollback()
            statement.addBatch()
            assertEquals(1, statement.executeBatch().size)
            assertTrue(connection.withSession { inTransaction })
            connection.rollback()
        }
    }

    @Test
    fun queryIsClassifiedOnceAcrossExecutionsAndPoolReuse() {
        verifySingleClassification("/* query */ SELECT 42") { statement ->
            assertTrue(statement.execute())
            assertNotNull(statement.resultSet).use {
                assertTrue(it.next())
                assertEquals(42, it.getInt(1))
            }
            statement.executeQuery().use {
                assertTrue(it.next())
                assertEquals(42, it.getInt(1))
            }
        }
    }

    private fun verifySingleClassification(sql: String, exercise: (JdbcPreparedStatement) -> Unit) {
        val functions = Class.forName("com.bloomberg.selekt.jdbc.statement.JdbcStatementKt")
        mockStatic(functions, CALLS_REAL_METHODS).use { classifier ->
            var previousEntry: PreparedStatementCacheEntry? = null
            repeat(3) {
                (connection.prepareStatement(sql) as JdbcPreparedStatement).use { statement ->
                    previousEntry?.let { assertSame(it, statement.cacheEntry) }
                    previousEntry = statement.cacheEntry
                    repeat(2) { exercise(statement) }
                }
            }
            classifier.verify({ sql.isWalCheckpoint() }, times(1))
        }
    }
}
