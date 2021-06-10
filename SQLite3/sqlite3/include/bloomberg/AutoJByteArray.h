/*
 * Copyright 2021 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef SELEKT_AUTOJBYTEARRAY_H
#define SELEKT_AUTOJBYTEARRAY_H

#include <jni.h>
#include "Throws.h"

class AutoJByteArray
{
public:
    AutoJByteArray(JNIEnv* env, jbyteArray j, jint length)
        : mEnv(env),
          mJArray(j),
          mpBytes(env->GetByteArrayElements(mJArray, nullptr)),
          mLength(length) {
        if (mpBytes == nullptr) {
            throwOutOfMemoryError(mEnv, "GetByteArrayElements");
        }
    }

    ~AutoJByteArray() {
        mEnv->ReleaseByteArrayElements(mJArray, mpBytes, JNI_ABORT);
    }

    operator const jbyte*() {
        return mpBytes;
    }

    jsize length() {
        return mLength;
    }

private:
    JNIEnv* const mEnv;
    jbyteArray mJArray;
    jbyte* const mpBytes;
    jsize const mLength;
};

#endif //SELEKT_AUTOJBYTEARRAY_H
