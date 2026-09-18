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

get_filename_component(OPENSSL_PROJECT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../OpenSSL" ABSOLUTE)
set(OPENSSL_PROPERTIES_FILE "${OPENSSL_PROJECT_DIR}/openssl.properties")
if(NOT EXISTS "${OPENSSL_PROPERTIES_FILE}")
  message(FATAL_ERROR "OpenSSL properties not found: ${OPENSSL_PROPERTIES_FILE}")
endif()
file(STRINGS "${OPENSSL_PROPERTIES_FILE}" OPENSSL_VERSION_PROPERTIES REGEX "^openssl\\.version=")
list(LENGTH OPENSSL_VERSION_PROPERTIES OPENSSL_VERSION_PROPERTY_COUNT)
if(NOT OPENSSL_VERSION_PROPERTY_COUNT EQUAL 1)
  message(FATAL_ERROR "Expected exactly one openssl.version in: ${OPENSSL_PROPERTIES_FILE}")
endif()
list(GET OPENSSL_VERSION_PROPERTIES 0 OPENSSL_VERSION_PROPERTY)
string(REGEX REPLACE "^openssl\\.version=(.+)$" "\\1" OPENSSL_VERSION "${OPENSSL_VERSION_PROPERTY}")
if(NOT OPENSSL_VERSION MATCHES "^[0-9]+\\.[0-9]+\\.[0-9]+[A-Za-z0-9.-]*$")
  message(FATAL_ERROR "Invalid OpenSSL version '${OPENSSL_VERSION}' in: ${OPENSSL_PROPERTIES_FILE}")
endif()

get_filename_component(
  OPENSSL_GENERATED_TARGET_DIR
  "${OPENSSL_PROJECT_DIR}/build/generated/${SLKT_TARGET_ABI}"
  ABSOLUTE
)
set(OPENSSL_SOURCE_DIR "${OPENSSL_GENERATED_TARGET_DIR}/openssl-${OPENSSL_VERSION}")
set(OPENSSL_INCLUDE_DIR "${OPENSSL_SOURCE_DIR}/include")
file(GLOB OPENSSL_SOURCE_CANDIDATES CONFIGURE_DEPENDS LIST_DIRECTORIES true
  "${OPENSSL_GENERATED_TARGET_DIR}/openssl-*"
)
set(STALE_OPENSSL_SOURCE_DIRS "")
foreach(OPENSSL_SOURCE_CANDIDATE IN LISTS OPENSSL_SOURCE_CANDIDATES)
  if(IS_DIRECTORY "${OPENSSL_SOURCE_CANDIDATE}"
     AND NOT "${OPENSSL_SOURCE_CANDIDATE}" STREQUAL "${OPENSSL_SOURCE_DIR}")
    list(APPEND STALE_OPENSSL_SOURCE_DIRS "${OPENSSL_SOURCE_CANDIDATE}")
  endif()
endforeach()
if(STALE_OPENSSL_SOURCE_DIRS)
  message(FATAL_ERROR
    "Stale OpenSSL source directories found for ${SLKT_TARGET_ABI}: ${STALE_OPENSSL_SOURCE_DIRS}"
  )
endif()
if(NOT IS_DIRECTORY "${OPENSSL_INCLUDE_DIR}")
  message(FATAL_ERROR "OpenSSL ${OPENSSL_VERSION} include directory not found: ${OPENSSL_INCLUDE_DIR}")
endif()
set(OPENSSL_VERSION_HEADER "${OPENSSL_INCLUDE_DIR}/openssl/opensslv.h")
if(NOT EXISTS "${OPENSSL_VERSION_HEADER}")
  message(FATAL_ERROR "OpenSSL version header not found: ${OPENSSL_VERSION_HEADER}")
endif()
set_property(DIRECTORY APPEND PROPERTY CMAKE_CONFIGURE_DEPENDS
  "${OPENSSL_PROPERTIES_FILE}"
  "${OPENSSL_VERSION_HEADER}"
)
file(STRINGS "${OPENSSL_VERSION_HEADER}" OPENSSL_HEADER_VERSION_LINE
  REGEX "^#[ \t]*define[ \t]+OPENSSL_VERSION_STR[ \t]+\"[^\"]+\""
)
list(LENGTH OPENSSL_HEADER_VERSION_LINE OPENSSL_HEADER_VERSION_LINE_COUNT)
if(NOT OPENSSL_HEADER_VERSION_LINE_COUNT EQUAL 1)
  message(FATAL_ERROR "Could not determine OpenSSL version from: ${OPENSSL_VERSION_HEADER}")
endif()
string(REGEX REPLACE ".*\"([^\"]+)\".*" "\\1" OPENSSL_HEADER_VERSION "${OPENSSL_HEADER_VERSION_LINE}")
if(NOT OPENSSL_HEADER_VERSION STREQUAL OPENSSL_VERSION)
  message(FATAL_ERROR
    "OpenSSL header version mismatch: expected ${OPENSSL_VERSION}, found ${OPENSSL_HEADER_VERSION}"
  )
endif()

add_subdirectory(sqlite3)

find_package(JNI QUIET)
if(JNI_INCLUDE_DIRS)
  target_include_directories(selekt SYSTEM PUBLIC ${JNI_INCLUDE_DIRS})
endif()

message(STATUS "Including OpenSSL ${OPENSSL_VERSION} headers from: ${OPENSSL_INCLUDE_DIR}")
target_include_directories(selekt SYSTEM PUBLIC "${OPENSSL_INCLUDE_DIR}")

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
