/*
 * Copyright 2020 Bloomberg Finance L.P.
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
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val databaseConfiguration = DatabaseConfiguration(
    evictionDelayMillis = 5_000L,
    maxConnectionPoolSize = 1,
    maxSqlCacheSize = 5,
    timeBetweenEvictionRunsMillis = 5_000L
)

internal class SQLConnectionTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    @Mock lateinit var sqlite: SQLite

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(sqlite.openV2(any(), any(), any())).thenAnswer {
            requireNotNull(it.arguments[2] as? LongArray)[0] = DB
            0
        }
    }

    @Test
    fun exceptionInConstruction() {
        whenever(sqlite.busyTimeout(any(), any())) doThrow IllegalStateException()
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null)
        }
    }

    @Test
    fun isAutoCommit1() {
        whenever(sqlite.getAutocommit(any())) doReturn 1
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertTrue(it.isAutoCommit)
        }
    }

    @Test
    fun isAutoCommit2() {
        whenever(sqlite.getAutocommit(any())) doReturn 2
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertTrue(it.isAutoCommit)
        }
    }

    @Test
    fun isAutoCommitFalse() {
        whenever(sqlite.getAutocommit(any())) doReturn 0
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            assertFalse(it.isAutoCommit)
        }
    }

    @Test
    fun checkpointDefault() {
        SQLConnection("file::memory:", sqlite, databaseConfiguration, 0, CommonThreadLocalRandom, null).use {
            it.checkpoint()
            verify(sqlite, times(1)).walCheckpointV2(eq(DB), isNull(), eq(SQLCheckpointMode.PASSIVE()))
        }
    }

    private companion object {
        const val DB = 1L
    }
}
