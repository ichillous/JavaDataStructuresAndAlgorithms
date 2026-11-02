# Working in this repository

This is a Java 17 educational library with historical Markdown study notes.

- Keep implementations under `src/main/java/io/github/ichillous/dsa` and JUnit
  Jupiter tests under matching packages in `src/test/java`.
- Document invariants, mutation/ownership, null/duplicate/empty behavior, bounds,
  and time/space assumptions. Prefer readable code and the JDK over dependencies.
- Test edge cases and compare against an independent reference model where practical.
  Use fixed seeds. Test behavior rather than private implementation details,
  except explicit structural invariants such as AVL balance.
- Validate with `./mvnw clean verify`, run the packaged JAR, and run
  `python3 scripts/check_docs.py`. Compiler warnings are errors. Do not describe
  focused tests as a complete build.
- Keep `README.md`, `docs/catalog.md`, and `docs/complexity.md` consistent with APIs.
  Preserve existing note paths and historical-note banners; Markdown snippets
  are explanatory and are not compiled by Maven.
- Do not commit generated `target/` content, IDE settings, credentials, or tool caches.
  Preserve existing history and inspect concurrent remote changes before pushing.
