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

package com.bloomberg.selekt.android.support

import android.content.ContentValues
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.util.Pair
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.android.SQLiteDatabase
import java.util.Locale

internal fun SQLiteDatabase.asSupportSQLiteDatabase(): SupportSQLiteDatabase = SupportSQLiteDatabase(this)

private class SupportSQLiteDatabase constructor(
    private val database: SQLiteDatabase
) : SupportSQLiteDatabase {
    override fun beginTransaction() = database.beginExclusiveTransaction()

    override fun beginTransactionNonExclusive() = database.beginImmediateTransaction()

    override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) = TODO()

    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) = TODO()

    override fun close() = database.close()

    override fun compileStatement(sql: String) = database.compileStatement(sql).asSupportSQLiteStatement()

    override fun delete(
        table: String,
        whereClause: String?,
        whereArgs: Array<out Any>?
    ) = database.delete(
        table,
        whereClause,
        whereArgs
    )

    override fun disableWriteAheadLogging() = Unit

    override fun enableWriteAheadLogging() = isWriteAheadLoggingEnabled

    override fun endTransaction() = database.endTransaction()

    override fun execSQL(sql: String) = database.exec(sql)

    override fun execSQL(sql: String, bindArgs: Array<out Any>) = database.exec(sql, bindArgs)

    override fun getAttachedDbs() = sequence<Pair<String, String>> {
        database.query("PRAGMA database_list", null).use {
            while (it.moveToNext()) {
                yield(Pair(it.getString(1), it.getString(2)))
            }
        }
    }.toList()

    override fun getMaximumSize() = database.maximumSize

    override fun getPageSize() = database.pageSize

    override fun getPath() = database.path

    override fun getVersion() = database.version

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

    override fun isDatabaseIntegrityOk(): Boolean {
        attachedDbs.forEach {
            if (!database.integrityCheck(it.first)) {
                return false
            }
        }
        return true
    }

    override fun isDbLockedByCurrentThread() = database.isConnectionHeldByCurrentThread

    override fun isOpen() = database.isOpen

    override fun isReadOnly() = false

    override fun isWriteAheadLoggingEnabled() = SQLiteJournalMode.WAL == database.journalMode

    override fun needUpgrade(newVersion: Int) = database.version < newVersion

    override fun query(query: String) = database.query(query, null)

    override fun query(query: String, bindArgs: Array<out Any>) = database.query(query, bindArgs)

    override fun query(query: SupportSQLiteQuery) = database.query(query.asSelektSQLQuery())

    // TODO Implement using the cancellation signal.
    override fun query(query: SupportSQLiteQuery, cancellationSignal: CancellationSignal) = query(query)

    override fun setForeignKeyConstraintsEnabled(enable: Boolean) = database.setForeignKeyConstraintsEnabled(enable)

    override fun setLocale(locale: Locale) = throw UnsupportedOperationException()

    override fun setMaximumSize(numBytes: Long) = database.setMaximumSize(numBytes)

    override fun setMaxSqlCacheSize(cacheSize: Int) = throw UnsupportedOperationException()

    override fun setPageSize(numBytes: Long) {
        database.pageSize = numBytes
    }

    override fun setTransactionSuccessful() = database.setTransactionSuccessful()

    override fun setVersion(version: Int) {
        database.version = version
    }

    override fun update(
        table: String,
        conflictAlgorithm: Int,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out Any>?
    ) = database.update(table, values, whereClause, whereArgs, conflictAlgorithm.toConflictAlgorithm())

    override fun yieldIfContendedSafely() = database.yieldTransaction()

    override fun yieldIfContendedSafely(sleepAfterYieldDelay: Long) = database.yieldTransaction(sleepAfterYieldDelay)
}
