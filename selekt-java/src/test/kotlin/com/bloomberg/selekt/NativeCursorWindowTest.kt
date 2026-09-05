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

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

private const val SLOT_SIZE_BYTES = 1 + Long.SIZE_BYTES

private fun payloadSize(value: Any?): Int = when (value) {
    is String -> value.toByteArray(StandardCharsets.UTF_8).size
    is ByteArray -> value.size
    null, is Long, is Double -> 0
    else -> error("Unsupported value type: ${value.javaClass}.")
}

private fun cursorWindowBuffer(vararg rows: List<Any?>, totalCount: Int = rows.size): ByteBuffer {
    val headerSize = 2 * Int.SIZE_BYTES
    val totalSize = headerSize + rows.sumOf { row ->
        row.size * SLOT_SIZE_BYTES + row.sumOf(::payloadSize)
    } + rows.size * Int.SIZE_BYTES
    return ByteBuffer.allocate(totalSize).order(ByteOrder.nativeOrder()).apply {
        putInt(0, rows.size)
        putInt(Int.SIZE_BYTES, totalCount)
        var offset = headerSize
        val rowOffsets = IntArray(rows.size)
        rows.forEachIndexed { index, row ->
            rowOffsets[index] = offset
            val rowOffset = offset
            offset += row.size * SLOT_SIZE_BYTES
            row.forEachIndexed { column, value ->
                val slotOffset = rowOffset + column * SLOT_SIZE_BYTES
                when (value) {
                    null -> {
                        put(slotOffset, SQL_NULL.toByte())
                        putLong(slotOffset + 1, 0L)
                    }
                    is Long -> {
                        put(slotOffset, SQL_INTEGER.toByte())
                        putLong(slotOffset + 1, value)
                    }
                    is Double -> {
                        put(slotOffset, SQL_FLOAT.toByte())
                        putDouble(slotOffset + 1, value)
                    }
                    is String -> value.toByteArray(StandardCharsets.UTF_8).let {
                        put(slotOffset, SQL_TEXT.toByte())
                        putInt(slotOffset + 1, it.size)
                        putInt(slotOffset + 1 + Int.SIZE_BYTES, offset)
                        position(offset)
                        put(it)
                        offset += it.size
                    }
                    is ByteArray -> {
                        put(slotOffset, SQL_BLOB.toByte())
                        putInt(slotOffset + 1, value.size)
                        putInt(slotOffset + 1 + Int.SIZE_BYTES, offset)
                        position(offset)
                        put(value)
                        offset += value.size
                    }
                }
            }
        }
        rowOffsets.forEach {
            putInt(offset, it)
            offset += Int.SIZE_BYTES
        }
    }
}

private fun fakeSQLite(): SQLite = mock()

internal class NativeCursorWindowTest {
    @Test
    fun close() {
        val sqlite = mock<SQLite>()
        val window = NativeCursorWindow(cursorWindowBuffer(), sqlite, 0)
        window.close()
        verify(sqlite, times(1)).freeCursorWindow(any())
    }

    @Test
    fun closeIsIdempotent() {
        val sqlite = mock<SQLite>()
        val window = NativeCursorWindow(cursorWindowBuffer(), sqlite, 0)
        window.close()
        window.close()
        verify(sqlite, times(1)).freeCursorWindow(any())
    }

    @Test
    fun readsFailAfterClose() {
        val window = NativeCursorWindow(cursorWindowBuffer(listOf(1L)), mock(), 1)
        window.close()
        assertFailsWith<IllegalStateException> { window.getLong(0, 0) }
    }

    @Test
    fun readsRejectInvalidRowsAndColumns() {
        NativeCursorWindow(cursorWindowBuffer(listOf(1L)), mock(), 1).use { window ->
            assertFailsWith<IndexOutOfBoundsException> { window.getLong(-1, 0) }
            assertFailsWith<IndexOutOfBoundsException> { window.getLong(1, 0) }
            assertFailsWith<IndexOutOfBoundsException> { window.getLong(0, -1) }
            assertFailsWith<IndexOutOfBoundsException> { window.getLong(0, 1) }
        }
    }

    @Test
    @Suppress("Detekt.ExplicitGarbageCollectionCall")
    fun cleanerFreesWindowWhenNeverClosed() {
        val sqlite = mock<SQLite>()
        createUnreferencedWindow(sqlite)
        val deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (System.nanoTime() < deadlineNanos) {
            System.gc()
            try {
                verify(sqlite, times(1)).freeCursorWindow(any())
                return
            } catch (@Suppress("SwallowedException") e: AssertionError) {
                Thread.sleep(50L)
            }
        }
        fail("Cleaner did not free the native cursor window before the test timeout.")
    }

    private fun createUnreferencedWindow(sqlite: SQLite) {
        NativeCursorWindow(cursorWindowBuffer(), sqlite, 0)
    }

    @Test
    fun emptyWindowHasNoRows() {
        NativeCursorWindow(cursorWindowBuffer(), mock(), 0).use {
            assertEquals(0, it.numberOfRows())
        }
    }

    @Test
    fun readsEveryColumnType() {
        NativeCursorWindow(
            cursorWindowBuffer(listOf(42L, 3.5, "hello", byteArrayOf(1, 2), null)),
            fakeSQLite(),
            5
        ).use {
            assertEquals(1, it.numberOfRows())
            assertEquals(ColumnType.INTEGER, it.type(0, 0))
            assertEquals(42L, it.getLong(0, 0))
            assertEquals(ColumnType.FLOAT, it.type(0, 1))
            assertEquals(3.5, it.getDouble(0, 1))
            assertEquals(ColumnType.STRING, it.type(0, 2))
            assertEquals("hello", it.getString(0, 2))
            assertEquals(ColumnType.BLOB, it.type(0, 3))
            assertContentEquals(byteArrayOf(1, 2), it.getBlob(0, 3))
            assertEquals(ColumnType.NULL, it.type(0, 4))
            assertTrue(it.isNull(0, 4))
            assertNull(it.getString(0, 4))
            assertNull(it.getBlob(0, 4))
        }
    }

    @Test
    fun readsMultipleRows() {
        NativeCursorWindow(
            cursorWindowBuffer(
                listOf(1L, "one"),
                listOf(2L, "two"),
                listOf(3L, "three")
            ),
            fakeSQLite(),
            2
        ).use {
            assertEquals(3, it.numberOfRows())
            assertEquals(1L, it.getLong(0, 0))
            assertEquals("one", it.getString(0, 1))
            assertEquals(2L, it.getLong(1, 0))
            assertEquals("two", it.getString(1, 1))
            assertEquals(3L, it.getLong(2, 0))
            assertEquals("three", it.getString(2, 1))
        }
    }

    @Test
    fun coercesAcrossColumnTypes() {
        NativeCursorWindow(cursorWindowBuffer(listOf(42L, 3.5, "7")), fakeSQLite(), 3).use {
            assertEquals("42", it.getString(0, 0))
            assertEquals(42.0, it.getDouble(0, 0))
            assertEquals(42, it.getInt(0, 0))
            assertEquals(4, it.getInt(0, 1))
            assertEquals(4L, it.getLong(0, 1))
            assertEquals("3.5", it.getString(0, 1))
            assertEquals(7L, it.getLong(0, 2))
            assertEquals(7.0, it.getDouble(0, 2))
        }
    }

    @Test
    fun preservesDoubleToStringPrecision() {
        val value = 123_456_789.123456
        NativeCursorWindow(cursorWindowBuffer(listOf(value)), fakeSQLite(), 1).use {
            assertEquals(value.toString(), it.getString(0, 0))
        }
    }

    @Test
    fun readsEmptyBlob() {
        NativeCursorWindow(cursorWindowBuffer(listOf(byteArrayOf())), fakeSQLite(), 1).use {
            assertEquals(ColumnType.BLOB, it.type(0, 0))
            assertContentEquals(byteArrayOf(), it.getBlob(0, 0))
        }
    }

    @Test
    fun readsEmptyText() {
        NativeCursorWindow(cursorWindowBuffer(listOf("", 1L)), fakeSQLite(), 2).use {
            assertEquals(ColumnType.STRING, it.type(0, 0))
            assertEquals("", it.getString(0, 0))
            assertEquals(1L, it.getLong(0, 1))
        }
    }

    @Test
    fun readsMultiByteText() {
        NativeCursorWindow(cursorWindowBuffer(listOf("😀ñ", 1L)), fakeSQLite(), 2).use {
            assertEquals("😀ñ", it.getString(0, 0))
            assertEquals(1L, it.getLong(0, 1))
        }
    }

    @Test
    fun returnedStringsDoNotShareTheReusableDecodeBuffer() {
        val longer = "a much longer 😀 value"
        NativeCursorWindow(cursorWindowBuffer(listOf(longer), listOf("x")), fakeSQLite(), 1).use {
            val first = it.getString(0, 0)
            assertEquals("x", it.getString(1, 0))
            assertEquals(longer, first)
        }
    }

    @Test
    fun mutatorsAreUnsupported() {
        NativeCursorWindow(cursorWindowBuffer(), mock(), 0).use { window ->
            listOf<() -> Any?>(
                { window.allocateRow() },
                { window.clear() },
                { window.put(1) },
                { window.put(1L) },
                { window.put(1.0) },
                { window.put(1.0f) },
                { window.put(1.toShort()) },
                { window.put("a") },
                { window.put(byteArrayOf(1)) },
                { window.putNull() }
            ).forEach {
                assertFailsWith<UnsupportedOperationException> { it() }
            }
        }
    }
}
