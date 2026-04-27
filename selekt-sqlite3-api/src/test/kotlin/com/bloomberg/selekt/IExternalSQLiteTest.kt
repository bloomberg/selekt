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

import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class IExternalSQLiteTest {
    private val statement = 0xCAFEL

    private val sqlite = mock<IExternalSQLite> {
        on { bindText(any(), any(), any()) }.thenReturn(SQL_OK)
        on { bindInt(any(), any(), any()) }.thenReturn(SQL_OK)
        on { bindInt64(any(), any(), any()) }.thenReturn(SQL_OK)
        on { bindDouble(any(), any(), any()) }.thenReturn(SQL_OK)
        on { bindNull(any(), any()) }.thenReturn(SQL_OK)
        on { bindBlob(any(), any(), any(), any()) }.thenReturn(SQL_OK)
        on { bindRow(any(), any()) }.thenCallRealMethod()
    }

    @Test
    fun `bindRow with string`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf("hello")))
        verify(sqlite).bindText(statement, 1, "hello")
    }

    @Test
    fun `bindRow with int`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(42)))
        verify(sqlite).bindInt(statement, 1, 42)
    }

    @Test
    fun `bindRow with null`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(null)))
        verify(sqlite).bindNull(statement, 1)
    }

    @Test
    fun `bindRow with long`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(Long.MAX_VALUE)))
        verify(sqlite).bindInt64(statement, 1, Long.MAX_VALUE)
    }

    @Test
    fun `bindRow with double`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(3.14)))
        verify(sqlite).bindDouble(statement, 1, 3.14)
    }

    @Test
    fun `bindRow with blob`() {
        val blob = byteArrayOf(1, 2, 3)
        assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(blob)))
        verify(sqlite).bindBlob(statement, 1, blob, 3)
    }

    @Test
    fun `bindRow with empty args`() {
        assertEquals(SQL_OK, sqlite.bindRow(statement, emptyArray()))
        verify(sqlite, never()).bindText(any(), any(), any())
        verify(sqlite, never()).bindInt(any(), any(), any())
        verify(sqlite, never()).bindNull(any(), any())
    }

    @Test
    fun `bindRow with mixed types`() {
        val blob = byteArrayOf(9)
        assertEquals(
            SQL_OK,
            sqlite.bindRow(statement, arrayOf("text", 42, null, Long.MAX_VALUE, 3.14, blob))
        )
        verify(sqlite).bindText(statement, 1, "text")
        verify(sqlite).bindInt(statement, 2, 42)
        verify(sqlite).bindNull(statement, 3)
        verify(sqlite).bindInt64(statement, 4, Long.MAX_VALUE)
        verify(sqlite).bindDouble(statement, 5, 3.14)
        verify(sqlite).bindBlob(statement, 6, blob, 1)
    }

    @Test
    fun `bindRow throws on unsupported type`() {
        assertFailsWith<IllegalArgumentException> {
            sqlite.bindRow(statement, arrayOf(Any()))
        }
    }

    @Test
    fun `bindRow stops on error`() {
        whenever(sqlite.bindInt(any(), any(), any())).thenReturn(1)
        val result = sqlite.bindRow(statement, arrayOf<Any?>("ok", 42, "never"))
        assertEquals(1, result)
        verify(sqlite).bindText(statement, 1, "ok")
        verify(sqlite).bindInt(statement, 2, 42)
        verify(sqlite, never()).bindText(eq(statement), eq(3), any())
    }
}
