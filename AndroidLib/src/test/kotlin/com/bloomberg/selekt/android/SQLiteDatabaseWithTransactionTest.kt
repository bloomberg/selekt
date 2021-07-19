/*
 * Copyright 2021 Bloomberg Finance L.P.
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
import com.bloomberg.selekt.Experimental
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SQLiteTransactionMode
import com.bloomberg.selekt.android.SQLiteDatabase.Companion.deleteDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(Experimental::class, ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class SQLiteDatabaseWithTransactionTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    private val dispatcher = TestCoroutineDispatcher()
    private val scope = TestCoroutineScope(dispatcher)

    private val file = File.createTempFile("test-with-transaction", ".db").also { it.deleteOnExit() }
    private val database = SQLiteDatabase.openOrCreateDatabase(file, SQLiteJournalMode.WAL.databaseConfiguration,
        ByteArray(32) { 0x42 })

    @After
    fun tearDown() {
        database.run {
            try {
                close()
                assertFalse(isOpen)
            } finally {
                assertTrue(deleteDatabase(file))
            }
        }
    }

    @Test
    fun withTransactionDefault(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
    }

    @Test
    fun withTransactionExclusively(): Unit = scope.runBlockingTest {
        database.withTransaction(SQLiteTransactionMode.EXCLUSIVE, dispatcher) {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
    }

    @Test
    fun withTransactionImmediately(): Unit = scope.runBlockingTest {
        database.withTransaction(SQLiteTransactionMode.IMMEDIATE, dispatcher) {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
    }

    @Test(expected = CancellationException::class)
    fun withTransactionIsCancellable(): Unit = scope.runBlockingTest {
        cancel("Test cancel!")
        database.withTransaction(dispatcher = Dispatchers.IO) { }
    }

    @Test
    fun delayTransactionDefault(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            delayTransaction()
        }
        assertFalse(database.isTransactionOpenedByCurrentThread)
    }

    @Test
    fun delayTransaction(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            delayTransaction(50L)
        }
        assertFalse(database.isTransactionOpenedByCurrentThread)
    }

    @Test
    fun delayInTransaction(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            delay(50L)
        }
        assertFalse(database.isTransactionOpenedByCurrentThread)
    }

    @Test
    fun sequentialWithTransaction(): Unit = scope.runBlockingTest {
        repeat(10) {
            database.withTransaction(dispatcher = dispatcher) { }
        }
        assertFalse(database.isTransactionOpenedByCurrentThread)
    }

    @Test
    fun nestedTransaction(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            withTransaction(dispatcher = dispatcher) { }
        }
        assertFalse(database.isTransactionOpenedByCurrentThread)
    }

    @Test
    fun multiNestedTransaction(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            withTransaction(dispatcher = dispatcher) {
                withTransaction(dispatcher = dispatcher) { }
            }
        }
        assertFalse(database.isTransactionOpenedByCurrentThread)
    }

    @Test
    fun nestedSequentialWithTransaction(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            repeat(10) {
                database.withTransaction(dispatcher = dispatcher) { }
            }
        }
        assertFalse(database.isTransactionOpenedByCurrentThread)
    }

    @Test
    fun delayNestedTransaction(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            withTransaction(dispatcher = dispatcher) {
                delayTransaction()
            }
            assertTrue(isTransactionOpenedByCurrentThread)
        }
        assertFalse(database.isTransactionOpenedByCurrentThread)
    }

    @Test
    fun withTransactionInsert(): Unit = scope.runBlockingTest {
        database.exec("CREATE TABLE Foo (bar TEXT)")
        withContext(dispatcher) {
            database.withTransaction(dispatcher = dispatcher) {
                insert("Foo", ContentValues().apply { put("bar", "xyz") }, ConflictAlgorithm.FAIL)
            }
        }
        database.query("SELECT * FROM Foo", emptyArray()).use {
            assertTrue(it.moveToNext())
            assertEquals("xyz", it.getString(0))
        }
    }

    @Test
    fun nestedWithTransactionInsert(): Unit = scope.runBlockingTest {
        database.exec("CREATE TABLE Foo (bar TEXT)")
        withContext(dispatcher) {
            database.withTransaction(dispatcher = dispatcher) {
                withTransaction(dispatcher = dispatcher) {
                    insert("Foo", ContentValues().apply { put("bar", "xyz") }, ConflictAlgorithm.FAIL)
                }
            }
        }
        database.query("SELECT * FROM Foo", emptyArray()).use {
            assertTrue(it.moveToNext())
            assertEquals("xyz", it.getString(0))
        }
    }

    @Test
    fun transactThenWithTransaction(): Unit = scope.runBlockingTest {
        database.apply {
            @Suppress("BlockingMethodInNonBlockingContext") // Test purpose.
            beginExclusiveTransaction()
            try {
                withTransaction(dispatcher = dispatcher) { }
                assertTrue(isConnectionHeldByCurrentThread)
                assertTrue(isTransactionOpenedByCurrentThread)
                setTransactionSuccessful()
            } finally {
                endTransaction()
            }
            assertFalse(isConnectionHeldByCurrentThread)
            assertFalse(isTransactionOpenedByCurrentThread)
        }
    }

    @Test(expected = IOException::class)
    fun withTransactionPropagatesException(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            throw IOException("Uh-oh")
        }
    }

    @Test(expected = IOException::class)
    fun nestedWithTransactionPropagatesException(): Unit = scope.runBlockingTest {
        database.withTransaction(dispatcher = dispatcher) {
            withTransaction(dispatcher = dispatcher) {
                throw IOException("Uh-oh")
            }
        }
    }

    @Test
    fun withTransactionPreservesTransactionOnException(): Unit = scope.runBlockingTest {
        @Suppress("BlockingMethodInNonBlockingContext") // Test purpose.
        database.beginExclusiveTransaction()
        try {
            database.withTransaction(dispatcher = dispatcher) {
                throw IOException("Uh-oh")
            }
        } catch (_: IOException) { }
        assertTrue(database.isTransactionOpenedByCurrentThread)
    }

    @Test
    fun compileStatementWithTransaction(): Unit = scope.runBlockingTest {
        database.exec("CREATE TABLE Foo (bar TEXT)")
        val statement = database.withTransaction(dispatcher = dispatcher) {
            compileStatement("INSERT INTO Foo VALUES ('abc')")
        }
        assertEquals(1L, statement.executeInsert())
        database.query("SELECT * FROM Foo", null).use {
            assertTrue(it.moveToFirst())
            assertEquals("abc", it.getString(0))
            assertFalse(it.moveToNext())
        }
    }
}
