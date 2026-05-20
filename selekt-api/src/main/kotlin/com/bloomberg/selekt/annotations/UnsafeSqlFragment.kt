/*
 * Copyright 2026 Bloomberg Finance L.P.
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

package com.bloomberg.selekt.annotations

/**
 * Marks a parameter, property, or function that accepts a raw SQL fragment which is concatenated
 * verbatim into a final SQL string. Such fragments are **not** validated, escaped, or
 * parameterised by Selekt and are therefore a SQL-injection vector if their value is derived,
 * directly or indirectly, from untrusted input.
 *
 * Callers MUST treat values supplied to `@UnsafeSqlFragment` sinks as if they were source code:
 * - prefer constants defined at compile time;
 * - never assemble fragments from user input (HTTP parameters, file contents, RPC payloads, …);
 * - if untrusted values must influence the fragment, validate them against a strict allow-list
 *   first, or quote them using a dedicated quoting helper (see `SqlIdentifiers` for identifier
 *   quoting).
 *
 * Typical examples in Selekt's Android-compatible helper APIs include `whereClause`,
 * `groupBy`, `having`, and `orderBy` parameters on `SQLDatabase`/`IDatabase`.
 *
 * This annotation is informational. It is intended to drive code review, KDoc rendering, and
 * future static analysis (e.g. detekt rules). It does not introduce an opt-in requirement so as
 * not to break existing source compatibility.
 *
 * @see RequiresTrustedInput for identifier-level sinks (table names, column names, …).
 * @since 0.32.0
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.TYPE
)
annotation class UnsafeSqlFragment

