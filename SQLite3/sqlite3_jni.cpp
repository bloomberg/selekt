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

#include <jni.h>
#include <sqlite3/sqlite3.h>
#include <string>
#include <cstring>
#include <bloomberg/AutoJByteArray.h>
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
    AutoJByteArray key(env, jkey, keyLength);
    std::string prefix = "PRAGMA key=\"x'";
    std::string suffix = "'\"";
    size_t const length = prefix.length() + (2 * keyLength) + suffix.length();
    char sql[length + 1];
    sql[length] = 0;
    std::strcpy(sql, prefix.c_str());
    int i;
    char * const hex = sql + prefix.length();
    for (i = 0; i < keyLength; ++i) {
        std::snprintf(hex + (2 * i), 3, "%02x", key[i]);
    }
    std::strcpy(sql + length - suffix.length(), suffix.c_str());
    auto result = sqlite3_exec(reinterpret_cast<sqlite3*>(jdb), sql, nullptr, nullptr, nullptr);
    std::fill(sql, sql + length + 1, 0);
    return result;
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
    AutoJByteArray value(env, jvalue, length);
    return sqlite3_bind_blob(statement, index, value, value.length(), SQLITE_TRANSIENT);
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
Java_com_bloomberg_selekt_ExternalSQLite_bindText(
    JNIEnv* env,
    jobject obj,
    jlong jstatement,
    jint index,
    jstring jvalue
) {
    auto statement = reinterpret_cast<sqlite3_stmt*>(jstatement);
    auto value = env->GetStringUTFChars(jvalue, nullptr);
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
    sqlite3_blob* blob;
    auto name = env->GetStringUTFChars(jname, nullptr);
    auto table = env->GetStringUTFChars(jtable, nullptr);
    auto column = env->GetStringUTFChars(jcolumn, nullptr);
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
    updateHolder(env, jholder, 0, blob);
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
    auto source = env->GetByteArrayElements(jDestination, nullptr);
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
    auto source = env->GetByteArrayElements(jSource, nullptr);
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
    return env->NewStringUTF(sqlite3_column_name(statement, index));
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
    return env->NewStringUTF(text);
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
    updateHolder(env, holder, 0, &current);
    updateHolder(env, holder, 1, &highWater);
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
    return env->NewStringUTF(sqlite3_expanded_sql(reinterpret_cast<sqlite3_stmt*>(jstatement)));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_exec(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jstring jquery
) {
    auto query = env->GetStringUTFChars(jquery, nullptr);
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

extern "C" JNIEXPORT jint JNICALL
Java_com_bloomberg_selekt_ExternalSQLite_key(
    JNIEnv* env,
    jobject jobj,
    jlong jdb,
    jbyteArray jkey,
    jint length
) {
    AutoJByteArray key(env, jkey, length);
    return sqlite3_key(reinterpret_cast<sqlite3*>(jdb), key, key.length());
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
    auto result = sqlite3_prepare_v2(reinterpret_cast<sqlite3*>(jdb), sql, jlength, &statement, nullptr);
    env->ReleaseStringUTFChars(jsql, sql);
    updateHolder(env, statementHolder, 0, statement);
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
    AutoJByteArray key(env, jkey, length);
    if (key.length() == 0) {
        return sqlite3_rekey(reinterpret_cast<sqlite3*>(jdb), nullptr, key.length());
    }
    return sqlite3_rekey(reinterpret_cast<sqlite3*>(jdb), key, key.length());
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
    return JNI_VERSION_1_6;
}
