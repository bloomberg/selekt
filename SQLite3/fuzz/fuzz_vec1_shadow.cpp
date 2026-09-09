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

#include <algorithm>
#include <array>

namespace {

struct Slice {
    const void* data;
    std::size_t size;
};

std::array<Slice, 3> splitInput(const std::byte* data, std::size_t size) {
    if (size < 5) return {{{data, size}, {data + size, 0}, {data + size, 0}}};
    const std::size_t payload = size - 5;
    // Model blobs may exceed 64 KiB, so reserve three bytes for their size.
    const std::size_t modelSize = std::min(
        (std::to_integer<std::size_t>(data[0]) << 16) |
            (std::to_integer<std::size_t>(data[1]) << 8) |
            std::to_integer<std::size_t>(data[2]),
        payload
    );
    const std::size_t afterModel = payload - modelSize;
    const std::size_t indexSize = std::min(
        (std::to_integer<std::size_t>(data[3]) << 8) | std::to_integer<std::size_t>(data[4]),
        afterModel
    );
    return {{{data + 5, modelSize}, {data + 5 + modelSize, indexSize},
        {data + 5 + modelSize + indexSize, afterModel - indexSize}}};
}

void bindBlobAndStep(sqlite3* database, const char* sql, Slice blob) {
    sqlite3_stmt* statement = nullptr;
    if (sqlite3_prepare_v2(database, sql, -1, &statement, nullptr) != SQLITE_OK) return;
    sqlite3_bind_blob(statement, 1, blob.data, static_cast<int>(blob.size), SQLITE_TRANSIENT);
    selekt::fuzz::resetExecutionBudget(database);
    selekt::fuzz::stepAndFinalize(statement);
}

}

extern "C" int LLVMFuzzerTestOneInput(const uint8_t* data, std::size_t size) {
    if (size > selekt::fuzz::MAX_INPUT_SIZE) return 0;
    static constexpr std::array<std::byte, 4> EMPTY{};
    const auto* bytes = reinterpret_cast<const std::byte*>(data);
    if (bytes == nullptr) {
        bytes = EMPTY.data();
        size = 0;
    }
    const auto slices = splitInput(bytes, size);
    selekt::fuzz::Database database;
    if (database.get() == nullptr) return 0;

    selekt::fuzz::executeSql(database.get(), "CREATE VIRTUAL TABLE model_fuzz USING vec1(vector, tag)");
    bindBlobAndStep(
        database.get(),
        "INSERT INTO model_fuzz(cmd, arg) VALUES('rebuild', ?1)",
        slices[0]
    );
    selekt::fuzz::executeSql(
        database.get(),
        "INSERT INTO model_fuzz(rowid, vector, tag) "
        "VALUES(1, vec1_from_json('[1,2,3,4]'), 1.0)"
    );
    bindBlobAndStep(
        database.get(),
        "UPDATE model_fuzz_base SET vector=?1 WHERE id=1",
        slices[2]
    );
    selekt::fuzz::executeSql(
        database.get(),
        "SELECT distance FROM model_fuzz WHERE rowid=1; "
        "PRAGMA integrity_check; "
        "DELETE FROM model_fuzz WHERE rowid=1"
    );

    selekt::fuzz::executeSql(database.get(), "CREATE VIRTUAL TABLE shadow_fuzz USING vec1(vector, tag)");
    static constexpr std::array<uint8_t, 24> VALID_MODEL = {
        0, 0, 0, 4, 0, 0, 0, 1, 0, 0, 0, 4,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1
    };
    bindBlobAndStep(
        database.get(),
        "INSERT INTO shadow_fuzz(cmd, arg) VALUES('rebuild', ?1)",
        {VALID_MODEL.data(), VALID_MODEL.size()}
    );
    bindBlobAndStep(
        database.get(),
        "INSERT INTO shadow_fuzz_idx VALUES(1, 0, 1, 1, ?1)",
        slices[1]
    );
    bindBlobAndStep(
        database.get(),
        "INSERT INTO shadow_fuzz_meta VALUES(256, ?1)",
        slices[2]
    );
    selekt::fuzz::executeSql(
        database.get(),
        "SELECT rowid FROM shadow_fuzz "
        "WHERE cmd=vec1_from_json('[0,0,0,0]') AND arg=1 AND tag=1.0; "
        "PRAGMA integrity_check; SELECT * FROM vec1cat"
    );
    return 0;
}
