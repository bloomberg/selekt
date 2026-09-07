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

#include "fuzz_support.h"

#include <cstring>
#include <mutex>
#include <vector>

extern "C" int sqlite3_vec1_extra_init(const char* argument);

namespace selekt::fuzz {
namespace {

constexpr int MAX_VM_STEPS = 100'000;

int& remainingVmSteps() {
    static thread_local int steps = MAX_VM_STEPS;
    return steps;
}

int progressHandler(void*) {
    return --remainingVmSteps() <= 0;
}

int authorizer(void*, int action, const char*, const char*, const char*, const char*) {
    return action == SQLITE_ATTACH || action == SQLITE_DETACH ? SQLITE_DENY : SQLITE_OK;
}

int initializeExtensions() {
    static std::once_flag once;
    static int result = SQLITE_ERROR;
    std::call_once(once, [] {
        result = sqlite3_initialize();
        if (result == SQLITE_OK) result = sqlite3_vec1_extra_init(nullptr);
    });
    return result;
}

}

sqlite3* openDatabase() {
    if (initializeExtensions() != SQLITE_OK) return nullptr;
    sqlite3* database = nullptr;
    if (sqlite3_open_v2(
            ":memory:",
            &database,
            SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE | SQLITE_OPEN_NOMUTEX,
            nullptr
        ) != SQLITE_OK) {
        sqlite3_close_v2(database);
        return nullptr;
    }
    sqlite3_extended_result_codes(database, 1);
    sqlite3_busy_timeout(database, 1);
    sqlite3_limit(database, SQLITE_LIMIT_LENGTH, static_cast<int>(MAX_INPUT_SIZE * 4));
    sqlite3_limit(database, SQLITE_LIMIT_SQL_LENGTH, static_cast<int>(MAX_INPUT_SIZE));
    sqlite3_limit(database, SQLITE_LIMIT_COLUMN, 256);
    sqlite3_limit(database, SQLITE_LIMIT_COMPOUND_SELECT, 16);
    sqlite3_limit(database, SQLITE_LIMIT_EXPR_DEPTH, 64);
    sqlite3_limit(database, SQLITE_LIMIT_VDBE_OP, MAX_VM_STEPS);
    sqlite3_limit(database, SQLITE_LIMIT_VARIABLE_NUMBER, 256);
    sqlite3_set_authorizer(database, authorizer, nullptr);
    sqlite3_progress_handler(database, 100, progressHandler, nullptr);
    return database;
}

void resetExecutionBudget(sqlite3* database) {
    remainingVmSteps() = MAX_VM_STEPS;
    sqlite3_progress_handler(database, 100, progressHandler, nullptr);
}

void stepAndFinalize(sqlite3_stmt* statement) {
    if (statement == nullptr) return;
    for (int rows = 0; rows < 256 && sqlite3_step(statement) == SQLITE_ROW; ++rows) {
        // Result rows are intentionally discarded; stepping exercises their native decoding paths.
    }
    sqlite3_finalize(statement);
}

void executeSql(sqlite3* database, const char* sql, std::size_t size) {
    if (database == nullptr || sql == nullptr || size > MAX_INPUT_SIZE) return;
    std::vector<char> terminated(size + 1);
    if (size > 0) std::memcpy(terminated.data(), sql, size);
    const char* cursor = terminated.data();
    const char* end = cursor + size;
    while (cursor < end) {
        sqlite3_stmt* statement = nullptr;
        const char* tail = nullptr;
        resetExecutionBudget(database);
        const auto remaining = static_cast<int>(end - cursor);
        if (const auto result = sqlite3_prepare_v3(database, cursor, remaining, 0, &statement, &tail);
            result == SQLITE_OK) stepAndFinalize(statement);
        else sqlite3_finalize(statement);
        if (tail == nullptr || tail <= cursor) break;
        cursor = tail;
    }
}

Database::Database() : database_(openDatabase()) {
}

Database::~Database() {
    if (database_ != nullptr) {
        sqlite3_progress_handler(database_, 0, nullptr, nullptr);
        sqlite3_close_v2(database_);
    }
}

}
