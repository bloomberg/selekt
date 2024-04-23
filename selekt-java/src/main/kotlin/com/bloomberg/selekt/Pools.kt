/*
 * Copyright 2020 Bloomberg Finance L.P.
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

import com.bloomberg.selekt.pools.IPooledObject
import com.bloomberg.selekt.pools.IObjectFactory
import com.bloomberg.selekt.pools.PoolConfiguration
import com.bloomberg.selekt.pools.createObjectPool
import java.io.Closeable
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import javax.annotation.concurrent.ThreadSafe

private const val KEEP_ALIVE_MULTIPLIER = 1.5

private val sharedExecutor = ScheduledThreadPoolExecutor(1, ThreadFactory {
    Thread(it).apply {
        isDaemon = true
        name = "Selekt.Evictor"
        priority = Thread.NORM_PRIORITY
    }
}).apply {
    removeOnCancelPolicy = true
    setKeepAliveTime(
        (KEEP_ALIVE_MULTIPLIER * SQLiteJournalMode.WAL.databaseConfiguration.timeBetweenEvictionRunsMillis).toLong(),
        TimeUnit.MILLISECONDS
    )
    allowCoreThreadTimeOut(true)
}

@JvmSynthetic
internal fun openConnectionPool(
    path: String,
    sqlite: SQLite,
    configuration: DatabaseConfiguration,
    random: IRandom,
    key: ByteArray?
) = createObjectPool(
    SQLConnectionFactory(path, sqlite, configuration, random, key?.let { Key(it) }),
    sharedExecutor,
    configuration.toPoolConfiguration()
)

internal interface CloseableSQLExecutor : SQLExecutor, Closeable, IPooledObject<String>

private fun DatabaseConfiguration.toPoolConfiguration() = PoolConfiguration(
    evictionDelayMillis = evictionDelayMillis,
    evictionIntervalMillis = timeBetweenEvictionRunsMillis,
    maxTotal = maxConnectionPoolSize
)

@ThreadSafe
internal class SQLConnectionFactory(
    private val path: String,
    private val sqlite: SQLite,
    private val configuration: DatabaseConfiguration,
    private val random: IRandom,
    private val key: Key?
) : IObjectFactory<CloseableSQLExecutor> {
    private val busyLock = Any()

    override fun close() {
        key?.zero()
    }

    override fun destroyObject(obj: CloseableSQLExecutor) = synchronized(busyLock) {
        obj.close()
    }

    override fun makeObject() = synchronized(busyLock) {
        SQLConnection(path, sqlite, configuration, SQL_OPEN_READONLY, random, key)
    }

    override fun makePrimaryObject() = synchronized(busyLock) {
        SQLConnection(path, sqlite, configuration, SQL_OPEN_READWRITE or SQL_OPEN_CREATE, random, key)
    }
}
