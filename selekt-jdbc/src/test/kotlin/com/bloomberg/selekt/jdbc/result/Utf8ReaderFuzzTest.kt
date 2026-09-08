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

package com.bloomberg.selekt.jdbc.result

import com.code_intelligence.jazzer.junit.FuzzTest
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.params.provider.MethodSource

@Suppress("MagicNumber")
internal class Utf8ReaderFuzzTest {
    @MethodSource("inputs")
    @FuzzTest
    fun fuzzReader(input: ByteArray) {
        if (input.size > MAX_INPUT_SIZE) return

        val scalar = input.readScalar()
        (1..MAX_CHUNK_SIZE).forEach { chunkSize ->
            assertEquals(scalar, input.readInChunks(chunkSize))
        }
        assertWellFormedUtf16(scalar)
    }

    private fun ByteArray.readScalar(): String = buildString {
        utf8Reader(this@readScalar).use { reader ->
            while (true) {
                val value = reader.read()
                if (value == -1) break
                append(value.toChar())
            }
        }
    }

    private fun ByteArray.readInChunks(chunkSize: Int): String = buildString {
        utf8Reader(this@readInChunks).use { reader ->
            val buffer = CharArray(chunkSize + BUFFER_PADDING)
            assertEquals(0, reader.read(buffer, BUFFER_OFFSET, 0))
            while (true) {
                val read = reader.read(buffer, BUFFER_OFFSET, chunkSize)
                if (read == -1) break
                append(buffer, BUFFER_OFFSET, read)
            }
        }
    }

    private fun assertWellFormedUtf16(value: String) {
        var index = 0
        while (index < value.length) {
            val character = value[index]
            when {
                character.isHighSurrogate() -> {
                    assertTrue(index + 1 < value.length)
                    assertTrue(value[index + 1].isLowSurrogate())
                    index += 2
                }
                character.isLowSurrogate() -> error("Unpaired low surrogate at index $index")
                else -> index += 1
            }
        }
    }

    companion object {
        private const val MAX_INPUT_SIZE = 4_096
        private const val MAX_CHUNK_SIZE = 8
        private const val BUFFER_OFFSET = 2
        private const val BUFFER_PADDING = 4

        @JvmStatic
        fun inputs(): Stream<ByteArray> = Stream.of(
            byteArrayOf(),
            "ASCII".toByteArray(),
            "£€😀".toByteArray(),
            byteArrayOf(0xC0.toByte(), 0x80.toByte(), 0xE2.toByte(), 0x82.toByte())
        )
    }
}
