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
}
