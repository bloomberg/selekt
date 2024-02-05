/*
 * Copyright 2020 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.android.support

import android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT
import android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteDatabase.CONFLICT_NONE
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteDatabase.CONFLICT_ROLLBACK
import com.bloomberg.selekt.android.ConflictAlgorithm
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ConflictAlgorithmsKtTest {
    @Test
    fun toAbortConflictAlgorithm() {
        assertEquals(ConflictAlgorithm.ABORT, CONFLICT_ABORT.toConflictAlgorithm())
    }

    @Test
    fun toFailConflictAlgorithm() {
        assertEquals(ConflictAlgorithm.FAIL, CONFLICT_FAIL.toConflictAlgorithm())
    }

    @Test
    fun toIgnoreConflictAlgorithm() {
        assertEquals(ConflictAlgorithm.IGNORE, CONFLICT_IGNORE.toConflictAlgorithm())
    }

    @Test
    fun toNoneConflictAlgorithm() {
        assertEquals(ConflictAlgorithm.NONE, CONFLICT_NONE.toConflictAlgorithm())
    }

    @Test
    fun toReplaceConflictAlgorithm() {
        assertEquals(ConflictAlgorithm.REPLACE, CONFLICT_REPLACE.toConflictAlgorithm())
    }

    @Test
    fun toRollbackConflictAlgorithm() {
        assertEquals(ConflictAlgorithm.ROLLBACK, CONFLICT_ROLLBACK.toConflictAlgorithm())
    }

    @Test
    fun toConflictAlgorithmThrows() {
        assertFailsWith<IndexOutOfBoundsException> {
            Int.MAX_VALUE.toConflictAlgorithm()
        }
    }
}
