#!/usr/bin/env bash
set -euo pipefail

require_command() {
  local command_name="$1"
  local description="$2"

  if ! command -v "$command_name" > /dev/null 2>&1; then
    echo "Missing required command: $description ($command_name)" >&2
    exit 1
  fi
}

resolve_eclipse_home() {
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

require_command java "Java runtime"
require_command mvn "Maven"

JAVA_VERSION_OUTPUT="$(java -version 2>&1 | head -n 1)"
MVN_VERSION_OUTPUT="$(mvn -version | head -n 1)"

if [[ "$JAVA_VERSION_OUTPUT" != *"21."* && "$JAVA_VERSION_OUTPUT" != *" 21"* ]]; then
  echo "Expected Java 21, found: $JAVA_VERSION_OUTPUT" >&2
  exit 1
fi

if ! ECLIPSE_HOME_RESOLVED="$(resolve_eclipse_home)"; then
  echo "Could not locate an Eclipse installation." >&2
  echo "Set ECLIPSE_SOURCE_HOME or ECLIPSE_HOME, or place 'eclipse' on PATH." >&2
  exit 1
fi

if [[ ! -x "$ECLIPSE_HOME_RESOLVED/eclipse" ]]; then
  echo "Resolved Eclipse home does not contain an executable launcher: $ECLIPSE_HOME_RESOLVED/eclipse" >&2
  exit 1
fi

echo "Java:    $JAVA_VERSION_OUTPUT"
echo "Maven:   $MVN_VERSION_OUTPUT"
echo "Eclipse: $ECLIPSE_HOME_RESOLVED"
