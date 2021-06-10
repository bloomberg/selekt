/*
 * Copyright 2021 Bloomberg Finance L.P.
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
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

internal class SQLBlobInputStreamTest {
    @Test
    fun availableInitially() {
        val expectedSize = 42
        mock<SQLBlob>().apply {
            whenever(size) doReturn expectedSize
        }.let {
            assertEquals(expectedSize, BlobInputStream(it).available())
        }
    }

    @Test
    fun close() {
        mock<SQLBlob>().let {
            BlobInputStream(it).close()
            verify(it, never()).close()
        }
    }

    @Test
    fun inputStreamChecksOffset() {
        BlobInputStream(mock()).run {
            assertThatExceptionOfType(ArrayIndexOutOfBoundsException::class.java).isThrownBy {
                read(ByteArray(1), -1, 1)
            }
        }
    }

    @Test
    fun inputStreamChecksLength() {
        BlobInputStream(mock()).run {
            assertThatExceptionOfType(ArrayIndexOutOfBoundsException::class.java).isThrownBy {
                read(ByteArray(1), 0, -1)
            }
        }
    }

    @Test
    fun inputStreamChecksUpperBounds() {
        val blob = mock<SQLBlob>().apply {
            whenever(size) doReturn 1
        }
        BlobInputStream(blob).run {
            assertThatExceptionOfType(ArrayIndexOutOfBoundsException::class.java).isThrownBy {
                read(ByteArray(1), 2, 1)
            }
        }
    }

    @Test
    fun inputStreamChecksDestinationSize() {
        val blob = mock<SQLBlob>().apply {
            whenever(size) doReturn 2
        }
        BlobInputStream(blob).run {
            assertThatExceptionOfType(ArrayIndexOutOfBoundsException::class.java).isThrownBy {
                read(ByteArray(1), 0, 2)
            }
        }
    }

    @Test
    fun readFromNegativeStart() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            BlobInputStream(mock(), start = -1)
        }
    }

    @Test
    fun readFromTooAdvancedStart() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            BlobInputStream(mock(), start = 1)
        }
    }

    @Test
    fun readWithTooFewBytes() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            BlobInputStream(mock(), limit = -1)
        }
    }

    @Test
    fun readWithTooManyBytes() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            BlobInputStream(mock(), limit = 1)
        }
    }

    @Test
    fun readEmpty() {
        val blob = mock<SQLBlob>().apply {
            whenever(size) doReturn 0
        }
        BlobInputStream(blob).use {
            assertEquals(-1, it.read())
        }
    }

    @Test
    fun readToEmptyBuffer() {
        val expectedBytes = ByteArray(1) { 0x42 }
        val blob = mock<SQLBlob>().apply {
            whenever(size) doReturn expectedBytes.size
            whenever(read(any(), any(), any(), any())).doAnswer {
                val buffer = requireNotNull(it.arguments[1] as? ByteArray)
                System.arraycopy(
                    expectedBytes,
                    requireNotNull(it.arguments[0] as? Int),
                    buffer,
                    requireNotNull(it.arguments[2] as? Int),
                    requireNotNull(it.arguments[3] as? Int)
                )
            }
        }
        BlobInputStream(blob).use {
            val buffer = ByteArray(0)
            assertEquals(0, it.read(buffer, 0, buffer.size))
        }
    }

    @Test
    fun readSingleByte() {
        val expectedSize = 1
        val expectedByte = 0x42.toByte()
        val blob = mock<SQLBlob>().apply {
            whenever(size) doReturn expectedSize
            whenever(read(any(), any(), any(), any())).doAnswer {
                requireNotNull(it.arguments[1] as? ByteArray)[0] = expectedByte
                Unit
            }
        }
        BlobInputStream(blob).use {
            assertEquals(expectedByte.toInt(), it.read())
            assertEquals(0, it.available())
            assertEquals(-1, it.read())
        }
    }

    @Test
    fun readToBuffer() {
        val expectedBytes = ByteArray(100) { (it + 42).toByte() }
        val blob = mock<SQLBlob>().apply {
            whenever(size) doReturn expectedBytes.size
            whenever(read(any(), any(), any(), any())).doAnswer {
                val buffer = requireNotNull(it.arguments[1] as? ByteArray)
                System.arraycopy(
                    expectedBytes,
                    requireNotNull(it.arguments[0] as? Int),
                    buffer,
                    requireNotNull(it.arguments[2] as? Int),
                    requireNotNull(it.arguments[3] as? Int)
                )
            }
        }
        BlobInputStream(blob).use {
            val buffer = ByteArray(40)
            assertEquals(40, it.read(buffer, 0, buffer.size))
            buffer.forEachIndexed { index, byte -> assertEquals(expectedBytes[index], byte) }
            assertEquals(40, it.read(buffer, 0, buffer.size))
            buffer.forEachIndexed { index, byte -> assertEquals(expectedBytes[index + 40], byte) }
            assertEquals(20, it.read(buffer, 0, buffer.size))
            buffer.sliceArray(0 until 20).forEachIndexed { index, byte -> assertEquals(expectedBytes[index + 80], byte) }
            assertEquals(-1, it.read(buffer, 0, buffer.size))
        }
    }

    @Test
    fun readFromOffsetWithLimit() {
        val expectedBytes = ByteArray(100) { (it + 42).toByte() }
        val blob = mock<SQLBlob>().apply {
            whenever(size) doReturn expectedBytes.size
            whenever(read(any(), any(), any(), any())).doAnswer {
                val buffer = requireNotNull(it.arguments[1] as? ByteArray)
                System.arraycopy(
                    expectedBytes,
                    requireNotNull(it.arguments[0] as? Int),
                    buffer,
                    requireNotNull(it.arguments[2] as? Int),
                    requireNotNull(it.arguments[3] as? Int)
                )
            }
        }
        BlobInputStream(blob, 40, 20).use {
            val buffer = ByteArray(20)
            assertEquals(20, it.read(buffer, 0, buffer.size))
            buffer.forEachIndexed { index, byte -> assertEquals(expectedBytes[40 + index], byte) }
            assertEquals(-1, it.read(buffer, 0, buffer.size))
        }
    }
}
