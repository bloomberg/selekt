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

import java.io.Closeable
import javax.annotation.concurrent.NotThreadSafe
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * A window of rows returned by a fill operation, carrying position and row-count metadata.
 *
 * A fully materialised page has `count == window.numberOfRows()`. A bounded single-window fill
 * can report the total result count separately, or use [NOT_COUNTED] when it stops early.
 */
internal data class CursorWindowPage(
    val window: ICursorWindow,
    val startPosition: Int,
    val count: Int
)

/**
 * @since 0.12.1
 */
@NotThreadSafe
@Suppress("Detekt.MethodOverloading")
internal class SimpleCursorWindow : ICursorWindow {
    private var rows = ArrayList<MutableList<Any?>>()

    override fun allocateRow() = rows.run { add(ArrayList(firstOrNull()?.size ?: INITIAL_COLUMN_CAPACITY)) }

    override fun clear() {
        rows = ArrayList()
    }

    override fun close() {
        clear()
    }

    override fun getBlob(row: Int, column: Int) = when (val value = get(row, column)) {
        is ByteArray -> value
        null -> null
        is String -> value.toByteArray(Charsets.UTF_8)
        else -> error("Unable to convert a ${value::class} to a ByteArray.")
    }

    override fun getDouble(row: Int, column: Int) = get(row, column).let {
        when (it) {
            null -> 0.0
            is Double -> it
            is Long -> it.toDouble()
            else -> it.toString().toDouble()
        }
    }

    override fun getFloat(row: Int, column: Int) = get(row, column).let {
        when (it) {
            null -> 0.0f
            is Double -> it.toFloat()
            else -> it.toString().toFloat()
        }
    }

    override fun getInt(row: Int, column: Int) = get(row, column).let {
        when (it) {
            null -> 0
            is Long -> it.toInt()
            is Double -> it.roundToInt()
            else -> it.toString().toInt()
        }
    }

    override fun getLong(row: Int, column: Int) = get(row, column).let {
        when (it) {
            null -> 0L
            is Long -> it
            is Double -> it.roundToLong()
            else -> it.toString().toLong()
        }
    }

    override fun getShort(row: Int, column: Int) = get(row, column).let {
        when (it) {
            null -> 0
            is Long -> it.toShort()
            is Double -> it.roundToInt().toShort()
            else -> it.toString().toShort()
        }
    }

    override fun getString(row: Int, column: Int) = get(row, column).let {
        when (it) {
            null -> null
            is String -> it
            else -> it.toString()
        }
    }

    override fun getTextBytes(row: Int, column: Int) =
        (get(row, column) as? String)?.toByteArray(Charsets.UTF_8)

    override fun isNull(row: Int, column: Int) = null == get(row, column)

    override fun numberOfRows() = rows.size

    override fun put(value: ByteArray?) = append(value)

    override fun put(value: Double) = append(value)

    override fun put(value: Float) = put(value.toDouble())

    override fun put(value: Int) = put(value.toLong())

    override fun put(value: Long) = append(value)

    override fun put(value: Short) = put(value.toLong())

    override fun put(value: String) = append(value)

    override fun putNull() = append(null)

    override fun type(row: Int, column: Int) = rows[row][column].toColumnType()

    private companion object {
        const val INITIAL_COLUMN_CAPACITY = 6
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun get(row: Int, column: Int) = rows[row][column]

    @Suppress("NOTHING_TO_INLINE")
    private inline fun append(value: Any?) = rows.last().add(value)
}

/**
 * Presents fixed-size cursor-window segments as one immutable, randomly accessible window.
 */
@NotThreadSafe
@Suppress("Detekt.MethodOverloading", "Detekt.TooManyFunctions")
internal class SegmentedCursorWindow(
    windows: List<ICursorWindow>,
    private val segmentSize: Int
) : ICursorWindow {
    private val windows = windows.toList()
    private var closed = false

    private val rowCount: Int

    init {
        require(segmentSize > 0) { "Segment size must be positive." }
        require(windows.isNotEmpty()) { "At least one cursor-window segment is required." }
        windows.dropLast(1).forEach {
            require(it.numberOfRows() == segmentSize) { "Only the final cursor-window segment may be partial." }
        }
        val lastSize = windows.last().numberOfRows()
        require(lastSize in 1..segmentSize) { "The final cursor-window segment must contain rows." }
        val total = (windows.size - 1L) * segmentSize + lastSize
        require(total <= Int.MAX_VALUE) { "Cursor row count exceeds Int.MAX_VALUE." }
        rowCount = total.toInt()
    }

    override fun allocateRow(): Boolean = immutable()

    override fun clear(): Unit = immutable()

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        var failure: Throwable? = null
        windows.forEach { window ->
            try {
                window.close()
            } catch (closeFailure: Throwable) {
                failure?.addSuppressed(closeFailure) ?: run { failure = closeFailure }
            }
        }
        failure?.let { throw it }
    }

    override fun getBlob(row: Int, column: Int) = read(row, column, ICursorWindow::getBlob)

    override fun getDouble(row: Int, column: Int) = read(row, column, ICursorWindow::getDouble)

    override fun getFloat(row: Int, column: Int) = read(row, column, ICursorWindow::getFloat)

    override fun getInt(row: Int, column: Int) = read(row, column, ICursorWindow::getInt)

    override fun getLong(row: Int, column: Int) = read(row, column, ICursorWindow::getLong)

    override fun getShort(row: Int, column: Int) = read(row, column, ICursorWindow::getShort)

    override fun getString(row: Int, column: Int) = read(row, column, ICursorWindow::getString)

    override fun getTextBytes(row: Int, column: Int) = read(row, column, ICursorWindow::getTextBytes)

    override fun isNull(row: Int, column: Int) = read(row, column, ICursorWindow::isNull)

    override fun numberOfRows() = rowCount

    override fun put(value: ByteArray?): Boolean = immutable()

    override fun put(value: Double): Boolean = immutable()

    override fun put(value: Float): Boolean = immutable()

    override fun put(value: Int): Boolean = immutable()

    override fun put(value: Long): Boolean = immutable()

    override fun put(value: Short): Boolean = immutable()

    override fun put(value: String): Boolean = immutable()

    override fun putNull(): Boolean = immutable()

    override fun type(row: Int, column: Int) = read(row, column, ICursorWindow::type)

    private inline fun <R> read(row: Int, column: Int, block: ICursorWindow.(Int, Int) -> R): R {
        check(!closed) { "Cursor window is closed." }
        if (row !in 0 until rowCount) {
            throw IndexOutOfBoundsException("Row $row is outside a cursor window containing $rowCount rows.")
        }
        val segment = row / segmentSize
        return windows[segment].block(row - segment * segmentSize, column)
    }

    private fun immutable(): Nothing = throw UnsupportedOperationException(
        "SegmentedCursorWindow is immutable."
    )
}

@Suppress("Detekt.ComplexInterface", "Detekt.MethodOverloading", "Detekt.TooManyFunctions")
internal interface ICursorWindow : Closeable {
    fun allocateRow(): Boolean

    fun clear()

    fun getBlob(row: Int, column: Int): ByteArray?

    fun getDouble(row: Int, column: Int): Double

    fun getFloat(row: Int, column: Int): Float

    fun getInt(row: Int, column: Int): Int

    fun getLong(row: Int, column: Int): Long

    fun getShort(row: Int, column: Int): Short

    fun getString(row: Int, column: Int): String?

    fun getTextBytes(row: Int, column: Int): ByteArray?

    fun isNull(row: Int, column: Int): Boolean

    fun numberOfRows(): Int

    fun put(value: ByteArray?): Boolean

    fun put(value: Double): Boolean

    fun put(value: Float): Boolean

    fun put(value: Int): Boolean

    fun put(value: Long): Boolean

    fun put(value: Short): Boolean

    fun put(value: String): Boolean

    fun putNull(): Boolean

    fun type(row: Int, column: Int): ColumnType
}
