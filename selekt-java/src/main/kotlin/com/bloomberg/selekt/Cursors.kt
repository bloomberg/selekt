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

/**
 * @since 0.12.1
 */
@Suppress("Detekt.ComplexInterface", "Detekt.TooManyFunctions")
interface ICursor : Closeable {
    val columnCount: Int

    val count: Int

    fun columnIndex(name: String): Int

    fun columnName(index: Int): String

    fun columnNames(): Array<out String>

    fun getBlob(index: Int): ByteArray?

    fun getFloat(index: Int): Float = getDouble(index).toFloat()

    fun getDouble(index: Int): Double

    fun getInt(index: Int): Int

    fun getLong(index: Int): Long

    fun getShort(index: Int): Short = getInt(index).toShort()

    fun getString(index: Int): String?

    /**
     * Returns the UTF-8 bytes of a text column, or null when the column does not contain text.
     *
     * @throws UnsupportedOperationException when direct text retrieval is not supported
     * @since 1.1.1
     */
    fun getTextBytes(index: Int): ByteArray? = throw UnsupportedOperationException(
        "Direct text retrieval is not supported."
    )

    fun isAfterLast(): Boolean

    fun isBeforeFirst(): Boolean

    fun isClosed(): Boolean

    fun isFirst(): Boolean

    val isForwardOnly: Boolean
        get() = false

    fun isLast(): Boolean

    fun isNull(index: Int): Boolean

    fun move(offset: Int): Boolean

    fun moveToFirst(): Boolean

    fun moveToLast(): Boolean

    fun moveToNext(): Boolean

    fun moveToPosition(position: Int): Boolean

    fun moveToPrevious(): Boolean

    fun position(): Int

    fun type(index: Int): ColumnType
}

internal fun interface CursorWindowRefill {
    fun refill(startPosition: Int): CursorWindowPage
}

private const val MIN_COLUMNS_FOR_INDEX_CACHE = 16
private const val LOOKUPS_BEFORE_INDEX_CACHE = 1
private const val HASH_MAP_LOAD_FACTOR = 0.75f
private const val MIN_ADAPTIVE_TEXT_BATCH_SIZE = 4
private const val MAX_ADAPTIVE_TEXT_BATCH_SIZE = 16
private const val NO_ADAPTIVE_TEXT_BATCH = -1L

private fun Array<out String>.columnIndexMap(): Map<String, Int> =
    HashMap<String, Int>((size / HASH_MAP_LOAD_FACTOR).toInt() + 1).also { indices ->
        forEachIndexed { index, name -> indices.putIfAbsent(name, index) }
    }

@NotThreadSafe
internal class WindowedCursor(
    private val columnNames: Array<out String>,
    page: CursorWindowPage,
    onClose: (() -> Unit)? = null,
    refill: CursorWindowRefill? = null
) : ICursor {
    private var closed = false
    private var position = -1
    private var onClose = onClose
    private var refill = refill

    private var window = page.window
    private var windowStart = page.startPosition
    private var columnIndexLookups = 0
    private var columnIndices: Map<String, Int>? = null

    override val columnCount = columnNames.size

    override val count = page.count

    private inline fun <R> read(index: Int, block: ICursorWindow.(row: Int) -> R): R {
        check(!closed) { "Cursor is closed." }
        check(position in 0 until count) { "Cursor position $position does not identify a row." }
        if (index !in 0 until columnCount) {
            throw IndexOutOfBoundsException("Column $index is outside a cursor containing $columnCount columns.")
        }
        if (position - windowStart !in 0 until window.numberOfRows()) {
            val next = requireNotNull(refill) {
                "Position $position lies outside a cursor window that cannot be refilled."
            }.refill(startPositionFor(position, window.numberOfRows()))
            if (position - next.startPosition !in 0 until next.window.numberOfRows()) {
                next.window.close()
                error(
                    "Refilled cursor window starting at ${next.startPosition} does not contain position $position."
                )
            }
            val previous = window
            window = next.window
            windowStart = next.startPosition
            previous.close()
        }
        return window.block(position - windowStart)
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        refill = null
        var failure = runCatching(window::close).exceptionOrNull()
        val release = onClose
        onClose = null
        runCatching { release?.invoke() }.exceptionOrNull()?.let {
            failure?.addSuppressed(it) ?: run { failure = it }
        }
        failure?.let { throw it }
    }

    override fun columnIndex(name: String): Int {
        val indices = columnIndices
        return when {
            indices != null -> indices[name] ?: -1
            columnNames.size < MIN_COLUMNS_FOR_INDEX_CACHE ||
                columnIndexLookups++ < LOOKUPS_BEFORE_INDEX_CACHE -> columnNames.indexOfFirst { it == name }
            else -> columnNames.columnIndexMap().let {
                columnIndices = it
                it[name] ?: -1
            }
        }
    }

    override fun columnName(index: Int) = columnNames[index]

    override fun columnNames() = columnNames

    override fun getBlob(index: Int) = read(index) { getBlob(it, index) }

    override fun getDouble(index: Int) = read(index) { getDouble(it, index) }

    override fun getInt(index: Int) = read(index) { getInt(it, index) }

    override fun getLong(index: Int) = read(index) { getLong(it, index) }

    override fun getString(index: Int) = read(index) { getString(it, index) }

    override fun getTextBytes(index: Int) = read(index) { getTextBytes(it, index) }

    override fun isAfterLast() = count.let { it == 0 || it == position }

    override fun isBeforeFirst() = count == 0 || position == -1

    override fun isClosed() = closed

    override fun isFirst() = position == 0 && count > 0

    override fun isLast() = count.let { it > 0 && it - 1 == position }

    override fun isNull(index: Int) = read(index) { isNull(it, index) }

    override fun move(offset: Int) = moveToPosition(position + offset)

    override fun moveToFirst() = moveToPosition(0)

    override fun moveToLast() = moveToPosition(count - 1)

    override fun moveToNext() = move(1)

    override fun moveToPosition(position: Int) = count.let {
        when {
            position >= it -> {
                this.position = it
                false
            }
            position < 0 -> {
                this.position = -1
                false
            }
            else -> {
                this.position = position
                true
            }
        }
    }

    override fun moveToPrevious() = move(-1)

    override fun position() = position

    override fun type(index: Int) = read(index) { type(it, index) }
}

private const val WINDOW_LOOKBEHIND_FRACTION = 3

private fun startPositionFor(
    position: Int,
    capacity: Int
) = maxOf(position - capacity / WINDOW_LOOKBEHIND_FRACTION, 0)

@NotThreadSafe
internal class ForwardCursor(
    statement: SQLPreparedStatement,
    private val onClose: (() -> Unit)? = null
) : ICursor {
    private var closed = false
    private var exhausted = false
    private var resourcesReleased = false
    private var statement: SQLPreparedStatement? = statement

    private val columnNames = statement.columnNames
    private var columnIndexLookups = 0
    private var columnIndices: Map<String, Int>? = null
    private val textColumnsRead = BooleanArray(columnNames.size)
    private var useBatchedTextValues = statement.useBatchedTextValues()
    private var textColumnReadCount = 0
    private var prefetchedTextFirstIndex = -1
    private var prefetchedText = emptyArray<String?>()
    private var prefetchedTextLoaded = BooleanArray(0)

    override val columnCount = columnNames.size

    override val count: Int
        get() = throw UnsupportedOperationException()

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        releaseResources()
    }

    override fun columnIndex(name: String): Int {
        val indices = columnIndices
        return when {
            indices != null -> indices[name] ?: -1
            columnNames.size < MIN_COLUMNS_FOR_INDEX_CACHE ||
                columnIndexLookups++ < LOOKUPS_BEFORE_INDEX_CACHE -> columnNames.indexOfFirst { it == name }
            else -> columnNames.columnIndexMap().let {
                columnIndices = it
                it[name] ?: -1
            }
        }
    }

    override fun columnName(index: Int) = columnNames[index]

    override fun columnNames() = columnNames

    override fun getBlob(index: Int) = statement().columnBlob(index)

    override fun getDouble(index: Int) = statement().columnDouble(index)

    override fun getInt(index: Int) = statement().columnInt(index)

    override fun getLong(index: Int) = statement().columnLong(index)

    override fun getString(index: Int): String? {
        val currentStatement = statement()
        if (
            useBatchedTextValues &&
            index in textColumnsRead.indices &&
            !textColumnsRead[index]
        ) {
            textColumnsRead[index] = true
            textColumnReadCount += 1
        }
        val prefetchedIndex = index - prefetchedTextFirstIndex
        return if (
            prefetchedTextFirstIndex >= 0 &&
            prefetchedIndex in prefetchedText.indices &&
            prefetchedTextLoaded[prefetchedIndex]
        ) {
            prefetchedText[prefetchedIndex]
        } else {
            currentStatement.columnString(index)
        }
    }

    override fun getTextBytes(index: Int) = if (statement().columnType(index) == SQL_TEXT) {
        statement().columnBlob(index)
    } else {
        null
    }

    override fun isAfterLast() = throw UnsupportedOperationException()

    override fun isBeforeFirst() = throw UnsupportedOperationException()

    override fun isClosed() = closed

    override fun isFirst() = throw UnsupportedOperationException()

    override val isForwardOnly: Boolean
        get() = true

    override fun isLast() = throw UnsupportedOperationException()

    override fun isNull(index: Int) = SQL_NULL == statement().columnType(index)

    override fun move(offset: Int) = throw UnsupportedOperationException()

    override fun moveToFirst() = throw UnsupportedOperationException()

    override fun moveToLast() = throw UnsupportedOperationException()

    override fun moveToNext(): Boolean {
        check(!closed) { "Cursor is closed." }
        if (exhausted) {
            return false
        }
        return try {
            val currentStatement = statement()
            val textBatch = adaptiveTextBatch()
            if (textColumnReadCount != 0) {
                textColumnsRead.fill(false)
                textColumnReadCount = 0
            }
            val result = currentStatement.step()
            if (result == SQL_ROW && textBatch != NO_ADAPTIVE_TEXT_BATCH) {
                val firstIndex = (textBatch ushr Int.SIZE_BITS).toInt()
                val count = textBatch.toInt()
                if (prefetchedText.size != count) {
                    prefetchedText = arrayOfNulls(count)
                    prefetchedTextLoaded = BooleanArray(count)
                }
                prefetchedTextFirstIndex = firstIndex
                currentStatement.columnTextValues(firstIndex, prefetchedText, prefetchedTextLoaded)
                useBatchedTextValues = currentStatement.useBatchedTextValues()
            } else {
                prefetchedTextFirstIndex = -1
            }
            if (SQL_ROW == result) {
                true
            } else {
                prefetchedTextFirstIndex = -1
                exhausted = true
                releaseResources()
                false
            }
        } catch (failure: Throwable) {
            exhausted = true
            runCatching(::releaseResources).exceptionOrNull()?.let(failure::addSuppressed)
            throw failure
        }
    }

    override fun moveToPosition(position: Int) = throw UnsupportedOperationException()

    override fun moveToPrevious() = throw UnsupportedOperationException()

    override fun position() = throw UnsupportedOperationException()

    override fun type(index: Int) = ColumnType.toColumnType(statement().columnType(index))

    private fun adaptiveTextBatch(): Long {
        if (!useBatchedTextValues || textColumnReadCount < MIN_ADAPTIVE_TEXT_BATCH_SIZE) {
            return NO_ADAPTIVE_TEXT_BATCH
        }
        var bestStart = -1
        var bestSize = 0
        var index = 0
        while (index < textColumnsRead.size) {
            if (!textColumnsRead[index]) {
                index += 1
                continue
            }
            val start = index
            while (index < textColumnsRead.size && textColumnsRead[index]) {
                index += 1
            }
            val size = minOf(index - start, MAX_ADAPTIVE_TEXT_BATCH_SIZE)
            if (size > bestSize) {
                bestStart = start
                bestSize = size
            }
        }
        return if (bestSize >= MIN_ADAPTIVE_TEXT_BATCH_SIZE) {
            bestStart.toLong() shl Int.SIZE_BITS or bestSize.toLong()
        } else {
            NO_ADAPTIVE_TEXT_BATCH
        }
    }

    private fun statement() = checkNotNull(statement) { "Cursor no longer identifies a row." }

    private fun releaseResources() {
        if (resourcesReleased) {
            return
        }
        resourcesReleased = true
        prefetchedTextFirstIndex = -1
        prefetchedText = emptyArray()
        prefetchedTextLoaded = BooleanArray(0)
        val statement = checkNotNull(statement)
        this.statement = null
        if (onClose != null) {
            onClose()
        } else {
            statement.close()
        }
    }
}
