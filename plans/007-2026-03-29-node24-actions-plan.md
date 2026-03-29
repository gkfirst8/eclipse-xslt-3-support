# Migrate GitHub Actions Workflows Off Node 20

## Summary

- Update the affected workflows to use Node24-capable action majors.
- Replace `actions/checkout@v4` with `actions/checkout@v6`.
- Replace `actions/setup-java@v4` with `actions/setup-java@v5`.
- Opt into Node24 now with `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: 'true'`.

## Key Changes

- Update `.github/workflows/ci.yml`.
- Update `.github/workflows/publish-p2-site.yml`.
- Keep the rest of the workflow steps unchanged.

## Test Plan

- Review the updated workflow YAML for syntax and consistency.
- Run a local workflow linter if available.
- Confirm the deprecation warning disappears on the next GitHub run.

## Assumptions

- `actions/checkout@v6` and `actions/setup-java@v5` are the intended
  Node24-capable majors for this repository.
- `ubuntu-latest` remains compatible with these action versions.
