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

import com.bloomberg.selekt.annotations.Generated
import java.io.Closeable
import javax.annotation.concurrent.NotThreadSafe
import kotlin.math.roundToInt
import kotlin.math.roundToLong

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

    override fun isNull(row: Int, column: Int) = null == get(row, column)

    override fun numberOfRows() = rows.size

    override fun put(value: ByteArray) = append(value)

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
    @Generated
    private inline fun get(row: Int, column: Int) = rows[row][column]

    @Suppress("NOTHING_TO_INLINE")
    @Generated
    private inline fun append(value: Any?) = rows.last().add(value)
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

    fun isNull(row: Int, column: Int): Boolean

    fun numberOfRows(): Int

    fun put(value: ByteArray): Boolean

    fun put(value: Double): Boolean

    fun put(value: Float): Boolean

    fun put(value: Int): Boolean

    fun put(value: Long): Boolean

    fun put(value: Short): Boolean

    fun put(value: String): Boolean

    fun putNull(): Boolean

    fun type(row: Int, column: Int): ColumnType
}
