/*
 * Copyright 2020 Bloomberg Finance L.P.
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

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class WindowedCursorTest {
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
    fun isClosedUponClose() {
        WindowedCursor(emptyArray(), mock()).apply {
            close()
            assertTrue(isClosed())
        }
    }

    @Test
    fun isNotClosedInitially() {
        assertFalse(WindowedCursor(emptyArray(), mock()).isClosed())
    }

    @Test
    fun isBeforeFirstInitially() {
        WindowedCursor(emptyArray(), mock()).apply {
            assertTrue(isBeforeFirst())
            assertEquals(-1, position())
        }
    }
}
