/*
 * Copyright 2026 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "vec1_training_chunk.h"

#include <limits.h>
#include <stdio.h>

#define CHECK(condition) do {                                             \
  if( !(condition) ){                                                     \
    fprintf(stderr, "%s:%d: check failed: %s\n",                         \
        __FILE__, __LINE__, #condition);                                  \
    return 1;                                                             \
  }                                                                      \
} while( 0 )

int main(void){
  const int nVecPerChunk = 16*1024*1024;
  const int nVec = 1073741825;

  CHECK( vec1TrainingChunkSize(nVec, nVecPerChunk, 4, 0)==64*1024*1024 );
  CHECK( vec1TrainingChunkSize(nVec, nVecPerChunk, 4, 63)==64*1024*1024 );
  CHECK( vec1TrainingChunkSize(nVec, nVecPerChunk, 4, 64)==4 );
  CHECK( vec1TrainingChunkSize(nVec, nVecPerChunk, 4, 65)==-1 );
  CHECK( vec1TrainingChunkSize(INT_MAX, 1, INT_MAX, INT_MAX-1)==INT_MAX );
  CHECK( vec1TrainingChunkSize(1, 1, INT_MAX, 0)==INT_MAX );
  CHECK( vec1TrainingChunkSize(2, 2, INT_MAX, 0)==-1 );
  return 0;
}
