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

import android.content.ContentValues
import android.content.Context
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.android.ConflictAlgorithm
import com.bloomberg.selekt.android.ISQLiteOpenHelper
import com.bloomberg.selekt.android.SQLiteOpenHelper
import com.bloomberg.selekt.android.SQLiteDatabase
import com.bloomberg.selekt.android.SQLiteOpenParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    context: Context,
    journalMode: SQLiteJournalMode
): ISQLiteOpenHelper = SQLiteOpenHelper(
    context,
    ISQLiteOpenHelper.Configuration(
        callback = SQLiteSupportOpenHelperCallback,
        key = "a".repeat(32).toByteArray(StandardCharsets.UTF_8),
        name = "test"
    ),
    1,
    SQLiteOpenParams(journalMode)
)

private object SQLiteSupportOpenHelperCallback : ISQLiteOpenHelper.Callback {
    override fun onCreate(database: SQLiteDatabase) = database.exec("CREATE TABLE 'Foo' (bar INT)")

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

data class Inputs(
    val description: String,
    val journalMode: SQLiteJournalMode
) {
    override fun toString() = description
}

@LargeTest
@RunWith(Parameterized::class)
internal class SQLiteJournalModeDatabaseBenchmark(inputs: Inputs) {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val databaseHelper = inputs.run { createSQLiteOpenHelper(targetContext, journalMode) }

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun initParameters(): Iterable<Inputs> = arrayOf(SQLiteJournalMode.DELETE, SQLiteJournalMode.WAL).run {
            List(size) { this[it].let { mode -> Inputs(mode.name, mode) } }
        }
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
    fun singleQuery(): Unit = databaseHelper.writableDatabase.run {
        insert("Foo", ContentValues().apply { put("bar", 42) }, ConflictAlgorithm.REPLACE)
        benchmarkRule.measureRepeated {
            query(false, "Foo", null, null, null).use { }
        }
    }

    @Test
    fun insertInt(): Unit = databaseHelper.writableDatabase.run {
        val arg = Array(1) { 0 }
        benchmarkRule.measureRepeated {
            transact {
                for (i in 1..100) {
                    arg[0] = i
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (?)", arg)
                }
            }
        }
    }

    @Test
    fun insertIntWithOnConflict(): Unit = databaseHelper.writableDatabase.run {
        val values = ContentValues()
        benchmarkRule.measureRepeated {
            transact {
                for (i in 1..100) {
                    values.put("bar", i)
                    insert("Foo", values, ConflictAlgorithm.REPLACE)
                }
            }
        }
    }

    @Test
    fun insertIntWithStatement(): Unit = databaseHelper.writableDatabase.run {
        val statement = compileStatement("INSERT OR REPLACE INTO 'Foo' VALUES (?)")
        benchmarkRule.measureRepeated {
            transact {
                for (i in 1L..100L) {
                    statement.bindLong(1, i)
                    statement.executeInsert()
                }
            }
        }
    }

    @Test
    fun insertFiveUnboundIntsSequentiallyWithOnConflict(): Unit = databaseHelper.writableDatabase.run {
        benchmarkRule.measureRepeated {
            transact {
                repeat(100) {
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (0)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (1)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (2)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (3)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (4)")
                }
            }
        }
    }

    @Test
    fun insertTwentyUnboundIntsSequentiallyWithOnConflict(): Unit = databaseHelper.writableDatabase.run {
        benchmarkRule.measureRepeated {
            transact {
                repeat(100) {
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (0)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (1)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (2)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (3)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (4)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (5)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (6)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (7)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (8)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (9)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (10)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (11)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (12)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (13)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (14)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (15)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (16)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (17)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (18)")
                    exec("INSERT OR REPLACE INTO 'Foo' VALUES (19)")
                }
            }
        }
    }

    @OptIn(com.bloomberg.selekt.Experimental::class)
    @Test
    fun batchInsertInt(): Unit = databaseHelper.writableDatabase.run {
        val args = Array(1) { 0 }
        benchmarkRule.measureRepeated {
            batch("INSERT OR REPLACE INTO 'Foo' VALUES (?)", sequence {
                for (i in 1..100) {
                    args[0] = i
                    yield(args)
                }
            })
        }
    }

    @Test
    fun queryAndInsertIntWithOnConflict(): Unit = databaseHelper.writableDatabase.run {
        val values = ContentValues()
        transact {
            for (i in 1..100) {
                values.put("bar", i)
                insert("Foo", values, ConflictAlgorithm.REPLACE)
            }
        }
        benchmarkRule.measureRepeated {
            runBlocking(Dispatchers.IO) {
                coroutineScope {
                    launch {
                        transact {
                            for (i in 1..100) {
                                values.put("bar", i)
                                insert("Foo", values, ConflictAlgorithm.REPLACE)
                            }
                        }
                    }
                    launch {
                        query(false, "Foo", emptyArray(), "", emptyArray(), limit = 100)
                    }
                }
            }
        }
    }

    @Test
    fun multiQueryAndInsertIntWithOnConflict(): Unit = databaseHelper.writableDatabase.run {
        val values = ContentValues()
        transact {
            for (i in 1..100) {
                values.put("bar", i)
                insert("Foo", values, ConflictAlgorithm.REPLACE)
            }
        }
        benchmarkRule.measureRepeated {
            runBlocking(Dispatchers.IO) {
                coroutineScope {
                    launch {
                        transact {
                            for (i in 1..100) {
                                values.put("bar", i)
                                insert("Foo", values, ConflictAlgorithm.REPLACE)
                            }
                        }
                    }
                    repeat(4) {
                        launch {
                            query(false, "Foo", emptyArray(), "", emptyArray(), limit = 100)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun multiQuery(): Unit = databaseHelper.writableDatabase.run {
        benchmarkRule.measureRepeated {
            runBlocking(Dispatchers.IO) {
                coroutineScope {
                    repeat(4) {
                        launch {
                            query(false, "Foo", emptyArray(), "", emptyArray(), limit = 100)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun queryLargeEntryWhileInsertWithOnConflict(): Unit = databaseHelper.writableDatabase.run {
        val values = ContentValues().apply { put("bar", "a".repeat(10_000)) }
        insert("Foo", values, ConflictAlgorithm.REPLACE)
        benchmarkRule.measureRepeated {
            runBlocking(Dispatchers.IO) {
                coroutineScope {
                    launch {
                        transact {
                            for (i in 1..100) {
                                insert("Foo", values, ConflictAlgorithm.REPLACE)
                            }
                        }
                    }
                    launch {
                        query(false, "Foo", emptyArray(), "", emptyArray(), limit = 100)
                    }
                }
            }
        }
    }
}
