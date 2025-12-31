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

package com.bloomberg.selekt.pools.benchmarks;

import com.bloomberg.selekt.pools.IObjectFactory;
import com.bloomberg.selekt.pools.IObjectPool;
import com.bloomberg.selekt.pools.PoolConfiguration;
import com.bloomberg.selekt.pools.SingleObjectPool;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

public class SingleObjectPoolBenchmark {
    @State(Scope.Thread)
    public static class PoolInput {
        IObjectPool<String, PooledObject> pool;
        private final PoolConfiguration configuration = new PoolConfiguration(
            20_000L,
            20_000L,
            1
        );

        @Setup(Level.Iteration)
        public void setUp() {
            IObjectFactory<PooledObject> factory = new IObjectFactory<>() {
                @Override
                public void close() {
                    // No-op
                }

                @Override
                public void destroyObject(@NotNull final PooledObject obj) {
                    // No-op
                }

                @Override
                public @NotNull PooledObject makeObject() {
                    return new PooledObject();
                }

                @Override
                public @NotNull PooledObject makePrimaryObject() {
                    return makeObject();
                }
            };

            pool = new SingleObjectPool<>(
                factory,
                BenchmarkExecutors.sharedExecutor,
                configuration.getEvictionDelayMillis(),
                configuration.getEvictionIntervalMillis()
            );
        }

        @TearDown(Level.Iteration)
        public void tearDown() throws Exception {
            pool.close();
        }
    }
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public PooledObject borrowThenReturnObject(@NotNull final PoolInput input) {
        final PooledObject obj = input.pool.borrowObject("");
        input.pool.returnObject(obj);
        return obj;
    }
}