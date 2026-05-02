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

import com.bloomberg.selekt.DatabaseConfiguration
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.commons.forEachCatching
import com.bloomberg.selekt.externalSQLiteSingleton
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import com.bloomberg.selekt.commons.zero
import java.nio.CharBuffer
import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager
import java.sql.DriverPropertyInfo
import java.sql.SQLException
import java.util.Properties
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Logger as JulLogger
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Supports the URL format: jdbc:sqlite:path/to/database.sqlite[?properties]
 *
 * Supported connection properties:
 * - key: Encryption key (hex string)
 * - poolSize: Maximum connection pool size (integer, default: 10)
 * - busyTimeout: SQLite busy timeout in milliseconds (integer, default: 2500)
 * - journalMode: SQLite journal mode (DELETE, WAL, MEMORY, etc., default: WAL)
 * - foreignKeys: Enable foreign key constraints (true/false, default: true)
 * - maxCachedDatabases: Maximum number of databases held in the driver cache (-1 = unlimited, 0 = no caching, >0 = bounded LRU, default: -1)
 */
@Suppress("TooGenericExceptionCaught")
class SelektDriver : Driver {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SelektDriver::class.java)

        const val DRIVER_NAME = "Selekt JDBC Driver"
        const val DRIVER_VERSION = "4.3.0"
        const val MAJOR_VERSION = 4
        const val MINOR_VERSION = 3

        private const val PROPERTY_KEY = "key"
        private const val PROPERTY_POOL_SIZE = "poolSize"
        private const val PROPERTY_BUSY_TIMEOUT = "busyTimeout"
        private const val PROPERTY_JOURNAL_MODE = "journalMode"
        private const val PROPERTY_FOREIGN_KEYS = "foreignKeys"

        private const val PROPERTY_MAX_CACHED_DATABASES = "maxCachedDatabases"

        private const val DEFAULT_POOL_SIZE = 10
        private const val DEFAULT_MAX_CACHED_DATABASES = -1

        private const val HEX_PREFIX_LENGTH = 2
        private const val HEX_CHUNK_SIZE = 2
        private const val HEX_RADIX = 16
        private const val BITS_PER_HEX_DIGIT = 4
        private const val REQUIRED_KEY_LENGTH_BYTES = 32

        private val BOOLEAN_CHOICES = arrayOf("true", "false")

        private val databaseCacheLock = ReentrantLock()
        private val databaseCache = LinkedHashMap<String, SharedDatabase>(16, 0.75f, true)
        private var maxCachedDatabases: Int = System.getProperty(
            "selekt.jdbc.maxCachedDatabases",
            DEFAULT_MAX_CACHED_DATABASES.toString()
        ).toIntOrNull() ?: DEFAULT_MAX_CACHED_DATABASES

        init {
            runCatching {
                DriverManager.registerDriver(SelektDriver())
                Runtime.getRuntime().addShutdownHook(thread(
                    start = false,
                    name = "selekt-driver-shutdown"
                ) {
                    databaseCacheLock.withLock {
                        databaseCache.values.toList().also {
                            databaseCache.clear()
                        }
                    }.forEachCatching(SharedDatabase::release)
                })
                logger.info("{} {} registered successfully", DRIVER_NAME, DRIVER_VERSION)
            }.onFailure { e ->
                logger.error("Failed to register {}: {}", DRIVER_NAME, e.message)
                throw SQLException("Failed to register Selekt JDBC driver", e)
            }
        }
    }

    override fun connect(url: String, info: Properties): Connection? = if (!acceptsURL(url)) {
        null
    } else {
        runCatching {
            val connectionURL = ConnectionURL.parse(url)
            val mergedProperties = mergeProperties(connectionURL.properties, info)
            val keyChars = removeKey(connectionURL.properties, mergedProperties, info)
            val sharedDatabase = try {
                getOrCreateDatabase(connectionURL, mergedProperties, keyChars)
            } finally {
                keyChars?.fill('\u0000')
            }
            JdbcConnection(sharedDatabase, connectionURL, mergedProperties)
        }.getOrElse { e ->
            val safeUrl = runCatching { ConnectionURL.parse(url).toString() }.getOrDefault("<unparseable URL>")
            throw SQLExceptionMapper.mapException(
                "Failed to create connection to $safeUrl: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    override fun acceptsURL(url: String?): Boolean = url != null && ConnectionURL.isValidUrl(url)

    override fun getPropertyInfo(url: String, info: Properties): Array<DriverPropertyInfo> = if (!acceptsURL(url)) {
        throw SQLException("Invalid URL format: $url")
    } else {
        arrayOf(
            DriverPropertyInfo(PROPERTY_KEY, info.getProperty(PROPERTY_KEY)).apply {
                description = "Encryption key (hex string or file path)"
                required = false
            },
            DriverPropertyInfo(PROPERTY_POOL_SIZE, info.getProperty(PROPERTY_POOL_SIZE, "10")).apply {
                description = "Maximum connection pool size"
                required = false
            },
            DriverPropertyInfo(PROPERTY_BUSY_TIMEOUT, info.getProperty(PROPERTY_BUSY_TIMEOUT, "30000")).apply {
                description = "SQLite busy timeout in milliseconds"
                required = false
            },
            DriverPropertyInfo(PROPERTY_JOURNAL_MODE, info.getProperty(PROPERTY_JOURNAL_MODE, "WAL")).apply {
                description = "SQLite journal mode"
                required = false
                choices = arrayOf("DELETE", "WAL", "MEMORY", "PERSIST", "TRUNCATE", "OFF")
            },
            DriverPropertyInfo(PROPERTY_FOREIGN_KEYS, info.getProperty(PROPERTY_FOREIGN_KEYS, "true")).apply {
                description = "Enable foreign key constraints"
                required = false
                choices = BOOLEAN_CHOICES
            },
            DriverPropertyInfo(
                PROPERTY_MAX_CACHED_DATABASES,
                info.getProperty(PROPERTY_MAX_CACHED_DATABASES, DEFAULT_MAX_CACHED_DATABASES.toString())
            ).apply {
                description = "Maximum number of databases held in the driver cache (-1 = unlimited, 0 = no caching)"
                required = false
            }
        )
    }

    override fun getMajorVersion(): Int = MAJOR_VERSION

    override fun getMinorVersion(): Int = MINOR_VERSION

    override fun jdbcCompliant(): Boolean = false

    override fun getParentLogger(): JulLogger = JulLogger.getLogger(SelektDriver::class.java.name)

    private fun getOrCreateDatabase(
        connectionURL: ConnectionURL,
        properties: Properties,
        keyChars: CharArray?
    ): SharedDatabase {
        val cacheKey = buildCacheKey(connectionURL, properties)
        val requestedMaxCachedDatabases = validateMaxCachedDatabases(
            properties.getProperty(PROPERTY_MAX_CACHED_DATABASES)?.toIntOrNull()
        )
        if (requestedMaxCachedDatabases == 0) {
            return SharedDatabase(createDatabase(connectionURL, properties, keyChars))
        }
        val evicted = mutableListOf<SharedDatabase>()
        val sharedDatabase = databaseCacheLock.withLock {
            if (requestedMaxCachedDatabases != null && requestedMaxCachedDatabases > 0) {
                maxCachedDatabases = requestedMaxCachedDatabases
            }
            databaseCache.getOrPut(cacheKey) {
                SharedDatabase(createDatabase(connectionURL, properties, keyChars)) {
                    databaseCacheLock.withLock {
                        databaseCache.remove(cacheKey)
                    }
                }
            }.also { db ->
                db.retain()
                evictExcess(cacheKey, evicted)
            }
        }
        evicted.forEachCatching(SharedDatabase::release)
        return sharedDatabase
    }

    private fun validateMaxCachedDatabases(value: Int?): Int? {
        if (value == null) return null
        require(value >= -1) {
            "maxCachedDatabases must be -1 (unlimited), 0 (no caching), or a positive integer, was: $value"
        }
        return value
    }

    private fun evictExcess(currentKey: String, evicted: MutableList<SharedDatabase>) {
        if (maxCachedDatabases < 0) {
            return
        }
        val iterator = databaseCache.iterator()
        while (databaseCache.size > maxCachedDatabases && iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key != currentKey) {
                iterator.remove()
                evicted.add(entry.value)
            }
        }
    }

    private fun createDatabase(
        connectionURL: ConnectionURL,
        properties: Properties,
        keyChars: CharArray?
    ): SQLDatabase {
        val configuration = buildDatabaseConfiguration(properties)
        val encryptionKey = encodeKeyToBytes(keyChars)
        val sqlite = object : com.bloomberg.selekt.SQLite(
            externalSQLiteSingleton()
        ) {
            override fun throwSQLException(
                code: com.bloomberg.selekt.SQLCode,
                extendedCode: com.bloomberg.selekt.SQLCode,
                message: String,
                context: String?
            ): Nothing {
                throw SQLExceptionMapper.mapException(message, code, extendedCode)
            }
        }
        return try {
            SQLDatabase(
                path = connectionURL.databasePath,
                sqlite = sqlite,
                configuration = configuration,
                key = encryptionKey,
                random = com.bloomberg.selekt.CommonThreadLocalRandom
            )
        } finally {
            encryptionKey?.zero()
        }
    }

    private fun buildDatabaseConfiguration(properties: Properties): DatabaseConfiguration = properties.run {
        val poolSize = getProperty(PROPERTY_POOL_SIZE)?.toIntOrNull() ?: DEFAULT_POOL_SIZE
        val busyTimeout = getProperty(PROPERTY_BUSY_TIMEOUT)?.toIntOrNull()
            ?: DatabaseConfiguration.COMMON_BUSY_TIMEOUT_MILLIS
        val journalMode = getProperty(PROPERTY_JOURNAL_MODE)?.let {
            SQLiteJournalMode.valueOf(it.uppercase())
        } ?: SQLiteJournalMode.WAL
        val baseConfig = journalMode.databaseConfiguration
        baseConfig.copy(
            maxConnectionPoolSize = poolSize,
            busyTimeoutMillis = busyTimeout,
            useNativeTransactionListeners = true
        )
    }

    private fun removeKey(
        urlProperties: Properties,
        mergedProperties: Properties,
        additionalProperties: Properties
    ): CharArray? = (mergedProperties.getProperty(PROPERTY_KEY) ?: return null).also {
        urlProperties.remove(PROPERTY_KEY)
        mergedProperties.remove(PROPERTY_KEY)
        additionalProperties.remove(PROPERTY_KEY)
    }.toCharArray()

    @Suppress("Detekt.ComplexCondition")
    private fun encodeKeyToBytes(keyChars: CharArray?): ByteArray? {
        if (keyChars == null) {
            return null
        }
        val bytes = if (
            keyChars.size >= HEX_PREFIX_LENGTH &&
            keyChars[0] == '0' &&
            keyChars[1].let { it == 'x' || it == 'X' }
        ) {
            parseHexKey(keyChars)
        } else {
            Charsets.UTF_8.encode(CharBuffer.wrap(keyChars)).let {
                ByteArray(it.remaining()).also(it::get)
            }
        }
        require(bytes.size == REQUIRED_KEY_LENGTH_BYTES) {
            "Encryption key must be exactly $REQUIRED_KEY_LENGTH_BYTES bytes, was ${bytes.size} bytes"
        }
        return bytes
    }

    private fun parseHexKey(keyChars: CharArray): ByteArray {
        val hexLength = keyChars.size - HEX_PREFIX_LENGTH
        require(hexLength > 0 && hexLength % HEX_CHUNK_SIZE == 0) {
            "Hex key must have an even number of hex digits after the '0x' prefix"
        }
        val byteArray = ByteArray(hexLength / HEX_CHUNK_SIZE)
        var i = HEX_PREFIX_LENGTH
        var j = 0
        while (i < keyChars.size - 1) {
            val high = Character.digit(keyChars[i], HEX_RADIX)
            val low = Character.digit(keyChars[i + 1], HEX_RADIX)
            require(high != -1 && low != -1) {
                "Invalid hex character in encryption key"
            }
            byteArray[j++] = (high shl BITS_PER_HEX_DIGIT or low).toByte()
            i += HEX_CHUNK_SIZE
        }
        return byteArray
    }

    private fun mergeProperties(
        urlProperties: Properties,
        additionalProperties: Properties
    ): Properties = Properties().apply {
        putAll(urlProperties)
        putAll(additionalProperties)
    }

    private fun buildCacheKey(
        connectionURL: ConnectionURL,
        properties: Properties
    ): String {
        val propertiesString = listOf(
            PROPERTY_BUSY_TIMEOUT,
            PROPERTY_FOREIGN_KEYS,
            PROPERTY_JOURNAL_MODE,
            PROPERTY_POOL_SIZE
        ).mapNotNull { key ->
            properties.getProperty(key)?.let { "$key=$it" }
        }.joinToString("&")
        return "${connectionURL.databasePath}?$propertiesString"
    }
}
