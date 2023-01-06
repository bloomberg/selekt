/*
 * Copyright 2022 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android

import android.database.Cursor
import com.bloomberg.selekt.ColumnType
import com.bloomberg.selekt.ICursor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class CursorWrapperTest {
    @Mock lateinit var cursor: ICursor
    private lateinit var wrapper: Cursor

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        doReturn(emptyArray<String>()).whenever(cursor).columnNames()
        wrapper = cursor.asAndroidCursor()
    }

    @Test
    fun close() {
        wrapper.close()
        verify(cursor, times(1)).close()
        assertTrue(wrapper.isClosed)
    }

    @Test
    fun getBlob() {
        val blob = ByteArray(1) { 42 }
        whenever(cursor.getBlob(1)) doReturn blob
        assertSame(blob, wrapper.getBlob(1))
        verify(cursor, times(1)).getBlob(eq(1))
    }

    @Test
    fun getColumnCount() {
        whenever(cursor.columnCount) doReturn 42
        assertEquals(42, wrapper.columnCount)
        verify(cursor, times(1)).columnCount
    }

    @Test
    fun getColumnIndex() {
        whenever(cursor.columnIndex(eq("foo"))) doReturn 1
        assertEquals(1, wrapper.getColumnIndex("foo"))
        verify(cursor, times(1)).columnIndex(eq("foo"))
    }

    @Test
    fun getColumnIndexOrThrow() {
        whenever(cursor.columnIndex(eq("foo"))) doReturn 1
        assertEquals(1, wrapper.getColumnIndexOrThrow("foo"))
        verify(cursor, times(1)).columnIndex(eq("foo"))
    }

    @Test
    fun getColumnIndexOrThrowThrows() {
        whenever(cursor.columnIndex(eq("foo"))) doReturn -1
        assertThrows<IllegalArgumentException> {
            wrapper.getColumnIndexOrThrow("foo")
        }
    }

    @Test
    fun getColumnName() {
        whenever(cursor.columnName(eq(1))) doReturn "foo"
        assertEquals("foo", wrapper.getColumnName(1))
        verify(cursor, times(1)).columnName(eq(1))
    }

    @Test
    fun getColumnNames() {
        val names = arrayOf("foo", "bar")
        whenever(cursor.columnNames()) doReturn names
        assertContentEquals(names, cursor.asAndroidCursor().columnNames)
    }

    @Test
    fun getCount() {
        whenever(cursor.count) doReturn 42
        assertEquals(42, wrapper.count)
        verify(cursor, times(1)).count
    }

    @Test
    fun getDouble() {
        whenever(cursor.getDouble(1)) doReturn 42.0
        assertEquals(42.0, wrapper.getDouble(1), 0.1)
        verify(cursor, times(1)).getDouble(eq(1))
    }

    @Test
    fun getFloat() {
        whenever(cursor.getFloat(1)) doReturn 42.0f
        assertEquals(42.0f, wrapper.getFloat(1), 0.1f)
        verify(cursor, times(1)).getFloat(eq(1))
    }

    @Test
    fun getInt() {
        whenever(cursor.getInt(1)) doReturn 42
        assertEquals(42, wrapper.getInt(1))
        verify(cursor, times(1)).getInt(eq(1))
    }

    @Test
    fun getLong() {
        whenever(cursor.getLong(1)) doReturn 42L
        assertEquals(42L, wrapper.getLong(1))
        verify(cursor, times(1)).getLong(eq(1))
    }

    @Test
    fun getShort() {
        whenever(cursor.getShort(1)) doReturn 42.toShort()
        assertEquals(42.toShort(), wrapper.getShort(1))
        verify(cursor, times(1)).getShort(eq(1))
    }

    @Test
    fun getString() {
        val text = "bar"
        whenever(cursor.getString(1)) doReturn text
        assertSame(text, wrapper.getString(1))
        verify(cursor, times(1)).getString(eq(1))
    }

    @Test
    fun getTypeBlob() {
        whenever(cursor.type(1)) doReturn ColumnType.BLOB
        assertEquals(Cursor.FIELD_TYPE_BLOB, wrapper.getType(1))
        verify(cursor, times(1)).type(eq(1))
    }

    @Test
    fun getTypeFloat() {
        whenever(cursor.type(1)) doReturn ColumnType.FLOAT
        assertEquals(Cursor.FIELD_TYPE_FLOAT, wrapper.getType(1))
        verify(cursor, times(1)).type(eq(1))
    }

    @Test
    fun getTypeInteger() {
        whenever(cursor.type(1)) doReturn ColumnType.INTEGER
        assertEquals(Cursor.FIELD_TYPE_INTEGER, wrapper.getType(1))
        verify(cursor, times(1)).type(eq(1))
    }

    @Test
    fun getTypeNull() {
        whenever(cursor.type(1)) doReturn ColumnType.NULL
        assertEquals(Cursor.FIELD_TYPE_NULL, wrapper.getType(1))
        verify(cursor, times(1)).type(eq(1))
    }

    @Test
    fun getTypeString() {
        whenever(cursor.type(1)) doReturn ColumnType.STRING
        assertEquals(Cursor.FIELD_TYPE_STRING, wrapper.getType(1))
        verify(cursor, times(1)).type(eq(1))
    }

    @Test
    fun isNull() {
        whenever(cursor.isNull(eq(1))) doReturn true
        assertTrue(wrapper.isNull(1))
        verify(cursor, times(1)).isNull(eq(1))
    }

    @Test
    fun moveToPosition() {
        assertEquals(-1, wrapper.position)
        whenever(cursor.count) doReturn 1
        whenever(cursor.moveToPosition(eq(0))) doReturn true
        assertTrue(wrapper.moveToPosition(0))
        verify(cursor, times(1)).count
        verify(cursor, times(1)).moveToPosition(eq(0))
        assertEquals(0, wrapper.position)
    }
}
