/*
 * Copyright 2024 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.database.benchmarks;

import com.bloomberg.selekt.SQLDatabase;
import com.bloomberg.selekt.ICursor;
import com.bloomberg.selekt.ISQLStatement;
import com.bloomberg.selekt.SQLiteJournalMode;
import com.bloomberg.selekt.commons.DatabaseKt;
import com.bloomberg.selekt.jvm.SQLiteDatabaseKt;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class JDBCBenchmark {
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS batch_test " +
        "(id INTEGER PRIMARY KEY, name TEXT UNIQUE, value INTEGER)";
    private static final String INSERT_SQL = "INSERT OR IGNORE INTO batch_test (name, value) VALUES (?, ?)";
    private static final String SELECT_SQL = "SELECT id, value FROM batch_test WHERE name = ?";

    @Param({"10", "100", "1000", "10000"})
    int batchSize;

    private File selektDatabaseFile;
    private File xerialDatabaseFile;

    private SQLDatabase selektDatabase;
    private ISQLStatement selektInsertStatement;
    private ISQLStatement selektSelectStatement;

    private Connection xerialConnection;
    private PreparedStatement xerialInsertStatement;
    private PreparedStatement xerialSelectStatement;

    private Object[][] selektBatchArgs;
    private String[] batchNames;
    private int[] batchValues;

    @Setup(Level.Iteration)
    public void setUp() throws SQLException, IOException {
        setupSelekt();
        setupXerial();
        prepopulateBindArgs();
    }

    @TearDown(Level.Iteration)
    public void tearDown() throws SQLException {
        try {
            if (selektInsertStatement != null) {
                selektInsertStatement.close();
            }
            if (selektSelectStatement != null) {
                selektSelectStatement.close();
            }
            if (selektDatabase != null) {
                selektDatabase.close();
            }
        } catch (final Exception e) {
            // Ignore.
        }

        if (xerialInsertStatement != null) {
            xerialInsertStatement.close();
        }
        if (xerialSelectStatement != null) {
            xerialSelectStatement.close();
        }
        if (xerialConnection != null) {
            xerialConnection.close();
        }

        if (selektDatabaseFile != null) {
            DatabaseKt.deleteDatabase(selektDatabaseFile);
        }
        if (xerialDatabaseFile != null) {
            DatabaseKt.deleteDatabase(xerialDatabaseFile);
        }
    }

    private void setupSelekt() throws IOException {
        selektDatabaseFile = File.createTempFile("selekt", ".db");
        selektDatabaseFile.deleteOnExit();
        selektDatabase = SQLiteDatabaseKt.openOrCreateDatabase(
            selektDatabaseFile,
            SQLiteJournalMode.WAL.databaseConfiguration,
            null
        );
        selektDatabase.exec(CREATE_TABLE_SQL, null);
        selektInsertStatement = selektDatabase.compileStatement(INSERT_SQL, null);
        selektSelectStatement = selektDatabase.compileStatement(SELECT_SQL, null);
        populateSelektData();
    }

    private void setupXerial() throws SQLException, IOException {
        xerialDatabaseFile = File.createTempFile("xerial", ".db");
        xerialDatabaseFile.deleteOnExit();
        xerialConnection = DriverManager.getConnection("jdbc:sqlite:" + xerialDatabaseFile.getAbsolutePath());
        try (Statement statement = xerialConnection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        }
        xerialInsertStatement = xerialConnection.prepareStatement(INSERT_SQL);
        xerialSelectStatement = xerialConnection.prepareStatement(SELECT_SQL);
        populateXerialData();
    }

    private void prepopulateBindArgs() {
        final int maxBatchSize = 10000;
        selektBatchArgs = new Object[maxBatchSize][2];
        for (int i = 0; i < maxBatchSize; i++) {
            selektBatchArgs[i][0] = "name_" + i;
            selektBatchArgs[i][1] = i;
        }
        batchNames = new String[maxBatchSize];
        batchValues = new int[maxBatchSize];
        for (int i = 0; i < maxBatchSize; i++) {
            batchNames[i] = "name_" + i;
            batchValues[i] = i;
        }
    }

    private void populateSelektData() {
        final Iterable<Object[]> initialData = () -> new Iterator<>() {
            private int index = 0;
            private final int dataSize = 10000;

            @Override
            public boolean hasNext() {
                return index < dataSize;
            }

            @Override
            public Object[] next() {
                if (index >= dataSize) {
                    throw new NoSuchElementException();
                }
                return new Object[]{"name_" + index, index++};
            }
        };
        selektDatabase.batch(INSERT_SQL, initialData);
    }

    private void populateXerialData() throws SQLException {
        xerialConnection.setAutoCommit(false);
        try {
            for (int i = 0; i < 10000; i++) {
                xerialInsertStatement.setString(1, "name_" + i);
                xerialInsertStatement.setInt(2, i);
                xerialInsertStatement.addBatch();
            }
            xerialInsertStatement.executeBatch();
            xerialConnection.commit();
        } catch (final SQLException e) {
            xerialConnection.rollback();
            throw e;
        } finally {
            xerialConnection.setAutoCommit(true);
        }
    }

    @Benchmark
    public void selektBatch() {
        selektDatabase.batch(INSERT_SQL, Arrays.stream(selektBatchArgs));
    }

    @Benchmark
    public void xerialBatch() throws SQLException {
        xerialConnection.setAutoCommit(false);
        try {
            for (int i = 0; i < batchSize; i++) {
                xerialInsertStatement.setString(1, batchNames[i]);
                xerialInsertStatement.setInt(2, batchValues[i]);
                xerialInsertStatement.addBatch();
            }
            xerialInsertStatement.executeBatch();
            xerialConnection.commit();
        } catch (final SQLException e) {
            xerialConnection.rollback();
            throw e;
        } finally {
            xerialConnection.setAutoCommit(true);
        }
    }

    @Benchmark
    public void selektSelect() throws IOException {
        for (int i = 0; i < batchSize; i++) {
            try (ICursor cursor = selektDatabase.query(SELECT_SQL, new String[]{batchNames[i]})) {
                if (cursor.moveToNext()) {
                    cursor.getLong(0);
                    cursor.getInt(1);
                }
            }
        }
    }

    @Benchmark
    public void xerialSelect() throws SQLException {
        for (int i = 0; i < batchSize; i++) {
            xerialSelectStatement.setString(1, batchNames[i]);
            try (ResultSet results = xerialSelectStatement.executeQuery()) {
                if (results.next()) {
                    results.getLong("id");
                    results.getInt("value");
                }
            }
        }
    }
}
