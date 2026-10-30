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

#include <algorithm>
#include <bit>
#include <cstddef>
#include <cstdint>
#include <span>

namespace selekt::fuzz {

class Input final {
public:
    Input(const std::uint8_t* data, std::size_t size)
        : data_(data == nullptr ? std::span<const std::uint8_t>{} :
            std::span<const std::uint8_t>{data, size}) {}

    std::uint8_t byte() {
        if (data_.empty()) return 0;
        const auto value = data_[offset_ % data_.size()];
        ++offset_;
        return value;
    }

    std::uint32_t uint32() {
        std::uint32_t value = 0;
        for (unsigned int shift = 0; shift < 32; shift += 8) {
            value |= static_cast<std::uint32_t>(byte()) << shift;
        }
        return value;
    }

    std::uint64_t uint64() {
        const auto low = static_cast<std::uint64_t>(uint32());
        return low | (static_cast<std::uint64_t>(uint32()) << 32);
    }

    std::span<const std::uint8_t> bytes(std::size_t maximum) {
        if (data_.empty()) return {};
        const auto start = std::min(offset_, data_.size());
        const auto available = std::min(maximum, data_.size() - start);
        const auto result = data_.subspan(start, available);
        offset_ += available;
        return result;
    }

    float finiteFloat() {
        auto bits = uint32();
        if ((bits & 0x7f800000U) == 0x7f800000U) {
            bits = (bits & 0x807fffffU) | 0x7f000000U;
        }
        return std::bit_cast<float>(bits);
    }

private:
    std::span<const std::uint8_t> data_;
    std::size_t offset_ = 0;
};

}
