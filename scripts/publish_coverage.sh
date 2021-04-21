#!/usr/bin/env bash

INITIAL_DIR=$(pwd)

curl -s https://codecov.io/bash > codecov
VERSION=$(grep 'VERSION=\"[0-9\.]*\"' codecov | cut -d'"' -f2)
echo "Verifying codecov ${VERSION}"

function tearDown {
  echo "Tearing down..."
  cd ${INITIAL_DIR}
  rm codecov
}
trap tearDown EXIT

for i in 1 256 512
do
  shasum -a $i codecov
  shasum -a $i -c <(curl -s "https://raw.githubusercontent.com/codecov/codecov-bash/${VERSION}/SHA${i}SUM" | grep codecov)
done

bash codecov
