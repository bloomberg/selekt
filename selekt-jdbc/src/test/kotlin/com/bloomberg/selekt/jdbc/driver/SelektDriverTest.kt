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

import com.bloomberg.selekt.SelektVersion
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.DriverPropertyInfo
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.util.Properties
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class SelektDriverTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var driver: SelektDriver
    private val connections = mutableListOf<Connection>()
    private val tempFiles = mutableListOf<File>()

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
        tempFiles.run {
            forEach(File::delete)
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
        val connection = driver.connect("jdbc:sqlite:/tmp/test.db", Properties())
        assertNotNull(connection)
        connections.add(connection)
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
            assertFalse(contains("key"))
            assertTrue(contains("poolSize"))
            assertTrue(contains("busyTimeout"))
            assertTrue(contains("cursorWindowSize"))
            assertTrue(contains("cursorWindowByteSize"))
            assertTrue(contains("journalMode"))
            assertTrue(contains("foreignKeys"))
        }
    }

    @Test
    fun getPropertyInfoWithInvalidURL() {
        val secret = "property-info-secret"
        val exception = assertFailsWith<SQLException> {
            driver.getPropertyInfo("invalid://url?password=$secret", Properties())
        }
        assertEquals("Invalid JDBC URL format", exception.message)
        assertFalse(exception.message.orEmpty().contains(secret))
    }

    @Test
    fun driverVersion(): Unit = driver.run {
        assertEquals(SelektVersion.majorVersion, majorVersion)
        assertEquals(SelektVersion.minorVersion, minorVersion)
        assertEquals(SelektVersion.patchVersion, SelektDriver.PATCH_VERSION)
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
        find { it.name == "poolSize" }.let {
            assertNotNull(it)
            assertEquals("Maximum connection pool size", it.description)
            assertFalse(it.required)
            assertEquals("4", it.value)
        }
        find { it.name == "busyTimeout" }.let {
            assertNotNull(it)
            assertEquals("SQLite busy timeout in milliseconds", it.description)
            assertFalse(it.required)
            assertEquals("2500", it.value)
        }
        find { it.name == "cursorWindowSize" }.let {
            assertNotNull(it)
            assertEquals("Maximum rows retained in a scrollable cursor window", it.description)
            assertFalse(it.required)
            assertEquals("1024", it.value)
        }
        find { it.name == "cursorWindowByteSize" }.let {
            assertNotNull(it)
            assertEquals("Maximum estimated bytes retained in a scrollable cursor window", it.description)
            assertFalse(it.required)
            assertEquals("2097152", it.value)
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
            setProperty("poolSize", "5")
            setProperty("busyTimeout", "2000")
            setProperty("cursorWindowSize", "64")
            setProperty("cursorWindowByteSize", "4194304")
            setProperty("journalMode", "DELETE")
            setProperty("foreignKeys", "false")
        }
        val connection = driver.connect(url, properties)
        assertNotNull(connection)
        connections.add(connection)
    }

    @Test
    fun invalidForeignKeysUrlIsRejectedBeforeDatabaseOpen() {
        listOf("tru", "yes", "1", "").forEachIndexed { index, value ->
            val databaseFile = File(tempDir, "invalid-url-$index.db")
            assertFailsWith<SQLException> {
                driver.connect("jdbc:sqlite:${databaseFile.absolutePath}?foreignKeys=$value", Properties())
            }
            assertFalse(databaseFile.exists())
        }
    }

    @Test
    fun invalidForeignKeysPropertyOverrideIsRejectedBeforeDatabaseOpen() {
        val databaseFile = File(tempDir, "invalid-property.db")
        val properties = Properties().apply { setProperty("foreignKeys", "tru") }
        assertFailsWith<SQLException> {
            driver.connect("jdbc:sqlite:${databaseFile.absolutePath}?foreignKeys=true", properties)
        }
        assertFalse(databaseFile.exists())
    }

    @Test
    fun foreignKeysConfigurationControlsReferentialIntegrity() {
        listOf(null, "true", "TRUE", "TrUe").forEach { value ->
            val property = value?.let { "?foreignKeys=$it" }.orEmpty()
            driver.connect("jdbc:sqlite::memory:$property", Properties())!!.use { connection ->
                createForeignKeyTables(connection)
                connection.createStatement().use { statement ->
                    assertFailsWith<SQLException> {
                        statement.executeUpdate("INSERT INTO child(parent_id) VALUES (404)")
                    }
                }
            }
        }
        listOf("false", "FALSE", "FaLsE").forEach { value ->
            driver.connect("jdbc:sqlite::memory:?foreignKeys=$value", Properties())!!.use { connection ->
                createForeignKeyTables(connection)
                connection.createStatement().use { statement ->
                    assertEquals(1, statement.executeUpdate("INSERT INTO child(parent_id) VALUES (404)"))
                }
            }
        }
    }

    private fun createForeignKeyTables(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.execute("CREATE TABLE parent(id INTEGER PRIMARY KEY)")
            statement.execute("CREATE TABLE child(parent_id INTEGER REFERENCES parent(id))")
        }
    }

    @Test
    fun rejectsInvalidCursorWindowSize() {
        val properties = Properties().apply { setProperty("cursorWindowSize", "0") }
        assertFailsWith<SQLException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        }
    }

    @Test
    fun rejectsInvalidCursorWindowByteSize() {
        val properties = Properties().apply { setProperty("cursorWindowByteSize", "7") }
        assertFailsWith<SQLException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        }
    }

    @Test
    fun connectWithURLProperties() {
        val url = "jdbc:sqlite:/tmp/test.db?poolSize=5"
        val connection = driver.connect(url, Properties())
        assertNotNull(connection)
        connections.add(connection)
    }

    @Test
    fun privateMemoryDatabaseForcesSingleConnectionPool() {
        driver.connect("jdbc:sqlite::memory:?poolSize=10", Properties())!!.use(::verifyPrivateMemoryRoundTrip)
    }

    @Test
    fun privateMemoryDatabaseEndsWithFinalConnection() {
        val url = "jdbc:sqlite::memory:?poolSize=9"
        driver.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use {
                it.executeUpdate("CREATE TABLE transient(value INTEGER)")
            }
        }

        driver.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use {
                it.executeQuery(
                    "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='transient'"
                ).use { resultSet ->
                    assertTrue(resultSet.next())
                    assertEquals(0, resultSet.getInt(1))
                }
            }
        }
    }

    private fun verifyPrivateMemoryRoundTrip(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.executeUpdate("CREATE TABLE test(value INTEGER)")
            statement.executeUpdate("INSERT INTO test VALUES (42)")
            statement.executeQuery("SELECT value FROM test").use {
                assertTrue(it.next())
                assertEquals(42, it.getInt(1))
            }
        }
    }

    @Test
    fun propertyInfoWithExistingProperties(): Unit = driver.getPropertyInfo(
        "jdbc:sqlite:/tmp/test.db",
        Properties().apply {
            setProperty("poolSize", "20")
        }
    ).run {
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
    fun connectRejectsEncryptionKeyInProperties() {
        listOf("key", "Key", "KEY").forEach { name ->
            val properties = Properties().apply { setProperty(name, "secret") }
            val exception = assertFailsWith<SQLFeatureNotSupportedException> {
                driver.connect("jdbc:sqlite:/tmp/test.db", properties)
            }
            assertEquals("0A000", exception.sqlState)
            assertTrue(exception.message.orEmpty().contains("SelektDataSource.setEncryption"))
        }
    }

    @Test
    fun connectRejectsEncryptionKeyInUrl() {
        listOf("key", "Key", "KEY", " key ", "%6Bey", "%4B%45%59", "%20Key%20", "+key+").forEach { name ->
            val exception = assertFailsWith<SQLFeatureNotSupportedException> {
                driver.connect("jdbc:sqlite:/tmp/test.db?$name=secret", Properties())
            }
            assertEquals("0A000", exception.sqlState)
        }
    }

    @Test
    fun connectRejectsNonStringEncryptionKeyInProperties() {
        val properties = Properties().apply {
            this["key"] = "secret".toCharArray()
        }
        assertFailsWith<SQLFeatureNotSupportedException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        }
    }

    @Test
    fun connectRejectsEncryptionKeyInDefaultProperties() {
        val defaults = Properties().apply { setProperty("key", "secret") }
        assertFailsWith<SQLFeatureNotSupportedException> {
            driver.connect("jdbc:sqlite:/tmp/test.db", Properties(defaults))
        }
    }

    @Test
    fun getPropertyInfoRejectsEncryptionKey() {
        val properties = Properties().apply { setProperty("key", "secret") }
        assertFailsWith<SQLFeatureNotSupportedException> {
            driver.getPropertyInfo("jdbc:sqlite:/tmp/test.db", properties)
        }
    }

    @Test
    fun getPropertyInfoRejectsEncodedEncryptionKey() {
        val secret = "property-info-encoded-secret"
        val exception = assertFailsWith<SQLFeatureNotSupportedException> {
            driver.getPropertyInfo("jdbc:sqlite:/tmp/test.db?%6Bey=$secret", Properties())
        }
        assertFalse(exception.message.orEmpty().contains(secret))
    }

    @Test
    fun connectWithoutKey() {
        val url = "jdbc:sqlite:/tmp/test.db"
        val properties = Properties()
        val connection = driver.connect(url, properties)
        assertNotNull(connection)
        connections.add(connection)
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
        val connection = driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        assertNotNull(connection)
        connections.add(connection)
    }

    @Test
    fun connectWithNullJournalMode() {
        val properties = Properties()
        val connection = driver.connect("jdbc:sqlite:/tmp/test.db", properties)
        assertNotNull(connection)
        connections.add(connection)
    }

    @Test
    fun sharedDatabaseCacheReturnsSharedInstance() {
        val url = "jdbc:sqlite:/tmp/test_shared.db"
        val properties = Properties()
        assertNotNull(driver.connect(url, properties)).also(connections::add)
        assertNotNull(driver.connect(url, properties)).also(connections::add)
    }

    @Test
    fun closingAllConnectionsRetainsDatabaseForIdleReuse() {
        val url = "jdbc:sqlite:/tmp/test_lifecycle.db"
        val properties = Properties()
        val connectionOne = driver.connect(url, properties)!!.also(connections::add)
        val connectionTwo = driver.connect(url, properties)!!.also(connections::add)
        val sharedDatabase = connectionOne.sharedDatabase()
        assertSame(sharedDatabase, connectionTwo.sharedDatabase())
        connectionOne.close()
        assertTrue(sharedDatabase.isOpen())
        connectionTwo.close()
        assertTrue(sharedDatabase.isOpen())
        val replacement = assertNotNull(driver.connect(url, properties)).also(connections::add)
        val replacementDatabase = replacement.sharedDatabase()
        assertSame(sharedDatabase, replacementDatabase)
        replacement.close()
        assertTrue(replacementDatabase.isOpen())
    }

    private fun Connection.sharedDatabase(): SharedDatabase = javaClass
        .getDeclaredField("sharedDatabase")
        .apply { isAccessible = true }
        .get(this) as SharedDatabase

    @Test
    fun concurrentConnectDoesNotRaceFinalRelease() {
        val databaseFile = File.createTempFile("selekt-driver-race-", ".db").also(tempFiles::add)
        val url = "jdbc:sqlite:${databaseFile.absolutePath}"
        val threadCount = 16
        val iterations = 50
        val barrier = CyclicBarrier(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        try {
            val tasks = List(threadCount) {
                executor.submit {
                    barrier.await()
                    repeat(iterations) {
                        connectAndQuery(url)
                    }
                }
            }
            tasks.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }
    }

    private fun connectAndQuery(url: String) {
        driver.connect(url, Properties())!!.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT 1").use { resultSet ->
                    assertTrue(resultSet.next())
                }
            }
        }
    }

    @Test
    fun closingConnectionDoesNotAffectOtherConnections() {
        val url = "jdbc:sqlite:/tmp/test_independent.db"
        val properties = Properties()
        val connectionOne = driver.connect(url, properties)!!.also(connections::add)
        val connectionTwo = driver.connect(url, properties)!!.also(connections::add)
        connectionOne.close()
        assertTrue(connectionOne.isClosed)
        assertFalse(connectionTwo.isClosed)
    }

    @Test
    fun rejectedEncryptionKeyIsNotIncludedInErrorMessage() {
        val secret = "SUPERSECRETKEY123"
        val properties = Properties().apply {
            setProperty("key", secret)
        }
        val exception = assertFailsWith<SQLFeatureNotSupportedException> {
            driver.connect("jdbc:sqlite:/tmp/test_redact_error.db?key=$secret", properties)
        }
        assertFalse(exception.message.orEmpty().contains(secret), "Error message should not contain the encryption key")
    }

    @Test
    fun connectionFailureDoesNotIncludeSecretUrlProperties() {
        val secret = "CONNECTION_FAILURE_SECRET"
        val exception = assertFailsWith<SQLException> {
            driver.connect(
                "jdbc:sqlite:/tmp/test_redact_failure.db?password=$secret&journalMode=invalid",
                Properties()
            )
        }
        assertFalse(exception.message.orEmpty().contains(secret))
    }

    @Test
    fun databaseMetadataRedactsSecretUrlProperties() {
        val secret = "METADATA_SECRET"
        driver.connect("jdbc:sqlite::memory:?password=$secret", Properties())!!.use { connection ->
            val metadataUrl = connection.metaData.url
            assertFalse(metadataUrl.contains(secret))
            assertTrue(metadataUrl.contains("password=***"))
        }
    }

    @Test
    fun getIndexInfoReturnsCreatedIndex() {
        val dbFile = File.createTempFile("selekt_idx_test_", ".db").also(tempFiles::add)
        driver.connect("jdbc:sqlite:${dbFile.absolutePath}", Properties())!!.use { connection ->
            connection.createStatement().use {
                it.execute("CREATE TABLE idx_test (id INTEGER PRIMARY KEY, name TEXT)")
                it.execute("CREATE INDEX idx_test_name ON idx_test (name)")
            }
            connection.metaData.getIndexInfo(null, null, "idx_test", false, false).use { rs ->
                val indexNames = mutableListOf<String?>()
                while (rs.next()) {
                    indexNames.add(rs.getString("INDEX_NAME"))
                }
                assertTrue(indexNames.any { it == "idx_test_name" })
            }
        }
    }

    @Test
    fun setReadOnlyPreventsWrites() {
        val dbFile = File.createTempFile("selekt_readonly_test_", ".db").also(tempFiles::add)
        driver.connect("jdbc:sqlite:${dbFile.absolutePath}", Properties())!!.use { connection ->
            connection.createStatement().use {
                it.execute("CREATE TABLE readonly_test (id INTEGER PRIMARY KEY, value TEXT)")
                it.execute("INSERT INTO readonly_test VALUES (1, 'initial')")
            }
            connection.isReadOnly = true
            connection.createStatement().use {
                assertFailsWith<SQLException> {
                    it.execute("INSERT INTO readonly_test VALUES (2, 'should_fail')")
                }
            }
            connection.isReadOnly = false
            connection.createStatement().use {
                it.execute("INSERT INTO readonly_test VALUES (3, 'after_restore')")
            }
        }
    }
}
