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

package com.bloomberg.selekt.jdbc.driver

import com.code_intelligence.jazzer.junit.FuzzTest
import java.util.stream.Stream
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.params.provider.MethodSource

@Suppress("MagicNumber")
internal class KeyEncodingFuzzTest {
    @MethodSource("inputs")
    @FuzzTest
    fun fuzzKeyEncoding(input: ByteArray) {
        if (input.size > MAX_INPUT_SIZE) return

        val key = input.toChars()
        val original = key.copyOf()
        var encoded: ByteArray? = null
        try {
            encoded = KeyEncoding.encode(key)
            assertEquals(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES, encoded.size)
            assertTrue(encoded.any { it != 0.toByte() })
        } catch (_: IllegalArgumentException) {
            // Rejection is expected for malformed, zero, or incorrectly sized keys.
        } finally {
            assertContentEquals(original, key)
            encoded?.fill(0)
        }
    }

    private fun ByteArray.toChars(): CharArray = CharArray((size + 1) / 2) { index ->
        val byteIndex = index * 2
        val high = this[byteIndex].toInt() and 0xFF
        val low = getOrNull(byteIndex + 1)?.toInt()?.and(0xFF) ?: 0
        (high shl 8 or low).toChar()
    }

    companion object {
        private const val MAX_INPUT_SIZE = 512

        @JvmStatic
        fun inputs(): Stream<ByteArray> = Stream.of(
            byteArrayOf(),
            "exactly-32-bytes-of-key-data!!!!".toInput(),
            ("0x" + "AB".repeat(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES)).toInput(),
            "0xABC".toInput(),
            CharArray(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES).toInput()
        )

        private fun String.toInput() = toCharArray().toInput()

        private fun CharArray.toInput() = ByteArray(size * 2) { byteIndex ->
            val character = this[byteIndex / 2].code
            if (byteIndex % 2 == 0) (character ushr 8).toByte() else character.toByte()
        }
    }
}
