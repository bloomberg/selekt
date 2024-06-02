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
import java.util.LinkedList

@State(Scope.Thread)
open class LinkedListInput {
    internal lateinit var list: LinkedList<Node>

    data class Node(
        val value: Any,
        val next: Any?
    )

    @Setup(Level.Iteration)
    fun setUp() {
        list = LinkedList<Node>().apply {
            val next = Any()
            add(Node(Any(), next))
            add(Node(Any(), null))
        }
    }
}

open class LinkedListBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getFirst(input: LinkedListInput) = input.list.run {
        firstOrNull()
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun getEntries(input: LinkedListInput) = input.list.run {
        val first = firstOrNull()
        first?.next
    }
}
