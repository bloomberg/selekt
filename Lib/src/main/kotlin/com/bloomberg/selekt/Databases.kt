/*
 * Copyright 2020 Bloomberg Finance L.P.
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

import com.bloomberg.commons.ManagedStringBuilder
import com.bloomberg.commons.forUntil
import com.bloomberg.commons.threadLocal
import java.io.Closeable
import javax.annotation.concurrent.ThreadSafe

private val sharedSqlBuilder by threadLocal { ManagedStringBuilder() }

/**
 * The use of ThreadLocal underpins [SQLDatabase]'s thread-safety.
 *
 * A thread can acquire at most one connection session per database instance. Sessions hold at most one connection at any
 * given time, and exclusively. Sessions control all access to a database's connections and each session is thread-bound.
 * This prevents two threads from ever sharing a single connection.
 *
 * This is the same strategy Google employs in the Android SDK.
 *
 * @link [Android's SQLiteDatabase](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/database/sqlite/SQLiteDatabase.java)
 */
@ThreadSafe
class SQLDatabase constructor(
    val path: String,
    sqlite: SQLite,
    configuration: DatabaseConfiguration,
    key: ByteArray?,
    random: IRandom = CommonThreadLocalRandom
) : IDatabase, SharedCloseable() {
    private val connectionPool = openConnectionPool(path, sqlite, configuration, random, key)
    private val session = ThreadLocalisedSession(connectionPool)

    data class Gauge(val connectionCount: Int)

    override val inTransaction: Boolean
        get() = session.inTransaction

    val isCurrentThreadSessionActive: Boolean
        get() = session.hasObject

    override fun beginDeferredTransaction() = pledge { session.beginDeferredTransaction() }

    override fun beginExclusiveTransaction() = pledge { session.beginExclusiveTransaction() }

    override fun beginImmediateTransaction() = pledge { session.beginImmediateTransaction() }

    override fun compileStatement(sql: String, bindArgs: Array<out Any?>?) = compileStatement(true, sql, bindArgs)

    private fun compileStatement(isRaw: Boolean, sql: String, bindArgs: Array<out Any?>?): ISQLStatement =
        SQLStatement.compile(session, isRaw, sql, bindArgs)

    override fun delete(
        table: String,
        whereClause: String,
        whereArgs: Array<out Any?>
    ) = pledge {
        compileStatement(
            false,
            "DELETE FROM $table${if (whereClause.isNotEmpty()) " WHERE $whereClause" else ""}",
            whereArgs
        ).executeUpdateDelete()
    }

    override fun endTransaction() = pledge { session.endTransaction() }

    override fun exec(sql: String, bindArgs: Array<out Any?>?): Unit = pledge {
        compileStatement(true, sql, bindArgs).execute()
    }

    fun gauge() = connectionPool.gauge().run {
        Gauge(connectionCount = count)
    }

    override fun insert(
        table: String,
        values: IContentValues,
        conflictAlgorithm: IConflictAlgorithm
    ) = pledge {
        require(!values.isEmpty) { "Empty initial values." }
        sharedSqlBuilder.use {
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
            SQLStatement.compileTrustedWrite(session, toString(), bindArgs)
        }.executeInsert()
    }

    fun pragma(key: String) = pledge {
        requireNotNull(compileStatement(false, "PRAGMA $key", null).simpleQueryForString())
    }

    fun pragma(key: String, value: Any) = pledge {
        SQLStatement.compile(session, false, "PRAGMA $key=$value", null).executeForString()
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
    ) = sharedSqlBuilder.use {
        selectColumns(columns, distinct)
            .fromTable(table)
            .where(selection)
            .groupBy(groupBy)
            .having(having)
            .orderBy(orderBy)
            .limit(limit)
            .toString()
            .let { query(SQLQuery.create(session, it, false, selectionArgs)) }
    }

    override fun query(
        sql: String,
        selectionArgs: Array<out Any?>
    ): ICursor = query(SQLQuery.create(session, sql, true, selectionArgs))

    override fun query(query: ISQLQuery): ICursor = query(
        SQLQuery.create(session, query.sql, true, query.argCount).also { query.bindTo(it) })

    override fun setTransactionSuccessful() = pledge { session.setTransactionSuccessful() }

    fun <T> transact(block: SQLDatabase.() -> T): T = pledge {
        session.beginImmediateTransaction()
        try {
            block(this).also { session.setTransactionSuccessful() }
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
        sharedSqlBuilder.use {
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
            SQLStatement.compileTrustedWrite(session, toString(), bindArgs)
        }.executeUpdateDelete()
    }

    override fun upsert(
        table: String,
        values: IContentValues,
        columns: Array<out String>,
        update: String
    ) {
        require(!values.isEmpty) { "Empty initial values." }
        require(columns.isNotEmpty()) { "Empty conflicting columns." }
        val bindArgs = arrayOfNulls<Any?>(values.size)
        sharedSqlBuilder.use {
            append("INSERT INTO ")
                .append(table)
                .append('(')
            val iterator = values.entrySet.iterator()
            iterator.next().apply {
                append(key)
                bindArgs[0] = value
            }
            repeat(values.size - 1) {
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
            SQLStatement.compileTrustedWrite(session, toString(), bindArgs)
        }.executeInsert()
    }

    override var version: Int
        get() = pledge { SQLStatement.compile(session, false, "PRAGMA user_version", null).executeForInt() }
        set(value) { pledge { compileStatement(false, "PRAGMA user_version=$value", null).execute() } }

    override fun yieldTransaction() = pledge {
        session.yieldTransaction()
    }

    override fun yieldTransaction(pauseMillis: Long) = pledge {
        session.yieldTransaction(pauseMillis)
    }

    override fun onReleased() {
        connectionPool.close()
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
    )

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
