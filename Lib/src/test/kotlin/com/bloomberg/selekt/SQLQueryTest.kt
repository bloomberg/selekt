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

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

private const val SQL = "SELECT * FROM Foo WHERE bar=?"

internal class SQLQueryTest {
    @Test
    fun bindBlob() {
        val arg = byteArrayOf()
        val args = arrayOfNulls<Any>(1)
        SQLQuery(mock(), SQL, SQLStatementType.SELECT, args).apply {
            bindBlob(1, arg)
        }
        assertSame(arg, args[0])
    }

    @Test
    fun bindDouble() {
        val args = arrayOfNulls<Any>(1)
        SQLQuery(mock(), SQL, SQLStatementType.SELECT, args).apply {
            bindDouble(1, 42.0)
        }
        assertEquals(42.0, args[0])
    }

    @Test
    fun bindInt() {
        val args = arrayOfNulls<Any>(1)
        SQLQuery(mock(), SQL, SQLStatementType.SELECT, args).apply {
            bindInt(1, 42)
        }
        assertEquals(42, args[0])
    }

    @Test
    fun bindLong() {
        val args = arrayOfNulls<Any>(1)
        SQLQuery(mock(), SQL, SQLStatementType.SELECT, args).apply {
            bindLong(1, 42L)
        }
        assertEquals(42L, args[0])
    }

    @Test
    fun bindNull() {
        val args = Array<Any?>(1) { "" }
        SQLQuery(mock(), SQL, SQLStatementType.SELECT, args).apply {
            bindNull(1)
        }
        assertNull(args[0])
    }

    @Test
    fun bindString() {
        val arg = "abc"
        val args = arrayOfNulls<Any>(1)
        SQLQuery(mock(), SQL, SQLStatementType.SELECT, args).apply {
            bindString(1, arg)
        }
        assertSame(arg, args[0])
    }

    @Test
    fun clearBindings() {
        val args = arrayOfNulls<Any>(1)
        val query = SQLQuery(mock(), SQL, SQLStatementType.SELECT, args).apply {
            bindInt(1, 42)
        }
        assertEquals(42, args[0])
        query.clearBindings()
        assertNull(args[0])
    }

    @Test
    fun close() {
        val args = arrayOfNulls<Any>(1)
        val query = SQLQuery(mock(), SQL, SQLStatementType.SELECT, args).apply {
            bindInt(1, 42)
        }
        assertEquals(42, args[0])
        query.close()
        assertNull(args[0])
    }
}
