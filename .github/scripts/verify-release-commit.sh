#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <release-commit> <protected-branch-ref>" >&2
  exit 2
fi

release_commit=$1
protected_branch_ref=$2

git rev-parse --verify "${release_commit}^{commit}" >/dev/null
git rev-parse --verify "${protected_branch_ref}^{commit}" >/dev/null

if ! git merge-base --is-ancestor "$release_commit" "$protected_branch_ref"; then
  echo "Release commit $release_commit is not part of $protected_branch_ref." >&2
  exit 1
fi
