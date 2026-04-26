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
public class JdbcForwardOnlyBenchmark {
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS bench (id INTEGER PRIMARY KEY, name TEXT, value REAL, category TEXT)";
    private static final String INSERT_SQL =
        "INSERT OR IGNORE INTO bench (id, name, value, category) VALUES (?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT id, name, value, category FROM bench";
    private static final String SELECT_BY_ID_SQL = "SELECT id, name, value, category FROM bench WHERE id = ?";

    private static final int DATA_SIZE = 10_000;
    private static final String[] CATEGORIES = {"alpha", "beta", "gamma", "delta", "epsilon"};
    private static final Driver SELEKT_DRIVER = new SelektDriver();
    private static final Driver XERIAL_DRIVER = new org.sqlite.JDBC();

    @Param({"1", "100", "1000", "10000"})
    int rowCount;

    private File selektDatabaseFile;
    private File xerialDatabaseFile;
    private Connection selektConnection;
    private Connection xerialConnection;
    private PreparedStatement selektForwardOnlyStatement;
    private PreparedStatement selektScrollInsensitiveStatement;
    private PreparedStatement xerialForwardOnlyStatement;

    @Setup(Level.Iteration)
    public void setUp() throws SQLException, IOException {
        selektDatabaseFile = Files.createTempFile("selekt-fwd-bench", ".db").toFile();
        selektDatabaseFile.deleteOnExit();
        selektConnection = SELEKT_DRIVER.connect(
            "jdbc:sqlite:" + selektDatabaseFile.getAbsolutePath(), new Properties());
        initializeDatabase(selektConnection);

        xerialDatabaseFile = Files.createTempFile("xerial-fwd-bench", ".db").toFile();
        xerialDatabaseFile.deleteOnExit();
        xerialConnection = XERIAL_DRIVER.connect(
            "jdbc:sqlite:" + xerialDatabaseFile.getAbsolutePath(), new Properties());
        initializeDatabase(xerialConnection);

        selektForwardOnlyStatement = selektConnection.prepareStatement(
            SELECT_BY_ID_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        selektScrollInsensitiveStatement = selektConnection.prepareStatement(
            SELECT_BY_ID_SQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        xerialForwardOnlyStatement = xerialConnection.prepareStatement(
            SELECT_BY_ID_SQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws SQLException {
        closeQuietly(selektForwardOnlyStatement);
        closeQuietly(selektScrollInsensitiveStatement);
        closeQuietly(xerialForwardOnlyStatement);
        if (selektConnection != null && !selektConnection.isClosed()) {
            selektConnection.close();
        }
        if (xerialConnection != null && !xerialConnection.isClosed()) {
            xerialConnection.close();
        }
        deleteDatabase(selektDatabaseFile);
        deleteDatabase(xerialDatabaseFile);
    }

    @Benchmark
    public void selektForwardOnlyFullScan(final Blackhole blackhole) throws SQLException {
        fullScan(selektConnection, ResultSet.TYPE_FORWARD_ONLY, blackhole);
    }

    @Benchmark
    public void xerialForwardOnlyFullScan(final Blackhole blackhole) throws SQLException {
        fullScan(xerialConnection, ResultSet.TYPE_FORWARD_ONLY, blackhole);
    }

    @Benchmark
    public void selektScrollInsensitiveFullScan(final Blackhole blackhole) throws SQLException {
        fullScan(selektConnection, ResultSet.TYPE_SCROLL_INSENSITIVE, blackhole);
    }

    @Benchmark
    public void selektForwardOnlyPointQuery(final Blackhole blackhole) throws SQLException {
        pointQuery(selektConnection, ResultSet.TYPE_FORWARD_ONLY, blackhole);
    }

    @Benchmark
    public void selektScrollInsensitivePointQuery(final Blackhole blackhole) throws SQLException {
        pointQuery(selektConnection, ResultSet.TYPE_SCROLL_INSENSITIVE, blackhole);
    }

    @Benchmark
    public void xerialForwardOnlyPointQuery(final Blackhole blackhole) throws SQLException {
        pointQuery(xerialConnection, ResultSet.TYPE_FORWARD_ONLY, blackhole);
    }

    @Benchmark
    public void selektForwardOnlyPointQueryReuse(final Blackhole blackhole) throws SQLException {
        pointQueryReuse(selektForwardOnlyStatement, blackhole);
    }

    @Benchmark
    public void selektScrollInsensitivePointQueryReuse(final Blackhole blackhole) throws SQLException {
        pointQueryReuse(selektScrollInsensitiveStatement, blackhole);
    }

    @Benchmark
    public void xerialForwardOnlyPointQueryReuse(final Blackhole blackhole) throws SQLException {
        pointQueryReuse(xerialForwardOnlyStatement, blackhole);
    }

    private void fullScan(final Connection conn, final int resultSetType,
            final Blackhole blackhole) throws SQLException {
        final String sql = SELECT_ALL_SQL + " LIMIT " + rowCount;
        try (Statement stmt = conn.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
             ResultSet rs = stmt.executeQuery(sql)) {
            consumeAll(rs, blackhole);
        }
    }

    private void pointQuery(final Connection conn, final int resultSetType,
            final Blackhole blackhole) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
            SELECT_BY_ID_SQL, resultSetType, ResultSet.CONCUR_READ_ONLY)
        ) {
            for (int i = 0; i < rowCount; i++) {
                ps.setInt(1, i % DATA_SIZE);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        blackhole.consume(rs.getInt(1));
                        blackhole.consume(rs.getString(2));
                        blackhole.consume(rs.getDouble(3));
                        blackhole.consume(rs.getString(4));
                    }
                }
            }
        }
    }

    private void pointQueryReuse(final PreparedStatement ps,
            final Blackhole blackhole) throws SQLException {
        for (int i = 0; i < rowCount; i++) {
            ps.setInt(1, i % DATA_SIZE);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    blackhole.consume(rs.getInt(1));
                    blackhole.consume(rs.getString(2));
                    blackhole.consume(rs.getDouble(3));
                    blackhole.consume(rs.getString(4));
                }
            }
        }
    }

    private void initializeDatabase(final Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute(CREATE_TABLE_SQL);
        }
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < DATA_SIZE; i++) {
                ps.setInt(1, i);
                ps.setString(2, "name_" + i);
                ps.setDouble(3, i * 1.5);
                ps.setString(4, CATEGORIES[i % CATEGORIES.length]);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (final SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void consumeAll(final ResultSet rs, final Blackhole blackhole) throws SQLException {
        while (rs.next()) {
            blackhole.consume(rs.getInt(1));
            blackhole.consume(rs.getString(2));
            blackhole.consume(rs.getDouble(3));
            blackhole.consume(rs.getString(4));
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

    private static void closeQuietly(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception ignored) { }
        }
    }
}

