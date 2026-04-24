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
import java.io.PrintWriter
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class SelektDataSourceTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var dataSource: SelektDataSource

    @BeforeEach
    fun setUp() {
        dataSource = SelektDataSource()
    }

    @AfterEach
    fun tearDown() {
        dataSource.close()
    }

    @Test
    fun dataSourceConfiguration(): Unit = dataSource.run {
        databasePath = "/tmp/test.db"
        maxPoolSize = 20
        busyTimeout = 5_000
        journalMode = "WAL"
        foreignKeys = true
        setEncryption(EncryptionKeySource.Literal("test-key".toCharArray()))

        assertEquals("/tmp/test.db", databasePath)
        assertEquals(20, maxPoolSize)
        assertEquals(5_000, busyTimeout)
        assertEquals("WAL", journalMode)
        assertTrue(foreignKeys)
        assertTrue(encryptionEnabled)
        assertEquals(EncryptionKeySource.Literal("test-key".toCharArray()), encryptionKeySource)
    }

    @Test
    fun invalidPoolSize(): Unit = dataSource.run {
        assertFailsWith<IllegalArgumentException> {
            maxPoolSize = 0
        }
        assertFailsWith<IllegalArgumentException> {
            maxPoolSize = -1
        }
    }

    @Test
    fun invalidBusyTimeout() {
        assertFailsWith<IllegalArgumentException> {
            dataSource.busyTimeout = -1
        }
    }

    @Test
    fun invalidJournalMode() {
        assertFailsWith<IllegalArgumentException> {
            dataSource.journalMode = "INVALID"
        }
    }

    @Test
    fun validJournalModes(): Unit = dataSource.run {
        journalMode = "DELETE"
        assertEquals("DELETE", journalMode)
        journalMode = "wal"
        assertEquals("wal", journalMode)
        journalMode = "MEMORY"
        assertEquals("MEMORY", journalMode)
    }

    @Test
    fun loginTimeout(): Unit = dataSource.run {
        assertEquals(0, loginTimeout)
        loginTimeout = 30
        assertEquals(30, loginTimeout)
        assertFailsWith<SQLException> {
            loginTimeout = -1
        }
    }

    @Test
    fun logWriter(): Unit = dataSource.run {
        val writer = PrintWriter(System.out)
        logWriter = writer
        assertSame(writer, logWriter)
    }

    @Test
    fun getConnectionWithoutConfiguration() {
        assertFailsWith<SQLException> {
            dataSource.getConnection()
        }
    }

    @Test
    fun close(): Unit = dataSource.run {
        assertFalse(isClosed())
        close()
        assertTrue(isClosed())
        close()
        assertTrue(isClosed())
    }

    @Test
    fun getConnectionAfterClose(): Unit = dataSource.run {
        databasePath = "/tmp/test.db"
        close()
        assertFailsWith<SQLException> {
            getConnection()
        }
    }

    @Test
    fun wrapperInterface(): Unit = dataSource.run {
        assertTrue(isWrapperFor(SelektDataSource::class.java))
        assertFalse(isWrapperFor(String::class.java))
        val unwrapped = unwrap(SelektDataSource::class.java)
        assertSame(this, unwrapped)
        assertFailsWith<SQLException> {
            unwrap(String::class.java)
        }
    }

    @Test
    fun parentLogger(): Unit = dataSource.parentLogger.run {
        assertNotNull(this)
        assertEquals("com.bloomberg.selekt.jdbc.driver.SelektDataSource", name)
    }

    @Test
    fun urlGeneration(): Unit = dataSource.run {
        databasePath = "/path/to/test.db"
        maxPoolSize = 5
        busyTimeout = 2_000
        journalMode = "DELETE"
        foreignKeys = false
        setEncryption(EncryptionKeySource.Literal("secret123".toCharArray()))

        assertEquals("/path/to/test.db", databasePath)
        assertEquals(5, maxPoolSize)
        assertEquals(2_000, busyTimeout)
        assertEquals("DELETE", journalMode)
        assertFalse(foreignKeys)
        assertTrue(encryptionEnabled)
        assertEquals(EncryptionKeySource.Literal("secret123".toCharArray()), encryptionKeySource)
    }

    @Test
    fun setEncryptionWithNullKey(): Unit = dataSource.run {
        setEncryption(null)
        assertFalse(encryptionEnabled)
        assertNull(encryptionKeySource)
        setEncryption(EncryptionKeySource.Literal("somekey".toCharArray()))
        assertTrue(encryptionEnabled)
        assertEquals(EncryptionKeySource.Literal("somekey".toCharArray()), encryptionKeySource)
    }

    @Test
    fun getConnectionWithUsernamePassword(): Unit = dataSource.run {
        databasePath = File(tempDir, "user-pass.db").absolutePath
        getConnection("user", "pass").close()
    }

    @Test
    fun foreignKeysDefault() {
        assertTrue(dataSource.foreignKeys)
    }

    @Test
    fun encryptionDisabledByDefault() {
        assertFalse(dataSource.encryptionEnabled)
    }

    @Test
    fun encryptionKeyNullByDefault() {
        assertNull(dataSource.encryptionKeySource)
    }

    @Test
    fun maxPoolSizeDefault() {
        assertEquals(10, dataSource.maxPoolSize)
    }

    @Test
    fun busyTimeoutDefault() {
        assertEquals(2_500, dataSource.busyTimeout)
    }

    @Test
    fun journalModeDefault() {
        assertEquals("WAL", dataSource.journalMode)
    }

    @Test
    fun logWriterNullByDefault() {
        assertEquals(null, dataSource.logWriter)
    }

    @Test
    fun databasePathEmptyByDefault() {
        assertEquals("", dataSource.databasePath)
    }

    @Test
    fun getConnectionAttempt(): Unit = dataSource.run {
        databasePath = File(tempDir, "test.db").absolutePath
        getConnection().close()
    }

    @Test
    fun getConnectionWithUsernamePasswordAttempt(): Unit = dataSource.run {
        databasePath = File(tempDir, "test2.db").absolutePath
        getConnection("user", "password").close()
    }

    @Test
    fun getConnectionWithEncryption(): Unit = dataSource.run {
        databasePath = File(tempDir, "encrypted.db").absolutePath
        setEncryption(EncryptionKeySource.Literal("0x0123456789ABCDEF".toCharArray()))
        getConnection().close()
    }

    @Test
    fun getConnectionWithFileBasedKey(): Unit = dataSource.run {
        databasePath = File(tempDir, "encrypted2.db").absolutePath
        setEncryption(EncryptionKeySource.FilePath(File(tempDir, "keyfile.bin").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))
        }.absolutePath))
        getConnection().close()
    }

    @Test
    fun getConnectionWithStringKey(): Unit = dataSource.run {
        databasePath = File(tempDir, "encrypted3.db").absolutePath
        setEncryption(EncryptionKeySource.Literal("my-secret-key".toCharArray()))
        getConnection().close()
    }

    @Test
    fun getConnectionWithEncryptionDisabled(): Unit = dataSource.run {
        databasePath = File(tempDir, "plain.db").absolutePath
        setEncryption(null)
        getConnection().close()
    }

    @Test
    fun getConnectionWithCustomPoolSize(): Unit = dataSource.run {
        databasePath = File(tempDir, "pooled.db").absolutePath
        maxPoolSize = 20
        getConnection().close()
    }

    @Test
    fun getConnectionWithCustomBusyTimeout(): Unit = dataSource.run {
        databasePath = File(tempDir, "timeout.db").absolutePath
        busyTimeout = 5000
        getConnection().close()
    }

    @Test
    fun getConnectionWithDifferentJournalModes(): Unit = dataSource.run {
        listOf(
            "DELETE",
            "TRUNCATE",
            "PERSIST",
            "MEMORY",
            "WAL",
            "OFF"
        ).forEach {
            databasePath = File(tempDir, "journal-$it.db").absolutePath
            journalMode = it
            getConnection().close()
        }
    }

    @Test
    fun getConnectionWithForeignKeysDisabled(): Unit = dataSource.run {
        databasePath = File(tempDir, "nofk.db").absolutePath
        foreignKeys = false
        getConnection().close()
    }

    @Test
    fun getConnectionWithAllProperties(): Unit = dataSource.run {
        databasePath = File(tempDir, "all-props.db").absolutePath
        maxPoolSize = 15
        busyTimeout = 3000
        journalMode = "DELETE"
        foreignKeys = false
        setEncryption(EncryptionKeySource.Literal("test-key-123".toCharArray()))
        getConnection().close()
    }

    @Test
    fun setEncryptionWithKey(): Unit = dataSource.run {
        setEncryption(EncryptionKeySource.Literal("my-key".toCharArray()))
        assertTrue(encryptionEnabled)
        assertEquals(EncryptionKeySource.Literal("my-key".toCharArray()), encryptionKeySource)
    }

    @Test
    fun setEncryptionWithKeyPath(): Unit = dataSource.run {
        setEncryption(EncryptionKeySource.FilePath("/path/to/key"))
        assertTrue(encryptionEnabled)
        assertEquals(EncryptionKeySource.FilePath("/path/to/key"), encryptionKeySource)
    }

    @Test
    fun setEncryptionLiteralReplacesFilePath(): Unit = dataSource.run {
        setEncryption(EncryptionKeySource.FilePath("/path/to/key"))
        setEncryption(EncryptionKeySource.Literal("literal".toCharArray()))
        assertEquals(EncryptionKeySource.Literal("literal".toCharArray()), encryptionKeySource)
    }

    @Test
    fun setEncryptionFilePathReplacesLiteral(): Unit = dataSource.run {
        setEncryption(EncryptionKeySource.Literal("literal".toCharArray()))
        setEncryption(EncryptionKeySource.FilePath("/path/to/key"))
        assertEquals(EncryptionKeySource.FilePath("/path/to/key"), encryptionKeySource)
    }

    @Test
    fun setEncryptionWithoutKey(): Unit = dataSource.run {
        setEncryption(null)
        assertFalse(encryptionEnabled)
        assertNull(encryptionKeySource)
    }

    @Test
    fun clearEncryption(): Unit = dataSource.run {
        setEncryption(EncryptionKeySource.Literal("key".toCharArray()))
        assertTrue(encryptionEnabled)
        setEncryption(null)
        assertFalse(encryptionEnabled)
        assertNull(encryptionKeySource)
    }

    @Test
    fun logWriterNull(): Unit = dataSource.run {
        logWriter = null
        assertNull(logWriter)
    }

    @Test
    fun closeIdempotent(): Unit = dataSource.run {
        close()
        assertTrue(isClosed())
        close()
        assertTrue(isClosed())
    }

    @Test
    fun setDatabasePathUpdatesUrl(): Unit = dataSource.run {
        databasePath = "/path/to/mydb.db"
        assertEquals("/path/to/mydb.db", databasePath)
    }

    @Test
    fun getConnectionWithHexKeyUppercaseX(): Unit = dataSource.run {
        databasePath = File(tempDir, "hex-upper.db").absolutePath
        setEncryption(EncryptionKeySource.Literal("0X123456".toCharArray()))
        getConnection().close()
    }

    @Test
    fun getConnectionWithHexKeyLowercaseX(): Unit = dataSource.run {
        databasePath = File(tempDir, "hex-lower.db").absolutePath
        setEncryption(EncryptionKeySource.Literal("0x123456".toCharArray()))
        getConnection().close()
    }

    @Test
    fun getConnectionWithNullEncryptionKey(): Unit = dataSource.run {
        databasePath = File(tempDir, "null-key.db").absolutePath
        setEncryption(null)
        getConnection().close()
    }

    @Test
    fun getConnectionWithNonExistentKeyFile(): Unit = dataSource.run {
        databasePath = File(tempDir, "nonexistent-key.db").absolutePath
        setEncryption(EncryptionKeySource.FilePath("/nonexistent/path/to/keyfile.bin"))
        getConnection().close()
    }

    @Test
    fun allJournalModesUppercase() {
        listOf(
            "DELETE",
            "TRUNCATE",
            "PERSIST",
            "MEMORY",
            "WAL",
            "OFF"
        ).forEach {
            SelektDataSource().run {
                try {
                    journalMode = it.uppercase()
                    assertEquals(it.uppercase(), journalMode)
                } finally {
                    close()
                }
            }
        }
    }

    @Test
    fun allJournalModesLowercase() {
        listOf(
            "delete",
            "truncate",
            "persist",
            "memory",
            "wal",
            "off"
        ).forEach {
            SelektDataSource().run {
                try {
                    journalMode = it
                    assertEquals(it, journalMode)
                } finally {
                    close()
                }
            }
        }
    }

    @Test
    fun allJournalModesMixedCase() {
        listOf(
            "Delete",
            "Truncate",
            "Persist",
            "Memory",
            "Wal",
            "Off"
        ).forEach {
            SelektDataSource().run {
                try {
                    journalMode = it
                    assertEquals(it, journalMode)
                } finally {
                    close()
                }
            }
        }
    }

    @Test
    fun validBusyTimeoutZero(): Unit = dataSource.run {
        busyTimeout = 0
        assertEquals(0, busyTimeout)
    }

    @Test
    fun validPoolSizeOne(): Unit = dataSource.run {
        maxPoolSize = 1
        assertEquals(1, maxPoolSize)
    }

    @Test
    fun validPoolSizeLarge(): Unit = dataSource.run {
        maxPoolSize = 1_000
        assertEquals(1_000, maxPoolSize)
    }

    @Test
    fun validBusyTimeoutLarge(): Unit = dataSource.run {
        busyTimeout = 60_000
        assertEquals(60_000, busyTimeout)
    }

    @Test
    fun validLoginTimeoutZero(): Unit = dataSource.run {
        loginTimeout = 0
        assertEquals(0, loginTimeout)
    }

    @Test
    fun validLoginTimeoutPositive(): Unit = dataSource.run {
        loginTimeout = 60
        assertEquals(60, loginTimeout)
    }

    @Test
    fun wrappedType(): Unit = dataSource.run {
        assertTrue(isWrapperFor(SelektDataSource::class.java))
        assertTrue(isWrapperFor(javax.sql.DataSource::class.java))
    }

    @Test
    fun notWrappedType(): Unit = dataSource.run {
        assertFalse(isWrapperFor(String::class.java))
        assertFalse(isWrapperFor(List::class.java))
    }

    @Test
    fun unwrapToDataSource(): Unit = dataSource.run {
        val unwrapped = unwrap(javax.sql.DataSource::class.java)
        assertSame(this, unwrapped)
    }

    @Test
    fun unwrapToSelektDataSource(): Unit = dataSource.run {
        assertSame(this, unwrap(SelektDataSource::class.java))
    }

    @Test
    fun unwrapToInvalidType() {
        assertFailsWith<SQLException> {
            dataSource.unwrap(List::class.java)
        }
    }
}
