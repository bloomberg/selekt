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

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit

internal class TieredObjectPoolTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    private val singleObjectPool: SingleObjectPool<String, PooledObject> = mock()
    private val commonObjectPool: CommonObjectPool<String, PooledObject> = mock()
    private val pool = TieredObjectPool(singleObjectPool, commonObjectPool)

    @Test
    fun borrowPrimaryObject() {
        pool.borrowPrimaryObject()
        verify(singleObjectPool, times(1)).borrowObject()
        verifyZeroInteractions(commonObjectPool)
    }

    @Test
    fun borrowSecondaryObject() {
        val key = ""
        pool.borrowObject(key)
        verifyZeroInteractions(singleObjectPool)
        verify(commonObjectPool, times(1)).borrowObject(eq(key))
    }

    @Test
    fun close() {
        pool.close()
        verify(singleObjectPool, times(1)).close()
        verify(commonObjectPool, times(1)).close()
    }

    @Test
    fun returnPrimaryObject() {
        val obj = mock<PooledObject>().apply {
            whenever(isPrimary) doReturn true
        }
        pool.returnObject(obj)
        verify(singleObjectPool, times(1)).returnObject(same(obj))
        verifyZeroInteractions(commonObjectPool)
    }

    @Test
    fun returnObject() {
        val obj = mock<PooledObject>().apply {
            whenever(isPrimary) doReturn false
        }
        pool.returnObject(obj)
        verify(commonObjectPool, times(1)).returnObject(same(obj))
        verifyZeroInteractions(singleObjectPool)
    }
}
