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

package com.bloomberg.selekt.pools

import java.io.Closeable
import java.util.concurrent.ScheduledExecutorService
import kotlin.jvm.Throws

@JvmSynthetic
internal fun <K : Any, T : IPooledObject<K>> createObjectPool(
    factory: IObjectFactory<T>,
    executor: ScheduledExecutorService,
    configuration: PoolConfiguration
) = SingleObjectPool(
    factory,
    executor,
    configuration.evictionDelayMillis,
    configuration.evictionIntervalMillis
).let {
    when (configuration.maxTotal) {
        1 -> TieredObjectPool(it, it)
        else -> TieredObjectPool(
            it,
            CommonObjectPool(factory, executor, configuration.copy(maxTotal = configuration.maxTotal - 1), it)
        )
    }
}

enum class Priority {
    LOW,
    HIGH
}

@JvmSynthetic
internal fun Priority?.isHigh() = this?.let { it >= Priority.HIGH } ?: false

interface IObjectPool<K : Any, T : Any> : Closeable {
    fun borrowObject(): T

    fun borrowObject(key: K): T

    fun clear(priority: Priority)

    fun returnObject(obj: T)
}

interface IObjectFactory<T : Any> : Closeable {
    @Throws(Exception::class)
    fun destroyObject(obj: T)

    @Throws(Exception::class)
    fun makeObject(): T

    @Throws(Exception::class)
    fun makePrimaryObject(): T
}

interface IPooledObject<K> {
    val isPrimary: Boolean

    var tag: Boolean

    fun matches(key: K): Boolean

    fun releaseMemory()
}
