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

fun <T, A : Appendable> Array<out T>.joinTo(buffer: A, separator: Char) = buffer.apply {
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
