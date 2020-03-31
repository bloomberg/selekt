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

package com.bloomberg.selekt.android.support

import androidx.sqlite.db.SupportSQLiteQuery
import com.bloomberg.selekt.ISQLProgram
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class SupportSQLiteQueryKtTest {
    @Mock lateinit var query: SupportSQLiteQuery

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
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
