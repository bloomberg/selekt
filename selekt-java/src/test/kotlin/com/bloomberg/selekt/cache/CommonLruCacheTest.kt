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

package com.bloomberg.selekt.cache

import org.junit.jupiter.api.Assertions.assertNull
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

internal class CommonLruCacheTest {
    private val first = Any()
    private val second = Any()
    private val supplier = mock<() -> Any>()
    private val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }

    @Test
    fun get() {
        val first = Any()
        val cache = CommonLruCache(1, disposal)
        cache.get("1") { first }
        assertSame(first, cache.get("1") { fail() })
        assertNotNull(cache.cache)
    }

    @Test
    fun getTwo() {
        val cache = CommonLruCache(2, disposal)
        cache.get("1") { first }
        cache.get("2") { second }
        assertSame(first, cache.get("1") { fail() })
        assertSame(second, cache.get("2") { fail() })
        assertNotNull(cache.cache)
    }

    @Test
    fun getAfterEvict() {
        val cache = CommonLruCache(1, disposal)
        cache.get("1") { first }
        cache.get("2") { second }
        assertFalse(cache.containsKey("1"))
        assertSame(second, cache.get("2") { fail() })
        assertNull(cache.cache)
    }

    @Test
    fun evict() {
        val cache = CommonLruCache(2, disposal)
        cache.get("1") { first }
        cache.get("2") { second }
        cache.evict("1")
        inOrder(disposal) {
            verify(disposal, times(1)).invoke(same(first))
        }
        assertSame(second, cache.get("2") { fail() })
    }

    @Test
    fun evictAll() {
        val cache = CommonLruCache(2, disposal)
        cache.get("1") { first }
        cache.get("2") { second }
        cache.evictAll()
        inOrder(disposal) {
            verify(disposal, times(1)).invoke(same(second))
            verify(disposal, times(1)).invoke(same(first))
        }
    }

    @Test
    fun evictWhenEmpty() {
        val cache = CommonLruCache(1, disposal)
        assertThrows<NoSuchElementException> {
            cache.evict("1")
        }
        verify(disposal, never()).invoke(anyOrNull())
    }

    @Test
    fun evictLeastRecentlyUsed() {
        val cache = CommonLruCache(2, disposal)
        val third = Any()
        cache.get("1") { first }
        cache.get("2") { second }
        cache.get("1") { fail() }
        cache.get("3") { third }
        assertFalse(cache.containsKey("2"))
        inOrder(disposal) {
            verify(disposal, times(1)).invoke(same(second))
        }
    }

    @Test
    fun getWhenAbsent() {
        whenever(supplier.invoke()) doReturn Any()
        val cache = CommonLruCache(1, disposal)
        val item = cache.get("1", supplier)
        verify(supplier, times(1)).invoke()
        assertSame(item, cache.get("1", supplier))
    }

    @Test
    fun containsFalse() {
        whenever(supplier.invoke()) doReturn Any()
        val cache = CommonLruCache(1, disposal)
        cache.get("1", supplier)
        assertFalse(cache.containsKey("2"))
    }

    @Test
    fun containsTrue() {
        whenever(supplier.invoke()) doReturn Any()
        val cache = CommonLruCache(1, disposal)
        cache.get("1", supplier)
        assertTrue(cache.containsKey("1"))
    }
}
