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
 * SQLCipher database format selected for an encrypted connection.
 *
 * SQLCipher 5 cannot open SQLCipher 4 databases with its new defaults. Selekt therefore keeps [V4] as the default for
 * existing applications. Select [V5] when creating a new database, or use [MIGRATE_TO_V5] once to migrate an existing
 * database before reopening it with [V5].
 */
enum class SQLCipherCompatibility(
    val majorVersion: Int,
    val migrates: Boolean = false
) {
    V4(4),
    V5(5),
    /** Runs `PRAGMA cipher_migrate` as the first operation after applying the key. Back up the database first. */
    MIGRATE_TO_V5(5, migrates = true)
}
