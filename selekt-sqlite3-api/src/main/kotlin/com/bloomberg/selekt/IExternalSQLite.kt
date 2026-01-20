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

private const val DEFAULT_SOFT_HEAP_LIMIT = 8 * 1024 * 1024L

data class SQLiteConfiguration(
    val softHeapLimit: Long = DEFAULT_SOFT_HEAP_LIMIT
)

@Suppress("Detekt.ComplexInterface", "Detekt.LongParameterList", "Detekt.TooManyFunctions") // Mirrors the SQLite3 C-api.
interface IExternalSQLite {
    fun bindBlob(statement: Long, index: Int, blob: ByteArray, length: Int): SQLCode

    fun bindDouble(statement: Long, index: Int, value: Double): SQLCode

    fun bindInt(statement: Long, index: Int, value: Int): SQLCode

    fun bindInt64(statement: Long, index: Int, value: Long): SQLCode

    fun bindNull(statement: Long, index: Int): SQLCode

    fun bindParameterCount(statement: Long): Int

    fun bindText(statement: Long, index: Int, value: String): SQLCode

    fun bindZeroBlob(statement: Long, index: Int, length: Int): SQLCode

    fun blobBytes(blob: Long): Int

    fun blobClose(blob: Long): SQLCode

    fun blobOpen(
        db: Long,
        name: String,
        table: String,
        column: String,
        row: Long,
        flags: Int,
        holder: LongArray
    ): SQLCode

    fun blobRead(
        blob: Long,
        offset: Int,
        destination: ByteArray,
        destinationOffset: Int,
        length: Int
    ): SQLCode

    fun blobReopen(blob: Long, row: Long): SQLCode

    fun blobWrite(
        blob: Long,
        offset: Int,
        source: ByteArray,
        sourceOffset: Int,
        length: Int
    ): SQLCode

    fun busyTimeout(db: Long, millis: Int): SQLCode

    fun changes(db: Long): Int

    fun clearBindings(statement: Long): SQLCode

    fun closeV2(db: Long): SQLCode

    fun columnBlob(statement: Long, index: Int): ByteArray?

    fun columnCount(statement: Long): Int

    fun columnDouble(statement: Long, index: Int): Double

    fun columnInt(statement: Long, index: Int): Int

    fun columnInt64(statement: Long, index: Int): Long

    fun columnName(statement: Long, index: Int): String

    fun columnText(statement: Long, index: Int): String

    fun columnType(statement: Long, index: Int): SQLDataType

    fun columnValue(statement: Long, index: Int): Long

    fun databaseHandle(statement: Long): Long

    fun databaseReadOnly(db: Long, name: String): Int

    fun databaseReleaseMemory(db: Long): Int

    fun databaseStatus(
        db: Long,
        options: Int,
        reset: Boolean,
        holder: IntArray
    ): SQLCode

    fun errorCode(db: Long): Int

    fun errorMessage(db: Long): String

    fun exec(db: Long, query: String): SQLCode

    fun expandedSql(statement: Long): String

    fun extendedErrorCode(db: Long): Int

    fun extendedResultCodes(db: Long, onOff: Int): Int

    fun finalize(statement: Long): SQLCode

    fun getAutocommit(db: Long): Int

    fun gitCommit(): String

    fun hardHeapLimit64(): Long

    fun key(db: Long, key: ByteArray, length: Int): SQLCode

    fun keyConventionally(db: Long, key: ByteArray, length: Int): SQLCode

    fun keywordCount(): Int

    fun lastInsertRowId(db: Long): Long

    fun libVersion(): String

    fun libVersionNumber(): Int

    fun memoryUsed(): Long

    fun openV2(path: String, flags: Int, dbHolder: LongArray): SQLCode

    fun prepareV2(db: Long, sql: String, length: Int, statementHolder: LongArray): SQLCode

    fun rawKey(db: Long, key: ByteArray, length: Int): SQLCode

    fun rekey(db: Long, key: ByteArray, length: Int): SQLCode

    fun releaseMemory(bytes: Int): Int

    fun reset(statement: Long): SQLCode

    fun resetAndClearBindings(statement: Long): SQLCode

    fun softHeapLimit64(): Long

    fun sql(statement: Long): String

    fun statementBusy(statement: Long): Int

    fun statementReadOnly(statement: Long): Int

    fun statementStatus(statement: Long, options: Int, reset: Boolean): Int

    fun step(statement: Long): SQLCode

    fun threadsafe(): Int

    fun totalChanges(db: Long): Int

    fun traceV2(db: Long, flag: Int)

    fun transactionState(db: Long): Int

    fun valueDup(value: Long): Long

    fun valueFree(value: Long)

    fun valueFromBind(value: Long): Int

    fun walAutoCheckpoint(db: Long, pages: Int): SQLCode

    fun walCheckpointV2(db: Long, name: String?, mode: Int): SQLCode
}
