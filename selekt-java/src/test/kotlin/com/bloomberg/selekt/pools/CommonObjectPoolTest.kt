/*
 * Copyright 2020 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.pools

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import java.io.IOException
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class CommonObjectPoolTest {
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

    @BeforeEach
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

    @AfterEach
    fun tearDown() {
        pool.close()
        executor.shutdown()
    }

    @Test
    fun requiresAtLeastOneObject() {
        assertFailsWith<IllegalArgumentException> {
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
        assertSame(obj, borrowObject().also { returnObject(it) }, "Pool must return the same object.")
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
        assertSame(obj, borrowObject().also { returnObject(it) }, "Pool must return the same object.")
    }

    @Test
    fun sameObjectAfterEvictWithNoneIdle() = pool.run {
        val obj = borrowObject()
        evict()
        returnObject(obj)
        assertSame(obj, borrowObject().also { returnObject(it) }, "Pool must return the same object.")
    }

    @Test
    fun sameObjectAfterFailedEviction() = pool.run {
        val obj = borrowObject().also {
            returnObject(it)
            returnObject(borrowObject())
        }
        evict()
        assertSame(obj, borrowObject().also { returnObject(it) }, "Pool must return the same object.")
    }

    @Test
    fun sameSingleObjectForNewKey() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        assertSame(obj, borrowObject("not").also { returnObject(it) }, "Pool must return the same object.")
    }

    @Test
    fun oldObjectForNewKey() = pool.run {
        val obj = borrowObject()
        val executor = Executors.newSingleThreadExecutor()
        val other = executor.submit<PooledObject> { borrowObject() }.get()
        returnObject(obj)
        executor.submit { returnObject(other) }.get()
        assertSame(obj, borrowObject("not").also { returnObject(it) }, "Pool must not return the same object.")
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
            assertSame(initial, first, "Pool must return same object on iteration $i")
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
        executors.forEachIndexed { i, value ->
            value.submit { returnObject(objects[i]) }.get()
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
        assertSame(obj, it.borrowObject(), "Object was evicted.")
    }

    @Test
    fun evictionNegativeIntervalFails() = CommonObjectPool(object : IObjectFactory<PooledObject> {
        override fun close() = Unit

        override fun destroyObject(obj: PooledObject) = Unit

        override fun makeObject() = makePrimaryObject()

        override fun makePrimaryObject() = PooledObject()
    }, executor, configuration.copy(evictionIntervalMillis = -1L, evictionDelayMillis = 100L), other).use {
        val obj = it.borrowObject().apply { it.returnObject(this) }
        Thread.sleep(500L)
        assertSame(obj, it.borrowObject().apply { it.returnObject(this) }, "Pool must return the same object.")
    }

    @Test
    fun throwsOnBorrowAfterClose(): Unit = pool.run {
        close()
        assertFailsWith<IllegalStateException> {
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
            assertFailsWith<InterruptedException> {
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
        verifyNoInteractions(other)
        borrowObject()
        verify(other, times(1)).borrowObjectOrNull()
    }

    @Test
    fun awaitCanBeInterrupted(): Unit = pool.run {
        repeat(configuration.maxTotal) { borrowObject() }
        Thread.currentThread().interrupt()
        assertFailsWith<InterruptedException> {
            borrowObject()
        }
    }

    @Test
    fun borrowCanBeInterrupted(): Unit = pool.run {
        Thread.currentThread().interrupt()
        assertFailsWith<InterruptedException> {
            borrowObject()
        }
    }

    @Test
    fun interleavedBorrowSchedulesEvictionIfCancelled() {
        @Suppress("JoinDeclarationAndAssignment")
        lateinit var pool: CommonObjectPool<String, PooledObject>
        val executor = object : ScheduledExecutorService by this@CommonObjectPoolTest.executor {
            var count = 0

            override fun scheduleAtFixedRate(
                command: Runnable,
                initialDelay: Long,
                period: Long,
                unit: TimeUnit
            ) = this@CommonObjectPoolTest.executor.scheduleAtFixedRate(command, initialDelay, period, unit).also {
                if (count++ == 0) {
                    it.cancel(false)
                }
            }
        }
        pool = CommonObjectPool(object : IObjectFactory<PooledObject> {
            private var count = 0

            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = Unit

            override fun makeObject() = PooledObject().also {
                if (count++ == 0) {
                    pool.borrowObject()
                }
            }

            override fun makePrimaryObject() = makeObject()
        }, executor, configuration, mock())
        pool.use {
            it.borrowObject()
            assertEquals(2, executor.count)
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
        assertTrue(
            objects.size <= configuration.maxTotal,
            "Expected at most ${configuration.maxTotal}, found ${objects.size}."
        )
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
    fun evictionFailsIfCancelled() {
        val executor = object : ScheduledExecutorService by this@CommonObjectPoolTest.executor {
            override fun scheduleAtFixedRate(
                command: Runnable,
                initialDelay: Long,
                period: Long,
                unit: TimeUnit
            ) = this@CommonObjectPoolTest.executor.scheduleAtFixedRate(command, initialDelay, period, unit).apply {
                cancel(false)
            }
        }
        CommonObjectPool(mock<IObjectFactory<PooledObject>>().apply {
            whenever(makeObject()) doAnswer Answer { PooledObject() }
        }, executor, configuration, mock()).use {
            val obj = it.borrowObject().apply { it.returnObject(this) }
            it.evict()
            it.evict()
            assertSame(obj, it.borrowObject())
        }
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
    fun clearLowPriorityAfterEvictionAttemptClearsIdle(): Unit = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        evict()
        clear(Priority.LOW)
        Thread.sleep(200L)
        assertNotSame(obj, borrowObject())
    }

    @Test
    fun clearLowPriorityReleasesMemoryFromEach() {
        val obj = mock<PooledObject>()
        CommonObjectPool(object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = Unit

            override fun makeObject() = obj

            override fun makePrimaryObject() = obj
        }, executor, configuration, other).use {
            it.borrowObject().apply { it.returnObject(this) }
            it.clear(Priority.LOW)
            Thread.sleep(200L)
            verify(obj, times(1)).releaseMemory()
        }
    }

    @Test
    fun factoryMakeObjectIOExceptionPropagates() {
        CommonObjectPool(object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = Unit

            override fun makeObject() = throw IOException("Oh no!")

            override fun makePrimaryObject() = throw IOException("Oh no!")
        }, executor, configuration, other).use {
            assertFailsWith<IOException> {
                it.borrowObject()
            }
        }
    }

    @Test
    fun factoryMakeObjectIOExceptionResetsCount() {
        CommonObjectPool(object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = Unit

            override fun makeObject() = throw IOException("Oh no!")

            override fun makePrimaryObject() = throw IOException("Oh no!")
        }, executor, configuration, other).use {
            repeat(configuration.maxTotal + 1) { _ ->
                runCatching { it.borrowObject() }
            }
        }
    }

    @Test
    fun factoryDestroyObjectIOExceptionPropagates() {
        val obj = mock<PooledObject>()
        CommonObjectPool(object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = throw IOException("Oh no!")

            override fun makeObject() = obj

            override fun makePrimaryObject() = obj
        }, executor, configuration, other).use {
            it.borrowObject().apply { it.returnObject(this) }
            assertFailsWith<IOException> {
                it.evict(Priority.HIGH)
            }
        }
    }
}

internal class CommonObjectPoolAsSingleObjectPoolTest {

    private val other: SingleObjectPool<String, PooledObject> = mock()
    private lateinit var pool: CommonObjectPool<String, PooledObject>
    private val configuration = PoolConfiguration(
        evictionDelayMillis = 5_000L,
        evictionIntervalMillis = 20_000L,
        maxTotal = 2
    )

    @BeforeEach
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
                    assertSame(obj, borrowObject(localKey).also {
                        Thread.sleep(1L)
                        returnObject(it)
                    }, "Pool must return the same object.")
                }
            }
        }.forEach { it.join() }
    }
}
