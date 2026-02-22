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

import com.bloomberg.selekt.ICursor
import com.bloomberg.selekt.ISQLStatement
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.result.JdbcResultSet
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import java.sql.ResultSet
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.sql.SQLException
import java.sql.Statement
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.mockito.kotlin.doReturn

internal class JdbcStatementTest {
    private lateinit var mockDatabase: SQLDatabase
    private lateinit var mockConnection: JdbcConnection
    private lateinit var mockCursor: ICursor
    private lateinit var statement: JdbcStatement

    @BeforeEach
    fun setUp() {
        mockDatabase = mock<SQLDatabase>()
        mockCursor = mock<ICursor>()

        val connectionURL = ConnectionURL.parse("jdbc:sqlite:/tmp/test.db")
        val properties = Properties()
        mockConnection = JdbcConnection(mockDatabase, connectionURL, properties)

        statement = JdbcStatement(mockConnection, mockDatabase)
    }

    @Test
    fun executeQuery() {
        val sql = "SELECT * FROM users"
        whenever(mockDatabase.query(sql, emptyArray())) doReturn mockCursor

        val resultSet = statement.executeQuery(sql)
        assertNotNull(resultSet)
        assertTrue(resultSet is JdbcResultSet)
        assertEquals(resultSet, statement.resultSet)
    }

    @Test
    fun executeUpdate() {
        val sql = "INSERT INTO users (name) VALUES ('test')"
        val mockStatement = mock<ISQLStatement>()
        whenever(mockDatabase.compileStatement(sql, null)) doReturn mockStatement
        whenever(mockStatement.executeUpdateDelete()) doReturn 1

        val updateCount = statement.executeUpdate(sql)
        assertEquals(1, updateCount)
        assertEquals(1, statement.updateCount)
    }

    @Test
    fun executeWithQuery() {
        val sql = "SELECT COUNT(*) FROM users"
        whenever(mockDatabase.query(sql, emptyArray())) doReturn mockCursor
        assertTrue(statement.execute(sql))
        assertNotNull(statement.resultSet)
        assertEquals(-1, statement.updateCount)
    }

    @Test
    fun executeWithUpdate() {
        val sql = "UPDATE users SET name = 'updated'"
        val mockStatement = mock<ISQLStatement> {
            whenever(it.executeUpdateDelete()) doReturn 3
        }
        whenever(mockDatabase.compileStatement(sql, null)) doReturn mockStatement
        val result = statement.execute(sql)
        assertFalse(result)
        assertNull(statement.resultSet)
        assertEquals(3, statement.updateCount)
    }

    @Test
    fun executeBatch() {
        statement.apply {
            addBatch("INSERT INTO users (name) VALUES ('user1')")
            addBatch("INSERT INTO users (name) VALUES ('user2')")
        }
        val mockStatement = mock<ISQLStatement> {
            whenever(it.executeUpdateDelete()) doReturn 1
        }
        whenever(mockDatabase.compileStatement(any<String>(), isNull())) doReturn mockStatement
        statement.executeBatch().run {
            assertEquals(2, size)
            assertEquals(1, this[0])
            assertEquals(1, this[1])
        }
    }

    @Test
    fun clearBatch() {
        assertEquals(0, statement.apply {
            addBatch("INSERT INTO users (name) VALUES ('test')")
            clearBatch()
        }.executeBatch().size)
    }

    @Test
    fun closure(): Unit = statement.run {
        assertFalse(isClosed)
        close()
        assertTrue(isClosed)
        assertFailsWith<SQLException> {
            executeQuery("SELECT 1")
        }
    }

    @Test
    fun maxFieldSize(): Unit = statement.run {
        assertEquals(0, maxFieldSize)
        maxFieldSize = 1000
        assertEquals(1000, maxFieldSize)
        assertFailsWith<SQLException> {
            maxFieldSize = -1
        }
    }

    @Test
    fun maxRows(): Unit = statement.run {
        assertEquals(0, maxRows)
        maxRows = 100
        assertEquals(100, maxRows)
        assertFailsWith<SQLException> {
            maxRows = -1
        }
    }

    @Test
    fun escapeProcessing(): Unit = statement.run {
        assertTrue(escapeProcessing)
        setEscapeProcessing(false)
        assertFalse(escapeProcessing)
    }

    @Test
    fun queryTimeout(): Unit = statement.run {
        assertEquals(0, queryTimeout)
        queryTimeout = 30
        assertEquals(30, queryTimeout)
        assertFailsWith<SQLException> {
            queryTimeout = -1
        }
    }

    @Test
    fun fetchDirection(): Unit = statement.run {
        assertEquals(ResultSet.FETCH_FORWARD, fetchDirection)
        fetchDirection = ResultSet.FETCH_FORWARD
        assertEquals(ResultSet.FETCH_FORWARD, fetchDirection)
        assertFailsWith<SQLException> {
            fetchDirection = ResultSet.FETCH_REVERSE
        }
    }

    @Test
    fun fetchSize(): Unit = statement.run {
        assertEquals(0, fetchSize)
        fetchSize = 100
        assertEquals(100, fetchSize)
        assertFailsWith<SQLException> {
            fetchSize = -1
        }
    }

    @Test
    fun resultSetConcurrency() {
        assertEquals(ResultSet.CONCUR_READ_ONLY, statement.resultSetConcurrency)
    }

    @Test
    fun resultSetType() {
        assertEquals(ResultSet.TYPE_FORWARD_ONLY, statement.resultSetType)
    }

    @Test
    fun resultSetHoldability() {
        assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, statement.resultSetHoldability)
    }

    @Test
    fun getConnection() {
        assertEquals(mockConnection, statement.connection)
    }

    @Test
    fun getWarnings() {
        assertNull(statement.warnings)
    }

    @Test
    fun clearWarnings() {
        statement.clearWarnings()
        assertNull(statement.warnings)
    }

    @Test
    fun cancellation() {
        statement.cancel()
    }

    @Test
    fun getMoreResults(): Unit = statement.run {
        assertFalse(getMoreResults())
        assertFalse(getMoreResults(Statement.CLOSE_CURRENT_RESULT))
    }

    @Test
    fun getGeneratedKeys() {
        assertFailsWith<SQLException> {
            statement.generatedKeys
        }
    }

    @Test
    fun executeUpdateWithGeneratedKeys() {
        assertFailsWith<SQLException> {
            statement.executeUpdate("INSERT INTO users (name) VALUES ('test')", Statement.RETURN_GENERATED_KEYS)
        }
    }

    @Test
    fun executeWithGeneratedKeys() {
        assertFailsWith<SQLException> {
            statement.execute("INSERT INTO users (name) VALUES ('test')", Statement.RETURN_GENERATED_KEYS)
        }
    }

    @Test
    fun wrapperInterface(): Unit = statement.run {
        assertTrue(isWrapperFor(JdbcStatement::class.java))
        assertFalse(isWrapperFor(String::class.java))
        val unwrapped = unwrap(JdbcStatement::class.java)
        assertSame(this, unwrapped)
        assertFailsWith<SQLException> {
            unwrap(String::class.java)
        }
    }

    @Test
    fun poolable(): Unit = statement.run {
        assertFalse(isPoolable)
        isPoolable = true
        assertTrue(isPoolable)
    }

    @Test
    fun closeOnCompletion(): Unit = statement.run {
        assertFalse(isCloseOnCompletion)
        closeOnCompletion()
        assertTrue(isCloseOnCompletion)
    }

    @Test
    fun invalidOperationsAfterClose(): Unit = statement.run {
        close()
        assertFailsWith<SQLException> {
            executeUpdate("INSERT INTO users (name) VALUES ('test')")
        }
        assertFailsWith<SQLException> {
            execute("SELECT 1")
        }
        assertFailsWith<SQLException> {
            addBatch("INSERT INTO users (name) VALUES ('test')")
        }
        assertFailsWith<SQLException> {
            executeBatch()
        }
    }

    @Test
    fun queryTypeDetection() {
        val queries = listOf(
            "SELECT * FROM users" to true,
            "  select count(*) from users  " to true,
            "WITH cte AS (SELECT * FROM users) SELECT * FROM cte" to true,
            "INSERT INTO users (name) VALUES ('test')" to false,
            "UPDATE users SET name = 'updated'" to false,
            "DELETE FROM users WHERE id = 1" to false,
            "CREATE TABLE test (id INTEGER)" to false,
            "DROP TABLE test" to false
        )
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doReturn mockCursor
        val mockSqlStatement = mock<ISQLStatement> {
            whenever(it.executeUpdateDelete()) doReturn 1
        }
        whenever(mockDatabase.compileStatement(any<String>(), isNull())) doReturn mockSqlStatement
        queries.forEach { (sql, isQuery) ->
            val result = statement.execute(sql)
            assertEquals(isQuery, result, "SQL: $sql should ${if (isQuery) {
                "produce a result set"
            } else {
                "be an update"
            }}")
        }
    }

    @Test
    fun pragmaQuery() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doReturn mockCursor
        assertTrue(statement.execute("PRAGMA table_info(users)"))
        assertNotNull(statement.resultSet)
    }

    @Test
    fun explainQuery() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doReturn mockCursor
        assertTrue(statement.execute("EXPLAIN SELECT * FROM users"))
        assertNotNull(statement.resultSet)
    }

    @Test
    fun executeUpdateOverloads() {
        val sqlStatement = mock<ISQLStatement> {
            whenever(it.executeUpdateDelete()) doReturn 1
        }
        whenever(mockDatabase.compileStatement(any<String>(), isNull())) doReturn sqlStatement
        statement.run {
            assertEquals(1, executeUpdate("INSERT INTO users (name) VALUES ('test')", Statement.NO_GENERATED_KEYS))
            assertEquals(1, executeUpdate("INSERT INTO users (name) VALUES ('test')", intArrayOf(1)))
            assertEquals(1, executeUpdate("INSERT INTO users (name) VALUES ('test')", arrayOf("id")))
        }
    }

    @Test
    fun executeOverloads() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doReturn mockCursor
        statement.run {
            assertTrue(execute("SELECT * FROM users", Statement.NO_GENERATED_KEYS))
            assertTrue(execute("SELECT * FROM users", intArrayOf(1)))
            assertTrue(execute("SELECT * FROM users", arrayOf("id")))
        }
    }

    @Test
    fun setCursorName() {
        assertFailsWith<SQLException> {
            statement.setCursorName("test")
        }
    }

    @Test
    fun unwrapToSQLDatabase() {
        assertSame(mockDatabase, statement.unwrap(SQLDatabase::class.java))
    }

    @Test
    fun isWrapperForSQLDatabase() {
        assertTrue(statement.isWrapperFor(SQLDatabase::class.java))
    }

    @Test
    fun currentResultSetClearedOnClose() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doReturn mockCursor
        statement.run {
            executeQuery("SELECT * FROM users")
            assertNotNull(resultSet)
            close()
            assertNull(resultSet)
        }
    }

    @Test
    fun getResultSetAfterUpdate() {
        val sqlStatement = mock<ISQLStatement> {
            whenever(it.executeUpdateDelete()) doReturn 1
        }
        whenever(mockDatabase.compileStatement(any<String>(), isNull())) doReturn sqlStatement
        statement.run {
            executeUpdate("INSERT INTO users (name) VALUES ('test')")
            assertNull(resultSet)
        }
    }

    @Test
    fun getUpdateCountAfterQuery() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doReturn mockCursor
        statement.run {
            executeQuery("SELECT * FROM users")
            assertEquals(-1, updateCount)
        }
    }

    @Test
    fun closeIdempotent(): Unit = statement.run {
        assertFalse(isClosed)
        close()
        assertTrue(isClosed)
        close()
        assertTrue(isClosed)
    }

    @Test
    fun executeQueryException() {
        whenever(mockDatabase.query(any<String>(), any<Array<Any?>>())) doThrow RuntimeException("Database error")
        assertFailsWith<SQLException> {
            statement.executeQuery("SELECT * FROM users")
        }
    }

    @Test
    fun executeUpdateException() {
        whenever(mockDatabase.compileStatement(any<String>(), isNull())) doThrow RuntimeException("Database error")
        assertFailsWith<SQLException> {
            statement.executeUpdate("INSERT INTO users (name) VALUES ('test')")
        }
    }

    @Test
    fun executeBatchWithException() {
        whenever(mockDatabase.compileStatement(any<String>(), isNull())) doThrow RuntimeException("Database error")
        statement.run {
            addBatch("INSERT INTO users (name) VALUES ('test')")
            assertFailsWith<SQLException> {
                executeBatch()
            }
        }
    }

    @Test
    fun addBatchWithBlankSql(): Unit = statement.run {
        assertFailsWith<SQLException> {
            addBatch("")
        }
        assertFailsWith<SQLException> {
            addBatch("   ")
        }
    }

    @Test
    fun executeBatchWithSelectStatement(): Unit = statement.run {
        addBatch("SELECT * FROM users")
        assertFailsWith<SQLException> {
            executeBatch()
        }
    }

    @Test
    fun executeBatchClearsOnFailure() {
        val sqlStatement = mock<ISQLStatement> {
            whenever(it.executeUpdateDelete()) doReturn 1
        }
        mockDatabase.apply {
            whenever(compileStatement("INSERT INTO users (name) VALUES ('test')", null)) doReturn sqlStatement
            whenever(compileStatement("INVALID SQL", null)) doThrow SQLException("Syntax error")
        }
        statement.run {
            addBatch("INSERT INTO users (name) VALUES ('test')")
            addBatch("INVALID SQL")
            assertFailsWith<SQLException> {
                executeBatch()
            }
            assertEquals(0, executeBatch().size)
        }
    }

    @Test
    fun setPoolableWhenClosed(): Unit = statement.run {
        close()
        assertFailsWith<SQLException> {
            isPoolable = true
        }
    }

    @Test
    fun setMaxFieldSizeWhenClosed(): Unit = statement.run {
        close()
        assertFailsWith<SQLException> {
            maxFieldSize = 100
        }
    }

    @Test
    fun addBatchWhenClosed(): Unit = statement.run {
        close()
        assertFailsWith<SQLException> {
            addBatch("INSERT INTO users (name) VALUES ('test')")
        }
    }

    @Test
    fun clearBatchWhenClosed(): Unit = statement.run {
        close()
        assertFailsWith<SQLException> {
            clearBatch()
        }
    }

    @Test
    fun closeOnCompletionWhenClosed(): Unit = statement.run {
        close()
        assertFailsWith<SQLException> {
            closeOnCompletion()
        }
    }
}
