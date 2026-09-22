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
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class ExternalSQLiteTextReadBenchmark {
    public enum TextKind {
        ASCII,
        UTF8,
        EMBEDDED_NUL
    }

    private static final IExternalSQLite SQLITE = com.bloomberg.selekt.ExternalSQLiteKt.externalSQLiteSingleton();

    @Param({"0", "8", "15", "16", "32", "64", "128", "256", "1024"})
    public int textLength;

    @Param({"ASCII", "UTF8", "EMBEDDED_NUL"})
    public TextKind textKind;

    private long database;
    private long statement;
    private StatementHandle statementHandle;

    @Setup(Level.Trial)
    public void setUp() {
        final long[] databaseHolder = new long[1];
        SQLITE.openV2(":memory:", 0x00000006, databaseHolder);
        database = databaseHolder[0];

        final long[] statementHolder = new long[1];
        final String sql = "SELECT ?";
        SQLITE.prepareV2(database, sql, sql.length(), statementHolder);
        statement = statementHolder[0];
        statementHandle = SQLITE.newStatementHandle(statement);
        SQLITE.bindText(statement, 1, text());
        SQLITE.step(statement);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        SQLITE.finalize(statementHandle);
        SQLITE.closeV2(database);
    }

    @Benchmark
    public String readText() {
        return SQLITE.columnText(statementHandle, 0);
    }

    private String text() {
        switch (textKind) {
            case UTF8:
                return textLength == 0 ? "" : "x".repeat(textLength - 2) + "é";
            case EMBEDDED_NUL:
                if (textLength == 0) {
                    return "";
                }
                final int prefixLength = (textLength - 1) / 2;
                return "x".repeat(prefixLength) + '\0' + "x".repeat(textLength - prefixLength - 1);
            default:
                return "x".repeat(textLength);
        }
    }
}
