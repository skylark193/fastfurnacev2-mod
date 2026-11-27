# Development guide

[Documentation home](README.md) · [Architecture notes](architecture.md) · [Texture tooling](../tools/README.md)

This page is for contributors building or modifying Fast Furnace V2. Player behavior and balance values are documented in the player guides rather than repeated here.

## Environment

| Tool | Required version |
|---|---|
| JDK | 25 |
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.126 |
| ModDev Gradle plugin | 2.0.148 |
| JEI API | 29.2.0.21, compile-only by default |
| Jade API | 26.1.12+neoforge, compile-only by default |

Use the included **Gradle 9.2.1 wrapper**; a system Gradle installation is unnecessary. Dependency and mod metadata versions live in [`gradle.properties`](../gradle.properties).

On Windows, if Gradle selects another JDK, set `JAVA_HOME` to a JDK 25 installation and prepend its `bin` directory to `PATH` before invoking `gradlew.bat`.

## Common commands

Run these from the repository root:

| Task | Windows | macOS / Linux |
|---|---|---|
| Build and check | `.\gradlew.bat build` | `./gradlew build` |
| Run GameTests | `.\gradlew.bat runGameTestServer` | `./gradlew runGameTestServer` |
| Launch development server | `.\gradlew.bat runServer` | `./gradlew runServer` |
| Launch development client | `.\gradlew.bat runClient` | `./gradlew runClient` |
| Generate data resources | `.\gradlew.bat runData` | `./gradlew runData` |
| Publish to local `repo/` | `.\gradlew.bat publish` | `./gradlew publish` |

The build writes `fastfurnacev2-<minecraft_version>.jar` to `build/libs/`; the distribution filename uses the Minecraft target rather than `mod_version`. Mod metadata and Maven coordinates continue to use `mod_version`. GameTests are registry-based tests in main Java sources, not JUnit tests under `src/test`. Judge their result from the server log; a lingering Gradle launcher after successful shutdown is not itself a test failure.

GitHub Actions runs `./gradlew build` with JDK 25. It does not run GameTests, texture tests, data generation, JEI runtime tests, or a client/server smoke test.

## Validation checklist

Run the checks affected by your change and report exactly what ran:

- **Every commit:** `build`, a complete staged-diff review, and `git diff --cached --check`.
- **Machine, menu, fuel, or block-entity changes:** also `runGameTestServer`.
- **Texture generator changes:** also run the Python unit tests documented in [tools/README.md](../tools/README.md#validate-the-generator).
- **Resource or metadata changes:** parse changed JSON, MCMETA, or TOML where practical and inspect the packaged JAR for required files.
- **GUI changes:** use a development client for visual and input validation when possible; compilation does not validate layout or interaction.
- **Side-separation changes:** smoke-test a dedicated server when possible.

Do not claim that an earlier test covers later edits. If validation fails or cannot run, leave the change uncommitted and report the exact limitation.

## JEI validation boundary

JEI APIs are `compileOnly`. JEI 29.2.0.21 declares Minecraft `[26.1,26.1]` and cannot launch on Minecraft 26.1.2, so normal development launches exclude its runtime.

After selecting a compatible JEI release, update `jei_version` and, if needed, `jei_minecraft_version`, then opt in with `-PwithJei`. Gradle checks property presence: even `-PwithJei=false` enables `localRuntime`. Never bypass JEI's version requirement or claim runtime validation without launching it.

## Jade validation boundary

Jade APIs are `compileOnly` by default. Use `-PwithJade` to add the configured Jade runtime to development launches; as with JEI, property presence enables it. Server-side plugin loading can be checked with `./gradlew runGameTestServer -PwithJade`, but tooltip layout and client interaction still require a development client. Do not automatically launch that client or claim visual validation from compilation alone.

## Version management

Use [Semantic Versioning](https://semver.org/) and update versions as part of each completed change, before validation and commit.

### Mod version

Update `mod_version` in `gradle.properties`:

- **MAJOR:** incompatible gameplay, save-data, dependency, or other breaking changes.
- **MINOR:** backward-compatible features, content, or public integrations.
- **PATCH:** backward-compatible fixes, refactors, cleanup, documentation, and resource corrections.

Update version references in documentation and dependency examples in the same change. Do not bump versions for abandoned experiments or uncommitted work in progress.

### Public API version

The API version is independent of the mod version. Update `FastFurnaceApi.API_VERSION` only when the supported surface under `com.skylark193.fastfurnacev2.api` changes:

- **MAJOR:** remove or change existing API behavior in an incompatible way.
- **MINOR:** add backward-compatible API capabilities.
- **PATCH:** clarify or fix API behavior without changing its compatible contract.

Keep `API_VERSION` and [the API guide](api.md) synchronized. Internal implementation changes and built-in content additions do not require an API bump. Do not change the version merely to match the mod version.

## Change workflow

1. Inspect current source, Git status, recent history, and the files relevant to the request. Preserve unrelated user changes.
2. Treat source, resources, and build configuration as authoritative. Documentation and conversation summaries may be stale.
3. Make focused changes. Remove obsolete code, imports, comments, and resources without changing behavior unintentionally.
4. Add or extend regression tests for behavior changes, especially timing, synchronization, persistence, transactions, and random-roll boundaries.
5. Update affected player or developer documentation in the same change. Keep one authoritative home for each topic and link to it rather than duplicating tables.
6. Run required validation, review the complete staged diff, and commit only intended files on the appropriate release branch.
7. Push the release branch, merge it into `main`, and push `main` automatically. Return to the release branch for subsequent patches.
8. Report compilation, automated tests, runtime testing, visual/manual validation, commit hashes, and push results separately, including anything not run.

## Branch, commit, and safety rules

- For every MAJOR or MINOR mod update, create `release/<major>.<minor>` from `main`, such as `release/2.0` or `release/1.6`. Keep all PATCH updates for that series on the same release branch rather than creating per-patch branches.
- Use `main` to integrate validated release branches, not for ongoing development. Commit all changes that pass validation when working on a maintainer-requested task.
- After each commit, automatically push the release branch, merge it into `main` with `--no-ff`, and push `main`. No additional push or merge confirmation is required. Return to the release branch afterward.
- Never force-push or bypass merge conflicts. If a merge or push fails, stop, preserve local work, and report the exact failure.
- Never commit while relevant validation is failing.
- Do not edit generated files under `build/` or Gradle caches. Edit `src/main/templates/META-INF/neoforge.mods.toml` and `gradle.properties` for metadata; `generateModMetadata` creates the build output.
- Do not automatically launch a Minecraft client or control device inputs.
- Keep implementation and validation knowledge in `CLAUDE.md` and linked developer documentation; keep the root README concise.

## Comments and documentation style

- Prefer clear names and straightforward control flow over clever compression.
- Comment on non-obvious reasons, lifecycle constraints, side boundaries, persistence compatibility, and timing invariants—not on statements the code already expresses.
- Keep inline comments concise and current. Remove commented-out code, narration, and duplicated Javadoc.
- Use short sections, task-oriented headings, tables for comparable values, and links to detailed references.
- Do not put transient branch names, commit hashes, pending-task status, or long logs into permanent documentation.
