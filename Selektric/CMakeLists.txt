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

cmake_minimum_required (VERSION 3.5.0 FATAL_ERROR)

set(PROJECT_NAME Selektric)
set(CMAKE_TRY_COMPILE_TARGET_TYPE "STATIC_LIBRARY")

project(${PROJECT_NAME})

set(CMAKE_CXX_STANDARD 14)

if(USE_CCACHE)
  find_program(CCACHE_FOUND ccache)
  if(CCACHE_FOUND)
    message(STATUS "Using ccache.")
    set_property(GLOBAL PROPERTY RULE_LAUNCH_COMPILE ccache)
    set_property(GLOBAL PROPERTY RULE_LAUNCH_LINK ccache)
  endif()
endif()

add_library(selektric SHARED)

find_package(JNI QUIET)
target_include_directories(selektric SYSTEM PUBLIC ${JNI_INCLUDE_DIRS})

target_sources(selektric PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/native_mocks.cpp)
