# Copyright 2022 Bloomberg Finance L.P.
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

add_library(selekt SHARED generated/cpp/sqlite3.c)

target_compile_definitions(selekt PUBLIC
  HAVE_USLEEP=1
  SQLCIPHER_CRYPTO_OPENSSL
  SQLITE_ALLOW_URI_AUTHORITY
  SQLITE_DEFAULT_AUTOVACUUM=2
  SQLITE_DEFAULT_CACHE_SIZE=200
  SQLITE_DEFAULT_FILE_PERMISSIONS=0600
  SQLITE_DEFAULT_MEMSTATUS=0
  SQLITE_DEFAULT_SYNCHRONOUS=2
  SQLITE_DEFAULT_WAL_SYNCHRONOUS=1
  SQLITE_DQS=0
  SQLITE_ENABLE_BATCH_ATOMIC_WRITE
  SQLITE_ENABLE_FTS3_PARENTHESIS
  SQLITE_ENABLE_FTS4
  SQLITE_ENABLE_FTS5
  SQLITE_ENABLE_JSON1
  # SQLITE_ENABLE_MATH_FUNCTIONS TODO Uncomment with SQLite 3.35.0.
  SQLITE_ENABLE_MEMORY_MANAGEMENT=1
  SQLITE_ENABLE_RTREE
  SQLITE_ENABLE_STAT4
  SQLITE_ENABLE_UNLOCK_NOTIFY
  SQLITE_HAS_CODEC=1
  SQLITE_LIKE_DOESNT_MATCH_BLOBS
  SQLITE_OMIT_AUTOINIT
  SQLITE_OMIT_DECLTYPE
  SQLITE_OMIT_DEPRECATED
  SQLITE_OMIT_LOAD_EXTENSION
  SQLITE_OMIT_PROGRESS_CALLBACK
  SQLITE_OMIT_SHARED_CACHE
  SQLITE_TEMP_STORE=3
  SQLITE_THREADSAFE=2
  SQLITE_TRUSTED_SCHEMA=0
  SQLITE_USE_ALLOCA
  SQLITE_USE_URI=1
)

target_include_directories(selekt SYSTEM PUBLIC include generated/include)

find_library(log-lib log)
if(log-lib)
  target_compile_definitions(selekt PUBLIC SELEKT_LOG)
  target_link_libraries(selekt ${log-lib})
endif()

set(LIBCRYPTO ${CMAKE_CURRENT_SOURCE_DIR}/../../OpenSSL/build/libs/${SLKT_TARGET_ABI}/libcrypto.a)
if(NOT LIBCRYPTO)
  message(WARNING "OpenSSL libcrypto.a not found.")
endif()
message(STATUS "Linking to OpenSSL libcrypto: ${LIBCRYPTO}")
target_link_libraries(selekt ${LIBCRYPTO})
