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
public class JdbcStatementBenchmark {
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS stmt_test (id INTEGER PRIMARY KEY, name TEXT, value REAL)";
    private static final String INSERT_SQL = "INSERT OR REPLACE INTO stmt_test (id, name, value) VALUES (?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE stmt_test SET value = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM stmt_test WHERE id = ?";
    private static final String SELECT_SQL = "SELECT id, name, value FROM stmt_test WHERE id = ?";

    private static final int DATA_SIZE = 5_000;

    private static final Driver SELEKT_DRIVER = new SelektDriver();
    private static final Driver XERIAL_DRIVER = new org.sqlite.JDBC();

    @Param({"100", "1000"})
    int operationCount;

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
        selektDatabaseFile = Files.createTempFile("selekt-stmt-bench", ".db").toFile();
        selektDatabaseFile.deleteOnExit();
        final String url = "jdbc:sqlite:" + selektDatabaseFile.getAbsolutePath();
        selektConnection = SELEKT_DRIVER.connect(url, new Properties());
        initializeDatabase(selektConnection);
    }

    private void setupXerial() throws SQLException, IOException {
        xerialDatabaseFile = Files.createTempFile("xerial-stmt-bench", ".db").toFile();
        xerialDatabaseFile.deleteOnExit();
        final String url = "jdbc:sqlite:" + xerialDatabaseFile.getAbsolutePath();
        xerialConnection = XERIAL_DRIVER.connect(url, new Properties());
        initializeDatabase(xerialConnection);
    }

    private void initializeDatabase(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute(CREATE_TABLE_SQL);
        }
        connection.setAutoCommit(false);
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < DATA_SIZE; i++) {
                preparedStatement.setInt(1, i);
                preparedStatement.setString(2, "name_" + i);
                preparedStatement.setDouble(3, i * 1.5);
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
    public int selektPreparedInsert() throws SQLException {
        return executePreparedInserts(selektConnection);
    }

    @Benchmark
    public int xerialPreparedInsert() throws SQLException {
        return executePreparedInserts(xerialConnection);
    }

    @Benchmark
    public int selektPreparedUpdate() throws SQLException {
        return executePreparedUpdates(selektConnection);
    }

    @Benchmark
    public int xerialPreparedUpdate() throws SQLException {
        return executePreparedUpdates(xerialConnection);
    }

    @Benchmark
    public int selektPreparedDelete() throws SQLException {
        return executePreparedDeletes(selektConnection);
    }

    @Benchmark
    public int xerialPreparedDelete() throws SQLException {
        return executePreparedDeletes(xerialConnection);
    }

    @Benchmark
    public int selektPreparedSelect() throws SQLException {
        return executePreparedSelects(selektConnection);
    }

    @Benchmark
    public int xerialPreparedSelect() throws SQLException {
        return executePreparedSelects(xerialConnection);
    }

    @Benchmark
    public int selektMixedWorkload() throws SQLException {
        return executeMixedWorkload(selektConnection);
    }

    @Benchmark
    public int xerialMixedWorkload() throws SQLException {
        return executeMixedWorkload(xerialConnection);
    }

    private int executePreparedInserts(final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        int count = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < operationCount; i++) {
                final int id = DATA_SIZE + i;
                preparedStatement.setInt(1, id);
                preparedStatement.setString(2, "new_" + id);
                preparedStatement.setDouble(3, id * 2.0);
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

    private int executePreparedUpdates(final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        int count = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            for (int i = 0; i < operationCount; i++) {
                final int id = i % DATA_SIZE;
                preparedStatement.setDouble(1, id * 3.0);
                preparedStatement.setInt(2, id);
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

    private int executePreparedDeletes(final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        int count = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            for (int i = 0; i < operationCount; i++) {
                preparedStatement.setInt(1, i);
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

    private int executePreparedSelects(final Connection connection) throws SQLException {
        int count = 0;
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SQL)) {
            for (int i = 0; i < operationCount; i++) {
                final int id = i % DATA_SIZE;
                preparedStatement.setInt(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private int executeMixedWorkload(final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        int count = 0;
        try (PreparedStatement insertStatement = connection.prepareStatement(INSERT_SQL);
             PreparedStatement selectStatement = connection.prepareStatement(SELECT_SQL);
             PreparedStatement updateStatement = connection.prepareStatement(UPDATE_SQL);
             PreparedStatement deleteStatement = connection.prepareStatement(DELETE_SQL)) {
            for (int i = 0; i < operationCount; i++) {
                final int op = i % 4;
                final int id = i % DATA_SIZE;
                switch (op) {
                    case 0 -> {
                        insertStatement.setInt(1, DATA_SIZE + i);
                        insertStatement.setString(2, "mixed_" + i);
                        insertStatement.setDouble(3, i * 1.0);
                        count += insertStatement.executeUpdate();
                    }
                    case 1 -> {
                        selectStatement.setInt(1, id);
                        try (ResultSet resultSet = selectStatement.executeQuery()) {
                            if (resultSet.next()) {
                                count++;
                            }
                        }
                    }
                    case 2 -> {
                        updateStatement.setDouble(1, id * 4.0);
                        updateStatement.setInt(2, id);
                        count += updateStatement.executeUpdate();
                    }
                    case 3 -> {
                        deleteStatement.setInt(1, DATA_SIZE + i - 3);
                        count += deleteStatement.executeUpdate();
                    }
                }
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

