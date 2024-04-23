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

package com.bloomberg.selekt

import com.bloomberg.selekt.commons.zero
import javax.annotation.concurrent.GuardedBy

internal class Key(value: ByteArray) {
    private val lock = Any()

    @GuardedBy("lock")
    private val value: ByteArray = value.copyOf()

    @GuardedBy("lock")
    private var isDestroyed = false

    fun zero() = synchronized(lock) {
        if (!isDestroyed) {
            value.zero()
            isDestroyed = true
        }
    }

    inline fun <R> use(action: (ByteArray) -> R) = synchronized(lock) {
        check(!isDestroyed) { "Key is destroyed." }
        value.copyOf()
    }.let {
        try {
            action(it)
        } finally {
            it.zero()
        }
    }
}
