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
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Types
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class JdbcResultSetMetaDataTest {
    private lateinit var mockCursor: ICursor
    private lateinit var metaData: JdbcResultSetMetaData

    @BeforeEach
    fun setUp() {
        mockCursor = mock<ICursor> {
            whenever(it.columnCount) doReturn 4
            whenever(it.columnNames()) doReturn arrayOf("id", "name", "age", "balance")
            whenever(it.columnName(0)) doReturn "id"
            whenever(it.type(0)) doReturn ColumnType.INTEGER
            whenever(it.columnName(1)) doReturn "name"
            whenever(it.type(1)) doReturn ColumnType.STRING
            whenever(it.columnName(2)) doReturn "age"
            whenever(it.type(2)) doReturn ColumnType.INTEGER
            whenever(it.columnName(3)) doReturn "balance"
            whenever(it.type(3)) doReturn ColumnType.FLOAT
        }
        metaData = JdbcResultSetMetaData(mockCursor)
    }

    @Test
    fun getColumnCount() {
        assertEquals(4, metaData.columnCount)
    }

    @Test
    fun getColumnName(): Unit = metaData.run {
        assertEquals("id", getColumnName(1))
        assertEquals("name", getColumnName(2))
        assertEquals("age", getColumnName(3))
        assertEquals("balance", getColumnName(4))
    }

    @Test
    fun getColumnLabel(): Unit = metaData.run {
        assertEquals("id", getColumnLabel(1))
        assertEquals("name", getColumnLabel(2))
        assertEquals("age", getColumnLabel(3))
        assertEquals("balance", getColumnLabel(4))
    }

    @Test
    fun getColumnType(): Unit = metaData.run {
        assertEquals(Types.BIGINT, getColumnType(1))
        assertEquals(Types.VARCHAR, getColumnType(2))
        assertEquals(Types.BIGINT, getColumnType(3))
        assertEquals(Types.DOUBLE, getColumnType(4))
    }

    @Test
    fun getColumnTypeName(): Unit = metaData.run {
        assertEquals("BIGINT", getColumnTypeName(1))
        assertEquals("VARCHAR", getColumnTypeName(2))
        assertEquals("BIGINT", getColumnTypeName(3))
        assertEquals("DOUBLE", getColumnTypeName(4))
    }

    @Test
    fun getColumnClassName(): Unit = metaData.run {
        assertEquals(Long::class.java.name, getColumnClassName(1))
        assertEquals(String::class.java.name, getColumnClassName(2))
        assertEquals(Long::class.java.name, getColumnClassName(3))
        assertEquals(Double::class.java.name, getColumnClassName(4))
    }

    @Test
    fun getPrecision(): Unit = metaData.run {
        assertEquals(19, getPrecision(1))
        assertEquals(0, getPrecision(2))
        assertEquals(19, getPrecision(3))
        assertEquals(15, getPrecision(4))
    }

    @Test
    fun getScale(): Unit = metaData.run {
        assertEquals(0, getScale(1))
        assertEquals(0, getScale(2))
        assertEquals(0, getScale(3))
        assertEquals(15, getScale(4))
    }

    @Test
    fun getDisplaySize(): Unit = metaData.run {
        assertEquals(20, getColumnDisplaySize(1))
        assertEquals(Integer.MAX_VALUE, getColumnDisplaySize(2))
        assertEquals(20, getColumnDisplaySize(3))
        assertEquals(24, getColumnDisplaySize(4))
    }

    @Test
    fun isNullable(): Unit = metaData.run {
        assertEquals(ResultSetMetaData.columnNullableUnknown, isNullable(1))
        assertEquals(ResultSetMetaData.columnNullableUnknown, isNullable(2))
        assertEquals(ResultSetMetaData.columnNullableUnknown, isNullable(3))
        assertEquals(ResultSetMetaData.columnNullableUnknown, isNullable(4))
    }

    @Test
    fun columnProperties(): Unit = metaData.run {
        assertFalse(isAutoIncrement(1))
        assertFalse(isCaseSensitive(1))
        assertTrue(isSearchable(1))
        assertFalse(isCurrency(1))
        assertTrue(isReadOnly(1))
        assertFalse(isWritable(1))
        assertFalse(isDefinitelyWritable(1))
        assertTrue(isSigned(1))
    }

    @Test
    fun stringColumnProperties(): Unit = metaData.run {
        assertTrue(isCaseSensitive(2))
        assertFalse(isSigned(2))
    }

    @Test
    fun floatColumnProperties(): Unit = metaData.run {
        assertTrue(isSigned(4))
        assertFalse(isCurrency(4))
    }

    @Test
    fun invalidColumnIndex(): Unit = metaData.run {
        assertFailsWith<SQLException> {
            getColumnName(0)
        }
        assertFailsWith<SQLException> {
            getColumnType(5)
        }
        assertFailsWith<SQLException> {
            getColumnName(-1)
        }
    }

    @Test
    fun schemaAndCatalogInfo(): Unit = metaData.run {
        assertTrue(getSchemaName(1).isEmpty())
        assertTrue(getCatalogName(1).isEmpty())
        assertTrue(getTableName(1).isEmpty())
    }

    @Test
    fun wrapperInterface(): Unit = metaData.run {
        assertTrue(isWrapperFor(JdbcResultSetMetaData::class.java))
        assertTrue(isWrapperFor(ICursor::class.java))
        assertFalse(isWrapperFor(String::class.java))
        assertSame(this, unwrap(JdbcResultSetMetaData::class.java))
        assertSame(mockCursor, unwrap(ICursor::class.java))
        assertFailsWith<SQLException> {
            unwrap(String::class.java)
        }
    }

    @Test
    fun nullColumnType(): Unit = metaData.run {
        whenever(mockCursor.type(0)) doReturn ColumnType.NULL
        assertEquals(Types.NULL, getColumnType(1))
        assertEquals("NULL", getColumnTypeName(1))
    }

    @Test
    fun blobColumnType(): Unit = metaData.run {
        whenever(mockCursor.type(0)) doReturn ColumnType.BLOB
        assertEquals(Types.VARBINARY, getColumnType(1))
        assertEquals("VARBINARY", getColumnTypeName(1))
        assertEquals(ByteArray::class.java.name, getColumnClassName(1))
    }

    @Test
    fun columnIndexValidation(): Unit = metaData.run {
        val invalidIndices = listOf(0, -1, 5)
        val validMethods = listOf<(Int) -> Any>(
            ::getColumnName,
            ::getColumnLabel,
            ::getColumnType,
            ::getColumnTypeName,
            ::getColumnClassName,
            ::getPrecision,
            ::getScale,
            ::getColumnDisplaySize,
            ::isNullable,
            ::isAutoIncrement,
            ::isCaseSensitive,
            ::isSearchable,
            ::isCurrency,
            ::isReadOnly,
            ::isWritable,
            ::isDefinitelyWritable,
            ::isSigned,
            ::getSchemaName,
            ::getCatalogName,
            ::getTableName
        )
        for (invalidIndex in invalidIndices) {
            for (method in validMethods) {
                assertFailsWith<SQLException>("Method should throw for index $invalidIndex") {
                    method(invalidIndex)
                }
            }
        }
    }

    @Test
    fun allColumnTypes() {
        val typeTests = listOf(
            ColumnType.INTEGER to Types.BIGINT,
            ColumnType.FLOAT to Types.DOUBLE,
            ColumnType.STRING to Types.VARCHAR,
            ColumnType.BLOB to Types.VARBINARY,
            ColumnType.NULL to Types.NULL
        )
        val testCursor = mock<ICursor> {
            whenever(it.columnCount) doReturn 5
            whenever(it.columnNames()) doReturn arrayOf("col1", "col2", "col3", "col4", "col5")
            whenever(it.position()) doReturn 0
            whenever(it.isBeforeFirst()) doReturn false
            whenever(it.isAfterLast()) doReturn false
            typeTests.forEachIndexed { index, (selektType, _) ->
                whenever(it.type(index)) doReturn selektType
            }
        }
        val testMetaData = JdbcResultSetMetaData(testCursor)
        typeTests.forEachIndexed { index, (_, expectedJdbcType) ->
            assertEquals(expectedJdbcType, testMetaData.getColumnType(index + 1))
        }
    }

    @Test
    fun emptyResultSet() {
        val emptyCursor = mock<ICursor> {
            whenever(it.columnCount) doReturn 0
            whenever(it.columnNames()) doReturn emptyArray()
        }
        JdbcResultSetMetaData(emptyCursor).run {
            assertEquals(0, columnCount)
            assertFailsWith<SQLException> {
                getColumnName(1)
            }
        }
    }

    @Test
    fun scrollableCursorWithoutCurrentRowUsesFallbackType() {
        whenever(mockCursor.isForwardOnly) doReturn false
        whenever(mockCursor.position()) doReturn -1
        whenever(mockCursor.isBeforeFirst()) doReturn true

        assertEquals(Types.VARCHAR, metaData.getColumnType(1))
    }
}
