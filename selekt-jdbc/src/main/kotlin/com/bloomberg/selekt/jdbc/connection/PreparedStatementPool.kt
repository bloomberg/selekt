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

package com.bloomberg.selekt.jdbc.connection

import com.bloomberg.selekt.jdbc.statement.PreparedStatementCacheEntry
import javax.annotation.concurrent.NotThreadSafe

private const val EMPTY = -1

private const val HASH_MULTIPLIER = 31

/**
 * A fixed-capacity, insertion-ordered hash table specialized for prepared-statement cache entries.
 *
 * The table uses preallocated primitive arrays and the cache entry itself as its key. Consequently, both [take] and
 * [put] are allocation-free. This class is not thread-safe; [JdbcConnection] serializes access with its pool lock.
 */
@NotThreadSafe
internal class PreparedStatementPool(private val capacity: Int) {
    private val bucketHeads: IntArray
    private val entries: Array<PreparedStatementCacheEntry?>
    private val hashes: IntArray
    private val nextInBucket: IntArray
    private val previousInOrder: IntArray
    private val nextInOrder: IntArray
    private val nextFree: IntArray
    private var freeHead = 0
    private var eldest = EMPTY
    private var newest = EMPTY

    var size: Int = 0
        private set

    init {
        require(capacity > 0) { "Capacity must be positive" }
        val bucketCount = bucketCount(capacity)
        bucketHeads = IntArray(bucketCount) { EMPTY }
        entries = arrayOfNulls(capacity)
        hashes = IntArray(capacity)
        nextInBucket = IntArray(capacity) { EMPTY }
        previousInOrder = IntArray(capacity) { EMPTY }
        nextInOrder = IntArray(capacity) { EMPTY }
        nextFree = IntArray(capacity) { index -> if (index + 1 < capacity) { index + 1 } else { EMPTY } }
    }

    fun take(
        sql: String,
        resultSetType: Int,
        resultSetConcurrency: Int,
        resultSetHoldability: Int
    ): PreparedStatementCacheEntry? {
        val hash = hash(sql, resultSetType, resultSetConcurrency, resultSetHoldability)
        val bucket = hash and bucketHeads.size - 1
        var previous = EMPTY
        var slot = bucketHeads[bucket]
        while (slot != EMPTY) {
            val entry = checkNotNull(entries[slot])
            if (hashes[slot] == hash && entry.matches(
                    sql,
                    resultSetType,
                    resultSetConcurrency,
                    resultSetHoldability
                )
            ) {
                unlinkFromBucket(bucket, previous, slot)
                unlinkFromOrder(slot)
                release(slot)
                return entry
            }
            previous = slot
            slot = nextInBucket[slot]
        }
        return null
    }

    /**
     * Adds [entry], returning a replaced entry or the eldest entry evicted at capacity.
     */
    fun put(entry: PreparedStatementCacheEntry): PreparedStatementCacheEntry? {
        val hash = entry.poolHash()
        val bucket = hash and bucketHeads.size - 1
        var slot = bucketHeads[bucket]
        while (slot != EMPTY) {
            val current = checkNotNull(entries[slot])
            if (hashes[slot] == hash && current.hasSamePoolKey(entry)) {
                entries[slot] = entry
                return current
            }
            slot = nextInBucket[slot]
        }

        val evicted = if (size == capacity) { remove(eldest) } else { null }
        slot = claim()
        entries[slot] = entry
        hashes[slot] = hash
        nextInBucket[slot] = bucketHeads[bucket]
        bucketHeads[bucket] = slot
        appendToOrder(slot)
        return evicted
    }

    fun isEmpty(): Boolean = size == 0

    fun drain(): List<PreparedStatementCacheEntry> {
        val snapshot = ArrayList<PreparedStatementCacheEntry>(size)
        var slot = eldest
        while (slot != EMPTY) {
            snapshot += checkNotNull(entries[slot])
            slot = nextInOrder[slot]
        }
        reset()
        return snapshot
    }

    private fun remove(slot: Int): PreparedStatementCacheEntry {
        val bucket = hashes[slot] and bucketHeads.size - 1
        var previous = EMPTY
        var current = bucketHeads[bucket]
        while (current != slot) {
            check(current != EMPTY)
            previous = current
            current = nextInBucket[current]
        }
        val entry = checkNotNull(entries[slot])
        unlinkFromBucket(bucket, previous, slot)
        unlinkFromOrder(slot)
        release(slot)
        return entry
    }

    private fun unlinkFromBucket(bucket: Int, previous: Int, slot: Int) {
        if (previous == EMPTY) {
            bucketHeads[bucket] = nextInBucket[slot]
        } else {
            nextInBucket[previous] = nextInBucket[slot]
        }
    }

    private fun unlinkFromOrder(slot: Int) {
        val previous = previousInOrder[slot]
        val next = nextInOrder[slot]
        if (previous == EMPTY) {
            eldest = next
        } else {
            nextInOrder[previous] = next
        }
        if (next == EMPTY) {
            newest = previous
        } else {
            previousInOrder[next] = previous
        }
    }

    private fun release(slot: Int) {
        entries[slot] = null
        hashes[slot] = 0
        nextInBucket[slot] = EMPTY
        previousInOrder[slot] = EMPTY
        nextInOrder[slot] = EMPTY
        nextFree[slot] = freeHead
        freeHead = slot
        --size
    }

    private fun claim(): Int {
        check(freeHead != EMPTY)
        val slot = freeHead
        freeHead = nextFree[slot]
        nextFree[slot] = EMPTY
        ++size
        return slot
    }

    private fun appendToOrder(slot: Int) {
        previousInOrder[slot] = newest
        if (newest == EMPTY) {
            eldest = slot
        } else {
            nextInOrder[newest] = slot
        }
        newest = slot
    }

    private fun reset() {
        bucketHeads.fill(EMPTY)
        entries.fill(null)
        hashes.fill(0)
        nextInBucket.fill(EMPTY)
        previousInOrder.fill(EMPTY)
        nextInOrder.fill(EMPTY)
        nextFree.indices.forEach { index ->
            nextFree[index] = if (index + 1 < capacity) { index + 1 } else { EMPTY }
        }
        freeHead = 0
        eldest = EMPTY
        newest = EMPTY
        size = 0
    }

    private fun PreparedStatementCacheEntry.poolHash(): Int = hash(
        sql,
        resultSetType,
        resultSetConcurrency,
        resultSetHoldability
    )

    private fun PreparedStatementCacheEntry.matches(
        sql: String,
        resultSetType: Int,
        resultSetConcurrency: Int,
        resultSetHoldability: Int
    ): Boolean = this.sql == sql &&
        this.resultSetType == resultSetType &&
        this.resultSetConcurrency == resultSetConcurrency &&
        this.resultSetHoldability == resultSetHoldability

    private fun PreparedStatementCacheEntry.hasSamePoolKey(other: PreparedStatementCacheEntry): Boolean = matches(
        other.sql,
        other.resultSetType,
        other.resultSetConcurrency,
        other.resultSetHoldability
    )

    private companion object {
        private fun bucketCount(capacity: Int): Int {
            var count = 1
            while (count < capacity * 2) {
                count = count shl 1
            }
            return count
        }

        private fun hash(
            sql: String,
            resultSetType: Int,
            resultSetConcurrency: Int,
            resultSetHoldability: Int
        ): Int {
            var result = sql.hashCode()
            result = HASH_MULTIPLIER * result + resultSetType
            result = HASH_MULTIPLIER * result + resultSetConcurrency
            result = HASH_MULTIPLIER * result + resultSetHoldability
            return result xor (result ushr Integer.SIZE / 2)
        }
    }
}
