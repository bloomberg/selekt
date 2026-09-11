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

package com.bloomberg.selekt;

import java.nio.ByteBuffer;
import java.util.IdentityHashMap;

final class CursorWindowOwnershipRegistry {
    // Identity is the ownership token. Buffer equality is content-based, and native addresses may be reused after free.
    private final IdentityHashMap<ByteBuffer, Boolean> allocations = new IdentityHashMap<>();

    synchronized ByteBuffer register(final ByteBuffer buffer) {
        if (allocations.containsKey(buffer)) {
            throw new IllegalStateException("Cursor window is already registered.");
        }
        allocations.put(buffer, Boolean.TRUE);
        return buffer;
    }

    synchronized ByteBuffer consume(final ByteBuffer buffer) {
        if (allocations.remove(buffer) == null) {
            throw new IllegalArgumentException(
                "Cursor window must be the live buffer returned by fillCursorWindow."
            );
        }
        return buffer;
    }
}
