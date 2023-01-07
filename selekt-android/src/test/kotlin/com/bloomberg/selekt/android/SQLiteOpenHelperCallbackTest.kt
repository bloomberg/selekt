/*
 * Copyright 2022 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android

import android.database.sqlite.SQLiteException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.test.assertFailsWith

internal class SQLiteOpenHelperCallbackTest {
    @Test
    fun downgradeThrowsByDefault() {
        assertFailsWith<SQLiteException> {
            object : ISQLiteOpenHelper.Callback {
                override fun onCreate(database: SQLiteDatabase) = Unit

                override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            }.onDowngrade(mock(), 1, 0)
        }
    }
}
