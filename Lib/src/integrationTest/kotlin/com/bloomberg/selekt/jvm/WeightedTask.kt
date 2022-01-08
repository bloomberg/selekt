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

package com.bloomberg.selekt.jvm

import com.bloomberg.selekt.SQLDatabase
import java.util.concurrent.ThreadLocalRandom

internal data class WeightedTask(val weight: Int, val block: suspend SQLDatabase.() -> Unit)

internal fun List<WeightedTask>.randomTask(): suspend (SQLDatabase) -> Unit {
    var total = sumOf { it.weight }
    val x = ThreadLocalRandom.current().nextInt(0, total + 1)
    asReversed().forEach {
        total -= it.weight
        if (x >= total) {
            return it.block
        }
    }
    error("Failed to select a task.")
}
