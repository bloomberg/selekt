/*
 * Copyright 2020 Bloomberg Finance L.P.
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
#include <cstring>
#include <new>
#include <stdexcept>
#include <vector>
#include "Throws.h"
#include "secure_zero.h"

struct JniOutOfMemoryError : std::runtime_error {
    using std::runtime_error::runtime_error;
};

struct JniArrayLengthError : std::runtime_error {
    using std::runtime_error::runtime_error;
};

class AutoJByteArray
{
public:
    AutoJByteArray(const AutoJByteArray&) = delete;
    AutoJByteArray& operator=(const AutoJByteArray&) = delete;

    AutoJByteArray(JNIEnv* env, jbyteArray j, jint length)
        : mEnv(env),
          mJArray(j),
          mLength(length) {
        if (mLength < 0 || mLength > mEnv->GetArrayLength(mJArray)) {
            throwIndexOutOfBoundsException(mEnv, "Byte-array length is out of bounds.");
            throw JniArrayLengthError("Byte-array length is out of bounds.");
        }
        mpBytes = env->GetByteArrayElements(mJArray, nullptr);
        if (mpBytes == nullptr) {
            throwOutOfMemoryError(mEnv, "GetByteArrayElements");
            throw JniOutOfMemoryError("GetByteArrayElements");
        }
    }

    ~AutoJByteArray() {
        mEnv->ReleaseByteArrayElements(mJArray, mpBytes, JNI_ABORT);
    }

    const jbyte* data() const {
        return mpBytes;
    }

    const jbyte& operator[](jsize index) const {
        return mpBytes[index];
    }

    jsize length() const {
        return mLength;
    }

private:
    JNIEnv* const mEnv;
    jbyteArray mJArray;
    jbyte* mpBytes = nullptr;
    jsize const mLength;
};

class AutoJSensitiveByteArray
{
public:
    AutoJSensitiveByteArray(const AutoJSensitiveByteArray&) = delete;
    AutoJSensitiveByteArray& operator=(const AutoJSensitiveByteArray&) = delete;

    AutoJSensitiveByteArray(JNIEnv* env, jbyteArray j, jint length)
        : mLength(length) {
        if (mLength < 0 || mLength > env->GetArrayLength(j)) {
            throwIndexOutOfBoundsException(env, "Sensitive byte-array length is out of bounds.");
            throw JniArrayLengthError("Sensitive byte-array length is out of bounds.");
        }
        if (mLength == 0) {
            return;
        }
        try {
            mBytes.resize(static_cast<size_t>(mLength));
        } catch (const std::bad_alloc&) {
            throwOutOfMemoryError(env, "Allocate sensitive byte-array copy");
            throw JniOutOfMemoryError("Allocate sensitive byte-array copy");
        }
        env->GetByteArrayRegion(j, 0, mLength, mBytes.data());
        if (env->ExceptionCheck()) {
            selekt::secure_zero(
                reinterpret_cast<unsigned char*>(mBytes.data()),
                static_cast<size_t>(mLength)
            );
            throw JniArrayLengthError("Copy sensitive byte-array contents.");
        }
    }

    ~AutoJSensitiveByteArray() {
        if (!mBytes.empty()) {
            selekt::secure_zero(
                reinterpret_cast<unsigned char*>(mBytes.data()),
                mBytes.size()
            );
        }
    }

    const jbyte* data() const {
        return mBytes.data();
    }

    jsize length() const {
        return mLength;
    }

private:
    jsize const mLength;
    std::vector<jbyte> mBytes;
};

#endif //SELEKT_AUTOJBYTEARRAY_H
