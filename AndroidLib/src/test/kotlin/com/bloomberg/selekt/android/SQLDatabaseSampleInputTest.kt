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

import com.bloomberg.commons.deleteDatabase
import com.bloomberg.commons.times
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ArrayInDataClass")
internal data class SQLSampleInputs(
    val resourceFileName: String,
    val journalMode: SQLiteJournalMode,
    val key: ByteArray?
) {
    override fun toString() = resourceFileName
}

@RunWith(Parameterized::class)
internal class SQLDatabaseSampleTests(private val inputs: SQLSampleInputs) {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    companion object {
        @Parameters(name = "{0}")
        @JvmStatic
        fun initParameters(): Iterable<SQLSampleInputs> = (arrayOf(
            "html.json",
            "japanese.txt",
            "magic_spaces.txt",
            "sample.json",
            "sample.txt",
            "tricky.txt"
        ) * arrayOf(SQLiteJournalMode.DELETE)).map {
            SQLSampleInputs(it.first, it.second, ByteArray(32) { 0x42 })
        }
    }

    private val file = File.createTempFile("test-samples", ".db").also { it.deleteOnExit() }

    private val database = SQLDatabase(file.absolutePath, SQLite, inputs.journalMode.databaseConfiguration,
        inputs.key)

    @After
    fun tearDown() {
        database.run {
            try {
                close()
                assertFalse(isOpen())
            } finally {
                assertTrue(deleteDatabase(file))
            }
        }
    }

    @Test
    fun insertSample(): Unit = database.run {
        transact {
            exec("CREATE TABLE 'Foo' (bar TEXT)")
            val json = requireNotNull(javaClass.classLoader?.getResource(inputs.resourceFileName))
                .readText(StandardCharsets.UTF_8)
            insert("Foo", ContentValues().apply { put("bar", json) }, ConflictAlgorithm.REPLACE)
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use {
                assertTrue(it.moveToNext())
                assertEquals(json, it.getString(0))
                assertFalse(it.moveToNext())
            }
        }
    }
}
