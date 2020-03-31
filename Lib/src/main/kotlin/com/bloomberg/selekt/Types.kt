/*
 * Copyright 2020 Bloomberg Finance L.P.
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

enum class ColumnType(val sqlDataType: SQLDataType) {
    INTEGER(SQL_INTEGER),
    FLOAT(SQL_FLOAT),
    STRING(SQL_TEXT),
    BLOB(SQL_BLOB),
    NULL(SQL_NULL);

    companion object {
        fun toColumnType(sqlDataType: SQLDataType) = values()[sqlDataType - 1]
    }
}

internal fun Any?.toColumnType() = when (this) {
    null -> ColumnType.NULL
    is String -> ColumnType.STRING
    is Long, is Int, is Short, is Byte -> ColumnType.INTEGER
    is Float, is Double -> ColumnType.FLOAT
    is ByteArray -> ColumnType.BLOB
    else -> ColumnType.STRING
}
