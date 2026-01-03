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

internal sealed interface SQLBindStrategy {
    fun bind(
        statement: SQLPreparedStatement,
        position: Int,
        value: Any?
    )

    object Universal : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.run {
            when (value) {
                is String -> bind(position, value)
                is Int -> bind(position, value)
                null -> bindNull(position)
                is Long -> bind(position, value)
                is Double -> bind(position, value)
                is ByteArray -> bind(position, value)
                is Float -> bind(position, value.toDouble())
                is Short -> bind(position, value.toInt())
                is Byte -> bind(position, value.toInt())
                is ZeroBlob -> bindZeroBlob(position, value.size)
                else -> throw IllegalArgumentException("Cannot bind arg of class ${value.javaClass} at position $position.")
            }
        }
    }

    object NullValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bindNull(position)
    }

    object ByteValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bind(position, (value as Byte).toInt())
    }

    object ShortValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bind(position, (value as Short).toInt())
    }

    object IntValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bind(position, value as Int)
    }

    object LongValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bind(position, value as Long)
    }

    object FloatValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bind(position, (value as Float).toDouble())
    }

    object DoubleValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bind(position, value as Double)
    }

    object StringValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bind(position, value as String)
    }

    object BlobValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bind(position, value as ByteArray)
    }

    object ZeroBlobValue : SQLBindStrategy {
        override fun bind(
            statement: SQLPreparedStatement,
            position: Int,
            value: Any?
        ) = statement.bindZeroBlob(position, (value as ZeroBlob).size)
    }
}
