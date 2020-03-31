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

#ifndef SELEKT_BBLOG_H
#define SELEKT_BBLOG_H

#ifdef SELEKT_LOG
#include <android/log.h>
#define LOG_TAG "SLKT"
#define android_printLog(priority, tag, format...) \
    __android_log_print(priority, tag, format)
#define LOG_PRI(priority, tag, ...) \
    android_printLog(priority, tag, __VA_ARGS__)
#define LOG(priority, tag, ...) \
    LOG_PRI(ANDROID_##priority, tag, __VA_ARGS__)
#define LOG_V(...) ((void)LOG(LOG_VERBOSE, LOG_TAG, __VA_ARGS__))
#define LOG_D(...) ((void)LOG(LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOG_I(...) ((void)LOG(LOG_INFO, LOG_TAG, __VA_ARGS__))
#define LOG_W(...) ((void)LOG(LOG_WARN, LOG_TAG, __VA_ARGS__))
#define LOG_E(...) ((void)LOG(LOG_ERROR, LOG_TAG, __VA_ARGS__))
#else
#define LOG_V(...)
#define LOG_D(...)
#define LOG_I(...)
#define LOG_W(...)
#define LOG_E(...)
#endif

#endif //SELEKT_BBLOG_H
