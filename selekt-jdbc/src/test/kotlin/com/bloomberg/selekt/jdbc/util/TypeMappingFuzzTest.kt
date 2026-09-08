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
import java.math.BigDecimal
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types
import java.util.stream.Stream
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.junit.jupiter.params.provider.MethodSource

internal class TypeMappingFuzzTest {
    @MethodSource("inputs")
    @FuzzTest
    fun fuzzTypeMapping(input: ByteArray) {
        if (input.size > MAX_INPUT_SIZE) return

        val value = input.toString(Charsets.UTF_8)
        TARGET_TYPES.forEach { jdbcType ->
            val converted = TypeMapping.convertFromSQLite(value, jdbcType)
            assertExpectedType(jdbcType, converted)
            TypeMapping.convertToSQLite(converted)
        }
        assertTrue(TypeMapping.convertToSQLite(value) === value)
    }

    private fun assertExpectedType(jdbcType: Int, value: Any?) {
        when (jdbcType) {
            Types.BOOLEAN -> assertIs<Boolean>(value)
            Types.TINYINT -> assertIs<Byte>(value)
            Types.SMALLINT -> assertIs<Short>(value)
            Types.INTEGER -> assertIs<Int>(value)
            Types.BIGINT -> assertIs<Long>(value)
            Types.FLOAT -> assertIs<Float>(value)
            Types.DOUBLE -> assertIs<Double>(value)
            Types.NUMERIC -> assertIs<BigDecimal>(value)
            Types.VARCHAR -> assertIs<String>(value)
            Types.DATE -> assertTrue(value == null || value is Date)
            Types.TIME -> assertTrue(value == null || value is Time)
            Types.TIMESTAMP -> assertTrue(value == null || value is Timestamp)
            Types.VARBINARY -> assertIs<ByteArray>(value)
        }
    }

    companion object {
        private const val MAX_INPUT_SIZE = 4_096
        private val TARGET_TYPES = intArrayOf(
            Types.BOOLEAN,
            Types.TINYINT,
            Types.SMALLINT,
            Types.INTEGER,
            Types.BIGINT,
            Types.FLOAT,
            Types.DOUBLE,
            Types.NUMERIC,
            Types.VARCHAR,
            Types.DATE,
            Types.TIME,
            Types.TIMESTAMP,
            Types.VARBINARY
        )

        @JvmStatic
        fun inputs(): Stream<ByteArray> = Stream.of(
            byteArrayOf(),
            "0".toByteArray(),
            "9223372036854775808".toByteArray(),
            "1e1000000".toByteArray(),
            "2026-09-08T23:59:59.999999999".toByteArray(),
            byteArrayOf(0xC0.toByte(), 0x80.toByte())
        )
    }
}
