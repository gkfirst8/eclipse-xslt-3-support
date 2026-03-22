#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="$ROOT/.runtime"
LOCAL_BASE="$RUNTIME_DIR/eclipse-base"
ECLIPSE_UNDER_TEST="$RUNTIME_DIR/eclipse-under-test"
WORKSPACE_DIR="$RUNTIME_DIR/workspace"
DROPINS_DIR="$ECLIPSE_UNDER_TEST/dropins/nl.indi.eclipse.xslt3/eclipse"
SOURCE_ECLIPSE_HOME="${ECLIPSE_HOME:-$LOCAL_BASE}"
SKIP_BUILD="false"
PREPARE_ONLY="false"

for argument in "$@"; do
  case "$argument" in
    --skip-build)
      SKIP_BUILD="true"
      ;;
    --prepare-only)
      PREPARE_ONLY="true"
      ;;
    *)
      echo "Unknown argument: $argument" >&2
      echo "Supported arguments: --skip-build --prepare-only" >&2
      exit 1
      ;;
  esac
done

if [[ "$SKIP_BUILD" != "true" ]]; then
  mvn -f "$ROOT/pom.xml" verify
fi

if [[ ! -x "$SOURCE_ECLIPSE_HOME/eclipse" ]]; then
  "$ROOT/scripts/install-local-eclipse.sh"
fi

if [[ ! -x "$SOURCE_ECLIPSE_HOME/eclipse" ]]; then
  echo "Expected an Eclipse launcher at $SOURCE_ECLIPSE_HOME/eclipse" >&2
  exit 1
fi

mkdir -p "$RUNTIME_DIR"

if [[ ! -d "$ECLIPSE_UNDER_TEST" ]]; then
  cp -a "$SOURCE_ECLIPSE_HOME" "$ECLIPSE_UNDER_TEST"
fi

rm -rf "$DROPINS_DIR"
mkdir -p "$DROPINS_DIR/plugins" "$DROPINS_DIR/features" "$WORKSPACE_DIR"

while IFS= read -r jar_file; do
  case "$jar_file" in
    */plugins/*)
      cp "$jar_file" "$DROPINS_DIR/plugins/"
      ;;
    */features/*)
      cp "$jar_file" "$DROPINS_DIR/features/"
      ;;
  esac
done < <(find "$ROOT/plugins" "$ROOT/features" -path '*/target/*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' | sort)

if [[ "$PREPARE_ONLY" == "true" ]]; then
  echo "Prepared runtime Eclipse at $ECLIPSE_UNDER_TEST"
  exit 0
fi

exec "$ECLIPSE_UNDER_TEST/eclipse" -clean -consoleLog -data "$WORKSPACE_DIR"
