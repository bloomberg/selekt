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

import javax.annotation.concurrent.NotThreadSafe

/**
 * @param capacity a power of two.
 */
@NotThreadSafe
open class FastStringMap<T : Any>(capacity: Int) {
    @JvmField
    var size: Int = 0

    @JvmField
    @PublishedApi
    internal val store = arrayOfNulls<Entry<T>>(capacity)
    private val hashLimit = capacity - 1

    fun isEmpty() = 0 == size

    fun containsKey(key: String): Boolean {
        val hashCode = hash(key)
        val index = hashIndex(hashCode)
        var entry = store[index]
        while (entry != null) {
            if (entry.hashCode == hashCode && entry.key == key) {
                return true
            }
            entry = entry.after
        }
        return false
    }

    inline fun getEntryElsePut(
        key: String,
        supplier: () -> T
    ): Entry<T> {
        val hashCode = hash(key)
        val index = hashIndex(hashCode)
        var entry = store[index]
        while (entry != null) {
            if (entry.hashCode == hashCode && entry.key == key) {
                return entry
            }
            entry = entry.after
        }
        return addAssociation(index, hashCode, key, supplier())
    }

    fun removeEntry(key: String): Entry<T> {
        val hashCode = hash(key)
        val index = hashIndex(hashCode)
        var entry = store[index]
        var previous: Entry<T>? = null
        while (entry != null) {
            if (entry.hashCode == hashCode && entry.key == key) {
                return removeAssociation(entry, previous)
            }
            previous = entry
            entry = entry.after
        }
        throw NoSuchElementException()
    }

    @PublishedApi
    internal open fun addAssociation(
        index: Int,
        hashCode: Int,
        key: String,
        value: T
    ): Entry<T> = createEntry(index, hashCode, key, value).also {
        store[index] = it
        size += 1
    }

    protected open fun createEntry(
        index: Int,
        hashCode: Int,
        key: String,
        value: T
    ): Entry<T> = Entry(index, hashCode, key, value, store[index])

    internal fun entries(): Iterable<Entry<T>> = store.flatMap {
        sequence {
            var current = it
            while (current != null) {
                yield(current)
                current = current.after
            }
        }
    }

    open fun clear() {
        store.fill(null)
        size = 0
    }

    @Suppress("NOTHING_TO_INLINE")
    @PublishedApi
    internal inline fun entryMatching(index: Int, hashCode: Int, key: String): Entry<T>? {
        var entry = store[index]
        while (entry != null) {
            if (entry.hashCode == hashCode && entry.key == key) {
                return entry
            }
            entry = entry.after
        }
        return null
    }

    private fun removeAssociation(
        entry: Entry<T>,
        previousEntry: Entry<T>?
    ): Entry<T> {
        if (previousEntry == null) {
            store[entry.index] = entry.after
        } else {
            previousEntry.after = entry.after
        }
        size -= 1
        return entry
    }

    @PublishedApi
    internal fun hash(key: String): Int = key.hashCode()

    @PublishedApi
    internal fun hashIndex(hashCode: Int): Int = hashCode and hashLimit

    open class Entry<T>(
        @JvmField
        var index: Int,
        @JvmField
        var hashCode: Int,
        @JvmField
        var key: String,
        @JvmField
        var value: T?,
        @JvmField
        var after: Entry<T>?
    ) {
        internal fun reset(): T? = value.also { _ ->
            key = ""
            value = null
            after = null
        }
    }
}
