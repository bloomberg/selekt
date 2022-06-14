/*
 * Copyright 2022 Bloomberg Finance L.P.
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

#include <time.h>
#include <jni.h>
#include <string.h>
#include <sys/types.h>

static jint android_util_Log_println_native(
    JNIEnv* env,
    jobject clazz,
    jint bufID,
    jint priority,
    jstring tagObj,
    jstring msgObj
) {
    return 0;
}

static jstring SystemProperties_get(
    JNIEnv *env,
    jclass clazz,
    jstring keyJ,
    jstring defJ
) {
    auto key = env->GetStringUTFChars(keyJ, NULL);
    if (strcmp("ro.product.cpu.abilist", key)) {
        env->ReleaseStringUTFChars(keyJ, key);
        return env->NewStringUTF("arm64-v8a,armeabi-v7a");
    } else if (strcmp("ro.product.cpu.abilist32", key)) {
        env->ReleaseStringUTFChars(keyJ, key);
        return env->NewStringUTF("armeabi-v7a");
    } else if (strcmp("ro.product.cpu.abilist64", key)) {
        env->ReleaseStringUTFChars(keyJ, key);
        return env->NewStringUTF("arm64-v8a");
    }
    env->ReleaseStringUTFChars(keyJ, key);
    return defJ;
}

static jboolean SystemProperties_get_boolean(
    JNIEnv *env,
    jclass clazz,
    jstring keyJ,
    jboolean defJ
) {
    return defJ;
}

static jboolean SystemProperties_get_int(
    JNIEnv *env,
    jclass clazz,
    jstring keyJ,
    jint defJ
) {
    return defJ;
}

jboolean is64Bit(
    JNIEnv *env,
    jobject jobj
) {
    return true;
}

static const JNINativeMethod kLogMethods[] = {
    { const_cast<char *>("println_native"), const_cast<char *>("(IILjava/lang/String;Ljava/lang/String;)I"), reinterpret_cast<void*>(android_util_Log_println_native) }
};
static const JNINativeMethod kSystemPropertiesMethods[] = {
    { const_cast<char *>("native_get"), const_cast<char *>("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"), reinterpret_cast<void*>(SystemProperties_get) },
    { const_cast<char *>("native_get_boolean"), const_cast<char *>("(Ljava/lang/String;Z)Z"), reinterpret_cast<void*>(SystemProperties_get_boolean) },
    { const_cast<char *>("native_get_int"), const_cast<char *>("(Ljava/lang/String;I)I"), reinterpret_cast<void*>(SystemProperties_get_int) }
};
static const JNINativeMethod kVMRuntimeMethods[] = {
    { const_cast<char *>("is64Bit"), const_cast<char *>("()Z"), reinterpret_cast<void*>(is64Bit) }
};

static int LOG_METHOD_COUNT = sizeof(kLogMethods) / sizeof(kLogMethods[0]);
static int SYSTEM_PROPERTIES_METHOD_COUNT = sizeof(kSystemPropertiesMethods) / sizeof(kSystemPropertiesMethods[0]);
static int VMRUNTIME_METHOD_COUNT = sizeof(kVMRuntimeMethods) / sizeof(kVMRuntimeMethods[0]);

static const char* LOG_CLASS_PATH = "android/util/Log";
static const char* SYSTEM_PROPERTIES_CLASS_PATH = "android/os/SystemProperties";
static const char* VMRUNTIME_CLASS_PATH = "dalvik/system/VMRuntime";

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_android_NativeFixtures_nativeInit(
    JNIEnv* env,
    jobject obj
) {
    auto clazz = env->FindClass(LOG_CLASS_PATH);
    auto result = env->RegisterNatives(clazz, kLogMethods, LOG_METHOD_COUNT);
    if (result != 0) {
        return result;
    }
    clazz = env->FindClass(SYSTEM_PROPERTIES_CLASS_PATH);
    result = env->RegisterNatives(clazz, kSystemPropertiesMethods, SYSTEM_PROPERTIES_METHOD_COUNT);
    if (result != 0) {
        return result;
    }
    clazz = env->FindClass(VMRUNTIME_CLASS_PATH);
    result = env->RegisterNatives(clazz, kVMRuntimeMethods, VMRUNTIME_METHOD_COUNT);
    return result;
}
