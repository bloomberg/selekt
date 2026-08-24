/*
 * Copyright 2020 Bloomberg Finance L.P.
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

package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun windowedCursor(columnNames: Array<out String>, window: ICursorWindow) =
    WindowedCursor(columnNames, CursorWindowPage(window, 0, window.numberOfRows()))

internal class WindowedCursorTest {
    @Test
    fun close() {
        val window = mock<ICursorWindow>()
        val cursor = windowedCursor(arrayOf("a", "b"), window)
        cursor.close()
        verify(window, times(1)).close()
        assertTrue(cursor.isClosed())
    }

    @Test
    fun columnIndex() {
        val cursor = windowedCursor(arrayOf("a", "b"), mock())
        assertEquals(1, cursor.columnIndex("b"))
    }

    @Test
    fun columnIndexIsCaseSensitive() {
        val cursor = windowedCursor(arrayOf("a", "A"), mock())
        assertEquals(1, cursor.columnIndex("A"))
    }

    @Test
    fun columnIndexNotFound() {
        val cursor = windowedCursor(arrayOf("a"), mock())
        assertEquals(-1, cursor.columnIndex("b"))
    }

    @Test
    fun columnName() {
        val cursor = windowedCursor(arrayOf("a"), mock())
        assertEquals("a", cursor.columnName(0))
    }

    @Test
    fun columnNames() {
        val columns = emptyArray<String>()
        val cursor = windowedCursor(columns, mock())
        assertSame(columns, cursor.columnNames())
    }

    @Test
    fun count() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 42
        }
        assertEquals(42, windowedCursor(arrayOf("a"), window).count)
        verify(window, times(1)).numberOfRows()
    }

    @Test
    fun isClosedUponClose() {
        windowedCursor(emptyArray(), mock()).apply {
            close()
            assertTrue(isClosed())
        }
    }

    @Test
    fun isFirst() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        windowedCursor(emptyArray(), window).apply {
            assertTrue(moveToNext())
            assertTrue(isFirst())
        }
    }

    @Test
    fun isNotFirst() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        windowedCursor(emptyArray(), window).apply {
            assertFalse(isFirst())
        }
    }

    @Test
    fun isNotFirstZero() {
        windowedCursor(emptyArray(), mock()).apply {
            assertFalse(isFirst())
        }
    }

    @Test
    fun isNotFirstZeroAfterLast() {
        windowedCursor(emptyArray(), mock()).apply {
            assertFalse(moveToPosition(0))
            assertFalse(isFirst())
        }
    }

    @Test
    fun isLast() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        windowedCursor(emptyArray(), window).apply {
            assertTrue(moveToPosition(0))
            assertTrue(isLast())
        }
    }

    @Test
    fun isNotLastInitially() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        windowedCursor(emptyArray(), window).apply {
            assertFalse(isLast())
        }
    }

    @Test
    fun isNotLastEmpty() {
        windowedCursor(emptyArray(), mock()).apply {
            assertFalse(isLast())
        }
    }

    @Test
    fun isBeforeFirstZeroCount() {
        windowedCursor(emptyArray(), mock()).apply {
            assertTrue(isBeforeFirst())
        }
    }

    @Test
    fun isBeforeFirstInitially() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        windowedCursor(emptyArray(), window).apply {
            assertTrue(isBeforeFirst())
            assertEquals(-1, position())
        }
    }

    @Test
    fun isNotBeforeFirst() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        windowedCursor(emptyArray(), window).apply {
            assertTrue(moveToFirst())
            assertFalse(isBeforeFirst())
        }
    }

    @Test
    fun isAfterLast() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        windowedCursor(emptyArray(), window).apply {
            assertFalse(moveToPosition(1))
            assertTrue(isAfterLast())
        }
    }

    @Test
    fun isNotAfterLast() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        windowedCursor(emptyArray(), window).apply {
            assertFalse(isAfterLast())
        }
    }

    @Test
    fun isNotClosedInitially() {
        assertFalse(windowedCursor(emptyArray(), mock()).isClosed())
    }

    @Test
    fun isNotFirstInitially() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
        }
        assertFalse(windowedCursor(emptyArray(), window).isFirst())
    }

    @Test
    fun isNull() {
        windowedCursor(arrayOf("foo"), mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 1
            whenever(isNull(eq(0), eq(0))) doReturn true
        }).apply {
            assertTrue(moveToFirst())
            assertTrue(isNull(0))
        }
    }

    @Test
    fun emptyIsAfterLast() {
        assertTrue(windowedCursor(emptyArray(), mock()).isAfterLast())
    }

    @Test
    fun moveToPreviousEmpty() {
        assertFalse(windowedCursor(emptyArray(), mock()).moveToPrevious())
    }

    @Test
    fun moveToPreviousFromLast() {
        val window = mock<ICursorWindow>().apply {
            whenever(numberOfRows()) doReturn 2
        }
        windowedCursor(emptyArray(), window).apply {
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
        windowedCursor(emptyArray(), window).apply {
            assertTrue(moveToLast())
            assertTrue(isLast())
        }
    }

    @Test
    fun countIsTheTotalNotTheWindow() {
        pagedCursor().apply {
            assertEquals(TOTAL_ROWS, count)
            assertTrue(moveToLast())
            assertTrue(isLast())
            assertEquals((TOTAL_ROWS - 1).toLong(), getLong(0))
        }
    }

    @Test
    fun readingWithinTheWindowDoesNotRefill() {
        val refills = mutableListOf<Int>()
        pagedCursor(refills).apply {
            repeat(WINDOW_SIZE) {
                assertTrue(moveToNext())
                assertEquals(it.toLong(), getLong(0))
            }
        }
        assertTrue(refills.isEmpty())
    }

    @Test
    fun readingBeyondTheWindowRefillsOnce() {
        val refills = mutableListOf<Int>()
        pagedCursor(refills).apply {
            assertTrue(moveToPosition(6))
            assertEquals(6L, getLong(0))
            assertEquals(6L, getLong(0))
        }
        assertEquals(listOf(5), refills)
    }

    @Test
    fun refillClosesTheWindowItReplaces() {
        var replaced: ICursorWindow? = null
        val first = spyWindow(0, WINDOW_SIZE).also { replaced = it }
        WindowedCursor(arrayOf("a"), CursorWindowPage(first, 0, TOTAL_ROWS)) { start ->
            CursorWindowPage(spyWindow(start, WINDOW_SIZE), start, TOTAL_ROWS)
        }.apply {
            assertTrue(moveToPosition(9))
            getLong(0)
        }
        verify(requireNotNull(replaced), times(1)).close()
    }

    @Test
    fun scansForwardAcrossEveryWindow() {
        pagedCursor().apply {
            repeat(TOTAL_ROWS) {
                assertTrue(moveToNext())
                assertEquals(it.toLong(), getLong(0))
            }
            assertFalse(moveToNext())
        }
    }

    @Test
    fun scansBackwardAcrossEveryWindow() {
        pagedCursor().apply {
            assertTrue(moveToLast())
            for (row in TOTAL_ROWS - 1 downTo 0) {
                assertEquals(row.toLong(), getLong(0))
                assertEquals(row > 0, moveToPrevious())
            }
        }
    }

    @Test
    fun steppingBackOverAWindowEdgeDoesNotRefillAgain() {
        val refills = mutableListOf<Int>()
        pagedCursor(refills).apply {
            assertTrue(moveToPosition(WINDOW_SIZE))
            getLong(0)
            assertEquals(1, refills.size)
            assertTrue(moveToPrevious())
            assertEquals((WINDOW_SIZE - 1).toLong(), getLong(0))
        }
        assertEquals(1, refills.size)
    }

    @Test
    fun readingOutsideAnUnrefillableWindowFails() {
        val cursor = WindowedCursor(arrayOf("a"), CursorWindowPage(window(0, WINDOW_SIZE), 0, TOTAL_ROWS))
        cursor.moveToPosition(6)
        assertFailsWith<IllegalArgumentException> { cursor.getLong(0) }
    }

    @Test
    fun readingWithoutAValidPositionFails() {
        windowedCursor(arrayOf("a"), window(0, WINDOW_SIZE)).apply {
            assertFailsWith<IllegalStateException> { getLong(0) }
            moveToLast()
            moveToNext()
            assertFailsWith<IllegalStateException> { getLong(0) }
        }
    }

    @Test
    fun readingAfterCloseFails() {
        windowedCursor(arrayOf("a"), window(0, WINDOW_SIZE)).apply {
            moveToFirst()
            close()
            assertFailsWith<IllegalStateException> { getLong(0) }
        }
    }

    @Test
    fun readingAnInvalidColumnFails() {
        windowedCursor(arrayOf("a"), window(0, WINDOW_SIZE)).apply {
            moveToFirst()
            assertFailsWith<IndexOutOfBoundsException> { getLong(-1) }
            assertFailsWith<IndexOutOfBoundsException> { getLong(1) }
        }
    }

    @Test
    fun refillMustContainTheRequestedPosition() {
        val invalidWindow = spyWindow(0, 0)
        WindowedCursor(
            arrayOf("a"),
            CursorWindowPage(window(0, WINDOW_SIZE), 0, TOTAL_ROWS)
        ) { CursorWindowPage(invalidWindow, it, NOT_COUNTED) }.apply {
            moveToLast()
            assertFailsWith<IllegalStateException> { getLong(0) }
        }
        verify(invalidWindow).close()
    }

    private companion object {
        const val TOTAL_ROWS = 10
        const val WINDOW_SIZE = 4

        fun window(start: Int, size: Int) = SimpleCursorWindow().apply {
            (start until minOf(start + size, TOTAL_ROWS)).forEach {
                allocateRow()
                put(it.toLong())
            }
        }

        fun spyWindow(start: Int, size: Int): ICursorWindow = spy(window(start, size))

        fun pagedCursor(refills: MutableList<Int> = mutableListOf()) = WindowedCursor(
            arrayOf("a"),
            CursorWindowPage(window(0, WINDOW_SIZE), 0, TOTAL_ROWS)
        ) { start ->
            refills.add(start)
            CursorWindowPage(window(start, WINDOW_SIZE), start, TOTAL_ROWS)
        }
    }
}
