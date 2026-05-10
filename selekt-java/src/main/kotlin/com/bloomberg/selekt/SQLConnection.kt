/*
 * Copyright 2020 Bloomberg Finance L.P.
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

package com.bloomberg.selekt

import com.bloomberg.selekt.cache.LruCache
import com.bloomberg.selekt.commons.forEachByIndexUntil
import com.bloomberg.selekt.commons.forEachByPositionUntil
import com.bloomberg.selekt.commons.forUntil
import javax.annotation.concurrent.NotThreadSafe

/**
 * @since 0.12.1
 */
@NotThreadSafe
internal class SQLConnection(
    path: String,
    private val sqlite: SQLite,
    private val configuration: DatabaseConfiguration,
    flags: Int,
    private val random: IRandom,
    key: Key?
) : CloseableSQLExecutor {
    private val databaseHandle = sqlite.open(path, flags)
    private val preparedStatements = LruCache<SQLPreparedStatement>(configuration.maxSqlCacheSize) {
        it.close()
    }
    private var commitListener: SQLTransactionListener? = null

    override val isAutoCommit: Boolean
        get() = sqlite.getAutocommit(databaseHandle) != 0

    override val isReadOnly = SQL_OPEN_READONLY and flags != 0

    override val isPrimary = !isReadOnly

    // Guarded by and exclusively used by the pool.
    override var tag = false

    private val nativeCommitListener = object : SQLCommitListener {
        override fun onCommit(): Int {
            commitListener?.onCommit()
            return 0
        }

        override fun onRollback() {
            commitListener?.onRollback()
        }
    }

    init {
        runCatching {
            key?.use { sqlite.keyConventionally(databaseHandle, it) }
            sqlite.extendedResultCodes(databaseHandle, 0)
            configuration.trace?.let { sqlite.traceV2(databaseHandle, it()) }
            sqlite.busyTimeout(databaseHandle, configuration.busyTimeoutMillis)
            sqlite.exec(databaseHandle, "PRAGMA secure_delete=${configuration.secureDelete.name}")
        }.exceptionOrNull()?.let {
            close()
            throw it
        }
    }

    override fun checkpoint(name: String?, mode: SQLCheckpointMode) {
        sqlite.walCheckpointV2(databaseHandle, name, mode())
    }

    override fun databaseConfig(op: Int, value: Int) {
        sqlite.databaseConfig(databaseHandle, op, value)
    }

    override fun close() {
        try {
            optimiseQuietly()
            sqlite.progressHandler(databaseHandle, 0, null)
            sqlite.commitHook(databaseHandle, false, null)
            sqlite.closeV2(databaseHandle)
        } finally {
            preparedStatements.evictAll()
        }
    }

    override fun execute(
        sql: String,
        bindArgs: Array<out Any?>
    ) = withPreparedStatement(sql, bindArgs, SQLPreparedStatement::step)

    override fun executeForBlob(
        name: String,
        table: String,
        column: String,
        row: Long
    ) = longArrayOf(0L).also {
        sqlite.blobOpen(databaseHandle, name, table, column, row, if (isReadOnly) { 0 } else { 1 }, it)
    }.first().let {
        SQLBlob(sqlite.newBlobHandle(it), sqlite, isReadOnly)
    }

    override fun executeForChangedRowCount(
        sql: String,
        bindArgs: Array<out Any?>
    ) = withPreparedStatement(sql, bindArgs) {
        if (SQL_DONE == step()) {
            sqlite.changes(databaseHandle)
        } else {
            -1
        }
    }

    override fun executeBatchForChangedRowCount(
        sql: String,
        bindArgs: Iterable<Array<out Any?>>
    ) = withPreparedStatement(sql) {
        sqlite.withScopedArena {
            val changes = sqlite.totalChanges(databaseHandle)
            bindArgs.forEach {
                reset()
                bindRow(it)
                if (SQL_DONE != step()) {
                    return@withScopedArena -1
                }
            }
            sqlite.totalChanges(databaseHandle) - changes
        }
    }

    override fun executeBatchForChangedRowCount(
        sql: String,
        bindArgs: Sequence<Array<out Any?>>
    ) = executeBatchForChangedRowCount(sql, bindArgs.asIterable())

    @Suppress("DuplicatedCode")
    override fun executeBatchForChangedRowCountRows(
        sql: String,
        bindArgs: Iterable<ParameterRow>
    ) = withPreparedStatement(sql) {
        sqlite.withScopedArena {
            val changes = sqlite.totalChanges(databaseHandle)
            bindArgs.forEach {
                reset()
                bindRow(it)
                if (SQL_DONE != step()) {
                    return@withScopedArena -1
                }
            }
            sqlite.totalChanges(databaseHandle) - changes
        }
    }

    @Suppress("DuplicatedCode")
    override fun executeBatchForChangedRowCount(
        sql: String,
        bindArgs: List<Array<out Any?>>
    ) = withPreparedStatement(sql) {
        sqlite.withScopedArena {
            val changes = sqlite.totalChanges(databaseHandle)
            bindArgs.forEachByIndexUntil { _, args ->
                reset()
                bindRow(args)
                if (SQL_DONE != step()) {
                    return@withScopedArena -1
                }
            }
            sqlite.totalChanges(databaseHandle) - changes
        }
    }

    @Suppress("DuplicatedCode")
    override fun executeBatchForChangedRowCount(
        sql: String,
        bindArgs: Array<out Array<out Any?>>,
        fromIndex: Int,
        toIndex: Int
    ) = withPreparedStatement(sql) {
        sqlite.withScopedArena {
            val changes = sqlite.totalChanges(databaseHandle)
            bindArgs.forEachByIndexUntil { _, args ->
                reset()
                bindRow(args)
                if (SQL_DONE != step()) {
                    return@withScopedArena -1
                }
            }
            sqlite.totalChanges(databaseHandle) - changes
        }
    }

    override fun executeForCursorWindow(
        sql: String,
        bindArgs: Array<out Any?>,
        window: ICursorWindow
    ) = withPreparedStatement(sql, bindArgs) {
        window.run {
            clear()
            while (SQL_ROW == step()) {
                check(allocateRow()) { "Failed to allocate a window row." }
                0.forUntil(columnCount) {
                    when (columnType(it)) {
                        ColumnType.STRING.sqlDataType -> put(columnString(it))
                        ColumnType.INTEGER.sqlDataType -> put(columnLong(it))
                        ColumnType.FLOAT.sqlDataType -> put(columnDouble(it))
                        ColumnType.NULL.sqlDataType -> putNull()
                        ColumnType.BLOB.sqlDataType -> put(columnBlob(it))
                        else -> error("Unrecognised column type for column $it.")
                    }
                }
            }
        }
    }

    override fun executeForForwardCursor(
        sql: String,
        bindArgs: Array<out Any?>,
        additionalOnClose: (() -> Unit)?
    ): ForwardCursor {
        val statement = acquirePreparedStatement(sql)
        return runCatching {
            statement.bindArguments(bindArgs)
            ForwardCursor(statement) {
                try {
                    releasePreparedStatement(statement)
                } finally {
                    additionalOnClose?.invoke()
                }
            }
        }.getOrElse {
            releasePreparedStatement(statement)
            throw it
        }
    }

    override fun executeForLastInsertedRowId(sql: String, bindArgs: Array<out Any?>) = withPreparedStatement(sql, bindArgs) {
        if (SQL_DONE == step() && sqlite.changes(databaseHandle) > 0) {
            sqlite.lastInsertRowId(databaseHandle)
        } else {
            -1L
        }
    }

    override fun executeForInt(sql: String, bindArgs: Array<out Any?>) = withPreparedStatement(sql, bindArgs) {
        step()
        columnInt(0)
    }

    override fun executeForLong(sql: String, bindArgs: Array<out Any?>) = withPreparedStatement(sql, bindArgs) {
        step()
        columnLong(0)
    }

    override fun executeForString(sql: String, bindArgs: Array<out Any?>) = withPreparedStatement(sql, bindArgs) {
        step()
        columnString(0)
    }

    override fun executeWithRetry(sql: String) = withPreparedStatement(sql) {
        step(configuration.busyTimeoutMillis.toLong())
    }

    override fun interrupt() {
        sqlite.interrupt(databaseHandle)
    }

    override val isInterrupted: Boolean
        get() = sqlite.isInterrupted(databaseHandle)

    override fun setProgressHandler(instructionCount: Int, handler: SQLProgressHandler?) {
        sqlite.progressHandler(databaseHandle, instructionCount, handler)
    }

    override fun matches(key: String) = preparedStatements.containsKey(key)

    override fun prepare(sql: String) = withPreparedStatement(sql) {
        SQLStatementInformation(isReadOnly, parameterCount, columnNames)
    }

    override fun releaseMemory() {
        preparedStatements.evictAll()
        sqlite.databaseReleaseMemory(databaseHandle)
    }

    override fun setTransactionListener(listener: SQLTransactionListener?) {
        (listener != null).let { enabled ->
            sqlite.commitHook(databaseHandle, enabled, if (enabled) { nativeCommitListener } else { null })
        }
        commitListener = listener
    }

    private inline fun <R> withPreparedStatement(
        sql: String,
        block: SQLPreparedStatement.() -> R
    ) = acquirePreparedStatement(sql).run {
        try {
            block()
        } finally {
            releasePreparedStatement(this)
        }
    }

    private inline fun <R> withPreparedStatement(
        sql: String,
        bindArgs: Array<out Any?>,
        crossinline block: SQLPreparedStatement.() -> R
    ) = withPreparedStatement(sql) {
        sqlite.withScopedArena {
            bindArguments(bindArgs)
            block()
        }
    }

    private fun acquirePreparedStatement(sql: String) = preparedStatements[
        sql, {
            val statement = sqlite.prepare(databaseHandle, sql)
            SQLPreparedStatement(statement, sql, sqlite, random)
        }
    ]

    private fun releasePreparedStatement(preparedStatement: SQLPreparedStatement) {
        try {
            preparedStatement.resetAndClearBindings()
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            preparedStatements.evict(preparedStatement.sql)
        }
    }

    private fun optimiseQuietly() {
        runCatching {
            // To achieve the best long-term query performance without the need to do a detailed engineering analysis of
            // the application schema and SQL, it is recommended that applications run "PRAGMA optimize" (with no
            // arguments) just before closing each database connection.
            // See: https://www.sqlite.org/pragma.html#pragma_optimize
            //
            // Applications with long-lived databases that use complex queries should consider running the following
            // commands just prior to closing each database connection:
            //     PRAGMA analysis_limit=400;
            //     PRAGMA optimize;
            // The optimize pragma is usually a no-op but it will occasionally run ANALYZE if it seems like doing so will
            // be useful to the query planner. The analysis_limit pragma limits the scope of any ANALYZE command that the
            // optimize pragma runs so that it does not consume too many CPU cycles. The constant "400" can be adjusted as
            // needed. Values between 100 and 1000 work well for most applications.
            // See: https://www.sqlite.org/lang_analyze.html
            sqlite.exec(databaseHandle, "PRAGMA analysis_limit=100")
            sqlite.exec(databaseHandle, "PRAGMA optimize")
        }
    }
}

private fun SQLite.open(path: String, flags: Int) = LongArray(1).apply {
    openV2(path, flags, this)
}.first().also {
    check(it != NULL)
}.let(::newDatabaseHandle)

private fun SQLite.prepare(db: DatabaseHandle, sql: String) = LongArray(1).apply {
    prepareV2(db, sql, this)
}.first().also {
    check(it != NULL)
}.let(::newStatementHandle)

private fun SQLPreparedStatement.bindArguments(args: Array<out Any?>) {
    require(parameterCount == args.size) {
        "Expected $parameterCount bind arguments but ${args.size} were provided."
    }
    args.forEachByPositionUntil(parameterCount) { arg, i ->
        bindArgument(i, arg)
    }
}

private fun SQLPreparedStatement.bindArgument(
    position: Int,
    arg: Any?
) = SQLBindStrategy.Universal.bind(this, position, arg)
