/*
 * Copyright 2024 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.collections.map.benchmarks

import com.bloomberg.selekt.collections.map.FastLinkedStringMap
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Thread)
open class LinkedMapInput {
    internal lateinit var smallMap: FastLinkedStringMap<Any>
    internal lateinit var largeMap: FastLinkedStringMap<Any>
    internal lateinit var largeAccessMap: FastLinkedStringMap<Any>

    @Setup(Level.Iteration)
    fun setUp() {
        smallMap = FastLinkedStringMap(1, 1) {}
        largeMap = FastLinkedStringMap(64, 64, false) {}
        largeAccessMap = FastLinkedStringMap(64, 64, true) {}
    }
}

open class FastLinkedStringMapBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntry(input: LinkedMapInput) = input.smallMap.run {
        getElsePut("1") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntries(input: LinkedMapInput) = input.largeMap.run {
        getElsePut("1") { "" }
        getElsePut("2") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntriesDifferentLengths(input: LinkedMapInput) = input.largeMap.run {
        getElsePut("1") { "" }
        getElsePut("23") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntriesAccessOrder(input: LinkedMapInput) = input.largeAccessMap.run {
        getElsePut("1") { "" }
        getElsePut("2") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntriesWithCollision(input: LinkedMapInput) = input.smallMap.run {
        getElsePut("1") { "" }
        getElsePut("2") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntriesDifferentLengthsWithCollision(input: LinkedMapInput) = input.smallMap.run {
        getElsePut("1") { "" }
        getElsePut("23") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getThenRemoveEntry(input: LinkedMapInput) = input.smallMap.run {
        getElsePut("1") { "" }
        removeEntry("1")
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getThenRemoveKey(input: LinkedMapInput) = input.smallMap.run {
        getElsePut("1") { "" }
        removeKey("1")
    }
}
