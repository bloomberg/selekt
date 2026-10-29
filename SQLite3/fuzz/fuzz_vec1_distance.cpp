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

#include <algorithm>
#include <array>
#include <bit>
#include <cmath>
#include <cstdint>
#include <cstdlib>

namespace {

bool approximatelyEqual(double actual, long double expected) {
    if (!std::isfinite(actual) || !std::isfinite(expected)) return false;
    const auto difference = std::fabs(static_cast<long double>(actual) - expected);
    const auto tolerance = std::max(2.0e-6L, std::fabs(expected) * 2.0e-6L);
    return difference <= tolerance;
}

float distanceFloat(selekt::fuzz::Input& input) {
    auto bits = input.uint32();
    if ((bits & 0x7fffffffU) == 0) return std::bit_cast<float>(bits);

    /* Keep products finite and non-zero with vec1's float accumulators. The
    ** dedicated distance regression tests cover the full finite-float range. */
    const auto exponent = std::clamp((bits >> 23U) & 0xffU, 100U, 154U);
    bits = (bits & 0x807fffffU) | (exponent << 23U);
    return std::bit_cast<float>(bits);
}

void verifyDistances(sqlite3* database, const float* left, const float* right, int dimensions) {
    sqlite3_stmt* statement = nullptr;
    if (static constexpr auto SQL =
            "SELECT vec1_l2_distance(?1, ?2), vec1_cos_distance(?1, ?2)";
        sqlite3_prepare_v2(database, SQL, -1, &statement, nullptr) != SQLITE_OK) return;
    const auto bytes = dimensions * static_cast<int>(sizeof(float));
    sqlite3_bind_blob(statement, 1, left, bytes, SQLITE_TRANSIENT);
    sqlite3_bind_blob(statement, 2, right, bytes, SQLITE_TRANSIENT);
    selekt::fuzz::resetExecutionBudget(database);
    if (sqlite3_step(statement) == SQLITE_ROW) {
        long double l2 = 0.0L;
        long double dot = 0.0L;
        long double leftNorm = 0.0L;
        long double rightNorm = 0.0L;
        for (int index = 0; index < dimensions; ++index) {
            const auto lhs = static_cast<long double>(left[index]);
            const auto rhs = static_cast<long double>(right[index]);
            const auto delta = lhs - rhs;
            l2 += delta * delta;
            dot += lhs * rhs;
            leftNorm += lhs * lhs;
            rightNorm += rhs * rhs;
        }
        if (!approximatelyEqual(sqlite3_column_double(statement, 0), l2)) std::abort();
        if (leftNorm > 0.0L && rightNorm > 0.0L) {
            const auto cosine = 1.0L - dot / std::sqrt(leftNorm * rightNorm);
            const auto actual = sqlite3_column_double(statement, 1);
            if (!approximatelyEqual(actual, cosine)) std::abort();
        }
    }
    sqlite3_finalize(statement);
}

}

extern "C" int LLVMFuzzerTestOneInput(const uint8_t* data, std::size_t size) {
    if (size > selekt::fuzz::MAX_INPUT_SIZE) return 0;
    selekt::fuzz::Input input(data, size);
    const auto dimensions = 1 + input.byte() % 17;
    std::array<float, 17> left{};
    std::array<float, 17> right{};
    for (int index = 0; index < dimensions; ++index) left[index] = distanceFloat(input);
    for (int index = 0; index < dimensions; ++index) right[index] = distanceFloat(input);

    if (selekt::fuzz::Database database; database.get() != nullptr) {
        verifyDistances(database.get(), left.data(), right.data(), dimensions);
    }
    return 0;
}
