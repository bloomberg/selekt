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

package com.bloomberg.selekt

import com.bloomberg.selekt.exceptions.SelektSQLException
import com.bloomberg.selekt.pools.TieredObjectPool
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class SQLSessionCoverageTest {
    private val executor = mock<CloseableSQLExecutor>()
    private val pool = mock<TieredObjectPool<String, CloseableSQLExecutor>> {
        on { borrowPrimaryObject() } doReturn executor
        on { borrowObject(any<String>()) } doReturn executor
        on { borrowSecondaryObject(any<String>()) } doReturn executor
    }

    @Test
    fun `thread local session default factory and override state`() {
        val sessions = ThreadLocalSession(pool)
        val session = sessions.newSession()

        assertFalse(sessions.isOverriddenBy(session))
        sessions.withSession(session) {
            assertTrue(sessions.isOverriddenBy(session))
            assertSame(session, sessions.freeze().invoke())
        }
        assertFalse(sessions.isOverriddenBy(session))
    }

    @Test
    fun `savepoint rollback removes newer savepoints`() {
        val session = SQLSession(pool)
        session.beginDeferredTransaction()
        val automaticName = session.setSavepoint()
        session.setSavepoint("newer")

        session.rollbackToSavepoint(automaticName)
        session.releaseSavepoint(automaticName)
        session.endTransaction()

        verify(executor).execute("ROLLBACK TO $automaticName")
    }

    @Test
    fun `transactional abort ends the active transaction`() {
        val session = SQLSession(pool)
        session.beginDeferredTransaction()
        val signal = Any()
        val sql = "-- audit\nROLLBACK TRANSACTION"

        assertSame(signal, session.execute(false, sql, sql.resolvedSqlStatementType(), signal) {
            error("Transactional control must not run the statement block")
        })
        assertFalse(session.inTransaction)
    }

    @Test
    fun `raw transaction retains and then returns the pooled executor`() {
        val session = SQLSession(pool)
        val begin = "/* audit */ BEGIN IMMEDIATE TRANSACTION"
        val end = "-- audit\nEND TRANSACTION"

        session.execute(true, begin, begin.resolvedSqlStatementType(), Unit) {
            error("Transactional control must not run the statement block")
        }
        assertTrue(session.inTransaction)
        verify(pool, never()).returnObject(executor)

        session.execute(true, end, end.resolvedSqlStatementType(), Unit) {
            error("Transactional control must not run the statement block")
        }
        assertFalse(session.inTransaction)
        verify(pool).returnObject(executor)

        session.execute(false, "SELECT 1") { assertSame(executor, it) }
        verify(executor, times(2)).onBorrowed()
        verify(pool, times(2)).returnObject(executor)
    }

    @Test
    fun `failed return discards executor and borrows a replacement`() {
        val failure = IllegalStateException("cleanup failed")
        val replacement = mock<CloseableSQLExecutor>()
        whenever(pool.borrowPrimaryObject()).thenReturn(executor, replacement)
        whenever(executor.onReturned()) doAnswer { throw failure }
        val session = SQLSession(pool)

        val thrown = assertFailsWith<IllegalStateException> {
            session.execute(true) { }
        }
        assertSame(failure, thrown)
        verify(pool).discardObject(executor)
        verify(pool, never()).returnObject(executor)

        assertSame(replacement, session.execute(true) { it })
        verify(pool, times(2)).borrowPrimaryObject()
        verify(pool).returnObject(replacement)
    }

    @Test
    fun `raw savepoint controls outside a transaction are rejected before borrowing an executor`() {
        val session = SQLSession(pool)
        listOf(
            "SAVEPOINT sp",
            "/* audit */ RELEASE SAVEPOINT sp",
            "-- audit\nROLLBACK TRANSACTION TO SAVEPOINT sp"
        ).forEach { sql ->
            assertEquals(SQLStatementType.SAVEPOINT, sql.resolvedSqlStatementType())
            assertFailsWith<IllegalArgumentException> {
                session.execute(true, sql, SQLStatementType.SAVEPOINT, Unit) {
                    error("Savepoint control must not run the statement block")
                }
            }
        }
        verify(pool, never()).borrowPrimaryObject()
    }

    @Test
    fun `raw savepoint controls execute on the active transaction executor`() {
        val session = SQLSession(pool)
        session.beginDeferredTransaction()
        val signal = Any()
        val controls = listOf(
            "/* audit */ SAVEPOINT sp",
            "-- audit\nROLLBACK TRANSACTION TO SAVEPOINT sp",
            "RELEASE SAVEPOINT sp"
        )

        controls.forEach { sql ->
            assertSame(signal, session.execute(true, sql, sql.resolvedSqlStatementType(), signal) {
                assertSame(executor, it)
                it.execute(sql)
                signal
            })
        }

        assertTrue(session.inTransaction)
        controls.forEach { verify(executor).execute(it) }
        verify(pool, never()).returnObject(executor)
        session.endTransaction()
        verify(pool).returnObject(executor)
    }

    @Test
    fun `parameter row forward cursor releases its executor on close`() {
        val session = SQLSession(pool)
        val row = ParameterRow(0)
        val cursor = mock<ForwardCursor>()
        var onClose: (() -> Unit)? = null
        whenever(executor.executeForForwardCursor(eq("SELECT 1"), same(row), any())) doAnswer {
            onClose = it.getArgument(2)
            cursor
        }

        assertSame(cursor, session.executeForForwardCursor("SELECT 1", row))
        checkNotNull(onClose).invoke()
        verify(pool).returnObject(executor)
    }

    @Test
    fun `failed raw statement preparation releases its executor`() {
        whenever(executor.prepareForRawStatement("bad")) doAnswer { error("prepare failed") }
        val session = SQLSession(pool)

        assertFailsWith<IllegalStateException> { session.prepareRawStatement("bad", false) }
        verify(pool).returnObject(executor)
    }

    @Test
    fun `failed begin listener is not notified by a later native commit`() {
        emulateNativeTransactionCallbacks()
        val failure = IllegalStateException("begin failed")
        val listener = mock<SQLTransactionListener> {
            on { onBegin() } doAnswer { throw failure }
        }
        val session = SQLSession(pool, useNativeListeners = true)

        assertSame(failure, assertFailsWith { session.beginDeferredTransactionWithListener(listener) })
        verify(listener).onRollback()

        session.beginDeferredTransaction()
        session.setTransactionSuccessful()
        session.endTransaction()

        verify(listener, never()).onCommit()
        verify(listener).onRollback()
        verify(executor).setTransactionListener(null)
    }

    @Test
    fun `failed begin listener is not notified by a later native rollback`() {
        emulateNativeTransactionCallbacks()
        val failure = IllegalStateException("begin failed")
        val listener = mock<SQLTransactionListener> {
            on { onBegin() } doAnswer { throw failure }
        }
        val session = SQLSession(pool, useNativeListeners = true)

        assertSame(failure, assertFailsWith { session.beginDeferredTransactionWithListener(listener) })
        verify(listener).onRollback()

        session.beginDeferredTransaction()
        session.endTransaction()

        verify(listener, never()).onCommit()
        verify(listener).onRollback()
        verify(executor).setTransactionListener(null)
    }

    @Test
    fun `failed native listener cleanup is suppressed by the begin failure`() {
        val beginFailure = IllegalStateException("begin failed")
        val cleanupFailure = IllegalArgumentException("cleanup failed")
        val listener = mock<SQLTransactionListener> {
            on { onBegin() } doAnswer { throw beginFailure }
        }
        whenever(executor.setTransactionListener(isNull())) doAnswer { throw cleanupFailure }
        val session = SQLSession(pool, useNativeListeners = true)

        val thrown = assertFailsWith<IllegalStateException> {
            session.beginDeferredTransactionWithListener(listener)
        }

        assertSame(beginFailure, thrown)
        assertEquals(listOf(cleanupFailure), thrown.suppressed.toList())
        assertFalse(session.inTransaction)
        verify(pool).returnObject(executor)
    }

    @Test
    fun `cancelled forward cursor failure clears progress and translates interrupt`() {
        val signal = CancellationSignal(1).apply { cancel() }
        val failure = object : RuntimeException(), SelektSQLException {
            override val code = SQL_INTERRUPT
            override val extendedCode = SQL_INTERRUPT
        }
        whenever(executor.executeForForwardCursor(eq("SELECT 1"), any<Array<out Any?>>(), any())) doAnswer {
            throw failure
        }
        val session = SQLSession(pool)

        assertFailsWith<OperationCancelledException> {
            session.executeForForwardCursorWithSignal("SELECT 1", emptyArray(), signal)
        }
        verify(executor).setProgressHandler(0, null)
        verify(pool).returnObject(executor)
    }

    private fun emulateNativeTransactionCallbacks() {
        var listener: SQLTransactionListener? = null
        whenever(executor.setTransactionListener(any())) doAnswer {
            listener = it.getArgument(0)
            Unit
        }
        whenever(executor.setTransactionListener(isNull())) doAnswer {
            listener = null
            Unit
        }
        whenever(executor.executeWithRetry(any())) doAnswer {
            if (it.getArgument<String>(0) == "END") {
                listener?.onCommit()
            }
            0
        }
        whenever(executor.execute(any(), any<Array<out Any?>>())) doAnswer {
            if (it.getArgument<String>(0) == "ROLLBACK") {
                listener?.onRollback()
            }
            0
        }
    }
}
