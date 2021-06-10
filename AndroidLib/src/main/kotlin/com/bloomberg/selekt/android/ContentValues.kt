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

package com.bloomberg.selekt.android

import android.content.ContentValues
import com.bloomberg.selekt.IContentValues

private class ContentValuesEntry : Map.Entry<String, Any?> {
    var internalKey: String = ""
    var internalValue: Any? = null

    override val key: String
        get() = internalKey

    override val value: Any?
        get() = internalValue
}

@Suppress("Detekt.MethodOverloading")
internal fun ContentValues.asSelektContentValues() = object : IContentValues {
    override val isEmpty: Boolean
        get() = this@asSelektContentValues.size() == 0

    override val size: Int
        get() = this@asSelektContentValues.size()

    override val entrySet: Set<Map.Entry<String, Any?>> = object : Set<Map.Entry<String, Any?>> {
        override val size: Int
            get() = this@asSelektContentValues.size()

        override fun contains(element: Map.Entry<String, Any?>) = this@asSelektContentValues.run {
            containsKey(element.key) && get(element.key) == element.value
        }

        override fun containsAll(elements: Collection<Map.Entry<String, Any?>>) = !elements.any { !contains(it) }

        override fun isEmpty() = isEmpty

        override fun iterator(): Iterator<Map.Entry<String, Any?>> = object : Iterator<Map.Entry<String, Any?>> {
            private val keys = this@asSelektContentValues.keySet().iterator()
            private val entry = ContentValuesEntry()

            override fun hasNext() = keys.hasNext()

            override fun next(): Map.Entry<String, Any?> = if (hasNext()) {
                entry.apply {
                    keys.next().let {
                        internalKey = it
                        internalValue = this@asSelektContentValues[it]
                    }
                }
            } else throw NoSuchElementException()
        }
    }

    override fun put(key: String, value: Byte) = this@asSelektContentValues.put(key, value)

    override fun put(key: String, value: ByteArray) = this@asSelektContentValues.put(key, value)

    override fun put(key: String, value: Double) = this@asSelektContentValues.put(key, value)

    override fun put(key: String, value: Float) = this@asSelektContentValues.put(key, value)

    override fun put(key: String, value: Int) = this@asSelektContentValues.put(key, value)

    override fun put(key: String, value: Long) = this@asSelektContentValues.put(key, value)

    override fun put(key: String, value: Short) = this@asSelektContentValues.put(key, value)

    override fun put(key: String, value: String) = this@asSelektContentValues.put(key, value)

    override fun putNull(key: String) = this@asSelektContentValues.putNull(key)
}
