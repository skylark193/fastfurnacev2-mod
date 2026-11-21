# CLAUDE.md

This file is the durable engineering handbook for Claude Code working in this repository. Keep implementation and validation knowledge here; keep `README.md` focused on users and contributors.

## Project Contract

Fast Furnace V2 is a NeoForge mod for Minecraft 26.1.2. Its registered content is:

- `fastfurnacev2:fast_cobblestone`: vanilla-style cobblestone with an animated lava-vein texture, a self-drop loot table, and use in the Fast Furnace crafting recipe.
- `fastfurnacev2:fast_coal`: vanilla-style coal with an animated texture and ordinary fuel value of 1,600 ticks.
- `fastfurnacev2:fast_coal_block`: a reversible nine-Fast-Coal storage block with vanilla coal-block properties, an animated lava-vein texture, and ordinary fuel value of 16,000 ticks.
- `fastfurnacev2:accelerator`: a lava-surrounded coal converter with an animated smooth-stone/lava-vein body and an eleven-stage top-face lightning bolt. It converts coal to Fast Coal in 100 work ticks and coal blocks to Blocks of Fast Coal in 900. It runs without fuel, accepts only Fast Coal or its block for an optional 3× boost, and supplies 900 or 1,000 boosted work units respectively.
- `fastfurnacev2:fast_furnace`, `iron_fast_furnace`, `gold_fast_furnace`, and `diamond_fast_furnace`: vanilla-compatible smelting furnaces with tiered speed, fuel-time, GUI, and optional JEI behavior.
- `fastfurnacev2:fast_crusher`, `iron_fast_crusher`, `gold_fast_crusher`, and `diamond_fast_crusher`: fuel-powered raw-ore crushers with the corresponding furnace tier's speed and no fuel-duration doubling. Each raw ore produces two matching crushed items instead of using its smelting result, with an independent 10%/15%/20%/25% tier chance to produce four. A successful doubled result that cannot fit remains pending and pauses processing rather than falling back to two. They have animated Netherrack sides and bottoms plus top-down blenders that are static while idle and animated while lit; upgrade tiers add their metal block's texture around the blender housing. The base Fast Crusher has no crafting recipe; upgrades surround the previous crusher with eight iron, gold, then diamond blocks.

| Tier | Normal speed | Fast Coal speed | Standard recipe/operation, normal / Fast Coal | Furnace fuel doubling / crusher output doubling | Furnace Fast Coal burn time before / after doubling |
|---|---:|---:|---:|---:|---:|
| Fast | 150% | 200% | 133 / 100 ticks | 10% | 2,400 / 4,800 ticks |
| Iron | 200% | 250% | 100 / 80 ticks | 15% | 2,800 / 5,600 ticks |
| Gold | 250% | 300% | 80 / 66 ticks | 20% | 3,200 / 6,400 ticks |
| Diamond | 300% | 350% | 66 / 57 ticks | 25% | 3,600 / 7,200 ticks |

In the four Fast Furnace tiers, each consumed fuel item independently rolls the tier's chance to double its adjusted burn time. Fast Crushers never make this roll; each completed crushing operation independently rolls its tier's 10%/15%/20%/25% chance to double its two-item crushed output. A successful roll remains pending until all four items fit, without consuming the raw input or falling back to normal output. Fast Coal and Block of Fast Coal always add 50 percentage points to active processing speed; their duration multiplier increases by 25 percentage points per tier. Before doubling, the block burns for 24,000/28,000/32,000/36,000 ticks by tier.

All four Fast Furnace tiers and all four Fast Crusher tiers pause their active fuel countdown whenever no required result can be processed, including empty or unprocessable input, blocked output, and a pending crusher double that cannot fit. The countdown resumes with valid work without losing active Fast Coal state or consuming additional fuel merely to idle. When fuel expires during an operation, recipe progress remains saved without cooling down and resumes when replacement fuel is supplied.

Mining stone or blocks in `BlockTags.COAL_ORES` can add matching Fast Cobblestone or Fast Coal at a base 10% chance, plus 5 percentage points per Fortune level and capped at 100%. These are bonuses alongside normal drops, not replacements. Fast Furnace is crafted from eight Fast Cobblestone using the vanilla furnace pattern. Each furnace upgrade surrounds the previous tier with eight iron, gold, then diamond blocks in tier order. The base Fast Crusher has no recipe, but its three upgrades use the same material progression around the previous crusher tier. Block of Fast Coal uses a full 3×3 Fast Coal recipe and unpacks into nine Fast Coal.

All content appears in the Fast Furnace V2 creative tab, whose icon is Fast Cobblestone. Fast Cobblestone and Block of Fast Coal also appear in Building Blocks, Fast Coal and crushed ores in Ingredients, and all furnaces and crushers in Functional Blocks.

The mod also discovers vanilla and modded raw ore items during registration and selects one crushed result per source. Discovery recognizes conventional `raw_...`/`..._raw`/`..._raw_ore` IDs and direct or nested entries in packaged `c:raw_materials` item tags. Matching external crushed items or dusts are reused instead of duplicated; conventional IDs and packaged `c:dusts`/`c:crushed_ores` tags supply candidates. Generated variants use a shared dust-pile model tinted from the source item texture. Generated ordinary smelting recipes preserve the raw recipe's output, experience, cooking time, category, and group, while existing external smelting recipes are reused, so both kinds work in vanilla and Fast Furnaces.

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

The GameTests are registry-based tests in main Java sources, not JUnit tests under `src/test`. They cover ordinary and Fast-Coal-boosted smelting, every tier's normal and Fast Coal timing, every crusher tier's raw-ore-only processing, two/four crushed output, normal and Fast Coal timing, menu identity, disabled fuel doubling, deterministic tier-specific output rolls, and blocked doubled-output pause/resume without rerolling, Fast Coal Block's ordinary and per-tier burn times, doubled fuel durations, exact countdown synchronization, fuel-countdown pausing and resumption for every furnace and crusher tier with coal and Fast Coal, saved recipe progress through fuel exhaustion and refueling for every block and both fuels, lava-bucket remainders, active-fuel synchronization, menu tiers and crusher identity, JEI smelt-count math, Fortune-scaled bonus-drop chances, and crushed-ore discovery, FTB/Create-style external ID matching, English fallback naming, generated-recipe equivalence, and external-recipe availability in ordinary and Fast Furnaces. Accelerator coverage includes exact 100/900-tick unboosted conversions, all-four-side lava requirements, unsupported slot/automation inputs, 34/300-tick boosted conversions, exact nine/ten-coal and one-block fuel capacities, remaining block-fuel work, blocked-output and missing-lava pause/resume, input-change reset, menu values, and lightning-bolt block-state progress. Judge their result from the test-server log; a lingering Gradle launcher after a successful server shutdown is not itself a test failure.

Texture unit tests cover generator determinism, distinct frames, path bounds, source preservation, transparency, the Accelerator bolt's monotonic fill and stationary outline, the crusher top's stationary housing, and visible, distinct, stationary tier-metal trim. They do not prove CLI regeneration or byte-for-byte reproduction of every checked-in PNG/MCMETA file; validate generated assets separately when needed.

GitHub Actions runs only `./gradlew build` with JDK 25. It does not run GameTests, texture tests, data generation, a client/server smoke test, or JEI runtime tests.

### JEI validation boundary

JEI APIs are `compileOnly`. JEI 29.2.0.21 declares Minecraft `[26.1,26.1]` and cannot launch on Minecraft 26.1.2, so normal development launches exclude its runtime. Compilation and dependency resolution are not JEI runtime validation.

After selecting a JEI release compatible with Minecraft and the integration API, update `jei_version` and, if needed, `jei_minecraft_version`, then opt in with `-PwithJei`. The Gradle check uses property presence: even `-PwithJei=false` enables `localRuntime`. Never bypass JEI's own version requirement or claim runtime validation without launching it.

## Architecture Map

- `FastFurnaceV2.java`: common entry point; all `DeferredRegister` instances; block/item/block-entity/menu/GameTest/creative-tab registration; tier lookup helpers; vanilla tab population; event listeners.
- `FastFurnaceTier.java`: shared source of truth for registry IDs, normal/Fast Coal speed, doubling chance, Fast Coal duration scaling, cook-time scaling, and JEI smelt counts.
- `FastBonusDrops.java`: appends Fast Cobblestone or Fast Coal to eligible vanilla drops.
- `AcceleratorBlock.java`: directional block, lava-independent placement, progress-level block state, server ticker, and menu opening.
- `AcceleratorBlockEntity.java`: fixed coal conversions, four-side lava checks, optional exact-work Fast Coal boosts, automation restrictions, persistence, and menu synchronization.
- `AcceleratorMenu.java`: three-slot menu with restricted input/fuel/output slots, shift-click routing, and six synchronized values.
- `client/AcceleratorScreen.java`: furnace-style GUI without a smelting recipe book, boost/progress sprites, Accelerator information fly-out, and JEI exclusion bounds.
- `CrushedOres.java`: registry-time raw ore discovery, external crushed-item selection, fallback dynamic registration, creative contents, and cloned smelting JSON generation.
- `CrushedOreItem.java`: generated-item source mapping and material-specific display names.
- `CrushedOreResources.java`: always-enabled in-memory client resource pack containing per-variant item definitions only for items generated by this mod.
- `FastFurnaceBlock.java`: tier- and machine-identity-aware block codec, facing/lit behavior, server ticker, menu opening, statistics, fire sounds, and particles.
- `FastFurnaceBlockEntity.java`: shared block entity for all four furnaces and all four crushers; custom cooking and burn timing, raw-ore crushing, pending doubled output, fuel consumption, active-fuel state, persistence, and server-to-menu data.
- `FastFurnaceMenu.java`: shared menu with four tier-specific `MenuType` registrations, vanilla smelting recipe-book setup, and ten synchronized data values. Each crusher reuses its corresponding furnace tier's menu and synchronizes its block identity for crusher-specific screen text.
- `FastFurnaceGameTests.java`: registered GameTests and custom direct function instance codec.
- `client/FastFurnaceV2Client.java`: client-only registration of each tier's menu and screen, crushed-ore tint source, and color reload listener.
- `client/CrushedOreColors.java`: source-texture color extraction, normalization, caching, and reload handling for generated crushed items.
- `client/CrushedOreTintSource.java`: shared model tint lookup for dynamic crushed items.
- `client/FastFurnaceScreen.java`: shared furnace screen, information button/fly-out, timer formatting, and JEI exclusion bounds. The three upgrade screens are trivial subclasses used to distinguish JEI click areas.
- `compat/jei/AcceleratorCategory.java`: dedicated Accelerator conversion category with coal/coal-block inputs, Fast Coal outputs, unboosted/boosted timing, and the four-side lava requirement.
- `compat/jei/AcceleratorRecipe.java`: immutable Accelerator display input, output, work duration, and derived boosted duration.
- `compat/jei/AcceleratorFuelCategory.java`: dedicated Accelerator boost category showing the two accepted fuels, triple speed, exact boosted work, and conversion capacities.
- `compat/jei/AcceleratorFuelRecipe.java`: immutable Accelerator boost-fuel input and exact work capacity, validated against the block entity.
- `compat/jei/FastCrusherCategory.java`: dedicated crushing category with raw input, two-item crushed output, a standard Fast-tier progress arrow, and the tier-dependent 10–25% four-item output note.
- `compat/jei/FastCrusherRecipe.java`: immutable display input/output for a discovered raw-ore crushing recipe.
- `compat/jei/FastFurnaceJeiPlugin.java`: optional per-tier catalysts, dynamic crushing recipes, fixed Accelerator conversions and boosts, recipe transfers, click areas, ingredient information, fuel enumeration, and GUI handlers.
- `compat/jei/FastFurnaceFuelCategory.java`: per-tier fuel categories and speed-adjusted smelt counts.
- `compat/jei/FastFurnaceFuelRecipe.java`: immutable JEI fuel inputs and burn time.
- `src/main/resources/assets/fastfurnacev2/`: blockstates, item definitions, models, translations, and animated textures.
- `src/main/resources/data/`: self-drop loot tables, furnace/crusher upgrades and Fast Coal Block recipes, pickaxe-minable tag, and Fast Coal fuel data maps.
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
- Block of Fast Coal explicitly mirrors vanilla coal blocks: black map color, base-drum instrument, `requiresCorrectToolForDrops()`, and strength 5.0/6.0. The mineable tag supplies pickaxe behavior; its loot table explicitly drops itself. Do not use `ofFullCopy(Blocks.COAL_BLOCK)`, which would inherit vanilla's loot-table key and description ID.
- The block's fuel data map uses `burn_time: 16000`, matching a vanilla coal block rather than nine Fast Coal's 14,400 ticks. Its full 3×3 packing recipe and shapeless nine-item unpacking recipe are reversible. Only Fast Furnace applies Fast Coal duration, speed, and doubling bonuses.
- This Minecraft version uses `assets/<namespace>/items/` for item model definitions. Fast Coal additionally has a generated item model under `models/item/`.
- Fast Furnace and Fast Crusher loot tables drop themselves and copy `minecraft:custom_name`; they do not preserve inventory, XP, or burn state when broken. The base Fast Crusher has no crafting recipe; Iron/Gold/Diamond Fast Crushers surround the previous crusher with eight matching metal blocks.
- Every Fast Crusher uses `cube_bottom_top` models with the shared animated Netherrack `fast_crusher` sides and bottom. Its blockstate maps `lit=false` to a model with its static `<tier>_top.png` and `lit=true` to its `<tier>_on.json` model with an eight-frame `<tier>_top_on.png`. Both tops share the same fixed housing and glass rim; only the rotor and liquid move. Iron/gold/diamond tops composite the corresponding preserved metal-block texture into the stationary outer housing around the blender, without changing the Netherrack sides/bottom or corners. Idle tops must not have animation metadata. The existing furnace `LIT` state drives the model switch, so no crusher-specific ticker or block-state property is needed. Crusher block properties and behavior otherwise copy Fast Furnace; do not apply the furnace orientable-model rule below to them.
- Every furnace blockstate maps all four facings and both `lit` values to `minecraft:block/orientable` models. Each tier has separate front, lit-front, side, and top textures; upgrade sources combine their metal block's pattern with vanilla furnace shading and preserve burning-fire pixels. Do not replace these with `cube_all` models, which would erase furnace fronts and visual lit states.

### Dynamic crushed ores

- `ModifyRegistriesEvent` is used to capture vanilla items, scan packaged external candidates, and install an item `AddCallback`; the item registry is frozen at that point. Actual inserts occur once the item `RegisterEvent` opens the registry. The callback covers mod registrations before and after this mod's listener. Guard recursive callbacks and duplicate source IDs.
- Item-stack components and runtime tags are not bound during discovery. Do not call `getDefaultInstance()` or `Holder.is(TagKey)` there. `CrushedOres` instead scans mod files through `ResourcePackLoader` for raw-material, dust, and crushed-ore tags, resolves nested tag references, and combines those IDs with conventional raw item paths. It also scans packaged `assets/<namespace>/items/` definitions so external results registered later are known before fallback generation. Exclude `BlockItem` entries so storage blocks are not treated as raw or crushed items. Datapacks cannot add registry items after startup.
- Reuse an external item when its conventional ID matches the normalized raw material: `crushed_raw_...`, `crushed_...`, `..._crushed`, `dust_...`, `..._dust`, and their `_ore` variants. `ftbmaterials:tin_dust` therefore matches `tin_raw_ore`, and `create:crushed_raw_iron` matches vanilla raw iron. Prefer the raw item's namespace, then candidates in packaged dust/crushed tags, then lexical ID order. Store raw-to-output registry IDs so a packaged candidate can be selected before its item is registered; `forRawOre` resolves the selected ID to a generic `Item` at runtime. Only required external tag entries are preselected, avoiding optional entries for absent items. The optional FTB Materials dependency uses `ordering="AFTER"` as an additional registration-order guarantee.
- `BY_RAW_ORE` includes both generated and external results, while `BY_ID`/`all()` contain only this mod's generated `CrushedOreItem` instances. Keep creative additions, dynamic resources, and tint extraction limited to `all()`; never replace an external item's model, name, or creative placement. `rawOres()` and `forRawOre` drive crusher output, JEI, and smelting behavior across both kinds.
- Without an external match, vanilla `minecraft:raw_iron` maps to `fastfurnacev2:crushed_iron`. Non-vanilla sources include their namespace in the generated crushed ID. `CrushedOreItem.getName` uses `Component.translatableWithFallback` with an ID-derived English fallback; `Component.translatable(key, fallback)` would treat the fallback as a formatting argument and expose the untranslated key. Display normalization removes `_raw_ore` and `_ore` suffixes without changing the existing generated registry ID, so a generated result for `tin_raw_ore` displays as Crushed Tin. Do not require generated language entries for every modded source.
- `ModifyRecipeJsonsEvent` clones only `minecraft:smelting` JSON after testing its parsed ingredient with `acceptsItem` and the raw item's registry holder; components are still unbound during this event, so do not create an `ItemStack` here. Parse the original recipes before inserting anything, detect existing smelting ingredients for external results, and skip cloning for those items. Generate only missing recipes, use collision-free generated IDs, and preserve every field except `ingredient`. Do not replace raw or external recipes or hardcode result items.
- `Item.Properties.finalizeInitializer` overwrites `DataComponents.ITEM_MODEL`, so a shared model component cannot replace per-item definitions. The in-memory client pack generates one `items/<id>.json` per generated variant, all referencing `models/item/crushed_ore.json` and the custom tint source. Keep this pack enabled and below user resource packs. Its generated `pack.mcmeta` must include `pack_format`, `min_format`, and `max_format`; current Minecraft rejects packs above legacy format 64 that omit the two range fields.
- `CrushedOreColors` reads `textures/item/<raw_item_id>.png` from the active resource manager, crops animated strips to their first square frame, averages pixels with alpha at least 128, normalizes the average's maximum channel to 230, and falls back to neutral 230 RGB if the conventional texture cannot be read. Refresh colors on client resource reloads and keep `NativeImage`/other client classes out of common code.

### Accelerator processing and synchronization

- The Accelerator is deliberately separate from `FastFurnaceTier`, ordinary smelting recipes, and the vanilla recipe book. Its only conversions are `minecraft:coal` → one Fast Coal in 100 work units and `minecraft:coal_block` → one Block of Fast Coal in 900. Do not register ordinary fuel data or smelting JSON for these operations.
- Require lava in every `Direction.Plane.HORIZONTAL` neighbor through `FluidTags.LAVA`; source and flowing lava both count, while above and below are irrelevant. Processing, fuel consumption, and boost drain occur only with all four sides and room for the required result. Missing lava or a blocked output preserves progress and boost. Empty/unsupported input resets progress, and changing input type resets it without discarding banked boost.
- Normal work advances one unit per tick. Fast Coal supplies 900 units and its block 1,000; active boost advances up to three per tick. Clamp each step by remaining recipe work and boost work. This gives exactly nine/ten boosted coal conversions, exactly one 900-unit coal-block conversion from either fuel, 34-tick boosted coal operations, and 300-tick boosted block operations without wasting partial ticks.
- The Accelerator works without fuel. When valid work starts with no banked boost, consume one accepted fuel item and store its work capacity. Ordinary coal and other furnace fuels must be rejected by menu slots, shift-click routing, `canPlaceItem`, and automation. Hoppers insert input from above, extract results below, and insert accepted boost fuel from horizontal sides.
- Persist `accelerator_progress`, `accelerator_recipe_ticks`, `accelerator_boost_work`, and `accelerator_total_boost_work`. Six menu values expose progress, recipe duration, remaining/total boost, horizontal lava count, and current processing state; all current values fit ordinary synchronized integers.
- `AcceleratorBlock.BOLT_LEVEL` ranges from 0–10 and uses ceiling progress so the last partial decile displays the full bolt before output resets to zero. The inherited `LIT` state means work advanced on that tick, not merely that boost remains banked. Blockstates select facing and bolt level while ignoring `lit`; all eleven top textures and the shared smooth-stone body have animated lava metadata. Models use `minecraft:block/orientable_with_bottom` with the bolt texture on `top` and the body on `front`, `side`, and `bottom`; ordinary `orientable` would incorrectly copy the bolt onto the bottom.
- The Accelerator has no crafting recipe. Its two conversions and exact-work boosts use dedicated JEI categories rather than ordinary smelting or Fast Furnace categories; do not route it into ordinary smelting transfers or categories, because its conversions and boost rules are custom.

### Furnace ticking and synchronization

- One `BlockEntityType` accepts all four furnace blocks and all four crusher blocks and is constructed with this version's public `BlockEntityType` constructor; `BlockEntityType.Builder` is unavailable. The block entity derives its immutable tier and crusher identity from the actual block. Each crusher uses its corresponding `FastFurnaceTier`, shares that tier's furnace menu and screen, and gets its default display name from its actual block rather than its tier.
- Each block uses a codec capturing both its tier and furnace/crusher identity; a shared codec that always constructs the base tier or a furnace would decode upgrades incorrectly.
- The ticker must use `FAST_FURNACE_BLOCK_ENTITY` and call `FastFurnaceBlockEntity.serverTick` through `createTickerHelper`. Vanilla `createFurnaceTicker` would bypass custom timing.
- Retain the custom `openContainer` block-entity check; vanilla `FurnaceBlock` specifically checks `FurnaceBlockEntity`.
- `FastFurnaceBlockEntity` extends `AbstractFurnaceBlockEntity` with `RecipeType.SMELTING`. Preserve vanilla recipes, other fuels, inventory, XP, comparators, fuel remainders, facing, light level 13 while lit, and custom-name drops.
- Scale cooking through `FastFurnaceTier.scaleCookTime`: 150/200/250/300% normally and an additional 50 percentage points with active Fast Coal. Standard normal-fuel recipes take 133/100/80/66 ticks; Fast Coal recipes take 100/80/66/57 ticks.
- Apply Fast Coal's tier duration multiplier through `scaleFastCoalBurnTime`, then apply the independent tier-specific doubling roll only for actual Fast Furnace tiers. Every Fast Crusher tier must skip the fuel roll entirely. Before doubling, Fast Coal burns for 2,400/2,800/3,200/3,600 ticks. Do not globally alter Fast Coal's ordinary fuel value.
- Fast Crushers derive their immutable identity from `FastFurnaceBlock.isFastCrusher()`, captured by the actual block's codec, not from the shared tier. Resolve input through `CrushedOres.forRawOre` and produce `FAST_CRUSHER_OUTPUT_COUNT` (two) matching crushed items; never use the raw smelting result or accept an unmapped ordinary smelting input. The raw smelting recipe supplies only `cookingTime`, falling back to `BURN_TIME_STANDARD` when absent, and tier/Fast Coal scaling remains unchanged.
- On each completed crushing operation only, roll `tier.getDoubleBurnChance()` (10%/15%/20%/25%) once for four items. This deliberately reuses the tier's furnace fuel-doubling percentage as the crusher output-doubling chance. If all four fit, consume the raw input and output them together. Otherwise persist `fast_crusher_double_output_pending`, hold progress at the final tick, require the doubled result in `canProcess`, and pause fuel until it fits. Resume that same four-item result without rerolling or consuming input early; clear pending state on successful output or a changed input. Ordinary furnaces and Fast Furnace tiers must not consume output-roll randomness or double results.
- Decrement `currentBurnTime` only when the required nonempty result passes `canBurn`; a pending crusher double checks all four items rather than its two-item base result. Empty or unprocessable input and blocked output pause the countdown without clearing `fastCoalBurning` or consuming more fuel. Keep the decrement before final-tick expiration and replacement-fuel consumption in the valid-work branch so existing processing timing remains unchanged. Banked fuel remains `LIT` while paused.
- Do not reset or cool down `DATA_COOKING_PROGRESS` when fuel expires or no replacement fuel is available while processable input remains. Preserve the saved progress through the vanilla block-entity data and resume it on refueling; the existing Fast Coal speed transition may rescale progress to preserve its fraction. Clearing or changing the input still resets progress.
- Use `FastFurnaceV2.isFastCoalFuel` for both Fast Coal and Block of Fast Coal in consumed-fuel tracking, burn-duration scaling, and JEI. Track `fastCoalBurning` from the fuel stack actually consumed, not the current fuel slot, which may already hold different fuel. Clear the flag when burn time expires and persist it as `fast_coal_burning`.
- Exact remaining and total burn times are stored under `lit_time_remaining` and `lit_total_time` and mirrored into vanilla data for furnace behavior.
- `FastFurnaceMenu` has ten data slots. Indices 0–3 retain vanilla behavior; 4–7 contain low/high halves of the exact remaining and total fuel times; index 8 is the active Fast Coal flag; index 9 identifies Fast Crusher. The crusher flag comes from the server block entity's immutable block type, is read-only in its custom server data, and must remain synchronized because the shared client `MenuType` cannot otherwise distinguish it from Fast Furnace. Use the split integers instead of vanilla short-value normalization or a custom per-tick packet.

### Client GUI and JEI

- Keep common logic separate from client-only code. Client entry points use `@Mod(value = MODID, dist = Dist.CLIENT)`. Client classes and Minecraft client references must stay out of dedicated-server paths.
- Do not add `@OnlyIn` for runtime stripping; NeoForge 26.1.2 no longer provides that behavior.
- Four `MenuType` registrations carry the tier to the client; all use the same `FastFurnaceMenu` logic and ten-value synchronization. The upgrade screens share `FastFurnaceScreen` through trivial subclasses so JEI can register tier-specific click areas.
- `FastFurnaceScreen` draws the information button and fly-out in `extractLabels`. It gets normal and Fast Coal speed from `menu.getTier()`, choosing current speed from synchronized active-fuel state. At draw time, it selects the crusher's tier-derived `double_output_chance` label when `menu.isFastCrusher()` and the tier's `double_burn_chance` otherwise; do not cache this choice in the screen constructor because crusher identity arrives through menu-data synchronization after client construction.
- Format timer numbers explicitly in Java with `Locale.ROOT` and `%.1f`. Draw each value one standard font space after its label and the translated seconds unit one space after the value; do not move numeric formatting into translations.
- `getJeiExtraAreas` uses current GUI coordinates and returns bounds only while the fly-out is visible.
- Register every furnace tier for `RecipeTypes.SMELTING` and its own `FastFurnaceFuelCategory` recipe type, including transfers and click areas for each. Because each crusher shares its corresponding furnace tier's menu and screen, smelting transfer filters must reject `menu.isFastCrusher()`, while each tier's dedicated crushing transfer accepts only that menu. Dynamic GUI click areas select crushing for crushers and smelting for ordinary Fast Furnaces; fuel transfers and the fuel click area remain available for both.
- Create one `FastCrusherRecipe` per `CrushedOres.rawOres()` entry from its raw ID and two items resolved by `forRawOre`, whether generated or external. Register these with `FastCrusherCategory`, register every crusher tier as a crushing catalyst and as a catalyst for its own fuel category, and show the independent tier-dependent 10–25% chance for four. The category's arrow uses the standard Fast-tier duration for its animation; do not turn crushing back into a smelting category or use raw smelting results.
- Register exactly two `AcceleratorRecipe` displays from the block entity's input items and duration constants before the client-level null check, so looking up Fast Coal or its block exposes Accelerator conversion alongside other crafting sources. The conversion category derives 34/300-tick boosted durations from `BOOSTED_WORK_PER_TICK` and displays the four-side lava requirement. Register exactly two `AcceleratorFuelRecipe` displays with capacities read from `getBoostWork`; their boost category shows 900/1,000 work, triple speed, and nine/ten-coal or coal-block capacities rather than furnace smelt counts.
- Register the Accelerator as a catalyst for both dedicated types. Its basic transfer handlers target input slot 0 or boost slot 1 with player inventory starting at slot 3. `AcceleratorScreen` click areas use the furnace-standard arrow `(78, 32, 28, 23)` and flame `(56, 36, 14, 14)` bounds; its GUI handler returns fly-out exclusion bounds only while visible. Keep these custom displays and transfers separate from vanilla smelting and tiered Fast Furnace fuel categories.
- The custom fuel categories enumerate item fuels once from the current level's `FuelValues`, then create per-tier recipes applying `scaleFastCoalBurnTime` only to fuels recognized by `isFastCoalFuel`. Smelt counts use `burnTime / tier.scaleCookTime(200, fastCoalActive)`, not vanilla's fixed 200-tick divisor. Regular coal displays approximately 12.03/16/20/24.24 smelts; Fast Coal displays 24/35/48.48/63.16 and its block 240/350/484.85/631.58 before any random doubling roll.
- Do not modify `RecipeTypes.SMELTING_FUEL` to implement Fast Furnace durations; that would incorrectly change ordinary furnace displays.

### GameTests and textures

- GameTests use `RegisterGameTestsEvent` plus the registered `fastfurnacev2:function` instance codec. Vanilla's `TestFunctionLoader` bootstraps before mod construction, so registering a loader during mod/class initialization is too late. `DirectGameTestInstance` invokes the mod's mapped functions directly.
- Preserve deterministic fuel-roll seeds in tests when asserting doubled versus non-doubled durations. Extend existing tests when timing, synchronization, remainders, or active-fuel behavior changes.
- Use the square sources under `tools/texture_sources/`; never feed a generated animation strip back into the generator. It writes both the vertical PNG strip and sibling `.png.mcmeta`.
- Production seeds: Fast Cobblestone 193; Fast Coal 415; Block of Fast Coal 416; Fast Crusher sides/bottom 417; Accelerator body and all top stages 418; all furnace textures 311. The shared furnace seed is intentional so every tier and lit state has the same vein/crust mask and tiles with the others. Default lava-vein animation is eight frames, two ticks per frame, non-interpolated, with two primary veins. The crusher top generator is deterministic without a seed and produces eight non-interpolated frames at one tick per frame. Its optional `--trim` input uses the preserved iron/gold/diamond block source only in the stationary outer housing.
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
3. Do not automatically launch a Minecraft client.
4. Do not take control of the device or cause mouse, keyboard, controller, touch, or other inputs on behalf of the user.
5. Commit all changes that pass validation, as requested by the maintainer. Run the build before every commit and never commit while relevant validation is failing.
6. Before committing, review the full diff and stage only intended files. Include documentation updates in the same validated commit as their implementation.
7. If validation cannot be run or fails, report the exact limitation or failure and leave the changes uncommitted.
