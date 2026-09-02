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
private const val VEC1_MODEL_ROTATE = 0x02
private const val VEC1_DISTANCE_L2 = 1
private const val VEC1_PQ_CODEBOOK_SZ = 256
private const val SIZEOF_F32 = 4
private const val WRAPPED_CODEBOOK_COUNT = (1 shl 24) + 1

private fun modelHeader(
    nElem: Int,
    nCodebook: Int = 0,
    nBucket: Int = 0,
    flags: Int = 0
): ByteArray = ByteBuffer.allocate(24).order(ByteOrder.BIG_ENDIAN).apply {
    putInt(0, VEC1_HEADER_VERSION)
    putInt(4, flags)
    putInt(8, nElem)
    putInt(12, nCodebook)
    putInt(16, nBucket)
    putInt(20, VEC1_DISTANCE_L2)
}.array()

internal class Vec1ModelDecodeSecurityTest {
    private val database = SQLiteDatabase.createInMemoryDatabase()

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun rejectsTamperedModelBlobThatWouldWrapCodebookSizeTo32Bits(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t1 USING vec1()")
        val blob = modelHeader(nElem = WRAPPED_CODEBOOK_COUNT, nCodebook = WRAPPED_CODEBOOK_COUNT) +
            ByteArray(VEC1_PQ_CODEBOOK_SZ * SIZEOF_F32)
        exec("REPLACE INTO t1_model VALUES(1, ?)", arrayOf(blob))
        exec("REPLACE INTO t1_config(id, val) VALUES(0, 1)")
        assertFails {
            exec("SELECT * FROM t1 WHERE rowid = 1")
        }
    }

    @Test
    fun rejectsModelHeaderWithExcessiveBucketCount(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t2 USING vec1()")
        val blob = modelHeader(nElem = 4, nBucket = Int.MAX_VALUE)
        assertFails {
            exec("INSERT INTO t2(cmd, arg) VALUES('rebuild', ?)", arrayOf(blob))
        }
    }

    @Test
    fun rejectsModelWithCentroidSectionExceedingMaxSize(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t4 USING vec1()")
        val blob = modelHeader(nElem = 9_000, nBucket = 8_000)
        assertFails {
            exec("INSERT INTO t4(cmd, arg) VALUES('rebuild', ?)", arrayOf(blob))
        }
    }

    @Test
    fun rejectsModelWithRotationSectionExceedingMaxSize(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t5 USING vec1()")
        val blob = modelHeader(nElem = 9_000, flags = VEC1_MODEL_ROTATE)
        assertFails {
            exec("INSERT INTO t5(cmd, arg) VALUES('rebuild', ?)", arrayOf(blob))
        }
    }

    @Test
    fun acceptsWellFormedMinimalModelHeader(): Unit = database.run {
        exec("CREATE VIRTUAL TABLE t3 USING vec1()")
        val blob = modelHeader(nElem = 4)
        exec("INSERT INTO t3(cmd, arg) VALUES('rebuild', ?)", arrayOf(blob))
    }
}
