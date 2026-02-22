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

package com.bloomberg.selekt.jdbc.util

import org.junit.jupiter.api.Test
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ConnectionURLTest {
    @Test
    fun basicURL(): Unit = ConnectionURL.parse("jdbc:sqlite:/path/to/test.db").run {
        assertEquals("/path/to/test.db", databasePath)
        assertTrue(properties.isEmpty)
    }

    @Test
    fun urlWithProperties(): Unit = ConnectionURL.parse(
        "jdbc:sqlite:/path/to/test.db?encrypt=true&key=abc123&poolSize=5"
    ).run {
        assertEquals("/path/to/test.db", databasePath)
        assertEquals("true", getProperty("encrypt"))
        assertEquals("abc123", getProperty("key"))
        assertEquals("5", getProperty("poolSize"))
    }

    @Test
    fun booleanProperties(): Unit = ConnectionURL.parse("jdbc:sqlite:/test.db?encrypt=true&foreignKeys=false").run {
        assertTrue(getBooleanProperty("encrypt"))
        assertFalse(getBooleanProperty("foreignKeys"))
        assertFalse(getBooleanProperty("nonexistent"))
    }

    @Test
    fun intProperties(): Unit = ConnectionURL.parse("jdbc:sqlite:/test.db?poolSize=10&busyTimeout=5000").run {
        assertEquals(10, getIntProperty("poolSize"))
        assertEquals(5_000, getIntProperty("busyTimeout"))
        assertEquals(0, getIntProperty("nonexistent"))
        assertEquals(42, getIntProperty("nonexistent", 42))
    }

    @Test
    fun invalidURL() {
        assertFailsWith<SQLException> {
            ConnectionURL.parse("invalid:url")
        }
        assertFailsWith<SQLException> {
            ConnectionURL.parse("jdbc:other:/test.db")
        }
    }

    @Test
    fun emptyPath() {
        assertFailsWith<SQLException> {
            ConnectionURL.parse("jdbc:sqlite:")
        }
    }

    @Test
    fun urlValidation() {
        listOf(
            "jdbc:sqlite:/test.db",
            "jdbc:sqlite:/path/to/test.db?prop=value"
        ).forEach {
            assertTrue(ConnectionURL.isValidUrl(it))
        }
        listOf(
            "jdbc:other:/test.db",
            "invalid",
            null
        ).forEach {
            assertFalse(ConnectionURL.isValidUrl(it))
        }
    }

    @Test
    fun connectionToString(): Unit = ConnectionURL.parse(
        "jdbc:sqlite:/test.db?encrypt=true&poolSize=10"
    ).toString().run {
        assertTrue(startsWith("jdbc:sqlite:/test.db"))
        assertTrue(contains("encrypt=true"))
        assertTrue(contains("poolSize=10"))
    }

    @Test
    fun urlEncoding() {
        assertEquals("hello world", ConnectionURL.parse("jdbc:sqlite:/test.db?key=hello%20world").getProperty("key"))
    }
}
