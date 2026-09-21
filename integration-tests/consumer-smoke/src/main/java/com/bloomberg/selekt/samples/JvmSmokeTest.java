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

package com.bloomberg.selekt.samples;

import java.lang.reflect.InvocationTargetException;

public final class JvmSmokeTest {
    private JvmSmokeTest() {
    }

    public static void main(final String[] args) {
        BackendSelection.assertSelectedBackend(args);

        final Object sqlite = externalSQLiteSingleton();
        try {
            final String version = (String) sqlite.getClass().getMethod("libVersion").invoke(sqlite);
            if (version.isBlank()) {
                throw new AssertionError("SQLite returned an empty version");
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            throw new AssertionError("Unable to call the selected SQLite backend", exception);
        }
    }

    private static Object externalSQLiteSingleton() {
        try {
            return Class.forName("com.bloomberg.selekt.ExternalSQLiteKt")
                .getMethod("externalSQLiteSingleton")
                .invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException
                 exception) {
            throw new AssertionError("Unable to load the selected SQLite backend", exception);
        }
    }
}
