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

package com.bloomberg.selekt.jdk.benchmarks

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Thread)
open class LinkedHashMapInput {
    internal lateinit var smallMap: LinkedHashMap<String, Any>
    internal lateinit var largeMap: LinkedHashMap<String, Any>
    internal lateinit var largeAccessOrderMap: LinkedHashMap<String, Any>

    @Setup(Level.Iteration)
    fun setUp() {
        smallMap = object : LinkedHashMap<String, Any>(1, 1.1f, false) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Any>) = size > 1
        }
        largeMap = object : LinkedHashMap<String, Any>(64, 1.1f, false) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Any>) = size > 64
        }
        largeAccessOrderMap = object : LinkedHashMap<String, Any>(64, 1.1f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Any>) = size > 64
        }
    }
}

open class LinkedHashMapBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntry(input: LinkedHashMapInput) = input.smallMap.run {
        getOrPut("1") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntries(input: LinkedHashMapInput) = input.largeMap.run {
        getOrPut("1") { "" }
        getOrPut("2") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntriesDifferentLengths(input: LinkedHashMapInput) = input.largeMap.run {
        getOrPut("1") { "" }
        getOrPut("23") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntriesAccessOrder(input: LinkedHashMapInput) = input.largeAccessOrderMap.run {
        getOrPut("1") { "" }
        getOrPut("2") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getManyEntriesAccessOrder(input: LinkedHashMapInput) = input.largeAccessOrderMap.run {
        getOrPut("0") { "" }
        getOrPut("1") { "" }
        getOrPut("2") { "" }
        getOrPut("3") { "" }
        getOrPut("4") { "" }
        getOrPut("5") { "" }
        getOrPut("6") { "" }
        getOrPut("7") { "" }
        getOrPut("8") { "" }
        getOrPut("9") { "" }
        getOrPut("2") { "" }
        getOrPut("3") { "" }
        getOrPut("9") { "" }
        getOrPut("4") { "" }
        getOrPut("5") { "" }
        getOrPut("0") { "" }
        getOrPut("8") { "" }
        getOrPut("6") { "" }
        getOrPut("1") { "" }
        getOrPut("7") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntryWithRemoval(input: LinkedHashMapInput) = input.smallMap.run {
        getOrPut("1") { "" }
        getOrPut("2") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getThenRemoveEntry(input: LinkedHashMapInput) = input.smallMap.run {
        getOrPut("1") { "" }
        remove("1")
    }
}
