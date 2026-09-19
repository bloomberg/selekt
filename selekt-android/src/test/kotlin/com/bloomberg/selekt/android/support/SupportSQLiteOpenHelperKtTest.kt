/*
 * Copyright 2020 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android.support

import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.bloomberg.selekt.DatabaseConfiguration
import com.bloomberg.selekt.SQLiteJournalMode
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SupportSQLiteOpenHelperKtTest {
    @Test
    fun asSelektConfigurationNullNameThrows() {
        SupportSQLiteOpenHelper.Configuration.builder(mock())
            .callback(mock())
            .build()
            .let {
                assertFailsWith<IllegalArgumentException>(block = it::asSelektConfiguration)
            }
    }

    @Test
    fun factoryOwnsAndClearsKeySnapshot() {
        val callerKey = ByteArray(32) { 0x42 }
        val expectedKey = callerKey.copyOf()
        val factory = assertIs<SupportSQLiteOpenHelperFactory>(
            createSupportSQLiteOpenHelperFactory(SQLiteJournalMode.WAL, callerKey)
        )
        val factoryKey = factory.keySnapshot()
        assertNotSame(callerKey, factoryKey)
        assertContentEquals(expectedKey, factoryKey)
        callerKey.fill(0)
        assertContentEquals(expectedKey, factoryKey)
        factory.close()
        factory.close()
        assertTrue(factoryKey.all { it == 0.toByte() })
        assertFailsWith<IllegalStateException> {
            factory.create(mock())
        }
    }

    @Test
    fun factoryAcceptsExplicitDatabaseConfiguration() {
        val configuration = SQLiteJournalMode.WAL.databaseConfiguration.copy(
            cursorWindowSize = 64,
            cursorWindowByteSize = 4 * 1024 * 1024
        )
        val factory = assertIs<SupportSQLiteOpenHelperFactory>(
            createSupportSQLiteOpenHelperFactory(SQLiteJournalMode.WAL, configuration, null)
        )
        factory.use {
            assertSame(configuration, factory.databaseConfiguration())
        }
    }

    private fun SupportSQLiteOpenHelperFactory.keySnapshot(): ByteArray {
        val field = SupportSQLiteOpenHelperFactory::class.java.getDeclaredField("key")
        field.isAccessible = true
        return field.get(this) as ByteArray
    }

    private fun SupportSQLiteOpenHelperFactory.databaseConfiguration(): DatabaseConfiguration {
        val field = SupportSQLiteOpenHelperFactory::class.java.getDeclaredField("databaseConfiguration")
        field.isAccessible = true
        return field.get(this) as DatabaseConfiguration
    }
}
