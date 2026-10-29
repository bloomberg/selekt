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
#include <cstddef>
#include <cstdio>
#include <vector>

namespace {

bool addVector(sqlite3_stmt* statement, const float* vector, int dimensions) {
    sqlite3_reset(statement);
    sqlite3_clear_bindings(statement);
    sqlite3_bind_blob(
        statement,
        1,
        vector,
        dimensions * static_cast<int>(sizeof(float)),
        SQLITE_TRANSIENT
    );
    return sqlite3_step(statement) == SQLITE_DONE;
}

bool requireInstallableModel(sqlite3* database, const std::vector<std::byte>& model) {
    selekt::fuzz::executeSql(database, "CREATE VIRTUAL TABLE trained USING vec1(vector)");
    sqlite3_stmt* statement = nullptr;
    if (sqlite3_prepare_v2(
            database,
            "INSERT INTO trained(cmd,arg) VALUES('rebuild',?1)",
            -1,
            &statement,
            nullptr
        ) != SQLITE_OK) return false;
    sqlite3_bind_blob(
        statement,
        1,
        model.data(),
        static_cast<int>(model.size()),
        SQLITE_TRANSIENT
    );
    const auto result = sqlite3_step(statement);
    sqlite3_finalize(statement);
    return result == SQLITE_DONE;
}

}

extern "C" int LLVMFuzzerTestOneInput(const uint8_t* data, std::size_t size) {
    if (size > selekt::fuzz::MAX_INPUT_SIZE) return 0;
    selekt::fuzz::Input input(data, size);
    const auto dimensions = 2 + input.byte() % 16;
    const auto rows = 16 + input.byte() % 17;
    const auto buckets = std::array{0, 2, 4}[input.byte() % 3];
    const auto opq = input.byte() % 2 != 0;
    const auto residual = buckets > 0 && input.byte() % 2 != 0;
    const auto threads = 1 + input.byte() % 4;

    selekt::fuzz::Database database;
    if (database.get() == nullptr) return 0;
    selekt::fuzz::executeSql(database.get(), "CREATE TABLE samples(vector BLOB NOT NULL); BEGIN");
    sqlite3_stmt* insert = nullptr;
    if (sqlite3_prepare_v2(database.get(), "INSERT INTO samples VALUES(?1)", -1, &insert, nullptr) != SQLITE_OK) return 0;
    std::array<float, 17> vector{};
    for (int row = 0; row < rows; ++row) {
        for (int index = 0; index < dimensions; ++index) {
            vector[index] = input.finiteFloat();
        }
        if (!addVector(insert, vector.data(), dimensions)) break;
    }
    sqlite3_finalize(insert);
    selekt::fuzz::executeSql(database.get(), "COMMIT");

    std::array<char, 160> configuration{};
    std::snprintf(
        configuration.data(),
        configuration.size(),
        R"({"codesize":8,"nbucket":%d,"opq":%s,"nopq_round":1,"residual":%s,"nthread":%d})",
        buckets,
        opq ? "true" : "false",
        residual ? "true" : "false",
        threads
    );
    sqlite3_stmt* training = nullptr;
    if (sqlite3_prepare_v2(
            database.get(),
            "SELECT vec1_train(vector,?1) FROM samples",
            -1,
            &training,
            nullptr
        ) != SQLITE_OK) return 0;
    sqlite3_bind_text(training, 1, configuration.data(), -1, SQLITE_TRANSIENT);
    selekt::fuzz::resetExecutionBudget(database.get());
    if (sqlite3_step(training) == SQLITE_ROW) {
        const auto* bytes = static_cast<const std::byte*>(sqlite3_column_blob(training, 0));
        const auto length = sqlite3_column_bytes(training, 0);
        if (bytes != nullptr && length > 0) {
            const std::vector<std::byte> model(bytes, bytes + length);
            sqlite3_finalize(training);
            requireInstallableModel(database.get(), model);
            return 0;
        }
    }
    sqlite3_finalize(training);
    return 0;
}
