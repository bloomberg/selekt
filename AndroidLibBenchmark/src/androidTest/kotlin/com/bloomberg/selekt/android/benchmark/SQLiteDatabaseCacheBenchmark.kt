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

package com.bloomberg.selekt.android.benchmark

import android.content.ContentValues
import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.android.ConflictAlgorithm
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
import java.nio.charset.StandardCharsets
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun createSQLiteOpenHelper(
    context: Context
): ISQLiteOpenHelper = SQLiteOpenHelper(
    context,
    ISQLiteOpenHelper.Configuration(
        callback = CacheSQLiteSupportOpenHelperCallback,
        key = "a".repeat(32).toByteArray(StandardCharsets.UTF_8),
        name = "test-cache"
    ),
    1,
    SQLiteOpenParams(SQLiteJournalMode.WAL)
)

private object CacheSQLiteSupportOpenHelperCallback : ISQLiteOpenHelper.Callback {
    override fun onCreate(database: SQLiteDatabase): Unit = database.run {
        val values = ContentValues()
        transact {
            exec("CREATE TABLE 'Foo' (bar INT)")
            for (i in 0 until 10_000) {
                values.put("bar", i)
                assertTrue(insert("Foo", values, ConflictAlgorithm.REPLACE) > 0)
            }
            exec("CREATE INDEX 'FooIndex' ON 'Foo' (bar)")
        }
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

data class CacheInputs(
    private val description: String,
    val statements: Iterable<Pair<String, Array<String?>>>
) {
    override fun toString() = description
}

@LargeTest
@RunWith(Parameterized::class)
internal class SQLiteDatabaseCacheBenchmark(private val inputs: CacheInputs) {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val databaseHelper = inputs.run { createSQLiteOpenHelper(targetContext) }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun initParameters(): Iterable<CacheInputs> = arrayOf(
            CacheInputs("reuse", Array(10_000) {
                Pair("SELECT * FROM 'Foo' WHERE bar=?", arrayOf<String?>("$it"))
            }.asIterable()),
            CacheInputs(
                "waste",
                Array(10_000) {
                    Pair("SELECT * FROM 'Foo' WHERE bar=$it", arrayOf<String?>())
                }.asIterable()
            )
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

    @Test
    fun simpleSelectPreparedStatementCache() {
        benchmarkRule.measureRepeated {
            databaseHelper.writableDatabase.run {
                inputs.statements.forEach {
                    query(it.first, it.second)
                }
            }
        }
    }
}
