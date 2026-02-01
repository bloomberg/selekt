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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class ExternalSQLiteTest {
    private companion object {
        val sqlite = externalSQLiteSingleton()

        const val SQL_OPEN_READONLY = 1
        const val SQL_OPEN_READWRITE = SQL_OPEN_READONLY shl 1
        const val SQL_OPEN_CREATE = SQL_OPEN_READWRITE shl 1
        const val SQL_OPEN_READWRITE_OR_CREATE = SQL_OPEN_READWRITE or SQL_OPEN_CREATE
        const val SQL_ROW = 100
        const val SQL_BLOB = 4
        const val SQL_NULL = 5
    }

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `externalSQLiteSingleton creates instance`() {
        assertNotNull(sqlite)
    }

    @Test
    fun `externalSQLiteSingleton creates singleton`() {
        assertFailsWith<IllegalStateException> {
            externalSQLiteSingleton()
        }
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
                assertEquals(SQL_OK, sqlite.bindBlob(statement, 1, blob, blob.size))
                assertEquals(SQL_ROW, sqlite.step(statement))
                val result = sqlite.columnBlob(statement, 0)
                assertNotNull(result)
                assertEquals(blob.toList(), result.toList())
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
                val text = "Hello 世界 🌍 Привет"
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
            // Transaction state can be 0 (none), 1 (read), or 2 (write)
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
}
