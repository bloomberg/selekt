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
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ChunkedParameterRowsTest {
    @Test
    fun clearZerosParametersAcrossAllChunksInTheChain() {
        val rows = ChunkedParameterRows(parameterCount = 4, initialChunkCapacity = 4)
        val scratch = ParameterRow(4)
        repeat(10) {
            scratch.setInt(0, Int.MAX_VALUE)
            scratch.setLong(1, Long.MAX_VALUE)
            scratch.setDouble(2, Double.MAX_VALUE)
            scratch.setObject(3, "sensitive-batch-value")
            rows.add(scratch)
        }

        val firstBefore = assertFieldNotNull<Any>(rows, "firstChunk")
        val currentBefore = assertFieldNotNull<Any>(rows, "currentChunk")
        assertTrue(firstBefore !== currentBefore, "sanity: rows should have spilled into more than one chunk")
        val chainBefore = collectChunkChain(firstBefore)
        assertTrue(chainBefore.size >= 2, "sanity: chain should have at least two chunks")

        rows.clear()

        chainBefore.forEach(::assertChunkFullyCleared)
        val retained = collectChunkChain(assertFieldNotNull(rows, "firstChunk"))
        assertSameElements(chainBefore, retained)
        assertSame(
            retained.first(),
            readField<Any>(rows, "currentChunk"),
            "currentChunk should return to the start of the retained chain"
        )
    }

    @Test
    fun clearedChunksAreReused() {
        val rows = ChunkedParameterRows(parameterCount = 1, initialChunkCapacity = 2)
        val scratch = ParameterRow(1)
        repeat(31) {
            scratch.setInt(0, it)
            rows.add(scratch)
        }
        val chunks = collectChunkChain(assertFieldNotNull(rows, "firstChunk"))

        rows.clear()
        repeat(31) {
            scratch.setInt(0, it)
            rows.add(scratch)
        }

        assertSameElements(chunks, collectChunkChain(assertFieldNotNull(rows, "firstChunk")))
        assertEquals((0 until 31).toList(), rows.map { it.ints[0] })
    }

    @Test
    fun clearDropsChunksBeyondRetainedCapacity() {
        val rows = ChunkedParameterRows(parameterCount = 1, initialChunkCapacity = 2)
        val scratch = ParameterRow(1)
        repeat(33) {
            scratch.setInt(0, it)
            rows.add(scratch)
        }
        val chunksBefore = collectChunkChain(assertFieldNotNull(rows, "firstChunk"))
        assertEquals(listOf(2, 2, 4, 8, 16, 32), chunksBefore.map(::chunkCapacity))

        rows.clear()

        val retained = collectChunkChain(assertFieldNotNull(rows, "firstChunk"))
        assertSameElements(chunksBefore.take(5), retained)
        assertEquals(32, retained.sumOf(::chunkCapacity))
        chunksBefore.forEach(::assertChunkFullyCleared)
    }

    @Test
    fun iteratorReturnsOnlyStoredRowsInOrder() {
        val rows = ChunkedParameterRows(parameterCount = 1, initialChunkCapacity = 2)
        val scratch = ParameterRow(1)
        repeat(5) {
            scratch.setInt(0, it)
            rows.add(scratch)
        }

        assertEquals(listOf(0, 1, 2, 3, 4), rows.map { it.ints[0] })
        assertEquals(5, rows.size)
    }

    @Test
    fun oversizedRowsDoNotWriteBeyondTheirPackedRow() {
        val rows = ChunkedParameterRows(parameterCount = 1, initialChunkCapacity = 2)
        rows.add(ParameterRow(2).apply {
            setInt(0, 1)
            setObject(1, "must not leak")
        })
        rows.add(ParameterRow(1).apply { setInt(0, 2) })

        assertEquals(listOf(1, 2), rows.map { it.ints[0] })
    }

    private fun collectChunkChain(head: Any): List<Any> = buildList {
        var node: Any? = head
        while (node != null) {
            add(node)
            node = readField<Any>(node, "next")
        }
    }

    private fun chunkCapacity(chunk: Any) = checkNotNull(readField<Int>(chunk, "capacity"))

    private fun assertSameElements(expected: List<Any>, actual: List<Any>) {
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (expectedChunk, actualChunk) ->
            assertSame(expectedChunk, actualChunk)
        }
    }

    private fun assertChunkFullyCleared(chunk: Any) {
        assertEquals(0, readField<Int>(chunk, "count"), "chunk count should be reset to 0")
        readField<ByteArray>(chunk, "tags")!!.forEach {
            assertEquals(0.toByte(), it, "no packed tag slot should retain a type tag")
        }
        readField<LongArray>(chunk, "values")!!.forEach {
            assertEquals(0L, it, "no packed value slot should retain a value")
        }
        readField<Array<Any?>>(chunk, "objects")!!.forEach {
            assertNull(it, "no packed object slot should retain a reference")
        }
    }

    private fun <T> assertFieldNotNull(target: Any, name: String): T = checkNotNull(readField(target, name))

    @Suppress("UNCHECKED_CAST")
    private fun <T> readField(target: Any, name: String): T? = target.javaClass
        .getDeclaredField(name)
        .apply { isAccessible = true }
        .get(target) as T?
}
