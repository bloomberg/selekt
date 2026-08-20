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

package com.bloomberg.selekt.jdbc.driver

import com.bloomberg.selekt.commons.zero
import java.nio.CharBuffer
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_ALGORITHM = "HmacSHA256"
private const val SALT_LENGTH_BYTES = 32
private const val CACHE_KEY_HASH_LENGTH_BYTES = 16

private val cacheKeyHashSalt = ByteArray(SALT_LENGTH_BYTES).also(SecureRandom()::nextBytes)

internal fun hashKeyChars(keyChars: CharArray): String = Charsets.UTF_8.encode(CharBuffer.wrap(keyChars)).let {
    ByteArray(it.remaining()).also(it::get)
}.run {
    try {
        Mac.getInstance(HMAC_ALGORITHM).apply {
            init(SecretKeySpec(cacheKeyHashSalt, HMAC_ALGORITHM))
        }.doFinal(this)
            .copyOf(CACHE_KEY_HASH_LENGTH_BYTES)
            .joinToString("") { "%02x".format(it) }
    } finally {
        zero()
    }
}
