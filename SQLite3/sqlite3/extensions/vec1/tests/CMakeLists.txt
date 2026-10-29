# Copyright 2026 Bloomberg Finance L.P.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

cmake_minimum_required(VERSION 3.13)
project(vec1_native_tests C)

enable_testing()

add_executable(vec1_kmeans_scratch_test vec1_kmeans_scratch_test.c)
set_property(TARGET vec1_kmeans_scratch_test PROPERTY C_STANDARD 99)
set_property(TARGET vec1_kmeans_scratch_test PROPERTY C_STANDARD_REQUIRED ON)
target_include_directories(vec1_kmeans_scratch_test PRIVATE ..)
if(MSVC)
  target_compile_options(vec1_kmeans_scratch_test PRIVATE /W4 /WX)
else()
  target_compile_options(vec1_kmeans_scratch_test PRIVATE -Wall -Wextra -Werror)
endif()

add_test(NAME vec1_kmeans_scratch_test COMMAND vec1_kmeans_scratch_test)

add_executable(vec1_training_chunk_test vec1_training_chunk_test.c)
set_property(TARGET vec1_training_chunk_test PROPERTY C_STANDARD 99)
set_property(TARGET vec1_training_chunk_test PROPERTY C_STANDARD_REQUIRED ON)
target_include_directories(vec1_training_chunk_test PRIVATE ..)
if(MSVC)
  target_compile_options(vec1_training_chunk_test PRIVATE /W4 /WX)
else()
  target_compile_options(vec1_training_chunk_test PRIVATE -Wall -Wextra -Werror)
endif()

add_test(NAME vec1_training_chunk_test COMMAND vec1_training_chunk_test)
