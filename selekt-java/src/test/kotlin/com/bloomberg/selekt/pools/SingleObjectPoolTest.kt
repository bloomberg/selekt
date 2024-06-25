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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SingleObjectPoolTest {
    private lateinit var pool: SingleObjectPool<String, PooledObject>
    private val executor = Executors.newSingleThreadScheduledExecutor {
        Thread(it).apply {
            isDaemon = true
        }
    }

    @BeforeEach
    fun setUp() {
        pool = SingleObjectPool(object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = Unit

            override fun makeObject() = PooledObject()

            override fun makePrimaryObject() = makeObject()
        }, executor, 1_000L, 20_000L)
    }

    @AfterEach
    fun tearDown() {
        pool.close()
        executor.shutdown()
    }

    @Test
    fun semaphoreExcessiveAcquisition() {
        Semaphore(Int.MAX_VALUE).let {
            it.acquire()
            assertEquals(Int.MAX_VALUE - 1, it.availablePermits())
            assertFalse(it.tryAcquire(Int.MAX_VALUE))
            assertEquals(Int.MAX_VALUE - 1, it.availablePermits())
        }
    }

    @Test
    fun semaphoreExcessiveAcquisitionWithoutDelay() {
        Semaphore(Int.MAX_VALUE).let {
            it.acquire()
            assertEquals(Int.MAX_VALUE - 1, it.availablePermits())
            assertFalse(it.tryAcquire(Int.MAX_VALUE, 0L, TimeUnit.MILLISECONDS))
            assertEquals(Int.MAX_VALUE - 1, it.availablePermits())
        }
    }

    @Test
    fun sameObject() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
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
    fun earlyInitialEvictionFails() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        evict()
        assertSame(obj, borrowObject().also { returnObject(it) }, "Pool must return the same object.")
    }

    @Test
    fun evictionAfterSuccessfulEvictionFails() = pool.run {
        val one = borrowObject().also { returnObject(it) }
        evict()
        evict()
        val two = borrowObject().also { returnObject(it) }
        assertNotSame(one, two)
        evict()
        assertSame(two, borrowObject().also { returnObject(it) }, "Pool must return the same object.")
    }

    @Test
    fun evictionNegativeIntervalFails() = SingleObjectPool(object : IObjectFactory<PooledObject> {
        override fun close() = Unit

        override fun destroyObject(obj: PooledObject) = Unit

        override fun makeObject() = makePrimaryObject()

        override fun makePrimaryObject() = PooledObject()
    }, executor, 100L, -1L).use {
        val obj = it.borrowObject().apply { it.returnObject(this) }
        Thread.sleep(500L)
        assertSame(obj, it.borrowObject().apply { it.returnObject(this) }, "Pool must return the same object.")
    }

    @Test
    fun newObjectAfterSuccessfulEviction() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        evict()
        evict()
        assertNotSame(obj, borrowObject().also { returnObject(it) }, "Pool must not return the same object.")
    }

    @Test
    fun scheduledEviction(): Unit = SingleObjectPool(object : IObjectFactory<PooledObject> {
        override fun close() = Unit

        override fun destroyObject(obj: PooledObject) = Unit

        override fun makeObject() = makePrimaryObject()

        override fun makePrimaryObject() = PooledObject()
    }, executor, 500L, 500L).use {
        val obj = it.borrowObject().apply { it.returnObject(this) }
        Thread.sleep(1_500L)
        assertNotSame(obj, it.borrowObject(), "Object was not evicted.")
    }

    @Test
    fun evictionFailsThenSucceeds(): Unit = pool.run {
        val obj = borrowObject().also {
            returnObject(it)
            borrowObject()
            returnObject(it)
        }
        evict()
        evict()
        assertNotSame(obj, borrowObject().also { returnObject(it) }, "Pool must not return the same object.")
    }

    @Test
    fun secondEvictionWhileBorrowingFails() {
        val factory = mock<IObjectFactory<IPooledObject<String>>>()
        whenever(factory.makePrimaryObject()) doReturn PooledObject()
        SingleObjectPool(factory, executor, 5_000L, 20_000L).use {
            val obj = it.borrowObject()
            it.returnObject(obj)
            it.borrowObject()
            it.evict()
            it.evict()
            it.returnObject(obj)
            it.evict()
            assertSame(obj, it.borrowObject(), "Pool must return the same object.")
            verify(factory, times(1)).makePrimaryObject()
            verify(factory, never()).destroyObject(any())
        }
    }

    @Test
    fun scheduledEvictionFailsFollowingFurtherUse(): Unit = SingleObjectPool(object : IObjectFactory<IPooledObject<String>> {
        override fun close() = Unit

        override fun destroyObject(obj: IPooledObject<String>) = Unit

        override fun makeObject() = makePrimaryObject()

        override fun makePrimaryObject() = PooledObject()
    }, executor, 1_000L, 1_000L).use {
        val obj = it.borrowObject().apply {
            it.returnObject(this)
            it.returnObject(it.borrowObject())
        }
        Thread.sleep(1_500L)
        assertSame(obj, it.borrowObject(), "Object was evicted.")
    }

    @Test
    fun throwsOnBorrowAfterClose(): Unit = pool.run {
        close()
        assertFailsWith<IllegalStateException> {
            borrowObject()
        }
    }

    @Test
    fun throwsOnCloseWhileWaiting(): Unit = pool.run {
        val obj = borrowObject()
        thread {
            Thread.sleep(100L)
            returnObject(obj)
        }
        close()
        assertFailsWith<IllegalStateException> {
            borrowObject()
        }
    }

    @Test
    fun borrowCloseThenReturn(): Unit = pool.run {
        returnObject(borrowObject().also { close() })
    }

    @Test
    fun closeDestroys() {
        val factory = mock<IObjectFactory<IPooledObject<String>>>()
        val obj = PooledObject()
        whenever(factory.makePrimaryObject()) doReturn obj
        SingleObjectPool(factory, executor, 5_000L, 20_000L).use {
            it.returnObject(it.borrowObject())
        }
        Thread.sleep(100L)
        verify(factory, times(1)).destroyObject(same(obj))
    }

    @Test
    fun closePreservesInterrupt(): Unit = pool.run {
        Thread.currentThread().interrupt()
        assertDoesNotThrow { close() }
        assertTrue(Thread.currentThread().isInterrupted)
    }

    @Test
    fun borrowReturnBorrowCloseThenReturn() {
        val factory = mock<IObjectFactory<IPooledObject<String>>>()
        val obj = PooledObject()
        whenever(factory.makePrimaryObject()) doReturn obj
        SingleObjectPool(factory, executor, 5_000L, 20_000L).use {
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
    fun borrowTwiceThenCloseNotifiesWaiters(): Unit = pool.run {
        borrowObject()
        thread {
            Thread.sleep(100L)
            close()
        }
        assertFailsWith<IllegalStateException> {
            borrowObject()
        }
    }

    @Test
    fun borrowOrNull(): Unit = pool.run {
        assertNotNull(borrowObjectOrNull())
    }

    @Test
    fun borrowOrNullIsNull(): Unit = pool.run {
        borrowObject()
        assertNull(borrowObjectOrNull())
    }

    @Test
    fun borrowKeyedObject(): Unit = pool.run {
        val obj = borrowObject("").also { returnObject(it) }
        assertSame(obj, borrowObject())
    }

    @Test
    fun returnOnAnotherThread(): Unit = pool.run {
        borrowObject().let {
            thread { returnObject(it) }.join()
        }
    }

    @Test
    fun borrowCanBeInterrupted(): Unit = pool.run {
        borrowObject()
        Thread.currentThread().interrupt()
        assertFailsWith<InterruptedException> {
            borrowObject()
        }
    }

    @Test
    fun borrowEvenAfterExternalInterruptions(): Unit = pool.run {
        val obj = borrowObject()
        val thread = thread { borrowObject() }
        Thread.sleep(250L)
        repeat(5) {
            thread.interrupt()
            Thread.sleep(10L)
        }
        Thread.sleep(100L)
        returnObject(obj)
        thread.join()
    }

    @Test
    fun borrowRecoversFromException() {
        val factory = mock<IObjectFactory<IPooledObject<String>>>()
        whenever(factory.makePrimaryObject()) doThrow IOException()
        SingleObjectPool(factory, executor, 5_000L, 20_000L).use {
            assertFailsWith<IOException> { it.borrowObject() }
            assertFailsWith<IOException> { it.borrowObject() }
        }
    }

    @Test
    fun evictionFailsIfCancelled() {
        val executor = object : ScheduledExecutorService by this@SingleObjectPoolTest.executor {
            override fun scheduleWithFixedDelay(
                command: Runnable,
                initialDelay: Long,
                delay: Long,
                unit: TimeUnit
            ) = this@SingleObjectPoolTest.executor.scheduleAtFixedRate(command, initialDelay, delay, unit).apply {
                cancel(false)
            }
        }
        SingleObjectPool(mock<IObjectFactory<PooledObject>>().apply {
            whenever(makePrimaryObject()) doAnswer Answer { PooledObject() }
        }, executor, 1_000L, 20_000L).use {
            val obj = it.borrowObject().apply { it.returnObject(this) }
            it.evict()
            it.evict()
            assertSame(obj, it.borrowObject())
        }
    }

    @Test
    fun closeNotifiesAllWaiters(): Unit = pool.run {
        borrowObject()
        thread {
            Thread.sleep(100L)
            close()
        }
        assertFailsWith<IllegalStateException> {
            borrowObject()
        }
    }

    @Test
    fun borrowAsClosingDoesNotScheduleEviction() {
        val executor = mock<ScheduledExecutorService>()
        val pool = SingleObjectPool(object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = Unit

            override fun makeObject() = PooledObject().also {
                pool.close()
            }

            override fun makePrimaryObject() = makeObject()
        }, executor, 1_000L, 20_000L)
        pool.use {
            it.borrowObject()
            verify(executor, never()).scheduleAtFixedRate(any(), any(), any(), any())
        }
    }

    @Test
    fun interruptBorrowerThenReturn(): Unit = pool.run {
        borrowObject().let {
            Thread.interrupted()
            assertDoesNotThrow {
                returnObject(it)
            }
        }
    }

    @Test
    fun interruptBorrowerThenBorrow(): Unit = pool.run {
        borrowObject()
        Thread.currentThread().interrupt()
        assertFailsWith<InterruptedException> {
            borrowObject()
        }
    }

    @Test
    fun concurrentAccess() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        runBlocking(Dispatchers.IO) {
            coroutineScope {
                repeat(4) {
                    launch {
                        repeat(100_000) {
                            assertSame(
                                obj,
                                borrowObject().also { returnObject(it) },
                                "Pool must return the same object."
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun concurrentEvict(): Unit = pool.run {
        runBlocking(Dispatchers.IO) {
            coroutineScope {
                launch {
                    repeat(100_000) { evict() }
                }
                launch {
                    repeat(100_000) { borrowObject().also { returnObject(it) } }
                }
            }
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

    @Test
    fun evictLowPriorityWhenEmptyDoesNotThrow(): Unit = pool.run {
        assertDoesNotThrow {
            evict(Priority.LOW)
        }
    }

    @Test
    fun factoryDestroyObjectIOExceptionPropagates() {
        val obj = mock<PooledObject>()
        SingleObjectPool(object : IObjectFactory<PooledObject> {
            override fun close() = Unit

            override fun destroyObject(obj: PooledObject) = throw IOException("Oh no!")

            override fun makeObject() = obj

            override fun makePrimaryObject() = obj
        }, executor, Long.MAX_VALUE, Long.MAX_VALUE).use {
            it.borrowObject().apply { it.returnObject(this) }
            assertFailsWith<IOException> {
                it.evict(Priority.HIGH)
            }
        }
    }
}
