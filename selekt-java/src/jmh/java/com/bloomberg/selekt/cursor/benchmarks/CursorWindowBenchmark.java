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

import com.bloomberg.selekt.CursorWindowPage;
import com.bloomberg.selekt.ICursor;
import com.bloomberg.selekt.ICursorWindow;
import com.bloomberg.selekt.NativeCursorWindow;
import com.bloomberg.selekt.SimpleCursorWindow;
import com.bloomberg.selekt.WindowedCursor;
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
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static com.bloomberg.selekt.SQLCodesKt.SQL_ROW;
import static com.bloomberg.selekt.SQLOpenOperationsKt.SQL_OPEN_CREATE;
import static com.bloomberg.selekt.SQLOpenOperationsKt.SQL_OPEN_READWRITE;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class CursorWindowBenchmark {
    private static final int MULTI_COLUMN_COUNT = 8;
    private static final int SOME_STRING_READ_COUNT = 10;

    public enum StringEncoding {
        ASCII("SELECT 'row-' || bar || '-a-bb' FROM Foo"),
        LATIN1("SELECT 'row-' || bar || '-é-ññ' FROM Foo"),
        UTF16("SELECT 'row-' || bar || '-€-😀' FROM Foo");

        private final String query;

        StringEncoding(String query) {
            this.query = query;
        }
    }

    @State(Scope.Thread)
    public static class StringState {
        @Param({"ASCII", "LATIN1", "UTF16"})
        public StringEncoding stringEncoding;

        private long statement;
        private ICursor cursor;
        private ICursorWindow window;

        @Setup(Level.Trial)
        public void setUp(CursorWindowBenchmark benchmark) throws IOException {
            long[] statementHolder = new long[1];
            SQLite.INSTANCE.prepareV2(benchmark.db, stringEncoding.query, statementHolder);
            statement = statementHolder[0];
            window = benchmark.cursorFactory.fillStrings(statement, SQLite.INSTANCE);
            cursor = stringCursor(window);
        }

        @TearDown(Level.Trial)
        public void tearDown() throws IOException {
            cursor.close();
            SQLite.INSTANCE.finalize(statement);
        }
    }

    private interface CursorFactory {
        ICursorWindow fill(long statement, SQLite sqlite, int columnCount);

        default ICursorWindow fill(long statement, SQLite sqlite) {
            return fill(statement, sqlite, 1);
        }

        ICursorWindow fillStrings(long statement, SQLite sqlite);
    }

    public enum CursorWindowKind implements CursorFactory {
        NATIVE {
            @Override
            public ICursorWindow fill(long statement, SQLite sqlite, int columnCount) {
                sqlite.reset(statement);
                return new NativeCursorWindow(
                        sqlite.fillCursorWindow(statement, 0, Integer.MAX_VALUE, true), sqlite, columnCount);
            }

            @Override
            public ICursorWindow fillStrings(long statement, SQLite sqlite) {
                return fill(statement, sqlite);
            }
        },
        SIMPLE {
            @Override
            public ICursorWindow fill(long statement, SQLite sqlite, int columnCount) {
                sqlite.reset(statement);
                ICursorWindow window = new SimpleCursorWindow();
                while (SQL_ROW == sqlite.step(statement)) {
                    if (!window.allocateRow()) {
                        throw new IllegalStateException("Failed to allocate a window row.");
                    }
                    for (int column = 0; column < columnCount; column++) {
                        window.put(sqlite.columnInt64(statement, column));
                    }
                }
                return window;
            }

            @Override
            public ICursorWindow fillStrings(long statement, SQLite sqlite) {
                sqlite.reset(statement);
                ICursorWindow window = new SimpleCursorWindow();
                while (SQL_ROW == sqlite.step(statement)) {
                    if (!window.allocateRow()) {
                        throw new IllegalStateException("Failed to allocate a window row.");
                    }
                    window.put(sqlite.columnText(statement, 0));
                }
                return window;
            }
        }
    }

    @Param({"1", "50", "1000", "50000"})
    public int rowCount;

    @Param({"NATIVE", "SIMPLE"})
    public CursorWindowKind cursorFactory;

    private File databaseFile;
    private long db;
    private long statement;
    private long multiColumnStatement;
    private ICursorWindow primitiveWindow;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        databaseFile = Files.createTempFile("benchmark-cursor-window", ".db").toFile();
        long[] dbHolder = new long[1];
        SQLite.INSTANCE.openV2(databaseFile.getAbsolutePath(), SQL_OPEN_READWRITE | SQL_OPEN_CREATE, dbHolder);
        db = dbHolder[0];
        SQLite.INSTANCE.exec(db, "CREATE TABLE Foo (bar INT)");
        SQLite.INSTANCE.exec(db, "BEGIN");
        for (int i = 0; i < rowCount; i++) {
            SQLite.INSTANCE.exec(db, "INSERT INTO Foo VALUES (" + i + ")");
        }
        SQLite.INSTANCE.exec(db, "COMMIT");
        long[] statementHolder = new long[1];
        SQLite.INSTANCE.prepareV2(db, "SELECT bar FROM Foo", statementHolder);
        statement = statementHolder[0];
        long[] multiColumnStatementHolder = new long[1];
        SQLite.INSTANCE.prepareV2(
                db,
                "SELECT bar, bar + 1, bar + 2, bar + 3, bar + 4, bar + 5, bar + 6, bar + 7 FROM Foo",
                multiColumnStatementHolder);
        multiColumnStatement = multiColumnStatementHolder[0];
        primitiveWindow = cursorFactory.fill(statement, SQLite.INSTANCE);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        primitiveWindow.close();
        SQLite.INSTANCE.finalize(multiColumnStatement);
        SQLite.INSTANCE.finalize(statement);
        SQLite.INSTANCE.closeV2(db);
        databaseFile.delete();
    }

    @Benchmark
    public int fillCursorWindow() throws IOException {
        try (ICursorWindow window = cursorFactory.fill(statement, SQLite.INSTANCE)) {
            return window.numberOfRows();
        }
    }

    @Benchmark
    public long fillAndReadAllLongs() throws IOException {
        try (ICursorWindow window = cursorFactory.fill(statement, SQLite.INSTANCE)) {
            long sum = 0;
            int rows = window.numberOfRows();
            for (int row = 0; row < rows; row++) {
                sum += window.getLong(row, 0);
            }
            return sum;
        }
    }

    @Benchmark
    public long getLong() {
        long sum = 0;
        int rows = primitiveWindow.numberOfRows();
        for (int row = 0; row < rows; row++) {
            sum += primitiveWindow.getLong(row, 0);
        }
        return sum;
    }

    @Benchmark
    public long getInt() {
        long sum = 0;
        int rows = primitiveWindow.numberOfRows();
        for (int row = 0; row < rows; row++) {
            sum += primitiveWindow.getInt(row, 0);
        }
        return sum;
    }

    @Benchmark
    public long fillAndReadAllColumns() throws IOException {
        try (ICursorWindow window = cursorFactory.fill(multiColumnStatement, SQLite.INSTANCE, MULTI_COLUMN_COUNT)) {
            long sum = 0;
            int rows = window.numberOfRows();
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < MULTI_COLUMN_COUNT; column++) {
                    sum += window.getLong(row, column);
                }
            }
            return sum;
        }
    }

    @Benchmark
    public void repeatedGetString(StringState state, Blackhole blackhole) {
        int rows = state.window.numberOfRows();
        for (int row = 0; row < rows; row++) {
            blackhole.consume(state.window.getString(row, 0));
        }
    }

    @Benchmark
    public void iterateCursorStrings(StringState state, Blackhole blackhole) {
        state.cursor.moveToPosition(-1);
        while (state.cursor.moveToNext()) {
            blackhole.consume(state.cursor.getString(0));
        }
    }

    @Benchmark
    public int fillStringCursorWindow(StringState state) throws IOException {
        try (ICursorWindow window = cursorFactory.fillStrings(state.statement, SQLite.INSTANCE)) {
            return window.numberOfRows();
        }
    }

    @Benchmark
    public void fillAndReadAllStrings(StringState state, Blackhole blackhole) throws IOException {
        try (ICursorWindow window = cursorFactory.fillStrings(state.statement, SQLite.INSTANCE)) {
            int rows = window.numberOfRows();
            for (int row = 0; row < rows; row++) {
                blackhole.consume(window.getString(row, 0));
            }
        }
    }

    @Benchmark
    public void fillAndIterateCursorStrings(StringState state, Blackhole blackhole) throws IOException {
        ICursorWindow window = cursorFactory.fillStrings(state.statement, SQLite.INSTANCE);
        try (ICursor cursor = stringCursor(window)) {
            while (cursor.moveToNext()) {
                blackhole.consume(cursor.getString(0));
            }
        }
    }

    @Benchmark
    public void fillAndReadSomeStrings(StringState state, Blackhole blackhole) throws IOException {
        try (ICursorWindow window = cursorFactory.fillStrings(state.statement, SQLite.INSTANCE)) {
            int rows = window.numberOfRows();
            int reads = Math.min(rows, SOME_STRING_READ_COUNT);
            for (int sample = 0; sample < reads; sample++) {
                int row = reads == 1 ? 0 : sample * (rows - 1) / (reads - 1);
                blackhole.consume(window.getString(row, 0));
            }
        }
    }

    private static ICursor stringCursor(ICursorWindow window) {
        return new WindowedCursor(
                new String[]{"value"},
                new CursorWindowPage(window, 0, window.numberOfRows()),
                null);
    }
}
