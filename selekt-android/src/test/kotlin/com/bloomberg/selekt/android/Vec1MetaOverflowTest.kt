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

package com.bloomberg.selekt.android

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertFails
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

private const val VEC1_HEADER_VERSION = 4
private const val VEC1_MODEL_INDEX = 1
private const val VEC1_DISTANCE_L2 = 1
private const val VEC1_META_COLUMN_BITS = 8
private const val VEC1_META_GENERIC = 0x01
private const val SIZEOF_F32 = 4
private const val SIZEOF_U32 = 4
private const val VEC1_LIST_SZHDR = 3 * SIZEOF_U32
private const val VEC1_META_SZHDR = 2 * SIZEOF_U32

private fun indexedModelHeader(nElem: Int): ByteArray = ByteBuffer.allocate(24).order(ByteOrder.BIG_ENDIAN).apply {
    putInt(0, VEC1_HEADER_VERSION)
    putInt(4, VEC1_MODEL_INDEX)
    putInt(8, nElem)
    putInt(12, 0)
    putInt(16, 0)
    putInt(20, VEC1_DISTANCE_L2)
}.array()

private fun vectorBlob(vararg values: Float): ByteArray = ByteBuffer.allocate(values.size * SIZEOF_F32)
    .order(ByteOrder.BIG_ENDIAN).apply {
        values.forEach(::putFloat)
    }.array()

private fun idxListBlob(
    rowid: Int,
    vector: FloatArray
): ByteArray = ByteBuffer.allocate(VEC1_LIST_SZHDR + SIZEOF_U32 + vector.size * SIZEOF_F32)
    .order(ByteOrder.BIG_ENDIAN).apply {
        putInt(0)
        putInt(1)
        putInt(0)
        putInt(rowid)
        vector.forEach(::putFloat)
    }.array()

private fun oversizedGenericMetaBlob(nFakeEntry: Int): ByteArray =
    ByteBuffer.allocate(VEC1_META_SZHDR + nFakeEntry * 2).order(ByteOrder.BIG_ENDIAN).apply {
        putInt(VEC1_META_GENERIC)
        putInt(nFakeEntry)
        repeat(nFakeEntry) {
            put(1)
            put(0)
        }
    }.array()

private fun sqliteVarIntBytes(value: Long): ByteArray {
    val groups = mutableListOf<Int>()
    var v = value
    do {
        groups.add((v and 0x7f).toInt())
        v = v ushr 7
    } while (v != 0L)
    val n = groups.size
    return ByteArray(n) { i ->
        val group = groups[n - 1 - i]
        (if (i == n - 1) group else group or 0x80).toByte()
    }
}

private fun genericTextMetaBlob(textLength: Int, truncateByBytes: Int = 0): ByteArray {
    val varInt = sqliteVarIntBytes((textLength * 2 + 5).toLong())
    return ByteBuffer.allocate(VEC1_META_SZHDR + varInt.size + textLength - truncateByBytes)
        .order(ByteOrder.BIG_ENDIAN).apply {
            putInt(VEC1_META_GENERIC)
            putInt(1)
            put(varInt)
            repeat(textLength - truncateByBytes) {
                put(0)
            }
        }.array()
}

private fun genericRawEntryMetaBlob(entryBytes: ByteArray): ByteArray =
    ByteBuffer.allocate(VEC1_META_SZHDR + entryBytes.size).order(ByteOrder.BIG_ENDIAN).apply {
        putInt(VEC1_META_GENERIC)
        putInt(1)
        put(entryBytes)
    }.array()

internal class Vec1MetaOverflowSecurityTest {
    private val database = SQLiteDatabase.createInMemoryDatabase()

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun rejectsMetaListWithMoreValuesThanItsIdxListEntryCount(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t1 USING vec1(vector, tag)")
        exec("INSERT INTO t1(cmd, arg) VALUES('rebuild', ?)", arrayOf(indexedModelHeader(nElem = 4)))
        exec(
            "INSERT INTO t1_idx VALUES(1, 0, 1, 1, ?)",
            arrayOf(idxListBlob(rowid = 1, vector = floatArrayOf(0f, 0f, 0f, 0f)))
        )
        exec(
            "INSERT INTO t1_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            arrayOf(oversizedGenericMetaBlob(nFakeEntry = 4_000))
        )
        assertFails {
            exec(
                "INSERT INTO t1(rowid, vector, tag) VALUES(2, ?, 9)",
                arrayOf(vectorBlob(1f, 1f, 1f, 1f))
            )
        }
    }

    @Test
    fun acceptsWellFormedMultiRowMetaRoundTrip(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t2 USING vec1(vector, tag)")
        exec("INSERT INTO t2(cmd, arg) VALUES('rebuild', ?)", arrayOf(indexedModelHeader(nElem = 4)))
        exec("INSERT INTO t2(rowid, vector, tag) VALUES(1, ?, 11)", arrayOf(vectorBlob(1f, 1f, 1f, 1f)))
        exec("INSERT INTO t2(rowid, vector, tag) VALUES(2, ?, 22)", arrayOf(vectorBlob(2f, 2f, 2f, 2f)))
        exec("SELECT * FROM t2 WHERE rowid = 1")
        exec("SELECT * FROM t2 WHERE rowid = 2")
    }

    @Test
    fun acceptsGenericMetaValueExactlyFillingTheDeclaredBuffer(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t3 USING vec1(vector, tag)")
        exec("INSERT INTO t3(cmd, arg) VALUES('rebuild', ?)", arrayOf(indexedModelHeader(nElem = 4)))
        exec(
            "INSERT INTO t3_idx VALUES(1, 0, 1, 1, ?)",
            arrayOf(idxListBlob(rowid = 1, vector = floatArrayOf(0f, 0f, 0f, 0f)))
        )
        exec(
            "INSERT INTO t3_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            arrayOf(genericTextMetaBlob(textLength = 10))
        )
        exec(
            "INSERT INTO t3(rowid, vector, tag) VALUES(2, ?, 9)",
            arrayOf(vectorBlob(1f, 1f, 1f, 1f))
        )
    }

    @Test
    fun rejectsGenericMetaValueClaimingOneByteMoreThanTheDeclaredBuffer(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t4 USING vec1(vector, tag)")
        exec("INSERT INTO t4(cmd, arg) VALUES('rebuild', ?)", arrayOf(indexedModelHeader(nElem = 4)))
        exec(
            "INSERT INTO t4_idx VALUES(1, 0, 1, 1, ?)",
            arrayOf(idxListBlob(rowid = 1, vector = floatArrayOf(0f, 0f, 0f, 0f)))
        )
        exec(
            "INSERT INTO t4_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            arrayOf(genericTextMetaBlob(textLength = 10, truncateByBytes = 1))
        )
        assertFails {
            exec(
                "INSERT INTO t4(rowid, vector, tag) VALUES(2, ?, 9)",
                arrayOf(vectorBlob(1f, 1f, 1f, 1f))
            )
        }
    }

    @Test
    fun rejectsMetaValueWithUnterminatedContinuationVarInt(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t5 USING vec1(vector, tag)")
        exec("INSERT INTO t5(cmd, arg) VALUES('rebuild', ?)", arrayOf(indexedModelHeader(nElem = 4)))
        exec(
            "INSERT INTO t5_idx VALUES(1, 0, 1, 1, ?)",
            arrayOf(idxListBlob(rowid = 1, vector = floatArrayOf(0f, 0f, 0f, 0f)))
        )
        exec(
            "INSERT INTO t5_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            arrayOf(genericRawEntryMetaBlob(byteArrayOf(-1, -1, -1, -1)))
        )
        assertFails {
            exec(
                "INSERT INTO t5(rowid, vector, tag) VALUES(2, ?, 9)",
                arrayOf(vectorBlob(1f, 1f, 1f, 1f))
            )
        }
    }

    @Test
    fun rejectsMetaValueClaimingLengthFarBeyondTruncatedBuffer(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t6 USING vec1(vector, tag)")
        exec("INSERT INTO t6(cmd, arg) VALUES('rebuild', ?)", arrayOf(indexedModelHeader(nElem = 4)))
        exec(
            "INSERT INTO t6_idx VALUES(1, 0, 1, 1, ?)",
            arrayOf(idxListBlob(rowid = 1, vector = floatArrayOf(0f, 0f, 0f, 0f)))
        )
        exec(
            "INSERT INTO t6_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            arrayOf(genericTextMetaBlob(textLength = 60, truncateByBytes = 55))
        )
        assertFails {
            exec(
                "INSERT INTO t6(rowid, vector, tag) VALUES(2, ?, 9)",
                arrayOf(vectorBlob(1f, 1f, 1f, 1f))
            )
        }
    }

    @Test
    fun rejectsMetaValueWithVarIntBelowMinimumTypeValue(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t7 USING vec1(vector, tag)")
        exec("INSERT INTO t7(cmd, arg) VALUES('rebuild', ?)", arrayOf(indexedModelHeader(nElem = 4)))
        exec(
            "INSERT INTO t7_idx VALUES(1, 0, 1, 1, ?)",
            arrayOf(idxListBlob(rowid = 1, vector = floatArrayOf(0f, 0f, 0f, 0f)))
        )
        exec(
            "INSERT INTO t7_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            arrayOf(genericRawEntryMetaBlob(byteArrayOf(-128, 0)))
        )
        assertFails {
            exec(
                "INSERT INTO t7(rowid, vector, tag) VALUES(2, ?, 9)",
                arrayOf(vectorBlob(1f, 1f, 1f, 1f))
            )
        }
    }

    @Test
    fun rejectsVarIntExceedingMaxEncodedLengthEvenWithBytesToSpare(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t8 USING vec1(vector, tag)")
        exec("INSERT INTO t8(cmd, arg) VALUES('rebuild', ?)", arrayOf(indexedModelHeader(nElem = 4)))
        exec(
            "INSERT INTO t8_idx VALUES(1, 0, 1, 1, ?)",
            arrayOf(idxListBlob(rowid = 1, vector = floatArrayOf(0f, 0f, 0f, 0f)))
        )
        exec(
            "INSERT INTO t8_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            arrayOf(genericRawEntryMetaBlob(ByteArray(12) { -1 }))
        )
        assertFails {
            exec(
                "INSERT INTO t8(rowid, vector, tag) VALUES(2, ?, 9)",
                arrayOf(vectorBlob(1f, 1f, 1f, 1f))
            )
        }
    }

    @Test
    fun acceptsGenericMetaValueWithMultiByteVarIntExactlyFillingBuffer(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t9 USING vec1(vector, tag)")
        exec("INSERT INTO t9(cmd, arg) VALUES('rebuild', ?)", arrayOf(indexedModelHeader(nElem = 4)))
        exec(
            "INSERT INTO t9_idx VALUES(1, 0, 1, 1, ?)",
            arrayOf(idxListBlob(rowid = 1, vector = floatArrayOf(0f, 0f, 0f, 0f)))
        )
        exec(
            "INSERT INTO t9_meta VALUES(${1 shl VEC1_META_COLUMN_BITS}, ?)",
            arrayOf(genericTextMetaBlob(textLength = 100))
        )
        exec(
            "INSERT INTO t9(rowid, vector, tag) VALUES(2, ?, 9)",
            arrayOf(vectorBlob(1f, 1f, 1f, 1f))
        )
    }
}
