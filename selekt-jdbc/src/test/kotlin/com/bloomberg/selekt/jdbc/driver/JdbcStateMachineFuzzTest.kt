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

package com.bloomberg.selekt.jdbc.driver

import com.bloomberg.selekt.jdbc.FUZZ_TEST_TIMEOUT_MINUTES
import com.code_intelligence.jazzer.junit.FuzzTest
import java.sql.Connection
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.math.min
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.params.provider.MethodSource

@Suppress("MagicNumber", "TooManyFunctions")
@Timeout(value = FUZZ_TEST_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
internal class JdbcStateMachineFuzzTest {
    @MethodSource("inputs")
    @FuzzTest
    fun fuzzJdbcStateMachine(input: ByteArray) {
        if (input.size > MAX_INPUT_SIZE) {
            return
        }

        val program = Program(input)
        val dataSource = SelektDataSource().apply {
            databasePath = ":memory:"
            journalMode = "MEMORY"
            maxPoolSize = 1
        }
        try {
            dataSource.runProgram(program)
        } finally {
            dataSource.close()
        }
        assertTrue(dataSource.isClosed())
    }

    private fun SelektDataSource.runProgram(program: Program) {
        connection.use { connection ->
            connection.createStatement().use {
                assertFalse(it.execute("CREATE TABLE fuzz_items (id INTEGER PRIMARY KEY, value TEXT, payload BLOB)"))
                assertFalse(it.execute("CREATE TABLE fuzz_guard (id INTEGER PRIMARY KEY, token TEXT NOT NULL)"))
            }
            connection.prepareStatement("INSERT INTO fuzz_guard VALUES (1, ?)").use {
                it.setString(1, GUARD_TOKEN)
                assertEquals(1, it.executeUpdate())
            }
            while (program.hasRemaining) {
                when (program.nextUnsignedByte() % OPERATION_COUNT) {
                    0 -> connection.upsert(program)
                    1 -> connection.query(program)
                    2 -> connection.batch(program)
                    3 -> connection.stream(program)
                    4 -> connection.toggleTransaction(program)
                    5 -> connection.inspectMetadata()
                    6 -> connection.clearAndRebind(program)
                    7 -> connection.closeStatementTwice()
                }
            }
            if (!connection.autoCommit) {
                connection.rollback()
            }
            connection.assertSqlIsolationGuard()
        }
    }

    private fun Connection.assertSqlIsolationGuard() {
        createStatement().use { statement ->
            statement.executeQuery("SELECT token FROM fuzz_guard WHERE id = 1").use { resultSet ->
                assertTrue(resultSet.next())
                assertEquals(GUARD_TOKEN, resultSet.getString(1))
                assertFalse(resultSet.next())
            }
            statement.executeQuery(
                "SELECT name FROM sqlite_schema WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name"
            ).use { resultSet ->
                val tables = buildList {
                    while (resultSet.next()) add(resultSet.getString(1))
                }
                assertEquals(listOf("fuzz_guard", "fuzz_items"), tables)
            }
            statement.executeQuery("PRAGMA database_list").use { resultSet ->
                assertTrue(resultSet.next())
                assertEquals("main", resultSet.getString("name"))
                assertFalse(resultSet.next())
            }
        }
    }

    private fun Connection.upsert(program: Program) {
        prepareStatement(UPSERT_SQL).use { statement ->
            statement.setInt(1, program.nextId())
            statement.setString(2, program.nextText())
            statement.setBytes(3, program.nextBytes())
            assertEquals(1, statement.executeUpdate())
        }
    }

    private fun Connection.query(program: Program) {
        prepareStatement("SELECT id, value, payload FROM fuzz_items WHERE id = ?").use { statement ->
            statement.setInt(1, program.nextId())
            statement.executeQuery().use { resultSet ->
                assertEquals(3, resultSet.metaData.columnCount)
                if (resultSet.next()) {
                    resultSet.getInt(1)
                    resultSet.getString(2)
                    resultSet.getBytes(3)
                }
            }
        }
    }

    private fun Connection.batch(program: Program) {
        prepareStatement(UPSERT_SQL).use { statement ->
            repeat(2) {
                statement.setInt(1, program.nextId())
                statement.setString(2, program.nextText())
                statement.setBytes(3, program.nextBytes())
                statement.addBatch()
            }
            assertEquals(2, statement.executeBatch().size)
        }
    }

    private fun Connection.stream(program: Program) {
        val id = program.nextId()
        val text = program.nextText()
        val bytes = program.nextBytes()
        prepareStatement(UPSERT_SQL).use { statement ->
            statement.setInt(1, id)
            statement.setCharacterStream(2, text.reader(), text.length)
            statement.setBinaryStream(3, bytes.inputStream(), bytes.size)
            assertEquals(1, statement.executeUpdate())
        }
        prepareStatement("SELECT value, payload FROM fuzz_items WHERE id = ?").use { statement ->
            statement.setInt(1, id)
            statement.executeQuery().use { resultSet ->
                assertTrue(resultSet.next())
                assertEquals(text, resultSet.getCharacterStream(1).readText())
                assertContentEquals(bytes, resultSet.getBinaryStream(2).readBytes())
            }
        }
    }

    private fun Connection.toggleTransaction(program: Program) {
        if (autoCommit) {
            autoCommit = false
        } else {
            if (program.nextUnsignedByte() % 2 == 0) {
                commit()
            } else {
                rollback()
            }
            autoCommit = true
        }
    }

    private fun Connection.inspectMetadata() {
        metaData.run {
            databaseProductName
            driverName
            driverVersion
            supportsTransactions()
            getTables(null, null, "fuzz_items", arrayOf("TABLE")).use { tables ->
                while (tables.next()) tables.getString("TABLE_NAME")
            }
        }
    }

    private fun Connection.clearAndRebind(program: Program) {
        prepareStatement(UPSERT_SQL).use { statement ->
            statement.setInt(1, program.nextId())
            statement.setString(2, program.nextText())
            statement.setBytes(3, program.nextBytes())
            statement.clearParameters()
            statement.setInt(1, program.nextId())
            statement.setString(2, program.nextText())
            statement.setBytes(3, program.nextBytes())
            assertEquals(1, statement.executeUpdate())
        }
    }

    private fun Connection.closeStatementTwice() {
        val statement = prepareStatement("SELECT 1")
        statement.close()
        statement.close()
        assertTrue(statement.isClosed)
    }

    private class Program(private val input: ByteArray) {
        private var position = 0

        val hasRemaining: Boolean
            get() = position < input.size

        fun nextUnsignedByte(): Int = if (hasRemaining) {
            input[position++].toInt() and 0xFF
        } else {
            0
        }

        fun nextId(): Int = nextUnsignedByte() % MAX_ROW_ID

        fun nextText(): String = nextBytes().toString(Charsets.UTF_8)

        fun nextBytes(): ByteArray {
            val length = min(nextUnsignedByte() % (MAX_VALUE_SIZE + 1), input.size - position)
            return input.copyOfRange(position, position + length).also { position += length }
        }
    }

    companion object {
        private const val MAX_INPUT_SIZE = 256
        private const val MAX_VALUE_SIZE = 32
        private const val MAX_ROW_ID = 64
        private const val OPERATION_COUNT = 8
        private const val GUARD_TOKEN = "jdbc-state-fuzz-guard"
        private const val UPSERT_SQL = "INSERT OR REPLACE INTO fuzz_items (id, value, payload) VALUES (?, ?, ?)"

        private fun boundTextSeed(payload: String): ByteArray {
            val bytes = payload.toByteArray()
            require(bytes.size <= MAX_VALUE_SIZE)
            return byteArrayOf(3, 1, bytes.size.toByte()) + bytes + byteArrayOf(0)
        }

        @JvmStatic
        fun inputs(): Stream<ByteArray> = Stream.of(
            byteArrayOf(),
            byteArrayOf(0, 1, 'a'.code.toByte(), 1, 1),
            byteArrayOf(3, 1, 0, 0),
            byteArrayOf(3, 2, 4, 't'.code.toByte(), 'e'.code.toByte(), 's'.code.toByte(), 't'.code.toByte(), 3, 1, 2, 3),
            boundTextSeed("' OR 1=1--"),
            boundTextSeed("'; DROP TABLE fuzz_guard;--"),
            boundTextSeed("'; ATTACH ':memory:' AS evil;--"),
            boundTextSeed("nul\u0000'; PRAGMA writable_schema=ON"),
            ByteArray(64) { it.toByte() }
        )
    }
}
