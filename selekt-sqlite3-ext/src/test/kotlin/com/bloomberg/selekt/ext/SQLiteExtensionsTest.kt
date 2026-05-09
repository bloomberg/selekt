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

package com.bloomberg.selekt.ext

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

internal class SQLiteExtensionsTest {
    @Test
    fun loadExtensionStatementPathOnly() {
        val sql = SQLiteExtensionSql.loadExtensionStatement(SQLiteExtension("/tmp/ext.so"))
        assertEquals("SELECT load_extension('/tmp/ext.so')", sql)
    }

    @Test
    fun loadExtensionStatementWithEntryPoint() {
        val sql = SQLiteExtensionSql.loadExtensionStatement(SQLiteExtension("/tmp/ext.so", "sqlite3_my_init"))
        assertEquals("SELECT load_extension('/tmp/ext.so', 'sqlite3_my_init')", sql)
    }

    @Test
    fun loadExtensionStatementEscapesSingleQuotes() {
        val sql = SQLiteExtensionSql.loadExtensionStatement(SQLiteExtension("/tmp/ext's.so", "entry'point"))
        assertEquals("SELECT load_extension('/tmp/ext''s.so', 'entry''point')", sql)
    }

    @Test
    fun loadExtensionStatementRejectsBlankPath() {
        assertFailsWith<IllegalArgumentException> {
            SQLiteExtensionSql.loadExtensionStatement(SQLiteExtension("   "))
        }
    }

    @Test
    fun vec1InfoStatement() {
        assertEquals("SELECT vec1_info()", Vec1Sql.INFO_SQL)
    }
}
