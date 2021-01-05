/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.RuleChain
import org.junit.rules.Timeout
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.IllegalStateException
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val databaseConfiguration = DatabaseConfiguration(
    busyTimeoutMillis = 2_000,
    evictionDelayMillis = 5_000L,
    maxConnectionPoolSize = 1,
    maxSqlCacheSize = 5,
    timeBetweenEvictionRunsMillis = 5_000L
)

internal class SQLDatabaseTest {
    @Rule
    @JvmField
    val rule: RuleChain = RuleChain.outerRule(DisableOnDebug(Timeout.seconds(10L)))

    @Mock lateinit var sqlite: SQLite

    private lateinit var database: SQLDatabase

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(sqlite.openV2(any(), any(), any())).thenAnswer {
            requireNotNull(it.arguments[2] as? LongArray)[0] = DB
            0
        }
        whenever(sqlite.prepareV2(any(), any(), any())).thenAnswer {
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
    fun nestedTransaction() = database.transact {
        transact { }
    }.also { verifyCommit() }

    @Test
    fun nestedTransactions() = database.transact {
        transact { transact { } }
    }.also { verifyCommit() }

    @Test
    fun badNestedTransactionThenGoodTransaction() {
        assertThatExceptionOfType(Exception::class.java).isThrownBy {
            database.transact { transact { error("uh-oh") } }
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
            assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
                exec("CREATE TABLE 'Foo' (bar INT)", emptyArray())
            }
        }
    }

    private fun verifyCommit(): Unit = inOrder(sqlite).run {
        verify(sqlite, times(1)).prepareV2(eq(DB), eq("END"), any())
        verify(sqlite, times(1)).stepWithoutThrowing(eq(STMT))
    }

    private fun verifyRollback(): Unit = inOrder(sqlite) {
        verify(sqlite, times(1)).prepareV2(eq(DB), eq("ROLLBACK"), any())
        verify(sqlite, times(1)).step(eq(STMT))
    }

    private companion object {
        const val DB = 1L
        const val STMT = 2L
    }
}
