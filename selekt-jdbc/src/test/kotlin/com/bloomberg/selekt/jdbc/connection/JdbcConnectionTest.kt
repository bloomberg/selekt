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

import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.jdbc.statement.JdbcPreparedStatement
import com.bloomberg.selekt.jdbc.statement.JdbcStatement
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Savepoint
import java.util.Properties
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class JdbcConnectionTest {
    private lateinit var mockDatabase: SQLDatabase
    private lateinit var connectionURL: ConnectionURL
    private lateinit var properties: Properties
    private lateinit var connection: JdbcConnection

    @BeforeEach
    fun setUp() {
        mockDatabase = mock()
        connectionURL = ConnectionURL.parse("jdbc:sqlite:/tmp/test.db")
        properties = Properties()
        connection = JdbcConnection(mockDatabase, connectionURL, properties)
    }

    @Test
    fun autoCommitDefault() {
        assertTrue(connection.autoCommit)
    }

    @Test
    fun setAutoCommitTrue(): Unit = connection.run {
        autoCommit = true
        assertTrue(autoCommit)
    }

    @Test
    fun setAutoCommitFalse(): Unit = connection.run {
        autoCommit = false
        assertFalse(autoCommit)
    }

    @Test
    fun commitWithAutoCommitEnabled(): Unit = connection.run {
        autoCommit = true
        assertFailsWith<SQLException> {
            commit()
        }
    }

    @Test
    fun rollbackWithAutoCommitEnabled(): Unit = connection.run {
        autoCommit = true
        assertFailsWith<SQLException> {
            rollback()
        }
    }

    @Test
    fun transactionOperations(): Unit = connection.run {
        autoCommit = false
        commit()
        rollback()
    }

    @Test
    fun autoCommitSwitchingPattern(): Unit = connection.run {
        assertTrue(autoCommit)
        autoCommit = false
        assertFalse(autoCommit)
        autoCommit = true
        assertTrue(autoCommit)
        autoCommit = false
        assertFalse(autoCommit)
        autoCommit = true
        assertTrue(autoCommit)
    }

    @Test
    fun autoCommitModeIdempotent(): Unit = connection.run {
        assertTrue(autoCommit)
        autoCommit = true
        assertTrue(autoCommit)
        autoCommit = true
        assertTrue(autoCommit)
        autoCommit = false
        assertFalse(autoCommit)
        autoCommit = false
        assertFalse(autoCommit)
    }

    @Test
    fun createStatement(): Unit = connection.createStatement().run {
        assertNotNull(this)
        assertTrue(this is JdbcStatement)
    }

    @Test
    fun createStatementWithScrollable() {
        assertFailsWith<SQLException> {
            connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            )
        }
    }

    @Test
    fun prepareStatement() {
        connection.prepareStatement("SELECT * FROM test WHERE id = ?").run {
            assertNotNull(this)
            assertTrue(this is JdbcPreparedStatement)
        }
    }

    @Test
    fun prepareStatementWithScrollable() {
        assertFailsWith<SQLException> {
            connection.prepareStatement(
                "SELECT * FROM test",
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
            )
        }
    }

    @Test
    fun prepareCall() {
        assertFailsWith<SQLException> {
            connection.prepareCall("{call test()}")
        }
    }

    @Test
    fun nativeSQL() {
        "SELECT * FROM test".let { sql ->
            assertEquals(sql, connection.nativeSQL(sql))
        }
    }

    @Test
    fun closure(): Unit = connection.run {
        assertFalse(isClosed)
        connection.close()
        assertTrue(isClosed)
        connection.close()
        assertTrue(isClosed)
    }

    @Test
    fun operationsAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            createStatement()
        }
        assertFailsWith<SQLException> {
            prepareStatement("SELECT 1")
        }
        assertFailsWith<SQLException> {
            commit()
        }
    }

    @Test
    fun getMetaData(): Unit = connection.metaData.let { metaData ->
        assertNotNull(metaData)
        assertEquals(connection, metaData.connection)
    }

    @Test
    fun readOnlyOperations(): Unit = connection.run {
        assertFalse(isReadOnly)
        isReadOnly = true
        assertTrue(isReadOnly)
        isReadOnly = false
        assertFalse(isReadOnly)
    }

    @Test
    fun catalogOperations(): Unit = connection.run {
        assertNull(catalog)
        catalog = "test_catalog"
        assertNull(catalog)
    }

    @Test
    fun transactionIsolation(): Unit = connection.run {
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, transactionIsolation)
        transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, transactionIsolation)
    }

    @Test
    fun warnings(): Unit = connection.run {
        assertNull(warnings)
        clearWarnings()
    }

    @Test
    fun clientInfo(): Unit = connection.run {
        clientInfo.let { clientInfo ->
            assertNotNull(clientInfo)
            assertTrue(clientInfo.isEmpty)
        }
        setClientInfo("ApplicationName", "Test")
        assertTrue(clientInfo.isEmpty)
    }

    @Test
    fun savepointOperations(): Unit = connection.run {
        autoCommit = false
        val savepoint = setSavepoint()
        assertNotNull(savepoint)
        val namedSavepoint = setSavepoint("test_savepoint")
        assertNotNull(namedSavepoint)
        assertEquals("test_savepoint", namedSavepoint.savepointName)
        rollback(savepoint)
        releaseSavepoint(namedSavepoint)
    }

    @Test
    fun savepointWithAutoCommit() {
        assertFailsWith<SQLException> {
            connection.setSavepoint()
        }
    }

    @Test
    fun holdability(): Unit = connection.run {
        assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, holdability)
        holdability = ResultSet.HOLD_CURSORS_OVER_COMMIT
        assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, holdability)
    }

    @Test
    fun schema(): Unit = connection.run {
        assertNull(schema)
        schema = "test_schema"
        assertNull(schema)
    }

    @Test
    fun abort(): Unit = connection.run {
        val executor = Executors.newSingleThreadExecutor()
        try {
            abort(executor)
            assertTrue(isClosed)
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun networkTimeout(): Unit = connection.run {
        assertEquals(0, networkTimeout)
        val executor = Executors.newSingleThreadExecutor()
        try {
            setNetworkTimeout(executor, 5_000)
            assertEquals(0, connection.networkTimeout)
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun wrapperInterface(): Unit = connection.run {
        assertTrue(isWrapperFor(JdbcConnection::class.java))
        assertFalse(isWrapperFor(String::class.java))
        assertEquals(this, unwrap(JdbcConnection::class.java))
        assertFailsWith<SQLException> {
            unwrap(String::class.java)
        }
    }

    @Test
    fun createBlob() {
        assertFailsWith<SQLException> {
            connection.createBlob()
        }
    }

    @Test
    fun createClob(): Unit = connection.createClob().run {
        assertNotNull(this)
        setString(1, "test")
        assertEquals("test", getSubString(1, 4))
    }

    @Test
    fun createNClob() {
        assertFailsWith<SQLException> {
            connection.createNClob()
        }
    }

    @Test
    fun createSQLXML() {
        assertFailsWith<SQLException> {
            connection.createSQLXML()
        }
    }

    @Test
    fun createArrayOf() {
        assertFailsWith<SQLException> {
            connection.createArrayOf("VARCHAR", arrayOf("test"))
        }
    }

    @Test
    fun createStruct() {
        assertFailsWith<SQLException> {
            connection.createStruct("TestStruct", arrayOf("value"))
        }
    }

    @Test
    fun isValid(): Unit = connection.run {
        assertTrue(isValid(0))
        assertTrue(isValid(5))
        close()
        assertFalse(isValid(0))
    }

    @Test
    fun closedStateIsThreadSafe(): Unit = connection.run {
        (1..10).map {
            Thread {
                close()
            }
        }.apply {
            forEach { it.start() }
        }.apply {
            forEach { it.join() }
        }
        assertTrue(isClosed)
    }

    @Test
    fun abortCallsClose(): Unit = connection.run {
        val executor = Executors.newSingleThreadExecutor()
        try {
            assertFalse(isClosed)
            abort(executor)
            assertTrue(isClosed)
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun closedStatePreventsFurtherOperations(): Unit = connection.run {
        close()
        assertTrue(isClosed)
        assertFailsWith<SQLException> {
            createStatement()
        }
        assertFailsWith<SQLException> {
            prepareStatement("SELECT 1")
        }
        assertFailsWith<SQLException> {
            commit()
        }
        assertFailsWith<SQLException> {
            rollback()
        }
        assertFailsWith<SQLException> {
            setAutoCommit(false)
        }
        assertFailsWith<SQLException> {
            setSavepoint()
        }
        assertFailsWith<SQLException> {
            createClob()
        }
    }

    @Test
    fun multipleCloseCallsAreIdempotent(): Unit = connection.run {
        assertFalse(isClosed)
        close()
        repeat(2) {
            assertTrue(isClosed)
            close()
        }
        assertTrue(isClosed)
    }

    @Test
    fun createStatementWithUpdatableConcurrency() {
        assertFailsWith<SQLException> {
            connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_UPDATABLE
            )
        }
    }

    @Test
    fun createStatementWithAllParameters() {
        connection.createStatement(
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY,
            ResultSet.CLOSE_CURSORS_AT_COMMIT
        ).run {
            assertNotNull(this)
            assertTrue(this is JdbcStatement)
        }
    }

    @Test
    fun createStatementWithInvalidHoldability() {
        connection.createStatement(
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY,
            ResultSet.HOLD_CURSORS_OVER_COMMIT
        ).run {
            assertNotNull(this)
        }
    }

    @Test
    fun prepareStatementWithUpdatableConcurrency() {
        assertFailsWith<SQLException> {
            connection.prepareStatement(
                "SELECT * FROM test",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_UPDATABLE
            )
        }
    }

    @Test
    fun prepareStatementWithAllParameters() {
        connection.prepareStatement(
            "SELECT * FROM test",
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY,
            ResultSet.CLOSE_CURSORS_AT_COMMIT
        ).run {
            assertNotNull(this)
            assertTrue(this is JdbcPreparedStatement)
        }
    }

    @Test
    fun prepareStatementWithAutoGeneratedKeys() {
        connection.prepareStatement(
            "INSERT INTO test VALUES (?)",
            java.sql.Statement.RETURN_GENERATED_KEYS
        ).run {
            assertNotNull(this)
            assertTrue(this is JdbcPreparedStatement)
        }
    }

    @Test
    fun prepareStatementWithColumnIndexes() {
        connection.prepareStatement(
            "INSERT INTO test VALUES (?)",
            intArrayOf(1, 2)
        ).run {
            assertNotNull(this)
            assertTrue(this is JdbcPreparedStatement)
        }
    }

    @Test
    fun prepareStatementWithColumnNames() {
        connection.prepareStatement(
            "INSERT INTO test VALUES (?)",
            arrayOf("id", "name")
        ).run {
            assertNotNull(this)
            assertTrue(this is JdbcPreparedStatement)
        }
    }

    @Test
    fun prepareCallWithResultSetParameters() {
        assertFailsWith<SQLException> {
            connection.prepareCall(
                "{call test()}",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
            )
        }
    }

    @Test
    fun prepareCallWithAllParameters() {
        assertFailsWith<SQLException> {
            connection.prepareCall(
                "{call test()}",
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT
            )
        }
    }

    @Test
    fun rollbackWithNullSavepoint(): Unit = connection.run {
        autoCommit = false
        rollback(null)
    }

    @Test
    fun getClientInfoWithName(): Unit = connection.run {
        assertNull(getClientInfo("ApplicationName"))
    }

    @Test
    fun setClientInfoWithProperties(): Unit = connection.run {
        setClientInfo(Properties().apply {
            setProperty("ApplicationName", "Test")
            setProperty("ClientUser", "testuser")
        })
        assertNotNull(warnings)
    }

    @Test
    fun getTypeMap(): Unit = connection.run {
        assertNotNull(typeMap)
        assertTrue(typeMap.isEmpty())
    }

    @Test
    fun setTypeMap(): Unit = connection.run {
        setTypeMap(mutableMapOf<String, Class<*>>().apply {
            this["CustomType"] = String::class.java
        })
    }

    @Test
    fun setHoldabilityUnsupportedValue() {
        assertFailsWith<SQLException> {
            connection.holdability = 999
        }
    }

    @Test
    fun isValidWithNegativeTimeout() {
        assertFalse(connection.isValid(-1))
    }

    @Test
    fun setNetworkTimeoutNegative() {
        val executor = Executors.newSingleThreadExecutor()
        try {
            assertFailsWith<SQLException> {
                connection.setNetworkTimeout(executor, -1)
            }
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun unwrapToSQLDatabase(): Unit = connection.run {
        assertTrue(isWrapperFor(SQLDatabase::class.java))
        unwrap(SQLDatabase::class.java).let {
            assertNotNull(it)
            assertEquals(mockDatabase, it)
        }
    }

    @Test
    fun warningsAccumulation(): Unit = connection.run {
        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED)
        assertNotNull(warnings)
        holdability = ResultSet.HOLD_CURSORS_OVER_COMMIT
        assertNotNull(warnings)
        setClientInfo("test", "value")
        assertNotNull(warnings)
        val executor = Executors.newSingleThreadExecutor()
        try {
            setNetworkTimeout(executor, 5000)
            assertNotNull(warnings)
        } finally {
            executor.shutdown()
        }
        clearWarnings()
        assertNull(warnings)
    }

    @Test
    fun nativeSQLAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            nativeSQL("SELECT 1")
        }
    }

    @Test
    fun prepareCallAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            prepareCall("{call test()}")
        }
    }

    @Test
    fun setCatalogAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            catalog = "test"
        }
    }

    @Test
    fun setTransactionIsolationAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
        }
    }

    @Test
    fun setTypeMapAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            setTypeMap(mutableMapOf())
        }
    }

    @Test
    fun setHoldabilityAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            holdability = ResultSet.CLOSE_CURSORS_AT_COMMIT
        }
    }

    @Test
    fun setClientInfoStringAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            setClientInfo("test", "value")
        }
    }

    @Test
    fun setClientInfoPropertiesAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            setClientInfo(Properties())
        }
    }

    @Test
    fun getClientInfoAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            getClientInfo("test")
        }
    }

    @Test
    fun getClientInfoPropertiesAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            clientInfo
        }
    }

    @Test
    fun setSchemaAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            schema = "test"
        }
    }

    @Test
    fun setNetworkTimeoutAfterClose(): Unit = connection.run {
        close()
        val executor = Executors.newSingleThreadExecutor()
        try {
            assertFailsWith<SQLException> {
                setNetworkTimeout(executor, 1000)
            }
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun setReadOnlyAfterClose(): Unit = connection.run {
        close()
        assertFailsWith<SQLException> {
            isReadOnly = true
        }
    }

    @Test
    fun releaseSavepointAfterClose() {
        val savepoint = mock<Savepoint>()
        connection.run {
            close()
            assertFailsWith<SQLException> {
                releaseSavepoint(savepoint)
            }
        }
    }

    @Test
    fun rollbackSavepointWithAutoCommit() {
        val savepoint = mock<Savepoint>()
        connection.run {
            autoCommit = true
            assertFailsWith<SQLException> {
                rollback(savepoint)
            }
        }
    }

    @Test
    fun setAutoCommitErrorHandling() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
            whenever(it.setTransactionSuccessful()) doThrow RuntimeException("Database error")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            assertFailsWith<SQLException> {
                autoCommit = true
            }
        }
    }

    @Test
    fun setAutoCommitEndTransactionErrorHandling() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
            whenever(it.endTransaction()) doThrow RuntimeException("End transaction failed")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            assertFailsWith<SQLException> {
                autoCommit = true
            }
        }
    }

    @Test
    fun commitErrorHandling() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
            whenever(it.setTransactionSuccessful()) doThrow RuntimeException("Commit failed")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            assertFailsWith<SQLException> {
                commit()
            }
        }
    }

    @Test
    fun rollbackErrorHandling() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
            whenever(it.endTransaction()) doThrow RuntimeException("Rollback failed")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            assertFailsWith<SQLException> {
                rollback()
            }
        }
    }

    @Test
    fun rollbackSavepointErrorHandling() {
        val database = mock<SQLDatabase> {
            whenever(it.exec("ROLLBACK TO SAVEPOINT test_sp")) doThrow RuntimeException("Savepoint rollback failed")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            val savepoint = mock<Savepoint> {
                whenever(it.savepointName) doReturn "test_sp"
            }
            assertFailsWith<SQLException> {
                rollback(savepoint)
            }
        }
    }

    @Test
    fun setSavepointErrorHandling() {
        val database = mock<SQLDatabase>()
        whenever(database.exec("SAVEPOINT test_savepoint")) doThrow RuntimeException("Savepoint creation failed")
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            assertFailsWith<SQLException> {
                setSavepoint("test_savepoint")
            }
        }
    }

    @Test
    fun setSavepointId(): Unit = connection.run {
        autoCommit = false
        assertEquals(0, connection.setSavepoint().savepointId)
    }

    @Test
    fun releaseSavepointErrorHandling() {
        val database = mock<SQLDatabase> {
            whenever(it.exec("RELEASE SAVEPOINT test_sp")) doThrow RuntimeException("Release failed")
        }
        JdbcConnection(database, connectionURL, properties).run {
            val savepoint = mock<Savepoint> {
                whenever(it.savepointName) doReturn "test_sp"
            }
            assertFailsWith<SQLException> {
                releaseSavepoint(savepoint)
            }
        }
    }

    @Test
    fun closeWithTransactionErrorHandling() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
            whenever(it.endTransaction()) doThrow RuntimeException("Transaction end failed")
        }
        JdbcConnection(database, connectionURL, properties).run {
            close()
            assertTrue(isClosed)
        }
    }

    @Test
    fun applyConnectionPropertiesErrorHandling() {
        val database = mock<SQLDatabase> {
            whenever(it.exec("PRAGMA foreign_keys = 1")) doThrow RuntimeException("PRAGMA failed")
        }
        assertFailsWith<SQLException> {
            JdbcConnection(database, connectionURL, properties)
        }
    }

    @Test
    fun ensureTransactionCalled() {
        val transactionDatabase = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn false
        }
        JdbcConnection(transactionDatabase, connectionURL, properties).apply {
            autoCommit = false
            ensureTransaction()
        }
        verify(transactionDatabase).beginImmediateTransaction()
    }

    @Test
    fun ensureTransactionNotCalledWhenInTransaction() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            ensureTransaction()
        }
        verify(database, never()).beginImmediateTransaction()
    }

    @Test
    fun ensureTransactionNotCalledInAutoCommit() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn false
        }
        JdbcConnection(database, connectionURL, properties).apply {
            autoCommit = true
            ensureTransaction()
        }
        verify(database, never()).beginImmediateTransaction()
    }

    @Test
    fun commitEndTransactionErrorHandling() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
            whenever(it.endTransaction()) doThrow RuntimeException("End transaction failed")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            assertFailsWith<SQLException> {
                commit()
            }
        }
    }

    @Test
    fun foreignKeysDisabled() {
        mock<SQLDatabase>().run {
            JdbcConnection(this, connectionURL, Properties().apply {
                setProperty("foreignKeys", "false")
            })
            verify(this).exec("PRAGMA foreign_keys = 0")
        }
    }

    @Test
    fun setAutoCommitWithSQLException() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
            whenever(it.setTransactionSuccessful()) doThrow SQLException("SQL error")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            val exception = assertFailsWith<SQLException> {
                autoCommit = true
            }
            assertNotNull(exception.message)
        }
    }

    @Test
    fun commitWithSQLException() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
            whenever(it.setTransactionSuccessful()) doThrow SQLException("Commit SQL error")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            val exception = assertFailsWith<SQLException> {
                commit()
            }
            assertNotNull(exception.message)
        }
    }

    @Test
    fun rollbackWithSQLException() {
        val database = mock<SQLDatabase> {
            whenever(it.inTransaction) doReturn true
            whenever(it.endTransaction()) doThrow SQLException("Rollback SQL error")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            val exception = assertFailsWith<SQLException> {
                rollback()
            }
            assertNotNull(exception.message)
        }
    }

    @Test
    fun setSavepointWithSQLException() {
        val database = mock<SQLDatabase> {
            whenever(it.exec("SAVEPOINT test_savepoint")) doThrow SQLException("Savepoint SQL error")
        }
        JdbcConnection(database, connectionURL, properties).run {
            autoCommit = false
            val exception = assertFailsWith<SQLException> {
                setSavepoint("test_savepoint")
            }
            assertNotNull(exception.message)
        }
    }

    @Test
    fun releaseSavepointWithSQLException() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        val database = mock<SQLDatabase> {
            whenever(it.exec("RELEASE SAVEPOINT test_sp")) doThrow SQLException("Release SQL error")
        }
        JdbcConnection(database, connectionURL, properties).run {
            val exception = assertFailsWith<SQLException> {
                releaseSavepoint(savepoint)
            }
            assertNotNull(exception.message)
        }
    }

    @Test
    fun setTransactionIsolationUnsupported(): Unit = connection.run {
        setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED)
        warnings.let {
            assertNotNull(it)
            assertTrue(it.message?.contains("TRANSACTION_SERIALIZABLE") ?: false)
        }
    }

    @Test
    fun setHoldabilityHoldCursorsOverCommit(): Unit = connection.run {
        setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT)
        warnings.let {
            assertNotNull(it)
            assertTrue(it.message?.contains("HOLD_CURSORS_OVER_COMMIT") ?: false)
        }
    }

    @Test
    fun setHoldabilityInvalid() {
        assertFailsWith<SQLException> {
            connection.setHoldability(999)
        }
    }

    @Test
    fun setTransactionIsolationSerializable(): Unit = connection.run {
        setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE)
        assertEquals(Connection.TRANSACTION_SERIALIZABLE, transactionIsolation)
    }

    @Test
    fun setHoldabilityCloseCursorsAtCommit(): Unit = connection.run {
        setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT)
        assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, holdability)
    }

    @Test
    fun rollbackSavepointWithSQLException() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        val database = mock<SQLDatabase> {
            whenever(it.exec("ROLLBACK TO SAVEPOINT test_sp")) doThrow SQLException("Rollback to savepoint failed")
        }
        JdbcConnection(database, connectionURL, properties).run {
            val exception = assertFailsWith<SQLException> {
                rollback(savepoint)
            }
            assertNotNull(exception.message)
        }
    }

    @Test
    fun releaseSavepointWithDirectSQLException() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        val database = mock<SQLDatabase> {
            whenever(it.exec("RELEASE SAVEPOINT test_sp")) doThrow SQLException("Release failed", "HY000", 999)
        }
        JdbcConnection(database, connectionURL, properties).run {
            val exception = assertFailsWith<SQLException> {
                releaseSavepoint(savepoint)
            }
            assertNotNull(exception.message)
        }
    }

    @Test
    fun rollbackSavepointWithRuntimeException() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        val database = mock<SQLDatabase> {
            whenever(it.exec("ROLLBACK TO SAVEPOINT test_sp")) doThrow RuntimeException("Rollback runtime error")
        }
        JdbcConnection(database, connectionURL, properties).run {
            val exception = assertFailsWith<SQLException> {
                rollback(savepoint)
            }
            assertNotNull(exception.message)
        }
    }

    @Test
    fun applyConnectionPropertiesWithSQLException() {
        val database = mock<SQLDatabase> {
            whenever(it.exec("PRAGMA foreign_keys = 1")) doThrow SQLException("PRAGMA failed", "HY000", 100)
        }
        assertFailsWith<SQLException> {
            JdbcConnection(database, connectionURL, properties)
        }
    }

    @Test
    fun rollbackSavepointWithNullMessageException() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        val database = mock<SQLDatabase> {
            whenever(it.exec("ROLLBACK TO SAVEPOINT test_sp")) doThrow SQLException(null, "HY000", 100)
        }
        JdbcConnection(database, connectionURL, properties).run {
            val exception = assertFailsWith<SQLException> {
                rollback(savepoint)
            }
            assertNotNull(exception)
        }
    }

    @Test
    fun rollbackSavepointWithRuntimeExceptionNullMessage() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        val customException = object : RuntimeException(null as String?) {}
        val database = mock<SQLDatabase> {
            whenever(it.exec("ROLLBACK TO SAVEPOINT test_sp")) doThrow customException
        }
        JdbcConnection(database, connectionURL, properties).run {
            val exception = assertFailsWith<SQLException> {
                rollback(savepoint)
            }
            assertNotNull(exception)
        }
    }

    @Test
    fun rollbackSavepointWithSQLExceptionEmptyMessage() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        val database = mock<SQLDatabase> {
            whenever(it.exec("ROLLBACK TO SAVEPOINT test_sp")) doThrow SQLException("")
        }
        JdbcConnection(database, connectionURL, properties).run {
            assertFailsWith<SQLException> {
                rollback(savepoint)
            }
        }
    }

    @Test
    fun rollbackSavepointWithRuntimeExceptionEmptyMessage() {
        val savepoint = mock<Savepoint> {
            whenever(it.savepointName) doReturn "test_sp"
        }
        val database = mock<SQLDatabase> {
            whenever(it.exec("ROLLBACK TO SAVEPOINT test_sp")) doThrow RuntimeException("")
        }
        JdbcConnection(database, connectionURL, properties).run {
            assertFailsWith<SQLException> {
                rollback(savepoint)
            }
        }
    }
}
