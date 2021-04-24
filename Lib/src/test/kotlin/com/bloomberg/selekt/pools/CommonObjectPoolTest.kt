/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.pools

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import java.lang.IllegalArgumentException
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.junit.JUnitAsserter.assertSame

internal class CommonObjectPoolTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(20L, TimeUnit.SECONDS))

    private lateinit var pool: CommonObjectPool<String, PooledObject>
    private val other: SingleObjectPool<String, PooledObject> = mock()
    private val executor = Executors.newSingleThreadScheduledExecutor {
        Thread(it).apply {
            isDaemon = true
        }
    }
    private val configuration = PoolConfiguration(
        evictionDelayMillis = 5_000L,
        evictionIntervalMillis = 20_000L,
        maxTotal = 10
    )

    @Before
    fun setUp() {
        val factory = object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = Unit

            override fun makePrimaryObject() = PooledObject(isPrimary = true)

            override fun makeObject() = PooledObject()
        }
        pool = CommonObjectPool(
            factory,
            executor,
            configuration,
            other
        )
    }

    @After
    fun tearDown() {
        pool.close()
        executor.shutdown()
    }

    @Test
    fun requiresAtLeastOneObject() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            CommonObjectPool(
                mock(),
                mock(),
                configuration.copy(maxTotal = 0),
                other
            )
        }
    }

    @Test
    fun sameObject() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        assertSame("Pool must return the same object.", obj, borrowObject().also { returnObject(it) })
    }

    @Test
    fun firstInFirstOutUnmatched() = pool.run {
        val first = borrowObject()
        thread {
            returnObject(borrowObject())
        }.join()
        returnObject(first)
        assertNotSame(first, borrowObject("not").also { returnObject(it) }, "Pool must return the first object.")
    }

    @Test
    fun sameObjectForKey() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        thread { borrowObject().also { returnObject(it) } }.join()
        assertSame("Pool must return the same object.", obj, borrowObject().also { returnObject(it) })
    }

    @Test
    fun sameObjectAfterEvictWithNoneIdle() = pool.run {
        val obj = borrowObject()
        evict()
        returnObject(obj)
        assertSame("Pool must return the same object.", obj, borrowObject().also { returnObject(it) })
    }

    @Test
    fun sameObjectAfterFailedEviction() = pool.run {
        val obj = borrowObject().also {
            returnObject(it)
            returnObject(borrowObject())
        }
        evict()
        assertSame("Pool must return the same object.", obj, borrowObject().also { returnObject(it) })
    }

    @Test
    fun sameSingleObjectForNewKey() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        assertSame("Pool must return the same object.", obj, borrowObject("not").also { returnObject(it) })
    }

    @Test
    fun oldObjectForNewKey() = pool.run {
        val obj = borrowObject()
        val executor = Executors.newSingleThreadExecutor()
        val other = executor.submit<PooledObject> { borrowObject() }.get()
        returnObject(obj)
        executor.submit { returnObject(other) }.get()
        assertSame("Pool must not return the same object.", obj, borrowObject("not").also { returnObject(it) })
        executor.shutdown()
    }

    @Test
    fun newObjectAfterSuccessfulEviction() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        evict()
        evict()
        assertNotSame(obj, borrowObject().also { returnObject(it) }, "Pool must not return the same object.")
    }

    @Test
    fun newObjectAfterSuccessfulEvictions() = pool.run {
        val first = borrowObject()
        thread { returnObject(borrowObject()) }.join()
        returnObject(first)
        evict()
        evict()
        val third = borrowObject().also { returnObject(it) }
        assertNotSame(first, third, "Pool must not return the same object.")
    }

    @Test
    fun interleavedSequentialBorrow() = pool.run {
        val initial = borrowObject().also {
            val other = borrowObject()
            it.key = "first"
            returnObject(it)
            returnObject(other)
        }
        repeat(100) { i ->
            val first = borrowObject("first")
            assertSame("Pool must return same object on iteration $i", initial, first)
            val second = borrowObject()
            returnObject(first)
            returnObject(second)
        }
    }

    @Test
    fun borrowCanSteal() = pool.run {
        val obj = borrowObject()
        lateinit var key: String
        thread {
            key = Thread.currentThread().id.toString()
            val other = borrowObject()
            thread {
                borrowObject().also { returnObject(it) }
            }.join()
            returnObject(other)
        }.join()
        returnObject(obj)
        assertNotSame(obj, borrowObject(key), "Pool must not return same object.")
    }

    @Test
    fun evictsAll() = pool.run {
        val executors = Array(5) { Executors.newSingleThreadExecutor() }
        val objects = executors.map {
            it.submit<PooledObject> { borrowObject() }.get()
        }
        executors.forEachIndexed { i, it ->
            it.submit { returnObject(objects[i]) }.get()
        }
        evict()
        evict()
        executors.forEach {
            it.submit {
                assertFalse(borrowObject() in objects)
            }.get().apply { it.shutdown() }
        }
    }

    @Test
    fun scheduledEviction(): Unit = CommonObjectPool(object : IObjectFactory<PooledObject> {
        override fun close() = Unit

        override fun destroyObject(obj: PooledObject) = Unit

        override fun makePrimaryObject() = PooledObject(isPrimary = true)

        override fun makeObject() = PooledObject()
    }, executor, configuration.copy(
        evictionDelayMillis = 0L,
        evictionIntervalMillis = 50L
    ), other).use {
        val obj = it.borrowObject().apply { it.returnObject(this) }
        Thread.sleep(100L)
        assertNotSame(obj, it.borrowObject(), "Object was not evicted.")
    }

    @Test
    fun scheduledEvictionFailsWithUse() = CommonObjectPool(object : IObjectFactory<PooledObject> {
        override fun close() = Unit

        override fun destroyObject(obj: PooledObject) = Unit

        override fun makePrimaryObject() = PooledObject(isPrimary = true)

        override fun makeObject() = PooledObject()
    }, executor, configuration.copy(evictionIntervalMillis = 200L), other).use {
        val obj = it.borrowObject().apply {
            it.returnObject(this)
            it.returnObject(it.borrowObject())
        }
        Thread.sleep(300L)
        assertSame("Object was evicted.", obj, it.borrowObject())
    }

    @Test
    fun throwsOnBorrowAfterClose(): Unit = pool.run {
        close()
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy {
            borrowObject()
        }
    }

    @Test
    fun closeDestroys() {
        val factory = mock<IObjectFactory<PooledObject>>()
        val obj = PooledObject()
        whenever(factory.makeObject()) doReturn obj
        CommonObjectPool(factory, executor, configuration, other).use {
            it.borrowObject().run { it.returnObject(this) }
        }
        Thread.sleep(100L)
        verify(factory, times(1)).destroyObject(same(obj))
    }

    @Test
    fun closePreservesInterrupt(): Unit = pool.run {
        Thread.currentThread().interrupt()
        assertDoesNotThrow {
            close()
        }
        assertTrue(Thread.currentThread().isInterrupted)
    }

    @Test
    fun borrowReturnBorrowCloseThenReturn() {
        val factory = mock<IObjectFactory<PooledObject>>()
        val obj = PooledObject()
        whenever(factory.makeObject()) doReturn obj
        CommonObjectPool(factory, executor, configuration, other).use {
            it.borrowObject()
            it.returnObject(obj)
            it.borrowObject()
            it.close()
            Thread.sleep(500L)
            it.returnObject(obj)
            Thread.sleep(100L)
            verify(factory, times(1)).destroyObject(same(obj))
        }
    }

    @Test
    fun borrowTwiceThenCloseInterrupts(): Unit = pool.run {
        borrowObject()
        val thread = thread {
            assertThatExceptionOfType(InterruptedException::class.java).isThrownBy {
                borrowObject()
            }
        }
        Thread.sleep(100L)
        close()
        thread.join()
    }

    @Test
    fun borrowSecondaryWithoutReturnThenPrimary(): Unit = pool.run {
        val obj = PooledObject()
        whenever(other.borrowObjectOrNull()) doReturn obj
        repeat(configuration.maxTotal) { borrowObject() }
        verifyZeroInteractions(other)
        borrowObject()
        verify(other, times(1)).borrowObjectOrNull()
    }

    @Test
    fun awaitCanBeInterrupted(): Unit = pool.run {
        repeat(configuration.maxTotal) { borrowObject() }
        Thread.currentThread().interrupt()
        assertThatExceptionOfType(InterruptedException::class.java).isThrownBy {
            borrowObject()
        }
    }

    @Test
    fun borrowCanBeInterrupted(): Unit = pool.run {
        Thread.currentThread().interrupt()
        assertThatExceptionOfType(InterruptedException::class.java).isThrownBy {
            borrowObject()
        }
    }

    @Test
    fun returnOnAnotherThread(): Unit = pool.run {
        borrowObject().let {
            thread { returnObject(it) }.join()
        }
    }

    @Test
    fun returnOnInterruptedThreadReturns(): Unit = pool.run {
        borrowObject().let {
            Thread.currentThread().interrupt()
            assertDoesNotThrow {
                returnObject(it)
            }
        }
        Thread.interrupted()
        assertFalse(Thread.currentThread().isInterrupted)
        borrowObject()
    }

    @Test
    fun concurrentAccess() = pool.run {
        val objects = Collections.synchronizedSet(mutableSetOf<PooledObject>())
        Array(2 * configuration.maxTotal) {
            thread {
                repeat(1_000) {
                    borrowObject().also {
                        objects.add(it)
                        Thread.sleep(1L)
                        returnObject(it)
                    }
                }
            }
        }.forEach { it.join() }
        assertTrue(objects.isNotEmpty())
        assertTrue(objects.size <= configuration.maxTotal,
            "Expected at most ${configuration.maxTotal}, found ${objects.size}.")
    }

    @Test
    fun concurrentEviction(): Unit = pool.run {
        val evictionThread = thread {
            repeat(100_000) { evict() }
        }
        Array(configuration.maxTotal + 1) {
            thread {
                repeat(1_000) {
                    borrowObject().also {
                        Thread.sleep(1L)
                        returnObject(it)
                    }
                }
            }
        }.forEach { it.join() }
        evictionThread.join()
    }

    @Test
    fun clearHighPriorityEvictsIdle(): Unit = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        clear(Priority.HIGH)
        Thread.sleep(200L)
        assertNotSame(obj, borrowObject())
    }

    @Test
    fun clearLowPriorityKeepsIdle(): Unit = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        clear(Priority.LOW)
        Thread.sleep(200L)
        assertSame(obj, borrowObject())
    }

    @Test
    fun clearLowPriorityReleasesMemoryFromEach() {
        val obj = mock<PooledObject>()
        SingleObjectPool(object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = Unit

            override fun makeObject() = obj

            override fun makePrimaryObject() = obj
        }, executor, Long.MAX_VALUE, Long.MAX_VALUE).use {
            it.borrowObject().apply { it.returnObject(this) }
            it.clear(Priority.LOW)
            Thread.sleep(200L)
            verify(obj, times(1)).releaseMemory()
        }
    }
}

internal class CommonObjectPoolAsSingleObjectPoolTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(30L, TimeUnit.SECONDS))

    private val other: SingleObjectPool<String, PooledObject> = mock()
    private lateinit var pool: CommonObjectPool<String, PooledObject>
    private val configuration = PoolConfiguration(
        evictionDelayMillis = 5_000L,
        evictionIntervalMillis = 20_000L,
        maxTotal = 2
    )

    @Before
    fun setUp() {
        pool = CommonObjectPool(
            object : IObjectFactory<PooledObject> {
                override fun close() = Unit

                override fun destroyObject(obj: PooledObject) = Unit

                override fun makePrimaryObject() = PooledObject(isPrimary = true)

                override fun makeObject() = PooledObject()
            },
            Executors.newSingleThreadScheduledExecutor {
                Thread(it).apply {
                    isDaemon = true
                    priority = Thread.MIN_PRIORITY
                }
            },
            configuration,
            other
        )
    }

    @Test
    fun concurrentAccess() = pool.run {
        val key = "abc"
        val obj = borrowObject(key).also { returnObject(it) }
        Array(2 * configuration.maxTotal) {
            thread {
                val localKey = key + Thread.currentThread().id
                repeat(1_000) {
                    assertSame("Pool must return the same object.",
                        obj, borrowObject(localKey).also {
                            Thread.sleep(1L)
                            returnObject(it)
                        }
                    )
                }
            }
        }.forEach { it.join() }
    }
}
