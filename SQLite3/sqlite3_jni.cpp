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

#include <jni.h>
#include <sqlite3/sqlite3.h>
#include <cstddef>
#include <cstring>
#include <bloomberg/AutoJByteArray.h>
#include <bloomberg/log.h>
#include <SelektConfig.h>

namespace {
    struct ThrowableClasses {
        jclass illegalArgumentException = nullptr;
        jclass illegalStateException = nullptr;
        jclass indexOutOfBoundsException = nullptr;
        jclass outOfMemoryError = nullptr;
    };

    ThrowableClasses& throwableClasses() {
        static ThrowableClasses instance;
        return instance;
    }
}

void initThrowableClasses(JNIEnv* env) {
    auto& classes = throwableClasses();
    classes.illegalArgumentException = (jclass)env->NewGlobalRef(env->FindClass("java/lang/IllegalArgumentException"));
    classes.illegalStateException = (jclass)env->NewGlobalRef(env->FindClass("java/lang/IllegalStateException"));
    classes.indexOutOfBoundsException = (jclass)env->NewGlobalRef(env->FindClass("java/lang/IndexOutOfBoundsException"));
    classes.outOfMemoryError = (jclass)env->NewGlobalRef(env->FindClass("java/lang/OutOfMemoryError"));
}

void throwIllegalArgumentException(JNIEnv* env, const char* message) {
    auto cls = throwableClasses().illegalArgumentException;
    if (cls != nullptr) {
        env->ThrowNew(cls, message);
    }
}

void throwIllegalStateException(JNIEnv* env, const char* message) {
    auto cls = throwableClasses().illegalStateException;
    if (cls != nullptr) {
        env->ThrowNew(cls, message);
    }
}

void throwIndexOutOfBoundsException(JNIEnv* env, const char* message) {
    auto cls = throwableClasses().indexOutOfBoundsException;
    if (cls != nullptr) {
        env->ThrowNew(cls, message);
    }
}

void throwOutOfMemoryError(JNIEnv* env, const char* message) {
    auto cls = throwableClasses().outOfMemoryError;
    if (cls != nullptr) {
        env->ThrowNew(cls, message);
    }
}

static inline void updateHolder(JNIEnv* env, jarray array, int offset, void* p) {
    auto pp = static_cast<size_t*>(env->GetPrimitiveArrayCritical(array, nullptr));
    if (pp == nullptr) {
        throwOutOfMemoryError(env, "GetPrimitiveArrayCritical");
        return;
    }
    *(pp + offset) = reinterpret_cast<size_t>(p);
    env->ReleasePrimitiveArrayCritical(array, pp, 0);
}

static jbyteArray newByteArray(JNIEnv* env, const void* p, jsize size) {
    jbyteArray array = env->NewByteArray(size);
    if (array == nullptr) {
        throwOutOfMemoryError(env, "NewByteArray");
        return nullptr;
    } else if (size > 0) {
        void* buffer = env->GetPrimitiveArrayCritical(array, nullptr);
        if (buffer == nullptr) {
            throwOutOfMemoryError(env, "GetPrimitiveArrayCritical");
            return nullptr;
        }
        std::memcpy(buffer, p, static_cast<size_t>(size));
        env->ReleasePrimitiveArrayCritical(array, buffer, 0);
    }
    return array;
}

static jint rawKey(
    JNIEnv* env,
    jlong jdb,
    jbyteArray jkey,
    jint keyLength
) {
    if (keyLength != 32) {
        throwIllegalArgumentException(env, "Key must be 32 bytes in size.");
        return SQLITE_ERROR;
    }
    try {
    AutoJByteArray key(env, jkey, keyLength);
    char sql[81];
    std::memcpy(sql, "PRAGMA key=\"x'", 14);
    const char hex_chars[] = "0123456789abcdef";
    for (int i = 0; i < keyLength; ++i) {
        auto byte = static_cast<std::byte>(key[i]);
        sql[14 + 2*i]     = hex_chars[std::to_integer<unsigned char>(byte >> 4)];
        sql[14 + 2*i + 1] = hex_chars[std::to_integer<unsigned char>(byte & std::byte{0xF})];
    }
    sql[78] = '\'';
    sql[79] = '"';
    sql[80] = '\0';
    auto result = sqlite3_exec(reinterpret_cast<sqlite3*>(jdb), sql, nullptr, nullptr, nullptr);
    volatile char* p = sql;
    for (size_t i = 0; i < sizeof(sql); ++i) {
        p[i] = '\0';
    }
    return result;
    } catch (const JniOutOfMemoryError&) {
        return SQLITE_NOMEM;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_gitCommit(
    JNIEnv* env,
    jobject obj
) {
    return env->NewStringUTF(SELEKT_GIT_COMMIT);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_bindBlob(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jbyteArray jvalue,
    jint length
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    try {
        AutoJByteArray value(env, jvalue, length);
        return sqlite3_bind_blob(statement, index, value.data(), value.length(), SQLITE_TRANSIENT);
    } catch (const JniOutOfMemoryError&) {
        return SQLITE_NOMEM;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_bindDouble(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jdouble jvalue
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_double(statement, index, jvalue);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_bindInt(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jint jvalue
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_int(statement, index, jvalue);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_bindInt64(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jlong jvalue
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_int64(statement, index, jvalue);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_bindNull(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_null(statement, index);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_bindParameterCount(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_parameter_count(statement);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_bindParameterIndex(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jstring jname
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    auto name = env->GetStringUTFChars(jname, nullptr);
    if (name == nullptr) {
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return 0;
    }
    auto result = sqlite3_bind_parameter_index(statement, name);
    env->ReleaseStringUTFChars(jname, name);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_bindText(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jstring jvalue
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    auto value = env->GetStringUTFChars(jvalue, nullptr);
    if (value == nullptr) {
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return SQLITE_NOMEM;
    }
    auto result = sqlite3_bind_text(
        statement,
        index,
        value,
        env->GetStringUTFLength(jvalue),
        SQLITE_TRANSIENT
    );
    env->ReleaseStringUTFChars(jvalue, value);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_bindZeroBlob(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jint length
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_zeroblob(statement, index, length);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_blobBytes(
    JNIEnv* env,
    jobject obj,
    jlong jblob
) {
    return sqlite3_blob_bytes(reinterpret_cast<sqlite3_blob*>(jblob));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_blobClose(
    JNIEnv* env,
    jobject obj,
    jlong jblob
) {
    return sqlite3_blob_close(reinterpret_cast<sqlite3_blob*>(jblob));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_blobOpen(
    JNIEnv* env,
    jobject obj,
    jlong jdb,
    jstring jname,
    jstring jtable,
    jstring jcolumn,
    jlong jrow,
    jint jflags,
    jlongArray jholder
) {
    sqlite3_blob* blob = nullptr;
    auto name = env->GetStringUTFChars(jname, nullptr);
    if (name == nullptr) {
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return SQLITE_NOMEM;
    }
    auto table = env->GetStringUTFChars(jtable, nullptr);
    if (table == nullptr) {
        env->ReleaseStringUTFChars(jname, name);
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return SQLITE_NOMEM;
    }
    auto column = env->GetStringUTFChars(jcolumn, nullptr);
    if (column == nullptr) {
        env->ReleaseStringUTFChars(jname, name);
        env->ReleaseStringUTFChars(jtable, table);
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return SQLITE_NOMEM;
    }
    auto result = sqlite3_blob_open(
        reinterpret_cast<sqlite3*>(jdb),
        name,
        table,
        column,
        jrow,
        jflags,
        &blob
    );
    env->ReleaseStringUTFChars(jname, name);
    env->ReleaseStringUTFChars(jtable, table);
    env->ReleaseStringUTFChars(jcolumn, column);
    if (result == SQLITE_OK) {
        updateHolder(env, jholder, 0, blob);
    }
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_blobRead(
    JNIEnv* env,
    jobject obj,
    jlong jBlob,
    jint jOffset,
    jbyteArray jDestination,
    jint jDestinationOffset,
    jint jLength
) {
    jsize arrayLength = env->GetArrayLength(jDestination);
    if (jLength < 0) {
        throwIllegalArgumentException(env, "blobRead: length must be non-negative");
        return SQLITE_ERROR;
    }
    if (jDestinationOffset < 0 || jDestinationOffset + jLength > arrayLength) {
        throwIndexOutOfBoundsException(env, "blobRead: offset/length out of bounds");
        return SQLITE_ERROR;
    }
    auto source = env->GetByteArrayElements(jDestination, nullptr);
    if (source == nullptr) {
        throwOutOfMemoryError(env, "GetByteArrayElements");
        return SQLITE_NOMEM;
    }
    auto result = sqlite3_blob_read(
        reinterpret_cast<sqlite3_blob*>(jBlob),
        source + jDestinationOffset,
        jLength,
        jOffset
    );
    env->ReleaseByteArrayElements(jDestination, source, 0);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_blobReopen(
    JNIEnv* env,
    jobject obj,
    jlong jblob,
    jlong jrow
) {
    return sqlite3_blob_reopen(reinterpret_cast<sqlite3_blob*>(jblob), jrow);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_blobWrite(
    JNIEnv* env,
    jobject obj,
    jlong jBlob,
    jint jOffset,
    jbyteArray jSource,
    jint jSourceOffset,
    jint jLength
) {
    jsize arrayLength = env->GetArrayLength(jSource);
    if (jLength < 0) {
        throwIllegalArgumentException(env, "blobWrite: length must be non-negative");
        return SQLITE_ERROR;
    }
    if (jSourceOffset < 0 || jSourceOffset + jLength > arrayLength) {
        throwIndexOutOfBoundsException(env, "blobWrite: offset/length out of bounds");
        return SQLITE_ERROR;
    }
    auto source = env->GetByteArrayElements(jSource, nullptr);
    if (source == nullptr) {
        throwOutOfMemoryError(env, "GetByteArrayElements");
        return SQLITE_NOMEM;
    }
    auto result = sqlite3_blob_write(
        reinterpret_cast<sqlite3_blob*>(jBlob),
        source + jSourceOffset,
        jLength,
        jOffset
    );
    env->ReleaseByteArrayElements(jSource, source, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_busyTimeout(
    JNIEnv* env,
    jobject clazz,
    jlong jdb,
    jint millis
) {
    return sqlite3_busy_timeout(reinterpret_cast<sqlite3*>(jdb), millis);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_changes(
    JNIEnv* env,
    jobject obj,
    jlong jdb
) {
    return sqlite3_changes(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_clearBindings(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_clear_bindings(statement);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_closeV2(
    JNIEnv* env,
    jobject jobj,
    jlong jdb
) {
    return sqlite3_close_v2(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_columnBlob(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    // sqlite3_column_blob returns null for a zero-length blob.
    // ref: https://www.sqlite.org/c3ref/column_blob.html
    auto result = sqlite3_column_blob(statement, index);
    if (result) {
        auto size = sqlite3_column_bytes(statement, index);
        if (size > 0) {
            return newByteArray(env, result, size);
        }
    }
    return nullptr;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_columnCount(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_count(statement);
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_columnDouble(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_double(statement, index);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_columnInt(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_int(statement, index);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_columnInt64(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_int64(statement, index);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_columnName(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    auto name = sqlite3_column_name(statement, index);
    return name != nullptr ? env->NewStringUTF(name) : nullptr;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_columnText(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    auto text = reinterpret_cast<const char*>(sqlite3_column_text(statement, index));
    return text != nullptr ? env->NewStringUTF(text) : nullptr;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_columnType(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_type(statement, index);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_columnValue(
    JNIEnv* env,
    jobject obj,
    jlong statement,
    jint index
) {
    return reinterpret_cast<jlong>(sqlite3_column_value(reinterpret_cast<sqlite3_stmt*>(statement), index));
}

struct CommitListenerContext {
    JavaVM* vm;
    jobject listener;
    jmethodID onCommitMethod;
    jmethodID onRollbackMethod;
};

static void freeCommitListenerContext(void* ctx) {
    if (ctx != nullptr) {
        auto context = static_cast<CommitListenerContext*>(ctx);
        JNIEnv* env = nullptr;
        if (context->vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) {
            env->DeleteGlobalRef(context->listener);
        }
        delete context;
    }
}

static int commitHookCallback(void* ctx) {
    auto context = static_cast<CommitListenerContext*>(ctx);
    JNIEnv* env = nullptr;
#ifdef __ANDROID__
    if (context->vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
#else
    if (context->vm->AttachCurrentThread(reinterpret_cast<void**>(&env), nullptr) != JNI_OK) {
#endif
        return 1;
    }
    jint result = env->CallIntMethod(context->listener, context->onCommitMethod);
    if (env->ExceptionCheck()) {
        return 1;
    }
    return result;
}

static void rollbackHookCallback(void* ctx) {
    auto context = static_cast<CommitListenerContext*>(ctx);
    JNIEnv* env = nullptr;
#ifdef __ANDROID__
    if (context->vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
#else
    if (context->vm->AttachCurrentThread(reinterpret_cast<void**>(&env), nullptr) != JNI_OK) {
#endif
        return;
    }
    env->CallVoidMethod(context->listener, context->onRollbackMethod);
    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_commitHook(
    JNIEnv* env,
    jobject obj,
    jlong jdb,
    jboolean enabled,
    jobject listener
) {
    auto db = reinterpret_cast<sqlite3*>(jdb);
    if (!enabled) {
        void* oldContext = sqlite3_commit_hook(db, nullptr, nullptr);
        sqlite3_rollback_hook(db, nullptr, nullptr);
        freeCommitListenerContext(oldContext);
        return SQLITE_OK;
    }
    if (listener == nullptr) {
        return SQLITE_ERROR;
    }
    jclass listenerClass = env->GetObjectClass(listener);
    jmethodID onCommitMethod = env->GetMethodID(listenerClass, "onCommit", "()I");
    jmethodID onRollbackMethod = env->GetMethodID(listenerClass, "onRollback", "()V");
    if (onCommitMethod == nullptr || onRollbackMethod == nullptr) {
        return SQLITE_ERROR;
    }
    auto context = new CommitListenerContext;
    env->GetJavaVM(&context->vm);
    context->listener = env->NewGlobalRef(listener);
    if (context->listener == nullptr) {
        delete context;
        throwOutOfMemoryError(env, "NewGlobalRef");
        return SQLITE_NOMEM;
    }
    context->onCommitMethod = onCommitMethod;
    context->onRollbackMethod = onRollbackMethod;
    void* oldContext = sqlite3_commit_hook(db, commitHookCallback, context);
    sqlite3_rollback_hook(db, rollbackHookCallback, context);
    freeCommitListenerContext(oldContext);
    return SQLITE_OK;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_databaseHandle(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    return reinterpret_cast<jlong>(sqlite3_db_handle(reinterpret_cast<sqlite3_stmt*>(jstatement)));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_databaseReadOnly(
    JNIEnv* env,
    jobject clazz,
    jlong jdb,
    jstring jname
) {
    auto name = env->GetStringUTFChars(jname, nullptr);
    if (name == nullptr) {
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return -1;
    }
    auto result = sqlite3_db_readonly(reinterpret_cast<sqlite3*>(jdb), name);
    env->ReleaseStringUTFChars(jname, name);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_databaseReleaseMemory(
    JNIEnv* env,
    jobject clazz,
    jlong jdb
) {
    return sqlite3_db_release_memory(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_databaseStatus(
    JNIEnv* env,
    jobject obj,
    jlong jdatabase,
    jint op,
    jboolean reset,
    jintArray holder
) {
    int current = 0;
    int highWater = 0;
    int result = sqlite3_db_status(
        reinterpret_cast<sqlite3*>(jdatabase),
        op,
        &current,
        &highWater,
        reset
    );
    auto elements = static_cast<jint*>(env->GetPrimitiveArrayCritical(holder, nullptr));
    if (elements == nullptr) {
        throwOutOfMemoryError(env, "GetPrimitiveArrayCritical");
        return result;
    }
    elements[0] = current;
    elements[1] = highWater;
    env->ReleasePrimitiveArrayCritical(holder, elements, 0);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_errorCode(
    JNIEnv* env,
    jobject clazz,
    jlong jdb
) {
    return sqlite3_errcode(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_errorMessage(
    JNIEnv* env,
    jobject clazz,
    jlong jdb
) {
    return env->NewStringUTF(sqlite3_errmsg(reinterpret_cast<sqlite3*>(jdb)));
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_expandedSql(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    auto sql = sqlite3_expanded_sql(reinterpret_cast<sqlite3_stmt*>(jstatement));
    if (sql == nullptr) {
        return nullptr;
    }
    auto result = env->NewStringUTF(sql);
    sqlite3_free(sql);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_exec(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jstring jquery
) {
    auto query = env->GetStringUTFChars(jquery, nullptr);
    if (query == nullptr) {
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return SQLITE_NOMEM;
    }
    auto result = sqlite3_exec(reinterpret_cast<sqlite3*>(jdb), query, nullptr, nullptr, nullptr);
    env->ReleaseStringUTFChars(jquery, query);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_extendedErrorCode(
    JNIEnv* env,
    jobject clazz,
    jlong jdb
) {
    return sqlite3_extended_errcode(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_extendedResultCodes(
    JNIEnv* env,
    jobject clazz,
    jlong jdb,
    jint onOff
) {
    return sqlite3_extended_result_codes(reinterpret_cast<sqlite3*>(jdb), onOff);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_finalize(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_finalize(statement);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_getAutocommit(
    JNIEnv* env,
    jobject obj,
    jlong jdb
) {
    return sqlite3_get_autocommit(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_hardHeapLimit64(
    JNIEnv* env,
    jobject obj
) {
    return sqlite3_hard_heap_limit64(-1);
}

extern "C" JNIEXPORT void JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_interrupt(
    JNIEnv* env,
    jobject obj,
    jlong jdb
) {
    sqlite3_interrupt(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_isInterrupted(
    JNIEnv* env,
    jobject obj,
    jlong jdb
) {
    return sqlite3_is_interrupted(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_key(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jbyteArray jkey,
    jint length
) {
    try {
        AutoJByteArray key(env, jkey, length);
        return sqlite3_key(reinterpret_cast<sqlite3*>(jdb), key.data(), key.length());
    } catch (const JniOutOfMemoryError&) {
        return SQLITE_NOMEM;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_keyConventionally(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jbyteArray jkey,
    jint length
) {
    return rawKey(env, jdb, jkey, length);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_keywordCount(
    JNIEnv* env,
    jobject jobj
) {
    return sqlite3_keyword_count();
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_lastInsertRowId(
    JNIEnv* env,
    jobject obj,
    jlong jdb
) {
    return sqlite3_last_insert_rowid(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_libVersion(
    JNIEnv* env,
    jobject jobj
) {
    return env->NewStringUTF(sqlite3_libversion());
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_libVersionNumber(
    JNIEnv* env,
    jobject jobj
) {
    return sqlite3_libversion_number();
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_memoryUsed(
    JNIEnv* env,
    jobject jobj
) {
    return sqlite3_memory_used();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_openV2(
    JNIEnv* env,
    jobject jobj,
    jstring jfilename,
    jint jflags,
    jlongArray dbHolder
) {
    sqlite3* db = nullptr;
    auto filename = env->GetStringUTFChars(jfilename, nullptr);
    if (filename == nullptr) {
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return SQLITE_NOMEM;
    }
    auto result = sqlite3_open_v2(filename, &db, jflags, nullptr);
    env->ReleaseStringUTFChars(jfilename, filename);
    updateHolder(env, dbHolder, 0, db);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_prepareV2(
    JNIEnv* env,
    jobject obj,
    jlong jdb,
    jstring jsql,
    jint jlength,
    jlongArray statementHolder
) {
    sqlite3_stmt* statement = nullptr;
    auto sql = env->GetStringUTFChars(jsql, nullptr);
    if (sql == nullptr) {
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return SQLITE_NOMEM;
    }
    auto result = sqlite3_prepare_v2(reinterpret_cast<sqlite3*>(jdb), sql, jlength, &statement, nullptr);
    env->ReleaseStringUTFChars(jsql, sql);
    if (result == SQLITE_OK) {
        updateHolder(env, statementHolder, 0, statement);
    }
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_rawKey(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jbyteArray jkey,
    jint keyLength
) {
    return rawKey(env, jdb, jkey, keyLength);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_rekey(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jbyteArray jkey,
    jint length
) {
    try {
        AutoJByteArray key(env, jkey, length);
        if (key.length() == 0) {
            return sqlite3_rekey(reinterpret_cast<sqlite3*>(jdb), nullptr, key.length());
        }
        return sqlite3_rekey(reinterpret_cast<sqlite3*>(jdb), key.data(), key.length());
    } catch (const JniOutOfMemoryError&) {
        return SQLITE_NOMEM;
    }
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_releaseMemory(
    JNIEnv* env,
    jobject clazz,
    jint bytes
) {
    return sqlite3_release_memory(bytes);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_reset(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    return sqlite3_reset(reinterpret_cast<sqlite3_stmt*>(jstatement));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_resetAndClearBindings(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    auto const result = sqlite3_reset(statement);
    if (SQLITE_OK != result) {
        return result;
    }
    return sqlite3_clear_bindings(statement);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_softHeapLimit64(
    JNIEnv* env,
    jobject obj
) {
    return sqlite3_soft_heap_limit64(-1);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_sql(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    return env->NewStringUTF(sqlite3_sql(reinterpret_cast<sqlite3_stmt*>(jstatement)));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_step(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_step(statement);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_statementBusy(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    return sqlite3_stmt_busy(reinterpret_cast<sqlite3_stmt*>(jstatement));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_statementReadOnly(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    return sqlite3_stmt_readonly(reinterpret_cast<sqlite3_stmt*>(jstatement));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_statementStatus(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint op,
    jboolean reset
) {
    return sqlite3_stmt_status(
        reinterpret_cast<sqlite3_stmt*>(jstatement),
        op,
        reset
    );
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_threadsafe(
    JNIEnv* env,
    jobject obj
) {
    return sqlite3_threadsafe();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_totalChanges(
    JNIEnv* env,
    jobject obj,
    jlong jdb
) {
    return sqlite3_total_changes(reinterpret_cast<sqlite3*>(jdb));
}

extern "C" JNIEXPORT void JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_traceV2(
    JNIEnv* env,
    jobject obj,
    jlong jdb,
    jint flag
) {
    sqlite3_trace_v2(
        reinterpret_cast<sqlite3*>(jdb),
        static_cast<unsigned int>(flag),
        [](unsigned trace, void* context, void* p, void* x){
            switch (trace) {
                case SQLITE_TRACE_ROW: LOG_D("ROW: %p", p); break;
                case SQLITE_TRACE_PROFILE: LOG_D("PROFILE: %p %lldns", p, reinterpret_cast<sqlite3_int64>(x)); break;
                case SQLITE_TRACE_STMT: LOG_D("STMT: %p %s", p, static_cast<const char*>(x)); break;
                case SQLITE_TRACE_CLOSE: LOG_D("CLOSE: %p", p); break;
                default: break;
            }
            return 0;
        },
        nullptr
    );
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_transactionState(
    JNIEnv* env,
    jobject obj,
    jlong jdb
) {
    return sqlite3_txn_state(reinterpret_cast<sqlite3*>(jdb), nullptr);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_valueDup(
    JNIEnv* env,
    jobject obj,
    jlong jvalue
) {
    return reinterpret_cast<jlong>(sqlite3_value_dup(reinterpret_cast<sqlite3_value*>(jvalue)));
}

extern "C" JNIEXPORT void JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_valueFree(
    JNIEnv* env,
    jobject obj,
    jlong jvalue
) {
    sqlite3_value_free(reinterpret_cast<sqlite3_value*>(jvalue));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_valueFromBind(
    JNIEnv* env,
    jobject obj,
    jlong jvalue
) {
    return sqlite3_value_frombind(reinterpret_cast<sqlite3_value*>(jvalue));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_walAutoCheckpoint(
    JNIEnv* env,
    jobject obj,
    jlong jdb,
    jint pages
) {
    return sqlite3_wal_autocheckpoint(reinterpret_cast<sqlite3*>(jdb), pages);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_walCheckpointV2(
    JNIEnv* env,
    jobject obj,
    jlong jdb,
    jstring jname,
    jint mode
) {
    if (env->IsSameObject(jname, nullptr)) {
        return sqlite3_wal_checkpoint_v2(reinterpret_cast<sqlite3*>(jdb), nullptr, mode, nullptr, nullptr);
    }
    auto name = env->GetStringUTFChars(jname, nullptr);
    if (name == nullptr) {
        throwOutOfMemoryError(env, "GetStringUTFChars");
        return SQLITE_NOMEM;
    }
    auto result = sqlite3_wal_checkpoint_v2(
        reinterpret_cast<sqlite3*>(jdb),
        name,
        mode,
        nullptr,
        nullptr
    );
    env->ReleaseStringUTFChars(jname, name);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_nativeInit(
    JNIEnv* env,
    jobject obj,
    jlong jSoftHeapLimit
) {
    sqlite3_initialize();
    sqlite3_soft_heap_limit64(jSoftHeapLimit);
    LOG_D("SQLite3 has soft heap limit %llu bytes.", sqlite3_soft_heap_limit64(-1));
    LOG_D("SQLite3 has hard heap limit %llu bytes.", sqlite3_hard_heap_limit64(-1));
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
JNIEXPORT jint
JNI_OnLoad(
    JavaVM* vm,
    void* reserved
) {
#pragma clang diagnostic pop
    JNIEnv* env;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    initThrowableClasses(env);
    return JNI_VERSION_1_6;
}
