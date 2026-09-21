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

package com.bloomberg.selekt.cursor.benchmarks;

import com.bloomberg.selekt.StatementHandle;
import com.bloomberg.selekt.jvm.SQLite;
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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static com.bloomberg.selekt.SQLOpenOperationsKt.SQL_OPEN_CREATE;
import static com.bloomberg.selekt.SQLOpenOperationsKt.SQL_OPEN_READWRITE;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class NativeCursorWindowFillBenchmark {
    private static final int MAX_ROWS = 1024;
    private static final int MAX_BYTES = 2 * 1024 * 1024;

    public enum ResultShape {
        ONE_INTEGER("bar"),
        EIGHT_INTEGERS(
                "bar, bar + 1, bar + 2, bar + 3, bar + 4, bar + 5, bar + 6, bar + 7"),
        SHORT_TEXT("'row-' || bar || '-value'"),
        LARGE_BLOB("zeroblob(1024)");

        private final String projection;

        ResultShape(String projection) {
            this.projection = projection;
        }
    }

    @Param({"0", "1", "50", "1000", "50000"})
    public int rowCount;

    @Param({"ONE_INTEGER", "EIGHT_INTEGERS", "SHORT_TEXT", "LARGE_BLOB"})
    public ResultShape resultShape;

    private File databaseFile;
    private long db;
    private StatementHandle statement;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        databaseFile = Files.createTempFile("benchmark-native-cursor-window", ".db").toFile();
        long[] dbHolder = new long[1];
        SQLite.INSTANCE.openV2(
                databaseFile.getAbsolutePath(), SQL_OPEN_READWRITE | SQL_OPEN_CREATE, dbHolder);
        db = dbHolder[0];
        SQLite.INSTANCE.exec(db, "CREATE TABLE Foo (bar INT)");
        if (rowCount > 0) {
            SQLite.INSTANCE.exec(
                    db,
                    "WITH RECURSIVE rows(value) AS (" +
                            "VALUES(0) UNION ALL SELECT value + 1 FROM rows WHERE value + 1 < " +
                            rowCount +
                            ") INSERT INTO Foo SELECT value FROM rows");
        }
        long[] statementHolder = new long[1];
        SQLite.INSTANCE.prepareV2(db, "SELECT " + resultShape.projection + " FROM Foo", statementHolder);
        statement = SQLite.INSTANCE.newStatementHandle(statementHolder[0]);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        SQLite.INSTANCE.finalize(statement);
        SQLite.INSTANCE.closeV2(db);
        Files.deleteIfExists(databaseFile.toPath());
    }

    @Benchmark
    public int fillBoundedWindow() throws IOException {
        SQLite.INSTANCE.reset(statement);
        ByteBuffer buffer = SQLite.INSTANCE.fillCursorWindow(statement, 0, MAX_ROWS, false, MAX_BYTES);
        try {
            return buffer.order(ByteOrder.nativeOrder()).getInt(0);
        } finally {
            SQLite.INSTANCE.freeCursorWindow(buffer);
        }
    }
}
