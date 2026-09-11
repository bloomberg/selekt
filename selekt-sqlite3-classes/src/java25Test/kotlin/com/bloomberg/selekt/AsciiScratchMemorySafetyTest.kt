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

import java.io.File
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.JAVA_BYTE
import java.lang.reflect.Field
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

private const val SQL_OPEN_READWRITE_OR_CREATE = 6
private const val SQL_RANGE = 25
private const val SQL_ROW = 100

internal class AsciiScratchMemorySafetyTest {
    @TempDir
    lateinit var tempDir: File

    private val sqlite = externalSQLiteSingleton()

    @Test
    fun `successful and rejected ASCII binds wipe statement scratch memory`() = withStatement { handle, scratch ->
        scratch.segment.fill(0x5a)
        assertEquals(SQL_OK, sqlite.bindTextAscii(handle, 1, "secret"))
        scratch.assertWiped()
        assertEquals(SQL_ROW, sqlite.step(handle))
        assertEquals("secret", sqlite.columnText(handle, 0))
        assertEquals(SQL_OK, sqlite.reset(handle))

        scratch.segment.fill(0x5a)
        assertEquals(SQL_MISMATCH, sqlite.bindTextAscii(handle, 1, "secrét"))
        scratch.assertWiped()
    }

    @Test
    fun `adaptive ASCII success and UTF-8 failover wipe statement scratch memory`() =
        withStatement { handle, scratch ->
            val utf8TextParameters = BooleanArray(2)
            scratch.segment.fill(0x5a)
            assertEquals(SQL_OK, sqlite.bindText(handle, 1, "secret", utf8TextParameters))
            scratch.assertWiped()
            assertEquals(SQL_OK, sqlite.reset(handle))

            scratch.segment.fill(0x5a)
            assertEquals(SQL_OK, sqlite.bindText(handle, 1, "secrét", utf8TextParameters))
            assertTrue(utf8TextParameters[1])
            scratch.assertWiped()
        }

    @Test
    fun `unsuccessful native bind wipes statement scratch memory`() = withStatement { handle, scratch ->
        scratch.segment.fill(0x5a)
        assertEquals(SQL_RANGE, sqlite.bindTextAscii(handle, 2, "secret"))
        scratch.assertWiped()
    }

    @Test
    fun `exception while populating scratch memory still wipes the writable span`() = withStatement { handle, scratch ->
        val shortScratch = MemorySegment.ofArray(ByteArray(2) { 0x5a })
        scratch.field.set(scratch.owner, shortScratch)
        assertFailsWith<IndexOutOfBoundsException> {
            sqlite.bindTextAscii(handle, 1, "secret")
        }
        assertWiped(shortScratch)
    }

    @Test
    fun `clear and reset clear statement scratch memory defensively`() = withStatement { handle, scratch ->
        scratch.segment.fill(0x5a)
        assertEquals(SQL_OK, sqlite.clearBindings(handle))
        scratch.assertWiped()

        scratch.segment.fill(0x5a)
        assertEquals(SQL_OK, sqlite.resetAndClearBindings(handle))
        scratch.assertWiped()
    }

    @Test
    fun `finalize wipes and releases statement scratch memory`() = withStatement { handle, scratch ->
        scratch.segment.fill(0x5a)
        assertEquals(SQL_OK, sqlite.finalize(handle))
        scratch.assertWiped()
        assertNull(scratch.field.get(scratch.owner))
    }

    private inline fun withStatement(block: (StatementHandle, Scratch) -> Unit) {
        val dbHolder = LongArray(1)
        assertEquals(
            SQL_OK,
            sqlite.openV2(File(tempDir, "scratch.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        )
        val statementHolder = LongArray(1)
        val sql = "SELECT ?"
        assertEquals(SQL_OK, sqlite.prepareV2(dbHolder.single(), sql, sql.length, statementHolder))
        val handle = sqlite.newStatementHandle(statementHolder.single())
        val scratch = scratch(handle)
        try {
            block(handle, scratch)
        } finally {
            if (scratch.field.get(scratch.owner) != null) {
                sqlite.finalize(handle)
            }
            sqlite.closeV2(dbHolder.single())
        }
    }

    private fun scratch(handle: StatementHandle): Scratch {
        val owner = checkNotNull(handle.attachment)
        val field = owner.javaClass.getDeclaredField("asciiText")
        assertTrue(field.trySetAccessible())
        return Scratch(owner, field, checkNotNull(field.get(owner) as? MemorySegment))
    }

    private data class Scratch(
        val owner: Any,
        val field: Field,
        val segment: MemorySegment
    ) {
        fun assertWiped() = assertWiped(segment)
    }
}

private fun assertWiped(segment: MemorySegment) {
    assertTrue((0L until segment.byteSize()).all { segment.get(JAVA_BYTE, it) == 0.toByte() })
}
