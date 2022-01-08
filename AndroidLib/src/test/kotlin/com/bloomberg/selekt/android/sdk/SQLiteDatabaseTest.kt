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

package com.bloomberg.selekt.android.sdk

import android.database.sqlite.SQLiteDatabase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class SQLiteDatabaseTest {
    @Test
    fun beginTransactionWithRawQuery(): Unit = SQLiteDatabase.create(null).run {
        execSQL("CREATE TABLE Foo (x INT)")
        execSQL("INSERT INTO Foo VALUES (42)")
        rawQuery("BEGIN TRANSACTION", null).use {
            assertFalse(it.moveToFirst())
        }
        assertTrue(inTransaction())
    }
}
