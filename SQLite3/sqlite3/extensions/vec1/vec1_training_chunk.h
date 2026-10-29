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

#ifndef VEC1_TRAINING_CHUNK_H
#define VEC1_TRAINING_CHUNK_H

#include <limits.h>

/* Return the byte size of chunk iChunk, or -1 for an invalid layout. */
static int vec1TrainingChunkSize(
  int nVec,
  int nVecPerChunk,
  int nBytePerVec,
  int iChunk
){
  int nRemain;
  int nChunkVec;

  if( nVec<1 || nVecPerChunk<1 || nBytePerVec<1 || iChunk<0 ) return -1;
  if( iChunk>(nVec-1)/nVecPerChunk ) return -1;

  nRemain = nVec - iChunk*nVecPerChunk;
  nChunkVec = nRemain<nVecPerChunk ? nRemain : nVecPerChunk;
  if( nChunkVec>INT_MAX/nBytePerVec ) return -1;
  return nChunkVec*nBytePerVec;
}

#endif
