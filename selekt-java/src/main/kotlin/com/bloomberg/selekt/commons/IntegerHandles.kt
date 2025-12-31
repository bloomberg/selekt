/*
 * Copyright 2025 Bloomberg Finance L.P.
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

@file:Suppress("TooManyFunctions")

package com.bloomberg.selekt.commons

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater

/**
 * Creates an optimized integer field handle using [VarHandle] when available, falling back to
 * [AtomicIntegerFieldUpdater] on older platforms.
 */
@Suppress("FunctionName", "NewApi")
internal inline fun <reified T> IntegerHandle(
    fieldName: String
): Any {
    return if (isVarHandleAvailable) {
        MethodHandles.privateLookupIn(T::class.java, MethodHandles.lookup())
            .findVarHandle(T::class.java, fieldName, Int::class.javaPrimitiveType)
    } else {
        AtomicIntegerFieldUpdater.newUpdater(T::class.java, fieldName)
    }
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> getInt(
    handle: Any,
    instance: T
): Int = if (isVarHandleAvailable) {
    (handle as VarHandle)[instance] as Int
} else {
    (handle as AtomicIntegerFieldUpdater<T>)[instance]
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> setInt(
    handle: Any,
    instance: T,
    value: Int
) = if (isVarHandleAvailable) {
    (handle as VarHandle).set(instance, value)
} else {
    (handle as AtomicIntegerFieldUpdater<T>)[instance] = value
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> compareAndSetInt(
    handle: Any,
    instance: T,
    expected: Int,
    updated: Int
): Boolean = if (isVarHandleAvailable) {
    (handle as VarHandle).compareAndSet(instance, expected, updated)
} else {
    (handle as AtomicIntegerFieldUpdater<T>).compareAndSet(instance, expected, updated)
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> getAndIncrement(
    handle: Any,
    instance: T
): Int = if (isVarHandleAvailable) {
    (handle as VarHandle).getAndAdd(instance, 1) as Int
} else {
    (handle as AtomicIntegerFieldUpdater<T>).getAndIncrement(instance)
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> decrementAndGet(
    handle: Any,
    instance: T
): Int = if (isVarHandleAvailable) {
    (handle as VarHandle).getAndAdd(instance, -1) as Int - 1
} else {
    (handle as AtomicIntegerFieldUpdater<T>).decrementAndGet(instance)
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> getAndSetInt(
    handle: Any,
    instance: T,
    value: Int
): Int = if (isVarHandleAvailable) {
    (handle as VarHandle).getAndSet(instance, value) as Int
} else {
    (handle as AtomicIntegerFieldUpdater<T>).getAndSet(instance, value)
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> getIntAcquire(
    handle: Any,
    instance: T
): Int = if (isVarHandleAvailable) {
    (handle as VarHandle).getAcquire(instance) as Int
} else {
    (handle as AtomicIntegerFieldUpdater<T>)[instance]
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> getAndIncrementAcquire(
    handle: Any,
    instance: T
): Int = if (isVarHandleAvailable) {
    (handle as VarHandle).getAndAddAcquire(instance, 1) as Int
} else {
    (handle as AtomicIntegerFieldUpdater<T>).getAndIncrement(instance)
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> decrementAndGetRelease(
    handle: Any,
    instance: T
): Int = if (isVarHandleAvailable) {
    (handle as VarHandle).getAndAddRelease(instance, -1) as Int - 1
} else {
    (handle as AtomicIntegerFieldUpdater<T>).decrementAndGet(instance)
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> compareAndSetIntAcquire(
    handle: Any,
    instance: T,
    expected: Int,
    updated: Int
): Boolean = if (isVarHandleAvailable) {
    (handle as VarHandle).weakCompareAndSetAcquire(instance, expected, updated)
} else {
    (handle as AtomicIntegerFieldUpdater<T>).compareAndSet(instance, expected, updated)
}

@Suppress("NewApi", "UNCHECKED_CAST")
internal inline fun <reified T> setIntRelease(
    handle: Any,
    instance: T,
    value: Int
) = if (isVarHandleAvailable) {
    (handle as VarHandle).setRelease(instance, value)
} else {
    (handle as AtomicIntegerFieldUpdater<T>)[instance] = value
}

private val isVarHandleAvailable: Boolean = runCatching {
    @Suppress("NewApi")
    MethodHandles.privateLookupIn(IntegerVarHandleTest::class.java, MethodHandles.lookup())
        .findVarHandle(IntegerVarHandleTest::class.java, "testField", Int::class.javaPrimitiveType)
    true
}.getOrDefault(false)

private class IntegerVarHandleTest {
    @Suppress("unused")
    @Volatile
    var testField: Int = 0
}
