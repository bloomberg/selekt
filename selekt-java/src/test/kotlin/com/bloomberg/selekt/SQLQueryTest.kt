/*
 * Copyright 2020 Bloomberg Finance L.P.
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
import kotlin.test.assertFailsWith

private const val SQL = "SELECT * FROM Foo WHERE bar=?"

internal class SQLQueryTest {
    @Test
    fun namedBindingsResolveEverySupportedType() {
        val args = arrayOfNulls<Any>(6)
        val query = SQLQuery(
            mock(),
            "SELECT :blob, :double, :int, :long, :null, :string",
            SQLStatementType.SELECT,
            args
        )
        val blob = byteArrayOf(1)

        query.bindBlob(":blob", blob)
        query.bindDouble(":double", 2.0)
        query.bindInt(":int", 3)
        query.bindLong(":long", 4L)
        query.bindNull(":null")
        query.bindString(":string", "six")

        assertSame(blob, args[0])
        assertEquals(listOf(2.0, 3, 4L, null, "six"), args.drop(1))
    }

    @Test
    fun unknownNamedBindingFailsWithAvailableNames() {
        val query = SQLQuery(mock(), "SELECT :known", SQLStatementType.SELECT, arrayOfNulls(1))

        assertFailsWith<IllegalArgumentException> { query.bindInt(":unknown", 1) }
    }

    @Test
    fun parameterRowFactoryMaterializesValues() {
        val row = ParameterRow(1).apply { setLong(0, 42L) }
        SQLQuery.create(mock(), SQL, SQLStatementType.SELECT, row).clearBindings()
    }

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
