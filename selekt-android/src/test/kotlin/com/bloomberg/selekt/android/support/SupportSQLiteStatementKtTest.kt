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

import com.bloomberg.selekt.ISQLStatement
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.Mock
import org.mockito.MockitoAnnotations

internal class SupportSQLiteStatementKtTest {
    @Mock
    lateinit var statement: ISQLStatement

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun asSupportSQLiteStatementBindBlob() {
        val blob = ByteArray(0)
        statement.asSupportSQLiteStatement().bindBlob(0, blob)
        verify(statement, times(1)).bindBlob(eq(0), same(blob))
    }

    @Test
    fun asSupportSQLiteStatementBindLong() {
        val value = 42L
        statement.asSupportSQLiteStatement().bindLong(0, value)
        verify(statement, times(1)).bindLong(eq(0), eq(value))
    }

    @Test
    fun asSupportSQLiteStatementBindDouble() {
        val value = 42.0
        statement.asSupportSQLiteStatement().bindDouble(0, value)
        verify(statement, times(1)).bindDouble(eq(0), eq(value))
    }

    @Test
    fun asSupportSQLiteStatementBindNull() {
        statement.asSupportSQLiteStatement().bindNull(0)
        verify(statement, times(1)).bindNull(eq(0))
    }

    @Test
    fun asSupportSQLiteStatementBindString() {
        val value = "hello"
        statement.asSupportSQLiteStatement().bindString(0, value)
        verify(statement, times(1)).bindString(eq(0), same(value))
    }

    @Test
    fun asSupportSQLiteStatementClearBindings() {
        statement.asSupportSQLiteStatement().clearBindings()
        verify(statement, times(1)).clearBindings()
    }

    @Test
    fun asSupportSQLiteStatementClose() {
        statement.asSupportSQLiteStatement().close()
        verify(statement, times(1)).close()
    }

    @Test
    fun asSupportSQLiteStatementExecute() {
        statement.asSupportSQLiteStatement().execute()
        verify(statement, times(1)).execute()
    }

    @Test
    fun asSupportSQLiteStatementExecuteInsert() {
        statement.asSupportSQLiteStatement().executeInsert()
        verify(statement, times(1)).executeInsert()
    }

    @Test
    fun asSupportSQLiteStatementExecuteUpdateDelete() {
        statement.asSupportSQLiteStatement().executeUpdateDelete()
        verify(statement, times(1)).executeUpdateDelete()
    }

    @Test
    fun asSupportSQLiteStatementSimpleQueryForLong() {
        statement.asSupportSQLiteStatement().simpleQueryForLong()
        verify(statement, times(1)).simpleQueryForLong()
    }

    @Test
    fun asSupportSQLiteStatementSimpleQueryForString() {
        statement.asSupportSQLiteStatement().simpleQueryForString()
        verify(statement, times(1)).simpleQueryForString()
    }
}
