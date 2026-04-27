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

package com.bloomberg.selekt

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify

private val databaseConfiguration = DatabaseConfiguration(
    busyTimeoutMillis = 2_000,
    evictionDelayMillis = 5_000L,
    maxConnectionPoolSize = 1,
    maxSqlCacheSize = 5,
    timeBetweenEvictionRunsMillis = 5_000L
)

internal class SQLDatabaseTest {
    @Mock lateinit var sqlite: SQLite

    private lateinit var database: SQLDatabase

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(sqlite.withScopedArena(any<() -> Any?>())) doAnswer {
            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as () -> Any?).invoke()
        }
        whenever(sqlite.openV2(any(), any(), any())) doAnswer {
            requireNotNull(it.arguments[2] as? LongArray)[0] = DB
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())) doAnswer {
            requireNotNull(it.arguments[2] as? LongArray)[0] = STMT
            0
        }
        whenever(sqlite.stepWithoutThrowing(any())) doReturn SQL_DONE
        whenever(sqlite.getAutocommit(any())) doReturn 1
        database = SQLDatabase("file::memory:", sqlite, databaseConfiguration, null)
    }

    @AfterEach
    fun tearDown() {
        database.run {
            close()
            assertFalse(isOpen())
        }
    }

    @Test
    fun nestedTransaction() = database.run {
        transact { transact { } }
    }.also { verifyCommit() }

    @Test
    fun nestedTransactions() = database.run {
        transact { transact { transact { } } }
    }.also { verifyCommit() }

    @Test
    fun badNestedTransactionThenGoodTransaction() {
        assertFailsWith<Exception> {
            database.apply {
                transact { transact { error("uh-oh") } }
            }
        }
        verifyRollback()
        database.transact { }
        verifyCommit()
    }

    @Test
    fun isOpen() {
        assertTrue(database.isOpen())
    }

    @Test
    fun execAfterDatabaseHasClosed() {
        database.run {
            close()
            assertFailsWith<IllegalStateException> {
                exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            }
        }
    }

    @Test
    fun insertVerifiesValues(): Unit = database.run {
        assertFailsWith<IllegalArgumentException> {
            insert("Foo", ContentValues(), ConflictAlgorithm.REPLACE)
        }
    }

    @Test
    fun interrupt() {
        database.transact { }
        database.interrupt()
        verify(sqlite, times(1)).interrupt(eq(DB))
    }

    @Test
    fun isInterruptedFalse() {
        database.transact { }
        whenever(sqlite.isInterrupted(any())) doReturn false
        assertFalse(database.isInterrupted)
    }

    @Test
    fun isInterruptedTrue() {
        database.transact { }
        whenever(sqlite.isInterrupted(any())) doReturn true
        assertTrue(database.isInterrupted)
    }

    @Test
    fun setProgressHandler() {
        database.transact { }
        val handler = SQLProgressHandler { 0 }
        database.setProgressHandler(100, handler)
        verify(sqlite, times(1)).progressHandler(eq(DB), eq(100), eq(handler))
    }

    @Test
    fun clearProgressHandler() {
        database.transact { }
        database.setProgressHandler(0, null)
        verify(sqlite, times(1)).progressHandler(eq(DB), eq(0), isNull())
    }

    @Test
    fun updateVerifiesValues(): Unit = database.run {
        assertFailsWith<IllegalArgumentException> {
            update("Foo", ContentValues(), "", emptyArray(), ConflictAlgorithm.REPLACE)
        }
    }

    @Test
    fun pragmaRejectsUnknownKey(): Unit = database.run {
        assertFailsWith<IllegalArgumentException> {
            pragma("malicious; DROP TABLE foo --")
        }
    }

    @Test
    fun pragmaRejectsArbitraryString(): Unit = database.run {
        assertFailsWith<IllegalArgumentException> {
            pragma("not_a_real_pragma")
        }
    }

    @Test
    fun pragmaWithValueRejectsUnknownKey(): Unit = database.run {
        assertFailsWith<IllegalArgumentException> {
            pragma("evil_pragma", 42)
        }
    }

    @Test
    fun pragmaAcceptsAllowListedKey(): Unit = database.run {
        whenever(sqlite.columnText(any(), any())) doReturn ""
        SQLitePragma.entries.forEach { pragma(it) }
    }

    @Test
    fun pragmaAcceptsEnumKey(): Unit = database.run {
        whenever(sqlite.columnText(any(), any())) doReturn "wal"
        pragma(SQLitePragma.JOURNAL_MODE)
    }

    @Test
    fun pragmaAcceptsIncrementalVacuumWithArgument(): Unit = database.run {
        whenever(sqlite.columnText(any(), any())) doReturn ""
        pragma("incremental_vacuum(100)")
    }

    @Test
    fun pragmaAcceptsSchemaPrefixedKey(): Unit = database.run {
        whenever(sqlite.columnText(any(), any())) doReturn "ok"
        pragma("main.integrity_check")
    }

    @Test
    fun pragmaValueRejectsSqlInjection(): Unit = database.run {
        assertFailsWith<IllegalArgumentException> {
            pragma(SQLitePragma.JOURNAL_MODE, "wal; DROP TABLE foo--")
        }
    }

    @Test
    fun pragmaValueRejectsSemiColon(): Unit = database.run {
        assertFailsWith<IllegalArgumentException> {
            pragma(SQLitePragma.JOURNAL_MODE, "wal;")
        }
    }

    @Test
    fun pragmaValueRejectsEmptyString(): Unit = database.run {
        assertFailsWith<IllegalArgumentException> {
            pragma(SQLitePragma.JOURNAL_MODE, "")
        }
    }

    @Test
    fun pragmaValueAcceptsAlphanumeric(): Unit = database.run {
        whenever(sqlite.columnText(any(), any())) doReturn "wal"
        pragma(SQLitePragma.JOURNAL_MODE, "wal")
    }

    @Test
    fun pragmaValueAcceptsInteger(): Unit = database.run {
        whenever(sqlite.columnText(any(), any())) doReturn "1"
        pragma(SQLitePragma.JOURNAL_MODE, 1)
    }

    @Test
    fun pragmaValueAcceptsNegativeInteger(): Unit = database.run {
        whenever(sqlite.columnText(any(), any())) doReturn "-1"
        pragma(SQLitePragma.SOFT_HEAP_LIMIT, -1)
    }

    private fun verifyCommit(): Unit = inOrder(sqlite).run {
        verify(sqlite, times(1)).prepareV2(eq(DB), eq("END"), any())
        verify(sqlite, times(1)).stepWithoutThrowing(eq(STMT))
    }

    private fun verifyRollback(): Unit = inOrder(sqlite) {
        verify(sqlite, times(1)).prepareV2(eq(DB), eq("ROLLBACK"), any())
        verify(sqlite, times(1)).step(eq(STMT))
    }

    @Test
    fun queryWithAlreadyCancelledSignalThrows() {
        val signal = CancellationSignal()
        signal.cancel()
        assertFailsWith<OperationCancelledException> {
            database.query("SELECT 1", emptyArray(), signal)
        }
    }

    @Test
    fun queryWithCancellationSignalSetsProgressHandler() {
        database.transact { }
        val signal = CancellationSignal(500)
        whenever(sqlite.columnCount(any())) doReturn 0
        whenever(sqlite.step(any())) doReturn SQL_DONE
        whenever(sqlite.statementReadOnly(any())) doReturn 1
        database.query("SELECT 1", emptyArray(), signal)
        verify(sqlite, times(1)).progressHandler(eq(DB), eq(500), any())
    }

    @Test
    fun queryWithCancellationSignalClearsProgressHandler() {
        database.transact { }
        val signal = CancellationSignal(500)
        whenever(sqlite.columnCount(any())) doReturn 0
        whenever(sqlite.step(any())) doReturn SQL_DONE
        whenever(sqlite.statementReadOnly(any())) doReturn 1
        database.query("SELECT 1", emptyArray(), signal)
        verify(sqlite).progressHandler(eq(DB), eq(0), isNull())
    }

    private companion object {
        const val DB = 1L
        const val STMT = 2L
    }
}
