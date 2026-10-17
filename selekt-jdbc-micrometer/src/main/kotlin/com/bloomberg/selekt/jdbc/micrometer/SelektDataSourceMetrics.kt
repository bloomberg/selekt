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

package com.bloomberg.selekt.jdbc.micrometer

import com.bloomberg.selekt.jdbc.driver.SelektDataSource
import com.bloomberg.selekt.jdbc.metrics.CacheEvictionReason
import com.bloomberg.selekt.jdbc.metrics.JdbcConnectionPoolMetrics
import com.bloomberg.selekt.jdbc.metrics.MetricOutcome
import com.bloomberg.selekt.jdbc.metrics.StatementOperation
import com.bloomberg.selekt.jdbc.metrics.TransactionOutcome
import io.micrometer.core.instrument.FunctionCounter
import io.micrometer.core.instrument.FunctionTimer
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.MeterBinder
import java.util.concurrent.TimeUnit
import java.util.function.ToDoubleFunction
import java.util.function.ToLongFunction

private const val METRIC_PREFIX = "selekt.jdbc"
private const val PRIMARY = "primary"
private const val SECONDARY = "secondary"
private const val ACTIVE = "active"
private const val IDLE = "idle"
private const val OUTCOME = "outcome"
private const val POOL_METRICS_REFRESH_MILLIS = 100L
private val POOL_METRICS_REFRESH_NANOS = TimeUnit.MILLISECONDS.toNanos(POOL_METRICS_REFRESH_MILLIS)

/**
 * Publishes bounded-cardinality operational metrics for a [SelektDataSource].
 *
 * SQL text, database paths, encryption state, and bound values are never used as tags. Applications with multiple data
 * sources should supply a distinguishing common tag such as `Tag.of("database", "orders")`.
 */
class SelektDataSourceMetrics @JvmOverloads constructor(
    private val dataSource: SelektDataSource,
    private val tags: Iterable<Tag> = emptyList()
) : MeterBinder {
    private val poolMetrics = PoolMetricsSampler()

    override fun bindTo(registry: MeterRegistry) {
        gauge(registry, "$METRIC_PREFIX.connections.active", "Open logical JDBC connections") {
            it.metricValues.activeConnections.toDouble()
        }
        gauge(registry, "$METRIC_PREFIX.databases.cached", "Database instances retained by the JDBC cache") {
            it.metricValues.cachedDatabases.toDouble()
        }
        bindConnectionMetrics(registry)
        bindStatementMetrics(registry)
        bindTransactionMetrics(registry)
        bindResultSetMetrics(registry)
        bindPreparedStatementCacheMetrics(registry)
        bindCacheMetrics(registry)
        bindPoolMetrics(registry)
    }

    private fun bindConnectionMetrics(registry: MeterRegistry) {
        MetricOutcome.entries.forEach { outcome ->
            functionTimer(
                registry,
                "$METRIC_PREFIX.connections.open",
                "Time spent opening logical JDBC connections",
                listOf(Tag.of(OUTCOME, outcome.tagValue)),
                { it.metricValues.connectionOpenCount(outcome) },
                { it.metricValues.connectionOpenTotalNanos(outcome).toDouble() }
            )
        }
    }

    private fun bindStatementMetrics(registry: MeterRegistry) {
        StatementOperation.entries.forEach { operation ->
            MetricOutcome.entries.forEach { outcome ->
                functionTimer(
                    registry,
                    "$METRIC_PREFIX.statements",
                    "Time spent executing JDBC statements; streaming result consumption is excluded",
                    listOf(Tag.of("operation", operation.tagValue), Tag.of(OUTCOME, outcome.tagValue)),
                    { it.metricValues.statementCount(operation, outcome) },
                    { it.metricValues.statementTotalNanos(operation, outcome).toDouble() }
                )
            }
        }
        StatementOperation.entries.filterNot { it == StatementOperation.QUERY }.forEach { operation ->
            functionCounter(
                registry,
                "$METRIC_PREFIX.rows.affected",
                "Rows affected by successful JDBC write operations",
                listOf(Tag.of("operation", operation.tagValue))
            ) { it.metricValues.affectedRowCount(operation).toDouble() }
        }
    }

    private fun bindTransactionMetrics(registry: MeterRegistry) {
        TransactionOutcome.entries.forEach { outcome ->
            functionTimer(
                registry,
                "$METRIC_PREFIX.transactions",
                "Explicit transaction count and duration from begin through completion",
                listOf(Tag.of(OUTCOME, outcome.tagValue)),
                { it.metricValues.transactionCount(outcome) },
                { it.metricValues.transactionTotalNanos(outcome).toDouble() }
            )
        }
    }

    private fun bindResultSetMetrics(registry: MeterRegistry) {
        MetricOutcome.entries.forEach { outcome ->
            functionTimer(
                registry,
                "$METRIC_PREFIX.resultsets",
                "Result set lifetime from creation through exhaustion or close",
                listOf(Tag.of(OUTCOME, outcome.tagValue)),
                { it.metricValues.resultSetCount(outcome) },
                { it.metricValues.resultSetTotalNanos(outcome).toDouble() }
            )
            functionCounter(
                registry,
                "$METRIC_PREFIX.resultsets.rows",
                "Rows returned by ResultSet.next()",
                listOf(Tag.of(OUTCOME, outcome.tagValue))
            ) { it.metricValues.resultSetRowCount(outcome).toDouble() }
        }
    }

    private fun bindPreparedStatementCacheMetrics(registry: MeterRegistry) {
        listOf(
            "hit" to { source: SelektDataSource -> source.metricValues.preparedStatementCacheHitCount.toDouble() },
            "miss" to { source: SelektDataSource -> source.metricValues.preparedStatementCacheMissCount.toDouble() }
        ).forEach { (result, value) ->
            functionCounter(
                registry,
                "$METRIC_PREFIX.statement.cache.requests",
                "Prepared-statement cache lookups",
                listOf(Tag.of("result", result)),
                value
            )
        }
        functionCounter(
            registry,
            "$METRIC_PREFIX.statement.cache.evictions",
            "Prepared statements evicted due to capacity or replacement",
            emptyList()
        ) { it.metricValues.preparedStatementCacheEvictionCount.toDouble() }
    }

    private fun bindCacheMetrics(registry: MeterRegistry) {
        CacheEvictionReason.entries.forEach { reason ->
            functionCounter(
                registry,
                "$METRIC_PREFIX.database.cache.evictions",
                "Database instances removed from the JDBC cache",
                listOf(Tag.of("reason", reason.tagValue))
            ) { it.metricValues.cacheEvictionCount(reason).toDouble() }
        }
    }

    private fun bindPoolMetrics(registry: MeterRegistry) {
        val sampler = poolMetrics
        listOf(
            PoolGauge(PRIMARY, ACTIVE) { it.activePrimaryConnections.toDouble() },
            PoolGauge(PRIMARY, IDLE) { it.idlePrimaryConnections.toDouble() },
            PoolGauge(SECONDARY, ACTIVE) { it.activeSecondaryConnections.toDouble() },
            PoolGauge(SECONDARY, IDLE) { it.idleSecondaryConnections.toDouble() }
        ).forEach { metric ->
            gauge(
                registry,
                "$METRIC_PREFIX.pool.connections",
                "Physical SQLite connections by role and state",
                listOf(Tag.of("role", metric.role), Tag.of("state", metric.state)),
                { source -> metric.value(sampler.sample(source)) }
            )
        }
        listOf(
            PRIMARY to { metrics: JdbcConnectionPoolMetrics -> metrics.pendingPrimaryBorrows.toDouble() },
            SECONDARY to { metrics: JdbcConnectionPoolMetrics -> metrics.pendingSecondaryBorrows.toDouble() }
        ).forEach { (role, value) ->
            gauge(
                registry,
                "$METRIC_PREFIX.pool.pending",
                "Current connection borrows waiting for the physical SQLite pool",
                listOf(Tag.of("role", role)),
                { source -> value(sampler.sample(source)) }
            )
        }
        functionTimer(
            registry,
            "$METRIC_PREFIX.pool.borrows",
            "Physical SQLite connection borrow count and wait time",
            emptyList(),
            { sampler.sample(it).borrowCount },
            { sampler.sample(it).borrowWaitNanos.toDouble() }
        )
        functionCounter(
            registry,
            "$METRIC_PREFIX.pool.borrow.timeouts",
            "Physical SQLite connection borrow timeouts",
            emptyList()
        ) { sampler.sample(it).borrowTimeoutCount.toDouble() }
        functionCounter(
            registry,
            "$METRIC_PREFIX.pool.evictions",
            "Physical SQLite connections evicted from the pool",
            emptyList()
        ) { sampler.sample(it).evictionCount.toDouble() }
    }

    private fun gauge(
        registry: MeterRegistry,
        name: String,
        description: String,
        meterTags: Iterable<Tag> = emptyList(),
        value: (SelektDataSource) -> Double
    ) {
        Gauge.builder(name, dataSource) { source -> safely(0.0) { value(source) } }
            .description(description)
            .tags(tags)
            .tags(meterTags)
            .register(registry)
    }

    @Suppress("LongParameterList")
    private fun functionTimer(
        registry: MeterRegistry,
        name: String,
        description: String,
        meterTags: Iterable<Tag>,
        count: (SelektDataSource) -> Long,
        totalNanos: (SelektDataSource) -> Double
    ) {
        FunctionTimer.builder(
            name,
            dataSource,
            ToLongFunction { source -> safely(0L) { count(source) } },
            ToDoubleFunction { source -> safely(0.0) { totalNanos(source) } },
            TimeUnit.NANOSECONDS
        )
            .description(description)
            .tags(tags)
            .tags(meterTags)
            .register(registry)
    }

    private fun functionCounter(
        registry: MeterRegistry,
        name: String,
        description: String,
        meterTags: Iterable<Tag>,
        count: (SelektDataSource) -> Double
    ) {
        FunctionCounter.builder(name, dataSource) { source -> safely(0.0) { count(source) } }
            .description(description)
            .tags(tags)
            .tags(meterTags)
            .register(registry)
    }

    private data class PoolGauge(
        val role: String,
        val state: String,
        val value: (JdbcConnectionPoolMetrics) -> Double
    )
}

internal class PoolMetricsSampler(
    private val refreshNanos: Long = POOL_METRICS_REFRESH_NANOS,
    private val nanoTime: () -> Long = System::nanoTime
) {
    init {
        require(refreshNanos >= 0L) { "Pool metrics refresh interval must be non-negative" }
    }

    @Volatile
    private var cached: Sample? = null

    fun sample(source: SelektDataSource): JdbcConnectionPoolMetrics = sample { source.metricValues.connectionPool }

    internal fun sample(load: () -> JdbcConnectionPoolMetrics): JdbcConnectionPoolMetrics {
        val now = nanoTime()
        cached?.takeIf { now - it.sampledAtNanos <= refreshNanos }?.let { return it.metrics }
        return synchronized(this) {
            val lockedNow = nanoTime()
            cached?.takeIf { lockedNow - it.sampledAtNanos <= refreshNanos }?.metrics
                ?: load().also { cached = Sample(it, lockedNow) }
        }
    }

    private data class Sample(
        val metrics: JdbcConnectionPoolMetrics,
        val sampledAtNanos: Long
    )
}

private inline fun <T> safely(fallback: T, value: () -> T): T = try {
    value()
} catch (_: Exception) {
    fallback
}

private val Enum<*>.tagValue: String
    get() = name.lowercase()
