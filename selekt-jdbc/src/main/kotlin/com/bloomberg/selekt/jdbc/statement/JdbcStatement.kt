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

import com.bloomberg.selekt.ISQLStatement
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.result.GeneratedKeysResultSet
import com.bloomberg.selekt.jdbc.result.JdbcResultSet
import java.sql.BatchUpdateException
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.SQLWarning
import java.sql.Statement
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import javax.annotation.concurrent.NotThreadSafe

private val emptyIntArray = IntArray(0)

internal fun isInsertSql(sql: String): Boolean = sql.trimStart().run {
    startsWith("INSERT", ignoreCase = true) || startsWith("REPLACE", ignoreCase = true)
}

@NotThreadSafe
@Suppress("TooGenericExceptionCaught")
open class JdbcStatement internal constructor(
    internal val connection: JdbcConnection,
    private val database: SQLDatabase,
    private val resultSetType: Int = ResultSet.TYPE_FORWARD_ONLY,
    private val resultSetConcurrency: Int = ResultSet.CONCUR_READ_ONLY,
    private val resultSetHoldability: Int = ResultSet.CLOSE_CURSORS_AT_COMMIT
) : Statement {
    companion object {
        private val CLOSED: VarHandle = MethodHandles.lookup()
            .findVarHandle(JdbcStatement::class.java, "closed", Boolean::class.javaPrimitiveType)
    }

    @Volatile
    private var closed = false
    private var currentResultSet: ResultSet? = null
    private var updateCount = -1
    protected var lastGeneratedKey = -1L
    private var fetchSize = 0
    private var maxRows = 0
    private var queryTimeout = 0
    private var maxFieldSize = 0
    private var poolable = false
    private var closeOnCompletion = false
    private val batchedSqlStatements = mutableListOf<String>()

    var escapeProcessing: Boolean = true
        private set

    override fun executeQuery(sql: String): ResultSet {
        checkClosed()
        try {
            connection.ensureTransaction()
            val cursor = database.query(sql, emptyArray())
            currentResultSet = JdbcResultSet(cursor, this, resultSetType, resultSetConcurrency, resultSetHoldability)
            updateCount = -1
            return currentResultSet!!
        } catch (e: Exception) {
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun executeUpdate(sql: String): Int {
        checkClosed()
        return runCatching {
            executeUpdate(sql, database.compileStatement(sql))
        }.getOrElse { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun execute(sql: String): Boolean {
        checkClosed()
        val statement = runCatching {
            connection.ensureTransaction()
            database.compileStatement(sql)
        }.getOrElse { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
        return if (statement.isReadOnly) {
            executeQuery(sql)
            true
        } else {
            executeUpdate(sql, statement)
            false
        }
    }

    private fun executeUpdate(sql: String, statement: ISQLStatement): Int {
        checkClosed()
        return runCatching {
            connection.ensureTransaction()
            statement.run {
                if (isReadOnly) {
                    lastGeneratedKey = -1L
                    updateCount = 0
                } else if (isInsertSql(sql)) {
                    lastGeneratedKey = executeInsert()
                    updateCount = 1
                } else {
                    lastGeneratedKey = -1L
                    updateCount = executeUpdateDelete()
                }
            }
            currentResultSet = null
            updateCount
        }.getOrElse { e ->
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun close() {
        if (CLOSED.compareAndSet(this, false, true)) {
            currentResultSet?.close()
            currentResultSet = null
        }
    }

    override fun isClosed(): Boolean = closed

    override fun getResultSet(): ResultSet? = currentResultSet

    override fun getUpdateCount(): Int = updateCount

    override fun getMoreResults(): Boolean {
        currentResultSet?.close()
        currentResultSet = null
        return false
    }

    override fun getMoreResults(current: Int): Boolean = getMoreResults()

    override fun getConnection(): Connection = connection

    override fun getWarnings(): SQLWarning? = null

    override fun clearWarnings() = Unit

    override fun setCursorName(name: String?) = throw SQLFeatureNotSupportedException("Named cursors not supported")

    override fun setEscapeProcessing(enable: Boolean) {
        escapeProcessing = enable
    }

    override fun setQueryTimeout(seconds: Int) {
        if (seconds < 0) {
            throw SQLException("Query timeout must be non-negative")
        }
        queryTimeout = seconds
    }

    override fun getQueryTimeout(): Int = queryTimeout

    override fun cancel() = Unit

    override fun setFetchDirection(direction: Int) {
        if (direction != ResultSet.FETCH_FORWARD) {
            throw SQLFeatureNotSupportedException("Only FETCH_FORWARD is supported")
        }
    }

    override fun getFetchDirection(): Int = ResultSet.FETCH_FORWARD

    override fun setFetchSize(rows: Int) {
        if (rows < 0) {
            throw SQLException("Fetch size must be non-negative")
        }
        fetchSize = rows
    }

    override fun getFetchSize(): Int = fetchSize

    override fun setMaxRows(max: Int) {
        if (max < 0) {
            throw SQLException("Max rows must be non-negative")
        }
        maxRows = max
    }

    override fun getMaxRows(): Int = maxRows

    override fun setMaxFieldSize(max: Int) {
        if (max < 0) {
            throw SQLException("Max field size must be non-negative")
        }
        checkClosed()
        maxFieldSize = max
    }

    override fun getMaxFieldSize(): Int = maxFieldSize

    override fun getResultSetConcurrency(): Int = resultSetConcurrency

    override fun getResultSetType(): Int = resultSetType

    override fun getResultSetHoldability(): Int = resultSetHoldability

    override fun addBatch(sql: String) {
        checkClosed()
        if (sql.isBlank()) {
            throw SQLException("SQL statement cannot be empty")
        }
        batchedSqlStatements.add(sql)
    }

    override fun clearBatch() {
        checkClosed()
        batchedSqlStatements.clear()
    }

    override fun executeBatch(): IntArray {
        checkClosed()
        return if (batchedSqlStatements.isEmpty()) {
            emptyIntArray
        } else {
            try {
                executeBatchStatements()
            } finally {
                clearBatch()
            }
        }
    }

    private fun executeBatchStatements(): IntArray {
        val results = mutableListOf<Int>()
        for (sql in batchedSqlStatements) {
            runCatching {
                validateBatchSql(sql)
                val updateCount = executeUpdate(sql)
                results.add(updateCount)
            }.onFailure { e ->
                (e as? SQLException ?: SQLException(e.message, e)).run {
                    throw BatchUpdateException(
                        message ?: "Batch execution failed",
                        sqlState,
                        errorCode,
                        results.toIntArray(),
                        this
                    )
                }
            }
        }
        return results.toIntArray()
    }

    private fun validateBatchSql(sql: String) {
        if (database.compileStatement(sql).isReadOnly) {
            throw SQLException("Read-only statements are not allowed in batch execution")
        }
    }

    override fun setPoolable(poolable: Boolean) {
        checkClosed()
        this.poolable = poolable
    }

    override fun isPoolable(): Boolean = poolable

    override fun closeOnCompletion() {
        checkClosed()
        closeOnCompletion = true
    }

    override fun isCloseOnCompletion(): Boolean = closeOnCompletion

    override fun executeUpdate(sql: String, autoGeneratedKeys: Int): Int = executeUpdate(sql)

    override fun executeUpdate(sql: String, columnIndexes: IntArray): Int = executeUpdate(sql)

    override fun executeUpdate(sql: String, columnNames: Array<out String>): Int = executeUpdate(sql)

    override fun execute(sql: String, autoGeneratedKeys: Int): Boolean = execute(sql)

    override fun execute(sql: String, columnIndexes: IntArray): Boolean = execute(sql)

    override fun execute(sql: String, columnNames: Array<out String>): Boolean = execute(sql)

    override fun getGeneratedKeys(): ResultSet {
        checkClosed()
        return GeneratedKeysResultSet(lastGeneratedKey, this)
    }

    override fun <T> unwrap(iface: Class<T>): T = if (iface.isAssignableFrom(this::class.java)) {
        @Suppress("UNCHECKED_CAST")
        this as T
    } else if (iface.isAssignableFrom(SQLDatabase::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return database as T
    } else {
        throw SQLException("Cannot unwrap to ${iface.name}")
    }

    override fun isWrapperFor(iface: Class<*>): Boolean = iface.isAssignableFrom(this::class.java) ||
        iface.isAssignableFrom(SQLDatabase::class.java)

    protected fun checkClosed() {
        if (closed) {
            throw SQLException("Statement is closed")
        }
    }
}
