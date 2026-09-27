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

package com.bloomberg.selekt.jdbc.benchmarks;

import com.bloomberg.selekt.jdbc.driver.SelektDataSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class JdbcPageCacheBenchmark {
    private static final int ROW_COUNT = 100_000;
    private static final int PAYLOAD_SIZE_BYTES = 256;
    private static final int INSERT_BATCH_SIZE = 1_000;
    private static final String SELECT_SQL = "SELECT length(payload) FROM bench WHERE id = ?";

    @State(Scope.Benchmark)
    public static class DatabaseState {
        @Param({"default", "800", "2048", "8192"})
        String pageCacheSizeKiB;

        @Param({"1", "4"})
        int poolSize;

        private File databaseFile;
        private SelektDataSource dataSource;

        @Setup(Level.Trial)
        public void setUp() throws IOException, SQLException {
            databaseFile = Files.createTempFile("selekt-page-cache-bench", ".db").toFile();
            initializeDatabase(databaseFile);

            dataSource = new SelektDataSource();
            dataSource.setDatabasePath(databaseFile.getAbsolutePath());
            dataSource.setMaxPoolSize(poolSize);
            if (!"default".equals(pageCacheSizeKiB)) {
                dataSource.setPageCacheSizeKiB(Integer.valueOf(pageCacheSizeKiB));
            }
            verifyCacheSize();
        }

        @TearDown(Level.Trial)
        public void tearDown() throws IOException {
            dataSource.close();
            deleteDatabase(databaseFile);
        }

        private void verifyCacheSize() throws SQLException {
            if ("default".equals(pageCacheSizeKiB)) {
                return;
            }
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("PRAGMA cache_size")) {
                if (!resultSet.next() || resultSet.getInt(1) != -Integer.parseInt(pageCacheSizeKiB)) {
                    throw new IllegalStateException("Configured SQLite page-cache size was not applied");
                }
            }
        }
    }

    @State(Scope.Thread)
    public static class ConnectionState {
        private Connection connection;
        private PreparedStatement statement;
        private int randomState;

        @Setup(Level.Trial)
        public void setUp(final DatabaseState databaseState) throws SQLException {
            connection = databaseState.dataSource.getConnection();
            statement = connection.prepareStatement(
                SELECT_SQL,
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY
            );
            randomState = 0x9e3779b9 ^ (int) Thread.currentThread().getId();
        }

        @TearDown(Level.Trial)
        public void tearDown() throws SQLException {
            statement.close();
            connection.close();
        }

        private int nextRowId() {
            int value = randomState;
            value ^= value << 13;
            value ^= value >>> 17;
            value ^= value << 5;
            randomState = value;
            return (value & Integer.MAX_VALUE) % ROW_COUNT + 1;
        }
    }

    @Benchmark
    @Threads(1)
    public int singleThreadRandomPrimaryKeyRead(final ConnectionState connectionState) throws SQLException {
        return randomPrimaryKeyRead(connectionState);
    }

    @Benchmark
    @Threads(4)
    public int fourThreadRandomPrimaryKeyRead(final ConnectionState connectionState) throws SQLException {
        return randomPrimaryKeyRead(connectionState);
    }

    private static int randomPrimaryKeyRead(final ConnectionState connectionState) throws SQLException {
        connectionState.statement.setInt(1, connectionState.nextRowId());
        try (ResultSet resultSet = connectionState.statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Benchmark row was not found");
            }
            return resultSet.getInt(1);
        }
    }

    private static void initializeDatabase(final File databaseFile) throws SQLException {
        final org.sqlite.JDBC driver = new org.sqlite.JDBC();
        try (Connection connection = driver.connect(
                "jdbc:sqlite:" + databaseFile.getAbsolutePath(), new Properties());
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("CREATE TABLE bench (id INTEGER PRIMARY KEY, payload TEXT NOT NULL)");
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO bench (id, payload) VALUES (?, ?)")) {
                final String payload = "x".repeat(PAYLOAD_SIZE_BYTES);
                for (int id = 1; id <= ROW_COUNT; id++) {
                    insert.setInt(1, id);
                    insert.setString(2, payload);
                    insert.addBatch();
                    if (id % INSERT_BATCH_SIZE == 0) {
                        insert.executeBatch();
                    }
                }
                insert.executeBatch();
            }
            connection.commit();
            connection.setAutoCommit(true);
            statement.execute("PRAGMA wal_checkpoint(TRUNCATE)");
        }
    }

    private static void deleteDatabase(final File databaseFile) throws IOException {
        Files.deleteIfExists(databaseFile.toPath());
        Files.deleteIfExists(new File(databaseFile.getPath() + "-wal").toPath());
        Files.deleteIfExists(new File(databaseFile.getPath() + "-shm").toPath());
    }
}
