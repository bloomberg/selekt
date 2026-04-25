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
import com.bloomberg.selekt.SQLitePragma
import com.bloomberg.selekt.jdbc.driver.SharedDatabase
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.lob.JdbcClob
import com.bloomberg.selekt.jdbc.metadata.JdbcDatabaseMetaData
import com.bloomberg.selekt.jdbc.statement.JdbcPreparedStatement
import com.bloomberg.selekt.jdbc.statement.JdbcStatement
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import java.sql.Blob
import java.sql.CallableStatement
import java.sql.Clob
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.NClob
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.SQLWarning
import java.sql.SQLXML
import java.sql.Savepoint
import java.sql.Statement
import java.sql.Struct
import java.lang.invoke.MethodHandles
import java.util.Properties
import java.util.concurrent.Executor
import javax.annotation.concurrent.NotThreadSafe
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("MethodOverloading", "TooGenericExceptionCaught", "Detekt.StringLiteralDuplication")
@NotThreadSafe
internal class JdbcConnection(
    private val sharedDatabase: SharedDatabase,
    private val connectionURL: ConnectionURL,
    private val properties: Properties
) : Connection {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(JdbcConnection::class.java)

        private val CLOSED = MethodHandles.lookup().findVarHandle(
            JdbcConnection::class.java,
            "closed",
            Boolean::class.javaPrimitiveType
        )
    }

    private val database: SQLDatabase get() = sharedDatabase.database

    @Volatile
    private var closed = false
    private var autoCommit = true
    private var readOnly = false
    private var transactionIsolation = Connection.TRANSACTION_SERIALIZABLE
    private var networkTimeout = 0
    private val holdability = ResultSet.CLOSE_CURSORS_AT_COMMIT
    private val warnings = mutableListOf<SQLWarning>()

    private val _metaData by lazy { JdbcDatabaseMetaData(this, database, connectionURL) }

    init {
        applyConnectionProperties()
    }

    override fun createStatement(): Statement = createStatement(
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY,
        holdability
    )

    override fun createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement = createStatement(
        resultSetType,
        resultSetConcurrency,
        holdability
    )

    override fun createStatement(
        resultSetType: Int,
        resultSetConcurrency: Int,
        resultSetHoldability: Int
    ): Statement {
        checkClosed()
        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY) {
            throw SQLException("SQLite only supports TYPE_FORWARD_ONLY result sets")
        }
        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
            throw SQLException("SQLite only supports CONCUR_READ_ONLY concurrency")
        }
        return JdbcStatement(this, database, resultSetType, resultSetConcurrency, resultSetHoldability)
    }

    override fun prepareStatement(sql: String): PreparedStatement = prepareStatement(
        sql,
        ResultSet.TYPE_FORWARD_ONLY,
        ResultSet.CONCUR_READ_ONLY,
        holdability
    )

    override fun prepareStatement(
        sql: String,
        resultSetType: Int,
        resultSetConcurrency: Int
    ): PreparedStatement = prepareStatement(sql, resultSetType, resultSetConcurrency, holdability)

    override fun prepareStatement(
        sql: String,
        resultSetType: Int,
        resultSetConcurrency: Int,
        resultSetHoldability: Int
    ): PreparedStatement {
        checkClosed()
        if (resultSetType != ResultSet.TYPE_FORWARD_ONLY) {
            throw SQLException("SQLite only supports TYPE_FORWARD_ONLY result sets")
        }
        if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
            throw SQLException("SQLite only supports CONCUR_READ_ONLY concurrency")
        }
        return JdbcPreparedStatement(this, database, sql, resultSetType, resultSetConcurrency, resultSetHoldability)
    }

    override fun prepareStatement(
        sql: String,
        autoGeneratedKeys: Int
    ): PreparedStatement {
        checkClosed()
        return JdbcPreparedStatement(
            this,
            database,
            sql,
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY,
            holdability,
        )
    }

    override fun prepareStatement(
        sql: String,
        columnIndexes: IntArray
    ): PreparedStatement {
        checkClosed()
        return JdbcPreparedStatement(
            this,
            database,
            sql,
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY,
            holdability,
        )
    }

    override fun prepareStatement(
        sql: String,
        columnNames: Array<out String>
    ): PreparedStatement {
        checkClosed()
        return JdbcPreparedStatement(
            this,
            database,
            sql,
            ResultSet.TYPE_FORWARD_ONLY,
            ResultSet.CONCUR_READ_ONLY,
            holdability,
        )
    }

    override fun prepareCall(sql: String): CallableStatement {
        checkClosed()
        throw SQLException("SQLite does not support stored procedures")
    }

    override fun prepareCall(
        sql: String,
        resultSetType: Int,
        resultSetConcurrency: Int
    ): CallableStatement {
        checkClosed()
        throw SQLException("SQLite does not support stored procedures")
    }

    override fun prepareCall(
        sql: String,
        resultSetType: Int,
        resultSetConcurrency: Int,
        resultSetHoldability: Int
    ): CallableStatement {
        checkClosed()
        throw SQLException("SQLite does not support stored procedures")
    }

    override fun nativeSQL(sql: String): String {
        checkClosed()
        return sql
    }

    override fun setAutoCommit(autoCommit: Boolean) {
        checkClosed()
        if (this.autoCommit == autoCommit) {
            return
        }
        database.runCatching {
            if (autoCommit && inTransaction) {
                setTransactionSuccessful()
                endTransaction()
            }
            this@JdbcConnection.autoCommit = autoCommit
        }.onFailure { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun getAutoCommit(): Boolean = autoCommit

    override fun commit() {
        checkClosed()
        if (autoCommit) {
            throw SQLException("Cannot call commit() while in auto-commit mode")
        }
        database.runCatching {
            if (inTransaction) {
                setTransactionSuccessful()
                endTransaction()
            }
        }.onFailure { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun rollback() {
        checkClosed()
        if (autoCommit) {
            throw SQLException("Cannot call rollback() while in auto-commit mode")
        }
        database.runCatching {
            if (inTransaction) {
                endTransaction()
            }
        }.onFailure { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun rollback(savepoint: Savepoint?) {
        checkClosed()
        if (autoCommit) {
            throw SQLException("Cannot call rollback() while in auto-commit mode")
        } else if (savepoint == null) {
            rollback()
            return
        }
        runCatching {
            database.rollbackToSavepoint(savepoint.savepointName)
        }.onFailure { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun setSavepoint(): Savepoint = setSavepoint(null)

    override fun setSavepoint(name: String?): Savepoint {
        checkClosed()
        if (autoCommit) {
            throw SQLException("Cannot create savepoint while in auto-commit mode")
        }
        return runCatching {
            val savepointName = database.setSavepoint(name)
            object : Savepoint {
                override fun getSavepointId(): Int = 0

                override fun getSavepointName(): String = savepointName
            }
        }.getOrElse { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun releaseSavepoint(savepoint: Savepoint) {
        checkClosed()
        runCatching {
            database.releaseSavepoint(savepoint.savepointName)
        }.onFailure { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun close() {
        if (CLOSED.compareAndSet(this, false, true)) {
            sharedDatabase.runCatching {
                release()
            }.onFailure { e ->
                logger.warn("Error releasing database on connection close: ${e.message}")
            }
        }
    }

    override fun isClosed(): Boolean = closed

    override fun getMetaData(): DatabaseMetaData = _metaData

    override fun setReadOnly(readOnly: Boolean) {
        checkClosed()
        database.pragma(SQLitePragma.QUERY_ONLY, if (readOnly) { 1 } else { 0 })
        this.readOnly = readOnly
    }

    override fun isReadOnly(): Boolean = readOnly

    override fun setCatalog(catalog: String?) {
        checkClosed()
    }

    override fun getCatalog(): String? = null

    override fun setTransactionIsolation(level: Int) {
        checkClosed()
        when (level) {
            Connection.TRANSACTION_SERIALIZABLE -> transactionIsolation = level
            else -> addWarning(
                SQLWarning("SQLite only supports TRANSACTION_SERIALIZABLE isolation level, ignoring level: $level")
            )
        }
    }

    override fun getTransactionIsolation(): Int = transactionIsolation

    override fun getWarnings(): SQLWarning? = warnings.firstOrNull()

    override fun clearWarnings() {
        warnings.clear()
    }

    override fun getTypeMap(): MutableMap<String, Class<*>> = mutableMapOf()

    override fun setTypeMap(map: MutableMap<String, Class<*>>?) {
        checkClosed()
    }

    override fun setHoldability(holdability: Int) {
        checkClosed()
        when (holdability) {
            ResultSet.CLOSE_CURSORS_AT_COMMIT -> Unit
            ResultSet.HOLD_CURSORS_OVER_COMMIT -> addWarning(
                SQLWarning("SQLite does not support holdable cursors, ignoring HOLD_CURSORS_OVER_COMMIT")
            )
            else -> throw SQLException("Unsupported holdability: $holdability")
        }
    }

    override fun getHoldability(): Int = holdability

    override fun setClientInfo(name: String, value: String?) {
        checkClosed()
        addWarning(SQLWarning("SQLite does not support client info properties, ignoring: $name=$value"))
    }

    override fun setClientInfo(properties: Properties?) {
        checkClosed()
        addWarning(SQLWarning("SQLite does not support client info properties, ignoring properties"))
    }

    override fun getClientInfo(name: String): String? {
        checkClosed()
        return null
    }

    override fun getClientInfo(): Properties {
        checkClosed()
        return Properties()
    }

    override fun createArrayOf(
        typeName: String,
        elements: Array<out Any?>
    ): java.sql.Array = throw SQLFeatureNotSupportedException("SQLite does not support arrays")

    override fun createStruct(
        typeName: String,
        attributes: Array<out Any?>
    ): Struct = throw SQLFeatureNotSupportedException("SQLite does not support structs")

    override fun createClob(): Clob {
        checkClosed()
        return JdbcClob()
    }

    override fun createBlob(): Blob = throw SQLFeatureNotSupportedException("Use byte arrays instead of BLOBs with SQLite")

    override fun createNClob(): NClob = throw SQLFeatureNotSupportedException("SQLite does not support NCLOBs")

    override fun createSQLXML(): SQLXML = throw SQLFeatureNotSupportedException("SQLite does not support SQLXML")

    override fun isValid(timeout: Int): Boolean {
        if (timeout < 0) {
            throw SQLException("Timeout must be non-negative")
        }
        if (isClosed) {
            return false
        }
        return runCatching {
            database.exec("SELECT 1")
            true
        }.getOrElse { e ->
            logger.warn("Connection validation failed: {}", e.message)
            false
        }
    }

    override fun setNetworkTimeout(executor: Executor?, milliseconds: Int) {
        checkClosed()
        if (milliseconds < 0) {
            throw SQLException("Network timeout must be non-negative")
        }
        addWarning(SQLWarning("SQLite does not support network timeouts, ignoring timeout: $milliseconds"))
    }

    override fun getNetworkTimeout(): Int = networkTimeout

    override fun getSchema(): String? = null

    override fun setSchema(schema: String?) {
        checkClosed()
    }

    override fun abort(executor: Executor?) {
        close()
    }

    override fun <T> unwrap(iface: Class<T>): T = if (iface.isAssignableFrom(this::class.java)) {
        @Suppress("UNCHECKED_CAST")
        this as T
    } else if (iface.isAssignableFrom(SQLDatabase::class.java)) {
        @Suppress("UNCHECKED_CAST")
        database as T
    } else {
        throw SQLException("Cannot unwrap to ${iface.name}")
    }

    override fun isWrapperFor(iface: Class<*>): Boolean = iface.isAssignableFrom(this::class.java) ||
        iface.isAssignableFrom(SQLDatabase::class.java)

    private fun checkClosed() {
        if (isClosed) {
            throw SQLException("Connection is closed")
        }
    }

    private fun applyConnectionProperties() {
        runCatching {
            val foreignKeys = properties.getProperty("foreignKeys")?.toBoolean() ?: true
            database.exec("PRAGMA foreign_keys = ${if (foreignKeys) { 1 } else { 0 } }")
        }.onFailure { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    internal fun ensureTransaction() {
        if (!autoCommit && !database.inTransaction) {
            database.beginImmediateTransaction()
        }
    }

    internal fun addWarning(warning: SQLWarning) {
        warnings.add(warning)
    }
}
