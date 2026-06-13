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

/**
 * @since 0.28.0
 */
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

        private val limitPattern = Regex("""\bLIMIT\s+\d+""", RegexOption.IGNORE_CASE)

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
    private var updateCount = -1
    protected var lastGeneratedKey = -1L
    private var fetchSize = 0
    private var maxRows = 0
    @Volatile
    private var queryTimeout = 0
    @Volatile
    private var currentSignal: CancellationSignal? = null
    @Volatile
    private var currentWatchdog: ScheduledFuture<*>? = null
    private var maxFieldSize = 0
    private var poolable = false
    private var closeOnCompletion = false
    private val batchedSqlStatements = mutableListOf<String>()

    var escapeProcessing: Boolean = true
        private set

    override fun executeQuery(sql: String): ResultSet {
        checkClosed()
        try {
            closeCurrentResultSet()
            val signal = activateCancellationSignalOrNull()
            val cursor = runCatching {
                queryWithSignal(applyMaxRows(sql), emptyArray(), signal)
            }.getOrElse {
                deactivateCancellationSignal()
                throw it.translateCancellation()
            }
            return JdbcResultSet(cursor, this, resultSetType, resultSetConcurrency, resultSetHoldability).also {
                updateCount = -1
                currentResultSet = it
            }
        } catch (e: Exception) {
            throw SQLExceptionMapper.mapException(e as? SQLException ?: SQLException(e.message, e))
        }
    }

    override fun executeUpdate(sql: String): Int {
        checkClosed()
        connection.checkWritable()
        return try {
            closeCurrentResultSet()
            val signal = activateCancellationSignalOrNull()
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
        val signal = activateCancellationSignalOrNull()
        var signalHandedOff = false
        try {
            val isReadOnly = runCatching {
                database.compileStatement(sql).use { it.isReadOnly }
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

    private fun executeQueryInternal(sql: String, signal: CancellationSignal?) {
        val cursor = queryWithSignal(applyMaxRows(sql), emptyArray(), signal)
        currentResultSet = JdbcResultSet(cursor, this, resultSetType, resultSetConcurrency, resultSetHoldability)
        updateCount = -1
    }

    protected fun queryWithSignal(
        sql: String,
        args: Array<Any?>,
        signal: CancellationSignal?
    ): ICursor = if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && !connection.isReadOnly) {
        if (signal != null) {
            database.queryForwardOnly(sql, args, signal)
        } else {
            database.queryForwardOnly(sql, args)
        }
    } else {
        if (signal != null) {
            database.query(sql, args, signal)
        } else {
            database.query(sql, args)
        }
    }

    internal fun <T> withCancellation(
        signal: CancellationSignal?,
        primary: Boolean,
        block: SQLDatabase.() -> T
    ): T = if (signal != null) {
        database.withCancellationSignal(signal, primary = primary, block = block)
    } else {
        block(database)
    }

    private fun executeUpdate(sql: String, statement: ISQLStatement): Int {
        checkClosed()
        return try {
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
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(SQLException(e.message, e))
        }
    }

    override fun close() {
        if (CLOSED.compareAndSet(this, false, true)) {
            deactivateCancellationSignal()
            currentResultSet?.close()
            currentResultSet = null
        }
    }

    protected fun markClosed() {
        CLOSED.set(this, true)
    }

    protected fun markOpen() {
        CLOSED.set(this, false)
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

    override fun cancel() {
        currentSignal?.cancel()
    }

    internal fun activateCancellationSignalOrNull(): CancellationSignal? {
        deactivateCancellationSignal()
        val seconds = queryTimeout
        if (seconds <= 0) {
            return null
        }
        val signal = CancellationSignal()
        currentSignal = signal
        currentWatchdog = TIMEOUT_SCHEDULER.schedule(
            signal::cancel,
            seconds.toLong(),
            TimeUnit.SECONDS
        )
        return signal
    }

    internal fun deactivateCancellationSignal() {
        currentWatchdog?.cancel(false)
        currentWatchdog = null
        currentSignal = null
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
            val signal = activateCancellationSignalOrNull()
            try {
                withCancellation(signal, primary = true) {
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
        val results = mutableListOf<Int>()
        for (sql in batchedSqlStatements) {
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

    protected fun applyMaxRows(sql: String): String {
        if (maxRows <= 0) {
            return sql
        }
        val trimmed = sql.trimEnd().removeSuffix(";").trimEnd()
        return if (trimmed.contains(limitPattern)) {
            sql
        } else {
            "$trimmed LIMIT $maxRows"
        }
    }

    protected fun checkClosed() {
        if (closed) {
            throw SQLException("Statement is closed")
        }
    }

    protected fun closeCurrentResultSet() {
        currentResultSet?.close()
        currentResultSet = null
    }
}
