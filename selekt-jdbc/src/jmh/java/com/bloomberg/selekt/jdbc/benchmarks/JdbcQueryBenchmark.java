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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.bloomberg.selekt.jdbc.driver.SelektDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class JdbcQueryBenchmark {
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS query_test (id INTEGER PRIMARY KEY, name TEXT, value REAL, category TEXT)";
    private static final String CREATE_INDEX_SQL = "CREATE INDEX IF NOT EXISTS idx_category ON query_test (category)";
    private static final String INSERT_SQL =
        "INSERT OR IGNORE INTO query_test (id, name, value, category) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT id, name, value, category FROM query_test WHERE id = ?";
    private static final String SELECT_BY_CATEGORY_SQL =
        "SELECT id, name, value, category FROM query_test WHERE category = ?";
    private static final String SELECT_ALL_SQL = "SELECT id, name, value, category FROM query_test";
    private static final String SELECT_COUNT_SQL = "SELECT COUNT(*) FROM query_test";

    private static final int DATA_SIZE = 10_000;
    private static final String[] CATEGORIES = {"alpha", "beta", "gamma", "delta", "epsilon"};

    private static final Driver SELEKT_DRIVER = new SelektDriver();
    private static final Driver XERIAL_DRIVER = new org.sqlite.JDBC();

    @Param({"1", "100", "1000"})
    int queryCount;

    private File selektDatabaseFile;
    private File xerialDatabaseFile;

    private Connection selektConnection;
    private Connection xerialConnection;

    @Setup(Level.Iteration)
    public void setUp() throws SQLException, IOException {
        setupSelekt();
        setupXerial();
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws SQLException {
        if (selektConnection != null && !selektConnection.isClosed()) {
            selektConnection.close();
        }
        if (xerialConnection != null && !xerialConnection.isClosed()) {
            xerialConnection.close();
        }
        if (selektDatabaseFile != null) {
            deleteDatabase(selektDatabaseFile);
        }
        if (xerialDatabaseFile != null) {
            deleteDatabase(xerialDatabaseFile);
        }
    }

    private void setupSelekt() throws SQLException, IOException {
        selektDatabaseFile = Files.createTempFile("selekt-query-bench", ".db").toFile();
        selektDatabaseFile.deleteOnExit();
        final String url = "jdbc:sqlite:" + selektDatabaseFile.getAbsolutePath();
        selektConnection = SELEKT_DRIVER.connect(url, new Properties());
        initializeDatabase(selektConnection);
    }

    private void setupXerial() throws SQLException, IOException {
        xerialDatabaseFile = Files.createTempFile("xerial-query-bench", ".db").toFile();
        xerialDatabaseFile.deleteOnExit();
        final String url = "jdbc:sqlite:" + xerialDatabaseFile.getAbsolutePath();
        xerialConnection = XERIAL_DRIVER.connect(url, new Properties());
        initializeDatabase(xerialConnection);
    }

    private void initializeDatabase(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute(CREATE_TABLE_SQL);
            statement.execute(CREATE_INDEX_SQL);
        }
        connection.setAutoCommit(false);
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < DATA_SIZE; i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, "name_" + i);
                preparedStatement.setDouble(3, i * 1.5);
                preparedStatement.setString(4, CATEGORIES[i % CATEGORIES.length]);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        } catch (final SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Benchmark
    public void selektSelectById(final Blackhole blackhole) throws SQLException {
        selectById(selektConnection, blackhole);
    }

    @Benchmark
    public void xerialSelectById(final Blackhole blackhole) throws SQLException {
        selectById(xerialConnection, blackhole);
    }

    @Benchmark
    public void selektSelectByCategory(final Blackhole blackhole) throws SQLException {
        selectByCategory(selektConnection, blackhole);
    }

    @Benchmark
    public void xerialSelectByCategory(final Blackhole blackhole) throws SQLException {
        selectByCategory(xerialConnection, blackhole);
    }

    @Benchmark
    public void selektFullTableScan(final Blackhole blackhole) throws SQLException {
        fullTableScan(selektConnection, blackhole);
    }

    @Benchmark
    public void xerialFullTableScan(final Blackhole blackhole) throws SQLException {
        fullTableScan(xerialConnection, blackhole);
    }

    @Benchmark
    public long selektSelectCount() throws SQLException {
        return selectCount(selektConnection);
    }

    @Benchmark
    public long xerialSelectCount() throws SQLException {
        return selectCount(xerialConnection);
    }

    private void selectById(final Connection connection, final Blackhole blackhole) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            for (int i = 0; i < queryCount; i++) {
                final int id = i % DATA_SIZE;
                preparedStatement.setInt(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        blackhole.consume(resultSet.getInt(1));
                        blackhole.consume(resultSet.getString(2));
                        blackhole.consume(resultSet.getDouble(3));
                        blackhole.consume(resultSet.getString(4));
                    }
                }
            }
        }
    }

    private void selectByCategory(final Connection connection, final Blackhole blackhole) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_CATEGORY_SQL)) {
            for (int i = 0; i < queryCount; i++) {
                final String category = CATEGORIES[i % CATEGORIES.length];
                preparedStatement.setString(1, category);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        blackhole.consume(resultSet.getInt(1));
                        blackhole.consume(resultSet.getString(2));
                    }
                }
            }
        }
    }

    private void fullTableScan(final Connection connection, final Blackhole blackhole) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL)) {
            while (resultSet.next()) {
                blackhole.consume(resultSet.getInt(1));
                blackhole.consume(resultSet.getString(2));
                blackhole.consume(resultSet.getDouble(3));
                blackhole.consume(resultSet.getString(4));
            }
        }
    }

    private long selectCount(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_COUNT_SQL)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private static void deleteDatabase(final File file) {
        if (file == null) {
            return;
        }
        file.delete();
        new File(file.getPath() + "-journal").delete();
        new File(file.getPath() + "-wal").delete();
        new File(file.getPath() + "-shm").delete();
    }
}

