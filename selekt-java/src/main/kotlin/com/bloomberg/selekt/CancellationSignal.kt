package com.bloomberg.selekt

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import javax.annotation.concurrent.ThreadSafe

@ThreadSafe
class CancellationSignal(
    internal val instructionCount: Int = DEFAULT_INSTRUCTION_COUNT
) {
    @Suppress("unused")
    @Volatile
    private var cancelled: Int = 0

    val isCancelled: Boolean
        get() = cancelledUpdater[this] != 0

    fun cancel() {
        cancelledUpdater[this] = 1
    }

    @JvmSynthetic
    internal fun throwIfCancelled() {
        if (isCancelled) {
            throw OperationCancelledException("Operation was cancelled.")
        }
    }

    internal companion object {
        const val DEFAULT_INSTRUCTION_COUNT = 1_000

        private val cancelledUpdater = AtomicIntegerFieldUpdater.newUpdater(
            CancellationSignal::class.java,
            "cancelled"
        )
    }
}

class OperationCancelledException(message: String) : RuntimeException(message)
