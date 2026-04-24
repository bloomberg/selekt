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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

internal class JdbcClobTest {
    @Test
    fun emptyConstructor() {
        assertEquals(0L, JdbcClob().length())
    }

    @Test
    fun constructorWithInitialContent() {
        val content = "Hello, World!"
        val clob = JdbcClob(content)
        assertEquals(content.length.toLong(), clob.length())
        assertEquals(content, clob.getSubString(1, content.length))
    }

    @Test
    fun getSubString(): Unit = JdbcClob("Hello, World!").run {
        assertEquals("Hello, World!", getSubString(1, 13))
        assertEquals("Hello", getSubString(1, 5))
        assertEquals("World", getSubString(8, 5))
        assertEquals("World!", getSubString(8, 20))
    }

    @Test
    fun getSubStringInvalidPosition(): Unit = JdbcClob("Hello").run {
        assertFailsWith<SQLException> {
            getSubString(0, 5)
        }
        assertFailsWith<SQLException> {
            getSubString(-1, 5)
        }
        assertFailsWith<SQLException> {
            getSubString(10, 5)
        }
    }

    @Test
    fun getSubStringNegativeLength() {
        assertFailsWith<SQLException> {
            JdbcClob("Hello").getSubString(1, -1)
        }
    }

    @Test
    fun getCharacterStream() {
        val content = "Hello, World!"
        val clob = JdbcClob(content)
        val reader = clob.getCharacterStream()
        val result = reader.readText()
        assertEquals(content, result)
    }

    @Test
    fun getCharacterStreamWithPosition() {
        assertEquals("World", JdbcClob("Hello, World!").getCharacterStream(8, 5).readText())
    }

    @Test
    fun getAsciiStream() {
        val content = "Hello"
        val clob = JdbcClob(content)
        val stream = clob.getAsciiStream()
        val result = stream.readBytes().toString(Charsets.US_ASCII)
        assertEquals(content, result)
    }

    @Test
    fun positionString(): Unit = JdbcClob("Hello, World! Hello again!").run {
        assertEquals(1L, position("Hello", 1))
        assertEquals(15L, position("Hello", 2))
        assertEquals(-1L, position("Goodbye", 1))
        assertEquals(8L, position("World", 1))
    }

    @Test
    fun positionStringInvalidStart(): Unit = JdbcClob("Hello").run {
        assertFailsWith<SQLException> {
            position("Hello", 0)
        }
    }

    @Test
    fun positionStringPastEnd() {
        assertEquals(-1L, JdbcClob("Hello").position("Hello", 10))
    }

    @Test
    fun positionClob() {
        val clob = JdbcClob("Hello, World!")
        val searchClob = JdbcClob("World")
        assertEquals(8L, clob.position(searchClob, 1))
    }

    @Test
    fun setStringSimple() {
        val clob = JdbcClob()
        val written = clob.setString(1, "Hello")
        assertEquals(5, written)
        assertEquals("Hello", clob.getSubString(1, 5))
    }

    @Test
    fun setStringReplace(): Unit = JdbcClob("Hello, World!").run {
        setString(8, "Earth")
        assertEquals("Hello, Earth!", getSubString(1, 13))
    }

    @Test
    fun setStringReplaceOverflowsContent(): Unit = JdbcClob("ABCD").run {
        setString(2, "XYZW")
        assertEquals("AXYZW", asString())
    }

    @Test
    fun setStringReplaceExactFit(): Unit = JdbcClob("ABCD").run {
        setString(2, "XYZ")
        assertEquals("AXYZ", asString())
    }

    @Test
    fun setStringReplaceShorter(): Unit = JdbcClob("ABCD").run {
        setString(2, "X")
        assertEquals("AXCD", asString())
    }

    @Test
    fun setStringExtend(): Unit = JdbcClob("Hello").run {
        setString(7, "World")
        assertEquals("Hello World", getSubString(1, 11))
    }

    @Test
    fun setStringWithOffset(): Unit = JdbcClob().run {
        val written = setString(1, "Hello, World!", 7, 5)
        assertEquals(5, written)
        assertEquals("World", getSubString(1, 5))
    }

    @Test
    fun setStringInvalidPosition(): Unit = JdbcClob().run {
        assertFailsWith<SQLException> {
            setString(0, "Hello")
        }
    }

    @Test
    fun setStringInvalidOffset() {
        val clob = JdbcClob()
        assertFailsWith<SQLException> {
            clob.setString(1, "Hello", -1, 5)
        }
        assertFailsWith<SQLException> {
            clob.setString(1, "Hello", 10, 5)
        }
    }

    @Test
    fun setStringInvalidLength() {
        val clob = JdbcClob()
        assertFailsWith<SQLException> {
            clob.setString(1, "Hello", 0, -1)
        }
        assertFailsWith<SQLException> {
            clob.setString(1, "Hello", 0, 10)
        }
    }

    @Test
    fun setCharacterStream() {
        val clob = JdbcClob()
        val writer = clob.setCharacterStream(1)
        writer.write("Hello, World!")
        writer.flush()
        assertEquals("Hello, World!", clob.getSubString(1, 13))
    }

    @Test
    fun setCharacterStreamAtPosition() {
        val clob = JdbcClob("Hello, World!")
        val writer = clob.setCharacterStream(8)
        writer.write("Earth!")
        writer.flush()
        assertEquals("Hello, Earth!", clob.getSubString(1, 13))
    }

    @Test
    fun setCharacterStreamInvalidPosition() {
        val clob = JdbcClob()
        assertFailsWith<SQLException> {
            clob.setCharacterStream(0)
        }
    }

    @Test
    fun setAsciiStream() {
        val clob = JdbcClob()
        val stream = clob.setAsciiStream(1)
        stream.write("Hello".toByteArray(Charsets.US_ASCII))
        stream.flush()
        assertEquals("Hello", clob.getSubString(1, 5))
    }

    @Test
    fun setAsciiStreamSingleByte() {
        val clob = JdbcClob()
        val stream = clob.setAsciiStream(1)
        stream.write('H'.code)
        stream.write('i'.code)
        stream.flush()
        assertEquals("Hi", clob.getSubString(1, 2))
    }

    @Test
    fun setAsciiStreamReplace() {
        val clob = JdbcClob("Hello, World!")
        val stream = clob.setAsciiStream(8)
        stream.write("Earth".toByteArray(Charsets.US_ASCII))
        stream.flush()
        assertEquals("Hello, Earth!", clob.getSubString(1, 13))
    }

    @Test
    fun setAsciiStreamInvalidPosition() {
        val clob = JdbcClob()
        assertFailsWith<SQLException> {
            clob.setAsciiStream(0)
        }
    }

    @Test
    fun truncate() {
        val clob = JdbcClob("Hello, World!")
        clob.truncate(5)
        assertEquals(5L, clob.length())
        assertEquals("Hello", clob.getSubString(1, 5))
    }

    @Test
    fun truncateToZero() {
        val clob = JdbcClob("Hello")
        clob.truncate(0)
        assertEquals(0L, clob.length())
    }

    @Test
    fun truncateBeyondLength() {
        val clob = JdbcClob("Hello")
        clob.truncate(10)
        assertEquals(5L, clob.length())
        assertEquals("Hello", clob.getSubString(1, 5))
    }

    @Test
    fun truncateNegative() {
        val clob = JdbcClob("Hello")
        assertFailsWith<SQLException> {
            clob.truncate(-1)
        }
    }

    @Test
    fun free() {
        val clob = JdbcClob("Hello")
        clob.free()
        assertFailsWith<SQLException> {
            clob.length()
        }
        assertFailsWith<SQLException> {
            clob.getSubString(1, 5)
        }
        assertFailsWith<SQLException> {
            clob.setString(1, "World")
        }
    }

    @Test
    fun freeIdempotent() {
        val clob = JdbcClob("Hello")
        clob.free()
        clob.free()
    }

    @Test
    fun asString() {
        val content = "Hello, World!"
        val clob = JdbcClob(content)
        assertEquals(content, clob.asString())
    }

    @Test
    fun asStringAfterModification(): Unit = JdbcClob("Hello").run {
        setString(7, "World")
        assertEquals("Hello World", asString())
    }

    @Test
    fun asStringAfterFree(): Unit = JdbcClob("Hello").run {
        free()
        assertFailsWith<SQLException> {
            asString()
        }
    }

    @Test
    fun complexModificationSequence(): Unit = JdbcClob().run {
        setString(1, "Hello")
        assertEquals("Hello", asString())
        setString(6, ", ")
        assertEquals("Hello, ", asString())
        setString(8, "World!")
        assertEquals("Hello, World!", asString())
        setString(8, "Earth!")
        assertEquals("Hello, Earth!", asString())
        truncate(7)
        assertEquals("Hello, ", asString())
    }

    @Test
    fun streamWriterInteraction(): Unit = JdbcClob().run {
        setAsciiStream(1).run {
            write("Hello".toByteArray(Charsets.US_ASCII))
            flush()
        }
        setCharacterStream(6).run {
            write(" World")
            flush()
        }
        assertEquals("Hello World", asString())
    }

    @Test
    fun characterStreamOverflowsContent(): Unit = JdbcClob("ABCD").run {
        setCharacterStream(2).run {
            write("XYZW")
            flush()
        }
        assertEquals("AXYZW", asString())
    }

    @Test
    fun asciiStreamOverflowsContent(): Unit = JdbcClob("ABCD").run {
        setAsciiStream(2).run {
            write("XYZW".toByteArray(Charsets.US_ASCII))
            flush()
        }
        assertEquals("AXYZW", asString())
    }

    @Test
    fun positionAfterModification(): Unit = JdbcClob("Hello, World!").run {
        assertEquals(8L, position("World", 1))
        setString(8, "Earth")
        assertEquals(-1L, position("World", 1))
        assertEquals(8L, position("Earth", 1))
    }

    @Test
    fun setStringWithGap(): Unit = JdbcClob("Hi").apply {
        setString(5, "There")
    }.asString().run {
        assertEquals(9, length)
        assertTrue(startsWith("Hi"))
        assertTrue(endsWith("There"))
        assertEquals(' ', this[2])
        assertEquals(' ', this[3])
    }
}
