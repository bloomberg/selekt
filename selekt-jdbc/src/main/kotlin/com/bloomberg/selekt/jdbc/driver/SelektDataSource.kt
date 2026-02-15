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
import com.bloomberg.selekt.SQLCode
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLite
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.externalSQLiteSingleton
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.util.ConnectionURL
import java.io.File
import java.io.PrintWriter
import java.sql.Connection
import java.sql.SQLException
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger as JulLogger
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("TooGenericExceptionCaught")
class SelektDataSource : DataSource {
    companion object {
        private const val PROPERTY_ENCRYPT = "encrypt"
        private const val PROPERTY_KEY = "key"
        private const val PROPERTY_POOL_SIZE = "poolSize"
        private const val PROPERTY_BUSY_TIMEOUT = "busyTimeout"
        private const val PROPERTY_JOURNAL_MODE = "journalMode"
        private const val PROPERTY_FOREIGN_KEYS = "foreignKeys"
        private const val DEFAULT_POOL_SIZE = 10
        private const val HEX_PREFIX_LENGTH = 2
        private const val HEX_CHUNK_SIZE = 2
        private const val HEX_RADIX = 16

        private val CLOSED: VarHandle = MethodHandles.lookup()
            .findVarHandle(SelektDataSource::class.java, "closed", Boolean::class.javaPrimitiveType)
    }

    private val logger: Logger = LoggerFactory.getLogger(SelektDataSource::class.java)

    @Volatile
    private var closed = false

    @Volatile
    private var url: String = ""

    @Volatile
    var databasePath: String = ""
        set(value) {
            field = value
            url = "jdbc:sqlite:$value"
        }

    @Volatile
    var maxPoolSize: Int = DEFAULT_POOL_SIZE
        set(value) {
            require(value > 0) { "Pool size must be positive" }
            field = value
        }

    @Volatile
    var busyTimeout: Int = DatabaseConfiguration.COMMON_BUSY_TIMEOUT_MILLIS
        set(value) {
            require(value >= 0) { "Busy timeout must be non-negative" }
            field = value
        }

    @Volatile
    var journalMode: String = "WAL"
        set(value) {
            val isValidMode = try {
                SQLiteJournalMode.valueOf(value.uppercase())
                true
            } catch (e: IllegalArgumentException) {
                logger.debug("Invalid journal mode value '{}': {}", value, e.message)
                false
            }
            require(isValidMode) { "Invalid journal mode: $value" }
            field = value
        }

    @Volatile
    var foreignKeys: Boolean = true

    @Volatile
    var encryptionEnabled: Boolean = false

    @Volatile
    var encryptionKey: String? = null

    @Volatile
    private var loginTimeoutSeconds = 0

    @Volatile
    private var logWriter: PrintWriter? = null

    private val databaseCache = ConcurrentHashMap<String, SQLDatabase>()

    override fun getConnection(): Connection = getConnection(null, null)

    override fun getConnection(username: String?, password: String?): Connection {
        if (closed) {
            throw SQLException("DataSource is closed")
        }
        return runCatching {
            val connectionURL = buildConnectionURL()
            val mergedProperties = buildConnectionProperties()

            val database = getOrCreateDatabase(connectionURL, mergedProperties)
            JdbcConnection(database, connectionURL, mergedProperties)
        }.getOrElse { e ->
            throw SQLExceptionMapper.mapException(
                "Failed to create connection: ${e.message}",
                -1,
                -1,
                e
            )
        }
    }

    fun setEncryption(enabled: Boolean, key: String? = null) {
        encryptionEnabled = enabled
        encryptionKey = key
    }

    fun close() {
        if (CLOSED.compareAndSet(this, false, true)) {
            var firstException: Throwable? = null
            databaseCache.values.forEach { database ->
                runCatching {
                    database.close()
                }.onFailure { e ->
                    if (firstException == null) {
                        firstException = e
                    } else {
                        firstException.addSuppressed(e)
                    }
                }
            }
            databaseCache.clear()
            logger.info("SelektDataSource closed")
            firstException?.let { throw it }
        }
    }

    fun isClosed(): Boolean = closed

    override fun getLogWriter(): PrintWriter? = logWriter

    override fun setLogWriter(out: PrintWriter?) {
        logWriter = out
    }

    override fun setLoginTimeout(seconds: Int) {
        if (seconds < 0) {
            throw SQLException("Login timeout must be non-negative")
        }
        loginTimeoutSeconds = seconds
    }

    override fun getLoginTimeout(): Int = loginTimeoutSeconds

    override fun getParentLogger(): JulLogger = JulLogger.getLogger(SelektDataSource::class.java.name)

    override fun <T> unwrap(iface: Class<T>): T {
        if (iface.isInstance(this)) {
            @Suppress("UNCHECKED_CAST")
            return this as T
        }
        throw SQLException("Cannot unwrap to ${iface.name}")
    }

    override fun isWrapperFor(iface: Class<*>): Boolean = iface.isInstance(this)

    private fun buildConnectionURL(): ConnectionURL {
        val effectiveUrl = if (url.isNotEmpty()) {
            url
        } else if (databasePath.isNotEmpty()) {
            buildUrlFromProperties()
        } else {
            throw SQLException("No database path or URL specified")
        }

        return ConnectionURL.parse(effectiveUrl)
    }

    private fun buildUrlFromProperties(): String {
        val baseUrl = "jdbc:sqlite:$databasePath"
        return mutableListOf<String>().apply {
            if (encryptionEnabled) {
                add("encrypt=true")
                encryptionKey?.let { add("key=$it") }
            }
            add("poolSize=$maxPoolSize")
            add("busyTimeout=$busyTimeout")
            add("journalMode=$journalMode")
            add("foreignKeys=$foreignKeys")
        }.run {
            if (isEmpty()) {
                baseUrl
            } else {
                "$baseUrl?${joinToString("&")}"
            }
        }
    }

    private fun buildConnectionProperties(): Properties = Properties().apply {
        setProperty(PROPERTY_ENCRYPT, encryptionEnabled.toString())
        encryptionKey?.let { setProperty(PROPERTY_KEY, it) }
        setProperty(PROPERTY_POOL_SIZE, maxPoolSize.toString())
        setProperty(PROPERTY_BUSY_TIMEOUT, busyTimeout.toString())
        setProperty(PROPERTY_JOURNAL_MODE, journalMode)
        setProperty(PROPERTY_FOREIGN_KEYS, foreignKeys.toString())
    }

    private fun getOrCreateDatabase(
        connectionURL: ConnectionURL,
        properties: Properties
    ): SQLDatabase {
        val cacheKey = buildCacheKey(connectionURL, properties)
        return databaseCache.computeIfAbsent(cacheKey) {
            createDatabase(connectionURL, properties)
        }
    }

    private fun createDatabase(
        connectionURL: ConnectionURL,
        properties: Properties
    ): SQLDatabase {
        val configuration = buildDatabaseConfiguration(properties)
        val encryptionKeyBytes = getEncryptionKey(properties)
        val sqlite = object : SQLite(externalSQLiteSingleton()) {
            override fun throwSQLException(
                code: SQLCode,
                extendedCode: SQLCode,
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
            key = encryptionKeyBytes,
            random = com.bloomberg.selekt.CommonThreadLocalRandom
        )
    }

    private fun buildDatabaseConfiguration(properties: Properties): DatabaseConfiguration {
        val poolSizeValue = properties.getProperty(PROPERTY_POOL_SIZE)?.toIntOrNull() ?: maxPoolSize
        val busyTimeoutValue = properties.getProperty(PROPERTY_BUSY_TIMEOUT)?.toIntOrNull() ?: busyTimeout
        val journalModeValue = properties.getProperty(PROPERTY_JOURNAL_MODE)?.let {
            SQLiteJournalMode.valueOf(it.uppercase())
        } ?: SQLiteJournalMode.valueOf(journalMode.uppercase())
        val baseConfig = journalModeValue.databaseConfiguration
        return baseConfig.copy(
            maxConnectionPoolSize = poolSizeValue,
            busyTimeoutMillis = busyTimeoutValue
        )
    }

    private fun getEncryptionKey(properties: Properties): ByteArray? {
        val encrypt = properties.getProperty(PROPERTY_ENCRYPT)?.toBoolean() == true
        val keyProperty = properties.getProperty(PROPERTY_KEY)
        if (!encrypt || keyProperty == null) {
            return null
        }
        return keyProperty.run {
            when {
                startsWith("0x") || startsWith("0X") -> parseHexKey(this)
                else -> parseStringOrFileKey(this)
            }
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

    private fun buildCacheKey(
        connectionURL: ConnectionURL,
        properties: Properties
    ): String {
        val propString = listOf(
            PROPERTY_ENCRYPT,
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
