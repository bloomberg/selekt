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

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import java.util.concurrent.ScheduledExecutorService
import kotlin.test.assertTrue

internal class PoolsKtTest {
    private abstract class KeyedObject : IKeyedObject<Any>
    private val executor = mock<ScheduledExecutorService>()

    @Test
    fun createSingleObjectPool() {
        createObjectPool(mock<IObjectFactory<KeyedObject>>(), executor,
            PoolConfiguration(5_000L, 20_000L, maxTotal = 1)).run {
            assertTrue(this is SingleObjectPool<Any, KeyedObject>)
        }
    }

    @Test
    fun createMultiObjectPool() {
        createObjectPool(mock<IObjectFactory<KeyedObject>>(), executor,
            PoolConfiguration(5_000L, 20_000L, maxTotal = 2)).run {
            assertTrue(this is CommonObjectPool<Any, KeyedObject>)
        }
    }
}
