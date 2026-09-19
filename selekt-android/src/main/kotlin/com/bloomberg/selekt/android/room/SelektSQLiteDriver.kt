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

package com.bloomberg.selekt.android.room

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.throwSQLiteException
import com.bloomberg.selekt.CommonThreadLocalRandom
import com.bloomberg.selekt.DatabaseConfiguration
import com.bloomberg.selekt.DatabaseKey
import com.bloomberg.selekt.ISQLRawStatement
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.android.sqlite
import com.bloomberg.selekt.exceptions.SelektSQLException
import java.io.Closeable
import java.sql.SQLException
import javax.annotation.concurrent.ThreadSafe

private const val MEMORY_FILE_NAME = ":memory:"
private const val MEMORY_PATH = "file::memory:"

/**
 * Creates a pooled SQLite driver for Room 2.8 or later, including Room 3.
 *
 * The supplied [key] remains owned by the caller and is never modified. Before this function returns, the driver copies
 * the key into protected native memory. The caller should clear the supplied array immediately afterwards, including
 * when this function throws. A key must be exactly 32 bytes and must not consist entirely of zero bytes.
 *
 * The returned driver owns the native key and must be closed after every Room database using it has been closed. Closing
 * the driver prevents new connections from being opened. Existing connections remain usable and retain the native key
 * until they are closed, after which the key is destroyed.
 *
 * @since 1.2.0
 */
fun createSelektSQLiteDriver(
    journalMode: SQLiteJournalMode = SQLiteJournalMode.WAL,
    key: ByteArray? = null
): SelektSQLiteDriver = SelektSQLiteDriver(journalMode, key)

/**
 * Creates a pooled SQLite driver with an explicit database configuration.
 */
fun createSelektSQLiteDriver(
    databaseConfiguration: DatabaseConfiguration,
    key: ByteArray? = null
): SelektSQLiteDriver = SelektSQLiteDriver(databaseConfiguration, key)

/**
 * A pooled SQLite driver that owns a native copy of its optional database key.
 *
 * @see createSelektSQLiteDriver
 * @since 1.2.0
 */
@ThreadSafe
class SelektSQLiteDriver internal constructor(
    private val databaseConfiguration: DatabaseConfiguration,
    key: ByteArray?
) : SQLiteDriver, Closeable {
    internal constructor(journalMode: SQLiteJournalMode, key: ByteArray?) : this(
        journalMode.databaseConfiguration,
        key
    )

    private val lifecycleLock = Any()
    private val key = key?.let {
        require(it.any { byte -> byte != 0.toByte() }) {
            "Encryption keys must not consist entirely of zero bytes."
        }
        DatabaseKey.of(SQLite, it)
    }
    private var closed = false

    override val hasConnectionPool = true

    override fun open(fileName: String): SQLiteConnection = synchronized(lifecycleLock) {
        check(!closed) { "Driver is closed." }
        translatingSQLiteExceptions {
            val isMemory = MEMORY_FILE_NAME == fileName
            val path = if (isMemory) { MEMORY_PATH } else { fileName }
            val configuration = if (isMemory) {
                databaseConfiguration.copy(
                    busyTimeoutMillis = SQLiteJournalMode.MEMORY.databaseConfiguration.busyTimeoutMillis,
                    maxConnectionPoolSize = SQLiteJournalMode.MEMORY.databaseConfiguration.maxConnectionPoolSize,
                    timeBetweenEvictionRunsMillis =
                        SQLiteJournalMode.MEMORY.databaseConfiguration.timeBetweenEvictionRunsMillis
                )
            } else {
                databaseConfiguration
            }
            SelektSQLiteConnection(SQLDatabase(path, SQLite, configuration, key, CommonThreadLocalRandom))
        }
    }

    /**
     * Releases this driver's ownership of its native key and prevents new connections from being opened.
     *
     * This operation is idempotent. It does not close existing connections; they remain usable and keep the native key
     * alive until they are closed. Close every associated Room database before closing its driver.
     */
    override fun close() = synchronized(lifecycleLock) {
        if (!closed) {
            closed = true
            key?.release()
        }
    }
}

private class SelektSQLiteConnection(private val database: SQLDatabase) : SQLiteConnection {
    override fun inTransaction() = translatingSQLiteExceptions { database.inTransaction }

    override fun prepare(sql: String): SQLiteStatement = translatingSQLiteExceptions {
        SelektSQLiteStatement(database.prepare(sql))
    }

    override fun close() = translatingSQLiteExceptions(database::close)
}

private class SelektSQLiteStatement(private val statement: ISQLRawStatement) : SQLiteStatement {
    override fun bindBlob(index: Int, value: ByteArray) = translatingSQLiteExceptions { statement.bindBlob(index, value) }

    override fun bindDouble(index: Int, value: Double) = translatingSQLiteExceptions { statement.bindDouble(index, value) }

    override fun bindLong(index: Int, value: Long) = translatingSQLiteExceptions { statement.bindLong(index, value) }

    override fun bindText(index: Int, value: String) = translatingSQLiteExceptions { statement.bindString(index, value) }

    override fun bindNull(index: Int) = translatingSQLiteExceptions { statement.bindNull(index) }

    override fun getBlob(index: Int): ByteArray = translatingSQLiteExceptions {
        checkNotNull(statement.columnBlob(index)) { "Column $index is NULL." }
    }

    override fun getDouble(index: Int) = translatingSQLiteExceptions { statement.columnDouble(index) }

    override fun getLong(index: Int) = translatingSQLiteExceptions { statement.columnLong(index) }

    override fun getText(index: Int): String = translatingSQLiteExceptions {
        checkNotNull(statement.columnString(index)) { "Column $index is NULL." }
    }

    override fun isNull(index: Int) = translatingSQLiteExceptions { statement.isNull(index) }

    override fun getColumnCount() = translatingSQLiteExceptions { statement.columnCount }

    override fun getColumnName(index: Int) = translatingSQLiteExceptions { statement.columnName(index) }

    override fun getColumnType(index: Int) = translatingSQLiteExceptions { statement.columnType(index) }

    override fun step() = translatingSQLiteExceptions(statement::step)

    override fun reset() = translatingSQLiteExceptions(statement::reset)

    override fun clearBindings() = translatingSQLiteExceptions(statement::clearBindings)

    override fun close() = translatingSQLiteExceptions(statement::close)
}

private object SQLite : com.bloomberg.selekt.SQLite(sqlite)

private inline fun <T> translatingSQLiteExceptions(block: () -> T): T = try {
    block()
} catch (e: SQLException) {
    throwSQLiteException((e as? SelektSQLException)?.code ?: -1, e.message)
}
