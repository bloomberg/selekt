#!/usr/bin/env bash

set -euo pipefail

script_directory=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
verifier="$script_directory/verify-release-commit.sh"
test_repository=$(mktemp -d)
trap 'rm -rf "$test_repository"' EXIT

git -C "$test_repository" init --quiet
git -C "$test_repository" config user.email 'release-gate-test@example.com'
git -C "$test_repository" config user.name 'Release gate test'
git -C "$test_repository" commit --quiet --allow-empty -m 'Initial commit'
initial_commit=$(git -C "$test_repository" rev-parse HEAD)
git -C "$test_repository" branch -M main
git -C "$test_repository" commit --quiet --allow-empty -m 'Reviewed commit'
reviewed_commit=$(git -C "$test_repository" rev-parse HEAD)

git -C "$test_repository" switch --quiet --detach "$initial_commit"
git -C "$test_repository" commit --quiet --allow-empty -m 'Unreviewed commit'
unreviewed_commit=$(git -C "$test_repository" rev-parse HEAD)

verify() {
  (cd "$test_repository" && "$verifier" "$@")
}

verify "$initial_commit" main
verify "$reviewed_commit" main
if verify "$unreviewed_commit" main; then
  echo 'An unreviewed release commit unexpectedly passed the ancestry check.' >&2
  exit 1
fi
