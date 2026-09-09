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
private const val VEC1_MODEL_RESIDUAL = 4
private const val VEC1_DISTANCE_L2 = 1
private const val VEC1_META_1BYTE_INT = 2
private const val VEC1_META_4BYTE_INT = 4
private const val VEC1_META_REAL = 8
private const val VEC1_META_TYPE_MASK = 15
private const val VEC1_META_COLUMN_BITS = 8
private const val VEC1_PQ_CODEBOOK_SIZE = 256
private const val VEC1_PQ_BLOCK_SIZE = 16
private const val VEC1_LIST_HEADER_SIZE = 12
private const val VEC1_LIST_64_BIT = 1
private const val VEC1_MAX_CODESIZE = 128
private const val SIZEOF_F32 = 4

private data class ExpectedSqlError(val code: Int, val message: String)
private data class StreamingTable(val name: String, val bucketCount: Int)

internal class Vec1InputsTest {
    @Test
    fun `vec1 from json accepts SQL null without crashing`() = runProbe("null-json")

    @Test
    fun `vec1 rejects an index entry count that overflows signed sizes`() = runProbe("index-overflow")

    @Test
    fun `vec1 validates incremental index blobs before rowid lookup and delete`() =
        runProbe("rowid-list-validation")

    @Test
    fun `vec1 shadow tables are protected by defensive mode`() =
        runProbe("defensive-shadow-tables")

    @Test
    fun `vec1 streaming scans every bucket exactly once across probe ratios`() =
        runProbe("streaming-buckets")

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

    @Test
    fun `vec1 rejects an ANN result count that overflows its heap allocation`() =
        runProbe("oversized-query-k")

    @Test
    fun `vec1 rejects models whose codebooks exceed fixed encoder capacity`() =
        runProbe("oversized-codebook-model")

    @Test
    fun `vec1 rejects residual models without buckets`() = runProbe("residual-without-buckets")

    @Test
    fun `vec1 rejects non-finite JSON vector elements`() = runProbe("non-finite-json")

    @Test
    fun `vec1 rejects non-finite raw vectors without corrupting its index`() =
        runProbe("non-finite-vector")

    @Test
    fun `vec1 training rejects non-finite aggregate input`() = runProbe("non-finite-training")

    @Test
    fun `vec1 rejects non-finite model sections`() = runProbe("non-finite-model")

    @Test
    fun `vec1 pads non-divisible PQ queries without reading past the vector`() =
        runProbe("padded-pq-query")

    @Test
    fun `vec1 validates numeric query options before conversion`() =
        runProbe("numeric-query-options")

    @Test
    fun `vec1 compares non-finite filters without integer conversion`() =
        runProbe("non-finite-meta-filters")

    @Test
    fun `vec1 zeroes unused slots in partial PQ blocks`() = runProbe("pq-block-padding")

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
                "rowid-list-validation" -> probeRowidListValidation(sqlite, db)
                "defensive-shadow-tables" -> probeDefensiveShadowTables(sqlite, db)
                "streaming-buckets" -> probeStreamingBuckets(sqlite, db)
                "truncated-meta" -> probeTruncatedMetadata(sqlite, db)
                "truncated-base-delete" -> probeTruncatedBaseDelete(sqlite, db)
                "truncated-base-distance" -> probeTruncatedBaseDistance(sqlite, db)
                "truncated-base-integrity" -> probeTruncatedBaseIntegrity(sqlite, db)
                "oversized-opq-model" -> probeOversizedOpqModel(sqlite, db)
                "oversized-query-k" -> probeOversizedQueryK(sqlite, db)
                "oversized-codebook-model" -> probeOversizedCodebookModel(sqlite, db)
                "residual-without-buckets" -> probeResidualModelWithoutBuckets(sqlite, db)
                "non-finite-json" -> probeNonFiniteJson(sqlite, db)
                "non-finite-vector" -> probeNonFiniteVector(sqlite, db)
                "non-finite-training" -> probeNonFiniteTraining(sqlite, db)
                "non-finite-model" -> probeNonFiniteModel(sqlite, db)
                "padded-pq-query" -> probePaddedPqQuery(sqlite, db)
                "numeric-query-options" -> probeNumericQueryOptions(sqlite, db)
                "non-finite-meta-filters" -> probeNonFiniteMetadataFilters(sqlite, db)
                "pq-block-padding" -> probePqBlockPadding(sqlite, db)
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

    private fun probeRowidListValidation(sqlite: IExternalSQLite, db: Long) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector)") == SQL_OK)
        executeBlob(
            sqlite,
            db,
            "INSERT INTO t(cmd, arg) VALUES('rebuild', ?)",
            indexedModelHeader()
        )
        check(
            sqlite.exec(
                db,
                "INSERT INTO t(rowid, vector) VALUES(1, vec1_from_json('[1,2,3,4]'))"
            ) == SQL_OK
        )
        val malformedLists = listOf(
            indexHeader(flags = 0, nEntry = 0x40000000, nTombstone = 0),
            indexHeader(flags = VEC1_LIST_64_BIT, nEntry = 0x20000000, nTombstone = 0),
            indexHeader(flags = 0, nEntry = Int.MIN_VALUE, nTombstone = 0),
            indexHeader(flags = 0, nEntry = 1, nTombstone = 2, size = 32),
            indexHeader(flags = 0, nEntry = 1, nTombstone = 0),
            indexHeader(flags = 0, nEntry = 1, nTombstone = 0, size = 16),
            validIndexBlob() + 0.toByte(),
            indexHeader(flags = 4, nEntry = 1, nTombstone = 0, size = 32)
        )
        malformedLists.forEach { blob ->
            executeBlob(sqlite, db, "UPDATE t_idx SET val=? WHERE id=1", blob)
            expectCorrupt(sqlite, db, "SELECT vector FROM t WHERE rowid=1")
            expectCorrupt(sqlite, db, "DELETE FROM t WHERE rowid=1")
            expectSingleRow(sqlite, db, "SELECT rowid FROM t WHERE rowid=1")
        }
        executeBlob(sqlite, db, "UPDATE t_idx SET val=? WHERE id=1", validIndexBlob())
        expectSingleRow(sqlite, db, "SELECT rowid FROM t WHERE vector IS NOT NULL AND rowid=1")
        check(sqlite.exec(db, "DELETE FROM t WHERE rowid=1") == SQL_OK)
        val count = prepare(sqlite, db, "SELECT count(*) FROM t")
        try {
            check(sqlite.step(count) == SQL_ROW)
            check(sqlite.columnInt64(count, 0) == 0L)
        } finally {
            sqlite.finalize(count)
        }
    }

    private fun indexHeader(
        flags: Int,
        nEntry: Int,
        nTombstone: Int,
        size: Int = VEC1_LIST_HEADER_SIZE
    ): ByteArray = ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN).apply {
        putInt(flags)
        putInt(nEntry)
        putInt(nTombstone)
    }.array()

    private fun probeDefensiveShadowTables(sqlite: IExternalSQLite, db: Long) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector)") == SQL_OK)
        check(sqlite.exec(db, "CREATE TABLE ordinary(value INTEGER)") == SQL_OK)
        check(sqlite.exec(db, "INSERT INTO ordinary VALUES(1)") == SQL_OK)
        check(sqlite.exec(db, "CREATE TABLE t_not_shadow(value INTEGER)") == SQL_OK)
        check(sqlite.exec(db, "INSERT INTO t_not_shadow VALUES(1)") == SQL_OK)
        check(sqlite.databaseConfig(db, SQLiteDbConfig.DEFENSIVE.code, 1) == SQL_OK)
        listOf("config", "base", "idx", "model", "meta").forEach { suffix ->
            val table = "t_$suffix"
            val result = sqlite.exec(db, "UPDATE $table SET rowid=rowid")
            check(result != SQL_OK) { "Defensive mode allowed an update to $table" }
            check(sqlite.errorMessage(db).contains("table $table may not be modified")) {
                "Unexpected defensive-mode error for $table: ${sqlite.errorMessage(db)}"
            }
        }
        check(sqlite.exec(db, "UPDATE ordinary SET value=2") == SQL_OK)
        check(sqlite.exec(db, "UPDATE t_not_shadow SET value=2") == SQL_OK)
        check(
            sqlite.exec(
                db,
                "INSERT INTO t(rowid, vector) VALUES(1, vec1_from_json('[1,2]'))"
            ) == SQL_OK
        )
        expectSingleRow(sqlite, db, "SELECT rowid FROM t WHERE rowid=1")
    }

    private fun probeStreamingBuckets(sqlite: IExternalSQLite, db: Long) {
        listOf(
            4 to listOf(1, 2, 3, 4),
            8 to listOf(1, 4, 5, 7, 8)
        ).forEach { (nBucket, probeCounts) ->
            val table = "streaming_$nBucket"
            val streamingTable = StreamingTable(table, nBucket)
            check(sqlite.exec(db, "CREATE VIRTUAL TABLE $table USING vec1(vector)") == SQL_OK)
            executeBlob(
                sqlite,
                db,
                "INSERT INTO $table(cmd, arg) VALUES('rebuild', ?)",
                flatBucketModel(nBucket)
            )

            var rowid = 1
            repeat(nBucket) { bucket ->
                repeat(2) { offset ->
                    check(
                        sqlite.exec(
                            db,
                            "INSERT INTO $table(rowid, vector) " +
                                "VALUES($rowid, vec1_from_json('[${bucket * 100},$offset]'))"
                        ) == SQL_OK
                    )
                    rowid++
                }
            }

            probeCounts.forEach { nProbe ->
                assertStreamingCoverage(sqlite, db, streamingTable, nProbe, null)
            }
            assertStreamingCoverage(sqlite, db, streamingTable, probeCounts.last(), 0.01)
        }
    }

    private fun assertStreamingCoverage(
        sqlite: IExternalSQLite,
        db: Long,
        table: StreamingTable,
        nProbe: Int,
        nProbeSlack: Double?
    ) {
        val slack = nProbeSlack?.let { ",\"nprobe_slack\":$it" }.orEmpty()
        val config = "{\"K\":1,\"nprobe\":$nProbe,\"streaming\":1$slack}"
        val statement = prepare(
            sqlite,
            db,
            "SELECT rowid FROM ${table.name} " +
                "WHERE cmd=vec1_from_json('[0,0]') AND arg='$config'"
        )
        try {
            val rowids = mutableSetOf<Long>()
            var result = sqlite.step(statement)
            while (result == SQL_ROW) {
                val rowid = sqlite.columnInt64(statement, 0)
                check(rowids.add(rowid)) {
                    "Duplicate streaming rowid $rowid for ${table.bucketCount} buckets, " +
                        "nprobe=$nProbe"
                }
                result = sqlite.step(statement)
            }
            check(result == SQL_DONE) { sqlite.errorMessage(db) }
            check(rowids == (1L..table.bucketCount * 2L).toSet()) {
                "Incomplete streaming results for ${table.bucketCount} buckets, " +
                    "nprobe=$nProbe: $rowids"
            }
        } finally {
            sqlite.finalize(statement)
        }
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

    private fun probeOversizedQueryK(sqlite: IExternalSQLite, db: Long) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector)") == SQL_OK)
        check(
            sqlite.exec(
                db,
                "WITH RECURSIVE c(x) AS (" +
                    "VALUES(1) UNION ALL SELECT x+1 FROM c WHERE x<300" +
                    ") INSERT INTO t(rowid,vector) " +
                    "SELECT x,vec1_from_json('[0,0]') FROM c"
            ) == SQL_OK
        )
        check(
            sqlite.exec(
                db,
                "INSERT INTO t(cmd,vector) " +
                    "SELECT 'rebuild',vec1_train(vector) FROM t"
            ) == SQL_OK
        )
        val statement = prepare(
            sqlite,
            db,
            "SELECT count(*) FROM t " +
                "WHERE cmd=vec1_from_json('[0,0]') " +
                "AND arg='{\"K\":1152921504606847232}'"
        )
        try {
            val result = sqlite.step(statement)
            check(result != SQL_ROW && result != SQL_DONE) { "Oversized ANN K was accepted" }
            check(sqlite.errorCode(db) == SQL_ERROR) {
                "Expected SQLITE_ERROR, got $result: ${sqlite.errorMessage(db)}"
            }
            check(
                sqlite.errorMessage(db).contains(
                    "vec1: K must be an integer between 1 and 2147483647"
                )
            )
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun probeOversizedCodebookModel(sqlite: IExternalSQLite, db: Long) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE valid USING vec1(vector)") == SQL_OK)
        executeBlob(
            sqlite,
            db,
            "INSERT INTO valid(cmd, arg) VALUES('rebuild', ?)",
            codebookModel(VEC1_MAX_CODESIZE)
        )
        check(
            sqlite.exec(
                db,
                "INSERT INTO valid(vector) VALUES(vec1_from_json('[1,1]'))"
            ) == SQL_OK
        )
        listOf(VEC1_MAX_CODESIZE + 1, 200).forEach { nCodebook ->
            check(
                sqlite.exec(
                    db,
                    "CREATE VIRTUAL TABLE oversized_$nCodebook USING vec1(vector)"
                ) == SQL_OK
            )
            val statement = prepare(
                sqlite,
                db,
                "INSERT INTO oversized_$nCodebook(cmd, arg) VALUES('rebuild', ?)"
            )
            try {
                val model = codebookModel(nCodebook)
                check(sqlite.bindBlob(statement, 1, model, model.size) == SQL_OK)
                val result = sqlite.step(statement)
                check(result != SQL_ROW && result != SQL_DONE) {
                    "Model with $nCodebook codebooks was accepted"
                }
                check(sqlite.errorCode(db) == SQL_CORRUPT) {
                    "Expected SQLITE_CORRUPT, got $result: ${sqlite.errorMessage(db)}"
                }
                check(
                    sqlite.errorMessage(db).contains(
                        "vec1: invalid nCodebook value: $nCodebook"
                    )
                )
            } finally {
                sqlite.finalize(statement)
            }
        }
    }

    private fun probeResidualModelWithoutBuckets(sqlite: IExternalSQLite, db: Long) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector)") == SQL_OK)
        expectBlobError(
            sqlite,
            db,
            "INSERT INTO t(cmd, arg) VALUES('rebuild', ?)",
            residualModelWithoutBuckets(),
            ExpectedSqlError(SQL_CORRUPT, "residual model requires PQ and at least two buckets")
        )
    }

    private fun probeNonFiniteJson(sqlite: IExternalSQLite, db: Long) {
        listOf("NaN", "Infinity", "-Infinity", "1e400").forEach { value ->
            expectError(
                sqlite,
                db,
                "SELECT vec1_from_json('[$value,0]')",
                SQL_ERROR,
                "vector elements must be finite"
            )
        }
    }

    private fun probeNonFiniteVector(sqlite: IExternalSQLite, db: Long) {
        createQuantizedTable(sqlite, db)
        rejectNonFiniteVectorInputs(sqlite, db)
        acceptFiniteVectorBoundaries(sqlite, db)
        rejectNonFinitePersistedVector(sqlite, db)
    }

    private fun createQuantizedTable(sqlite: IExternalSQLite, db: Long) {
        val zeroVector = List(8) { "0" }.joinToString(prefix = "[", postfix = "]")
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector)") == SQL_OK)
        check(
            sqlite.exec(
                db,
                "WITH RECURSIVE c(x) AS (" +
                    "VALUES(1) UNION ALL SELECT x+1 FROM c WHERE x<8" +
                    ") INSERT INTO t(rowid,vector) " +
                    "SELECT x,vec1_from_json('$zeroVector') FROM c"
            ) == SQL_OK
        )
        check(
            sqlite.exec(
                db,
                "INSERT INTO t(cmd,vector) " +
                    "SELECT 'rebuild',vec1_train(vector,'{\"nbucket\":2}') FROM t"
            ) == SQL_OK
        )
    }

    private fun rejectNonFiniteVectorInputs(sqlite: IExternalSQLite, db: Long) {
        val statements = listOf(
            "INSERT INTO t(vector) VALUES(?)",
            "SELECT rowid FROM t WHERE cmd=? AND arg=1",
            "SELECT vec1_train(?)",
            "SELECT vec1_l2_distance(?, zeroblob(32))",
            "SELECT vec1_to_json(?)"
        )
        val expected = ExpectedSqlError(SQL_ERROR, "vector elements must be finite")
        listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { value ->
            val vector = vectorBytes(value, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
            statements.forEach { expectBlobError(sqlite, db, it, vector, expected) }
        }
    }

    private fun probeNonFiniteTraining(sqlite: IExternalSQLite, db: Long) {
        val sql = "WITH RECURSIVE c(x) AS (" +
            "VALUES(1) UNION ALL SELECT x+1 FROM c WHERE x<512" +
            ") SELECT length(vec1_train(?,'{\"codesize\":8}')) FROM c"
        val expected = ExpectedSqlError(SQL_ERROR, "vector elements must be finite")
        listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { value ->
            expectBlobError(
                sqlite,
                db,
                sql,
                vectorBytes(value, value, value, value, value, value, value, value),
                expected
            )
        }
    }

    private fun acceptFiniteVectorBoundaries(sqlite: IExternalSQLite, db: Long) {
        executeBlob(
            sqlite,
            db,
            "INSERT INTO t(vector) VALUES(?)",
            vectorBytes(Float.MAX_VALUE, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )
        executeBlob(
            sqlite,
            db,
            "INSERT INTO t(vector) VALUES(?)",
            vectorBytes(Float.MIN_VALUE, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )
    }

    private fun rejectNonFinitePersistedVector(sqlite: IExternalSQLite, db: Long) {
        executeBlob(
            sqlite,
            db,
            "UPDATE t_base SET vector=? WHERE id=1",
            vectorBytes(Float.NaN, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )
        expectError(
            sqlite,
            db,
            "INSERT INTO t(cmd) VALUES('rebuild')",
            SQL_CORRUPT,
            "non-finite vector in t_base"
        )
    }

    private fun probeNonFiniteModel(sqlite: IExternalSQLite, db: Long) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector)") == SQL_OK)
        expectBlobError(
            sqlite,
            db,
            "INSERT INTO t(cmd, arg) VALUES('rebuild', ?)",
            nonFiniteCentroidModel(),
            ExpectedSqlError(SQL_CORRUPT, "non-finite value in model centroid section")
        )
    }

    private fun probePaddedPqQuery(sqlite: IExternalSQLite, db: Long) {
        val vector = List(11) { "0" }.joinToString(prefix = "[", postfix = "]")
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE t USING vec1(vector)") == SQL_OK)
        check(
            sqlite.exec(
                db,
                "WITH RECURSIVE c(x) AS (" +
                    "VALUES(1) UNION ALL SELECT x+1 FROM c WHERE x<512" +
                    ") INSERT INTO t(rowid,vector) " +
                    "SELECT x,vec1_from_json('$vector') FROM c"
            ) == SQL_OK
        )
        check(
            sqlite.exec(
                db,
                "INSERT INTO t(cmd,vector) " +
                    "SELECT 'rebuild',vec1_train(vector,'{\"codesize\":8}') FROM t"
            ) == SQL_OK
        )
        val statement = prepare(
            sqlite,
            db,
            "SELECT count(*) FROM t " +
                "WHERE cmd=vec1_from_json('$vector') AND arg=1"
        )
        try {
            check(sqlite.step(statement) == SQL_ROW)
            check(sqlite.columnInt64(statement, 0) == 1L)
            check(sqlite.step(statement) == SQL_DONE)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun probeNumericQueryOptions(sqlite: IExternalSQLite, db: Long) {
        expectError(
            sqlite,
            db,
            "SELECT vec1_config('nprobe', 1e999)",
            SQL_ERROR,
            "nprobe requires a finite value larger than 0.0"
        )

        check(sqlite.exec(db, "CREATE VIRTUAL TABLE numeric_options USING vec1(vector)") == SQL_OK)
        executeBlob(
            sqlite,
            db,
            "INSERT INTO numeric_options(cmd, arg) VALUES('rebuild', ?)",
            indexedModelHeader()
        )
        check(
            sqlite.exec(
                db,
                "INSERT INTO numeric_options(rowid, vector) " +
                    "VALUES(1, vec1_from_json('[0,0,0,0]'))"
            ) == SQL_OK
        )

        val invalidOptions = listOf(
            "{\"K\":1,\"nprobe\":1e999}" to "nprobe requires a finite numeric value",
            "{\"K\":1,\"nprobe\":\"1\"}" to "nprobe requires a finite numeric value",
            "{\"K\":1,\"streaming\":1e999}" to "streaming requires a finite numeric value",
            "{\"K\":1,\"streaming\":\"1\"}" to "streaming requires a finite numeric value",
            "{\"K\":1,\"nprobe_slack\":1e999}" to
                "nprobe_slack requires a finite numeric value",
            "{\"K\":1e999}" to "K must be an integer between 1 and 2147483647"
        )
        invalidOptions.forEach { (arg, message) ->
            expectError(
                sqlite,
                db,
                "SELECT rowid FROM numeric_options " +
                    "WHERE cmd=vec1_from_json('[0,0,0,0]') AND arg='$arg'",
                SQL_ERROR,
                message
            )
        }

        check(sqlite.exec(db, "SELECT vec1_config('nprobe', 1e300)") == SQL_OK)
        expectSingleRow(
            sqlite,
            db,
            "SELECT rowid FROM numeric_options " +
                "WHERE cmd=vec1_from_json('[0,0,0,0]') AND arg=1"
        )
        expectSingleRow(
            sqlite,
            db,
            "SELECT rowid FROM numeric_options " +
                "WHERE cmd=vec1_from_json('[0,0,0,0]') " +
                "AND arg='{\"K\":1,\"nprobe\":1e300,\"streaming\":1e300}'"
        )
    }

    private fun probeNonFiniteMetadataFilters(sqlite: IExternalSQLite, db: Long) {
        createIntegerMetadataTable(sqlite, db, "meta_byte", listOf(0, 1, 2))
        expectMetadataFormat(sqlite, db, "meta_byte", VEC1_META_1BYTE_INT)
        createIntegerMetadataTable(sqlite, db, "meta_int", listOf(-2, -1, 0))
        expectMetadataFormat(sqlite, db, "meta_int", VEC1_META_4BYTE_INT)

        listOf("meta_byte", "meta_int").forEach { table ->
            expectMetadataCount(sqlite, db, table, "< 1e999", 3)
            expectMetadataCount(sqlite, db, table, "<= 1e999", 3)
            expectMetadataCount(sqlite, db, table, "> 1e999", 0)
            expectMetadataCount(sqlite, db, table, ">= 1e999", 0)
            expectMetadataCount(sqlite, db, table, "= 1e999", 0)
            expectMetadataCount(sqlite, db, table, "> -1e999", 3)
            expectMetadataCount(sqlite, db, table, ">= -1e999", 3)
            expectMetadataCount(sqlite, db, table, "< -1e999", 0)
            expectMetadataCount(sqlite, db, table, "<= -1e999", 0)
            expectMetadataCount(sqlite, db, table, "= -1e999", 0)
            expectMetadataCount(sqlite, db, table, "< 1e300", 3)
            expectMetadataCount(sqlite, db, table, "> -1e300", 3)
        }
        expectMetadataCount(sqlite, db, "meta_byte", "IN (1e999, 1)", 1)
        expectMetadataCount(sqlite, db, "meta_int", "IN (-1e999, -1)", 1)

        expectMetadataCount(sqlite, db, "meta_int", "< -1.5", 1)
        expectMetadataCount(sqlite, db, "meta_int", "<= -1.5", 1)
        expectMetadataCount(sqlite, db, "meta_int", "> -1.5", 2)
        expectMetadataCount(sqlite, db, "meta_int", ">= -1.5", 2)
        expectMetadataCount(sqlite, db, "meta_int", "= -1.5", 0)
        expectBoundMetadataCount(sqlite, db, "meta_int", Double.NaN, 0)
    }

    private fun createIntegerMetadataTable(
        sqlite: IExternalSQLite,
        db: Long,
        table: String,
        values: List<Int>
    ) {
        check(sqlite.exec(db, "CREATE VIRTUAL TABLE $table USING vec1(vector, tag)") == SQL_OK)
        executeBlob(
            sqlite,
            db,
            "INSERT INTO $table(cmd, arg) VALUES('rebuild', ?)",
            indexedModelHeader()
        )
        val rows = values.mapIndexed { index, value ->
            "(${index + 1}, vec1_from_json('[0,0,0,0]'), $value)"
        }.joinToString()
        check(sqlite.exec(db, "INSERT INTO $table(rowid, vector, tag) VALUES $rows") == SQL_OK)
    }

    private fun expectMetadataFormat(
        sqlite: IExternalSQLite,
        db: Long,
        table: String,
        expected: Int
    ) {
        val statement = prepare(sqlite, db, "SELECT val FROM ${table}_meta")
        try {
            check(sqlite.step(statement) == SQL_ROW)
            val blob = checkNotNull(sqlite.columnBlob(statement, 0))
            val format = ByteBuffer.wrap(blob).order(ByteOrder.BIG_ENDIAN).getInt(0)
            check(format and VEC1_META_TYPE_MASK == expected)
            check(sqlite.step(statement) == SQL_DONE)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun probePqBlockPadding(sqlite: IExternalSQLite, db: Long) {
        val nCodebook = 8
        listOf(1, 15, 16, 17).forEach { nEntry ->
            val table = "pq_padding_$nEntry"
            check(sqlite.exec(db, "CREATE VIRTUAL TABLE $table USING vec1(vector)") == SQL_OK)
            executeBlob(
                sqlite,
                db,
                "INSERT INTO $table(cmd, arg) VALUES('rebuild', ?)",
                codebookModel(nCodebook)
            )
            check(
                sqlite.exec(
                    db,
                    "WITH RECURSIVE c(x) AS (" +
                        "VALUES(1) UNION ALL SELECT x+1 FROM c WHERE x<$nEntry" +
                        ") INSERT INTO $table(rowid, vector) " +
                        "SELECT x, vec1_from_json('[0,0]') FROM c"
                ) == SQL_OK
            )
            val blob = readSingleIndexBlob(sqlite, db, table)
            assertPqPaddingIsZero(blob, nEntry, nCodebook)
            if (nEntry == 1) {
                poisonPqPadding(blob, nEntry, nCodebook)
                executeBlob(sqlite, db, "UPDATE ${table}_idx SET val=?", blob)
                check(
                    sqlite.exec(
                        db,
                        "INSERT INTO $table(rowid, vector) " +
                            "VALUES(2, vec1_from_json('[0,0]'))"
                    ) == SQL_OK
                )
                assertPqPaddingIsZero(readSingleIndexBlob(sqlite, db, table), 2, nCodebook)
            }
        }
    }

    private fun readSingleIndexBlob(sqlite: IExternalSQLite, db: Long, table: String): ByteArray {
        val statement = prepare(sqlite, db, "SELECT val FROM ${table}_idx")
        try {
            check(sqlite.step(statement) == SQL_ROW)
            val blob = checkNotNull(sqlite.columnBlob(statement, 0))
            check(sqlite.step(statement) == SQL_DONE)
            return blob
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun expectMetadataCount(
        sqlite: IExternalSQLite,
        db: Long,
        table: String,
        filter: String,
        expected: Long
    ) {
        val statement = prepare(
            sqlite,
            db,
            "SELECT count(*) FROM $table " +
                "WHERE cmd=vec1_from_json('[0,0,0,0]') AND arg=10 AND tag $filter"
        )
        try {
            check(sqlite.step(statement) == SQL_ROW)
            check(sqlite.columnInt64(statement, 0) == expected)
            check(sqlite.step(statement) == SQL_DONE)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun expectBoundMetadataCount(
        sqlite: IExternalSQLite,
        db: Long,
        table: String,
        value: Double,
        expected: Long
    ) {
        val statement = prepare(
            sqlite,
            db,
            "SELECT count(*) FROM $table " +
                "WHERE cmd=vec1_from_json('[0,0,0,0]') AND arg=10 AND tag < ?"
        )
        try {
            check(sqlite.bindDouble(statement, 1, value) == SQL_OK)
            check(sqlite.step(statement) == SQL_ROW)
            check(sqlite.columnInt64(statement, 0) == expected)
            check(sqlite.step(statement) == SQL_DONE)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun assertPqPaddingIsZero(blob: ByteArray, nEntry: Int, nCodebook: Int) {
        val dataOffset = pqDataOffset(blob, nEntry)
        val nBlock = (nEntry + VEC1_PQ_BLOCK_SIZE - 1) / VEC1_PQ_BLOCK_SIZE
        check(blob.size == dataOffset + nBlock * VEC1_PQ_BLOCK_SIZE * nCodebook)
        val nUsed = nEntry % VEC1_PQ_BLOCK_SIZE
        if (nUsed == 0) return
        val blockOffset = dataOffset + (nBlock - 1) * VEC1_PQ_BLOCK_SIZE * nCodebook
        repeat(nCodebook) { iCodebook ->
            for (iSlot in nUsed until VEC1_PQ_BLOCK_SIZE) {
                val offset = blockOffset + iCodebook * VEC1_PQ_BLOCK_SIZE + iSlot
                check(blob[offset] == 0.toByte()) { "Non-zero PQ padding at offset $offset" }
            }
        }
    }

    private fun poisonPqPadding(blob: ByteArray, nEntry: Int, nCodebook: Int) {
        val dataOffset = pqDataOffset(blob, nEntry)
        val nUsed = nEntry % VEC1_PQ_BLOCK_SIZE
        repeat(nCodebook) { iCodebook ->
            for (iSlot in nUsed until VEC1_PQ_BLOCK_SIZE) {
                blob[dataOffset + iCodebook * VEC1_PQ_BLOCK_SIZE + iSlot] = 0x5A
            }
        }
    }

    private fun pqDataOffset(blob: ByteArray, nEntry: Int): Int {
        val header = ByteBuffer.wrap(blob).order(ByteOrder.BIG_ENDIAN)
        val flags = header.getInt(0)
        check(header.getInt(4) == nEntry)
        val rowidSize = if (flags and VEC1_LIST_64_BIT != 0) Long.SIZE_BYTES else Int.SIZE_BYTES
        return VEC1_LIST_HEADER_SIZE + nEntry * rowidSize
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

    private fun flatBucketModel(nBucket: Int): ByteArray =
        ByteBuffer.allocate(24 + nBucket * 2 * SIZEOF_F32).apply {
            order(ByteOrder.BIG_ENDIAN)
            putInt(4)
            putInt(VEC1_MODEL_INDEX)
            putInt(2)
            putInt(0)
            putInt(nBucket)
            putInt(VEC1_DISTANCE_L2)
            order(ByteOrder.nativeOrder())
            repeat(nBucket) { bucket ->
                putFloat(bucket * 100f)
                putFloat(0f)
            }
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

    private fun codebookModel(nCodebook: Int): ByteArray {
        val nElem = 2
        val nCodeElem = (nElem + nCodebook - 1) / nCodebook
        val modelSize = 24 + nCodebook * nCodeElem * VEC1_PQ_CODEBOOK_SIZE * SIZEOF_F32
        return ByteBuffer.allocate(modelSize).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(4)
            putInt(VEC1_MODEL_INDEX)
            putInt(nElem)
            putInt(nCodebook)
            putInt(0)
            putInt(VEC1_DISTANCE_L2)
        }.array()
    }

    private fun residualModelWithoutBuckets(): ByteArray {
        val nElem = 2
        val nCodebook = 1
        val modelSize = 24 + nCodebook * nElem * VEC1_PQ_CODEBOOK_SIZE * SIZEOF_F32
        return ByteBuffer.allocate(modelSize).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(4)
            putInt(VEC1_MODEL_INDEX or VEC1_MODEL_RESIDUAL)
            putInt(nElem)
            putInt(nCodebook)
            putInt(0)
            putInt(VEC1_DISTANCE_L2)
        }.array()
    }

    private fun nonFiniteCentroidModel(): ByteArray {
        val model = ByteArray(24 + 4 * SIZEOF_F32)
        ByteBuffer.wrap(model).order(ByteOrder.BIG_ENDIAN).apply {
            putInt(4)
            putInt(VEC1_MODEL_INDEX)
            putInt(2)
            putInt(0)
            putInt(2)
            putInt(VEC1_DISTANCE_L2)
        }
        ByteBuffer.wrap(model).order(ByteOrder.nativeOrder()).apply {
            position(24)
            putFloat(Float.NaN)
            putFloat(0f)
            putFloat(0f)
            putFloat(0f)
        }
        return model
    }

    private fun vectorBytes(vararg values: Float): ByteArray =
        ByteBuffer.allocate(values.size * SIZEOF_F32).order(ByteOrder.nativeOrder()).apply {
            values.forEach { putFloat(it) }
        }.array()

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

    private fun expectBlobError(
        sqlite: IExternalSQLite,
        db: Long,
        sql: String,
        blob: ByteArray,
        expected: ExpectedSqlError
    ) {
        val statement = prepare(sqlite, db, sql)
        try {
            check(sqlite.bindBlob(statement, 1, blob, blob.size) == SQL_OK)
            expectStatementError(sqlite, db, statement, expected.code, expected.message)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun expectError(
        sqlite: IExternalSQLite,
        db: Long,
        sql: String,
        expectedCode: Int,
        message: String
    ) {
        val statement = prepare(sqlite, db, sql)
        try {
            expectStatementError(sqlite, db, statement, expectedCode, message)
        } finally {
            sqlite.finalize(statement)
        }
    }

    private fun expectStatementError(
        sqlite: IExternalSQLite,
        db: Long,
        statement: Long,
        expectedCode: Int,
        message: String
    ) {
        val result = sqlite.step(statement)
        check(result != SQL_ROW && result != SQL_DONE) { "Malformed vec1 input was accepted" }
        check(sqlite.errorCode(db) == expectedCode) {
            "Expected SQLite error $expectedCode, got $result: ${sqlite.errorMessage(db)}"
        }
        check(sqlite.errorMessage(db).contains(message)) {
            "Expected '$message', got: ${sqlite.errorMessage(db)}"
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
