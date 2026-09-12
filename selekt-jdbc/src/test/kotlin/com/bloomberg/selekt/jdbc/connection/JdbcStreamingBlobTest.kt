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

import com.bloomberg.selekt.CancellationSignal
import com.bloomberg.selekt.StreamingBlobBatch
import com.bloomberg.selekt.StreamingBlobRow
import com.bloomberg.selekt.jdbc.SelektConnection
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FilterInputStream
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val PAYLOAD_SIZE = 4 * 1024 * 1024

internal class JdbcStreamingBlobTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun streamLargeBlobBatchThroughPublicExtension() {
        val databaseFile = File(tempDir, "streaming-blob.db")
        val payload = Random.nextBytes(PAYLOAD_SIZE)
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            val input = CloseTrackingInputStream(payload)
            val selekt = connection.unwrap(SelektConnection::class.java)
            assertTrue(connection.isWrapperFor(SelektConnection::class.java))
            assertEquals(
                1,
                selekt.insertBlobs(
                    batch(),
                    listOf(StreamingBlobRow(arrayOf(null, null), payload.size, input))
                )
            )
            assertFalse(input.closed, "The caller retains ownership of its input stream")
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT data FROM files WHERE id = 1").use {
                    assertTrue(it.next())
                    assertTrue(payload.contentEquals(it.getBytes(1)), "Streamed blob should round-trip exactly")
                }
            }
        }
    }

    @Test
    fun transferBufferIsWipedAfterSuccessfulStreaming() {
        val databaseFile = File(tempDir, "wiped-streaming-buffer.db")
        val input = BufferCapturingInputStream(byteArrayOf(1, 2, 3, 4))
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            assertEquals(
                1,
                connection.unwrap(SelektConnection::class.java).insertBlobs(
                    batch(2),
                    listOf(StreamingBlobRow(arrayOf(null, null), 4, input))
                )
            )
        }
        assertTrue(input.transferBuffer.all { it == 0.toByte() })
    }

    @Test
    fun zeroLengthBulkReadFallsBackToSingleByteRead() {
        val databaseFile = File(tempDir, "zero-read-streaming-buffer.db")
        val input = object : ByteArrayInputStream(byteArrayOf(1, 2)) {
            override fun read(bytes: ByteArray, offset: Int, length: Int): Int = 0
        }
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            assertEquals(
                1,
                connection.unwrap(SelektConnection::class.java).insertBlobs(
                    batch(1),
                    listOf(StreamingBlobRow(arrayOf(null, null), 2, input))
                )
            )
        }
    }

    @Test
    fun validatesBatchShapeAndInsertedBlobSize() {
        val databaseFile = File(tempDir, "invalid-batch-shape.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            val selekt = connection.unwrap(SelektConnection::class.java)
            assertFailsWith<SQLException> {
                selekt.insertBlobs(
                    batch().copy(blobParameterIndex = 3),
                    listOf(StreamingBlobRow(arrayOf(null, null), 1, byteArrayOf(1).inputStream()))
                )
            }
            assertFailsWith<SQLException> {
                selekt.insertBlobs(
                    batch(),
                    listOf(StreamingBlobRow(arrayOf(null), 1, byteArrayOf(1).inputStream()))
                )
            }
            assertFailsWith<SQLException> {
                selekt.insertBlobs(
                    batch().copy(insertSql = "INSERT INTO files (id, data) VALUES (?, zeroblob(length(?) + 2))"),
                    listOf(StreamingBlobRow(arrayOf(null, null), 1, byteArrayOf(1).inputStream()))
                )
            }
        }
    }

    @Test
    fun requiresEachStreamingInsertToChangeOneRow() {
        val databaseFile = File(tempDir, "ignored-streaming-insert.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
                it.executeUpdate("INSERT INTO files VALUES (1, X'01')")
            }
            assertFailsWith<SQLException> {
                connection.unwrap(SelektConnection::class.java).insertBlobs(
                    batch().copy(insertSql = "INSERT OR IGNORE INTO files (id, data) VALUES (?, ?)"),
                    listOf(StreamingBlobRow(arrayOf(1L, null), 1, byteArrayOf(2).inputStream(), 1L))
                )
            }
        }
    }

    @Test
    @Suppress("Detekt.NestedBlockDepth")
    fun reusesBlobHandleAcrossGeneratedAndExplicitRowsOfDifferentSizes() {
        val databaseFile = File(tempDir, "varying-blob.db")
        val payloads = listOf(byteArrayOf(), Random.nextBytes(13), Random.nextBytes(8_193))
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            val rows = listOf(
                StreamingBlobRow(arrayOf(null, null), payloads[0].size, payloads[0].inputStream()),
                StreamingBlobRow(arrayOf(10L, null), payloads[1].size, payloads[1].inputStream(), 10L),
                StreamingBlobRow(arrayOf(null, null), payloads[2].size, payloads[2].inputStream())
            )
            assertEquals(3, connection.unwrap(SelektConnection::class.java).insertBlobs(batch(7), rows))
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT id, data FROM files ORDER BY id").use { resultSet ->
                    listOf(1L, 10L, 11L).zip(payloads).forEach { (id, payload) ->
                        assertTrue(resultSet.next())
                        assertEquals(id, resultSet.getLong(1))
                        val actual = resultSet.getBytes(2)
                        assertTrue(
                            payload.contentEquals(actual),
                            "BLOB for row $id differed: expected ${payload.size} bytes, got ${actual?.size}"
                        )
                    }
                    assertFalse(resultSet.next())
                }
            }
        }
    }

    @Test
    fun shortStreamRollsBackBatchAndReleasesResources() = assertInvalidStreamRollsBack(
        declaredLength = 4,
        payload = byteArrayOf(1, 2)
    )

    @Test
    fun overlongStreamRollsBackBatchAndReleasesResources() = assertInvalidStreamRollsBack(
        declaredLength = 1,
        payload = byteArrayOf(1, 2)
    )

    @Test
    fun cancellationDuringStreamingRollsBackBatch() {
        val databaseFile = File(tempDir, "cancelled-blob.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            val signal = CancellationSignal()
            val input = object : FilterInputStream(ByteArrayInputStream(ByteArray(32))) {
                override fun read(bytes: ByteArray, offset: Int, length: Int): Int =
                    super.read(bytes, offset, length).also { signal.cancel() }
            }
            assertFailsWith<SQLTimeoutException> {
                connection.unwrap(SelektConnection::class.java).insertBlobs(
                    batch(8),
                    listOf(StreamingBlobRow(arrayOf(null, null), 32, input)),
                    signal
                )
            }
            assertEquals(0, connection.rowCount())
        }
    }

    @Test
    fun callerStreamFailureRollsBackBatch() {
        val databaseFile = File(tempDir, "failed-stream-blob.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            var transferBuffer: ByteArray? = null
            val input = object : ByteArrayInputStream(byteArrayOf(1, 2, 3, 4)) {
                private var reads = 0

                override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
                    transferBuffer = bytes
                    if (++reads > 1) {
                        throw IOException("source failed")
                    }
                    return super.read(bytes, offset, length)
                }
            }
            assertFailsWith<SQLException> {
                connection.unwrap(SelektConnection::class.java).insertBlobs(
                    batch(2),
                    listOf(StreamingBlobRow(arrayOf(null, null), 4, input))
                )
            }
            assertEquals(0, connection.rowCount())
            assertTrue(checkNotNull(transferBuffer).all { it == 0.toByte() })
        }
    }

    @Test
    fun suppliedRowIdMustMatchInsertedRow() {
        val databaseFile = File(tempDir, "mismatched-rowid.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            assertFailsWith<SQLException> {
                connection.unwrap(SelektConnection::class.java).insertBlobs(
                    batch(),
                    listOf(StreamingBlobRow(arrayOf(1L, null), 1, byteArrayOf(7).inputStream(), 2L))
                )
            }
            assertEquals(0, connection.rowCount())
        }
    }

    @Test
    fun constraintFailureRollsBackEarlierRows() {
        val databaseFile = File(tempDir, "constraint-blob.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            assertFailsWith<SQLException> {
                connection.unwrap(SelektConnection::class.java).insertBlobs(
                    batch(),
                    listOf(
                        StreamingBlobRow(arrayOf(1L, null), 1, byteArrayOf(1).inputStream(), 1L),
                        StreamingBlobRow(arrayOf(1L, null), 1, byteArrayOf(2).inputStream(), 1L)
                    )
                )
            }
            assertEquals(0, connection.rowCount())
        }
    }

    @Test
    fun triggerFailureRollsBackEarlierRows() {
        val databaseFile = File(tempDir, "trigger-blob.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
                it.executeUpdate(
                    "CREATE TRIGGER reject_second BEFORE INSERT ON files " +
                        "WHEN NEW.id = 2 BEGIN SELECT RAISE(ABORT, 'rejected'); END"
                )
            }
            assertFailsWith<SQLException> {
                connection.unwrap(SelektConnection::class.java).insertBlobs(
                    batch(),
                    listOf(
                        StreamingBlobRow(arrayOf(1L, null), 1, byteArrayOf(1).inputStream(), 1L),
                        StreamingBlobRow(arrayOf(2L, null), 1, byteArrayOf(2).inputStream(), 2L)
                    )
                )
            }
            assertEquals(0, connection.rowCount())
        }
    }

    @Test
    fun participatesInManualTransaction() {
        val databaseFile = File(tempDir, "transactional-blob.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            connection.autoCommit = false
            connection.unwrap(SelektConnection::class.java).insertBlobs(
                batch(),
                listOf(StreamingBlobRow(arrayOf(null, null), 3, byteArrayOf(1, 2, 3).inputStream()))
            )
            assertEquals(1, connection.rowCount())
            connection.rollback()
            assertEquals(0, connection.rowCount())
        }
    }

    @Test
    fun failedBatchRollsBackToSavepointInManualTransaction() {
        val databaseFile = File(tempDir, "failed-transactional-blob.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            connection.autoCommit = false
            connection.createStatement().use {
                it.executeUpdate("INSERT INTO files VALUES (100, X'64')")
            }
            assertFailsWith<SQLException> {
                connection.unwrap(SelektConnection::class.java).insertBlobs(
                    batch(),
                    listOf(
                        StreamingBlobRow(arrayOf(1L, null), 1, byteArrayOf(1).inputStream(), 1L),
                        StreamingBlobRow(arrayOf(2L, null), 2, byteArrayOf(2).inputStream(), 2L)
                    )
                )
            }
            assertEquals(1, connection.rowCount())
            connection.commit()
            assertEquals(1, connection.rowCount())
        }
    }

    private fun assertInvalidStreamRollsBack(declaredLength: Int, payload: ByteArray) {
        val databaseFile = File(tempDir, "invalid-$declaredLength-${payload.size}.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
            }
            val selekt = connection.unwrap(SelektConnection::class.java)
            assertFailsWith<SQLException> {
                selekt.insertBlobs(
                    batch(2),
                    listOf(
                        StreamingBlobRow(arrayOf(null, null), 1, byteArrayOf(7).inputStream()),
                        StreamingBlobRow(arrayOf(null, null), declaredLength, payload.inputStream())
                    )
                )
            }
            assertEquals(0, connection.rowCount())
            assertEquals(
                1,
                selekt.insertBlobs(
                    batch(),
                    listOf(StreamingBlobRow(arrayOf(null, null), 1, byteArrayOf(9).inputStream()))
                )
            )
            assertEquals(1, connection.rowCount())
        }
    }

    private fun batch(transferBufferSize: Int = 64 * 1024) = StreamingBlobBatch(
        table = "files",
        column = "data",
        insertSql = "INSERT INTO files (id, data) VALUES (?, ?)",
        blobParameterIndex = 2,
        transferBufferSize = transferBufferSize
    )

    private fun Connection.rowCount(): Int = createStatement().use { statement ->
        statement.executeQuery("SELECT count(*) FROM files").use {
            assertTrue(it.next())
            it.getInt(1)
        }
    }

    private class CloseTrackingInputStream(payload: ByteArray) : ByteArrayInputStream(payload) {
        var closed = false
            private set

        override fun close() {
            closed = true
            super.close()
        }
    }

    private class BufferCapturingInputStream(payload: ByteArray) : ByteArrayInputStream(payload) {
        lateinit var transferBuffer: ByteArray
            private set

        override fun read(bytes: ByteArray, offset: Int, length: Int): Int =
            super.read(bytes, offset, length).also { transferBuffer = bytes }
    }
}
