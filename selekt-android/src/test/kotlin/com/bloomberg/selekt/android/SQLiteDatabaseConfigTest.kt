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

package com.bloomberg.selekt.android

import com.bloomberg.selekt.SQLiteDbConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class SQLiteDatabaseConfigTest {
    private val database = SQLiteDatabase.createInMemoryDatabase()

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun defensiveModeCanBeEnabled(): Unit = database.run {
        databaseConfig(SQLiteDbConfig.DEFENSIVE, 1)
    }

    @Test
    fun defensiveModeCanBeDisabled(): Unit = database.run {
        databaseConfig(SQLiteDbConfig.DEFENSIVE, 1)
        databaseConfig(SQLiteDbConfig.DEFENSIVE, 0)
    }

    @Test
    fun defensiveModePreventsDirectSqliteMasterWrite(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (bar INT)")
        databaseConfig(SQLiteDbConfig.DEFENSIVE, 1)
        assertFails {
            exec("UPDATE sqlite_master SET sql='CREATE TABLE Foo (bar INT, baz INT)' WHERE name='Foo'")
        }
    }

    @Test
    fun writableSchemaCanBeDisabled(): Unit = database.run {
        databaseConfig(SQLiteDbConfig.WRITABLE_SCHEMA, 0)
    }
}

