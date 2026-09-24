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
import java.nio.ByteBuffer
import javax.annotation.concurrent.NotThreadSafe

private const val DIRECT_ASCII_BIND_MAX_LENGTH = 256
private const val DIRECT_ASCII_COLUMN_MAX_LENGTH = 64
// End-to-end scan benchmarks show that packing wins from four adjacent short text columns onward.
private const val MIN_PACKED_TEXT_COLUMNS = 4
private const val MAX_PACKED_TEXT_COLUMNS = 16
private const val PACKED_TEXT_NULL = 0xff
private const val PACKED_TEXT_EXACT = 0xfe
private const val PACKED_TEXT_DEFERRED = 0xfd

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
) : IExternalSQLite, IBatchedTextValuesSQLite, INativeCursorWindowSQLite {
    private val cursorWindowOwnership = CursorWindowOwnershipRegistry()

    @NotThreadSafe
    private class StatementAttachment {
        var packedText = ByteArray(0)
            private set
        var packedTextReadsEnabled = true
            private set

        fun prepareTextBatch(count: Int) {
            val byteCapacity = count * (DIRECT_ASCII_COLUMN_MAX_LENGTH + 1)
            if (packedText.size < byteCapacity) {
                packedText = ByteArray(byteCapacity)
            }
        }

        fun disablePackedTextReads() {
            packedTextReadsEnabled = false
        }
    }

    init {
        loader()
        nativeInit(configuration.softHeapLimit)
    }

    internal object Singleton {
        @Volatile
        private var instance: IExternalSQLite? = null

        operator fun invoke(
            configuration: SQLiteConfiguration,
            loader: () -> Unit
        ): IExternalSQLite = instance ?: synchronized(this) {
            instance ?: ExternalSQLite(configuration, loader).also { instance = it }
        }
    }

    override fun newStatementHandle(pointer: Long): StatementHandle =
        StatementHandle(pointer, if (pointer == 0L) null else StatementAttachment())

    override fun useBatchedTextValues(statement: StatementHandle): Boolean =
        (statement.attachment as? StatementAttachment)?.packedTextReadsEnabled == true

    external override fun allocateSecret(size: Int): Long

    external override fun freeSecret(pointer: Long, size: Int)

    external override fun storeSecret(pointer: Long, capacity: Int, source: ByteArray, length: Int)

    external override fun bindBlob(statement: Long, index: Int, blob: ByteArray, length: Int): SQLCode

    external override fun bindDouble(statement: Long, index: Int, value: Double): SQLCode

    external override fun bindInt(statement: Long, index: Int, value: Int): SQLCode

    external override fun bindInt64(statement: Long, index: Int, value: Long): SQLCode

    external override fun bindNull(statement: Long, index: Int): SQLCode

    external override fun bindParameterCount(statement: Long): Int

    external override fun bindParameterIndex(statement: Long, name: String): Int

    override fun bindText(statement: Long, index: Int, value: String): SQLCode =
        bindTextUtf8(statement, index, value.toByteArray(Charsets.UTF_8))

    override fun bindTextAscii(statement: Long, index: Int, value: String): SQLCode {
        if (value.length <= DIRECT_ASCII_BIND_MAX_LENGTH) {
            return bindTextAsciiDirect(statement, index, value)
        }
        val bytes = value.toByteArray(Charsets.UTF_8)
        // For well-formed UTF-16, UTF-8 byte length equals UTF-16 length only for ASCII.
        // String.toByteArray replaces malformed surrogates with one-byte '?', matching bindText.
        return if (bytes.size == value.length) {
            bindTextUtf8(statement, index, bytes)
        } else {
            SQL_MISMATCH
        }
    }

    private external fun bindTextAsciiDirect(statement: Long, index: Int, value: String): SQLCode

    private external fun bindTextUtf8(statement: Long, index: Int, value: ByteArray): SQLCode

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

    external override fun columnBytes(statement: Long, index: Int): Int

    external override fun columnCount(statement: Long): Int

    external override fun columnDouble(statement: Long, index: Int): Double

    external override fun columnInt(statement: Long, index: Int): Int

    external override fun columnInt64(statement: Long, index: Int): Long

    external override fun columnName(statement: Long, index: Int): String

    override fun columnText(statement: Long, index: Int): String? = when (
        val value = columnTextOptimized(statement, index)
    ) {
        is String -> value
        is ByteArray -> value.toString(Charsets.UTF_8)
        null -> null
        else -> error("Unexpected native text representation: ${value::class.java.name}")
    }

    private external fun columnTextOptimized(statement: Long, index: Int): Any?

    override fun columnTexts(
        statement: StatementHandle,
        firstIndex: Int,
        destination: Array<String?>
    ) {
        if (destination.isEmpty()) {
            return
        }
        val attachment = statement.attachment as? StatementAttachment
        if (attachment == null) {
            super<IExternalSQLite>.columnTexts(statement, firstIndex, destination)
            return
        }
        if (!attachment.packedTextReadsEnabled) {
            for (offset in destination.indices) {
                destination[offset] = columnText(statement.pointer, firstIndex + offset)
            }
            return
        }

        var destinationOffset = 0
        while (destinationOffset < destination.size) {
            if (!attachment.packedTextReadsEnabled) {
                for (offset in destinationOffset until destination.size) {
                    destination[offset] = columnText(statement.pointer, firstIndex + offset)
                }
                return
            }
            val columnIndex = firstIndex + destinationOffset
            val count = minOf(MAX_PACKED_TEXT_COLUMNS, destination.size - destinationOffset)
            if (count < MIN_PACKED_TEXT_COLUMNS) {
                repeat(count) { offset ->
                    destination[destinationOffset + offset] = columnText(statement.pointer, columnIndex + offset)
                }
            } else {
                columnTextsAsciiPacked(statement.pointer, columnIndex, count, destination, destinationOffset, attachment)
            }
            destinationOffset += count
        }
    }

    private fun columnTextsAsciiPacked(
        statement: Long,
        firstIndex: Int,
        count: Int,
        destination: Array<String?>,
        destinationOffset: Int,
        attachment: StatementAttachment
    ) {
        attachment.prepareTextBatch(count)
        columnTextsAsciiPacked(
            statement,
            firstIndex,
            count,
            attachment.packedText
        )
        decodePackedTextValues(statement, firstIndex, count, destination, destinationOffset, null, attachment)
    }

    private fun decodePackedTextValues(
        statement: Long,
        firstIndex: Int,
        count: Int,
        destination: Array<String?>,
        destinationOffset: Int,
        loaded: BooleanArray?,
        attachment: StatementAttachment
    ) {
        var packedOffset = count
        for (offset in 0 until count) {
            val byteLength = attachment.packedText[offset].toInt() and 0xff
            destination[destinationOffset + offset] = when (byteLength) {
                PACKED_TEXT_NULL -> {
                    loaded?.set(destinationOffset + offset, true)
                    null
                }
                PACKED_TEXT_DEFERRED -> {
                    checkNotNull(loaded) { "Deferred text is only valid for storage-class-preserving reads." }
                    loaded[destinationOffset + offset] = false
                    null
                }
                PACKED_TEXT_EXACT -> {
                    loaded?.set(destinationOffset + offset, true)
                    attachment.disablePackedTextReads()
                    columnText(statement, firstIndex + offset)
                }
                else -> {
                    loaded?.set(destinationOffset + offset, true)
                    String(attachment.packedText, packedOffset, byteLength, Charsets.ISO_8859_1).also {
                        packedOffset += byteLength
                    }
                }
            }
        }
    }

    private external fun columnTextsAsciiPacked(
        statement: Long,
        firstIndex: Int,
        count: Int,
        packedText: ByteArray
    )

    override fun columnTextValues(
        statement: StatementHandle,
        firstIndex: Int,
        destination: Array<String?>,
        loaded: BooleanArray
    ) {
        require(destination.size == loaded.size) { "Text values and loaded flags must have equal sizes." }
        if (destination.isEmpty()) {
            return
        }
        val attachment = statement.attachment as? StatementAttachment
        if (
            destination.size !in MIN_PACKED_TEXT_COLUMNS..MAX_PACKED_TEXT_COLUMNS ||
            attachment == null ||
            !attachment.packedTextReadsEnabled
        ) {
            super<IExternalSQLite>.columnTextValues(statement, firstIndex, destination, loaded)
            return
        }
        attachment.prepareTextBatch(destination.size)
        columnTextValuesAsciiPacked(statement.pointer, firstIndex, destination.size, attachment.packedText)
        decodePackedTextValues(statement.pointer, firstIndex, destination.size, destination, 0, loaded, attachment)
    }

    private external fun columnTextValuesAsciiPacked(
        statement: Long,
        firstIndex: Int,
        count: Int,
        packedText: ByteArray
    )

    external override fun columnType(statement: Long, index: Int): SQLDataType

    external override fun columnValue(statement: Long, index: Int): Long

    external override fun commitHook(db: Long, enabled: Boolean, listener: SQLCommitListener?): SQLCode

    external override fun databaseConfig(db: Long, op: Int, value: Int): Int

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

    override fun fillCursorWindow(
        statement: Long,
        startRow: Int,
        maxRows: Int,
        countAllRows: Boolean
    ): ByteBuffer? = fillCursorWindow(statement, startRow, maxRows, countAllRows, Int.MAX_VALUE)

    override fun fillCursorWindow(
        statement: Long,
        startRow: Int,
        maxRows: Int,
        countAllRows: Boolean,
        maxBytes: Int
    ): ByteBuffer? = fillCursorWindowNative(statement, startRow, maxRows, countAllRows, maxBytes)?.let { buffer ->
        try {
            cursorWindowOwnership.register(buffer)
        } catch (@Suppress("TooGenericExceptionCaught") failure: Throwable) {
            val result = freeCursorWindowNative(buffer)
            if (result != SQL_OK) {
                failure.addSuppressed(IllegalStateException("Native cursor window ownership is inconsistent."))
            }
            throw failure
        }
    }

    private external fun fillCursorWindowNative(
        statement: Long,
        startRow: Int,
        maxRows: Int,
        countAllRows: Boolean,
        maxBytes: Int
    ): ByteBuffer?

    external override fun finalize(statement: Long): SQLCode

    override fun freeCursorWindow(buffer: ByteBuffer) {
        val ownedBuffer = cursorWindowOwnership.consume(buffer)
        check(freeCursorWindowNative(ownedBuffer) == SQL_OK) {
            "Native cursor window ownership is inconsistent."
        }
    }

    private external fun freeCursorWindowNative(buffer: ByteBuffer): SQLCode

    external override fun getAutocommit(db: Long): Int

    external override fun gitCommit(): String

    external override fun hardHeapLimit64(): Long

    external override fun interrupt(db: Long)

    external override fun isInterrupted(db: Long): Int

    external override fun key(db: Long, key: ByteArray, length: Int): SQLCode

    external override fun keyConventionally(db: Long, key: ByteArray, length: Int): SQLCode

    external override fun keyConventionallyAt(db: Long, pointer: Long, length: Int): SQLCode

    external override fun keywordCount(): Int

    external override fun lastInsertRowId(db: Long): Long

    external override fun libVersion(): String

    external override fun libVersionNumber(): Int

    external override fun memoryUsed(): Long

    external override fun openV2(path: String, flags: Int, dbHolder: LongArray): SQLCode

    external override fun prepareV2(db: Long, sql: String, length: Int, statementHolder: LongArray): SQLCode

    external override fun progressHandler(db: Long, instructionCount: Int, handler: SQLProgressHandler?)

    external override fun rawKey(db: Long, key: ByteArray, length: Int): SQLCode

    external override fun rawKeyAt(db: Long, pointer: Long, length: Int): SQLCode

    external override fun rekey(db: Long, key: ByteArray, length: Int): SQLCode

    external override fun rekeyAt(db: Long, pointer: Long, length: Int): SQLCode

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
