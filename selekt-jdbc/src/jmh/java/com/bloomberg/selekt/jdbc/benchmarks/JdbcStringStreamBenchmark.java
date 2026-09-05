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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class JdbcStringStreamBenchmark {
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE stream_bench (id INTEGER PRIMARY KEY, value TEXT NOT NULL)";
    private static final String INSERT_SQL = "INSERT INTO stream_bench (id, value) VALUES (?, ?)";
    private static final String SELECT_SQL = "SELECT value FROM stream_bench ORDER BY id";

    public enum DriverKind {
        SELEKT(new SelektDriver()),
        XERIAL(new org.sqlite.JDBC());

        private final Driver driver;

        DriverKind(final Driver driver) {
            this.driver = driver;
        }
    }

    public enum StringEncoding {
        ASCII("-a-bb"),
        LATIN1("-é-ññ"),
        UTF16("-€-😀");

        private final String suffix;

        StringEncoding(final String suffix) {
            this.suffix = suffix;
        }
    }

    @Param({"SELEKT", "XERIAL"})
    public DriverKind driverKind;

    @Param({"1", "1000", "50000"})
    public int rowCount;

    @Param({"ASCII", "LATIN1", "UTF16"})
    public StringEncoding stringEncoding;

    private File databaseFile;
    private Connection connection;
    private PreparedStatement selectStatement;
    private final byte[] byteBuffer = new byte[256];
    private final char[] characterBuffer = new char[256];

    @Setup(Level.Trial)
    public void setUp() throws IOException, SQLException {
        databaseFile = Files.createTempFile("selekt-jdbc-stream-benchmark", ".db").toFile();
        connection = driverKind.driver.connect(
            "jdbc:sqlite:" + databaseFile.getAbsolutePath(), new Properties());
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        }
        connection.setAutoCommit(false);
        try (PreparedStatement insertStatement = connection.prepareStatement(INSERT_SQL)) {
            for (int row = 0; row < rowCount; row++) {
                insertStatement.setInt(1, row);
                insertStatement.setString(2, "row-" + row + stringEncoding.suffix);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
        selectStatement = connection.prepareStatement(SELECT_SQL);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws SQLException {
        if (selectStatement != null) {
            selectStatement.close();
        }
        if (connection != null) {
            connection.close();
        }
        deleteDatabase(databaseFile);
    }

    @Benchmark
    public void createString(final Blackhole blackhole) throws SQLException {
        try (ResultSet resultSet = selectStatement.executeQuery()) {
            while (resultSet.next()) {
                blackhole.consume(resultSet.getString(1));
            }
        }
    }

    @Benchmark
    public long readString() throws SQLException {
        long characters = 0;
        try (ResultSet resultSet = selectStatement.executeQuery()) {
            while (resultSet.next()) {
                characters += resultSet.getString(1).length();
            }
        }
        return characters;
    }

    @Benchmark
    public void createAsciiStream(final Blackhole blackhole) throws SQLException {
        try (ResultSet resultSet = selectStatement.executeQuery()) {
            while (resultSet.next()) {
                blackhole.consume(resultSet.getAsciiStream(1));
            }
        }
    }

    @Benchmark
    public long readAsciiStream() throws IOException, SQLException {
        long bytes = 0;
        try (ResultSet resultSet = selectStatement.executeQuery()) {
            while (resultSet.next()) {
                try (InputStream stream = resultSet.getAsciiStream(1)) {
                    int read;
                    while ((read = stream.read(byteBuffer)) != -1) {
                        bytes += read;
                    }
                }
            }
        }
        return bytes;
    }

    @Benchmark
    public void createCharacterStream(final Blackhole blackhole) throws SQLException {
        try (ResultSet resultSet = selectStatement.executeQuery()) {
            while (resultSet.next()) {
                blackhole.consume(resultSet.getCharacterStream(1));
            }
        }
    }

    @Benchmark
    public long readCharacterStream() throws IOException, SQLException {
        long characters = 0;
        try (ResultSet resultSet = selectStatement.executeQuery()) {
            while (resultSet.next()) {
                try (Reader reader = resultSet.getCharacterStream(1)) {
                    int read;
                    while ((read = reader.read(characterBuffer)) != -1) {
                        characters += read;
                    }
                }
            }
        }
        return characters;
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
