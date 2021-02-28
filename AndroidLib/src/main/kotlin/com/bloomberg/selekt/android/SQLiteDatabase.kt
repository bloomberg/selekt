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

package com.bloomberg.selekt.android

import android.content.ContentValues
import androidx.annotation.Size
import android.util.Log
import com.bloomberg.selekt.DatabaseConfiguration
import com.bloomberg.selekt.Experimental
import com.bloomberg.selekt.ISQLQuery
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLTransactionListener
import com.bloomberg.selekt.SQLiteAutoVacuumMode
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SQLiteTraceEventMode
import com.bloomberg.selekt.SQLiteTransactionMode
import com.bloomberg.selekt.pools.Priority
import org.intellij.lang.annotations.Language
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import javax.annotation.concurrent.GuardedBy
import javax.annotation.concurrent.ThreadSafe

internal object SQLiteDatabaseRegistry {
    private val lock = Any()
    @GuardedBy("lock")
    private val store = mutableSetOf<SQLiteDatabase>()

    fun register(database: SQLiteDatabase) = synchronized(lock) {
        check(store.add(database)) { "Failed to register a database, ${store.count()} registered." }
    }

    fun unregister(database: SQLiteDatabase) = synchronized(lock) {
        check(store.remove(database)) { "Failed to unregister a database, ${store.count()} registered." }
    }

    fun releaseMemory(priority: Priority) {
        synchronized(lock) { store.toList() }.run {
            forEach {
                it.releaseMemory(priority)
            }
            Log.d(Selekt.TAG, "Released resources from ${count()} databases.")
        }
    }
}

/**
 * @since v0.1.0.
 */
@ThreadSafe
@Suppress("Detekt.TooManyFunctions", "Detekt.LongParameterList") // Mirrors the Android SDK APIs.
class SQLiteDatabase private constructor(
    private val database: SQLDatabase,
    private val file: File?
) : Closeable {
    companion object {
        private fun internalOpenOrCreateDatabase(
            file: File?,
            configuration: DatabaseConfiguration,
            key: ByteArray?
        ) = SQLiteDatabase(
            SQLDatabase(
                file.let { if (it != null) it.path else "file::memory:" },
                SQLite,
                configuration,
                key,
                randomCompat
            ),
            file
        )

        @JvmStatic
        fun openOrCreateDatabase(
            file: File,
            configuration: DatabaseConfiguration,
            key: ByteArray?
        ) = internalOpenOrCreateDatabase(file, configuration, key)

        @JvmStatic
        fun createInMemoryDatabase(trace: SQLiteTraceEventMode? = null) = internalOpenOrCreateDatabase(null,
            SQLiteJournalMode.MEMORY.databaseConfiguration.copy(trace = trace), null)

        @JvmStatic
        fun deleteDatabase(file: File) = com.bloomberg.selekt.commons.deleteDatabase(file)
    }

    init {
        SQLiteDatabaseRegistry.register(this)
    }

    /**
     * Auto-vacuuming is only possible if the database stores some additional information that allows each database page to
     * be traced backwards to its referrer. Therefore, auto-vacuuming must be turned on before any tables are created. It is
     * not possible to enable or disable auto-vacuum after a table has been created.
     */
    var autoVacuum: SQLiteAutoVacuumMode
        get() = SQLiteAutoVacuumMode.values()[Integer.parseInt(database.pragma("auto_vacuum"))]
        set(value) { database.pragma("auto_vacuum", value) }

    /**
     * Get whether this thread currently holds a connection to the database.
     */
    val isConnectionHeldByCurrentThread: Boolean
        get() = database.isCurrentThreadSessionActive

    val isOpen: Boolean
        get() = database.isOpen()

    /**
     * Get whether the current thread has an open transaction.
     */
    val isTransactionOpenedByCurrentThread: Boolean
        get() = database.inTransaction

    val journalMode: SQLiteJournalMode
        get() = SQLiteJournalMode.valueOf(database.pragma("journal_mode").toUpperCase(Locale.US))

    val maximumSize: Long
        get() = database.run { maxPageCount * pageSize }

    val path = database.path

    fun setMaximumSize(bytes: Long) = database.run {
        val currentPageSize = pageSize
        val pageCount = (bytes / currentPageSize).also {
            if (it % currentPageSize != 0L) {
                it + 1
            }
        }
        setMaxPageCount(pageCount) * currentPageSize
    }

    val maxPageCount: Long
        get() = database.pragma("max_page_count").toLong()

    internal fun setMaxPageCount(value: Long) = requireNotNull(database.pragma("max_page_count", value)).toLong()

    val pageCount: Int
        get() = database.pragma("page_count").toInt()

    var pageSize: Long
        get() = database.pragma(PAGE_SIZE).toLong()
        /**
         * Set the page size of the database. The page size must be a power of two between 512 and 65536 inclusive.
         *
         * Specifying a new page size does not change the page size immediately. Instead, the new page size is remembered
         * and is used to set the page size when the database is first created, if it does not already exist when the
         * query is issued, or at the next VACUUM command that is run on the same database connection while not in WAL mode.
         */
        set(value) { database.pragma(PAGE_SIZE, value) }

    var version: Int
        get() = database.version
        set(value) { database.version = value }

    override fun close() {
        database.use {
            SQLiteDatabaseRegistry.unregister(this)
        }
    }

    /**
     * Transacts to the database in exclusive mode a batch of queries with the same underlying SQL statement. The
     * prototypical use case is for database modifications inside a tight loop to which this is optimised.
     *
     * Each array in the sequence must have the same length, corresponding to the number of arguments in the SQL statement.
     * It is safe for the sequence to recycle the array with each yield.
     *
     * The transaction is not committed by this method until the sequence ends. For long sequences you may therefore wish
     * to yield the transaction periodically.
     *
     * @param sql statement.
     * @param bindArgs sequence of standard type arguments for binding to the statement.
     * @return the number of rows affected.
     */
    @Experimental
    fun batch(@Language("RoomSql") sql: String, bindArgs: Sequence<Array<out Any?>>) = database.batch(sql, bindArgs)

    /**
     * Begins a transaction in exclusive mode.
     *
     * Transactions can be nested. When the outer transaction is ended all of the work done in that transaction and all of
     * the nested transactions will be committed or rolled back. The changes will be rolled back if any transaction is ended
     * without being marked as clean (by calling [setTransactionSuccessful]). Otherwise they will be committed.
     *
     * When in WAL-journal mode, this is equivalent to calling [beginImmediateTransaction].
     *
     * @link [SQLite's transaction](https://www.sqlite.org/lang_transaction.html)
     */
    internal fun beginExclusiveTransaction() = database.beginExclusiveTransaction()

    internal fun beginExclusiveTransactionWithListener(listener: SQLTransactionListener) =
        database.beginExclusiveTransactionWithListener(listener)

    /**
     * Begins a transaction in immediate mode. Prefer [transact] whenever possible.
     *
     * Transactions can be nested. When the outer transaction is ended all of the work done in that transaction and all of
     * the nested transactions will be committed or rolled back. The changes will be rolled back if any transaction is ended
     * without being marked as clean (by calling [setTransactionSuccessful]). Otherwise they will be committed.
     *
     * When in WAL-journal mode, this is equivalent to calling [beginExclusiveTransaction].
     *
     * @link [SQLite's transaction](https://www.sqlite.org/lang_transaction.html)
     */
    internal fun beginImmediateTransaction() = database.beginImmediateTransaction()

    internal fun beginImmediateTransactionWithListener(listener: SQLTransactionListener) =
        database.beginImmediateTransactionWithListener(listener)

    fun compileStatement(@Language("RoomSql") sql: String) = database.compileStatement(sql)

    fun delete(table: String, whereClause: String?, whereArgs: Array<out Any?>?) =
        database.delete(
            table,
            whereClause.orEmpty(),
            whereArgs.orEmpty())

    internal fun endTransaction() = database.endTransaction()

    fun exec(@Language("RoomSql") sql: String) = database.exec(sql)

    fun exec(@Language("RoomSql") sql: String, @Size(min = 1) bindArgs: Array<out Any?>) = database.exec(sql, bindArgs)

    /**
     * The incremental vacuum pragma causes pages to be removed from the freelist. The database file is truncated by the
     * same number of pages. The incremental vacuum pragma has no effect if the database is not already in incremental
     * vacuum mode or if there are no pages on the freelist.
     */
    fun incrementalVacuum() {
        database.pragma("incremental_vacuum")
    }

    /**
     * The incremental vacuum pragma causes up to N pages to be removed from the freelist. The database file is truncated
     * by the same amount. The incremental vacuum pragma has no effect if the database is not already in incremental mode
     * or if there are no pages on the freelist. If there are fewer on the freelist, or if `pages` is less than 1, then the
     * entire freelist is cleared.
     *
     * @param pages to remove from the freelist.
     */
    fun incrementalVacuum(pages: Int) {
        database.pragma("incremental_vacuum($pages)")
    }

    fun insert(
        table: String,
        values: ContentValues,
        conflictAlgorithm: ConflictAlgorithm
    ) = database.insert(table, values.asSelektContentValues(), conflictAlgorithm)

    fun integrityCheck(name: String = "main") = "ok".equals(database.pragma("$name.integrity_check(1)"), true)

    fun query(
        distinct: Boolean,
        table: String,
        columns: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out Any?>?,
        groupBy: String? = null,
        having: String? = null,
        limit: Int? = null,
        orderBy: String? = null
    ) = database.query(
        distinct,
        table,
        columns.orEmpty(),
        selection.orEmpty(),
        selectionArgs.orEmpty(),
        groupBy,
        having,
        orderBy,
        limit
    ).asAndroidCursor()

    fun query(@Language("RoomSql") sql: String, selectionArgs: Array<out Any?>?) =
        database.query(sql, selectionArgs.orEmpty()).asAndroidCursor()

    fun query(query: ISQLQuery) = database.query(query).asAndroidCursor()

    /**
     * @since 0.7.4
     */
    @Experimental
    fun readFromBlob(
        table: String,
        column: String,
        row: Long,
        offset: Int,
        limit: Int,
        stream: OutputStream
    ) = readFromBlob("main", table, column, row, offset, limit, stream)

    /**
     * @since 0.7.3
     */
    @Experimental
    fun readFromBlob(
        name: String,
        table: String,
        column: String,
        row: Long,
        offset: Int,
        limit: Int,
        stream: OutputStream
    ) = database.readFromBlob(name, table, column, row, offset, limit, stream)

    internal fun releaseMemory(priority: Priority) {
        database.releaseMemory(priority)
    }

    /**
     * Setting foreign key constraints is not possible within a transaction; foreign key constraint enforcement may only be
     * enabled or disabled when there are no pending transactions.
     */
    fun setForeignKeyConstraintsEnabled(enabled: Boolean) {
        check(!isTransactionOpenedByCurrentThread) {
            "Setting of foreign key constraints is a no-op within a transaction."
        }
        database.pragma("foreign_keys", if (enabled) "ON" else "OFF")
    }

    internal fun setJournalMode(mode: SQLiteJournalMode) {
        check(!isTransactionOpenedByCurrentThread) { "Journal mode cannot be changed within a transaction." }
        val nextMode = SQLiteJournalMode.valueOf(requireNotNull(database.pragma("journal_mode", mode))
            .toUpperCase(Locale.US))
        check(mode == nextMode) { "Failed to set journal mode to $mode, mode is $nextMode." }
    }

    /**
     * Sets the database page size. The final page size must be a power of two between 512 and 65536 inclusive. This method
     * does not work if any data has been written to the database file, and must be called right after the database has been
     * created.
     *
     * @param value to raise 2 to for the page size.
     */
    internal fun setPageSizeExponent(value: Int) = run {
        require(value in SQLiteOpenParams.LOWEST_PAGE_SIZE_EXPONENT..SQLiteOpenParams.HIGHEST_PAGE_SIZE_EXPONENT) {
            "The page size must be a power of two between 512 and 65536 inclusive."
        }
        database.pragma(PAGE_SIZE, 1 shl value)
    }

    internal fun setTransactionSuccessful() = database.setTransactionSuccessful()

    /**
     * @since 0.7.4
     */
    @Experimental
    fun sizeOfBlob(
        table: String,
        column: String,
        row: Long
    ) = sizeOfBlob("main", table, column, row)

    /**
     * @since 0.7.3
     */
    @Experimental
    fun sizeOfBlob(
        name: String,
        table: String,
        column: String,
        row: Long
    ) = database.sizeOfBlob(name, table, column, row)

    /**
     * Execute a block inside either an exclusive or an immediate database transaction. These transaction modes are the
     * same in WAL-journal mode; in other journal modes, exclusive transaction mode prevents other connections from reading
     * the database while the transaction is underway.
     *
     * Performing write operations within the block on this database on a thread other than the calling thread can result
     * in deadlock.
     *
     * @param transactionMode of the transaction; the default is [SQLiteTransactionMode.EXCLUSIVE].
     * @param block to transact.
     * @return result of the transaction.
     * @link [SQLite's transaction](https://sqlite.org/lang_transaction.html)
     */
    fun <T> transact(
        transactionMode: SQLiteTransactionMode = SQLiteTransactionMode.EXCLUSIVE,
        block: () -> T
    ) = database.transact(transactionMode, block)

    fun update(
        table: String,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out Any?>?,
        conflictAlgorithm: ConflictAlgorithm
    ) = database.update(
        table,
        values.asSelektContentValues(),
        whereClause.orEmpty(),
        whereArgs.orEmpty(),
        conflictAlgorithm
    )

    @Experimental
    fun upsert(
        table: String,
        values: ContentValues,
        columns: Array<out String>,
        update: String
    ) = database.upsert(
        table,
        values.asSelektContentValues(),
        columns,
        update
    )

    /**
     * The vacuum command rebuilds the database file, repacking it into a minimal amount of disk space.
     *
     * A vacuum will fail if there is an open transaction on the database connection that is attempting to run the vacuum.
     * Unfinalized SQL statements typically hold a read transaction open, so the vacuum might fail if there are unfinalized
     * SQL statements on the same connection. Vacuum is a write operation and so if another database connection is holding a
     * lock that prevents writes, then the vacuum will fail.
     *
     * @link [SQLite's VACUUM](https://www.sqlite.org/lang_vacuum.html)
     */
    fun vacuum() = exec("VACUUM")

    /**
     * @link [SQLite's blob_write](https://www.sqlite.org/c3ref/blob_write.html)
     * @since 0.7.4
     */
    @Experimental
    fun writeToBlob(
        table: String,
        column: String,
        row: Long,
        offset: Int,
        stream: InputStream
    ) = writeToBlob("main", table, column, row, offset, stream)

    /**
     * Modifies the contents of a blob; it is not possible to increase the size of a blob using this method.
     *
     * @link [SQLite's blob_write](https://www.sqlite.org/c3ref/blob_write.html)
     * @since 0.7.3
     */
    @Experimental
    fun writeToBlob(
        name: String,
        table: String,
        column: String,
        row: Long,
        offset: Int,
        stream: InputStream
    ) = database.writeToBlob(name, table, column, row, offset, stream)

    fun yieldTransaction() = database.yieldTransaction()

    fun yieldTransaction(pauseMillis: Long) = database.yieldTransaction(pauseMillis)
}

private const val PAGE_SIZE = "page_size"
