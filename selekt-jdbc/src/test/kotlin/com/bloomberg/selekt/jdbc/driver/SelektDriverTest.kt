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

package com.bloomberg.selekt.jdbc.driver

import java.sql.Connection
import java.sql.DriverManager
import java.sql.DriverPropertyInfo
import java.sql.SQLException
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SelektDriverTest {
    private lateinit var driver: SelektDriver
    private val connections = mutableListOf<Connection>()

    @BeforeEach
    fun setUp() {
        driver = SelektDriver()
    }

    @AfterEach
    fun tearDown() {
        connections.run {
            forEach {
                if (!it.isClosed) {
                    it.close()
                }
            }
            clear()
        }
    }

    @Test
    fun driverRegistration() {
        assertTrue(DriverManager.getDrivers().toList().any { it is SelektDriver })
    }

    @Test
    fun acceptsValidURLs() {
        driver.run {
            listOf(
                "jdbc:sqlite:/path/to/test.db",
                "jdbc:sqlite:/path/to/test.db?prop=value",
                "jdbc:sqlite:./relative/path.db",
            ).forEach {
                assertTrue(acceptsURL(it))
            }
        }
    }

    @Test
    fun rejectsInvalidURLs() {
        driver.run {
            listOf(
                "jdbc:selekt:/path/to/test.db",
                "jdbc:mysql://localhost:3306/test",
                "invalid://url",
                null
            ).forEach {
                assertFalse(acceptsURL(it))
            }
        }
    }

    @Test
    fun driverConnects() {
        assertFailsWith<SQLException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", Properties())
        }
    }

    @Test
    fun connectWithInvalidURL() {
        assertEquals(null, driver.connect("jdbc:mysql://localhost:3306/test", Properties()))
    }

    @Test
    fun getPropertyInfo() {
        driver.getPropertyInfo("jdbc:sqlite:/tmp/test.db", Properties()).also {
            assertNotNull(it)
            assertTrue(it.isNotEmpty())
        }.map(DriverPropertyInfo::name).run {
            assertTrue(contains("key"))
            assertTrue(contains("poolSize"))
            assertTrue(contains("busyTimeout"))
            assertTrue(contains("journalMode"))
            assertTrue(contains("foreignKeys"))
        }
    }

    @Test
    fun getPropertyInfoWithInvalidURL() {
        assertFailsWith<SQLException> {
            driver.getPropertyInfo("invalid://url", Properties())
        }
    }

    @Test
    fun driverVersion(): Unit = driver.run {
        assertEquals(4, majorVersion)
        assertEquals(3, minorVersion)
    }

    @Test
    fun jdbcCompliant() {
        assertFalse(driver.jdbcCompliant())
    }

    @Test
    fun getParentLogger(): Unit = driver.parentLogger.run {
        assertNotNull(this)
        assertEquals("com.bloomberg.selekt.jdbc.driver.SelektDriver", name)
    }

    @Test
    fun propertyInfoDetails(): Unit = driver.getPropertyInfo("jdbc:sqlite:/tmp/test.db", Properties()).run {
        find { it.name == "key" }.let {
            assertNotNull(it)
            assertEquals("Encryption key (hex string or file path)", it.description)
            assertFalse(it.required)
        }
        find { it.name == "poolSize" }.let {
            assertNotNull(it)
            assertEquals("Maximum connection pool size", it.description)
            assertFalse(it.required)
            assertEquals("10", it.value)
        }
        find { it.name == "busyTimeout" }.let {
            assertNotNull(it)
            assertEquals("SQLite busy timeout in milliseconds", it.description)
            assertFalse(it.required)
        }
        find { it.name == "journalMode" }.let {
            assertNotNull(it)
            assertEquals("SQLite journal mode", it.description)
            assertFalse(it.required)
            assertEquals("WAL", it.value)
        }
        find { it.name == "foreignKeys" }.let {
            assertNotNull(it)
            assertEquals("Enable foreign key constraints", it.description)
            assertFalse(it.required)
            assertEquals("true", it.value)
        }
    }

    @Test
    fun connectWithProperties() {
        val url = "jdbc:sqlite:/tmp/test.db"
        val properties = Properties().apply {
            setProperty("key", "test-key")
            setProperty("poolSize", "5")
            setProperty("busyTimeout", "2000")
            setProperty("journalMode", "DELETE")
            setProperty("foreignKeys", "false")
        }
        assertFailsWith<SQLException> {
            driver.connect(url, properties)
        }
    }

    @Test
    fun connectWithURLProperties() {
        val url = "jdbc:sqlite:/tmp/test.db?key=test-key&poolSize=5"
        val properties = Properties()
        assertFailsWith<SQLException> {
            driver.connect(url, properties)
        }
    }

    @Test
    fun propertyInfoWithExistingProperties(): Unit = driver.getPropertyInfo(
        "jdbc:sqlite:/tmp/test.db",
        Properties().apply {
            setProperty("poolSize", "20")
            setProperty("key", "test-key")
        }
    ).run {
        find { it.name == "key" }.let {
            assertNotNull(it)
            assertEquals("test-key", it.value)
        }
        find { it.name == "poolSize" }.let {
            assertNotNull(it)
            assertEquals("20", it.value)
        }
        find { it.name == "journalMode" }.let {
            assertNotNull(it)
            assertEquals("WAL", it.value)
        }
    }

    @Test
    fun booleanPropertyChoices(): Unit = driver.getPropertyInfo("jdbc:sqlite:/tmp/test.db", Properties()).run {
        find { it.name == "foreignKeys" }.let {
            assertNotNull(it, "Property foreignKeys should exist")
            assertNotNull(it.choices, "Property foreignKeys should have choices")
            assertEquals(2, it.choices.size, "Property foreignKeys should have 2 choices")
            assertTrue(it.choices.contains("true"), "Property foreignKeys should have 'true' choice")
            assertTrue(it.choices.contains("false"), "Property foreignKeys should have 'false' choice")
        }
    }

    @Test
    fun journalModeChoices() {
        val propertyInfo = driver.getPropertyInfo("jdbc:sqlite:/tmp/test.db", Properties())
        val journalModeProperty = propertyInfo.find { it.name == "journalMode" }
        assertNotNull(journalModeProperty)
        assertNotNull(journalModeProperty.choices)
        val expectedModes = arrayOf("DELETE", "TRUNCATE", "PERSIST", "MEMORY", "WAL", "OFF")
        assertEquals(expectedModes.size, journalModeProperty.choices.size)
        for (mode in expectedModes) {
            assertTrue(journalModeProperty.choices.contains(mode), "Should contain journal mode $mode")
        }
    }

    @Test
    fun urlValidationValid() {
        listOf(
            "jdbc:sqlite:/absolute/path/test.db",
            "jdbc:sqlite:./relative/path/test.db",
            "jdbc:sqlite:../parent/test.db",
            "jdbc:sqlite:/path/with spaces/test.db",
            "jdbc:sqlite:/path/test.db?prop=value",
            "jdbc:sqlite:/path/test.db?prop1=value1&prop2=value2"
        ).forEach {
            assertTrue(driver.acceptsURL(it), "Should accept URL: $it")
        }
    }

    @Test
    fun urlValidationInvalid() {
        listOf(
            "jdbc:selekt:/path/test.db",
            "jdbc:sqlite:",
            "jdbc:sqlite",
            "selekt:/path/test.db",
            "invalid://url",
            "",
            null
        ).forEach {
            assertFalse(driver.acceptsURL(it), "Should reject URL: $it")
        }
    }

    @Test
    fun connectWithHexKey() {
        val properties = Properties().apply {
            setProperty("key", "0x0123456789ABCDEF")
        }
        assertFailsWith<SQLException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        }
    }

    @Test
    fun connectWithHexKeyUppercasePrefix() {
        val properties = Properties().apply {
            setProperty("key", "0X0123456789ABCDEF")
        }
        assertFailsWith<SQLException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        }
    }

    @Test
    fun connectWithKey() {
        val properties = Properties().apply {
            setProperty("key", "some-key")
        }
        assertFailsWith<SQLException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        }
    }

    @Test
    fun connectWithoutKey() {
        val url = "jdbc:sqlite:/tmp/test.db"
        val properties = Properties()
        assertFailsWith<SQLException> {
            driver.connect(url, properties)
        }
    }

    @Test
    fun getPropertyInfoWithBusyTimeout() {
        val properties = Properties().apply {
            setProperty("busyTimeout", "5000")
        }
        driver.getPropertyInfo("jdbc:sqlite:/tmp/test.db", properties).find {
            it.name == "busyTimeout"
        }.let {
            assertNotNull(it)
            assertEquals("5000", it.value)
        }
    }

    @Test
    fun propertyInfoWithAllJournalModes() {
        listOf(
            "DELETE",
            "TRUNCATE",
            "PERSIST",
            "MEMORY",
            "WAL",
            "OFF"
        ).forEach {
            val properties = Properties().apply {
                setProperty("journalMode", it)
            }
            driver.getPropertyInfo("jdbc:sqlite:/tmp/test.db", properties).find { info ->
                info.name == "journalMode"
            }.run {
                assertNotNull(this)
                assertEquals(it, value)
            }
        }
    }

    @Test
    fun connectWithValidPoolSizeAndBusyTimeout() {
        val properties = Properties().apply {
            setProperty("poolSize", "20")
            setProperty("busyTimeout", "5000")
        }
        assertFailsWith<SQLException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        }
    }

    @Test
    fun connectWithNullJournalMode() {
        val properties = Properties()
        assertFailsWith<SQLException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        }
    }
}
