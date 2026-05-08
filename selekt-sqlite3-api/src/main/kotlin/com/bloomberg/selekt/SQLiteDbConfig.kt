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
 * @since 0.30.0
 */
enum class SQLiteDbConfig(val code: Int) {
    DEFENSIVE(code = 1010),
    DQS_DDL(code = 1014),
    DQS_DML(code = 1013),
    ENABLE_FKEY(code = 998),
    ENABLE_FTS3_TOKENIZER(code = 1002),
    ENABLE_LOAD_EXTENSION(code = 1004),
    ENABLE_QPSG(code = 1006),
    ENABLE_TRIGGER(code = 999),
    ENABLE_VIEW(code = 1015),
    LEGACY_ALTER_TABLE(code = 1012),
    LEGACY_FILE_FORMAT(code = 1016),
    NO_CKPT_ON_CLOSE(code = 1005),
    RESET_DATABASE(code = 1008),
    REVERSE_SCANORDER(code = 1019),
    STMT_SCANSTATUS(code = 1018),
    TRIGGER_EQP(code = 1007),
    TRUSTED_SCHEMA(code = 1017),
    WRITABLE_SCHEMA(code = 1011)
}
