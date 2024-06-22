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

package com.bloomberg.selekt.cache

class CommonLruCache<T : Any>(
    @PublishedApi
    internal val maxSize: Int,
    disposal: (T) -> Unit
) {
    @PublishedApi
    internal var cache: Any = StampedCache(maxSize, disposal)

    fun evict(key: String) {
        when (val cache = cache) {
            is StampedCache<*> -> cache.evict(key)
            is LinkedLruCache<*> -> cache.evict(key)
            else -> error("Unrecognized cache class: {}")
        }
    }

    fun evictAll() {
        when (val cache = cache) {
            is StampedCache<*> -> cache.evictAll()
            is LinkedLruCache<*> -> cache.evictAll()
            else -> error("Unrecognized cache class: {}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun get(key: String, supplier: () -> T): T = when (cache) {
        is StampedCache<*> -> (cache as StampedCache<T>).let {
            it.get(key) {
                supplier().also { value ->
                    if (it.shouldTransform()) {
                        // Adding another entry to the cache will necessitate the removal of the
                        // least recently used entry first to honour our maximum size constraint.
                        // For the implementation of the store currently assigned, this is an O(N)
                        // operation. We transform to an O(1) implementation.
                        transform()
                        (this@CommonLruCache.cache as LinkedLruCache<T>).store.put(key, value)
                    }
                }
            }
        }
        is LinkedLruCache<*> -> (cache as LinkedLruCache<T>).get(key, supplier)
        else -> error("Unrecognized cache class: {}")
    }

    fun containsKey(key: String) = when (val cache = cache) {
        is StampedCache<*> -> cache.containsKey(key)
        is LinkedLruCache<*> -> cache.containsKey(key)
        else -> error("Unrecognized cache class: {}")
    }

    @PublishedApi
    internal fun StampedCache<T>.shouldTransform() = (store.size >= maxSize)

    @PublishedApi
    internal fun transform() {
        (cache as StampedCache<*>).asLruCache().also {
            cache = it
        }
    }
}
