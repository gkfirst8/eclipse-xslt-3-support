#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

RESULT_FILE="$(mktemp -t xslt3-hardcoded-paths.XXXXXX)"
trap 'rm -f "$RESULT_FILE"' EXIT

if git grep -nE '/home/|/Users/|[A-Za-z]:\\\\Users\\\\|/tmp/' -- . ':!scripts/check-hardcoded-paths.sh' > "$RESULT_FILE"; then
  echo "Found hard-coded host paths in tracked files:" >&2
  cat "$RESULT_FILE" >&2
  exit 1
fi

echo "No hard-coded host paths found in tracked files."
