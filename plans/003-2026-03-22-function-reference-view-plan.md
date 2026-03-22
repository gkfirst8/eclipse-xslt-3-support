# Function Reference View Plan

## Summary

- Implement the bottom-docked XSLT function reference view in a dedicated `git worktree` so this task stays isolated from other concurrent work.
- Add bundled offline XSLT/XPath function reference content to the UI plugin.
- Synchronize the view automatically from the active XSLT editor caret/selection.

## Workflow

- Use the dedicated `function-reference-view` branch and worktree for all edits, tests, and commits.
- Keep this plan file in the active worktree under `plans/`.
- Record the worktree-first rule in `AGENTS.md` for future concurrent-agent work.

## Implementation

- Add a new Eclipse view contribution and perspective extension for a bottom-docked `Function Reference` view in the standard IDE/resource perspective.
- Build the view as a `ViewPart` that renders bundled offline reference content via SWT `Browser`, with a safe text fallback if the browser widget is unavailable.
- Bundle function reference data and styling in the UI plugin, then load it through a repository class.
- Add lookup logic that resolves the built-in function at the current caret position, including standard `fn`, `map`, `array`, and `math` functions plus unprefixed core functions.
- Update the XSLT editor so the reference view opens visibly without stealing focus and exposes the active document for view synchronization.
- Keep V1 scope to automatic sync only; defer F3, Ctrl-click, hover integration, and user-defined `xsl:function` navigation.

## Verification

- Add automated coverage where practical for lookup normalization and resource loading.
- Verify manually in the runtime Eclipse that the view opens in the bottom dock, updates while moving the caret across supported function calls, shows a neutral placeholder for unsupported locations, and works without network access.
