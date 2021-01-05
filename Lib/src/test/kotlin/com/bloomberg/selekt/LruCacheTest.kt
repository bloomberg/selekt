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

package com.bloomberg.selekt

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.RuleChain
import org.junit.rules.Timeout
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class LruCacheTest {
    @Rule
    @JvmField
    val rule: RuleChain = RuleChain.outerRule(DisableOnDebug(Timeout.seconds(10L)))

    @Test
    fun evict() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val cache = LruCache(2, disposal)
        cache["1", { first }]
        cache["2", { second }]
        cache.evict("1")
        inOrder(disposal) {
            verify(disposal, times(1)).invoke(same(first))
        }
    }

    @Test
    fun evictAll() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val cache = LruCache(2, disposal)
        cache["1", { first }]
        cache["2", { second }]
        cache.evictAll()
        inOrder(disposal) {
            verify(disposal, times(1)).invoke(same(first))
            verify(disposal, times(1)).invoke(same(second))
        }
    }

    @Test
    fun evictWhenEmpty() {
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val cache = LruCache(1, disposal)
        cache.evict("1")
        verify(disposal, never()).invoke(anyOrNull())
    }

    @Test
    fun evictLeastRecentlyUsed() {
        val first = Any()
        val second = Any()
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val cache = LruCache(1, disposal)
        cache["1", { first }]
        cache["2", { second }]
        inOrder(disposal) {
            verify(disposal, times(1)).invoke(same(first))
        }
    }

    @Test
    fun getWhenAbsent() {
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val supplier = mock<() -> Any>()
        whenever(supplier.invoke()) doReturn Any()
        val cache = LruCache(1, disposal)
        val item = cache["1", supplier]
        verify(supplier, times(1)).invoke()
        assertSame(item, cache["1", supplier])
    }

    @Test
    fun containsFalse() {
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val supplier = mock<() -> Any>()
        whenever(supplier.invoke()) doReturn Any()
        val cache = LruCache(1, disposal)
        cache["1", supplier]
        assertFalse(cache.containsKey("2"))
    }

    @Test
    fun containsTrue() {
        val disposal: (Any) -> Unit = mock { onGeneric { invoke(it) } doReturn Unit }
        val supplier = mock<() -> Any>()
        whenever(supplier.invoke()) doReturn Any()
        val cache = LruCache(1, disposal)
        cache["1", supplier]
        assertTrue(cache.containsKey("1"))
    }
}
