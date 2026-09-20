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

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 7, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class JdbcColumnLabelBenchmark {
    private static final int ROW_COUNT = 1_000;
    private static final Driver DRIVER = new SelektDriver();

    @Param({"4", "16", "64", "256"})
    int columnCount;

    private File databaseFile;
    private Connection connection;
    private PreparedStatement query;
    private String[] labels;

    @Setup(Level.Trial)
    public void setUp() throws IOException, SQLException {
        databaseFile = Files.createTempFile("selekt-column-label-bench", ".db").toFile();
        databaseFile.deleteOnExit();
        connection = DRIVER.connect("jdbc:sqlite:" + databaseFile.getAbsolutePath(), new Properties());
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE bench (value INTEGER NOT NULL)");
        }
        connection.setAutoCommit(false);
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO bench VALUES (?)")) {
            for (int row = 0; row < ROW_COUNT; ++row) {
                insert.setInt(1, row);
                insert.addBatch();
            }
            insert.executeBatch();
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }

        labels = new String[columnCount];
        final StringBuilder sql = new StringBuilder("SELECT ");
        for (int column = 0; column < columnCount; ++column) {
            if (column != 0) {
                sql.append(',');
            }
            labels[column] = "column_" + column;
            sql.append("value AS ").append(labels[column]);
        }
        query = connection.prepareStatement(sql.append(" FROM bench").toString());
    }

    @TearDown(Level.Trial)
    public void tearDown() throws SQLException {
        if (query != null) {
            query.close();
        }
        if (connection != null) {
            connection.close();
        }
        if (databaseFile != null) {
            databaseFile.delete();
            new File(databaseFile.getPath() + "-journal").delete();
            new File(databaseFile.getPath() + "-wal").delete();
            new File(databaseFile.getPath() + "-shm").delete();
        }
    }

    @Benchmark
    public void byIndex(final Blackhole blackhole) throws SQLException {
        try (ResultSet resultSet = query.executeQuery()) {
            while (resultSet.next()) {
                for (int column = 1; column <= columnCount; ++column) {
                    blackhole.consume(resultSet.getInt(column));
                }
            }
        }
    }

    @Benchmark
    public void byLabel(final Blackhole blackhole) throws SQLException {
        try (ResultSet resultSet = query.executeQuery()) {
            while (resultSet.next()) {
                for (int column = 0; column < columnCount; ++column) {
                    blackhole.consume(resultSet.getInt(labels[column]));
                }
            }
        }
    }
}
