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

import com.bloomberg.selekt.CommonThreadLocalRandom
import com.bloomberg.selekt.commons.forEachCatching
import com.bloomberg.selekt.commons.zero
import com.bloomberg.selekt.DatabaseConfiguration
import com.bloomberg.selekt.DatabaseKey
import com.bloomberg.selekt.SQLCode
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLite
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.externalSQLiteSingleton
import com.bloomberg.selekt.jdbc.connection.JdbcConnection
import com.bloomberg.selekt.jdbc.exception.SQLExceptionMapper
import com.bloomberg.selekt.jdbc.util.ConnectionURL
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

/**
 * @since 0.28.0
 */
sealed interface EncryptionKeySource {
    data class Literal(val key: CharArray) : EncryptionKeySource {
        fun zero() { key.fill('\u0000') }

        override fun equals(other: Any?): Boolean = other is Literal && key.contentEquals(other.key)

        override fun hashCode(): Int = key.contentHashCode()
    }
}

@Suppress("TooGenericExceptionCaught")
class SelektDataSource : DataSource {
    companion object {
        private const val PROPERTY_BUSY_TIMEOUT = "busyTimeout"
        private const val PROPERTY_CURSOR_WINDOW_SIZE = "cursorWindowSize"
        private const val PROPERTY_FOREIGN_KEYS = "foreignKeys"
        private const val PROPERTY_JOURNAL_MODE = "journalMode"
        private const val PROPERTY_POOL_SIZE = "poolSize"
        private const val DEFAULT_POOL_SIZE = 10

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
    var cursorWindowSize: Int = Int.MAX_VALUE
        set(value) {
            require(value > 0) { "Cursor window size must be positive" }
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

    private val keyLock = Any()

    @Volatile
    private var keySource: EncryptionKeySource? = null

    var encryptionKeySource: EncryptionKeySource?
        get() = keySource
        set(value) {
            val next = if (value is EncryptionKeySource.Literal) {
                KeyEncoding.validateLength(value.key)
                EncryptionKeySource.Literal(value.key.copyOf())
            } else {
                value
            }
            synchronized(keyLock) {
                (keySource as? EncryptionKeySource.Literal)?.zero()
                keySource = next
            }
        }

    val encryptionEnabled: Boolean
        get() = encryptionKeySource != null


    @Volatile
    private var loginTimeoutSeconds = 0

    @Volatile
    private var logWriter: PrintWriter? = null

    private val databaseCache = ConcurrentHashMap<String, SharedDatabase>()

    override fun getConnection(): Connection = getConnection(null, null)

    override fun getConnection(username: String?, password: String?): Connection {
        if (closed) {
            throw SQLException("DataSource is closed")
        }
        if (username != null || password != null) {
            throw SQLException(
                "SelektDataSource ignores explicit username/password credentials; " +
                    "configure encryption via setEncryption(...) at DataSource construction time.",
                "28000"
            )
        }
        val (encryptionKeyBytes, keyHash) = snapshotEncryptionKey()
        return try {
            runCatching {
                val connectionURL = buildConnectionURL()
                val mergedProperties = buildConnectionProperties()
                val database = getOrCreateDatabase(connectionURL, mergedProperties, encryptionKeyBytes, keyHash)
                runCatching {
                    JdbcConnection(database, connectionURL, mergedProperties)
                }.getOrElse {
                    runCatching(database::release)
                    throw it
                }
            }.getOrElse { e ->
                throw SQLExceptionMapper.mapException(
                    "Failed to create connection: ${e.message}",
                    -1,
                    -1,
                    e
                )
            }
        } finally {
            encryptionKeyBytes?.zero()
        }
    }

    /**
     * Stores an internal copy of a literal key. The caller retains ownership of the supplied [CharArray]
     * and should zero it after this method returns.
     */
    fun setEncryption(keySource: EncryptionKeySource?) {
        encryptionKeySource = keySource
    }

    private fun snapshotEncryptionKey(): Pair<ByteArray?, String?> = synchronized(keyLock) {
        when (val source = keySource) {
            is EncryptionKeySource.Literal -> KeyEncoding.encode(source.key).let { it to hashKeyBytes(it) }
            null -> null to null
        }
    }

    fun close() {
        if (CLOSED.compareAndSet(this, false, true)) {
            encryptionKeySource = null
            databaseCache.run {
                values.forEachCatching(SharedDatabase::release)
                clear()
            }
            logger.info("SelektDataSource closed")
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
            add("busyTimeout=$busyTimeout")
            add("cursorWindowSize=$cursorWindowSize")
            add("foreignKeys=$foreignKeys")
            add("journalMode=$journalMode")
            add("poolSize=$maxPoolSize")
        }.run {
            if (isEmpty()) {
                baseUrl
            } else {
                "$baseUrl?${joinToString("&")}"
            }
        }
    }

    private fun buildConnectionProperties(): Properties = Properties().apply {
        setProperty(PROPERTY_POOL_SIZE, maxPoolSize.toString())
        setProperty(PROPERTY_BUSY_TIMEOUT, busyTimeout.toString())
        setProperty(PROPERTY_CURSOR_WINDOW_SIZE, cursorWindowSize.toString())
        setProperty(PROPERTY_JOURNAL_MODE, journalMode)
        setProperty(PROPERTY_FOREIGN_KEYS, foreignKeys.toString())
    }

    private fun getOrCreateDatabase(
        connectionURL: ConnectionURL,
        properties: Properties,
        encryptionKeyBytes: ByteArray?,
        keyHash: String?
    ): SharedDatabase {
        val cacheKey = buildCacheKey(connectionURL, properties, keyHash)
        while (true) {
            val cached = databaseCache.computeIfAbsent(cacheKey) {
                SharedDatabase(createDatabase(connectionURL, properties, encryptionKeyBytes)) {
                    databaseCache.remove(cacheKey)
                }
            }
            if (cached.tryRetain()) {
                return cached
            }
            databaseCache.remove(cacheKey, cached)
        }
    }

    private fun createDatabase(
        connectionURL: ConnectionURL,
        properties: Properties,
        encryptionKeyBytes: ByteArray?
    ): SQLDatabase {
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
        return encryptionKeyBytes?.let { DatabaseKey.take(sqlite, it) }.use {
            SQLDatabase(
                path = connectionURL.databasePath,
                sqlite = sqlite,
                configuration = buildDatabaseConfiguration(properties),
                key = it,
                random = CommonThreadLocalRandom
            )
        }
    }

    private fun buildDatabaseConfiguration(properties: Properties): DatabaseConfiguration {
        val poolSizeValue = properties.getProperty(PROPERTY_POOL_SIZE)?.toIntOrNull() ?: maxPoolSize
        val busyTimeoutValue = properties.getProperty(PROPERTY_BUSY_TIMEOUT)?.toIntOrNull() ?: busyTimeout
        val cursorWindowSizeValue = properties.getProperty(PROPERTY_CURSOR_WINDOW_SIZE)?.toIntOrNull()
            ?: cursorWindowSize
        val journalModeValue = properties.getProperty(PROPERTY_JOURNAL_MODE)?.let {
            SQLiteJournalMode.valueOf(it.uppercase())
        } ?: SQLiteJournalMode.valueOf(journalMode.uppercase())
        val baseConfig = journalModeValue.databaseConfiguration
        return baseConfig.copy(
            maxConnectionPoolSize = poolSizeValue,
            busyTimeoutMillis = busyTimeoutValue,
            useNativeTransactionListeners = true,
            cursorWindowSize = cursorWindowSizeValue
        )
    }

    private fun buildCacheKey(
        connectionURL: ConnectionURL,
        properties: Properties,
        keyHash: String?
    ): String = buildString {
        append(connectionURL.databasePath)
        append("?busyTimeout=").append(properties.getProperty(PROPERTY_BUSY_TIMEOUT))
        append("&cursorWindowSize=").append(properties.getProperty(PROPERTY_CURSOR_WINDOW_SIZE))
        append("&foreignKeys=").append(properties.getProperty(PROPERTY_FOREIGN_KEYS))
        append("&journalMode=").append(properties.getProperty(PROPERTY_JOURNAL_MODE))
        append("&poolSize=").append(properties.getProperty(PROPERTY_POOL_SIZE))
        keyHash?.let { append("&keyHash=").append(it) }
    }
}
