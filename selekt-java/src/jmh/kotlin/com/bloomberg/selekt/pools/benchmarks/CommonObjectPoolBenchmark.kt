/*
 * Copyright 2023 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.pools.benchmarks

import com.bloomberg.selekt.pools.CommonObjectPool
import com.bloomberg.selekt.pools.IObjectFactory
import com.bloomberg.selekt.pools.IObjectPool
import com.bloomberg.selekt.pools.IPooledObject
import com.bloomberg.selekt.pools.PoolConfiguration
import com.bloomberg.selekt.pools.SingleObjectPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown

class Task(@Volatile var name: String) : IPooledObject<String> {
    fun work() = Unit

    override val isPrimary = false

    override var tag = false

    override fun matches(key: String) = key == this.name

    override fun releaseMemory() = Unit
}

@State(Scope.Thread)
open class SingleObjectInput {
    internal lateinit var pool: IObjectPool<String, Task>
    private val configuration = PoolConfiguration(
        evictionDelayMillis = 20_000L,
        evictionIntervalMillis = 20_000L,
        maxTotal = 2
    )

    @Setup(Level.Iteration)
    fun setUp() {
        val factory = object : IObjectFactory<Task> {
            override fun close() = Unit

            override fun destroyObject(obj: Task) = Unit

            override fun makeObject() = Task(Thread.currentThread().id.toString())

            override fun makePrimaryObject() = makeObject()
        }
        pool = CommonObjectPool(
            factory,
            sharedExecutor,
            configuration,
            SingleObjectPool(
                factory,
                sharedExecutor,
                configuration.evictionDelayMillis,
                configuration.evictionIntervalMillis
            )
        )
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        pool.close()
    }
}

@State(Scope.Thread)
open class MultipleObjectInput {
    internal lateinit var pool: IObjectPool<String, Task>
    internal val configuration = PoolConfiguration(
        evictionDelayMillis = 20_000L,
        evictionIntervalMillis = 20_000L,
        maxTotal = 4
    )

    @Setup(Level.Invocation)
    fun setUp() {
        val factory = object : IObjectFactory<Task> {
            override fun close() = Unit

            override fun destroyObject(obj: Task) = Unit

            override fun makeObject() = Task(Thread.currentThread().id.toString())

            override fun makePrimaryObject() = makeObject()
        }
        pool = CommonObjectPool(
            factory,
            sharedExecutor,
            configuration,
            SingleObjectPool(
                factory,
                sharedExecutor,
                configuration.evictionDelayMillis,
                configuration.evictionIntervalMillis
            )
        )
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        pool.close()
    }
}

open class CommonObjectPoolBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun borrowThenReturnSingleObject(input: SingleObjectInput) = input.pool.run {
        borrowObject("").also { returnObject(it) }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun sequentialExactBorrowMultipleObjects(
        input: MultipleObjectInput
    ) = input.pool.run {
        val initial = borrowObject("first").also {
            val other = borrowObject("other")
            it.name = "first"
            returnObject(it)
            returnObject(other)
        }
        repeat(200) {
            val first = borrowObject("first")
            if (first !== initial) {
                Thread.sleep(5L)
            }
            val second = borrowObject("other")
            returnObject(first)
            returnObject(second)
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun exactBorrowThenReturnMultipleObjects(input: MultipleObjectInput) = input.pool.run {
        runBlocking(Dispatchers.IO) {
            coroutineScope {
                repeat(input.configuration.maxTotal) { i ->
                    val key = "$i"
                    borrowObject(key).also {
                        it.name = key
                        returnObject(it)
                    }
                    launch {
                        repeat(1_000) {
                            borrowObject(key).also {
                                delay(1L)
                                returnObject(it)
                            }
                        }
                    }
                }
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun excessiveBorrowThenReturnMultipleObjects(input: MultipleObjectInput) = input.pool.run {
        runBlocking(Dispatchers.IO) {
            coroutineScope {
                repeat(input.configuration.maxTotal) { i ->
                    val key = "$i"
                    borrowObject(key).also {
                        it.name = key
                        returnObject(it)
                    }
                }
                repeat(2 * input.configuration.maxTotal) { i ->
                    val key = "$i"
                    launch {
                        repeat(1_000) {
                            borrowObject(key).also {
                                delay(1L)
                                returnObject(it)
                            }
                        }
                    }
                }
            }
        }
    }
}
