/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.jupiter.api.assertThrows

internal class SimpleSQLQueryTest {
    @Test
    fun bindByte() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42.toByte())).bindTo(it)
            verify(it, times(1)).bindLong(eq(1), eq(42L))
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
    fun bindBoolean() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(true)).bindTo(it)
            verify(it, times(1)).bindLong(eq(1), eq(1L))
        }
    }

    @Test
    fun bindChar() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42.toChar())).bindTo(it)
            verify(it, times(1)).bindLong(eq(1), eq(42L))
        }
    }

    @Test
    fun bindDouble() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42.0f)).bindTo(it)
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
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42L)).bindTo(it)
            verify(it, times(1)).bindLong(eq(1), eq(42L))
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
    fun bindShort() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(42.toShort())).bindTo(it)
            verify(it, times(1)).bindLong(eq(1), eq(42L))
        }
    }

    @Test
    fun bindString() {
        mock<ISQLProgram>().let {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf("a")).bindTo(it)
            verify(it, times(1)).bindString(eq(1), eq("a"))
        }
    }

    @Test
    fun bindAny() {
        assertThrows<IllegalArgumentException> {
            SimpleSQLQuery("SELECT * FROM Foo WHERE bar=?", arrayOf(Any())).bindTo(mock())
        }
    }
}
