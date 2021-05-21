/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bloomberg.selekt.android.support

import android.database.sqlite.SQLiteTransactionListener
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class WrappedSQLiteTransactionListenerTest {
    @Test
    fun listenerCallsBegin() {
        val listener = mock<SQLiteTransactionListener>()
        listener.asSQLTransactionListener().onBegin()
        verify(listener, times(1)).onBegin()
        verify(listener, never()).onCommit()
        verify(listener, never()).onRollback()
    }

    @Test
    fun listenerCallsCommit() {
        val listener = mock<SQLiteTransactionListener>()
        listener.asSQLTransactionListener().onCommit()
        verify(listener, never()).onBegin()
        verify(listener, times(1)).onCommit()
        verify(listener, never()).onRollback()
    }

    @Test
    fun listenerCallsRollback() {
        val listener = mock<SQLiteTransactionListener>()
        listener.asSQLTransactionListener().onRollback()
        verify(listener, never()).onBegin()
        verify(listener, never()).onCommit()
        verify(listener, times(1)).onRollback()
    }

    @Test
    fun equalsThis() {
        val listener = mock<SQLiteTransactionListener>().asSQLTransactionListener()
        assertEquals(listener, listener)
    }

    @Test
    fun equalsListener() {
        val listener = mock<SQLiteTransactionListener>()
        assertEquals(listener.asSQLTransactionListener(), listener.asSQLTransactionListener())
    }

    @Test
    fun notEquals() {
        assertNotEquals(
            mock<SQLiteTransactionListener>().asSQLTransactionListener(),
            mock<SQLiteTransactionListener>().asSQLTransactionListener()
        )
    }

    @Test
    fun notEqualsAny() {
        assertNotEquals(mock<SQLiteTransactionListener>().asSQLTransactionListener(), Any())
    }

    @Test
    fun hashCodeListener() {
        val listener = object : SQLiteTransactionListener {
            override fun onBegin() = Unit

            override fun onCommit() = Unit

            override fun onRollback() = Unit

            override fun hashCode() = 42
        }
        assertEquals(listener.hashCode(), listener.asSQLTransactionListener().hashCode())
    }
}
