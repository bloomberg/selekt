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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.parallel.Isolated

@Isolated
internal class NativeOutputHolderTest {
    private companion object {
        const val SQL_OPEN_READWRITE_OR_CREATE = 6
    }

    private val sqlite = externalSQLiteSingleton()

    @Test
    fun `zero-length output holders are rejected without retaining native resources`() {
        assertRejectedWithoutNativeMemoryChange("openV2") {
            sqlite.openV2(":memory:", SQL_OPEN_READWRITE_OR_CREATE, LongArray(0))
        }

        val dbHolder = LongArray(1)
        assertEquals(SQL_OK, sqlite.openV2(":memory:", SQL_OPEN_READWRITE_OR_CREATE, dbHolder))
        val db = dbHolder.single()
        var session = 0L
        try {
            assertEquals(SQL_OK, sqlite.exec(db, "CREATE TABLE t(id INTEGER PRIMARY KEY, data BLOB)"))

            assertRejectedWithoutNativeMemoryChange("prepareV2") {
                sqlite.prepareV2(db, "SELECT 1", 8, LongArray(0))
            }
            assertRejectedWithoutNativeMemoryChange("prepareV2") {
                sqlite.prepareV2(DatabaseHandle(db), "SELECT 1", 8, LongArray(0))
            }
            assertRejectedWithoutNativeMemoryChange("sessionCreate") {
                sqlite.sessionCreate(db, "main", LongArray(0))
            }

            val sessionHolder = LongArray(1)
            assertEquals(SQL_OK, sqlite.sessionCreate(db, "main", sessionHolder))
            session = sessionHolder.single()
            assertEquals(SQL_OK, sqlite.sessionAttach(session, null))
            assertEquals(SQL_OK, sqlite.exec(db, "INSERT INTO t VALUES(1, zeroblob(4))"))

            assertRejectedWithoutNativeMemoryChange("blobOpen") {
                sqlite.blobOpen(db, "main", "t", "data", 1, 1, LongArray(0))
            }
            assertRejectedWithoutNativeMemoryChange("blobOpen") {
                sqlite.blobOpen(DatabaseHandle(db), "main", "t", "data", 1, 1, LongArray(0))
            }
            assertRejectedWithoutNativeMemoryChange("sessionChangeset") {
                sqlite.sessionChangeset(session, arrayOfNulls(0))
            }
        } finally {
            if (session != 0L) {
                sqlite.sessionDelete(session)
            }
            assertEquals(SQL_OK, sqlite.closeV2(db))
        }
    }

    private fun assertRejectedWithoutNativeMemoryChange(operation: String, action: () -> Unit) {
        val memoryBefore = sqlite.memoryUsed()
        repeat(16) {
            val failure = assertFailsWith<IndexOutOfBoundsException> { action() }
            assertEquals("$operation: holder must contain at least one element.", failure.message)
        }
        assertEquals(memoryBefore, sqlite.memoryUsed())
    }
}
