/*
 * Copyright 2020 Bloomberg Finance L.P.
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

private const val KEY_SIZE = 32
private fun ByteArray.asBlobLiteral() = "x'${joinToString("") { "%02X".format(it) }}'"

internal class Key(value: ByteArray) {
    init {
        require(KEY_SIZE == value.size) { "Key must be 32 bytes in size." }
    }

    private val lock = Any()
    private val value: ByteArray = value.copyOf()

    fun zero() = synchronized(lock) {
        value.fill(0)
    }

    operator fun invoke() = synchronized(lock) {
        value.asBlobLiteral()
    }
}
