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

import com.bloomberg.selekt.commons.loadLibrary
import java.util.concurrent.atomic.AtomicBoolean

fun externalSQLiteSingleton() = externalSQLiteSingleton(SQLiteConfiguration())

fun externalSQLiteSingleton(
    configuration: SQLiteConfiguration = SQLiteConfiguration(),
    loader: () -> Unit = {
        loadLibrary(checkNotNull(ExternalSQLite::class.java.classLoader), "jni", "selekt")
    }
) = ExternalSQLite.Singleton(configuration, loader)

@Suppress("Detekt.LongParameterList", "Detekt.TooManyFunctions")
internal class ExternalSQLite(
    configuration: SQLiteConfiguration,
    loader: () -> Unit
) : IExternalSQLite {
    init {
        loader()
        nativeInit(configuration.softHeapLimit)
    }

    internal object Singleton {
        private val isInitialised = AtomicBoolean(false)

        operator fun invoke(configuration: SQLiteConfiguration, loader: () -> Unit): IExternalSQLite {
            check(!isInitialised.getAndSet(true)) { "Singleton is already initialised." }
            return ExternalSQLite(configuration, loader)
        }
    }

    external override fun bindBlob(statement: Long, index: Int, blob: ByteArray, length: Int): SQLCode

    external override fun bindDouble(statement: Long, index: Int, value: Double): SQLCode

    external override fun bindInt(statement: Long, index: Int, value: Int): SQLCode

    external override fun bindInt64(statement: Long, index: Int, value: Long): SQLCode

    external override fun bindNull(statement: Long, index: Int): SQLCode

    external override fun bindParameterCount(statement: Long): Int

    external override fun bindText(statement: Long, index: Int, value: String): SQLCode

    external override fun bindZeroBlob(statement: Long, index: Int, length: Int): SQLCode

    external override fun blobBytes(blob: Long): Int

    external override fun blobClose(blob: Long): SQLCode

    external override fun blobOpen(
        db: Long,
        name: String,
        table: String,
        column: String,
        row: Long,
        flags: Int,
        holder: LongArray
    ): SQLCode

    external override fun blobRead(
        blob: Long,
        offset: Int,
        destination: ByteArray,
        destinationOffset: Int,
        length: Int
    ): SQLCode

    external override fun blobReopen(blob: Long, row: Long): SQLCode

    external override fun blobWrite(
        blob: Long,
        offset: Int,
        source: ByteArray,
        sourceOffset: Int,
        length: Int
    ): SQLCode

    external override fun busyTimeout(db: Long, millis: Int): SQLCode

    external override fun changes(db: Long): Int

    external override fun clearBindings(statement: Long): SQLCode

    external override fun closeV2(db: Long): SQLCode

    external override fun columnBlob(statement: Long, index: Int): ByteArray?

    external override fun columnCount(statement: Long): Int

    external override fun columnDouble(statement: Long, index: Int): Double

    external override fun columnInt(statement: Long, index: Int): Int

    external override fun columnInt64(statement: Long, index: Int): Long

    external override fun columnName(statement: Long, index: Int): String

    external override fun columnText(statement: Long, index: Int): String

    external override fun columnType(statement: Long, index: Int): SQLDataType

    external override fun columnValue(statement: Long, index: Int): Long

    external override fun databaseHandle(statement: Long): Long

    external override fun databaseReadOnly(db: Long, name: String): Int

    external override fun databaseReleaseMemory(db: Long): Int

    external override fun databaseStatus(
        db: Long,
        options: Int,
        reset: Boolean,
        holder: IntArray
    ): SQLCode

    external override fun errorCode(db: Long): Int

    external override fun errorMessage(db: Long): String

    external override fun exec(db: Long, query: String): SQLCode

    external override fun expandedSql(statement: Long): String

    external override fun extendedErrorCode(db: Long): Int

    external override fun extendedResultCodes(db: Long, onOff: Int): Int

    external override fun finalize(statement: Long): SQLCode

    external override fun getAutocommit(db: Long): Int

    external override fun gitCommit(): String

    external override fun hardHeapLimit64(): Long

    external override fun key(db: Long, key: ByteArray, length: Int): SQLCode

    external override fun keyConventionally(db: Long, key: ByteArray, length: Int): SQLCode

    external override fun keywordCount(): Int

    external override fun lastInsertRowId(db: Long): Long

    external override fun libVersion(): String

    external override fun libVersionNumber(): Int

    external override fun memoryUsed(): Long

    external override fun openV2(path: String, flags: Int, dbHolder: LongArray): SQLCode

    external override fun prepareV2(db: Long, sql: String, length: Int, statementHolder: LongArray): SQLCode

    external override fun rawKey(db: Long, key: ByteArray, length: Int): SQLCode

    external override fun rekey(db: Long, key: ByteArray, length: Int): SQLCode

    external override fun releaseMemory(bytes: Int): Int

    external override fun reset(statement: Long): SQLCode

    external override fun resetAndClearBindings(statement: Long): SQLCode

    external override fun softHeapLimit64(): Long

    external override fun sql(statement: Long): String

    external override fun statementBusy(statement: Long): Int

    external override fun statementReadOnly(statement: Long): Int

    external override fun statementStatus(statement: Long, options: Int, reset: Boolean): Int

    external override fun step(statement: Long): SQLCode

    external override fun threadsafe(): Int

    external override fun totalChanges(db: Long): Int

    external override fun traceV2(db: Long, flag: Int)

    external override fun transactionState(db: Long): Int

    external override fun valueDup(value: Long): Long

    external override fun valueFree(value: Long)

    external override fun valueFromBind(value: Long): Int

    external override fun walAutoCheckpoint(db: Long, pages: Int): SQLCode

    external override fun walCheckpointV2(db: Long, name: String?, mode: Int): SQLCode

    private external fun nativeInit(softHeapLimit: Long)
}
