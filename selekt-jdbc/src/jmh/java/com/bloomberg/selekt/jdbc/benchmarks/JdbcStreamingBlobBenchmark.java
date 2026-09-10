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

import com.bloomberg.selekt.StreamingBlobBatch;
import com.bloomberg.selekt.StreamingBlobRow;
import com.bloomberg.selekt.jdbc.SelektConnection;
import com.bloomberg.selekt.jdbc.driver.SelektDriver;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class JdbcStreamingBlobBenchmark {
    private static final int TOTAL_PAYLOAD_BYTES = 64 * 1024 * 1024;
    private static final int TRANSFER_BUFFER_BYTES = 64 * 1024;
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE files (id INTEGER PRIMARY KEY, data BLOB NOT NULL)";
    private static final String INSERT_SQL =
        "INSERT INTO files (id, data) VALUES (?, ?)";
    private static final Driver SELEKT_DRIVER = new SelektDriver();
    private static final Driver XERIAL_DRIVER = new org.sqlite.JDBC();

    @Param({"2048", "65536", "1048576", "16777216"})
    int blobSize;

    private int rowCount;
    private byte[] payload;
    private File selektDatabaseFile;
    private File xerialDatabaseFile;
    private Connection selektConnection;
    private Connection xerialConnection;
    private SelektConnection streamingConnection;
    private StreamingBlobBatch streamingBatch;

    @Setup(Level.Trial)
    public void setUp() throws SQLException, IOException {
        rowCount = Math.max(1, TOTAL_PAYLOAD_BYTES / blobSize);
        payload = new byte[blobSize];
        new Random(42).nextBytes(payload);
        selektDatabaseFile = Files.createTempFile("selekt-streaming-blob-bench", ".db").toFile();
        xerialDatabaseFile = Files.createTempFile("xerial-streaming-blob-bench", ".db").toFile();
        final Properties selektProperties = new Properties();
        selektProperties.setProperty("poolSize", "1");
        selektProperties.setProperty("foreignKeys", "false");
        selektConnection = SELEKT_DRIVER.connect(
            "jdbc:sqlite:" + selektDatabaseFile.getAbsolutePath(), selektProperties);
        xerialConnection = XERIAL_DRIVER.connect(
            "jdbc:sqlite:" + xerialDatabaseFile.getAbsolutePath(), new Properties());
        configure(selektConnection);
        configure(xerialConnection);
        streamingConnection = selektConnection.unwrap(SelektConnection.class);
        streamingBatch = new StreamingBlobBatch(
            "files", "data", INSERT_SQL, 2, "main", TRANSFER_BUFFER_BYTES);
    }

    @Setup(Level.Invocation)
    public void clearTables() throws SQLException {
        clearTable(selektConnection);
        clearTable(xerialConnection);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws SQLException {
        close(selektConnection);
        close(xerialConnection);
        deleteDatabase(selektDatabaseFile);
        deleteDatabase(xerialDatabaseFile);
    }

    @Benchmark
    public void selektSetBytes(final Blackhole blackhole) throws SQLException {
        blackhole.consume(executeByteArrayBatch(selektConnection));
    }

    @Benchmark
    public void selektSetBinaryStream(final Blackhole blackhole) throws SQLException {
        blackhole.consume(executeStreamShapedBatch(selektConnection));
    }

    @Benchmark
    public void selektIncrementalBlobWrite(final Blackhole blackhole) throws SQLException {
        blackhole.consume(streamingConnection.insertBlobs(streamingBatch, streamingRows()));
    }

    @Benchmark
    public void xerialSetBytes(final Blackhole blackhole) throws SQLException {
        blackhole.consume(executeByteArrayBatch(xerialConnection));
    }

    @Benchmark
    public void xerialSetBinaryStream(final Blackhole blackhole) throws SQLException {
        blackhole.consume(executeStreamShapedBatch(xerialConnection));
    }

    private int[] executeByteArrayBatch(final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < rowCount; ++i) {
                statement.setInt(1, i + 1);
                statement.setBytes(2, payload);
                statement.addBatch();
            }
            final int[] result = statement.executeBatch();
            connection.commit();
            return result;
        } catch (final SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private int[] executeStreamShapedBatch(final Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            for (int i = 0; i < rowCount; ++i) {
                statement.setInt(1, i + 1);
                statement.setBinaryStream(2, new ByteArrayInputStream(payload), blobSize);
                statement.addBatch();
            }
            final int[] result = statement.executeBatch();
            connection.commit();
            return result;
        } catch (final SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private Iterable<StreamingBlobRow> streamingRows() {
        return () -> new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < rowCount;
            }

            @Override
            public StreamingBlobRow next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final long rowId = ++index;
                return new StreamingBlobRow(
                    new Object[]{rowId, null}, blobSize, new ByteArrayInputStream(payload), rowId);
            }
        };
    }

    private static void configure(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA page_size=16384");
            statement.execute("PRAGMA auto_vacuum=NONE");
            statement.execute("VACUUM");
            statement.execute("PRAGMA secure_delete=OFF");
            statement.execute("PRAGMA cache_size=-2000");
            statement.execute("PRAGMA foreign_keys=OFF");
            statement.execute("PRAGMA journal_mode=WAL");
            statement.execute("PRAGMA synchronous=NORMAL");
            statement.execute(CREATE_TABLE_SQL);
        }
        requirePragma(connection, "page_size", "16384");
        requirePragma(connection, "auto_vacuum", "0");
        requirePragma(connection, "secure_delete", "0");
        requirePragma(connection, "cache_size", "-2000");
        requirePragma(connection, "foreign_keys", "0");
        requirePragma(connection, "journal_mode", "wal");
        requirePragma(connection, "synchronous", "1");
    }

    private static void requirePragma(
        final Connection connection,
        final String name,
        final String expected
    ) throws SQLException {
        try (Statement statement = connection.createStatement();
             java.sql.ResultSet result = statement.executeQuery("PRAGMA " + name)) {
            if (!result.next()) {
                throw new SQLException("PRAGMA " + name + " returned no value");
            }
            final String actual = result.getString(1);
            if (!expected.equalsIgnoreCase(actual)) {
                throw new SQLException(
                    "PRAGMA " + name + " was " + actual + ", expected " + expected);
            }
        }
    }

    private static void clearTable(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM files");
        }
    }

    private static void close(final Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
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
