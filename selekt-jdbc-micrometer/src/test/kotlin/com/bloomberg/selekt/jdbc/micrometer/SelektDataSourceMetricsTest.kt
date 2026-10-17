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

import com.bloomberg.selekt.jdbc.driver.EncryptionKeySource
import com.bloomberg.selekt.jdbc.driver.SelektDataSource
import com.bloomberg.selekt.jdbc.metrics.JdbcConnectionPoolMetrics
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.io.File
import java.sql.BatchUpdateException
import java.sql.SQLException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class SelektDataSourceMetricsTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    @Suppress("LongMethod", "NestedBlockDepth")
    fun publishesConnectionStatementCacheAndPoolMetrics() {
        val registry = SimpleMeterRegistry()
        val databaseFile = File(tempDir, "metrics.db")
        val dataSource = SelektDataSource().apply {
            databasePath = databaseFile.absolutePath
        }
        SelektDataSourceMetrics(dataSource, listOf(Tag.of("pool", "test"))).bindTo(registry)

        try {
            assertGauge(registry, "selekt.jdbc.connections.active", 0.0)
            assertGauge(registry, "selekt.jdbc.databases.cached", 0.0)

            dataSource.connection.use { connection ->
                assertGauge(registry, "selekt.jdbc.connections.active", 1.0)
                assertGauge(registry, "selekt.jdbc.databases.cached", 1.0)
                connection.createStatement().use { statement ->
                    statement.executeUpdate("CREATE TABLE metric_test(value INTEGER)")
                    statement.executeQuery("SELECT 1").use { resultSet ->
                        assertTrue(resultSet.next())
                        assertFalse(resultSet.next())
                    }
                    statement.execute("SELECT 1")
                    assertFailsWith<SQLException> { statement.executeQuery("THIS IS NOT SQL") }
                    statement.addBatch("INSERT INTO metric_test VALUES (1)")
                    statement.addBatch("INSERT INTO metric_test VALUES (2)")
                    statement.executeBatch()
                    connection.autoCommit = false
                    statement.executeUpdate("INSERT INTO metric_test VALUES (3)")
                    connection.commit()
                    statement.executeUpdate("INSERT INTO metric_test VALUES (4)")
                    connection.rollback()
                }
                connection.prepareStatement("SELECT 42").close()
                connection.prepareStatement("SELECT 42").close()
                repeat(33) { index ->
                    connection.prepareStatement("SELECT $index").close()
                }
            }

            dataSource.setEncryption(EncryptionKeySource.Literal("exactly-32-bytes-of-key-data!!!!".toCharArray()))
            assertGauge(registry, "selekt.jdbc.connections.active", 0.0)
            assertGauge(registry, "selekt.jdbc.databases.cached", 0.0)
            assertEquals(1.0, functionTimer(registry, "connections.open", "success").count())
            assertEquals(0.0, functionTimer(registry, "connections.open", "error").count())
            assertEquals(1.0, statementTimer(registry, "query", "success").count())
            assertEquals(1.0, statementTimer(registry, "query", "error").count())
            assertEquals(3.0, statementTimer(registry, "update", "success").count())
            assertEquals(1.0, statementTimer(registry, "execute", "success").count())
            assertEquals(1.0, statementTimer(registry, "batch", "success").count())
            assertEquals(1.0, functionTimer(registry, "transactions", "commit").count())
            assertEquals(1.0, functionTimer(registry, "transactions", "rollback").count())
            assertTrue(functionTimer(registry, "resultsets", "success").count() >= 2.0)
            assertEquals(
                1.0,
                registry.get("selekt.jdbc.resultsets.rows")
                    .tags("pool", "test", "outcome", "success")
                    .functionCounter()
                    .count()
            )
            assertTrue(rowsAffected(registry, "update") >= 2.0)
            assertEquals(2.0, rowsAffected(registry, "batch"))
            assertTrue(statementCacheRequests(registry, "hit") >= 1.0)
            assertTrue(statementCacheRequests(registry, "miss") >= 1.0)
            assertTrue(
                registry.get("selekt.jdbc.statement.cache.evictions")
                    .tag("pool", "test")
                    .functionCounter()
                    .count() >= 1.0
            )
            assertEquals(
                1.0,
                registry.get("selekt.jdbc.database.cache.evictions")
                    .tags("pool", "test", "reason", "invalidated")
                    .functionCounter()
                    .count()
            )
            assertTrue(registry.get("selekt.jdbc.pool.borrows").tag("pool", "test").functionTimer().count() > 0)
            assertTrue(registry.get("selekt.jdbc.pool.evictions").tag("pool", "test").functionCounter().count() > 0)
        } finally {
            dataSource.close()
            registry.close()
        }
    }

    @Test
    fun connectionOpenMetricsExcludeCredentialValidation() {
        val registry = SimpleMeterRegistry()
        val dataSource = SelektDataSource().apply {
            databasePath = File(tempDir, "missing/metrics.db").absolutePath
        }
        SelektDataSourceMetrics(dataSource, listOf(Tag.of("pool", "test"))).bindTo(registry)

        try {
            assertFailsWith<SQLException> { dataSource.getConnection("user", "password") }
            assertEquals(0.0, functionTimer(registry, "connections.open", "error").count())
            assertFailsWith<SQLException> { dataSource.connection }
            assertEquals(1.0, functionTimer(registry, "connections.open", "error").count())
        } finally {
            dataSource.close()
            registry.close()
        }
    }

    @Test
    fun metricTagsNeverContainSqlOrDatabasePaths() {
        val registry = SimpleMeterRegistry()
        val databaseFile = File(tempDir, "secret-database-name.db")
        val dataSource = SelektDataSource().apply { databasePath = databaseFile.absolutePath }
        SelektDataSourceMetrics(dataSource).bindTo(registry)

        try {
            dataSource.connection.use { connection ->
                connection.createStatement().use { it.executeQuery("SELECT 'sensitive-value'").close() }
            }
            registry.meters.flatMap { it.id.tags }.forEach { tag ->
                assertTrue("sensitive-value" !in tag.value)
                assertTrue(databaseFile.name !in tag.value)
                assertTrue(tag.key in setOf("operation", "outcome", "reason", "result", "role", "state"))
            }
        } finally {
            dataSource.close()
            registry.close()
        }
    }

    @Test
    @Suppress("NestedBlockDepth")
    fun failedBatchRecordsAffectedRowsFromSuccessfulPrefix() {
        val registry = SimpleMeterRegistry()
        val dataSource = SelektDataSource().apply {
            databasePath = File(tempDir, "partial-batch.db").absolutePath
        }
        SelektDataSourceMetrics(dataSource, listOf(Tag.of("pool", "test"))).bindTo(registry)

        try {
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate("CREATE TABLE items(value INTEGER)")
                    statement.addBatch("INSERT INTO items VALUES (1)")
                    statement.addBatch("THIS IS NOT SQL")
                    assertFailsWith<BatchUpdateException> { statement.executeBatch() }
                    statement.executeQuery("SELECT COUNT(*) FROM items").use { resultSet ->
                        assertTrue(resultSet.next())
                        assertEquals(1, resultSet.getInt(1))
                    }
                }
            }

            assertEquals(1.0, rowsAffected(registry, "batch"))
            assertEquals(1.0, statementTimer(registry, "batch", "error").count())
            assertEquals(0.0, statementTimer(registry, "batch", "success").count())
        } finally {
            dataSource.close()
            registry.close()
        }
    }

    @Test
    fun poolMetricsAreSampledOncePerRefreshInterval() {
        var now = 1_000L
        var loads = 0
        val metrics = JdbcConnectionPoolMetrics(1, 2, 3, 4, 5, 6, 7L, 8L, 9L, 10L)
        val sampler = PoolMetricsSampler(refreshNanos = 100L) { now }

        repeat(54) {
            assertEquals(metrics, sampler.sample {
                ++loads
                metrics
            })
        }
        assertEquals(1, loads)

        now += 101L
        assertEquals(metrics, sampler.sample {
            ++loads
            metrics
        })
        assertEquals(2, loads)
    }

    @Test
    fun concurrentPoolMetricsReadsShareOneSample() {
        val loads = AtomicInteger()
        val loading = CountDownLatch(1)
        val release = CountDownLatch(1)
        val metrics = JdbcConnectionPoolMetrics(1, 2, 3, 4, 5, 6, 7L, 8L, 9L, 10L)
        val sampler = PoolMetricsSampler(refreshNanos = 100L) { 1_000L }
        val executor = Executors.newFixedThreadPool(2)

        try {
            val first = executor.submit<JdbcConnectionPoolMetrics> {
                sampler.sample {
                    loads.incrementAndGet()
                    loading.countDown()
                    release.await()
                    metrics
                }
            }
            assertTrue(loading.await(5L, TimeUnit.SECONDS))
            val second = executor.submit<JdbcConnectionPoolMetrics> {
                sampler.sample {
                    loads.incrementAndGet()
                    metrics
                }
            }
            release.countDown()

            assertEquals(metrics, first.get(5L, TimeUnit.SECONDS))
            assertEquals(metrics, second.get(5L, TimeUnit.SECONDS))
            assertEquals(1, loads.get())
        } finally {
            release.countDown()
            executor.shutdownNow()
        }
    }

    private fun assertGauge(registry: SimpleMeterRegistry, name: String, expected: Double) {
        assertEquals(expected, registry.get(name).tag("pool", "test").gauge().value(), absoluteTolerance = 1e-6)
    }

    private fun functionTimer(registry: SimpleMeterRegistry, name: String, outcome: String) = registry
        .get("selekt.jdbc.$name")
        .tags("pool", "test", "outcome", outcome)
        .functionTimer()

    private fun statementTimer(registry: SimpleMeterRegistry, operation: String, outcome: String) = registry
        .get("selekt.jdbc.statements")
        .tags("pool", "test", "operation", operation, "outcome", outcome)
        .functionTimer()

    private fun rowsAffected(registry: SimpleMeterRegistry, operation: String) = registry
        .get("selekt.jdbc.rows.affected")
        .tags("pool", "test", "operation", operation)
        .functionCounter()
        .count()

    private fun statementCacheRequests(registry: SimpleMeterRegistry, result: String) = registry
        .get("selekt.jdbc.statement.cache.requests")
        .tags("pool", "test", "result", result)
        .functionCounter()
        .count()
}
