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

import com.bloomberg.selekt.commons.forEachByPosition

private const val DEFAULT_SOFT_HEAP_LIMIT = 8 * 1024 * 1024L

/**
 * @since 0.27.0
 */
data class SQLiteConfiguration(
    val softHeapLimit: Long = DEFAULT_SOFT_HEAP_LIMIT
)

data class DatabaseHandle(
    val pointer: Long,
    val attachment: Any? = null
)

data class StatementHandle(
    val pointer: Long,
    val attachment: Any? = null
)

data class BlobHandle(
    val pointer: Long,
    val attachment: Any? = null
)

@Suppress(
    "Detekt.ComplexInterface",
    "Detekt.LongParameterList",
    "Detekt.TooManyFunctions"
) // Mirrors the SQLite3 C-api.
interface IExternalSQLite {
    fun newDatabaseHandle(pointer: Long): DatabaseHandle = DatabaseHandle(pointer)

    fun newStatementHandle(pointer: Long): StatementHandle = StatementHandle(pointer)

    fun newBlobHandle(pointer: Long): BlobHandle = BlobHandle(pointer)

    fun bindBlob(statement: Long, index: Int, blob: ByteArray, length: Int): SQLCode

    fun bindBlob(
        statement: StatementHandle,
        index: Int,
        blob: ByteArray,
        length: Int
    ): SQLCode = bindBlob(statement.pointer, index, blob, length)

    fun bindDouble(statement: Long, index: Int, value: Double): SQLCode

    fun bindDouble(
        statement: StatementHandle,
        index: Int,
        value: Double
    ): SQLCode =
        bindDouble(statement.pointer, index, value)

    fun bindInt(statement: Long, index: Int, value: Int): SQLCode

    fun bindInt(
        statement: StatementHandle,
        index: Int,
        value: Int
    ): SQLCode = bindInt(statement.pointer, index, value)

    fun bindInt64(statement: Long, index: Int, value: Long): SQLCode

    fun bindInt64(
        statement: StatementHandle,
        index: Int,
        value: Long
    ): SQLCode =
        bindInt64(statement.pointer, index, value)

    fun bindNull(statement: Long, index: Int): SQLCode

    fun bindNull(
        statement: StatementHandle,
        index: Int
    ): SQLCode = bindNull(statement.pointer, index)

    fun bindParameterCount(statement: Long): Int

    fun bindParameterCount(statement: StatementHandle): Int = bindParameterCount(statement.pointer)

    fun bindParameterIndex(statement: Long, name: String): Int

    fun bindParameterIndex(
        statement: StatementHandle,
        name: String
    ): Int = bindParameterIndex(statement.pointer, name)

    fun bindText(statement: Long, index: Int, value: String): SQLCode

    fun bindText(
        statement: StatementHandle,
        index: Int,
        value: String
    ): SQLCode = bindText(statement.pointer, index, value)

    fun bindZeroBlob(statement: Long, index: Int, length: Int): SQLCode

    fun bindZeroBlob(
        statement: StatementHandle,
        index: Int,
        length: Int
    ): SQLCode =
        bindZeroBlob(statement.pointer, index, length)

    fun <T> withScopedArena(block: () -> T): T = block()

    /**
     * Bind all arguments to a prepared statement.
     *
     * @param args values to bind at 1-based positions.
     */
    fun bindRow(statement: Long, args: Array<out Any?>): SQLCode {
        args.forEachByPosition { arg, position ->
            val result = when (arg) {
                is String -> bindText(statement, position, arg)
                is Int -> bindInt(statement, position, arg)
                null -> bindNull(statement, position)
                is Long -> bindInt64(statement, position, arg)
                is Double -> bindDouble(statement, position, arg)
                is ByteArray -> bindBlob(statement, position, arg, arg.size)
                else -> throw IllegalArgumentException("Cannot bind arg of class ${arg.javaClass} at position $position.")
            }
            if (result != SQL_OK) {
                return result
            }
        }
        return SQL_OK
    }

    fun bindRow(statement: StatementHandle, args: Array<out Any?>): SQLCode {
        args.forEachByPosition { arg, position ->
            val result = when (arg) {
                is String -> bindText(statement, position, arg)
                is Int -> bindInt(statement, position, arg)
                null -> bindNull(statement, position)
                is Long -> bindInt64(statement, position, arg)
                is Double -> bindDouble(statement, position, arg)
                is ByteArray -> bindBlob(statement, position, arg, arg.size)
                else -> throw IllegalArgumentException("Cannot bind arg of class ${arg.javaClass} at position $position.")
            }
            if (result != SQL_OK) {
                return result
            }
        }
        return SQL_OK
    }

    fun bindRowTyped(
        statement: Long,
        tags: ByteArray,
        ints: IntArray,
        longs: LongArray,
        doubles: DoubleArray,
        objects: Array<out Any?>,
        size: Int
    ): SQLCode {
        for (i in 0 until size) {
            val position = i + 1
            val result = when (tags[i]) {
                1.toByte() -> bindInt(statement, position, ints[i])
                2.toByte() -> bindInt64(statement, position, longs[i])
                3.toByte() -> bindDouble(statement, position, doubles[i])
                4.toByte() -> {
                    val obj = objects[i]
                    when (obj) {
                        is String -> bindText(statement, position, obj)
                        is ByteArray -> bindBlob(statement, position, obj, obj.size)
                        else -> bindNull(statement, position)
                    }
                }
                else -> bindNull(statement, position)
            }
            if (result != SQL_OK) {
                return result
            }
        }
        return SQL_OK
    }

    fun bindRowTyped(
        statement: StatementHandle,
        tags: ByteArray,
        ints: IntArray,
        longs: LongArray,
        doubles: DoubleArray,
        objects: Array<out Any?>,
        size: Int
    ): SQLCode {
        for (i in 0 until size) {
            val position = i + 1
            val result = when (tags[i]) {
                1.toByte() -> bindInt(statement, position, ints[i])
                2.toByte() -> bindInt64(statement, position, longs[i])
                3.toByte() -> bindDouble(statement, position, doubles[i])
                4.toByte() -> {
                    val obj = objects[i]
                    when (obj) {
                        is String -> bindText(statement, position, obj)
                        is ByteArray -> bindBlob(statement, position, obj, obj.size)
                        else -> bindNull(statement, position)
                    }
                }
                else -> bindNull(statement, position)
            }
            if (result != SQL_OK) {
                return result
            }
        }
        return SQL_OK
    }

    fun blobBytes(blob: Long): Int

    fun blobBytes(blob: BlobHandle): Int = blobBytes(blob.pointer)

    fun blobClose(blob: Long): SQLCode

    fun blobClose(
        blob: BlobHandle
    ): SQLCode = blobClose(blob.pointer)

    fun blobOpen(
        db: Long,
        name: String,
        table: String,
        column: String,
        row: Long,
        flags: Int,
        holder: LongArray
    ): SQLCode

    fun blobOpen(
        db: DatabaseHandle,
        name: String,
        table: String,
        column: String,
        row: Long,
        flags: Int,
        holder: LongArray
    ): SQLCode = blobOpen(db.pointer, name, table, column, row, flags, holder)

    fun blobRead(
        blob: Long,
        offset: Int,
        destination: ByteArray,
        destinationOffset: Int,
        length: Int
    ): SQLCode

    fun blobRead(
        blob: BlobHandle,
        offset: Int,
        destination: ByteArray,
        destinationOffset: Int,
        length: Int
    ): SQLCode = blobRead(blob.pointer, offset, destination, destinationOffset, length)

    fun blobReopen(blob: Long, row: Long): SQLCode

    fun blobReopen(
        blob: BlobHandle,
        row: Long
    ): SQLCode = blobReopen(blob.pointer, row)

    fun blobWrite(
        blob: Long,
        offset: Int,
        source: ByteArray,
        sourceOffset: Int,
        length: Int
    ): SQLCode

    fun blobWrite(
        blob: BlobHandle,
        offset: Int,
        source: ByteArray,
        sourceOffset: Int,
        length: Int
    ): SQLCode = blobWrite(blob.pointer, offset, source, sourceOffset, length)

    fun busyTimeout(db: Long, millis: Int): SQLCode

    fun busyTimeout(
        db: DatabaseHandle,
        millis: Int
    ): SQLCode = busyTimeout(db.pointer, millis)

    fun changes(db: Long): Int

    fun changes(db: DatabaseHandle): Int = changes(db.pointer)

    fun clearBindings(statement: Long): SQLCode

    fun clearBindings(
        statement: StatementHandle
    ): SQLCode = clearBindings(statement.pointer)

    fun closeV2(db: Long): SQLCode

    fun closeV2(
        db: DatabaseHandle
    ): SQLCode = closeV2(db.pointer)

    fun columnBlob(statement: Long, index: Int): ByteArray?

    fun columnBlob(
        statement: StatementHandle,
        index: Int
    ): ByteArray? = columnBlob(statement.pointer, index)

    fun columnCount(statement: Long): Int

    fun columnCount(statement: StatementHandle): Int = columnCount(statement.pointer)

    fun columnDouble(statement: Long, index: Int): Double

    fun columnDouble(
        statement: StatementHandle,
        index: Int
    ): Double = columnDouble(statement.pointer, index)

    fun columnInt(statement: Long, index: Int): Int

    fun columnInt(
        statement: StatementHandle,
        index: Int
    ): Int = columnInt(statement.pointer, index)

    fun columnInt64(statement: Long, index: Int): Long

    fun columnInt64(
        statement: StatementHandle,
        index: Int
    ): Long = columnInt64(statement.pointer, index)

    fun columnName(statement: Long, index: Int): String

    fun columnName(
        statement: StatementHandle,
        index: Int
    ): String = columnName(statement.pointer, index)

    fun columnText(statement: Long, index: Int): String

    fun columnText(
        statement: StatementHandle,
        index: Int
    ): String = columnText(statement.pointer, index)

    fun columnType(statement: Long, index: Int): SQLDataType

    fun columnType(
        statement: StatementHandle,
        index: Int
    ): SQLDataType = columnType(statement.pointer, index)

    fun columnValue(statement: Long, index: Int): Long

    fun columnValue(
        statement: StatementHandle,
        index: Int
    ): Long = columnValue(statement.pointer, index)

    fun commitHook(db: Long, enabled: Boolean, listener: SQLCommitListener?): SQLCode

    fun commitHook(db: DatabaseHandle, enabled: Boolean, listener: SQLCommitListener?): SQLCode =
        commitHook(db.pointer, enabled, listener)

    fun databaseConfig(db: Long, op: Int, value: Int): Int

    fun databaseConfig(
        db: DatabaseHandle,
        op: Int,
        value: Int
    ): Int = databaseConfig(db.pointer, op, value)

    fun databaseHandle(statement: Long): Long

    fun databaseHandle(statement: StatementHandle): Long = databaseHandle(statement.pointer)

    fun databaseReadOnly(db: Long, name: String): Int

    fun databaseReadOnly(
        db: DatabaseHandle,
        name: String
    ): Int = databaseReadOnly(db.pointer, name)

    fun databaseReleaseMemory(db: Long): Int

    fun databaseReleaseMemory(db: DatabaseHandle): Int = databaseReleaseMemory(db.pointer)

    fun databaseStatus(
        db: Long,
        options: Int,
        reset: Boolean,
        holder: IntArray
    ): SQLCode

    fun errorCode(db: Long): Int

    fun errorCode(db: DatabaseHandle): Int = errorCode(db.pointer)

    fun errorMessage(db: Long): String

    fun errorMessage(db: DatabaseHandle): String = errorMessage(db.pointer)

    fun exec(db: Long, query: String): SQLCode

    fun exec(
        db: DatabaseHandle,
        query: String
    ): SQLCode = exec(db.pointer, query)

    fun expandedSql(statement: Long): String

    fun expandedSql(statement: StatementHandle): String = expandedSql(statement.pointer)

    fun extendedErrorCode(db: Long): Int

    fun extendedErrorCode(db: DatabaseHandle): Int = extendedErrorCode(db.pointer)

    fun extendedResultCodes(db: Long, onOff: Int): Int

    fun extendedResultCodes(
        db: DatabaseHandle,
        onOff: Int
    ): Int = extendedResultCodes(db.pointer, onOff)

    fun finalize(statement: Long): SQLCode

    fun finalize(statement: StatementHandle): SQLCode = finalize(statement.pointer)

    fun getAutocommit(db: Long): Int

    fun getAutocommit(db: DatabaseHandle): Int = getAutocommit(db.pointer)

    fun gitCommit(): String

    fun hardHeapLimit64(): Long

    fun interrupt(db: Long)

    fun interrupt(db: DatabaseHandle): Unit = interrupt(db.pointer)

    fun isInterrupted(db: Long): Int

    fun isInterrupted(db: DatabaseHandle): Int = isInterrupted(db.pointer)

    fun key(db: Long, key: ByteArray, length: Int): SQLCode

    fun key(
        db: DatabaseHandle,
        key: ByteArray,
        length: Int
    ): SQLCode = key(db.pointer, key, length)

    fun keyConventionally(db: Long, key: ByteArray, length: Int): SQLCode

    fun keyConventionally(db: DatabaseHandle, key: ByteArray, length: Int): SQLCode =
        keyConventionally(db.pointer, key, length)

    fun keywordCount(): Int

    fun lastInsertRowId(db: Long): Long

    fun lastInsertRowId(db: DatabaseHandle): Long = lastInsertRowId(db.pointer)

    fun libVersion(): String

    fun libVersionNumber(): Int

    fun memoryUsed(): Long

    fun openV2(path: String, flags: Int, dbHolder: LongArray): SQLCode

    fun prepareV2(db: Long, sql: String, length: Int, statementHolder: LongArray): SQLCode

    fun prepareV2(
        db: DatabaseHandle,
        sql: String,
        length: Int,
        statementHolder: LongArray
    ): SQLCode =
        prepareV2(db.pointer, sql, length, statementHolder)

    fun progressHandler(db: Long, instructionCount: Int, handler: SQLProgressHandler?)

    fun progressHandler(db: DatabaseHandle, instructionCount: Int, handler: SQLProgressHandler?) =
        progressHandler(db.pointer, instructionCount, handler)

    fun rawKey(db: Long, key: ByteArray, length: Int): SQLCode

    fun rawKey(
        db: DatabaseHandle,
        key: ByteArray,
        length: Int
    ): SQLCode = rawKey(db.pointer, key, length)

    fun rekey(db: Long, key: ByteArray, length: Int): SQLCode

    fun rekey(
        db: DatabaseHandle,
        key: ByteArray,
        length: Int
    ): SQLCode = rekey(db.pointer, key, length)

    fun releaseMemory(bytes: Int): Int

    fun reset(statement: Long): SQLCode

    fun reset(statement: StatementHandle): SQLCode = reset(statement.pointer)

    fun resetAndClearBindings(statement: Long): SQLCode

    fun resetAndClearBindings(statement: StatementHandle): SQLCode = resetAndClearBindings(statement.pointer)

    fun softHeapLimit64(): Long

    fun sql(statement: Long): String

    fun sql(statement: StatementHandle): String = sql(statement.pointer)

    fun statementBusy(statement: Long): Int

    fun statementBusy(statement: StatementHandle): Int = statementBusy(statement.pointer)

    fun statementReadOnly(statement: Long): Int

    fun statementReadOnly(statement: StatementHandle): Int = statementReadOnly(statement.pointer)

    fun statementStatus(statement: Long, options: Int, reset: Boolean): Int

    fun statementStatus(
        statement: StatementHandle,
        options: Int,
        reset: Boolean
    ): Int =
        statementStatus(statement.pointer, options, reset)

    fun step(statement: Long): SQLCode

    fun step(statement: StatementHandle): SQLCode = step(statement.pointer)

    fun threadsafe(): Int

    fun totalChanges(db: Long): Int

    fun totalChanges(db: DatabaseHandle): Int = totalChanges(db.pointer)

    fun traceV2(db: Long, flag: Int)

    fun traceV2(
        db: DatabaseHandle,
        flag: Int
    ): Unit = traceV2(db.pointer, flag)

    fun transactionState(db: Long): Int

    fun transactionState(db: DatabaseHandle): Int = transactionState(db.pointer)

    fun valueDup(value: Long): Long

    fun valueFree(value: Long)

    fun valueFromBind(value: Long): Int

    fun walAutoCheckpoint(db: Long, pages: Int): SQLCode

    fun walAutoCheckpoint(
        db: DatabaseHandle,
        pages: Int
    ): SQLCode = walAutoCheckpoint(db.pointer, pages)

    fun walCheckpointV2(db: Long, name: String?, mode: Int): SQLCode

    fun walCheckpointV2(
        db: DatabaseHandle,
        name: String?,
        mode: Int
    ): SQLCode = walCheckpointV2(db.pointer, name, mode)
}
