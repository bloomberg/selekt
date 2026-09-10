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

import java.lang.foreign.Arena
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.JAVA_BYTE

internal class SlabArena(
    capacity: Long = DEFAULT_CAPACITY
) : AutoCloseable {
    private var backingArena = Arena.ofConfined()
    private var slab: MemorySegment = backingArena.allocate(capacity)
    private var offset: Long = 0L

    private fun grow(required: Long) {
        backingArena.use { _ ->
            backingArena = Arena.ofConfined()
            slab = backingArena.allocate(maxOf(slab.byteSize() * 2, offset + required))
            offset = 0L
        }
    }

    fun allocate(byteSize: Long): MemorySegment {
        require(byteSize >= 0) { "byteSize must be non-negative, was: $byteSize" }
        if (offset + byteSize > slab.byteSize()) {
            grow(byteSize)
        }
        return slab.asSlice(offset, byteSize).also {
            offset += byteSize
        }
    }

    fun allocate(layout: MemoryLayout): MemorySegment {
        val align = layout.byteAlignment()
        val padding = (align - (offset % align)) % align
        val needed = padding + layout.byteSize()
        return if (offset + needed > slab.byteSize()) {
            grow(needed)
            allocate(layout)
        } else {
            offset += padding
            slab.asSlice(offset, layout.byteSize()).also {
                offset += layout.byteSize()
            }
        }
    }

    fun allocateFrom(value: String): MemorySegment = allocateFromAscii(value) ?: allocateFromNonAscii(value)

    fun allocateFromAscii(value: String): MemorySegment? {
        val length = value.length
        val needed = length + 1L
        if (offset + needed > slab.byteSize()) {
            grow(needed)
        }
        val segment = slab.asSlice(offset, needed)
        for (i in 0 until length) {
            val character = value[i]
            val byte = when {
                character.code < 0x80 -> character.code.toByte()
                character.isHighSurrogate() && i + 1 < length && value[i + 1].isLowSurrogate() -> return null
                character.isSurrogate() -> '?'.code.toByte()
                else -> return null
            }
            segment.set(JAVA_BYTE, i.toLong(), byte)
        }
        segment.set(JAVA_BYTE, length.toLong(), 0)
        offset += needed
        return segment
    }

    private fun allocateFromNonAscii(value: String): MemorySegment {
        val bytes = value.toByteArray(Charsets.UTF_8)
        val needed = bytes.size + 1L
        if (offset + needed > slab.byteSize()) {
            grow(needed)
        }
        return slab.asSlice(offset, needed).also {
            MemorySegment.copy(bytes, 0, it, JAVA_BYTE, 0, bytes.size)
            it.set(JAVA_BYTE, bytes.size.toLong(), 0)
            offset += needed
        }
    }

    fun allocateFromBytes(bytes: ByteArray): MemorySegment {
        val needed = bytes.size.toLong()
        if (offset + needed > slab.byteSize()) {
            grow(needed)
        }
        return slab.asSlice(offset, needed).also {
            MemorySegment.copy(bytes, 0, it, JAVA_BYTE, 0, bytes.size)
            offset += needed
        }
    }

    fun reset() {
        offset = 0L
    }

    override fun close() {
        backingArena.close()
    }

    companion object {
        private const val DEFAULT_CAPACITY = 4_096L
    }
}
