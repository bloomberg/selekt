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

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class WindowedCursorTest {
    @Test
    fun close() {
        val window = mock<ICursorWindow>()
        val cursor = WindowedCursor(arrayOf("a", "b"), window)
        cursor.close()
        verify(window, times(1)).close()
        assertTrue(cursor.isClosed())
    }

    @Test
    fun columnIndex() {
        val cursor = WindowedCursor(arrayOf("a", "b"), mock())
        assertEquals(1, cursor.columnIndex("b"))
    }

    @Test
    fun columnIndexIsCaseSensitive() {
        val cursor = WindowedCursor(arrayOf("a", "A"), mock())
        assertEquals(1, cursor.columnIndex("A"))
    }

    @Test
    fun columnIndexNotFound() {
        val cursor = WindowedCursor(arrayOf("a"), mock())
        assertEquals(-1, cursor.columnIndex("b"))
    }

    @Test
    fun columnName() {
        val cursor = WindowedCursor(arrayOf("a"), mock())
        assertEquals("a", cursor.columnName(0))
    }

    @Test
    fun columnNames() {
        val columns = emptyArray<String>()
        val cursor = WindowedCursor(columns, mock())
        assertSame(columns, cursor.columnNames())
    }

    @Test
    fun count() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 42
        }
        assertEquals(42, WindowedCursor(arrayOf("a"), window).count)
        verify(window, times(1)).numberOfRows()
    }

    @Test
    fun isClosedUponClose() {
        WindowedCursor(emptyArray(), mock()).apply {
            close()
            assertTrue(isClosed())
        }
    }

    @Test
    fun isFirst() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        WindowedCursor(emptyArray(), window).apply {
            assertTrue(moveToNext())
            assertTrue(isFirst())
        }
    }

    @Test
    fun isBeforeFirstInitially() {
        WindowedCursor(emptyArray(), mock()).apply {
            assertTrue(isBeforeFirst())
            assertEquals(-1, position())
        }
    }

    @Test
    fun isAfterLast() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        WindowedCursor(emptyArray(), window).apply {
            assertFalse(moveToPosition(1))
            assertTrue(isAfterLast())
        }
    }

    @Test
    fun isNotClosedInitially() {
        assertFalse(WindowedCursor(emptyArray(), mock()).isClosed())
    }

    @Test
    fun isNotFirstInitially() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        assertFalse(WindowedCursor(emptyArray(), window).isFirst())
    }

    @Test
    fun isNull() {
        WindowedCursor(arrayOf("foo"), mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
            whenever(isNull(eq(0), eq(0))) doReturn true
        }).apply {
            assertTrue(moveToFirst())
            assertTrue(isNull(0))
        }
    }

    @Test
    fun emptyIsAfterLast() {
        assertTrue(WindowedCursor(emptyArray(), mock()).isAfterLast())
    }

    @Test
    fun moveToPreviousEmpty() {
        assertFalse(WindowedCursor(emptyArray(), mock()).moveToPrevious())
    }

    @Test
    fun moveToPreviousFromLast() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 2
        }
        WindowedCursor(emptyArray(), window).apply {
            assertTrue(moveToLast())
            assertTrue(moveToPrevious())
            assertTrue(isFirst())
        }
    }

    @Test
    fun moveToLastIsLast() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        WindowedCursor(emptyArray(), window).apply {
            assertTrue(moveToLast())
            assertTrue(isLast())
        }
    }
}
