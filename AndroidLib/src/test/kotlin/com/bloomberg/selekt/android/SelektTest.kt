/*
 * Copyright 2021 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android

import android.app.Application
import com.bloomberg.selekt.annotations.Experimental
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.junit.Rule
import org.junit.Test
import org.junit.rules.DisableOnDebug
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SelektTest {
    @get:Rule
    val timeoutRule = DisableOnDebug(Timeout(10L, TimeUnit.SECONDS))

    @Test
    fun gitCommitIsNotBlank() = Selekt.gitCommit().let {
        assertTrue(it.isNotBlank())
        assertEquals(40, it.length)
    }

    @Test
    fun libVersion() {
        assertEquals("3.36.0", Selekt.sqliteLibVersion())
    }

    @Test
    fun libVersionNumber() {
        assertEquals(3_036_000, Selekt.sqliteLibVersionNumber())
    }

    @OptIn(Experimental::class)
    @Test
    fun registerComponentCallback() {
        mock<Application>().apply {
            Selekt.registerComponentCallbackWith(this)
            verify(this, times(1)).registerComponentCallbacks(same(MemoryComponentCallback))
            Selekt.unregisterComponentCallbackFrom(this)
            verify(this, times(1)).unregisterComponentCallbacks(same(MemoryComponentCallback))
        }
    }
}
