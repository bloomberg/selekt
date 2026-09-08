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

package com.bloomberg.selekt

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

private const val SQL_OPEN_READWRITE = 2
private const val SQL_OPEN_CREATE = 4
private const val VEC1_MODEL_INDEX = 1
private const val VEC1_DISTANCE_L2 = 1
private const val VEC1_META_REAL = 8
private const val VEC1_META_COLUMN_BITS = 8
private const val VEC1_PQ_CODEBOOK_SIZE = 256
private const val SIZEOF_F32 = 4

internal class Vec1InputsTest {
    @Test
    fun `vec1 from json accepts SQL null without crashing`() = runProbe("null-json")

    @Test
    fun `vec1 rejects an index entry count that overflows signed sizes`() = runProbe("index-overflow")

    @Test
    fun `vec1 rejects truncated real metadata before query-time decoding`() = runProbe("truncated-meta")

    @Test
    fun `vec1 validates base vector size before delete-time transformation`() = runProbe("truncated-base-delete")

    @Test
    fun `vec1 validates base vector size before distance statistics`() = runProbe("truncated-base-distance")

    @Test
    fun `vec1 validates base vector size before integrity checking`() = runProbe("truncated-base-integrity")

    @Test
    fun `vec1 train rejects an oversized OPQ model without overflowing`() = runProbe("oversized-opq-model")

    private fun runProbe(mode: String) {
        val command = mutableListOf(
            Path.of(System.getProperty("java.home"), "bin", "java").toString()
        )
        if (Runtime.version().feature() >= 25) {
            command += "--enable-native-access=ALL-UNNAMED"
        }
        System.getProperty("com.bloomberg.selekt.library_path")?.let {
            command += "-Dcom.bloomberg.selekt.library_path=$it"
        }
        command += listOf(
            "-cp",
            System.getProperty("java.class.path"),
            Vec1SecurityProbeMain::class.java.name,
            mode
        )

        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val completed = process.waitFor(30, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            process.waitFor()
        }
        assertTrue(completed, "vec1 probe timed out")
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(0, process.exitValue(), "vec1 probe '$mode' failed:\n$output")
    }
}

internal object Vec1SecurityProbeMain {
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 1)
        val sqlite = externalSQLiteSingleton()
        val dbHolder = LongArray(1)
        check(sqlite.openV2(":memory:", SQL_OPEN_READWRITE or SQL_OPEN_CREATE, dbHolder) == SQL_OK)
        val db = dbHolder[0]
        try {
            when (args.single()) {
                "null-json" -> probeNullJson(sqlite, db)
                "index-overflow" -> probeIndexOverflow(sqlite, db)
                "truncated-meta" -> probeTruncatedMetadata(sqlite, db)
                "truncated-base-delete" -> probeTruncatedBaseDelete(sqlite, db)
                "truncated-base-distance" -> probeTruncatedBaseDistance(sqlite, db)
                "truncated-base-integrity" -> probeTruncatedBaseIntegrity(sqlite, db)
                "oversized-opq-model" -> probeOversizedOpqModel(sqlite, db)
                else -> error("Unknown probe")
            }
        } finally {
            sqlite.closeV2(db)
        }
    }

    private fun probeNullJson(sqlite: IExternalSQLite, db: Long) {
        val statement = prepare(sqlite, db, "SELECT vec1_from_json(NULL)")
        try {
            check(sqlite.step(statement) == SQL_ROW)
            check(sqlite.columnType(statement, 0) == 5)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun probeIndexOverflow(sqlite: IExternalSQLite, db: Long) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector)") == SQL_OK)
        executeBlob(
            sqlite,
            db,
            "INSERT INTO t(cmd, arg) VALUES('rebuild', ?)",
            indexedModelHeader()
        )
        executeBlob(
            sqlite,
            db,
            "INSERT INTO t_idx VALUES(1, 0, 1, 1, ?)",
            ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN).apply {
                putInt(0)
                putInt(0x40000000)
                putInt(0)
            }.array()
        )
        expectCorrupt(
            sqlite,
            db,
            "SELECT rowid FROM t " +
                "WHERE cmd=vec1_from_json('[0,0,0,0]') AND arg=1"
        )
    }

    private fun probeTruncatedMetadata(sqlite: IExternalSQLite, db: Long) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector, tag)") == SQL_OK)
        executeBlob(
            sqlite,
            db,
            "INSERT INTO t(cmd, arg) VALUES('rebuild', ?)",
            indexedModelHeader()
        )
        executeBlob(
            sqlite,
            db,
            "INSERT INTO t_idx VALUES(1, 0, 1, 1, ?)",
            validIndexBlob()
        )
        executeBlob(
            sqlite,
            db,
            "INSERT INTO t_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN).apply {
                putInt(VEC1_META_REAL)
                putInt(1)
                putDouble(1.0)
            }.array()
        )
        val query =
            "SELECT rowid FROM t " +
                "WHERE cmd=vec1_from_json('[0,0,0,0]') AND arg=1 AND tag=1.0"
        expectSingleRow(sqlite, db, query)
        executeBlob(
            sqlite,
            db,
            "REPLACE INTO t_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).apply {
                putInt(VEC1_META_REAL)
                putInt(1)
            }.array()
        )
        expectCorrupt(sqlite, db, query)
    }

    private fun probeTruncatedBaseDelete(sqlite: IExternalSQLite, db: Long) {
        createQuantizedTableWithCorruptBase(sqlite, db)
        expectCorrupt(sqlite, db, "DELETE FROM t WHERE rowid=1")
    }

    private fun probeTruncatedBaseDistance(sqlite: IExternalSQLite, db: Long) {
        createQuantizedTableWithCorruptBase(sqlite, db)
        expectCorrupt(sqlite, db, "SELECT distance FROM t WHERE rowid=1")
    }

    private fun probeTruncatedBaseIntegrity(sqlite: IExternalSQLite, db: Long) {
        createQuantizedTableWithCorruptBase(sqlite, db)
        val statement = prepare(sqlite, db, "PRAGMA integrity_check")
        try {
            check(sqlite.step(statement) == SQL_ROW)
            check(sqlite.columnText(statement, 0).contains("vector in %_base row 1 is wrong size"))
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun probeOversizedOpqModel(sqlite: IExternalSQLite, db: Long) {
        val statement = prepare(
            sqlite,
            db,
            "SELECT length(vec1_train(zeroblob(200000), '{\"opq\":true}'))"
        )
        try {
            val result = sqlite.step(statement)
            check(result != SQL_ROW && result != SQL_DONE) { "Oversized OPQ model was accepted" }
            check(sqlite.errorCode(db) == SQL_TOO_BIG) {
                "Expected SQLITE_TOOBIG, got $result: ${sqlite.errorMessage(db)}"
            }
            check(sqlite.errorMessage(db).contains("rotation section too large"))
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun createQuantizedTableWithCorruptBase(sqlite: IExternalSQLite, db: Long) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector)") == SQL_OK)
        executeBlob(
            sqlite,
            db,
            "INSERT INTO t(cmd, arg) VALUES('rebuild', ?)",
            quantizedModel()
        )
        check(
            sqlite.exec(
                db,
                "INSERT INTO t(rowid, vector) VALUES(1, vec1_from_json('[1,2,3,4]'))"
            ) == SQL_OK
        )
        check(sqlite.exec(db, "UPDATE t_base SET vector=X'00' WHERE id=1") == SQL_OK)
    }

    private fun indexedModelHeader(): ByteArray =
        ByteBuffer.allocate(24).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(4)
            putInt(VEC1_MODEL_INDEX)
            putInt(4)
            putInt(0)
            putInt(0)
            putInt(1)
        }.array()

    private fun quantizedModel(): ByteArray {
        val nElem = 4
        val nCodebook = 1
        val nBucket = 1
        val modelSize = 24 + nCodebook * nElem * VEC1_PQ_CODEBOOK_SIZE * SIZEOF_F32 +
            nBucket * nElem * SIZEOF_F32
        return ByteBuffer.allocate(modelSize).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(4)
            putInt(VEC1_MODEL_INDEX)
            putInt(nElem)
            putInt(nCodebook)
            putInt(nBucket)
            putInt(VEC1_DISTANCE_L2)
        }.array()
    }

    private fun validIndexBlob(): ByteArray =
        ByteBuffer.allocate(32).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(0)
            putInt(1)
            putInt(0)
            putInt(1)
            repeat(4) { putFloat(0f) }
        }.array()

    private fun executeBlob(sqlite: IExternalSQLite, db: Long, sql: String, blob: ByteArray) {
        val statement = prepare(sqlite, db, sql)
        try {
            check(sqlite.bindBlob(statement, 1, blob, blob.size) == SQL_OK)
            check(sqlite.step(statement) == SQL_DONE)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun expectCorrupt(sqlite: IExternalSQLite, db: Long, sql: String) {
        val statement = prepare(sqlite, db, sql)
        try {
            val result = sqlite.step(statement)
            check(result != SQL_ROW && result != SQL_DONE) { "Malformed vec1 data was accepted" }
            check(sqlite.errorCode(db) == SQL_CORRUPT) {
                "Expected SQLITE_CORRUPT, got $result: ${sqlite.errorMessage(db)}"
            }
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun expectSingleRow(sqlite: IExternalSQLite, db: Long, sql: String) {
        val statement = prepare(sqlite, db, sql)
        try {
            check(sqlite.step(statement) == SQL_ROW)
            check(sqlite.columnInt64(statement, 0) == 1L)
            check(sqlite.step(statement) == SQL_DONE)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun prepare(sqlite: IExternalSQLite, db: Long, sql: String): Long {
        val statementHolder = LongArray(1)
        check(sqlite.prepareV2(db, sql, sql.length, statementHolder) == SQL_OK) {
            sqlite.errorMessage(db)
        }
        return statementHolder[0]
    }
}
