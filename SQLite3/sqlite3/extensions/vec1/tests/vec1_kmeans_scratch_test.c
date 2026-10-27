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

#include "vec1_kmeans_scratch.h"

#include <limits.h>
#include <stdint.h>
#include <stdio.h>

#define CHECK(condition) do {                                             \
  if( !(condition) ){                                                     \
    fprintf(stderr, "%s:%d: check failed: %s\n",                         \
        __FILE__, __LINE__, #condition);                                  \
    return 1;                                                             \
  }                                                                      \
} while( 0 )

static int testLayoutCalculation(void){
  Vec1KMeansScratchLayout layout;

  CHECK( 0==vec1KMeansScratchSize(
      UINT64_MAX, 96, 4, 4, 2, 3, 5, 7, &layout
  ));
  CHECK( layout.iMin==192 );
  CHECK( layout.iJobSum==204 );
  CHECK( layout.iJobCount==484 );
  CHECK( layout.iSum==540 );
  CHECK( layout.iCount==680 );
  CHECK( layout.nSumByte==140 );
  CHECK( layout.nCountByte==28 );
  CHECK( layout.nSumElem==35 );
  CHECK( layout.nByte==708 );
  return 0;
}

static int testSimulated32BitBoundary(void){
  Vec1KMeansScratchLayout layout;
  uint64_t nMaxVec;

  CHECK( 0==vec1KMeansScratchSize(
      UINT32_MAX, 96, 4, 4, 1, 0, 1, 1, &layout
  ));
  nMaxVec = (UINT32_MAX-layout.nByte)/4;

  CHECK( 0==vec1KMeansScratchSize(
      UINT32_MAX, 96, 4, 4, 1, nMaxVec, 1, 1, &layout
  ));
  CHECK( layout.nByte==UINT32_MAX-3 );
  CHECK( 0!=vec1KMeansScratchSize(
      UINT32_MAX, 96, 4, 4, 1, nMaxVec+1, 1, 1, &layout
  ));
  return 0;
}

static int testComponentOverflow(void){
  Vec1KMeansScratchLayout layout;

  /* The sum is exactly 4GiB. Its old int calculation wrapped to zero. */
  CHECK( 0!=vec1KMeansScratchSize(
      UINT32_MAX, 96, 4, 4, 1, 1, 4096, 262144, &layout
  ));

  /* The element count is consumed by APIs that take a signed int. */
  CHECK( 0!=vec1KMeansScratchSize(
      UINT64_MAX, 96, 4, 4, 1, 1, 4096, 524288, &layout
  ));
  return 0;
}

static int testIntBoundary(void){
  Vec1KMeansScratchLayout layout;
  const uint64_t nMaxThread = 128;

  /* The exact INT_MAX product remains valid, including at the maximum
  ** production thread count. */
  CHECK( 0==vec1KMeansScratchSize(
      UINT64_MAX, 96, 4, 4, nMaxThread, 0, 1, INT_MAX, &layout
  ));
  CHECK( layout.nSumElem==INT_MAX );

  /* One more element would overflow the signed-int count. */
  CHECK( 0!=vec1KMeansScratchSize(
      UINT64_MAX, 96, 4, 4, nMaxThread, 0, 1,
      (uint64_t)INT_MAX+1, &layout
  ));
  return 0;
}

static int testTotalOverflow(void){
  Vec1KMeansScratchLayout layout;

  /* Individual sums fit in 32 bits, but all thread-local sums do not. */
  CHECK( 0!=vec1KMeansScratchSize(
      UINT32_MAX, 96, 4, 4, 4, 1, 4096, 65535, &layout
  ));

  /* Exercise checked 64-bit multiplication independently of pointer width. */
  CHECK( 0!=vec1KMeansScratchSize(
      UINT64_MAX, UINT64_MAX, 4, 4, 2, 1, 1, 1, &layout
  ));
  return 0;
}

int main(void){
  CHECK( testLayoutCalculation()==0 );
  CHECK( testSimulated32BitBoundary()==0 );
  CHECK( testComponentOverflow()==0 );
  CHECK( testIntBoundary()==0 );
  CHECK( testTotalOverflow()==0 );
  return 0;
}
