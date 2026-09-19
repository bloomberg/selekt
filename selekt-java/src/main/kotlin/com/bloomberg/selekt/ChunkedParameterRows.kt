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

import javax.annotation.concurrent.NotThreadSafe

private const val DEFAULT_INITIAL_CHUNK_CAPACITY = 1024
private const val MAX_RETAINED_CAPACITY_FACTOR = 16

@NotThreadSafe
class ChunkedParameterRows(
    private val parameterCount: Int,
    private val initialChunkCapacity: Int = DEFAULT_INITIAL_CHUNK_CAPACITY
) : Iterable<ParameterRow> {
    @Suppress("Detekt.UseDataClass")
    private class Chunk(val capacity: Int, parameterCount: Int) {
        private val slotCount = Math.multiplyExact(capacity, parameterCount)
        val tags = ByteArray(slotCount)
        val values = LongArray(slotCount)
        val objects = arrayOfNulls<Any>(slotCount)
        var count = 0
        var next: Chunk? = null
    }

    private var firstChunk: Chunk? = null
    private var currentChunk: Chunk? = null
    private val maximumRetainedCapacity = minOf(
        initialChunkCapacity.toLong() * MAX_RETAINED_CAPACITY_FACTOR,
        Int.MAX_VALUE.toLong()
    ).toInt()

    var size = 0
        private set

    fun add(row: ParameterRow) {
        if (firstChunk == null) {
            firstChunk = Chunk(initialChunkCapacity, parameterCount)
            currentChunk = firstChunk
        }
        currentChunk!!.run {
            if (count == capacity) {
                currentChunk = next ?: Chunk(size, parameterCount).also { next = it }
            }
        }
        currentChunk!!.run {
            row.copyToPacked(tags, values, objects, count * parameterCount, parameterCount)
            ++count
        }
        ++size
    }

    fun clear() {
        var chunk = firstChunk
        var lastRetainedChunk: Chunk? = null
        var retainedCapacity = 0
        var retaining = true
        while (chunk != null) {
            chunk.apply {
                val usedSlots = count * parameterCount
                tags.fill(0, 0, usedSlots)
                values.fill(0L, 0, usedSlots)
                objects.fill(null, 0, usedSlots)
                count = 0
            }
            if (retaining && chunk.capacity <= maximumRetainedCapacity - retainedCapacity) {
                retainedCapacity += chunk.capacity
                lastRetainedChunk = chunk
            } else {
                retaining = false
            }
            chunk = chunk.next
        }
        lastRetainedChunk?.next = null
        currentChunk = firstChunk
        size = 0
    }

    internal fun forEachPackedRow(
        action: (tags: ByteArray, values: LongArray, objects: Array<Any?>, offset: Int) -> Boolean
    ): Boolean {
        var chunk = firstChunk
        while (chunk != null) {
            for (index in 0 until chunk.count) {
                if (!action(chunk.tags, chunk.values, chunk.objects, index * parameterCount)) {
                    return false
                }
            }
            chunk = chunk.next
        }
        return true
    }

    override fun iterator() = object : Iterator<ParameterRow> {
        private var chunk: Chunk? = firstChunk
        private var index = 0

        override fun hasNext() = chunk != null && index < chunk!!.count

        override fun next(): ParameterRow {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            val current = chunk ?: throw NoSuchElementException()
            val row = ParameterRow(parameterCount).apply {
                copyFromPacked(current.tags, current.values, current.objects, index * parameterCount)
            }
            if (++index >= current.count) {
                chunk = current.next
                index = 0
            }
            return row
        }
    }
}
