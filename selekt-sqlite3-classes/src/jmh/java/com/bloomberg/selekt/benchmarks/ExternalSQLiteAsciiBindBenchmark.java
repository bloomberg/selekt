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
import kotlin.jvm.functions.Function0;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 1, jvmArgsAppend = {"-Xms2g", "-Xmx2g"})
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 4, time = 500, timeUnit = TimeUnit.MILLISECONDS)
public class ExternalSQLiteAsciiBindBenchmark {
    private static final int BINDS_PER_OPERATION = 1_000;
    private static final int SQL_OPEN_READWRITE_OR_CREATE = 0x00000006;
    private static final IExternalSQLite SQLITE = com.bloomberg.selekt.ExternalSQLiteKt.externalSQLiteSingleton();

    @Param({"8", "32", "40", "48", "56", "64", "72", "80", "96", "128", "256", "512", "1024", "2048", "4096"})
    int textLength;

    private File databaseFile;
    private long database;
    private long statement;
    private Function0<Integer> bindAsciiBatch;
    private Function0<Integer> bindUtf8Batch;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        databaseFile = Files.createTempFile("selekt-ascii-bind", ".db").toFile();
        final long[] databaseHolder = new long[1];
        SQLITE.openV2(databaseFile.getAbsolutePath(), SQL_OPEN_READWRITE_OR_CREATE, databaseHolder);
        database = databaseHolder[0];
        final String sql = "SELECT ?";
        final long[] statementHolder = new long[1];
        SQLITE.prepareV2(database, sql, sql.length(), statementHolder);
        statement = statementHolder[0];
        final String text = "x".repeat(textLength);
        bindAsciiBatch = bindBatch(text, true);
        bindUtf8Batch = bindBatch(text, false);
    }

    private Function0<Integer> bindBatch(String text, boolean ascii) {
        return () -> {
            int result = 0;
            for (int i = 0; i < BINDS_PER_OPERATION; i++) {
                result |= ascii
                    ? SQLITE.bindTextAscii(statement, 1, text)
                    : SQLITE.bindText(statement, 1, text);
            }
            return result;
        };
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (statement != 0L) {
            SQLITE.finalize(statement);
        }
        if (database != 0L) {
            SQLITE.closeV2(database);
        }
        if (databaseFile != null) {
            databaseFile.delete();
        }
    }

    @Benchmark
    public int bindAsciiBatch() {
        return SQLITE.withScopedArena(bindAsciiBatch);
    }

    @Benchmark
    public int bindUtf8Batch() {
        return SQLITE.withScopedArena(bindUtf8Batch);
    }
}
