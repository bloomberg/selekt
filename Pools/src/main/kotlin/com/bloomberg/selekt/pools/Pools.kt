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

package com.bloomberg.selekt.pools

import java.io.Closeable
import java.util.concurrent.ScheduledExecutorService

fun <K : Any, T : IKeyedObject<K>> createObjectPool(
    factory: IObjectFactory<T>,
    executor: ScheduledExecutorService,
    configuration: PoolConfiguration
): IObjectPool<K, T> = when (configuration.maxTotal) {
    1 -> SingleObjectPool(factory, executor, configuration.evictionDelayMillis, configuration.evictionIntervalMillis)
    else -> CommonObjectPool(factory, executor, configuration)
}

interface IObjectPool<K : Any, T : Any> : Closeable {
    fun borrowObject(key: K): T

    fun borrowPrimaryObject(): T

    fun gauge(): PoolGauge

    fun returnObject(obj: T)
}

interface IObjectFactory<T : Any> : Closeable {
    fun activateObject(obj: T)

    fun destroyObject(obj: T)

    fun gauge(): FactoryGauge

    fun makeObject(): T

    fun makePrimaryObject(): T

    fun passivateObject(obj: T)

    fun validateObject(obj: T): Boolean
}

interface IKeyedObject<K> {
    val isPrimary: Boolean

    fun matches(key: K): Boolean
}
