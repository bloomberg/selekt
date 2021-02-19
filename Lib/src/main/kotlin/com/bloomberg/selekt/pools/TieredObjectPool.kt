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

package com.bloomberg.selekt.pools

import java.io.Closeable

internal class TieredObjectPool<K : Any, T : IPooledObject<K>> internal constructor(
    private val primaryPool: SingleObjectPool<K, T>,
    private val secondaryPool: IObjectPool<K, T>
) : Closeable {
    override fun close() {
        secondaryPool.use {
            primaryPool.close()
        }
    }

    fun borrowObject() = secondaryPool.borrowObject()

    fun borrowObject(key: K) = secondaryPool.borrowObject(key)

    fun borrowPrimaryObject() = primaryPool.borrowObject()

    fun clear(priority: Priority) {
        try {
            primaryPool.clear(priority)
        } finally {
            secondaryPool.clear(priority)
        }
    }

    fun returnObject(obj: T) = if (obj.isPrimary) {
        primaryPool.returnObject(obj)
    } else {
        secondaryPool.returnObject(obj)
    }
}
