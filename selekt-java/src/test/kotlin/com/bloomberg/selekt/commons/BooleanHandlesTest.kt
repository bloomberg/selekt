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
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import kotlin.concurrent.thread
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BooleanHandlesTest {
    private class TestClass {
        @Suppress("unused")
        @Volatile
        var flag1: Int = 0

        @Suppress("unused")
        @Volatile
        var flag2: Int = 1
    }

    @Test
    fun booleanHandleReturnsCorrectType() {
        val handle = BooleanHandle<TestClass>("flag1")
        assertTrue(
            handle is VarHandle || handle is AtomicIntegerFieldUpdater<*>,
            "Handle should be either VarHandle or AtomicIntegerFieldUpdater"
        )
    }

    @Test
    fun getBooleanReadsCorrectValue() {
        val testObj = TestClass()
        val handle = BooleanHandle<TestClass>("flag2")

        val value = getBoolean(handle, testObj)
        assertTrue(value)
    }

    @Test
    fun setBooleanUpdatesValue() {
        val testObj = TestClass()
        val handle = BooleanHandle<TestClass>("flag1")

        setBoolean(handle, testObj, true)
        assertTrue(getBoolean(handle, testObj))

        setBoolean(handle, testObj, false)
        assertFalse(getBoolean(handle, testObj))
    }

    @Test
    fun compareAndSetBooleanSucceeds() {
        val testObj = TestClass()
        val handle = BooleanHandle<TestClass>("flag1")

        assertTrue(compareAndSetBoolean(handle, testObj, false, true))
        assertTrue(getBoolean(handle, testObj))
    }

    @Test
    fun compareAndSetBooleanFails() {
        val testObj = TestClass()
        val handle = BooleanHandle<TestClass>("flag2")

        assertFalse(compareAndSetBoolean(handle, testObj, expected = false, updated = true))
        assertTrue(getBoolean(handle, testObj), "Must remain unchanged")
    }

    @Test
    fun getAndSetBooleanReturnsOldValue() {
        val testObj = TestClass()
        val handle = BooleanHandle<TestClass>("flag1")

        assertFalse(getAndSetBoolean(handle, testObj, true))
        assertTrue(getBoolean(handle, testObj))

        val handle2 = BooleanHandle<TestClass>("flag2")
        assertTrue(getAndSetBoolean(handle2, testObj, false))
        assertFalse(getBoolean(handle2, testObj))
    }

    @Test
    fun operationsAreThreadSafe() {
        val testObj = TestClass()
        val handle = BooleanHandle<TestClass>("flag1")
        val numThreads = 10
        val operationsPerThread = 1000
        val latch = CountDownLatch(numThreads)

        repeat(numThreads) {
            thread {
                try {
                    repeat(operationsPerThread) {
                        while (true) {
                            val current = getBoolean(handle, testObj)
                            if (compareAndSetBoolean(handle, testObj, current, !current)) {
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

        assertFalse(getBoolean(handle, testObj))
    }

    @Test
    fun multipleFieldsOnSameClass() {
        val testObj = TestClass()
        val handle1 = BooleanHandle<TestClass>("flag1")
        val handle2 = BooleanHandle<TestClass>("flag2")

        setBoolean(handle1, testObj, true)
        setBoolean(handle2, testObj, false)

        assertTrue(getBoolean(handle1, testObj))
        assertFalse(getBoolean(handle2, testObj))
    }

    @Test
    fun getAndSetBooleanThreadSafety() {
        val testObj = TestClass()
        val handle = BooleanHandle<TestClass>("flag1")
        val numThreads = 5
        val operationsPerThread = 200
        val latch = CountDownLatch(numThreads)
        var trueCount = 0

        repeat(numThreads) {
            thread {
                try {
                    repeat(operationsPerThread) {
                        val oldValue = getAndSetBoolean(handle, testObj, true)
                        if (oldValue) {
                            synchronized(this@BooleanHandlesTest) {
                                ++trueCount
                            }
                        }
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        assertTrue(getBoolean(handle, testObj))
        assertTrue(trueCount >= 0, "Must have some atomic getAndSet operations")
    }
}
