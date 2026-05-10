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
    fun positionBlob() {
        val blob = JdbcBlob("Hello World".toByteArray())
        val searchBlob = JdbcBlob("World".toByteArray())
        assertEquals(7L, blob.position(searchBlob, 1))
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
    fun setBytesWithGap() {
        JdbcBlob("Hi".toByteArray()).apply {
            setBytes(5, "There".toByteArray())
        }.asBytes().run {
            assertEquals(9, size)
            assertContentEquals("Hi".toByteArray(), copyOfRange(0, 2))
            assertEquals(0, this[2].toInt()) // null padding
            assertEquals(0, this[3].toInt()) // null padding
            assertContentEquals("There".toByteArray(), copyOfRange(4, 9))
        }
    }
}

