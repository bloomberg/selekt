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

import com.bloomberg.selekt.pools.CommonObjectPool;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CommonObjectPoolBenchmark {
    static class Task implements com.bloomberg.selekt.pools.IPooledObject<String> {
        private volatile String name;
        private boolean tag = false;

        public Task(String name) {
            this.name = name;
        }

        public void work() {
            // No-op
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean isPrimary() {
            return false;
        }

        @Override
        public boolean getTag() {
            return tag;
        }

        @Override
        public void setTag(boolean tag) {
            this.tag = tag;
        }

        @Override
        public boolean matches(String key) {
            return key.equals(this.name);
        }

        @Override
        public void releaseMemory() {
            // No-op
        }
    }

    @State(Scope.Thread)
    public static class SingleObjectInput {
        IObjectPool<String, Task> pool;
        private final PoolConfiguration configuration = new PoolConfiguration(
            20_000L, // evictionDelayMillis
            20_000L, // evictionIntervalMillis
            2        // maxTotal
        );

        @Setup(Level.Iteration)
        public void setUp() {
            IObjectFactory<Task> factory = new IObjectFactory<Task>() {
                @Override
                public void close() {
                    // No-op
                }

                @Override
                public void destroyObject(@NotNull final Task obj) {
                    // No-op
                }

                @Override
                public @NotNull Task makeObject() {
                    return new Task(String.valueOf(Thread.currentThread().getId()));
                }

                @Override
                public @NotNull Task makePrimaryObject() {
                    return makeObject();
                }
            };

            pool = new CommonObjectPool<>(
                factory,
                BenchmarkExecutors.sharedExecutor,
                configuration,
                new SingleObjectPool<>(
                    factory,
                    BenchmarkExecutors.sharedExecutor,
                    configuration.getEvictionDelayMillis(),
                    configuration.getEvictionIntervalMillis()
                )
            );
        }

        @TearDown(Level.Iteration)
        public void tearDown() throws java.io.IOException {
            pool.close();
        }
    }

    @State(Scope.Thread)
    public static class MultipleObjectInput {
        IObjectPool<String, Task> pool;
        final PoolConfiguration configuration = new PoolConfiguration(
            20_000L, // evictionDelayMillis
            20_000L, // evictionIntervalMillis
            4        // maxTotal
        );

        @Setup(Level.Invocation)
        public void setUp() {
            final IObjectFactory<Task> factory = new IObjectFactory<>() {
                @Override
                public void close() {
                    // No-op
                }

                @Override
                public void destroyObject(final Task obj) {
                    // No-op
                }

                @Override
                public @NotNull Task makeObject() {
                    return new Task(String.valueOf(Thread.currentThread().getId()));
                }

                @Override
                public @NotNull Task makePrimaryObject() {
                    return makeObject();
                }
            };

            pool = new CommonObjectPool<>(
                factory,
                BenchmarkExecutors.sharedExecutor,
                configuration,
                new SingleObjectPool<>(
                    factory,
                    BenchmarkExecutors.sharedExecutor,
                    configuration.getEvictionDelayMillis(),
                    configuration.getEvictionIntervalMillis()
                )
            );
        }

        @TearDown(Level.Iteration)
        public void tearDown() throws java.io.IOException {
            pool.close();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public @NotNull Task borrowThenReturnSingleObject(@NotNull final SingleObjectInput input) {
        Task obj = input.pool.borrowObject("");
        input.pool.returnObject(obj);
        return obj;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void sequentialExactBorrowMultipleObjects(MultipleObjectInput input) throws InterruptedException {
        Task initial = input.pool.borrowObject("first");
        Task other = input.pool.borrowObject("other");
        initial.setName("first");
        input.pool.returnObject(initial);
        input.pool.returnObject(other);

        for (int i = 0; i < 200; i++) {
            Task first = input.pool.borrowObject("first");
            if (first != initial) {
                Thread.sleep(5L);
            }
            Task second = input.pool.borrowObject("other");
            input.pool.returnObject(first);
            input.pool.returnObject(second);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void exactBorrowThenReturnMultipleObjects(MultipleObjectInput input)
        throws java.util.concurrent.ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(input.configuration.getMaxTotal());
        try {
            // Pre-populate the pool
            for (int i = 0; i < input.configuration.getMaxTotal(); i++) {
                String key = String.valueOf(i);
                Task obj = input.pool.borrowObject(key);
                obj.setName(key);
                input.pool.returnObject(obj);
            }

            // Launch concurrent tasks
            CompletableFuture<?>[] futures = new CompletableFuture[input.configuration.getMaxTotal()];
            for (int i = 0; i < input.configuration.getMaxTotal(); i++) {
                final String key = String.valueOf(i);
                futures[i] = CompletableFuture.runAsync(() -> {
                    try {
                        for (int j = 0; j < 1_000; j++) {
                            Task obj = input.pool.borrowObject(key);
                            Thread.sleep(1L);
                            input.pool.returnObject(obj);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }, executor);
            }

            CompletableFuture.allOf(futures).get();
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void excessiveBorrowThenReturnMultipleObjects(MultipleObjectInput input) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(input.configuration.getMaxTotal() * 2);
        try {
            // Pre-populate the pool
            for (int i = 0; i < input.configuration.getMaxTotal(); i++) {
                String key = String.valueOf(i);
                Task obj = input.pool.borrowObject(key);
                obj.setName(key);
                input.pool.returnObject(obj);
            }

            // Launch excessive number of concurrent tasks
            final CompletableFuture<?>[] futures = new CompletableFuture[input.configuration.getMaxTotal() * 2];
            for (int i = 0; i < input.configuration.getMaxTotal() * 2; i++) {
                final String key = String.valueOf(i);
                futures[i] = CompletableFuture.runAsync(() -> {
                    try {
                        for (int j = 0; j < 1_000; j++) {
                            Task obj = input.pool.borrowObject(key);
                            Thread.sleep(1L);
                            input.pool.returnObject(obj);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }, executor);
            }

            CompletableFuture.allOf(futures).get();
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }
}