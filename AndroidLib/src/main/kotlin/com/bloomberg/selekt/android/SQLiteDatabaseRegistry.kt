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

import android.util.Log
import com.bloomberg.selekt.pools.Priority
import javax.annotation.concurrent.GuardedBy

internal object SQLiteDatabaseRegistry {
    private val lock = Any()
    @GuardedBy("lock")
    private val store = mutableSetOf<SQLiteDatabase>()

    fun register(database: SQLiteDatabase) = synchronized(lock) {
        check(store.add(database)) { "Failed to register a database, ${store.count()} registered." }
    }

    fun unregister(database: SQLiteDatabase) = synchronized(lock) {
        check(store.remove(database)) { "Failed to unregister a database, ${store.count()} registered." }
    }

    fun releaseMemory(priority: Priority) {
        synchronized(lock) { store.toList() }.run {
            forEach {
                it.releaseMemory(priority)
            }
            Log.d(Selekt.TAG, "Released resources from ${count()} databases.")
        }
    }
}
