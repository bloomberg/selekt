/*
 * Copyright 2026 Bloomberg Finance L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 */

#include <sqlite3/sqlite3.h>

#include <errno.h>
#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#ifndef SELEKT_BENCHMARK_OPTIMIZATION
#define SELEKT_BENCHMARK_OPTIMIZATION "unknown"
#endif
#ifndef SELEKT_BENCHMARK_PGO
#define SELEKT_BENCHMARK_PGO "unknown"
#endif
#ifndef SELEKT_BENCHMARK_THINLTO
#define SELEKT_BENCHMARK_THINLTO 0
#endif

#define DEFAULT_ROWS 20000
#define DEFAULT_SAMPLES 7
#define POINT_OPERATIONS 50000
#define SCAN_REPETITIONS 8
#define SORT_REPETITIONS 20
#define VECTOR_OPERATIONS 30000
#define REPRESENTATIVE_WRITE_TRANSACTIONS 50
#define REPRESENTATIVE_RECORDS_PER_BATCH 10
#define REPRESENTATIVE_GROUPS_PER_BATCH 5
#define REPRESENTATIVE_JOIN_REPETITIONS 2
#define REPRESENTATIVE_PREFIX_REPETITIONS 2
#define REPRESENTATIVE_IN_LIST_REPETITIONS 100
#define REPRESENTATIVE_UPSERT_OPERATIONS 100
#define REPRESENTATIVE_PGO_WRITE_MULTIPLIER 32
#define REPRESENTATIVE_PGO_UPSERT_MULTIPLIER 12
#define KEY "selekt-android-benchmark-key"

extern int sqlite3_vec1_extra_init(const char *argument);

typedef struct Options {
  const char *directory;
  int rows;
  int samples;
} Options;

static void fail(const char *operation, sqlite3 *database, int result) {
  const char *detail = database ? sqlite3_errmsg(database) : sqlite3_errstr(result);
  fprintf(stderr, "%s failed: %s (%d)\n", operation, detail, result);
  exit(EXIT_FAILURE);
}

static void require_ok(sqlite3 *database, int result, const char *operation) {
  if (result != SQLITE_OK) fail(operation, database, result);
}

static void execute(sqlite3 *database, const char *sql) {
  char *error = NULL;
  int result = sqlite3_exec(database, sql, NULL, NULL, &error);
  if (result != SQLITE_OK) {
    fprintf(stderr, "%s failed: %s (%d)\n", sql, error ? error : sqlite3_errmsg(database), result);
    sqlite3_free(error);
    exit(EXIT_FAILURE);
  }
}

static uint64_t nanoseconds(void) {
  struct timespec value;
#if defined(CLOCK_MONOTONIC_RAW)
  const clockid_t clock_id = CLOCK_MONOTONIC_RAW;
#else
  const clockid_t clock_id = CLOCK_MONOTONIC;
#endif
  if (clock_gettime(clock_id, &value) != 0) {
    perror("clock_gettime");
    exit(EXIT_FAILURE);
  }
  return (uint64_t)value.tv_sec * UINT64_C(1000000000) + (uint64_t)value.tv_nsec;
}

static int compare_u64(const void *left, const void *right) {
  uint64_t a = *(const uint64_t *)left;
  uint64_t b = *(const uint64_t *)right;
  return (a > b) - (a < b);
}

static uint64_t median(uint64_t *samples, int count) {
  qsort(samples, (size_t)count, sizeof(samples[0]), compare_u64);
  if ((count & 1) != 0) return samples[count / 2];
  return samples[count / 2 - 1] / 2 + samples[count / 2] / 2;
}

static void emit_result(
  const char *component,
  const char *benchmark,
  int operations,
  uint64_t *samples,
  int sample_count,
  int dimension
) {
  uint64_t value = median(samples, sample_count);
  printf(
    "SELEKT_NATIVE_BENCHMARK {\"component\":\"%s\",\"benchmark\":\"%s\","
    "\"operations\":%d,\"medianNanoseconds\":%" PRIu64 ","
    "\"nanosecondsPerOperation\":%.3f,\"dimension\":%d}\n",
    component,
    benchmark,
    operations,
    value,
    (double)value / (double)operations,
    dimension
  );
  fflush(stdout);
}

static int parse_positive(const char *text, const char *option) {
  char *end = NULL;
  long value = strtol(text, &end, 10);
  if (end == text || *end != '\0' || value <= 0 || value > 100000000L) {
    fprintf(stderr, "%s requires a positive integer\n", option);
    exit(EXIT_FAILURE);
  }
  return (int)value;
}

static Options parse_options(int argc, char **argv) {
  Options options;
  options.directory = ".";
  options.rows = DEFAULT_ROWS;
  options.samples = DEFAULT_SAMPLES;
  int index = 1;
  while (index < argc) {
    const char *argument = argv[index];
    ++index;
    if (strcmp(argument, "--help") == 0) {
      puts("Usage: selekt_native_benchmark [--directory PATH] [--rows N] [--samples N]");
      exit(EXIT_SUCCESS);
    }
    if (index >= argc) {
      fprintf(stderr, "unknown or incomplete option: %s\n", argument);
      exit(EXIT_FAILURE);
    }
    if (strcmp(argument, "--directory") == 0) {
      options.directory = argv[index];
    } else if (strcmp(argument, "--rows") == 0) {
      options.rows = parse_positive(argv[index], "--rows");
    } else if (strcmp(argument, "--samples") == 0) {
      options.samples = parse_positive(argv[index], "--samples");
    } else {
      fprintf(stderr, "unknown or incomplete option: %s\n", argument);
      exit(EXIT_FAILURE);
    }
    ++index;
  }
  return options;
}

static void join_path(char *output, size_t capacity, const char *directory, const char *name) {
  int length = snprintf(output, capacity, "%s/%s", directory, name);
  if (length < 0 || (size_t)length >= capacity) {
    fprintf(stderr, "benchmark path exceeds %zu bytes\n", capacity - 1);
    exit(EXIT_FAILURE);
  }
}

static void remove_database(const char *path) {
  char sidecar[1024];
  unlink(path);
  const char *suffixes[] = {"-journal", "-wal", "-shm"};
  for (size_t index = 0; index < sizeof(suffixes) / sizeof(suffixes[0]); ++index) {
    int length = snprintf(sidecar, sizeof(sidecar), "%s%s", path, suffixes[index]);
    if (length < 0 || (size_t)length >= sizeof(sidecar)) {
      fputs("benchmark sidecar path is too long\n", stderr);
      exit(EXIT_FAILURE);
    }
    unlink(sidecar);
  }
}

static sqlite3 *open_database(const char *path, int encrypted) {
  sqlite3 *database = NULL;
  int flags = SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE | SQLITE_OPEN_NOMUTEX;
  int result = sqlite3_open_v2(path, &database, flags, NULL);
  if (result != SQLITE_OK) fail("open database", database, result);
  if (encrypted) {
    require_ok(database, sqlite3_key(database, KEY, (int)sizeof(KEY) - 1), "set database key");
  }
  return database;
}

static void populate(sqlite3 *database, int rows) {
  execute(database, "PRAGMA journal_mode=OFF");
  execute(database, "PRAGMA synchronous=OFF");
  execute(database, "CREATE TABLE item(id INTEGER PRIMARY KEY, value TEXT NOT NULL, score REAL NOT NULL)");
  execute(database, "CREATE INDEX item_score ON item(score)");
  sqlite3_stmt *insert = NULL;
  require_ok(
    database,
    sqlite3_prepare_v2(database, "INSERT INTO item VALUES(?1,?2,?3)", -1, &insert, NULL),
    "prepare insert"
  );
  execute(database, "BEGIN IMMEDIATE");
  for (int index = 0; index < rows; ++index) {
    char text[48];
    int length = snprintf(text, sizeof(text), "row-%08d-payload", index);
    require_ok(database, sqlite3_bind_int(insert, 1, index), "bind insert id");
    require_ok(database, sqlite3_bind_text(insert, 2, text, length, SQLITE_TRANSIENT), "bind insert text");
    require_ok(database, sqlite3_bind_double(insert, 3, (double)((index * 7919) % rows)), "bind insert score");
    if (sqlite3_step(insert) != SQLITE_DONE) fail("step insert", database, sqlite3_errcode(database));
    require_ok(database, sqlite3_reset(insert), "reset insert");
    require_ok(database, sqlite3_clear_bindings(insert), "clear insert");
  }
  execute(database, "COMMIT");
  require_ok(database, sqlite3_finalize(insert), "finalize insert");
}

static sqlite3_stmt *prepare_multivalue_insert(
  sqlite3 *database,
  const char *prefix,
  int columns,
  int rows
) {
  size_t capacity = strlen(prefix) + (size_t)rows * ((size_t)columns * 2 + 3) + 1;
  char *sql = malloc(capacity);
  if (!sql) fail("allocate multi-value SQL", NULL, SQLITE_NOMEM);
  size_t length = (size_t)snprintf(sql, capacity, "%s", prefix);
  if (length >= capacity) {
    fputs("multi-value SQL prefix is too long\n", stderr);
    exit(EXIT_FAILURE);
  }
  for (int row = 0; row < rows; ++row) {
    int written = snprintf(sql + length, capacity - length, "%s(", row == 0 ? "" : ",");
    if (written < 0 || (size_t)written >= capacity - length) {
      fputs("multi-value SQL is too long\n", stderr);
      exit(EXIT_FAILURE);
    }
    length += (size_t)written;
    for (int column = 0; column < columns; ++column) {
      written = snprintf(sql + length, capacity - length, "%s?", column == 0 ? "" : ",");
      if (written < 0 || (size_t)written >= capacity - length) {
        fputs("multi-value SQL is too long\n", stderr);
        exit(EXIT_FAILURE);
      }
      length += (size_t)written;
    }
    if (length + 1 >= capacity) {
      fputs("multi-value SQL is too long\n", stderr);
      exit(EXIT_FAILURE);
    }
    sql[length++] = ')';
    sql[length] = '\0';
  }

  sqlite3_stmt *statement = NULL;
  int result = sqlite3_prepare_v2(database, sql, -1, &statement, NULL);
  free(sql);
  require_ok(database, result, "prepare multi-value insert");
  return statement;
}

static void bind_text(sqlite3 *database, sqlite3_stmt *statement, int parameter, const char *value) {
  require_ok(
    database,
    sqlite3_bind_text(statement, parameter, value, -1, SQLITE_TRANSIENT),
    "bind representative text"
  );
}

static void finish_statement(sqlite3 *database, sqlite3_stmt *statement, const char *operation) {
  if (sqlite3_step(statement) != SQLITE_DONE) fail(operation, database, sqlite3_errcode(database));
  require_ok(database, sqlite3_reset(statement), "reset representative statement");
  require_ok(database, sqlite3_clear_bindings(statement), "clear representative bindings");
}

static void bind_representative_record(
  sqlite3 *database,
  sqlite3_stmt *statement,
  int row,
  int record_id
) {
  char record_key[96];
  char category[48];
  char item_name[48];
  char generation[32];
  char payload_name[48];
  int group_number = record_id / 2;
  snprintf(record_key, sizeof(record_key), "https://example.invalid/records/%08d", record_id);
  snprintf(category, sizeof(category), "category-%03d", group_number % 100);
  snprintf(item_name, sizeof(item_name), "item-%08d", group_number);
  snprintf(generation, sizeof(generation), "generation-%d", record_id & 1);
  snprintf(payload_name, sizeof(payload_name), "payload-%08d-%d", group_number, record_id & 1);
  int parameter = row * 6;
  bind_text(database, statement, parameter + 1, record_key);
  bind_text(database, statement, parameter + 2, category);
  bind_text(database, statement, parameter + 3, item_name);
  bind_text(database, statement, parameter + 4, generation);
  bind_text(database, statement, parameter + 5, payload_name);
  require_ok(
    database,
    sqlite3_bind_int64(statement, parameter + 6, INT64_C(1700000000000) + record_id),
    "bind record observation time"
  );
}

static void bind_representative_entity(
  sqlite3 *database,
  sqlite3_stmt *statement,
  int row,
  int record_id
) {
  char entity_key[96];
  char category[48];
  char item_name[48];
  char generation[32];
  int group_number = record_id / 2;
  snprintf(entity_key, sizeof(entity_key), "https://example.invalid/entities/%08d", record_id);
  snprintf(category, sizeof(category), "category-%03d", group_number % 100);
  snprintf(item_name, sizeof(item_name), "item-%08d", group_number);
  snprintf(generation, sizeof(generation), "generation-%d", record_id & 1);
  int parameter = row * 5;
  bind_text(database, statement, parameter + 1, entity_key);
  bind_text(database, statement, parameter + 2, category);
  bind_text(database, statement, parameter + 3, item_name);
  bind_text(database, statement, parameter + 4, generation);
  require_ok(
    database,
    sqlite3_bind_int64(statement, parameter + 5, INT64_C(1700000000000) + record_id),
    "bind entity observation time"
  );
}

static void bind_representative_group(
  sqlite3 *database,
  sqlite3_stmt *statement,
  int row,
  int group_number
) {
  char group_key[96];
  char category[48];
  char item_name[48];
  snprintf(group_key, sizeof(group_key), "https://example.invalid/groups/%08d", group_number);
  snprintf(category, sizeof(category), "category-%03d", group_number % 100);
  snprintf(item_name, sizeof(item_name), "item-%08d", group_number);
  int parameter = row * 4;
  bind_text(database, statement, parameter + 1, group_key);
  bind_text(database, statement, parameter + 2, category);
  bind_text(database, statement, parameter + 3, item_name);
  require_ok(
    database,
    sqlite3_bind_int64(statement, parameter + 4, INT64_C(1700000000000) + group_number),
    "bind group observation time"
  );
}

static uint64_t run_representative_writes(sqlite3 *database, int first_transaction) {
  sqlite3_stmt *records = prepare_multivalue_insert(
    database,
    "INSERT OR IGNORE INTO records"
    "(record_key,category,item_name,generation,payload_name,observed_at) VALUES",
    6,
    REPRESENTATIVE_RECORDS_PER_BATCH
  );
  sqlite3_stmt *entities = prepare_multivalue_insert(
    database,
    "INSERT OR IGNORE INTO entities"
    "(entity_key,category,item_name,generation,observed_at) VALUES",
    5,
    REPRESENTATIVE_RECORDS_PER_BATCH
  );
  sqlite3_stmt *groups = prepare_multivalue_insert(
    database,
    "INSERT OR IGNORE INTO groups(group_key,category,item_name,observed_at) VALUES",
    4,
    REPRESENTATIVE_GROUPS_PER_BATCH
  );

  uint64_t start = nanoseconds();
  for (int transaction = 0; transaction < REPRESENTATIVE_WRITE_TRANSACTIONS; ++transaction) {
    int absolute_transaction = first_transaction + transaction;
    int source_transaction = absolute_transaction % 4 == 3 ? absolute_transaction - 1 : absolute_transaction;
    int first_record = source_transaction * REPRESENTATIVE_RECORDS_PER_BATCH;
    execute(database, "BEGIN IMMEDIATE");
    for (int row = 0; row < REPRESENTATIVE_RECORDS_PER_BATCH; ++row) {
      bind_representative_record(database, records, row, first_record + row);
      bind_representative_entity(database, entities, row, first_record + row);
    }
    for (int row = 0; row < REPRESENTATIVE_GROUPS_PER_BATCH; ++row) {
      bind_representative_group(database, groups, row, first_record / 2 + row);
    }
    finish_statement(database, records, "insert representative records");
    finish_statement(database, entities, "insert representative entities");
    finish_statement(database, groups, "insert representative groups");
    execute(database, "COMMIT");
  }
  uint64_t elapsed = nanoseconds() - start;
  require_ok(database, sqlite3_finalize(groups), "finalize representative group insert");
  require_ok(database, sqlite3_finalize(entities), "finalize representative entity insert");
  require_ok(database, sqlite3_finalize(records), "finalize representative record insert");
  return elapsed;
}

static uint64_t run_representative_join(sqlite3 *database) {
  const char *sql =
    "SELECT r.category,r.item_name,GROUP_CONCAT(DISTINCT r.generation) "
    "FROM records r LEFT JOIN groups g "
    "ON r.category=g.category AND r.item_name=g.item_name AND g.group_key GLOB ?1 "
    "WHERE r.record_key GLOB ?2 GROUP BY r.category,r.item_name "
    "ORDER BY g.observed_at DESC";
  sqlite3_stmt *statement = NULL;
  require_ok(database, sqlite3_prepare_v2(database, sql, -1, &statement, NULL), "prepare representative join");
  bind_text(database, statement, 1, "https://example.invalid/*");
  bind_text(database, statement, 2, "https://example.invalid/*");
  int visited = 0;
  uint64_t checksum = 0;
  uint64_t start = nanoseconds();
  for (int repetition = 0; repetition < REPRESENTATIVE_JOIN_REPETITIONS; ++repetition) {
    int result;
    while ((result = sqlite3_step(statement)) == SQLITE_ROW) {
      checksum += (uint64_t)sqlite3_column_bytes(statement, 0);
      checksum += (uint64_t)sqlite3_column_bytes(statement, 1);
      checksum += (uint64_t)sqlite3_column_bytes(statement, 2);
      ++visited;
    }
    if (result != SQLITE_DONE) fail("step representative join", database, result);
    require_ok(database, sqlite3_reset(statement), "reset representative join");
  }
  uint64_t elapsed = nanoseconds() - start;
  if (visited == 0 || checksum == 0) {
    fputs("representative join correctness check failed\n", stderr);
    exit(EXIT_FAILURE);
  }
  require_ok(database, sqlite3_finalize(statement), "finalize representative join");
  return elapsed;
}

static uint64_t run_representative_prefix_reads(sqlite3 *database) {
  sqlite3_stmt *count = NULL;
  sqlite3_stmt *distinct = NULL;
  require_ok(
    database,
    sqlite3_prepare_v2(database, "SELECT count(*) FROM records WHERE record_key GLOB ?1", -1, &count, NULL),
    "prepare representative prefix count"
  );
  require_ok(
    database,
    sqlite3_prepare_v2(
      database,
      "SELECT DISTINCT category,item_name,generation FROM records WHERE record_key GLOB ?1",
      -1,
      &distinct,
      NULL
    ),
    "prepare representative distinct read"
  );
  bind_text(database, count, 1, "https://example.invalid/*");
  bind_text(database, distinct, 1, "https://example.invalid/*");
  uint64_t checksum = 0;
  uint64_t start = nanoseconds();
  for (int repetition = 0; repetition < REPRESENTATIVE_PREFIX_REPETITIONS; ++repetition) {
    if (sqlite3_step(count) != SQLITE_ROW) fail("step representative prefix count", database, sqlite3_errcode(database));
    checksum += (uint64_t)sqlite3_column_int64(count, 0);
    require_ok(database, sqlite3_reset(count), "reset representative prefix count");
    int result;
    while ((result = sqlite3_step(distinct)) == SQLITE_ROW) {
      checksum += (uint64_t)sqlite3_column_bytes(distinct, 0);
      checksum += (uint64_t)sqlite3_column_bytes(distinct, 1);
      checksum += (uint64_t)sqlite3_column_bytes(distinct, 2);
    }
    if (result != SQLITE_DONE) fail("step representative distinct read", database, result);
    require_ok(database, sqlite3_reset(distinct), "reset representative distinct read");
  }
  uint64_t elapsed = nanoseconds() - start;
  if (checksum == 0) {
    fputs("representative prefix-read correctness check failed\n", stderr);
    exit(EXIT_FAILURE);
  }
  require_ok(database, sqlite3_finalize(distinct), "finalize representative distinct read");
  require_ok(database, sqlite3_finalize(count), "finalize representative prefix count");
  return elapsed;
}

static uint64_t run_representative_in_list(sqlite3 *database) {
  sqlite3_stmt *statement = NULL;
  require_ok(
    database,
    sqlite3_prepare_v2(
      database,
      "SELECT record_key FROM records WHERE record_key IN (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10)",
      -1,
      &statement,
      NULL
    ),
    "prepare representative IN-list read"
  );
  for (int index = 0; index < REPRESENTATIVE_RECORDS_PER_BATCH; ++index) {
    char record_key[96];
    snprintf(record_key, sizeof(record_key), "https://example.invalid/records/%08d", index);
    bind_text(database, statement, index + 1, record_key);
  }
  int visited = 0;
  uint64_t checksum = 0;
  uint64_t start = nanoseconds();
  for (int repetition = 0; repetition < REPRESENTATIVE_IN_LIST_REPETITIONS; ++repetition) {
    int result;
    while ((result = sqlite3_step(statement)) == SQLITE_ROW) {
      checksum += (uint64_t)sqlite3_column_bytes(statement, 0);
      ++visited;
    }
    if (result != SQLITE_DONE) fail("step representative IN-list read", database, result);
    require_ok(database, sqlite3_reset(statement), "reset representative IN-list read");
  }
  uint64_t elapsed = nanoseconds() - start;
  if (visited != REPRESENTATIVE_IN_LIST_REPETITIONS * REPRESENTATIVE_RECORDS_PER_BATCH || checksum == 0) {
    fputs("representative IN-list correctness check failed\n", stderr);
    exit(EXIT_FAILURE);
  }
  require_ok(database, sqlite3_finalize(statement), "finalize representative IN-list read");
  return elapsed;
}

static uint64_t run_representative_upserts(sqlite3 *database) {
  sqlite3_stmt *statement = NULL;
  require_ok(
    database,
    sqlite3_prepare_v2(
      database,
      "INSERT INTO digests(resource_key,category,item_name,digest) VALUES(?1,?2,?3,?4) "
      "ON CONFLICT(resource_key) DO UPDATE SET digest=excluded.digest",
      -1,
      &statement,
      NULL
    ),
    "prepare representative upsert"
  );
  uint64_t start = nanoseconds();
  for (int operation = 0; operation < REPRESENTATIVE_UPSERT_OPERATIONS; ++operation) {
    char resource_key[96];
    char item_name[48];
    char digest[48];
    int key = operation % 32;
    snprintf(resource_key, sizeof(resource_key), "https://example.invalid/resources/%08d", key);
    snprintf(item_name, sizeof(item_name), "item-%08d", key);
    snprintf(digest, sizeof(digest), "%040x", operation + 1);
    bind_text(database, statement, 1, resource_key);
    bind_text(database, statement, 2, "digest-category");
    bind_text(database, statement, 3, item_name);
    bind_text(database, statement, 4, digest);
    finish_statement(database, statement, "execute representative upsert");
  }
  uint64_t elapsed = nanoseconds() - start;
  require_ok(database, sqlite3_finalize(statement), "finalize representative upsert");
  return elapsed;
}

static void benchmark_representative_patterns(const char *path, const Options *options) {
  sqlite3 *database = open_database(path, 0);
  execute(database, "PRAGMA page_size=32768");
  execute(database, "PRAGMA journal_mode=WAL");
  execute(database, "PRAGMA synchronous=NORMAL");
  execute(database, "PRAGMA mmap_size=8589934592");
  execute(database, "CREATE TABLE records("
                    "record_key TEXT PRIMARY KEY,category TEXT NOT NULL,item_name TEXT NOT NULL,"
                    "generation TEXT NOT NULL,payload_name TEXT NOT NULL,observed_at INTEGER NOT NULL)");
  execute(database, "CREATE INDEX records_key_category_item_generation "
                    "ON records(record_key,category,item_name,generation)");
  execute(database, "CREATE TABLE entities("
                    "entity_key TEXT PRIMARY KEY,category TEXT NOT NULL,item_name TEXT NOT NULL,"
                    "generation TEXT NOT NULL,observed_at INTEGER NOT NULL)");
  execute(database, "CREATE TABLE groups("
                    "group_key TEXT PRIMARY KEY,category TEXT NOT NULL,item_name TEXT NOT NULL,"
                    "observed_at INTEGER NOT NULL)");
  execute(database, "CREATE TABLE digests("
                    "resource_key TEXT PRIMARY KEY,category TEXT NOT NULL,item_name TEXT NOT NULL,digest TEXT NOT NULL)");

  uint64_t *samples = calloc((size_t)options->samples, sizeof(*samples));
  if (!samples) fail("allocate representative samples", NULL, SQLITE_NOMEM);
  int next_transaction = 0;
  (void)run_representative_writes(database, next_transaction);
  next_transaction += REPRESENTATIVE_WRITE_TRANSACTIONS;
  for (int index = 0; index < options->samples; ++index) {
    samples[index] = run_representative_writes(database, next_transaction);
    next_transaction += REPRESENTATIVE_WRITE_TRANSACTIONS;
  }
  emit_result(
    "sqlite",
    "wal_batch_write",
    REPRESENTATIVE_WRITE_TRANSACTIONS,
    samples,
    options->samples,
    0
  );

  (void)run_representative_join(database);
  for (int index = 0; index < options->samples; ++index) samples[index] = run_representative_join(database);
  emit_result("sqlite", "join_aggregate", REPRESENTATIVE_JOIN_REPETITIONS, samples, options->samples, 0);

  (void)run_representative_prefix_reads(database);
  for (int index = 0; index < options->samples; ++index) samples[index] = run_representative_prefix_reads(database);
  emit_result(
    "sqlite",
    "glob_distinct",
    REPRESENTATIVE_PREFIX_REPETITIONS * 2,
    samples,
    options->samples,
    0
  );

  (void)run_representative_in_list(database);
  for (int index = 0; index < options->samples; ++index) samples[index] = run_representative_in_list(database);
  emit_result(
    "sqlite",
    "in_list",
    REPRESENTATIVE_IN_LIST_REPETITIONS,
    samples,
    options->samples,
    0
  );

  (void)run_representative_upserts(database);
  for (int index = 0; index < options->samples; ++index) samples[index] = run_representative_upserts(database);
  emit_result(
    "sqlite",
    "upsert_conflict",
    REPRESENTATIVE_UPSERT_OPERATIONS,
    samples,
    options->samples,
    0
  );
  free(samples);

  if (strcmp(SELEKT_BENCHMARK_PGO, "GENERATE") == 0) {
    for (int multiplier = 1; multiplier < REPRESENTATIVE_PGO_WRITE_MULTIPLIER; ++multiplier) {
      for (int run = 0; run <= options->samples; ++run) {
        (void)run_representative_writes(database, next_transaction);
        next_transaction += REPRESENTATIVE_WRITE_TRANSACTIONS;
      }
    }
    for (int multiplier = 1; multiplier < REPRESENTATIVE_PGO_UPSERT_MULTIPLIER; ++multiplier) {
      for (int run = 0; run <= options->samples; ++run) {
        (void)run_representative_upserts(database);
      }
    }
  }

  execute(database, "PRAGMA wal_checkpoint(TRUNCATE)");
  require_ok(database, sqlite3_close(database), "close representative database");
}

static uint64_t run_point_reads(sqlite3 *database, int rows) {
  sqlite3_stmt *statement = NULL;
  require_ok(
    database,
    sqlite3_prepare_v2(database, "SELECT value, score FROM item WHERE id=?1", -1, &statement, NULL),
    "prepare point read"
  );
  uint64_t checksum = 0;
  uint32_t state = UINT32_C(0x12345678);
  uint64_t start = nanoseconds();
  for (int index = 0; index < POINT_OPERATIONS; ++index) {
    state = state * UINT32_C(1664525) + UINT32_C(1013904223);
    require_ok(database, sqlite3_bind_int(statement, 1, (int)(state % (uint32_t)rows)), "bind point id");
    if (sqlite3_step(statement) != SQLITE_ROW) fail("step point read", database, sqlite3_errcode(database));
    checksum += (uint64_t)sqlite3_column_bytes(statement, 0);
    checksum += (uint64_t)sqlite3_column_int64(statement, 1);
    require_ok(database, sqlite3_reset(statement), "reset point read");
    require_ok(database, sqlite3_clear_bindings(statement), "clear point read");
  }
  uint64_t elapsed = nanoseconds() - start;
  if (checksum == 0) {
    fputs("point-read checksum failed\n", stderr);
    exit(EXIT_FAILURE);
  }
  require_ok(database, sqlite3_finalize(statement), "finalize point read");
  return elapsed;
}

static uint64_t run_scan(sqlite3 *database, int rows) {
  sqlite3_stmt *statement = NULL;
  require_ok(
    database,
    sqlite3_prepare_v2(database, "SELECT id,value,score FROM item NOT INDEXED", -1, &statement, NULL),
    "prepare scan"
  );
  uint64_t checksum = 0;
  int visited = 0;
  uint64_t start = nanoseconds();
  for (int repetition = 0; repetition < SCAN_REPETITIONS; ++repetition) {
    int result;
    while ((result = sqlite3_step(statement)) == SQLITE_ROW) {
      checksum += (uint64_t)sqlite3_column_int64(statement, 0);
      checksum += (uint64_t)sqlite3_column_bytes(statement, 1);
      checksum += (uint64_t)sqlite3_column_int64(statement, 2);
      ++visited;
    }
    if (result != SQLITE_DONE) fail("step scan", database, result);
    require_ok(database, sqlite3_reset(statement), "reset scan");
  }
  uint64_t elapsed = nanoseconds() - start;
  if (visited != rows * SCAN_REPETITIONS || checksum == 0) {
    fputs("scan correctness check failed\n", stderr);
    exit(EXIT_FAILURE);
  }
  require_ok(database, sqlite3_finalize(statement), "finalize scan");
  return elapsed;
}

static uint64_t run_sort(sqlite3 *database) {
  sqlite3_stmt *statement = NULL;
  require_ok(
    database,
    sqlite3_prepare_v2(
      database,
      "SELECT id FROM item NOT INDEXED ORDER BY (score * 1.000001) DESC, value LIMIT 1000",
      -1,
      &statement,
      NULL
    ),
    "prepare sort"
  );
  uint64_t checksum = 0;
  int visited = 0;
  uint64_t start = nanoseconds();
  for (int repetition = 0; repetition < SORT_REPETITIONS; ++repetition) {
    int result;
    while ((result = sqlite3_step(statement)) == SQLITE_ROW) {
      checksum += (uint64_t)sqlite3_column_int64(statement, 0);
      ++visited;
    }
    if (result != SQLITE_DONE) fail("step sort", database, result);
    require_ok(database, sqlite3_reset(statement), "reset sort");
  }
  uint64_t elapsed = nanoseconds() - start;
  if (visited != 1000 * SORT_REPETITIONS || checksum == 0) {
    fputs("sort correctness check failed\n", stderr);
    exit(EXIT_FAILURE);
  }
  require_ok(database, sqlite3_finalize(statement), "finalize sort");
  return elapsed;
}

static void benchmark_sqlite_core(sqlite3 *database, const Options *options) {
  uint64_t *samples = calloc((size_t)options->samples, sizeof(*samples));
  if (!samples) fail("allocate samples", NULL, SQLITE_NOMEM);
  (void)run_point_reads(database, options->rows);
  for (int index = 0; index < options->samples; ++index) samples[index] = run_point_reads(database, options->rows);
  emit_result("sqlite", "point_read", POINT_OPERATIONS, samples, options->samples, 0);

  (void)run_scan(database, options->rows);
  for (int index = 0; index < options->samples; ++index) samples[index] = run_scan(database, options->rows);
  emit_result("sqlite", "full_scan", options->rows * SCAN_REPETITIONS, samples, options->samples, 0);

  (void)run_sort(database);
  for (int index = 0; index < options->samples; ++index) samples[index] = run_sort(database);
  emit_result("sqlite", "sort_limit", 1000 * SORT_REPETITIONS, samples, options->samples, 0);
  free(samples);
}

static void benchmark_file_scan(
  const char *component,
  const char *path,
  int encrypted,
  const Options *options
) {
  sqlite3 *database = open_database(path, encrypted);
  execute(database, "PRAGMA mmap_size=0");
  execute(database, "PRAGMA cache_size=64");
  execute(database, "PRAGMA query_only=ON");
  uint64_t *samples = calloc((size_t)options->samples, sizeof(*samples));
  if (!samples) fail("allocate samples", NULL, SQLITE_NOMEM);
  (void)run_scan(database, options->rows);
  for (int index = 0; index < options->samples; ++index) samples[index] = run_scan(database, options->rows);
  emit_result(component, "page_scan", options->rows * SCAN_REPETITIONS, samples, options->samples, 0);
  free(samples);
  require_ok(database, sqlite3_close(database), "close scan database");
}

static void benchmark_cipher_open(const char *path, const Options *options) {
  uint64_t *samples = calloc((size_t)options->samples, sizeof(*samples));
  if (!samples) fail("allocate samples", NULL, SQLITE_NOMEM);
  for (int index = -1; index < options->samples; ++index) {
    uint64_t start = nanoseconds();
    sqlite3 *database = open_database(path, 1);
    sqlite3_stmt *statement = NULL;
    require_ok(database, sqlite3_prepare_v2(database, "SELECT count(*) FROM item", -1, &statement, NULL), "prepare open check");
    if (sqlite3_step(statement) != SQLITE_ROW || sqlite3_column_int(statement, 0) != options->rows) {
      fail("cipher open check", database, sqlite3_errcode(database));
    }
    require_ok(database, sqlite3_finalize(statement), "finalize open check");
    require_ok(database, sqlite3_close(database), "close cipher open");
    if (index >= 0) samples[index] = nanoseconds() - start;
  }
  emit_result("sqlcipher", "open_and_kdf", 1, samples, options->samples, 0);
  free(samples);
}

static void fill_vector(float *vector, int dimension, uint32_t seed) {
  uint32_t state = seed;
  for (int index = 0; index < dimension; ++index) {
    state = state * UINT32_C(1664525) + UINT32_C(1013904223);
    vector[index] = (float)(state & UINT32_C(0xffff)) / 65535.0f;
  }
}

static uint64_t run_vec1(sqlite3 *database, int dimension) {
  float *left = malloc((size_t)dimension * sizeof(*left));
  float *right = malloc((size_t)dimension * sizeof(*right));
  if (!left || !right) fail("allocate vectors", NULL, SQLITE_NOMEM);
  fill_vector(left, dimension, UINT32_C(0x10203040));
  fill_vector(right, dimension, UINT32_C(0x50607080));
  sqlite3_stmt *statement = NULL;
  require_ok(
    database,
    sqlite3_prepare_v2(database, "SELECT vec1_l2_distance(?1,?2)", -1, &statement, NULL),
    "prepare vec1 distance"
  );
  require_ok(
    database,
    sqlite3_bind_blob(statement, 1, left, dimension * (int)sizeof(*left), SQLITE_STATIC),
    "bind left vector"
  );
  require_ok(
    database,
    sqlite3_bind_blob(statement, 2, right, dimension * (int)sizeof(*right), SQLITE_STATIC),
    "bind right vector"
  );
  double checksum = 0.0;
  uint64_t start = nanoseconds();
  for (int index = 0; index < VECTOR_OPERATIONS; ++index) {
    if (sqlite3_step(statement) != SQLITE_ROW) fail("step vec1 distance", database, sqlite3_errcode(database));
    checksum += sqlite3_column_double(statement, 0);
    require_ok(database, sqlite3_reset(statement), "reset vec1 distance");
  }
  uint64_t elapsed = nanoseconds() - start;
  if (checksum <= 0.0) {
    fputs("vec1 checksum failed\n", stderr);
    exit(EXIT_FAILURE);
  }
  require_ok(database, sqlite3_finalize(statement), "finalize vec1 distance");
  free(right);
  free(left);
  return elapsed;
}

static void benchmark_vec1(sqlite3 *database, const Options *options) {
  const int dimensions[] = {128, 384, 768};
  uint64_t *samples = calloc((size_t)options->samples, sizeof(*samples));
  if (!samples) fail("allocate samples", NULL, SQLITE_NOMEM);
  for (size_t dimension_index = 0; dimension_index < sizeof(dimensions) / sizeof(dimensions[0]); ++dimension_index) {
    int dimension = dimensions[dimension_index];
    (void)run_vec1(database, dimension);
    for (int index = 0; index < options->samples; ++index) samples[index] = run_vec1(database, dimension);
    emit_result("vec1", "l2_distance", VECTOR_OPERATIONS, samples, options->samples, dimension);
  }
  free(samples);
}

int main(int argc, char **argv) {
  Options options = parse_options(argc, argv);
  char working_directory[1024];
  char plaintext_path[1024];
  char cipher_path[1024];
  char representative_path[1024];
  join_path(working_directory, sizeof(working_directory), options.directory, "selekt-benchmark-XXXXXX");
  if (!mkdtemp(working_directory)) {
    perror("create private benchmark directory");
    return EXIT_FAILURE;
  }
  join_path(plaintext_path, sizeof(plaintext_path), working_directory, "plain.db");
  join_path(cipher_path, sizeof(cipher_path), working_directory, "cipher.db");
  join_path(representative_path, sizeof(representative_path), working_directory, "representative.db");

  require_ok(NULL, sqlite3_initialize(), "initialize SQLite");
  require_ok(NULL, sqlite3_vec1_extra_init(NULL), "register vec1");
  printf(
    "SELEKT_NATIVE_BENCHMARK_CONFIG {\"optimization\":\"%s\",\"pgo\":\"%s\","
    "\"thinLto\":%s,\"rows\":%d,\"samples\":%d,\"sqliteVersion\":\"%s\"}\n",
    SELEKT_BENCHMARK_OPTIMIZATION,
    SELEKT_BENCHMARK_PGO,
    SELEKT_BENCHMARK_THINLTO ? "true" : "false",
    options.rows,
    options.samples,
    sqlite3_libversion()
  );

  sqlite3 *memory = open_database(":memory:", 0);
  populate(memory, options.rows);
  benchmark_sqlite_core(memory, &options);
  benchmark_vec1(memory, &options);
  require_ok(memory, sqlite3_close(memory), "close memory database");

  sqlite3 *plaintext = open_database(plaintext_path, 0);
  populate(plaintext, options.rows);
  require_ok(plaintext, sqlite3_close(plaintext), "close plaintext setup");
  sqlite3 *cipher = open_database(cipher_path, 1);
  populate(cipher, options.rows);
  require_ok(cipher, sqlite3_close(cipher), "close cipher setup");

  benchmark_file_scan("sqlite", plaintext_path, 0, &options);
  benchmark_file_scan("sqlcipher", cipher_path, 1, &options);
  benchmark_cipher_open(cipher_path, &options);
  benchmark_representative_patterns(representative_path, &options);

  remove_database(plaintext_path);
  remove_database(cipher_path);
  remove_database(representative_path);
  require_ok(NULL, sqlite3_shutdown(), "shutdown SQLite");
  if (rmdir(working_directory) != 0) {
    perror("remove private benchmark directory");
    return EXIT_FAILURE;
  }
  return EXIT_SUCCESS;
}
