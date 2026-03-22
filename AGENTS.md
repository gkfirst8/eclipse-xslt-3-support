# AGENTS

## Planning conventions
- Plans should always be written to a file in `plans/` directory.
- When a plan is requested, always write it to the `plans/` directory.
- Plan filenames must include a global 3-digit sequence number and an ISO date.
- Preferred filename format: `###-YYYY-MM-DD-short-topic-plan.md`.

## Coding Style & Naming Conventions
- Java 21 features are available; keep 4-space indentation and brace placement consistent with existing classes.
- Lombok annotations (`@Builder`, `@Getter`) are standard; prefer them over manual boilerplate.
- For small value objects and context carriers, prefer Lombok builders over handwritten constructors/getters unless there is a concrete reason not to. Use `@Builder.Default` for mutable collection fields that need a safe default instance.
- Apply your IDE formatter or `mvn fmt:format` (if configured) before committing to keep imports tidy.
- When splitting long argument lists or builder chains, retain the trailing `//` markers already in the code or add them when appropriate to keep Eclipse from reflowing the formatting. Never add this right after ';' or '{'!
- Box primitives explicitly when passing them into varargs APIs such as `String.format` (e.g. `Integer.valueOf(...)`) to avoid auto-boxing warnings.
- When ordering method parameters, place broader context first and operational details later.
- Prefer this order when practical:
  - execution/context objects
  - primary subject/input being acted on
  - supporting configuration or identifiers
  - trailing detail values, flags, or labels
- In short: a context parameter should appear as early as possible in the signature, before the lower-level values it governs.

## Testing Guidelines
- Tests rely on JUnit 5, AssertJ. Name new classes `*Test` in the matching package.
- For a quick test of a class just adjusted/created use direct running only the tests for that specific class: `mvn test -Dtest=NewOrUpdatedClassTest`.
- For a quick test of all classes in a package please use `mvn test '-Dtest=nl.indi.csv_to_database.attributes.**.*Test'` for example (as some tests are very slow).
- Extend existing datasets under `src/test/resources/db/...` and follow Flyway naming (`V#__Description.sql`).
- Cover CSV parsing edge cases (NULL markers, delimiter handling) and database state transitions; add regression tests for new scenarios.
