# CLAUDE.md

This file is the durable engineering handbook for Claude Code working in this repository. Keep implementation and validation knowledge here; keep `README.md` focused on users and contributors.

## Project Contract

Fast Furnace V2 is a NeoForge mod for Minecraft 26.1.2. Its registered content is:

- `fastfurnacev2:fast_cobblestone`: vanilla-style cobblestone with an animated lava-vein texture, a self-drop loot table, and use in the Fast Furnace crafting recipe.
- `fastfurnacev2:fast_coal`: vanilla-style coal with an animated texture and ordinary fuel value of 1,600 ticks.
- `fastfurnacev2:fast_furnace`, `iron_fast_furnace`, `gold_fast_furnace`, and `diamond_fast_furnace`: vanilla-compatible smelting furnaces with tiered speed, fuel-time, GUI, and optional JEI behavior.

| Tier | Normal speed | Fast Coal speed | Standard recipe, normal / Fast Coal | Fuel doubling | Fast Coal burn time before / after doubling |
|---|---:|---:|---:|---:|---:|
| Fast | 150% | 200% | 133 / 100 ticks | 10% | 2,400 / 4,800 ticks |
| Iron | 200% | 250% | 100 / 80 ticks | 15% | 2,800 / 5,600 ticks |
| Gold | 250% | 300% | 80 / 66 ticks | 20% | 3,200 / 6,400 ticks |
| Diamond | 300% | 350% | 66 / 57 ticks | 25% | 3,600 / 7,200 ticks |

Each consumed fuel item independently rolls the tier's chance to double its adjusted burn time. Fast Coal always adds 50 percentage points to active smelting speed; its duration multiplier increases by 25 percentage points per tier.

Mining stone or blocks in `BlockTags.COAL_ORES` can add matching Fast Cobblestone or Fast Coal at a base 10% chance, plus 5 percentage points per Fortune level and capped at 100%. These are bonuses alongside normal drops, not replacements. Fast Furnace is crafted from eight Fast Cobblestone using the vanilla furnace pattern. Each upgrade surrounds the previous tier with eight iron, gold, then diamond blocks in tier order.

All content appears in the Fast Furnace V2 creative tab, whose icon is Fast Cobblestone. Vanilla tabs are Building Blocks, Ingredients, and Functional Blocks respectively.

## Source of Truth and Session Startup

1. Current source, resources, `gradle.properties`, and `build.gradle` are authoritative. Conversation summaries and documentation can be stale.
2. At session start, inspect `git status`, recent commits, and the files relevant to the request before editing. Preserve unrelated user changes.
3. Use Git history for past changes, but do not assume a past fix still exists without checking the current code.
4. When documentation and implementation disagree, verify the implementation, correct the documentation, and mention the correction.
5. Do not rely on chat context as the only record of a non-obvious decision. Promote durable constraints and their rationale into this file or `tools/README.md` in the same change.

## Toolchain and Metadata

- Java 25; Mojang official mappings.
- Gradle 9.2.1 through the included wrapper; do not require a system Gradle installation.
- NeoForge ModDev Gradle plugin 2.0.148.
- Minecraft 26.1.2; NeoForge 26.1.2.126.
- Mod ID `fastfurnacev2`; mod version 1.0.0; group `com.skylark193.fastfurnacev2`.
- JEI 29.2.0.21 common and NeoForge APIs, using the BlameJared Maven repository.
- Optional texture tooling uses Python 3 and Pillow 13.0.0 in an ignored `.venv`; see `tools/README.md`.

Versions and mod metadata live in `gradle.properties`. Keep `mod_id` synchronized with `FastFurnaceV2.MODID`, resource namespaces, and package references. Keep Minecraft, NeoForge, JEI API, and JEI runtime versions compatible.

Edit `src/main/templates/META-INF/neoforge.mods.toml` and `gradle.properties` for metadata changes. `generateModMetadata` expands the template into `build/generated/sources/modMetadata`; never edit generated build output.

On the current Windows Git Bash setup, select JDK 25 explicitly if Gradle resolves another Java version:

```bash
JAVA_HOME='C:\Program Files\Microsoft\jdk-25.0.5.21-hotspot' \
PATH='/c/Program Files/Microsoft/jdk-25.0.5.21-hotspot/bin':"$PATH" \
./gradlew.bat build
```

## Commands and Validation

Use `.\gradlew.bat` on Windows and `./gradlew` on macOS/Linux.

```bash
# Primary Java/resource validation; also used by CI
build

# Development launches
runClient
runServer

# Furnace behavior tests
runGameTestServer

# Generate resources into src/generated/resources
runData

# Publish only to the ignored project-local repo/ directory
publish
```

For example, run `.\gradlew.bat build` or `./gradlew build` from the repository root. Built JARs go to `build/libs/`.

### Required checks by change type

- **Every commit:** run `build`, review the complete staged diff, and run `git diff --cached --check`.
- **Furnace, menu, fuel, or block-entity changes:** also run `runGameTestServer`.
- **Texture generator changes:** also run `.venv/Scripts/python -I -m unittest discover -s tools -p 'test_*.py'` on Windows or `.venv/bin/python -I -m unittest discover -s tools -p 'test_*.py'` on macOS/Linux.
- **Resource or metadata changes:** parse changed JSON/MCMETA/TOML where practical and inspect the packaged JAR for required files.
- **GUI changes:** use a development client for visual/input validation when possible; compilation does not validate layout or interaction.
- **Side-separation changes:** smoke-test the dedicated server when possible.

The GameTests are registry-based tests in main Java sources, not JUnit tests under `src/test`. They cover ordinary and Fast-Coal-boosted smelting, every tier's normal and Fast Coal timing, tier-specific fuel configuration, doubled fuel durations, exact countdown synchronization, lava-bucket remainders, active-fuel synchronization, menu tiers, JEI smelt-count math, and Fortune-scaled bonus-drop chances. Judge their result from the test-server log; a lingering Gradle launcher after a successful server shutdown is not itself a test failure.

Texture unit tests cover generator determinism, distinct frames, path bounds, source preservation, and transparency. They do not prove CLI regeneration or byte-for-byte reproduction of every checked-in PNG/MCMETA file; validate generated assets separately when needed.

GitHub Actions runs only `./gradlew build` with JDK 25. It does not run GameTests, texture tests, data generation, a client/server smoke test, or JEI runtime tests.

### JEI validation boundary

JEI APIs are `compileOnly`. JEI 29.2.0.21 declares Minecraft `[26.1,26.1]` and cannot launch on Minecraft 26.1.2, so normal development launches exclude its runtime. Compilation and dependency resolution are not JEI runtime validation.

After selecting a JEI release compatible with Minecraft and the integration API, update `jei_version` and, if needed, `jei_minecraft_version`, then opt in with `-PwithJei`. The Gradle check uses property presence: even `-PwithJei=false` enables `localRuntime`. Never bypass JEI's own version requirement or claim runtime validation without launching it.

## Architecture Map

- `FastFurnaceV2.java`: common entry point; all `DeferredRegister` instances; block/item/block-entity/menu/GameTest/creative-tab registration; tier lookup helpers; vanilla tab population; event listeners.
- `FastFurnaceTier.java`: shared source of truth for registry IDs, normal/Fast Coal speed, doubling chance, Fast Coal duration scaling, cook-time scaling, and JEI smelt counts.
- `FastBonusDrops.java`: appends Fast Cobblestone or Fast Coal to eligible vanilla drops.
- `FastFurnaceBlock.java`: tier-aware block codec, facing/lit behavior, server ticker, menu opening, statistics, fire sounds, and particles.
- `FastFurnaceBlockEntity.java`: shared block entity for all four blocks; custom cooking and burn timing, fuel consumption, active-fuel state, persistence, and server-to-menu data.
- `FastFurnaceMenu.java`: shared menu with four tier-specific `MenuType` registrations, vanilla smelting recipe-book setup, and nine synchronized data values.
- `FastFurnaceGameTests.java`: registered GameTests and custom direct function instance codec.
- `client/FastFurnaceV2Client.java`: client-only registration of each tier's menu and screen.
- `client/FastFurnaceScreen.java`: shared furnace screen, information button/fly-out, timer formatting, and JEI exclusion bounds. The three upgrade screens are trivial subclasses used to distinguish JEI click areas.
- `compat/jei/FastFurnaceJeiPlugin.java`: optional per-tier catalysts, recipe transfers, click areas, ingredient information, fuel enumeration, and GUI handlers.
- `compat/jei/FastFurnaceFuelCategory.java`: per-tier fuel categories and speed-adjusted smelt counts.
- `compat/jei/FastFurnaceFuelRecipe.java`: immutable JEI fuel inputs and burn time.
- `src/main/resources/assets/fastfurnacev2/`: blockstates, item definitions, models, translations, and animated textures.
- `src/main/resources/data/`: self-drop loot tables, the furnace recipe, pickaxe-minable tag, and Fast Coal fuel data map.
- `tools/`: deterministic texture generator, tests, preserved square sources, and detailed regeneration instructions.
- `.github/workflows/build.yml`: Java 25 CI build.

`src/generated/resources` is configured for data generation but currently contains no tracked generated resources. Do not edit Gradle caches or files under `build/`.

## Implementation Invariants

### Registration, drops, and resources

- Register every `DeferredRegister` with the mod event bus. Add translations for new registry objects and user-facing text.
- Fast Cobblestone explicitly mirrors vanilla cobblestone: stone map color, base-drum instrument, `requiresCorrectToolForDrops()`, and strength 2.0/6.0. The mineable tag supplies pickaxe behavior; its loot table explicitly drops Fast Cobblestone.
- Do not use `BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)`: it also copies vanilla's loot-table key and description ID, causing incorrect drops or translations.
- `FastBonusDrops` runs during NeoForge's server-side `BlockDropsEvent`, after vanilla drops are calculated. Require a non-creative player breaker, enabled block drops, a correct tool, and no Silk Touch. `getDropChance` starts at 10% and adds 5 percentage points per Fortune level, capped at 100% for nonstandard levels. Append to `event.getDrops()`; never clear or replace normal drops. `BlockTags.COAL_ORES` covers coal ore and deepslate coal ore.
- Fast Coal uses default vanilla item properties and `data/neoforge/data_maps/item/furnace_fuels.json` with `burn_time: 1600`. Only Fast Furnace applies its longer duration and speed boost.
- This Minecraft version uses `assets/<namespace>/items/` for item model definitions. Fast Coal additionally has a generated item model under `models/item/`.
- Fast Furnace's loot table drops itself and copies `minecraft:custom_name`; it does not preserve inventory, XP, or burn state when broken.
- Every furnace blockstate maps all four facings and both `lit` values to `minecraft:block/orientable` models. Each tier has separate front, lit-front, side, and top textures; upgrade sources combine their metal block's pattern with vanilla furnace shading and preserve burning-fire pixels. Do not replace these with `cube_all` models, which would erase furnace fronts and visual lit states.

### Furnace ticking and synchronization

- One `BlockEntityType` accepts all four furnace blocks and is constructed with this version's public `BlockEntityType` constructor; `BlockEntityType.Builder` is unavailable. The block entity derives its immutable tier from the actual block state.
- Each block uses a tier-capturing codec; a shared codec that always constructs the base tier would decode upgrades incorrectly.
- The ticker must use `FAST_FURNACE_BLOCK_ENTITY` and call `FastFurnaceBlockEntity.serverTick` through `createTickerHelper`. Vanilla `createFurnaceTicker` would bypass custom timing.
- Retain the custom `openContainer` block-entity check; vanilla `FurnaceBlock` specifically checks `FurnaceBlockEntity`.
- `FastFurnaceBlockEntity` extends `AbstractFurnaceBlockEntity` with `RecipeType.SMELTING`. Preserve vanilla recipes, other fuels, inventory, XP, comparators, fuel remainders, facing, light level 13 while lit, and custom-name drops.
- Scale cooking through `FastFurnaceTier.scaleCookTime`: 150/200/250/300% normally and an additional 50 percentage points with active Fast Coal. Standard normal-fuel recipes take 133/100/80/66 ticks; Fast Coal recipes take 100/80/66/57 ticks.
- Apply Fast Coal's tier duration multiplier through `scaleFastCoalBurnTime`, then apply the independent tier-specific doubling roll. Before doubling, Fast Coal burns for 2,400/2,800/3,200/3,600 ticks. Do not globally alter Fast Coal's ordinary fuel value.
- Track `fastCoalBurning` from the fuel stack actually consumed, not the current fuel slot, which may already hold different fuel. Clear the flag when burn time expires and persist it as `fast_coal_burning`.
- Exact remaining and total burn times are stored under `lit_time_remaining` and `lit_total_time` and mirrored into vanilla data for furnace behavior.
- `FastFurnaceMenu` has nine data slots. Indices 0–3 retain vanilla behavior; 4–7 contain low/high halves of the exact remaining and total fuel times; index 8 is the active Fast Coal flag. Use the split integers instead of vanilla short-value normalization or a custom per-tick packet.

### Client GUI and JEI

- Keep common logic separate from client-only code. Client entry points use `@Mod(value = MODID, dist = Dist.CLIENT)`. Client classes and Minecraft client references must stay out of dedicated-server paths.
- Do not add `@OnlyIn` for runtime stripping; NeoForge 26.1.2 no longer provides that behavior.
- Four `MenuType` registrations carry the tier to the client; all use the same `FastFurnaceMenu` logic and nine-value synchronization. The upgrade screens share `FastFurnaceScreen` through trivial subclasses so JEI can register tier-specific click areas.
- `FastFurnaceScreen` draws the information button and fly-out in `extractLabels`. It gets normal and Fast Coal speed and doubling chance from `menu.getTier()`, choosing current speed from synchronized active-fuel state.
- Format timer numbers explicitly in Java with `Locale.ROOT` and `%.1f`. Right-align the value and draw the translated seconds unit separately at a fixed X coordinate; do not move numeric formatting back into translations or allow units to shift with number width.
- `getJeiExtraAreas` uses current GUI coordinates and returns bounds only while the fly-out is visible.
- Register every tier for `RecipeTypes.SMELTING` and its own `FastFurnaceFuelCategory` recipe type, including transfers and click areas for each.
- The custom fuel categories enumerate item fuels once from the current level's `FuelValues`, then create per-tier recipes applying `scaleFastCoalBurnTime` only to Fast Coal. Smelt counts use `burnTime / tier.scaleCookTime(200, fastCoalActive)`, not vanilla's fixed 200-tick divisor. Regular coal displays approximately 12.03/16/20/24.24 smelts; Fast Coal displays 24/35/48.48/63.16 before any random doubling roll.
- Do not modify `RecipeTypes.SMELTING_FUEL` to implement Fast Furnace durations; that would incorrectly change ordinary furnace displays.

### GameTests and textures

- GameTests use `RegisterGameTestsEvent` plus the registered `fastfurnacev2:function` instance codec. Vanilla's `TestFunctionLoader` bootstraps before mod construction, so registering a loader during mod/class initialization is too late. `DirectGameTestInstance` invokes the mod's mapped functions directly.
- Preserve deterministic fuel-roll seeds in tests when asserting doubled versus non-doubled durations. Extend existing tests when timing, synchronization, remainders, or active-fuel behavior changes.
- Use the square sources under `tools/texture_sources/`; never feed a generated animation strip back into the generator. It writes both the vertical PNG strip and sibling `.png.mcmeta`.
- Production seeds: Fast Cobblestone 193; Fast Coal 415; all furnace textures 311. The shared furnace seed is intentional so every tier and lit state has the same vein/crust mask and tiles with the others. Default animation is eight frames, two ticks per frame, non-interpolated, with two primary veins.
- Follow `tools/README.md` for environment setup and exact regeneration commands. Keep new sources and seeds documented.

## Documentation, Continuity, and Compaction

1. Review `README.md` and `CLAUDE.md` with every project change and update whichever is affected. README contains player/contributor information only; this file contains architecture, constraints, commands, and agent workflow.
2. Record durable discoveries promptly: exact versions, class/method names, failure symptoms, correct patterns, and the reason behind non-obvious decisions. Do not leave critical knowledge only in reasoning, tool output, or a completed subagent report.
3. Do not put transient branch names, current commit hashes, pending-task status, or long logs into permanent documentation. Keep those in the active task handoff and Git history.
4. For multi-step work, maintain a concise running handoff containing the user's goal, decisions made, modified/uncommitted files, exact validation commands and results, and the next action. Refresh it before compaction or interruption.
5. After compaction, compare the handoff with `git status`, staged/unstaged/untracked files, and current source before resuming. Do not redo completed work or accidentally discard pending changes.
6. Earlier validation may be reused only if the relevant files have not changed and the result is known. Never claim that a previous test run covers later edits.
7. At completion, report what was actually validated, what was not run, and the commit hash when committed. Clearly distinguish compilation, automated tests, runtime testing, and visual/manual validation.

## Working Rules

1. Make focused changes, remove obsolete code/imports/resources, and keep user-facing behavior stable unless the request changes it.
2. Inspect local dependency sources or mapped APIs when version-specific behavior is uncertain instead of guessing from another Minecraft/NeoForge/JEI version.
3. Commit all changes that pass validation, as requested by the maintainer. Run the build before every commit and never commit while relevant validation is failing.
4. Before committing, review the full diff and stage only intended files. Include documentation updates in the same validated commit as their implementation.
5. If validation cannot be run or fails, report the exact limitation or failure and leave the changes uncommitted.
