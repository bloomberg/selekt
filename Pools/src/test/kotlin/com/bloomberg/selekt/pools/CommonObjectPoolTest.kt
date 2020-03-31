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
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.rules.DisableOnDebug
import java.lang.IllegalArgumentException
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue
import kotlin.test.junit.JUnitAsserter.assertSame

internal class CommonObjectPoolTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(org.junit.rules.Timeout(10L, TimeUnit.SECONDS))

    private lateinit var pool: CommonObjectPool<String, KeyedObject>
    private val executor = Executors.newSingleThreadScheduledExecutor {
        Thread(it).apply {
            isDaemon = true
            priority = Thread.MIN_PRIORITY
        }
    }
    private val configuration = PoolConfiguration(
        evictionDelayMillis = 5_000L,
        evictionIntervalMillis = 20_000L,
        maxTotal = 10
    )

    @BeforeEach
    fun setUp() {
        pool = CommonObjectPool(
            object : IObjectFactory<KeyedObject> {
                override fun activateObject(obj: KeyedObject) = Unit

                override fun close() = Unit

                override fun destroyObject(obj: KeyedObject) = Unit

                override fun gauge(): FactoryGauge = mock()

                override fun makePrimaryObject() = KeyedObject(isPrimary = true)

                override fun makeObject() = KeyedObject()

                override fun passivateObject(obj: KeyedObject) = Unit

                override fun validateObject(obj: KeyedObject) = true
            },
            executor,
            configuration
        )
    }

    @AfterEach
    fun tearDown() {
        pool.close()
        executor.shutdown()
    }

    @Test
    fun requiresAtLeastTwoObjects() {
        assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
            CommonObjectPool(
                mock<IObjectFactory<KeyedObject>>(),
                mock(),
                configuration.copy(maxTotal = 1)
            )
        }
    }

    @Test
    fun samePrimaryObject() = pool.run {
        val obj = borrowPrimaryObject().also { returnObject(it) }
        assertSame("Pool must return the same object.", obj, borrowPrimaryObject().also { returnObject(it) })
    }

    @Test
    fun sameSecondaryObject() = pool.run {
        val obj = borrowObject().also { returnObject(it) }
        assertSame("Pool must return the same object.", obj, borrowObject().also { returnObject(it) })
    }

    @Test
    fun lastInFirstOut() = pool.run {
        val first = borrowObject()
        thread {
            returnObject(borrowObject())
        }.join()
        returnObject(first)
        assertSame("Pool must return the first object.", first, borrowObject("not").also { returnObject(it) })
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
    fun newObjectForNewKey() = pool.run {
        val obj = borrowObject()
        val executor = Executors.newSingleThreadExecutor()
        val other = executor.submit<KeyedObject> { borrowObject() }.get()
        returnObject(obj)
        executor.submit { returnObject(other) }.get()
        assertNotSame(obj, borrowObject("not").also { returnObject(it) }, "Pool must not return the same object.")
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
            it.submit<KeyedObject> { borrowObject() }.get()
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
    @Timeout(value = 5L, unit = TimeUnit.SECONDS)
    fun scheduledEviction(): Unit = CommonObjectPool(object : IObjectFactory<KeyedObject> {
        override fun activateObject(obj: KeyedObject) = Unit

        override fun close() = Unit

        override fun destroyObject(obj: KeyedObject) = Unit

        override fun gauge(): FactoryGauge = mock()

        override fun makePrimaryObject() = KeyedObject(isPrimary = true)

        override fun makeObject() = KeyedObject()

        override fun passivateObject(obj: KeyedObject) = Unit

        override fun validateObject(obj: KeyedObject) = true
    }, executor, configuration.copy(
        evictionDelayMillis = 0L,
        evictionIntervalMillis = 50L
    )).use {
        val obj = it.borrowObject().apply { it.returnObject(this) }
        Thread.sleep(60L)
        assertNotSame(obj, it.borrowObject(), "Object was not evicted.")
    }

    @Test
    @Timeout(value = 5L, unit = TimeUnit.SECONDS)
    fun scheduledEvictionFailsWithUse() = CommonObjectPool(object : IObjectFactory<KeyedObject> {
        override fun activateObject(obj: KeyedObject) = Unit

        override fun close() = Unit

        override fun destroyObject(obj: KeyedObject) = Unit

        override fun gauge(): FactoryGauge = mock()

        override fun makePrimaryObject() = KeyedObject(isPrimary = true)

        override fun makeObject() = KeyedObject()

        override fun passivateObject(obj: KeyedObject) = Unit

        override fun validateObject(obj: KeyedObject) = true
    }, executor, configuration.copy(evictionIntervalMillis = 200L)).use {
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
        val factory = mock<IObjectFactory<KeyedObject>>()
        val primaryObj = KeyedObject(isPrimary = true)
        val secondaryObj = KeyedObject()
        whenever(factory.makePrimaryObject()) doReturn primaryObj
        whenever(factory.makeObject()) doReturn secondaryObj
        whenever(factory.validateObject(any())) doReturn true
        CommonObjectPool(factory, executor, configuration).use {
            val obj = it.borrowPrimaryObject()
            it.borrowObject().run { it.returnObject(this) }
            it.returnObject(obj)
        }
        Thread.sleep(100L)
        verify(factory, times(1)).destroyObject(same(primaryObj))
        verify(factory, times(1)).destroyObject(same(secondaryObj))
    }

    @Test
    fun borrowReturnBorrowCloseThenReturn() {
        val factory = mock<IObjectFactory<KeyedObject>>()
        val obj = KeyedObject()
        whenever(factory.makeObject()) doReturn obj
        CommonObjectPool(factory, executor, configuration).use {
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
    @Timeout(value = 2L, unit = TimeUnit.SECONDS)
    fun borrowSecondaryThenPrimary(): Unit = pool.run {
        val primaryObject = borrowPrimaryObject()
        val allSecondaryObjects = Array(configuration.maxTotal - 1) { borrowObject() }
        returnObject(primaryObject)
        assertFalse(allSecondaryObjects.contains(primaryObject))
        assertSame(
            "Last secondary object must be the primary object.",
            primaryObject,
            borrowObject()
        )
    }

    @Test
    fun borrowSecondaryWithoutReturnThenPrimary(): Unit = pool.run {
        val primaryObject = borrowPrimaryObject().also { returnObject(it) }
        val allSecondaryObjects = Array(configuration.maxTotal - 1) { borrowObject() }
        assertFalse(allSecondaryObjects.last().isPrimary)
        assertSame(
            "Secondary object must be allowed to be the primary object.",
            primaryObject,
            borrowObject()
        )
    }

    @Test
    fun borrowSecondaryCanCreatePrimary(): Unit = pool.run {
        repeat(configuration.maxTotal - 1) { borrowObject() }
        val lastObject = borrowObject().also { returnObject(it) }
        val primaryObject = borrowPrimaryObject()
        assertSame(
            "Secondary object must be allowed to be the new primary object.",
            primaryObject,
            lastObject
        )
    }

    @Test
    @Timeout(value = 1L, unit = TimeUnit.SECONDS)
    fun returnOnAnotherThread(): Unit = pool.run {
        borrowObject().let {
            thread { returnObject(it) }.join()
        }
    }

    @Test
    @Timeout(value = 15L, unit = TimeUnit.SECONDS)
    fun concurrentAccess() = pool.run {
        val objects = Collections.synchronizedSet(mutableSetOf<KeyedObject>())
        Array(2 * configuration.maxTotal - 1) {
            thread {
                repeat(1_000) {
                    borrowObject().also {
                        objects.add(it)
                        Thread.sleep(1L)
                        returnObject(it)
                    }
                }
            }
        }.plus(
            thread {
                repeat(1_000) {
                    borrowPrimaryObject().also {
                        objects.add(it)
                        Thread.sleep(1L)
                        returnObject(it)
                    }
                }
            }
        ).forEach { it.join() }
        assertTrue(objects.isNotEmpty())
        assertTrue(objects.size <= configuration.maxTotal,
            "Expected at most ${configuration.maxTotal}, found ${objects.size}.")
    }

    @Test
    @Timeout(value = 15L, unit = TimeUnit.SECONDS)
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
        }.plus(
            thread {
                repeat(1_000) {
                    borrowPrimaryObject().also {
                        Thread.sleep(1L)
                        returnObject(it)
                    }
                }
            }
        ).forEach { it.join() }
        evictionThread.join()
    }
}

internal class CommonObjectPoolAsSingleObjectPoolTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(org.junit.rules.Timeout(30L, TimeUnit.SECONDS))

    private lateinit var pool: CommonObjectPool<String, KeyedObject>
    private val configuration = PoolConfiguration(
        evictionDelayMillis = 5_000L,
        evictionIntervalMillis = 20_000L,
        maxTotal = 2
    )

    @BeforeEach
    fun setUp() {
        pool = CommonObjectPool(
            object : IObjectFactory<KeyedObject> {
                override fun activateObject(obj: KeyedObject) = Unit

                override fun close() = Unit

                override fun destroyObject(obj: KeyedObject) = Unit

                override fun gauge(): FactoryGauge = mock()

                override fun makePrimaryObject() = KeyedObject(isPrimary = true)

                override fun makeObject() = KeyedObject()

                override fun passivateObject(obj: KeyedObject) = Unit

                override fun validateObject(obj: KeyedObject) = true
            },
            Executors.newSingleThreadScheduledExecutor {
                Thread(it).apply {
                    isDaemon = true
                    priority = Thread.MIN_PRIORITY
                }
            },
            configuration
        )
    }

    @Test
    @Timeout(value = 20L, unit = TimeUnit.SECONDS)
    fun concurrentAccess() = pool.run {
        val key = "abc"
        val obj = borrowObject(key).also { returnObject(it) }
        Array(2 * configuration.maxTotal - 1) {
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
        }.plus(
            thread {
                repeat(1_000) {
                    assertSame("Pool must return the same object.",
                        obj, borrowPrimaryObject().also {
                            Thread.sleep(1L)
                            returnObject(it)
                        }
                    )
                }
            }
        ).forEach { it.join() }
    }
}
