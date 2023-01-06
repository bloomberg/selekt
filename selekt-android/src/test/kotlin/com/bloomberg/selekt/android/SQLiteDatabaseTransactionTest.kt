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

import android.content.Context
import android.database.sqlite.SQLiteException
import com.bloomberg.selekt.SQLTransactionListener
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SQLiteTransactionMode
import com.bloomberg.selekt.annotations.DelicateApi
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.io.path.createTempFile
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun createSQLiteOpenHelper(
    context: Context,
    journalMode: SQLiteJournalMode
): ISQLiteOpenHelper = SQLiteOpenHelper(
    context,
    ISQLiteOpenHelper.Configuration(
        callback = object : ISQLiteOpenHelper.Callback {
            override fun onCreate(database: SQLiteDatabase) = database.run {
                exec("CREATE TABLE 'Foo' (bar INT)")
            }

            override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        },
        key = null,
        name = "test-transactions-$journalMode"
    ),
    1,
    SQLiteOpenParams(journalMode)
)

@DelicateApi
internal class SQLiteDatabaseTransactionTest {
    private val file = createTempFile("test-transactions", ".db").toFile()

    private val targetContext = mock<Context>().apply {
        whenever(getDatabasePath(any())) doReturn file
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun isConnectionHeldByCurrentThreadInTransaction(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.run {
            transact {
                assertTrue(isConnectionHeldByCurrentThread)
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun isConnectionNotHeldByCurrentThreadOutsideTransaction(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        assertFalse(it.isConnectionHeldByCurrentThread)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun isTransactionOpenedByCurrentThreadInTransaction(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.transact {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun isTransactionNotOpenedByCurrentThreadOutsideTransaction(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        assertFalse(it.isTransactionOpenedByCurrentThread)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactDefault(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.transact {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
        assertFalse(it.isTransactionOpenedByCurrentThread)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactExclusively(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.transact(SQLiteTransactionMode.EXCLUSIVE) {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
        assertFalse(it.isTransactionOpenedByCurrentThread)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactImmediately(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.transact(SQLiteTransactionMode.IMMEDIATE) {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
        assertFalse(it.isTransactionOpenedByCurrentThread)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun setImmediateTransactionSuccessful(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.beginImmediateTransaction()
        try {
            assertTrue(it.isTransactionOpenedByCurrentThread)
            it.setTransactionSuccessful()
        } finally {
            it.endTransaction()
        }
        assertFalse(it.isTransactionOpenedByCurrentThread)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun setExclusiveTransactionSuccessful(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.beginExclusiveTransaction()
        try {
            assertTrue(it.isTransactionOpenedByCurrentThread)
            it.setTransactionSuccessful()
        } finally {
            it.endTransaction()
        }
        assertFalse(it.isTransactionOpenedByCurrentThread)
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun setTransactionSuccessfulTwiceThrows(
        input: SQLiteJournalMode
    ): Unit = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.beginExclusiveTransaction()
        it.setTransactionSuccessful()
        assertThrows<IllegalStateException> {
            it.setTransactionSuccessful()
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun vacuumInsideTransaction(
        input: SQLiteJournalMode
    ): Unit = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.transact {
            assertThrows<SQLiteException> {
                vacuum()
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun setJournalModeInsideTransaction(
        input: SQLiteJournalMode
    ): Unit = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.transact {
            assertThrows<IllegalStateException> {
                setJournalMode(if (SQLiteJournalMode.WAL == journalMode) SQLiteJournalMode.DELETE else SQLiteJournalMode.WAL)
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun setForeignKeyConstraintsEnabledInsideTransaction(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.transact {
            assertThrows<IllegalStateException> {
                setForeignKeyConstraintsEnabled(true)
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun setForeignKeyConstraintsDisabledInsideTransaction(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.transact {
            assertThrows<IllegalStateException> {
                setForeignKeyConstraintsEnabled(false)
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun throwInTransaction(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        assertThrows<IllegalStateException> {
            it.transact {
                error("Bad")
            }
        }
        it.exec("INSERT INTO Foo VALUES (?)", arrayOf(42))
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun throwInNestedTransaction(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        assertThrows<IllegalStateException> {
            it.transact {
                transact {
                    error("Bad")
                }
            }
        }
        it.exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(42))
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun interruptInTransaction(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        it.transact {
            Thread.currentThread().interrupt()
        }
        assertThrows<InterruptedException> {
            it.exec("INSERT INTO Foo VALUES (?)", arrayOf(42))
        }
        assertFalse(Thread.interrupted())
        it.exec("INSERT INTO Foo VALUES (?)", arrayOf(42))
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun exclusiveTransactionWithListenerCommit(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        val listener = mock<SQLTransactionListener>()
        it.beginExclusiveTransactionWithListener(listener)
        verify(listener, times(1)).onBegin()
        try {
            it.setTransactionSuccessful()
        } finally {
            it.endTransaction()
        }
        verify(listener, times(1)).onCommit()
        verify(listener, never()).onRollback()
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun exclusiveTransactionWithListenerRollback(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        val listener = mock<SQLTransactionListener>()
        it.beginExclusiveTransactionWithListener(listener)
        verify(listener, times(1)).onBegin()
        it.endTransaction()
        verify(listener, never()).onCommit()
        verify(listener, times(1)).onRollback()
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun immediateTransactionWithListenerCommit(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        val listener = mock<SQLTransactionListener>()
        it.beginImmediateTransactionWithListener(listener)
        verify(listener, times(1)).onBegin()
        try {
            it.setTransactionSuccessful()
        } finally {
            it.endTransaction()
        }
        verify(listener, times(1)).onCommit()
        verify(listener, never()).onRollback()
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun immediateTransactionWithListenerRollback(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        val listener = mock<SQLTransactionListener>()
        it.beginImmediateTransactionWithListener(listener)
        verify(listener, times(1)).onBegin()
        it.endTransaction()
        verify(listener, never()).onCommit()
        verify(listener, times(1)).onRollback()
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactionListenerIsEventuallyCleared(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        val listener = mock<SQLTransactionListener>()
        it.beginExclusiveTransactionWithListener(listener)
        it.endTransaction()
        it.beginExclusiveTransaction()
        verify(listener, times(1)).onBegin()
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactionListenerNotifiedOnceInNestedTransactions(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        val listener = mock<SQLTransactionListener>()
        it.beginExclusiveTransactionWithListener(listener)
        it.transact { }
        verify(listener, times(1)).onBegin()
        try {
            it.setTransactionSuccessful()
        } finally {
            it.endTransaction()
        }
        verify(listener, times(1)).onCommit()
        verify(listener, never()).onRollback()
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactionListenerNotifiedOnceInNestedTransactionRollback(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        val listener = mock<SQLTransactionListener>()
        it.beginExclusiveTransactionWithListener(listener)
        it.transact { }
        verify(listener, times(1)).onBegin()
        it.endTransaction()
        verify(listener, never()).onCommit()
        verify(listener, times(1)).onRollback()
    }

    @ParameterizedTest
    @EnumSource(value = SQLiteJournalMode::class, names = ["DELETE", "WAL"])
    fun transactionWithListenerWillRollbackWithOnBeginThrow(
        input: SQLiteJournalMode
    ) = createSQLiteOpenHelper(targetContext, input).writableDatabase.destroy {
        val listener = mock<SQLTransactionListener>().apply {
            whenever(onBegin()) doThrow IllegalStateException("Bad")
        }
        assertThrows<IllegalStateException> {
            it.beginExclusiveTransactionWithListener(listener)
        }
        verify(listener, times(1)).onRollback()
    }
}
