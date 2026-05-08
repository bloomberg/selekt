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

package com.bloomberg.selekt

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SQLiteDbConfigTest {
    @Test
    fun enableFKey() = assertEquals(998, SQLiteDbConfig.ENABLE_FKEY.code)

    @Test
    fun enableTrigger() = assertEquals(999, SQLiteDbConfig.ENABLE_TRIGGER.code)

    @Test
    fun enableFts3Tokenizer() = assertEquals(1002, SQLiteDbConfig.ENABLE_FTS3_TOKENIZER.code)

    @Test
    fun enableLoadExtension() = assertEquals(1004, SQLiteDbConfig.ENABLE_LOAD_EXTENSION.code)

    @Test
    fun noCkptOnClose() = assertEquals(1005, SQLiteDbConfig.NO_CKPT_ON_CLOSE.code)

    @Test
    fun enableQpsg() = assertEquals(1006, SQLiteDbConfig.ENABLE_QPSG.code)

    @Test
    fun triggerEqp() = assertEquals(1007, SQLiteDbConfig.TRIGGER_EQP.code)

    @Test
    fun resetDatabase() = assertEquals(1008, SQLiteDbConfig.RESET_DATABASE.code)

    @Test
    fun defensive() = assertEquals(1010, SQLiteDbConfig.DEFENSIVE.code)

    @Test
    fun writableSchema() = assertEquals(1011, SQLiteDbConfig.WRITABLE_SCHEMA.code)

    @Test
    fun legacyAlterTable() = assertEquals(1012, SQLiteDbConfig.LEGACY_ALTER_TABLE.code)

    @Test
    fun dqsDml() = assertEquals(1013, SQLiteDbConfig.DQS_DML.code)

    @Test
    fun dqsDdl() = assertEquals(1014, SQLiteDbConfig.DQS_DDL.code)

    @Test
    fun enableView() = assertEquals(1015, SQLiteDbConfig.ENABLE_VIEW.code)

    @Test
    fun legacyFileFormat() = assertEquals(1016, SQLiteDbConfig.LEGACY_FILE_FORMAT.code)

    @Test
    fun trustedSchema() = assertEquals(1017, SQLiteDbConfig.TRUSTED_SCHEMA.code)

    @Test
    fun stmtScanStatus() = assertEquals(1018, SQLiteDbConfig.STMT_SCANSTATUS.code)

    @Test
    fun reverseScanOrder() = assertEquals(1019, SQLiteDbConfig.REVERSE_SCANORDER.code)

    @Test
    fun entryCount() = assertEquals(18, SQLiteDbConfig.entries.size)

    @Test
    fun entriesAreSortedLexicographically() {
        SQLiteDbConfig.entries.map(SQLiteDbConfig::name).let {
            assertEquals(it.sorted(), it)
        }
    }
}
