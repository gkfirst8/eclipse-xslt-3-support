#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_DIR="$ROOT/.site/p2"
SKIP_BUILD="false"
LANDING_PAGE_DIR="$ROOT/site"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --output)
      OUTPUT_DIR="$2"
      shift 2
      ;;
    --skip-build)
      SKIP_BUILD="true"
      shift
      ;;
    *)
      echo "Unknown argument: $1" >&2
      echo "Supported arguments: --output <dir> --skip-build" >&2
      exit 1
      ;;
  esac
done

if [[ "$SKIP_BUILD" != "true" ]]; then
  "$ROOT/scripts/fetch-third-party-libs.sh"
  mvn -f "$ROOT/pom.xml" verify
fi

SOURCE_REPOSITORY="$ROOT/releng/nl.indi.eclipse.xslt3.repository/target/repository"
if [[ ! -f "$SOURCE_REPOSITORY/p2.index" ]]; then
  echo "Missing built p2 repository under $SOURCE_REPOSITORY" >&2
  echo "Run this script without --skip-build or build the repo first." >&2
  exit 1
fi

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"
cp -a "$SOURCE_REPOSITORY/." "$OUTPUT_DIR/"

if [[ -d "$LANDING_PAGE_DIR" ]]; then
  cp -a "$LANDING_PAGE_DIR/." "$OUTPUT_DIR/"
fi

echo "Prepared p2 site at $OUTPUT_DIR"
