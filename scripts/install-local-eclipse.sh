#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="$ROOT/.runtime"
LOCAL_BASE="$RUNTIME_DIR/eclipse-base"
SOURCE_HOME=""

resolve_source_home() {
  if [[ -n "$SOURCE_HOME" ]]; then
    printf '%s\n' "$SOURCE_HOME"
    return 0
  fi

  if [[ -n "${ECLIPSE_SOURCE_HOME:-}" ]]; then
    printf '%s\n' "$ECLIPSE_SOURCE_HOME"
    return 0
  fi

  if [[ -n "${ECLIPSE_HOME:-}" ]]; then
    printf '%s\n' "$ECLIPSE_HOME"
    return 0
  fi

  if command -v eclipse > /dev/null 2>&1; then
    dirname "$(readlink -f "$(command -v eclipse)")"
    return 0
  fi

  return 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --source)
      SOURCE_HOME="$2"
      shift 2
      ;;
    *)
      echo "Unknown argument: $1" >&2
      echo "Supported arguments: --source <eclipse-home>" >&2
      exit 1
      ;;
  esac
done

if [[ -x "$LOCAL_BASE/eclipse" ]]; then
  echo "Repo-local Eclipse already exists at $LOCAL_BASE"
  exit 0
fi

if ! SOURCE_HOME="$(resolve_source_home)"; then
  echo "Could not locate a source Eclipse installation." >&2
  echo "Use --source, set ECLIPSE_SOURCE_HOME or ECLIPSE_HOME, or place 'eclipse' on PATH." >&2
  exit 1
fi

if [[ ! -x "$SOURCE_HOME/eclipse" ]]; then
  echo "Could not find a source Eclipse installation at $SOURCE_HOME" >&2
  exit 1
fi

mkdir -p "$RUNTIME_DIR"
cp -a "$SOURCE_HOME" "$LOCAL_BASE"

echo "Installed repo-local Eclipse at $LOCAL_BASE"
