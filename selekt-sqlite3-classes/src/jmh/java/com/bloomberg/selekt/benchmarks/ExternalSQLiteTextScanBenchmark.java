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

import com.bloomberg.selekt.IExternalSQLite;
import com.bloomberg.selekt.StatementHandle;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@SuppressWarnings("deprecation")
public class ExternalSQLiteTextScanBenchmark {
    private static final int ROW_COUNT = 1_000;
    private static final int SQL_OPEN_READWRITE_OR_CREATE = 0x00000006;
    private static final int SQL_ROW = 100;
    private static final IExternalSQLite SQLITE = com.bloomberg.selekt.ExternalSQLiteKt.externalSQLiteSingleton();

    public enum TextKind {
        ASCII,
        UTF8,
        EMBEDDED_NUL,
        MIXED
    }

    @Param({"2", "4", "8"})
    public int columnCount;

    @Param({"16", "64"})
    public int textLength;

    @Param({"ASCII", "UTF8", "EMBEDDED_NUL", "MIXED"})
    public TextKind textKind;

    private long database;
    private StatementHandle statement;
    private String[] destination;

    @Setup(Level.Trial)
    public void setUp() {
        final long[] databaseHolder = new long[1];
        SQLITE.openV2(":memory:", SQL_OPEN_READWRITE_OR_CREATE, databaseHolder);
        database = databaseHolder[0];

        final String projection = String.join(",", Collections.nCopies(columnCount, "?"));
        final String sql = "WITH RECURSIVE rows(row_number) AS (VALUES(1) " +
            "UNION ALL SELECT row_number + 1 FROM rows WHERE row_number < " + ROW_COUNT + ") SELECT " +
            projection + " FROM rows";
        final long[] statementHolder = new long[1];
        SQLITE.prepareV2(database, sql, sql.length(), statementHolder);
        statement = SQLITE.newStatementHandle(statementHolder[0]);
        for (int index = 1; index <= columnCount; ++index) {
            SQLITE.bindText(statement, index, text(index));
        }
        destination = new String[columnCount];
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        SQLITE.finalize(statement);
        SQLITE.closeV2(database);
    }

    @Benchmark
    @OperationsPerInvocation(ROW_COUNT)
    public int scanIndependently() {
        return scan(0);
    }

    @Benchmark
    @OperationsPerInvocation(ROW_COUNT)
    public int scanBatch() {
        return scan(1);
    }

    @Benchmark
    @OperationsPerInvocation(ROW_COUNT)
    public int scanDefaultBatch() {
        return scan(2);
    }

    private int scan(final int strategy) {
        SQLITE.reset(statement);
        int hash = 1;
        while (SQLITE.step(statement) == SQL_ROW) {
            if (strategy == 1) {
                SQLITE.columnTexts(statement, 0, destination);
            } else if (strategy == 2) {
                IExternalSQLite.DefaultImpls.columnTexts(SQLITE, statement, 0, destination);
            } else {
                for (int index = 0; index < columnCount; ++index) {
                    destination[index] = SQLITE.columnText(statement, index);
                }
            }
            hash = 31 * hash + Arrays.hashCode(destination);
        }
        return hash;
    }

    private String text(final int columnIndex) {
        switch (textKind) {
            case UTF8:
                return "x".repeat(textLength - 2) + "é";
            case EMBEDDED_NUL:
                final int prefixLength = (textLength - 1) / 2;
                return "x".repeat(prefixLength) + '\0' + "x".repeat(textLength - prefixLength - 1);
            case MIXED:
                return columnIndex <= columnCount / 2 ?
                    "x".repeat(textLength) :
                    "x".repeat(textLength - 2) + "é";
            default:
                return "x".repeat(textLength);
        }
    }
}
