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

package com.bloomberg.selekt.jvm

import com.bloomberg.selekt.DatabaseConfiguration
import com.bloomberg.selekt.SQLDatabase
import com.bloomberg.selekt.SQLiteJournalMode
import com.bloomberg.selekt.SQLiteTraceEventMode
import java.io.File

fun openOrCreateDatabase(file: File, configuration: DatabaseConfiguration, key: ByteArray?) =
    openOrCreateDatabase(file.absolutePath, configuration, key)

fun createInMemoryDatabase(trace: SQLiteTraceEventMode? = null) = openOrCreateDatabase(
    "file::memory:",
    SQLiteJournalMode.MEMORY.databaseConfiguration.copy(trace = trace),
    null
)

private fun openOrCreateDatabase(
    path: String,
    configuration: DatabaseConfiguration,
    key: ByteArray?
) = SQLDatabase(path, SQLite, configuration, key)
