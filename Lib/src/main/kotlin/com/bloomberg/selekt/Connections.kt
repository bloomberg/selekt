/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt

import com.bloomberg.selekt.commons.forEachByIndex
import com.bloomberg.selekt.commons.forUntil
import javax.annotation.Generated
import javax.annotation.concurrent.NotThreadSafe

@NotThreadSafe
internal class SQLConnection constructor(
    path: String,
    internal val sqlite: SQLite,
    private val configuration: DatabaseConfiguration,
    flags: Int,
    private val random: IRandom,
    key: Key?
) : CloseableSQLExecutor {
    private val pointer = sqlite.open(path, flags)
    private val preparedStatements = LruCache<SQLPreparedStatement>(configuration.maxSqlCacheSize) { it.close() }

    override val isAutoCommit: Boolean
        get() = sqlite.getAutocommit(pointer) != 0

    override val isReadOnly = SQL_OPEN_READONLY and flags != 0

    override val isPrimary = !isReadOnly

    // Guarded by and exclusively used by the pool.
    override var tag = false

    init {
        runCatching {
            key?.let { sqlite.exec(pointer, "PRAGMA key=\"${it()}\"") }
            sqlite.extendedResultCodes(pointer, 0)
            configuration.trace?.let { sqlite.traceV2(pointer, it()) }
            sqlite.busyTimeout(pointer, configuration.busyTimeoutMillis)
            sqlite.exec(pointer, "PRAGMA secure_delete=${configuration.secureDelete.name}")
        }.exceptionOrNull()?.let {
            close()
            throw IllegalStateException(it)
        }
    }

    override fun checkpoint(name: String?, mode: SQLCheckpointMode) {
        sqlite.walCheckpointV2(pointer, name, mode())
    }

    override fun close() {
        try {
            optimiseQuietly()
            sqlite.closeV2(pointer)
        } finally {
            preparedStatements.evictAll()
        }
    }

    override fun execute(sql: String, bindArgs: Array<*>) = withPreparedStatement(sql, bindArgs) {
        step()
    }

    override fun execute(
        sql: String,
        statementType: SQLStatementType,
        bindArgs: Array<*>
    ) = execute(sql, bindArgs)

    override fun executeForBlob(
        name: String,
        table: String,
        column: String,
        row: Long
    ) = longArrayOf(0L).also {
        sqlite.blobOpen(pointer, name, table, column, row, if (isReadOnly) 0 else 1, it)
    }.first().let {
        SQLBlob(it, sqlite, isReadOnly)
    }

    override fun executeForChangedRowCount(sql: String, bindArgs: Array<*>) = withPreparedStatement(sql, bindArgs) {
        step()
        sqlite.changes(pointer)
    }

    override fun executeForChangedRowCount(
        sql: String,
        statementType: SQLStatementType,
        bindArgs: Array<*>
    ) = executeForChangedRowCount(sql, bindArgs)

    override fun executeForChangedRowCount(sql: String, bindArgs: Iterable<Array<*>>) = withPreparedStatement(sql) {
        bindArgs.sumBy {
            reset()
            bindArguments(it)
            step()
            sqlite.changes(pointer)
        }
    }

    override fun executeForCursorWindow(
        sql: String,
        bindArgs: Array<*>,
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
                        else -> put(columnString(it))
                    }
                }
            }
        }
    }

    override fun executeForLastInsertedRowId(sql: String, bindArgs: Array<*>) = withPreparedStatement(sql, bindArgs) {
        step()
        sqlite.lastInsertRowId(pointer)
    }

    override fun executeForLastInsertedRowId(
        sql: String,
        statementType: SQLStatementType,
        bindArgs: Array<*>
    ) = executeForLastInsertedRowId(sql, bindArgs)

    override fun executeForInt(sql: String, bindArgs: Array<*>) = withPreparedStatement(sql, bindArgs) {
        step()
        columnInt(0)
    }

    override fun executeForLong(sql: String, bindArgs: Array<*>) = withPreparedStatement(sql, bindArgs) {
        step()
        columnLong(0)
    }

    override fun executeForString(sql: String, bindArgs: Array<*>) = withPreparedStatement(sql, bindArgs) {
        step()
        columnString(0)
    }

    override fun executeWithRetry(sql: String) = withPreparedStatement(sql) {
        step(configuration.busyTimeoutMillis.toLong())
    }

    override fun executeWithRetry(sql: String, statementType: SQLStatementType) = executeWithRetry(sql)

    override fun matches(key: String) = preparedStatements.containsKey(key)

    override fun prepare(sql: String) = withPreparedStatement(sql) {
        SQLStatementInformation(isReadOnly, parameterCount, columnNames)
    }

    @Generated
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

    @Generated
    private inline fun <R> withPreparedStatement(
        sql: String,
        bindArgs: Array<*>,
        block: SQLPreparedStatement.() -> R
    ) = withPreparedStatement(sql) {
        bindArguments(bindArgs)
        block()
    }

    // TODO Handle re-entrant queries (via triggers!) where statement is going to be in use already!
    private fun acquirePreparedStatement(sql: String) = preparedStatements[
        sql, { SQLPreparedStatement(sqlite.prepare(pointer, sql), sql, sqlite, random) }
    ]

    private fun releasePreparedStatement(preparedStatement: SQLPreparedStatement) {
        if (runCatching { preparedStatement.resetAndClearBindings() }.isFailure) {
            preparedStatements.evict(preparedStatement.sql)
        }
    }

    private fun optimiseQuietly() {
        runCatching {
            // To achieve the best long-term query performance without the need to do a detailed engineering analysis of
            // the application schema and SQL, it is recommended that applications run "PRAGMA optimize" (with no arguments)
            // just before closing each database connection.
            // See: https://www.sqlite.org/pragma.html#pragma_optimize
            //
            // Applications with long-lived databases that use complex queries should consider running the following commands
            // just prior to closing each database connection:
            //     PRAGMA analysis_limit=400;
            //     PRAGMA optimize;
            // The optimize pragma is usually a no-op but it will occasionally run ANALYZE if it seems like doing so will be
            // useful to the query planner. The analysis_limit pragma limits the scope of any ANALYZE command that the
            // optimize pragma runs so that it does not consume too many CPU cycles. The constant "400" can be adjusted as
            // needed. Values between 100 and 1000 work well for most applications.
            // See: https://www.sqlite.org/lang_analyze.html
            if (isReadOnly) {
                sqlite.exec(pointer, "PRAGMA analysis_limit=100")
            }
            sqlite.exec(pointer, "PRAGMA optimize")
        }
    }
}

private fun SQLite.open(path: String, flags: Int) = LongArray(1).apply {
    openV2(path, flags, this)
}.first().also {
    check(it != NULL)
}

private fun SQLite.prepare(db: Long, sql: String) = LongArray(1).apply {
    prepareV2(db, sql, this)
}.first().also {
    check(it != NULL)
}

private fun SQLPreparedStatement.bindArguments(args: Array<*>) {
    require(parameterCount == args.size) {
        "Expected $parameterCount bind arguments but ${args.size} were provided."
    }
    args.forEachByIndex { i, arg ->
        bindArgument(i + 1, arg)
    }
}

private fun SQLPreparedStatement.bindArgument(position: Int, arg: Any?) {
    when (arg) {
        is String -> bind(position, arg)
        is Int -> bind(position, arg)
        is Long -> bind(position, arg)
        null -> bindNull(position)
        is Double -> bind(position, arg)
        is Float -> bind(position, arg.toDouble())
        is Short -> bind(position, arg.toInt())
        is Byte -> bind(position, arg.toInt())
        is ByteArray -> bind(position, arg)
        is ZeroBlob -> bindZeroBlob(position, arg.size)
        else -> bind(position, arg.toString())
    }
}

private fun SQLPreparedStatement.resetAndClearBindings() {
    reset()
    clearBindings()
}
