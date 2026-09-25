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

package com.bloomberg.selekt.jdbc

import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * Selekt-specific operations available through [PreparedStatement.unwrap].
 *
 * @since 1.6.9
 */
fun interface SelektPreparedStatement {
    /**
     * Executes the current batch and returns a driver-owned update-count array without copying it.
     *
     * The returned array must be treated as read-only. It may be reused by a later batch execution or after the
     * prepared statement is closed and returned to Selekt's statement pool. Call [PreparedStatement.executeBatch]
     * instead when the result must remain independently owned by the caller.
     */
    @Throws(SQLException::class)
    fun executeBatchShared(): IntArray
}
