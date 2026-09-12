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

import com.bloomberg.selekt.ICursor
import com.bloomberg.selekt.ColumnType
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class RowLimitedCursorTest {
    @Test
    fun maximumRowsMustBePositive() {
        assertFailsWith<IllegalArgumentException> { RowLimitedCursor(mock(), 0) }
    }

    @Test
    fun scrollableMovementCannotReachRowsPastLimit() {
        var position = -1
        val cursor = mock<ICursor> {
            whenever(it.count) doReturn 10
            whenever(it.isForwardOnly) doReturn false
            whenever(it.position()) doAnswer { position }
            whenever(it.isBeforeFirst()) doAnswer { position < 0 }
            whenever(it.isAfterLast()) doAnswer { position >= 10 }
            whenever(it.moveToPosition(any())) doAnswer { invocation ->
                val requested = invocation.getArgument<Int>(0)
                position = requested.coerceIn(-1, 10)
                position in 0 until 10
            }
        }
        val limited = RowLimitedCursor(cursor, 3)

        assertEquals(3, limited.count)
        assertTrue(limited.moveToNext())
        assertTrue(limited.moveToNext())
        assertTrue(limited.moveToNext())
        assertTrue(limited.isLast())
        assertFalse(limited.moveToNext())
        assertTrue(limited.isAfterLast())
        assertEquals(3, limited.position())
        assertTrue(limited.moveToPrevious())
        assertEquals(2, limited.position())
        assertTrue(limited.isLast())
    }

    @Test
    fun scrollableLastUsesLimitedRowCount() {
        var position = -1
        val cursor = mock<ICursor> {
            whenever(it.count) doReturn 10
            whenever(it.isForwardOnly) doReturn false
            whenever(it.position()) doAnswer { position }
            whenever(it.isBeforeFirst()) doAnswer { position < 0 }
            whenever(it.isAfterLast()) doAnswer { position >= 10 }
            whenever(it.moveToPosition(any())) doAnswer { invocation ->
                val requested = invocation.getArgument<Int>(0)
                position = requested.coerceIn(-1, 10)
                position in 0 until 10
            }
        }
        val limited = RowLimitedCursor(cursor, 3)

        assertTrue(limited.moveToLast())
        assertEquals(2, limited.position())
        assertTrue(limited.isLast())
    }

    @Test
    fun forwardLimitReleasesUnderlyingCursorWithoutLogicallyClosingWrapper() {
        val cursor = mock<ICursor> {
            whenever(it.isForwardOnly) doReturn true
            whenever(it.moveToNext()) doReturn true
        }
        val limited = RowLimitedCursor(cursor, 2)

        assertTrue(limited.moveToNext())
        assertTrue(limited.moveToNext())
        assertFalse(limited.moveToNext())
        assertFalse(limited.isClosed())
        verify(cursor, times(1)).close()
        assertFailsWith<IllegalStateException> { limited.getString(0) }

        assertFalse(limited.moveToNext())
        limited.close()
        assertTrue(limited.isClosed())
        verify(cursor, times(1)).close()
    }

    @Test
    fun forwardExhaustionReleasesUnderlyingCursor() {
        val cursor = mock<ICursor> {
            whenever(it.isForwardOnly) doReturn true
            whenever(it.moveToNext()) doReturn false
        }
        val limited = RowLimitedCursor(cursor, 2)

        assertFalse(limited.moveToNext())
        assertFalse(limited.moveToNext())
        verify(cursor, times(1)).moveToNext()
        verify(cursor, times(1)).close()
    }

    @Test
    fun forwardCursorDelegatesMovementAndValuesWhileOnRow() {
        val bytes = byteArrayOf(1)
        val cursor = mock<ICursor> {
            whenever(it.isForwardOnly) doReturn true
            whenever(it.moveToNext()) doReturn true
            whenever(it.getBlob(0)) doReturn bytes
            whenever(it.getDouble(0)) doReturn 1.5
            whenever(it.getInt(0)) doReturn 2
            whenever(it.getLong(0)) doReturn 3L
            whenever(it.getShort(0)) doReturn 4.toShort()
            whenever(it.getString(0)) doReturn "five"
            whenever(it.getTextBytes(0)) doReturn bytes
            whenever(it.isNull(0)) doReturn false
            whenever(it.type(0)) doReturn ColumnType.STRING
        }
        val limited = RowLimitedCursor(cursor, 2)

        assertFailsWith<IllegalStateException> { limited.getInt(0) }
        assertTrue(limited.moveToNext())
        assertEquals(bytes.toList(), limited.getBlob(0)?.toList())
        assertEquals(1.5, limited.getDouble(0))
        assertEquals(2, limited.getInt(0))
        assertEquals(3L, limited.getLong(0))
        assertEquals(4.toShort(), limited.getShort(0))
        assertEquals("five", limited.getString(0))
        assertEquals(bytes.toList(), limited.getTextBytes(0)?.toList())
        assertFalse(limited.isNull(0))
        assertEquals(ColumnType.STRING, limited.type(0))
        limited.isFirst()
        limited.isLast()
        limited.move(1)
        limited.moveToFirst()
        limited.moveToLast()
        limited.moveToPosition(0)
        limited.moveToPrevious()
        limited.position()
    }

    @Test
    fun scrollableCursorHandlesEveryBoundaryState() {
        var position = -1
        val cursor = mock<ICursor> {
            whenever(it.count) doReturn 10
            whenever(it.isForwardOnly) doReturn false
            whenever(it.position()) doAnswer { position }
            whenever(it.isBeforeFirst()) doAnswer { position < 0 }
            whenever(it.isAfterLast()) doAnswer { position >= 10 }
            whenever(it.moveToPosition(any())) doAnswer { invocation ->
                position = invocation.getArgument(0)
                position in 0 until 10
            }
        }
        val limited = RowLimitedCursor(cursor, 3)

        assertTrue(limited.isFirst().not())
        assertTrue(limited.isLast().not())
        assertFailsWith<IllegalStateException> { limited.getString(0) }
        assertFalse(limited.moveToPosition(-1))
        assertEquals(-1, limited.position())
        assertTrue(limited.moveToFirst())
        assertTrue(limited.isFirst())
        assertFalse(limited.move(Int.MIN_VALUE))
        assertFalse(limited.moveToPosition(3))
        position = 10
        assertEquals(3, limited.position())
        assertFalse(limited.move(1))
        assertTrue(limited.moveToPrevious())
        limited.close()
        limited.close()
        verify(cursor, times(1)).close()
    }
}
