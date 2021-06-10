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

import android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
import android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE
import android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE
import android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
import android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW
import android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE
import android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
import com.bloomberg.selekt.pools.Priority
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class MemoryComponentCallbackTest {
    @Test
    fun onConfigurationChangedDoesNotThrow() {
        MemoryComponentCallback.onConfigurationChanged(mock())
    }

    @Test
    fun onLowMemoryDoesNotThrow() {
        MemoryComponentCallback.onLowMemory()
    }

    @Test
    fun onTrimMemoryLow() {
        arrayOf(
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL,
            TRIM_MEMORY_UI_HIDDEN
        ).forEach {
            val database = mock<SQLiteDatabase>()
            SQLiteDatabaseRegistry.register(database)
            try {
                MemoryComponentCallback.onTrimMemory(it)
                verify(database, times(1)).releaseMemory(same(Priority.LOW))
            } finally {
                SQLiteDatabaseRegistry.unregister(database)
            }
        }
    }

    @Test
    fun onTrimMemoryHigh() {
        arrayOf(
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE
        ).forEach {
            val database = mock<SQLiteDatabase>()
            SQLiteDatabaseRegistry.register(database)
            try {
                MemoryComponentCallback.onTrimMemory(it)
                verify(database, times(1)).releaseMemory(same(Priority.HIGH))
            } finally {
                SQLiteDatabaseRegistry.unregister(database)
            }
        }
    }

    @Test
    fun onTrimMemoryNone() {
        val database = mock<SQLiteDatabase>()
        SQLiteDatabaseRegistry.register(database)
        try {
            arrayOf(
                TRIM_MEMORY_RUNNING_MODERATE
            ).forEach {
                MemoryComponentCallback.onTrimMemory(it)
                verify(database, never()).releaseMemory(any())
            }
        } finally {
            SQLiteDatabaseRegistry.unregister(database)
        }
    }
}
