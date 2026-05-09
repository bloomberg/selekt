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

private const val TAG_NULL: Byte = 0
private const val TAG_INT: Byte = 1
private const val TAG_LONG: Byte = 2
private const val TAG_DOUBLE: Byte = 3
private const val TAG_OBJECT: Byte = 4

class ParameterRow(
    @JvmField val size: Int
) {
    @JvmField val tags = ByteArray(size)
    @JvmField val ints = IntArray(size)
    @JvmField val longs = LongArray(size)
    @JvmField val doubles = DoubleArray(size)
    @JvmField val objects = arrayOfNulls<Any>(size)

    fun setNull(index: Int) {
        tags[index] = TAG_NULL
        objects[index] = null
    }

    fun setInt(index: Int, value: Int) {
        tags[index] = TAG_INT
        ints[index] = value
    }

    fun setLong(index: Int, value: Long) {
        tags[index] = TAG_LONG
        longs[index] = value
    }

    fun setDouble(index: Int, value: Double) {
        tags[index] = TAG_DOUBLE
        doubles[index] = value
    }

    fun setObject(index: Int, value: Any?) {
        if (value == null) {
            setNull(index)
        } else {
            tags[index] = TAG_OBJECT
            objects[index] = value
        }
    }

    fun setAny(index: Int, value: Any?): Unit = when (value) {
        null -> setNull(index)
        is Int -> setInt(index, value)
        is Long -> setLong(index, value)
        is Double -> setDouble(index, value)
        else -> setObject(index, value)
    }

    fun copyFrom(other: ParameterRow) {
        System.arraycopy(other.tags, 0, tags, 0, size)
        System.arraycopy(other.ints, 0, ints, 0, size)
        System.arraycopy(other.longs, 0, longs, 0, size)
        System.arraycopy(other.doubles, 0, doubles, 0, size)
        System.arraycopy(other.objects, 0, objects, 0, size)
    }

    fun materializeTo(target: Array<Any?>) {
        for (i in 0 until size) {
            target[i] = when (tags[i]) {
                TAG_INT -> ints[i]
                TAG_LONG -> longs[i]
                TAG_DOUBLE -> doubles[i]
                TAG_OBJECT -> objects[i]
                else -> null
            }
        }
    }

    fun clear() {
        tags.fill(TAG_NULL)
        objects.fill(null)
    }
}
