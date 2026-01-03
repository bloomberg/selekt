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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class SQLBindStrategyTest {
    private val mockStatement = mock<SQLPreparedStatement>()
    private val position = 1

    @Test
    fun universalBindString() {
        val value = "test"
        SQLBindStrategy.Universal.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun universalBindInt() {
        val value = 42
        SQLBindStrategy.Universal.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun universalBindLong() {
        val value = 42L
        SQLBindStrategy.Universal.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun universalBindDouble() {
        val value = 3.14
        SQLBindStrategy.Universal.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun universalBindFloat() {
        val value = 3.14f
        SQLBindStrategy.Universal.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value.toDouble()))
    }

    @Test
    fun universalBindShort() {
        val value: Short = 42
        SQLBindStrategy.Universal.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value.toInt()))
    }

    @Test
    fun universalBindByte() {
        val value: Byte = 42
        SQLBindStrategy.Universal.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value.toInt()))
    }

    @Test
    fun universalBindByteArray() {
        val value = byteArrayOf(1, 2, 3)
        SQLBindStrategy.Universal.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun universalBindNull() {
        SQLBindStrategy.Universal.bind(mockStatement, position, null)
        verify(mockStatement, times(1)).bindNull(eq(position))
    }

    @Test
    fun universalBindZeroBlob() {
        val value = ZeroBlob(100)
        SQLBindStrategy.Universal.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bindZeroBlob(eq(position), eq(100))
    }

    @Test
    fun universalBindUnsupportedType() {
        val value = Any()
        val exception = assertFailsWith<IllegalArgumentException> {
            SQLBindStrategy.Universal.bind(mockStatement, position, value)
        }
        assertEquals("Cannot bind arg of class ${value.javaClass} at position $position.", exception.message)
    }

    @Test
    fun stringValueBind() {
        val value = "test"
        SQLBindStrategy.StringValue.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun intValueBind() {
        val value = 42
        SQLBindStrategy.IntValue.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun longValueBind() {
        val value = 42L
        SQLBindStrategy.LongValue.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun doubleValueBind() {
        val value = 3.14
        SQLBindStrategy.DoubleValue.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun floatValueBind() {
        val value = 3.14f
        SQLBindStrategy.FloatValue.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value.toDouble()))
    }

    @Test
    fun shortValueBind() {
        val value: Short = 42
        SQLBindStrategy.ShortValue.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value.toInt()))
    }

    @Test
    fun byteValueBind() {
        val value: Byte = 42
        SQLBindStrategy.ByteValue.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value.toInt()))
    }

    @Test
    fun blobValueBind() {
        val value = byteArrayOf(1, 2, 3)
        SQLBindStrategy.BlobValue.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bind(eq(position), eq(value))
    }

    @Test
    fun nullValueBind() {
        SQLBindStrategy.NullValue.bind(mockStatement, position, null)
        verify(mockStatement, times(1)).bindNull(eq(position))
    }

    @Test
    fun nullValueBindIgnoresActualValue() {
        SQLBindStrategy.NullValue.bind(mockStatement, position, "ignored")
        verify(mockStatement, times(1)).bindNull(eq(position))
    }

    @Test
    fun zeroBlobValueBind() {
        val value = ZeroBlob(100)
        SQLBindStrategy.ZeroBlobValue.bind(mockStatement, position, value)
        verify(mockStatement, times(1)).bindZeroBlob(eq(position), eq(100))
    }

    @Test
    fun stringValueBindWithWrongType() {
        assertFailsWith<ClassCastException> {
            SQLBindStrategy.StringValue.bind(mockStatement, position, 42)
        }
    }

    @Test
    fun intValueBindWithWrongType() {
        assertFailsWith<ClassCastException> {
            SQLBindStrategy.IntValue.bind(mockStatement, position, "not an int")
        }
    }

    @Test
    fun longValueBindWithWrongType() {
        assertFailsWith<ClassCastException> {
            SQLBindStrategy.LongValue.bind(mockStatement, position, "not a long")
        }
    }

    @Test
    fun doubleValueBindWithWrongType() {
        assertFailsWith<ClassCastException> {
            SQLBindStrategy.DoubleValue.bind(mockStatement, position, "not a double")
        }
    }

    @Test
    fun floatValueBindWithWrongType() {
        assertFailsWith<ClassCastException> {
            SQLBindStrategy.FloatValue.bind(mockStatement, position, "not a float")
        }
    }

    @Test
    fun shortValueBindWithWrongType() {
        assertFailsWith<ClassCastException> {
            SQLBindStrategy.ShortValue.bind(mockStatement, position, "not a short")
        }
    }

    @Test
    fun byteValueBindWithWrongType() {
        assertFailsWith<ClassCastException> {
            SQLBindStrategy.ByteValue.bind(mockStatement, position, "not a byte")
        }
    }

    @Test
    fun blobValueBindWithWrongType() {
        assertFailsWith<ClassCastException> {
            SQLBindStrategy.BlobValue.bind(mockStatement, position, "not a byte array")
        }
    }

    @Test
    fun zeroBlobValueBindWithWrongType() {
        assertFailsWith<ClassCastException> {
            SQLBindStrategy.ZeroBlobValue.bind(mockStatement, position, "not a zeroblob")
        }
    }
}
