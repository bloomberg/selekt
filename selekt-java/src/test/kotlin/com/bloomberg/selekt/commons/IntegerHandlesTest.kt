/*
 * Copyright 2025 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.commons

import org.junit.jupiter.api.Test
import java.lang.invoke.VarHandle
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertDoesNotThrow

internal class IntegerHandlesTest {
    private class TestClass {
        @Suppress("unused")
        @Volatile
        var field1: Int = 0

        @Suppress("unused")
        @Volatile
        var field2: Int = 42
    }

    @Test
    fun integerHandleReturnsCorrectType() {
        val handle = IntegerHandle<TestClass>("field1")
        assertTrue(
            handle is VarHandle || handle is AtomicIntegerFieldUpdater<*>,
            "Handle should be either VarHandle or AtomicIntegerFieldUpdater"
        )
    }

    @Test
    fun getIntReadsCorrectValue() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field2")

        val value = getInt(handle, testObj)
        assertEquals(42, value)
    }

    @Test
    fun setIntUpdatesValue() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field1")

        setInt(handle, testObj, 123)
        assertEquals(123, getInt(handle, testObj))
    }

    @Test
    fun compareAndSetIntSucceeds() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field1")

        assertTrue(compareAndSetInt(handle, testObj, 0, 999))
        assertEquals(999, getInt(handle, testObj))
    }

    @Test
    fun compareAndSetIntFails() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field2")

        assertFalse(compareAndSetInt(handle, testObj, 0, 999))
        assertEquals(42, getInt(handle, testObj)) // Should remain unchanged
    }

    @Test
    fun compareAndSetIntWithCorrectExpectedValue() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field2")

        assertTrue(compareAndSetInt(handle, testObj, 42, 100))
        assertEquals(100, getInt(handle, testObj))
    }

    @Test
    fun operationsAreThreadSafe() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field1")
        val numThreads = 10
        val incrementsPerThread = 1000
        val latch = CountDownLatch(numThreads)

        repeat(numThreads) {
            thread {
                try {
                    repeat(incrementsPerThread) {
                        while (true) {
                            val current = getInt(handle, testObj)
                            if (compareAndSetInt(handle, testObj, current, current + 1)) {
                                break
                            }
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        assertEquals(numThreads * incrementsPerThread, getInt(handle, testObj))
    }

    @Test
    fun multipleFieldsOnSameClass() {
        val testObj = TestClass()
        val handle1 = IntegerHandle<TestClass>("field1")
        val handle2 = IntegerHandle<TestClass>("field2")

        setInt(handle1, testObj, 111)
        setInt(handle2, testObj, 222)

        assertEquals(111, getInt(handle1, testObj))
        assertEquals(222, getInt(handle2, testObj))
    }

    @Test
    fun stressTestConcurrentOperations() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field1")
        val executor = Executors.newFixedThreadPool(4)

        try {
            val tasks = (0 until 4).map {
                executor.submit {
                    repeat(250) {
                        while (true) {
                            val current = getInt(handle, testObj)
                            if (compareAndSetInt(handle, testObj, current, current + 1)) {
                                break
                            }
                        }

                        val testValue = Thread.currentThread().hashCode()
                        setInt(handle, testObj, testValue)
                        assertDoesNotThrow {
                            getInt(handle, testObj)
                        }
                    }
                }
            }

            tasks.forEach { it.get() }

            val finalValue = getInt(handle, testObj)
            assertTrue(finalValue >= 1000, "Expected at least 1000, got $finalValue")
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun getAndIncrementReturnsOldValue() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field1")

        assertEquals(0, getAndIncrement(handle, testObj))
        assertEquals(1, getInt(handle, testObj))

        val handle2 = IntegerHandle<TestClass>("field2")
        assertEquals(42, getAndIncrement(handle2, testObj))
        assertEquals(43, getInt(handle2, testObj))
    }

    @Test
    fun decrementAndGetReturnsNewValue() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field2") // Initial value is 42

        assertEquals(41, decrementAndGet(handle, testObj))
        assertEquals(41, getInt(handle, testObj))

        assertEquals(40, decrementAndGet(handle, testObj))
        assertEquals(40, getInt(handle, testObj))
    }

    @Test
    fun getAndIncrementThreadSafety() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field1")
        val numThreads = 5
        val incrementsPerThread = 200
        val latch = CountDownLatch(numThreads)
        val results = mutableListOf<Int>()

        repeat(numThreads) {
            thread {
                try {
                    repeat(incrementsPerThread) {
                        val oldValue = getAndIncrement(handle, testObj)
                        synchronized(results) {
                            results.add(oldValue)
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        assertEquals(numThreads * incrementsPerThread, getInt(handle, testObj))
        assertEquals(numThreads * incrementsPerThread, results.size)
        assertEquals(results.toSet().size, results.size, "All values must be unique")
        assertTrue(results.all { it in 0 until numThreads * incrementsPerThread })
    }

    @Test
    fun decrementAndGetThreadSafety() {
        val testObj = TestClass()
        val handle = IntegerHandle<TestClass>("field1")
        val startValue = 1000
        val numThreads = 5
        val decrementsPerThread = 200

        setInt(handle, testObj, startValue)

        val latch = CountDownLatch(numThreads)
        val results = mutableListOf<Int>()

        repeat(numThreads) {
            thread {
                try {
                    repeat(decrementsPerThread) {
                        val newValue = decrementAndGet(handle, testObj)
                        synchronized(results) {
                            results.add(newValue)
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        assertEquals(0, getInt(handle, testObj))
        assertEquals(numThreads * decrementsPerThread, results.size)
        assertEquals(results.toSet().size, results.size, "All values must be unique")
        assertTrue(results.all { it in 0 until startValue })
    }
}
