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

package com.bloomberg.selekt.android

import com.bloomberg.selekt.IExternalSQLite
import com.bloomberg.selekt.SQLite
import com.bloomberg.selekt.commons.loadLibrary
import com.bloomberg.selekt.externalSQLiteSingleton

private const val CAN_USE_LOAD_PROPERTY_KEY = "com.bloomberg.selekt.can_use_load"

fun loadSQLite(): IExternalSQLite = externalSQLiteSingleton {
    "selekt".let {
        try {
            System.loadLibrary(it)
        } catch (e: UnsatisfiedLinkError) {
            if (System.getProperty(CAN_USE_LOAD_PROPERTY_KEY, null) == "true") {
                loadLibrary(checkNotNull(SQLite::class.java.classLoader), "jni", it)
            } else {
                throw e
            }
        }
    }
}
