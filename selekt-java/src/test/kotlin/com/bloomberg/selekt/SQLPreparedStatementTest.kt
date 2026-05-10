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
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
        verify(sqlite, times(1)).bindText(eq(STATEMENT), eq(1), eq("test"))
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
