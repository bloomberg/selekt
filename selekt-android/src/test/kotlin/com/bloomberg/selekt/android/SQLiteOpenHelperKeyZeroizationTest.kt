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
import com.bloomberg.selekt.DatabaseKey
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
import kotlin.test.assertNull
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
    fun `configuration has no key property and helper does not retain caller key`() {
        val key = ByteArray(32) { 0x42 }
        val configuration = ISQLiteOpenHelper.Configuration(
            callback = mock(),
            name = file.name
        )
        val helper = SQLiteOpenHelper(
            context = targetContext,
            configuration = configuration,
            openParams = SQLiteOpenParams(journalMode = SQLiteJournalMode.WAL),
            version = 1,
            key = key
        )
        try {
            assertFalse(ISQLiteOpenHelper.Configuration::class.java.declaredFields.any { it.type == ByteArray::class.java })
            val fields = SQLiteOpenHelper::class.java.declaredFields.onEach { it.isAccessible = true }
            assertFalse(fields.any { it.type == ByteArray::class.java })
            assertFalse(fields.any { it.get(helper) === key })
        } finally {
            helper.close()
        }
    }

    @Test
    fun `caller can clear key immediately after helper construction`() {
        val key = ByteArray(32) { 0x42 }
        val helper = newHelper(key)
        key.fill(0)
        helper.use {
            assertTrue(it.writableDatabase.isOpen)
            assertFalse(key.any { byte -> byte != 0.toByte() })
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
    fun `helper hands its native key ownership to the opened database`() {
        val key = ByteArray(32) { 0x42 }
        val helper = newHelper(key)
        val internalKey = requireNotNull(internalKeyOf(helper)) {
            "A keyed helper should own a DatabaseKey before opening"
        }
        try {
            assertTrue(internalKey.isOpen())
            helper.writableDatabase
            assertNull(internalKeyOf(helper), "The helper must release its key reference after opening")
            assertTrue(internalKey.isOpen(), "The opened database must retain the native key")
        } finally {
            helper.close()
        }
        assertFalse(internalKey.isOpen(), "Closing the database must destroy its native key")
    }

    @Test
    fun `close destroys native key even if writableDatabase was never accessed`() {
        val key = ByteArray(32) { 0x42 }
        val helper = newHelper(key)
        val internalKey = requireNotNull(internalKeyOf(helper))
        helper.close()
        assertNull(internalKeyOf(helper))
        assertFalse(internalKey.isOpen(), "close() must destroy an unopened helper's native key")
    }

    @Test
    fun `close is idempotent and repeated calls keep native key destroyed`() {
        val key = ByteArray(32) { 0x42 }
        val helper = newHelper(key)
        val internalKey = requireNotNull(internalKeyOf(helper))
        helper.writableDatabase
        helper.close()
        helper.close()
        assertNull(internalKeyOf(helper))
        assertFalse(internalKey.isOpen())
    }

    private fun newHelper(key: ByteArray) = SQLiteOpenHelper(
        context = targetContext,
        configuration = ISQLiteOpenHelper.Configuration(
            callback = mock(),
            name = file.name
        ),
        openParams = SQLiteOpenParams(journalMode = SQLiteJournalMode.WAL),
        version = 1,
        key = key
    )

    private fun internalKeyOf(helper: SQLiteOpenHelper): DatabaseKey? {
        val field = SQLiteOpenHelper::class.java.getDeclaredField("databaseKey")
        field.isAccessible = true
        return field.get(helper) as DatabaseKey?
    }
}
