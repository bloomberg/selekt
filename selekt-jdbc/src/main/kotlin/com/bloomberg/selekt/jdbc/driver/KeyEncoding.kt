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

import java.nio.CharBuffer

/**
 * @since 0.34.1
 */
internal object KeyEncoding {
    const val REQUIRED_KEY_LENGTH_BYTES = 32

    private const val HEX_PREFIX_LENGTH = 2
    private const val HEX_CHUNK_SIZE = 2
    private const val HEX_RADIX = 16
    private const val BITS_PER_HEX_DIGIT = 4

    fun encode(keyChars: CharArray): ByteArray {
        val bytes = if (keyChars.isHexPrefixed()) {
            parseHexKey(keyChars)
        } else {
            Charsets.UTF_8.encode(CharBuffer.wrap(keyChars)).let {
                ByteArray(it.remaining()).also(it::get)
            }
        }
        require(bytes.size == REQUIRED_KEY_LENGTH_BYTES) {
            "Encryption key must be exactly $REQUIRED_KEY_LENGTH_BYTES bytes, was ${bytes.size} bytes"
        }
        return bytes
    }

    fun validateLength(keyChars: CharArray) {
        val encodedLength = if (keyChars.isHexPrefixed()) {
            val hexLength = keyChars.size - HEX_PREFIX_LENGTH
            require(hexLength > 0 && hexLength % HEX_CHUNK_SIZE == 0) {
                "Hex key must have an even number of hex digits after the '0x' prefix"
            }
            hexLength / HEX_CHUNK_SIZE
        } else {
            Charsets.UTF_8.encode(CharBuffer.wrap(keyChars)).remaining()
        }
        require(encodedLength == REQUIRED_KEY_LENGTH_BYTES) {
            "Encryption key must be exactly $REQUIRED_KEY_LENGTH_BYTES bytes, was $encodedLength bytes"
        }
    }

    private fun CharArray.isHexPrefixed(): Boolean = size >= HEX_PREFIX_LENGTH
        && this[0] == '0'
        && this[1].let { it == 'x' || it == 'X' }

    private fun parseHexKey(keyChars: CharArray): ByteArray {
        val hexLength = keyChars.size - HEX_PREFIX_LENGTH
        require(hexLength > 0 && hexLength % HEX_CHUNK_SIZE == 0) {
            "Hex key must have an even number of hex digits after the '0x' prefix"
        }
        val byteArray = ByteArray(hexLength / HEX_CHUNK_SIZE)
        var i = HEX_PREFIX_LENGTH
        var j = 0
        while (i < keyChars.size - 1) {
            val high = Character.digit(keyChars[i], HEX_RADIX)
            val low = Character.digit(keyChars[i + 1], HEX_RADIX)
            require(high != -1 && low != -1) {
                "Invalid hex character in encryption key"
            }
            byteArray[j++] = (high shl BITS_PER_HEX_DIGIT or low).toByte()
            i += HEX_CHUNK_SIZE
        }
        return byteArray
    }
}
