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

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.concurrent.ScheduledExecutorService

internal class PoolsKtTest {
    private abstract class PooledObject : IPooledObject<Any>
    private val executor = mock<ScheduledExecutorService>()

    @Test
    fun createSingleObjectPool() {
        createObjectPool(mock<IObjectFactory<PooledObject>>(), executor, PoolConfiguration(5_000L, 20_000L, maxTotal = 1))
    }

    @Test
    fun createMultiObjectPool() {
        createObjectPool(mock<IObjectFactory<PooledObject>>(), executor, PoolConfiguration(5_000L, 20_000L, maxTotal = 2))
    }
}
