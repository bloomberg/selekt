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

package com.bloomberg.selekt.batch.benchmarks;

import com.bloomberg.selekt.CommonThreadLocalRandom;
import com.bloomberg.selekt.SQLDatabase;
import com.bloomberg.selekt.SQLiteJournalMode;
import com.bloomberg.selekt.SQLiteTransactionMode;
import com.bloomberg.selekt.commons.DatabaseKt;
import com.bloomberg.selekt.jvm.SQLite;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Benchmark)
public class BatchMethodBenchmark {
    private static final int SQLITE_MAX_VARIABLE_NUMBER = 999;
    private static final int PARAMS_PER_ROW = 4;
    private static final int MAX_ROWS_PER_MULTI_INSERT = SQLITE_MAX_VARIABLE_NUMBER / PARAMS_PER_ROW;

    @Param({"100", "1000", "5000", "10000"})
    public int batchSize;

    @Param({"SIMPLE", "MIXED", "LARGE_BLOBS"})
    public String dataType;

    private File databaseFile;
    private SQLDatabase database;
    private Object[][] arrayBatchData;
    private final Random random = new Random(42);

    private String multiValuedInsertSql;
    private String multiValuedInsertSqlRemainder;
    private Sequence<Object[]> fullChunksSequence;
    private Object[] remainderChunk;

    private static final String SQL_INSERT = "INSERT INTO batch_test (id, name, value, data) " +
        "VALUES (?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET name=excluded.name, value=excluded.value, data=excluded.data";

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        databaseFile = Files.createTempFile("benchmark-batch", ".db").toFile();
        database = new SQLDatabase(
            databaseFile.getAbsolutePath(),
            SQLite.INSTANCE,
            SQLiteJournalMode.WAL.databaseConfiguration,
            null,
            CommonThreadLocalRandom.INSTANCE
        );
        database.exec("DROP TABLE IF EXISTS batch_test", null);
        database.exec("CREATE TABLE batch_test (" +
            "id INTEGER PRIMARY KEY, " +
            "name TEXT, " +
            "value REAL, " +
            "data BLOB" +
            ")", null);
        generateTestData();
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        database.close();
        DatabaseKt.deleteDatabase(databaseFile);
    }

    private void generateTestData() {
        arrayBatchData = new Object[batchSize][];
        for (int i = 0; i < batchSize; i++) {
            arrayBatchData[i] = createRowData(i);
        }
        generateFlattenedData();
    }

    private void generateFlattenedData() {
        final int fullChunkCount = batchSize / MAX_ROWS_PER_MULTI_INSERT;
        final int remainder = batchSize % MAX_ROWS_PER_MULTI_INSERT;
        multiValuedInsertSql = buildMultiValuedInsertSql(MAX_ROWS_PER_MULTI_INSERT);
        if (fullChunkCount > 0) {
            final Object[][] fullChunks = new Object[fullChunkCount][];
            for (int chunk = 0; chunk < fullChunkCount; chunk++) {
                fullChunks[chunk] = flattenArgs(chunk * MAX_ROWS_PER_MULTI_INSERT, MAX_ROWS_PER_MULTI_INSERT);
            }
            fullChunksSequence = SequencesKt.sequenceOf(fullChunks);
        } else {
            fullChunksSequence = SequencesKt.emptySequence();
        }
        multiValuedInsertSqlRemainder = buildMultiValuedInsertSql(remainder);
        remainderChunk = flattenArgs(fullChunkCount * MAX_ROWS_PER_MULTI_INSERT, remainder);
    }

    private Object[] createRowData(final int index) {
        return switch (dataType) {
            case "SIMPLE" -> new Object[]{
                index,
                "item_" + index,
                index * 1.5,
                null
            };
            case "MIXED" -> new Object[]{
                index,
                (index % 3 == 0) ? "mixed_" + index : "default_" + index,
                (index % 2 == 0) ? index * 2.5 : 0.0,
                (index % 5 == 0) ? generateRandomBytes(64) : generateRandomBytes(16)
            };
            case "LARGE_BLOBS" -> new Object[]{
                index,
                "blob_item_" + index,
                index * 1.0,
                generateRandomBytes(2048)
            };
            default -> new Object[]{index, "default", 1.0, null};
        };
    }

    private byte[] generateRandomBytes(final int size) {
        final byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    @Benchmark
    public void multiValuedInsertManual(final Blackhole blackhole) {
        database.transact(SQLiteTransactionMode.EXCLUSIVE, db -> {
            blackhole.consume(db.batch(multiValuedInsertSql, fullChunksSequence));
            if (remainderChunk != null) {
                db.exec(multiValuedInsertSqlRemainder, remainderChunk);
            }
            return null;
        });
    }

    @Benchmark
    public void individualInsertsInTransaction() {
        database.transact(SQLiteTransactionMode.EXCLUSIVE, db -> {
            for (Object[] row : arrayBatchData) {
                db.exec(SQL_INSERT, row);
            }
            return null;
        });
    }

    @Benchmark
    public void individualInsertsNoTransaction() {
        for (Object[] row : arrayBatchData) {
            database.exec(SQL_INSERT, row);
        }
    }

    private String buildMultiValuedInsertSql(final int rowCount) {
        final StringBuilder builder = new StringBuilder(256 + rowCount * 12);
        builder.append("INSERT INTO batch_test (id, name, value, data) VALUES ");
        for (int i = 0; i < rowCount; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append("(?, ?, ?, ?)");
        }
        builder.append(" ON CONFLICT(id) DO UPDATE SET name=excluded.name, value=excluded.value, data=excluded.data");
        return builder.toString();
    }

    private Object[] flattenArgs(final int offset, final int count) {
        final Object[] flat = new Object[count * PARAMS_PER_ROW];
        for (int i = 0; i < count; i++) {
            Object[] row = arrayBatchData[offset + i];
            System.arraycopy(row, 0, flat, i * PARAMS_PER_ROW, PARAMS_PER_ROW);
        }
        return flat;
    }
}
