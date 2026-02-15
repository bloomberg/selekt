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
import com.bloomberg.selekt.externalSQLiteSingleton
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import java.io.File
import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager
import java.sql.DriverPropertyInfo
import java.sql.SQLException
import java.util.Properties
import java.util.logging.Logger as JulLogger
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Supports the URL format: jdbc:sqlite:path/to/database.sqlite[?properties]
 *
 * Supported connection properties:
 * - key: Encryption key (hex string or file path)
 * - poolSize: Maximum connection pool size (integer)
 * - busyTimeout: SQLite busy timeout in milliseconds (integer)
 * - journalMode: SQLite journal mode (DELETE, WAL, MEMORY, etc.)
 * - foreignKeys: Enable foreign key constraints (true/false)
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

        private const val DEFAULT_POOL_SIZE = 10

        private const val HEX_PREFIX_LENGTH = 2
        private const val HEX_CHUNK_SIZE = 2
        private const val HEX_RADIX = 16

        private val BOOLEAN_CHOICES = arrayOf("true", "false")

        private val databaseCache = ConcurrentHashMap<String, SQLDatabase>()

        init {
            runCatching {
                DriverManager.registerDriver(SelektDriver())
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
            val database = getOrCreateDatabase(connectionURL, mergedProperties)
            JdbcConnection(database, connectionURL, mergedProperties)
        }.getOrElse { e ->
            throw SQLExceptionMapper.mapException(
                "Failed to create connection to $url: ${e.message}",
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
    ): SQLDatabase = databaseCache.computeIfAbsent(buildCacheKey(connectionURL, properties)) {
        createDatabase(connectionURL, properties)
    }

    private fun createDatabase(
        connectionURL: ConnectionURL,
        properties: Properties
    ): SQLDatabase {
        val configuration = buildDatabaseConfiguration(properties)
        val encryptionKey = getEncryptionKey(properties)
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
            key = encryptionKey,
            random = com.bloomberg.selekt.CommonThreadLocalRandom
        )
    }

    private fun buildDatabaseConfiguration(properties: Properties): DatabaseConfiguration {
        val poolSize = properties.getProperty(PROPERTY_POOL_SIZE)?.toIntOrNull() ?: DEFAULT_POOL_SIZE
        val busyTimeout = properties.getProperty(PROPERTY_BUSY_TIMEOUT)?.toIntOrNull()
            ?: DatabaseConfiguration.COMMON_BUSY_TIMEOUT_MILLIS
        val journalMode = properties.getProperty(PROPERTY_JOURNAL_MODE)?.let {
            SQLiteJournalMode.valueOf(it.uppercase())
        } ?: SQLiteJournalMode.WAL
        val baseConfig = journalMode.databaseConfiguration
        return baseConfig.copy(
            maxConnectionPoolSize = poolSize,
            busyTimeoutMillis = busyTimeout
        )
    }

    private fun getEncryptionKey(
        properties: Properties
    ): ByteArray? = (properties.getProperty(PROPERTY_KEY) ?: return null).run {
        when {
            startsWith("0x") || startsWith("0X") -> parseHexKey(this)
            else -> parseStringOrFileKey(this)
        }
    }

    private fun parseHexKey(keyProperty: String): ByteArray = keyProperty.substring(HEX_PREFIX_LENGTH)
        .chunked(HEX_CHUNK_SIZE)
        .map {
            it.toInt(HEX_RADIX).toByte()
        }.toByteArray()

    private fun parseStringOrFileKey(keyProperty: String): ByteArray = runCatching {
        val file = File(keyProperty)
        if (file.exists() && file.isFile) {
            file.readBytes()
        } else {
            keyProperty.toByteArray(Charsets.UTF_8)
        }
    }.getOrElse { e ->
        logger.debug("Failed to read key from file '{}', treating as string key: {}", keyProperty, e.message)
        keyProperty.toByteArray(Charsets.UTF_8)
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
        val propString = listOf(
            PROPERTY_KEY,
            PROPERTY_POOL_SIZE,
            PROPERTY_BUSY_TIMEOUT,
            PROPERTY_JOURNAL_MODE,
            PROPERTY_FOREIGN_KEYS
        ).mapNotNull { key ->
            properties.getProperty(key)?.let { "$key=$it" }
        }.sorted().joinToString("&")
        return "${connectionURL.databasePath}?$propString"
    }
}
