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
import com.bloomberg.selekt.DatabaseKey
import com.bloomberg.selekt.ISQLRawStatement
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.android.sqlite
import com.bloomberg.selekt.exceptions.SelektSQLException
import java.sql.SQLException

private const val MEMORY_FILE_NAME = ":memory:"
private const val MEMORY_PATH = "file::memory:"

/**
 * @since 1.1.0
 */
fun createSelektSQLiteDriver(
    journalMode: SQLiteJournalMode = SQLiteJournalMode.WAL,
    key: ByteArray? = null
): SQLiteDriver = SelektSQLiteDriver(journalMode, key)

private class SelektSQLiteDriver(
    private val journalMode: SQLiteJournalMode,
    private val key: ByteArray?
) : SQLiteDriver {
    override val hasConnectionPool = true

    override fun open(fileName: String): SQLiteConnection = translatingSQLiteExceptions {
        val keyCopy = key?.copyOf()
        try {
            val isMemory = MEMORY_FILE_NAME == fileName
            val path = if (isMemory) { MEMORY_PATH } else { fileName }
            val configuration = if (isMemory) {
                SQLiteJournalMode.MEMORY.databaseConfiguration
            } else {
                journalMode.databaseConfiguration
            }
            keyCopy?.let { DatabaseKey.of(SQLite, it) }.use { databaseKey ->
                SelektSQLiteConnection(SQLDatabase(path, SQLite, configuration, databaseKey, CommonThreadLocalRandom))
            }
        } finally {
            keyCopy?.fill(0)
        }
    }
}

private class SelektSQLiteConnection(private val database: SQLDatabase) : SQLiteConnection {
    override fun inTransaction() = translatingSQLiteExceptions { database.inTransaction }

    override fun prepare(sql: String): SQLiteStatement = translatingSQLiteExceptions {
        SelektSQLiteStatement(database.prepare(sql))
    }

    override fun close() = translatingSQLiteExceptions { database.close() }
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

    override fun step() = translatingSQLiteExceptions { statement.step() }

    override fun reset() = translatingSQLiteExceptions { statement.reset() }

    override fun clearBindings() = translatingSQLiteExceptions { statement.clearBindings() }

    override fun close() = translatingSQLiteExceptions { statement.close() }
}

private object SQLite : com.bloomberg.selekt.SQLite(sqlite)

private inline fun <T> translatingSQLiteExceptions(block: () -> T): T = try {
    block()
} catch (e: SQLException) {
    throwSQLiteException((e as? SelektSQLException)?.code ?: -1, e.message)
}
