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

import com.bloomberg.selekt.collections.map.FastStringMap
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Thread)
open class MapInput {
    internal lateinit var map: FastStringMap<Any>

    @Setup(Level.Iteration)
    fun setUp() {
        map = FastStringMap(1)
    }
}

open class FastStringMapBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntry(input: MapInput) = input.map.run {
        getEntryElsePut("1") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntryWithCollision(input: MapInput) = input.map.run {
        getEntryElsePut("1") { "" }
        getEntryElsePut("2") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getThenRemoveEntry(input: MapInput) = input.map.run {
        getEntryElsePut("1") { "" }
        removeEntry("1")
    }
}
