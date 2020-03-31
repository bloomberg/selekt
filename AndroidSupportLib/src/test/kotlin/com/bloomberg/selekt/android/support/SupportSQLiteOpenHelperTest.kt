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

import com.bloomberg.selekt.android.ISQLiteOpenHelper
import com.bloomberg.selekt.android.SQLiteDatabase
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import kotlin.test.assertSame

internal class SupportSQLiteOpenHelperTest {
    @Test
    fun close() {
        mock<ISQLiteOpenHelper>().let {
            it.asSupportSQLiteOpenHelper().close()
            verify(it, times(1)).close()
        }
    }

    @Test
    fun databaseName() {
        val name = "foo"
        mock<ISQLiteOpenHelper>().let {
            whenever(it.databaseName) doReturn name
            assertSame(name, it.asSupportSQLiteOpenHelper().databaseName)
            verify(it, times(1)).databaseName
        }
    }

    @Test
    fun readableDatabase() {
        val database = mock<SQLiteDatabase>()
        mock<ISQLiteOpenHelper>().let {
            whenever(it.writableDatabase) doReturn database
            it.asSupportSQLiteOpenHelper().readableDatabase
            verify(it, times(1)).writableDatabase
        }
    }

    @Test
    fun setWriteAheadLoggingEnabledFalse() {
        val database = mock<SQLiteDatabase>()
        mock<ISQLiteOpenHelper>().let {
            whenever(it.writableDatabase) doReturn database
            it.asSupportSQLiteOpenHelper().setWriteAheadLoggingEnabled(false)
            verifyNoMoreInteractions(database)
        }
    }

    @Test
    fun setWriteAheadLoggingEnabledTrue() {
        val database = mock<SQLiteDatabase>()
        mock<ISQLiteOpenHelper>().let {
            whenever(it.writableDatabase) doReturn database
            it.asSupportSQLiteOpenHelper().setWriteAheadLoggingEnabled(true)
            verify(database, times(1)).journalMode
            verifyNoMoreInteractions(database)
        }
    }

    @Test
    fun writableDatabase() {
        val database = mock<SQLiteDatabase>()
        mock<ISQLiteOpenHelper>().let {
            whenever(it.writableDatabase) doReturn database
            it.asSupportSQLiteOpenHelper().writableDatabase
            verify(it, times(1)).writableDatabase
        }
    }
}
