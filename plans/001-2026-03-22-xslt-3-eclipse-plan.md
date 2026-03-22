# XSLT 3.0 Eclipse Support Plan

## Summary

- Bootstrap a Tycho-based Eclipse feature/installable unit from this repository.
- Add `nl.indi.eclipse.xslt3.core` for Saxon-backed validation.
- Add `nl.indi.eclipse.xslt3.ui` for editor integration, syntax coloring, outline, and lightweight content assist.
- Produce a p2 repository and a helper script that launches a clean inspection Eclipse from this directory.

## Initial scope

- XSLT editor for `*.xsl` and `*.xslt`
- validation on save using Saxon-HE 12.8
- outline for common top-level declarations
- local sample project for manual testing

## Deferred

- debugger support
- formatter
- deep semantic navigation
- full WTP XSL codebase import/fork

