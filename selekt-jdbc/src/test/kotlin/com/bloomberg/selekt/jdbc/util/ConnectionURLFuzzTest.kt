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

package com.bloomberg.selekt.jdbc.util

import com.code_intelligence.jazzer.junit.FuzzTest
import java.net.URLEncoder
import java.sql.SQLException
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.params.provider.MethodSource

internal class ConnectionURLFuzzTest {
    @MethodSource("inputs")
    @FuzzTest
    fun fuzzConnectionUrl(input: ByteArray) {
        if (input.size > MAX_INPUT_SIZE) return

        val fuzzed = input.toString(Charsets.UTF_8)
        val candidate = if (input.firstOrNull()?.toInt()?.and(1) == 0) {
            fuzzed
        } else {
            "jdbc:sqlite:$fuzzed"
        }
        try {
            val parsed = ConnectionURL.parse(candidate)
            assertTrue(parsed.databasePath.isNotBlank())
            assertEquals(parsed.databasePath, ConnectionURL.parse(parsed.toString()).databasePath)
        } catch (exception: SQLException) {
            assertTrue(exception.message in SAFE_PARSE_ERRORS)
        }

        val propertyName = SENSITIVE_PROPERTY_NAMES[input.firstOrNull()?.toInt()?.and(0xFF)
            ?.rem(SENSITIVE_PROPERTY_NAMES.size) ?: 0]
        val encodedValue = URLEncoder.encode(fuzzed, Charsets.UTF_8)
        val parsed = ConnectionURL.parse("jdbc:sqlite:/fuzz.db?$propertyName=$encodedValue")
        val normalizedName = if (propertyName.equals("key", ignoreCase = true)) "key" else propertyName
        assertEquals(fuzzed, parsed.getProperty(normalizedName))
        assertEquals("jdbc:sqlite:/fuzz.db?$normalizedName=***", parsed.toString())
    }

    companion object {
        private const val MAX_INPUT_SIZE = 4_096
        private val SAFE_PARSE_ERRORS = setOf(
            "Failed to parse JDBC URL",
            "Invalid JDBC URL format. Expected format: jdbc:sqlite:path/to/database.sqlite[?properties...]"
        )
        private val SENSITIVE_PROPERTY_NAMES = listOf(
            "key",
            "Key",
            "password",
            "api_key",
            "accessToken",
            "client-secret",
            "privateKey",
            "refresh_token"
        )

        @JvmStatic
        fun inputs(): Stream<ByteArray> = Stream.of(
            byteArrayOf(),
            "/fuzz.db".toByteArray(),
            "/fuzz.db?poolSize=4&foreignKeys=true".toByteArray(),
            "/fuzz.db?bad=%2".toByteArray(),
            "super secret + % value".toByteArray()
        )
    }
}
