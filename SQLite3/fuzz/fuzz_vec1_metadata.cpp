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

#include "fuzz_input.h"
#include "fuzz_support.h"

#include <array>
#include <bit>
#include <cmath>
#include <cstdlib>
#include <string>
#include <vector>

namespace {

struct Value {
    enum class Type : std::uint8_t { Null, Integer, Real, Text, Blob };
    Type type = Type::Null;
    sqlite3_int64 integer = 0;
    double real = 0.0;
    std::array<std::uint8_t, 8> bytes{};
    int size = 0;
};

Value nextValue(selekt::fuzz::Input& input) {
    using enum Value::Type;
    Value value;
    value.type = static_cast<Value::Type>(input.byte() % 5);
    if (value.type == Integer) {
        value.integer = static_cast<sqlite3_int64>(input.uint64());
    } else if (value.type == Real) {
        value.real = std::bit_cast<double>(input.uint64());
        if (!std::isfinite(value.real)) value.real = std::copysign(0x1.fffffffffffffp+1023, value.real);
    } else if (value.type == Text || value.type == Blob) {
        value.size = input.byte() % static_cast<int>(value.bytes.size() + 1);
        for (int index = 0; index < value.size; ++index) value.bytes[index] = input.byte();
    }
    return value;
}

void bindValue(sqlite3_stmt* statement, int index, const Value& value) {
    using enum Value::Type;
    switch (value.type) {
        case Null:
            sqlite3_bind_null(statement, index);
            break;
        case Integer:
            sqlite3_bind_int64(statement, index, value.integer);
            break;
        case Real:
            sqlite3_bind_double(statement, index, value.real);
            break;
        case Text:
            sqlite3_bind_text(
                statement,
                index,
                reinterpret_cast<const char*>(value.bytes.data()),
                value.size,
                SQLITE_TRANSIENT
            );
            break;
        case Blob:
            sqlite3_bind_blob(statement, index, value.bytes.data(), value.size, SQLITE_TRANSIENT);
            break;
    }
}

bool insertValue(sqlite3* database, const char* sql, sqlite3_int64 rowid, const Value& value) {
    sqlite3_stmt* statement = nullptr;
    if (sqlite3_prepare_v2(database, sql, -1, &statement, nullptr) != SQLITE_OK) return false;
    sqlite3_bind_int64(statement, 1, rowid);
    bindValue(statement, 2, value);
    const auto result = sqlite3_step(statement);
    sqlite3_finalize(statement);
    return result == SQLITE_DONE;
}

struct Rows {
    bool valid = false;
    std::vector<sqlite3_int64> values;
};

Rows queryRows(sqlite3* database, const std::string& sql, const std::vector<Value>& filters) {
    Rows rows;
    sqlite3_stmt* statement = nullptr;
    if (sqlite3_prepare_v2(database, sql.c_str(), -1, &statement, nullptr) != SQLITE_OK) return rows;
    for (std::size_t index = 0; index < filters.size(); ++index) {
        bindValue(statement, static_cast<int>(index + 1), filters[index]);
    }
    selekt::fuzz::resetExecutionBudget(database);
    int result = SQLITE_OK;
    while ((result = sqlite3_step(statement)) == SQLITE_ROW) {
        rows.values.push_back(sqlite3_column_int64(statement, 0));
        if (rows.values.size() > 32) break;
    }
    rows.valid = result == SQLITE_DONE;
    sqlite3_finalize(statement);
    return rows;
}

void comparePredicate(
    sqlite3* database,
    const std::string& predicate,
    const std::vector<Value>& filters
) {
    const auto reference = queryRows(
        database,
        "SELECT rowid FROM reference_values WHERE tag " + predicate + " ORDER BY rowid",
        filters
    );
    const auto noIndex = queryRows(
        database,
        "SELECT rowid FROM no_index WHERE cmd=zeroblob(16) AND arg=64 AND tag " + predicate +
            " ORDER BY rowid",
        filters
    );
    const auto indexed = queryRows(
        database,
        "SELECT rowid FROM flat_index WHERE cmd=zeroblob(16) AND arg=64 AND tag " + predicate +
            " ORDER BY rowid",
        filters
    );
    if (reference.valid && noIndex.valid && indexed.valid &&
        (reference.values != noIndex.values || reference.values != indexed.values)) {
        std::abort();
    }
}

}

extern "C" int LLVMFuzzerTestOneInput(const uint8_t* data, std::size_t size) {
    if (size > selekt::fuzz::MAX_INPUT_SIZE) return 0;
    selekt::fuzz::Input input(data, size);
    selekt::fuzz::Database database;
    if (database.get() == nullptr) return 0;

    selekt::fuzz::executeSql(
        database.get(),
        R"(CREATE TABLE reference_values(tag);)"
        R"(CREATE VIRTUAL TABLE no_index USING vec1(vector, tag);)"
        R"(CREATE VIRTUAL TABLE flat_index USING vec1(vector, tag);)"
        R"(INSERT INTO flat_index(cmd,arg) VALUES('rebuild','{"index":"flat"}'))"
    );
    const auto rowCount = 1 + input.byte() % 8;
    for (int index = 0; index < rowCount; ++index) {
        const auto value = nextValue(input);
        const auto rowid = static_cast<sqlite3_int64>(index + 1);
        if (!insertValue(
                database.get(),
                "INSERT INTO reference_values(rowid,tag) VALUES(?1,?2)",
                rowid,
                value
            )) return 0;
        if (!insertValue(
                database.get(),
                "INSERT INTO no_index(rowid,vector,tag) VALUES(?1,zeroblob(16),?2)",
                rowid,
                value
            )) return 0;
        if (!insertValue(
                database.get(),
                "INSERT INTO flat_index(rowid,vector,tag) VALUES(?1,zeroblob(16),?2)",
                rowid,
                value
            )) return 0;
    }

    static constexpr std::array<const char*, 8> OPERATORS = {"= ?1", "!= ?1", "< ?1", "<= ?1", "> ?1", ">= ?1", "IS ?1", "IS NOT ?1"};
    const auto operatorIndex = input.byte() % OPERATORS.size();
    comparePredicate(database.get(), OPERATORS[operatorIndex], {nextValue(input)});

    const auto groupSize = input.byte() % 5;
    std::vector<Value> group;
    std::string placeholders;
    for (int index = 0; index < groupSize; ++index) {
        if (index != 0) placeholders += ',';
        placeholders += '?';
        placeholders += std::to_string(index + 1);
        group.push_back(nextValue(input));
    }
    std::string predicate{"IN ("};
    predicate.append(placeholders);
    predicate += ')';
    comparePredicate(database.get(), predicate, group);
    return 0;
}
