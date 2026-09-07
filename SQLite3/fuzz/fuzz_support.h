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

#pragma once

#include <cstddef>
#include <cstdint>

#include "sqlite3.h"

namespace selekt::fuzz {

constexpr std::size_t MAX_INPUT_SIZE = 1U << 20;

sqlite3* openDatabase();
void resetExecutionBudget(sqlite3* database);
void executeSql(sqlite3* database, const char* sql, std::size_t size);

template<std::size_t N>
void executeSql(sqlite3* database, const char (&sql)[N]) {
    static_assert(N > 0);
    executeSql(database, sql, N - 1);
}

void stepAndFinalize(sqlite3_stmt* statement);

class Database final {
public:
    Database();
    ~Database();

    Database(const Database&) = delete;
    Database& operator=(const Database&) = delete;

    sqlite3* get() const { return database_; }

private:
    sqlite3* database_ = nullptr;
};

}
