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

require_build_output() {
  local pattern="$1"

  if compgen -G "$pattern" > /dev/null; then
    return 0
  fi

  echo "Missing expected build output: $pattern" >&2
  return 1
}

verify_build_outputs() {
  require_build_output "$ROOT/plugins/nl.indi.eclipse.xslt3.core/target/nl.indi.eclipse.xslt3.core-*.jar" &&
    require_build_output "$ROOT/plugins/nl.indi.eclipse.xslt3.ui/target/nl.indi.eclipse.xslt3.ui-*.jar" &&
    require_build_output "$ROOT/features/nl.indi.eclipse.xslt3.feature/target/nl.indi.eclipse.xslt3.feature-*.jar" &&
    require_build_output "$ROOT/releng/nl.indi.eclipse.xslt3.repository/target/repository/p2.index"
}

run_build() {
  if mvn -f "$ROOT/pom.xml" verify && verify_build_outputs; then
    return 0
  fi

  echo "Incremental Tycho build was incomplete or inconsistent. Retrying with 'mvn clean verify'." >&2
  mvn -f "$ROOT/pom.xml" clean verify
  verify_build_outputs
}

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
  run_build
else
  verify_build_outputs
fi

if [[ ! -x "$SOURCE_ECLIPSE_HOME/eclipse" ]]; then
  "$ROOT/scripts/install-local-eclipse.sh"
fi

if [[ ! -x "$SOURCE_ECLIPSE_HOME/eclipse" ]]; then
  echo "Expected an Eclipse launcher at $SOURCE_ECLIPSE_HOME/eclipse" >&2
  exit 1
fi

mkdir -p "$RUNTIME_DIR"

if [[ ! -x "$ECLIPSE_UNDER_TEST/eclipse" ]]; then
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
