# Third-Party Content

This repository includes or consumes third-party components in order to build
and run the Eclipse XSLT 3.0 feature. The built core plug-in redistributes the
runtime jars listed below.

## Bundled/runtime dependencies

| Component | Version | Upstream License | Redistributed In | Source Form / Upstream Location |
| --- | --- | --- | --- | --- |
| Saxon-HE | 12.8 | Mozilla Public License 2.0 | `nl.indi.eclipse.xslt3.core` | Maven Central `net.sf.saxon:Saxon-HE:12.8` and `:sources`, plus https://www.saxonica.com/html/documentation12/conditions/index.html |
| xmlresolver | 5.3.3 | Apache License 2.0 | `nl.indi.eclipse.xslt3.core` | Maven Central `org.xmlresolver:xmlresolver:5.3.3` and `:sources`, plus https://github.com/xmlresolver/xmlresolver |
| xmlresolver data | 5.3.3 | Apache License 2.0 | `nl.indi.eclipse.xslt3.core` | Maven Central `org.xmlresolver:xmlresolver:5.3.3:jar:data`, plus https://github.com/xmlresolver/xmlresolver |

## Build/platform dependencies

| Component | Source | Notes |
| --- | --- | --- |
| Eclipse platform target | `https://download.eclipse.org/releases/2025-03` | Pulled at build time through the Tycho target definition; not tracked in this Git repository. |

## Publication notes

- The repository's original project code and scripts are distributed under
  `EPL-2.0`.
- Third-party components retain their upstream licenses and are not relicensed by this repository.
- The built feature jar includes `epl-2.0.html` as the packaged feature
  license file.
- The built core plug-in includes:
  - `about.html`
  - `legal/MPL-2.0.txt`
  - `legal/Apache-2.0.txt`
  - `legal/THIRD-PARTY-NOTICES.txt`
- If you decide not to redistribute bundled binaries in Git, use `scripts/fetch-third-party-libs.sh` to populate the local `plugins/nl.indi.eclipse.xslt3.core/lib/` directory from Maven artifacts instead.
- Before a public release, re-check the upstream license pages and any required notice obligations for the exact versions you publish.
