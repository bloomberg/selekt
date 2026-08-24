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
import com.bloomberg.selekt.SelektVersion
import com.bloomberg.selekt.SharedResource
import com.bloomberg.selekt.commons.forEachCatching
import com.bloomberg.selekt.externalSQLiteSingleton
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager
import java.sql.DriverPropertyInfo
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
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
 * - poolSize: Maximum connection pool size (integer, default: 10)
 * - busyTimeout: SQLite busy timeout in milliseconds (integer, default: 2500)
 * - cursorWindowSize: Maximum rows retained by a cursor window (positive integer, default: unbounded)
 * - journalMode: SQLite journal mode (DELETE, WAL, MEMORY, etc., default: WAL)
 * - foreignKeys: Enable foreign key constraints (true/false, default: true)
 *
 * @since 0.28.0
 */
@Suppress("TooGenericExceptionCaught")
class SelektDriver : Driver {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SelektDriver::class.java)

        const val DRIVER_NAME = "Selekt JDBC Driver"

        @JvmStatic
        val DRIVER_VERSION: String get() = SelektVersion.version

        @JvmStatic
        val MAJOR_VERSION: Int get() = SelektVersion.majorVersion

        @JvmStatic
        val MINOR_VERSION: Int get() = SelektVersion.minorVersion

        @JvmStatic
        val PATCH_VERSION: Int get() = SelektVersion.patchVersion

        private const val PROPERTY_KEY = "key"
        private const val PROPERTY_POOL_SIZE = "poolSize"
        private const val PROPERTY_BUSY_TIMEOUT = "busyTimeout"
        private const val PROPERTY_CURSOR_WINDOW_SIZE = "cursorWindowSize"
        private const val PROPERTY_JOURNAL_MODE = "journalMode"
        private const val PROPERTY_FOREIGN_KEYS = "foreignKeys"

        private const val DEFAULT_POOL_SIZE = 10
        private const val ENCRYPTION_KEY_UNSUPPORTED_MESSAGE =
            "Encryption keys are not supported by SelektDriver because JDBC URLs and Properties use " +
                "immutable Strings that cannot be scrubbed; use SelektDataSource.setEncryption with a CharArray"

        private val BOOLEAN_CHOICES = arrayOf("true", "false")

        private val databaseCacheLock = ReentrantLock()
        private val databaseCache = LinkedHashMap<String, SharedDatabase>(16, 0.75f, true)

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
        rejectEncryptionKey(url, info)
        runCatching {
            val connectionURL = ConnectionURL.parse(url)
            val mergedProperties = mergeProperties(connectionURL.properties, info)
            val sharedDatabase = getOrCreateDatabase(connectionURL, mergedProperties)
            runCatching {
                JdbcConnection(sharedDatabase, connectionURL, mergedProperties)
            }.getOrElse {
                runCatching(sharedDatabase::release)
                throw it
            }
        }.getOrElse { e ->
            if (e is SQLFeatureNotSupportedException) {
                throw e
            }
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
        rejectEncryptionKey(url, info)
        arrayOf(
            DriverPropertyInfo(PROPERTY_POOL_SIZE, info.getProperty(PROPERTY_POOL_SIZE, "10")).apply {
                description = "Maximum connection pool size"
                required = false
            },
            DriverPropertyInfo(PROPERTY_BUSY_TIMEOUT, info.getProperty(PROPERTY_BUSY_TIMEOUT, "30000")).apply {
                description = "SQLite busy timeout in milliseconds"
                required = false
            },
            DriverPropertyInfo(
                PROPERTY_CURSOR_WINDOW_SIZE,
                info.getProperty(PROPERTY_CURSOR_WINDOW_SIZE, Int.MAX_VALUE.toString())
            ).apply {
                description = "Maximum rows retained by a cursor window"
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
            }
        )
    }

    override fun getMajorVersion(): Int = MAJOR_VERSION

    override fun getMinorVersion(): Int = MINOR_VERSION

    override fun jdbcCompliant(): Boolean = false

    override fun getParentLogger(): JulLogger = JulLogger.getLogger(SelektDriver::class.java.name)

    private fun getOrCreateDatabase(
        connectionURL: ConnectionURL,
        properties: Properties
    ): SharedDatabase {
        val cacheKey = buildCacheKey(connectionURL, properties)
        return databaseCacheLock.withLock {
            databaseCache.getOrPut(cacheKey) {
                SharedDatabase(createDatabase(connectionURL, properties)) {
                    databaseCacheLock.withLock {
                        databaseCache.remove(cacheKey)
                    }
                }
            }.also(SharedResource::retain)
        }
    }

    private fun createDatabase(
        connectionURL: ConnectionURL,
        properties: Properties
    ): SQLDatabase {
        val configuration = buildDatabaseConfiguration(properties)
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
        return SQLDatabase(
            path = connectionURL.databasePath,
            sqlite = sqlite,
            configuration = configuration,
            key = null,
            random = com.bloomberg.selekt.CommonThreadLocalRandom
        )
    }

    private fun buildDatabaseConfiguration(properties: Properties): DatabaseConfiguration = properties.run {
        val poolSize = getProperty(PROPERTY_POOL_SIZE)?.toIntOrNull() ?: DEFAULT_POOL_SIZE
        val busyTimeout = getProperty(PROPERTY_BUSY_TIMEOUT)?.toIntOrNull()
            ?: DatabaseConfiguration.COMMON_BUSY_TIMEOUT_MILLIS
        val cursorWindowSize = getProperty(PROPERTY_CURSOR_WINDOW_SIZE)?.toIntOrNull() ?: Int.MAX_VALUE
        val journalMode = getProperty(PROPERTY_JOURNAL_MODE)?.let {
            SQLiteJournalMode.valueOf(it.uppercase())
        } ?: SQLiteJournalMode.WAL
        val baseConfig = journalMode.databaseConfiguration
        baseConfig.copy(
            maxConnectionPoolSize = poolSize,
            busyTimeoutMillis = busyTimeout,
            useNativeTransactionListeners = true,
            cursorWindowSize = cursorWindowSize
        )
    }

    private fun rejectEncryptionKey(url: String, properties: Properties) {
        if (ConnectionURL.containsEncryptionKey(url) || containsEncryptionKey(properties)) {
            logger.warn(ENCRYPTION_KEY_UNSUPPORTED_MESSAGE)
            throw SQLFeatureNotSupportedException(ENCRYPTION_KEY_UNSUPPORTED_MESSAGE, "0A000")
        }
    }

    private fun containsEncryptionKey(properties: Properties): Boolean = properties.keys.any {
        it is String && it.equals(PROPERTY_KEY, ignoreCase = true)
    } || properties.stringPropertyNames().any { it.equals(PROPERTY_KEY, ignoreCase = true) }

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
            PROPERTY_CURSOR_WINDOW_SIZE,
            PROPERTY_FOREIGN_KEYS,
            PROPERTY_JOURNAL_MODE,
            PROPERTY_POOL_SIZE
        ).mapNotNull { key ->
            properties.getProperty(key)?.let { "$key=$it" }
        }.joinToString("&")
        return buildString {
            append(connectionURL.databasePath)
            append('?')
            append(propertiesString)
        }
    }
}
