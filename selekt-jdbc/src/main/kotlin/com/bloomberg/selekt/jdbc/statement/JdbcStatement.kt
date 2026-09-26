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

import com.bloomberg.selekt.CancellationSignal
import com.bloomberg.selekt.ICursor
import com.bloomberg.selekt.ISQLStatement
import com.bloomberg.selekt.OperationCancelledException
import com.bloomberg.selekt.ParameterRow
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.result.GeneratedKeysResultSet
import com.bloomberg.selekt.jdbc.result.JdbcResultSet
import com.bloomberg.selekt.jdbc.result.RowLimitedCursor
import java.sql.BatchUpdateException
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.SQLWarning
import java.sql.Statement
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.annotation.concurrent.NotThreadSafe

private val emptyIntArray = IntArray(0)

internal fun isInsertSql(sql: String): Boolean = sql.trimStart().run {
    startsWith("INSERT", ignoreCase = true) || startsWith("REPLACE", ignoreCase = true)
}

internal open class JdbcStatementState(
    internal val resultSetType: Int,
    internal val resultSetConcurrency: Int,
    internal val resultSetHoldability: Int
) {
    internal var dependentResultSets: MutableList<ResultSet>? = null
    internal var closingDependentResultSets = false
    internal var updateCount = -1
    internal var lastGeneratedKey = -1L
    internal var fetchSize = 0
    internal var maxRows = 0
    @Volatile
    internal var queryTimeout = 0
    @Volatile
    internal var currentSignal: CancellationSignal? = null
    @Volatile
    internal var currentWatchdog: ScheduledFuture<*>? = null
    internal var maxFieldSize = 0
    internal var poolable = false
    internal var closeOnCompletion = false
    internal var batchedSqlStatements: MutableList<String>? = null
    internal var escapeProcessing = true

    internal open fun reset() {
        dependentResultSets?.clear()
        closingDependentResultSets = false
        updateCount = -1
        lastGeneratedKey = -1L
        fetchSize = 0
        maxRows = 0
        queryTimeout = 0
        currentSignal = null
        currentWatchdog = null
        maxFieldSize = 0
        poolable = false
        closeOnCompletion = false
        batchedSqlStatements?.clear()
        escapeProcessing = true
    }
}

/**
 * @since 0.28.0
 */
@NotThreadSafe
@Suppress("TooGenericExceptionCaught")
open class JdbcStatement internal constructor(
    internal val connection: JdbcConnection,
    protected val database: SQLDatabase,
    resultSetType: Int = ResultSet.TYPE_FORWARD_ONLY,
    resultSetConcurrency: Int = ResultSet.CONCUR_READ_ONLY,
    resultSetHoldability: Int = ResultSet.CLOSE_CURSORS_AT_COMMIT,
    private val statementState: JdbcStatementState = JdbcStatementState(
        resultSetType,
        resultSetConcurrency,
        resultSetHoldability
    )
) : Statement {
    companion object {
        private val CLOSED: VarHandle = MethodHandles.lookup()
            .findVarHandle(JdbcStatement::class.java, "closed", Boolean::class.javaPrimitiveType)

        private val TIMEOUT_SCHEDULER: ScheduledExecutorService = Executors.newScheduledThreadPool(
            1,
            object : ThreadFactory {
                private val counter = AtomicInteger(0)
                override fun newThread(r: Runnable): Thread = Thread(
                    r,
                    "selekt-jdbc-timeout-${counter.incrementAndGet()}"
                ).apply { isDaemon = true }
            }
        )
    }

    @Volatile
    private var closed = false
    private var currentResultSet: ResultSet? = null
    protected var lastGeneratedKey: Long
        get() = statementState.lastGeneratedKey
        set(value) {
            statementState.lastGeneratedKey = value
        }

    val escapeProcessing: Boolean
        get() {
            checkClosed()
            return statementState.escapeProcessing
        }

    protected fun sharedStatementState(): Any = statementState

    override fun executeQuery(sql: String): ResultSet {
        checkClosed()
        try {
            closeCurrentResultSet()
            val signal = activateCancellationSignal()
            val cursor = runCatching {
                queryWithMaxRows(sql, emptyArray(), signal)
            }.getOrElse {
                deactivateCancellationSignal()
                throw it.translateCancellation()
            }
            return trackResultSet(
                JdbcResultSet(
                    cursor,
                    this,
                    statementState.resultSetType,
                    statementState.resultSetConcurrency,
                    statementState.resultSetHoldability
                )
            )
        } catch (e: Exception) {
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun executeUpdate(sql: String): Int {
        checkClosed()
        connection.checkWritable()
        return try {
            closeCurrentResultSet()
            val signal = activateCancellationSignal()
            try {
                withCancellation(signal, primary = true) {
                    database.compileStatement(sql).use { executeUpdate(sql, it) }
                }
            } catch (e: OperationCancelledException) {
                throw SQLExceptionMapper.mapCancellation(e)
            } finally {
                deactivateCancellationSignal()
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(SQLException(e.message, e))
        }
    }

    override fun execute(sql: String): Boolean {
        checkClosed()
        closeCurrentResultSet()
        val signal = activateCancellationSignal()
        var signalHandedOff = false
        try {
            val isReadOnly = runCatching {
                connection.withSession {
                    database.compileStatement(sql).use { it.isReadOnly }
                }
            }.getOrElse { e ->
                throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
            }
            return if (isReadOnly) {
                executeQueryInternal(sql, signal)
                signalHandedOff = true
                true
            } else {
                connection.checkWritable()
                try {
                    withCancellation(signal, primary = true) {
                        database.compileStatement(sql).use { executeUpdate(sql, it) }
                    }
                } catch (e: OperationCancelledException) {
                    throw SQLExceptionMapper.mapCancellation(e)
                }
                false
            }
        } finally {
            if (!signalHandedOff) {
                deactivateCancellationSignal()
            }
        }
    }

    private fun executeQueryInternal(sql: String, signal: CancellationSignal) {
        val cursor = queryWithMaxRows(sql, emptyArray(), signal)
        trackResultSet(
            JdbcResultSet(
                cursor,
                this,
                statementState.resultSetType,
                statementState.resultSetConcurrency,
                statementState.resultSetHoldability
            )
        )
    }

    protected fun <T : ResultSet> trackResultSet(resultSet: T): T {
        currentResultSet = resultSet
        dependentResultSets().add(resultSet)
        statementState.updateCount = -1
        return resultSet
    }

    protected fun queryWithSignal(
        sql: String,
        args: Array<Any?>,
        signal: CancellationSignal
    ): ICursor = connection.withSession {
        checkReadOnlyQuery(sql, args)
        // A read-only manual transaction deliberately materialises its result. Keeping a streaming
        // SQLite statement open there would pin a WAL snapshot beyond this call and can block a
        // FULL checkpoint. Auto-commit read-only queries have no such transaction-lifetime contract.
        val shouldStream = statementState.resultSetType == ResultSet.TYPE_FORWARD_ONLY &&
            (!connection.isReadOnly || connection.autoCommit)
        if (shouldStream) {
            database.queryForwardOnly(sql, args, signal)
        } else if (statementState.maxRows > 0) {
            database.queryUpTo(sql, args, statementState.maxRows, signal)
        } else {
            database.query(sql, args, signal)
        }
    }

    protected fun queryWithSignal(
        sql: String,
        args: ParameterRow,
        signal: CancellationSignal,
        isReadOnly: Boolean
    ): ICursor = connection.withSession {
        if (connection.isReadOnly && !isReadOnly) {
            connection.checkWritable()
        }
        val shouldStream = statementState.resultSetType == ResultSet.TYPE_FORWARD_ONLY &&
            (!connection.isReadOnly || connection.autoCommit)
        if (shouldStream) {
            database.queryForwardOnly(sql, args, signal)
        } else if (statementState.maxRows > 0) {
            database.queryUpTo(sql, args, statementState.maxRows, signal)
        } else {
            database.query(sql, args, signal)
        }
    }

    private fun checkReadOnlyQuery(sql: String, args: Array<Any?>) {
        if (connection.isReadOnly) {
            database.compileStatement(sql, args).use { statement ->
                if (!statement.isReadOnly) {
                    connection.checkWritable()
                }
            }
        }
    }

    internal fun <T> withCancellation(
        signal: CancellationSignal,
        primary: Boolean,
        block: SQLDatabase.() -> T
    ): T = connection.withSession {
        database.withCancellationSignal(signal, primary = primary, block = block)
    }

    private fun executeUpdate(sql: String, statement: ISQLStatement): Int {
        checkClosed()
        return try {
            connection.ensureTransaction()
            statement.run {
                if (isReadOnly) {
                    lastGeneratedKey = -1L
                    statementState.updateCount = 0
                } else if (isInsertSql(sql)) {
                    lastGeneratedKey = executeInsert()
                    statementState.updateCount = 1
                } else {
                    lastGeneratedKey = -1L
                    statementState.updateCount = executeUpdateDelete()
                }
            }
            currentResultSet = null
            statementState.updateCount
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(SQLException(e.message, e))
        }
    }

    override fun close() {
        closeOnce()
    }

    protected fun closeOnce(): Boolean {
        if (!CLOSED.compareAndSet(this, false, true)) {
            return false
        }
        deactivateCancellationSignal()
        closeDependentResultSets()
        return true
    }

    override fun isClosed(): Boolean = closed

    override fun getResultSet(): ResultSet? = currentResultSet

    override fun getUpdateCount(): Int {
        checkClosed()
        return statementState.updateCount
    }

    override fun getMoreResults(): Boolean {
        checkClosed()
        currentResultSet?.close()
        return false
    }

    override fun getMoreResults(current: Int): Boolean = getMoreResults()

    override fun getConnection(): Connection = connection

    override fun getWarnings(): SQLWarning? = null

    override fun clearWarnings() = Unit

    override fun setCursorName(name: String?) = throw SQLFeatureNotSupportedException("Named cursors not supported")

    override fun setEscapeProcessing(enable: Boolean) {
        checkClosed()
        statementState.escapeProcessing = enable
    }

    override fun setQueryTimeout(seconds: Int) {
        checkClosed()
        if (seconds < 0) {
            throw SQLException("Query timeout must be non-negative")
        }
        statementState.queryTimeout = seconds
    }

    override fun getQueryTimeout(): Int {
        checkClosed()
        return statementState.queryTimeout
    }

    override fun cancel() {
        checkClosed()
        statementState.currentSignal?.cancel()
    }

    internal fun activateCancellationSignal(): CancellationSignal {
        deactivateCancellationSignal()
        val signal = CancellationSignal()
        statementState.currentSignal = signal
        val seconds = statementState.queryTimeout
        if (seconds > 0) {
            statementState.currentWatchdog = TIMEOUT_SCHEDULER.schedule(
                signal::cancel,
                seconds.toLong(),
                TimeUnit.SECONDS
            )
        }
        return signal
    }

    internal fun deactivateCancellationSignal() {
        statementState.currentWatchdog?.cancel(false)
        statementState.currentWatchdog = null
        statementState.currentSignal = null
    }

    private fun Throwable.translateCancellation(): Throwable = when {
        this is OperationCancelledException -> SQLExceptionMapper.mapCancellation(this)
        cause is OperationCancelledException -> SQLExceptionMapper.mapCancellation(cause!!, message ?: "Query was cancelled")
        else -> this
    }

    override fun setFetchDirection(direction: Int) {
        if (direction != ResultSet.FETCH_FORWARD) {
            throw SQLFeatureNotSupportedException("Only FETCH_FORWARD is supported")
        }
    }

    override fun getFetchDirection(): Int = ResultSet.FETCH_FORWARD

    override fun setFetchSize(rows: Int) {
        checkClosed()
        if (rows < 0) {
            throw SQLException("Fetch size must be non-negative")
        }
        statementState.fetchSize = rows
    }

    override fun getFetchSize(): Int {
        checkClosed()
        return statementState.fetchSize
    }

    override fun setMaxRows(max: Int) {
        checkClosed()
        if (max < 0) {
            throw SQLException("Max rows must be non-negative")
        }
        statementState.maxRows = max
    }

    override fun getMaxRows(): Int {
        checkClosed()
        return statementState.maxRows
    }

    override fun setMaxFieldSize(max: Int) {
        if (max < 0) {
            throw SQLException("Max field size must be non-negative")
        }
        checkClosed()
        statementState.maxFieldSize = max
    }

    override fun getMaxFieldSize(): Int {
        checkClosed()
        return statementState.maxFieldSize
    }

    override fun getResultSetConcurrency(): Int = statementState.resultSetConcurrency

    override fun getResultSetType(): Int = statementState.resultSetType

    override fun getResultSetHoldability(): Int = statementState.resultSetHoldability

    override fun addBatch(sql: String) {
        checkClosed()
        if (sql.isBlank()) {
            throw SQLException("SQL statement cannot be empty")
        }
        val statements = statementState.batchedSqlStatements ?: mutableListOf<String>().also {
            statementState.batchedSqlStatements = it
        }
        statements.add(sql)
    }

    override fun clearBatch() {
        checkClosed()
        statementState.batchedSqlStatements?.clear()
    }

    override fun executeBatch(): IntArray {
        checkClosed()
        closeCurrentResultSet()
        return if (statementState.batchedSqlStatements.isNullOrEmpty()) {
            emptyIntArray
        } else {
            val signal = activateCancellationSignal()
            try {
                withCancellation(signal, primary = true) {
                    connection.ensureTransaction()
                    executeBatchStatements()
                }
            } catch (e: OperationCancelledException) {
                throw SQLExceptionMapper.mapCancellation(e)
            } finally {
                deactivateCancellationSignal()
                clearBatch()
            }
        }
    }

    private fun executeBatchStatements(): IntArray {
        val statements = checkNotNull(statementState.batchedSqlStatements)
        val results = mutableListOf<Int>()
        for (sql in statements) {
            runCatching {
                validateBatchSql(sql)
                connection.checkWritable()
                val count = database.compileStatement(sql).use {
                    if (isInsertSql(sql)) {
                        lastGeneratedKey = it.executeInsert()
                        1
                    } else {
                        lastGeneratedKey = -1L
                        it.executeUpdateDelete()
                    }
                }
                results.add(count)
            }.onFailure { e ->
                if (e is OperationCancelledException) {
                    throw e
                }
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
        database.compileStatement(sql).use {
            if (it.isReadOnly) {
                throw SQLException("Read-only statements are not allowed in batch execution")
            }
        }
    }

    override fun setPoolable(poolable: Boolean) {
        checkClosed()
        statementState.poolable = poolable
    }

    override fun isPoolable(): Boolean {
        checkClosed()
        return statementState.poolable
    }

    override fun closeOnCompletion() {
        checkClosed()
        statementState.closeOnCompletion = true
    }

    override fun isCloseOnCompletion(): Boolean {
        checkClosed()
        return statementState.closeOnCompletion
    }

    override fun executeUpdate(sql: String, autoGeneratedKeys: Int): Int = executeUpdate(sql)

    override fun executeUpdate(sql: String, columnIndexes: IntArray): Int = executeUpdate(sql)

    override fun executeUpdate(sql: String, columnNames: Array<out String>): Int = executeUpdate(sql)

    override fun execute(sql: String, autoGeneratedKeys: Int): Boolean = execute(sql)

    override fun execute(sql: String, columnIndexes: IntArray): Boolean = execute(sql)

    override fun execute(sql: String, columnNames: Array<out String>): Boolean = execute(sql)

    override fun getGeneratedKeys(): ResultSet {
        checkClosed()
        return GeneratedKeysResultSet(lastGeneratedKey, this).also(dependentResultSets()::add)
    }

    override fun <T> unwrap(iface: Class<T>): T = if (iface.isAssignableFrom(this::class.java)) {
        @Suppress("UNCHECKED_CAST")
        this as T
    } else {
        throw SQLException("Cannot unwrap to ${iface.name}")
    }

    override fun isWrapperFor(iface: Class<*>): Boolean = iface.isAssignableFrom(this::class.java)

    protected fun queryWithMaxRows(
        sql: String,
        args: Array<Any?>,
        signal: CancellationSignal
    ): ICursor {
        val cursor = queryWithSignal(sql, args, signal)
        return if (statementState.maxRows > 0) {
            RowLimitedCursor(cursor, statementState.maxRows)
        } else {
            cursor
        }
    }

    protected fun queryWithMaxRows(
        sql: String,
        args: ParameterRow,
        signal: CancellationSignal,
        isReadOnly: Boolean
    ): ICursor {
        val cursor = queryWithSignal(sql, args, signal, isReadOnly)
        return if (statementState.maxRows > 0) {
            RowLimitedCursor(cursor, statementState.maxRows)
        } else {
            cursor
        }
    }

    protected fun checkClosed() {
        if (closed) {
            throw SQLException("Statement is closed")
        }
    }

    protected fun closeCurrentResultSet() {
        val resultSet = currentResultSet ?: return
        resultSet.close()
        checkClosed()
    }

    internal fun hasOpenResultSet(): Boolean = currentResultSet?.isClosed == false

    internal fun onResultSetClosed(resultSet: ResultSet, exhausted: Boolean = false) {
        if (!isClosed) {
            val wasCurrent = currentResultSet === resultSet
            if (wasCurrent) {
                if (!exhausted) {
                    currentResultSet = null
                }
                deactivateCancellationSignal()
            }
            val resultSets = statementState.dependentResultSets
            val shouldClose = !exhausted &&
                resultSets?.remove(resultSet) == true &&
                resultSets.isEmpty() &&
                statementState.closeOnCompletion &&
                !statementState.closingDependentResultSets &&
                !isClosed
            if (shouldClose) {
                close()
            }
        }
    }

    protected fun closeDependentResultSets() {
        statementState.closingDependentResultSets = true
        try {
            statementState.dependentResultSets?.toList()?.forEach { resultSet ->
                resultSet.close()
            }
        } finally {
            statementState.dependentResultSets?.clear()
            currentResultSet = null
            statementState.closingDependentResultSets = false
        }
    }

    private fun dependentResultSets(): MutableList<ResultSet> =
        statementState.dependentResultSets ?: mutableListOf<ResultSet>().also {
            statementState.dependentResultSets = it
        }
}
