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

import java.sql.SQLException

@Suppress("Detekt.LongParameterList", "Detekt.TooManyFunctions")
open class SQLite(
    private val sqlite: ExternalSQLite
) {
    fun bindBlob(statement: Long, index: Int, blob: ByteArray) = checkBindSQLCode(
        statement,
        sqlite.bindBlob(statement, index, blob, blob.size)
    )

    fun bindDouble(statement: Long, index: Int, value: Double) = checkBindSQLCode(
        statement,
        sqlite.bindDouble(statement, index, value)
    )

    fun bindInt(statement: Long, index: Int, value: Int) = checkBindSQLCode(
        statement,
        sqlite.bindInt(statement, index, value)
    )

    fun bindInt64(statement: Long, index: Int, value: Long) = checkBindSQLCode(
        statement,
        sqlite.bindInt64(statement, index, value)
    )

    fun bindNull(statement: Long, index: Int) = checkBindSQLCode(
        statement,
        sqlite.bindNull(statement, index)
    )

    fun bindParameterCount(statement: Long) = sqlite.bindParameterCount(statement)

    fun bindText(statement: Long, index: Int, value: String) = checkBindSQLCode(
        statement,
        sqlite.bindText(statement, index, value)
    )

    fun bindZeroBlob(
        statement: Long,
        index: Int,
        length: Int
    ) = checkBindSQLCode(
        statement,
        sqlite.bindZeroBlob(statement, index, length)
    )

    fun blobBytes(blob: Long) = sqlite.blobBytes(blob)

    fun blobClose(blob: Long) = checkSQLCode(sqlite.blobClose(blob))

    fun blobOpen(
        db: Long,
        name: String,
        table: String,
        column: String,
        row: Long,
        flags: Int,
        holder: LongArray
    ) = checkConnectionSQLCode(
        db,
        sqlite.blobOpen(
            db,
            name,
            table,
            column,
            row,
            flags,
            holder
        )
    )

    fun blobRead(
        blob: Long,
        offset: Int,
        destination: ByteArray,
        destinationOffset: Int,
        length: Int
    ) = checkSQLCode(sqlite.blobRead(
        blob,
        offset,
        destination,
        destinationOffset,
        length
    ))

    fun blobReopen(
        blob: Long,
        row: Long
    ) = checkSQLCode(sqlite.blobReopen(blob, row))

    fun blobWrite(
        blob: Long,
        offset: Int,
        source: ByteArray,
        sourceOffset: Int,
        length: Int
    ) = checkSQLCode(sqlite.blobWrite(
        blob,
        offset,
        source,
        sourceOffset,
        length
    ))

    fun busyTimeout(db: Long, millis: Int) = checkConnectionSQLCode(db, sqlite.busyTimeout(db, millis))

    fun changes(db: Long) = sqlite.changes(db)

    fun clearBindings(statement: Long) = checkSQLCode(sqlite.clearBindings(statement))

    fun closeV2(db: Long) = checkConnectionSQLCode(db, sqlite.closeV2(db))

    fun columnBlob(statement: Long, index: Int) = sqlite.columnBlob(statement, index)

    fun columnCount(statement: Long) = sqlite.columnCount(statement)

    fun columnDouble(statement: Long, index: Int) = sqlite.columnDouble(statement, index)

    fun columnInt(statement: Long, index: Int) = sqlite.columnInt(statement, index)

    fun columnInt64(statement: Long, index: Int) = sqlite.columnInt64(statement, index)

    fun columnName(statement: Long, index: Int) = sqlite.columnName(statement, index)

    fun columnText(statement: Long, index: Int) = sqlite.columnText(statement, index)

    fun columnType(statement: Long, index: Int) = sqlite.columnType(statement, index)

    fun columnValue(statement: Long, index: Int) = sqlite.columnValue(statement, index)

    fun databaseHandle(statement: Long) = sqlite.databaseHandle(statement)

    fun databaseReadOnly(db: Long, name: String) = sqlite.databaseReadOnly(db, name)

    fun databaseReleaseMemory(db: Long) = sqlite.databaseReleaseMemory(db)

    fun databaseStatus(
        db: Long,
        options: Int,
        reset: Boolean,
        holder: IntArray
    ) = sqlite.databaseStatus(db, options, reset, holder)

    fun errorCode(db: Long) = sqlite.errorCode(db)

    fun errorMessage(db: Long) = sqlite.errorMessage(db)

    fun exec(db: Long, query: String) = checkConnectionSQLCode(db, sqlite.exec(db, query))

    fun expandedSql(statement: Long) = sqlite.expandedSql(statement)

    fun extendedErrorCode(db: Long) = sqlite.extendedErrorCode(db)

    fun extendedResultCodes(db: Long, onOff: Int) = sqlite.extendedResultCodes(db, onOff)

    fun finalize(statement: Long) = checkStatementSQLCode(statement, sqlite.finalize(statement))

    fun getAutocommit(db: Long) = sqlite.getAutocommit(db)

    fun hardHeapLimit64() = sqlite.hardHeapLimit64()

    fun key(db: Long, key: ByteArray) = checkConnectionSQLCode(db, sqlite.key(db, key, key.size))

    fun keyConventionally(db: Long, key: ByteArray) = checkConnectionSQLCode(db, sqlite.keyConventionally(db, key, key.size))

    fun keywordCount() = sqlite.keywordCount()

    fun lastInsertRowId(db: Long) = sqlite.lastInsertRowId(db)

    fun memoryUsed() = sqlite.memoryUsed()

    fun openV2(
        path: String,
        flags: Int,
        dbHolder: LongArray
    ) = checkSQLCode(sqlite.openV2(path, flags, dbHolder))

    fun prepareV2(
        db: Long,
        sql: String,
        statementHolder: LongArray
    ) = checkConnectionSQLCode(db, sqlite.prepareV2(db, sql, sql.length, statementHolder))

    fun rawKey(db: Long, key: ByteArray) = checkConnectionSQLCode(db, sqlite.rawKey(db, key, key.size))

    fun rekey(db: Long, key: ByteArray) = checkConnectionSQLCode(db, sqlite.rekey(db, key, key.size))

    fun releaseMemory(bytes: Int) = sqlite.releaseMemory(bytes)

    fun reset(statement: Long) = checkStatementSQLCode(statement, sqlite.reset(statement))

    fun resetAndClearBindings(statement: Long) = checkStatementSQLCode(statement, sqlite.resetAndClearBindings(statement))

    fun softHeapLimit64() = sqlite.softHeapLimit64()

    fun sql(statement: Long) = sqlite.sql(statement)

    fun statementBusy(statement: Long) = sqlite.statementBusy(statement)

    fun statementReadOnly(statement: Long) = sqlite.statementReadOnly(statement)

    fun statementStatus(
        statement: Long,
        options: Int,
        reset: Boolean
    ) = sqlite.statementStatus(statement, options, reset)

    fun step(statement: Long) = checkStepSQLCode(statement, sqlite.step(statement))

    fun stepWithoutThrowing(statement: Long) = sqlite.step(statement)

    fun threadsafe() = sqlite.threadsafe()

    fun totalChanges(db: Long) = sqlite.totalChanges(db)

    fun traceV2(db: Long, flag: Int) = sqlite.traceV2(db, flag)

    fun transactionState(db: Long) = sqlite.transactionState(db)

    fun valueDup(value: Long) = sqlite.valueDup(value)

    fun valueFree(value: Long) = sqlite.valueFree(value)

    fun valueFromBind(value: Long) = sqlite.valueFromBind(value)

    fun walAutoCheckpoint(db: Long, pages: Int) = checkConnectionSQLCode(db, sqlite.walAutoCheckpoint(db, pages))

    fun walCheckpointV2(
        db: Long,
        name: String?,
        mode: Int
    ) = checkConnectionSQLCode(db, sqlite.walCheckpointV2(db, name, mode))

    open fun throwSQLException(
        code: SQLCode,
        extendedCode: SQLCode,
        message: String,
        context: String? = null
    ): Nothing = throw SQLException("Code: $code; Extended: $extendedCode; Message: $message; Context: $context")

    fun throwSQLException(db: Long, context: String? = null): Nothing =
        throwSQLException(errorCode(db), extendedErrorCode(db), errorMessage(db), context)

    private fun checkSQLCode(code: SQLCode): SQLCode = when (code) {
        SQL_OK -> SQL_OK
        else -> throwSQLException(code, -1, "Error information not accessible.")
    }

    private fun checkConnectionSQLCode(db: Long, code: SQLCode): SQLCode {
        if (SQL_OK == code) {
            return SQL_OK
        }
        throwSQLException(db)
    }

    private fun checkStatementSQLCode(statement: Long, code: SQLCode): SQLCode = if (SQL_OK == code) {
        SQL_OK
    } else {
        throwSQLException(databaseHandle(statement))
    }

    private fun checkBindSQLCode(statement: Long, code: SQLCode): SQLCode {
        if (SQL_OK == code) {
            return SQL_OK
        }
        throwSQLException(databaseHandle(statement))
    }

    private fun checkStepSQLCode(statement: Long, code: SQLCode): SQLCode {
        if (SQL_ROW == code || SQL_DONE == code) {
            return code
        }
        throwSQLException(databaseHandle(statement))
    }
}
