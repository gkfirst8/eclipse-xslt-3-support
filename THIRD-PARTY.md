# Third-Party Content

This repository includes or consumes third-party components in order to build
and run the Eclipse XSLT 3.0 feature.

## Bundled/runtime dependencies

| Component | Version | Upstream License | Current Use | Source |
| --- | --- | --- | --- | --- |
| Saxon-HE | 12.8 | Mozilla Public License 2.0 | XSLT/XPath processing in the core plugin | https://www.saxonica.com/html/documentation12/conditions/index.html |
| xmlresolver | 5.3.3 | Apache License 2.0 | XML catalog and URI resolution support | https://github.com/xmlresolver/xmlresolver/blob/main/LICENSE.md |
| xmlresolver data | 5.3.3 | Apache License 2.0 | Resolver catalog data bundle | https://github.com/xmlresolver/xmlresolver/blob/main/LICENSE.md |

## Build/platform dependencies

| Component | Source | Notes |
| --- | --- | --- |
| Eclipse platform target | `https://download.eclipse.org/releases/2025-03` | Pulled at build time through the Tycho target definition; not tracked in this Git repository. |

## Publication notes

- The repo is intended to be published under `EPL-2.0` for the original project code.
- Third-party components retain their upstream licenses and are not relicensed by this repository.
- If you decide not to redistribute bundled binaries in Git, use `scripts/fetch-third-party-libs.sh` to populate the local `plugins/nl.indi.eclipse.xslt3.core/lib/` directory from Maven artifacts instead.
- Before a public release, re-check the upstream license pages and any required notice obligations for the exact versions you publish.
