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

import javax.annotation.concurrent.NotThreadSafe

@NotThreadSafe
class SQLiteTraceEventMode {
    private var flag: Int = 0

    /**
     * Tracing is invoked when a database connection object closes.
     */
    fun enableClose() = apply {
        flag = flag.or(SQLTraceEventCode.CLOSE())
    }

    /**
     * Tracing is invoked with the estimated number of nanoseconds that a prepared statement took to run.
     */
    fun enableProfile() = apply {
        flag = flag.or(SQLTraceEventCode.PROFILE())
    }

    /**
     * Tracing is invoked whenever a prepared statement generates a single row of result.
     */
    fun enableRow() = apply {
        flag = flag.or(SQLTraceEventCode.ROW())
    }

    /**
     * Tracing is invoked when a prepared statement first begins running and possibly at other times during the execution of
     * the prepared statement, such as at the start of each trigger subprogram.
     */
    fun enableStatement() = apply {
        flag = flag.or(SQLTraceEventCode.STATEMENT())
    }

    operator fun invoke() = flag
}
