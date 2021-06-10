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

import android.content.Context
import android.database.sqlite.SQLiteException
import com.bloomberg.selekt.SQLTransactionListener
import com.bloomberg.selekt.commons.deleteDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SQLiteTransactionMode
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun createSQLiteOpenHelper(
    context: Context,
    inputs: TransactionTestInputs
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
        name = "test-transactions"
    ),
    1,
    SQLiteOpenParams(inputs.journalMode)
)

internal data class TransactionTestInputs(
    val journalMode: SQLiteJournalMode
) {
    override fun toString() = "$journalMode"
}

@RunWith(Parameterized::class)
internal class SQLiteDatabaseTransactionTest(inputs: TransactionTestInputs) {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun initParameters(): Iterable<TransactionTestInputs> = arrayOf(
            SQLiteJournalMode.DELETE,
            SQLiteJournalMode.WAL
        ).map { TransactionTestInputs(it) }
    }

    private val file = File.createTempFile("test-transactions", ".db").also { it.deleteOnExit() }

    private val targetContext = mock<Context>().apply {
        whenever(getDatabasePath(any())) doReturn file
    }
    private val databaseHelper = createSQLiteOpenHelper(targetContext, inputs)

    @After
    fun tearDown() {
        databaseHelper.writableDatabase.run {
            try {
                close()
                assertFalse(isOpen)
            } finally {
                assertTrue(deleteDatabase(file))
            }
        }
    }

    @Test
    fun isConnectionHeldByCurrentThreadInTransaction() = databaseHelper.writableDatabase.run {
        transact {
            assertTrue(isConnectionHeldByCurrentThread)
        }
    }

    @Test
    fun isConnectionNotHeldByCurrentThreadOutsideTransaction() = databaseHelper.writableDatabase.run {
        assertFalse(isConnectionHeldByCurrentThread)
    }

    @Test
    fun isTransactionOpenedByCurrentThreadInTransaction() = databaseHelper.writableDatabase.run {
        transact {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
    }

    @Test
    fun isTransactionNotOpenedByCurrentThreadOutsideTransaction() = databaseHelper.writableDatabase.run {
        assertFalse(isTransactionOpenedByCurrentThread)
    }

    @Test
    fun transactDefault() = databaseHelper.writableDatabase.run {
        transact {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
        assertFalse(isTransactionOpenedByCurrentThread)
    }

    @Test
    fun transactExclusively() = databaseHelper.writableDatabase.run {
        transact(SQLiteTransactionMode.EXCLUSIVE) {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
        assertFalse(isTransactionOpenedByCurrentThread)
    }

    @Test
    fun transactImmediately() = databaseHelper.writableDatabase.run {
        transact(SQLiteTransactionMode.IMMEDIATE) {
            assertTrue(isTransactionOpenedByCurrentThread)
        }
        assertFalse(isTransactionOpenedByCurrentThread)
    }

    @Test
    fun setImmediateTransactionSuccessful() = databaseHelper.writableDatabase.run {
        beginImmediateTransaction()
        try {
            assertTrue(isTransactionOpenedByCurrentThread)
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
        assertFalse(isTransactionOpenedByCurrentThread)
    }

    @Test
    fun setExclusiveTransactionSuccessful() = databaseHelper.writableDatabase.run {
        beginExclusiveTransaction()
        try {
            assertTrue(isTransactionOpenedByCurrentThread)
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
        assertFalse(isTransactionOpenedByCurrentThread)
    }

    @Test
    fun setTransactionSuccessfulTwiceThrows(): Unit = databaseHelper.writableDatabase.run {
        beginExclusiveTransaction()
        setTransactionSuccessful()
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            setTransactionSuccessful()
        }
    }

    @Test(expected = SQLiteException::class)
    fun vacuumInsideTransaction() = databaseHelper.writableDatabase.run { transact { vacuum() } }

    @Test(expected = IllegalStateException::class)
    fun setJournalModeInsideTransaction(): Unit = databaseHelper.writableDatabase.run {
        transact {
            setJournalMode(if (SQLiteJournalMode.WAL == journalMode) SQLiteJournalMode.DELETE else SQLiteJournalMode.WAL)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun setForeignKeyConstraintsEnabledInsideTransaction() = databaseHelper.writableDatabase.run {
        transact {
            setForeignKeyConstraintsEnabled(true)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun setForeignKeyConstraintsDisabledInsideTransaction() = databaseHelper.writableDatabase.run {
        transact {
            setForeignKeyConstraintsEnabled(false)
        }
    }

    @Test
    fun throwInTransaction() = databaseHelper.writableDatabase.run {
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            transact {
                error("Bad")
            }
        }
        exec("INSERT INTO Foo VALUES (?)", arrayOf(42))
    }

    @Test
    fun throwInNestedTransaction() = databaseHelper.writableDatabase.run {
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            transact {
                transact {
                    error("Bad")
                }
            }
        }
        exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(42))
    }

    @Test
    fun interruptInTransaction() = databaseHelper.writableDatabase.run {
        transact {
            Thread.currentThread().interrupt()
        }
        assertThatExceptionOfType(InterruptedException::class.java).isThrownBy {
            exec("INSERT INTO Foo VALUES (?)", arrayOf(42))
        }
        assertFalse(Thread.interrupted())
        exec("INSERT INTO Foo VALUES (?)", arrayOf(42))
    }

    @Test
    fun exclusiveTransactionWithListenerCommit() = databaseHelper.writableDatabase.run {
        val listener = mock<SQLTransactionListener>()
        beginExclusiveTransactionWithListener(listener)
        verify(listener, times(1)).onBegin()
        try {
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
        verify(listener, times(1)).onCommit()
        verify(listener, never()).onRollback()
    }

    @Test
    fun exclusiveTransactionWithListenerRollback() = databaseHelper.writableDatabase.run {
        val listener = mock<SQLTransactionListener>()
        beginExclusiveTransactionWithListener(listener)
        verify(listener, times(1)).onBegin()
        endTransaction()
        verify(listener, never()).onCommit()
        verify(listener, times(1)).onRollback()
    }

    @Test
    fun immediateTransactionWithListenerCommit() = databaseHelper.writableDatabase.run {
        val listener = mock<SQLTransactionListener>()
        beginImmediateTransactionWithListener(listener)
        verify(listener, times(1)).onBegin()
        try {
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
        verify(listener, times(1)).onCommit()
        verify(listener, never()).onRollback()
    }

    @Test
    fun immediateTransactionWithListenerRollback() = databaseHelper.writableDatabase.run {
        val listener = mock<SQLTransactionListener>()
        beginImmediateTransactionWithListener(listener)
        verify(listener, times(1)).onBegin()
        endTransaction()
        verify(listener, never()).onCommit()
        verify(listener, times(1)).onRollback()
    }

    @Test
    fun transactionListenerIsEventuallyCleared() = databaseHelper.writableDatabase.run {
        val listener = mock<SQLTransactionListener>()
        beginExclusiveTransactionWithListener(listener)
        endTransaction()
        beginExclusiveTransaction()
        verify(listener, times(1)).onBegin()
    }

    @Test
    fun transactionListenerNotifiedOnceInNestedTransactions() = databaseHelper.writableDatabase.run {
        val listener = mock<SQLTransactionListener>()
        beginExclusiveTransactionWithListener(listener)
        transact { }
        verify(listener, times(1)).onBegin()
        try {
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
        verify(listener, times(1)).onCommit()
        verify(listener, never()).onRollback()
    }

    @Test
    fun transactionListenerNotifiedOnceInNestedTransactionRollback() = databaseHelper.writableDatabase.run {
        val listener = mock<SQLTransactionListener>()
        beginExclusiveTransactionWithListener(listener)
        transact { }
        verify(listener, times(1)).onBegin()
        endTransaction()
        verify(listener, never()).onCommit()
        verify(listener, times(1)).onRollback()
    }

    @Test
    fun transactionWithListenerWillRollbackWithOnBeginThrow() = databaseHelper.writableDatabase.run {
        val listener = mock<SQLTransactionListener>().apply {
            whenever(onBegin()) doThrow IllegalStateException("Bad")
        }
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            beginExclusiveTransactionWithListener(listener)
        }
        verify(listener, times(1)).onRollback()
    }
}
