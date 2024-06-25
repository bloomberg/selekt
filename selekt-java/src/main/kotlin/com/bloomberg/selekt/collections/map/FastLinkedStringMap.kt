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
class FastLinkedStringMap<T>(
    @PublishedApi
    @JvmField
    internal val maxSize: Int,
    capacity: Int = maxSize,
    @PublishedApi
    @JvmField
    internal val accessOrder: Boolean = false,
    private val disposal: (T & Any) -> Unit
) : FastStringMap<T>(capacity) {
    private var head: LinkedEntry<T & Any>? = null
    private var tail: LinkedEntry<T & Any>? = null

    @PublishedApi
    @JvmField
    internal var spare: LinkedEntry<T & Any>? = null

    inline fun getElsePut(
        key: String,
        supplier: () -> T & Any
    ): T & Any {
        val hashCode = hash(key)
        val index = hashIndex(hashCode)
        entryMatching(index, hashCode, key)?.let {
            if (accessOrder) {
                putFirst(it as LinkedEntry<T & Any>)
            }
            return it.value!!
        }
        return addAssociation(index, hashCode, key, supplier()).value!!
    }

    @PublishedApi
    internal fun put(
        key: String,
        value: T & Any
    ): T & Any {
        val hashCode = hash(key)
        val index = hashIndex(hashCode)
        return addAssociation(index, hashCode, key, value).value!!
    }

    fun removeKey(key: String) {
        disposal((super.removeEntry(key) as LinkedEntry<T & Any>).unlink().value!!)
    }

    override fun clear() {
        super.clear()
        spare = null
        var entry = tail
        while (entry != null) {
            val previous = entry.previous
            disposal(entry.unlink().value!!)
            entry.key = ""
            entry.value = null
            entry = previous
        }
    }

    private fun LinkedEntry<T & Any>.unlink(): Entry<T & Any> = apply {
        previous?.let { it.next = next }
        next?.let { it.previous = previous }
        if (this === head) {
            head = next
        }
        if (this === tail) {
            tail = previous
        }
        previous = null
        next = null
    }

    @PublishedApi
    @JvmSynthetic
    internal fun putFirst(node: LinkedEntry<T & Any>): Unit = node.run {
        if (this === head) {
            return
        }
        previous?.let { it.next = next }
        next?.let { it.previous = previous }
        if (this === tail) {
            tail = previous
        }
        next = head
        previous = null
        head?.let { it.previous = this }
        head = this
        if (tail == null) {
            tail = this
        }
    }

    @PublishedApi
    override fun addAssociation(
        index: Int,
        hashCode: Int,
        key: String,
        value: T & Any
    ): Entry<T & Any> {
        if (size >= maxSize) {
            spare = removeLastEntry()
        }
        return (super.addAssociation(index, hashCode, key, value) as LinkedEntry<T & Any>).also {
            putFirst(it)
        }
    }

    override fun createEntry(
        index: Int,
        hashCode: Int,
        key: String,
        value: T & Any
    ): Entry<T & Any> {
        spare?.let {
            spare = null
            return it.update(index, hashCode, key, value, store[index])
        }
        return LinkedEntry(index, hashCode, key, value, store[index])
    }

    @PublishedApi
    @JvmSynthetic
    internal fun removeLastEntry(): LinkedEntry<T & Any> = tail!!.apply {
        previous?.let { it.next = null } ?: run { head = null }
        tail = previous
        previous = null
        super.removeEntry(key)
        key = ""
        disposal(value!!)
        value = null
    }

    @PublishedApi
    internal class LinkedEntry<T>(
        index: Int,
        hashCode: Int,
        key: String,
        value: T & Any,
        after: Entry<T & Any>?
    ) : Entry<T & Any>(index, hashCode, key, value, after) {
        @JvmField
        var previous: LinkedEntry<T & Any>? = null

        @JvmField
        var next: LinkedEntry<T>? = null

        @Suppress("NOTHING_TO_INLINE")
        inline fun update(
            index: Int,
            hashCode: Int,
            key: String,
            value: T & Any,
            after: Entry<T & Any>?
        ) = apply {
            this.index = index
            this.hashCode = hashCode
            this.key = key
            this.value = value
            this.after = after
        }
    }
}
