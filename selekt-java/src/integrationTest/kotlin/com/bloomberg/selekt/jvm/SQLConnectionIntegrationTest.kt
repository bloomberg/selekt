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

package com.bloomberg.selekt.jvm

import com.bloomberg.selekt.CloseableSQLExecutor
import com.bloomberg.selekt.ChunkedParameterRows
import com.bloomberg.selekt.CommonThreadLocalRandom
import com.bloomberg.selekt.ParameterRow
import com.bloomberg.selekt.SQLConnection
import com.bloomberg.selekt.SQLConnectionFactory
import com.bloomberg.selekt.SQL_OPEN_CREATE
import com.bloomberg.selekt.SQL_OPEN_READWRITE
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.pools.SingleObjectPool
import org.junit.jupiter.api.Test
import java.util.concurrent.ScheduledThreadPoolExecutor
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SQLConnectionIntegrationTest {
    @Test
    fun overlappingSameSqlForwardCursorsCanCloseFirstThenSecond() {
        assertOverlappingSameSqlForwardCursors(closeFirstCursorFirst = true)
    }

    @Test
    fun overlappingSameSqlForwardCursorsCanCloseSecondThenFirst() {
        assertOverlappingSameSqlForwardCursors(closeFirstCursorFirst = false)
    }

    @Test
    fun returningConnectionRollsBackLeakedSavepoint() {
        SQLConnection(
            "file::memory:",
            SQLite,
            SQLiteJournalMode.MEMORY.databaseConfiguration,
            SQL_OPEN_READWRITE or SQL_OPEN_CREATE,
            CommonThreadLocalRandom,
            null
        ).use { connection ->
            connection.onBorrowed()
            connection.execute("SAVEPOINT leaked", emptyArray())
            assertFalse(connection.isAutoCommit)

            connection.onReturned()

            assertTrue(connection.isAutoCommit)
            assertEquals(1L, connection.executeForLong("SELECT 1", emptyArray()))
        }
    }

    @Test
    fun reborrowedConnectionIsCleanAfterLeakedTransaction() {
        val configuration = SQLiteJournalMode.MEMORY.databaseConfiguration
        val factory = SQLConnectionFactory("file::memory:", SQLite, configuration, CommonThreadLocalRandom, null)
        val executor = ScheduledThreadPoolExecutor(1)
        val pool = SingleObjectPool<String, CloseableSQLExecutor>(
            factory,
            executor,
            configuration.evictionDelayMillis,
            configuration.timeBetweenEvictionRunsMillis,
            configuration.borrowWaitTimeoutMillis
        )
        try {
            val leaked = pool.borrowObject() as SQLConnection
            leaked.onBorrowed()
            leaked.execute("BEGIN", emptyArray())
            assertFalse(leaked.isAutoCommit)
            returnConnection(pool, leaked)

            val reborrowed = pool.borrowObject() as SQLConnection
            reborrowed.onBorrowed()
            assertSame(leaked, reborrowed)
            assertTrue(reborrowed.isAutoCommit)
            assertEquals(1L, reborrowed.executeForLong("SELECT 1", emptyArray()))
            returnConnection(pool, reborrowed)
        } finally {
            pool.close()
            executor.shutdownNow()
        }
    }

    @Test
    fun batchRowsRequireExactArityAndPreparedStatementCanBeReusedAfterFailure() {
        SQLConnection(
            "file::memory:",
            SQLite,
            SQLiteJournalMode.MEMORY.databaseConfiguration,
            SQL_OPEN_READWRITE or SQL_OPEN_CREATE,
            CommonThreadLocalRandom,
            null
        ).use { connection ->
            connection.execute("CREATE TABLE batch_rows (id INTEGER, value TEXT)", emptyArray())
            val sql = "INSERT INTO batch_rows VALUES (?, ?)"

            assertFailsWith<IllegalArgumentException> {
                connection.executeBatchForChangedRowCount(
                    sql,
                    listOf(arrayOf<Any?>(1, "secret"), arrayOf(2))
                )
            }
            assertEquals(1L, connection.executeForLong("SELECT count(*) FROM batch_rows", emptyArray()))

            assertFailsWith<IllegalArgumentException> {
                connection.executeBatchForChangedRowCount(sql, sequenceOf(emptyArray()))
            }
            val overlongRows: Iterable<Array<out Any?>> = setOf(arrayOf<Any?>(2, "value", "extra"))
            assertFailsWith<IllegalArgumentException> {
                connection.executeBatchForChangedRowCount(sql, overlongRows)
            }
            assertFailsWith<IllegalArgumentException> {
                connection.executeBatchForChangedRowCount(sql, arrayOf(arrayOf(2)), 0, 1)
            }

            assertEquals(
                1,
                connection.executeBatchForChangedRowCount(sql, listOf(arrayOf(2, null)))
            )
            assertEquals(
                1L,
                connection.executeForLong(
                    "SELECT count(*) FROM batch_rows WHERE id = 2 AND value IS NULL",
                    emptyArray()
                )
            )
        }
    }

    @Test
    fun typedBatchRowsRequireExactArity() {
        SQLConnection(
            "file::memory:",
            SQLite,
            SQLiteJournalMode.MEMORY.databaseConfiguration,
            SQL_OPEN_READWRITE or SQL_OPEN_CREATE,
            CommonThreadLocalRandom,
            null
        ).use { connection ->
            connection.execute("CREATE TABLE typed_batch_rows (id INTEGER, value TEXT)", emptyArray())
            val sql = "INSERT INTO typed_batch_rows VALUES (?, ?)"

            assertFailsWith<IllegalArgumentException> {
                connection.executeBatchForChangedRowCountRows(sql, listOf(ParameterRow(1)))
            }
            val packedRows = ChunkedParameterRows(1).apply { add(ParameterRow(1)) }
            assertFailsWith<IllegalArgumentException> {
                connection.executeBatchForChangedRowCountRows(sql, packedRows)
            }
            assertEquals(0L, connection.executeForLong("SELECT count(*) FROM typed_batch_rows", emptyArray()))
        }
    }

    private fun returnConnection(
        pool: SingleObjectPool<String, CloseableSQLExecutor>,
        connection: SQLConnection
    ) {
        connection.onReturned()
        pool.returnObject(connection)
    }

    private fun assertOverlappingSameSqlForwardCursors(closeFirstCursorFirst: Boolean) {
        SQLConnection(
            "file::memory:",
            SQLite,
            SQLiteJournalMode.MEMORY.databaseConfiguration,
            SQL_OPEN_READWRITE or SQL_OPEN_CREATE,
            CommonThreadLocalRandom,
            null
        ).use { connection ->
            val sql = "SELECT ? UNION ALL SELECT ? + 1"
            val first = connection.executeForForwardCursor(sql, arrayOf(1, 1))
            val second = connection.executeForForwardCursor(sql, arrayOf(10, 10))
            try {
                assertTrue(first.moveToNext())
                assertEquals(1, first.getInt(0))
                assertTrue(second.moveToNext())
                assertEquals(10, second.getInt(0))

                val remaining = if (closeFirstCursorFirst) {
                    first.close()
                    second
                } else {
                    second.close()
                    first
                }
                assertTrue(remaining.moveToNext())
                assertEquals(if (closeFirstCursorFirst) { 11 } else { 2 }, remaining.getInt(0))
                remaining.close()
            } finally {
                first.close()
                second.close()
            }
        }
    }
}
