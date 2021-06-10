/*
 * Copyright 2021 Bloomberg Finance L.P.
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

import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.junit.Test

internal class SupportSQLiteOpenHelperCallbackTest {
    @Test
    fun asSelektCallbackOnConfigure() {
        val callback = mock<SupportSQLiteOpenHelper.Callback>()
        callback.asSelektCallback().onConfigure(mock())
        verify(callback, times(1)).onConfigure(any())
    }

    @Test
    fun asSelektCallbackOnCreate() {
        val callback = mock<SupportSQLiteOpenHelper.Callback>()
        callback.asSelektCallback().onCreate(mock())
        verify(callback, times(1)).onCreate(any())
    }

    @Test
    fun asSelektCallbackOnDowngrade() {
        val callback = mock<SupportSQLiteOpenHelper.Callback>()
        callback.asSelektCallback().onDowngrade(mock(), 2, 1)
        verify(callback, times(1)).onDowngrade(any(), eq(2), eq(1))
    }

    @Test
    fun asSelektCallbackOnUpgrade() {
        val callback = mock<SupportSQLiteOpenHelper.Callback>()
        callback.asSelektCallback().onUpgrade(mock(), 1, 2)
        verify(callback, times(1)).onUpgrade(any(), eq(1), eq(2))
    }
}
