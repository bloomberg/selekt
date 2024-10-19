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

package com.bloomberg.selekt.cache.benchmarks

import com.bloomberg.selekt.cache.LinkedLruCache
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State

@State(Scope.Thread)
open class LinkedCacheInput {
    internal lateinit var cache: LinkedLruCache<Any>
    internal lateinit var largeCache: LinkedLruCache<Any>

    @Setup(Level.Iteration)
    fun setUp() {
        cache = LinkedLruCache(1) {}
        largeCache = LinkedLruCache(64) {}
    }
}

open class LinkedLruCacheBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntry(input: LinkedCacheInput) = input.cache.run {
        get("1") {}
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntryWithEviction(input: LinkedCacheInput) = input.cache.run {
        get("1") {}
        get("2") {}
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntries(input: LinkedCacheInput) = input.largeCache.run {
        get("1") { "" }
        get("2") { "" }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getManyEntries(input: LinkedCacheInput) = input.largeCache.run {
        get("0") { "" }
        get("1") { "" }
        get("2") { "" }
        get("3") { "" }
        get("4") { "" }
        get("5") { "" }
        get("6") { "" }
        get("7") { "" }
        get("8") { "" }
        get("9") { "" }
        get("2") { "" }
        get("3") { "" }
        get("9") { "" }
        get("4") { "" }
        get("5") { "" }
        get("0") { "" }
        get("8") { "" }
        get("6") { "" }
        get("1") { "" }
        get("7") { "" }
    }
}
