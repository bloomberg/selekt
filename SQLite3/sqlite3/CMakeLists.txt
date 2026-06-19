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

set(SELEKT_VEC1_SOURCE extensions/vec1/vec1.c)
set(SELEKT_SOURCES generated/cpp/sqlite3.c)
if(SELEKT_ENABLE_VEC1)
  list(APPEND SELEKT_SOURCES ${SELEKT_VEC1_SOURCE})
endif()

add_library(selekt SHARED
  ${SELEKT_SOURCES}
)

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
  SQLITE_ENABLE_RTREE
  SQLITE_ENABLE_STAT4
  SQLITE_ENABLE_UNLOCK_NOTIFY
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

option(SELEKT_VEC1_ENABLE_X86_AVX2 "Build vec1 with -mavx2 -mfma on x86_64" OFF)
if(SELEKT_ENABLE_VEC1 AND SELEKT_VEC1_ENABLE_X86_AVX2 AND NOT MSVC AND CMAKE_SYSTEM_PROCESSOR MATCHES "^(x86_64|amd64|AMD64)$")
  set_source_files_properties(${SELEKT_VEC1_SOURCE} PROPERTIES COMPILE_OPTIONS "-mavx2;-mfma")
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

set(LIBCRYPTO_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../OpenSSL/build/libs/${SLKT_TARGET_ABI})
set(LIBCRYPTO ${LIBCRYPTO_DIR}/libcrypto.a)
if(NOT EXISTS ${LIBCRYPTO} AND EXISTS ${LIBCRYPTO_DIR}/libcrypto.lib)
  set(LIBCRYPTO ${LIBCRYPTO_DIR}/libcrypto.lib)
endif()
if(NOT EXISTS ${LIBCRYPTO})
  message(WARNING "OpenSSL libcrypto not found under: ${LIBCRYPTO_DIR}")
endif()
message(STATUS "Linking to OpenSSL libcrypto: ${LIBCRYPTO}")
target_link_libraries(selekt PRIVATE ${LIBCRYPTO})

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

if(CMAKE_SYSTEM_NAME STREQUAL "Linux" AND NOT ANDROID_ABI AND NOT ANDROID)
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
