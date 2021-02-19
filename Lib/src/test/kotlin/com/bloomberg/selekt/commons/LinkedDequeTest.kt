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

package com.bloomberg.selekt.commons

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class LinkedDequeTest {
    @Test
    fun putLastThenPollLast() {
        LinkedDeque<Any>().apply {
            val obj = Any()
            putFirst(obj)
            assertSame(obj, pollLast())
        }
    }

    @Test
    fun pollLastNull() {
        assertNull(LinkedDeque<Any>().pollLast())
    }

    @Test
    fun isEmpty() {
        assertTrue(LinkedDeque<Any>().isEmpty)
    }

    @Test
    fun hasSizeOneFalseWhenEmpty() {
        assertFalse(LinkedDeque<Any>().hasSizeOne)
    }

    @Test
    fun hasSizeOneFalseWhenTwo() {
        assertFalse(LinkedDeque<Any>().apply {
            repeat(2) { putFirst(Any()) }
        }.hasSizeOne)
    }

    @Test
    fun hasSizeOneTrue() {
        assertTrue(LinkedDeque<Any>().apply { putFirst(Any()) }.hasSizeOne)
    }

    @Test
    fun pollLastInOrder() {
        LinkedDeque<Any>().apply {
            val one = Any()
            val two = Any()
            putFirst(one)
            putFirst(two)
            assertSame(one, pollLast())
            assertSame(two, pollLast())
        }
    }

    @Test
    fun putPollPut() {
        LinkedDeque<Any>().apply {
            val one = Any()
            val two = Any()
            putFirst(one)
            pollLast()
            putFirst(two)
            assertSame(two, pollLast())
        }
    }

    @Test
    fun removeFirst() {
        LinkedDeque<Any>().apply {
            val one = Any()
            val two = Any()
            putFirst(one)
            putFirst(two)
            assertSame(one, pollFirst { it === one })
            assertSame(two, pollFirst { it === two })
            assertTrue(isEmpty)
        }
    }

    @Test
    fun reverseIterate() {
        val one = Any()
        val two = Any()
        LinkedDeque<Any>().apply {
            putFirst(one)
            putFirst(two)
        }.reverseMutableIterator().let {
            assertTrue(it.hasNext())
            assertSame(one, it.next())
            assertSame(two, it.next())
            assertFalse(it.hasNext())
        }
    }

    @Test
    fun reverseIterateEmpty() {
        assertFalse(LinkedDeque<Any>().reverseMutableIterator().hasNext())
    }

    @Test
    fun reverseIterateEmptyThrows() {
        assertThatExceptionOfType(NoSuchElementException::class.java).isThrownBy {
            LinkedDeque<Any>().reverseMutableIterator().next()
        }
    }

    @Test
    fun reverseRemoveEmptyThrows() {
        assertThatExceptionOfType(UnsupportedOperationException::class.java).isThrownBy {
            LinkedDeque<Any>().reverseMutableIterator().remove()
        }
    }

    @Test
    fun reverseIterateThenThrow() {
        LinkedDeque<Any>().apply {
            putFirst(Any())
            reverseMutableIterator().let {
                it.next()
                assertThatExceptionOfType(NoSuchElementException::class.java).isThrownBy {
                    it.next()
                }
            }
        }
    }

    @Test
    fun removeSingleViaReverseIterator() {
        LinkedDeque<Any>().apply {
            putFirst(Any())
            reverseMutableIterator().let {
                assertTrue(it.hasNext())
                it.next()
                it.remove()
                assertTrue(isEmpty)
            }
        }
    }

    @Test
    fun removeAllWhileReverseIterating() {
        LinkedDeque<Any>().apply {
            repeat(5) { putFirst(Any()) }
            val iterator = reverseMutableIterator()
            while (iterator.hasNext()) {
                iterator.next()
                iterator.remove()
            }
            assertTrue(isEmpty)
        }
    }

    @Test
    fun removeSomeWhileReverseIterating() {
        LinkedDeque<Any>().apply {
            val one = Any()
            val two = Any()
            repeat(3) { putFirst(Any()) }
            putFirst(one)
            putFirst(two)
            val iterator = reverseMutableIterator()
            repeat(3) {
                iterator.next()
                iterator.remove()
            }
            assertSame(one, pollLast())
            assertSame(two, pollLast())
        }
    }
}
