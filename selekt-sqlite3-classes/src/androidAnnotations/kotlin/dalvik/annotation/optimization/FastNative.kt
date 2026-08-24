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

package dalvik.annotation.optimization

// ART's dalvik.annotation.optimization.FastNative is a hidden libcore API: it is absent from the
// public Android SDK, so it cannot be imported here. ART's JNI linker recognises the annotation by
// its binary type descriptor alone when resolving a native method's calling convention, not by
// class identity, so redeclaring it under the identical fully-qualified name, retention, and
// target (matched against AOSP's libcore/dalvik/src/main/java/dalvik/annotation/optimization/
// FastNative.java) lets consumers opt selected native methods into ART's fast JNI transition
// without depending on platform internals. Only ever consumed as java17CompileOnly: with CLASS
// retention the annotation is recorded as a type reference in the annotated method's bytecode,
// never packaged into a jar or dex on its own.
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class FastNative
