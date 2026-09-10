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
public class JdbcScrollableCursorBenchmark {
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE bench (id INTEGER PRIMARY KEY, name TEXT, value REAL, category TEXT)";
    private static final String INSERT_SQL =
        "INSERT INTO bench (id, name, value, category) VALUES (?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL =
        "SELECT id, name, value, category FROM bench ORDER BY id";
    private static final String[] CATEGORIES = {"alpha", "beta", "gamma", "delta", "epsilon"};
    private static final Driver SELEKT_DRIVER = new SelektDriver();

    @Param({"1024", "10240", "51200", "102400"})
    int rowCount;

    @Param({"256", "1024", "8192", "2147483647"})
    int cursorWindowSize;

    private File databaseFile;
    private Connection connection;

    @Setup(Level.Trial)
    public void setUp() throws SQLException, IOException {
        databaseFile = Files.createTempFile("selekt-scroll-bench", ".db").toFile();
        databaseFile.deleteOnExit();
        final Properties properties = new Properties();
        properties.setProperty("cursorWindowSize", Integer.toString(cursorWindowSize));
        connection = SELEKT_DRIVER.connect("jdbc:sqlite:" + databaseFile.getAbsolutePath(), properties);
        initializeDatabase();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        deleteDatabase(databaseFile);
    }

    @Benchmark
    public void fullScan(final Blackhole blackhole) throws SQLException {
        try (Statement statement = connection.createStatement(
                 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL)) {
            while (resultSet.next()) {
                blackhole.consume(resultSet.getInt(1));
                blackhole.consume(resultSet.getString(2));
                blackhole.consume(resultSet.getDouble(3));
                blackhole.consume(resultSet.getString(4));
            }
        }
    }

    private void initializeDatabase() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        }
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < rowCount; i++) {
                statement.setInt(1, i);
                statement.setString(2, "name_" + i);
                statement.setDouble(3, i * 1.5);
                statement.setString(4, CATEGORIES[i % CATEGORIES.length]);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (final SQLException exception) {
            connection.rollback();
            throw exception;
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
