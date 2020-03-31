#!/usr/bin/env bash -ex

./gradlew :OpenSSL:assembleArmeabi-v7a
./gradlew :OpenSSL:assembleX86
./gradlew :OpenSSL:assembleArm64-v8a
./gradlew :OpenSSL:assembleX86_64
./gradlew :SQLite3:amalgamate
./gradlew assembleSelekt

