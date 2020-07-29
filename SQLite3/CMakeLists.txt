cmake_minimum_required (VERSION 3.4.0 FATAL_ERROR)

set(PROJECT_NAME Selekt)
set(CMAKE_TRY_COMPILE_TARGET_TYPE "STATIC_LIBRARY")

project(${PROJECT_NAME})

set(CMAKE_CXX_STANDARD 14)

add_subdirectory(sqlite3)

find_package(JNI QUIET)
if(JNI_INCLUDE_DIRS)
  target_include_directories(selekt SYSTEM PUBLIC ${JNI_INCLUDE_DIRS})
endif()

file(GLOB INCLUDE_OPENSSL_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../OpenSSL/build/generated/openssl-*/include)
if(NOT INCLUDE_OPENSSL_DIR)
  message(WARNING "OpenSSL include directory not found.")
endif()
message(STATUS "Including OpenSSL headers from: ${INCLUDE_OPENSSL_DIR}")
target_include_directories(selekt SYSTEM PUBLIC ${INCLUDE_OPENSSL_DIR})

target_sources(selekt PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/sqlite3_jni.cpp)
#target_compile_options(selekt PRIVATE "-Werror")
