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

import android.database.SQLException
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLTransactionListener
import com.bloomberg.selekt.SQLiteJournalMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.io.path.createTempFile
import kotlin.test.assertFailsWith

private fun createFile(
    input: SQLiteJournalMode
) = createTempFile("test-transactions-${input.name}-", ".db").toFile().apply { deleteOnExit() }

internal class SQLDatabaseTransactionTest {
    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun rollbackOnSQLException(
        input: SQLiteJournalMode
    ) = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        val result = runCatching {
            it.transact {
                insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
                throw SQLException()
            }
        }
        assertTrue(result.isFailure)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertFalse(cursor.moveToFirst())
            assertEquals(0, cursor.count)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun rollbackOnNestedSQLException(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        val result = runCatching {
            it.transact {
                insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
                transact {
                    throw SQLException()
                }
            }
        }
        assertTrue(result.isFailure)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertFalse(cursor.moveToFirst())
            assertEquals(0, cursor.count)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun rollbackOnIllegalStateException(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        val result = runCatching {
            it.transact {
                insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
                error("Something bad just happened!")
            }
        }
        assertTrue(result.isFailure)
        it.query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
            assertFalse(cursor.moveToFirst())
            assertEquals(0, cursor.count)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun inTransaction(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.transact { assertTrue(inTransaction) }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun outOfTransaction(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        assertFalse(it.inTransaction)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactionWithResult(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        val result = Any()
        assertSame(result, it.transact { result })
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun rawBeginAndRawEnd(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("BEGIN")
        assertTrue(it.inTransaction)
        it.exec("END")
        assertFalse(it.inTransaction)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun rawBeginImmediateAndRawEnd(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("BEGIN IMMEDIATE")
        assertTrue(it.inTransaction)
        it.exec("END")
        assertFalse(it.inTransaction)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun rawNested(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("BEGIN")
        assertTrue(it.inTransaction)
        it.exec("BEGIN")
        assertTrue(it.inTransaction)
        it.exec("END")
        assertTrue(it.inTransaction)
        it.exec("END")
        assertFalse(it.inTransaction)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun rawBeginThenEnd(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("BEGIN")
        assertTrue(it.inTransaction)
        it.setTransactionSuccessful()
        it.endTransaction()
        assertFalse(it.inTransaction)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun rawBeginRollback(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("BEGIN")
        assertTrue(it.inTransaction)
        it.exec("ROLLBACK")
        assertFalse(it.inTransaction)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun beginImmediateThenRawEnd(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.beginImmediateTransaction()
        assertTrue(it.inTransaction)
        it.exec("END")
        assertFalse(it.inTransaction)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun beginExclusiveThenRawEnd(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.beginExclusiveTransaction()
        assertTrue(it.inTransaction)
        it.exec("END")
        assertFalse(it.inTransaction)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun rawBeginBeginRollbackThenEnd(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("BEGIN")
        it.exec("BEGIN")
        it.exec("ROLLBACK")
        assertTrue(it.inTransaction)
        it.endTransaction()
        assertFalse(it.inTransaction)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun connectionHeldInTransaction(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.transact { assertTrue(inTransaction) }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun connectionNotHeldOutOfTransaction(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        assertFalse(it.isCurrentThreadSessionActive)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun earlyEnd(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        assertFailsWith<IllegalStateException> {
            it.endTransaction()
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactionOnlyRead(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.transact {
            repeat(3) {
                query("SELECT * FROM Foo", emptyArray())
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactionReadThenWrite(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.transact {
            query("SELECT * FROM Foo", emptyArray())
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionBetweenInserts(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            yieldTransaction()
            insert("Foo", ContentValues().apply { put("bar", 43) }, ConflictAlgorithm.REPLACE)
        }
        it.query("SELECT * FROM Foo", emptyArray()).use { cursor ->
            assertEquals(2, cursor.count)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldNestedTransactionBetweenInserts(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.transact { transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            yieldTransaction()
            insert("Foo", ContentValues().apply { put("bar", 43) }, ConflictAlgorithm.REPLACE)
        } }
        it.query("SELECT * FROM Foo", emptyArray()).use { cursor ->
            assertEquals(2, cursor.count)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionWithPauseBetweenInserts(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        val pauseMillis = 100L
        it.transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            val now = System.currentTimeMillis()
            yieldTransaction(pauseMillis)
            assertTrue(System.currentTimeMillis() >= pauseMillis + now)
            insert("Foo", ContentValues().apply { put("bar", 43) }, ConflictAlgorithm.REPLACE)
        }
        it.query("SELECT * FROM Foo", emptyArray()).use { cursor ->
            assertEquals(2, cursor.count)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionCommits(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            yieldTransaction()
            query("SELECT * FROM Foo", emptyArray()).use { cursor ->
                assertEquals(1, cursor.count)
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionAllowsAnotherTransactionToProgress(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.transact {
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
            query("SELECT * FROM Foo", emptyArray()).use { cursor ->
                assertEquals(2, cursor.count)
                arrayOf(42, 43).forEach { x ->
                    assertTrue(cursor.moveToNext())
                    assertEquals(x, cursor.getInt(0))
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionAllowsAnotherMode(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.exec("CREATE TABLE 'Foo' (bar INT)")
        it.transact {
            insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            yieldTransaction()
        }
        it.beginExclusiveTransaction()
        try {
            it.insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
            it.setTransactionSuccessful()
        } finally {
            it.endTransaction()
        }
        it.query("SELECT * FROM Foo", emptyArray()).use { cursor ->
            assertEquals(2, cursor.count)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionThrows(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        assertFailsWith<IllegalStateException> {
            it.yieldTransaction()
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionThrowsIfTransactionMarkedAsSuccessful(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        it.beginExclusiveTransaction()
        try {
            it.setTransactionSuccessful()
            assertFailsWith<IllegalStateException> {
                it.yieldTransaction()
            }
        } finally {
            it.endTransaction()
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionWithPauseThrows(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        assertFailsWith<IllegalStateException> {
            it.yieldTransaction(100L)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionRestoresTransactionListener(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        val listener = mock<SQLTransactionListener>()
        it.beginExclusiveTransactionWithListener(listener)
        it.yieldTransaction()
        it.setTransactionSuccessful()
        it.endTransaction()
        verify(listener, times(2)).onCommit()
        verify(listener, never()).onRollback()
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun yieldTransactionMultipleRestoresTransactionListener(
        input: SQLiteJournalMode
    ): Unit = SQLDatabase(createFile(input).absolutePath, SQLite, input.databaseConfiguration, key = null).use {
        val listener = mock<SQLTransactionListener>()
        it.beginExclusiveTransactionWithListener(listener)
        repeat(100) { _ ->
            it.yieldTransaction()
        }
        it.setTransactionSuccessful()
        it.endTransaction()
        verify(listener, times(101)).onCommit()
        verify(listener, never()).onRollback()
    }
}
