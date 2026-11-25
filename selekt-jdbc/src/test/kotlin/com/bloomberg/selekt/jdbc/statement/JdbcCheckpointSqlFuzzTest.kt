/*
 * Copyright 2026 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.jdbc.statement

import com.bloomberg.selekt.jdbc.FUZZ_TEST_TIMEOUT_MINUTES
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.driver.SelektDataSource
import com.code_intelligence.jazzer.junit.FuzzTest
import java.sql.SQLException
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.provider.MethodSource

@Suppress("MagicNumber")
@Timeout(value = FUZZ_TEST_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
internal class JdbcCheckpointSqlFuzzTest {
    @MethodSource("inputs")
    @FuzzTest
    fun fuzzCheckpointSql(input: ByteArray) {
        val sql = CheckpointSyntax(input).sql()
        val source = SelektDataSource().apply {
            databasePath = ":memory:"
            journalMode = "MEMORY"
            maxPoolSize = 1
        }
        try {
            source.connection.use { connection ->
                connection as JdbcConnection
                // Preparing without stepping uses SQLite's parser as an independent classification oracle.
                val checkpoint = try {
                    connection.withSession { prepare(sql).use { it.columnNames().toList() == CHECKPOINT_COLUMNS } }
                } catch (exception: SQLException) {
                    return
                }
                assertEquals(checkpoint, sql.isWalCheckpoint(), sql)
                if (checkpoint) {
                    connection.verifyCheckpoint(sql)
                }
            }
        } finally {
            source.close()
        }
    }

    private fun JdbcConnection.verifyCheckpoint(sql: String) {
        autoCommit = false
        prepareStatement(sql).use { statement ->
            assertTrue(statement.execute(), sql)
            assertNotNull(statement.resultSet).use { result ->
                val metadata = result.metaData
                assertEquals(CHECKPOINT_COLUMNS, (1..metadata.columnCount).map(metadata::getColumnName))
                assertTrue(result.next())
                assertEquals(0, result.getInt(1))
                assertFalse(result.next())
            }
        }
        assertFalse(withSession { inTransaction }, sql)
    }

    companion object {
        private val CHECKPOINT_COLUMNS = listOf("busy", "log", "checkpointed")

        @JvmStatic
        fun inputs(): Stream<ByteArray> = Stream.concat(
            Stream.of(byteArrayOf()),
            IntStream.range(0, 32).mapToObj { seed -> ByteArray(16) { (seed * 17 + it).toByte() } }
        )
    }
}

/** Generates bounded SQL from a grammar of checkpoint and harmless non-checkpoint statements. */
private class CheckpointSyntax(private val input: ByteArray) {
    private var index = 0

    fun sql(): String {
        val prefix = choose("", ";;", "\uFEFF;/* before */;", "SELECT 42;", "PRAGMA user_version;", "PRAGMA main;")
        val keyword = choose("PRAGMA", "pragma", "PrAgMa")
        val beforeName = separator()
        val schema = choose("", "main", "\"main\"", "[main]", "`main`", "'main'", "\"ma\"\"in\"")
        val qualification = if (schema.isEmpty()) "" else schema + separator() + "." + separator()
        val name = choose(
            "wal_checkpoint", "WAL_CHECKPOINT", "\"wal_checkpoint\"", "[wal_checkpoint]", "`wal_checkpoint`",
            "'wal_checkpoint'", "user_version", "wal_checkpoint_extra", "'wal_check''point'", "'wal_checkpoint;extra'",
            "wal_checkpoint\uFEFF", "wal_checkpo\u0131nt", "wal_chec\u212Apoint"
        )
        val beforeArgument = separator()
        val argument = choose("(TRUNCATE)", "=TRUNCATE", "", "(PASSIVE)", "/* mode */(FULL)")
        val tail = choose("", ";", "; PRAGMA user_version=23", "; PRAGMA wal_checkpoint(TRUNCATE)")
        return prefix + keyword + beforeName + qualification + name + beforeArgument + argument + tail
    }

    private fun separator(): String = choose(
        " ", "\t", "\n", "\r", "\u000C", "\uFEFF", "/* ; 'quoted' */", "-- ; comment\r\n", ";", "", "-- comment\n",
        "-- comment\r", "\u2003", "\u000B"
    )

    private fun choose(vararg choices: String): String {
        val value = input.getOrElse(index++) { 0 }.toInt() and 0xFF
        return choices[value % choices.size]
    }
}
