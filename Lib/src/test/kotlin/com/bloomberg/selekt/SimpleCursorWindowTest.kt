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

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.RuleChain
import org.junit.rules.Timeout
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SimpleCursorWindowTest {
    @Rule
    @JvmField
    val rule: RuleChain = RuleChain.outerRule(DisableOnDebug(Timeout.seconds(10L)))

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
    fun getBlob() = window.run {
        val blob = ByteArray(32) { 42 }
        allocateRow()
        put(blob)
        assertSame(blob, getBlob(0, 0))
    }

    @Test
    fun getDouble() = window.run {
        allocateRow()
        put(42.0)
        assertEquals(42.0, getDouble(0, 0))
    }

    @Test
    fun getFloat() = window.run {
        allocateRow()
        put(42.0f)
        assertEquals(42.0f, getFloat(0, 0))
    }

    @Test
    fun getInt() = window.run {
        allocateRow()
        put(42)
        assertEquals(42, getInt(0, 0))
    }

    @Test
    fun getLong() = window.run {
        allocateRow()
        put(42L)
        assertEquals(42L, getLong(0, 0))
    }

    @Test
    fun getShort() = window.run {
        allocateRow()
        put(42.toShort())
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
    fun getStringBlob() = window.run {
        val text = "foo"
        allocateRow()
        put(text)
        assertTrue(requireNotNull(getBlob(0, 0)).contentEquals(byteArrayOf(102, 111, 111)))
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
        assertThatExceptionOfType(NoSuchElementException::class.java).isThrownBy {
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
