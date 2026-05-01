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

package com.bloomberg.selekt.jdbc.util

import com.bloomberg.selekt.ColumnType
import java.math.BigDecimal
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("TooGenericExceptionCaught")
internal object TypeMapping {
    private val digitsOnly = Regex("\\d+")
    private val logger: Logger = LoggerFactory.getLogger(TypeMapping::class.java)

    private const val BOOLEAN_PRECISION = 1
    private const val TINYINT_PRECISION = 3
    private const val SMALLINT_PRECISION = 5
    private const val INTEGER_PRECISION = 10
    private const val BIGINT_PRECISION = 19
    private const val FLOAT_PRECISION = 7
    private const val DOUBLE_PRECISION = 15
    private const val DATE_PRECISION = 10
    private const val TIME_PRECISION = 8
    private const val TIMESTAMP_PRECISION = 23

    private const val FLOAT_SCALE = 7
    private const val DOUBLE_SCALE = 15

    fun toJdbcType(columnType: ColumnType): Int = when (columnType) {
        ColumnType.INTEGER -> Types.BIGINT
        ColumnType.FLOAT -> Types.DOUBLE
        ColumnType.STRING -> Types.VARCHAR
        ColumnType.BLOB -> Types.VARBINARY
        ColumnType.NULL -> Types.NULL
    }

    fun toSelektType(jdbcType: Int): ColumnType = when (jdbcType) {
        Types.BOOLEAN,
        Types.TINYINT,
        Types.SMALLINT,
        Types.INTEGER,
        Types.BIGINT -> ColumnType.INTEGER
        Types.REAL,
        Types.FLOAT,
        Types.DOUBLE,
        Types.NUMERIC,
        Types.DECIMAL -> ColumnType.FLOAT
        Types.CHAR,
        Types.VARCHAR,
        Types.LONGVARCHAR,
        Types.NCHAR,
        Types.NVARCHAR,
        Types.LONGNVARCHAR,
        Types.CLOB,
        Types.NCLOB,
        Types.DATE,
        Types.TIME,
        Types.TIMESTAMP,
        Types.TIMESTAMP_WITH_TIMEZONE -> ColumnType.STRING
        Types.BINARY,
        Types.VARBINARY,
        Types.LONGVARBINARY,
        Types.BLOB -> ColumnType.BLOB
        Types.NULL -> ColumnType.NULL
        else -> ColumnType.STRING
    }

    fun getJavaClassName(jdbcType: Int): String = when (jdbcType) {
        Types.BOOLEAN -> Boolean::class.java.name
        Types.TINYINT -> Byte::class.java.name
        Types.SMALLINT -> Short::class.java.name
        Types.INTEGER -> Int::class.java.name
        Types.BIGINT -> Long::class.java.name
        Types.REAL, Types.FLOAT -> Float::class.java.name
        Types.DOUBLE -> Double::class.java.name
        Types.NUMERIC, Types.DECIMAL -> BigDecimal::class.java.name
        Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR,
        Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR,
        Types.CLOB, Types.NCLOB -> String::class.java.name
        Types.DATE -> Date::class.java.name
        Types.TIME -> Time::class.java.name
        Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> Timestamp::class.java.name
        Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> ByteArray::class.java.name
        Types.BLOB -> java.sql.Blob::class.java.name
        else -> String::class.java.name
    }

    fun convertFromSQLite(value: Any?, targetJdbcType: Int): Any? = if (value == null) {
        null
    } else {
        when (targetJdbcType) {
            Types.BOOLEAN -> convertToBoolean(value)
            Types.TINYINT -> convertToTinyInt(value)
            Types.SMALLINT -> convertToSmallInt(value)
            Types.INTEGER -> convertToInteger(value)
            Types.BIGINT -> convertToBigInt(value)
            Types.REAL, Types.FLOAT -> convertToFloat(value)
            Types.DOUBLE -> convertToDouble(value)
            Types.NUMERIC, Types.DECIMAL -> convertToDecimal(value)
            Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR,
            Types.NCHAR, Types.NVARCHAR, Types.LONGNVARCHAR,
            Types.CLOB, Types.NCLOB -> value.toString()
            Types.DATE -> convertToDate(value)
            Types.TIME -> convertToTime(value)
            Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> convertToTimestamp(value)
            Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> convertToBinary(value)
            else -> value
        }
    }

    private fun convertToBoolean(value: Any): Boolean = when (value) {
        is Boolean -> value
        is Number -> value.toLong() != 0L
        is String -> value.equals("true", ignoreCase = true) || value == "1"
        else -> false
    }

    private fun convertToTinyInt(value: Any): Byte = when (value) {
        is Number -> value.toByte()
        is String -> value.toByteOrNull() ?: 0.toByte()
        else -> 0.toByte()
    }

    private fun convertToSmallInt(value: Any): Short = when (value) {
        is Number -> value.toShort()
        is String -> value.toShortOrNull() ?: 0.toShort()
        else -> 0.toShort()
    }

    private fun convertToInteger(value: Any): Int = when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: 0
        else -> 0
    }

    private fun convertToBigInt(value: Any): Long = when (value) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull() ?: 0L
        else -> 0L
    }

    private fun convertToFloat(value: Any): Float = when (value) {
        is Number -> value.toFloat()
        is String -> value.toFloatOrNull() ?: 0f
        else -> 0f
    }

    private fun convertToDouble(value: Any): Double = when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }

    private fun convertToDecimal(value: Any): BigDecimal = when (value) {
        is Number -> BigDecimal.valueOf(value.toDouble())
        is String -> try {
            BigDecimal(value)
        } catch (_: NumberFormatException) {
            BigDecimal.ZERO
        }
        else -> BigDecimal.ZERO
    }

    private fun convertToDate(value: Any): Date? = when (value) {
        is String -> parseDate(value)
        is Number -> Date(value.toLong())
        else -> null
    }

    private fun convertToTime(value: Any): Time? = when (value) {
        is String -> parseTime(value)
        is Number -> Time(value.toLong())
        else -> null
    }

    private fun convertToTimestamp(value: Any): Timestamp? = when (value) {
        is String -> parseTimestamp(value)
        is Number -> Timestamp(value.toLong())
        else -> null
    }

    private fun convertToBinary(value: Any): ByteArray = when (value) {
        is ByteArray -> value
        is String -> value.toByteArray(Charsets.UTF_8)
        else -> ByteArray(0)
    }

    fun convertToSQLite(value: Any?): Any? = when (value) {
        null -> null
        is Boolean -> if (value) { 1 } else { 0 }
        is Byte -> value.toInt()
        is Short -> value.toInt()
        is Int -> value
        is Long -> value
        is Float -> value.toDouble()
        is Double -> value
        is BigDecimal -> value.toDouble()
        is String -> value
        is Date -> value.toString()
        is Time -> value.toString()
        is Timestamp -> value.toString()
        is LocalDate -> value.toString()
        is LocalTime -> value.toString()
        is LocalDateTime -> value.toString()
        is ByteArray -> value
        else -> value.toString()
    }

    fun getJdbcTypeName(jdbcType: Int): String = when (jdbcType) {
        Types.BOOLEAN -> "BOOLEAN"
        Types.TINYINT -> "TINYINT"
        Types.SMALLINT -> "SMALLINT"
        Types.INTEGER -> "INTEGER"
        Types.BIGINT -> "BIGINT"
        Types.REAL -> "REAL"
        Types.FLOAT -> "FLOAT"
        Types.DOUBLE -> "DOUBLE"
        Types.NUMERIC -> "NUMERIC"
        Types.DECIMAL -> "DECIMAL"
        Types.CHAR -> "CHAR"
        Types.VARCHAR -> "VARCHAR"
        Types.LONGVARCHAR -> "LONGVARCHAR"
        Types.NCHAR -> "NCHAR"
        Types.NVARCHAR -> "NVARCHAR"
        Types.LONGNVARCHAR -> "LONGNVARCHAR"
        Types.DATE -> "DATE"
        Types.TIME -> "TIME"
        Types.TIMESTAMP -> "TIMESTAMP"
        Types.TIMESTAMP_WITH_TIMEZONE -> "TIMESTAMP_WITH_TIMEZONE"
        Types.BINARY -> "BINARY"
        Types.VARBINARY -> "VARBINARY"
        Types.LONGVARBINARY -> "LONGVARBINARY"
        Types.BLOB -> "BLOB"
        Types.CLOB -> "CLOB"
        Types.NCLOB -> "NCLOB"
        Types.NULL -> "NULL"
        else -> "OTHER"
    }

    fun getPrecision(jdbcType: Int): Int = when (jdbcType) {
        Types.BOOLEAN -> BOOLEAN_PRECISION
        Types.TINYINT -> TINYINT_PRECISION
        Types.SMALLINT -> SMALLINT_PRECISION
        Types.INTEGER -> INTEGER_PRECISION
        Types.BIGINT -> BIGINT_PRECISION
        Types.REAL, Types.FLOAT -> FLOAT_PRECISION
        Types.DOUBLE -> DOUBLE_PRECISION
        Types.DATE -> DATE_PRECISION
        Types.TIME -> TIME_PRECISION
        Types.TIMESTAMP -> TIMESTAMP_PRECISION
        else -> 0
    }

    fun getScale(jdbcType: Int): Int = when (jdbcType) {
        Types.REAL, Types.FLOAT -> FLOAT_SCALE
        Types.DOUBLE -> DOUBLE_SCALE
        else -> 0
    }

    private fun parseDate(dateString: String): Date? = runCatching {
        Date.valueOf(LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE))
    }.getOrElse {
        runCatching {
            val timestamp = parseTimestamp(dateString)
            timestamp?.let { Date(it.time) }
        }.getOrElse {
            null
        }
    }

    private fun parseTime(timeString: String): Time? = runCatching {
        Time.valueOf(LocalTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_TIME))
    }.getOrElse {
        null
    }

    private fun parseTimestamp(timestampString: String): Timestamp? = timestampString.runCatching {
        when {
            contains('T') -> {
                val dateTime = LocalDateTime.parse(timestampString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                Timestamp.valueOf(dateTime)
            }
            contains(' ') -> Timestamp.valueOf(timestampString)
            matches(digitsOnly) -> Timestamp(timestampString.toLong())
            else -> null
        }
    }.getOrElse {
        when (it) {
            is DateTimeParseException -> {
                logger.debug("Failed to parse timestamp '{}': {}", timestampString, it.message)
                null
            }
            is NumberFormatException -> {
                logger.debug("Invalid numeric timestamp '{}': {}", timestampString, it.message)
                null
            }
            is IllegalArgumentException -> {
                logger.debug("Invalid timestamp format '{}': {}", timestampString, it.message)
                null
            }
            else -> throw it
        }
    }
}
