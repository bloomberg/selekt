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

internal object SQLBindStrategyResolver {
    fun resolveAll(values: Array<*>): Array<SQLBindStrategy> = Array(values.size) {
        resolve(values[it])
    }

    private fun resolve(value: Any?): SQLBindStrategy = when (value) {
        is String -> SQLBindStrategy.StringValue
        is Int -> SQLBindStrategy.IntValue
        is Long -> SQLBindStrategy.LongValue
        null -> SQLBindStrategy.NullValue
        is Double -> SQLBindStrategy.DoubleValue
        is Float -> SQLBindStrategy.FloatValue
        is Short -> SQLBindStrategy.ShortValue
        is Byte -> SQLBindStrategy.ByteValue
        is ByteArray -> SQLBindStrategy.BlobValue
        is ZeroBlob -> SQLBindStrategy.ZeroBlobValue
        else -> throw IllegalArgumentException("Cannot bind arg of class ${value.javaClass}.")
    }
}
