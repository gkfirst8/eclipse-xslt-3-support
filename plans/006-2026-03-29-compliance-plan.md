# Make The Repo and Published Eclipse Artifacts Compliance-Ready

## Summary

- Keep the current model of bundling the third-party jars, but make the repo
  and shipped Eclipse feature and plug-in artifacts explicitly compliant.
- Fix both source-repo compliance and Eclipse distribution compliance.
- Do not add SPDX headers across all source files in this pass.

## Key Changes

- Add explicit Eclipse feature license metadata and package the referenced
  legal file.
- Add plug-in-level legal and about files so installed Eclipse artifacts
  surface licensing correctly.
- Tighten top-level repository legal docs so they match actual redistribution.
- Add an automated compliance guardrail and run it in CI and publish flows.

## Public Interfaces / Artifacts

- No runtime API or functional behavior changes.
- New packaged legal assets become part of the published feature and plug-in
  contents.

## Test Plan

- Run `mvn verify` and confirm the build still succeeds.
- Verify the built feature jar contains `feature.xml` and `epl-2.0.html`.
- Verify the built core plug-in jar contains `about.html` and `legal/*`.
- Verify the built UI plug-in jar contains `about.html`.
- Run the legal compliance script locally and in CI.

## Assumptions

- Bundled third-party jars remain in Git and in the shipped core plug-in.
- Compliance target covers both the repository and the published p2 site.
