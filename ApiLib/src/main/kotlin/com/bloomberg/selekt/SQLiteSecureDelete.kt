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

package com.bloomberg.selekt

/**
 * When secure_delete is on, SQLite overwrites deleted content with zeros.
 */
enum class SQLiteSecureDelete {
    /** The "fast" setting is an intermediate setting in between "on" and "off".
     * When secure_delete is set to "fast", SQLite will overwrite deleted content with zeros only if doing so does not
     * increase the amount of I/O. In other words, the "fast" setting uses more CPU cycles but does not use more I/O. This
     * has the effect of purging all old content from b-tree pages, but leaving forensic traces on freelist pages.
     */
    FAST,

    OFF,

    ON
}
