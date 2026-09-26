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

import com.bloomberg.selekt.SQLCipherCompatibility
import com.bloomberg.selekt.SQLiteJournalMode
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class SQLiteDatabaseSampleTest {
    private val database = openOrCreateDatabase(
        File(requireNotNull(javaClass.classLoader?.getResource("databases/sample.sqlcipher.db")?.file)),
        SQLiteJournalMode.WAL.databaseConfiguration,
        ByteArray(32) { 0x42 }
    )

    @Test
    fun readDatabase() {
        database.query("SELECT * FROM Users", emptyArray()).use {
            assertEquals(1, it.count)
        }
    }

    @Test
    fun migrateSQLCipher4DatabaseTo5() {
        val migrated = createTempFile("sample-sqlcipher-migrated", ".db").toFile().apply { deleteOnExit() }
        requireNotNull(javaClass.classLoader?.getResourceAsStream("databases/sample.sqlcipher.db")).use {
            Files.copy(it, migrated.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        }
        val migrationConfiguration = SQLiteJournalMode.DELETE.databaseConfiguration.copy(
            sqlCipherCompatibility = SQLCipherCompatibility.MIGRATE_TO_V5
        )
        openOrCreateDatabase(migrated, migrationConfiguration, ByteArray(32) { 0x42 }).use {
            it.query("SELECT * FROM Users", emptyArray()).use { cursor ->
                assertEquals(1, cursor.count)
            }
        }
        openOrCreateDatabase(
            migrated,
            migrationConfiguration.copy(sqlCipherCompatibility = SQLCipherCompatibility.V5),
            ByteArray(32) { 0x42 }
        ).use {
            it.query("SELECT * FROM Users", emptyArray()).use { cursor ->
                assertEquals(1, cursor.count)
            }
        }
    }
}
