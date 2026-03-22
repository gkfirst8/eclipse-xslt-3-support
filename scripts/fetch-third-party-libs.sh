#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LIB_DIR="$ROOT/plugins/nl.indi.eclipse.xslt3.core/lib"
M2_REPO="${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}"
FORCE="false"

for argument in "$@"; do
  case "$argument" in
    --force)
      FORCE="true"
      ;;
    *)
      echo "Unknown argument: $argument" >&2
      echo "Supported arguments: --force" >&2
      exit 1
      ;;
  esac
done

require_command() {
  local command_name="$1"
  if ! command -v "$command_name" > /dev/null 2>&1; then
    echo "Missing required command: $command_name" >&2
    exit 1
  fi
}

ensure_cached() {
  local artifact="$1"
  local repository_path="$2"

  if [[ -f "$M2_REPO/$repository_path" && "$FORCE" != "true" ]]; then
    return 0
  fi

  mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.8.1:get \
    -Dartifact="$artifact" \
    -Dtransitive=false
}

copy_cached() {
  local repository_path="$1"
  local output_file="$2"

  if [[ ! -f "$M2_REPO/$repository_path" ]]; then
    echo "Expected Maven artifact not found in local repository: $repository_path" >&2
    exit 1
  fi

  cp "$M2_REPO/$repository_path" "$LIB_DIR/$output_file"
}

require_command mvn
mkdir -p "$LIB_DIR"

ensure_cached "net.sf.saxon:Saxon-HE:12.8" "net/sf/saxon/Saxon-HE/12.8/Saxon-HE-12.8.jar"
copy_cached "net/sf/saxon/Saxon-HE/12.8/Saxon-HE-12.8.jar" "Saxon-HE-12.8.jar"

ensure_cached "org.xmlresolver:xmlresolver:5.3.3" "org/xmlresolver/xmlresolver/5.3.3/xmlresolver-5.3.3.jar"
copy_cached "org/xmlresolver/xmlresolver/5.3.3/xmlresolver-5.3.3.jar" "xmlresolver-5.3.3.jar"

ensure_cached "org.xmlresolver:xmlresolver:5.3.3:jar:data" "org/xmlresolver/xmlresolver/5.3.3/xmlresolver-5.3.3-data.jar"
copy_cached "org/xmlresolver/xmlresolver/5.3.3/xmlresolver-5.3.3-data.jar" "xmlresolver-5.3.3-data.jar"

echo "Prepared third-party jars in $LIB_DIR"
