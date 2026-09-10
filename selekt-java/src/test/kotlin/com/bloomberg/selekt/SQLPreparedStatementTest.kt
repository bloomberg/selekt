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

import org.junit.jupiter.api.Test
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val POINTER = 42L
private const val DB = 43L
private const val INTERVAL_MILLIS = 2_000L
private val STATEMENT = StatementHandle(POINTER)

internal class SQLPreparedStatementTest {
    @Test
    fun clearBindings(): Unit = mock<SQLite>().run {
        SQLPreparedStatement(STATEMENT, "SELECT * FROM Foo", this, CommonThreadLocalRandom).clearBindings()
        verify(this, times(1)).clearBindings(eq(STATEMENT))
    }

    @Test
    fun stepWithRetryDone() {
        val sqlite = mock<SQLite> {
            whenever(it.stepWithoutThrowing(any<StatementHandle>())) doReturn SQL_DONE
        }
        val statement = SQLPreparedStatement(STATEMENT, "BEGIN IMMEDIATE TRANSACTION", sqlite, CommonThreadLocalRandom)
        assertEquals(SQL_DONE, statement.step(INTERVAL_MILLIS))
    }

    @Test
    fun stepWithRetryRow() {
        val sqlite = mock<SQLite> {
            whenever(it.stepWithoutThrowing(any<StatementHandle>())) doReturn SQL_ROW
        }
        val statement = SQLPreparedStatement(STATEMENT, "SELECT * FROM Foo", sqlite, CommonThreadLocalRandom)
        assertEquals(SQL_ROW, statement.step(INTERVAL_MILLIS))
    }

    @Test
    fun stepWithRetryExpires() {
        val sqlite = mock<SQLite> {
            whenever(it.databaseHandle(any<StatementHandle>())) doReturn DB
            whenever(it.step(any<StatementHandle>())) doReturn SQL_BUSY
        }
        val statement = SQLPreparedStatement(STATEMENT, "BEGIN BLAH", sqlite, CommonThreadLocalRandom)
        assertFailsWith<Exception> {
            statement.step(0L)
        }
    }

    @Test
    fun stepWithRetryCanUltimatelySucceed() {
        val sqlite = mock<SQLite> {
            whenever(it.stepWithoutThrowing(any<StatementHandle>())) doAnswer object : Answer<SQLCode> {
                private var count = 0

                override fun answer(invocation: InvocationOnMock) = when (count++) {
                    0 -> SQL_BUSY
                    else -> SQL_DONE
                }
            }
        }
        val statement = SQLPreparedStatement(STATEMENT, "BEGIN IMMEDIATE TRANSACTION", sqlite, CommonThreadLocalRandom)
        assertEquals(SQL_DONE, statement.step(500L))
    }

    @Test
    fun stepRetryDoesNotStackOverflow() {
        val sqlite = mock<SQLite> {
            whenever(it.databaseHandle(any<StatementHandle>())) doReturn DB
            whenever(it.stepWithoutThrowing(any<StatementHandle>())) doReturn SQL_BUSY
        }
        val statement = SQLPreparedStatement(STATEMENT, "BEGIN BLAH", sqlite, CommonThreadLocalRandom)
        assertFailsWith<Exception> {
            statement.step(2_000L)
        }
    }

    @Test
    fun stepRejectsNegativeInterval() {
        val statement = SQLPreparedStatement(STATEMENT, "BEGIN BLAH", mock(), CommonThreadLocalRandom)
        assertFailsWith<IllegalArgumentException> {
            statement.step(-1L)
        }
    }

    @Test
    fun isBusyTrue() {
        val sqlite = mock<SQLite> {
            whenever(it.databaseHandle(any<StatementHandle>())) doReturn DB
            whenever(it.statementBusy(any<StatementHandle>())) doReturn 1
        }
        assertTrue(SQLPreparedStatement(STATEMENT, "BEGIN BLAH", sqlite, CommonThreadLocalRandom).isBusy())
    }

    @Test
    fun isBusyFalse() {
        val sqlite = mock<SQLite> {
            whenever(it.databaseHandle(any<StatementHandle>())) doReturn DB
            whenever(it.statementBusy(any<StatementHandle>())) doReturn 0
        }
        assertFalse(SQLPreparedStatement(STATEMENT, "BEGIN BLAH", sqlite, CommonThreadLocalRandom).isBusy())
    }

    @Test
    fun columnName() {
        val sqlite = mock<SQLite> {
            whenever(it.databaseHandle(any<StatementHandle>())) doReturn DB
            whenever(it.columnName(any<StatementHandle>(), any())) doReturn "foo"
        }
        assertEquals("foo", SQLPreparedStatement(STATEMENT, "BEGIN BLAH", sqlite, CommonThreadLocalRandom).columnName(0))
        verify(sqlite, times(1)).columnName(eq(STATEMENT), eq(0))
    }

    @Test
    fun bindBlobByName() {
        val blob = byteArrayOf(1, 2, 3)
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterIndex(any<StatementHandle>(), eq(":data"))) doReturn 1
        }
        SQLPreparedStatement(STATEMENT, "INSERT INTO t VALUES (:data)", sqlite, CommonThreadLocalRandom)
            .bind(":data", blob)
        verify(sqlite, times(1)).bindParameterIndex(eq(STATEMENT), eq(":data"))
        verify(sqlite, times(1)).bindBlob(eq(STATEMENT), eq(1), eq(blob))
    }

    @Test
    fun bindDoubleByName() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterIndex(any<StatementHandle>(), eq(":value"))) doReturn 1
        }
        SQLPreparedStatement(STATEMENT, "INSERT INTO t VALUES (:value)", sqlite, CommonThreadLocalRandom)
            .bind(":value", 3.14)
        verify(sqlite, times(1)).bindParameterIndex(eq(STATEMENT), eq(":value"))
        verify(sqlite, times(1)).bindDouble(eq(STATEMENT), eq(1), eq(3.14))
    }

    @Test
    fun bindIntByName() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterIndex(any<StatementHandle>(), eq("@count"))) doReturn 2
        }
        SQLPreparedStatement(STATEMENT, "INSERT INTO t VALUES (?, @count)", sqlite, CommonThreadLocalRandom)
            .bind("@count", 42)
        verify(sqlite, times(1)).bindParameterIndex(eq(STATEMENT), eq("@count"))
        verify(sqlite, times(1)).bindInt(eq(STATEMENT), eq(2), eq(42))
    }

    @Test
    fun bindLongByName() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterIndex(any<StatementHandle>(), eq($$"$id"))) doReturn 1
        }
        SQLPreparedStatement(STATEMENT, $$"SELECT * FROM t WHERE id = $id", sqlite, CommonThreadLocalRandom)
            .bind($$"$id", 123_456_789L)
        verify(sqlite, times(1)).bindParameterIndex(eq(STATEMENT), eq($$"$id"))
        verify(sqlite, times(1)).bindInt64(eq(STATEMENT), eq(1), eq(123_456_789L))
    }

    @Test
    fun bindStringByName() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterIndex(any<StatementHandle>(), eq(":name"))) doReturn 1
        }
        SQLPreparedStatement(STATEMENT, "INSERT INTO t VALUES (:name)", sqlite, CommonThreadLocalRandom)
            .bind(":name", "test")
        verify(sqlite, times(1)).bindParameterIndex(eq(STATEMENT), eq(":name"))
        verify(sqlite, times(1)).bindText(eq(STATEMENT), eq(1), eq("test"), any())
    }

    @Test
    fun bindStringReusesEncodingModesAcrossParameters() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterCount(any<StatementHandle>())) doReturn 2
        }
        val statement = SQLPreparedStatement(STATEMENT, "INSERT INTO t VALUES (?, ?)", sqlite, CommonThreadLocalRandom)
        statement.bind(1, "first")
        statement.bind(1, "second")
        statement.bind(2, "third")
        val modes = argumentCaptor<BooleanArray>()
        verify(sqlite, times(3)).bindText(eq(STATEMENT), any(), any(), modes.capture())
        assertSame(modes.allValues[0], modes.allValues[1])
        assertSame(modes.allValues[0], modes.allValues[2])
    }

    @Test
    fun bindParameterRowReusesEncodingModes() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterCount(any<StatementHandle>())) doReturn 1
        }
        val statement = SQLPreparedStatement(STATEMENT, "INSERT INTO t VALUES (?)", sqlite, CommonThreadLocalRandom)
        val row = ParameterRow(1).apply { setObject(0, "text") }
        statement.bindRow(row)
        statement.bindRow(row)
        val modes = argumentCaptor<BooleanArray>()
        verify(sqlite, times(2)).bindRow(eq(STATEMENT), same(row), modes.capture())
        assertSame(modes.allValues[0], modes.allValues[1])
    }

    @Test
    fun bindArrayRowReusesEncodingModes() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterCount(any<StatementHandle>())) doReturn 1
        }
        val statement = SQLPreparedStatement(STATEMENT, "INSERT INTO t VALUES (?)", sqlite, CommonThreadLocalRandom)
        statement.bindRow(arrayOf("first"))
        statement.bindRow(arrayOf("second"))
        val modes = argumentCaptor<BooleanArray>()
        verify(sqlite, times(2)).bindRow(eq(STATEMENT), any<Array<out Any?>>(), modes.capture())
        assertSame(modes.allValues[0], modes.allValues[1])
    }

    @Test
    fun textEncodingModesSurviveResetAndClearingBindings() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterCount(any<StatementHandle>())) doReturn 1
            whenever(it.bindText(any<StatementHandle>(), any(), any(), any())) doAnswer {
                (it.arguments[3] as BooleanArray)[1] = true
                SQL_OK
            }
        }
        val statement = SQLPreparedStatement(STATEMENT, "INSERT INTO t VALUES (?)", sqlite, CommonThreadLocalRandom)
        statement.bind(1, "café")
        statement.reset()
        statement.resetAndClearBindings()
        statement.clearBindings()
        statement.bind(1, "ascii later")
        val modes = argumentCaptor<BooleanArray>()
        verify(sqlite, times(2)).bindText(eq(STATEMENT), eq(1), any(), modes.capture())
        assertSame(modes.allValues[0], modes.allValues[1])
        assertTrue(modes.secondValue[1])
    }

    @Test
    fun bindNullByName() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterIndex(any<StatementHandle>(), eq(":nullable"))) doReturn 1
        }
        SQLPreparedStatement(STATEMENT, "INSERT INTO t VALUES (:nullable)", sqlite, CommonThreadLocalRandom)
            .bindNull(":nullable")
        verify(sqlite, times(1)).bindParameterIndex(eq(STATEMENT), eq(":nullable"))
        verify(sqlite, times(1)).bindNull(eq(STATEMENT), eq(1))
    }

    @Test
    fun closeFinalizesStatement() {
        val sqlite = mock<SQLite>()
        val statement = SQLPreparedStatement(STATEMENT, "SELECT * FROM Foo", sqlite, CommonThreadLocalRandom)
        statement.close()
        verify(sqlite, times(1)).finalize(eq(STATEMENT))
    }

    @Test
    fun closeIsIdempotent() {
        val sqlite = mock<SQLite>()
        val statement = SQLPreparedStatement(STATEMENT, "SELECT * FROM Foo", sqlite, CommonThreadLocalRandom)
        statement.close()
        statement.close()
        verify(sqlite, times(1)).finalize(eq(STATEMENT))
    }

    @Test
    fun retainDefersFinalizationUntilFullyReleased() {
        val sqlite = mock<SQLite>()
        val statement = SQLPreparedStatement(STATEMENT, "SELECT * FROM Foo", sqlite, CommonThreadLocalRandom)
        statement.retain()
        statement.close()
        verify(sqlite, never()).finalize(eq(STATEMENT))
        statement.release()
        verify(sqlite, times(1)).finalize(eq(STATEMENT))
    }

    @Test
    fun bindByNameThrowsForUnknownParameter() {
        val sqlite = mock<SQLite> {
            whenever(it.bindParameterIndex(any<StatementHandle>(), eq(":unknown"))) doReturn 0
        }
        val statement = SQLPreparedStatement(
            STATEMENT,
            "INSERT INTO t VALUES (:known)",
            sqlite,
            CommonThreadLocalRandom
        )
        assertFailsWith<IllegalArgumentException> {
            statement.bind(":unknown", "value")
        }
    }
}
