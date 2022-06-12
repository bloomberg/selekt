/*
 * Copyright 2022 Bloomberg Finance L.P.
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

import android.content.ContentValues
import com.bloomberg.selekt.annotations.Experimental
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.ZeroBlob
import com.bloomberg.selekt.commons.deleteDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import kotlin.io.path.createTempFile
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(Experimental::class)
internal class SQLiteDatabaseBlobTest {
    private val file = createTempFile("test-sql-database-blob", ".db").toFile().also { it.deleteOnExit() }

    private val database = SQLiteDatabase.openOrCreateDatabase(file, SQLiteJournalMode.WAL.databaseConfiguration,
        ByteArray(32) { 0x42 })

    @BeforeEach
    fun setUp() {
        database.exec("PRAGMA journal_mode=${SQLiteJournalMode.WAL}")
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

    @Test
    fun sizeOfEmptyBlob(): Unit = database.run {
        val expectedSize = 0
        exec("CREATE TABLE 'Foo' (data BLOB)")
        exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(ZeroBlob(0)))
        assertEquals(expectedSize, sizeOfBlob("Foo", "data", 1L))
    }

    @Test
    fun sizeOfBlob(): Unit = database.run {
        val expectedSize = 42
        exec("CREATE TABLE 'Foo' (data BLOB)")
        exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(ZeroBlob(expectedSize)))
        assertEquals(expectedSize, sizeOfBlob("Foo", "data", 1L))
    }

    @Test
    fun emptyBlob(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (identifier TEXT PRIMARY KEY, data BLOB)")
        val identifier = "abc"
        exec("INSERT INTO 'Foo' VALUES (?, ?)", arrayOf(identifier, ZeroBlob(0)))
        val row = query("SELECT rowid FROM 'Foo' WHERE identifier=?", arrayOf(identifier)).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            it.getLong(0)
        }
        assertEquals(1L, row)
        byteArrayOf().inputStream().use { writeToBlob("Foo", "data", row, 0, it) }
        ByteArrayOutputStream(0).use {
            readFromBlob("Foo", "data", row, 0, 0, it)
            assertTrue(it.toByteArray().isEmpty())
        }
    }

    @Test
    fun singleBlob(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (identifier TEXT PRIMARY KEY, data BLOB)")
        val identifier = "abc"
        val size = 10
        exec("INSERT INTO 'Foo' VALUES (?, ?)", arrayOf(identifier, ZeroBlob(size)))
        val row = query("SELECT rowid FROM 'Foo' WHERE identifier=?", arrayOf(identifier)).use {
            assertEquals(1, it.count)
            assertTrue(it.moveToFirst())
            it.getLong(0)
        }
        assertEquals(1L, row)
        val expectedBytes = ByteArray(size) { (it + 0x42).toByte() }
        expectedBytes.inputStream().use { writeToBlob("Foo", "data", row, 0, it) }
        ByteArrayOutputStream(size).use {
            readFromBlob("Foo", "data", row, 0, size, it)
            it.toByteArray().forEachIndexed { index, byte -> assertEquals(expectedBytes[index], byte) }
        }
    }

    @Test
    fun singleBlobWithContentValues(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (data BLOB)")
        val size = 10
        val row = insert("Foo", ContentValues().apply { putNull("data") }, ConflictAlgorithm.REPLACE)
        assertEquals(1L, row)
        exec("UPDATE OR ROLLBACK 'Foo' SET data=? WHERE rowid=?", arrayOf(ZeroBlob(size), row))
        assertEquals(size, sizeOfBlob("Foo", "data", row))
    }

    @Test
    fun readSingleBlobWithOffsetAndLimit(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (identifier TEXT PRIMARY KEY, data BLOB)")
        val identifier = "abc"
        val size = 10
        exec("INSERT INTO 'Foo' VALUES (?, ?)", arrayOf(identifier, ZeroBlob(size)))
        val expectedBytes = ByteArray(size) { (it + 0x42).toByte() }
        expectedBytes.inputStream().use { writeToBlob("Foo", "data", 1L, 0, it) }
        ByteArrayOutputStream(2).use {
            readFromBlob("Foo", "data", 1L, 1, 2, it)
            it.toByteArray().forEachIndexed { index, byte -> assertEquals(expectedBytes[index + 1], byte) }
        }
    }

    @Test
    fun writeSingleBlobWithOffsetAndLimit(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (identifier TEXT PRIMARY KEY, data BLOB)")
        val identifier = "abc"
        val size = 10
        exec("INSERT INTO 'Foo' VALUES (?, ?)", arrayOf(identifier, ZeroBlob(size)))
        val expectedBytes = ByteArray(2) { (it + 0x42).toByte() }
        expectedBytes.inputStream().use { writeToBlob("Foo", "data", 1L, 1, it) }
        ByteArrayOutputStream(4).use {
            readFromBlob("Foo", "data", 1L, 0, 4, it)
            val buffer = it.toByteArray()
            assertEquals(0, buffer.first())
            buffer.sliceArray(1..2).forEachIndexed { index, byte ->
                assertEquals(expectedBytes[index], byte)
            }
            assertEquals(0, buffer.last())
        }
    }

    @Test
    fun resizeBlob(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (data BLOB)")
        exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(ZeroBlob(10)))
        exec("UPDATE OR ROLLBACK 'Foo' SET data=? WHERE rowid=?", arrayOf(ZeroBlob(42), 1L))
        assertEquals(42, sizeOfBlob("Foo", "data", 1L))
    }

    @Test
    fun resizingZerosOutBlob(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (data BLOB)")
        exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(ZeroBlob(1)))
        ByteArray(1) { 0x42 }.inputStream().use {
            writeToBlob("Foo", "data", 1L, 0, it)
        }
        ByteArrayOutputStream(1).use {
            readFromBlob("Foo", "data", 1L, 0, 1, it)
            assertEquals(0x42, it.toByteArray().first())
        }
        exec("UPDATE OR ROLLBACK 'Foo' SET data=? WHERE rowid=?", arrayOf(ZeroBlob(1), 1L))
        ByteArrayOutputStream(1).use {
            readFromBlob("Foo", "data", 1L, 0, 1, it)
            assertEquals(0, it.toByteArray().first())
        }
    }

    @Test
    fun interleavedBlobs(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (data BLOB)")
        val size = 10
        repeat(2) {
            exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(ZeroBlob(size)))
        }
        repeat(size) { i ->
            arrayOf(i, size - i - 1).forEachIndexed { index, j ->
                byteArrayOf(j.toByte()).inputStream().use {
                    writeToBlob("Foo", "data", index + 1L, i, it)
                }
            }
        }
        ByteArrayOutputStream(size).use {
            readFromBlob("Foo", "data", 1L, 0, size, it)
            it.toByteArray().forEachIndexed { index, byte -> assertEquals(index.toByte(), byte) }
        }
        ByteArrayOutputStream(size).use {
            readFromBlob("Foo", "data", 2L, 0, size, it)
            it.toByteArray().forEachIndexed { index, byte -> assertEquals((size - index - 1).toByte(), byte) }
        }
    }

    @Test
    fun failedBlobWriteIsRolledBack(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (data BLOB)")
        exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(ZeroBlob(10)))
        object : InputStream() {
            private var index = 0

            override fun read() = if (index++ < 10 * DEFAULT_BUFFER_SIZE) 42 else throw IOException()
        }.let {
            runCatching { writeToBlob("Foo", "data", 1L, 0, it) }
        }
        ByteArrayOutputStream(1).use {
            readFromBlob("Foo", "data", 1L, 0, 1, it)
            assertEquals(0, it.toByteArray().first())
        }
    }

    @Test
    fun concurrentBlobWrites(): Unit = database.run {
        exec("CREATE TABLE 'Foo' (data BLOB)")
        val size = 1_000_000
        val batchSize = 1_000
        repeat(2) {
            exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(ZeroBlob(size)))
        }
        val latch = CountDownLatch(2)
        arrayOf(
            thread {
                latch.apply {
                    countDown()
                    await()
                }
                repeat(size / batchSize) { j ->
                    ByteArray(batchSize) { i -> (batchSize * j + i).toByte() }.inputStream().use {
                        writeToBlob("Foo", "data", 1L, batchSize * j, it)
                    }
                }
            },
            thread {
                latch.apply {
                    countDown()
                    await()
                }
                repeat(size / batchSize) { j ->
                    ByteArray(batchSize) { i -> (batchSize * j + i + 0x42).toByte() }.inputStream().use {
                        writeToBlob("Foo", "data", 2L, batchSize * j, it)
                    }
                }
            }
        ).forEach { it.join() }
        ByteArrayOutputStream(size).use {
            readFromBlob("Foo", "data", 1L, 0, size, it)
            it.toByteArray().forEachIndexed { index, byte -> assertEquals(index.toByte(), byte) }
        }
        ByteArrayOutputStream(size).use {
            readFromBlob("Foo", "data", 2L, 0, size, it)
            it.toByteArray().forEachIndexed { index, byte -> assertEquals((index + 0x42).toByte(), byte) }
        }
    }
}
