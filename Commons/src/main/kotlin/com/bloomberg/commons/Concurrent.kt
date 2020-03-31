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

package com.bloomberg.commons

import java.util.concurrent.Semaphore
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class InterruptibleSemaphore(permits: Int, isFair: Boolean = true) : Semaphore(permits, isFair) {
    fun interruptWaiters() = queuedThreads.forEach { it.interrupt() }
}

class InterruptibleReentrantLock(isFair: Boolean) : ReentrantLock(isFair) {
    fun interruptWaiters() = queuedThreads.forEach { it.interrupt() }
}

inline fun <T> Lock.withTryLock(action: () -> T): T? {
    if (tryLock()) {
        try {
            return action()
        } finally {
            unlock()
        }
    }
    return null
}

inline fun <R> Semaphore.withTryAcquisition(permits: Int = 1, block: () -> R): R? = run {
    if (tryAcquire(permits)) {
        try {
            block()
        } finally {
            release(permits)
        }
    } else {
        null
    }
}
