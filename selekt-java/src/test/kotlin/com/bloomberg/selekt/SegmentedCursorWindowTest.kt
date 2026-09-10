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

package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame

internal class SegmentedCursorWindowTest {
    @Test
    fun readsAcrossSegmentBoundaries() {
        SegmentedCursorWindow(listOf(window(0, 4), window(4, 6)), 4).use { window ->
            assertEquals(6, window.numberOfRows())
            repeat(6) { row ->
                assertEquals(row.toLong(), window.getLong(row, 0))
                assertEquals("row$row", window.getString(row, 1))
                assertEquals(ColumnType.INTEGER, window.type(row, 0))
                assertEquals(ColumnType.STRING, window.type(row, 1))
                assertFalse(window.isNull(row, 0))
            }
        }
    }

    @Test
    fun closeClosesEverySegmentOnce() {
        val first = spy(window(0, 4))
        val second = spy(window(4, 6))
        SegmentedCursorWindow(listOf(first, second), 4).apply {
            close()
            close()
        }
        verify(first, times(1)).close()
        verify(second, times(1)).close()
    }

    @Test
    fun closeAttemptsEverySegmentAndSuppressesLaterFailures() {
        val firstFailure = IllegalStateException("first")
        val secondFailure = IllegalStateException("second")
        val first = mock<ICursorWindow>()
        val second = mock<ICursorWindow>()
        whenever(first.numberOfRows()).thenReturn(4)
        whenever(second.numberOfRows()).thenReturn(1)
        doThrow(firstFailure).whenever(first).close()
        doThrow(secondFailure).whenever(second).close()

        val thrown = assertFailsWith<IllegalStateException> {
            SegmentedCursorWindow(listOf(first, second), 4).close()
        }

        assertSame(firstFailure, thrown)
        assertEquals(listOf(secondFailure), thrown.suppressed.toList())
        verify(first).close()
        verify(second).close()
    }

    @Test
    fun accessAfterCloseFails() {
        SegmentedCursorWindow(listOf(window(0, 1)), 4).apply {
            close()
            assertFailsWith<IllegalStateException> { getLong(0, 0) }
        }
    }

    @Test
    fun onlyFinalSegmentMayBePartial() {
        assertFailsWith<IllegalArgumentException> {
            SegmentedCursorWindow(listOf(window(0, 2), window(2, 3)), 4)
        }
    }

    @Test
    fun isImmutable() {
        SegmentedCursorWindow(listOf(window(0, 1)), 4).use {
            assertFailsWith<UnsupportedOperationException> { it.allocateRow() }
            assertFailsWith<UnsupportedOperationException> { it.put(42L) }
            assertFailsWith<UnsupportedOperationException> { it.clear() }
        }
    }

    private fun window(start: Int, end: Int) = SimpleCursorWindow().apply {
        (start until end).forEach { row ->
            allocateRow()
            put(row.toLong())
            put("row$row")
        }
    }
}
