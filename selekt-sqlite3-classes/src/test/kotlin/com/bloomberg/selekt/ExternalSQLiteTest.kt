/*
 * Copyright 2024 Bloomberg Finance L.P.
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

package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ExternalSQLiteTest {
    private companion object {
        val sqlite = externalSQLiteSingleton()

        const val SQL_OPEN_READONLY = 1
        const val SQL_OPEN_READWRITE = SQL_OPEN_READONLY shl 1
        const val SQL_OPEN_CREATE = SQL_OPEN_READWRITE shl 1
        const val SQL_OPEN_READWRITE_OR_CREATE = SQL_OPEN_READWRITE or SQL_OPEN_CREATE
        const val SQL_CONSTRAINT = 19
        const val SQL_ROW = 100
        const val SQL_BLOB = 4
        const val SQL_NULL = 5
        const val KEY_SIZE = 32
    }

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `externalSQLiteSingleton creates instance`() {
        assertNotNull(sqlite)
    }

    @Test
    fun `externalSQLiteSingleton creates singleton`() {
        assertSame(sqlite, externalSQLiteSingleton())
    }

    @Test
    fun `libVersion returns non-empty string`() {
        val version = sqlite.libVersion()
        assertNotNull(version)
        assertTrue(version.isNotEmpty())
    }

    @Test
    fun `libVersionNumber returns positive integer`() {
        assertTrue(sqlite.libVersionNumber() > 0)
    }

    @Test
    fun `threadsafe returns non-zero value`() {
        assertTrue(sqlite.threadsafe() > 0)
    }

    @Test
    fun `can open and close database`() {
        val dbPath = File(tempDir, "test.db").absolutePath
        val dbHolder = LongArray(1)
        val openResult = sqlite.openV2(dbPath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        assertEquals(SQL_OK, openResult)
        assertTrue(dbHolder[0] > 0L)
        assertEquals(SQL_OK, sqlite.closeV2(dbHolder[0]))
    }

    @Test
    fun `can prepare and finalize statement`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT 1"
            assertEquals(SQL_OK, sqlite.prepareV2(db, sql, sql.length, statementHolder))
            assertTrue(statementHolder[0] > 0L)
            assertEquals(SQL_OK, sqlite.finalize(statementHolder[0]))
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can execute simple query`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT 42"
            sqlite.prepareV2(db, sql, sql.length, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(42, sqlite.columnInt(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `progress handler can be replaced and cleared`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        val firstCalls = AtomicInteger()
        val secondCalls = AtomicInteger()
        val sql = """
            WITH RECURSIVE counter(value) AS (
                VALUES(0)
                UNION ALL
                SELECT value + 1 FROM counter WHERE value < 100
            )
            SELECT sum(value) FROM counter
        """.trimIndent()
        try {
            sqlite.progressHandler(db, 1) {
                firstCalls.incrementAndGet()
                0
            }
            assertEquals(SQL_OK, sqlite.exec(db, sql))
            assertTrue(firstCalls.get() > 0)
            val firstCallsAfterReplacement = firstCalls.get()
            sqlite.progressHandler(db, 1) {
                secondCalls.incrementAndGet()
                0
            }
            assertEquals(SQL_OK, sqlite.exec(db, sql))
            assertEquals(firstCallsAfterReplacement, firstCalls.get())
            assertTrue(secondCalls.get() > 0)
            val secondCallsAfterClearing = secondCalls.get()
            sqlite.progressHandler(db, 0, null)
            assertEquals(SQL_OK, sqlite.exec(db, sql))
            assertEquals(secondCallsAfterClearing, secondCalls.get())
        } finally {
            sqlite.progressHandler(db, 0, null)
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `native cursor window preserves an empty blob on every backend`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT x''"
            assertEquals(SQL_OK, sqlite.prepareV2(db, sql, sql.length, statementHolder))
            val statement = statementHolder[0]
            try {
                val nativeSQLite = assertIs<INativeCursorWindowSQLite>(sqlite)
                val buffer = assertNotNull(nativeSQLite.fillCursorWindow(statement, 0, 1, true))
                    .order(ByteOrder.nativeOrder())
                try {
                    assertEquals(1, buffer.getInt(0))
                    assertEquals(1, buffer.getInt(Int.SIZE_BYTES))
                    val rowOffset = buffer.getInt(buffer.capacity() - Int.SIZE_BYTES)
                    assertEquals(SQL_BLOB, buffer[rowOffset].toInt())
                    assertEquals(0, buffer.getInt(rowOffset + 1))
                    buffer.position(1)
                } finally {
                    nativeSQLite.freeCursorWindow(buffer)
                }
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `native cursor window preserves SQLite step errors on every backend`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            assertEquals(SQL_OK, sqlite.exec(db, "CREATE TABLE Foo (bar INTEGER UNIQUE)"))
            assertEquals(SQL_OK, sqlite.exec(db, "INSERT INTO Foo VALUES (1)"))
            val statementHolder = LongArray(1)
            val sql = "INSERT INTO Foo VALUES (1) RETURNING bar"
            assertEquals(SQL_OK, sqlite.prepareV2(db, sql, sql.length, statementHolder))
            val statement = statementHolder[0]
            try {
                val nativeSQLite = assertIs<INativeCursorWindowSQLite>(sqlite)
                assertNull(nativeSQLite.fillCursorWindow(statement, 0, 1, true))
                assertEquals(SQL_CONSTRAINT, sqlite.errorCode(db))
                assertEquals(SQL_CONSTRAINT, sqlite.extendedErrorCode(db) and 0xFF)
                assertTrue(sqlite.errorMessage(db).contains("UNIQUE constraint failed"))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `vec1 extension is available`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT vec1_info()"
            sqlite.prepareV2(db, sql, sql.length, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertTrue(checkNotNull(sqlite.columnText(statement, 0)).startsWith("version "))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `vec1 computes l2 squared distance on real database`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT vec1_l2_distance(vec1_from_json('[1,2]'), vec1_from_json('[4,6]'))"
            sqlite.prepareV2(db, sql, sql.length, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                val distance = sqlite.columnDouble(statement, 0)
                assertTrue(abs(distance - 25.0) < 1e-9, "Expected squared distance 25.0, got $distance")
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `vec1 vector json round trip works on real database`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = """
                SELECT
                    json_array_length(vec1_to_json(vec1_from_json('[1,2,3]'))),
                    json_extract(vec1_to_json(vec1_from_json('[1,2,3]')), '$[1]')
            """.trimIndent()
            sqlite.prepareV2(db, sql, sql.length, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(3, sqlite.columnInt(statement, 0))
                val element = sqlite.columnDouble(statement, 1)
                assertTrue(abs(element - 2.0) < 1e-9, "Expected element 2.0, got $element")
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `vec1 virtual table returns nearest neighbors in distance order`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.run {
                assertEquals(SQL_OK, exec(db, "CREATE VIRTUAL TABLE vnn USING vec1(embedding)"))
                assertEquals(SQL_OK, exec(db, "INSERT INTO vnn(embedding) VALUES(vec1_from_json('[0,0]'))"))
                assertEquals(SQL_OK, exec(db, "INSERT INTO vnn(embedding) VALUES(vec1_from_json('[1,0]'))"))
                assertEquals(SQL_OK, exec(db, "INSERT INTO vnn(embedding) VALUES(vec1_from_json('[2,0]'))"))
                assertEquals(SQL_OK, exec(db, "INSERT INTO vnn(embedding) VALUES(vec1_from_json('[5,0]'))"))
            }
            val statementHolder = LongArray(1)
            val sql = """
                SELECT rowid, vec1_l2_distance(vec1_from_json('[0,0]'), embedding) AS d
                FROM vnn
                WHERE cmd = vec1_from_json('[0,0]') AND arg = 3
                ORDER BY d
            """.trimIndent()
            assertEquals(SQL_OK, sqlite.prepareV2(db, sql, sql.length, statementHolder))
            val statement = statementHolder[0]
            try {
                sqlite.run {
                    assertEquals(SQL_ROW, step(statement))
                    assertEquals(1L, columnInt64(statement, 0))
                    assertTrue(abs(columnDouble(statement, 1) - 0.0) < 1e-9)
                    assertEquals(SQL_ROW, step(statement))
                    assertEquals(2L, columnInt64(statement, 0))
                    assertTrue(abs(columnDouble(statement, 1) - 1.0) < 1e-9)
                    assertEquals(SQL_ROW, step(statement))
                    assertEquals(3L, columnInt64(statement, 0))
                    assertTrue(abs(columnDouble(statement, 1) - 4.0) < 1e-9)
                }
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can bind and retrieve text`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT ?"
            sqlite.prepareV2(db, sql, sql.length, statementHolder)
            val statement = statementHolder[0]
            try {
                val testText = "Hello, SQLite!"
                val bindResult = sqlite.bindText(statement, 1, testText)
                assertEquals(SQL_OK, bindResult)
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(testText, sqlite.columnText(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can bind and retrieve double`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindDouble(statement, 1, 3.14159))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(3.14159, sqlite.columnDouble(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can bind and retrieve int64`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                val largeValue = Long.MAX_VALUE
                assertEquals(SQL_OK, sqlite.bindInt64(statement, 1, largeValue))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(largeValue, sqlite.columnInt64(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can bind and retrieve null`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindNull(statement, 1))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(SQL_NULL, sqlite.columnType(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can bind and retrieve blob`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                val blob = byteArrayOf(1, 2, 3, 4, 5)
                val original = blob.copyOf()
                assertEquals(SQL_OK, sqlite.bindBlob(statement, 1, blob, blob.size))
                assertContentEquals(original, blob)
                assertEquals(SQL_ROW, sqlite.step(statement))
                val result = sqlite.columnBlob(statement, 0)
                assertNotNull(result)
                assertEquals(blob.toList(), result.toList())
                assertContentEquals(original, blob)
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindBlob rejects invalid declared lengths`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                val blob = byteArrayOf(1, 2, 3)
                assertFailsWith<IndexOutOfBoundsException> {
                    sqlite.bindBlob(statement, 1, blob, -1)
                }
                assertFailsWith<IndexOutOfBoundsException> {
                    sqlite.bindBlob(statement, 1, blob, blob.size + 1)
                }
                assertContentEquals(byteArrayOf(1, 2, 3), blob)
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindBlob accepts zero and partial declared lengths`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?, ?, ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                val blob = byteArrayOf(1, 2, 3)
                assertEquals(SQL_OK, sqlite.bindBlob(statement, 1, blob, 0))
                assertEquals(SQL_OK, sqlite.bindBlob(statement, 2, blob, 2))
                assertEquals(SQL_OK, sqlite.bindBlob(statement, 3, byteArrayOf(), 0))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(SQL_BLOB, sqlite.columnType(statement, 0))
                assertContentEquals(byteArrayOf(), sqlite.columnBlob(statement, 0))
                assertContentEquals(byteArrayOf(1, 2), sqlite.columnBlob(statement, 1))
                assertEquals(SQL_BLOB, sqlite.columnType(statement, 2))
                assertContentEquals(byteArrayOf(), sqlite.columnBlob(statement, 2))
                assertContentEquals(byteArrayOf(1, 2, 3), blob)
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can bind and retrieve text with unicode`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                val text = "Hello\u0000 世界 🌍 Привет"
                assertEquals(SQL_OK, sqlite.bindText(statement, 1, text))
                assertEquals(SQL_ROW, sqlite.step(statement))
                val result = sqlite.columnText(statement, 0)
                assertNotNull(result)
                assertEquals(text, result)
                assertEquals(text.toList(), result.toList())
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `ASCII text binding covers ASCII and Unicode boundaries without changing rejected bindings`() =
        withStatement("SELECT ?") { statement ->
            val handle = sqlite.newStatementHandle(statement)
            listOf("\u0000", "\u007F", "ascii\u0000text").forEach { ascii ->
                assertEquals(SQL_OK, sqlite.bindTextAscii(handle, 1, ascii), ascii)
                assertEquals(SQL_ROW, sqlite.step(statement), ascii)
                assertEquals(ascii, sqlite.columnText(statement, 0), ascii)
                assertEquals(SQL_OK, sqlite.reset(statement), ascii)
            }
            val retained = "retained"
            assertEquals(SQL_OK, sqlite.bindTextAscii(statement, 1, retained))
            listOf(
                "\u0080",
                "\u07FF",
                "\u0800",
                "\uFFFF",
                "\uD800\uDC00",
                "\uD83C\uDF0D",
                "\uDBFF\uDFFF",
                "ascii café suffix"
            ).forEach { unicode ->
                assertEquals(SQL_MISMATCH, sqlite.bindTextAscii(statement, 1, unicode), unicode)
            }
            assertEquals(SQL_ROW, sqlite.step(statement))
            assertEquals(retained, sqlite.columnText(statement, 0))
        }

    @Test
    fun `empty text remains distinct from SQL null`() = withStatement("SELECT ?, NULL") { statement ->
        assertEquals(SQL_OK, sqlite.bindText(statement, 1, ""))
        assertEquals(SQL_ROW, sqlite.step(statement))
        assertEquals("", sqlite.columnText(statement, 0))
        assertNull(sqlite.columnText(statement, 1))
        assertEquals(SQL_OK, sqlite.reset(statement))
        assertEquals(SQL_OK, sqlite.bindTextAscii(statement, 1, ""))
        assertEquals(SQL_ROW, sqlite.step(statement))
        assertEquals("", sqlite.columnText(statement, 0))
    }

    @Test
    fun `malformed surrogates bind identically through ASCII and UTF-8 paths`() =
        withStatement("SELECT ?") { statement ->
            listOf(
                "high-min \uD800 surrogate",
                "high-max \uDBFF surrogate",
                "low-min \uDC00 surrogate",
                "low-max \uDFFF surrogate",
                "reversed \uDC00\uD800 pair"
            ).forEach { malformed ->
                val expected = malformed.toByteArray(Charsets.UTF_8).toString(Charsets.UTF_8)
                assertEquals(SQL_OK, sqlite.bindText(statement, 1, malformed), malformed)
                assertEquals(SQL_ROW, sqlite.step(statement), malformed)
                assertEquals(expected, sqlite.columnText(statement, 0), malformed)
                assertEquals(SQL_OK, sqlite.reset(statement), malformed)
                assertEquals(SQL_OK, sqlite.bindTextAscii(statement, 1, malformed), malformed)
                assertEquals(SQL_ROW, sqlite.step(statement), malformed)
                assertEquals(expected, sqlite.columnText(statement, 0), malformed)
                assertEquals(SQL_OK, sqlite.reset(statement), malformed)
            }
        }

    @Test
    fun `adaptive text binding retains independent modes and error transitions`() =
        withStatement("SELECT ?, ?") { statement ->
            val handle = sqlite.newStatementHandle(statement)
            val utf8TextParameters = BooleanArray(3)
            assertEquals(SQL_OK, sqlite.bindText(handle, 1, "café", utf8TextParameters))
            assertTrue(utf8TextParameters[1])
            assertEquals(SQL_OK, sqlite.bindText(handle, 2, "ascii", utf8TextParameters))
            assertFalse(utf8TextParameters[2])
            assertEquals(SQL_ROW, sqlite.step(statement))
            assertEquals("café", sqlite.columnText(statement, 0))
            assertEquals("ascii", sqlite.columnText(statement, 1))
            assertEquals(SQL_OK, sqlite.reset(statement))
            assertEquals(SQL_OK, sqlite.bindText(handle, 1, "ascii later", utf8TextParameters))
            assertTrue(utf8TextParameters[1])
            assertEquals(SQL_ROW, sqlite.step(statement))
            assertEquals("ascii later", sqlite.columnText(statement, 0))
            assertEquals(SQL_OK, sqlite.reset(statement))
            val asciiErrorModes = BooleanArray(1)
            assertEquals(SQL_RANGE, sqlite.bindText(handle, 0, "ascii", asciiErrorModes))
            assertFalse(asciiErrorModes[0])
            assertEquals(SQL_RANGE, sqlite.bindText(handle, 0, "café", asciiErrorModes))
            assertTrue(asciiErrorModes[0])
        }

    @Test
    fun `adaptive array rows use ASCII and permanent UTF-8 modes on each runtime`() =
        withStatement("SELECT ?, ?") { statement ->
            val handle = sqlite.newStatementHandle(statement)
            val utf8TextParameters = BooleanArray(3)
            assertEquals(
                SQL_OK,
                sqlite.bindRow(handle, arrayOf("café", "ascii"), utf8TextParameters)
            )
            assertTrue(utf8TextParameters[1])
            assertFalse(utf8TextParameters[2])
            assertEquals(SQL_ROW, sqlite.step(statement))
            assertEquals("café", sqlite.columnText(statement, 0))
            assertEquals("ascii", sqlite.columnText(statement, 1))
            assertEquals(SQL_OK, sqlite.reset(statement))
            assertEquals(
                SQL_OK,
                sqlite.bindRow(handle, arrayOf("ascii later", "still ascii"), utf8TextParameters)
            )
            assertTrue(utf8TextParameters[1])
            assertFalse(utf8TextParameters[2])
            assertEquals(SQL_ROW, sqlite.step(statement))
            assertEquals("ascii later", sqlite.columnText(statement, 0))
            assertEquals("still ascii", sqlite.columnText(statement, 1))
        }

    @Test
    fun `typed rows bind every tag and object case through long overload`() =
        withStatement("SELECT ?, ?, ?, ?, ?, ?, ?") { statement ->
            val fixture = TypedRowFixture()
            assertEquals(SQL_OK, bindTyped(statement, fixture))
            assertEquals(SQL_ROW, sqlite.step(statement))
            assertEquals(7, sqlite.columnInt(statement, 0))
            assertEquals(8L, sqlite.columnInt64(statement, 1))
            assertEquals(2.5, sqlite.columnDouble(statement, 2))
            assertEquals("café", sqlite.columnText(statement, 3))
            assertContentEquals(fixture.blob, sqlite.columnBlob(statement, 4))
            assertEquals(SQL_NULL, sqlite.columnType(statement, 5))
            assertEquals(SQL_NULL, sqlite.columnType(statement, 6))
        }

    @Test
    fun `adaptive typed rows cover ASCII mismatch and retained UTF-8 modes`() =
        withStatement("SELECT ?, ?, ?, ?, ?, ?, ?") { statement ->
            val fixture = TypedRowFixture()
            val handle = sqlite.newStatementHandle(statement)
            val utf8TextParameters = BooleanArray(fixture.tags.size + 1)

            assertEquals(SQL_OK, bindTyped(handle, fixture, utf8TextParameters))
            assertTrue(utf8TextParameters[4])
            assertEquals(SQL_ROW, sqlite.step(statement))
            assertEquals("café", sqlite.columnText(statement, 3))
            assertContentEquals(fixture.blob, sqlite.columnBlob(statement, 4))

            assertEquals(SQL_OK, sqlite.reset(statement))
            fixture.objects[3] = "ascii after UTF-8"
            assertEquals(SQL_OK, bindTyped(handle, fixture, utf8TextParameters))
            assertTrue(utf8TextParameters[4])
            assertEquals(SQL_ROW, sqlite.step(statement))
            assertEquals("ascii after UTF-8", sqlite.columnText(statement, 3))

            assertEquals(SQL_OK, sqlite.reset(statement))
            val asciiModes = BooleanArray(fixture.tags.size + 1)
            fixture.objects[3] = "ascii"
            assertEquals(SQL_OK, bindTyped(handle, fixture, asciiModes))
            assertFalse(asciiModes[4])
        }

    @Test
    fun `typed row binding stops on native error`() =
        withStatement("SELECT ?, ?, ?, ?, ?, ?, ?") { statement ->
            assertEquals(
                SQL_RANGE,
                sqlite.bindRowTyped(
                    statement,
                    ByteArray(8) { 1 },
                    IntArray(8),
                    LongArray(8),
                    DoubleArray(8),
                    arrayOfNulls<Any>(8),
                    8
                )
            )
        }

    private class TypedRowFixture {
        val tags = byteArrayOf(1, 2, 3, 4, 4, 4, 0)
        val ints = intArrayOf(7, 0, 0, 0, 0, 0, 0)
        val longs = longArrayOf(0, 8, 0, 0, 0, 0, 0)
        val doubles = doubleArrayOf(0.0, 0.0, 2.5, 0.0, 0.0, 0.0, 0.0)
        val blob = byteArrayOf(9, 10)
        val objects = arrayOfNulls<Any>(tags.size).also {
            it[3] = "café"
            it[4] = blob
            it[5] = Any()
        }
    }

    private fun bindTyped(statement: Long, fixture: TypedRowFixture) = sqlite.bindRowTyped(
        statement,
        fixture.tags,
        fixture.ints,
        fixture.longs,
        fixture.doubles,
        fixture.objects,
        fixture.tags.size
    )

    private fun bindTyped(
        statement: StatementHandle,
        fixture: TypedRowFixture,
        utf8TextParameters: BooleanArray
    ) = sqlite.bindRowTyped(
        statement,
        fixture.tags,
        fixture.ints,
        fixture.longs,
        fixture.doubles,
        fixture.objects,
        fixture.tags.size,
        utf8TextParameters
    )

    @Test
    fun `columnText preserves SQL nulls and embedded nulls and decodes standard UTF-8`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT CAST(X'610062F09F8C8D63' AS TEXT), CAST(X'61FF62' AS TEXT), NULL"
            assertEquals(SQL_OK, sqlite.prepareV2(db, sql, sql.length, statementHolder))
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals("a\u0000b🌍c", sqlite.columnText(statement, 0))
                assertEquals("a\uFFFDb", sqlite.columnText(statement, 1))
                assertNull(sqlite.columnText(statement, 2))
                val handle = sqlite.newStatementHandle(statement)
                assertEquals("a\u0000b🌍c", sqlite.columnText(handle, 0))
                assertEquals("a\uFFFDb", sqlite.columnText(handle, 1))
                assertNull(sqlite.columnText(handle, 2))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `databaseConfig supports every boolean operation accepted by the native bridge`() =
        withDatabase { db ->
            (1002..1019).forEach { operation ->
                assertEquals(
                    SQL_OK,
                    sqlite.databaseConfig(db, operation, -1),
                    operation.toString()
                )
            }
        }

    @Test
    fun `databaseConfig rejects unsupported operations`() = withDatabase { db ->
        val result = runCatching { sqlite.databaseConfig(db, Int.MAX_VALUE, -1) }
        result.onSuccess { assertEquals(SQL_ERROR, it) }
        result.onFailure { assertIs<IllegalArgumentException>(it) }
    }

    private inline fun withDatabase(block: (Long) -> Unit) {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            block(db)
        } finally {
            sqlite.closeV2(db)
        }
    }

    private inline fun withStatement(sql: String, block: (Long) -> Unit) = withDatabase { db ->
        val statementHolder = LongArray(1)
        assertEquals(SQL_OK, sqlite.prepareV2(db, sql, sql.toByteArray(Charsets.UTF_8).size, statementHolder))
        val statement = statementHolder[0]
        try {
            block(statement)
        } finally {
            sqlite.finalize(statement)
        }
    }

    @Test
    fun `can prepare with non-ascii sql`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT 1 -- Comment: 世界"
            assertEquals(SQL_OK, sqlite.prepareV2(db, sql, sql.toByteArray(Charsets.UTF_8).size, statementHolder))
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can prepare with non-ascii sql with clause`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE t(x INTEGER)")
            val insertStatement = LongArray(1)
            sqlite.prepareV2(db, "INSERT INTO t VALUES(1)", 24, insertStatement)
            sqlite.step(insertStatement[0])
            sqlite.finalize(insertStatement[0])
            val statementHolder = LongArray(1)
            val sql = "SELECT * FROM t WHERE x = 1 -- 世界 comment"
            assertEquals(SQL_OK, sqlite.prepareV2(db, sql, sql.toByteArray(Charsets.UTF_8).size, statementHolder))
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(1, sqlite.columnInt(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can prepare with unpaired surrogate in sql`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT 1 -- unpaired surrogate: \uD800 end"
            assertEquals(SQL_OK, sqlite.prepareV2(db, sql, sql.length, statementHolder))
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(1, sqlite.columnInt(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can bind zero blob`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindZeroBlob(statement, 1, 10))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(SQL_BLOB, sqlite.columnType(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get column count and name`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER, name TEXT)")
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, "SELECT id, name FROM test", 26, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(2, sqlite.columnCount(statement))
                assertEquals("id", sqlite.columnName(statement, 0))
                assertEquals("name", sqlite.columnName(statement, 1))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can clear bindings`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                sqlite.bindInt(statement, 1, 42)
                assertEquals(SQL_OK, sqlite.clearBindings(statement))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(SQL_NULL, sqlite.columnType(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can reset statement`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, "SELECT 1", 9, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(SQL_OK, sqlite.reset(statement))
                assertEquals(SQL_ROW, sqlite.step(statement))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can reset and clear bindings`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                sqlite.bindInt(statement, 1, 42)
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(SQL_OK, sqlite.resetAndClearBindings(statement))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(SQL_NULL, sqlite.columnType(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get sql from statement`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT 1"
            sqlite.prepareV2(db, sql, sql.length, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(sql, sqlite.sql(statement))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get expanded sql from statement`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                sqlite.bindInt(statement, 1, 42)
                val expanded = sqlite.expandedSql(statement)
                assertNotNull(expanded)
                assertTrue(expanded.contains("42"))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `expandedSql returns full text when parameter exceeds 64 KiB`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                val payloadSize = 128 * 1024
                val payload = "A".repeat(payloadSize)
                assertEquals(SQL_OK, sqlite.bindText(statement, 1, payload))
                val expanded = sqlite.expandedSql(statement)
                assertNotNull(expanded)
                assertTrue(
                    expanded.length >= payloadSize,
                    "expanded_sql was truncated: got ${expanded.length} chars, expected >= $payloadSize"
                )
                assertTrue(expanded.startsWith("SELECT '"))
                assertTrue(expanded.endsWith("'"))
                assertTrue(expanded.contains("A".repeat(1024)))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get bind parameter count`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, "SELECT ?, ?, ?", 15, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(3, sqlite.bindParameterCount(statement))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can check statement read only`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, "SELECT 1", 9, statementHolder)
            val statement = statementHolder[0]
            try {
                assertTrue(sqlite.statementReadOnly(statement) != 0)
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can check statement busy`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, "SELECT 1", 9, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(0, sqlite.statementBusy(statement))
                sqlite.step(statement)
                assertTrue(sqlite.statementBusy(statement) != 0)
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can execute statement and track changes`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER)")
            sqlite.exec(db, "INSERT INTO test VALUES (1)")
            assertEquals(1, sqlite.changes(db))
            sqlite.exec(db, "INSERT INTO test VALUES (2)")
            assertEquals(2, sqlite.totalChanges(db))
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get last insert row id`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER PRIMARY KEY)")
            sqlite.exec(db, "INSERT INTO test VALUES (NULL)")
            assertTrue(sqlite.lastInsertRowId(db) > 0)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can check autocommit status`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            assertTrue(sqlite.getAutocommit(db) != 0)
            sqlite.exec(db, "BEGIN")
            assertEquals(0, sqlite.getAutocommit(db))
            sqlite.exec(db, "COMMIT")
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get database handle from statement`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, "SELECT 1", 9, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(db, sqlite.databaseHandle(statement))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can check database read only`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            assertTrue(sqlite.databaseReadOnly(db, "main") >= 0)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get error code and message`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "INVALID SQL")
            assertTrue(sqlite.errorCode(db) != SQL_OK)
            val message = sqlite.errorMessage(db)
            assertNotNull(message)
            assertTrue(message.isNotEmpty())
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get extended error code`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.extendedResultCodes(db, 1)
            sqlite.exec(db, "INVALID SQL")
            assertTrue(sqlite.extendedErrorCode(db) != SQL_OK)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can set busy timeout`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            assertEquals(SQL_OK, sqlite.busyTimeout(db, 1000))
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get transaction state`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val initialState = sqlite.transactionState(db)
            assertTrue(initialState >= 0)
            sqlite.exec(db, "CREATE TABLE IF NOT EXISTS test (id INTEGER)")
            sqlite.exec(db, "BEGIN")
            sqlite.exec(db, "INSERT INTO test VALUES (1)")
            val txState = sqlite.transactionState(db)
            assertTrue(txState > 0, "Expected transaction state > 0, got $txState")
            sqlite.exec(db, "COMMIT")
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can work with blobs`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO test VALUES (1, zeroblob(10))")
            val blobHolder = LongArray(1)
            assertEquals(SQL_OK, sqlite.blobOpen(db, "main", "test", "data", 1, 1, blobHolder))
            val blob = blobHolder[0]
            try {
                assertEquals(10, sqlite.blobBytes(blob))
                val writeData = byteArrayOf(1, 2, 3, 4, 5)
                assertEquals(SQL_OK, sqlite.blobWrite(blob, 0, writeData, 0, writeData.size))
                val readData = ByteArray(5)
                assertEquals(SQL_OK, sqlite.blobRead(blob, 0, readData, 0, readData.size))
                assertEquals(writeData.toList(), readData.toList())
            } finally {
                sqlite.blobClose(blob)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can reopen blob`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO test VALUES (1, zeroblob(10))")
            sqlite.exec(db, "INSERT INTO test VALUES (2, zeroblob(10))")
            val blobHolder = LongArray(1)
            sqlite.blobOpen(db, "main", "test", "data", 1, 1, blobHolder)
            val blob = blobHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.blobReopen(blob, 2))
            } finally {
                sqlite.blobClose(blob)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can set wal auto checkpoint`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "PRAGMA journal_mode=WAL")
            assertEquals(SQL_OK, sqlite.walAutoCheckpoint(db, 1000))
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can perform wal checkpoint`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "PRAGMA journal_mode=WAL")
            assertEquals(SQL_OK, sqlite.walCheckpointV2(db, null, 0))
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get memory used`() {
        assertTrue(sqlite.memoryUsed() >= 0)
    }

    @Test
    fun `can get soft heap limit`() {
        assertTrue(sqlite.softHeapLimit64() > 0)
    }

    @Test
    fun `can get hard heap limit`() {
        assertTrue(sqlite.hardHeapLimit64() >= 0)
    }

    @Test
    fun `can get keyword count`() {
        assertTrue(sqlite.keywordCount() > 0)
    }

    @Test
    fun `gitCommit returns non-empty string`() {
        val commit = sqlite.gitCommit()
        assertNotNull(commit)
        assertTrue(commit.isNotEmpty())
    }

    @Test
    fun `can get column value`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, "SELECT 42", 10, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertTrue(sqlite.columnValue(statement, 0) != 0L)
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can duplicate and free value`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, "SELECT 42", 10, statementHolder)
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(statement))
                val value = sqlite.columnValue(statement, 0)
                val dup = sqlite.valueDup(value)
                assertTrue(dup != 0L)
                sqlite.valueFree(dup)
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can check value from bind`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                sqlite.bindInt(statement, 1, 42)
                assertEquals(SQL_ROW, sqlite.step(statement))
                val value = sqlite.columnValue(statement, 0)
                assertTrue(sqlite.valueFromBind(value) != 0)
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get database status`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val holder = IntArray(2)
            assertEquals(SQL_OK, sqlite.databaseStatus(db, 0, false, holder))
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can get statement status`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, "SELECT 1", 9, statementHolder)
            val statement = statementHolder[0]
            try {
                sqlite.statementStatus(statement, 1, false)
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can release memory`() {
        sqlite.releaseMemory(1024)
    }

    @Test
    fun `can release database memory`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.databaseReleaseMemory(db)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `can enable trace`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.traceV2(db, 0)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `isInterrupted returns zero for fresh database`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            assertEquals(0, sqlite.isInterrupted(db))
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `interrupt sets interrupted flag`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.interrupt(db)
            assertTrue(sqlite.isInterrupted(db) != 0)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `interrupt flag is cleared after completion`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.interrupt(db)
            assertTrue(sqlite.isInterrupted(db) != 0)
            sqlite.exec(db, "SELECT 1")
            assertEquals(0, sqlite.isInterrupted(db))
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `interrupt does not affect other database connections`() {
        val dbHolder1 = LongArray(1)
        val dbHolder2 = LongArray(1)
        sqlite.openV2(File(tempDir, "test1.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder1)
        sqlite.openV2(File(tempDir, "test2.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder2)
        val db1 = dbHolder1[0]
        val db2 = dbHolder2[0]
        try {
            sqlite.interrupt(db1)
            assertTrue(sqlite.isInterrupted(db1) != 0)
            assertEquals(0, sqlite.isInterrupted(db2))
        } finally {
            sqlite.closeV2(db1)
            sqlite.closeV2(db2)
        }
    }

    @Test
    fun `interrupt can be called multiple times`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.interrupt(db)
            sqlite.interrupt(db)
            assertTrue(sqlite.isInterrupted(db) != 0)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `interrupt from another thread sets flag`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            assertEquals(0, sqlite.isInterrupted(db))
            val latch = CountDownLatch(1)
            val thread = Thread {
                sqlite.interrupt(db)
                latch.countDown()
            }
            thread.start()
            latch.await()
            assertTrue(sqlite.isInterrupted(db) != 0)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `interrupt clears after step completes`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.interrupt(db)
            assertTrue(sqlite.isInterrupted(db) != 0)
            val statementHolder = LongArray(1)
            val sql = "SELECT 1"
            sqlite.prepareV2(db, sql, sql.length, statementHolder)
            val statement = statementHolder[0]
            try {
                sqlite.step(statement)
            } finally {
                sqlite.finalize(statement)
            }
            assertEquals(0, sqlite.isInterrupted(db), "Interrupt flag should be cleared after step")
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobRead rejects out of bounds offset`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO test VALUES (1, zeroblob(10))")
            val blobHolder = LongArray(1)
            assertEquals(SQL_OK, sqlite.blobOpen(db, "main", "test", "data", 1, 0, blobHolder))
            val blob = blobHolder[0]
            try {
                val buffer = ByteArray(5)
                assertFailsWith<IndexOutOfBoundsException> {
                    sqlite.blobRead(blob, 0, buffer, 3, 5)
                }
            } finally {
                sqlite.blobClose(blob)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobRead rejects negative offset`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO test VALUES (1, zeroblob(10))")
            val blobHolder = LongArray(1)
            assertEquals(SQL_OK, sqlite.blobOpen(db, "main", "test", "data", 1, 0, blobHolder))
            val blob = blobHolder[0]
            try {
                val buffer = ByteArray(5)
                assertFailsWith<IndexOutOfBoundsException> {
                    sqlite.blobRead(blob, 0, buffer, -1, 5)
                }
            } finally {
                sqlite.blobClose(blob)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobWrite rejects out of bounds offset`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO test VALUES (1, zeroblob(10))")
            val blobHolder = LongArray(1)
            assertEquals(SQL_OK, sqlite.blobOpen(db, "main", "test", "data", 1, 1, blobHolder))
            val blob = blobHolder[0]
            try {
                val buffer = byteArrayOf(1, 2, 3)
                assertFailsWith<IndexOutOfBoundsException> {
                    sqlite.blobWrite(blob, 0, buffer, 2, 3)
                }
            } finally {
                sqlite.blobClose(blob)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobWrite rejects negative length`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO test VALUES (1, zeroblob(10))")
            val blobHolder = LongArray(1)
            assertEquals(SQL_OK, sqlite.blobOpen(db, "main", "test", "data", 1, 1, blobHolder))
            val blob = blobHolder[0]
            try {
                val buffer = byteArrayOf(1, 2, 3)
                assertFailsWith<IllegalArgumentException> {
                    sqlite.blobWrite(blob, 0, buffer, 0, -1)
                }
            } finally {
                sqlite.blobClose(blob)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobRead rejects length exceeding array size`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO test VALUES (1, zeroblob(10))")
            val blobHolder = LongArray(1)
            assertEquals(SQL_OK, sqlite.blobOpen(db, "main", "test", "data", 1, 0, blobHolder))
            val blob = blobHolder[0]
            try {
                val buffer = ByteArray(0)
                assertFailsWith<IndexOutOfBoundsException> {
                    sqlite.blobRead(blob, 0, buffer, 0, 1)
                }
            } finally {
                sqlite.blobClose(blob)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobWrite rejects length exceeding array size`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE test (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO test VALUES (1, zeroblob(10))")
            val blobHolder = LongArray(1)
            assertEquals(SQL_OK, sqlite.blobOpen(db, "main", "test", "data", 1, 1, blobHolder))
            val blob = blobHolder[0]
            try {
                val buffer = ByteArray(2)
                assertFailsWith<IndexOutOfBoundsException> {
                    sqlite.blobWrite(blob, 0, buffer, 0, 5)
                }
            } finally {
                sqlite.blobClose(blob)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `closeV2 cleans up commit hook context`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        val listener = object : SQLCommitListener {
            override fun onCommit(): Int = 0

            override fun onRollback() = Unit
        }
        assertEquals(SQL_OK, sqlite.commitHook(db, true, listener))
        assertEquals(SQL_OK, sqlite.closeV2(db))
    }

    @Test
    fun `closeV2 succeeds without commit hook`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        assertEquals(SQL_OK, sqlite.closeV2(db))
    }

    @Test
    fun `bindRow with string`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf("hello")))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals("hello", sqlite.columnText(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindRow with int`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(42)))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(42, sqlite.columnInt(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindRow with null`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(null)))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(SQL_NULL, sqlite.columnType(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindRow with long`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(Long.MAX_VALUE)))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(Long.MAX_VALUE, sqlite.columnInt64(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindRow with double`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(3.14)))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(3.14, sqlite.columnDouble(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindRow with blob`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                val blob = byteArrayOf(1, 2, 3)
                assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf(blob)))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(blob.toList(), sqlite.columnBlob(statement, 0)!!.toList())
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindRow with empty args`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT 1".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindRow(statement, emptyArray()))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals(1, sqlite.columnInt(statement, 0))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindRow with mixed types`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?, ?, ?, ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf<Any?>("text", 42, 3.14, null)))
                assertEquals(SQL_ROW, sqlite.step(statement))
                assertEquals("text", sqlite.columnText(statement, 0))
                assertEquals(42, sqlite.columnInt(statement, 1))
                assertEquals(3.14, sqlite.columnDouble(statement, 2))
                assertEquals(SQL_NULL, sqlite.columnType(statement, 3))
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindRow throws on unsupported type`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length + 1, statementHolder) }
            val statement = statementHolder[0]
            try {
                assertFailsWith<IllegalArgumentException> {
                    sqlite.bindRow(statement, arrayOf<Any>(Any()))
                }
            } finally {
                sqlite.finalize(statement)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindRow in batch insert`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE t (id INTEGER, name TEXT)")
            val sql = "INSERT INTO t VALUES (?, ?)"
            val statementHolder = LongArray(1)
            sqlite.prepareV2(db, sql, sql.length + 1, statementHolder)
            val statement = statementHolder[0]
            try {
                for (i in 1..3) {
                    sqlite.reset(statement)
                    sqlite.clearBindings(statement)
                    assertEquals(SQL_OK, sqlite.bindRow(statement, arrayOf<Any?>(i, "name$i")))
                    sqlite.step(statement)
                }
            } finally {
                sqlite.finalize(statement)
            }
            val selectSql = "SELECT COUNT(*) FROM t"
            sqlite.prepareV2(db, selectSql, selectSql.length + 1, statementHolder)
            val selectStmt = statementHolder[0]
            try {
                assertEquals(SQL_ROW, sqlite.step(selectStmt))
                assertEquals(3, sqlite.columnInt(selectStmt, 0))
            } finally {
                sqlite.finalize(selectStmt)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `newDatabaseHandle returns handle with correct pointer`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val handle = sqlite.newDatabaseHandle(db)
            assertEquals(db, handle.pointer)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `newStatementHandle returns handle with correct pointer`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT 1".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val ptr = statementHolder[0]
            try {
                val handle = sqlite.newStatementHandle(ptr)
                assertEquals(ptr, handle.pointer)
            } finally {
                sqlite.finalize(ptr)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `newBlobHandle returns handle with correct pointer`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO t VALUES (1, zeroblob(4))")
            val blobHolder = LongArray(1)
            sqlite.blobOpen(db, "main", "t", "data", 1, 1, blobHolder)
            val ptr = blobHolder[0]
            try {
                val handle = sqlite.newBlobHandle(ptr)
                assertEquals(ptr, handle.pointer)
            } finally {
                sqlite.blobClose(ptr)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `prepareV2 with DatabaseHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        val dbHandle = sqlite.newDatabaseHandle(db)
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT 1"
            assertEquals(SQL_OK, sqlite.prepareV2(dbHandle, sql, sql.length, statementHolder))
            assertTrue(statementHolder[0] > 0L)
            sqlite.finalize(statementHolder[0])
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `step and finalize with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        val dbHandle = sqlite.newDatabaseHandle(db)
        try {
            val statementHolder = LongArray(1)
            val sql = "SELECT 42"
            sqlite.prepareV2(dbHandle, sql, sql.length, statementHolder)
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(42, sqlite.columnInt(stmtHandle, 0))
            } finally {
                assertEquals(SQL_OK, sqlite.finalize(stmtHandle))
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindText with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(SQL_OK, sqlite.bindText(stmtHandle, 1, "hello"))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals("hello", sqlite.columnText(stmtHandle, 0))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindInt with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(SQL_OK, sqlite.bindInt(stmtHandle, 1, 99))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(99, sqlite.columnInt(stmtHandle, 0))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindInt64 with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(SQL_OK, sqlite.bindInt64(stmtHandle, 1, Long.MAX_VALUE))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(Long.MAX_VALUE, sqlite.columnInt64(stmtHandle, 0))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindDouble with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(SQL_OK, sqlite.bindDouble(stmtHandle, 1, 2.718))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(2.718, sqlite.columnDouble(stmtHandle, 0))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindNull with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(SQL_OK, sqlite.bindNull(stmtHandle, 1))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(SQL_NULL, sqlite.columnType(stmtHandle, 0))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindBlob with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?, ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                val data = byteArrayOf(1, 2, 3)
                assertFailsWith<IndexOutOfBoundsException> {
                    sqlite.bindBlob(stmtHandle, 1, data, -1)
                }
                assertFailsWith<IndexOutOfBoundsException> {
                    sqlite.bindBlob(stmtHandle, 1, data, data.size + 1)
                }
                assertEquals(SQL_OK, sqlite.bindBlob(stmtHandle, 1, data, data.size))
                assertEquals(SQL_OK, sqlite.bindBlob(stmtHandle, 2, byteArrayOf(), 0))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(SQL_BLOB, sqlite.columnType(stmtHandle, 0))
                assertEquals(SQL_BLOB, sqlite.columnType(stmtHandle, 1))
                assertContentEquals(byteArrayOf(), sqlite.columnBlob(stmtHandle, 1))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindZeroBlob with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(SQL_OK, sqlite.bindZeroBlob(stmtHandle, 1, 8))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(SQL_BLOB, sqlite.columnType(stmtHandle, 0))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindParameterCount with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?, ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(2, sqlite.bindParameterCount(stmtHandle))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `bindParameterIndex with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT :val".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(1, sqlite.bindParameterIndex(stmtHandle, ":val"))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `clearBindings with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                sqlite.bindInt(stmtHandle, 1, 7)
                assertEquals(SQL_OK, sqlite.clearBindings(stmtHandle))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(SQL_NULL, sqlite.columnType(stmtHandle, 0))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `reset with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT 1".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                sqlite.step(stmtHandle)
                assertEquals(SQL_OK, sqlite.reset(stmtHandle))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `resetAndClearBindings with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                sqlite.bindInt(stmtHandle, 1, 5)
                sqlite.step(stmtHandle)
                assertEquals(SQL_OK, sqlite.resetAndClearBindings(stmtHandle))
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(SQL_NULL, sqlite.columnType(stmtHandle, 0))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `columnCount with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT 1 AS a, 2 AS b, 3 AS c".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(3, sqlite.columnCount(stmtHandle))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `columnName with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT 1 AS myCol".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals("myCol", sqlite.columnName(stmtHandle, 0))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `columnBlob with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT ?".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                val data = byteArrayOf(7, 8, 9)
                sqlite.bindBlob(stmtHandle, 1, data, data.size)
                assertEquals(SQL_ROW, sqlite.step(stmtHandle))
                assertEquals(data.toList(), sqlite.columnBlob(stmtHandle, 0)!!.toList())
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `statementBusy and statementReadOnly with StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT 1".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(0, sqlite.statementBusy(stmtHandle))
                assertTrue(sqlite.statementReadOnly(stmtHandle) != 0)
                sqlite.step(stmtHandle)
                assertTrue(sqlite.statementBusy(stmtHandle) != 0)
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `databaseHandle from StatementHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            val statementHolder = LongArray(1)
            "SELECT 1".let { sqlite.prepareV2(db, it, it.length, statementHolder) }
            val stmtHandle = sqlite.newStatementHandle(statementHolder[0])
            try {
                assertEquals(db, sqlite.databaseHandle(stmtHandle))
            } finally {
                sqlite.finalize(stmtHandle)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobOpen with DatabaseHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        val dbHandle = sqlite.newDatabaseHandle(db)
        try {
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO t VALUES (1, zeroblob(8))")
            val blobHolder = LongArray(1)
            assertEquals(SQL_OK, sqlite.blobOpen(dbHandle, "main", "t", "data", 1, 1, blobHolder))
            assertTrue(blobHolder[0] > 0L)
            sqlite.blobClose(blobHolder[0])
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobBytes with BlobHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO t VALUES (1, zeroblob(12))")
            val blobHolder = LongArray(1)
            sqlite.blobOpen(db, "main", "t", "data", 1, 0, blobHolder)
            val blobHandle = sqlite.newBlobHandle(blobHolder[0])
            try {
                assertEquals(12, sqlite.blobBytes(blobHandle))
            } finally {
                sqlite.blobClose(blobHandle.pointer)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobWrite and blobRead with BlobHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO t VALUES (1, zeroblob(5))")
            val blobHolder = LongArray(1)
            sqlite.blobOpen(db, "main", "t", "data", 1, 1, blobHolder)
            val blobHandle = sqlite.newBlobHandle(blobHolder[0])
            try {
                val src = byteArrayOf(10, 20, 30, 40, 50)
                assertEquals(SQL_OK, sqlite.blobWrite(blobHandle, 0, src, 0, src.size))
                val dst = ByteArray(5)
                assertEquals(SQL_OK, sqlite.blobRead(blobHandle, 0, dst, 0, dst.size))
                assertEquals(src.toList(), dst.toList())
            } finally {
                sqlite.blobClose(blobHandle.pointer)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobReopen with BlobHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO t VALUES (1, zeroblob(4))")
            sqlite.exec(db, "INSERT INTO t VALUES (2, zeroblob(4))")
            val blobHolder = LongArray(1)
            sqlite.blobOpen(db, "main", "t", "data", 1, 1, blobHolder)
            val blobHandle = sqlite.newBlobHandle(blobHolder[0])
            try {
                assertEquals(SQL_OK, sqlite.blobReopen(blobHandle, 2))
            } finally {
                sqlite.blobClose(blobHandle.pointer)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `blobClose with BlobHandle`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY, data BLOB)")
            sqlite.exec(db, "INSERT INTO t VALUES (1, zeroblob(4))")
            val blobHolder = LongArray(1)
            sqlite.blobOpen(db, "main", "t", "data", 1, 0, blobHolder)
            val blobHandle = sqlite.newBlobHandle(blobHolder[0])
            assertEquals(SQL_OK, sqlite.blobClose(blobHandle))
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `allocateSecret then freeSecret does not throw`() {
        val pointer = sqlite.allocateSecret(KEY_SIZE)
        assertTrue(pointer != 0L)
        sqlite.freeSecret(pointer, KEY_SIZE)
    }

    @Test
    fun `allocateSecret rejects non-positive size`() {
        assertFailsWith<IllegalArgumentException> { sqlite.allocateSecret(0) }
    }

    @Test
    fun `freeSecret tolerates a zero pointer`() {
        sqlite.freeSecret(0L, KEY_SIZE)
    }

    @Test
    fun `storeSecret then keyConventionallyAt opens a database created with keyConventionally`() {
        val key = ByteArray(KEY_SIZE) { 0x11 }
        val dbPath = File(tempDir, "keyed.db").absolutePath
        val dbHolder = LongArray(1)
        sqlite.openV2(dbPath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            assertEquals(SQL_OK, sqlite.keyConventionally(db, key, key.size))
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY)")
            sqlite.exec(db, "INSERT INTO t VALUES (1)")
        } finally {
            sqlite.closeV2(db)
        }

        val pointer = sqlite.allocateSecret(KEY_SIZE)
        try {
            sqlite.storeSecret(pointer, KEY_SIZE, key, key.size)
            val reopenHolder = LongArray(1)
            sqlite.openV2(dbPath, SQL_OPEN_READWRITE_OR_CREATE, reopenHolder)
            val reopened = reopenHolder[0]
            try {
                assertEquals(SQL_OK, sqlite.keyConventionallyAt(reopened, pointer, KEY_SIZE))
                assertEquals(SQL_OK, sqlite.exec(reopened, "SELECT * FROM t"))
            } finally {
                sqlite.closeV2(reopened)
            }
        } finally {
            sqlite.freeSecret(pointer, KEY_SIZE)
        }
        assertTrue(key.all { it == 0x11.toByte() })
    }

    @Test
    fun `storeSecret rejects a length greater than capacity`() {
        val pointer = sqlite.allocateSecret(KEY_SIZE)
        try {
            assertFailsWith<IndexOutOfBoundsException> {
                sqlite.storeSecret(pointer, KEY_SIZE, ByteArray(KEY_SIZE + 1), KEY_SIZE + 1)
            }
        } finally {
            sqlite.freeSecret(pointer, KEY_SIZE)
        }
    }

    @Test
    fun `storeSecret rejects a length greater than its source`() {
        val pointer = sqlite.allocateSecret(KEY_SIZE)
        try {
            assertFailsWith<IndexOutOfBoundsException> {
                sqlite.storeSecret(pointer, KEY_SIZE, ByteArray(1), 2)
            }
        } finally {
            sqlite.freeSecret(pointer, KEY_SIZE)
        }
    }

    @Test
    fun `rawKeyAt behaves identically to keyConventionallyAt`() {
        val key = ByteArray(KEY_SIZE) { 0x22 }
        val dbPath = File(tempDir, "rawkeyed.db").absolutePath
        val dbHolder = LongArray(1)
        sqlite.openV2(dbPath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        val pointer = sqlite.allocateSecret(KEY_SIZE)
        try {
            sqlite.storeSecret(pointer, KEY_SIZE, key, key.size)
            assertEquals(SQL_OK, sqlite.rawKeyAt(db, pointer, KEY_SIZE))
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY)")
        } finally {
            sqlite.closeV2(db)
            sqlite.freeSecret(pointer, KEY_SIZE)
        }
    }

    @Test
    fun `keyConventionallyAt requires a 32 byte key`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        val pointer = sqlite.allocateSecret(1)
        try {
            assertFailsWith<IllegalArgumentException> { sqlite.keyConventionallyAt(db, pointer, 1) }
        } finally {
            sqlite.closeV2(db)
            sqlite.freeSecret(pointer, 1)
        }
    }

    @Test
    fun `raw key byte array size must match its declared length`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            assertFailsWith<IllegalArgumentException> {
                sqlite.keyConventionally(db, ByteArray(1), KEY_SIZE)
            }
            assertFailsWith<IllegalArgumentException> {
                sqlite.rawKey(db, ByteArray(1), KEY_SIZE)
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `key and rekey reject invalid declared lengths without changing caller arrays`() {
        val dbHolder = LongArray(1)
        sqlite.openV2(File(tempDir, "test.db").absolutePath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        val key = ByteArray(KEY_SIZE) { 0x55 }
        val original = key.copyOf()
        try {
            assertFailsWith<IndexOutOfBoundsException> {
                sqlite.key(db, key, -1)
            }
            assertFailsWith<IndexOutOfBoundsException> {
                sqlite.key(db, key, key.size + 1)
            }
            assertFailsWith<IndexOutOfBoundsException> {
                sqlite.rekey(db, key, -1)
            }
            assertFailsWith<IndexOutOfBoundsException> {
                sqlite.rekey(db, key, key.size + 1)
            }
            assertContentEquals(original, key)
        } finally {
            sqlite.closeV2(db)
        }
    }

    @Test
    fun `key and rekey use copies without changing caller arrays`() {
        val dbPath = File(tempDir, "keyed.db").absolutePath
        val originalKey = ByteArray(KEY_SIZE) { 0x66 }
        val replacementKey = ByteArray(KEY_SIZE) { 0x77 }
        val originalKeySnapshot = originalKey.copyOf()
        val replacementKeySnapshot = replacementKey.copyOf()
        val dbHolder = LongArray(1)
        sqlite.openV2(dbPath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        try {
            assertEquals(SQL_OK, sqlite.key(db, originalKey, originalKey.size))
            assertContentEquals(originalKeySnapshot, originalKey)
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY)")
            assertEquals(SQL_OK, sqlite.rekey(db, replacementKey, replacementKey.size))
            assertContentEquals(replacementKeySnapshot, replacementKey)
        } finally {
            sqlite.closeV2(db)
        }

        val reopenHolder = LongArray(1)
        sqlite.openV2(dbPath, SQL_OPEN_READWRITE_OR_CREATE, reopenHolder)
        val reopened = reopenHolder[0]
        try {
            assertEquals(SQL_OK, sqlite.key(reopened, replacementKey, replacementKey.size))
            assertEquals(SQL_OK, sqlite.exec(reopened, "SELECT * FROM t"))
            assertContentEquals(replacementKeySnapshot, replacementKey)
        } finally {
            sqlite.closeV2(reopened)
        }
    }

    @Test
    fun `rekeyAt changes the key of an already keyed database`() {
        val originalKey = ByteArray(KEY_SIZE) { 0x33 }
        val newKey = ByteArray(KEY_SIZE) { 0x44 }
        val dbPath = File(tempDir, "rekeyed.db").absolutePath
        val dbHolder = LongArray(1)
        sqlite.openV2(dbPath, SQL_OPEN_READWRITE_OR_CREATE, dbHolder)
        val db = dbHolder[0]
        val newKeyPointer = sqlite.allocateSecret(KEY_SIZE)
        try {
            assertEquals(SQL_OK, sqlite.keyConventionally(db, originalKey, originalKey.size))
            sqlite.exec(db, "CREATE TABLE t (id INTEGER PRIMARY KEY)")
            sqlite.storeSecret(newKeyPointer, KEY_SIZE, newKey, newKey.size)
            assertEquals(SQL_OK, sqlite.rekeyAt(db, newKeyPointer, KEY_SIZE))
        } finally {
            sqlite.closeV2(db)
            sqlite.freeSecret(newKeyPointer, KEY_SIZE)
        }
        val reopenHolder = LongArray(1)
        sqlite.openV2(dbPath, SQL_OPEN_READWRITE_OR_CREATE, reopenHolder)
        val reopened = reopenHolder[0]
        try {
            assertEquals(SQL_OK, sqlite.key(reopened, newKey, newKey.size))
            assertEquals(SQL_OK, sqlite.exec(reopened, "SELECT * FROM t"))
        } finally {
            sqlite.closeV2(reopened)
        }
    }
}
