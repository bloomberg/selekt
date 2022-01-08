/*
 * Copyright 2022 Bloomberg Finance L.P.
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

import java.util.concurrent.atomic.AtomicBoolean

private const val DEFAULT_SOFT_HEAP_LIMIT = 8 * 1024 * 1024L

data class SQLiteConfiguration(
    val softHeapLimit: Long = DEFAULT_SOFT_HEAP_LIMIT
)

fun externalSQLiteSingleton(
    configuration: SQLiteConfiguration = SQLiteConfiguration(),
    loader: () -> Unit = { System.loadLibrary("selekt") }
) = ExternalSQLite.Singleton(configuration, loader)

@Suppress("Detekt.LongParameterList", "Detekt.TooManyFunctions") // Mirrors the SQLite3 C-api.
class ExternalSQLite private constructor(
    configuration: SQLiteConfiguration,
    loader: () -> Unit
) {
    init {
        loader()
        nativeInit(configuration.softHeapLimit)
    }

    internal object Singleton {
        private val isInitialised = AtomicBoolean(false)

        operator fun invoke(configuration: SQLiteConfiguration, loader: () -> Unit): ExternalSQLite {
            check(!isInitialised.getAndSet(true)) { "Singleton is already initialised." }
            return ExternalSQLite(configuration, loader)
        }
    }

    external fun bindBlob(statement: Long, index: Int, blob: ByteArray, length: Int): SQLCode

    external fun bindDouble(statement: Long, index: Int, value: Double): SQLCode

    external fun bindInt(statement: Long, index: Int, value: Int): SQLCode

    external fun bindInt64(statement: Long, index: Int, value: Long): SQLCode

    external fun bindNull(statement: Long, index: Int): SQLCode

    external fun bindParameterCount(statement: Long): Int

    external fun bindText(statement: Long, index: Int, value: String): SQLCode

    external fun bindZeroBlob(statement: Long, index: Int, length: Int): SQLCode

    external fun blobBytes(blob: Long): Int

    external fun blobClose(blob: Long): SQLCode

    external fun blobOpen(
        db: Long,
        name: String,
        table: String,
        column: String,
        row: Long,
        flags: Int,
        holder: LongArray
    ): SQLCode

    external fun blobRead(
        blob: Long,
        offset: Int,
        destination: ByteArray,
        destinationOffset: Int,
        length: Int
    ): SQLCode

    external fun blobReopen(blob: Long, row: Long): SQLCode

    external fun blobWrite(
        blob: Long,
        offset: Int,
        source: ByteArray,
        sourceOffset: Int,
        length: Int
    ): SQLCode

    external fun busyTimeout(db: Long, millis: Int): SQLCode

    external fun changes(db: Long): Int

    external fun clearBindings(statement: Long): SQLCode

    external fun closeV2(db: Long): SQLCode

    external fun columnBlob(statement: Long, index: Int): ByteArray?

    external fun columnCount(statement: Long): Int

    external fun columnDouble(statement: Long, index: Int): Double

    external fun columnInt(statement: Long, index: Int): Int

    external fun columnInt64(statement: Long, index: Int): Long

    external fun columnName(statement: Long, index: Int): String

    external fun columnText(statement: Long, index: Int): String

    external fun columnType(statement: Long, index: Int): SQLDataType

    external fun columnValue(statement: Long, index: Int): Long

    external fun databaseHandle(statement: Long): Long

    external fun databaseReadOnly(db: Long, name: String): Int

    external fun databaseReleaseMemory(db: Long): Int

    external fun databaseStatus(
        db: Long,
        options: Int,
        reset: Boolean,
        holder: IntArray
    ): SQLCode

    external fun errorCode(db: Long): Int

    external fun errorMessage(db: Long): String

    external fun exec(db: Long, query: String): SQLCode

    external fun expandedSql(statement: Long): String

    external fun extendedErrorCode(db: Long): Int

    external fun extendedResultCodes(db: Long, onOff: Int): Int

    external fun finalize(statement: Long): SQLCode

    external fun getAutocommit(db: Long): Int

    external fun gitCommit(): String

    external fun hardHeapLimit64(): Long

    external fun key(db: Long, key: ByteArray, length: Int): SQLCode

    external fun keyConventionally(db: Long, key: ByteArray, length: Int): SQLCode

    external fun keywordCount(): Int

    external fun lastInsertRowId(db: Long): Long

    external fun libVersion(): String

    external fun libVersionNumber(): Int

    external fun memoryUsed(): Long

    external fun openV2(path: String, flags: Int, dbHolder: LongArray): SQLCode

    external fun prepareV2(db: Long, sql: String, length: Int, statementHolder: LongArray): SQLCode

    external fun rawKey(db: Long, key: ByteArray, length: Int): SQLCode

    external fun rekey(db: Long, key: ByteArray, length: Int): SQLCode

    external fun releaseMemory(bytes: Int): Int

    external fun reset(statement: Long): SQLCode

    external fun resetAndClearBindings(statement: Long): SQLCode

    external fun softHeapLimit64(): Long

    external fun sql(statement: Long): String

    external fun statementBusy(statement: Long): Int

    external fun statementReadOnly(statement: Long): Int

    external fun statementStatus(statement: Long, options: Int, reset: Boolean): Int

    external fun step(statement: Long): SQLCode

    external fun threadsafe(): Int

    external fun totalChanges(db: Long): Int

    external fun traceV2(db: Long, flag: Int)

    external fun transactionState(db: Long): Int

    external fun valueDup(value: Long): Long

    external fun valueFree(value: Long)

    external fun valueFromBind(value: Long): Int

    external fun walAutoCheckpoint(db: Long, pages: Int): SQLCode

    external fun walCheckpointV2(db: Long, name: String?, mode: Int): SQLCode

    private external fun nativeInit(softHeapLimit: Long)
}
