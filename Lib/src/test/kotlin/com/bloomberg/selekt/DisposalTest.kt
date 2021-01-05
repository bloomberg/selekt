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

package com.bloomberg.selekt

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test

private const val DB = 42L
private const val STMT = 43L

internal class DisposalTest {
    @Test
    fun registerConnection() {
        assertThatCode {
            Disposal.registerConnection(mock(), DB, mock())
            System.gc()
        }.doesNotThrowAnyException()
    }

    @Test
    fun registerStatement() {
        assertThatCode {
            Disposal.registerStatement(mock(), STMT, mock())
            System.gc()
        }.doesNotThrowAnyException()
    }
}
