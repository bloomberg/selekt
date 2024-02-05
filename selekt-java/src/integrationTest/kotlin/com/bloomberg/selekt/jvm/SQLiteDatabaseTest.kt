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

package com.bloomberg.selekt.jvm

import com.bloomberg.selekt.ConflictAlgorithm
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SimpleSQLQuery
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SQLiteDatabaseTest {
    @Test
    fun inMemoryDatabase(): Unit = createInMemoryDatabase().use {
        it.run {
            exec("CREATE TABLE 'Foo' (bar INT)")
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            query(SimpleSQLQuery("SELECT * FROM Foo")).use { cursor ->
                assertEquals(1, cursor.count)
            }
        }
    }
}
