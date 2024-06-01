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

import javax.annotation.concurrent.NotThreadSafe

private const val NO_RESIZE_LOAD_FACTOR = 1.1f

@NotThreadSafe
class LruCache<T : Any>(private val maxSize: Int, private val disposal: (T) -> Unit) {
    @PublishedApi
    @JvmField
    @JvmSynthetic
    internal val store = object : LinkedHashMap<String, T>(maxSize, NO_RESIZE_LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, T>) = (size > maxSize).also {
            if (it) {
                disposal(eldest.value)
            }
        }

        override fun remove(key: String): T? = super.remove(key)?.also { disposal(it) }
    }

    fun evict(key: String) {
        store.remove(key)
    }

    fun evictAll() {
        store.values.toList()
            .also { store.clear() }
            .forEach { disposal(it) }
    }

    inline operator fun get(key: String, supplier: () -> T): T = store.getOrPut(key, supplier)

    fun containsKey(key: String) = store.containsKey(key)
}
