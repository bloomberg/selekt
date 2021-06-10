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

package com.bloomberg.selekt

private const val SQL_CHECKPOINT_PASSIVE = 0
private const val SQL_CHECKPOINT_FULL = 1
private const val SQL_CHECKPOINT_RESTART = 2
private const val SQL_CHECKPOINT_TRUNCATE = 3

internal enum class SQLCheckpointMode(
    private val value: Int
) {
    /**
     * Wait for writers, then checkpoint.
     */
    FULL(SQL_CHECKPOINT_FULL),

    /**
     * Do as much as possible without blocking.
     */
    PASSIVE(SQL_CHECKPOINT_PASSIVE),

    /**
     * Like FULL but wait for readers.
     */
    RESTART(SQL_CHECKPOINT_RESTART),

    /**
     * Like RESTART but also truncate WAL.
     */
    TRUNCATE(SQL_CHECKPOINT_TRUNCATE);

    operator fun invoke() = value
}
