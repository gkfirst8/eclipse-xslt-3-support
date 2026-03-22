#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SAXON_CP="$ROOT/plugins/nl.indi.eclipse.xslt3.core/lib/Saxon-HE-12.8.jar:$ROOT/plugins/nl.indi.eclipse.xslt3.core/lib/xmlresolver-5.3.3.jar:$ROOT/plugins/nl.indi.eclipse.xslt3.core/lib/xmlresolver-5.3.3-data.jar"
DEFAULT_XSL="$ROOT/samples/xslt3-demo/xslt3-kitchen-sink.xsl"
DEFAULT_INPUT="$ROOT/samples/xslt3-demo/xslt3-kitchen-sink-input.xml"
DEFAULT_OUTPUT="$ROOT/.runtime/xslt3-demo/xslt3-kitchen-sink-output.xml"

XSL_FILE="$DEFAULT_XSL"
INPUT_FILE="$DEFAULT_INPUT"
OUTPUT_FILE="$DEFAULT_OUTPUT"
WRITE_TO_STDOUT="false"
USE_INITIAL_TEMPLATE="false"
SHOW_OUTPUT="true"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --xsl)
            XSL_FILE="$2"
            shift 2
            ;;
        --input)
            INPUT_FILE="$2"
            shift 2
            ;;
        --output)
            OUTPUT_FILE="$2"
            shift 2
            ;;
        --stdout)
            WRITE_TO_STDOUT="true"
            shift
            ;;
        --it-main)
            USE_INITIAL_TEMPLATE="true"
            shift
            ;;
        --no-show)
            SHOW_OUTPUT="false"
            shift
            ;;
        *)
            echo "Unknown argument: $1" >&2
            echo "Supported arguments: --xsl <file> --input <file> --output <file> --stdout --it-main --no-show" >&2
            exit 1
            ;;
    esac
done

if [[ ! -f "$XSL_FILE" ]]; then
    echo "XSLT file not found: $XSL_FILE" >&2
    exit 1
fi

if [[ "$USE_INITIAL_TEMPLATE" != "true" && ! -f "$INPUT_FILE" ]]; then
    echo "Input XML file not found: $INPUT_FILE" >&2
    exit 1
fi

if [[ ! -f "$ROOT/plugins/nl.indi.eclipse.xslt3.core/lib/Saxon-HE-12.8.jar" ]]; then
    echo "Bundled Saxon jar not found under plugins/nl.indi.eclipse.xslt3.core/lib" >&2
    exit 1
fi

if [[ "$WRITE_TO_STDOUT" == "true" ]]; then
    if [[ "$USE_INITIAL_TEMPLATE" == "true" ]]; then
        exec java -cp "$SAXON_CP" net.sf.saxon.Transform -xsl:"$XSL_FILE" -it:main
    fi

    exec java -cp "$SAXON_CP" net.sf.saxon.Transform -s:"$INPUT_FILE" -xsl:"$XSL_FILE"
fi

mkdir -p "$(dirname "$OUTPUT_FILE")"

if [[ "$USE_INITIAL_TEMPLATE" == "true" ]]; then
    java -cp "$SAXON_CP" net.sf.saxon.Transform -xsl:"$XSL_FILE" -it:main -o:"$OUTPUT_FILE"
else
    java -cp "$SAXON_CP" net.sf.saxon.Transform -s:"$INPUT_FILE" -xsl:"$XSL_FILE" -o:"$OUTPUT_FILE"
fi

echo "Wrote output to $OUTPUT_FILE"

if [[ "$SHOW_OUTPUT" == "true" ]]; then
    echo
    cat "$OUTPUT_FILE"
fi
