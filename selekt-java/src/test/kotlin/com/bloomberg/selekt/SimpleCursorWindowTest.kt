/*
 * Copyright 2020 Bloomberg Finance L.P.
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SimpleCursorWindowTest {
    private val window = SimpleCursorWindow()

    @Test
    fun allocateRow() = window.run {
        allocateRow()
        assertEquals(1, numberOfRows())
    }

    @Test
    fun clear() = window.run {
        allocateRow()
        assertEquals(1, numberOfRows())
        clear()
        assertEquals(0, numberOfRows())
    }

    @Test
    fun close() = window.run {
        allocateRow()
        assertEquals(1, numberOfRows())
        close()
        assertEquals(0, numberOfRows())
    }

    @Test
    fun getBlob() = window.run {
        val blob = ByteArray(1) { 42 }
        allocateRow()
        put(blob)
        assertSame(blob, getBlob(0, 0))
    }

    @Test
    fun getBlobAsNull() = window.run {
        allocateRow()
        putNull()
        assertSame(null, getBlob(0, 0))
    }

    @Test
    fun getBlobAsLong(): Unit = window.run {
        allocateRow()
        put(1L)
        assertFailsWith<IllegalStateException> {
            getBlob(0, 0)
        }
    }

    @Test
    fun getDouble() = window.run {
        allocateRow()
        put(42.0)
        assertEquals(42.0, getDouble(0, 0))
    }

    @Test
    fun getDoubleAsFloat() = window.run {
        allocateRow()
        put(42.0f)
        assertEquals(42.0, getDouble(0, 0))
    }

    @Test
    fun getDoubleAsLong() = window.run {
        allocateRow()
        put(42L)
        assertEquals(42.0, getDouble(0, 0))
    }

    @Test
    fun getDoubleAsString() = window.run {
        allocateRow()
        put("42.0")
        assertEquals(42.0, getDouble(0, 0))
    }

    @Test
    fun getDoubleAsNull() = window.run {
        allocateRow()
        putNull()
        assertEquals(0.0, getDouble(0, 0))
    }

    @Test
    fun getFloat() = window.run {
        allocateRow()
        put(42.0f)
        assertEquals(42.0f, getFloat(0, 0))
    }

    @Test
    fun getFloatAsDouble() = window.run {
        allocateRow()
        put(42.0)
        assertEquals(42.0f, getFloat(0, 0))
    }

    @Test
    fun getFloatAsNull() = window.run {
        allocateRow()
        putNull()
        assertEquals(0.0f, getFloat(0, 0))
    }

    @Test
    fun getFloatAsString() = window.run {
        allocateRow()
        put("42.0")
        assertEquals(42.0f, getFloat(0, 0))
    }

    @Test
    fun getInt() = window.run {
        allocateRow()
        put(42)
        assertEquals(42, getInt(0, 0))
    }

    @Test
    fun getIntAsNull() = window.run {
        allocateRow()
        putNull()
        assertEquals(0, getInt(0, 0))
    }

    @Test
    fun getIntAsLong() = window.run {
        allocateRow()
        put(42L)
        assertEquals(42, getInt(0, 0))
    }

    @Test
    fun getIntAsString() = window.run {
        allocateRow()
        put("42")
        assertEquals(42, getInt(0, 0))
    }

    @Test
    fun getIntAsDoubleRoundedDown() = window.run {
        allocateRow()
        put(42.3)
        assertEquals(42, getInt(0, 0))
    }

    @Test
    fun getIntAsDoubleRoundedUp() = window.run {
        allocateRow()
        put(42.7)
        assertEquals(43, getInt(0, 0))
    }

    @Test
    fun getIntAsFloatRoundedDown() = window.run {
        allocateRow()
        put(42.3f)
        assertEquals(42, getInt(0, 0))
    }

    @Test
    fun getIntAsFloatRoundedUp() = window.run {
        allocateRow()
        put(42.7f)
        assertEquals(43, getInt(0, 0))
    }

    @Test
    fun getLong() = window.run {
        allocateRow()
        put(42L)
        assertEquals(42L, getLong(0, 0))
    }

    @Test
    fun getLongAsNull() = window.run {
        allocateRow()
        putNull()
        assertEquals(0L, getLong(0, 0))
    }

    @Test
    fun getLongAsDoubleRoundedDown() = window.run {
        allocateRow()
        put(42.3)
        assertEquals(42L, getLong(0, 0))
    }

    @Test
    fun getLongAsDoubleRoundedUp() = window.run {
        allocateRow()
        put(42.7)
        assertEquals(43L, getLong(0, 0))
    }

    @Test
    fun getLongAsFloatRoundedDown() = window.run {
        allocateRow()
        put(42.3f)
        assertEquals(42L, getLong(0, 0))
    }

    @Test
    fun getLongAsFloatRoundedUp() = window.run {
        allocateRow()
        put(42.7f)
        assertEquals(43L, getLong(0, 0))
    }

    @Test
    fun getLongAsString() = window.run {
        allocateRow()
        put("42")
        assertEquals(42L, getLong(0, 0))
    }

    @Test
    fun getShort() = window.run {
        allocateRow()
        put(42.toShort())
        assertEquals(42.toShort(), getShort(0, 0))
    }

    @Test
    fun getShortAsNull() = window.run {
        allocateRow()
        putNull()
        assertEquals(0.toShort(), getShort(0, 0))
    }

    @Test
    fun getShortAsDoubleRoundedDown() = window.run {
        allocateRow()
        put(42.3)
        assertEquals(42.toShort(), getShort(0, 0))
    }

    @Test
    fun getShortAsDoubleRoundedUp() = window.run {
        allocateRow()
        put(42.7)
        assertEquals(43.toShort(), getShort(0, 0))
    }

    @Test
    fun getShortAsFloatRoundedDown() = window.run {
        allocateRow()
        put(42.3f)
        assertEquals(42.toShort(), getShort(0, 0))
    }

    @Test
    fun getShortAsFloatRoundedUp() = window.run {
        allocateRow()
        put(42.7f)
        assertEquals(43.toShort(), getShort(0, 0))
    }

    @Test
    fun getShortAsString() = window.run {
        allocateRow()
        put("42")
        assertEquals(42.toShort(), getShort(0, 0))
    }

    @Test
    fun getString() = window.run {
        val text = "foo"
        allocateRow()
        put(text)
        assertEquals(text, getString(0, 0))
    }

    @Test
    fun getStringAsNull() = window.run {
        allocateRow()
        putNull()
        assertEquals(null, getString(0, 0))
    }

    @Test
    fun getStringBlob() = window.run {
        val text = "foo"
        allocateRow()
        put(text)
        assertTrue(requireNotNull(getBlob(0, 0)) contentEquals byteArrayOf(102, 111, 111))
    }

    @Test
    fun getStringAsLong() = window.run {
        allocateRow()
        put(42L)
        assertEquals("42", getString(0, 0))
    }

    @Test
    fun isNull() = window.run {
        allocateRow()
        putNull()
        assertTrue(isNull(0, 0))
    }

    @Test
    fun numberOfRowsInitial() = window.run {
        assertEquals(0, numberOfRows())
    }

    @Test
    fun numberOfRowsSingle() = window.run {
        allocateRow()
        assertEquals(1, numberOfRows())
    }

    @Test
    fun numberOfRowsMultiple() = window.run {
        repeat(3) { allocateRow() }
        assertEquals(3, numberOfRows())
    }

    @Test
    fun putBeforeAllocatingRow(): Unit = window.run {
        assertFailsWith<NoSuchElementException> {
            put("foo")
        }
    }

    @Test
    fun type() = window.run {
        allocateRow()
        put("foo")
        assertSame(ColumnType.STRING, type(0, 0))
    }
}
