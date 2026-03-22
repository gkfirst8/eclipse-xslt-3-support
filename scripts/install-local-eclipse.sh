#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="$ROOT/.runtime"
LOCAL_BASE="$RUNTIME_DIR/eclipse-base"
DEFAULT_SOURCE="${ECLIPSE_SOURCE_HOME}"
SOURCE_HOME="${ECLIPSE_SOURCE_HOME:-${ECLIPSE_HOME:-$DEFAULT_SOURCE}}"

if [[ -x "$LOCAL_BASE/eclipse" ]]; then
  echo "Repo-local Eclipse already exists at $LOCAL_BASE"
  exit 0
fi

if [[ ! -x "$SOURCE_HOME/eclipse" ]]; then
  echo "Could not find a source Eclipse installation at $SOURCE_HOME" >&2
  echo "Set ECLIPSE_SOURCE_HOME or ECLIPSE_HOME to a valid Eclipse installation." >&2
  exit 1
fi

mkdir -p "$RUNTIME_DIR"
cp -a "$SOURCE_HOME" "$LOCAL_BASE"

echo "Installed repo-local Eclipse at $LOCAL_BASE"

