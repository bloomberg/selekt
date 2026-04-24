/*
 * Copyright 2026 Bloomberg Finance L.P.
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

package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class TestResource : SharedResource() {
    val releaseCount = AtomicInteger(0)

    override fun onReleased() {
        releaseCount.incrementAndGet()
    }
}

internal class SharedResourceTest {
    @Test
    fun startsOpen(): Unit = TestResource().run {
        assertTrue(isOpen())
    }

    @Test
    fun releaseClosesResource(): Unit = TestResource().run {
        release()
        assertFalse(isOpen())
        assertEquals(1, releaseCount.get())
    }

    @Test
    fun retainPreventsRelease(): Unit = TestResource().run {
        retain()
        release()
        assertTrue(isOpen())
        assertEquals(0, releaseCount.get())
    }

    @Test
    fun retainThenReleaseTwiceCloses(): Unit = TestResource().run {
        retain()
        release()
        release()
        assertFalse(isOpen())
        assertEquals(1, releaseCount.get())
    }

    @Test
    fun multipleRetainsRequireMatchingReleases(): Unit = TestResource().run {
        retain()
        retain()
        release()
        release()
        assertTrue(isOpen())
        release()
        assertFalse(isOpen())
        assertEquals(1, releaseCount.get())
    }

    @Test
    fun doubleReleaseIsIdempotent(): Unit = TestResource().run {
        release()
        release()
        assertEquals(1, releaseCount.get())
    }

    @Test
    fun retainAfterReleaseThrows(): Unit = TestResource().run {
        release()
        assertFailsWith<IllegalStateException> {
            retain()
        }
    }

    @Test
    fun pledgeRetainsAndReleases(): Unit = TestResource().run {
        val result = pledge { 42 }
        assertEquals(42, result)
        assertTrue(isOpen())
    }

    @Test
    fun pledgeReleasesOnException(): Unit = TestResource().run {
        assertFailsWith<RuntimeException> {
            pledge { throw RuntimeException("Uh-oh!") }
        }
        assertTrue(isOpen())
    }

    @Test
    fun concurrentRetainRelease(): Unit = TestResource().run {
        val threadCount = 100
        val barrier = CyclicBarrier(threadCount)
        val latch = CountDownLatch(threadCount)
        repeat(threadCount) {
            Thread {
                barrier.await()
                retain()
                release()
                latch.countDown()
            }.start()
        }
        latch.await()
        assertTrue(isOpen())
        assertEquals(0, releaseCount.get())
        release()
        assertFalse(isOpen())
        assertEquals(1, releaseCount.get())
    }
}
