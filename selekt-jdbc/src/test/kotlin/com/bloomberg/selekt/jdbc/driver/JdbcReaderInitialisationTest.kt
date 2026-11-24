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

import com.bloomberg.selekt.SQLDatabase
import java.io.File
import java.sql.Connection
import java.sql.SQLException
import java.util.Properties
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class JdbcReaderInitialisationTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun driverOpensReaderDuringWriteTransaction() {
        val driver = SelektDriver()
        val url = "jdbc:sqlite:${File(tempDir, "driver.db").absolutePath}"
        verifyConcurrentReader { assertNotNull(driver.connect(url, Properties())) }
    }

    @Test
    fun dataSourceOpensReaderDuringWriteTransaction() {
        val source = SelektDataSource().apply {
            databasePath = File(tempDir, "source.db").absolutePath
        }
        try {
            verifyConcurrentReader { source.connection }
        } finally {
            source.close()
        }
    }

    @Test
    fun driverDisablesForeignKeysOnReaders() {
        val driver = SelektDriver()
        val url = "jdbc:sqlite:${File(tempDir, "disabled-driver.db").absolutePath}?foreignKeys=false"
        verifyConcurrentReader(expectedForeignKeys = 0) { assertNotNull(driver.connect(url, Properties())) }
    }

    @Test
    fun dataSourceDisablesForeignKeysOnReaders() {
        val source = SelektDataSource().apply {
            databasePath = File(tempDir, "disabled-source.db").absolutePath
            foreignKeys = false
        }
        try {
            verifyConcurrentReader(expectedForeignKeys = 0) { source.connection }
        } finally {
            source.close()
        }
    }

    @Test
    fun failedInitialisationClosesDatabase() {
        val database = mock<SQLDatabase> {
            whenever(it.configureForeignKeys(true)) doThrow SQLException("initialisation failed")
        }
        assertFailsWith<SQLException> { database.initialiseForJdbc(true) }
        verify(database).close()
    }

    private fun verifyConcurrentReader(expectedForeignKeys: Int = 1, openConnection: () -> Connection) {
        openConnection().use { writer ->
            writer.createStatement().use {
                it.execute("PRAGMA journal_mode=WAL")
                it.executeUpdate("CREATE TABLE entries (id INTEGER PRIMARY KEY)")
                it.executeUpdate("INSERT INTO entries VALUES (1)")
            }
            writer.autoCommit = false
            writer.createStatement().use { it.executeUpdate("INSERT INTO entries VALUES (2)") }
            val executor = Executors.newSingleThreadExecutor()
            try {
                val result = executor.submit<Pair<Int, Int>> {
                    openConnection().use { reader ->
                        reader.readInt("SELECT count(*) FROM entries") to reader.readInt("PRAGMA foreign_keys")
                    }
                }
                assertEquals(1 to expectedForeignKeys, result.get(5, TimeUnit.SECONDS))
                verifyCompetingWriter(openConnection, writer)
            } finally {
                writer.rollback()
                executor.shutdown()
                assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
            }
        }
    }

    private fun verifyCompetingWriter(openConnection: () -> Connection, writer: Connection) {
        val attempted = CountDownLatch(1)
        val executor = Executors.newSingleThreadExecutor()
        try {
            val competingWrite = executor.submit<Int> {
                openConnection().use { competitor ->
                    competitor.createStatement().use { statement ->
                        attempted.countDown()
                        statement.executeUpdate("INSERT INTO entries VALUES (3)")
                    }
                }
            }
            assertTrue(attempted.await(5, TimeUnit.SECONDS))
            assertFalse(competingWrite.isDone)
            assertEquals(2, writer.readInt("SELECT count(*) FROM entries"))
            assertFailsWith<java.util.concurrent.TimeoutException> {
                competingWrite.get(100, TimeUnit.MILLISECONDS)
            }
            writer.rollback()
            assertEquals(1, competingWrite.get(5, TimeUnit.SECONDS))
            openConnection().use { reader ->
                assertEquals(0, reader.readInt("SELECT count(*) FROM entries WHERE id=2"))
                assertEquals(1, reader.readInt("SELECT count(*) FROM entries WHERE id=3"))
            }
        } finally {
            writer.rollback()
            executor.shutdown()
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
        }
    }

    private fun Connection.readInt(sql: String): Int = createStatement().use { statement ->
        statement.executeQuery(sql).use { result ->
            assertTrue(result.next())
            result.getInt(1)
        }
    }
}
