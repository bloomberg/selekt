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

package com.bloomberg.selekt.jdbc.result

import com.bloomberg.selekt.ColumnType
import com.bloomberg.selekt.ICursor

/**
 * Applies JDBC's maximum-row contract without parsing or rewriting SQL.
 *
 * @since 1.2.4
 */
internal class RowLimitedCursor(
    private val cursor: ICursor,
    private val maximumRows: Int
) : ICursor by cursor {
    private var closed = false
    private var resourcesReleased = false
    private var forwardPosition = -1
    private var forwardOnRow = false
    private var forwardExhausted = false

    init {
        require(maximumRows > 0) { "Maximum rows must be positive." }
    }

    override val count: Int
        get() = minOf(cursor.count, maximumRows)

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        releaseResources()
    }

    override fun isClosed() = closed

    override fun getBlob(index: Int): ByteArray? {
        checkOnRow()
        return cursor.getBlob(index)
    }

    override fun getDouble(index: Int): Double {
        checkOnRow()
        return cursor.getDouble(index)
    }

    override fun getInt(index: Int): Int {
        checkOnRow()
        return cursor.getInt(index)
    }

    override fun getLong(index: Int): Long {
        checkOnRow()
        return cursor.getLong(index)
    }

    override fun getShort(index: Int): Short {
        checkOnRow()
        return cursor.getShort(index)
    }

    override fun getString(index: Int): String? {
        checkOnRow()
        return cursor.getString(index)
    }

    override fun getTextBytes(index: Int): ByteArray? {
        checkOnRow()
        return cursor.getTextBytes(index)
    }

    override fun isFirst(): Boolean = if (cursor.isForwardOnly) {
        cursor.isFirst()
    } else {
        cursor.position() == 0 && count > 0
    }

    override fun isLast(): Boolean = if (cursor.isForwardOnly) {
        cursor.isLast()
    } else {
        cursor.position() == count - 1 && count > 0
    }

    override fun isNull(index: Int): Boolean {
        checkOnRow()
        return cursor.isNull(index)
    }

    override fun move(offset: Int): Boolean {
        if (cursor.isForwardOnly) {
            return cursor.move(offset)
        }
        val currentPosition = when {
            cursor.isBeforeFirst() -> -1
            cursor.isAfterLast() -> count
            else -> minOf(cursor.position(), count)
        }
        val requestedPosition = (currentPosition.toLong() + offset).coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong())
        return moveToPosition(requestedPosition.toInt())
    }

    override fun moveToFirst(): Boolean {
        if (cursor.isForwardOnly) {
            return cursor.moveToFirst()
        }
        return moveToPosition(0)
    }

    override fun moveToLast(): Boolean {
        if (cursor.isForwardOnly) {
            return cursor.moveToLast()
        }
        return moveToPosition(count - 1)
    }

    override fun moveToNext(): Boolean = if (cursor.isForwardOnly) {
        moveForwardToNext()
    } else {
        move(1)
    }

    private fun moveForwardToNext(): Boolean {
        if (forwardExhausted || forwardPosition + 1 >= maximumRows) {
            forwardOnRow = false
            forwardExhausted = true
            releaseResources()
            return false
        }
        return cursor.moveToNext().also { moved ->
            forwardOnRow = moved
            if (moved) {
                forwardPosition += 1
            } else {
                forwardExhausted = true
            }
        }
    }

    override fun moveToPosition(position: Int): Boolean {
        if (cursor.isForwardOnly) {
            return cursor.moveToPosition(position)
        }
        return when {
            position < 0 -> {
                cursor.moveToPosition(-1)
                false
            }
            position >= count -> {
                cursor.moveToPosition(cursor.count)
                false
            }
            else -> cursor.moveToPosition(position)
        }
    }

    override fun moveToPrevious(): Boolean {
        if (cursor.isForwardOnly) {
            return cursor.moveToPrevious()
        }
        return move(-1)
    }

    override fun position(): Int {
        if (cursor.isForwardOnly) {
            return cursor.position()
        }
        return when {
            cursor.isBeforeFirst() -> -1
            cursor.isAfterLast() -> count
            else -> minOf(cursor.position(), count)
        }
    }

    override fun type(index: Int): ColumnType {
        checkOnRow()
        return cursor.type(index)
    }

    private fun checkOnRow() {
        if (cursor.isForwardOnly) {
            check(forwardOnRow) { "Cursor does not identify a row." }
        } else {
            check(!cursor.isBeforeFirst() && !cursor.isAfterLast()) { "Cursor does not identify a row." }
        }
    }

    private fun releaseResources() {
        if (resourcesReleased) {
            return
        }
        resourcesReleased = true
        cursor.close()
    }
}
