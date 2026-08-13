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

package com.bloomberg.selekt.jdbc.connection

import com.bloomberg.selekt.SQLDatabase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.sql.DriverManager
import java.sql.Statement
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val PAYLOAD_SIZE = 4 * 1024 * 1024

internal class JdbcStreamingBlobTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun streamLargeBlobThroughUnwrappedDatabase() {
        val databaseFile = File(tempDir, "streaming-blob.db")
        val payload = Random.nextBytes(PAYLOAD_SIZE)
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("DROP TABLE IF EXISTS files")
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            val rowId = connection.prepareStatement(
                "INSERT INTO files (data) VALUES (zeroblob(?))",
                Statement.RETURN_GENERATED_KEYS
            ).use {
                it.setInt(1, payload.size)
                it.executeUpdate()
                it.generatedKeys.use { keys ->
                    assertTrue(keys.next(), "Expected a generated rowid")
                    keys.getLong(1)
                }
            }

            val database = connection.unwrap(SQLDatabase::class.java)
            ByteArrayInputStream(payload).use { database.writeToBlob("files", "data", rowId, 0, it) }

            assertEquals(payload.size, database.sizeOfBlob("files", "data", rowId))

            val roundTripped = ByteArrayOutputStream(payload.size).also {
                database.readFromBlob("files", "data", rowId, 0, payload.size, it)
            }.toByteArray()
            assertTrue(payload.contentEquals(roundTripped), "Streamed blob should round-trip exactly")
        }
    }

    @Test
    fun sizeOfBlobMatchesZeroblobReservation() {
        val databaseFile = File(tempDir, "sized-blob.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("DROP TABLE IF EXISTS files")
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
                it.executeUpdate("INSERT INTO files (data) VALUES (zeroblob(1024))")
            }
            val database = connection.unwrap(SQLDatabase::class.java)
            assertEquals(1024, database.sizeOfBlob("files", "data", 1L))
        }
    }
}
