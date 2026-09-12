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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
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

        assertSame(signal, session.execute(false, "ROLLBACK", SQLStatementType.ABORT, signal) {
            error("Transactional control must not run the statement block")
        })
        assertFalse(session.inTransaction)
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
}
