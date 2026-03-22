# Eclipse XSLT 3.0 Support

This repository bootstraps an Eclipse installable feature that adds basic XSLT 3.0 editing support backed by Saxon-HE 12.8.

## Modules

- `plugins/nl.indi.eclipse.xslt3.core`: validation and Saxon integration
- `plugins/nl.indi.eclipse.xslt3.ui`: editor, syntax coloring, outline, and content assist
- `features/nl.indi.eclipse.xslt3.feature`: installable Eclipse feature
- `releng/nl.indi.eclipse.xslt3.repository`: p2/update-site repository
- `releng/nl.indi.eclipse.xslt3.target`: Tycho target definition

## Build

```bash
mvn verify
```

## Install a repo-local Eclipse base

This repository can keep its own local Eclipse installation under `.runtime/eclipse-base`.

To copy a local Eclipse installation into the repo-managed runtime area, run:

```bash
scripts/install-local-eclipse.sh
```

By default the script uses:

- `ECLIPSE_SOURCE_HOME`, if set
- otherwise `ECLIPSE_HOME`, if set
- otherwise the machine-local installation at `${ECLIPSE_SOURCE_HOME}`, if present

## Run the inspection Eclipse

Once the local base exists, run:

```bash
scripts/run-runtime-eclipse.sh
```

The script:

- builds this repo
- copies `.runtime/eclipse-base` into `.runtime/eclipse-under-test`
- overlays the freshly built feature/plugin JARs via `dropins/`
- launches a separate Eclipse workspace in `.runtime/workspace`

`ECLIPSE_HOME` still overrides the repo-local base if you want to launch from another installation.
Use `--skip-build` if you already ran `mvn verify`.
Use `--prepare-only` to stage `.runtime/eclipse-under-test` without launching the GUI.

## Run the sample XSLT during development

To run the kitchen-sink sample transformation while developing the Eclipse feature, use:

```bash
scripts/run-xslt3-demo.sh
```

This uses:

- [xslt3-kitchen-sink.xsl](samples/xslt3-demo/xslt3-kitchen-sink.xsl)
- [xslt3-kitchen-sink-input.xml](samples/xslt3-demo/xslt3-kitchen-sink-input.xml)
- the bundled Saxon jars from `plugins/nl.indi.eclipse.xslt3.core/lib`

The default output file is written to:

```bash
.runtime/xslt3-demo/xslt3-kitchen-sink-output.xml
```

By default the script also prints the transformation result to the terminal.

Useful variants:

```bash
scripts/run-xslt3-demo.sh --stdout
scripts/run-xslt3-demo.sh --no-show
scripts/run-xslt3-demo.sh --it-main
scripts/run-xslt3-demo.sh --output .runtime/xslt3-demo/custom-output.xml
```

## Current scope

The first implementation includes:

- file association for `*.xsl` and `*.xslt`
- a dedicated XSLT editor
- basic XSLT/XML syntax coloring
- outline entries for common top-level XSLT declarations
- simple XSLT element content assist
- validation on save using Saxon-HE

Not yet implemented:

- debugger integration
- formatter
- full semantic navigation/refactoring
- deep reuse of the historic WTP XSL source bundles
