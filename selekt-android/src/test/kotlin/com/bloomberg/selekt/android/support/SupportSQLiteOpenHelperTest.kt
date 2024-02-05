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

import com.bloomberg.selekt.android.ISQLiteOpenHelper
import com.bloomberg.selekt.android.SQLiteDatabase
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import kotlin.test.assertSame

internal class SupportSQLiteOpenHelperTest {
    private val database = mock<SQLiteDatabase>()
    private val helper = mock<ISQLiteOpenHelper>().apply {
        whenever(writableDatabase) doReturn database
    }

    @Test
    fun close() {
        helper.let {
            it.asSupportSQLiteOpenHelper().close()
            verify(it, times(1)).close()
        }
    }

    @Test
    fun databaseName() {
        val name = "foo"
        helper.let {
            whenever(it.databaseName) doReturn name
            assertSame(name, it.asSupportSQLiteOpenHelper().databaseName)
            verify(it, times(1)).databaseName
        }
    }

    @Test
    fun readableDatabase() {
        helper.let {
            it.asSupportSQLiteOpenHelper().readableDatabase
            verify(it, times(1)).writableDatabase
        }
    }

    @Test
    fun setWriteAheadLoggingEnabledFalse() {
        helper.let {
            it.asSupportSQLiteOpenHelper().setWriteAheadLoggingEnabled(false)
            verifyNoMoreInteractions(database)
        }
    }

    @Test
    fun setWriteAheadLoggingEnabledTrue() {
        helper.let {
            it.asSupportSQLiteOpenHelper().setWriteAheadLoggingEnabled(true)
            verify(database, times(1)).journalMode
            verifyNoMoreInteractions(database)
        }
    }

    @Test
    fun writableDatabase() {
        helper.let {
            it.asSupportSQLiteOpenHelper().writableDatabase
            verify(it, times(1)).writableDatabase
        }
    }
}
