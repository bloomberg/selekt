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

package com.bloomberg.selekt.jdbc.statement

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.sql.DriverManager
import java.sql.Statement
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SimpleGeneratedKeysTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun generatedKeysWithPreparedStatement() {
        val databaseFile = File(tempDir, "genkeys.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("DROP TABLE IF EXISTS test")
                it.executeUpdate("CREATE TABLE test (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)")
            }
            val preparedStatement = connection.prepareStatement("INSERT INTO test (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)
            preparedStatement.setString(1, "First")
            val updateCount = preparedStatement.executeUpdate()
            assertEquals(1, updateCount, "Update count should be 1")
            val keys = preparedStatement.generatedKeys
            val hasRow = keys.next()
            assertTrue(hasRow, "Generated keys should have a row")
            val id = keys.getLong(1)
            assertTrue(id > 0, "Generated ID should be > 0, but was $id")
        }
    }
}
