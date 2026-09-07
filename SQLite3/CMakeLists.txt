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

cmake_minimum_required (VERSION 3.13.0 FATAL_ERROR)

set(PROJECT_NAME Selekt)
set(CMAKE_TRY_COMPILE_TARGET_TYPE "STATIC_LIBRARY")

project(${PROJECT_NAME})

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

option(SELEKT_BUILD_FUZZERS "Build CI-only native fuzz targets" OFF)
set(SELEKT_FUZZ_ENGINE "libfuzzer" CACHE STRING "Fuzz engine: libfuzzer or afl")
set_property(CACHE SELEKT_FUZZ_ENGINE PROPERTY STRINGS libfuzzer afl)

if(USE_CCACHE)
  find_program(CCACHE_FOUND ccache)
  if(CCACHE_FOUND)
    message(STATUS "Using ccache.")
    set_property(GLOBAL PROPERTY RULE_LAUNCH_COMPILE ccache)
    set_property(GLOBAL PROPERTY RULE_LAUNCH_LINK ccache)
  endif()
endif()

if(NOT SLKT_TARGET_ABI)
  set(SLKT_TARGET_ABI ${ANDROID_ABI})
endif()

add_subdirectory(sqlite3)

find_package(JNI QUIET)
if(JNI_INCLUDE_DIRS)
  target_include_directories(selekt SYSTEM PUBLIC ${JNI_INCLUDE_DIRS})
endif()

set(SSL_INCLUDE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../OpenSSL/build/generated/${SLKT_TARGET_ABI}/openssl-*/include)
file(GLOB INCLUDE_OPENSSL_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../OpenSSL/build/generated/${SLKT_TARGET_ABI}/openssl-*/include)
if(NOT INCLUDE_OPENSSL_DIR)
  message(WARNING "OpenSSL include directory not found: ${SSL_INCLUDE_DIR}")
endif()
message(STATUS "Including OpenSSL headers from: ${INCLUDE_OPENSSL_DIR}")
target_include_directories(selekt SYSTEM PUBLIC ${INCLUDE_OPENSSL_DIR})

target_sources(selekt PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/sqlite3_jni.cpp)

if(SELEKT_BUILD_FUZZERS)
  if(NOT CMAKE_C_COMPILER_ID MATCHES "Clang")
    message(FATAL_ERROR "Native fuzz targets require Clang or an AFL++ Clang wrapper")
  endif()
  target_compile_options(selekt PRIVATE -fsanitize=address,undefined -fno-omit-frame-pointer)
  target_link_options(selekt PRIVATE -fsanitize=address,undefined)
  add_subdirectory(fuzz)
endif()

if((CMAKE_SYSTEM_NAME STREQUAL "Linux" OR MINGW) AND NOT SELEKT_BUILD_FUZZERS)
  target_link_options(selekt PRIVATE -static-libstdc++ -static-libgcc)
endif()

#target_compile_options(selekt PRIVATE "-Werror")
