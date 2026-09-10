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

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class StreamingBlobBatchTest {
    @Test
    fun defaultsAreSuitableForIncrementalWrites() {
        val batch = StreamingBlobBatch("files", "data", "INSERT INTO files VALUES (?, ?)", 2)
        assertEquals("main", batch.databaseName)
        assertEquals(64 * 1024, batch.transferBufferSize)
    }

    @Test
    fun rejectsInvalidConfiguration() {
        assertFailsWith<IllegalArgumentException> {
            StreamingBlobBatch("", "data", "INSERT INTO files VALUES (?)", 1)
        }
        assertFailsWith<IllegalArgumentException> {
            StreamingBlobBatch("files", "", "INSERT INTO files VALUES (?)", 1)
        }
        assertFailsWith<IllegalArgumentException> {
            StreamingBlobBatch("files", "data", " ", 1)
        }
        assertFailsWith<IllegalArgumentException> {
            StreamingBlobBatch("files", "data", "INSERT INTO files VALUES (?)", 0)
        }
        assertFailsWith<IllegalArgumentException> {
            StreamingBlobBatch("files", "data", "INSERT INTO files VALUES (?)", 1, "", 1)
        }
        assertFailsWith<IllegalArgumentException> {
            StreamingBlobBatch("files", "data", "INSERT INTO files VALUES (?)", 1, "main", 0)
        }
    }

    @Test
    fun rowRejectsNegativeLength() {
        assertFailsWith<IllegalArgumentException> {
            StreamingBlobRow(emptyArray(), -1, byteArrayOf().inputStream())
        }
    }
}
