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
import com.bloomberg.selekt.pools.Priority
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.lang.StringBuilder
import javax.annotation.concurrent.ThreadSafe

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
@ThreadSafe
class SQLDatabase(
    val path: String,
    sqlite: SQLite,
    configuration: DatabaseConfiguration,
    key: ByteArray?,
    random: IRandom = CommonThreadLocalRandom
) : IDatabase, SharedCloseable() {
    private val connectionPool = openConnectionPool(path, sqlite, configuration, random, key)
    private val session = ThreadLocalSession(connectionPool)

    override val inTransaction: Boolean
        get() = session.get().inTransaction

    val isCurrentThreadSessionActive: Boolean
        get() = session.get().hasObject

    fun batch(sql: String, bindArgs: Sequence<Array<out Any?>>): Int = transact {
        SQLStatement.execute(session, sql, bindArgs)
    }

    override fun beginExclusiveTransaction() = pledge { session.get().beginExclusiveTransaction() }

    override fun beginExclusiveTransactionWithListener(listener: SQLTransactionListener) = pledge {
        session.get().beginExclusiveTransactionWithListener(listener)
    }

    override fun beginImmediateTransaction() = pledge { session.get().beginImmediateTransaction() }

    override fun beginImmediateTransactionWithListener(listener: SQLTransactionListener) = pledge {
        session.get().beginImmediateTransactionWithListener(listener)
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

    override fun endTransaction() = pledge { session.get().endTransaction() }

    override fun exec(sql: String, bindArgs: Array<out Any?>?): Unit = pledge {
        compileStatement(
            sql,
            sql.resolvedSqlStatementType(),
            bindArgs
        ).execute()
    }

    fun <T> execute(readOnly: Boolean, block: SQLDatabase.() -> T) = pledge {
        session.get().execute(readOnly) { block() }
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

    fun pragma(key: String) = pledge {
        checkNotNull(SQLStatement.executeForString(
            session,
            "PRAGMA $key",
            SQLStatementType.PRAGMA,
            EMPTY_ARRAY
        ))
    }

    fun pragma(key: String, value: Any) = pledge {
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

    override fun setTransactionSuccessful() = pledge { session.get().setTransactionSuccessful() }

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
        transactionMode: SQLiteTransactionMode = SQLiteTransactionMode.EXCLUSIVE,
        block: D.() -> T
    ): T = pledge {
        val session = session.get()
        when (transactionMode) {
            SQLiteTransactionMode.EXCLUSIVE -> session.beginExclusiveTransaction()
            SQLiteTransactionMode.IMMEDIATE -> session.beginImmediateTransaction()
        }
        try {
            block(database).also { session.setTransactionSuccessful() }
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
                append(',').append(it)
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
        session.get().yieldTransaction()
    }

    override fun yieldTransaction(pauseMillis: Long) = pledge {
        session.get().yieldTransaction(pauseMillis)
    }

    override fun onReleased() {
        connectionPool.close()
    }

    private fun blob(
        name: String,
        table: String,
        column: String,
        row: Long,
        readOnly: Boolean
    ) = pledge {
        session.get().blob(name, table, column, row, readOnly)
    }

    private fun query(query: SQLQuery): ICursor = pledge {
        SimpleCursorWindow().let {
            val information = query.fill(it)
            WindowedCursor(information.columnNames, it)
        }
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
