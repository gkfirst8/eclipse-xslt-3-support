# Fix Explicit Encoding Warning For `xslt3-demo`

## Summary

- Remove the Eclipse warning by making the sample project's default text encoding explicit at the project level.
- Keep the fix minimal and repo-local by targeting the checked-in sample project metadata under `samples/xslt3-demo`.
- Add a lightweight regression guard so the sample does not lose its explicit encoding metadata later.

## Implementation Changes

- Add `samples/xslt3-demo/.settings/org.eclipse.core.resources.prefs` with:
  - `eclipse.preferences.version=1`
  - `encoding/<project>=UTF-8`
- Keep `xslt3-demo` as a plain Eclipse project; do not add Java or PDE natures just to clear the warning.
- Document the requirement so the sample keeps explicit UTF-8 project encoding if its Eclipse metadata is ever recreated.

## Public Interfaces / Behavior

- No runtime or plugin API changes.
- User-visible change: importing or refreshing `xslt3-demo` in Eclipse should no longer show `Project 'xslt3-demo' has no explicit encoding set`.
- Encoding behavior becomes explicit and stable: the project default is UTF-8 instead of inheriting workspace defaults.

## Test Plan

- Import or refresh `samples/xslt3-demo` in the inspection Eclipse and confirm the warning disappears from Problems.
- Open existing sample files such as `test.xsl`, `common.xsl`, and `xslt3-kitchen-sink-input.xml` and confirm they still render normally under UTF-8.

## Assumptions And Defaults

- Default encoding to declare is UTF-8.
- Scope is limited to the checked-in sample project and a lightweight documentation guard, not a repo-wide Eclipse metadata normalization pass.
