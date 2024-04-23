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

import com.bloomberg.selekt.pools.IObjectFactory
import com.bloomberg.selekt.pools.IObjectPool
import com.bloomberg.selekt.pools.PoolConfiguration
import com.bloomberg.selekt.pools.SingleObjectPool
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown

@State(Scope.Thread)
open class PoolInput {
    internal lateinit var pool: IObjectPool<String, PooledObject>
    private val configuration = PoolConfiguration(
        evictionDelayMillis = 20_000L,
        evictionIntervalMillis = 20_000L,
        maxTotal = 1
    )

    @Setup(Level.Iteration)
    fun setUp() {
        pool = SingleObjectPool(object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = Unit

            override fun makeObject() = PooledObject()

            override fun makePrimaryObject() = makeObject()
        }, sharedExecutor, configuration.evictionDelayMillis, configuration.evictionIntervalMillis)
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        pool.close()
    }
}

open class SingleObjectPoolBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun borrowThenReturnObject(input: PoolInput) = input.pool.run {
        borrowObject("").also { returnObject(it) }
    }
}
