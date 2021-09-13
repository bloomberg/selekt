/*
 * Copyright 2021 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android.benchmark

import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.bloomberg.selekt.annotations.Experimental
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.ZeroBlob
import com.bloomberg.selekt.android.ISQLiteOpenHelper
import com.bloomberg.selekt.android.SQLiteDatabase
import com.bloomberg.selekt.android.SQLiteOpenHelper
import com.bloomberg.selekt.android.SQLiteOpenParams
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun createSQLiteOpenHelper(
    context: Context
): ISQLiteOpenHelper = SQLiteOpenHelper(
    context,
    ISQLiteOpenHelper.Configuration(
        callback = BlobSQLiteSupportOpenHelperCallback,
        key = "a".repeat(32).toByteArray(StandardCharsets.UTF_8),
        name = "test-index"
    ),
    1,
    SQLiteOpenParams(SQLiteJournalMode.WAL)
)

private object BlobSQLiteSupportOpenHelperCallback : ISQLiteOpenHelper.Callback {
    override fun onCreate(database: SQLiteDatabase) {
        database.apply {
            exec("CREATE TABLE 'Foo' (data BLOB)")
            exec("INSERT INTO 'Foo' VALUES (?)", arrayOf(ZeroBlob(10 * 1_000_000)))
        }
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

data class BlobInputs(
    private val description: String,
    val numberOfBytes: Int
) {
    override fun toString() = description
}

@LargeTest
@RunWith(Parameterized::class)
internal class SQLiteDatabaseBlobBenchmark(private val inputs: BlobInputs) {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val databaseHelper = inputs.run { createSQLiteOpenHelper(targetContext) }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun initParameters(): Iterable<BlobInputs> = arrayOf(
            BlobInputs("tiny", 100),
            BlobInputs("small", 16 * 1_000),
            BlobInputs("medium", 500 * 1_000),
            BlobInputs("large", 10 * 1_000_000)
        ).asIterable()
    }

    @After
    fun tearDown() {
        databaseHelper.writableDatabase.run {
            try {
                close()
                assertFalse(isOpen)
            } finally {
                assertTrue(SQLiteDatabase.deleteDatabase(targetContext.getDatabasePath(databaseHelper.databaseName)))
            }
        }
    }

    @OptIn(Experimental::class)
    @Test
    fun readBlob() {
        ByteArray(inputs.numberOfBytes) { 0x42 }.inputStream().use {
            databaseHelper.writableDatabase.writeToBlob("Foo", "data", 1L, 0, it)
        }
        benchmarkRule.measureRepeated {
            ByteArrayOutputStream(inputs.numberOfBytes).use {
                databaseHelper.writableDatabase.readFromBlob("Foo", "data", 1L, 0, inputs.numberOfBytes, it)
            }
        }
    }

    @OptIn(Experimental::class)
    @Test
    fun writeBlob() {
        val blob = ByteArray(inputs.numberOfBytes) { 0x42 }
        benchmarkRule.measureRepeated {
            blob.inputStream().use {
                databaseHelper.writableDatabase.writeToBlob("Foo", "data", 1L, 0, it)
            }
        }
    }
}
