/*
 * Copyright 2025 Bloomberg Finance L.P.
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
import com.bloomberg.selekt.commons.DatabaseKt;
import com.bloomberg.selekt.SQLDatabase;
import com.bloomberg.selekt.SQLiteJournalMode;
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
    @Param({"5000", "10000", "25000"})
    public int batchSize;

    @Param({"SIMPLE", "MIXED", "LARGE_BLOBS"})
    public String dataType;

    private File databaseFile;
    private SQLDatabase database;
    private Object[][] arrayBatchData;
    private Sequence<Object[]> sequenceBatchData;
    private Object[][] lazyArrayData;
    private Sequence<Object[]> lazySequenceData;
    private Object[][] complexArrayData;
    private Sequence<Object[]> complexSequenceData;
    private Object[][] updateData;
    private Sequence<Object[]> updateSequenceData;
    private final Random random = new Random(42);

    private static final String SQL_INSERT = "INSERT OR REPLACE INTO batch_test (id, name, value, data) " +
        "VALUES (?, ?, ?, ?)";

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
        sequenceBatchData = SequencesKt.sequenceOf(arrayBatchData);

        lazyArrayData = new Object[batchSize][];
        for (int i = 0; i < batchSize; i++) {
            lazyArrayData[i] = new Object[]{
                i,
                "lazy_" + i,
                i * Math.E,
                (i % 10 == 0) ? generateRandomBytes(128) : generateRandomBytes(32)
            };
        }
        lazySequenceData = SequencesKt.sequenceOf(lazyArrayData);

        complexArrayData = new Object[batchSize][];
        for (int i = 0; i < batchSize; i++) {
            switch (i % 4) {
                case 0:
                    complexArrayData[i] = new Object[]{i, "type_a", i * 1.0, generateRandomBytes(8)};
                    break;
                case 1:
                    complexArrayData[i] = new Object[]{i, "type_b", i * 2.0, generateRandomBytes(32)};
                    break;
                case 2:
                    complexArrayData[i] = new Object[]{i, "type_c", i * 3.0, generateRandomBytes(64)};
                    break;
                default:
                    complexArrayData[i] = new Object[]{i, "type_d", i * 0.5, generateRandomBytes(16)};
                    break;
            }
        }
        complexSequenceData = SequencesKt.sequenceOf(complexArrayData);

        updateData = new Object[batchSize][];
        for (int i = 0; i < batchSize; i++) {
            updateData[i] = new Object[]{
                "updated_" + i,
                i * 10.0,
                i
            };
        }
        updateSequenceData = SequencesKt.sequenceOf(updateData);
    }

    private Object[] createRowData(int index) {
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

    private byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    @Benchmark
    public int arrayBatchMethod() {
        return database.batch(SQL_INSERT, arrayBatchData);
    }

    @Benchmark
    public int sequenceBatchMethod() {
        return database.batch(SQL_INSERT, sequenceBatchData);
    }

    @Benchmark
    public int arrayBatchUpdate() {
        database.batch(SQL_INSERT, arrayBatchData);
        return database.batch(
            "UPDATE batch_test SET name = ?, value = ? WHERE id = ?",
            updateData
        );
    }

    @Benchmark
    public int sequenceBatchUpdate() {
        database.batch(SQL_INSERT, sequenceBatchData);
        return database.batch(
            "UPDATE batch_test SET name = ?, value = ? WHERE id = ?",
            updateSequenceData
        );
    }

    @Benchmark
    public int arrayBatchLazyEquivalent() {
        return database.batch(SQL_INSERT, lazyArrayData);
    }

    @Benchmark
    public int sequenceBatchLazy() {
        return database.batch(SQL_INSERT, lazySequenceData);
    }

    @Benchmark
    public int arrayBatchComplex() {
        return database.batch(SQL_INSERT, complexArrayData);
    }

    @Benchmark
    public int sequenceBatchComplex() {
        return database.batch(SQL_INSERT, complexSequenceData);
    }
}
