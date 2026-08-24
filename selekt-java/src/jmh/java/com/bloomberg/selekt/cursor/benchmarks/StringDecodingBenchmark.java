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
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
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
public class StringDecodingBenchmark {
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

    private ByteBuffer data;
    private ByteBuffer directInput;
    private ByteBuffer heapInput;
    private CharBuffer characters;
    private CharsetDecoder directDecoder;
    private CharsetDecoder heapDecoder;
    private int[] offsets;
    private int[] lengths;
    private byte[] bytes;
    private char[] chars;

    @Setup(Level.Trial)
    public void setUp() {
        List<byte[]> values = new ArrayList<>(rowCount);
        offsets = new int[rowCount];
        lengths = new int[rowCount];
        int byteCount = 0;
        int maximumLength = 0;
        for (int row = 0; row < rowCount; row++) {
            byte[] value = ("row-" + row + stringEncoding.suffix).getBytes(StandardCharsets.UTF_8);
            values.add(value);
            offsets[row] = byteCount;
            lengths[row] = value.length;
            byteCount += value.length;
            maximumLength = Math.max(maximumLength, value.length);
        }
        data = ByteBuffer.allocateDirect(byteCount);
        for (byte[] value : values) {
            data.put(value);
        }
        data.clear();
        directInput = data.asReadOnlyBuffer();
        bytes = new byte[maximumLength];
        heapInput = ByteBuffer.wrap(bytes);
        chars = new char[maximumLength];
        characters = CharBuffer.wrap(chars);
        directDecoder = newDecoder();
        heapDecoder = newDecoder();
    }

    @Benchmark
    public void stringConstructor(Blackhole blackhole) {
        for (int row = 0; row < rowCount; row++) {
            int length = lengths[row];
            data.get(offsets[row], bytes, 0, length);
            blackhole.consume(new String(bytes, 0, length, StandardCharsets.UTF_8));
        }
    }

    @Benchmark
    public void directCharsetDecoder(Blackhole blackhole) throws CharacterCodingException {
        for (int row = 0; row < rowCount; row++) {
            directInput.clear();
            directInput.position(offsets[row]);
            directInput.limit(offsets[row] + lengths[row]);
            blackhole.consume(decode(directDecoder, directInput));
        }
    }

    @Benchmark
    public void heapCharsetDecoder(Blackhole blackhole) throws CharacterCodingException {
        for (int row = 0; row < rowCount; row++) {
            int length = lengths[row];
            data.get(offsets[row], bytes, 0, length);
            heapInput.clear();
            heapInput.limit(length);
            blackhole.consume(decode(heapDecoder, heapInput));
        }
    }

    private String decode(CharsetDecoder decoder, ByteBuffer input) throws CharacterCodingException {
        decoder.reset();
        characters.clear();
        CoderResult result = decoder.decode(input, characters, true);
        if (result.isError()) {
            result.throwException();
        }
        if (result.isOverflow()) {
            throw new IllegalStateException("Reusable character buffer is too small.");
        }
        result = decoder.flush(characters);
        if (result.isError()) {
            result.throwException();
        }
        if (result.isOverflow()) {
            throw new IllegalStateException("Reusable character buffer is too small.");
        }
        return new String(chars, 0, characters.position());
    }

    private static CharsetDecoder newDecoder() {
        return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
    }
}
