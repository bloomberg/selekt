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

package com.bloomberg.selekt.android

import com.bloomberg.selekt.annotations.Experimental
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.ZeroBlob
import com.bloomberg.selekt.commons.deleteDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SQLiteDatabaseAttachTest {
    private val file = createTempFile("test-sql-database-sharding", ".db").toFile().apply { deleteOnExit() }
    private val otherFile = createTempFile("test-sql-database-sharding-other", ".db").toFile().apply { deleteOnExit() }

    // DELETE mode because it has one connection. Databases must be attached per connection.
    private val database = SQLiteDatabase.openOrCreateDatabase(file, SQLiteJournalMode.DELETE.databaseConfiguration,
        ByteArray(32) { 0x42 })
    private val other = SQLiteDatabase.openOrCreateDatabase(
        otherFile, SQLiteJournalMode.DELETE.databaseConfiguration, ByteArray(32) { 0x42 })

    @BeforeEach
    fun setUp() {
        database.exec("PRAGMA journal_mode=${SQLiteJournalMode.DELETE}")
    }

    @AfterEach
    fun tearDown() {
        database.run {
            try {
                if (isOpen) {
                    close()
                }
                assertFalse(isOpen)
            } finally {
                if (file.exists()) {
                    assertTrue(deleteDatabase(file))
                }
            }
        }
    }

    @OptIn(Experimental::class)
    @Test
    fun attachSingle() {
        other.exec("CREATE TABLE 'Files' (identifier TEXT PRIMARY KEY, data BLOB NOT NULL)")
        database.run {
            exec("ATTACH '${other.path}' AS other")
            exec("CREATE TABLE 'Catalogue' (identifier TEXT PRIMARY KEY, database TEXT NOT NULL)")
        }
        val identifier = UUID.randomUUID().toString()
        other.apply {
            exec("INSERT INTO 'Files' VALUES (?, ?)", arrayOf(identifier, ZeroBlob(10)))
            ByteArray(10) { 0x02 }.inputStream().use {
                writeToBlob("Files", "data", 1L, 0, it)
            }
        }
        database.run {
            ByteArrayOutputStream(10).use {
                readFromBlob("other", "Files", "data", 1L, 0, 10, it)
                assertEquals(0x02, it.toByteArray().first())
            }
        }
    }
}
