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

package com.bloomberg.selekt

typealias SQLCode = Int
typealias SQLDataType = Int

const val SQL_OK: SQLCode = 0
const val SQL_ERROR: SQLCode = 1
const val SQL_ABORT: SQLCode = 4
const val SQL_BUSY: SQLCode = 5
const val SQL_LOCKED: SQLCode = 6
const val SQL_NOMEM: SQLCode = 7
const val SQL_READONLY: SQLCode = 8
const val SQL_IO_ERROR: SQLCode = 10
const val SQL_CORRUPT: SQLCode = 11
const val SQL_NOT_FOUND: SQLCode = 12
const val SQL_FULL: SQLCode = 13
const val SQL_CANT_OPEN: SQLCode = 14
const val SQL_TOO_BIG: SQLCode = 18
const val SQL_CONSTRAINT: SQLCode = 19
const val SQL_MISMATCH: SQLCode = 20
const val SQL_MISUSE: SQLCode = 21
const val SQL_AUTH: SQLCode = 23
const val SQL_RANGE: SQLCode = 25
const val SQL_NOT_A_DATABASE: SQLCode = 26
const val SQL_ROW: SQLCode = 100
const val SQL_DONE: SQLCode = 101

// Extended SQL codes.
// https://www.sqlite.org/rescode.html#extrc
const val SQL_ABORT_ROLLBACK: SQLCode = 516
const val SQL_IO_ERROR_ACCESS: SQLCode = 3338
const val SQL_IO_ERROR_BLOCKED: SQLCode = 2826
const val SQL_IO_ERROR_CHECK_RESERVED_LOCK: SQLCode = 3594
const val SQL_IO_ERROR_CLOSE: SQLCode = 4106
const val SQL_IO_ERROR_CONVPATH: SQLCode = 6666
const val SQL_IO_ERROR_DELETE: SQLCode = 2570
const val SQL_IO_ERROR_DELETE_NO_ENT: SQLCode = 5898
const val SQL_IO_ERROR_DIR_CLOSE: SQLCode = 4362
const val SQL_IO_ERROR_DIR_FSYNC: SQLCode = 1290
const val SQL_IO_ERROR_DIR_FSTAT: SQLCode = 1802
const val SQL_IO_ERROR_FSYNC: SQLCode = 1034
const val SQL_IO_ERROR_GET_TEMP_PATH: SQLCode = 6410
const val SQL_IO_ERROR_LOCK: SQLCode = 3850
const val SQL_IO_ERROR_MMAP: SQLCode = 6154
const val SQL_IO_ERROR_NOMEM: SQLCode = 3082
const val SQL_IO_ERROR_RDLOCK: SQLCode = 2314
const val SQL_IO_ERROR_READ: SQLCode = 266
const val SQL_IO_ERROR_SEEK: SQLCode = 5642
const val SQL_IO_ERROR_SHMLOCK: SQLCode = 5130
const val SQL_IO_ERROR_SHMMAP: SQLCode = 5386
const val SQL_IO_ERROR_SHMOPEN: SQLCode = 4618
const val SQL_IO_ERROR_SHMSIZE: SQLCode = 4874
const val SQL_IO_ERROR_SHORT_READ: SQLCode = 522
const val SQL_IO_ERROR_TRUNCATE: SQLCode = 1546
const val SQL_IO_ERROR_UNLOCK: SQLCode = 2058
const val SQL_IO_ERROR_WRITE: SQLCode = 778
const val SQL_LOCKED_SHARED_CACHE: SQLCode = 262
const val SQL_LOCKED_VTAB: SQLCode = 518
const val SQL_NOTICE_RECOVER_ROLLBACK: SQLCode = 539
const val SQL_NOTICE_RECOVER_WAL: SQLCode = 283
const val SQL_OK_LOAD_PERMANENTLY: SQLCode = 256
const val SQL_READONLY_CANT_INIT: SQLCode = 1288
const val SQL_READONLY_CANT_LOCK: SQLCode = 520
const val SQL_READONLY_DB_MOVED: SQLCode = 1032
const val SQL_READONLY_DIRECTORY: SQLCode = 1544
const val SQL_READONLY_RECOVERY: SQLCode = 264
const val SQL_READONLY_ROLLBACK: SQLCode = 776
const val SQL_WARNING_AUTOINDEX: SQLCode = 284
