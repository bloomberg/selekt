# Copyright 2026 Bloomberg Finance L.P.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if(NOT SELEKT_ENABLE_VEC1)
  message(FATAL_ERROR "Native fuzz targets require SELEKT_ENABLE_VEC1=ON")
endif()

if(NOT SELEKT_FUZZ_ENGINE STREQUAL "libfuzzer" AND NOT SELEKT_FUZZ_ENGINE STREQUAL "afl")
  message(FATAL_ERROR "SELEKT_FUZZ_ENGINE must be 'libfuzzer' or 'afl'")
endif()

add_library(selekt_fuzz_support STATIC fuzz_support.cpp)
target_link_libraries(selekt_fuzz_support PUBLIC selekt)
target_include_directories(selekt_fuzz_support PUBLIC
  ${CMAKE_CURRENT_SOURCE_DIR}
  ${CMAKE_CURRENT_SOURCE_DIR}/../sqlite3/generated/include/sqlite3
)
target_compile_options(selekt_fuzz_support PRIVATE
  -fsanitize=address,undefined
  -fno-omit-frame-pointer
)
target_link_options(selekt_fuzz_support PRIVATE -fsanitize=address,undefined)

function(selekt_add_fuzzer target source)
  add_executable(${target} ${source})
  target_link_libraries(${target} PRIVATE selekt_fuzz_support)
  target_compile_options(${target} PRIVATE
    -fsanitize=address,undefined
    -fno-omit-frame-pointer
  )
  if(SELEKT_FUZZ_ENGINE STREQUAL "libfuzzer")
    target_link_options(${target} PRIVATE -fsanitize=fuzzer,address,undefined)
  else()
    target_sources(${target} PRIVATE standalone_main.cpp)
    target_link_options(${target} PRIVATE -fsanitize=address,undefined)
  endif()
endfunction()

selekt_add_fuzzer(selekt_fuzz_sql fuzz_sql.cpp)
selekt_add_fuzzer(selekt_fuzz_vec1_scalar fuzz_vec1_scalar.cpp)
selekt_add_fuzzer(selekt_fuzz_vec1_shadow fuzz_vec1_shadow.cpp)
selekt_add_fuzzer(selekt_fuzz_database fuzz_database.cpp)

add_custom_target(selekt_fuzzers DEPENDS
  selekt_fuzz_sql
  selekt_fuzz_vec1_scalar
  selekt_fuzz_vec1_shadow
  selekt_fuzz_database
)
