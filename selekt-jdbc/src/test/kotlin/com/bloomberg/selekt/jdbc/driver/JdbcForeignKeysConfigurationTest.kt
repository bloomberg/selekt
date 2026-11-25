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

package com.bloomberg.selekt.jdbc.driver

import java.io.File
import java.sql.Connection
import java.sql.SQLException
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class JdbcForeignKeysConfigurationTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun driverPropertiesOverrideUrlOnEveryConnection() {
        val driver = SelektDriver()
        listOf(true, false, true).forEach { enabled ->
            val url = "jdbc:sqlite:${File(tempDir, "driver.db").absolutePath}?foreignKeys=${!enabled}"
            val properties = Properties().apply { setProperty("foreignKeys", enabled.toString()) }
            repeat(2) {
                assertNotNull(driver.connect(url, properties)).use { connection ->
                    connection.verifyForeignKeys(enabled)
                }
            }
        }
    }

    @Test
    fun dataSourcePropertiesOverrideUrlAndPartitionCache() {
        val source = SelektDataSource().apply {
            databasePath = "${File(tempDir, "source.db").absolutePath}?foreignKeys=false"
        }
        try {
            source.connection.use { original ->
                original.verifyForeignKeys(true)
                source.foreignKeys = false
                source.connection.use { it.verifyForeignKeys(false) }
                original.verifyForeignKeys(true)
                source.foreignKeys = true
                source.connection.use { it.verifyForeignKeys(true) }
                assertEquals(2, source.cachedDatabaseCount)
            }
        } finally {
            source.close()
        }
    }

    @Test
    fun recreatedDataSourceUsesResolvedProperty() {
        listOf(true, false).forEach { enabled ->
            repeat(2) {
                val source = SelektDataSource().apply {
                    databasePath = "${File(tempDir, "recreated.db").absolutePath}?foreignKeys=${!enabled}"
                    foreignKeys = enabled
                }
                try {
                    source.connection.use { it.verifyForeignKeys(enabled) }
                } finally {
                    source.close()
                }
            }
        }
    }

    private fun Connection.verifyForeignKeys(enabled: Boolean) {
        createStatement().use { statement ->
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS parent(id INTEGER PRIMARY KEY)")
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS child(parent_id INTEGER REFERENCES parent(id))")
            if (enabled) {
                assertFailsWith<SQLException> { statement.executeUpdate("INSERT INTO child VALUES (404)") }
            } else {
                assertEquals(1, statement.executeUpdate("INSERT INTO child VALUES (404)"))
            }
            statement.executeQuery("PRAGMA foreign_keys").use {
                assertTrue(it.next())
                assertEquals(if (enabled) 1 else 0, it.getInt(1))
            }
        }
    }
}
