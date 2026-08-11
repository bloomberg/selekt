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
    fun clearZerosParameterReferencesAcrossAllChunksInTheChain() {
        val rows = ChunkedParameterRows(parameterCount = 1, initialChunkCapacity = 4)
        val scratch = ParameterRow(1)
        repeat(10) {
            scratch.setObject(0, "sensitive-batch-value")
            rows.add(scratch)
        }

        val firstBefore = assertFieldNotNull<Any>(rows, "firstChunk")
        val currentBefore = assertFieldNotNull<Any>(rows, "currentChunk")
        assertTrue(firstBefore !== currentBefore, "sanity: rows should have spilled into more than one chunk")
        val chainBefore = collectChunkChain(firstBefore)
        assertTrue(chainBefore.size >= 2, "sanity: chain should have at least two chunks")

        rows.clear()

        chainBefore.forEach(::assertChunkFullyCleared)
        val retained = assertFieldNotNull<Any>(rows, "firstChunk")
        assertEquals(
            chainBefore.maxOf { readField<Int>(it, "capacity")!! },
            readField<Int>(retained, "capacity")!!,
            "clear should retain the largest chunk for reuse"
        )
        assertSame(
            retained,
            readField<Any>(rows, "currentChunk"),
            "firstChunk and currentChunk should both point at the retained chunk"
        )
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

    private fun collectChunkChain(head: Any): List<Any> = buildList {
        var node: Any? = head
        while (node != null) {
            add(node)
            node = readField<Any>(node, "next")
        }
    }

    private fun assertChunkFullyCleared(chunk: Any) {
        assertEquals(0, readField<Int>(chunk, "count"), "chunk count should be reset to 0")
        @Suppress("UNCHECKED_CAST")
        val storedRows = readField<Array<Any?>>(chunk, "data")!!
        for (row in storedRows) {
            val parameterRow = row as? ParameterRow ?: continue
            parameterRow.objects.forEach {
                assertNull(it, "no ParameterRow.objects slot should retain a reference")
            }
        }
    }

    private fun <T> assertFieldNotNull(target: Any, name: String): T = checkNotNull(readField(target, name))

    @Suppress("UNCHECKED_CAST")
    private fun <T> readField(target: Any, name: String): T? = target.javaClass
        .getDeclaredField(name)
        .apply { isAccessible = true }
        .get(target) as T?
}
