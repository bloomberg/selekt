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
import java.sql.DriverManager
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class JdbcAdaptiveTextBatchTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `adaptive text reads preserve mixed storage classes and UTF-8 fallback`() {
        val url = "jdbc:sqlite:${tempDir.resolve("adaptive-text.db")}?poolSize=1"
        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(QUERY).use { resultSet ->
                    assertTrue(resultSet.next())
                    assertEquals(listOf("a", "b", "c", "d"), (1..4).map(resultSet::getString))

                    assertTrue(resultSet.next())
                    assertNull(resultSet.getObject(1))
                    assertEquals(42L, resultSet.getObject(2))
                    assertContentEquals(byteArrayOf('a'.code.toByte(), 0, 'b'.code.toByte()),
                        assertIs<ByteArray>(resultSet.getObject(3)))
                    assertNull(resultSet.getString(1))
                    assertEquals("42", resultSet.getString(2))
                    assertEquals("a\u0000b", resultSet.getString(3))
                    assertEquals("€", resultSet.getString(4))

                    assertTrue(resultSet.next())
                    assertEquals(listOf("e", "f", "g", "h"), (1..4).map(resultSet::getString))
                    assertFalse(resultSet.next())
                }
            }
        }
    }

    private companion object {
        const val QUERY = """
            SELECT first, second, third, fourth
            FROM (
                SELECT 'a' AS first, 'b' AS second, 'c' AS third, 'd' AS fourth, 1 AS ordinal
                UNION ALL SELECT NULL, 42, x'610062', '€', 2
                UNION ALL SELECT 'e', 'f', 'g', 'h', 3
            )
            ORDER BY ordinal
        """
    }
}
