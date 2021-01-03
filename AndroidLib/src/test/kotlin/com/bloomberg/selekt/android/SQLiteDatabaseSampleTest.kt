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

package com.bloomberg.selekt.android

import com.bloomberg.selekt.SQLiteJournalMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class SQLiteDatabaseSampleTest {
    private val database = SQLiteDatabase.openOrCreateDatabase(
        File(requireNotNull(javaClass.classLoader?.getResource("databases/sample.sqlite")?.file)),
        SQLiteJournalMode.WAL.databaseConfiguration,
        ByteArray(32) { 0x42 }
    )

    @Test
    fun readDatabase() {
        database.query("SELECT * FROM Foo", emptyArray()).use {
            assertTrue(it.moveToFirst())
        }
    }
}
