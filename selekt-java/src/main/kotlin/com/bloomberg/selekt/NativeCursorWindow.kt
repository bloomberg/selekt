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

package com.bloomberg.selekt

import java.lang.ref.Cleaner
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import javax.annotation.concurrent.NotThreadSafe
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private const val NOT_MUTABLE_MESSAGE = "NativeCursorWindow is populated natively and is immutable."
private const val SLOT_SIZE_BYTES = 1 + Long.SIZE_BYTES
private val EMPTY_BYTES = ByteArray(0)

internal const val NOT_COUNTED = -1

private class FreeCursorWindowAction(
    private val buffer: ByteBuffer,
    private val sqlite: SQLite
) : Runnable {
    override fun run() {
        sqlite.freeCursorWindow(buffer)
    }
}

@Suppress("Detekt.MethodOverloading")
@NotThreadSafe
internal class NativeCursorWindow(
    private val buffer: ByteBuffer,
    private val sqlite: SQLite,
    private val columnCount: Int
) : ICursorWindow {
    private companion object {
        val cleaner: Cleaner = Cleaner.create()
    }

    private val cleanable = cleaner.register(this, FreeCursorWindowAction(buffer, sqlite))

    private var closed = false

    private var stringBytes = EMPTY_BYTES

    private val rowCount: Int

    val totalCount: Int

    init {
        buffer.order(ByteOrder.nativeOrder())
        rowCount = buffer.getInt(0)
        totalCount = buffer.getInt(Int.SIZE_BYTES)
    }

    override fun allocateRow(): Boolean = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun clear(): Unit = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun close() {
        if (!closed) {
            closed = true
            stringBytes = EMPTY_BYTES
            cleanable.clean()
        }
    }

    override fun getBlob(row: Int, column: Int): ByteArray? {
        val offset = slotOffset(row, column)
        return when (tagAt(offset)) {
            SQL_BLOB, SQL_TEXT -> readBytes(offset)
            SQL_NULL -> null
            else -> error("Unable to convert column $column of row $row to a ByteArray.")
        }
    }

    override fun getDouble(row: Int, column: Int): Double = slotOffset(row, column).let { offset ->
        when (tagAt(offset)) {
            SQL_FLOAT -> buffer.getDouble(offset + 1)
            SQL_INTEGER -> buffer.getLong(offset + 1).toDouble()
            SQL_TEXT -> readString(offset).toDouble()
            else -> 0.0
        }
    }

    override fun getFloat(row: Int, column: Int): Float = getDouble(row, column).toFloat()

    override fun getInt(row: Int, column: Int): Int = slotOffset(row, column).let { offset ->
        when (tagAt(offset)) {
            SQL_INTEGER -> buffer.getLong(offset + 1).toInt()
            SQL_FLOAT -> buffer.getDouble(offset + 1).roundToInt()
            SQL_TEXT -> readString(offset).toInt()
            else -> 0
        }
    }

    override fun getLong(row: Int, column: Int): Long = slotOffset(row, column).let { offset ->
        when (tagAt(offset)) {
            SQL_INTEGER -> buffer.getLong(offset + 1)
            SQL_FLOAT -> buffer.getDouble(offset + 1).roundToLong()
            SQL_TEXT -> readString(offset).toLong()
            else -> 0L
        }
    }

    override fun getShort(row: Int, column: Int): Short = getInt(row, column).toShort()

    override fun getString(row: Int, column: Int): String? = slotOffset(row, column).let { offset ->
        when (tagAt(offset)) {
            SQL_TEXT -> readString(offset)
            SQL_INTEGER -> buffer.getLong(offset + 1).toString()
            SQL_FLOAT -> buffer.getDouble(offset + 1).toString()
            else -> null
        }
    }

    override fun isNull(row: Int, column: Int) = SQL_NULL == tagAt(slotOffset(row, column))

    override fun numberOfRows() = rowCount

    override fun put(value: ByteArray?): Boolean = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun put(value: Double): Boolean = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun put(value: Float): Boolean = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun put(value: Int): Boolean = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun put(value: Long): Boolean = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun put(value: Short): Boolean = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun put(value: String): Boolean = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun putNull(): Boolean = throw UnsupportedOperationException(NOT_MUTABLE_MESSAGE)

    override fun type(row: Int, column: Int): ColumnType = ColumnType.toColumnType(tagAt(slotOffset(row, column)))

    private fun tagAt(offset: Int) = buffer[offset].toInt()

    private fun rowOffset(row: Int) = buffer.getInt(buffer.capacity() - rowCount * Int.SIZE_BYTES + row * Int.SIZE_BYTES)

    private fun slotOffset(row: Int, column: Int): Int {
        check(!closed) { "Cursor window is closed." }
        if (row !in 0 until rowCount) {
            throw IndexOutOfBoundsException("Row $row is outside a cursor window containing $rowCount rows.")
        }
        if (column !in 0 until columnCount) {
            throw IndexOutOfBoundsException("Column $column is outside a cursor containing $columnCount columns.")
        }
        return rowOffset(row) + column * SLOT_SIZE_BYTES
    }

    private fun readBytes(offset: Int): ByteArray {
        val length = buffer.getInt(offset + 1)
        val bytes = ByteArray(length)
        buffer.get(buffer.getInt(offset + 1 + Int.SIZE_BYTES), bytes, 0, length)
        return bytes
    }

    private fun readString(offset: Int): String {
        val length = buffer.getInt(offset + 1)
        if (stringBytes.size < length) {
            stringBytes = ByteArray(length)
        }
        buffer.get(buffer.getInt(offset + 1 + Int.SIZE_BYTES), stringBytes, 0, length)
        return String(stringBytes, 0, length, StandardCharsets.UTF_8)
    }
}
