# Add A Root Landing Page For GitHub Pages

## Summary

- Keep the p2 repository at the site root and add a browser-facing `index.html`
  alongside it.
- Use the same public URL for both Eclipse installation and human-readable
  documentation.
- Ship a compact docs-style page with install instructions, feature overview,
  troubleshooting, and screenshot placeholders for later replacement.

## Key Changes

- Add a small static site bundle under `site/` with `index.html` and CSS.
- Extend `scripts/prepare-p2-site.sh` to overlay the static site bundle after
  copying the built p2 repository into the output directory.
- Keep all p2 metadata and artifact files in place so Eclipse can still install
  from the root URL unchanged.
- Mention the dual-purpose URL in the README where helpful.

## Test Plan

- Stage the Pages output with `scripts/prepare-p2-site.sh --output <tmp-dir>`.
- Verify the staged output contains `index.html` plus `p2.index`,
  `content.jar`, and `artifacts.jar`.
- Open the staged `index.html` in a browser and verify links, layout, and
  responsive behavior.
- Confirm Eclipse can still load the same root URL as an update site.

## Assumptions

- A root `index.html` does not interfere with p2 repository consumption.
- Real screenshots will be added later; the first version uses placeholders.
- The landing page remains plain static HTML/CSS with no Jekyll or site
  generator.
