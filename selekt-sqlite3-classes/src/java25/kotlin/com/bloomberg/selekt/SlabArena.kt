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
import java.nio.charset.StandardCharsets

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

    fun allocateFrom(value: String): MemorySegment {
        val length = value.length
        for (i in 0 until length) {
            if (value[i].code >= 0x80) {
                return allocateFromNonAscii(value)
            }
        }
        return allocateFromAscii(value, length)
    }

    private fun allocateFromAscii(value: String, length: Int): MemorySegment {
        val needed = length + 1L
        if (offset + needed > slab.byteSize()) {
            grow(needed)
        }
        return slab.asSlice(offset, needed).also { segment ->
            for (i in 0 until length) {
                segment.set(JAVA_BYTE, i.toLong(), value[i].code.toByte())
            }
            segment.set(JAVA_BYTE, length.toLong(), 0)
            offset += needed
        }
    }

    private fun allocateFromNonAscii(value: String): MemorySegment {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
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
