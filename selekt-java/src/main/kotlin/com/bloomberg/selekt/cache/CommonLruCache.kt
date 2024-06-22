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
    @JvmField
    internal val maxSize: Int,
    disposal: (T) -> Unit
) {
    @PublishedApi
    @JvmField
    internal var cache: StampedCache<T>? = StampedCache(maxSize, disposal)
    @PublishedApi
    @JvmField
    internal var linkedCache: LinkedLruCache<T>? = null

    fun evict(key: String) {
        cache?.let {
            it.evict(key)
            return
        }
        linkedCache!!.evict(key)
    }

    fun evictAll() {
        cache?.let {
            it.evictAll()
            return
        }
        linkedCache!!.evictAll()
    }

    inline fun get(key: String, supplier: () -> T): T {
        cache?.let {
            return it.get(key) {
                supplier().also { value ->
                    if (it.shouldTransform()) {
                        // Adding another entry to the cache will necessitate the removal of the
                        // least recently used entry first to honour our maximum size constraint.
                        // For the implementation of the store currently assigned, this is an O(N)
                        // operation. We transform to an O(1) implementation.
                        transform()
                        linkedCache!!.store.put(key, value)
                    }
                }
            }
        }
        return linkedCache!!.get(key, supplier)
    }

    fun containsKey(key: String): Boolean {
        cache?.let {
            return it.containsKey(key)
        }
        return linkedCache!!.containsKey(key)
    }

    @PublishedApi
    internal fun StampedCache<T>.shouldTransform() = (store.size >= maxSize)

    @PublishedApi
    internal fun transform() {
        linkedCache = cache!!.asLruCache().also {
            cache = null
        }
    }
}
