/*
 * Copyright 2026 Bloomberg Finance L.P.
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
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.concurrent.ThreadSafe

/**
 * @since 0.36.0
 */
@ThreadSafe
class DatabaseKey internal constructor(
    private val sqlite: IExternalSQLite,
    @JvmSynthetic internal val pointer: Long,
    @JvmSynthetic internal val size: Int
) : SharedResource(), Closeable {
    private val closed = AtomicBoolean(false)

    override fun onReleased() {
        sqlite.freeSecret(pointer, size)
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            release()
        }
    }

    @JvmSynthetic
    internal inline fun <R> use(action: (pointer: Long, size: Int) -> R): R {
        check(tryRetain()) { "Key is destroyed." }
        try {
            return action(pointer, size)
        } finally {
            release()
        }
    }

    companion object {
        /** Required size of raw database encryption keys. */
        const val REQUIRED_LENGTH_BYTES = 32

        @JvmStatic
        fun of(sqlite: SQLite, key: ByteArray): DatabaseKey = sqlite.newKey(key)

        @JvmStatic
        fun take(sqlite: SQLite, key: ByteArray): DatabaseKey = of(sqlite, key).also { key.zero() }
    }
}
