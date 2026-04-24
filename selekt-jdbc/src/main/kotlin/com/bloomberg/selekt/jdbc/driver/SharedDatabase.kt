/*
 * Copyright 2026 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.jdbc.driver

import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SharedResource

/**
 * A reference-counted wrapper around [SQLDatabase] for use by the JDBC driver cache.
 *
 * The cache holds one reference (the initial retain count of 1), keeping the database alive
 * for reuse even when no connections are open. Each
 * [JdbcConnection][com.bloomberg.selekt.jdbc.connection.JdbcConnection] obtains an additional
 * reference via [retain], and releases it via [release]. When the last reference is released
 * the underlying [SQLDatabase] is closed and [onClose] is invoked.
 */
internal class SharedDatabase(
    val database: SQLDatabase,
    private val onClose: () -> Unit = {}
) : SharedResource() {
    override fun onReleased() {
        database.use { _ ->
            onClose()
        }
    }
}
