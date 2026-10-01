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

package com.bloomberg.selekt.jdbc.result

import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Types
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class JdbcColumnMetadataIntegrationTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun forwardResultSetReportsColumnOrigins() {
        verifyMetadata(ResultSet.TYPE_FORWARD_ONLY)
    }

    @Test
    fun windowedResultSetReportsColumnOrigins() {
        verifyMetadata(ResultSet.TYPE_SCROLL_SENSITIVE)
    }

    private fun verifyMetadata(resultSetType: Int) {
        val url = "jdbc:sqlite:${tempDir.resolve("metadata-$resultSetType.db")}?poolSize=1"
        DriverManager.getConnection(url).use { connection ->
            createSchema(connection)
            verifyMetadata(connection, resultSetType)
        }
    }

    private fun createSchema(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.execute("CREATE TABLE users (id INTEGER, display_name TEXT)")
            statement.execute("INSERT INTO users VALUES (1, 'Ada')")
        }
    }

    private fun verifyMetadata(connection: Connection, resultSetType: Int) {
        connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY).use { statement ->
            statement.executeQuery(
                "SELECT id AS user_id, display_name AS label, id + 1 AS next_id FROM users"
            ).use(::verifyMetadata)
        }
    }

    private fun verifyMetadata(resultSet: ResultSet) {
        resultSet.next()
        resultSet.metaData.run {
            assertColumn(1, ExpectedColumn("user_id", "id", "main", "users", "INTEGER", Types.BIGINT))
            assertColumn(2, ExpectedColumn("label", "display_name", "main", "users", "TEXT", Types.VARCHAR))
            assertColumn(3, ExpectedColumn("next_id", "next_id", "", "", "BIGINT", Types.BIGINT))
        }
    }

    private fun ResultSetMetaData.assertColumn(
        index: Int,
        expected: ExpectedColumn
    ) {
        assertEquals(expected.label, getColumnLabel(index))
        assertEquals(expected.name, getColumnName(index))
        assertEquals(expected.schema, getSchemaName(index))
        assertEquals(expected.table, getTableName(index))
        assertEquals("", getCatalogName(index))
        assertEquals(expected.typeName, getColumnTypeName(index))
        assertEquals(expected.type, getColumnType(index))
    }

    private data class ExpectedColumn(
        val label: String,
        val name: String,
        val schema: String,
        val table: String,
        val typeName: String,
        val type: Int
    )
}
