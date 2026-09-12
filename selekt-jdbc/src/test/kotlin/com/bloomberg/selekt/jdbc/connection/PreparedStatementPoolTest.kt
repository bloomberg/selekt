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

package com.bloomberg.selekt.jdbc.connection

import com.bloomberg.selekt.jdbc.statement.JdbcPreparedStatement
import java.sql.ResultSet
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class PreparedStatementPoolTest {
    @Test
    fun putAndTakeWithoutAllocatingLookupKeys() {
        val pool = PreparedStatementPool(2)
        val statement = statement("SELECT 1")
        assertNull(pool.take("SELECT 1", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
        assertNull(pool.put(statement))
        assertEquals(1, pool.size)
        assertSame(statement, pool.take("SELECT 1", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
        assertTrue(pool.isEmpty())
        assertNull(pool.take("SELECT 1", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
    }

    @Test
    fun lookupIncludesEveryResultSetCharacteristic() {
        val pool = PreparedStatementPool(5)
        val forwardClose = statement("SELECT 1", FORWARD_ONLY, CLOSE_AT_COMMIT)
        val forwardHold = statement("SELECT 1", FORWARD_ONLY, HOLD_OVER_COMMIT)
        val scrollClose = statement("SELECT 1", SCROLL_INSENSITIVE, CLOSE_AT_COMMIT)
        val scrollHold = statement("SELECT 1", SCROLL_INSENSITIVE, HOLD_OVER_COMMIT)
        val updatable = statement("SELECT 1", resultSetConcurrency = UPDATABLE)
        listOf(forwardClose, forwardHold, scrollClose, scrollHold, updatable).forEach { pool.put(it) }
        assertSame(scrollHold, pool.take("SELECT 1", SCROLL_INSENSITIVE, READ_ONLY, HOLD_OVER_COMMIT))
        assertSame(updatable, pool.take("SELECT 1", FORWARD_ONLY, UPDATABLE, CLOSE_AT_COMMIT))
        assertSame(forwardClose, pool.take("SELECT 1", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
        assertSame(scrollClose, pool.take("SELECT 1", SCROLL_INSENSITIVE, READ_ONLY, CLOSE_AT_COMMIT))
        assertSame(forwardHold, pool.take("SELECT 1", FORWARD_ONLY, READ_ONLY, HOLD_OVER_COMMIT))
        assertTrue(pool.isEmpty())
    }

    @Test
    fun equalKeyReplacesExistingStatement() {
        val pool = PreparedStatementPool(2)
        val first = statement("SELECT 1")
        val replacement = statement("SELECT 1")
        assertNull(pool.put(first))
        assertSame(first, pool.put(replacement))
        assertEquals(1, pool.size)
        assertSame(replacement, pool.take("SELECT 1", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
    }

    @Test
    fun capacityEvictsEldestStatement() {
        val pool = PreparedStatementPool(2)
        val first = statement("SELECT 1")
        val second = statement("SELECT 2")
        val third = statement("SELECT 3")
        pool.put(first)
        pool.put(second)
        assertSame(first, pool.put(third))
        assertNull(pool.take("SELECT 1", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
        assertSame(second, pool.take("SELECT 2", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
        assertSame(third, pool.take("SELECT 3", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
    }

    @Test
    fun hashCollisionsRetainDistinctStatements() {
        val pool = PreparedStatementPool(2)
        val first = statement("Aa")
        val second = statement("BB")
        pool.put(first)
        pool.put(second)
        assertSame(first, pool.take("Aa", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
        assertSame(second, pool.take("BB", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
    }

    @Test
    fun evictionUnlinksEldestFromCollisionChain() {
        val pool = PreparedStatementPool(2)
        val first = statement("Aa")
        val second = statement("BB")
        val third = statement("SELECT 3")
        pool.put(first)
        pool.put(second)
        assertSame(first, pool.put(third))
        assertSame(second, pool.take("BB", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
        assertSame(third, pool.take("SELECT 3", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
    }

    @Test
    fun drainPreservesOrderAndResetsPool() {
        val pool = PreparedStatementPool(2)
        val first = statement("SELECT 1")
        val second = statement("SELECT 2")
        val afterReset = statement("SELECT 3")
        pool.put(first)
        pool.put(second)
        assertEquals(listOf(first, second), pool.drain())
        assertTrue(pool.isEmpty())
        assertNull(pool.put(afterReset))
        assertSame(afterReset, pool.take("SELECT 3", FORWARD_ONLY, READ_ONLY, CLOSE_AT_COMMIT))
    }

    @Test
    fun capacityMustBePositive() {
        assertFailsWith<IllegalArgumentException> { PreparedStatementPool(0) }
    }

    private fun statement(
        sql: String,
        resultSetType: Int = FORWARD_ONLY,
        resultSetHoldability: Int = CLOSE_AT_COMMIT,
        resultSetConcurrency: Int = READ_ONLY
    ): JdbcPreparedStatement = mock {
        on { this.sql } doReturn sql
        on { this.resultSetType } doReturn resultSetType
        on { this.resultSetConcurrency } doReturn resultSetConcurrency
        on { this.resultSetHoldability } doReturn resultSetHoldability
    }

    private companion object {
        private const val FORWARD_ONLY = ResultSet.TYPE_FORWARD_ONLY
        private const val SCROLL_INSENSITIVE = ResultSet.TYPE_SCROLL_INSENSITIVE
        private const val READ_ONLY = ResultSet.CONCUR_READ_ONLY
        private const val UPDATABLE = ResultSet.CONCUR_UPDATABLE
        private const val CLOSE_AT_COMMIT = ResultSet.CLOSE_CURSORS_AT_COMMIT
        private const val HOLD_OVER_COMMIT = ResultSet.HOLD_CURSORS_OVER_COMMIT
    }
}
