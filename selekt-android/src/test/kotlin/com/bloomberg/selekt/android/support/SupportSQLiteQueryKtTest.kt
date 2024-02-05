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

package com.bloomberg.selekt.android.support

import androidx.sqlite.db.SupportSQLiteQuery
import com.bloomberg.selekt.ISQLProgram
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class SupportSQLiteQueryKtTest {
    @Mock lateinit var query: SupportSQLiteQuery

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        doReturn(42).whenever(query).argCount
        doReturn("SELECT * FROM Foo").whenever(query).sql
    }

    @Test
    fun asSelektSQLQueryArgCount() {
        val argCount = query.asSelektSQLQuery().argCount
        verify(query, times(1)).argCount
        assertEquals(query.argCount, argCount)
    }

    @Test
    fun asSelektSQLQuerySql() {
        val sql = query.asSelektSQLQuery().sql
        verify(query, times(1)).sql
        assertSame(query.sql, sql)
    }

    @Test
    fun asSelektSQLQueryBindTo() {
        val program = mock<ISQLProgram>()
        query.asSelektSQLQuery().bindTo(program)
        verify(query, atLeastOnce()).bindTo(any())
    }
}
