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

package com.bloomberg.selekt.android.support

import com.bloomberg.selekt.ISQLProgram
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.same
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.Mock
import org.mockito.MockitoAnnotations

internal class SupportSQLiteProgramKtTest {
    @Mock
    lateinit var program: ISQLProgram

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun asSupportSQLiteProgramBindBlob() {
        val blob = ByteArray(0)
        program.asSupportSQLiteProgram().bindBlob(0, blob)
        verify(program, times(1)).bindBlob(eq(0), same(blob))
    }

    @Test
    fun asSupportSQLiteProgramBindLong() {
        val value = 42L
        program.asSupportSQLiteProgram().bindLong(0, value)
        verify(program, times(1)).bindLong(eq(0), eq(value))
    }

    @Test
    fun asSupportSQLiteProgramBindDouble() {
        val value = 42.0
        program.asSupportSQLiteProgram().bindDouble(0, value)
        verify(program, times(1)).bindDouble(eq(0), eq(value))
    }

    @Test
    fun asSupportSQLiteProgramBindNull() {
        program.asSupportSQLiteProgram().bindNull(0)
        verify(program, times(1)).bindNull(eq(0))
    }

    @Test
    fun asSupportSQLiteProgramBindString() {
        val value = "hello"
        program.asSupportSQLiteProgram().bindString(0, value)
        verify(program, times(1)).bindString(eq(0), same(value))
    }

    @Test
    fun asSupportSQLiteProgramClearBindings() {
        program.asSupportSQLiteProgram().clearBindings()
        verify(program, times(1)).clearBindings()
    }

    @Test
    fun asSupportSQLiteProgramClose() {
        program.asSupportSQLiteProgram().close()
        verify(program, times(1)).close()
    }
}
