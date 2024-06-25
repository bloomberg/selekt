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

@NotThreadSafe
class FastStampedStringMap<T>(
    capacity: Int,
    private val disposal: (T & Any) -> Unit
) : FastStringMap<T>(capacity) {
    private var currentStamp = Int.MIN_VALUE
    private var spare: StampedEntry<T & Any>? = null

    inline fun getElsePut(
        key: String,
        supplier: () -> T & Any
    ): T & Any {
        val hashCode = hash(key)
        val index = hashIndex(hashCode)
        entryMatching(index, hashCode, key)?.let {
            (it as StampedEntry<T & Any>).stamp = nextStamp()
            return it.value!!
        }
        return addAssociation(index, hashCode, key, supplier()).value!!
    }

    override fun createEntry(
        index: Int,
        hashCode: Int,
        key: String,
        value: T & Any
    ): Entry<T & Any> {
        spare?.let {
            spare = null
            return it.update(index, hashCode, key, value, nextStamp(), store[index])
        }
        return StampedEntry(index, hashCode, key, value, nextStamp(), store[index])
    }

    fun removeKey(key: String) {
        disposal(super.removeEntry(key).value!!)
    }

    override fun clear() {
        entries().forEach {
            disposal(it.value!!)
        }
        spare = null
        super.clear()
    }

    @PublishedApi
    internal fun nextStamp(): Int {
        if (Int.MAX_VALUE == currentStamp) {
            resetAllStamps()
        }
        currentStamp += 1
        return currentStamp
    }

    internal fun asLinkedMap(
        maxSize: Int = size,
        disposal: (T & Any) -> Unit
    ) = FastLinkedStringMap<T & Any>(
        maxSize = maxSize,
        capacity = maxSize,
        accessOrder = true,
        disposal = disposal
    ).apply {
        this@FastStampedStringMap.entries().sortedBy {
            (it as StampedEntry<T & Any>).stamp
        }.forEach {
            addAssociation(it.index, it.hashCode, it.key, it.value!!)
        }
    }

    private fun resetAllStamps() {
        @Suppress("UNCHECKED_CAST")
        (entries() as Iterable<StampedEntry<T>>).sortedBy(StampedEntry<T>::stamp).run {
            currentStamp = Int.MIN_VALUE + maxOf(0, size - 1)
            forEachIndexed { index, it ->
                it.stamp = Int.MIN_VALUE + index
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun removeLastEntry(): StampedEntry<T> = (entries() as Iterable<StampedEntry<T>>)
        .minBy(StampedEntry<T>::stamp).let {
            (removeEntry(it.key) as StampedEntry<T>).apply { disposal(value!!) }
        }

    @PublishedApi
    internal class StampedEntry<T>(
        index: Int,
        hashCode: Int,
        key: String,
        value: T & Any,
        var stamp: Int,
        after: Entry<T & Any>?
    ) : Entry<T & Any>(index, hashCode, key, value, after) {
        @Suppress("NOTHING_TO_INLINE")
        inline fun update(
            index: Int,
            hashCode: Int,
            key: String,
            value: T & Any,
            stamp: Int,
            after: Entry<T & Any>?
        ) = apply {
            this.index = index
            this.hashCode = hashCode
            this.key = key
            this.value = value
            this.stamp = stamp
            this.after = after
        }
    }
}
