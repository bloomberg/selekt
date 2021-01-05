/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt

typealias SQLOpenOperation = Int

// https://sqlite.org/c3ref/c_open_autoproxy.html
const val SQL_OPEN_READONLY: SQLOpenOperation = 1
const val SQL_OPEN_READWRITE: SQLOpenOperation = SQL_OPEN_READONLY shl 1
const val SQL_OPEN_CREATE: SQLOpenOperation = SQL_OPEN_READWRITE shl 1

const val SQL_OPEN_NO_MUTEX: SQLOpenOperation = 0x00008000
const val SQL_OPEN_FULL_MUTEX: SQLOpenOperation = SQL_OPEN_NO_MUTEX shl 1
const val SQL_OPEN_SHARED_CACHE: SQLOpenOperation = SQL_OPEN_FULL_MUTEX shl 1
const val SQL_OPEN_PRIVATE_CACHE: SQLOpenOperation = SQL_OPEN_SHARED_CACHE shl 1
