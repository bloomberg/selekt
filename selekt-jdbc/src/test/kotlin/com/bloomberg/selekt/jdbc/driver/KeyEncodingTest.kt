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

import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

internal class KeyEncodingTest {
    @Test
    fun `encode plain 32-byte ASCII key returns those bytes`() {
        val key = "exactly-32-bytes-of-key-data!!!!"
        assertEquals(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES, key.length)
        val bytes = KeyEncoding.encode(key.toCharArray())
        assertContentEquals(key.toByteArray(Charsets.UTF_8), bytes)
        assertEquals(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES, bytes.size)
    }

    @Test
    fun `encode hex-prefixed key returns raw decoded bytes (not the literal chars)`() {
        val hex = "AB".repeat(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES)
        val keyChars = "0x$hex".toCharArray()
        val bytes = KeyEncoding.encode(keyChars)
        assertEquals(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES, bytes.size)
        val expected = ByteArray(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES) { 0xAB.toByte() }
        assertContentEquals(expected, bytes)
    }

    @Test
    fun `encode hex-prefixed key (uppercase 0X) decodes the same as lowercase 0x`() {
        val hex = "ab".repeat(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES)
        val lower = KeyEncoding.encode("0x$hex".toCharArray())
        val upper = KeyEncoding.encode("0X$hex".toCharArray())
        assertContentEquals(lower, upper)
    }

    @Test
    fun `encode rejects key not exactly 32 bytes when not hex-prefixed`() {
        assertFailsWith<IllegalArgumentException> { KeyEncoding.encode("short".toCharArray()) }
        assertFailsWith<IllegalArgumentException> { KeyEncoding.encode("x".repeat(33).toCharArray()) }
    }

    @Test
    fun `encode rejects hex key with wrong number of digits`() {
        assertFailsWith<IllegalArgumentException> {
            KeyEncoding.encode(("0x" + "AB".repeat(31)).toCharArray())
        }
        assertFailsWith<IllegalArgumentException> {
            KeyEncoding.encode("0xABC".toCharArray())
        }
    }

    @Test
    fun `encode rejects hex key containing non-hex chars`() {
        val bad = ("0x" + "AB".repeat(31) + "ZZ").toCharArray()
        assertFailsWith<IllegalArgumentException> { KeyEncoding.encode(bad) }
    }

    @Test
    fun `driver and data-source paths both produce the same bytes for the same hex key`() {
        val hex = "0123456789abcdef".repeat(4)
        val keyString = "0x$hex"
        val viaDriverPath = KeyEncoding.encode(keyString.toCharArray())
        val viaDataSourcePath = KeyEncoding.encode(keyString.toCharArray())
        assertContentEquals(viaDriverPath, viaDataSourcePath)
        assertEquals(KeyEncoding.REQUIRED_KEY_LENGTH_BYTES, viaDriverPath.size)
        assertEquals(0x01.toByte(), viaDriverPath[0])
        assertEquals(0x23.toByte(), viaDriverPath[1])
    }

    @Test
    fun `validateLength accepts both hex-prefixed and plain 32-byte keys`() {
        KeyEncoding.validateLength("exactly-32-bytes-of-key-data!!!!".toCharArray())
        KeyEncoding.validateLength(("0x" + "AB".repeat(32)).toCharArray())
    }

    @Test
    fun `validateLength rejects wrong sizes consistently with encode`() {
        assertFailsWith<IllegalArgumentException> { KeyEncoding.validateLength("short".toCharArray()) }
        assertFailsWith<IllegalArgumentException> {
            KeyEncoding.validateLength(("0x" + "AB".repeat(31)).toCharArray())
        }
    }
}
