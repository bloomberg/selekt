/*
 * Copyright 2020 Bloomberg Finance L.P.
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

#include <jni.h>
#include <sqlite3/sqlite3.h>
#include <string>
#include <cstring>
#include <bloomberg/AutoJByteArray.h>
#include <bloomberg/AutoJString.h>
#include <bloomberg/log.h>
#include <SelektConfig.h>

static void updateHolder(JNIEnv* env, jarray array, int offset, void* p) {
    auto pp = reinterpret_cast<size_t*>(env->GetPrimitiveArrayCritical(array, nullptr));
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
        std::memcpy(buffer, p, static_cast<size_t>(size));
        env->ReleasePrimitiveArrayCritical(array, buffer, 0);
    }
    return array;
}

JNIEXPORT static jstring JNICALL
jni_git_commit(
    JNIEnv* env,
    jobject obj
) {
    return env->NewStringUTF(SELEKT_GIT_COMMIT);
}

JNIEXPORT static jint JNICALL
jni_bind_blob(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jbyteArray jvalue,
    jint length
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    AutoJByteArray value(env, jvalue, length);
    return sqlite3_bind_blob(statement, index, value, value.length(), SQLITE_TRANSIENT);
}

JNIEXPORT static jint JNICALL
jni_bind_double(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jdouble jvalue
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_double(statement, index, jvalue);
}

JNIEXPORT static jint JNICALL
jni_bind_int(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jint jvalue
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_int(statement, index, jvalue);
}

JNIEXPORT static jint JNICALL
jni_bind_int64(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jlong jvalue
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_int64(statement, index, jvalue);
}

JNIEXPORT static jint JNICALL
jni_bind_null(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_null(statement, index);
}

JNIEXPORT static jint JNICALL
jni_bind_parameter_count(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_bind_parameter_count(statement);
}

JNIEXPORT static jint JNICALL
jni_bind_text(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jstring jvalue
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    AutoJString value(env, jvalue);
    return sqlite3_bind_text(
        statement,
        index,
        value,
        value.length8(),
        SQLITE_TRANSIENT
    );
}

JNIEXPORT static jint JNICALL
jni_busy_timeout(
    JNIEnv* env,
    jobject clazz,
    jlong jdb,
    jint millis
) {
    return sqlite3_busy_timeout(reinterpret_cast<sqlite3*>(jdb), millis);
}

JNIEXPORT static jint JNICALL
jni_changes(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3*>(jstatement);
    return sqlite3_changes(statement);
}

JNIEXPORT static jint JNICALL
jni_clear_bindings(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_clear_bindings(statement);
}

JNIEXPORT static jint JNICALL
jni_close_v2(
    JNIEnv* env,
    jobject jobj,
    jlong jdb
) {
    return sqlite3_close_v2(reinterpret_cast<sqlite3*>(jdb));
}

JNIEXPORT static jbyteArray JNICALL
jni_column_blob(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    auto result = sqlite3_column_blob(statement, index);
    auto size = sqlite3_column_bytes(statement, index);
    return newByteArray(env, result, size);
}

JNIEXPORT static jint JNICALL
jni_column_count(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_count(statement);
}

JNIEXPORT static jdouble JNICALL
jni_column_double(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_double(statement, index);
}

JNIEXPORT static jint JNICALL
jni_column_int(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_int(statement, index);
}

JNIEXPORT static jlong JNICALL
jni_column_int64(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_int64(statement, index);
}

JNIEXPORT static jstring JNICALL
jni_column_name(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return env->NewStringUTF(sqlite3_column_name(statement, index));
}

JNIEXPORT static jstring JNICALL
jni_column_text(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    auto text = reinterpret_cast<const char*>(sqlite3_column_text(statement, index));
    return env->NewStringUTF(text);
}

JNIEXPORT static jint JNICALL
jni_column_type(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_column_type(statement, index);
}

JNIEXPORT static jlong JNICALL
jni_column_value(
    JNIEnv* env,
    jobject obj,
    jlong statement,
    jint index
) {
    return reinterpret_cast<jlong>(sqlite3_column_value(reinterpret_cast<sqlite3_stmt*>(statement), index));
}

JNIEXPORT static jlong JNICALL
jni_db_handle(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    return reinterpret_cast<jlong>(sqlite3_db_handle(reinterpret_cast<sqlite3_stmt*>(jstatement)));
}

JNIEXPORT static jint JNICALL
jni_db_readonly(
    JNIEnv* env,
    jobject clazz,
    jlong jdb,
    jstring jName
) {
    AutoJString name(env, jName);
    return sqlite3_db_readonly(reinterpret_cast<sqlite3*>(jdb), name);
}

JNIEXPORT static jint JNICALL
jni_db_status(
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
        reset ? 1 : 0
    );
    updateHolder(env, holder, 0, &current);
    updateHolder(env, holder, 1, &highWater);
    return result;
}

JNIEXPORT static jint JNICALL
jni_errcode(
    JNIEnv* env,
    jobject clazz,
    jlong jdb
) {
    return sqlite3_errcode(reinterpret_cast<sqlite3*>(jdb));
}

JNIEXPORT static jstring JNICALL
jni_errmsg(
    JNIEnv* env,
    jobject clazz,
    jlong jdb
) {
    return env->NewStringUTF(sqlite3_errmsg(reinterpret_cast<sqlite3*>(jdb)));
}

JNIEXPORT static jstring JNICALL
jni_expanded_sql(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    return env->NewStringUTF(sqlite3_expanded_sql(reinterpret_cast<sqlite3_stmt*>(jstatement)));
}

JNIEXPORT static jint JNICALL
jni_exec(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jstring jquery
) {
    AutoJString query(env, jquery);
    return sqlite3_exec(reinterpret_cast<sqlite3*>(jdb), query, nullptr, nullptr, nullptr);
}

JNIEXPORT static jint JNICALL
jni_extended_errcode(
    JNIEnv* env,
    jobject clazz,
    jlong jdb
) {
    return sqlite3_extended_errcode(reinterpret_cast<sqlite3*>(jdb));
}

JNIEXPORT static jint JNICALL
jni_extended_result_codes(
    JNIEnv* env,
    jobject clazz,
    jlong jdb,
    jint onOff
) {
    return sqlite3_extended_result_codes(reinterpret_cast<sqlite3*>(jdb), onOff);
}

JNIEXPORT static jint JNICALL
jni_finalize(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_finalize(statement);
}

JNIEXPORT static jint JNICALL
jni_get_autocommit(
    JNIEnv* env,
    jobject obj,
    jlong jdb
) {
    return sqlite3_get_autocommit(reinterpret_cast<sqlite3*>(jdb));
}

JNIEXPORT static jint JNICALL
jni_key(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jbyteArray jkey,
    jint length
) {
    AutoJByteArray key(env, jkey, length);
    return sqlite3_key(reinterpret_cast<sqlite3*>(jdb), key, key.length());
}

JNIEXPORT static jlong JNICALL
jni_last_insert_rowid(
    JNIEnv* env,
    jobject obj,
    jlong jdb
) {
    return sqlite3_last_insert_rowid(reinterpret_cast<sqlite3*>(jdb));
}

JNIEXPORT static jstring JNICALL
jni_lib_version(
    JNIEnv* env,
    jobject jobj
) {
    return env->NewStringUTF(sqlite3_libversion());
}

JNIEXPORT static jint JNICALL
jni_lib_version_number(
    JNIEnv* env,
    jobject jobj
) {
    return sqlite3_libversion_number();
}

JNIEXPORT static jint JNICALL
jni_open_v2(
    JNIEnv* env,
    jobject jobj,
    jstring jfilename,
    jint jflags,
    jlongArray dbHolder
) {
    AutoJString filename(env, jfilename);
    sqlite3* db = nullptr;
    auto r = sqlite3_open_v2(filename, &db, jflags, nullptr);
    updateHolder(env, dbHolder, 0, db);
    return r;
}

JNIEXPORT static jlong JNICALL
jni_prepare_v2(
    JNIEnv* env,
    jobject obj,
    jlong jdb,
    jstring jsql,
    jint jlength,
    jlongArray statementHolder
) {
    AutoJString sql(env, jsql);
    sqlite3_stmt* statement = nullptr;
    auto r = sqlite3_prepare_v2(reinterpret_cast<sqlite3*>(jdb), sql, jlength, &statement, nullptr);
    updateHolder(env, statementHolder, 0, statement);
    return r;
}

JNIEXPORT static jint JNICALL
jni_rekey(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jbyteArray jkey,
    jint length
) {
    AutoJByteArray key(env, jkey, length);
    if (key.length() == 0) {
        return sqlite3_rekey(reinterpret_cast<sqlite3*>(jdb), nullptr, key.length());
    }
    return sqlite3_rekey(reinterpret_cast<sqlite3*>(jdb), key, key.length());
}   

JNIEXPORT static jint JNICALL
jni_reset(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    return sqlite3_reset(reinterpret_cast<sqlite3_stmt*>(jstatement));
}

JNIEXPORT static jstring JNICALL
jni_sql(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    return env->NewStringUTF(sqlite3_sql(reinterpret_cast<sqlite3_stmt*>(jstatement)));
}

JNIEXPORT static jint JNICALL
jni_step(
    JNIEnv* env,
    jobject obj,
    jlong jstatement
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    return sqlite3_step(statement);
}

JNIEXPORT static jint JNICALL
jni_stmt_readonly(
    JNIEnv* env,
    jobject clazz,
    jlong jstatement
) {
    return sqlite3_stmt_readonly(reinterpret_cast<sqlite3_stmt*>(jstatement));
}

JNIEXPORT static jint JNICALL
jni_stmt_status(
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

JNIEXPORT static jint JNICALL
jni_threadsafe(
    JNIEnv* env,
    jobject obj
) {
    return sqlite3_threadsafe();
}

JNIEXPORT static void JNICALL
jni_trace_v2(
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
                case SQLITE_TRACE_ROW: LOG_D("ROW: %p", p);
                case SQLITE_TRACE_PROFILE: LOG_D("PROFILE: %p %lldns", p, reinterpret_cast<sqlite3_int64>(x));
                case SQLITE_TRACE_STMT: LOG_D("STMT: %p %s", p, reinterpret_cast<const char*>(x));
                case SQLITE_TRACE_CLOSE: LOG_D("CLOSE: %p", p);
                default: break;
            }
            return 0;
        },
        nullptr
    );
}

JNIEXPORT static jlong JNICALL
jni_value_dup(
    JNIEnv* env,
    jobject obj,
    jlong jvalue
) {
    return reinterpret_cast<jlong>(sqlite3_value_dup(reinterpret_cast<sqlite3_value*>(jvalue)));
}

JNIEXPORT static void JNICALL
jni_value_free(
    JNIEnv* env,
    jobject obj,
    jlong jvalue
) {
    sqlite3_value_free(reinterpret_cast<sqlite3_value*>(jvalue));
}

JNIEXPORT static jint JNICALL
jni_value_from_bind(
    JNIEnv* env,
    jobject obj,
    jlong jvalue
) {
    return sqlite3_value_frombind(reinterpret_cast<sqlite3_value*>(jvalue));
}

JNIEXPORT static jint JNICALL
jni_wal_autocheckpoint(
    JNIEnv* env,
    jobject obj,
    jlong jdb,
    jint pages
) {
    return sqlite3_wal_autocheckpoint(reinterpret_cast<sqlite3*>(jdb), pages);
}

JNIEXPORT static jint JNICALL
jni_wal_checkpoint_v2(
    JNIEnv* env,
    jobject obj,
    jlong jdb,
    jstring name,
    jint mode
) {
    if (env->IsSameObject(name, nullptr)) {
        return sqlite3_wal_checkpoint_v2(reinterpret_cast<sqlite3*>(jdb), nullptr, mode, nullptr, nullptr);
    }
    return sqlite3_wal_checkpoint_v2(reinterpret_cast<sqlite3*>(jdb), AutoJString(env, name), mode, nullptr, nullptr);
}

JNIEXPORT static void JNICALL
jni_native_init(
    JNIEnv* env,
    jobject obj
) {
   sqlite3_initialize();
}

static const char* CLASS_PATH = "com/bloomberg/selekt/ExternalSQLite";

static const JNINativeMethod kMethods[] = {
    { const_cast<char *>("bindBlob"), const_cast<char *>("(JI[BI)I"), reinterpret_cast<void*>(jni_bind_blob) },
    { const_cast<char *>("bindDouble"), const_cast<char *>("(JID)I"), reinterpret_cast<void*>(jni_bind_double) },
    { const_cast<char *>("bindInt"), const_cast<char *>("(JII)I"), reinterpret_cast<void*>(jni_bind_int) },
    { const_cast<char *>("bindInt64"), const_cast<char *>("(JIJ)I"), reinterpret_cast<void*>(jni_bind_int64) },
    { const_cast<char *>("bindNull"), const_cast<char *>("(JI)I"), reinterpret_cast<void*>(jni_bind_null) },
    { const_cast<char *>("bindParameterCount"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_bind_parameter_count) },
    { const_cast<char *>("bindText"), const_cast<char *>("(JILjava/lang/String;)I"), reinterpret_cast<void*>(jni_bind_text) },
    { const_cast<char *>("busyTimeout"), const_cast<char *>("(JI)I"), reinterpret_cast<void*>(jni_busy_timeout) },
    { const_cast<char *>("changes"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_changes) },
    { const_cast<char *>("clearBindings"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_clear_bindings) },
    { const_cast<char *>("closeV2"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_close_v2) },
    { const_cast<char *>("columnBlob"), const_cast<char *>("(JI)[B"), reinterpret_cast<void*>(jni_column_blob) },
    { const_cast<char *>("columnCount"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_column_count) },
    { const_cast<char *>("columnDouble"), const_cast<char *>("(JI)D"), reinterpret_cast<void*>(jni_column_double) },
    { const_cast<char *>("columnInt"), const_cast<char *>("(JI)I"), reinterpret_cast<void*>(jni_column_int) },
    { const_cast<char *>("columnInt64"), const_cast<char *>("(JI)J"), reinterpret_cast<void*>(jni_column_int64) },
    { const_cast<char *>("columnName"), const_cast<char *>("(JI)Ljava/lang/String;"), reinterpret_cast<void*>(jni_column_name) },
    { const_cast<char *>("columnText"), const_cast<char *>("(JI)Ljava/lang/String;"), reinterpret_cast<void*>(jni_column_text) },
    { const_cast<char *>("columnType"), const_cast<char *>("(JI)I"), reinterpret_cast<void*>(jni_column_type) },
    { const_cast<char *>("columnValue"), const_cast<char *>("(JI)J"), reinterpret_cast<void*>(jni_column_value) },
    { const_cast<char *>("databaseHandle"), const_cast<char *>("(J)J"), reinterpret_cast<void*>(jni_db_handle) },
    { const_cast<char *>("databaseReadOnly"), const_cast<char *>("(JLjava/lang/String;)I"), reinterpret_cast<void*>(jni_db_readonly) },
    { const_cast<char *>("databaseStatus"), const_cast<char *>("(JIZ[I)I"), reinterpret_cast<void*>(jni_db_status) },
    { const_cast<char *>("errorCode"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_errcode) },
    { const_cast<char *>("errorMessage"), const_cast<char *>("(J)Ljava/lang/String;"), reinterpret_cast<void*>(jni_errmsg) },
    { const_cast<char *>("exec"), const_cast<char *>("(JLjava/lang/String;)I"), reinterpret_cast<void*>(jni_exec) },
    { const_cast<char *>("expandedSql"), const_cast<char *>("(J)Ljava/lang/String;"), reinterpret_cast<void*>(jni_expanded_sql) },
    { const_cast<char *>("extendedErrorCode"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_extended_errcode) },
    { const_cast<char *>("extendedResultCodes"), const_cast<char *>("(JI)I"), reinterpret_cast<void*>(jni_extended_result_codes) },
    { const_cast<char *>("finalize"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_finalize) },
    { const_cast<char *>("getAutocommit"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_get_autocommit) },
    { const_cast<char *>("gitCommit"), const_cast<char *>("()Ljava/lang/String;"), reinterpret_cast<void*>(jni_git_commit) },
    { const_cast<char *>("key"), const_cast<char *>("(J[BI)I"), reinterpret_cast<void*>(jni_key) },
    { const_cast<char *>("lastInsertRowId"), const_cast<char *>("(J)J"), reinterpret_cast<void*>(jni_last_insert_rowid) },
    { const_cast<char *>("libVersion"), const_cast<char *>("()Ljava/lang/String;"), reinterpret_cast<void*>(jni_lib_version) },
    { const_cast<char *>("libVersionNumber"), const_cast<char *>("()I"), reinterpret_cast<void*>(jni_lib_version_number) },
    { const_cast<char *>("nativeInit"), const_cast<char *>("()V"), reinterpret_cast<void*>(jni_native_init) },
    { const_cast<char *>("openV2"), const_cast<char *>("(Ljava/lang/String;I[J)I"), reinterpret_cast<void*>(jni_open_v2) },
    { const_cast<char *>("prepareV2"), const_cast<char *>("(JLjava/lang/String;I[J)I"), reinterpret_cast<void*>(jni_prepare_v2) },
    { const_cast<char *>("rekey"), const_cast<char *>("(J[BI)I"), reinterpret_cast<void*>(jni_rekey) },
    { const_cast<char *>("reset"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_reset) },
    { const_cast<char *>("sql"), const_cast<char *>("(J)Ljava/lang/String;"), reinterpret_cast<void*>(jni_sql) },
    { const_cast<char *>("statementReadOnly"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_stmt_readonly) },
    { const_cast<char *>("statementStatus"), const_cast<char *>("(JIZ)I"), reinterpret_cast<void*>(jni_stmt_status) },
    { const_cast<char *>("step"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_step) },
    { const_cast<char *>("threadsafe"), const_cast<char *>("()I"), reinterpret_cast<void*>(jni_threadsafe) },
    { const_cast<char *>("traceV2"), const_cast<char *>("(JI)V"), reinterpret_cast<void*>(jni_trace_v2) },
    { const_cast<char *>("valueDup"), const_cast<char *>("(J)J"), reinterpret_cast<void*>(jni_value_dup) },
    { const_cast<char *>("valueFree"), const_cast<char *>("(J)V"), reinterpret_cast<void*>(jni_value_free) },
    { const_cast<char *>("valueFromBind"), const_cast<char *>("(J)I"), reinterpret_cast<void*>(jni_value_from_bind) },
    { const_cast<char *>("walAutocheckpoint"), const_cast<char *>("(JI)I"), reinterpret_cast<void*>(jni_wal_autocheckpoint) },
    { const_cast<char *>("walCheckpointV2"), const_cast<char *>("(JLjava/lang/String;I)I"), reinterpret_cast<void*>(jni_wal_checkpoint_v2) }
};
static auto METHOD_COUNT = sizeof(kMethods) / sizeof(kMethods[0]);

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
    jclass clazz = env->FindClass(CLASS_PATH);
    if (clazz == nullptr || env->RegisterNatives(clazz, kMethods, METHOD_COUNT) < 0) {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}
