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

import com.bloomberg.selekt.commons.ManagedStringBuilder
import com.bloomberg.selekt.commons.forUntil
import com.bloomberg.selekt.exceptions.SelektSQLException
import com.bloomberg.selekt.pools.Priority
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.lang.StringBuilder
import java.util.stream.Stream
import javax.annotation.concurrent.ThreadSafe

private val allowedPragmaKeys = SQLitePragma.entries.map(SQLitePragma::key).toSet()

private fun requireSafePragmaKey(key: String) {
    val baseKey = key.substringBefore('(').substringBefore('=').substringAfterLast('.').lowercase()
    require(baseKey in allowedPragmaKeys) {
        "Unknown pragma key: '$baseKey'. Use SQLitePragma enum for type-safe access."
    }
}

private val safePragmaValuePattern = Regex("^[a-zA-Z0-9_.+-]+$")

private fun requireSafePragmaValue(value: Any) {
    value.toString().let {
        require(it.isNotEmpty() && safePragmaValuePattern.matches(it)) {
            "Invalid pragma value: '$it'. Must contain only alphanumeric characters, dots, underscores, plus, or minus."
        }
    }
}

private val EMPTY_ARRAY = emptyArray<Any?>()

private object SharedSqlBuilder {
    private val threadLocal = object : ThreadLocal<ManagedStringBuilder>() {
        override fun initialValue() = ManagedStringBuilder()
    }

    inline fun <T> use(block: StringBuilder.() -> T) = threadLocal.get().use { block(this) }
}

/**
 * The use of ThreadLocal underpins [SQLDatabase]'s thread-safety.
 *
 * A thread can acquire at most one connection session per database instance. Sessions hold at most one connection at any
 * given time, and exclusively. Sessions control all access to a database's connections and each session is thread-bound.
 * This prevents two threads from ever sharing a single connection.
 *
 * This is the same strategy Google employs in the Android SDK.
 *
 * @see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/database/sqlite/SQLiteDatabase.java">Android's SQLiteDatabase</a>
 */
@Suppress("Detekt.MethodOverloading", "Detekt.TooManyFunctions")
@ThreadSafe
class SQLDatabase(
    val path: String,
    private val sqlite: SQLite,
    configuration: DatabaseConfiguration,
    key: ByteArray?,
    random: IRandom = CommonThreadLocalRandom
) : IDatabase, SharedCloseable() {
    private val connectionFactory: SQLConnectionFactory
    private val connectionPool: SQLExecutorPool
    private val session: ThreadLocalSession

    init {
        val (pool, factory) = openConnectionPool(path, sqlite, configuration, random, key)
        connectionPool = pool
        connectionFactory = factory
        session = ThreadLocalSession(connectionPool, configuration.useNativeTransactionListeners)
    }

    override val inTransaction: Boolean
        get() = session().inTransaction

    val isCurrentThreadSessionActive: Boolean
        get() = session().hasObject

    fun batch(sql: String, bindArgs: Sequence<Array<out Any?>>): Int = transact {
        SQLStatement.execute(session, sql, bindArgs)
    }

    fun batch(sql: String, bindArgs: List<Array<out Any?>>): Int = transact {
        SQLStatement.execute(session, sql, bindArgs)
    }

    fun batch(
        sql: String,
        bindArgs: Array<out Array<out Any?>>,
        fromIndex: Int = 0,
        toIndex: Int = bindArgs.size
    ): Int = transact {
        SQLStatement.execute(session, sql, bindArgs, fromIndex, toIndex)
    }

    fun batch(sql: String, bindArgs: Iterable<Array<out Any?>>): Int = transact {
        SQLStatement.execute(session, sql, bindArgs)
    }

    fun batch(sql: String, bindArgs: Stream<Array<out Any?>>): Int = transact {
        SQLStatement.execute(session, sql, bindArgs)
    }

    override fun beginDeferredTransaction() = pledge { session().beginDeferredTransaction() }

    override fun beginExclusiveTransaction() = pledge { session().beginExclusiveTransaction() }

    override fun beginExclusiveTransactionWithListener(listener: SQLTransactionListener) = pledge {
        session().beginExclusiveTransactionWithListener(listener)
    }

    override fun beginImmediateTransaction() = pledge { session().beginImmediateTransaction() }

    override fun beginImmediateTransactionWithListener(listener: SQLTransactionListener) = pledge {
        session().beginImmediateTransactionWithListener(listener)
    }

    override fun compileStatement(sql: String, bindArgs: Array<out Any?>?) = compileStatement(
        sql,
        sql.resolvedSqlStatementType(),
        bindArgs
    )

    private fun compileStatement(
        sql: String,
        sqlStatementType: SQLStatementType,
        bindArgs: Array<out Any?>?
    ): ISQLStatement = pledge {
        SQLStatement.compile(session, sql, sqlStatementType, bindArgs)
    }

    override fun delete(
        table: String,
        whereClause: String,
        whereArgs: Array<out Any?>
    ) = pledge {
        SQLStatement.executeUpdateDelete(
            session,
            "DELETE FROM $table${if (whereClause.isNotEmpty()) " WHERE $whereClause" else ""}",
            whereArgs
        )
    }

    override fun endTransaction(): Unit = pledge { session().endTransaction() }

    fun databaseConfig(op: Int, value: Int): Unit = pledge {
        session().execute(false) { executor ->
            executor.databaseConfig(op, value)
        }
    }

    override fun exec(sql: String, bindArgs: Array<out Any?>?): Unit = pledge {
        compileStatement(
            sql,
            sql.resolvedSqlStatementType(),
            bindArgs
        ).execute()
    }

    fun <T> execute(readOnly: Boolean, block: SQLDatabase.() -> T) = pledge {
        session().execute(readOnly) { block() }
    }

    override fun insert(
        table: String,
        values: IContentValues,
        conflictAlgorithm: IConflictAlgorithm
    ) = pledge {
        require(!values.isEmpty) { "Empty initial values." }
        SharedSqlBuilder.use {
            append("INSERT")
                .append(conflictAlgorithm.sql)
                .append("INTO ")
                .append(table)
                .append('(')
            val iterator = values.entrySet.iterator()
            val bindArgs = Array(values.size) {
                if (it > 0) { append(',') }
                iterator.next().run {
                    append(key)
                    value
                }
            }
            append(") VALUES (?")
                .apply { repeat(bindArgs.size - 1) { append(",?") } }
                .append(')')
            SQLStatement.executeInsert(session, toString(), bindArgs)
        }
    }

    /**
     * Interrupts all database connections managed by this database. This causes any pending database operations on those
     * connections to abort and return at the earliest opportunity. This method is safe to call from any thread.
     *
     * @see <a href="https://www.sqlite.org/c3ref/interrupt.html">sqlite3_interrupt</a>
     */
    fun interrupt() {
        connectionFactory.interrupt()
    }

    /**
     * Returns true if any database connection managed by this database has been interrupted and has not yet completed.
     * This method is safe to call from any thread.
     *
     * @see <a href="https://www.sqlite.org/c3ref/interrupt.html">sqlite3_is_interrupted</a>
     */
    val isInterrupted: Boolean
        get() = connectionFactory.isInterrupted

    /**
     * Registers a progress handler that is invoked periodically during long-running SQL statements on all database
     * connections managed by this database. If the handler returns non-zero, the operation is interrupted.
     *
     * @param instructionCount approximate number of virtual machine instructions between invocations of the handler.
     * @param handler the progress handler, or null to clear the handler.
     * @see <a href="https://www.sqlite.org/c3ref/progress_handler.html">sqlite3_progress_handler</a>
     */
    fun setProgressHandler(instructionCount: Int, handler: SQLProgressHandler?) {
        connectionFactory.setProgressHandler(instructionCount, handler)
    }

    fun pragma(pragma: SQLitePragma) = pragma(pragma.key)

    fun pragma(pragma: SQLitePragma, value: Any) = pragma(pragma.key, value)

    fun pragma(key: String) = pledge {
        requireSafePragmaKey(key)
        checkNotNull(SQLStatement.executeForString(
            session,
            "PRAGMA $key",
            SQLStatementType.PRAGMA,
            EMPTY_ARRAY
        ))
    }

    fun pragma(key: String, value: Any) = pledge {
        requireSafePragmaKey(key)
        requireSafePragmaValue(value)
        SQLStatement.executeForString(session, "PRAGMA $key=$value", SQLStatementType.PRAGMA, EMPTY_ARRAY)
    }

    override fun query(
        distinct: Boolean,
        table: String,
        columns: Array<out String>,
        selection: String,
        selectionArgs: Array<out Any?>,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: Int?
    ) = SharedSqlBuilder.use {
        selectColumns(columns, distinct)
            .fromTable(table)
            .where(selection)
            .groupBy(groupBy)
            .having(having)
            .orderBy(orderBy)
            .limit(limit)
            .toString()
            .let { query(SQLQuery.create(session, it, SQLStatementType.SELECT, selectionArgs)) }
    }

    override fun query(
        sql: String,
        selectionArgs: Array<out Any?>
    ): ICursor = query(SQLQuery.create(session, sql, sql.resolvedSqlStatementType(), selectionArgs))

    override fun query(query: ISQLQuery): ICursor = query(
        SQLQuery.create(
            session,
            query.sql,
            query.sql.resolvedSqlStatementType(),
            query.argCount
        ).also { query.bindTo(it) }
    )

    /**
     * Executes a cancellable query. If the [cancellationSignal] is cancelled from another thread, the query will be
     * aborted at the earliest opportunity and an [OperationCancelledException] will be thrown.
     *
     * @throws OperationCancelledException if the operation was cancelled.
     */
    @Suppress("Detekt.LongParameterList")
    fun query(
        distinct: Boolean,
        table: String,
        columns: Array<out String>,
        selection: String,
        selectionArgs: Array<out Any?>,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: Int?,
        cancellationSignal: CancellationSignal
    ): ICursor = withCancellationSignal(cancellationSignal) {
        query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
    }

    /**
     * Executes a cancellable query. If the [cancellationSignal] is cancelled from another thread, the query will be
     * aborted at the earliest opportunity and an [OperationCancelledException] will be thrown.
     *
     * @param sql the SQL query.
     * @param selectionArgs arguments to bind.
     * @param cancellationSignal signal to cancel the query.
     * @throws OperationCancelledException if the operation was cancelled.
     */
    fun query(
        sql: String,
        selectionArgs: Array<out Any?>,
        cancellationSignal: CancellationSignal
    ): ICursor = withCancellationSignal(cancellationSignal) {
        query(sql, selectionArgs)
    }

    /**
     * Executes a cancellable query. If the [cancellationSignal] is cancelled from another thread, the query will be
     * aborted at the earliest opportunity and an [OperationCancelledException] will be thrown.
     *
     * @param query the query to execute.
     * @param cancellationSignal signal to cancel the query.
     * @throws OperationCancelledException if the operation was cancelled.
     */
    fun query(
        query: ISQLQuery,
        cancellationSignal: CancellationSignal
    ): ICursor = withCancellationSignal(cancellationSignal) {
        query(query)
    }

    @Suppress("Detekt.LongParameterList")
    fun readFromBlob(
        name: String,
        table: String,
        column: String,
        row: Long,
        offset: Int,
        limit: Int,
        stream: OutputStream
    ): Unit = execute(true) {
        blob(name, table, column, row, true).use { b ->
            b.inputStream(offset, limit).use { it.copyTo(stream) }
        }
    }

    fun releaseMemory(priority: Priority) {
        connectionPool.clear(priority)
    }

    override fun setTransactionSuccessful() = pledge { session().setTransactionSuccessful() }

    fun sizeOfBlob(
        name: String,
        table: String,
        column: String,
        row: Long
    ) = execute(true) {
        blob(name, table, column, row, true).use {
            it.size
        }
    }

    fun <T> transact(
        transactionMode: SQLiteTransactionMode = SQLiteTransactionMode.EXCLUSIVE,
        block: SQLDatabase.() -> T
    ) = transact(this, transactionMode, block)

    fun <D, T> transact(
        database: D,
        transactionMode: SQLiteTransactionMode,
        block: D.() -> T
    ): T = pledge {
        val session = session()
        when (transactionMode) {
            SQLiteTransactionMode.DEFERRED -> session.beginDeferredTransaction()
            SQLiteTransactionMode.EXCLUSIVE -> session.beginExclusiveTransaction()
            SQLiteTransactionMode.IMMEDIATE -> session.beginImmediateTransaction()
        }
        try {
            block(database).also { session.setTransactionSuccessful() }
        } finally {
            session.endTransaction()
        }
    }

    fun <T> transact(
        listener: SQLTransactionListener,
        transactionMode: SQLiteTransactionMode = SQLiteTransactionMode.EXCLUSIVE,
        block: SQLDatabase.() -> T
    ): T = pledge {
        val session = session()
        when (transactionMode) {
            SQLiteTransactionMode.DEFERRED -> session.beginDeferredTransaction()
            SQLiteTransactionMode.EXCLUSIVE -> session.beginExclusiveTransactionWithListener(listener)
            SQLiteTransactionMode.IMMEDIATE -> session.beginImmediateTransactionWithListener(listener)
        }
        try {
            block(this@SQLDatabase).also { session.setTransactionSuccessful() }
        } finally {
            session.endTransaction()
        }
    }

    override fun update(
        table: String,
        values: IContentValues,
        whereClause: String,
        whereArgs: Array<out Any?>,
        conflictAlgorithm: IConflictAlgorithm
    ) = pledge {
        require(!values.isEmpty) { "Empty values." }
        val valuesSize = values.size
        val iterator = values.entrySet.iterator()
        SharedSqlBuilder.use {
            append("UPDATE")
                .append(conflictAlgorithm.sql)
                .append(table)
                .append(" SET ")
            val bindArgs = Array(valuesSize + whereArgs.size) {
                if (it < valuesSize) {
                    if (it > 0) { append(',') }
                    iterator.next().run {
                        append(key).append("=?")
                        value
                    }
                } else {
                    whereArgs[it - valuesSize]
                }
            }
            if (whereClause.isNotEmpty()) {
                append(" WHERE ").append(whereClause)
            }
            SQLStatement.executeUpdateDelete(session, toString(), bindArgs)
        }
    }

    override fun upsert(
        table: String,
        values: IContentValues,
        columns: Array<out String>,
        update: String
    ) = pledge {
        require(!values.isEmpty) { "Empty initial values." }
        require(columns.isNotEmpty()) { "Empty conflicting columns." }
        val bindArgs = arrayOfNulls<Any?>(values.size)
        SharedSqlBuilder.use {
            append("INSERT INTO ")
                .append(table)
                .append('(')
            val iterator = values.entrySet.iterator()
            iterator.next().apply {
                append(key)
                bindArgs[0] = value
            }
            1.forUntil(values.size) {
                append(',')
                iterator.next().run {
                    append(key)
                    bindArgs[it] = value
                }
            }
            append(") VALUES (?")
                .apply { repeat(bindArgs.size - 1) { append(",?") } }
                .append(") ON CONFLICT (")
                .append(columns.first())
            1.forUntil(columns.size) {
                append(',').append(columns[it])
            }
            append(") DO UPDATE SET ")
                .append(update)
            SQLStatement.executeInsert(session, toString(), bindArgs)
        }
    }

    override var version: Int
        get() = pledge {
            SQLStatement.executeForInt(
                session,
                "PRAGMA user_version",
                SQLStatementType.PRAGMA,
                EMPTY_ARRAY
            )
        }
        set(value) = pledge {
            SQLStatement.execute(
                session,
                "PRAGMA user_version=$value",
                SQLStatementType.PRAGMA,
                EMPTY_ARRAY
            )
        }

    @Suppress("Detekt.LongParameterList")
    fun writeToBlob(
        name: String,
        table: String,
        column: String,
        row: Long,
        offset: Int,
        stream: InputStream
    ): Unit = transact {
        blob(name, table, column, row, false).use { b ->
            b.outputStream(offset).use { stream.copyTo(it) }
        }
    }

    override fun yieldTransaction() = pledge {
        session().yieldTransaction()
    }

    override fun yieldTransaction(pauseMillis: Long) = pledge {
        session().yieldTransaction(pauseMillis)
    }

    fun setSavepoint(name: String? = null): String = pledge {
        session().setSavepoint(name)
    }

    fun rollbackToSavepoint(name: String) = pledge {
        session().rollbackToSavepoint(name)
    }

    fun releaseSavepoint(name: String) = pledge {
        session().releaseSavepoint(name)
    }

    override fun onReleased() {
        connectionPool.close()
    }

    private fun <T> withCancellationSignal(
        cancellationSignal: CancellationSignal,
        block: SQLDatabase.() -> T
    ): T = pledge {
        cancellationSignal.throwIfCancelled()
        session().execute(false) { executor ->
            executor.setProgressHandler(cancellationSignal.instructionCount) {
                if (cancellationSignal.isCancelled) { 1 } else { 0 }
            }
            try {
                block()
            } catch (@Suppress("Detekt.TooGenericExceptionCaught") e: Exception) {
                if (cancellationSignal.isCancelled &&
                    (e as? SelektSQLException)?.code == SQL_INTERRUPT
                ) {
                    throw OperationCancelledException("Operation was cancelled.")
                }
                throw e
            } finally {
                executor.setProgressHandler(0, null)
            }
        }
    }

    private fun blob(
        name: String,
        table: String,
        column: String,
        row: Long,
        readOnly: Boolean
    ) = pledge {
        session().blob(name, table, column, row, readOnly)
    }

    private fun query(query: SQLQuery): ICursor = pledge {
        SimpleCursorWindow().let {
            val information = query.fill(it)
            WindowedCursor(information.columnNames, it)
        }
    }

    /**
     * Returns a forward-only streaming [ICursor] backed by a [ForwardCursor].
     * The caller **must** close the returned cursor to release the underlying prepared statement
     * back to the connection's cache and to release the session's connection back to the pool.
     */
    fun queryForwardOnly(
        sql: String,
        selectionArgs: Array<out Any?>
    ): ICursor = pledge {
        session().executeForForwardCursor(sql, selectionArgs)
    }
}

interface IDatabase : IReadableDatabase, ISQLTransactor {
    fun compileStatement(sql: String, bindArgs: Array<out Any?>? = null): ISQLStatement

    fun delete(
        table: String,
        whereClause: String,
        whereArgs: Array<out Any?>
    ): Int

    fun exec(sql: String, bindArgs: Array<out Any?>? = null)

    fun insert(
        table: String,
        values: IContentValues,
        conflictAlgorithm: IConflictAlgorithm
    ): Long

    fun update(
        table: String,
        values: IContentValues,
        whereClause: String,
        whereArgs: Array<out Any?>,
        conflictAlgorithm: IConflictAlgorithm
    ): Int

    fun upsert(
        table: String,
        values: IContentValues,
        columns: Array<out String>,
        update: String
    ): Long

    var version: Int
}

interface IReadableDatabase : Closeable {
    fun isOpen(): Boolean

    @Suppress("Detekt.LongParameterList")
    fun query(
        distinct: Boolean,
        table: String,
        columns: Array<out String>,
        selection: String,
        selectionArgs: Array<out Any?>,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: Int?
    ): ICursor

    fun query(
        sql: String,
        selectionArgs: Array<out Any?>
    ): ICursor

    fun query(query: ISQLQuery): ICursor
}
