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

package com.bloomberg.selekt

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SQLBlobTest {
    @Test
    fun verifiesPointer() {
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            SQLBlob(0L, mock(), false)
        }
    }

    @Test
    fun close() {
        val pointer = 42L
        mock<SQLite>().let {
            SQLBlob(pointer, it, false).close()
            verify(it, times(1)).blobClose(eq(pointer))
        }
    }

    @Test
    fun read() {
        val pointer = 42L
        val destination = ByteArray(0)
        val offset = 5
        val destinationOffset = 6
        val length = 60
        mock<SQLite>().let {
            val blob = SQLBlob(pointer, it, false)
            blob.read(
                offset,
                destination,
                destinationOffset,
                length
            )
            verify(it, times(1)).blobRead(eq(pointer), eq(offset), same(destination), eq(destinationOffset), eq(length))
        }
    }

    @Test
    fun readOnlyTrue() {
        assertTrue(SQLBlob(42L, mock(), true).readOnly)
    }

    @Test
    fun readOnlyFalse() {
        assertFalse(SQLBlob(42L, mock(), false).readOnly)
    }

    @Test
    fun reopen() {
        val pointer = 42L
        val row = 2L
        mock<SQLite>().let {
            SQLBlob(pointer, it, false).reopen(row)
            verify(it, times(1)).blobReopen(eq(pointer), eq(row))
        }
    }

    @Test
    fun size() {
        val pointer = 1L
        val expectedSize = 42
        mock<SQLite>().apply {
            whenever(blobBytes(any())) doReturn expectedSize
        }.let {
            assertEquals(expectedSize, SQLBlob(pointer, it, false).size)
            verify(it, times(1)).blobBytes(eq(pointer))
        }
    }

    @Test
    fun write() {
        val pointer = 42L
        val source = ByteArray(0)
        val offset = 5
        val sourceOffset = 6
        val length = 60
        mock<SQLite>().let {
            val blob = SQLBlob(pointer, it, false)
            blob.write(
                offset,
                source,
                sourceOffset,
                length
            )
            verify(it, times(1)).blobWrite(eq(pointer), eq(offset), same(source), eq(sourceOffset), eq(length))
        }
    }
}
