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

    @Suppress("Detekt.LongMethod")
    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(sqlite.withScopedArena(any<() -> Any?>())) doAnswer {
            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as () -> Any?).invoke()
        }
        whenever(sqlite.newDatabaseHandle(any<Long>())) doAnswer { DatabaseHandle(it.getArgument(0)) }
        whenever(sqlite.newStatementHandle(any<Long>())) doAnswer { StatementHandle(it.getArgument(0)) }
        whenever(sqlite.prepareV2(any<DatabaseHandle>(), any<String>(), any<LongArray>())) doAnswer {
            sqlite.prepareV2((it.arguments[0] as DatabaseHandle).pointer, it.arguments[1] as String, it.arguments[2] as LongArray)
        }
        whenever(sqlite.step(any<StatementHandle>())) doAnswer {
            sqlite.step((it.arguments[0] as StatementHandle).pointer)
        }
        whenever(sqlite.stepWithoutThrowing(any<StatementHandle>())) doAnswer {
            sqlite.stepWithoutThrowing((it.arguments[0] as StatementHandle).pointer)
        }
        whenever(sqlite.columnText(any<StatementHandle>(), any<Int>())) doAnswer {
            sqlite.columnText((it.arguments[0] as StatementHandle).pointer, it.arguments[1] as Int)
        }
        whenever(sqlite.columnCount(any<StatementHandle>())) doAnswer {
            sqlite.columnCount((it.arguments[0] as StatementHandle).pointer)
        }
        whenever(sqlite.statementReadOnly(any<StatementHandle>())) doAnswer {
            sqlite.statementReadOnly((it.arguments[0] as StatementHandle).pointer)
        }
        whenever(sqlite.resetAndClearBindings(any<StatementHandle>())) doAnswer {
            sqlite.resetAndClearBindings((it.arguments[0] as StatementHandle).pointer)
        }
        whenever(sqlite.getAutocommit(any<DatabaseHandle>())) doAnswer { sqlite.getAutocommit((it.arguments[0] as DatabaseHandle).pointer) }
        whenever(sqlite.interrupt(any<DatabaseHandle>())) doAnswer {
            sqlite.interrupt((it.arguments[0] as DatabaseHandle).pointer)
        }
        whenever(sqlite.isInterrupted(any<DatabaseHandle>())) doAnswer { sqlite.isInterrupted((it.arguments[0] as DatabaseHandle).pointer) }
        whenever(sqlite.progressHandler(any<DatabaseHandle>(), any<Int>(), any<SQLProgressHandler>())) doAnswer {
            sqlite.progressHandler((it.arguments[0] as DatabaseHandle).pointer, it.arguments[1] as Int, it.arguments[2] as SQLProgressHandler?)
        }
        whenever(sqlite.progressHandler(any<DatabaseHandle>(), any<Int>(), isNull())) doAnswer {
            sqlite.progressHandler((it.arguments[0] as DatabaseHandle).pointer, it.arguments[1] as Int, null)
        }
        whenever(sqlite.commitHook(any<DatabaseHandle>(), any<Boolean>(), any<SQLCommitListener>())) doAnswer {
            sqlite.commitHook((it.arguments[0] as DatabaseHandle).pointer, it.arguments[1] as Boolean, it.arguments[2] as SQLCommitListener?)
        }
        whenever(sqlite.commitHook(any<DatabaseHandle>(), any<Boolean>(), isNull())) doAnswer {
            sqlite.commitHook((it.arguments[0] as DatabaseHandle).pointer, it.arguments[1] as Boolean, null)
        }
        whenever(sqlite.extendedResultCodes(any<DatabaseHandle>(), any<Int>())) doAnswer {
            sqlite.extendedResultCodes((it.arguments[0] as DatabaseHandle).pointer, it.arguments[1] as Int)
        }
        whenever(sqlite.busyTimeout(any<DatabaseHandle>(), any<Int>())) doAnswer {
            sqlite.busyTimeout((it.arguments[0] as DatabaseHandle).pointer, it.arguments[1] as Int)
        }
        whenever(sqlite.exec(any<DatabaseHandle>(), any<String>())) doAnswer {
            sqlite.exec((it.arguments[0] as DatabaseHandle).pointer, it.arguments[1] as String)
        }
        whenever(sqlite.closeV2(any<DatabaseHandle>())) doAnswer {
            sqlite.closeV2((it.arguments[0] as DatabaseHandle).pointer)
        }
        whenever(sqlite.openV2(any(), any(), any())) doAnswer {
            requireNotNull(it.arguments[2] as? LongArray)[0] = DB
            0
        }
        whenever(sqlite.prepareV2(any<Long>(), any<String>(), any<LongArray>())) doAnswer {
            requireNotNull(it.arguments[2] as? LongArray)[0] = STMT
            0
        }
        whenever(sqlite.stepWithoutThrowing(any<Long>())) doReturn SQL_DONE
        whenever(sqlite.getAutocommit(any<Long>())) doReturn 1
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
        whenever(sqlite.isInterrupted(any<Long>())) doReturn false
        assertFalse(database.isInterrupted)
    }

    @Test
    fun isInterruptedTrue() {
        database.transact { }
        whenever(sqlite.isInterrupted(any<Long>())) doReturn true
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
        whenever(sqlite.columnText(any<Long>(), any<Int>())) doReturn ""
        SQLitePragma.entries.forEach { pragma(it) }
    }

    @Test
    fun pragmaAcceptsEnumKey(): Unit = database.run {
        whenever(sqlite.columnText(any<Long>(), any<Int>())) doReturn "wal"
        pragma(SQLitePragma.JOURNAL_MODE)
    }

    @Test
    fun pragmaAcceptsIncrementalVacuumWithArgument(): Unit = database.run {
        whenever(sqlite.columnText(any<Long>(), any<Int>())) doReturn ""
        pragma("incremental_vacuum(100)")
    }

    @Test
    fun pragmaAcceptsSchemaPrefixedKey(): Unit = database.run {
        whenever(sqlite.columnText(any<Long>(), any<Int>())) doReturn "ok"
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
        whenever(sqlite.columnText(any<Long>(), any<Int>())) doReturn "wal"
        pragma(SQLitePragma.JOURNAL_MODE, "wal")
    }

    @Test
    fun pragmaValueAcceptsInteger(): Unit = database.run {
        whenever(sqlite.columnText(any<Long>(), any<Int>())) doReturn "1"
        pragma(SQLitePragma.JOURNAL_MODE, 1)
    }

    @Test
    fun pragmaValueAcceptsNegativeInteger(): Unit = database.run {
        whenever(sqlite.columnText(any<Long>(), any<Int>())) doReturn "-1"
        pragma(SQLitePragma.SOFT_HEAP_LIMIT, -1)
    }

    private fun verifyCommit(): Unit = inOrder(sqlite).run {
        verify(sqlite, times(1)).prepareV2(eq(DB), eq("END"), any<LongArray>())
        verify(sqlite, times(1)).stepWithoutThrowing(eq(STMT))
    }

    private fun verifyRollback(): Unit = inOrder(sqlite) {
        verify(sqlite, times(1)).prepareV2(eq(DB), eq("ROLLBACK"), any<LongArray>())
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
        whenever(sqlite.columnCount(any<Long>())) doReturn 0
        whenever(sqlite.step(any<Long>())) doReturn SQL_DONE
        whenever(sqlite.statementReadOnly(any<Long>())) doReturn 1
        database.query("SELECT 1", emptyArray(), signal)
        verify(sqlite, times(1)).progressHandler(eq(DB), eq(500), any<SQLProgressHandler>())
    }

    @Test
    fun queryWithCancellationSignalClearsProgressHandler() {
        database.transact { }
        val signal = CancellationSignal(500)
        whenever(sqlite.columnCount(any<Long>())) doReturn 0
        whenever(sqlite.step(any<Long>())) doReturn SQL_DONE
        whenever(sqlite.statementReadOnly(any<Long>())) doReturn 1
        database.query("SELECT 1", emptyArray(), signal)
        verify(sqlite).progressHandler(eq(DB), eq(0), isNull())
    }

    private companion object {
        const val DB = 1L
        const val STMT = 2L
    }
}
