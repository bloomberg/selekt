/*
 * Copyright 2024 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.collections.map

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

internal class FastStringMapTest {
    @Test
    fun get() {
        val first = Any()
        val map = FastStringMap<Any>(1)
        assertSame(first, map.getEntryElsePut("1") { first }.value)
    }

    @Test
    fun sizeOne() {
        val first = Any()
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1") { first }
        assertEquals(1, map.size)
    }

    @Test
    fun getTwice() {
        val first = Any()
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1") { first }
        assertSame(first, map.getEntryElsePut("1") { fail() }.value)
    }

    @Test
    fun getWhenAbsent() {
        val supplier = mock<() -> Any>()
        whenever(supplier.invoke()) doReturn Any()
        val map = FastStringMap<Any>(1)
        val item = map.getEntryElsePut("1", supplier)
        verify(supplier, times(1)).invoke()
        assertSame(item, map.getEntryElsePut("1", supplier))
        verifyNoMoreInteractions(supplier)
    }

    @Test
    fun getTwo() {
        val first = Any()
        val second = Any()
        val map = FastStringMap<Any>(64)
        map.getEntryElsePut("1") { first }
        map.getEntryElsePut("2") { second }
        assertEquals(2, map.size)
    }

    @Test
    fun getTwoWithCollisions() {
        val first = Any()
        val second = Any()
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1") { first }
        map.getEntryElsePut("2") { second }
        assertSame(first, map.getEntryElsePut("1") { fail() }.value)
        assertSame(second, map.getEntryElsePut("2") { fail() }.value)
    }

    @Test
    fun sizeTwo() {
        val first = Any()
        val second = Any()
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1") { first }
        map.getEntryElsePut("2") { second }
        assertSame(first, map.getEntryElsePut("1") { fail() }.value)
        assertSame(second, map.getEntryElsePut("2") { fail() }.value)
    }

    @Test
    fun removeOne() {
        val first = Any()
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1") { first }
        assertSame(first, map.removeEntry("1").value)
    }

    @Test
    fun removeTwo() {
        val first = Any()
        val second = Any()
        val map = FastStringMap<Any>(2)
        map.getEntryElsePut("1") { first }
        map.getEntryElsePut("2") { second }
        assertSame(first, map.removeEntry("1").value)
        assertSame(second, map.getEntryElsePut("2") { fail() }.value)
    }

    @Test
    fun removeTwoWithCollisions() {
        val first = Any()
        val second = Any()
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1") { first }
        map.getEntryElsePut("2") { second }
        assertSame(first, map.removeEntry("1").value)
        assertSame(second, map.getEntryElsePut("2") { fail() }.value)
    }

    @Test
    fun removeThenSize() {
        val first = Any()
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1") { first }
        map.removeEntry("1")
        assertEquals(0, map.size)
    }

    @Test
    fun removeWhenEmpty() {
        val map = FastStringMap<Any>(1)
        assertThrows<NoSuchElementException> {
            map.removeEntry("1")
        }
        assertEquals(0, map.size)
    }

    @Test
    fun clear() {
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1") { Any() }
        assertEquals(1, map.size)
        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun clearWhenEmpty() {
        val map = FastStringMap<Any>(1)
        map.clear()
        assertTrue(map.isEmpty())
    }

    @Test
    fun containsFalse() {
        val supplier = mock<() -> Any>()
        whenever(supplier.invoke()) doReturn Any()
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1", supplier)
        assertFalse(map.containsKey("2"))
    }

    @Test
    fun containsTrue() {
        val supplier = mock<() -> Any>()
        whenever(supplier.invoke()) doReturn Any()
        val map = FastStringMap<Any>(1)
        map.getEntryElsePut("1", supplier)
        assertTrue(map.containsKey("1"))
    }
}
