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

import android.content.Context
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.commons.deleteDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.io.path.createTempFile
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SQLiteOpenHelperKeyZeroizationTest {
    private val file = createTempFile("test-open-helper-key", ".db").toFile().apply { deleteOnExit() }

    private val targetContext = mock<Context>().apply {
        whenever(getDatabasePath(any())) doReturn file
    }

    @AfterEach
    fun tearDown() {
        deleteDatabase(file)
    }

    @Test
    fun `caller supplied key is not mutated by the SQLiteOpenHelper constructor`() {
        val key = ByteArray(32) { 0x42 }
        val helper = newHelper(key)
        try {
            assertTrue(
                key.all { it == 0x42.toByte() },
                "Constructor must not mutate the caller's key ByteArray"
            )
        } finally {
            helper.close()
        }
    }

    @Test
    fun `caller supplied key remains intact after writableDatabase is opened`() {
        val key = ByteArray(32) { 0x42 }
        newHelper(key).use {
            it.writableDatabase
            assertTrue(
                key.all { b -> b == 0x42.toByte() },
                "Opening the database must not mutate the caller's key ByteArray"
            )
        }
    }

    @Test
    fun `internal key copy is zeroed after writableDatabase is opened`() {
        val key = ByteArray(32) { 0x42 }
        val helper = newHelper(key)
        try {
            helper.writableDatabase
            val internalKey = internalKeyOf(helper)
            requireNotNull(internalKey) { "Internal _key should have been allocated for a keyed helper" }
            assertTrue(
                internalKey.all { it == 0.toByte() },
                "Internal _key copy must be zeroed after the database is opened"
            )
        } finally {
            helper.close()
        }
    }

    @Test
    fun `internal key copy is zeroed by close even if writableDatabase was never accessed`() {
        val key = ByteArray(32) { 0x42 }
        val helper = newHelper(key)
        helper.close()
        val internalKey = internalKeyOf(helper)
        requireNotNull(internalKey) { "Internal _key should have been allocated for a keyed helper" }
        assertTrue(
            internalKey.all { it == 0.toByte() },
            "close() must zero the internal _key copy even when writableDatabase was never accessed"
        )
    }

    @Test
    fun `close is idempotent and repeated calls keep internal key zeroed`() {
        val key = ByteArray(32) { 0x42 }
        val helper = newHelper(key)
        helper.writableDatabase
        helper.close()
        helper.close()
        val internalKey = internalKeyOf(helper)
        assertFalse(internalKey?.any { it != 0.toByte() } == true)
    }

    private fun newHelper(key: ByteArray) = SQLiteOpenHelper(
        context = targetContext,
        configuration = ISQLiteOpenHelper.Configuration(
            callback = mock(),
            key = key,
            name = file.name
        ),
        openParams = SQLiteOpenParams(journalMode = SQLiteJournalMode.WAL),
        version = 1
    )

    private fun internalKeyOf(helper: SQLiteOpenHelper): ByteArray? {
        val field = SQLiteOpenHelper::class.java.getDeclaredField("_key")
        field.isAccessible = true
        return field.get(helper) as ByteArray?
    }
}
