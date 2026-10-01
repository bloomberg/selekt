# Copyright 2020 Bloomberg Finance L.P.
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

execute_process(
  COMMAND git log -1 --format=%H
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  OUTPUT_VARIABLE SELEKT_GIT_COMMIT
  OUTPUT_STRIP_TRAILING_WHITESPACE
)

configure_file(
  "${CMAKE_CURRENT_SOURCE_DIR}/SelektConfig.h.in"
  "${CMAKE_CURRENT_SOURCE_DIR}/generated/include/SelektConfig.h"
)

option(SELEKT_ENABLE_VEC1 "Build with embedded vec1 extension" ON)
option(
  SELEKT_VEC1_ENABLE_X86_AVX2
  "Build vec1 scalar and AVX2/FMA implementations with runtime dispatch on x86_64"
  ON
)
option(SELEKT_ENABLE_THINLTO "Enable ThinLTO for the Selekt native library" OFF)
option(SELEKT_BUILD_NATIVE_BENCHMARKS "Build the native SQLite/SQLCipher/vec1 benchmark" OFF)
set(SELEKT_SQLITE_OPTIMIZATION "DEFAULT" CACHE STRING "Optimization level for the SQLite amalgamation")
set_property(CACHE SELEKT_SQLITE_OPTIMIZATION PROPERTY STRINGS DEFAULT O2 O3)
set(SELEKT_SQLITE_PGO "AUTO" CACHE STRING "SQLite PGO mode: AUTO, OFF, GENERATE, or USE")
set_property(CACHE SELEKT_SQLITE_PGO PROPERTY STRINGS AUTO OFF GENERATE USE)
set(
  SELEKT_SQLITE_PGO_PROFILE
  ""
  CACHE FILEPATH
  "Merged .profdata file used by SQLite PGO"
)

string(TOUPPER "${SELEKT_SQLITE_OPTIMIZATION}" SELEKT_SQLITE_OPTIMIZATION)
if(NOT SELEKT_SQLITE_OPTIMIZATION MATCHES "^(DEFAULT|O2|O3)$")
  message(FATAL_ERROR "SELEKT_SQLITE_OPTIMIZATION must be DEFAULT, O2, or O3")
endif()
string(TOUPPER "${SELEKT_SQLITE_PGO}" SELEKT_SQLITE_PGO)
if(NOT SELEKT_SQLITE_PGO MATCHES "^(AUTO|OFF|GENERATE|USE)$")
  message(FATAL_ERROR "SELEKT_SQLITE_PGO must be AUTO, OFF, GENERATE, or USE")
endif()
set(SELEKT_SQLITE_PGO_ACTIVE "${SELEKT_SQLITE_PGO}")
if(SELEKT_SQLITE_PGO STREQUAL "AUTO")
  set(SELEKT_SQLITE_PGO_ACTIVE "OFF")
  if(ANDROID
     AND CMAKE_SYSTEM_PROCESSOR MATCHES "^(aarch64|arm64|ARM64)$"
     AND NOT CMAKE_BUILD_TYPE STREQUAL "Debug"
     AND EXISTS "${SELEKT_SQLITE_PGO_PROFILE}")
    set(SELEKT_SQLITE_PGO_ACTIVE "USE")
  endif()
endif()
if(SELEKT_SQLITE_PGO_ACTIVE STREQUAL "USE" AND NOT EXISTS "${SELEKT_SQLITE_PGO_PROFILE}")
  message(FATAL_ERROR
    "SELEKT_SQLITE_PGO=USE requires an existing SELEKT_SQLITE_PGO_PROFILE (.profdata)"
  )
endif()

set(SELEKT_VEC1_SOURCE extensions/vec1/vec1.c)
set(SELEKT_SQLITE_SOURCE generated/cpp/sqlite3.c)
set(SELEKT_SOURCES ${SELEKT_SQLITE_SOURCE})
if(SELEKT_ENABLE_VEC1)
  if(SELEKT_VEC1_ENABLE_X86_AVX2
     AND NOT MSVC
     AND CMAKE_SYSTEM_PROCESSOR MATCHES "^(x86_64|amd64|AMD64)$")
    # vec1 supports multi-architecture builds by compiling the extension once
    # for the baseline ISA and once for AVX2/FMA. Only the scalar object exports
    # the SQLite entry point; it selects the appropriate implementation after
    # checking the CPU and operating-system AVX state at runtime.
    add_library(selekt_vec1_scalar OBJECT ${SELEKT_VEC1_SOURCE})
    add_library(selekt_vec1_avx2 OBJECT ${SELEKT_VEC1_SOURCE})
    foreach(SELEKT_VEC1_TARGET selekt_vec1_scalar selekt_vec1_avx2)
      set_target_properties(${SELEKT_VEC1_TARGET} PROPERTIES
        C_STANDARD 99
        C_STANDARD_REQUIRED ON
        POSITION_INDEPENDENT_CODE ON
      )
      target_compile_definitions(${SELEKT_VEC1_TARGET} PRIVATE VEC1_STATIC)
      target_include_directories(${SELEKT_VEC1_TARGET} SYSTEM PRIVATE
        include
        generated/include
        generated/include/sqlite3
      )
      target_compile_options(${SELEKT_VEC1_TARGET} PRIVATE -include stdint.h)
    endforeach()
    target_compile_definitions(selekt_vec1_scalar PRIVATE VEC1SIMD=SCALAR)
    target_compile_definitions(selekt_vec1_avx2 PRIVATE VEC1SIMD=AVX2)
    target_compile_options(selekt_vec1_avx2 PRIVATE -mavx2 -mfma)
    list(APPEND SELEKT_SOURCES
      $<TARGET_OBJECTS:selekt_vec1_scalar>
      $<TARGET_OBJECTS:selekt_vec1_avx2>
    )
  else()
    list(APPEND SELEKT_SOURCES ${SELEKT_VEC1_SOURCE})
  endif()
endif()

add_library(selekt SHARED
  ${SELEKT_SOURCES}
)

if(NOT SELEKT_SQLITE_OPTIMIZATION STREQUAL "DEFAULT"
   AND CMAKE_C_COMPILER_ID MATCHES "Clang|GNU")
  set_property(SOURCE ${SELEKT_SQLITE_SOURCE} APPEND PROPERTY
    COMPILE_OPTIONS "-${SELEKT_SQLITE_OPTIMIZATION}"
  )
endif()

if((NOT SELEKT_SQLITE_PGO_ACTIVE STREQUAL "OFF" OR SELEKT_ENABLE_THINLTO)
   AND NOT CMAKE_C_COMPILER_ID MATCHES "Clang")
  message(FATAL_ERROR "SQLite PGO and ThinLTO require Clang")
endif()
if(SELEKT_SQLITE_PGO_ACTIVE STREQUAL "GENERATE")
  set_property(SOURCE ${SELEKT_SQLITE_SOURCE} APPEND PROPERTY
    COMPILE_OPTIONS "-fprofile-instr-generate"
  )
  target_link_options(selekt PRIVATE -fprofile-instr-generate)
elseif(SELEKT_SQLITE_PGO_ACTIVE STREQUAL "USE")
  set_property(SOURCE ${SELEKT_SQLITE_SOURCE} APPEND PROPERTY
    COMPILE_OPTIONS "-fprofile-instr-use=${SELEKT_SQLITE_PGO_PROFILE}"
  )
  set_property(SOURCE ${SELEKT_SQLITE_SOURCE} APPEND PROPERTY
    OBJECT_DEPENDS "${SELEKT_SQLITE_PGO_PROFILE}"
  )
endif()
if(SELEKT_ENABLE_THINLTO)
  target_compile_options(selekt PRIVATE -flto=thin)
  target_link_options(selekt PRIVATE -flto=thin)
endif()

set_property(TARGET selekt PROPERTY C_STANDARD 99)
set_property(TARGET selekt PROPERTY C_STANDARD_REQUIRED ON)

set_target_properties(selekt PROPERTIES PREFIX "lib")

if(MSVC)
  target_compile_options(selekt PRIVATE /FI stdint.h)
else()
  target_compile_options(selekt PRIVATE -include stdint.h)
endif()

target_compile_definitions(selekt PUBLIC
  SQLCIPHER_CRYPTO_OPENSSL
  SQLITE_ALLOW_URI_AUTHORITY
  SQLITE_DEFAULT_AUTOVACUUM=2
  SQLITE_DEFAULT_CACHE_SIZE=200
  SQLITE_DEFAULT_MEMSTATUS=0
  SQLITE_DEFAULT_SYNCHRONOUS=2
  SQLITE_DEFAULT_WAL_SYNCHRONOUS=1
  SQLITE_DQS=0
  SQLITE_ENABLE_FTS3_PARENTHESIS
  SQLITE_ENABLE_FTS4
  SQLITE_ENABLE_FTS5
  SQLITE_ENABLE_JSON1
  SQLITE_ENABLE_MATH_FUNCTIONS
  SQLITE_ENABLE_MEMORY_MANAGEMENT=1
  SQLITE_ENABLE_NORMALIZE
  SQLITE_ENABLE_PERCENTILE
  SQLITE_ENABLE_RTREE
  SQLITE_ENABLE_STAT4
  SQLITE_ENABLE_UNLOCK_NOTIFY
  SQLITE_ENABLE_UPDATE_DELETE_LIMIT
  SQLITE_EXTRA_INIT=sqlcipher_extra_init
  SQLITE_EXTRA_SHUTDOWN=sqlcipher_extra_shutdown
  SQLITE_HAS_CODEC=1
  SQLITE_LIKE_DOESNT_MATCH_BLOBS
  SQLITE_OMIT_AUTOINIT
  SQLITE_OMIT_DECLTYPE
  SQLITE_OMIT_DEPRECATED
  SQLITE_OMIT_LOAD_EXTENSION
  SQLITE_OMIT_SHARED_CACHE
  SQLITE_TEMP_STORE=3
  SQLITE_THREADSAFE=2
  SQLITE_TRUSTED_SCHEMA=0
  SQLITE_USE_URI=1
)

if(SELEKT_ENABLE_VEC1)
  target_compile_definitions(selekt PUBLIC VEC1_STATIC)
endif()

if(SELEKT_ENABLE_VEC1
   AND CMAKE_C_COMPILER_ID STREQUAL "GNU"
   AND CMAKE_SYSTEM_PROCESSOR MATCHES "^(aarch64|arm64)$")
  set_property(SOURCE ${SELEKT_VEC1_SOURCE} APPEND PROPERTY COMPILE_OPTIONS -flax-vector-conversions)
endif()

if(NOT WIN32)
  target_compile_definitions(selekt PUBLIC
    _GNU_SOURCE
    HAVE_USLEEP=1
    SQLITE_DEFAULT_FILE_PERMISSIONS=0600
    SQLITE_ENABLE_BATCH_ATOMIC_WRITE
    SQLITE_USE_ALLOCA
  )
endif()

target_include_directories(selekt SYSTEM PUBLIC include generated/include generated/include/sqlite3)

find_library(log-lib log)
if(log-lib)
  target_compile_definitions(selekt PUBLIC SELEKT_LOG)
  target_link_libraries(selekt PRIVATE ${log-lib})
endif()

set(LIBCRYPTO_DIR "${OPENSSL_PROJECT_DIR}/build/libs/${SLKT_TARGET_ABI}")
set(LIBCRYPTO "${LIBCRYPTO_DIR}/libcrypto.a")
if(NOT EXISTS "${LIBCRYPTO}" AND EXISTS "${LIBCRYPTO_DIR}/libcrypto.lib")
  set(LIBCRYPTO "${LIBCRYPTO_DIR}/libcrypto.lib")
endif()
if(NOT EXISTS "${LIBCRYPTO}")
  message(FATAL_ERROR "OpenSSL libcrypto not found under: ${LIBCRYPTO_DIR}")
endif()
set(OPENSSL_BUILD_MANIFEST
  "${OPENSSL_PROJECT_DIR}/build/manifests/${SLKT_TARGET_ABI}/openssl-build.properties"
)
if(NOT EXISTS "${OPENSSL_BUILD_MANIFEST}")
  message(FATAL_ERROR "OpenSSL build manifest not found: ${OPENSSL_BUILD_MANIFEST}")
endif()
set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS
  "${OPENSSL_BUILD_MANIFEST}"
  "${LIBCRYPTO}"
)
file(STRINGS "${OPENSSL_BUILD_MANIFEST}" OPENSSL_MANIFEST_VERSION_PROPERTIES
  REGEX "^openssl\\.version="
)
list(LENGTH OPENSSL_MANIFEST_VERSION_PROPERTIES OPENSSL_MANIFEST_VERSION_PROPERTY_COUNT)
if(NOT OPENSSL_MANIFEST_VERSION_PROPERTY_COUNT EQUAL 1)
  message(FATAL_ERROR "Expected exactly one openssl.version in: ${OPENSSL_BUILD_MANIFEST}")
endif()
list(GET OPENSSL_MANIFEST_VERSION_PROPERTIES 0 OPENSSL_MANIFEST_VERSION_PROPERTY)
string(REGEX REPLACE
  "^openssl\\.version=(.+)$" "\\1" OPENSSL_MANIFEST_VERSION "${OPENSSL_MANIFEST_VERSION_PROPERTY}"
)
if(NOT OPENSSL_MANIFEST_VERSION STREQUAL OPENSSL_VERSION)
  message(FATAL_ERROR
    "OpenSSL library version mismatch: expected ${OPENSSL_VERSION}, found ${OPENSSL_MANIFEST_VERSION}"
  )
endif()
file(STRINGS "${OPENSSL_BUILD_MANIFEST}" OPENSSL_MANIFEST_HEADER_HASH_PROPERTIES
  REGEX "^openssl\\.opensslv\\.sha256=[0-9A-Fa-f]+$"
)
list(LENGTH OPENSSL_MANIFEST_HEADER_HASH_PROPERTIES OPENSSL_MANIFEST_HEADER_HASH_PROPERTY_COUNT)
if(NOT OPENSSL_MANIFEST_HEADER_HASH_PROPERTY_COUNT EQUAL 1)
  message(FATAL_ERROR "Expected exactly one OpenSSL header SHA-256 in: ${OPENSSL_BUILD_MANIFEST}")
endif()
list(GET OPENSSL_MANIFEST_HEADER_HASH_PROPERTIES 0 OPENSSL_MANIFEST_HEADER_HASH_PROPERTY)
string(REGEX REPLACE
  "^openssl\\.opensslv\\.sha256=(.+)$" "\\1" EXPECTED_OPENSSL_HEADER_SHA256
  "${OPENSSL_MANIFEST_HEADER_HASH_PROPERTY}"
)
string(LENGTH "${EXPECTED_OPENSSL_HEADER_SHA256}" EXPECTED_OPENSSL_HEADER_SHA256_LENGTH)
if(NOT EXPECTED_OPENSSL_HEADER_SHA256_LENGTH EQUAL 64)
  message(FATAL_ERROR "Invalid OpenSSL header SHA-256 in: ${OPENSSL_BUILD_MANIFEST}")
endif()
file(SHA256 "${OPENSSL_VERSION_HEADER}" ACTUAL_OPENSSL_HEADER_SHA256)
string(TOLOWER "${EXPECTED_OPENSSL_HEADER_SHA256}" EXPECTED_OPENSSL_HEADER_SHA256)
string(TOLOWER "${ACTUAL_OPENSSL_HEADER_SHA256}" ACTUAL_OPENSSL_HEADER_SHA256)
if(NOT ACTUAL_OPENSSL_HEADER_SHA256 STREQUAL EXPECTED_OPENSSL_HEADER_SHA256)
  message(FATAL_ERROR
    "OpenSSL version header SHA-256 mismatch for ${OPENSSL_VERSION_HEADER}: "
    "expected ${EXPECTED_OPENSSL_HEADER_SHA256}, found ${ACTUAL_OPENSSL_HEADER_SHA256}"
  )
endif()
file(STRINGS "${OPENSSL_BUILD_MANIFEST}" OPENSSL_MANIFEST_HASH_PROPERTIES
  REGEX "^openssl\\.libcrypto\\.sha256=[0-9A-Fa-f]+$"
)
list(LENGTH OPENSSL_MANIFEST_HASH_PROPERTIES OPENSSL_MANIFEST_HASH_PROPERTY_COUNT)
if(NOT OPENSSL_MANIFEST_HASH_PROPERTY_COUNT EQUAL 1)
  message(FATAL_ERROR "Expected exactly one libcrypto SHA-256 in: ${OPENSSL_BUILD_MANIFEST}")
endif()
list(GET OPENSSL_MANIFEST_HASH_PROPERTIES 0 OPENSSL_MANIFEST_HASH_PROPERTY)
string(REGEX REPLACE
  "^openssl\\.libcrypto\\.sha256=(.+)$" "\\1" EXPECTED_LIBCRYPTO_SHA256 "${OPENSSL_MANIFEST_HASH_PROPERTY}"
)
string(LENGTH "${EXPECTED_LIBCRYPTO_SHA256}" EXPECTED_LIBCRYPTO_SHA256_LENGTH)
if(NOT EXPECTED_LIBCRYPTO_SHA256_LENGTH EQUAL 64)
  message(FATAL_ERROR "Invalid libcrypto SHA-256 in: ${OPENSSL_BUILD_MANIFEST}")
endif()
file(SHA256 "${LIBCRYPTO}" ACTUAL_LIBCRYPTO_SHA256)
string(TOLOWER "${EXPECTED_LIBCRYPTO_SHA256}" EXPECTED_LIBCRYPTO_SHA256)
string(TOLOWER "${ACTUAL_LIBCRYPTO_SHA256}" ACTUAL_LIBCRYPTO_SHA256)
if(NOT ACTUAL_LIBCRYPTO_SHA256 STREQUAL EXPECTED_LIBCRYPTO_SHA256)
  message(FATAL_ERROR
    "OpenSSL libcrypto SHA-256 mismatch for ${LIBCRYPTO}: expected ${EXPECTED_LIBCRYPTO_SHA256}, "
    "found ${ACTUAL_LIBCRYPTO_SHA256}"
  )
endif()
message(STATUS "Linking to verified OpenSSL ${OPENSSL_VERSION} libcrypto: ${LIBCRYPTO}")
target_link_libraries(selekt PRIVATE "${LIBCRYPTO}")

if(CMAKE_SYSTEM_NAME STREQUAL "Linux" AND NOT ANDROID_ABI AND NOT ANDROID)
  target_link_libraries(selekt PRIVATE pthread ${CMAKE_DL_LIBS})
endif()

if(WIN32)
  target_link_libraries(selekt PRIVATE ws2_32 crypt32 advapi32 user32 bcrypt gdi32)
  if(MINGW)
    target_link_options(selekt PRIVATE
      -static
      -static-libgcc
      -static-libstdc++
    )
  endif()
endif()

if(CMAKE_SYSTEM_NAME STREQUAL "Linux" AND NOT ANDROID_ABI AND NOT ANDROID AND NOT SELEKT_BUILD_FUZZERS)
  find_library(MIMALLOC_STATIC_LIB
    NAMES libmimalloc.a mimalloc-static mimalloc
    PATHS
      /usr/local/lib
      /usr/lib
      $ENV{CMAKE_PREFIX_PATH}/lib
    NO_DEFAULT_PATH)
  if(MIMALLOC_STATIC_LIB)
    message(STATUS "Found static mimalloc: ${MIMALLOC_STATIC_LIB}")
    target_link_libraries(selekt PRIVATE ${MIMALLOC_STATIC_LIB})
    target_compile_definitions(selekt PRIVATE MI_MALLOC_OVERRIDE)
  else()
    find_package(mimalloc QUIET)
    if(mimalloc_FOUND)
      message(STATUS "Found mimalloc via find_package")
      target_link_libraries(selekt PRIVATE mimalloc)
      target_compile_definitions(selekt PRIVATE MI_MALLOC_OVERRIDE)
    else()
      message(FATAL_ERROR "mimalloc not found (required on Linux)")
    endif()
  endif()
endif()

if(SELEKT_BUILD_NATIVE_BENCHMARKS)
  add_executable(selekt_native_benchmark ../benchmarks/native_sqlite_benchmark.c)
  set_property(TARGET selekt_native_benchmark PROPERTY C_STANDARD 99)
  set_property(TARGET selekt_native_benchmark PROPERTY C_STANDARD_REQUIRED ON)
  target_include_directories(selekt_native_benchmark SYSTEM PRIVATE
    include
    generated/include
    generated/include/sqlite3
  )
  target_compile_definitions(selekt_native_benchmark PRIVATE
    SELEKT_BENCHMARK_OPTIMIZATION="${SELEKT_SQLITE_OPTIMIZATION}"
    SELEKT_BENCHMARK_PGO="${SELEKT_SQLITE_PGO_ACTIVE}"
    SELEKT_BENCHMARK_THINLTO=$<BOOL:${SELEKT_ENABLE_THINLTO}>
  )
  target_link_libraries(selekt_native_benchmark PRIVATE selekt)
endif()
