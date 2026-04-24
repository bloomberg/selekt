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
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class TestCloseable : SharedCloseable() {
    val releaseCount = AtomicInteger(0)

    override fun onReleased() {
        releaseCount.incrementAndGet()
    }
}

internal class SharedCloseableTest {
    @Test
    fun closeDelegatesToRelease(): Unit = TestCloseable().run {
        close()
        assertFalse(isOpen())
        assertEquals(1, releaseCount.get())
    }

    @Test
    fun retainThenCloseKeepsOpen(): Unit = TestCloseable().run {
        retain()
        close()
        assertTrue(isOpen())
        assertEquals(0, releaseCount.get())
    }

    @Test
    fun useBlockClosesAfterExecution(): Unit = TestCloseable().run {
        retain()
        use {
            assertTrue(it.isOpen())
        }
        assertTrue(isOpen())
        close()
        assertFalse(isOpen())
    }

    @Test
    fun doubleCloseIsIdempotent(): Unit = TestCloseable().run {
        close()
        close()
        assertEquals(1, releaseCount.get())
    }
}
