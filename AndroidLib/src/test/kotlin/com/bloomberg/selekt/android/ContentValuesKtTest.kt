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

package com.bloomberg.selekt.android

import android.content.ContentValues
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class ContentValuesKtTest {
    @Test
    fun asSelektContentValuesPutString() {
        ContentValues().apply { put("a", 42) }.asSelektContentValues().run {
            assertFalse(entrySet.isEmpty())
        }
    }

    @Test
    fun iterateOverNull() {
        ContentValues().apply { putNull("a") }.asSelektContentValues().run {
            for (entry in entrySet) {
                assertEquals("a", entry.key)
                assertNull(entry.value)
            }
        }
    }

    @Test
    fun isEmpty() {
        assertTrue(ContentValues().asSelektContentValues().isEmpty)
    }

    @Test
    fun isNotEmpty() {
        assertFalse(ContentValues().apply { putNull("a") }.asSelektContentValues().isEmpty)
    }
}
