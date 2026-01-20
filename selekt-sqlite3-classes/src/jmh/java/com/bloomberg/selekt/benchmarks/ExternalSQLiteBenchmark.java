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

package com.bloomberg.selekt.benchmarks;

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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class ExternalSQLiteBenchmark {
    private static final com.bloomberg.selekt.IExternalSQLite sqlite = com.bloomberg.selekt.ExternalSQLiteKt.externalSQLiteSingleton();
    private File tempDir;
    private long db;

    @Setup(Level.Iteration)
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("selekt_benchmark_").toFile();
        final String dbPath = new File(tempDir, "benchmark.db").getAbsolutePath();
        long[] dbHolder = new long[1];
        sqlite.openV2(dbPath, 0x00000006, dbHolder);
        db = dbHolder[0];
        long[] statementHolder = new long[1];
        final String createSql = "CREATE TABLE test (id INTEGER PRIMARY KEY, name TEXT)";
        sqlite.prepareV2(db, createSql, createSql.length(), statementHolder);
        final long statement = statementHolder[0];
        sqlite.step(statement);
        sqlite.finalize(statement);
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        if (db != 0L) {
            sqlite.closeV2(db);
        }
        if (tempDir != null) {
            deleteRecursively(tempDir);
        }
    }

    private static void deleteRecursively(final File file) {
        final File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                deleteRecursively(f);
            }
        }
        file.delete();
    }

    @Benchmark
    public int openAndCloseDatabase() {
        final String dbPath = new File(tempDir, "temp_" + System.nanoTime() + ".db").getAbsolutePath();
        final long[] dbHolder = new long[1];

        final int openResult = sqlite.openV2(dbPath, 0x00000006, dbHolder);
        final int closeResult = sqlite.closeV2(dbHolder[0]);

        return openResult + closeResult;
    }

    @Benchmark
    public int prepareAndFinalizeStatement() {
        final long[] stmtHolder = new long[1];
        final String sql = "SELECT 1";

        final int prepareResult = sqlite.prepareV2(db, sql, sql.length(), stmtHolder);
        final int finalizeResult = sqlite.finalize(stmtHolder[0]);

        return prepareResult + finalizeResult;
    }

    @Benchmark
    public int simpleQuery() {
        final long[] statementHolder = new long[1];
        final String sql = "SELECT 42";

        sqlite.prepareV2(db, sql, sql.length(), statementHolder);
        final long statement = statementHolder[0];

        final int stepResult = sqlite.step(statement);
        final int value = sqlite.columnInt(statement, 0);
        final int finalizeResult = sqlite.finalize(statement);

        return stepResult + value + finalizeResult;
    }

    @Benchmark
    public String bindAndQueryText() {
        final long[] stmtHolder = new long[1];
        final String sql = "SELECT ?";

        sqlite.prepareV2(db, sql, sql.length(), stmtHolder);
        final long statement = stmtHolder[0];

        sqlite.bindText(statement, 1, "Benchmark string for FFM vs JNI comparison");
        sqlite.step(statement);
        final String result = sqlite.columnText(statement, 0);
        sqlite.finalize(statement);

        return result;
    }

    @Benchmark
    public int insertRow() {
        final long[] statementHolder = new long[1];
        final String sql = "INSERT INTO test (name) VALUES (?)";

        sqlite.prepareV2(db, sql, sql.length(), statementHolder);
        final long statement = statementHolder[0];

        sqlite.bindText(statement, 1, "Test Name");
        final int stepResult = sqlite.step(statement);
        final int finalizeResult = sqlite.finalize(statement);

        return stepResult + finalizeResult;
    }

    @Benchmark
    public int bulkInsert10Rows() {
        final long[] statementHolder = new long[1];
        final String sql = "INSERT INTO test (name) VALUES (?)";

        sqlite.prepareV2(db, sql, sql.length(), statementHolder);
        final long statement = statementHolder[0];

        int totalResult = 0;
        for (int i = 0; i < 10; i++) {
            sqlite.bindText(statement, 1, "Bulk insert row " + i);
            totalResult += sqlite.step(statement);
            sqlite.reset(statement);
        }
        totalResult += sqlite.finalize(statement);
        return totalResult;
    }

    @Benchmark
    public int selectMultipleRows() {
        long[] insertStatementHolder = new long[1];
        final String insertSql = "INSERT INTO test (name) VALUES (?)";
        sqlite.prepareV2(db, insertSql, insertSql.length(), insertStatementHolder);
        final long insertStatement = insertStatementHolder[0];

        for (int i = 0; i < 5; i++) {
            sqlite.bindText(insertStatement, 1, "Row " + i);
            sqlite.step(insertStatement);
            sqlite.reset(insertStatement);
        }
        sqlite.finalize(insertStatement);

        final long[] selectStmtHolder = new long[1];
        final String selectSql = "SELECT id, name FROM test LIMIT 5";
        sqlite.prepareV2(db, selectSql, selectSql.length(), selectStmtHolder);
        long selectStatement = selectStmtHolder[0];

        int count = 0;
        while (sqlite.step(selectStatement) == com.bloomberg.selekt.SQLCodesKt.SQL_ROW) {
            sqlite.columnInt(selectStatement, 0);
            sqlite.columnText(selectStatement, 1);
            count++;
        }

        sqlite.finalize(selectStatement);
        return count;
    }

    @Benchmark
    public int libVersionNumber() {
        return sqlite.libVersionNumber();
    }

    @Benchmark
    public String libVersion() {
        return sqlite.libVersion();
    }

    @Benchmark
    public int bindAndQueryInteger() {
        final long[] statementHolder = new long[1];
        final String sql = "SELECT ?";

        sqlite.prepareV2(db, sql, sql.length(), statementHolder);
        final long statement = statementHolder[0];

        sqlite.bindInt64(statement, 1, 123456789L);
        sqlite.step(statement);
        final long result = sqlite.columnInt64(statement, 0);
        sqlite.finalize(statement);

        return (int) result;
    }

    @Benchmark
    public double bindAndQueryDouble() {
        final long[] statementHolder = new long[1];
        final String sql = "SELECT ?";

        sqlite.prepareV2(db, sql, sql.length(), statementHolder);
        final long stmt = statementHolder[0];

        sqlite.bindDouble(stmt, 1, 3.141592653589793);
        sqlite.step(stmt);
        final double result = sqlite.columnDouble(stmt, 0);
        sqlite.finalize(stmt);

        return result;
    }

    @Benchmark
    public byte[] bindAndQueryBlob() {
        final long[] statementHolder = new long[1];
        final String sql = "SELECT ?";

        sqlite.prepareV2(db, sql, sql.length(), statementHolder);
        final long statement = statementHolder[0];

        final byte[] testBlob = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        sqlite.bindBlob(statement, 1, testBlob, testBlob.length);
        sqlite.step(statement);
        final byte[] result = sqlite.columnBlob(statement, 0);
        sqlite.finalize(statement);

        return result;
    }
}
