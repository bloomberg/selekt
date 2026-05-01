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
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TypeMappingTest {
    @Test
    fun selektToJdbcTypeMapping() {
        assertEquals(Types.BIGINT, TypeMapping.toJdbcType(ColumnType.INTEGER))
        assertEquals(Types.DOUBLE, TypeMapping.toJdbcType(ColumnType.FLOAT))
        assertEquals(Types.VARCHAR, TypeMapping.toJdbcType(ColumnType.STRING))
        assertEquals(Types.VARBINARY, TypeMapping.toJdbcType(ColumnType.BLOB))
        assertEquals(Types.NULL, TypeMapping.toJdbcType(ColumnType.NULL))
    }

    @Test
    fun jdbcToSelektTypeMapping() {
        assertEquals(ColumnType.INTEGER, TypeMapping.toSelektType(Types.INTEGER))
        assertEquals(ColumnType.INTEGER, TypeMapping.toSelektType(Types.BIGINT))
        assertEquals(ColumnType.INTEGER, TypeMapping.toSelektType(Types.BOOLEAN))

        assertEquals(ColumnType.FLOAT, TypeMapping.toSelektType(Types.DOUBLE))
        assertEquals(ColumnType.FLOAT, TypeMapping.toSelektType(Types.FLOAT))
        assertEquals(ColumnType.FLOAT, TypeMapping.toSelektType(Types.DECIMAL))

        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.VARCHAR))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.DATE))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.TIMESTAMP))

        assertEquals(ColumnType.BLOB, TypeMapping.toSelektType(Types.BINARY))
        assertEquals(ColumnType.BLOB, TypeMapping.toSelektType(Types.VARBINARY))

        assertEquals(ColumnType.NULL, TypeMapping.toSelektType(Types.NULL))
    }

    @Test
    fun convertFromSQLiteToBoolean() {
        assertEquals(true, TypeMapping.convertFromSQLite(1L, Types.BOOLEAN))
        assertEquals(false, TypeMapping.convertFromSQLite(0L, Types.BOOLEAN))
        assertEquals(true, TypeMapping.convertFromSQLite("true", Types.BOOLEAN))
        assertEquals(true, TypeMapping.convertFromSQLite("1", Types.BOOLEAN))
        assertEquals(false, TypeMapping.convertFromSQLite("false", Types.BOOLEAN))
        assertEquals(false, TypeMapping.convertFromSQLite("other", Types.BOOLEAN))
    }

    @Test
    fun convertFromSQLiteToInteger() {
        assertEquals(42, TypeMapping.convertFromSQLite(42L, Types.INTEGER))
        assertEquals(42, TypeMapping.convertFromSQLite("42", Types.INTEGER))
        assertEquals(0, TypeMapping.convertFromSQLite("invalid", Types.INTEGER))
        assertEquals(42, TypeMapping.convertFromSQLite(42.7, Types.INTEGER))
    }

    @Test
    fun convertFromSQLiteToString() {
        assertEquals("hello", TypeMapping.convertFromSQLite("hello", Types.VARCHAR))
        assertEquals("42", TypeMapping.convertFromSQLite(42L, Types.VARCHAR))
        assertEquals("3.14", TypeMapping.convertFromSQLite(3.14, Types.VARCHAR))
    }

    @Test
    fun convertFromSQLiteToDecimal() {
        assertEquals(3.14159, (TypeMapping.convertFromSQLite(3.14159, Types.DECIMAL) as BigDecimal).toDouble(), 0.000001)
        assertEquals(123.45, (TypeMapping.convertFromSQLite("123.45", Types.DECIMAL) as BigDecimal).toDouble(), 0.000001)
        assertEquals(BigDecimal.ZERO, TypeMapping.convertFromSQLite("invalid", Types.DECIMAL) as BigDecimal)
    }

    @Test
    fun convertFromSQLiteToDate() {
        assertTrue(TypeMapping.convertFromSQLite("2025-12-25", Types.DATE) is Date)
        assertNull(TypeMapping.convertFromSQLite("invalid-date", Types.DATE))
    }

    @Test
    fun convertFromSQLiteToTime() {
        assertTrue(TypeMapping.convertFromSQLite("10:30:45", Types.TIME) is Time)
        assertNull(TypeMapping.convertFromSQLite("invalid-time", Types.TIME))
    }

    @Test
    fun convertFromSQLiteToTimestamp() {
        listOf(
            "2025-12-25T10:30:45",
            "2025-12-25 10:30:45",
            "1640995200000"
        ).forEach {
            assertTrue(TypeMapping.convertFromSQLite(it, Types.TIMESTAMP) is Timestamp)
        }
        assertNull(TypeMapping.convertFromSQLite("invalid-timestamp", Types.TIMESTAMP))
    }

    @Test
    fun convertFromSQLiteToByteArray() {
        assertEquals(4, (TypeMapping.convertFromSQLite(byteArrayOf(1, 2, 3, 4), Types.VARBINARY) as ByteArray).size)
        assertTrue(
            (TypeMapping.convertFromSQLite("hello", Types.VARBINARY) as ByteArray)
                .contentEquals("hello".toByteArray(Charsets.UTF_8))
        )
    }

    @Test
    fun convertToSQLite() {
        assertEquals(1, TypeMapping.convertToSQLite(true))
        assertEquals(0, TypeMapping.convertToSQLite(false))
        assertEquals(42, TypeMapping.convertToSQLite(42))
        assertEquals(42L, TypeMapping.convertToSQLite(42L))
        assertEquals(3.14, TypeMapping.convertToSQLite(3.14))
        assertEquals("hello", TypeMapping.convertToSQLite("hello"))

        val bytes = byteArrayOf(1, 2, 3)
        assertEquals(bytes, TypeMapping.convertToSQLite(bytes))

        assertNull(TypeMapping.convertToSQLite(null))
    }

    @Test
    fun getJavaClassName() {
        assertEquals(Boolean::class.java.name, TypeMapping.getJavaClassName(Types.BOOLEAN))
        assertEquals(Int::class.java.name, TypeMapping.getJavaClassName(Types.INTEGER))
        assertEquals(Long::class.java.name, TypeMapping.getJavaClassName(Types.BIGINT))
        assertEquals(Double::class.java.name, TypeMapping.getJavaClassName(Types.DOUBLE))
        assertEquals(String::class.java.name, TypeMapping.getJavaClassName(Types.VARCHAR))
        assertEquals(ByteArray::class.java.name, TypeMapping.getJavaClassName(Types.VARBINARY))
    }

    @Test
    fun getJdbcTypeName() {
        assertEquals("BOOLEAN", TypeMapping.getJdbcTypeName(Types.BOOLEAN))
        assertEquals("INTEGER", TypeMapping.getJdbcTypeName(Types.INTEGER))
        assertEquals("VARCHAR", TypeMapping.getJdbcTypeName(Types.VARCHAR))
        assertEquals("VARBINARY", TypeMapping.getJdbcTypeName(Types.VARBINARY))
        assertEquals("NULL", TypeMapping.getJdbcTypeName(Types.NULL))
        assertEquals("OTHER", TypeMapping.getJdbcTypeName(999_999))
    }

    @Test
    fun getPrecisionAndScale() {
        assertEquals(19, TypeMapping.getPrecision(Types.BIGINT))
        assertEquals(0, TypeMapping.getScale(Types.BIGINT))

        assertEquals(15, TypeMapping.getPrecision(Types.DOUBLE))
        assertEquals(15, TypeMapping.getScale(Types.DOUBLE))

        assertEquals(0, TypeMapping.getPrecision(Types.VARCHAR))
        assertEquals(0, TypeMapping.getScale(Types.VARCHAR))
    }

    @Test
    fun convertFromSQLiteWithNullValue() {
        assertNull(TypeMapping.convertFromSQLite(null, Types.INTEGER))
        assertNull(TypeMapping.convertFromSQLite(null, Types.VARCHAR))
        assertNull(TypeMapping.convertFromSQLite(null, Types.BOOLEAN))
    }

    @Test
    fun convertFromSQLiteToTinyInt() {
        assertEquals(42.toByte(), TypeMapping.convertFromSQLite(42L, Types.TINYINT))
        assertEquals(42.toByte(), TypeMapping.convertFromSQLite("42", Types.TINYINT))
        assertEquals(0.toByte(), TypeMapping.convertFromSQLite("invalid", Types.TINYINT))
        assertEquals(0.toByte(), TypeMapping.convertFromSQLite(Any(), Types.TINYINT))
    }

    @Test
    fun convertFromSQLiteToSmallInt() {
        assertEquals(42.toShort(), TypeMapping.convertFromSQLite(42L, Types.SMALLINT))
        assertEquals(42.toShort(), TypeMapping.convertFromSQLite("42", Types.SMALLINT))
        assertEquals(0.toShort(), TypeMapping.convertFromSQLite("invalid", Types.SMALLINT))
        assertEquals(0.toShort(), TypeMapping.convertFromSQLite(Any(), Types.SMALLINT))
    }

    @Test
    fun convertFromSQLiteToBigInt() {
        assertEquals(42L, TypeMapping.convertFromSQLite(42, Types.BIGINT))
        assertEquals(42L, TypeMapping.convertFromSQLite("42", Types.BIGINT))
        assertEquals(0L, TypeMapping.convertFromSQLite("invalid", Types.BIGINT))
        assertEquals(0L, TypeMapping.convertFromSQLite(Any(), Types.BIGINT))
    }

    @Test
    fun convertFromSQLiteToFloat() {
        assertEquals(3.14f, TypeMapping.convertFromSQLite(3.14, Types.FLOAT))
        assertEquals(42f, TypeMapping.convertFromSQLite(42L, Types.FLOAT))
        assertEquals(3.14f, TypeMapping.convertFromSQLite("3.14", Types.FLOAT))
        assertEquals(0f, TypeMapping.convertFromSQLite("invalid", Types.FLOAT))
        assertEquals(0f, TypeMapping.convertFromSQLite(Any(), Types.FLOAT))
    }

    @Test
    fun convertFromSQLiteToDouble() {
        assertEquals(3.14, TypeMapping.convertFromSQLite(3.14, Types.DOUBLE))
        assertEquals(42.0, TypeMapping.convertFromSQLite(42L, Types.DOUBLE))
        assertEquals(3.14, TypeMapping.convertFromSQLite("3.14", Types.DOUBLE))
        assertEquals(0.0, TypeMapping.convertFromSQLite("invalid", Types.DOUBLE))
        assertEquals(0.0, TypeMapping.convertFromSQLite(Any(), Types.DOUBLE))
    }

    @Test
    fun convertFromSQLiteToNumeric() {
        val resultOne = TypeMapping.convertFromSQLite(3.14159, Types.NUMERIC) as BigDecimal
        assertEquals(3.14159, resultOne.toDouble(), 0.000001)
        val resultTwo = TypeMapping.convertFromSQLite("123.45", Types.NUMERIC) as BigDecimal
        assertEquals(123.45, resultTwo.toDouble(), 0.000001)
        assertEquals(BigDecimal.ZERO, TypeMapping.convertFromSQLite("invalid", Types.NUMERIC))
        assertEquals(BigDecimal.ZERO, TypeMapping.convertFromSQLite(Any(), Types.NUMERIC))
    }

    @Test
    fun convertFromSQLiteDateFromNumber() {
        val epochMillis = 1_640_995_200_000L
        val date = TypeMapping.convertFromSQLite(epochMillis, Types.DATE) as Date
        assertEquals(epochMillis, date.time)
    }

    @Test
    fun convertFromSQLiteTimeFromNumber() {
        val epochMillis = 37_845_000L
        val time = TypeMapping.convertFromSQLite(epochMillis, Types.TIME) as Time
        assertEquals(epochMillis, time.time)
    }

    @Test
    fun convertFromSQLiteTimestampFromNumber() {
        val epochMillis = 1_640_995_200_000L
        val timestamp = TypeMapping.convertFromSQLite(epochMillis, Types.TIMESTAMP) as Timestamp
        assertEquals(epochMillis, timestamp.time)
    }

    @Test
    fun convertFromSQLiteDateWithTimestampFallback() {
        val date = TypeMapping.convertFromSQLite("2025-12-25 10:30:45", Types.DATE)
        assertTrue(date is Date)
    }

    @Test
    fun convertFromSQLiteUnknownType() {
        val value = "test"
        assertEquals(value, TypeMapping.convertFromSQLite(value, 999_999))
    }

    @Test
    fun convertFromSQLiteNonStringTypes() {
        assertEquals("test", TypeMapping.convertFromSQLite("test", Types.CHAR))
        assertEquals("test", TypeMapping.convertFromSQLite("test", Types.LONGVARCHAR))
        assertEquals("test", TypeMapping.convertFromSQLite("test", Types.NCHAR))
        assertEquals("test", TypeMapping.convertFromSQLite("test", Types.NVARCHAR))
        assertEquals("test", TypeMapping.convertFromSQLite("test", Types.LONGNVARCHAR))
        assertEquals("test", TypeMapping.convertFromSQLite("test", Types.CLOB))
        assertEquals("test", TypeMapping.convertFromSQLite("test", Types.NCLOB))
    }

    @Test
    fun convertBinaryTypes() {
        val bytes = byteArrayOf(1, 2, 3)
        assertTrue((TypeMapping.convertFromSQLite(bytes, Types.BINARY) as ByteArray).contentEquals(bytes))
        assertTrue((TypeMapping.convertFromSQLite(bytes, Types.LONGVARBINARY) as ByteArray).contentEquals(bytes))
        assertEquals(0, (TypeMapping.convertFromSQLite(Any(), Types.BINARY) as ByteArray).size)
    }

    @Test
    fun convertToBooleanFromAlreadyBoolean() {
        assertEquals(true, TypeMapping.convertFromSQLite(true, Types.BOOLEAN))
        assertEquals(false, TypeMapping.convertFromSQLite(false, Types.BOOLEAN))
    }

    @Test
    fun convertToSQLiteFromByte() {
        assertEquals(42, TypeMapping.convertToSQLite(42.toByte()))
    }

    @Test
    fun convertToSQLiteFromShort() {
        assertEquals(42, TypeMapping.convertToSQLite(42.toShort()))
    }

    @Test
    fun convertToSQLiteFromFloat() {
        assertEquals(3.14f.toDouble(), TypeMapping.convertToSQLite(3.14f))
    }

    @Test
    fun convertToSQLiteFromBigDecimal() {
        assertEquals(123.45, TypeMapping.convertToSQLite(BigDecimal("123.45")))
    }

    @Test
    fun convertToSQLiteFromDate() {
        val date = Date.valueOf("2025-12-25")
        assertEquals("2025-12-25", TypeMapping.convertToSQLite(date))
    }

    @Test
    fun convertToSQLiteFromTime() {
        val time = Time.valueOf("10:30:45")
        assertEquals("10:30:45", TypeMapping.convertToSQLite(time))
    }

    @Test
    fun convertToSQLiteFromTimestamp() {
        val timestamp = Timestamp.valueOf("2025-12-25 10:30:45")
        assertEquals("2025-12-25 10:30:45.0", TypeMapping.convertToSQLite(timestamp))
    }

    @Test
    fun convertToSQLiteFromLocalDate() {
        val localDate = java.time.LocalDate.of(2025, 12, 25)
        assertEquals("2025-12-25", TypeMapping.convertToSQLite(localDate))
    }

    @Test
    fun convertToSQLiteFromLocalTime() {
        val localTime = java.time.LocalTime.of(10, 30, 45)
        assertEquals("10:30:45", TypeMapping.convertToSQLite(localTime))
    }

    @Test
    fun convertToSQLiteFromLocalDateTime() {
        val localDateTime = java.time.LocalDateTime.of(2025, 12, 25, 10, 30, 45)
        assertEquals("2025-12-25T10:30:45", TypeMapping.convertToSQLite(localDateTime))
    }

    @Test
    fun convertToSQLiteFromUnknownObject() {
        val obj = object : Any() {
            override fun toString() = "custom"
        }
        assertEquals("custom", TypeMapping.convertToSQLite(obj))
    }

    @Test
    fun jdbcToSelektTypeAdditionalMappings() {
        assertEquals(ColumnType.INTEGER, TypeMapping.toSelektType(Types.TINYINT))
        assertEquals(ColumnType.INTEGER, TypeMapping.toSelektType(Types.SMALLINT))
        assertEquals(ColumnType.FLOAT, TypeMapping.toSelektType(Types.REAL))
        assertEquals(ColumnType.FLOAT, TypeMapping.toSelektType(Types.NUMERIC))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.CHAR))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.LONGVARCHAR))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.NCHAR))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.NVARCHAR))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.LONGNVARCHAR))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.CLOB))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.NCLOB))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.TIME))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(Types.TIMESTAMP_WITH_TIMEZONE))
        assertEquals(ColumnType.BLOB, TypeMapping.toSelektType(Types.LONGVARBINARY))
        assertEquals(ColumnType.BLOB, TypeMapping.toSelektType(Types.BLOB))
        assertEquals(ColumnType.STRING, TypeMapping.toSelektType(999_999)) // Unknown type defaults to STRING
    }

    @Test
    fun getJavaClassNameAdditionalTypes() {
        assertEquals(Byte::class.java.name, TypeMapping.getJavaClassName(Types.TINYINT))
        assertEquals(Short::class.java.name, TypeMapping.getJavaClassName(Types.SMALLINT))
        assertEquals(Float::class.java.name, TypeMapping.getJavaClassName(Types.FLOAT))
        assertEquals(Float::class.java.name, TypeMapping.getJavaClassName(Types.REAL))
        assertEquals(BigDecimal::class.java.name, TypeMapping.getJavaClassName(Types.NUMERIC))
        assertEquals(BigDecimal::class.java.name, TypeMapping.getJavaClassName(Types.DECIMAL))
        assertEquals(String::class.java.name, TypeMapping.getJavaClassName(Types.CHAR))
        assertEquals(String::class.java.name, TypeMapping.getJavaClassName(Types.LONGVARCHAR))
        assertEquals(String::class.java.name, TypeMapping.getJavaClassName(Types.NCHAR))
        assertEquals(String::class.java.name, TypeMapping.getJavaClassName(Types.NVARCHAR))
        assertEquals(String::class.java.name, TypeMapping.getJavaClassName(Types.LONGNVARCHAR))
        assertEquals(String::class.java.name, TypeMapping.getJavaClassName(Types.CLOB))
        assertEquals(String::class.java.name, TypeMapping.getJavaClassName(Types.NCLOB))
        assertEquals(Date::class.java.name, TypeMapping.getJavaClassName(Types.DATE))
        assertEquals(Time::class.java.name, TypeMapping.getJavaClassName(Types.TIME))
        assertEquals(Timestamp::class.java.name, TypeMapping.getJavaClassName(Types.TIMESTAMP))
        assertEquals(Timestamp::class.java.name, TypeMapping.getJavaClassName(Types.TIMESTAMP_WITH_TIMEZONE))
        assertEquals(ByteArray::class.java.name, TypeMapping.getJavaClassName(Types.BINARY))
        assertEquals(ByteArray::class.java.name, TypeMapping.getJavaClassName(Types.LONGVARBINARY))
        assertEquals(java.sql.Blob::class.java.name, TypeMapping.getJavaClassName(Types.BLOB))
        assertEquals(String::class.java.name, TypeMapping.getJavaClassName(999_999))
    }

    @Test
    fun getJdbcTypeNameAllTypes() {
        assertEquals("TINYINT", TypeMapping.getJdbcTypeName(Types.TINYINT))
        assertEquals("SMALLINT", TypeMapping.getJdbcTypeName(Types.SMALLINT))
        assertEquals("BIGINT", TypeMapping.getJdbcTypeName(Types.BIGINT))
        assertEquals("REAL", TypeMapping.getJdbcTypeName(Types.REAL))
        assertEquals("FLOAT", TypeMapping.getJdbcTypeName(Types.FLOAT))
        assertEquals("DOUBLE", TypeMapping.getJdbcTypeName(Types.DOUBLE))
        assertEquals("NUMERIC", TypeMapping.getJdbcTypeName(Types.NUMERIC))
        assertEquals("DECIMAL", TypeMapping.getJdbcTypeName(Types.DECIMAL))
        assertEquals("CHAR", TypeMapping.getJdbcTypeName(Types.CHAR))
        assertEquals("LONGVARCHAR", TypeMapping.getJdbcTypeName(Types.LONGVARCHAR))
        assertEquals("NCHAR", TypeMapping.getJdbcTypeName(Types.NCHAR))
        assertEquals("NVARCHAR", TypeMapping.getJdbcTypeName(Types.NVARCHAR))
        assertEquals("LONGNVARCHAR", TypeMapping.getJdbcTypeName(Types.LONGNVARCHAR))
        assertEquals("DATE", TypeMapping.getJdbcTypeName(Types.DATE))
        assertEquals("TIME", TypeMapping.getJdbcTypeName(Types.TIME))
        assertEquals("TIMESTAMP", TypeMapping.getJdbcTypeName(Types.TIMESTAMP))
        assertEquals("TIMESTAMP_WITH_TIMEZONE", TypeMapping.getJdbcTypeName(Types.TIMESTAMP_WITH_TIMEZONE))
        assertEquals("BINARY", TypeMapping.getJdbcTypeName(Types.BINARY))
        assertEquals("LONGVARBINARY", TypeMapping.getJdbcTypeName(Types.LONGVARBINARY))
        assertEquals("BLOB", TypeMapping.getJdbcTypeName(Types.BLOB))
        assertEquals("CLOB", TypeMapping.getJdbcTypeName(Types.CLOB))
        assertEquals("NCLOB", TypeMapping.getJdbcTypeName(Types.NCLOB))
    }

    @Test
    fun getPrecisionAllTypes() {
        assertEquals(1, TypeMapping.getPrecision(Types.BOOLEAN))
        assertEquals(3, TypeMapping.getPrecision(Types.TINYINT))
        assertEquals(5, TypeMapping.getPrecision(Types.SMALLINT))
        assertEquals(10, TypeMapping.getPrecision(Types.INTEGER))
        assertEquals(7, TypeMapping.getPrecision(Types.FLOAT))
        assertEquals(7, TypeMapping.getPrecision(Types.REAL))
        assertEquals(10, TypeMapping.getPrecision(Types.DATE))
        assertEquals(8, TypeMapping.getPrecision(Types.TIME))
        assertEquals(23, TypeMapping.getPrecision(Types.TIMESTAMP))
        assertEquals(0, TypeMapping.getPrecision(999_999))
    }

    @Test
    fun getScaleAllTypes() {
        assertEquals(7, TypeMapping.getScale(Types.FLOAT))
        assertEquals(7, TypeMapping.getScale(Types.REAL))
        assertEquals(15, TypeMapping.getScale(Types.DOUBLE))
        assertEquals(0, TypeMapping.getScale(Types.INTEGER))
        assertEquals(0, TypeMapping.getScale(Types.VARCHAR))
        assertEquals(0, TypeMapping.getScale(999_999))
    }

    @Test
    fun convertTimestampWithTimezone() {
        val timestampStr = "2025-12-25T10:30:45"
        assertTrue(TypeMapping.convertFromSQLite(timestampStr, Types.TIMESTAMP_WITH_TIMEZONE) is Timestamp)
    }

    @Test
    fun convertFromSQLiteDateFromObject() {
        assertNull(TypeMapping.convertFromSQLite(Any(), Types.DATE))
    }

    @Test
    fun convertFromSQLiteTimeFromObject() {
        assertNull(TypeMapping.convertFromSQLite(Any(), Types.TIME))
    }

    @Test
    fun convertFromSQLiteTimestampFromObject() {
        assertNull(TypeMapping.convertFromSQLite(Any(), Types.TIMESTAMP))
    }

    @Test
    fun convertFromSQLiteBooleanFromObject() {
        assertEquals(false, TypeMapping.convertFromSQLite(Any(), Types.BOOLEAN))
    }
}
