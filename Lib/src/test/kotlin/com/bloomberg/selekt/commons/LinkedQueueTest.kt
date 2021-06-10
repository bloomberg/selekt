/*
 * Copyright 2021 Bloomberg Finance L.P.
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class LinkedQueueTest {
    @Test
    fun isEmpty() {
        assertTrue(LinkedQueue<Any>().isEmpty)
    }

    @Test
    fun isNotEmpty() {
        LinkedQueue<Any>().apply {
            add(Any())
            assertFalse(isEmpty)
        }
    }

    @Test
    fun addThenPoll() {
        LinkedQueue<Any>().apply {
            val obj = Any()
            add(obj)
            assertSame(obj, poll())
            assertTrue(isEmpty)
        }
    }

    @Test
    fun pollNull() {
        assertNull(LinkedQueue<Any>().poll())
    }

    @Test
    fun firstInFirstOut() {
        LinkedQueue<Any>().apply {
            val one = Any()
            val two = Any()
            add(one)
            add(two)
            assertSame(one, poll())
            assertSame(two, poll())
        }
    }
}
