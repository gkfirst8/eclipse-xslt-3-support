# Eclipse XSLT 3.0 Support

This repository builds an installable Eclipse feature that adds basic XSLT 3.0
editing support backed by Saxon-HE 12.8.

## License

- Original project code and scripts are distributed under `EPL-2.0`.
- The built Eclipse feature packages `epl-2.0.html` as the installable feature
  license text.
- The built core plug-in packages third-party notices and license texts under
  `plugins/nl.indi.eclipse.xslt3.core/legal/`.
- Third-party redistribution details: see `THIRD-PARTY.md`.

## Modules

- `plugins/nl.indi.eclipse.xslt3.core`: validation and Saxon integration
- `plugins/nl.indi.eclipse.xslt3.ui`: editor, syntax coloring, outline, content assist, and function reference view
- `features/nl.indi.eclipse.xslt3.feature`: installable Eclipse feature
- `releng/nl.indi.eclipse.xslt3.repository`: p2 update-site repository
- `releng/nl.indi.eclipse.xslt3.target`: Tycho target definition

## Prerequisites

Supported publication baseline:

- Ubuntu x86_64
- Java 21 on `PATH`
- Maven on `PATH`
- An existing Eclipse installation available through `ECLIPSE_SOURCE_HOME`,
  `ECLIPSE_HOME`, `--source`, or `eclipse` on `PATH`

Check the local environment with:

```bash
scripts/check-prereqs.sh
```

## Fetch third-party jars

The repo can work with the bundled jars currently checked in under
`plugins/nl.indi.eclipse.xslt3.core/lib/`.

The built core plug-in redistributes these jars together with:

- `about.html`
- `legal/MPL-2.0.txt`
- `legal/Apache-2.0.txt`
- `legal/THIRD-PARTY-NOTICES.txt`

If you want to rebuild those jars from Maven artifacts instead of relying on
the checked-in binaries, run:

```bash
scripts/fetch-third-party-libs.sh
```

## Build

```bash
mvn verify
```

## Run the inspection Eclipse

Install a repo-local Eclipse base:

```bash
scripts/install-local-eclipse.sh
```

Then launch the runtime Eclipse:

```bash
scripts/run-runtime-eclipse.sh
```

Helpful variants:

```bash
scripts/run-runtime-eclipse.sh --skip-build
scripts/run-runtime-eclipse.sh --prepare-only
```

## Run the sample transformation

```bash
scripts/run-xslt3-demo.sh
```

Useful variants:

```bash
scripts/run-xslt3-demo.sh --stdout
scripts/run-xslt3-demo.sh --no-show
scripts/run-xslt3-demo.sh --it-main
scripts/run-xslt3-demo.sh --output .runtime/xslt3-demo/custom-output.xml
```

## Prepare a p2 update site

To build and stage the p2 repository for local testing or GitHub Pages
publication:

```bash
scripts/prepare-p2-site.sh
```

By default the staged site is written to `.site/p2/`.

## Publish on GitHub

Recommended public distribution flow:

1. Push the repository to GitHub.
2. Enable GitHub Pages for the repository.
3. Use the included GitHub Actions workflows to:
   - run CI on pushes and pull requests
   - publish the Tycho-generated p2 repository on release tags
4. Share the public update-site URL:

```text
https://<github-owner>.github.io/<repo-name>/
```

## Install in Eclipse

Once the update site is published, Eclipse users can install it through:

Public update-site URL:

```text
https://gkfirst8.github.io/eclipse-xslt-3-support/
```

1. `Help`
2. `Install New Software...`
3. `Add...`
4. Enter the public update-site URL
5. Select `XSLT 3 Support`

The same URL now also serves a browser landing page with installation notes,
troubleshooting hints, and links to the raw p2 repository files.

## Current scope

Implemented:

- XSLT editor for `*.xsl` and `*.xslt`
- validation on save using Saxon-HE
- outline for common top-level declarations
- simple XSLT element content assist
- function reference view for bundled XSLT/XPath function docs
- installable Eclipse feature plus p2 update site

Deferred:

- debugger integration
- formatter
- full semantic navigation/refactoring
- Eclipse Marketplace catalog entry
