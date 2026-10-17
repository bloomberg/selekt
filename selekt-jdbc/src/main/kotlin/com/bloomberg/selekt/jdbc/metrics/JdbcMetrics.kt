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

package com.bloomberg.selekt.jdbc.metrics

import com.bloomberg.selekt.ConnectionPoolMetrics
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.LongAdder

enum class MetricOutcome { SUCCESS, ERROR }

enum class StatementOperation { QUERY, UPDATE, EXECUTE, BATCH }

enum class TransactionOutcome { COMMIT, ROLLBACK, ERROR }

enum class CacheEvictionReason { CAPACITY, IDLE, IMMEDIATE, INVALIDATED }

data class TimedMetric(val count: Long, val totalNanos: Long)

data class JdbcConnectionPoolMetrics(
    val activePrimaryConnections: Int,
    val idlePrimaryConnections: Int,
    val pendingPrimaryBorrows: Int,
    val activeSecondaryConnections: Int,
    val idleSecondaryConnections: Int,
    val pendingSecondaryBorrows: Int,
    val borrowCount: Long,
    val borrowWaitNanos: Long,
    val borrowTimeoutCount: Long,
    val evictionCount: Long
)

data class PreparedStatementCacheMetrics(
    val hitCount: Long,
    val missCount: Long,
    val evictionCount: Long
)

/** Framework-neutral, point-in-time operational metrics for a Selekt JDBC data source. */
data class JdbcMetricsSnapshot(
    val activeConnections: Int,
    val cachedDatabases: Int,
    val connectionOpens: Map<MetricOutcome, TimedMetric>,
    val statements: Map<StatementOperation, Map<MetricOutcome, TimedMetric>>,
    val transactions: Map<TransactionOutcome, TimedMetric>,
    val resultSets: Map<MetricOutcome, TimedMetric>,
    val resultSetRows: Map<MetricOutcome, Long>,
    val rowsAffected: Map<StatementOperation, Long>,
    val preparedStatementCache: PreparedStatementCacheMetrics,
    val cacheEvictions: Map<CacheEvictionReason, Long>,
    val connectionPool: JdbcConnectionPoolMetrics
)

internal class MetricAccumulator {
    private val count = LongAdder()
    private val totalNanos = LongAdder()

    fun record(durationNanos: Long) {
        count.increment()
        totalNanos.add(durationNanos.coerceAtLeast(0L))
    }

    fun snapshot() = TimedMetric(count.sum(), totalNanos.sum())
}

internal class JdbcMetrics {
    private val activeConnections = AtomicInteger()
    private val connectionOpen = Array(MetricOutcome.entries.size) { MetricAccumulator() }
    private val statements = Array(StatementOperation.entries.size) {
        Array(MetricOutcome.entries.size) { MetricAccumulator() }
    }
    private val transactions = Array(TransactionOutcome.entries.size) { MetricAccumulator() }
    private val resultSets = Array(MetricOutcome.entries.size) { MetricAccumulator() }
    private val resultSetRows = Array(MetricOutcome.entries.size) { LongAdder() }
    private val rowsAffected = Array(StatementOperation.entries.size) { LongAdder() }
    private val preparedStatementCacheHits = LongAdder()
    private val preparedStatementCacheMisses = LongAdder()
    private val preparedStatementCacheEvictions = LongAdder()
    private val cacheEvictions = Array(CacheEvictionReason.entries.size) { LongAdder() }
    private val archivedPoolMetrics = AtomicReference(ConnectionPoolMetrics.EMPTY)

    fun connectionOpened() {
        activeConnections.incrementAndGet()
    }

    fun connectionClosed() {
        activeConnections.updateAndGet { count -> (count - 1).coerceAtLeast(0) }
    }

    fun recordConnectionOpen(durationNanos: Long, successful: Boolean) {
        connectionOpen[successful.outcome().ordinal].record(durationNanos)
    }

    fun <T> recordStatement(operation: StatementOperation, block: () -> T): T {
        val startNanos = System.nanoTime()
        var successful = false
        try {
            return block().also { successful = true }
        } finally {
            statements[operation.ordinal][successful.outcome().ordinal].record(System.nanoTime() - startNanos)
        }
    }

    fun recordTransaction(outcome: TransactionOutcome, durationNanos: Long) {
        transactions[outcome.ordinal].record(durationNanos)
    }

    fun recordResultSet(durationNanos: Long, rows: Long, successful: Boolean) {
        val outcome = successful.outcome()
        resultSets[outcome.ordinal].record(durationNanos)
        resultSetRows[outcome.ordinal].add(rows.coerceAtLeast(0L))
    }

    fun rowsAffected(operation: StatementOperation, count: Long) {
        rowsAffected[operation.ordinal].add(count.coerceAtLeast(0L))
    }

    fun preparedStatementCacheHit() = preparedStatementCacheHits.increment()

    fun preparedStatementCacheMiss() = preparedStatementCacheMisses.increment()

    fun preparedStatementCacheEvicted() = preparedStatementCacheEvictions.increment()

    fun cacheEvicted(reason: CacheEvictionReason, count: Int = 1) {
        cacheEvictions[reason.ordinal].add(count.toLong())
    }

    fun archive(metrics: ConnectionPoolMetrics) {
        archivedPoolMetrics.updateAndGet { it + metrics }
    }

    fun poolMetricsWith(liveMetrics: ConnectionPoolMetrics): ConnectionPoolMetrics =
        archivedPoolMetrics.get() + liveMetrics

    fun snapshot(cachedDatabases: Int, connectionPool: ConnectionPoolMetrics) = JdbcMetricsSnapshot(
        activeConnections = activeConnections.get(),
        cachedDatabases = cachedDatabases,
        connectionOpens = MetricOutcome.entries.associateWith { connectionOpen[it.ordinal].snapshot() },
        statements = StatementOperation.entries.associateWith { operation ->
            MetricOutcome.entries.associateWith { outcome ->
                statements[operation.ordinal][outcome.ordinal].snapshot()
            }
        },
        transactions = TransactionOutcome.entries.associateWith { transactions[it.ordinal].snapshot() },
        resultSets = MetricOutcome.entries.associateWith { resultSets[it.ordinal].snapshot() },
        resultSetRows = MetricOutcome.entries.associateWith { resultSetRows[it.ordinal].sum() },
        rowsAffected = StatementOperation.entries.associateWith { rowsAffected[it.ordinal].sum() },
        preparedStatementCache = PreparedStatementCacheMetrics(
            hitCount = preparedStatementCacheHits.sum(),
            missCount = preparedStatementCacheMisses.sum(),
            evictionCount = preparedStatementCacheEvictions.sum()
        ),
        cacheEvictions = CacheEvictionReason.entries.associateWith { cacheEvictions[it.ordinal].sum() },
        connectionPool = connectionPool.toJdbcMetrics()
    )

    private fun Boolean.outcome() = if (this) { MetricOutcome.SUCCESS } else { MetricOutcome.ERROR }
}

private fun ConnectionPoolMetrics.toJdbcMetrics() = JdbcConnectionPoolMetrics(
    activePrimaryConnections,
    idlePrimaryConnections,
    pendingPrimaryBorrows,
    activeSecondaryConnections,
    idleSecondaryConnections,
    pendingSecondaryBorrows,
    borrowCount,
    borrowWaitNanos,
    borrowTimeoutCount,
    evictionCount
)
