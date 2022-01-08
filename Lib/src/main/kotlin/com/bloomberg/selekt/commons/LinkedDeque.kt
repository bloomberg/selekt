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
import javax.annotation.concurrent.NotThreadSafe

@NotThreadSafe
internal class LinkedDeque<T> {
    @NotThreadSafe
    @PublishedApi
    internal data class Node<T>(
        @JvmField
        var item: T?,
        @JvmField
        var previous: Node<T>?,
        @JvmField
        var next: Node<T>?
    ) {
        fun clear() = apply {
            item = null
            previous = null
            next = null
        }
    }
    private var pool: Node<T>? = null

    private val emptyIterator = emptyLinkedDequeMutableIterator<T>()

    @JvmField
    var first: Node<T>? = null
    private var last: Node<T>? = null

    val isEmpty: Boolean
        get() = first == null

    val hasSizeOne: Boolean
        get() = first === last && first != null

    @Generated
    inline fun pollFirst(predicate: (T) -> Boolean): T? {
        var head = first
        while (head != null) {
            if (predicate(head.item!!)) {
                return unlink(head)
            }
            head = head.next
        }
        return null
    }

    fun pollLast() = last?.let {
        last = it.previous
        if (it === first) {
            first = null
        } else {
            it.previous!!.next = null
        }
        it.item.also { _ -> recycle(it) }
    }

    fun putFirst(item: T) {
        val head = first
        first = acquireNode(item)
        if (head == null) {
            last = first
        } else {
            head.previous = first
        }
    }

    fun reverseMutableIterator(): MutableIterator<T> = last.let {
        if (it != null) LinkedDequeReversedIterator(this, it) else emptyIterator
    }

    fun unlink(node: Node<T>) = node.run {
        next.let {
            if (it != null) {
                it.previous = previous
            } else {
                last = previous
            }
        }
        previous.let {
            if (it != null) {
                it.next = next
            } else {
                first = next
            }
        }
        item.also { recycle(this) }
    }

    private fun acquireNode(item: T): Node<T> {
        pool?.also {
            pool = null
            it.item = item
            it.next = first
            return@acquireNode it
        }
        return Node(item, null, first)
    }

    private fun recycle(node: Node<T>) {
        if (pool != null) {
            return
        }
        pool = node.clear()
    }
}

private class LinkedDequeReversedIterator<T>(
    private val deque: LinkedDeque<T>,
    last: LinkedDeque.Node<T>
) : MutableIterator<T> {
    private var head = LinkedDeque.Node(last.item, last, null)

    override fun hasNext() = head.previous != null

    override fun next() = (head.previous ?: throw NoSuchElementException()).also {
        head.previous = it.previous
        head.next = it
    }.item!!

    override fun remove() {
        deque.unlink(head.next!!)
    }
}

private fun <T> emptyLinkedDequeMutableIterator() = object : MutableIterator<T> {
    override fun hasNext() = false

    override fun next() = throw NoSuchElementException()

    override fun remove() = throw UnsupportedOperationException()
}
