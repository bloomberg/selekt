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

package com.bloomberg.selekt.collections.map

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

internal class FastLinkedStringMapTest {
    @Test
    fun get() {
        val first = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val map = FastLinkedStringMap(1, 1, false, disposal)
        assertSame(first, map.getElsePut("1") { first })
    }

    @Test
    fun getTwo() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val map = FastLinkedStringMap(2, 64, false, disposal)
        map.getElsePut("1") { first }
        map.getElsePut("2") { second }
        assertSame(first, map.getElsePut("1") { fail() })
        assertSame(second, map.getElsePut("2") { fail() })
    }

    @Test
    fun getAfterEvict() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val map = FastLinkedStringMap(1, 1, false, disposal)
        map.getElsePut("1") { first }
        map.getElsePut("2") { second }
        assertFalse(map.containsKey("1"))
        assertSame(second, map.getElsePut("2") { fail() })
    }

    @Test
    fun remove() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val map = FastLinkedStringMap(2, 64, false, disposal)
        map.getElsePut("1") { first }
        map.getElsePut("2") { second }
        map.removeKey("1")
        inOrder(disposal) {
            verify(disposal, times(1)).invoke(same(first))
        }
        assertSame(second, map.getElsePut("2") { fail() })
    }

    @Test
    fun clear() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val map = FastLinkedStringMap(2, 64, false, disposal)
        map.getElsePut("1") { first }
        map.getElsePut("2") { second }
        map.clear()
        inOrder(disposal) {
            verify(disposal, times(1)).invoke(same(first))
            verify(disposal, times(1)).invoke(same(second))
        }
    }

    @Test
    fun removeWhenEmpty() {
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val map = FastLinkedStringMap(1, 1, false, disposal)
        assertThrows<NoSuchElementException> {
            map.removeKey("1")
        }
        verify(disposal, never()).invoke(anyOrNull())
    }

    @Test
    fun removeLastEntryAccessed() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val map = FastLinkedStringMap(2, 2, true, disposal)
        map.getElsePut("1") { first }
        map.getElsePut("2") { second }
        map.getElsePut("1") { first }
        map.removeLastEntry()
        assertEquals(1, map.size)
        assertTrue(map.containsKey("1"))
        assertFalse(map.containsKey("2"))
    }

    @Test
    fun removeLastEntryInserted() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val map = FastLinkedStringMap(2, 2, false, disposal)
        map.getElsePut("1") { first }
        map.getElsePut("2") { second }
        map.getElsePut("1") { first }
        map.removeLastEntry()
        assertEquals(1, map.size)
        assertFalse(map.containsKey("1"))
        assertTrue(map.containsKey("2"))
    }

    @Test
    fun evictLeastRecentlyUsed() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val map = FastLinkedStringMap(1, 1, false, disposal)
        map.getElsePut("1") { first }
        map.getElsePut("2") { second }
        inOrder(disposal) {
            verify(disposal, times(1)).invoke(same(first))
        }
    }

    @Test
    fun getWhenAbsent() {
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val supplier = mock<() -> Any>()
        whenever(supplier.invoke()) doReturn Any()
        val map = FastLinkedStringMap(1, 1, false, disposal)
        val item = map.getElsePut("1", supplier)
        verify(supplier, times(1)).invoke()
        assertSame(item, map.getElsePut("1", supplier))
    }

    @Test
    fun containsFalse() {
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val supplier = mock<() -> Any>()
        whenever(supplier.invoke()) doReturn Any()
        val map = FastLinkedStringMap(1, 1, false, disposal)
        map.getElsePut("1", supplier)
        assertFalse(map.containsKey("2"))
    }

    @Test
    fun containsTrue() {
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val supplier = mock<() -> Any>()
        whenever(supplier.invoke()) doReturn Any()
        val map = FastLinkedStringMap(1, 1, false, disposal)
        map.getElsePut("1", supplier)
        assertTrue(map.containsKey("1"))
    }
}
