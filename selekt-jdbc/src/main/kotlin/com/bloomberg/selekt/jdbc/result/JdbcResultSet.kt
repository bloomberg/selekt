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
import com.bloomberg.selekt.OperationCancelledException
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.lob.JdbcBlob
import com.bloomberg.selekt.jdbc.lob.JdbcClob
import com.bloomberg.selekt.jdbc.statement.JdbcStatement
import com.bloomberg.selekt.jdbc.util.TypeMapping
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.NClob
import java.sql.Ref
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.RowId
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.sql.SQLXML
import java.sql.SQLWarning
import java.sql.Statement
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import javax.annotation.concurrent.NotThreadSafe

private const val READ_ONLY_ERROR = "ResultSet is read-only"

private fun asciiStream(utf8: ByteArray): InputStream {
    var readPosition = utf8.indexOfFirst { it < 0 }
    if (readPosition == -1) {
        return ByteArrayInputStream(utf8)
    }
    var writePosition = readPosition
    while (readPosition < utf8.size) {
        val byte = utf8[readPosition]
        if (byte >= 0) {
            utf8[writePosition++] = byte
            readPosition += 1
        } else {
            val sequenceLength = validUtf8SequenceLength(utf8, readPosition)
            utf8[writePosition++] = '?'.code.toByte()
            readPosition += sequenceLength
        }
    }
    return ByteArrayInputStream(utf8, 0, writePosition)
}

@Suppress("ComplexCondition", "MagicNumber", "ReturnCount", "UnnecessaryParentheses")
private fun validUtf8SequenceLength(utf8: ByteArray, position: Int): Int {
    val first = utf8[position].toInt() and 0xFF
    val continuationCount = when (first) {
        in 0xC2..0xDF -> 1
        in 0xE0..0xEF -> 2
        in 0xF0..0xF4 -> 3
        else -> return 1
    }
    if (position + continuationCount >= utf8.size) {
        return 1
    }
    val second = utf8[position + 1].toInt() and 0xFF
    if (second !in 0x80..0xBF ||
        first == 0xE0 && second < 0xA0 ||
        first == 0xED && second >= 0xA0 ||
        first == 0xF0 && second < 0x90 ||
        first == 0xF4 && second >= 0x90
    ) {
        return 1
    }
    repeat(continuationCount - 1) {
        if ((utf8[position + 2 + it].toInt() and 0xFF) !in 0x80..0xBF) {
            return 1
        }
    }
    return continuationCount + 1
}

internal fun utf8Reader(utf8: ByteArray): Reader = Utf8ByteArrayReader(utf8)

@Suppress(
    "CognitiveComplexMethod",
    "ComplexCondition",
    "MagicNumber",
    "ReturnCount",
    "UnnecessaryParentheses"
)
private class Utf8ByteArrayReader(
    private val bytes: ByteArray
) : Reader() {
    private var position = 0
    private var pendingLowSurrogate = 0

    override fun close() = Unit

    override fun read(): Int {
        if (pendingLowSurrogate != 0) {
            return pendingLowSurrogate.also { pendingLowSurrogate = 0 }
        }
        if (position >= bytes.size) {
            return -1
        }
        val codePoint = nextCodePoint()
        return if (codePoint <= Char.MAX_VALUE.code) {
            codePoint
        } else {
            pendingLowSurrogate = Character.lowSurrogate(codePoint).code
            Character.highSurrogate(codePoint).code
        }
    }

    override fun read(
        destination: CharArray,
        offset: Int,
        length: Int
    ): Int {
        validateBounds(destination, offset, length)
        if (length == 0) {
            return 0
        }
        var copied = copyPendingLowSurrogate(destination, offset)
        while (copied < length && position < bytes.size) {
            val ascii = bytes[position].toInt()
            if (ascii >= 0) {
                destination[offset + copied++] = ascii.toChar()
                position += 1
                continue
            }
            val codePoint = nextCodePoint()
            if (codePoint <= Char.MAX_VALUE.code) {
                destination[offset + copied++] = codePoint.toChar()
            } else {
                destination[offset + copied++] = Character.highSurrogate(codePoint)
                val lowSurrogate = Character.lowSurrogate(codePoint)
                if (copied < length) {
                    destination[offset + copied++] = lowSurrogate
                } else {
                    pendingLowSurrogate = lowSurrogate.code
                }
            }
        }
        if (copied == 0) {
            return -1
        }
        return copied
    }

    private fun validateBounds(
        destination: CharArray,
        offset: Int,
        length: Int
    ) {
        if (offset < 0 || length < 0 || offset > destination.size - length) {
            throw IndexOutOfBoundsException(
                "Size: ${destination.size}; offset: $offset; length: $length"
            )
        }
    }

    private fun copyPendingLowSurrogate(
        destination: CharArray,
        offset: Int
    ): Int = if (pendingLowSurrogate == 0) {
        0
    } else {
        destination[offset] = pendingLowSurrogate.toChar()
        pendingLowSurrogate = 0
        1
    }

    private fun nextCodePoint(): Int {
        val first = bytes[position++].toInt() and 0xFF
        if (first < 0x80) {
            return first
        }
        return when (first) {
            in 0xC2..0xDF -> decodeTwoBytes(first)
            in 0xE0..0xEF -> decodeThreeBytes(first)
            in 0xF0..0xF4 -> decodeFourBytes(first)
            else -> REPLACEMENT_CHARACTER
        }
    }

    private fun decodeTwoBytes(first: Int): Int {
        if (position >= bytes.size) {
            return REPLACEMENT_CHARACTER
        }
        val second = bytes[position].toInt() and 0xFF
        if (second !in 0x80..0xBF) {
            return REPLACEMENT_CHARACTER
        }
        position += 1
        return (first and 0x1F) shl 6 or (second and 0x3F)
    }

    private fun decodeThreeBytes(first: Int): Int {
        if (position + 1 >= bytes.size) {
            return REPLACEMENT_CHARACTER
        }
        val second = bytes[position].toInt() and 0xFF
        val third = bytes[position + 1].toInt() and 0xFF
        if (second !in 0x80..0xBF ||
            third !in 0x80..0xBF ||
            first == 0xE0 && second < 0xA0 ||
            first == 0xED && second >= 0xA0
        ) {
            return REPLACEMENT_CHARACTER
        }
        position += 2
        return (first and 0x0F) shl 12 or
            ((second and 0x3F) shl 6) or
            (third and 0x3F)
    }

    private fun decodeFourBytes(first: Int): Int {
        if (position + 2 >= bytes.size) {
            return REPLACEMENT_CHARACTER
        }
        val second = bytes[position].toInt() and 0xFF
        val third = bytes[position + 1].toInt() and 0xFF
        val fourth = bytes[position + 2].toInt() and 0xFF
        if (second !in 0x80..0xBF ||
            third !in 0x80..0xBF ||
            fourth !in 0x80..0xBF ||
            first == 0xF0 && second < 0x90 ||
            first == 0xF4 && second >= 0x90
        ) {
            return REPLACEMENT_CHARACTER
        }
        position += 3
        return (first and 0x07) shl 18 or
            ((second and 0x3F) shl 12) or
            ((third and 0x3F) shl 6) or
            (fourth and 0x3F)
    }

    private companion object {
        const val REPLACEMENT_CHARACTER = 0xFFFD
    }
}

/**
 * @since 0.28.0
 */
@NotThreadSafe
@Suppress("MethodOverloading", "LargeClass", "TooGenericExceptionCaught")
internal class JdbcResultSet(
    private val cursor: ICursor,
    private val statement: Statement?,
    private val resultSetType: Int = ResultSet.TYPE_FORWARD_ONLY,
    private val resultSetConcurrency: Int = ResultSet.CONCUR_READ_ONLY,
    private val resultSetHoldability: Int = ResultSet.CLOSE_CURSORS_AT_COMMIT
) : ResultSet {
    private var wasNull = false
    private val metadata by lazy { JdbcResultSetMetaData(cursor) }
    private var fetchSize = 0

    override fun next(): Boolean {
        checkClosed()
        return try {
            cursor.moveToNext().also {
                if (!it) {
                    onExhausted()
                }
            }
        } catch (e: OperationCancelledException) {
            onExhausted()
            throw SQLExceptionMapper.mapCancellation(e)
        } catch (e: SQLException) {
            onExhausted()
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            onExhausted()
            if (e.message?.contains("interrupt", ignoreCase = true) == true) {
                throw SQLExceptionMapper.mapCancellation(e)
            }
            throw SQLExceptionMapper.mapException(
                "Error advancing cursor: ${e.message}", -1, -1, e
            )
        }
    }

    override fun close() {
        if (cursor.isClosed()) {
            return
        }
        cursor.close()
        (statement as? JdbcStatement)?.onResultSetClosed(this)
    }

    private fun onExhausted() {
        (statement as? JdbcStatement)?.onResultSetClosed(this, exhausted = true)
    }

    override fun wasNull(): Boolean = wasNull

    override fun getString(columnIndex: Int): String? {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                null
            } else {
                wasNull = false
                cursor.getString(columnIndex - 1)
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting string from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getString(columnLabel: String): String? = getString(findColumn(columnLabel))

    override fun getBoolean(columnIndex: Int): Boolean {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                false
            } else {
                wasNull = false
                try {
                    cursor.getLong(columnIndex - 1) != 0L
                } catch (_: Exception) {
                    cursor.getString(columnIndex - 1)?.toBoolean() ?: false
                }
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting boolean from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getBoolean(columnLabel: String): Boolean = getBoolean(findColumn(columnLabel))

    override fun getByte(columnIndex: Int): Byte {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                0
            } else {
                wasNull = false
                cursor.getInt(columnIndex - 1).toByte()
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting byte from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getByte(columnLabel: String): Byte = getByte(findColumn(columnLabel))

    override fun getShort(columnIndex: Int): Short {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                0
            } else {
                wasNull = false
                cursor.getShort(columnIndex - 1)
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting short from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getShort(columnLabel: String): Short = getShort(findColumn(columnLabel))

    override fun getInt(columnIndex: Int): Int {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                0
            } else {
                wasNull = false
                when (cursor.type(columnIndex - 1)) {
                    ColumnType.INTEGER -> cursor.getInt(columnIndex - 1)
                    ColumnType.STRING -> {
                        val stringValue = cursor.getString(columnIndex - 1)
                        TypeMapping.convertFromSQLite(stringValue, Types.INTEGER) as Int
                    }
                    ColumnType.FLOAT -> cursor.getDouble(columnIndex - 1).toInt()
                    ColumnType.NULL -> 0
                    ColumnType.BLOB -> 0 // Cannot convert blob to int
                }
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting int from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getInt(columnLabel: String): Int = getInt(findColumn(columnLabel))

    override fun getLong(columnIndex: Int): Long {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                0L
            } else {
                wasNull = false
                cursor.getLong(columnIndex - 1)
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting long from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getLong(columnLabel: String): Long = getLong(findColumn(columnLabel))

    override fun getFloat(columnIndex: Int): Float {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                0f
            } else {
                wasNull = false
                cursor.getFloat(columnIndex - 1)
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting float from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getFloat(columnLabel: String): Float = getFloat(findColumn(columnLabel))

    override fun getDouble(columnIndex: Int): Double {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                0.0
            } else {
                wasNull = false
                val columnType = cursor.type(columnIndex - 1)

                when (columnType) {
                    ColumnType.FLOAT -> cursor.getDouble(columnIndex - 1)
                    ColumnType.INTEGER -> cursor.getInt(columnIndex - 1).toDouble()
                    ColumnType.STRING -> {
                        val stringValue = cursor.getString(columnIndex - 1)
                        TypeMapping.convertFromSQLite(stringValue, Types.DOUBLE) as Double
                    }
                    ColumnType.NULL -> 0.0
                    ColumnType.BLOB -> 0.0 // Cannot convert blob to double
                }
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting double from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getDouble(columnLabel: String): Double = getDouble(findColumn(columnLabel))

    override fun getBigDecimal(columnIndex: Int): BigDecimal? {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                null
            } else {
                wasNull = false
                val value = cursor.getDouble(columnIndex - 1)
                BigDecimal.valueOf(value)
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting BigDecimal from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getBigDecimal(columnIndex)"))
    @Suppress("DEPRECATION")
    override fun getBigDecimal(columnIndex: Int, scale: Int): BigDecimal? = getBigDecimal(columnIndex)?.setScale(
        scale, BigDecimal.ROUND_HALF_UP)

    override fun getBigDecimal(columnLabel: String): BigDecimal? = getBigDecimal(findColumn(columnLabel))

    @Deprecated("Deprecated in Java", ReplaceWith("getBigDecimal(columnLabel)"))
    @Suppress("DEPRECATION")
    override fun getBigDecimal(
        columnLabel: String,
        scale: Int
    ): BigDecimal? = getBigDecimal(columnLabel)?.setScale(scale, BigDecimal.ROUND_HALF_UP)

    override fun getBytes(columnIndex: Int): ByteArray? {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                null
            } else {
                wasNull = false
                cursor.getBlob(columnIndex - 1)
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting bytes from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getBytes(columnLabel: String): ByteArray? = getBytes(findColumn(columnLabel))

    override fun getDate(columnIndex: Int): Date? {
        val dateString = getString(columnIndex)
        return if (wasNull) {
            null
        } else {
            try {
                dateString?.let {
                    TypeMapping.convertFromSQLite(it, Types.DATE) as? Date
                }
            } catch (e: SQLException) {
                throw SQLExceptionMapper.mapException(e)
            } catch (e: RuntimeException) {
                throw SQLExceptionMapper.mapException(
                    "Error parsing date from column $columnIndex: ${e.message}",
                    -1,
                    -1,
                    e
                )
            }
        }
    }

    override fun getDate(columnLabel: String): Date? = getDate(findColumn(columnLabel))

    override fun getDate(columnIndex: Int, cal: Calendar?): Date? = getDate(columnIndex)

    override fun getDate(columnLabel: String, cal: Calendar?): Date? = getDate(columnLabel)

    override fun getTime(columnIndex: Int): Time? {
        val timeString = getString(columnIndex)
        return if (wasNull) {
            null
        } else {
            try {
                timeString?.let {
                    TypeMapping.convertFromSQLite(it, Types.TIME) as? Time
                }
            } catch (e: SQLException) {
                throw SQLExceptionMapper.mapException(e)
            } catch (e: RuntimeException) {
                throw SQLExceptionMapper.mapException(
                    "Error parsing time from column $columnIndex: ${e.message}",
                    -1,
                    -1,
                    e
                )
            }
        }
    }

    override fun getTime(columnLabel: String): Time? = getTime(findColumn(columnLabel))

    override fun getTime(columnIndex: Int, cal: Calendar?): Time? = getTime(columnIndex)

    override fun getTime(columnLabel: String, cal: Calendar?): Time? = getTime(columnLabel)

    override fun getTimestamp(columnIndex: Int): Timestamp? {
        val timestampString = getString(columnIndex)
        return if (wasNull) {
            null
        } else {
            try {
                timestampString?.let {
                    TypeMapping.convertFromSQLite(it, Types.TIMESTAMP) as? Timestamp
                }
            } catch (e: SQLException) {
                throw SQLExceptionMapper.mapException(e)
            } catch (e: RuntimeException) {
                throw SQLExceptionMapper.mapException(
                    "Error parsing timestamp from column $columnIndex: ${e.message}",
                    -1,
                    -1,
                    e
                )
            }
        }
    }

    override fun getTimestamp(columnLabel: String): Timestamp? = getTimestamp(findColumn(columnLabel))

    override fun getTimestamp(columnIndex: Int, cal: Calendar?): Timestamp? = getTimestamp(columnIndex)

    override fun getTimestamp(columnLabel: String, cal: Calendar?): Timestamp? = getTimestamp(columnLabel)

    override fun findColumn(columnLabel: String): Int {
        checkClosed()
        when (val index = cursor.columnIndex(columnLabel)) {
            -1 -> throw SQLException("Column '$columnLabel' not found")
            else -> return index + 1
        }
    }

    override fun isBeforeFirst(): Boolean {
        checkClosed()
        return cursor.isBeforeFirst()
    }

    override fun isAfterLast(): Boolean {
        checkClosed()
        return cursor.isAfterLast()
    }

    override fun isFirst(): Boolean {
        checkClosed()
        return cursor.isFirst()
    }

    override fun isLast(): Boolean {
        checkClosed()
        return cursor.isLast()
    }

    override fun beforeFirst() {
        checkClosed()
        checkScrollable()
        cursor.moveToPosition(-1)
    }

    override fun afterLast() {
        checkClosed()
        checkScrollable()
        cursor.moveToPosition(cursor.count)
    }

    override fun first(): Boolean {
        checkClosed()
        checkScrollable()
        return cursor.moveToFirst()
    }

    override fun last(): Boolean {
        checkClosed()
        checkScrollable()
        return cursor.moveToLast()
    }

    override fun getRow(): Int {
        checkClosed()
        return if (cursor.isBeforeFirst() || cursor.isAfterLast()) { 0 } else { cursor.position() + 1 }
    }

    override fun absolute(row: Int): Boolean {
        checkClosed()
        checkScrollable()
        return cursor.moveToPosition(row - 1)
    }

    override fun relative(rows: Int): Boolean {
        checkClosed()
        checkScrollable()
        return cursor.move(rows)
    }

    override fun previous(): Boolean {
        checkClosed()
        checkScrollable()
        return cursor.moveToPrevious()
    }

    override fun getMetaData(): ResultSetMetaData = metadata

    override fun getObject(columnIndex: Int): Any? {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            if (cursor.isNull(columnIndex - 1)) {
                wasNull = true
                null
            } else {
                wasNull = false
                val columnType = cursor.type(columnIndex - 1)
                when (columnType) {
                    ColumnType.INTEGER -> cursor.getLong(columnIndex - 1)
                    ColumnType.FLOAT -> cursor.getDouble(columnIndex - 1)
                    ColumnType.STRING -> cursor.getString(columnIndex - 1)
                    ColumnType.BLOB -> cursor.getBlob(columnIndex - 1)
                    ColumnType.NULL -> null
                }
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting object from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getObject(columnLabel: String): Any? = getObject(findColumn(columnLabel))

    override fun <T> getObject(columnIndex: Int, type: Class<T>): T? {
        val value = getObject(columnIndex)
        @Suppress("UNCHECKED_CAST")
        return when {
            value == null || wasNull -> null as T?
            type.isInstance(value) -> value as T
            type == String::class.java -> value.toString() as T
            type == Int::class.java && value is Number -> value.toInt() as T
            type == Long::class.java && value is Number -> value.toLong() as T
            type == Double::class.java && value is Number -> value.toDouble() as T
            type == Float::class.java && value is Number -> value.toFloat() as T
            type == Boolean::class.java -> when (value) {
                is Boolean -> value
                is Number -> value.toLong() != 0L
                is String -> value.equals("true", ignoreCase = true) || value == "1"
                else -> false
            } as T
            type == LocalDate::class.java && value is String -> LocalDate.parse(value) as T
            type == LocalTime::class.java && value is String -> LocalTime.parse(value) as T
            type == LocalDateTime::class.java && value is String -> LocalDateTime.parse(value) as T
            else -> throw SQLException("Cannot convert ${value.javaClass.name} to ${type.name}")
        }
    }

    override fun <T> getObject(columnLabel: String, type: Class<T>): T? = getObject(findColumn(columnLabel), type)

    override fun getObject(columnIndex: Int, map: MutableMap<String, Class<*>>?): Any? = getObject(columnIndex)

    override fun getObject(columnLabel: String, map: MutableMap<String, Class<*>>?): Any? = getObject(columnLabel)

    override fun getAsciiStream(
        columnIndex: Int
    ): InputStream? = textStreamValue(
        columnIndex = columnIndex,
        fromUtf8 = ::asciiStream,
        fallback = { it.byteInputStream(Charsets.US_ASCII) }
    )

    override fun getAsciiStream(columnLabel: String): InputStream? = getAsciiStream(findColumn(columnLabel))

    @Deprecated("Deprecated in Java", ReplaceWith("getCharacterStream(columnIndex)"))
    override fun getUnicodeStream(
        columnIndex: Int
    ): InputStream? = getString(columnIndex)?.byteInputStream(Charsets.UTF_16)

    @Deprecated("Deprecated in Java", ReplaceWith("getCharacterStream(columnLabel)"))
    @Suppress("DEPRECATION")
    override fun getUnicodeStream(columnLabel: String): InputStream? = getUnicodeStream(findColumn(columnLabel))

    override fun getBinaryStream(columnIndex: Int): InputStream? = getBytes(columnIndex)?.inputStream()

    override fun getBinaryStream(columnLabel: String): InputStream? = getBinaryStream(findColumn(columnLabel))

    override fun getCharacterStream(columnIndex: Int): Reader? = textStreamValue(
        columnIndex = columnIndex,
        fromUtf8 = ::utf8Reader,
        fallback = String::reader
    )

    override fun getCharacterStream(columnLabel: String): Reader? = getCharacterStream(findColumn(columnLabel))

    private inline fun <T> textStreamValue(
        columnIndex: Int,
        fromUtf8: (ByteArray) -> T,
        fallback: (String) -> T
    ): T? {
        checkClosed()
        validateColumnIndex(columnIndex)
        return try {
            val cursorIndex = columnIndex - 1
            cursor.getTextBytes(cursorIndex)?.let {
                wasNull = false
                return fromUtf8(it)
            }
            when (cursor.type(cursorIndex)) {
                ColumnType.NULL -> {
                    wasNull = true
                    null
                }
                ColumnType.STRING -> {
                    wasNull = false
                    cursor.getBlob(cursorIndex)?.let(fromUtf8)
                        ?: cursor.getString(cursorIndex)?.let(fallback)
                }
                else -> {
                    wasNull = false
                    cursor.getString(cursorIndex)?.let(fallback)
                }
            }
        } catch (e: SQLException) {
            throw SQLExceptionMapper.mapException(e)
        } catch (e: RuntimeException) {
            throw SQLExceptionMapper.mapException(
                "Error getting text stream from column $columnIndex: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun getWarnings(): SQLWarning? = null

    override fun clearWarnings() {}

    override fun getCursorName(): String = throw SQLFeatureNotSupportedException("Named cursors not supported")

    override fun getStatement(): Statement? = statement

    override fun getType(): Int = resultSetType

    override fun getConcurrency(): Int = resultSetConcurrency

    override fun getHoldability(): Int = resultSetHoldability

    override fun isClosed(): Boolean = cursor.isClosed()

    override fun setFetchDirection(direction: Int) {
        if (direction != ResultSet.FETCH_FORWARD) {
            throw SQLFeatureNotSupportedException("Only FETCH_FORWARD is supported")
        }
    }

    override fun getFetchDirection(): Int = ResultSet.FETCH_FORWARD

    override fun setFetchSize(rows: Int) {
        checkClosed()
        if (rows < 0) {
            throw SQLException("Fetch size must be >= 0, got: $rows")
        }
        fetchSize = rows
    }

    override fun getFetchSize(): Int = fetchSize

    override fun getNClob(columnIndex: Int): NClob = throw SQLFeatureNotSupportedException()

    override fun getNClob(columnLabel: String): NClob = throw SQLFeatureNotSupportedException()

    override fun getSQLXML(columnIndex: Int): SQLXML = throw SQLFeatureNotSupportedException()

    override fun getSQLXML(columnLabel: String): SQLXML = throw SQLFeatureNotSupportedException()

    override fun getURL(columnIndex: Int): URL = throw SQLFeatureNotSupportedException()

    override fun getURL(columnLabel: String): URL = throw SQLFeatureNotSupportedException()

    override fun getArray(columnIndex: Int): java.sql.Array = throw SQLFeatureNotSupportedException()

    override fun getArray(columnLabel: String): java.sql.Array = throw SQLFeatureNotSupportedException()

    override fun getBlob(columnIndex: Int): Blob? {
        checkClosed()
        val bytes = getBytes(columnIndex)
        wasNull = bytes == null
        return if (bytes != null) {
            JdbcBlob(bytes)
        } else {
            null
        }
    }

    override fun getBlob(columnLabel: String): Blob? {
        checkClosed()
        val bytes = getBytes(columnLabel)
        wasNull = bytes == null
        return if (bytes != null) {
            JdbcBlob(bytes)
        } else {
            null
        }
    }

    override fun getClob(columnIndex: Int): Clob? {
        checkClosed()
        val text = getString(columnIndex)
        wasNull = text == null
        return if (text != null) JdbcClob(text) else null
    }

    override fun getClob(columnLabel: String): Clob? {
        checkClosed()
        val text = getString(columnLabel)
        wasNull = text == null
        return if (text != null) {
            JdbcClob(text)
        } else {
            null
        }
    }

    override fun getRef(columnIndex: Int): Ref = throw SQLFeatureNotSupportedException()

    override fun getRef(columnLabel: String): Ref = throw SQLFeatureNotSupportedException()

    override fun getNString(columnIndex: Int): String? = getString(columnIndex)

    override fun getNString(columnLabel: String): String? = getString(columnLabel)

    override fun getNCharacterStream(columnIndex: Int): Reader? = getCharacterStream(columnIndex)

    override fun getNCharacterStream(columnLabel: String): Reader? = getCharacterStream(columnLabel)

    override fun getRowId(columnIndex: Int): RowId = throw SQLFeatureNotSupportedException()

    override fun getRowId(columnLabel: String): RowId = throw SQLFeatureNotSupportedException()

    override fun rowUpdated(): Boolean = false

    override fun rowInserted(): Boolean = false

    override fun rowDeleted(): Boolean = false

    override fun updateNull(columnIndex: Int) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNull(columnLabel: String) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBoolean(columnIndex: Int, x: Boolean) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBoolean(columnLabel: String, x: Boolean) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateByte(columnIndex: Int, x: Byte) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateByte(columnLabel: String, x: Byte) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateShort(columnIndex: Int, x: Short) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateShort(columnLabel: String, x: Short) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateInt(columnIndex: Int, x: Int) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateInt(columnLabel: String, x: Int) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateLong(columnIndex: Int, x: Long) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateLong(columnLabel: String, x: Long) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateFloat(columnIndex: Int, x: Float) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateFloat(columnLabel: String, x: Float) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateDouble(columnIndex: Int, x: Double) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateDouble(columnLabel: String, x: Double) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBigDecimal(columnIndex: Int, x: BigDecimal?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBigDecimal(
        columnLabel: String,
        x: BigDecimal?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateString(columnIndex: Int, x: String?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateString(columnLabel: String, x: String?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBytes(columnIndex: Int, x: ByteArray?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBytes(columnLabel: String, x: ByteArray?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateDate(columnIndex: Int, x: Date?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateDate(columnLabel: String, x: Date?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateTime(columnIndex: Int, x: Time?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateTime(columnLabel: String, x: Time?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateTimestamp(columnIndex: Int, x: Timestamp?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateTimestamp(columnLabel: String, x: Timestamp?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateAsciiStream(
        columnIndex: Int,
        x: InputStream?,
        length: Int
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateAsciiStream(
        columnLabel: String,
        x: InputStream?,
        length: Int
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateAsciiStream(
        columnIndex: Int,
        x: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateAsciiStream(
        columnLabel: String,
        x: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateAsciiStream(
        columnIndex: Int,
        x: InputStream?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateAsciiStream(
        columnLabel: String,
        x: InputStream?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBinaryStream(
        columnIndex: Int,
        x: InputStream?,
        length: Int
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBinaryStream(
        columnLabel: String,
        x: InputStream?,
        length: Int
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBinaryStream(
        columnIndex: Int,
        x: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBinaryStream(
        columnLabel: String,
        x: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBinaryStream(
        columnIndex: Int,
        x: InputStream?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBinaryStream(
        columnLabel: String,
        x: InputStream?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateCharacterStream(
        columnIndex: Int,
        x: Reader?,
        length: Int
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateCharacterStream(
        columnLabel: String,
        reader: Reader?,
        length: Int
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateCharacterStream(
        columnIndex: Int,
        x: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateCharacterStream(
        columnLabel: String,
        reader: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateCharacterStream(
        columnIndex: Int,
        reader: Reader?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateCharacterStream(
        columnLabel: String,
        reader: Reader?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateObject(
        columnIndex: Int,
        x: Any?,
        scaleOrLength: Int
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateObject(
        columnIndex: Int,
        x: Any?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateObject(
        columnLabel: String,
        x: Any?,
        scaleOrLength: Int
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateObject(
        columnLabel: String,
        x: Any?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun insertRow() = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateRow() = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun deleteRow() = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun refreshRow() = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun cancelRowUpdates() = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun moveToInsertRow() = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun moveToCurrentRow() = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateRef(columnIndex: Int, x: Ref?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateRef(columnLabel: String, x: Ref?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBlob(columnIndex: Int, x: Blob?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBlob(columnLabel: String, x: Blob?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBlob(
        columnIndex: Int,
        inputStream: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBlob(
        columnLabel: String,
        inputStream: InputStream?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBlob(
        columnIndex: Int,
        inputStream: InputStream?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateBlob(
        columnLabel: String,
        inputStream: InputStream?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateClob(columnIndex: Int, x: Clob?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateClob(columnLabel: String, x: Clob?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateClob(
        columnIndex: Int,
        reader: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateClob(
        columnLabel: String,
        reader: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateClob(columnIndex: Int, reader: Reader?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateClob(columnLabel: String, reader: Reader?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateArray(columnIndex: Int, x: java.sql.Array?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateArray(
        columnLabel: String,
        x: java.sql.Array?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateRowId(columnIndex: Int, x: RowId?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateRowId(columnLabel: String, x: RowId?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNString(columnIndex: Int, nString: String?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNString(
        columnLabel: String,
        nString: String?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNClob(columnIndex: Int, nClob: NClob?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNClob(columnLabel: String, nClob: NClob?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNClob(
        columnIndex: Int,
        reader: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNClob(
        columnLabel: String,
        reader: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNClob(columnIndex: Int, reader: Reader?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNClob(columnLabel: String, reader: Reader?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateSQLXML(columnIndex: Int, xmlObject: SQLXML?) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateSQLXML(
        columnLabel: String,
        xmlObject: SQLXML?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNCharacterStream(
        columnIndex: Int,
        x: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNCharacterStream(
        columnLabel: String,
        reader: Reader?,
        length: Long
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNCharacterStream(
        columnIndex: Int,
        x: Reader?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun updateNCharacterStream(
        columnLabel: String,
        reader: Reader?
    ) = throw SQLFeatureNotSupportedException(READ_ONLY_ERROR)

    override fun <T> unwrap(iface: Class<T>): T = if (iface.isAssignableFrom(this::class.java)) {
        @Suppress("UNCHECKED_CAST")
        this as T
    } else if (iface.isAssignableFrom(ICursor::class.java)) {
        @Suppress("UNCHECKED_CAST")
        cursor as T
    } else {
        throw SQLException("Cannot unwrap to ${iface.name}")
    }

    override fun isWrapperFor(
        iface: Class<*>
    ): Boolean = iface.isAssignableFrom(this::class.java) || iface.isAssignableFrom(ICursor::class.java)

    private fun checkClosed() {
        if (cursor.isClosed()) {
            throw SQLException("ResultSet is closed")
        }
    }

    private fun checkScrollable() {
        if (resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
            throw SQLException("ResultSet is TYPE_FORWARD_ONLY and does not support this operation")
        }
    }

    private fun validateColumnIndex(columnIndex: Int) {
        if (columnIndex < 1 || columnIndex > cursor.columnCount) {
            throw SQLException("Column index $columnIndex is out of range (1, ${cursor.columnCount})")
        }
    }
}
