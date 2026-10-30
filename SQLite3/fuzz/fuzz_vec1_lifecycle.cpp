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
#include <limits>

namespace {

class LifecycleDatabase final {
public:
    LifecycleDatabase() {
        connections_[0] = selekt::fuzz::openDatabase(DATABASE_URI);
        connections_[1] = selekt::fuzz::openDatabase(DATABASE_URI);
    }

    ~LifecycleDatabase() {
        for (auto* connection : connections_) {
            if (connection != nullptr) sqlite3_close_v2(connection);
        }
    }

    LifecycleDatabase(const LifecycleDatabase&) = delete;
    LifecycleDatabase& operator=(const LifecycleDatabase&) = delete;
    LifecycleDatabase(LifecycleDatabase&&) = delete;
    LifecycleDatabase& operator=(LifecycleDatabase&&) = delete;

    sqlite3* get(int index) const { return connections_[index & 1]; }

    void reopen(int index) {
        index &= 1;
        if (connections_[index] != nullptr) sqlite3_close_v2(connections_[index]);
        connections_[index] = selekt::fuzz::openDatabase(DATABASE_URI);
    }

private:
    static constexpr const char* DATABASE_URI =
        "file:selekt_vec1_fuzz?mode=memory&cache=shared";
    std::array<sqlite3*, 2> connections_{};
};

constexpr std::array<sqlite3_int64, 14> ROWIDS = {
    0,
    -1,
    1,
    127,
    128,
    255,
    256,
    32767,
    32768,
    -32768,
    -32769,
    std::numeric_limits<std::int32_t>::max(),
    std::numeric_limits<std::int32_t>::min(),
    std::numeric_limits<sqlite3_int64>::max()
};

void insertVector(sqlite3* database, sqlite3_int64 rowid, selekt::fuzz::Input& input) {
    if (database == nullptr) return;
    std::array<float, 4> vector{};
    for (auto& element : vector) element = input.finiteFloat();
    sqlite3_stmt* statement = nullptr;
    if (static constexpr auto SQL =
            "INSERT OR REPLACE INTO vectors(rowid,vector,tag) VALUES(?1,?2,?3)";
        sqlite3_prepare_v2(database, SQL, -1, &statement, nullptr) != SQLITE_OK) return;
    sqlite3_bind_int64(statement, 1, rowid);
    sqlite3_bind_blob(statement, 2, vector.data(), sizeof(vector), SQLITE_TRANSIENT);
    sqlite3_bind_int64(statement, 3, static_cast<sqlite3_int64>(input.uint64()));
    selekt::fuzz::resetExecutionBudget(database);
    sqlite3_step(statement);
    sqlite3_finalize(statement);
}

void deleteVector(sqlite3* database, sqlite3_int64 rowid) {
    if (database == nullptr) return;
    sqlite3_stmt* statement = nullptr;
    if (sqlite3_prepare_v2(database, "DELETE FROM vectors WHERE rowid=?1", -1, &statement, nullptr) != SQLITE_OK) return;
    sqlite3_bind_int64(statement, 1, rowid);
    selekt::fuzz::resetExecutionBudget(database);
    sqlite3_step(statement);
    sqlite3_finalize(statement);
}

void rebuild(sqlite3* database, bool flat, bool cosine) {
    if (database == nullptr) return;
    static constexpr std::array MODELS = {
        R"({"index":"none"})",
        R"({"index":"none","distance":"cos"})",
        R"({"index":"flat"})",
        R"({"index":"flat","distance":"cos"})"
    };
    const auto modelIndex = (static_cast<unsigned int>(flat) << 1U) |
        static_cast<unsigned int>(cosine);
    const auto* model = MODELS[modelIndex];
    sqlite3_stmt* statement = nullptr;
    if (sqlite3_prepare_v2(database, "INSERT INTO vectors(cmd,arg) VALUES('rebuild',?1)", -1, &statement, nullptr) != SQLITE_OK) return;
    sqlite3_bind_text(statement, 1, model, -1, SQLITE_STATIC);
    selekt::fuzz::resetExecutionBudget(database);
    sqlite3_step(statement);
    sqlite3_finalize(statement);
}

void exerciseQuery(sqlite3* database, selekt::fuzz::Input& input) {
    if (database == nullptr) return;
    std::array<float, 4> vector{};
    for (auto& element : vector) element = input.finiteFloat();
    sqlite3_stmt* statement = nullptr;
    if (static constexpr auto SQL =
            "SELECT rowid,distance,vector,tag FROM vectors "
            "WHERE cmd=?1 AND arg=16 ORDER BY distance";
        sqlite3_prepare_v2(database, SQL, -1, &statement, nullptr) != SQLITE_OK) return;
    sqlite3_bind_blob(statement, 1, vector.data(), sizeof(vector), SQLITE_TRANSIENT);
    selekt::fuzz::resetExecutionBudget(database);
    selekt::fuzz::stepAndFinalize(statement);
}

}

extern "C" int LLVMFuzzerTestOneInput(const uint8_t* data, std::size_t size) {
    if (size > selekt::fuzz::MAX_INPUT_SIZE) return 0;
    if (data == nullptr) size = 0;
    selekt::fuzz::Input input(data, size);
    LifecycleDatabase databases;
    if (databases.get(0) == nullptr || databases.get(1) == nullptr) return 0;
    selekt::fuzz::executeSql(databases.get(0), "CREATE VIRTUAL TABLE vectors USING vec1(vector,tag)");

    const auto operations = std::min<std::size_t>(size, 512);
    for (std::size_t index = 0; index < operations; ++index) {
        const auto connection = input.byte() % 2;
        auto* database = databases.get(connection);
        switch (input.byte() % 15) {
            case 0: selekt::fuzz::executeSql(database, "BEGIN"); break;
            case 1: selekt::fuzz::executeSql(database, "SAVEPOINT fuzz_savepoint"); break;
            case 2: selekt::fuzz::executeSql(database, "RELEASE fuzz_savepoint"); break;
            case 3: selekt::fuzz::executeSql(database, "ROLLBACK TO fuzz_savepoint"); break;
            case 4: selekt::fuzz::executeSql(database, "COMMIT"); break;
            case 5: selekt::fuzz::executeSql(database, "ROLLBACK"); break;
            case 6: insertVector(database, ROWIDS[input.byte() % ROWIDS.size()], input); break;
            case 7: deleteVector(database, ROWIDS[input.byte() % ROWIDS.size()]); break;
            case 8: rebuild(database, false, input.byte() % 2 != 0); break;
            case 9: rebuild(database, true, input.byte() % 2 != 0); break;
            case 10: exerciseQuery(database, input); break;
            case 11: selekt::fuzz::executeSql(database, "SELECT rowid,vector,tag FROM vectors ORDER BY rowid"); break;
            case 12: selekt::fuzz::executeSql(database, "PRAGMA integrity_check"); break;
            case 13: databases.reopen(connection); break;
            case 14:
                selekt::fuzz::executeSql(
                    database,
                    R"(INSERT INTO vectors(rowid,vector,tag) VALUES)"
                    R"((42,zeroblob(16),1),(43,zeroblob(16),2);)"
                    R"(INSERT INTO vectors(cmd,arg) VALUES('rebuild','{"index":"flat"}');)"
                    R"(INSERT INTO vectors(rowid,vector,tag) VALUES(44,zeroblob(16),3))"
                );
                break;
            default:
                break;
        }
    }
    for (int index = 0; index < 2; ++index) {
        selekt::fuzz::executeSql(databases.get(index), "ROLLBACK; PRAGMA integrity_check; SELECT count(*) FROM vectors");
    }
    return 0;
}
