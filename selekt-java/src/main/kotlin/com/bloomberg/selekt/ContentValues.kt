/*
 * Copyright 2022 Bloomberg Finance L.P.
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

package com.bloomberg.selekt

import javax.annotation.concurrent.NotThreadSafe

@NotThreadSafe
@Suppress("Detekt.MethodOverloading")
class ContentValues : IContentValues {
    private val store = mutableMapOf<String, Any?>()

    override val isEmpty: Boolean get() { return store.isEmpty() }

    override val size: Int get() { return store.size }

    override val entrySet: Set<Map.Entry<String, Any?>> get() { return store.entries }

    override fun put(key: String, value: Byte) {
        store[key] = value
    }

    override fun put(key: String, value: ByteArray) {
        store[key] = value
    }

    override fun put(key: String, value: Double) {
        store[key] = value
    }

    override fun put(key: String, value: Float) {
        store[key] = value
    }

    override fun put(key: String, value: Int) {
        store[key] = value
    }

    override fun put(key: String, value: Long) {
        store[key] = value
    }

    override fun put(key: String, value: Short) {
        store[key] = value
    }

    override fun put(key: String, value: String) {
        store[key] = value
    }

    override fun putNull(key: String) {
        store[key] = null
    }
}

@Suppress("Detekt.ComplexInterface", "Detekt.MethodOverloading")
interface IContentValues {
    val isEmpty: Boolean

    val size: Int

    val entrySet: Set<Map.Entry<String, Any?>>

    fun put(key: String, value: Byte)

    fun put(key: String, value: ByteArray)

    fun put(key: String, value: Double)

    fun put(key: String, value: Float)

    fun put(key: String, value: Int)

    fun put(key: String, value: Long)

    fun put(key: String, value: Short)

    fun put(key: String, value: String)

    fun putNull(key: String)
}
