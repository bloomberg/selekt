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

/**
 * Constants for [sqlite3_db_config](https://www.sqlite.org/c3ref/db_config.html) operations
 * that use the boolean `(int, int*)` variadic signature.
 *
 * @see <a href="https://www.sqlite.org/c3ref/c_dbconfig_defensive.html">SQLite DBCONFIG constants</a>
 */
object SQLiteDbConfig {
    const val ENABLE_FKEY = 998
    const val ENABLE_TRIGGER = 999
    const val ENABLE_FTS3_TOKENIZER = 1002
    const val ENABLE_LOAD_EXTENSION = 1004
    const val NO_CKPT_ON_CLOSE = 1005
    const val ENABLE_QPSG = 1006
    const val TRIGGER_EQP = 1007
    const val RESET_DATABASE = 1008
    const val DEFENSIVE = 1010
    const val WRITABLE_SCHEMA = 1011
    const val LEGACY_ALTER_TABLE = 1012
    const val DQS_DML = 1013
    const val DQS_DDL = 1014
    const val ENABLE_VIEW = 1015
    const val LEGACY_FILE_FORMAT = 1016
    const val TRUSTED_SCHEMA = 1017
    const val STMT_SCANSTATUS = 1018
    const val REVERSE_SCANORDER = 1019
}
