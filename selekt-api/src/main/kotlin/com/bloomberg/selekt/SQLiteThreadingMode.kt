/*
 * Copyright 2020 Bloomberg Finance L.P.
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

package com.bloomberg.selekt

enum class SQLiteThreadingMode(private val value: Int) {
    /**
     * This option sets the threading mode to "multi-thread". In other words, it disables mutexing on database connection
     * and prepared statement objects. The application is responsible for serializing access to database connections and
     * prepared statements. But other mutexes are enabled so that SQLite will be safe to use in a multi-threaded environment
     * as long as no two threads attempt to use the same database connection at the same time.
     */
    MULTITHREAD(2),

    /**
     * This option sets the threading mode to "serialized". In other words, this option enables all mutexes including the
     * recursive mutexes on database connection and prepared statement objects. In this mode the SQLite library will itself
     * serialize access to database connections and prepared statements so that the application is free to use the same
     * database connection or the same prepared statement in different threads at the same time.
     */
    SERIALIZED(1),

    /**
     * This option sets the threading mode to "single-thread". In other words, it disables all mutexing and puts SQLite into
     * a mode where it can only be used by a single thread.
     */
    SINGLETHREAD(0);

    override fun toString() = "$value"
}
