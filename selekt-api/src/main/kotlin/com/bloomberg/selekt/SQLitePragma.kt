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
 * An allow-list of SQLite PRAGMA statements that may be used with [SQLDatabase.pragma].
 *
 * @since 0.29.9.
 */
enum class SQLitePragma(
    @JvmField
    val key: String
) {
    ANALYSIS_LIMIT("analysis_limit"),
    APPLICATION_ID("application_id"),
    AUTO_VACUUM("auto_vacuum"),
    AUTOMATIC_INDEX("automatic_index"),
    BUSY_TIMEOUT("busy_timeout"),
    CACHE_SIZE("cache_size"),
    CACHE_SPILL("cache_spill"),
    CASE_SENSITIVE_LIKE("case_sensitive_like"),
    CELL_SIZE_CHECK("cell_size_check"),
    CHECKPOINT_FULLFSYNC("checkpoint_fullfsync"),
    COLLATION_LIST("collation_list"),
    COMPILE_OPTIONS("compile_options"),
    DATA_VERSION("data_version"),
    DATABASE_LIST("database_list"),
    DEFER_FOREIGN_KEYS("defer_foreign_keys"),
    ENCODING("encoding"),
    FOREIGN_KEY_CHECK("foreign_key_check"),
    FOREIGN_KEY_LIST("foreign_key_list"),
    FOREIGN_KEYS("foreign_keys"),
    FREELIST_COUNT("freelist_count"),
    FULLFSYNC("fullfsync"),
    FUNCTION_LIST("function_list"),
    HARD_HEAP_LIMIT("hard_heap_limit"),
    IGNORE_CHECK_CONSTRAINTS("ignore_check_constraints"),
    INCREMENTAL_VACUUM("incremental_vacuum"),
    INDEX_INFO("index_info"),
    INDEX_LIST("index_list"),
    INDEX_XINFO("index_xinfo"),
    INTEGRITY_CHECK("integrity_check"),
    JOURNAL_MODE("journal_mode"),
    JOURNAL_SIZE_LIMIT("journal_size_limit"),
    LEGACY_ALTER_TABLE("legacy_alter_table"),
    LOCKING_MODE("locking_mode"),
    MAX_PAGE_COUNT("max_page_count"),
    MMAP_SIZE("mmap_size"),
    MODULE_LIST("module_list"),
    OPTIMIZE("optimize"),
    PAGE_COUNT("page_count"),
    PAGE_SIZE("page_size"),
    PRAGMA_LIST("pragma_list"),
    QUERY_ONLY("query_only"),
    QUICK_CHECK("quick_check"),
    READ_UNCOMMITTED("read_uncommitted"),
    RECURSIVE_TRIGGERS("recursive_triggers"),
    REVERSE_UNORDERED_SELECTS("reverse_unordered_selects"),
    SCHEMA_VERSION("schema_version"),
    SECURE_DELETE("secure_delete"),
    SHRINK_MEMORY("shrink_memory"),
    SOFT_HEAP_LIMIT("soft_heap_limit"),
    SYNCHRONOUS("synchronous"),
    TABLE_INFO("table_info"),
    TABLE_LIST("table_list"),
    TABLE_XINFO("table_xinfo"),
    TEMP_STORE("temp_store"),
    THREADS("threads"),
    TRUSTED_SCHEMA("trusted_schema"),
    USER_VERSION("user_version"),
    WAL_AUTOCHECKPOINT("wal_autocheckpoint"),
    WAL_CHECKPOINT("wal_checkpoint"),
    WRITABLE_SCHEMA("writable_schema");
}
