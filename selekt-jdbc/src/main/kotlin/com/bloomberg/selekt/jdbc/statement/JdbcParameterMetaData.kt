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

package com.bloomberg.selekt.jdbc.statement

import java.sql.JDBCType
import java.sql.ParameterMetaData
import java.sql.SQLException
import java.sql.Types
import javax.annotation.concurrent.NotThreadSafe

@NotThreadSafe
internal class JdbcParameterMetaData(
    private val parameterCount: Int,
    private val parameters: Array<Any?>? = null
) : ParameterMetaData {
    override fun getParameterCount(): Int = parameterCount

    override fun isNullable(param: Int): Int {
        validateParameterIndex(param)
        return ParameterMetaData.parameterNullable
    }

    override fun isSigned(param: Int): Boolean {
        validateParameterIndex(param)
        return true
    }

    override fun getPrecision(param: Int): Int {
        validateParameterIndex(param)
        return 0
    }

    override fun getScale(param: Int): Int {
        validateParameterIndex(param)
        return 0
    }

    override fun getParameterType(param: Int): Int {
        validateParameterIndex(param)
        return when (parameters?.get(param - 1)) {
            is String -> Types.VARCHAR
            is Int -> Types.INTEGER
            null -> Types.NULL
            is Double, is Float -> Types.REAL
            is Long -> Types.BIGINT
            is Short, is Boolean -> Types.INTEGER
            is ByteArray -> Types.BLOB
            else -> Types.VARCHAR
        }
    }

    override fun getParameterTypeName(param: Int): String {
        validateParameterIndex(param)
        return JDBCType.valueOf(getParameterType(param)).name
    }

    override fun getParameterClassName(param: Int): String {
        validateParameterIndex(param)
        return String::class.java.name
    }

    override fun getParameterMode(param: Int): Int {
        validateParameterIndex(param)
        return ParameterMetaData.parameterModeIn
    }

    override fun <T> unwrap(iface: Class<T>): T {
        if (iface.isAssignableFrom(this::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return this as T
        }
        throw SQLException("Cannot unwrap to ${iface.name}")
    }

    override fun isWrapperFor(iface: Class<*>): Boolean = iface.isAssignableFrom(this::class.java)

    private fun validateParameterIndex(param: Int) {
        if (param !in 1..parameterCount) {
            throw SQLException("Parameter index $param is out of range (1, $parameterCount)")
        }
    }
}
