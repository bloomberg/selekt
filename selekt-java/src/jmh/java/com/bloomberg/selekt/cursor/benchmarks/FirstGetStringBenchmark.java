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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class FirstGetStringBenchmark {
    public enum StringEncoding {
        ASCII("-a-bb"),
        LATIN1("-é-ññ"),
        UTF16("-€-😀");

        private final String suffix;

        StringEncoding(String suffix) {
            this.suffix = suffix;
        }
    }

    @Param({"1000", "50000"})
    public int rowCount;

    @Param({"ASCII", "LATIN1", "UTF16"})
    public StringEncoding stringEncoding;

    private ByteBuffer utf8Data;
    private ByteBuffer compactData;
    private CharBuffer utf16Data;
    private int[] utf8Offsets;
    private int[] utf8Lengths;
    private int[] readyOffsets;
    private int[] readyLengths;
    private byte[] bytes;
    private char[] chars;

    @Setup(Level.Trial)
    public void setUp() {
        List<String> strings = new ArrayList<>(rowCount);
        List<byte[]> utf8Values = new ArrayList<>(rowCount);
        utf8Offsets = new int[rowCount];
        utf8Lengths = new int[rowCount];
        readyOffsets = new int[rowCount];
        readyLengths = new int[rowCount];
        int utf8ByteCount = 0;
        int readyUnitCount = 0;
        int maximumUtf8Length = 0;
        int maximumCharacterCount = 0;
        for (int row = 0; row < rowCount; row++) {
            String string = "row-" + row + stringEncoding.suffix;
            byte[] utf8 = string.getBytes(StandardCharsets.UTF_8);
            strings.add(string);
            utf8Values.add(utf8);
            utf8Offsets[row] = utf8ByteCount;
            utf8Lengths[row] = utf8.length;
            readyOffsets[row] = readyUnitCount;
            readyLengths[row] = string.length();
            utf8ByteCount += utf8.length;
            readyUnitCount += string.length();
            maximumUtf8Length = Math.max(maximumUtf8Length, utf8.length);
            maximumCharacterCount = Math.max(maximumCharacterCount, string.length());
        }
        utf8Data = ByteBuffer.allocateDirect(utf8ByteCount);
        for (byte[] value : utf8Values) {
            utf8Data.put(value);
        }
        utf8Data.clear();
        if (stringEncoding == StringEncoding.UTF16) {
            ByteBuffer storage = ByteBuffer.allocateDirect(readyUnitCount * Character.BYTES)
                    .order(ByteOrder.nativeOrder());
            utf16Data = storage.asCharBuffer();
            for (String string : strings) {
                utf16Data.put(string);
            }
            utf16Data.clear();
        } else {
            compactData = ByteBuffer.allocateDirect(readyUnitCount);
            for (String string : strings) {
                compactData.put(string.getBytes(StandardCharsets.ISO_8859_1));
            }
            compactData.clear();
        }
        bytes = new byte[maximumUtf8Length];
        chars = new char[maximumCharacterCount];
    }

    @Benchmark
    public void currentUtf8(Blackhole blackhole) {
        for (int row = 0; row < rowCount; row++) {
            int length = utf8Lengths[row];
            utf8Data.get(utf8Offsets[row], bytes, 0, length);
            blackhole.consume(new String(bytes, 0, length, StandardCharsets.UTF_8));
        }
    }

    @Benchmark
    public void asciiFlag(Blackhole blackhole) {
        for (int row = 0; row < rowCount; row++) {
            int length = utf8Lengths[row];
            utf8Data.get(utf8Offsets[row], bytes, 0, length);
            blackhole.consume(new String(
                    bytes,
                    0,
                    length,
                    stringEncoding == StringEncoding.ASCII
                            ? StandardCharsets.ISO_8859_1
                            : StandardCharsets.UTF_8));
        }
    }

    @Benchmark
    public void stringReadyStorage(Blackhole blackhole) {
        if (stringEncoding == StringEncoding.UTF16) {
            for (int row = 0; row < rowCount; row++) {
                int length = readyLengths[row];
                utf16Data.get(readyOffsets[row], chars, 0, length);
                blackhole.consume(new String(chars, 0, length));
            }
        } else {
            for (int row = 0; row < rowCount; row++) {
                int length = readyLengths[row];
                compactData.get(readyOffsets[row], bytes, 0, length);
                blackhole.consume(new String(bytes, 0, length, StandardCharsets.ISO_8859_1));
            }
        }
    }
}
