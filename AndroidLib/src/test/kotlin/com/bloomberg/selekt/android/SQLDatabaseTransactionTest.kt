/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.android

import android.database.SQLException
import com.bloomberg.selekt.commons.deleteDatabase
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLTransactionListener
import com.bloomberg.selekt.SQLiteJournalMode
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

data class SQLTransactionTestInputs(
    val journalMode: SQLiteJournalMode
) {
    override fun toString() = "$journalMode"
}

@RunWith(Parameterized::class)
internal class SQLDatabaseTransactionTest(inputs: SQLTransactionTestInputs) {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    private val file = File.createTempFile("test-transactions", ".db").also { it.deleteOnExit() }

    private val database = SQLDatabase(file.absolutePath, SQLite, inputs.journalMode.databaseConfiguration, key = null)

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun initParameters(): Iterable<SQLTransactionTestInputs> = arrayOf(
            SQLiteJournalMode.DELETE,
            SQLiteJournalMode.WAL
        ).map { SQLTransactionTestInputs(it) }
    }

    @Before
    fun setUp() {
        database.exec("CREATE TABLE 'Foo' (bar INT)")
    }

    @After
    fun tearDown() {
        database.run {
            try {
                close()
                assertFalse(isOpen())
            } finally {
                assertTrue(deleteDatabase(file))
            }
        }
    }

    @Test
    fun rollbackOnSQLException(): Unit = database.run {
        val result = runCatching {
            transact {
                insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
                throw SQLException()
            }
        }
        assertTrue(result.isFailure)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertFalse(it.moveToFirst())
            assertEquals(0, it.count)
        }
    }

    @Test
    fun rollbackOnNestedSQLException(): Unit = database.run {
        val result = runCatching {
            transact {
                insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
                transact {
                    throw SQLException()
                }
            }
        }
        assertTrue(result.isFailure)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertFalse(it.moveToFirst())
            assertEquals(0, it.count)
        }
    }

    @Test
    fun rollbackOnIllegalStateException(): Unit = database.run {
        val result = runCatching {
            transact {
                insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
                error("Something bad just happened!")
            }
        }
        assertTrue(result.isFailure)
        query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
            assertFalse(it.moveToFirst())
            assertEquals(0, it.count)
        }
    }

    @Test
    fun inTransaction(): Unit = database.run {
        transact { assertTrue(inTransaction) }
    }

    @Test
    fun outOfTransaction(): Unit = database.run {
        assertFalse(inTransaction)
    }

    @Test
    fun transactionWithResult(): Unit = database.run {
        val result = Any()
        assertSame(result, transact { result })
    }

    @Test
    fun rawBeginAndRawEnd(): Unit = database.run {
        exec("BEGIN")
        assertTrue(inTransaction)
        exec("END")
        assertFalse(inTransaction)
    }

    @Test
    fun rawBeginImmediateAndRawEnd(): Unit = database.run {
        exec("BEGIN IMMEDIATE")
        assertTrue(inTransaction)
        exec("END")
        assertFalse(inTransaction)
    }

    @Test
    fun rawNested(): Unit = database.run {
        exec("BEGIN")
        assertTrue(inTransaction)
        exec("BEGIN")
        assertTrue(inTransaction)
        exec("END")
        assertTrue(inTransaction)
        exec("END")
        assertFalse(inTransaction)
    }

    @Test
    fun rawBeginThenEnd(): Unit = database.run {
        exec("BEGIN")
        assertTrue(inTransaction)
        setTransactionSuccessful()
        endTransaction()
        assertFalse(inTransaction)
    }

    @Test
    fun rawBeginRollback(): Unit = database.run {
        exec("BEGIN")
        assertTrue(inTransaction)
        exec("ROLLBACK")
        assertFalse(inTransaction)
    }

    @Test
    fun beginImmediateThenRawEnd(): Unit = database.run {
        beginImmediateTransaction()
        assertTrue(inTransaction)
        exec("END")
        assertFalse(inTransaction)
    }

    @Test
    fun beginExclusiveThenRawEnd(): Unit = database.run {
        beginExclusiveTransaction()
        assertTrue(inTransaction)
        exec("END")
        assertFalse(inTransaction)
    }

    @Test
    fun rawBeginBeginRollbackThenEnd(): Unit = database.run {
        exec("BEGIN")
        exec("BEGIN")
        exec("ROLLBACK")
        assertTrue(inTransaction)
        endTransaction()
        assertFalse(inTransaction)
    }

    @Test
    fun connectionHeldInTransaction(): Unit = database.run {
        transact { assertTrue(inTransaction) }
    }

    @Test
    fun connectionNotHeldOutOfTransaction(): Unit = database.run {
        assertFalse(isCurrentThreadSessionActive)
    }

    @Test(expected = IllegalStateException::class)
    fun earlyEnd(): Unit = database.run {
        endTransaction()
    }

    @Test
    fun transactionOnlyRead(): Unit = database.run {
        transact {
            repeat(3) {
                query("SELECT * FROM Foo", emptyArray())
            }
        }
    }

    @Test
    fun transactionReadThenWrite(): Unit = database.run {
        transact {
            query("SELECT * FROM Foo", emptyArray())
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        }
    }

    @Test
    fun yieldTransactionBetweenInserts(): Unit = database.run {
        transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            yieldTransaction()
            insert("Foo", ContentValues().apply { put("bar", 43) }, ConflictAlgorithm.REPLACE)
        }
        query("SELECT * FROM Foo", emptyArray()).use {
            assertEquals(2, it.count)
        }
    }

    @Test
    fun yieldNestedTransactionBetweenInserts(): Unit = database.run {
        transact { transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            yieldTransaction()
            insert("Foo", ContentValues().apply { put("bar", 43) }, ConflictAlgorithm.REPLACE)
        } }
        query("SELECT * FROM Foo", emptyArray()).use {
            assertEquals(2, it.count)
        }
    }

    @Test
    fun yieldTransactionWithPauseBetweenInserts(): Unit = database.run {
        val pauseMillis = 100L
        transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            val now = System.currentTimeMillis()
            yieldTransaction(pauseMillis)
            assertTrue(System.currentTimeMillis() >= pauseMillis + now)
            insert("Foo", ContentValues().apply { put("bar", 43) }, ConflictAlgorithm.REPLACE)
        }
        query("SELECT * FROM Foo", emptyArray()).use {
            assertEquals(2, it.count)
        }
    }

    @Test
    fun yieldTransactionCommits(): Unit = database.run {
        transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            yieldTransaction()
            query("SELECT * FROM Foo", emptyArray()).use {
                assertEquals(1, it.count)
            }
        }
    }

    @Test
    fun yieldTransactionAllowsAnotherTransactionToProgress(): Unit = database.run {
        transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            val latch = CountDownLatch(1)
            thread {
                latch.countDown()
                transact {
                    insert("Foo", ContentValues().apply { put("bar", 43) }, ConflictAlgorithm.REPLACE)
                }
            }
            latch.await()
            yieldTransaction(100L)
            query("SELECT * FROM Foo", emptyArray()).use {
                assertEquals(2, it.count)
                arrayOf(42, 43).forEach { x ->
                    assertTrue(it.moveToNext())
                    assertEquals(x, it.getInt(0))
                }
            }
        }
    }

    @Test
    fun yieldTransactionAllowsAnotherMode(): Unit = database.run {
        transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            yieldTransaction()
        }
        beginExclusiveTransaction()
        try {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
        query("SELECT * FROM Foo", emptyArray()).use {
            assertEquals(2, it.count)
        }
    }

    @Test
    fun yieldTransactionThrows(): Unit = database.run {
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            yieldTransaction()
        }
    }

    @Test
    fun yieldTransactionWithPauseThrows(): Unit = database.run {
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            yieldTransaction(100L)
        }
    }

    @Test
    fun yieldTransactionRestoresTransactionListener(): Unit = database.run {
        val listener = mock<SQLTransactionListener>()
        beginExclusiveTransactionWithListener(listener)
        yieldTransaction()
        setTransactionSuccessful()
        endTransaction()
        verify(listener, times(2)).onCommit()
        verify(listener, never()).onRollback()
    }

    @Test
    fun yieldTransactionMultipleRestoresTransactionListener(): Unit = database.run {
        val listener = mock<SQLTransactionListener>()
        beginExclusiveTransactionWithListener(listener)
        repeat(100) {
            yieldTransaction()
        }
        setTransactionSuccessful()
        endTransaction()
        verify(listener, times(101)).onCommit()
        verify(listener, never()).onRollback()
    }
}
