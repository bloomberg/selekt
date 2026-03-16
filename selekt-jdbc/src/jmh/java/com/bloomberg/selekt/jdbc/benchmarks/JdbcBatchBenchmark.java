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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class JdbcBatchBenchmark {
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS batch_test (id INTEGER PRIMARY KEY, name TEXT, value REAL, data BLOB)";
    private static final String INSERT_SQL =
        "INSERT OR REPLACE INTO batch_test (id, name, value, data) VALUES (?, ?, ?, ?)";

    private static final Driver SELEKT_DRIVER = new SelektDriver();
    private static final Driver XERIAL_DRIVER = new org.sqlite.JDBC();

    @Param({"100", "1000", "5000", "10000"})
    int batchSize;

    @Param({"SIMPLE", "MIXED", "LARGE_BLOBS"})
    String dataType;

    private File selektDatabaseFile;
    private File xerialDatabaseFile;

    private Connection selektConnection;
    private Connection xerialConnection;

    private String[] names;
    private double[] values;
    private byte[][] blobs;

    @Setup(Level.Iteration)
    public void setUp() throws SQLException, IOException {
        generateTestData();
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
        selektDatabaseFile = Files.createTempFile("selekt-batch-bench", ".db").toFile();
        selektDatabaseFile.deleteOnExit();
        final String url = "jdbc:sqlite:" + selektDatabaseFile.getAbsolutePath();
        selektConnection = SELEKT_DRIVER.connect(url, new Properties());
        try (Statement statement = selektConnection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute(CREATE_TABLE_SQL);
        }
    }

    private void setupXerial() throws SQLException, IOException {
        xerialDatabaseFile = Files.createTempFile("xerial-batch-bench", ".db").toFile();
        xerialDatabaseFile.deleteOnExit();
        final String url = "jdbc:sqlite:" + xerialDatabaseFile.getAbsolutePath();
        xerialConnection = XERIAL_DRIVER.connect(url, new Properties());
        try (Statement statement = xerialConnection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute(CREATE_TABLE_SQL);
        }
    }

    private void generateTestData() {
        names = new String[batchSize];
        values = new double[batchSize];
        blobs = new byte[batchSize][];
        for (int i = 0; i < batchSize; i++) {
            switch (dataType) {
                case "SIMPLE" -> {
                    names[i] = "item_" + i;
                    values[i] = i * 1.5;
                    blobs[i] = null;
                }
                case "MIXED" -> {
                    names[i] = (i % 3 == 0) ? "mixed_" + i : "default_" + i;
                    values[i] = (i % 2 == 0) ? i * 2.5 : 0.0;
                    blobs[i] = (i % 5 == 0) ? new byte[64] : new byte[16];
                }
                case "LARGE_BLOBS" -> {
                    names[i] = "blob_item_" + i;
                    values[i] = i * 1.0;
                    blobs[i] = new byte[2048];
                }
                default -> {
                    names[i] = "default_" + i;
                    values[i] = 1.0;
                    blobs[i] = null;
                }
            }
        }
    }

    @Benchmark
    public void selektBatchInsert(final Blackhole blackhole) throws SQLException {
        blackhole.consume(executeBatch(selektConnection));
    }

    @Benchmark
    public void xerialBatchInsert(final Blackhole blackhole) throws SQLException {
        blackhole.consume(executeBatch(xerialConnection));
    }

    @Benchmark
    public void selektIndividualInserts(final Blackhole blackhole) throws SQLException {
        blackhole.consume(executeIndividual(selektConnection));
    }

    @Benchmark
    public void xerialIndividualInserts(final Blackhole blackhole) throws SQLException {
        blackhole.consume(executeIndividual(xerialConnection));
    }

    private int[] executeBatch(final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < batchSize; i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, names[i]);
                preparedStatement.setDouble(3, values[i]);
                preparedStatement.setBytes(4, blobs[i]);
                preparedStatement.addBatch();
            }
            final int[] result = preparedStatement.executeBatch();
            connection.commit();
            return result;
        } catch (final SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private int executeIndividual(final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        int totalUpdated = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < batchSize; i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, names[i]);
                preparedStatement.setDouble(3, values[i]);
                preparedStatement.setBytes(4, blobs[i]);
                totalUpdated += preparedStatement.executeUpdate();
            }
            connection.commit();
            return totalUpdated;
        } catch (final SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
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

