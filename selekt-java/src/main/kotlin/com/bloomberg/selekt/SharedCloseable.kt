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

import java.io.Closeable
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import javax.annotation.concurrent.ThreadSafe

@ThreadSafe
abstract class SharedCloseable : Closeable {
    @Volatile private var retainCount = 1

    final override fun close() = release()

    fun isOpen() = retainCount > 0

    protected abstract fun onReleased()

    private fun retain() {
        check(retainCountUpdater.getAndIncrement(this) > 0) { "Attempting to retain an already released object: $this." }
    }

    private fun release() {
        if (retainCountUpdater.decrementAndGet(this) == 0) {
            onReleased()
        }
    }

    internal inline fun <T> pledge(block: () -> T): T {
        retain()
        try {
            return block()
        } finally {
            release()
        }
    }

    private companion object {
        val retainCountUpdater: AtomicIntegerFieldUpdater<SharedCloseable> = AtomicIntegerFieldUpdater.newUpdater(
            SharedCloseable::class.java,
            "retainCount"
        )
    }
}
