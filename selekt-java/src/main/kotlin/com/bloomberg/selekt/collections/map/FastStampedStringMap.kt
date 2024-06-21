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
    capacity: Int
) : FastStringMap<T>(capacity) {
    private var currentStamp = Int.MIN_VALUE
    private var spare: StampedEntry<T>? = null

    inline fun getElsePut(
        key: String,
        supplier: () -> T
    ): T {
        val hashCode = hash(key)
        val index = hashIndex(hashCode)
        entryMatching(index, hashCode, key)?.let {
            (it as StampedEntry<T>).stamp = nextStamp()
            return it.value!!
        }
        return addAssociation(index, hashCode, key, supplier()).value!!
    }

    override fun createEntry(
        index: Int,
        hashCode: Int,
        key: String,
        value: T
    ): Entry<T> {
        spare?.let {
            spare = null
            return it.update(index, hashCode, key, value, nextStamp(), store[index])
        }
        return StampedEntry(index, hashCode, key, value, nextStamp(), store[index])
    }

    override fun clear() {
        super.clear()
        spare = null
    }

    @PublishedApi
    internal fun nextStamp(): Int {
        if (Int.MAX_VALUE == currentStamp) {
            resetAllStamps()
        }
        currentStamp += 1
        return currentStamp
    }

    private fun entries(): Iterable<Entry<T>> = store.flatMap {
        sequence {
            var current = it
            while (current != null) {
                yield(current)
                current = current.after
            }
        }
    }.asIterable()

    private fun resetAllStamps() {
        entries().sortedBy {
            (it as StampedEntry<T>).stamp
        }.run {
            currentStamp = Int.MIN_VALUE + maxOf(0, size - 1)
            forEachIndexed { index, it ->
                (it as StampedEntry<T>).stamp = Int.MIN_VALUE + index
            }
        }
    }

    @PublishedApi
    internal class StampedEntry<T>(
        index: Int,
        hashCode: Int,
        key: String,
        value: T,
        var stamp: Int,
        after: Entry<T>?
    ) : Entry<T>(index, hashCode, key, value, after) {
        @Suppress("NOTHING_TO_INLINE")
        inline fun update(
            index: Int,
            hashCode: Int,
            key: String,
            value: T,
            stamp: Int,
            after: Entry<T>?
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
