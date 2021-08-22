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

import com.bloomberg.selekt.commons.loadEmbeddedLibrary
import com.bloomberg.selekt.externalSQLiteSingleton

private val sqlite = externalSQLiteSingleton {
    loadEmbeddedLibrary(com.bloomberg.selekt.SQLite::class.java.classLoader, "jni", "selekt")
}

internal object SQLite : com.bloomberg.selekt.SQLite(sqlite)
