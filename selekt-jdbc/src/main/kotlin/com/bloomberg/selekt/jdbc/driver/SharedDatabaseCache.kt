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

import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.commons.forEachCatching
import java.io.Closeable
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.concurrent.GuardedBy
import kotlin.concurrent.withLock
import org.slf4j.LoggerFactory

internal const val JDBC_DATABASE_IDLE_TIMEOUT_MILLIS = 30_000L
internal const val DEFAULT_MAX_IDLE_JDBC_DATABASES = 16

private const val CACHE_INITIAL_CAPACITY = 16
private const val CACHE_LOAD_FACTOR = 0.75f

internal enum class DatabaseIdlePolicy {
    EVICT_AFTER_TIMEOUT,
    RELEASE_IMMEDIATELY,
    RETAIN_UNTIL_CLEARED
}

/**
 * Retains shared databases briefly between JDBC connections while bounding idle resources.
 * Active and explicitly retained database identities do not count towards [maxIdleEntries].
 */
internal class SharedDatabaseCache(
    private val maxIdleEntries: Int = DEFAULT_MAX_IDLE_JDBC_DATABASES,
    private val idleTimeoutMillis: Long = JDBC_DATABASE_IDLE_TIMEOUT_MILLIS,
    private val scheduler: ScheduledExecutorService = SHARED_SCHEDULER
) : Closeable {
    private data class Entry(
        val database: SharedDatabase,
        val idlePolicy: DatabaseIdlePolicy,
        var activeConnections: Int
    ) {
        var generation = 0L
        var expiration: ScheduledFuture<*>? = null
    }

    private companion object {
        val logger = LoggerFactory.getLogger(SharedDatabaseCache::class.java)
        val SHARED_SCHEDULER: ScheduledExecutorService = ScheduledThreadPoolExecutor(
            1,
            ThreadFactory { runnable ->
                Thread(runnable, "Selekt.JdbcDatabaseCacheEvictor").apply { isDaemon = true }
            }
        ).apply {
            removeOnCancelPolicy = true
        }
    }

    init {
        require(maxIdleEntries >= 0) { "Maximum idle database count must be non-negative" }
        require(idleTimeoutMillis >= 0L) { "Database idle timeout must be non-negative" }
    }

    private val lock = ReentrantLock()

    @GuardedBy("lock")
    private val entries = LinkedHashMap<String, Entry>(CACHE_INITIAL_CAPACITY, CACHE_LOAD_FACTOR, true)

    @GuardedBy("lock")
    private var closed = false

    @GuardedBy("lock")
    private var idleEntries = 0

    internal val size: Int
        get() = lock.withLock(entries::size)

    fun acquire(
        cacheKey: String,
        idlePolicy: DatabaseIdlePolicy = DatabaseIdlePolicy.EVICT_AFTER_TIMEOUT,
        createDatabase: () -> SQLDatabase
    ): SharedDatabase = lock.withLock {
        check(!closed) { "Database cache is closed" }
        entries[cacheKey]?.let { entry ->
            check(entry.idlePolicy == idlePolicy) { "Cached database idle policy changed" }
            check(entry.database.tryRetain()) { "Cached database was already released" }
            if (entry.activeConnections == 0 &&
                entry.idlePolicy == DatabaseIdlePolicy.EVICT_AFTER_TIMEOUT
            ) {
                --idleEntries
            }
            ++entry.activeConnections
            ++entry.generation
            entry.expiration?.cancel(false)
            entry.expiration = null
            return@withLock entry.database
        }

        lateinit var created: SharedDatabase
        created = SharedDatabase(
            database = createDatabase(),
            onConnectionReleased = { releaseConnection(cacheKey, it) }
        )
        created.retain()
        entries[cacheKey] = Entry(created, idlePolicy, activeConnections = 1)
        created
    }

    private fun releaseConnection(cacheKey: String, database: SharedDatabase) {
        val evictions = lock.withLock {
            val entry = entries[cacheKey]
            if (entry == null || entry.database !== database) {
                return@withLock emptyList()
            }
            check(entry.activeConnections > 0) { "Database connection count underflow" }
            if (--entry.activeConnections != 0) {
                return@withLock emptyList()
            }

            val generation = ++entry.generation
            when (entry.idlePolicy) {
                DatabaseIdlePolicy.RETAIN_UNTIL_CLEARED -> emptyList()
                DatabaseIdlePolicy.RELEASE_IMMEDIATELY -> {
                    entries.remove(cacheKey)
                    listOf(entry.database)
                }
                DatabaseIdlePolicy.EVICT_AFTER_TIMEOUT -> {
                    ++idleEntries
                    if (idleTimeoutMillis == 0L) {
                        entries.remove(cacheKey)
                        --idleEntries
                        listOf(entry.database)
                    } else {
                        entry.expiration = scheduler.schedule(
                            { expire(cacheKey, database, generation) },
                            idleTimeoutMillis,
                            TimeUnit.MILLISECONDS
                        )
                        evictExcessIdleEntries()
                    }
                }
            }
        }
        evictions.releaseAll()
    }

    private fun expire(cacheKey: String, database: SharedDatabase, generation: Long) {
        val expired = lock.withLock {
            val entry = entries[cacheKey]
            when {
                entry == null -> null
                entry.database !== database -> null
                entry.activeConnections != 0 -> null
                entry.generation != generation -> null
                else -> {
                    entries.remove(cacheKey)
                    --idleEntries
                    entry.database
                }
            }
        }
        listOfNotNull(expired).releaseAll()
    }

    @GuardedBy("lock")
    private fun evictExcessIdleEntries(): List<SharedDatabase> {
        var excess = idleEntries - maxIdleEntries
        if (excess <= 0) {
            return emptyList()
        }
        return buildList {
            val iterator = entries.entries.iterator()
            while (iterator.hasNext() && excess > 0) {
                val entry = iterator.next().value
                if (entry.activeConnections == 0 &&
                    entry.idlePolicy == DatabaseIdlePolicy.EVICT_AFTER_TIMEOUT
                ) {
                    entry.expiration?.cancel(false)
                    iterator.remove()
                    add(entry.database)
                    --idleEntries
                    --excess
                }
            }
        }
    }

    fun clear() {
        removeAll(markClosed = false).releaseAll()
    }

    override fun close() {
        removeAll(markClosed = true).releaseAll()
    }

    private fun removeAll(markClosed: Boolean): List<SharedDatabase> = lock.withLock {
        if (markClosed) {
            closed = true
        }
        entries.values.map { entry ->
            entry.expiration?.cancel(false)
            entry.database
        }.also {
            entries.clear()
            idleEntries = 0
        }
    }

    private fun Iterable<SharedDatabase>.releaseAll() {
        forEachCatching(SharedDatabase::release).forEach {
            logger.warn("Failed to release cached JDBC database", it)
        }
    }
}
