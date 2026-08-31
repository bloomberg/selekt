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

import android.content.Context
import android.database.sqlite.SQLiteException
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.commons.deleteDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

private class ThrowOnceThenSucceedCallback(
    private val exceptionSupplier: () -> Throwable
) : ISQLiteOpenHelper.Callback {
    var attempts = 0
        private set

    override fun onCreate(database: SQLiteDatabase) {
        attempts++
        if (attempts == 1) {
            throw exceptionSupplier()
        }
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

private class ConfigureCapturingCallback : ISQLiteOpenHelper.Callback {
    var capturedDatabase: SQLiteDatabase? = null
        private set

    override fun onConfigure(database: SQLiteDatabase) {
        capturedDatabase = database
    }

    override fun onCreate(database: SQLiteDatabase) = Unit

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

internal class SQLiteOpenHelperKeyRetryTest {
    private val file = createTempFile("test-open-helper-key-retry", ".db").toFile().apply { deleteOnExit() }

    private val targetContext = mock<Context>().apply {
        whenever(getDatabasePath(any())) doReturn file
    }

    @AfterEach
    fun tearDown() {
        deleteDatabase(file)
    }

    @Test
    fun `key is not zeroed after a failed open and is used again on retry`() {
        val key = ByteArray(32) { 0x42 }
        val callback = ThrowOnceThenSucceedCallback { IllegalStateException("Simulated onCreate failure.") }
        newHelper(key, callback).use {
            assertFailsWith<IllegalStateException> {
                it.writableDatabase
            }
            val internalKeyAfterFailure = internalKeyOf(it)
            requireNotNull(internalKeyAfterFailure)
            assertTrue(
                internalKeyAfterFailure.all { it == 0x42.toByte() },
                "Internal _key must not be zeroed after a failed open"
            )
            val database = it.writableDatabase
            assertTrue(database.isOpen)
            assertEquals(2, callback.attempts)
            val internalKeyAfterSuccess = internalKeyOf(it)
            requireNotNull(internalKeyAfterSuccess)
            assertTrue(
                internalKeyAfterSuccess.all { it == 0.toByte() },
                "Internal _key must be zeroed once the retry succeeds"
            )
        }
    }

    @Test
    fun `partially opened database is closed when onCreate throws`() {
        val key = ByteArray(32) { 0x42 }
        var capturedDatabase: SQLiteDatabase? = null
        val callback = object : ISQLiteOpenHelper.Callback {
            override fun onConfigure(database: SQLiteDatabase) {
                capturedDatabase = database
            }

            override fun onCreate(database: SQLiteDatabase): Unit = error("Simulated failure.")

            override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        newHelper(key, callback).use {
            assertFailsWith<IllegalStateException> {
                it.writableDatabase
            }
            assertFalse(
                requireNotNull(capturedDatabase).isOpen,
                "The partially-opened database must be closed when initialisation fails"
            )
        }
    }

    @Test
    fun `key is not zeroed and database is closed when onDowngrade throws by default`() {
        val key = ByteArray(32) { 0x42 }
        newHelper(key.copyOf(), mock(), version = 2).use { it.writableDatabase }
        val callback = ConfigureCapturingCallback()
        newHelper(key, callback, version = 1).use {
            assertFailsWith<SQLiteException> {
                it.writableDatabase
            }
            val internalKey = internalKeyOf(it)
            requireNotNull(internalKey)
            assertTrue(
                internalKey.all { it == 0x42.toByte() },
                "Internal _key must not be zeroed when onDowngrade throws"
            )
            assertFalse(
                requireNotNull(callback.capturedDatabase).isOpen,
                "The partially-opened database must be closed when the default onDowngrade throws"
            )
        }
    }

    @Test
    fun `writableDatabase throws after close is called before any access`() {
        val key = ByteArray(32) { 0x42 }
        val helper = newHelper(key, mock())
        helper.close()
        assertFailsWith<IllegalStateException> {
            helper.writableDatabase
        }
    }

    @Test
    fun `writableDatabase succeeds after close on an unkeyed helper`() {
        val helper = newHelper(key = null, callback = mock())
        helper.close()
        helper.writableDatabase.use {
            assertTrue(it.isOpen)
        }
    }

    @Test
    fun `close waits for database initialization and closes the initialized database`() {
        val configured = CountDownLatch(1)
        val resumeInitialization = CountDownLatch(1)
        val callback = object : ISQLiteOpenHelper.Callback {
            override fun onConfigure(database: SQLiteDatabase) {
                configured.countDown()
                check(resumeInitialization.await(10, TimeUnit.SECONDS))
            }

            override fun onCreate(database: SQLiteDatabase) = Unit

            override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        val helper = newHelper(ByteArray(32) { 0x42 }, callback)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val databaseFuture = executor.submit<SQLiteDatabase> { helper.writableDatabase }
            assertTrue(configured.await(10, TimeUnit.SECONDS))
            val closeStarted = CountDownLatch(1)
            val closeFuture = executor.submit {
                closeStarted.countDown()
                helper.close()
            }
            assertTrue(closeStarted.await(10, TimeUnit.SECONDS))
            assertFailsWith<TimeoutException> {
                closeFuture.get(100, TimeUnit.MILLISECONDS)
            }

            resumeInitialization.countDown()
            val database = databaseFuture.get(10, TimeUnit.SECONDS)
            closeFuture.get(10, TimeUnit.SECONDS)
            assertFalse(database.isOpen)
        } finally {
            resumeInitialization.countDown()
            executor.shutdownNow()
            helper.close()
        }
    }

    @Test
    fun `helper rejects an all-zero encryption key`() {
        newHelper(ByteArray(32), mock()).use {
            assertFailsWith<IllegalArgumentException> {
                it.writableDatabase
            }
        }
    }

    @Test
    fun `database rejects an all-zero encryption key`() {
        assertFailsWith<IllegalArgumentException> {
            SQLiteDatabase.openOrCreateDatabase(
                file,
                SQLiteJournalMode.WAL.databaseConfiguration,
                ByteArray(32)
            )
        }
    }

    @Test
    fun `database cleanup failure is suppressed by initialization failure`() {
        val initializationFailure = IllegalStateException("Simulated initialization failure.")
        val callback = object : ISQLiteOpenHelper.Callback {
            override fun onConfigure(database: SQLiteDatabase) {
                SQLiteDatabaseRegistry.unregister(database)
            }

            override fun onCreate(database: SQLiteDatabase): Unit = throw initializationFailure

            override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        newHelper(ByteArray(32) { 0x42 }, callback).use {
            val thrown = assertFailsWith<IllegalStateException> {
                it.writableDatabase
            }
            assertSame(initializationFailure, thrown)
            assertEquals(1, thrown.suppressed.size)
            assertTrue(thrown.suppressed.single().message.orEmpty().contains("Failed to unregister a database"))
        }
    }

    private fun newHelper(
        key: ByteArray?,
        callback: ISQLiteOpenHelper.Callback,
        version: Int = 1
    ) = SQLiteOpenHelper(
        context = targetContext,
        configuration = ISQLiteOpenHelper.Configuration(
            callback = callback,
            key = key,
            name = file.name
        ),
        openParams = SQLiteOpenParams(journalMode = SQLiteJournalMode.WAL),
        version = version
    )

    private fun internalKeyOf(helper: SQLiteOpenHelper): ByteArray? {
        val field = SQLiteOpenHelper::class.java.getDeclaredField("_key")
        field.isAccessible = true
        return field.get(helper) as ByteArray?
    }
}
