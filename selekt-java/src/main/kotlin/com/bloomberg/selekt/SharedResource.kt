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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import javax.annotation.concurrent.ThreadSafe

/**
 * A thread-safe reference-counted resource. The resource starts with a retain count of 1.
 * Each call to [retain] increments the count; each call to [release] decrements it.
 * When the count reaches zero, [onReleased] is invoked exactly once.
 */
@ThreadSafe
abstract class SharedResource {
    @Volatile private var retainCount = 1

    fun isOpen() = retainCount > 0

    protected abstract fun onReleased()

    fun retain() {
        while (true) {
            val current = retainCountUpdater[this]
            check(current > 0) { "Attempting to retain an already released resource: $this." }
            if (retainCountUpdater.compareAndSet(this, current, current + 1)) {
                return
            }
        }
    }

    fun release() {
        while (true) {
            val current = retainCountUpdater[this]
            if (current <= 0) {
                return
            }
            if (retainCountUpdater.compareAndSet(this, current, current - 1)) {
                if (current == 1) {
                    onReleased()
                }
                return
            }
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
        val retainCountUpdater: AtomicIntegerFieldUpdater<SharedResource> = AtomicIntegerFieldUpdater.newUpdater(
            SharedResource::class.java,
            "retainCount"
        )
    }
}

