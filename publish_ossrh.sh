#!/usr/bin/env bash
set +x
set -eu

GPG_KEY_RING_FILE=$(find ${GPG_DIR} -name '*.gpg' | head -1)
GPG_KEY_ID_SHORT="${${GPG_KEY_ID}:(-8)}"

./gradlew publish \
  -Possrh.password="${OSSRH_PASSWORD}" \
  -Possrh.username="${OSSRH_USERNAME}" \
  -Psigning.secretKeyRingFile=${GPG_KEY_RING_FILE} \
  -Psigning.password="${GPG_KEY_PASSPHRASE}" \
  -Psigning.keyId="${GPG_KEY_ID_SHORT}"
