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

#include <string>

namespace {

void exercise(sqlite3* database, const char* sql, const uint8_t* data, std::size_t size, bool text) {
    sqlite3_stmt* statement = nullptr;
    if (sqlite3_prepare_v2(database, sql, -1, &statement, nullptr) != SQLITE_OK) return;
    const auto length = static_cast<int>(size);
    if (text) sqlite3_bind_text(statement, 1, reinterpret_cast<const char*>(data), length, SQLITE_TRANSIENT);
    else sqlite3_bind_blob(statement, 1, data, length, SQLITE_TRANSIENT);
    selekt::fuzz::resetExecutionBudget(database);
    selekt::fuzz::stepAndFinalize(statement);
}

void exerciseDistance(sqlite3* database, const char* function, const uint8_t* data, std::size_t size) {
    const std::string sql = std::string("SELECT ") + function + "(?1, ?2)";
    sqlite3_stmt* statement = nullptr;
    if (sqlite3_prepare_v2(database, sql.c_str(), -1, &statement, nullptr) != SQLITE_OK) return;
    const std::size_t split = size / 2;
    sqlite3_bind_blob(statement, 1, data, static_cast<int>(split), SQLITE_TRANSIENT);
    sqlite3_bind_blob(statement, 2, data + split, static_cast<int>(size - split), SQLITE_TRANSIENT);
    selekt::fuzz::resetExecutionBudget(database);
    selekt::fuzz::stepAndFinalize(statement);
}

}

extern "C" int LLVMFuzzerTestOneInput(const uint8_t* data, std::size_t size) {
    if (size > selekt::fuzz::MAX_INPUT_SIZE) return 0;
    static constexpr uint8_t EMPTY = 0;
    if (data == nullptr) data = &EMPTY;
    selekt::fuzz::Database database;
    if (database.get() == nullptr) return 0;

    selekt::fuzz::executeSql(database.get(), "SELECT vec1_from_json(NULL)");
    exercise(database.get(), "SELECT vec1_from_json(?1)", data, size, true);
    exercise(database.get(), "SELECT vec1_to_json(?1)", data, size, false);
    exercise(database.get(), "SELECT vec1_config(?1)", data, size, true);
    exerciseDistance(database.get(), "vec1_l2_distance", data, size);
    exerciseDistance(database.get(), "vec1_cos_distance", data, size);
    return 0;
}
