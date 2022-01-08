/*
 * Copyright 2022 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.commons

import com.bloomberg.selekt.annotations.Generated

@Generated("Jacoco does not report coverage for inline methods")
internal inline fun <T> Array<T>.forEachByIndex(block: (Int, T) -> Unit) {
    var i = 0
    while (i < size) {
        block(i, this[i++])
    }
}

@Generated("Jacoco does not report coverage for inline methods")
internal inline fun <T> Array<T>.forEachByPosition(block: (T, Int) -> Unit) {
    var i = 0
    while (i < size) {
        block(this[i++], i)
    }
}

internal fun <T, A : Appendable> Array<out T>.joinTo(buffer: A, separator: Char) = buffer.apply {
    forEachByIndex { i, it ->
        if (i > 0) {
            append(separator)
        }
        appendElement(it)
    }
}

private fun <T> Appendable.appendElement(element: T) = when (element) {
    is CharSequence? -> append(element)
    is Char -> append(element)
    else -> append(element.toString())
}
