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

package com.bloomberg.selekt.jvm

import com.bloomberg.selekt.SimpleSQLQuery
import com.bloomberg.selekt.StreamingBlobBatch
import com.bloomberg.selekt.StreamingBlobRow
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SQLiteDatabaseStreamingBlobTest {
    @Test
    fun streamsRowsThroughJniBackend() = createInMemoryDatabase().use { database ->
        database.exec("CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)")
        val payloads = listOf(byteArrayOf(), byteArrayOf(1, 2, 3), ByteArray(1_025) { it.toByte() })
        val batch = StreamingBlobBatch(
            "files",
            "data",
            "INSERT INTO files (id, data) VALUES (?, ?)",
            2,
            transferBufferSize = 17
        )
        val rows = payloads.mapIndexed { index, payload ->
            val rowId = index + 1L
            StreamingBlobRow(arrayOf(rowId, null), payload.size, payload.inputStream(), rowId)
        }
        assertEquals(payloads.size, database.insertBlobs(batch, rows))
        database.query(SimpleSQLQuery("SELECT data FROM files ORDER BY id")).use { cursor ->
            payloads.forEach { expected ->
                assertTrue(cursor.moveToNext())
                assertContentEquals(expected, cursor.getBlob(0))
            }
        }
    }
}
