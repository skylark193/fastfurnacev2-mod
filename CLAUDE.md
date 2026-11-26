# CLAUDE.md

Project instructions for Claude Code. Keep this file focused on durable workflow and high-risk constraints; use linked documentation for details.

## Project

Fast Furnace V2 is a NeoForge mod for Minecraft 26.1.2, using Java 25 and Mojang official mappings. Its content includes tiered furnaces, crushers, generators, batteries, and power conduits, Fast and Extreme materials, dynamic crushed ores, an Accelerator, an Extremely Fast Alloyer, enhanced tools, and optional JEI integration.

- Current mod version: `1.12.0`; public API version: `1.4.0`.
- Mod ID and base package: `fastfurnacev2` and `com.skylark193.fastfurnacev2`.
- Versions and metadata: `gradle.properties` and `src/main/templates/META-INF/neoforge.mods.toml`.
- Player behavior and numeric tables: [`docs/README.md`](docs/README.md).
- Build, test, contribution, and versioning: [`docs/development.md`](docs/development.md).
- Code map and version-specific implementation constraints: [`docs/architecture.md`](docs/architecture.md).
- Texture sources, seeds, commands, and tests: [`tools/README.md`](tools/README.md).

## Start and finish

1. Inspect `git status`, recent commits, and relevant current files before editing. Preserve unrelated user changes.
2. Treat source, resources, and build configuration as authoritative. Verify current code instead of trusting old summaries or assuming a past fix remains.
3. Keep changes focused. Do not rewrite working systems merely for style; optimize only where the benefit is clear and regression checks cover the risk.
4. Update affected documentation and tests in the same change. When documentation and implementation disagree, verify the implementation, correct the documentation, and mention the correction.
5. Run required validation, review the complete staged diff, and run `git diff --cached --check`.
6. Commit validated changes on the appropriate release branch and push that branch automatically.
7. Merge the release branch into `main` with `--no-ff`, push `main` automatically, and return to the release branch.
8. Report what was validated and what was not, distinguishing compilation, automated tests, runtime testing, and visual/manual validation. Include commit hashes and push results.

Create `release/<major>.<minor>` from `main` for every MAJOR or MINOR mod update. Keep PATCH updates on the same minor release branch; use `main` only to integrate validated release branches. Never force-push or bypass merge conflicts. If validation fails, leave changes uncommitted; if a merge or push fails, preserve local work and report the exact failure.

## Commands

Use the included Gradle wrapper from the repository root:

```bash
# Windows
.\gradlew.bat build
.\gradlew.bat runGameTestServer
.\gradlew.bat runServer
.\gradlew.bat runData

# macOS / Linux
./gradlew build
./gradlew runGameTestServer
./gradlew runServer
./gradlew runData
```

Use `runClient` only when explicitly needed; do not automatically launch a Minecraft client. Use `publish` only for the ignored project-local `repo/` directory.

If Windows Gradle selects another JDK, set `JAVA_HOME` to JDK 25 and prepend its `bin` directory to `PATH`. See the [development guide](docs/development.md#environment).

## Required validation

- **Every commit:** `build`, full staged-diff review, and `git diff --cached --check`.
- **Machine, menu, fuel, or block-entity changes:** also `runGameTestServer`.
- **Texture generator changes:** also run the Python tests in [`tools/README.md`](tools/README.md#validate-the-generator).
- **Resource or metadata changes:** parse changed JSON/MCMETA/TOML where practical and inspect the packaged JAR for required files.
- **GUI changes:** use a development client for visual/input validation when possible.
- **Side-separation changes:** smoke-test the dedicated server when possible.

GameTests are registry-based tests in main Java sources, not JUnit tests. Judge results from the server log; a lingering Gradle launcher after successful shutdown is not itself a test failure. CI runs only `./gradlew build` with JDK 25.

JEI is `compileOnly` by default. JEI 29.2.0.21 supports Minecraft 26.1, not 26.1.2; compilation is not JEI runtime validation. Follow the [JEI opt-in procedure](docs/development.md#jei-validation-boundary) after selecting a compatible release.

## Version management

Automatically update versions as part of each completed change using [Semantic Versioning](https://semver.org/). Update them before final validation and commit, not for abandoned experiments.

- **Mod:** update `mod_version` in `gradle.properties`.
  - MAJOR for incompatible gameplay, save-data, dependency, or other breaking changes.
  - MINOR for backward-compatible features and content.
  - PATCH for backward-compatible fixes, refactors, cleanup, documentation, and resource corrections.
- **API:** independently update `FastFurnaceApi.API_VERSION` only when the supported surface under `com.skylark193.fastfurnacev2.api` changes.
  - MAJOR for incompatible API changes.
  - MINOR for backward-compatible API additions.
  - PATCH for compatible API contract clarifications or fixes.
- Synchronize version references and examples in documentation. Internal implementation and built-in content changes do not require an API bump.

See [Version management](docs/development.md#version-management) for the contributor-facing policy.

## High-risk invariants

Read the relevant [architecture notes](docs/architecture.md) before changing these areas:

- **Registration:** explicit vanilla-like block properties avoid copied loot/description keys. Every tool-required mod block needs the pickaxe-minable tag. Preserve block codecs that capture both tier and machine identity.
- **Timing:** furnaces double fuel duration; crushers double completed output. Pending crusher doubles never reroll or fall back to normal output. Paused work preserves fuel state, and fuel exhaustion preserves recipe progress.
- **Fuel:** use bonuses from the consumed stack's matched definition, not the current fuel slot. Preserve active-fuel IDs and legacy-save fallback.
- **Transactions:** NeoForge item snapshots do not restore custom progress or pending output. Journal deferred resets and apply them only on root commit; aborted transfers must preserve machine state.
- **Accelerator:** exact-work conversions require four horizontal lava sides. Ingredient acceptance and full-count processing are deliberately separate checks. Boost drain is clamped to remaining recipe and boost work.
- **Alloyer:** dedicated three-distinct-slot shapeless recipes only. Only the Extreme Coal item fuels it. Lava changes recalculate duration without resetting elapsed work.
- **Energy items:** battery drops preserve exact FE through `fastfurnacev2:stored_energy`. Item energy handlers must use NeoForge's supplied `ItemAccess` context; placement clamps restored FE to tier capacity.
- **Power conduits:** each connected network runs once per tick with a shared budget equal to its slowest tier. Chains, loops, buffers, and branches must not multiply throughput or permit same-tick backflow.
- **API initialization:** register definitions from `DefaultDataComponentsBoundEvent` on the mod bus when static data should update; common setup is too early to construct recipe `ItemStack`s. Patch tool defaults earlier through `ModifyDefaultComponentsEvent`.
- **Tools:** custom durability saving requires the API factory's `damageItem` route; other proc modifiers are ingredient-driven. Preserve direct-melee checks, correct-tool block drops, and deterministic roll boundaries.
- **Crushed ores:** during discovery, components and runtime tags are unbound. Reject mod-namespace raw items except the three explicit Fast raw materials to prevent recursive registration. Reuse registered external results and preserve their resources.
- **Client separation:** keep client classes and `NativeImage` out of common code. Do not rely on `@OnlyIn` runtime stripping in this NeoForge version.
- **Menus and JEI:** crusher identity arrives through menu synchronization, so select screen text and click behavior at draw time. Register shared transfer handlers once with machine filters; duplicate JEI registrations overwrite one another.
- **Textures:** generate only from preserved square sources. Use the all-lava generator where required and keep exact seeds/commands in `tools/README.md`.

## Code and comment style

- Prefer clear names, small helpers, and existing project patterns over clever compression.
- Remove dead code, unused imports, obsolete resources, and stale comments when touching the affected area.
- Inline comments should explain non-obvious reasons, lifecycle ordering, side boundaries, persistence compatibility, or timing constraints. Do not narrate obvious statements.
- Keep comments and Javadoc concise and synchronized with behavior. Remove commented-out code and duplicated explanations.
- Preserve deterministic random boundaries in GameTests and add regression coverage for behavior changes.

## Documentation continuity

- Keep `README.md` concise and link to detailed guides.
- Keep player/modpack information in `docs/`; keep contributor commands and policies in `docs/development.md`; keep implementation constraints in `docs/architecture.md`.
- Avoid duplicating numeric tables or long feature descriptions across pages. Link to the authoritative section instead.
- Record durable discoveries promptly: exact versions, APIs, failure symptoms, correct patterns, and rationale. Do not leave critical knowledge only in chat or a subagent report.
- Do not put transient branch names, current commit hashes, pending-task status, or long logs into permanent documentation.
- For multi-step work, maintain a concise running handoff with the goal, decisions, modified files, validation results, and next action. After compaction, compare it with Git status and current source before resuming.
- Never claim that earlier validation covers later edits.

## Safety

- Do not edit generated files under `build/` or Gradle caches.
- Do not take control of the device or cause mouse, keyboard, controller, touch, or other inputs for the user.
- Do not automatically launch a Minecraft client.
