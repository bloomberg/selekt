/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

    fun isAfterLast(): Boolean

    fun isBeforeFirst(): Boolean

    fun isClosed(): Boolean

    fun isFirst(): Boolean

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

@NotThreadSafe
internal class WindowedCursor(
    private val columnNames: Array<out String>,
    private val window: ICursorWindow
) : ICursor {
    private var closed = false
    private var position = -1

    override val columnCount = columnNames.size

    override val count = window.numberOfRows()

    override fun close() {
        closed = true
        window.close()
    }

    override fun columnIndex(name: String) = columnNames.indexOfFirst { it == name }

    override fun columnName(index: Int) = columnNames[index]

    override fun columnNames() = columnNames

    override fun getBlob(index: Int) = window.getBlob(position, index)

    override fun getDouble(index: Int) = window.getDouble(position, index)

    override fun getInt(index: Int) = window.getInt(position, index)

    override fun getLong(index: Int) = window.getLong(position, index)

    override fun getString(index: Int) = window.getString(position, index)

    override fun isAfterLast() = count.let { it == 0 || it == position }

    override fun isBeforeFirst() = count == 0 || position == -1

    override fun isClosed() = closed

    override fun isFirst() = position == 0 && count > 0

    override fun isLast() = count.let { it > 0 && it - 1 == position }

    override fun isNull(index: Int) = window.isNull(position, index)

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

    override fun type(index: Int) = window.type(position, index)
}

@NotThreadSafe
internal class ForwardCursor(
    private val statement: SQLPreparedStatement
) : ICursor {
    private var closed = false

    private val columnNames = statement.columnNames

    override val columnCount = columnNames.size

    override val count: Int
        get() = throw UnsupportedOperationException()

    override fun close() {
        closed = true
        statement.close()
    }

    override fun columnIndex(name: String) = columnNames.indexOfFirst { it == name }

    override fun columnName(index: Int) = columnNames[index]

    override fun columnNames() = columnNames

    override fun getBlob(index: Int) = statement.columnBlob(index)

    override fun getDouble(index: Int) = statement.columnDouble(index)

    override fun getInt(index: Int) = statement.columnInt(index)

    override fun getLong(index: Int) = statement.columnLong(index)

    override fun getString(index: Int) = statement.columnString(index)

    override fun isAfterLast() = throw UnsupportedOperationException()

    override fun isBeforeFirst() = throw UnsupportedOperationException()

    override fun isClosed() = closed

    override fun isFirst() = throw UnsupportedOperationException()

    override fun isLast() = throw UnsupportedOperationException()

    override fun isNull(index: Int) = SQL_NULL == statement.columnType(index)

    override fun move(offset: Int) = throw UnsupportedOperationException()

    override fun moveToFirst() = throw UnsupportedOperationException()

    override fun moveToLast() = throw UnsupportedOperationException()

    override fun moveToNext() = SQL_ROW == statement.step()

    override fun moveToPosition(position: Int) = throw UnsupportedOperationException()

    override fun moveToPrevious() = throw UnsupportedOperationException()

    override fun position() = throw UnsupportedOperationException()

    override fun type(index: Int) = ColumnType.toColumnType(statement.columnType(index))
}
