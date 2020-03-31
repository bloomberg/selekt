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

package com.bloomberg.commons

fun <T : Any?> emptyIterable() = object : Iterable<T> {
    private val iterator = emptyIterator<T>()

    override fun iterator() = iterator
}

private class EmptyIterator<L, R> : Iterator<Pair<L, R>> {
    override fun hasNext() = false

    override fun next() = throw NoSuchElementException()
}

private fun <T : Any?> emptyIterator() = object : Iterator<T> {
    override fun hasNext() = false

    override fun next() = throw NoSuchElementException()
}

operator fun <L : Any?, R : Any?> Iterable<L>.times(other: Iterable<R>) = object : Iterable<Pair<L, R>> {
    override fun iterator(): Iterator<Pair<L, R>> = if (!(this@times.any() && other.any())) {
        EmptyIterator()
    } else {
        object : Iterator<Pair<L, R>> {
            private val left = this@times.iterator()
            private var current = left.next()
            private var right = other.iterator()

            override fun hasNext() = left.hasNext() || right.hasNext()

            override fun next() = when {
                right.hasNext() -> Pair(current, right.next())
                left.hasNext() -> {
                    right = other.iterator()
                    Pair(left.next(), right.next()).also { current = it.first }
                }
                else -> throw NoSuchElementException()
            }
        }
    }
}

operator fun <L : Any?, R : Any?> Array<L>.times(other: Array<R>) = asIterable() * other.asIterable()

operator fun <L : Any?, R : Any?> Array<L>.times(other: Iterable<R>) = asIterable() * other

operator fun <L : Any?, R : Any?> Iterable<L>.times(other: Array<R>) = this * other.asIterable()
