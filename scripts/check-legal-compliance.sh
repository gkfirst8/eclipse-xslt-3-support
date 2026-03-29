#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FAILURES=0

fail() {
  echo "Compliance check failed: $1" >&2
  FAILURES=$((FAILURES + 1))
}

require_file() {
  local path="$1"
  if [[ ! -f "$ROOT/$path" ]]; then
    fail "missing file $path"
  fi
}

require_text() {
  local path="$1"
  local text="$2"
  if ! grep -qF "$text" "$ROOT/$path"; then
    fail "missing text '$text' in $path"
  fi
}

require_zip_entry() {
  local archive="$1"
  local entry="$2"
  if ! unzip -l "$archive" | grep -qF "$entry"; then
    fail "missing archive entry $entry in $archive"
  fi
}

resolve_single_match() {
  local pattern="$1"
  local matches=($pattern)

  if [[ ${#matches[@]} -eq 0 ]]; then
    return 1
  fi

  printf '%s\n' "${matches[0]}"
}

require_file "features/nl.indi.eclipse.xslt3.feature/epl-2.0.html"
require_file "plugins/nl.indi.eclipse.xslt3.core/about.html"
require_file "plugins/nl.indi.eclipse.xslt3.ui/about.html"
require_file "plugins/nl.indi.eclipse.xslt3.core/legal/MPL-2.0.txt"
require_file "plugins/nl.indi.eclipse.xslt3.core/legal/Apache-2.0.txt"
require_file "plugins/nl.indi.eclipse.xslt3.core/legal/THIRD-PARTY-NOTICES.txt"

require_text "features/nl.indi.eclipse.xslt3.feature/feature.xml" "<license url=\"epl-2.0.html\">"
require_text "features/nl.indi.eclipse.xslt3.feature/feature.xml" "<copyright>"
require_text "features/nl.indi.eclipse.xslt3.feature/build.properties" "epl-2.0.html"
require_text "plugins/nl.indi.eclipse.xslt3.core/build.properties" "about.html"
require_text "plugins/nl.indi.eclipse.xslt3.core/build.properties" "legal/"
require_text "plugins/nl.indi.eclipse.xslt3.ui/build.properties" "about.html"
require_text "plugins/nl.indi.eclipse.xslt3.core/META-INF/MANIFEST.MF" "Bundle-License:"
require_text "plugins/nl.indi.eclipse.xslt3.ui/META-INF/MANIFEST.MF" "Bundle-License:"
require_text "THIRD-PARTY.md" "legal/MPL-2.0.txt"
require_text "NOTICE" "core Eclipse plug-in built from this repository redistributes"

shopt -s nullglob
REPOSITORY_DIR="$ROOT/releng/nl.indi.eclipse.xslt3.repository/target/repository"
FEATURE_GLOB="$REPOSITORY_DIR/features/nl.indi.eclipse.xslt3.feature_"'*.jar'
CORE_GLOB="$REPOSITORY_DIR/plugins/nl.indi.eclipse.xslt3.core_"'*.jar'
UI_GLOB="$REPOSITORY_DIR/plugins/nl.indi.eclipse.xslt3.ui_"'*.jar'

if [[ -d "$REPOSITORY_DIR" ]]; then
  FEATURE_JAR="$(resolve_single_match "$FEATURE_GLOB" || true)"
  CORE_JAR="$(resolve_single_match "$CORE_GLOB" || true)"
  UI_JAR="$(resolve_single_match "$UI_GLOB" || true)"

  if [[ -z "${FEATURE_JAR:-}" || -z "${CORE_JAR:-}" || -z "${UI_JAR:-}" ]]; then
    fail "expected built feature and plug-in jars under $REPOSITORY_DIR"
  else
  require_zip_entry "$FEATURE_JAR" "feature.xml"
  require_zip_entry "$FEATURE_JAR" "epl-2.0.html"
  require_zip_entry "$CORE_JAR" "about.html"
  require_zip_entry "$CORE_JAR" "legal/MPL-2.0.txt"
  require_zip_entry "$CORE_JAR" "legal/Apache-2.0.txt"
  require_zip_entry "$CORE_JAR" "legal/THIRD-PARTY-NOTICES.txt"
  require_zip_entry "$UI_JAR" "about.html"
  fi
else
  echo "Artifact checks skipped because the built p2 repository was not found." >&2
fi

if [[ "$FAILURES" -ne 0 ]]; then
  exit 1
fi

echo "Legal compliance checks passed."
