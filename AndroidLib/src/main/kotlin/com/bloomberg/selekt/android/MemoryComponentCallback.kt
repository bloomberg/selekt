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

package com.bloomberg.selekt.android

import android.content.ComponentCallbacks2
import android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
import android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW
import android.content.res.Configuration
import android.util.Log
import com.bloomberg.selekt.android.Selekt.TAG
import com.bloomberg.selekt.pools.Priority

internal object MemoryComponentCallback : ComponentCallbacks2 {
    override fun onConfigurationChanged(newConfig: Configuration) = Unit

    override fun onLowMemory() = Unit

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "Received onTrimMemory code $level.")
        if (level >= TRIM_MEMORY_BACKGROUND) {
            sqlite.releaseMemory(Int.MAX_VALUE).also { Log.d(TAG, "sqlite released $it bytes.") }
            Log.d(TAG, "Releasing resources from all registered databases.")
            SQLiteDatabaseRegistry.releaseMemory(Priority.HIGH)
        } else if (level >= TRIM_MEMORY_RUNNING_LOW) {
            sqlite.releaseMemory(minOf(Int.MIN_VALUE, sqlite.softHeapLimit64().toInt() / 2)).also {
                Log.d(TAG, "sqlite released $it bytes.")
            }
            Log.d(TAG, "Releasing some resources from all registered databases.")
            SQLiteDatabaseRegistry.releaseMemory(Priority.LOW)
        }
    }
}
