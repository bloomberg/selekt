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
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.verify
import java.nio.ByteBuffer
import java.io.ByteArrayOutputStream
import java.util.stream.Stream

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
        whenever(sqlite.capabilities) doReturn PlatformCapabilities(useNativeCursorWindow = true)
        whenever(sqlite.fillCursorWindow(any<StatementHandle>(), any(), any(), any())) doAnswer {
            ByteBuffer.allocate(2 * Int.SIZE_BYTES).apply { putInt(0, 0); putInt(Int.SIZE_BYTES, 0) }
        }
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
    fun batchCollectionOverloads() {
        val sql = "INSERT INTO t VALUES (?)"
        val row = arrayOf<Any?>(1)
        whenever(sqlite.changes(any<DatabaseHandle>())) doReturn 1
        whenever(sqlite.changes(any<Long>())) doReturn 1
        whenever(sqlite.step(any<Long>())) doReturn SQL_DONE
        whenever(sqlite.step(any<StatementHandle>())) doReturn SQL_DONE

        assertEquals(0, database.batch(sql, sequenceOf(row)))
        assertEquals(0, database.batch(sql, listOf(row)))
        assertEquals(0, database.batch(sql, arrayOf(row)))
        assertEquals(0, database.batch(sql, listOf(row) as Iterable<Array<out Any?>>))
        assertEquals(0, database.batch(sql, Stream.of(row)))
    }

    @Test
    fun compiledStatementNamedBindingsAndUnknownName() {
        whenever(sqlite.bindParameterCount(any<StatementHandle>())) doReturn 6
        whenever(sqlite.bindParameterCount(any<Long>())) doReturn 6
        val statement = database.compileStatement(
            "UPDATE t SET a=:blob, b=:double, c=:int, d=:long, e=:null, f=:string"
        )

        statement.bindBlob(":blob", byteArrayOf(1))
        statement.bindDouble(":double", 2.0)
        statement.bindInt(":int", 3)
        statement.bindLong(":long", 4L)
        statement.bindNull(":null")
        statement.bindString(":string", "six")
        assertFailsWith<IllegalArgumentException> { statement.bindInt(":missing", 7) }
        statement.close()
    }

    @Test
    fun deferredTransactionListenerCommits() {
        val listener = org.mockito.kotlin.mock<SQLTransactionListener>()
        database.beginDeferredTransactionWithListener(listener)
        database.setTransactionSuccessful()
        database.endTransaction()

        verify(listener).onCommit()
    }

    @Test
    fun transactionModesAndListenerModes() {
        database.transact(SQLiteTransactionMode.DEFERRED) { }
        database.transact(SQLiteTransactionMode.IMMEDIATE) { }

        val listener = org.mockito.kotlin.mock<SQLTransactionListener>()
        database.transact(listener = listener, transactionMode = SQLiteTransactionMode.DEFERRED) { }
        database.transact(listener = listener, transactionMode = SQLiteTransactionMode.IMMEDIATE) { }
        verify(listener).onCommit()
    }

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
    fun rawStatementBeginAndCommitTransaction() {
        database.prepare("BEGIN IMMEDIATE TRANSACTION").use {
            assertFalse(database.inTransaction)
            it.step()
            assertTrue(database.inTransaction)
        }
        database.prepare("END TRANSACTION").use { it.step() }
        assertFalse(database.inTransaction)
        verifyCommit()
    }

    @Test
    fun rawStatementBeginAndRollbackTransaction() {
        database.prepare("BEGIN DEFERRED TRANSACTION").use { it.step() }
        database.prepare("ROLLBACK TRANSACTION").use { it.step() }
        assertFalse(database.inTransaction)
        verifyRollback()
    }

    @Test
    fun rawStatementNestedBeginCommitsViaSavepoint() {
        database.prepare("BEGIN EXCLUSIVE TRANSACTION").use { it.step() }
        database.prepare("BEGIN IMMEDIATE TRANSACTION").use { it.step() }
        database.prepare("END TRANSACTION").use { it.step() }
        assertTrue(database.inTransaction)
        database.prepare("END TRANSACTION").use { it.step() }
        assertFalse(database.inTransaction)
        verifyCommit()
    }

    @Test
    fun rawStatementReleasesConnectionOnClose() {
        database.prepare("SELECT 1").close()
        database.prepare("SELECT 1").close()
    }

    @Test
    fun rawStatementIntegerAccessorsAndClosedColumnNames() {
        whenever(sqlite.columnInt(any<StatementHandle>(), any())) doReturn 42
        val statement = database.prepare("SELECT ?")

        statement.bindInt(1, 7)
        assertEquals(42, statement.columnInt(0))
        statement.close()
        assertTrue(statement.columnNames().isEmpty())
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

    @Test
    fun cancellableStructuredAndQueryObjectOverloads() {
        whenever(sqlite.columnCount(any<Long>())) doReturn 0
        whenever(sqlite.step(any<Long>())) doReturn SQL_DONE
        whenever(sqlite.statementReadOnly(any<Long>())) doReturn 1
        val signal = CancellationSignal()

        database.query(
            distinct = false,
            table = "items",
            columns = arrayOf("*"),
            selection = "",
            selectionArgs = emptyArray(),
            groupBy = null,
            having = null,
            orderBy = null,
            limit = null,
            cancellationSignal = signal
        ).close()
        val query = object : ISQLQuery {
            override val sql = "SELECT 1"
            override val argCount = 0
            override fun bindTo(statement: ISQLProgram) = Unit
        }
        database.query(query, signal).close()
    }

    @Test
    fun parameterRowCancellableQueryOverloads() {
        whenever(sqlite.columnCount(any<Long>())) doReturn 0
        whenever(sqlite.step(any<Long>())) doReturn SQL_DONE
        whenever(sqlite.statementReadOnly(any<Long>())) doReturn 1
        val signal = CancellationSignal()
        val row = ParameterRow(0)

        database.query("SELECT 1", row, signal).close()
        database.queryUpTo("SELECT 1", row, 1, signal).close()
        assertFailsWith<IllegalArgumentException> {
            database.queryUpTo("SELECT 1", row, 0, signal)
        }
    }

    @Test
    fun boundedArrayQueryHandlesInitiallyWritableStatement() {
        whenever(sqlite.columnCount(any<Long>())) doReturn 0
        whenever(sqlite.statementReadOnly(any<Long>())) doReturn 0

        database.queryUpTo("SELECT 1", emptyArray(), 1, CancellationSignal()).close()
    }

    @Test
    fun queryRejectsTooFewAndOutOfRangeBindings() {
        whenever(sqlite.columnCount(any<Long>())) doReturn 0
        whenever(sqlite.statementReadOnly(any<Long>())) doReturn 1
        whenever(sqlite.bindParameterCount(any<Long>())) doReturn 2
        whenever(sqlite.bindParameterCount(any<StatementHandle>())) doReturn 2
        assertFailsWith<IllegalArgumentException> {
            database.query("SELECT ?, ?", arrayOf(1)).close()
        }

        whenever(sqlite.bindParameterCount(any<Long>())) doReturn 1
        whenever(sqlite.bindParameterCount(any<StatementHandle>())) doReturn 1
        val query = object : ISQLQuery {
            override val sql = "SELECT ?"
            override val argCount = 2
            override fun bindTo(statement: ISQLProgram) = statement.bindLong(2, 42L)
        }
        assertFailsWith<IllegalArgumentException> { database.query(query).close() }
    }

    @Test
    fun defaultBlobSchemaOverloads() {
        whenever(sqlite.newBlobHandle(any<Long>())) doAnswer { BlobHandle(it.getArgument(0)) }
        whenever(sqlite.blobBytes(any<BlobHandle>())) doReturn 0
        whenever(
            sqlite.blobOpen(
                any<DatabaseHandle>(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any<LongArray>()
            )
        ) doAnswer {
            it.getArgument<LongArray>(6)[0] = 3L
            SQL_OK
        }
        assertEquals(0, database.sizeOfBlob("items", "data", 1))
        val output = ByteArrayOutputStream()
        database.readFromBlob("items", "data", 1, 0, 0, output)
        database.writeToBlob("items", "data", 1, 0, byteArrayOf().inputStream())
        assertEquals(0, output.size())
    }

    @Test
    fun exhaustingForwardQueryClearsProgressHandlerWithoutClosingCursor() {
        database.transact { }
        val signal = CancellationSignal(500)
        whenever(sqlite.columnCount(any<Long>())) doReturn 0
        whenever(sqlite.step(any<Long>())) doReturn SQL_DONE
        whenever(sqlite.statementReadOnly(any<Long>())) doReturn 1

        val cursor = database.queryForwardOnly("SELECT 1", emptyArray(), signal)
        verify(sqlite, times(1)).progressHandler(eq(DB), eq(500), any<SQLProgressHandler>())
        verify(sqlite, never()).progressHandler(eq(DB), eq(0), isNull())

        assertFalse(cursor.moveToNext())
        assertFalse(cursor.isClosed())
        verify(sqlite, times(1)).progressHandler(eq(DB), eq(0), isNull())
        cursor.close()
    }

    private companion object {
        const val DB = 1L
        const val STMT = 2L
    }
}
