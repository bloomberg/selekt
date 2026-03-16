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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

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
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class JdbcTransactionBenchmark {
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS txn_test (id INTEGER PRIMARY KEY, value TEXT)";
    private static final String INSERT_SQL = "INSERT OR REPLACE INTO txn_test (id, value) VALUES (?, ?)";

    private static final int OPERATIONS_PER_TRANSACTION = 100;
    private static final int TRANSACTION_COUNT = 50;

    private static final Driver SELEKT_DRIVER = new SelektDriver();
    private static final Driver XERIAL_DRIVER = new org.sqlite.JDBC();

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
        selektDatabaseFile = Files.createTempFile("selekt-txn-bench", ".db").toFile();
        selektDatabaseFile.deleteOnExit();
        final String url = "jdbc:sqlite:" + selektDatabaseFile.getAbsolutePath();
        selektConnection = SELEKT_DRIVER.connect(url, new Properties());
        try (Statement statement = selektConnection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute(CREATE_TABLE_SQL);
        }
    }

    private void setupXerial() throws SQLException, IOException {
        xerialDatabaseFile = Files.createTempFile("xerial-txn-bench", ".db").toFile();
        xerialDatabaseFile.deleteOnExit();
        final String url = "jdbc:sqlite:" + xerialDatabaseFile.getAbsolutePath();
        xerialConnection = XERIAL_DRIVER.connect(url, new Properties());
        try (Statement statement = xerialConnection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute(CREATE_TABLE_SQL);
        }
    }

    @Benchmark
    public int selektAutoCommitInserts() throws SQLException {
        return autoCommitInserts(selektConnection);
    }

    @Benchmark
    public int xerialAutoCommitInserts() throws SQLException {
        return autoCommitInserts(xerialConnection);
    }

    @Benchmark
    public int selektExplicitTransactions() throws SQLException {
        return explicitTransactions(selektConnection);
    }

    @Benchmark
    public int xerialExplicitTransactions() throws SQLException {
        return explicitTransactions(xerialConnection);
    }

    @Benchmark
    public int selektManySmallTransactions() throws SQLException {
        return manySmallTransactions(selektConnection);
    }

    @Benchmark
    public int xerialManySmallTransactions() throws SQLException {
        return manySmallTransactions(xerialConnection);
    }

    @Benchmark
    public int selektTransactionWithRollback() throws SQLException {
        return transactionWithRollback(selektConnection);
    }

    @Benchmark
    public int xerialTransactionWithRollback() throws SQLException {
        return transactionWithRollback(xerialConnection);
    }

    private int autoCommitInserts(final Connection connection) throws SQLException {
        connection.setAutoCommit(true);
        int count = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < OPERATIONS_PER_TRANSACTION; i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, "auto_" + i);
                count += preparedStatement.executeUpdate();
            }
        }
        return count;
    }

    private int explicitTransactions(final Connection connection) throws SQLException {
        int count = 0;
        connection.setAutoCommit(false);
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < OPERATIONS_PER_TRANSACTION * TRANSACTION_COUNT; i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, "explicit_" + i);
                count += preparedStatement.executeUpdate();
            }
            connection.commit();
        } catch (final SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
        return count;
    }

    private int manySmallTransactions(final Connection connection) throws SQLException {
        int count = 0;
        for (int txn = 0; txn < TRANSACTION_COUNT; txn++) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
                for (int i = 0; i < OPERATIONS_PER_TRANSACTION; i++) {
                    final int id = txn * OPERATIONS_PER_TRANSACTION + i;
                    preparedStatement.setInt(1, id);
                    preparedStatement.setString(2, "small_" + id);
                    count += preparedStatement.executeUpdate();
                }
                connection.commit();
            } catch (final SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return count;
    }

    private int transactionWithRollback(final Connection connection) throws SQLException {
        int count = 0;
        for (int txn = 0; txn < TRANSACTION_COUNT; txn++) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
                for (int i = 0; i < OPERATIONS_PER_TRANSACTION; i++) {
                    final int id = txn * OPERATIONS_PER_TRANSACTION + i;
                    preparedStatement.setInt(1, id);
                    preparedStatement.setString(2, "rollback_" + id);
                    count += preparedStatement.executeUpdate();
                }
                if (txn % 2 == 0) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (final SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return count;
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

