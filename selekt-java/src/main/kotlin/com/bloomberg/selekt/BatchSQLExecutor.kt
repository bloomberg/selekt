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

import java.util.stream.Stream
import kotlin.streams.asSequence

internal interface BatchSQLExecutor {
    fun executeBatchForChangedRowCount(sql: String, bindArgs: Sequence<Array<out Any?>>): Int

    fun executeBatchForChangedRowCount(sql: String, bindArgs: Iterable<Array<out Any?>>): Int =
        executeBatchForChangedRowCount(sql, bindArgs.asSequence())

    fun executeBatchForChangedRowCount(sql: String, bindArgs: Stream<Array<out Any?>>): Int =
        executeBatchForChangedRowCount(sql, bindArgs.asSequence())
}
