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

package com.bloomberg.selekt.jdbc.lob

import java.sql.Blob
import java.sql.SQLException
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

internal class JdbcBlobTest {
    private val testData = "Hello".toByteArray()

    @Test
    fun emptyConstructor() {
        assertEquals(0L, JdbcBlob().length())
    }

    @Test
    fun constructorWithInitialContent() {
        val blob = JdbcBlob(testData)
        assertEquals(testData.size.toLong(), blob.length())
        assertEquals(testData.joinToString(",") { it.toString() },
            blob.getBytes(1, testData.size).joinToString(",") { it.toString() })
    }

    @Test
    fun getBytes() {
        JdbcBlob(testData).run {
            val allBytes = getBytes(1, testData.size)
            assertEquals(testData.size, allBytes.size)
            testData.forEachIndexed { index, byte ->
                assertEquals(byte, allBytes[index])
            }
        }
    }

    @Test
    fun getBytesPartial() {
        JdbcBlob(testData).run {
            val bytes = getBytes(2, 3)
            assertContentEquals("ell".toByteArray(), bytes)
        }
    }

    @Test
    fun getBytesInvalidPosition() {
        JdbcBlob(testData).run {
            assertFailsWith<SQLException> {
                getBytes(0, 5)
            }
            assertFailsWith<SQLException> {
                getBytes(-1, 5)
            }
            assertFailsWith<SQLException> {
                getBytes(10, 5)
            }
        }
    }

    @Test
    fun getBytesNegativeLength() {
        assertFailsWith<SQLException> {
            JdbcBlob(testData).getBytes(1, -1)
        }
    }

    @Test
    fun getBytesRejectsUnrepresentablePosition() {
        val blob = JdbcBlob(testData)
        assertFailsWith<SQLException> {
            blob.getBytes(Int.MAX_VALUE.toLong() + 2L, 1)
        }
        assertFailsWith<SQLException> {
            blob.getBytes(Long.MAX_VALUE, 1)
        }
    }

    @Test
    fun getBytesDoesNotOverflowEndIndex() {
        assertContentEquals("ello".toByteArray(), JdbcBlob(testData).getBytes(2, Int.MAX_VALUE))
    }

    @Test
    fun getBinaryStream() {
        val blob = JdbcBlob(testData)
        val stream = blob.getBinaryStream()
        val result = stream.readBytes()
        assertEquals(testData.size, result.size)
        testData.forEachIndexed { index, byte ->
            assertEquals(byte, result[index])
        }
    }

    @Test
    fun getBinaryStreamWithPosition() {
        val blob = JdbcBlob(testData)
        val stream = blob.getBinaryStream(2, 3)
        val result = stream.readBytes()
        assertContentEquals("ell".toByteArray(), result)
    }

    @Test
    fun getBinaryStreamValidatesLongLengthBeforeConversion() {
        val blob = JdbcBlob(testData)
        assertContentEquals("ello".toByteArray(), blob.getBinaryStream(2, Int.MAX_VALUE.toLong()).readBytes())
        assertFailsWith<SQLException> {
            blob.getBinaryStream(1, -1L)
        }
        assertFailsWith<SQLException> {
            blob.getBinaryStream(1, Int.MAX_VALUE.toLong() + 1L)
        }
    }

    @Test
    fun positionByteArray() {
        val data = "Hello, World".toByteArray()
        val blob = JdbcBlob(data)
        val helloPattern = "Hello".toByteArray()
        assertEquals(1L, blob.position(helloPattern, 1))
        val worldPattern = "World".toByteArray()
        assertEquals(8L, blob.position(worldPattern, 1))
        val notFoundPattern = "BBB".toByteArray()
        assertEquals(-1L, blob.position(notFoundPattern, 1))
    }

    @Test
    fun positionByteArrayInvalidStart() {
        JdbcBlob(testData).run {
            assertFailsWith<SQLException> {
                position(testData, 0)
            }
        }
    }

    @Test
    fun positionByteArrayDoesNotWrapLargeStart() {
        assertEquals(-1L, JdbcBlob(testData).position(testData, Long.MAX_VALUE))
    }

    @Test
    fun positionBlob() {
        val blob = JdbcBlob("Hello World".toByteArray())
        val searchBlob = JdbcBlob("World".toByteArray())
        assertEquals(7L, blob.position(searchBlob, 1))
    }

    @Test
    fun positionBlobRejectsUnrepresentableSearchLength() {
        val searchBlob = object : Blob by JdbcBlob() {
            override fun length(): Long = Int.MAX_VALUE.toLong() + 1L
        }
        assertFailsWith<SQLException> {
            JdbcBlob(testData).position(searchBlob, 1)
        }
    }

    @Test
    fun setBytes() {
        val blob = JdbcBlob()
        val newBytes = "Hello".toByteArray()
        val written = blob.setBytes(1, newBytes)
        assertEquals(5, written)
        assertEquals(newBytes.joinToString(",") { it.toString() },
            blob.getBytes(1, 5).joinToString(",") { it.toString() })
    }

    @Test
    fun setBytesReplace() {
        val blob = JdbcBlob("Hello World".toByteArray())
        blob.setBytes(8, "Earth".toByteArray())
        val result = blob.getBytes(1, 11)
        assertEquals(11, result.size)
        assertEquals("E".toByteArray()[0], result[7])
    }

    @Test
    fun setBytesWithOffset() {
        val blob = JdbcBlob()
        val written = blob.setBytes(1, "Hello, World".toByteArray(), 6, 5)
        assertEquals(5, written)
        assertContentEquals(" Worl".toByteArray(), blob.getBytes(1, 5))
    }

    @Test
    fun setBytesRejectsInvalidOffset() {
        val blob = JdbcBlob()
        assertFailsWith<SQLException> { blob.setBytes(1, testData, -1, 1) }
        assertFailsWith<SQLException> { blob.setBytes(1, testData, testData.size + 1, 0) }
    }

    @Test
    fun setBytesRejectsOverflowingOffsetPlusLength() {
        assertFailsWith<SQLException> {
            JdbcBlob().setBytes(1, byteArrayOf(1), 1, Int.MAX_VALUE)
        }
    }

    @Test
    fun setBytesInvalidPosition() {
        val blob = JdbcBlob()
        assertFailsWith<SQLException> {
            blob.setBytes(0, testData)
        }
    }

    @Test
    fun setBinaryStream() {
        val blob = JdbcBlob()
        val stream = blob.setBinaryStream(1)
        stream.write(testData)
        stream.flush()
        stream.close()
        val result = blob.getBytes(1, testData.size)
        assertEquals(testData.joinToString(",") { it.toString() },
            result.joinToString(",") { it.toString() })
    }

    @Test
    fun setBinaryStreamSingleByte() {
        val blob = JdbcBlob()
        val stream = blob.setBinaryStream(1)
        "Hi".forEach { stream.write(it.code) }
        stream.flush()
        val result = blob.getBytes(1, 2)
        assertContentEquals("Hi".toByteArray(), result)
    }

    @Test
    fun setBinaryStreamReplace() {
        val blob = JdbcBlob("Hello World".toByteArray())
        val stream = blob.setBinaryStream(8)
        stream.write("Earth".toByteArray())
        stream.flush()
        val result = blob.getBytes(1, 11)
        assertEquals(11, result.size)
        assertEquals("E".toByteArray()[0], result[7])
    }

    @Test
    fun setBinaryStreamInvalidPosition() {
        val blob = JdbcBlob()
        assertFailsWith<SQLException> {
            blob.setBinaryStream(0)
        }
    }

    @Test
    fun truncate() {
        val blob = JdbcBlob(testData)
        blob.truncate(3)
        assertEquals(3L, blob.length())
        val result = blob.getBytes(1, 3)
        assertContentEquals("Hel".toByteArray(), result)
    }

    @Test
    fun truncateToZero() {
        val blob = JdbcBlob(testData)
        blob.truncate(0)
        assertEquals(0L, blob.length())
    }

    @Test
    fun truncateBeyondLength() {
        val blob = JdbcBlob(testData)
        blob.truncate(20)
        assertEquals(testData.size.toLong(), blob.length())
    }

    @Test
    fun truncateNegative() {
        val blob = JdbcBlob(testData)
        assertFailsWith<SQLException> {
            blob.truncate(-1)
        }
    }

    @Test
    fun free() {
        val blob = JdbcBlob(testData)
        blob.free()
        assertFailsWith<SQLException> {
            blob.length()
        }
        assertFailsWith<SQLException> {
            blob.getBytes(1, 5)
        }
        assertFailsWith<SQLException> {
            blob.setBytes(1, "B".toByteArray())
        }
    }

    @Test
    fun freeIdempotent() {
        val blob = JdbcBlob(testData)
        blob.free()
        blob.free()
    }

    @Test
    fun asBytes() {
        val blob = JdbcBlob(testData)
        val result = blob.asBytes()
        assertEquals(testData.joinToString(",") { it.toString() },
            result.joinToString(",") { it.toString() })
    }

    @Test
    fun asBytesAfterModification() {
        val blob = JdbcBlob(testData)
        blob.setBytes(6, "World".toByteArray())
        val result = blob.asBytes()
        assertEquals(10, result.size)
    }

    @Test
    fun asBytesAfterFree() {
        val blob = JdbcBlob(testData)
        blob.free()
        assertFailsWith<SQLException> {
            blob.asBytes()
        }
    }

    @Test
    fun complexModificationSequence() {
        JdbcBlob().run {
            setBytes(1, "Hello".toByteArray())
            assertEquals(5L, length())
            setBytes(6, ", ".toByteArray())
            assertEquals(7L, length())
            setBytes(8, "World!".toByteArray())
            assertEquals(13L, length())
            truncate(7)
            assertEquals(7L, length())
        }
    }

    @Test
    fun streamReaderInteraction() {
        JdbcBlob().run {
            setBinaryStream(1).run {
                write("Hello".toByteArray())
                flush()
            }
            setBinaryStream(6).run {
                write(" World".toByteArray())
                flush()
            }
            assertEquals(11L, length())
        }
    }

    @Test
    fun binaryStreamOverflowsContent() {
        JdbcBlob("ABCD".toByteArray()).run {
            setBinaryStream(2).run {
                write("XYZW".toByteArray())
                flush()
            }
            val result = asBytes()
            assertEquals(5, result.size)
            assertContentEquals("AXYZW".toByteArray(), result)
        }
    }

    @Test
    fun positionAfterModification() {
        val data = "Hello, World".toByteArray()
        val blob = JdbcBlob(data)
        val worldPattern = "World".toByteArray()
        assertEquals(8L, blob.position(worldPattern, 1))
        blob.setBytes(8, "Earth".toByteArray())
        assertEquals(-1L, blob.position(worldPattern, 1))
    }

    @Test
    fun setBytesRejectsGapWithoutChangingContent() {
        val blob = JdbcBlob("Hi".toByteArray())
        assertFailsWith<SQLException> {
            blob.setBytes(4, "There".toByteArray())
        }
        assertContentEquals("Hi".toByteArray(), blob.asBytes())
    }

    @Test
    fun setBytesRejectsPositionExceedingIntMax() {
        val blob = JdbcBlob(testData)
        val overflow = Int.MAX_VALUE.toLong() + 2L
        assertFailsWith<SQLException> { blob.setBytes(overflow, "x".toByteArray()) }
        assertFailsWith<SQLException> { blob.setBytes(Long.MAX_VALUE, "x".toByteArray()) }
        assertEquals(testData.size.toLong(), blob.length())
    }

    @Test
    fun setBytesRejectsPositionPlusLengthExceedingIntMax() {
        assertFailsWith<SQLException> {
            JdbcBlob().setBytes(Int.MAX_VALUE.toLong(), ByteArray(16))
        }
    }

    @Test
    fun setBytesRejectsMaximumSparsePositionEvenForEmptyWrite() {
        val maximumLength = 16
        val blob = JdbcBlob(maximumLength = maximumLength)
        assertFailsWith<SQLException> {
            blob.setBytes(maximumLength.toLong() + 1L, byteArrayOf())
        }
        assertEquals(0L, blob.length())
    }

    @Test
    fun setBytesAcceptsEmptyWriteAtEnd() {
        val blob = JdbcBlob(testData)
        assertEquals(0, blob.setBytes(testData.size.toLong() + 1L, byteArrayOf()))
        assertContentEquals(testData, blob.asBytes())
    }

    @Test
    fun setBytesChecksGrowthBeforeChangingContent() {
        val blob = JdbcBlob("abc".toByteArray(), maximumLength = 4)
        assertFailsWith<SQLException> {
            blob.setBytes(4, byteArrayOf(1, 2))
        }
        assertContentEquals("abc".toByteArray(), blob.asBytes())
        assertEquals(1, blob.setBytes(4, "d".toByteArray()))
        assertContentEquals("abcd".toByteArray(), blob.asBytes())
    }

    @Test
    fun constructorRejectsMaximumLengthBelowInitialLength() {
        assertFailsWith<IllegalArgumentException> {
            JdbcBlob(byteArrayOf(1), maximumLength = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            JdbcBlob(maximumLength = -1)
        }
    }

    @Test
    fun setBinaryStreamRejectsPositionExceedingIntMax() {
        val blob = JdbcBlob(testData)
        assertFailsWith<SQLException> { blob.setBinaryStream(Int.MAX_VALUE.toLong() + 2L) }
        assertFailsWith<SQLException> { blob.setBinaryStream(Long.MAX_VALUE) }
    }

    @Test
    fun setBinaryStreamRejectsGapBeforeReturningStream() {
        val blob = JdbcBlob("Hi".toByteArray())
        assertFailsWith<SQLException> {
            blob.setBinaryStream(4)
        }
        assertContentEquals("Hi".toByteArray(), blob.asBytes())
    }

    @Test
    fun setBinaryStreamRejectsMaximumSparsePosition() {
        val maximumLength = 16
        val blob = JdbcBlob(maximumLength = maximumLength)
        assertFailsWith<SQLException> {
            blob.setBinaryStream(maximumLength.toLong() + 1L)
        }
        assertEquals(0L, blob.length())
    }

    @Test
    fun setBinaryStreamChecksGrowthBeforeChangingContent() {
        val blob = JdbcBlob("abc".toByteArray(), maximumLength = 4)
        val stream = blob.setBinaryStream(4)
        assertFailsWith<SQLException> {
            stream.write(byteArrayOf(1, 2))
        }
        assertContentEquals("abc".toByteArray(), blob.asBytes())
        stream.write('d'.code)
        assertContentEquals("abcd".toByteArray(), blob.asBytes())
        assertFailsWith<SQLException> { stream.write('e'.code) }
        assertContentEquals("abcd".toByteArray(), blob.asBytes())
    }

    @Test
    fun setBinaryStreamEmptyWriteDoesNotTruncateContent() {
        val blob = JdbcBlob("abcd".toByteArray())
        blob.setBinaryStream(2).write(byteArrayOf())
        assertContentEquals("abcd".toByteArray(), blob.asBytes())
    }

    @Test
    fun setBinaryStreamRevalidatesDeferredPosition() {
        val blob = JdbcBlob("abcd".toByteArray())
        val stream = blob.setBinaryStream(5)
        blob.truncate(2)
        assertFailsWith<SQLException> { stream.write('e'.code) }
        assertContentEquals("ab".toByteArray(), blob.asBytes())
    }

    @Test
    fun setBinaryStreamInvalidRangeDoesNotTruncateContent() {
        val blob = JdbcBlob("abcd".toByteArray())
        val stream = blob.setBinaryStream(2)
        assertFailsWith<IndexOutOfBoundsException> {
            stream.write(byteArrayOf(1, 2), 1, 2)
        }
        assertContentEquals("abcd".toByteArray(), blob.asBytes())
    }
}
