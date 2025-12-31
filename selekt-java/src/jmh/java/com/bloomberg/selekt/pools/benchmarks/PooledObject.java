/*
 * Copyright 2023 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.pools.benchmarks;

import com.bloomberg.selekt.pools.IPooledObject;

public final class PooledObject implements IPooledObject<String> {
    private final String key;
    private boolean tag = false;

    public PooledObject(String key) {
        this.key = key;
    }

    public PooledObject() {
        this(String.valueOf(Thread.currentThread().getId()));
    }

    @Override
    public boolean isPrimary() {
        return false;
    }

    @Override
    public boolean matches(String key) {
        return this.key.equals(key);
    }

    @Override
    public boolean getTag() {
        return tag;
    }

    @Override
    public void setTag(boolean tag) {
        this.tag = tag;
    }

    @Override
    public void releaseMemory() {
        // No-op
    }
}