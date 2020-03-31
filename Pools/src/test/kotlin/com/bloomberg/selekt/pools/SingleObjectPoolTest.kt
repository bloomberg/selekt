/*
 * Copyright 2020 Bloomberg Finance L.P.
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
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.rules.DisableOnDebug
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.junit.JUnitAsserter.assertSame

internal class SingleObjectPoolTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(org.junit.rules.Timeout(10L, TimeUnit.SECONDS))

    private lateinit var pool: SingleObjectPool<String, KeyedObject>
    private val executor = Executors.newSingleThreadScheduledExecutor {
        Thread(it).apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY
        }
    }

    @BeforeEach
    fun setUp() {
        pool = SingleObjectPool(object : IObjectFactory<KeyedObject> {
            override fun activateObject(obj: KeyedObject) = Unit

            override fun close() = Unit

            override fun destroyObject(obj: KeyedObject) = Unit

            override fun gauge(): FactoryGauge = mock()

            override fun makeObject() = KeyedObject()

            override fun makePrimaryObject() = makeObject()

            override fun passivateObject(obj: KeyedObject) = Unit

            override fun validateObject(obj: KeyedObject) = true
        }, executor, 1_000L, 20_000L)
    }

    @AfterEach
    fun tearDown() {
        pool.close()
        executor.shutdown()
    }

    @Test
    fun sameObject() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
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
    fun newObjectAfterSuccessfulEviction() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        evict()
        assertNotSame(obj, borrowObject().also { returnObject(it) }, "Pool must not return the same object.")
    }

    @Test
    @Timeout(value = 5L, unit = TimeUnit.SECONDS)
    fun scheduledEviction(): Unit = SingleObjectPool(object : IObjectFactory<KeyedObject> {
        override fun activateObject(obj: KeyedObject) = Unit

        override fun close() = Unit

        override fun destroyObject(obj: KeyedObject) = Unit

        override fun gauge(): FactoryGauge = mock()

        override fun makeObject() = makePrimaryObject()

        override fun makePrimaryObject() = KeyedObject()

        override fun passivateObject(obj: KeyedObject) = Unit

        override fun validateObject(obj: KeyedObject) = true
    }, executor, 1_000L, 1_000L).use {
        val obj = it.borrowObject().apply { it.returnObject(this) }
        Thread.sleep(2_000L)
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
    @Timeout(value = 1L, unit = TimeUnit.SECONDS)
    fun secondEvictionWhileBorrowingFails() {
        val factory = mock<IObjectFactory<IKeyedObject<String>>>()
        whenever(factory.makePrimaryObject()) doReturn KeyedObject()
        whenever(factory.validateObject(any())) doReturn true
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
    @Timeout(value = 5L, unit = TimeUnit.SECONDS)
    fun scheduledEvictionFailsFollowingFurtherUse(): Unit = SingleObjectPool(object : IObjectFactory<IKeyedObject<String>> {
        override fun activateObject(obj: IKeyedObject<String>) = Unit

        override fun close() = Unit

        override fun destroyObject(obj: IKeyedObject<String>) = Unit

        override fun gauge(): FactoryGauge = mock()

        override fun makeObject() = makePrimaryObject()

        override fun makePrimaryObject() = KeyedObject()

        override fun passivateObject(obj: IKeyedObject<String>) = Unit

        override fun validateObject(obj: IKeyedObject<String>) = true
    }, executor, 1_000L, 1_000L).use {
        val obj = it.borrowObject().apply {
            it.returnObject(this)
            it.returnObject(it.borrowObject())
        }
        Thread.sleep(1_500L)
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
    @Timeout(value = 1L, unit = TimeUnit.SECONDS)
    fun borrowCloseThenReturn(): Unit = pool.run {
        returnObject(borrowObject().also { close() })
    }

    @Test
    fun closeDestroys() {
        val factory = mock<IObjectFactory<IKeyedObject<String>>>().apply {
            whenever(validateObject(any())) doReturn true
        }
        val obj = KeyedObject()
        whenever(factory.makePrimaryObject()) doReturn obj
        SingleObjectPool(factory, executor, 5_000L, 20_000L).use {
            it.borrowObject().run { it.returnObject(this) }
        }
        Thread.sleep(100L)
        verify(factory, times(1)).destroyObject(same(obj))
    }

    @Test
    fun borrowReturnBorrowCloseThenReturn() {
        val factory = mock<IObjectFactory<IKeyedObject<String>>>()
        val obj = KeyedObject()
        whenever(factory.makePrimaryObject()) doReturn obj
        whenever(factory.validateObject(any())) doReturn true
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
    @Timeout(value = 2L, unit = TimeUnit.SECONDS)
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
    @Timeout(value = 1L, unit = TimeUnit.SECONDS)
    fun returnOnAnotherThread(): Unit = pool.run {
        borrowObject().let {
            thread { returnObject(it) }.join()
        }
    }

    @Test
    @Timeout(value = 3L, unit = TimeUnit.SECONDS)
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
    @Timeout(value = 10L, unit = TimeUnit.SECONDS)
    fun concurrentAccess() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        runBlocking(Dispatchers.IO) {
            coroutineScope {
                repeat(4) {
                    launch {
                        repeat(100_000) {
                            assertSame("Pool must return the same object.",
                                obj, borrowObject().also { returnObject(it) })
                        }
                    }
                }
            }
        }
    }

    @Test
    @Timeout(value = 10L, unit = TimeUnit.SECONDS)
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
}
