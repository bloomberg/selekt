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

#include <array>
#include <cstring>

namespace {

constexpr std::array<unsigned char, 32> FIXED_KEY = {
    0x37, 0x0f, 0x2c, 0x88, 0xb1, 0x53, 0xda, 0x40,
    0x74, 0x5d, 0xa8, 0x19, 0xf0, 0xc4, 0x12, 0x61,
    0x8d, 0x13, 0x36, 0xe8, 0x5a, 0x71, 0x09, 0xbd,
    0xec, 0x04, 0xca, 0x67, 0x42, 0x9a, 0xf5, 0x11
};

void exercise(const uint8_t* data, std::size_t size, bool encrypted) {
    selekt::fuzz::Database database;
    if (database.get() == nullptr) return;
    auto* copy = static_cast<unsigned char*>(sqlite3_malloc64(size == 0 ? 1 : size));
    if (copy == nullptr) return;
    if (size > 0) std::memcpy(copy, data, size);
    sqlite3_set_authorizer(database.get(), nullptr, nullptr);
    const int result = sqlite3_deserialize(
        database.get(),
        "main",
        copy,
        static_cast<sqlite3_int64>(size),
        static_cast<sqlite3_int64>(size),
        SQLITE_DESERIALIZE_FREEONCLOSE | SQLITE_DESERIALIZE_READONLY
    );
    if (result != SQLITE_OK) return;
    if (encrypted) sqlite3_key(database.get(), FIXED_KEY.data(), static_cast<int>(FIXED_KEY.size()));
    selekt::fuzz::executeSql(
        database.get(),
        "PRAGMA quick_check; PRAGMA integrity_check; PRAGMA cipher_integrity_check; "
        "SELECT type, name, tbl_name, sql FROM sqlite_schema; SELECT * FROM vec1cat"
    );
}

}

extern "C" int LLVMFuzzerTestOneInput(const uint8_t* data, std::size_t size) {
    if (size > selekt::fuzz::MAX_INPUT_SIZE) return 0;
    static constexpr uint8_t EMPTY = 0;
    if (data == nullptr) data = &EMPTY;
    exercise(data, size, false);
    exercise(data, size, true);
    return 0;
}
