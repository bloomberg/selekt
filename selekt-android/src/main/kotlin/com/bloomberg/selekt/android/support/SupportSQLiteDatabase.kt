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

package com.bloomberg.selekt.android.support

import android.content.ContentValues
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.util.Pair
import androidx.annotation.VisibleForTesting
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import com.bloomberg.selekt.SQLTransactionListener
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.android.SQLiteDatabase
import com.bloomberg.selekt.annotations.DelicateApi
import org.intellij.lang.annotations.Language
import java.util.Locale

@DelicateApi
@JvmSynthetic
internal fun SQLiteDatabase.asSupportSQLiteDatabase(): SupportSQLiteDatabase = SupportSQLiteDatabase(this)

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@JvmSynthetic
internal fun SQLiteTransactionListener.asSQLTransactionListener(): SQLTransactionListener =
    WrappedSQLiteTransactionListener(this)

private class WrappedSQLiteTransactionListener(
    private val listener: SQLiteTransactionListener
) : SQLTransactionListener {
    override fun onBegin() = listener.onBegin()

    override fun onCommit() = listener.onCommit()

    override fun onRollback() = listener.onRollback()

    override fun equals(other: Any?) = this === other ||
        other is WrappedSQLiteTransactionListener && listener == other.listener

    override fun hashCode() = listener.hashCode()
}

@DelicateApi
private class SupportSQLiteDatabase(
    private val database: SQLiteDatabase
) : SupportSQLiteDatabase {
    override fun beginTransaction() = database.beginExclusiveTransaction()

    override fun beginTransactionNonExclusive() = database.beginImmediateTransaction()

    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) =
        database.beginExclusiveTransactionWithListener(transactionListener.asSQLTransactionListener())

    override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) =
        database.beginImmediateTransactionWithListener(transactionListener.asSQLTransactionListener())

    override fun close() = database.close()

    override fun compileStatement(@Language("RoomSql") sql: String) =
        database.compileStatement(sql).asSupportSQLiteStatement()

    override fun delete(
        table: String,
        whereClause: String?,
        whereArgs: Array<out Any?>?
    ): Int = database.delete(
        table,
        whereClause,
        whereArgs
    )

    override fun disableWriteAheadLogging() = Unit

    override fun enableWriteAheadLogging() = isWriteAheadLoggingEnabled

    override fun endTransaction() = database.endTransaction()

    override fun execSQL(@Language("RoomSql") sql: String) = database.exec(sql)

    override fun execSQL(@Language("RoomSql") sql: String, bindArgs: Array<out Any?>) = database.exec(sql, bindArgs)

    override val attachedDbs: List<Pair<String, String>>
        get() = database.query("PRAGMA database_list", null).use {
            List(it.count) { _ ->
                it.moveToNext()
                Pair(it.getString(1), it.getString(2))
            }
        }

    override val maximumSize: Long
        get() = database.maximumSize

    override var pageSize: Long
        get() = database.pageSize
        set(value) {
            database.pageSize = value
        }

    override val path: String
        get() = database.path

    override var version: Int
        get() = database.version
        set(value) {
            database.version = value
        }

    override fun insert(
        table: String,
        conflictAlgorithm: Int,
        values: ContentValues
    ) = database.insert(
        table,
        values,
        conflictAlgorithm.toConflictAlgorithm()
    )

    override fun inTransaction() = database.isTransactionOpenedByCurrentThread

    override val isDatabaseIntegrityOk: Boolean
        get() = !attachedDbs.any {
            !database.integrityCheck(it.first)
        }

    override val isDbLockedByCurrentThread: Boolean
        get() = database.isConnectionHeldByCurrentThread

    override val isOpen: Boolean
        get() = database.isOpen

    override val isReadOnly: Boolean = false

    override val isWriteAheadLoggingEnabled: Boolean
        get() = SQLiteJournalMode.WAL == database.journalMode

    override fun needUpgrade(newVersion: Int) = database.version < newVersion

    override fun query(query: String) = database.query(query, null)

    override fun query(query: String, bindArgs: Array<out Any?>) = database.query(query, bindArgs)

    override fun query(query: SupportSQLiteQuery) = database.query(query.asSelektSQLQuery())

    // TODO Implement using the cancellation signal.
    override fun query(query: SupportSQLiteQuery, cancellationSignal: CancellationSignal?) = query(query)

    override fun setForeignKeyConstraintsEnabled(enabled: Boolean) = database.setForeignKeyConstraintsEnabled(enabled)

    override fun setLocale(locale: Locale) = throw UnsupportedOperationException()

    override fun setMaximumSize(numBytes: Long) = database.setMaximumSize(numBytes)

    override fun setMaxSqlCacheSize(cacheSize: Int) = throw UnsupportedOperationException()

    override fun setTransactionSuccessful() = database.setTransactionSuccessful()

    override fun update(
        table: String,
        conflictAlgorithm: Int,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out Any?>?
    ) = database.update(table, values, whereClause, whereArgs, conflictAlgorithm.toConflictAlgorithm())

    override fun yieldIfContendedSafely() = yieldIfContendedSafely(0L)

    override fun yieldIfContendedSafely(
        sleepAfterYieldDelayMillis: Long
    ) = database.yieldTransaction(sleepAfterYieldDelayMillis)
}
