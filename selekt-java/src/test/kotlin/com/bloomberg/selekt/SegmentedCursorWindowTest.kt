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
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

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
            listOf<() -> Any?>(
                { it.allocateRow() },
                { it.put(byteArrayOf(1)) },
                { it.put(1.0) },
                { it.put(1.0f) },
                { it.put(1) },
                { it.put(1L) },
                { it.put(1.toShort()) },
                { it.put("one") },
                { it.putNull() },
                { it.clear() }
            ).forEach { operation ->
                assertFailsWith<UnsupportedOperationException> { operation() }
            }
        }
    }

    @Test
    fun delegatesEveryReaderAndRejectsOutOfRangeRows() {
        val bytes = byteArrayOf(1, 2)
        val underlying = SimpleCursorWindow().apply {
            allocateRow()
            put(bytes)
            put(2.5)
            put(3.5f)
            put(4)
            put(5L)
            put(6.toShort())
            put("seven")
            putNull()
        }
        SegmentedCursorWindow(listOf(underlying), 1).use { window ->
            assertContentEquals(bytes, window.getBlob(0, 0))
            assertEquals(2.5, window.getDouble(0, 1))
            assertEquals(3.5f, window.getFloat(0, 2))
            assertEquals(4, window.getInt(0, 3))
            assertEquals(5L, window.getLong(0, 4))
            assertEquals(6.toShort(), window.getShort(0, 5))
            assertEquals("seven", window.getString(0, 6))
            assertContentEquals("seven".toByteArray(), window.getTextBytes(0, 6))
            assertTrue(window.isNull(0, 7))
            assertFailsWith<IndexOutOfBoundsException> { window.getLong(-1, 0) }
            assertFailsWith<IndexOutOfBoundsException> { window.getLong(1, 0) }
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
