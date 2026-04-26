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

package com.bloomberg.selekt

/**
 * Callback interface for SQLite's progress handler. The handler is invoked periodically during long-running SQL
 * statements.
 *
 * @see <a href="https://www.sqlite.org/c3ref/progress_handler.html">sqlite3_progress_handler</a>
 */
fun interface SQLProgressHandler {
    /**
     * Called periodically during long-running SQL statements.
     *
     * @return non-zero to interrupt the operation, zero to continue.
     */
    fun onProgress(): Int
}
