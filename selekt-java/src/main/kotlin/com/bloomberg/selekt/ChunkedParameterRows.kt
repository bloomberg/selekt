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

@NotThreadSafe
class ChunkedParameterRows(
    private val parameterCount: Int,
    private val initialChunkCapacity: Int = DEFAULT_INITIAL_CHUNK_CAPACITY
) : Iterable<ParameterRow> {
    @Suppress("Detekt.UseDataClass")
    private class Chunk(val capacity: Int, parameterCount: Int) {
        val data = Array(capacity) { ParameterRow(parameterCount) }
        var count = 0
        var next: Chunk? = null
    }

    private var firstChunk: Chunk? = null
    private var currentChunk: Chunk? = null

    var size = 0
        private set

    fun add(row: ParameterRow) {
        if (firstChunk == null) {
            firstChunk = Chunk(initialChunkCapacity, parameterCount)
            currentChunk = firstChunk
        }
        currentChunk!!.run {
            if (count == capacity) {
                currentChunk = Chunk(size, parameterCount).also {
                    next = it
                }
            }
        }
        currentChunk!!.run {
            data[count].copyFrom(row)
            ++count
        }
        ++size
    }

    fun clear() {
        var chunk = firstChunk
        while (chunk != null) {
            val rows = chunk.data
            for (i in 0 until chunk.count) {
                rows[i].clear()
            }
            chunk.count = 0
            chunk = chunk.next
        }
        firstChunk = currentChunk
        size = 0
    }

    override fun iterator() = object : Iterator<ParameterRow> {
        private var chunk: Chunk? = firstChunk
        private var index = 0

        override fun hasNext() = chunk != null && index < chunk!!.count

        override fun next(): ParameterRow {
            val current = chunk ?: throw NoSuchElementException()
            val row = current.data[index]
            if (++index >= current.count) {
                chunk = current.next
                index = 0
            }
            return row
        }
    }
}
