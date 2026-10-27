/*
** Copyright 2026 Bloomberg Finance L.P.
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#ifndef VEC1_KMEANS_SCRATCH_H
#define VEC1_KMEANS_SCRATCH_H

#include <limits.h>
#include <stdint.h>

typedef struct Vec1KMeansScratchLayout Vec1KMeansScratchLayout;

/*
** The allocation is laid out in this order:
**
**   job array, aMin, per-thread sums, per-thread counts,
**   aggregate sums, aggregate counts.
**
** The i* fields are byte offsets to each section. The first offset is the
** end of the job array and the last section ends at nByte.
*/
struct Vec1KMeansScratchLayout {
  uint64_t iMin;       /* Start of aMin; end of the job array. */
  uint64_t iJobSum;    /* Start of the per-thread sums. */
  uint64_t iJobCount;  /* Start of the per-thread counts. */
  uint64_t iSum;       /* Start of the aggregate sums. */
  uint64_t iCount;     /* Start of the aggregate counts. */
  uint64_t nSumByte;
  uint64_t nCountByte;
  uint64_t nByte;
  int nSumElem;
};

static int vec1KMeansScratchProduct(
  uint64_t nLimit,
  uint64_t n1,
  uint64_t n2,
  uint64_t *pResult
){
  if( n2!=0 && n1>nLimit/n2 ) return 1;
  *pResult = n1*n2;
  return 0;
}

static int vec1KMeansScratchAppend(
  uint64_t nLimit,
  uint64_t nCount,
  uint64_t nItemByte,
  uint64_t *pnByte
){
  uint64_t nAppend;
  if( vec1KMeansScratchProduct(nLimit, nCount, nItemByte, &nAppend) ){
    return 1;
  }
  if( *pnByte>nLimit || nAppend>nLimit-*pnByte ) return 1;
  *pnByte += nAppend;
  return 0;
}

/*
** Calculate all byte offsets in the scratch allocation used by K-means.
** Return non-zero if any component or the complete layout exceeds nLimit.
*/
static int vec1KMeansScratchSize(
  uint64_t nLimit,
  uint64_t nJobSize,
  uint64_t nFloatSize,
  uint64_t nCountSize,
  uint64_t nThread,
  uint64_t nVec,
  uint64_t nSampleElem,
  uint64_t nK,
  Vec1KMeansScratchLayout *pLayout
){
  Vec1KMeansScratchLayout layout;
  uint64_t nByte = 0;
  uint64_t nSumElem;

  /* nSumElem is passed to vec1AddInPlace(), whose count is an int. Keep
  ** the signed-int boundary explicit: INT_MAX itself is valid. */
  if( nK!=0 && nSampleElem>(uint64_t)INT_MAX/nK ) return 1;
  nSumElem = nSampleElem*nK;

  if( vec1KMeansScratchProduct(
          nLimit, nSumElem, nFloatSize, &layout.nSumByte
      )
   || vec1KMeansScratchProduct(
          nLimit, nK, nCountSize, &layout.nCountByte
      )
  ){
    return 1;
  }
  layout.nSumElem = (int)nSumElem;

  if( vec1KMeansScratchAppend(nLimit, nThread, nJobSize, &nByte) ){
    return 1;
  }
  layout.iMin = nByte;
  if( vec1KMeansScratchAppend(nLimit, nVec, nFloatSize, &nByte) ){
    return 1;
  }
  layout.iJobSum = nByte;
  if( vec1KMeansScratchAppend(nLimit, nThread, layout.nSumByte, &nByte) ){
    return 1;
  }
  layout.iJobCount = nByte;
  if( vec1KMeansScratchAppend(nLimit, nThread, layout.nCountByte, &nByte) ){
    return 1;
  }
  layout.iSum = nByte;
  if( vec1KMeansScratchAppend(nLimit, 1, layout.nSumByte, &nByte) ){
    return 1;
  }
  layout.iCount = nByte;
  if( vec1KMeansScratchAppend(nLimit, 1, layout.nCountByte, &nByte) ){
    return 1;
  }

  layout.nByte = nByte;
  *pLayout = layout;
  return 0;
}

#endif
