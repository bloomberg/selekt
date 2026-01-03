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
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

internal class SQLBindStrategyResolverTest {
    @Test
    fun resolveString() {
        assertSame(SQLBindStrategy.StringValue, SQLBindStrategyResolver.resolveAll(arrayOf("test"))[0])
    }

    @Test
    fun resolveInt() {
        assertSame(SQLBindStrategy.IntValue, SQLBindStrategyResolver.resolveAll(arrayOf(42))[0])
    }

    @Test
    fun resolveLong() {
        assertSame(SQLBindStrategy.LongValue, SQLBindStrategyResolver.resolveAll(arrayOf(42L))[0])
    }

    @Test
    fun resolveDouble() {
        assertSame(SQLBindStrategy.DoubleValue, SQLBindStrategyResolver.resolveAll(arrayOf(3.14))[0])
    }

    @Test
    fun resolveFloat() {
        assertSame(SQLBindStrategy.FloatValue, SQLBindStrategyResolver.resolveAll(arrayOf(3.14f))[0])
    }

    @Test
    fun resolveShort() {
        assertSame(SQLBindStrategy.ShortValue, SQLBindStrategyResolver.resolveAll(arrayOf(42.toShort()))[0])
    }

    @Test
    fun resolveByte() {
        assertSame(SQLBindStrategy.ByteValue, SQLBindStrategyResolver.resolveAll(arrayOf(42.toByte()))[0])
    }

    @Test
    fun resolveByteArray() {
        assertSame(SQLBindStrategy.BlobValue, SQLBindStrategyResolver.resolveAll(arrayOf(byteArrayOf(1, 2, 3)))[0])
    }

    @Test
    fun resolveNull() {
        assertSame(SQLBindStrategy.NullValue, SQLBindStrategyResolver.resolveAll(arrayOf(null))[0])
    }

    @Test
    fun resolveZeroBlob() {
        assertSame(SQLBindStrategy.ZeroBlobValue, SQLBindStrategyResolver.resolveAll(arrayOf(ZeroBlob(100)))[0])
    }

    @Test
    fun resolveUnsupportedType() {
        val exception = assertFailsWith<IllegalArgumentException> {
            SQLBindStrategyResolver.resolveAll(arrayOf(Any()))
        }
        assertEquals("Cannot bind arg of ${Any::class.java}.", exception.message)
    }

    @Test
    fun resolveAllMultipleValues() {
        SQLBindStrategyResolver.resolveAll(arrayOf(
            "string",
            42,
            42L,
            3.14,
            3.14f,
            42.toShort(),
            42.toByte(),
            byteArrayOf(1, 2, 3),
            null,
            ZeroBlob(50)
        )).let {
            assertEquals(10, it.size)
            assertSame(SQLBindStrategy.StringValue, it[0])
            assertSame(SQLBindStrategy.IntValue, it[1])
            assertSame(SQLBindStrategy.LongValue, it[2])
            assertSame(SQLBindStrategy.DoubleValue, it[3])
            assertSame(SQLBindStrategy.FloatValue, it[4])
            assertSame(SQLBindStrategy.ShortValue, it[5])
            assertSame(SQLBindStrategy.ByteValue, it[6])
            assertSame(SQLBindStrategy.BlobValue, it[7])
            assertSame(SQLBindStrategy.NullValue, it[8])
            assertSame(SQLBindStrategy.ZeroBlobValue, it[9])
        }
    }

    @Test
    fun resolveAllEmptyArray() {
        val strategies = SQLBindStrategyResolver.resolveAll(emptyArray())
        assertEquals(0, strategies.size)
    }

    @Test
    fun resolveAllSingleValue() {
        SQLBindStrategyResolver.resolveAll(arrayOf("test")).let {
            assertEquals(1, it.size)
            assertSame(SQLBindStrategy.StringValue, it[0])
        }
    }

    @Test
    fun resolveAllDuplicateTypes() {
        SQLBindStrategyResolver.resolveAll(arrayOf("first", "second", "third")).let {
            assertEquals(3, it.size)
            assertSame(SQLBindStrategy.StringValue, it[0])
            assertSame(SQLBindStrategy.StringValue, it[1])
            assertSame(SQLBindStrategy.StringValue, it[2])
            assertSame(it[0], it[1])
            assertSame(it[1], it[2])
        }
    }

    @Test
    fun resolveAllWithMixedNullValues() {
        SQLBindStrategyResolver.resolveAll(arrayOf<Any?>("test", null, 42, null, 3.14)).let {
            assertEquals(5, it.size)
            assertSame(SQLBindStrategy.StringValue, it[0])
            assertSame(SQLBindStrategy.NullValue, it[1])
            assertSame(SQLBindStrategy.IntValue, it[2])
            assertSame(SQLBindStrategy.NullValue, it[3])
            assertSame(SQLBindStrategy.DoubleValue, it[4])
        }
    }

    @Test
    fun resolveAllPreservesOrder() {
        SQLBindStrategyResolver.resolveAll(arrayOf<Any?>(3.14, 42L, "test", 42.toByte())).let {
            assertEquals(4, it.size)
            assertSame(SQLBindStrategy.DoubleValue, it[0])
            assertSame(SQLBindStrategy.LongValue, it[1])
            assertSame(SQLBindStrategy.StringValue, it[2])
            assertSame(SQLBindStrategy.ByteValue, it[3])
        }
    }

    @Test
    fun resolveAllFailsOnFirstUnsupportedType() {
        val exception = assertFailsWith<IllegalArgumentException> {
            SQLBindStrategyResolver.resolveAll(arrayOf("test", Any(), 42))
        }
        assertEquals("Cannot bind arg of ${Any::class.java}.", exception.message)
    }

    @Test
    fun resolveAllTypeOrderMatches() {
        arrayOf(
            "string" to SQLBindStrategy.StringValue,
            42 to SQLBindStrategy.IntValue,
            null to SQLBindStrategy.NullValue,
            42L to SQLBindStrategy.LongValue,
            3.14 to SQLBindStrategy.DoubleValue,
            byteArrayOf(1) to SQLBindStrategy.BlobValue,
            3.14f to SQLBindStrategy.FloatValue,
            42.toShort() to SQLBindStrategy.ShortValue,
            42.toByte() to SQLBindStrategy.ByteValue,
            ZeroBlob(10) to SQLBindStrategy.ZeroBlobValue
        ).forEach { (value, expectedStrategy) ->
            val actualStrategy = SQLBindStrategyResolver.resolveAll(arrayOf(value))[0]
            assertSame(expectedStrategy, actualStrategy, "Failed for value: $value")
        }
    }
}
