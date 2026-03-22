# GitHub Publication Plan

## Summary

- Publish the repo under EPL-2.0 with explicit third-party dependency documentation.
- Remove host-specific paths and make the repo portable for Ubuntu users with Java 21, Maven, and Eclipse installed.
- Publish the Tycho-generated p2 repository as the first public install path.

## Implementation

- Add `LICENSE`, `NOTICE`, and `THIRD-PARTY.md`.
- Scrub hard-coded machine paths from scripts and docs.
- Add bootstrap and verification scripts for prerequisites, third-party jars, and p2 site staging.
- Add GitHub Actions for CI and Pages-based p2 publication.

## Verification

- `scripts/check-hardcoded-paths.sh`
- `scripts/check-prereqs.sh`
- `mvn verify`
- `scripts/prepare-p2-site.sh`
