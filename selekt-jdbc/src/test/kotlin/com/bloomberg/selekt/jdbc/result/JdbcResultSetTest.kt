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
import com.bloomberg.selekt.jdbc.statement.JdbcStatement
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.NClob
import java.sql.Ref
import java.sql.ResultSet
import java.sql.RowId
import java.sql.SQLException
import java.sql.SQLXML
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class JdbcResultSetTest {
    private lateinit var mockCursor: ICursor
    private lateinit var mockStatement: JdbcStatement
    private lateinit var resultSet: JdbcResultSet

    @BeforeEach
    fun setUp() {
        mockCursor = mock<ICursor> {
            whenever(it.columnCount) doReturn 4
            whenever(it.columnNames()) doReturn arrayOf("id", "name", "age", "balance")

            whenever(it.columnName(0)) doReturn "id"
            whenever(it.columnName(1)) doReturn "name"
            whenever(it.columnName(2)) doReturn "age"
            whenever(it.columnName(3)) doReturn "balance"

            whenever(it.columnIndex(any())) doReturn -1
            whenever(it.columnIndex("id")) doReturn 0
            whenever(it.columnIndex("name")) doReturn 1
            whenever(it.columnIndex("age")) doReturn 2
            whenever(it.columnIndex("balance")) doReturn 3

            whenever(it.type(0)) doReturn ColumnType.INTEGER
            whenever(it.type(1)) doReturn ColumnType.STRING
            whenever(it.type(2)) doReturn ColumnType.INTEGER
            whenever(it.type(3)) doReturn ColumnType.FLOAT
        }
        mockStatement = mock<JdbcStatement>()
        resultSet = JdbcResultSet(mockCursor, mockStatement)
    }

    @Test
    fun next() {
        whenever(mockCursor.moveToNext()).doReturn(true, true, false)
        resultSet.run {
            assertTrue(next())
            assertTrue(next())
            assertFalse(next())
        }
    }

    @Test
    fun getIntByIndex() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getInt(0)) doReturn 42
        }
        assertEquals(42, resultSet.getInt(1))
    }

    @Test
    fun getIntByName() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(mockCursor.getInt(0)) doReturn 42
        }
        assertEquals(42, resultSet.getInt("id"))
    }

    @Test
    fun getStringByIndex() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "John Doe"
        }
        assertEquals("John Doe", resultSet.getString(2))
    }

    @Test
    fun getStringByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "John Doe"
        }
        assertEquals("John Doe", resultSet.getString("name"))
    }

    @Test
    fun getDoubleByIndex() {
        mockCursor.run {
            whenever(isNull(3)) doReturn false
            whenever(getDouble(3)) doReturn 123.45
        }
        assertEquals(123.45, resultSet.getDouble(4), 0.001)
    }

    @Test
    fun getBooleanFromInteger() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getLong(0)) doReturn 1L
        }
        assertTrue(resultSet.getBoolean(1))
        whenever(mockCursor.getLong(0)) doReturn 0L
        assertFalse(resultSet.getBoolean(1))
    }

    @Test
    fun getBooleanFromString() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getLong(1)) doThrow IllegalArgumentException("Not a number")
            whenever(getString(1)) doReturn "true"
        }
        assertTrue(resultSet.getBoolean(2))
        whenever(mockCursor.getString(1)) doReturn "false"
        assertFalse(resultSet.getBoolean(2))
    }

    @Test
    fun getBytes() {
        val testBytes = byteArrayOf(1, 2, 3, 4)
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getBlob(1)) doReturn testBytes
        }
        assertEquals(testBytes.toList(), resultSet.getBytes(2)?.toList())
    }

    @Test
    fun getDate() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "2025-12-25"
        }
        assertEquals(Date.valueOf("2025-12-25"), resultSet.getDate(2))
    }

    @Test
    fun getTime() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "10:30:45"
        }
        assertEquals(Time.valueOf("10:30:45"), resultSet.getTime(2))
    }

    @Test
    fun getTimestamp() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "2025-12-25 10:30:45"
        }
        assertEquals(Timestamp.valueOf("2025-12-25 10:30:45"), resultSet.getTimestamp(2))
    }

    @Test
    fun getBigDecimal() {
        mockCursor.run {
            whenever(isNull(3)) doReturn false
            whenever(getDouble(3)) doReturn 123.45
        }
        assertEquals(BigDecimal.valueOf(123.45), resultSet.getBigDecimal(4))
    }

    @Test
    fun getObject() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getLong(0)) doReturn 42L
        }
        assertEquals(42L, resultSet.getObject(1))
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "test"
        }
        assertEquals("test", resultSet.getObject(2))
    }

    @Test
    fun wasNull() {
        whenever(mockCursor.isNull(0)) doReturn true
        resultSet.run {
            getInt(1)
            assertTrue(wasNull())
        }
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "test"
        }
        resultSet.run {
            getString(2)
            assertFalse(wasNull())
        }
    }

    @Test
    fun findColumn(): Unit = resultSet.run {
        assertEquals(1, findColumn("id"))
        assertEquals(2, findColumn("name"))
        assertEquals(3, findColumn("age"))
        assertEquals(4, findColumn("balance"))
        assertFailsWith<SQLException> {
            findColumn("nonexistent")
        }
    }

    @Test
    fun invalidColumnIndex() {
        assertFailsWith<SQLException> {
            resultSet.getInt(0)
        }
        assertFailsWith<SQLException> {
            resultSet.getString(5)
        }
    }

    @Test
    fun metaData() {
        assertEquals(4, resultSet.metaData.columnCount)
    }

    @Test
    fun getStatement() {
        assertEquals(mockStatement, resultSet.statement)
    }

    @Test
    fun closure() {
        whenever(mockCursor.isClosed()) doReturn false
        assertFalse(resultSet.isClosed)
        whenever(mockCursor.isClosed()) doReturn true
        resultSet.close()
        assertTrue(resultSet.isClosed)
        assertFailsWith<SQLException> {
            resultSet.next()
        }
        assertFailsWith<SQLException> {
            resultSet.getString(1)
        }
    }

    @Test
    fun cursorMovement() {
        assertEquals(ResultSet.FETCH_FORWARD, resultSet.fetchDirection)
        assertFailsWith<SQLException> {
            resultSet.fetchDirection = ResultSet.FETCH_REVERSE
        }
    }

    @Test
    fun unsupportedOperations(): Unit = resultSet.run {
        assertFailsWith<SQLException> {
            previous()
        }
        assertFailsWith<SQLException> {
            first()
        }
        assertFailsWith<SQLException> {
            last()
        }
        assertFailsWith<SQLException> {
            absolute(1)
        }
        assertFailsWith<SQLException> {
            relative(1)
        }
        assertFailsWith<SQLException> {
            updateString(1, "test")
        }
        assertFailsWith<SQLException> {
            updateString("label", "test")
        }
        assertFailsWith<SQLException> {
            insertRow()
        }
        assertFailsWith<SQLException> {
            deleteRow()
        }
    }

    @Test
    fun resultSetProperties(): Unit = resultSet.run {
        assertEquals(ResultSet.TYPE_FORWARD_ONLY, type)
        assertEquals(ResultSet.CONCUR_READ_ONLY, concurrency)
        assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, holdability)
    }

    @Test
    fun warnings(): Unit = resultSet.run {
        assertNull(warnings)
        clearWarnings()
    }

    @Test
    fun rowOperations() {
        mockCursor.run {
            whenever(position()) doReturn 0
            whenever(isBeforeFirst()) doReturn true
            whenever(isFirst()) doReturn false
            whenever(isLast()) doReturn false
            whenever(isAfterLast()) doReturn false
        }
        resultSet.run {
            assertEquals(0, row)
            assertFalse(isFirst())
            assertFalse(isLast())
            assertTrue(isBeforeFirst())
            assertFalse(isAfterLast())
        }
    }

    @Test
    fun fetchSize(): Unit = resultSet.run {
        assertEquals(0, fetchSize)
        fetchSize = 100
        assertEquals(100, fetchSize)
        assertFailsWith<SQLException> {
            fetchSize = -1
        }
    }

    @Test
    fun wrapperInterface(): Unit = resultSet.run {
        assertTrue(isWrapperFor(JdbcResultSet::class.java))
        assertFalse(isWrapperFor(String::class.java))
        assertEquals(resultSet, unwrap(JdbcResultSet::class.java))
        assertFailsWith<SQLException> {
            resultSet.unwrap(String::class.java)
        }
    }

    @Test
    fun cursorName() {
        assertFailsWith<SQLException> {
            resultSet.cursorName
        }
    }

    @Test
    fun nullValues() {
        whenever(mockCursor.isNull(0)) doReturn true
        resultSet.run {
            assertEquals(0, getInt(1))
            assertTrue(wasNull())
            assertNull(getString(1))
            assertTrue(wasNull())
            assertNull(getObject(1))
            assertTrue(wasNull())
        }
    }

    @Test
    fun typeConversions() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getInt(0)) doReturn 42
            whenever(getShort(0)) doReturn 42.toShort()
            whenever(getFloat(0)) doReturn 42.0f
        }
        resultSet.run {
            assertEquals(42.toByte(), getByte(1))
            assertEquals(42.toShort(), getShort(1))
            assertEquals(42.toFloat(), getFloat(1), 0.001f)
        }
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "123"
        }
        assertEquals(123, resultSet.getInt(2))
        whenever(mockCursor.getString(1)) doReturn "123.45"
        assertEquals(123.45, resultSet.getDouble(2), 0.001)
    }

    @Test
    fun getLongByIndex() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getLong(0)) doReturn 123_456_789L
        }
        assertEquals(123_456_789L, resultSet.getLong(1))
    }

    @Test
    fun getLongByName() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getLong(0)) doReturn 987_654_321L
        }
        assertEquals(987_654_321L, resultSet.getLong("id"))
    }

    @Test
    fun getFloatByName() {
        mockCursor.run {
            whenever(isNull(3)) doReturn false
            whenever(getFloat(3)) doReturn 99.5f
        }
        assertEquals(99.5f, resultSet.getFloat("balance"), 0.001f)
    }

    @Test
    fun getDoubleByName() {
        mockCursor.run {
            whenever(isNull(3)) doReturn false
            whenever(getDouble(3)) doReturn 555.55
        }
        assertEquals(555.55, resultSet.getDouble("balance"), 0.001)
    }

    @Test
    fun getByteByName() {
        mockCursor.run {
            whenever(isNull(2)) doReturn false
            whenever(getInt(2)) doReturn 25
        }
        assertEquals(25.toByte(), resultSet.getByte("age"))
    }

    @Test
    fun getShortByName() {
        mockCursor.run {
            whenever(isNull(2)) doReturn false
            whenever(getShort(2)) doReturn 300.toShort()
        }
        assertEquals(300.toShort(), resultSet.getShort("age"))
    }

    @Test
    fun getBigDecimalByName() {
        mockCursor.run {
            whenever(isNull(3)) doReturn false
            whenever(getDouble(3)) doReturn 999.99
        }
        assertEquals(BigDecimal.valueOf(999.99), resultSet.getBigDecimal("balance"))
    }

    @Test
    fun getBigDecimalNullValue() {
        whenever(mockCursor.isNull(3)) doReturn true
        resultSet.run {
            assertNull(getBigDecimal(4))
            assertTrue(wasNull())
        }
    }

    @Test
    fun getBigDecimalWithScale() {
        mockCursor.run {
            whenever(isNull(3)) doReturn false
            whenever(getDouble(3)) doReturn 123.456789
        }
        @Suppress("DEPRECATION")
        val result = resultSet.getBigDecimal(4, 2)
        assertEquals(BigDecimal.valueOf(123.46), result)
    }

    @Test
    fun getBigDecimalWithScaleByName() {
        mockCursor.run {
            whenever(isNull(3)) doReturn false
            whenever(getDouble(3)) doReturn 123.456789
        }
        @Suppress("DEPRECATION")
        val result = resultSet.getBigDecimal("balance", 2)
        assertEquals(BigDecimal.valueOf(123.46), result)
    }

    @Test
    fun getBytesByName() {
        val bytes = byteArrayOf(5, 6, 7, 8)
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getBlob(1)) doReturn bytes
        }
        assertEquals(bytes.toList(), resultSet.getBytes("name")?.toList())
    }

    @Test
    fun getDateByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "2024-01-15"
        }
        assertEquals(Date.valueOf("2024-01-15"), resultSet.getDate("name"))
    }

    @Test
    fun getDateWithCalendar() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "2024-01-15"
        }
        assertEquals(Date.valueOf("2024-01-15"), resultSet.getDate(2, null))
        assertEquals(Date.valueOf("2024-01-15"), resultSet.getDate("name", null))
    }

    @Test
    fun getTimeByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "14:30:00"
        }
        assertEquals(Time.valueOf("14:30:00"), resultSet.getTime("name"))
    }

    @Test
    fun getTimeWithCalendar() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "14:30:00"
        }
        assertEquals(Time.valueOf("14:30:00"), resultSet.getTime(2, null))
        assertEquals(Time.valueOf("14:30:00"), resultSet.getTime("name", null))
    }

    @Test
    fun getTimestampByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "2024-01-15 14:30:00"
        }
        assertEquals(Timestamp.valueOf("2024-01-15 14:30:00"), resultSet.getTimestamp("name"))
    }

    @Test
    fun getTimestampWithCalendar() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "2024-01-15 14:30:00"
        }
        assertEquals(Timestamp.valueOf("2024-01-15 14:30:00"), resultSet.getTimestamp(2, null))
        assertEquals(Timestamp.valueOf("2024-01-15 14:30:00"), resultSet.getTimestamp("name", null))
    }

    @Test
    fun getDateNull() {
        whenever(mockCursor.isNull(1)) doReturn true
        assertNull(resultSet.getDate(2))
        assertTrue(resultSet.wasNull())
    }

    @Test
    fun getTimeNull() {
        whenever(mockCursor.isNull(1)) doReturn true
        assertNull(resultSet.getTime(2))
        assertTrue(resultSet.wasNull())
    }

    @Test
    fun getTimestampNull() {
        whenever(mockCursor.isNull(1)) doReturn true
        assertNull(resultSet.getTimestamp(2))
        assertTrue(resultSet.wasNull())
    }

    @Test
    fun getAsciiStream() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "test data"
        }
        resultSet.getAsciiStream(2).use {
            assertEquals("test data", it?.readBytes()?.toString(Charsets.US_ASCII))
        }
    }

    @Test
    fun getAsciiStreamByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "named test"
        }
        resultSet.getAsciiStream("name").use {
            assertEquals("named test", it?.readBytes()?.toString(Charsets.US_ASCII))
        }
    }

    @Test
    fun getUnicodeStream() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "unicode"
        }
        @Suppress("DEPRECATION")
        resultSet.getUnicodeStream(2).use {
            assertNotNull(it)
        }
    }

    @Test
    fun getUnicodeStreamByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "unicode"
        }
        @Suppress("DEPRECATION")
        resultSet.getUnicodeStream("name").use {
            assertNotNull(it)
        }
    }

    @Test
    fun getBinaryStream() {
        val bytes = byteArrayOf(1, 2, 3)
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getBlob(1)) doReturn bytes
        }
        resultSet.getBinaryStream(2).use {
            assertEquals(bytes.toList(), it?.readBytes()?.toList())
        }
    }

    @Test
    fun getBinaryStreamByName() {
        val bytes = byteArrayOf(4, 5, 6)
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getBlob(1)) doReturn bytes
        }
        resultSet.getBinaryStream("name").use {
            assertEquals(bytes.toList(), it?.readBytes()?.toList())
        }
    }

    @Test
    fun getCharacterStream() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "character stream"
        }
        resultSet.getCharacterStream(2).use {
            assertEquals("character stream", it?.readText())
        }
    }

    @Test
    fun getCharacterStreamByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "named stream"
        }
        resultSet.getCharacterStream("name").use {
            assertEquals("named stream", it?.readText())
        }
    }

    @Test
    fun getNCharacterStream() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "nchar stream"
        }
        resultSet.getNCharacterStream(2).use {
            assertEquals("nchar stream", it?.readText())
        }
    }

    @Test
    fun getNCharacterStreamByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "nchar named"
        }
        resultSet.getNCharacterStream("name").use {
            assertEquals("nchar named", it?.readText())
        }
    }

    @Test
    fun getClob() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "clob data"
        }
        assertEquals("clob data", resultSet.getClob(2)?.getSubString(1, "clob data".length))
    }

    @Test
    fun getClobByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "named clob"
        }
        assertEquals("named clob", resultSet.getClob("name")?.getSubString(1, "named clob".length))
    }

    @Test
    fun getClobNull(): Unit = resultSet.run {
        whenever(mockCursor.isNull(1)) doReturn true
        assertNull(getClob(2))
        assertTrue(wasNull())
        assertNull(getClob("name"))
        assertTrue(wasNull())
    }

    @Test
    fun getNString() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "nstring"
        }
        resultSet.run {
            assertEquals("nstring", getNString(2))
            assertEquals("nstring", getNString("name"))
        }
    }

    @Test
    fun getObjectByName() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "object by name"
        }
        assertEquals("object by name", resultSet.getObject("name"))
    }

    @Test
    fun getObjectWithType() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getLong(0)) doReturn 42L
        }
        resultSet.run {
            assertEquals(42, getObject(1, Int::class.java))
            assertEquals(42L, getObject(1, Long::class.java))
            assertEquals(42.0, getObject(1, Double::class.java))
            assertEquals(42.0f, getObject(1, Float::class.java))
            assertEquals("42", getObject(1, String::class.java))
            assertEquals(true, getObject(1, Boolean::class.java))
        }
    }

    @Test
    fun getObjectWithTypeByName() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getLong(0)) doReturn 100L
        }
        assertEquals(100, resultSet.getObject("id", Int::class.java))
    }

    @Test
    fun getObjectWithTypeNull() {
        whenever(mockCursor.isNull(0)) doReturn true
        assertNull(resultSet.getObject(1, String::class.java))
    }

    @Test
    fun getObjectWithTypeBoolean() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "true"
        }
        assertEquals(true, resultSet.getObject(2, Boolean::class.java))
        whenever(mockCursor.getString(1)) doReturn "1"
        assertEquals(true, resultSet.getObject(2, Boolean::class.java))
        whenever(mockCursor.getString(1)) doReturn "false"
        assertEquals(false, resultSet.getObject(2, Boolean::class.java))
    }

    @Test
    fun getObjectWithTypeLocalDate() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "2024-01-15"
        }
        assertEquals(LocalDate.parse("2024-01-15"), resultSet.getObject(2, LocalDate::class.java))
    }

    @Test
    fun getObjectWithTypeLocalTime() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "14:30:00"
        }
        assertEquals(LocalTime.parse("14:30:00"), resultSet.getObject(2, LocalTime::class.java))
    }

    @Test
    fun getObjectWithTypeLocalDateTime() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "2024-01-15T14:30:00"
        }
        assertEquals(LocalDateTime.parse("2024-01-15T14:30:00"), resultSet.getObject(2, LocalDateTime::class.java))
    }

    @Test
    fun getObjectWithTypeInvalid() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "test"
        }
        assertFailsWith<SQLException> {
            resultSet.getObject(2, Blob::class.java)
        }
    }

    @Test
    fun getObjectWithMap() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getLong(0)) doReturn 123L
        }
        assertEquals(123L, resultSet.getObject(1, mutableMapOf()))
        assertEquals(123L, resultSet.getObject("id", mutableMapOf()))
    }

    @Test
    fun getIntTypeConversions() {
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "456"
        }
        assertEquals(456, resultSet.getInt(2))
        mockCursor.run {
            whenever(isNull(3)) doReturn false
            whenever(getDouble(3)) doReturn 789.99
        }
        assertEquals(789, resultSet.getInt(4))
        whenever(mockCursor.isNull(0)) doReturn false
        assertEquals(0, resultSet.getInt(1))
    }

    @Test
    fun getDoubleTypeConversions() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getInt(0)) doReturn 100
        }
        assertEquals(100.0, resultSet.getDouble(1), 0.001)
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "200.5"
        }
        assertEquals(200.5, resultSet.getDouble(2), 0.001)
        whenever(mockCursor.isNull(3)) doReturn false
        assertEquals(0.0, resultSet.getDouble(4), 0.001)
    }

    @Test
    fun scrollableResultSet() {
        val scrollable = JdbcResultSet(
            mockCursor,
            mockStatement,
            ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY
        )
        whenever(mockCursor.moveToFirst()) doReturn true
        assertTrue(scrollable.first())
        whenever(mockCursor.moveToLast()) doReturn true
        assertTrue(scrollable.last())
        whenever(mockCursor.moveToPrevious()) doReturn true
        assertTrue(scrollable.previous())
        whenever(mockCursor.moveToPosition(4)) doReturn true
        assertTrue(scrollable.absolute(5))
        whenever(mockCursor.move(3)) doReturn true
        assertTrue(scrollable.relative(3))
        whenever(mockCursor.count) doReturn 10
        scrollable.afterLast()
        scrollable.beforeFirst()
    }

    @Test
    fun rowStatus(): Unit = resultSet.run {
        assertFalse(rowUpdated())
        assertFalse(rowInserted())
        assertFalse(rowDeleted())
    }

    @Test
    fun unsupportedFeatures(): Unit = resultSet.run {
        assertFailsWith<SQLException> { getNClob(1) }
        assertFailsWith<SQLException> { getNClob("name") }
        assertFailsWith<SQLException> { getSQLXML(1) }
        assertFailsWith<SQLException> { getSQLXML("name") }
        assertFailsWith<SQLException> { getURL(1) }
        assertFailsWith<SQLException> { getURL("name") }
        assertFailsWith<SQLException> { getArray(1) }
        assertFailsWith<SQLException> { getArray("name") }
        assertFailsWith<SQLException> { getRef(1) }
        assertFailsWith<SQLException> { getRef("name") }
        assertFailsWith<SQLException> { getRowId(1) }
        assertFailsWith<SQLException> { getRowId("name") }
    }

    @Test
    fun allUpdateOperations(): Unit = resultSet.run {
        assertFailsWith<SQLException> { updateNull(1) }
        assertFailsWith<SQLException> { updateNull("name") }
        assertFailsWith<SQLException> { updateBoolean(1, true) }
        assertFailsWith<SQLException> { updateBoolean("name", true) }
        assertFailsWith<SQLException> { updateByte(1, 1) }
        assertFailsWith<SQLException> { updateByte("name", 1) }
        assertFailsWith<SQLException> { updateShort(1, 1) }
        assertFailsWith<SQLException> { updateShort("name", 1) }
        assertFailsWith<SQLException> { updateInt(1, 1) }
        assertFailsWith<SQLException> { updateInt("name", 1) }
        assertFailsWith<SQLException> { updateLong(1, 1L) }
        assertFailsWith<SQLException> { updateLong("name", 1L) }
        assertFailsWith<SQLException> { updateFloat(1, 1.0f) }
        assertFailsWith<SQLException> { updateFloat("name", 1.0f) }
        assertFailsWith<SQLException> { updateDouble(1, 1.0) }
        assertFailsWith<SQLException> { updateDouble("name", 1.0) }
        assertFailsWith<SQLException> { updateBigDecimal(1, BigDecimal.ONE) }
        assertFailsWith<SQLException> { updateBigDecimal("name", BigDecimal.ONE) }
        assertFailsWith<SQLException> { updateBytes(1, byteArrayOf()) }
        assertFailsWith<SQLException> { updateBytes("name", byteArrayOf()) }
        assertFailsWith<SQLException> { updateDate(1, Date(0)) }
        assertFailsWith<SQLException> { updateDate("name", Date(0)) }
        assertFailsWith<SQLException> { updateTime(1, Time(0)) }
        assertFailsWith<SQLException> { updateTime("name", Time(0)) }
        assertFailsWith<SQLException> { updateTimestamp(1, Timestamp(0)) }
        assertFailsWith<SQLException> { updateTimestamp("name", Timestamp(0)) }
        assertFailsWith<SQLException> { updateRow() }
        assertFailsWith<SQLException> { refreshRow() }
        assertFailsWith<SQLException> { cancelRowUpdates() }
        assertFailsWith<SQLException> { moveToInsertRow() }
        assertFailsWith<SQLException> { moveToCurrentRow() }
    }

    @Test
    fun streamUpdateOperations(): Unit = resultSet.run {
        val stream = mock<InputStream>()
        val reader = mock<Reader>()
        assertFailsWith<SQLException> { updateAsciiStream(1, stream, 10) }
        assertFailsWith<SQLException> { updateAsciiStream("name", stream, 10) }
        assertFailsWith<SQLException> { updateAsciiStream(1, stream, 10L) }
        assertFailsWith<SQLException> { updateAsciiStream("name", stream, 10L) }
        assertFailsWith<SQLException> { updateAsciiStream(1, stream) }
        assertFailsWith<SQLException> { updateAsciiStream("name", stream) }
        assertFailsWith<SQLException> { updateBinaryStream(1, stream, 10) }
        assertFailsWith<SQLException> { updateBinaryStream("name", stream, 10) }
        assertFailsWith<SQLException> { updateBinaryStream(1, stream, 10L) }
        assertFailsWith<SQLException> { updateBinaryStream("name", stream, 10L) }
        assertFailsWith<SQLException> { updateBinaryStream(1, stream) }
        assertFailsWith<SQLException> { updateBinaryStream("name", stream) }
        assertFailsWith<SQLException> { updateCharacterStream(1, reader, 10) }
        assertFailsWith<SQLException> { updateCharacterStream("name", reader, 10) }
        assertFailsWith<SQLException> { updateCharacterStream(1, reader, 10L) }
        assertFailsWith<SQLException> { updateCharacterStream("name", reader, 10L) }
        assertFailsWith<SQLException> { updateCharacterStream(1, reader) }
        assertFailsWith<SQLException> { updateCharacterStream("name", reader) }
        assertFailsWith<SQLException> { updateNCharacterStream(1, reader, 10L) }
        assertFailsWith<SQLException> { updateNCharacterStream("name", reader, 10L) }
        assertFailsWith<SQLException> { updateNCharacterStream(1, reader) }
        assertFailsWith<SQLException> { updateNCharacterStream("name", reader) }
    }

    @Test
    fun lobUpdateOperations(): Unit = resultSet.run {
        val ref = mock<Ref>()
        val blob = mock<Blob>()
        val clob = mock<Clob>()
        val nclob = mock<NClob>()
        val array = mock<java.sql.Array>()
        val rowId = mock<RowId>()
        val sqlxml = mock<SQLXML>()
        val stream = mock<InputStream>()
        val reader = mock<Reader>()

        assertFailsWith<SQLException> { updateRef(1, ref) }
        assertFailsWith<SQLException> { updateRef("name", ref) }
        assertFailsWith<SQLException> { updateBlob(1, blob) }
        assertFailsWith<SQLException> { updateBlob("name", blob) }
        assertFailsWith<SQLException> { updateBlob(1, stream, 10L) }
        assertFailsWith<SQLException> { updateBlob("name", stream, 10L) }
        assertFailsWith<SQLException> { updateBlob(1, stream) }
        assertFailsWith<SQLException> { updateBlob("name", stream) }
        assertFailsWith<SQLException> { updateClob(1, clob) }
        assertFailsWith<SQLException> { updateClob("name", clob) }
        assertFailsWith<SQLException> { updateClob(1, reader, 10L) }
        assertFailsWith<SQLException> { updateClob("name", reader, 10L) }
        assertFailsWith<SQLException> { updateClob(1, reader) }
        assertFailsWith<SQLException> { updateClob("name", reader) }
        assertFailsWith<SQLException> { updateArray(1, array) }
        assertFailsWith<SQLException> { updateArray("name", array) }
        assertFailsWith<SQLException> { updateRowId(1, rowId) }
        assertFailsWith<SQLException> { updateRowId("name", rowId) }
        assertFailsWith<SQLException> { updateNString(1, "test") }
        assertFailsWith<SQLException> { updateNString("name", "test") }
        assertFailsWith<SQLException> { updateNClob(1, nclob) }
        assertFailsWith<SQLException> { updateNClob("name", nclob) }
        assertFailsWith<SQLException> { updateNClob(1, reader, 10L) }
        assertFailsWith<SQLException> { updateNClob("name", reader, 10L) }
        assertFailsWith<SQLException> { updateNClob(1, reader) }
        assertFailsWith<SQLException> { updateNClob("name", reader) }
        assertFailsWith<SQLException> { updateSQLXML(1, sqlxml) }
        assertFailsWith<SQLException> { updateSQLXML("name", sqlxml) }
        assertFailsWith<SQLException> { updateObject(1, "test", 1) }
        assertFailsWith<SQLException> { updateObject(1, "test") }
        assertFailsWith<SQLException> { updateObject("name", "test", 1) }
        assertFailsWith<SQLException> { updateObject("name", "test") }
    }

    @Test
    fun unwrapCursor(): Unit = resultSet.run {
        assertTrue(isWrapperFor(ICursor::class.java))
        assertEquals(mockCursor, unwrap(ICursor::class.java))
    }

    @Test
    fun getBooleanWithString() {
        mockCursor.apply {
            whenever(isNull(0)) doReturn false
            whenever(getLong(0)) doThrow RuntimeException("Not a long")
            whenever(getString(0)) doReturn "true"
        }
        assertTrue(resultSet.getBoolean(1))
        whenever(mockCursor.getString(0)) doReturn "false"
        assertFalse(resultSet.getBoolean(1))
        whenever(mockCursor.getString(0)) doReturn null
        assertFalse(resultSet.getBoolean(1))
    }

    @Test
    fun getRowReturnsPosition() {
        mockCursor.apply {
            whenever(isBeforeFirst()) doReturn false
            whenever(isAfterLast()) doReturn false
            whenever(position()) doReturn 0
        }
        assertEquals(1, resultSet.row)
    }

    @Test
    fun getRowWhenBeforeFirst() {
        mockCursor.apply {
            whenever(isBeforeFirst()) doReturn true
            whenever(isAfterLast()) doReturn false
        }
        assertEquals(0, resultSet.row)
    }

    @Test
    fun getRowWhenAfterLast() {
        mockCursor.apply {
            whenever(isBeforeFirst()) doReturn false
            whenever(isAfterLast()) doReturn true
        }
        assertEquals(0, resultSet.row)
    }

    @Test
    fun isLastOnForwardOnlyResultSet() {
        assertFalse(resultSet.isLast)
    }

    @Test
    fun rowDeletedInsertedUpdated(): Unit = resultSet.run {
        assertFalse(rowDeleted())
        assertFalse(rowInserted())
        assertFalse(rowUpdated())
    }

    @Test
    fun getCursorName() {
        assertFailsWith<SQLException> {
            resultSet.cursorName
        }
    }

    @Test
    fun getStatementWhenNull() {
        assertNull(JdbcResultSet(mockCursor, null).statement)
    }

    @Test
    fun getHoldability() {
        assertEquals(ResultSet.CLOSE_CURSORS_AT_COMMIT, resultSet.holdability)
    }

    @Test
    fun getWarnings(): Unit = resultSet.run {
        assertNull(warnings)
        clearWarnings()
        assertNull(warnings)
    }

    @Test
    fun unwrapInvalidClass() {
        assertFailsWith<SQLException> {
            resultSet.unwrap(String::class.java)
        }
    }

    @Test
    fun isWrapperForInvalidClass() {
        assertFalse(resultSet.isWrapperFor(String::class.java))
    }

    @Test
    fun findColumnInvalid() {
        whenever(mockCursor.columnNames()) doReturn arrayOf("id", "name")
        assertFailsWith<SQLException> {
            resultSet.findColumn("invalid_column")
        }
    }

    @Test
    fun getObjectWithNullType() {
        whenever(mockCursor.isNull(0)) doReturn true
        resultSet.run {
            assertNull(getObject(1, String::class.java))
            assertTrue(wasNull())
        }
    }

    @Test
    fun getURLThrows(): Unit = resultSet.run {
        assertFailsWith<SQLException> {
            getURL(1)
        }
        assertFailsWith<SQLException> {
            getURL("url")
        }
    }

    @Test
    fun getRefThrows(): Unit = resultSet.run {
        assertFailsWith<SQLException> {
            getRef(1)
        }
        assertFailsWith<SQLException> {
            getRef("name")
        }
    }

    @Test
    fun getBlob() {
        val blobData = "blob".toByteArray()
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getBlob(1)) doReturn blobData
        }
        assertNotNull(resultSet.getBlob(2)).let {
            assertEquals(blobData.size.toLong(), it.length())
            assertEquals(blobData[0], it.getBytes(1, 1)?.get(0))
        }
    }

    @Test
    fun getBlobByName() {
        val blobData = "name".toByteArray()
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getBlob(1)) doReturn blobData
        }
        assertNotNull(resultSet.getBlob("name")).let {
            assertEquals(blobData.size.toLong(), it.length())
            assertEquals(blobData[0], it.getBytes(1, 1)?.get(0))
        }
    }

    @Test
    fun getBlobNull(): Unit = resultSet.run {
        whenever(mockCursor.isNull(1)) doReturn true
        assertNull(getBlob(2))
        assertTrue(wasNull())
        assertNull(getBlob("name"))
        assertTrue(wasNull())
    }

    @Test
    fun getArrayThrows(): Unit = resultSet.run {
        assertFailsWith<SQLException> {
            getArray(1)
        }
        assertFailsWith<SQLException> {
            getArray("name")
        }
    }

    @Test
    fun getRowIdThrows(): Unit = resultSet.run {
        assertFailsWith<SQLException> {
            getRowId(1)
        }
        assertFailsWith<SQLException> {
            getRowId("name")
        }
    }

    @Test
    fun getNStringDelegatesToGetString() {
        mockCursor.apply {
            whenever(isNull(0)) doReturn false
            whenever(getString(0)) doReturn "test"
        }
        assertEquals("test", resultSet.getNString(1))
        mockCursor.apply {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "test"
        }
        assertEquals("test", resultSet.getNString("name"))
    }

    @Test
    fun getNCharacterStreamDelegatesToGetCharacterStream() {
        mockCursor.run {
            whenever(isNull(0)) doReturn false
            whenever(getString(0)) doReturn "test data"
        }
        assertNotNull(resultSet.getNCharacterStream(1))
        mockCursor.run {
            whenever(isNull(1)) doReturn false
            whenever(getString(1)) doReturn "test data"
        }
        assertNotNull(resultSet.getNCharacterStream("name"))
    }

    @Test
    fun getNClobThrows(): Unit = resultSet.run {
        assertFailsWith<SQLException> {
            getNClob(1)
        }
        assertFailsWith<SQLException> {
            getNClob("name")
        }
    }

    @Test
    fun getSQLXMLThrows(): Unit = resultSet.run {
        assertFailsWith<SQLException> {
            getSQLXML(1)
        }
        assertFailsWith<SQLException> {
            getSQLXML("name")
        }
    }

    @Test
    fun updateNCharacterStreamThrows(): Unit = resultSet.run {
        assertFailsWith<SQLException> {
            updateNCharacterStream(1, "data".reader(), 4L)
        }
        assertFailsWith<SQLException> {
            updateNCharacterStream("name", "data".reader(), 4L)
        }
        assertFailsWith<SQLException> {
            updateNCharacterStream(1, "data".reader())
        }
        assertFailsWith<SQLException> {
            updateNCharacterStream("name", "data".reader())
        }
    }

    @Test
    fun getObjectWithInvalidDateFormat() {
        mockCursor.apply {
            whenever(isNull(0)) doReturn false
            whenever(getString(0)) doReturn "invalid-date"
        }
        resultSet.run {
            assertFailsWith<SQLException> {
                getObject(1, LocalDate::class.java)
            }
            assertFailsWith<SQLException> {
                getObject(1, LocalTime::class.java)
            }
            assertFailsWith<SQLException> {
                getObject(1, LocalDateTime::class.java)
            }
        }
    }

    @Test
    fun getMetaDataReturnsCorrectInstance() {
        resultSet.metaData.let {
            assertNotNull(it)
            assertSame(it, resultSet.metaData)
        }
    }

    @Test
    fun setFetchDirection(): Unit = resultSet.run {
        fetchDirection = ResultSet.FETCH_FORWARD
        assertEquals(ResultSet.FETCH_FORWARD, fetchDirection)
        assertFailsWith<SQLException> {
            fetchDirection = ResultSet.FETCH_REVERSE
        }
    }

    @Test
    fun setFetchSize(): Unit = resultSet.run {
        fetchSize = 100
        assertEquals(100, fetchSize)
        assertFailsWith<SQLException> {
            fetchSize = -1
        }
    }

    @Test
    fun typeAndConcurrency(): Unit = resultSet.run {
        assertEquals(ResultSet.TYPE_FORWARD_ONLY, type)
        assertEquals(ResultSet.CONCUR_READ_ONLY, concurrency)
    }
}
