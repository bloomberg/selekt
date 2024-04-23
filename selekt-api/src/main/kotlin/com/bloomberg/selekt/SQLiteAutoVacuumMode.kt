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

enum class SQLiteAutoVacuumMode(private val value: Int) {
    /**
     * Auto-vacuum is disabled. When auto-vacuum is disabled and data is deleted data from a database, the database file
     * remains the same size. Unused database file pages are added to a "freelist" and reused for subsequent inserts. So no
     * database file space is lost. However, the database file does not shrink. In this mode vacuum can be used to rebuild
     * the entire database file and thus reclaim unused disk space.
     */
    NONE(0),

    /**
     * The freelist pages are moved to the end of the database file and the database file is truncated to remove the
     * freelist pages at every transaction commit. Note, however, that auto-vacuum only truncates the freelist pages from
     * the file. Auto-vacuum does not defragment the database nor repack individual database pages the way that vacuum does.
     * In fact, because it moves pages around within the file, auto-vacuum can actually make fragmentation worse.
     */
    FULL(1),

    /**
     * The additional information needed to do auto-vacuuming is stored in the database file but auto-vacuuming does not
     * occur automatically at each commit as it does with "full". In incremental mode, the separate incremental vacuum
     * pragma must be invoked to cause the auto-vacuum to occur.
     */
    INCREMENTAL(2);

    operator fun invoke() = value
}
