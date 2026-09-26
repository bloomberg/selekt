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

internal class DatabaseConfigurationTest {
    private fun configuration(cursorWindowSize: Int) = DatabaseConfiguration(
        cursorWindowSize = cursorWindowSize,
        evictionDelayMillis = 5_000L,
        maxConnectionPoolSize = 1,
        maxSqlCacheSize = 5,
        timeBetweenEvictionRunsMillis = 5_000L
    )

    @Test
    fun cursorWindowUsesPlatformDefaultsByDefault() {
        val configuration = DatabaseConfiguration(
            evictionDelayMillis = 5_000L,
            maxConnectionPoolSize = 1,
            maxSqlCacheSize = 5,
            timeBetweenEvictionRunsMillis = 5_000L
        )
        assertEquals(DatabaseConfiguration.PLATFORM_DEFAULT_CURSOR_WINDOW_SIZE, configuration.cursorWindowSize)
        assertEquals(DatabaseConfiguration.PLATFORM_DEFAULT_CURSOR_WINDOW_BYTE_SIZE, configuration.cursorWindowByteSize)
        assertEquals(SQLCipherCompatibility.V4, configuration.sqlCipherCompatibility)
    }

    @Test
    fun cursorWindowSizeIsRetained() {
        assertEquals(64, configuration(64).cursorWindowSize)
    }

    @Test
    fun cursorWindowLimitsCanBeCopiedTogether() {
        val configuration = configuration(64).withCursorWindowLimits(128, 4 * 1024 * 1024)
        assertEquals(128, configuration.cursorWindowSize)
        assertEquals(4 * 1024 * 1024, configuration.cursorWindowByteSize)
    }

    @Test
    fun cursorWindowSizeRejectsZero() {
        assertFailsWith<IllegalArgumentException> { configuration(0) }
    }

    @Test
    fun cursorWindowSizeRejectsNegative() {
        assertFailsWith<IllegalArgumentException> { configuration(-2) }
    }

    @Test
    fun cursorWindowByteSizeRejectsUndersizedValues() {
        assertFailsWith<IllegalArgumentException> {
            configuration(1).copy(cursorWindowByteSize = DatabaseConfiguration.MINIMUM_CURSOR_WINDOW_BYTE_SIZE - 1)
        }
    }

    @Test
    fun legacyPositionalConstructorRemainsAvailable() {
        val configuration = DatabaseConfiguration(
            -1L,
            0,
            5_000L,
            1,
            5,
            "main",
            SQLiteSecureDelete.FAST,
            5_000L,
            null,
            false
        )
        assertEquals(DatabaseConfiguration.PLATFORM_DEFAULT_CURSOR_WINDOW_SIZE, configuration.cursorWindowSize)
        assertEquals(DatabaseConfiguration.PLATFORM_DEFAULT_CURSOR_WINDOW_BYTE_SIZE, configuration.cursorWindowByteSize)
    }
}
