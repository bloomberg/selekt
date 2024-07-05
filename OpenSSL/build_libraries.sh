#!/usr/bin/env bash

set -e

OPENSSL_PATH=$1
TARGET_ABI=$2
ANDROID_API=$3

cd ${0%/*} || exit
INITIAL_DIR=$(pwd)

function _tearDown {
  echo "Tearing down..."
  cd ${INITIAL_DIR} || exit
}
trap _tearDown EXIT

WORKING_DIR=${OPENSSL_PATH}
cd ${WORKING_DIR} || exit

OUTPUT_DIR="${INITIAL_DIR}/build/libs/${TARGET_ABI}"

case $(uname -a) in
    Darwin*)
        TOOLCHAIN_SYSTEM=darwin-x86_64
        ;;
    Linux*)
        TOOLCHAIN_SYSTEM=linux-x86_64
        ;;
   *)
        echo "Unrecognised host."
        exit 1
        ;;
esac

case ${TARGET_ABI} in
    arm64-v8a)
        ARCH='android-arm64'
        OFFSET=64
        ;;
    x86_64)
        ARCH='android64-x86_64'
        OFFSET=64
        ;;
    *)
        echo "Target ABI '${TARGET_ABI}' is not supported."
        exit 1
        ;;
esac

# https://github.com/openssl/openssl/blob/837017b4748d587912d9d218894644d6ca86721f/INSTALL#L469-L482
OPENSSL_CONFIGURE_OPTIONS="-fPIC -fstack-protector-all no-idea no-camellia \
    no-seed no-bf no-blake2 no-cast no-rc2 no-rc4 no-rc5 no-md2 no-rmd160 \
    no-md4 no-ecdh no-sock no-ssl no-ssl3 \
    no-cast no-chacha no-des no-dh no-dsa no-ec no-ecdsa no-ec2m no-ocb no-tls no-dtls no-nextprotoneg \
    no-poly1305 no-rfc3779 no-whirlpool no-scrypt no-srp \
    no-mdc2 no-engine no-ts no-sse2 \
    no-sm2 no-sm3 no-sm4 no-ocsp no-cmac \
    no-srtp no-shared no-comp no-ct no-cms no-capieng \
    no-deprecated no-autoerrinit no-stdio no-ui-console \
    no-filenames"

TOOLCHAIN_BIN="${ANDROID_NDK_HOME}/toolchains/llvm/prebuilt/${TOOLCHAIN_SYSTEM}/bin/"
PATH=${TOOLCHAIN_BIN}:${PATH}

./config ${ARCH} \
    -D__ANDROID_API__=${ANDROID_API} \
    -D_FILE_OFFSET_BITS=${OFFSET} \
    ${OPENSSL_CONFIGURE_OPTIONS}

if [[ $? -ne 0 ]]; then
    echo "Failed to configure OpenSSL."
    exit 1
fi

make clean
if [[ $? -ne 0 ]]; then
    echo "Failed to clean OpenSSL."
    exit 1
fi

make build_libs
if [[ $? -ne 0 ]]; then
    echo "Failed to build OpenSSL libraries."
    exit 1
fi

rm -rf ${OUTPUT_DIR}
mkdir -p ${OUTPUT_DIR}
cp libcrypto.a ${OUTPUT_DIR}
