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

import java.io.File
import java.sql.Connection
import java.sql.ResultSet
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class XerialInteropTest {
    private val selekt = SelektDriver()
    private val xerial = org.sqlite.JDBC()
    private val connections = mutableListOf<Connection>()
    private val tempFiles = mutableListOf<File>()

    @AfterEach
    fun tearDown() {
        connections.run {
            forEach {
                if (!it.isClosed) {
                    it.close()
                }
            }
            clear()
        }
        tempFiles.run {
            forEach(File::delete)
            clear()
        }
    }

    private fun tempDatabaseUrl(prefix: String): String {
        val dbFile = File.createTempFile(prefix, ".db").also(tempFiles::add)
        return "jdbc:sqlite:${dbFile.absolutePath}"
    }

    @Test
    fun selektWritesXerialReads() {
        val url = tempDatabaseUrl("xerial_interop_swxr_")
        val blob = byteArrayOf(1, 2, 3, 4, 5)
        selekt.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use {
                it.execute(
                    "CREATE TABLE interop (id INTEGER PRIMARY KEY, name TEXT, score REAL, data BLOB)"
                )
                it.execute("INSERT INTO interop VALUES (1, 'alpha', 1.5, X'0102030405')")
                it.execute("INSERT INTO interop VALUES (2, NULL, NULL, NULL)")
            }
        }
        xerial.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT * FROM interop ORDER BY id").use { rs ->
                    assertTrue(rs.next())
                    assertEquals(1, rs.getInt("id"))
                    assertEquals("alpha", rs.getString("name"))
                    assertEquals(1.5, rs.getDouble("score"))
                    assertTrue(blob.contentEquals(rs.getBytes("data")))

                    assertTrue(rs.next())
                    assertEquals(2, rs.getInt("id"))
                    assertNull(rs.getString("name"))
                    rs.getDouble("score")
                    assertTrue(rs.wasNull())
                    assertNull(rs.getBytes("data"))

                    assertFalse(rs.next())
                }
            }
        }
    }

    @Test
    fun xerialWritesSelektReads() {
        val url = tempDatabaseUrl("xerial_interop_xwsr_")
        val blob = byteArrayOf(9, 8, 7, 6, 5)
        xerial.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use {
                it.execute(
                    "CREATE TABLE interop (id INTEGER PRIMARY KEY, name TEXT, score REAL, data BLOB)"
                )
                it.execute("INSERT INTO interop VALUES (1, 'beta', 2.5, X'0908070605')")
                it.execute("INSERT INTO interop VALUES (2, NULL, NULL, NULL)")
            }
        }
        selekt.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT * FROM interop ORDER BY id").use { rs ->
                    assertTrue(rs.next())
                    assertEquals(1, rs.getInt("id"))
                    assertEquals("beta", rs.getString("name"))
                    assertEquals(2.5, rs.getDouble("score"))
                    assertTrue(blob.contentEquals(rs.getBytes("data")))

                    assertTrue(rs.next())
                    assertEquals(2, rs.getInt("id"))
                    assertNull(rs.getString("name"))
                    assertNull(rs.getBytes("data"))

                    assertFalse(rs.next())
                }
            }
        }
    }

    @Test
    fun roundTripAcrossBothDrivers() {
        val url = tempDatabaseUrl("xerial_interop_rt_")
        selekt.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use {
                it.execute("CREATE TABLE interop (id INTEGER PRIMARY KEY, name TEXT)")
                it.execute("INSERT INTO interop VALUES (1, 'from-selekt')")
            }
        }
        xerial.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT name FROM interop WHERE id = 1").use { rs ->
                    assertTrue(rs.next())
                    assertEquals("from-selekt", rs.getString("name"))
                }
                statement.execute("INSERT INTO interop VALUES (2, 'from-xerial')")
                statement.execute("CREATE INDEX interop_name_idx ON interop (name)")
            }
        }
        selekt.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT id, name FROM interop ORDER BY id").use { rs ->
                    assertTrue(rs.next())
                    assertEquals(1, rs.getInt("id"))
                    assertEquals("from-selekt", rs.getString("name"))

                    assertTrue(rs.next())
                    assertEquals(2, rs.getInt("id"))
                    assertEquals("from-xerial", rs.getString("name"))

                    assertFalse(rs.next())
                }
            }
        }
    }

    @Test
    fun xerialReadsSelektCreatedIndexMetadata() {
        val url = tempDatabaseUrl("xerial_interop_idx_")
        selekt.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use {
                it.execute("CREATE TABLE interop (id INTEGER PRIMARY KEY, name TEXT)")
                it.execute("CREATE INDEX interop_name_idx ON interop (name)")
            }
        }
        xerial.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA index_list('interop')").use { rs ->
                    assertTrue(columnValues(rs, "name").any { it == "interop_name_idx" })
                }
            }
        }
    }

    private fun columnValues(rs: ResultSet, column: String): List<String?> {
        val values = mutableListOf<String?>()
        while (rs.next()) {
            values.add(rs.getString(column))
        }
        return values
    }
}
