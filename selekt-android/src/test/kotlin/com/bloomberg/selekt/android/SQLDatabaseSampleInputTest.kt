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

package com.bloomberg.selekt.android

import com.bloomberg.selekt.commons.times
import com.bloomberg.selekt.ContentValues
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.nio.charset.StandardCharsets
import java.util.stream.Stream
import kotlin.io.path.createTempFile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ArrayInDataClass")
internal data class SQLSampleInputs(
    val resourceFileName: String,
    val journalMode: SQLiteJournalMode,
    val key: ByteArray?
) {
    override fun toString() = "$resourceFileName-$journalMode-${if (key != null) { "keyed" } else { null }}"
}

internal class SampleArgumentsProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> = (arrayOf(
        "html.json",
        "japanese.txt",
        "magic_spaces.txt",
        "sample.json",
        "sample.txt",
        "tricky.txt"
    ) * arrayOf(SQLiteJournalMode.DELETE)).map {
        SQLSampleInputs(it.first, it.second, ByteArray(32) { 0x42 })
    }.stream().map { Arguments.of(it) }
}

private fun createFile(
    input: SQLSampleInputs
) = createTempFile("test-samples-$input-", ".db").toFile().apply { deleteOnExit() }

internal class SQLDatabaseSampleTests {
    @ParameterizedTest
    @ArgumentsSource(SampleArgumentsProvider::class)
    fun insertSample(
        inputs: SQLSampleInputs
    ): Unit = SQLDatabase(
        createFile(inputs).absolutePath,
        SQLite,
        inputs.journalMode.databaseConfiguration,
        key = null
    ).use {
        it.transact {
            exec("CREATE TABLE 'Foo' (bar TEXT)")
            val json = javaClass.classLoader!!.getResource(inputs.resourceFileName).readText(StandardCharsets.UTF_8)
            insert("Foo", ContentValues().apply { put("bar", json) }, ConflictAlgorithm.REPLACE)
            query(false, "Foo", arrayOf("bar"), "", emptyArray(), null, null, null, null).use { cursor ->
                assertTrue(cursor.moveToNext())
                assertEquals(json, cursor.getString(0))
                assertFalse(cursor.moveToNext())
            }
        }
    }
}
