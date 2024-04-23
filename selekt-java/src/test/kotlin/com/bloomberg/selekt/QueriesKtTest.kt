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
import kotlin.test.assertEquals

internal class QueriesKtTest {
    @Test
    fun appendWhere() {
        assertEquals("SELECT * FROM Foo WHERE bar=?", StringBuilder("SELECT * FROM Foo").where("bar=?").toString())
    }

    @Test
    fun appendGroupBy() {
        assertEquals("SELECT * FROM Foo GROUP BY bar", StringBuilder("SELECT * FROM Foo").groupBy("bar").toString())
    }

    @Test
    fun appendGroupByNull() {
        assertEquals("SELECT * FROM Foo", StringBuilder("SELECT * FROM Foo").groupBy(null).toString())
    }

    @Test
    fun appendGroupByHaving() {
        assertEquals(
            "SELECT * FROM Foo GROUP BY bar HAVING COUNT(id) > 42",
            StringBuilder("SELECT * FROM Foo").groupBy("bar").having("COUNT(id) > 42").toString()
        )
    }

    @Test
    fun appendGroupByHavingNull() {
        assertEquals(
            "SELECT * FROM Foo GROUP BY bar",
            StringBuilder("SELECT * FROM Foo").groupBy("bar").having(null).toString()
        )
    }

    @Test
    fun appendOrderBy() {
        assertEquals(
            "SELECT * FROM Foo ORDER BY bar ASC",
            StringBuilder("SELECT * FROM Foo").orderBy("bar ASC").toString()
        )
    }

    @Test
    fun appendOrderByNull() {
        assertEquals("SELECT * FROM Foo", StringBuilder("SELECT * FROM Foo").orderBy(null).toString())
    }

    @Test
    fun appendLimit() {
        assertEquals("SELECT * FROM Foo LIMIT 42", StringBuilder("SELECT * FROM Foo").limit(42).toString())
    }

    @Test
    fun appendLimitNull() {
        assertEquals("SELECT * FROM Foo", StringBuilder("SELECT * FROM Foo").limit(null).toString())
    }
}
