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

package com.bloomberg.selekt.jdbc.metadata

import com.bloomberg.selekt.jdbc.FUZZ_TEST_TIMEOUT_MINUTES
import com.bloomberg.selekt.jdbc.driver.SelektDataSource
import com.code_intelligence.jazzer.junit.FuzzTest
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.math.min
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.provider.MethodSource

@Suppress("MagicNumber")
@Timeout(value = FUZZ_TEST_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
internal class JdbcMetadataSqlIsolationFuzzTest {
    @MethodSource("inputs")
    @FuzzTest
    fun fuzzMetadataSqlIsolation(input: ByteArray) {
        if (input.size > MAX_INPUT_SIZE) {
            return
        }

        val fuzzInput = MetadataFuzzInput.from(input)
        val dataSource = SelektDataSource().apply {
            databasePath = ":memory:"
            journalMode = "MEMORY"
            maxPoolSize = 1
        }
        try {
            dataSource.verifySqlIsolation(fuzzInput)
        } finally {
            dataSource.close()
        }
        assertTrue(dataSource.isClosed())
    }

    private fun SelektDataSource.verifySqlIsolation(input: MetadataFuzzInput) = connection.use { connection ->
        connection.createAdversarialSchema()
        val expectedState = connection.securityState()
        try {
            connection.exerciseMetadata(input)
        } catch (exception: SQLException) {
            // SQLite rejects SQL text containing NUL. Rejection is safe provided that it has no side effects.
            if (!input.containsNul) {
                throw exception
            }
        }
        assertEquals(expectedState, connection.securityState())
    }

    private fun Connection.exerciseMetadata(input: MetadataFuzzInput) {
        when (input.operation) {
            MetadataOperation.TABLES -> metaData.getTables(null, null, input.combined, null).use { result ->
                val matcher = JdbcPatternMatcher(input.combined)
                result.consumeDistinctRows("TABLE_NAME") {
                    val tableName = getString("TABLE_NAME")
                    assertEquals(KNOWN_TABLE_TYPES[tableName], getString("TABLE_TYPE"))
                    assertTrue(matcher.matches(tableName), "Metadata pattern returned non-matching table '$tableName'.")
                    tableName
                }
            }
            MetadataOperation.COLUMNS -> {
                val tablePattern = input.first.ifEmpty { null }
                val columnPattern = input.second.ifEmpty { null }
                val tableMatcher = tablePattern?.let(::JdbcPatternMatcher)
                val columnMatcher = columnPattern?.let(::JdbcPatternMatcher)
                metaData.getColumns(null, null, tablePattern, columnPattern).use { result ->
                    result.consumeDistinctRows("TABLE_NAME/COLUMN_NAME") {
                        val tableName = getString("TABLE_NAME")
                        val columnName = getString("COLUMN_NAME")
                        val knownColumn = KNOWN_COLUMNS[tableName]?.get(columnName)
                        assertTrue(knownColumn != null, "Metadata returned unknown column '$tableName.$columnName'.")
                        assertTrue(tableMatcher?.matches(tableName) != false)
                        assertTrue(columnMatcher?.matches(columnName) != false)
                        assertEquals(knownColumn.defaultExpression, getString("COLUMN_DEF"))
                        "$tableName\u0000$columnName"
                    }
                }
            }
            MetadataOperation.PRIMARY_KEYS -> metaData.getPrimaryKeys(null, null, input.combined).use { result ->
                val matchingTable = KNOWN_PRIMARY_KEYS.keys.firstOrNull { it.equals(input.combined, ignoreCase = true) }
                result.consumeDistinctRows("COLUMN_NAME") {
                    assertEquals(input.combined, getString("TABLE_NAME"))
                    val columnName = getString("COLUMN_NAME")
                    assertTrue(columnName in KNOWN_PRIMARY_KEYS[matchingTable].orEmpty())
                    assertTrue(getInt("KEY_SEQ") > 0)
                    columnName
                }
            }
            MetadataOperation.INDEXES -> metaData.getIndexInfo(
                null,
                null,
                input.combined,
                false,
                false
            ).use { result ->
                val matchingTable = KNOWN_INDEXES.keys.firstOrNull { it.equals(input.combined, ignoreCase = true) }
                result.consumeDistinctRows("INDEX_NAME") {
                    assertEquals(input.combined, getString("TABLE_NAME"))
                    val indexName = getString("INDEX_NAME")
                    assertTrue(indexName in KNOWN_INDEXES[matchingTable].orEmpty())
                    indexName
                }
            }
        }
    }

    private inline fun ResultSet.consumeDistinctRows(label: String, key: ResultSet.() -> String) {
        val seen = mutableSetOf<String>()
        while (next()) {
            assertTrue(seen.add(key()), "Metadata returned a duplicate $label row.")
        }
    }

    private fun Connection.createAdversarialSchema() {
        createStatement().use { statement ->
            statement.executeUpdate("CREATE TABLE guard (id INTEGER PRIMARY KEY, token TEXT NOT NULL)")
            statement.executeUpdate("CREATE TABLE ordinary (id INTEGER PRIMARY KEY, value TEXT DEFAULT 'ordinary')")
            statement.executeUpdate("CREATE INDEX ordinary_value_idx ON ordinary(value)")
            statement.executeUpdate(
                "CREATE TABLE ${quoteIdentifier(ADVERSARIAL_TABLE)} (" +
                    "${quoteIdentifier(ADVERSARIAL_PRIMARY_KEY)} INTEGER PRIMARY KEY," +
                    "${quoteIdentifier(ADVERSARIAL_COLUMN)} TEXT DEFAULT ${quoteLiteral(ADVERSARIAL_DEFAULT)})"
            )
            statement.executeUpdate(
                "CREATE INDEX ${quoteIdentifier(ADVERSARIAL_INDEX)} " +
                    "ON ${quoteIdentifier(ADVERSARIAL_TABLE)}(${quoteIdentifier(ADVERSARIAL_COLUMN)})"
            )
            statement.executeUpdate(
                "CREATE VIEW ${quoteIdentifier(ADVERSARIAL_VIEW)} AS SELECT id, value FROM ordinary"
            )
        }
        prepareStatement("INSERT INTO guard VALUES (1, ?)").use { statement ->
            statement.setString(1, GUARD_TOKEN)
            assertEquals(1, statement.executeUpdate())
        }
    }

    private fun Connection.securityState(): SecurityState {
        val schema = mutableListOf<SchemaEntry>()
        createStatement().use { statement ->
            statement.executeQuery(
                "SELECT location, type, name, tbl_name, COALESCE(sql, '') FROM (" +
                    "SELECT 'main' AS location, type, name, tbl_name, sql FROM sqlite_schema " +
                    "UNION ALL " +
                    "SELECT 'temp' AS location, type, name, tbl_name, sql FROM sqlite_temp_schema" +
                    ") ORDER BY location, type, name, tbl_name"
            ).use { result ->
                while (result.next()) {
                    schema += SchemaEntry(
                        result.getString(1),
                        result.getString(2),
                        result.getString(3),
                        result.getString(4),
                        result.getString(5)
                    )
                }
            }
        }
        val attachedDatabases = mutableListOf<Pair<String, String>>()
        createStatement().use { statement ->
            statement.executeQuery("PRAGMA database_list").use { result ->
                while (result.next()) {
                    attachedDatabases += result.getString("name") to result.getString("file")
                }
            }
        }
        val pragmas = SECURITY_PRAGMAS.associateWith { pragma ->
            createStatement().use { statement ->
                statement.executeQuery("PRAGMA $pragma").use { result ->
                    assertTrue(result.next())
                    result.getString(1)
                }
            }
        }
        val guardRows = mutableListOf<Pair<Int, String>>()
        createStatement().use { statement ->
            statement.executeQuery("SELECT id, token FROM guard ORDER BY id").use { result ->
                while (result.next()) guardRows += result.getInt(1) to result.getString(2)
            }
        }
        return SecurityState(schema, attachedDatabases, pragmas, guardRows)
    }

    private data class SchemaEntry(
        val location: String,
        val type: String,
        val name: String,
        val tableName: String,
        val sql: String
    )

    private data class SecurityState(
        val schema: List<SchemaEntry>,
        val attachedDatabases: List<Pair<String, String>>,
        val securityPragmas: Map<String, String>,
        val guardRows: List<Pair<Int, String>>
    )

    private data class KnownColumn(val defaultExpression: String?)

    private enum class MetadataOperation {
        TABLES,
        COLUMNS,
        PRIMARY_KEYS,
        INDEXES
    }

    private data class MetadataFuzzInput(
        val operation: MetadataOperation,
        val first: String,
        val second: String
    ) {
        val combined = first + second
        val containsNul = '\u0000' in first || '\u0000' in second

        companion object {
            fun from(input: ByteArray): MetadataFuzzInput {
                val operation = MetadataOperation.entries[
                    (input.firstOrNull()?.toInt() ?: 0).and(0xFF) % MetadataOperation.entries.size
                ]
                val payload = if (input.size > 2) {
                    input.copyOfRange(2, input.size)
                } else {
                    byteArrayOf()
                }
                val split = min((input.getOrNull(1)?.toInt() ?: 0).and(0xFF), payload.size)
                return MetadataFuzzInput(
                    operation,
                    payload.copyOfRange(0, split).toString(Charsets.UTF_8),
                    payload.copyOfRange(split, payload.size).toString(Charsets.UTF_8)
                )
            }
        }
    }

    companion object {
        private const val MAX_INPUT_SIZE = 512
        private const val GUARD_TOKEN = "metadata-fuzz-guard"
        private const val ADVERSARIAL_TABLE = "meta'\"; DROP TABLE guard;--"
        private const val ADVERSARIAL_PRIMARY_KEY = "pk'; PRAGMA writable_schema=ON;--"
        private const val ADVERSARIAL_COLUMN = "column' UNION SELECT 'injected';--"
        private const val ADVERSARIAL_DEFAULT = "value'; DROP TABLE guard;--"
        private const val ADVERSARIAL_INDEX = "index'; ATTACH DATABASE ':memory:' AS evil;--"
        private const val ADVERSARIAL_VIEW = "view' OR 1=1;--"

        private val KNOWN_TABLE_TYPES = mapOf(
            "guard" to "TABLE",
            "ordinary" to "TABLE",
            ADVERSARIAL_TABLE to "TABLE",
            ADVERSARIAL_VIEW to "VIEW"
        )
        private val KNOWN_COLUMNS = mapOf(
            "guard" to mapOf("id" to KnownColumn(null), "token" to KnownColumn(null)),
            "ordinary" to mapOf("id" to KnownColumn(null), "value" to KnownColumn("'ordinary'")),
            ADVERSARIAL_TABLE to mapOf(
                ADVERSARIAL_PRIMARY_KEY to KnownColumn(null),
                ADVERSARIAL_COLUMN to KnownColumn("'${ADVERSARIAL_DEFAULT.replace("'", "''")}'")
            ),
            ADVERSARIAL_VIEW to mapOf("id" to KnownColumn(null), "value" to KnownColumn(null))
        )
        private val KNOWN_PRIMARY_KEYS = mapOf(
            "guard" to setOf("id"),
            "ordinary" to setOf("id"),
            ADVERSARIAL_TABLE to setOf(ADVERSARIAL_PRIMARY_KEY),
            ADVERSARIAL_VIEW to emptySet()
        )
        private val KNOWN_INDEXES = mapOf(
            "guard" to emptySet(),
            "ordinary" to setOf("ordinary_value_idx"),
            ADVERSARIAL_TABLE to setOf(ADVERSARIAL_INDEX),
            ADVERSARIAL_VIEW to emptySet()
        )
        private val SECURITY_PRAGMAS = listOf(
            "foreign_keys",
            "query_only",
            "trusted_schema",
            "user_version",
            "writable_schema"
        )

        private fun quoteIdentifier(value: String) = "\"${value.replace("\"", "\"\"")}\""

        private fun quoteLiteral(value: String) = "'${value.replace("'", "''")}'"

        private fun seed(operation: MetadataOperation, first: String, second: String = ""): ByteArray {
            val firstBytes = first.toByteArray()
            val secondBytes = second.toByteArray()
            require(firstBytes.size <= 255)
            return byteArrayOf(operation.ordinal.toByte(), firstBytes.size.toByte()) + firstBytes + secondBytes
        }

        @JvmStatic
        fun inputs(): Stream<ByteArray> = Stream.of(
            byteArrayOf(),
            seed(MetadataOperation.TABLES, "%"),
            seed(MetadataOperation.TABLES, "'; DROP TABLE guard;--"),
            seed(MetadataOperation.TABLES, "%'; ATTACH DATABASE ':memory:' AS evil;--"),
            seed(MetadataOperation.COLUMNS, "%", "%"),
            seed(MetadataOperation.COLUMNS, "%", "' UNION SELECT 'injected';--"),
            seed(MetadataOperation.PRIMARY_KEYS, ADVERSARIAL_TABLE),
            seed(MetadataOperation.PRIMARY_KEYS, "'; PRAGMA writable_schema=ON;--"),
            seed(MetadataOperation.INDEXES, ADVERSARIAL_TABLE),
            seed(MetadataOperation.INDEXES, "'; ATTACH DATABASE ':memory:' AS evil;--"),
            seed(MetadataOperation.TABLES, "nul\u0000'; DROP TABLE guard;--")
        )
    }
}
