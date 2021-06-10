/*
 * Copyright 2021 Bloomberg Finance L.P.
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

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class SimpleSQLQueryTest {
    @Test
    fun bindAny() {
        assertThrows<IllegalArgumentException> {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(Any())).bindTo(mock())
        }
    }

    @Test
    fun bindByte() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42.toByte())).bindTo(it)
            verify(it, times(1)).bindInt(eq(1), eq(42))
        }
    }

    @Test
    fun bindByteArray() {
        mock<ISQLProgram>().let {
            val blob = byteArrayOf(42.toByte())
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(blob)).bindTo(it)
            verify(it, times(1)).bindBlob(eq(1), same(blob))
        }
    }

    @Test
    fun bindBooleanTrue() {
        assertThrows<IllegalArgumentException> {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(true)).bindTo(mock())
        }
    }

    @Test
    fun bindBooleanFalse() {
        assertThrows<IllegalArgumentException> {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(false)).bindTo(mock())
        }
    }

    @Test
    fun bindChar() {
        assertThrows<IllegalArgumentException> {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf('a')).bindTo(mock())
        }
    }

    @Test
    fun bindDouble() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42.0)).bindTo(it)
            verify(it, times(1)).bindDouble(eq(1), eq(42.0))
        }
    }

    @Test
    fun bindFloat() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42.0f)).bindTo(it)
            verify(it, times(1)).bindDouble(eq(1), eq(42.0))
        }
    }

    @Test
    fun bindInt() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42)).bindTo(it)
            verify(it, times(1)).bindInt(eq(1), eq(42))
        }
    }

    @Test
    fun bindLong() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42L)).bindTo(it)
            verify(it, times(1)).bindLong(eq(1), eq(42L))
        }
    }

    @Test
    fun bindNull() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOfNulls(1)).bindTo(it)
            verify(it, times(1)).bindNull(eq(1))
        }
    }

    @Test
    fun bindNumber() {
        mock<ISQLProgram>().let {
            assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
                SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(mock<Number>())).bindTo(it)
            }
        }
    }

    @Test
    fun bindShort() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42.toShort())).bindTo(it)
            verify(it, times(1)).bindInt(eq(1), eq(42))
        }
    }

    @Test
    fun bindString() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf("a")).bindTo(it)
            verify(it, times(1)).bindString(eq(1), eq("a"))
        }
    }
}
