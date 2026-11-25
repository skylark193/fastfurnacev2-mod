# Architecture notes

[Documentation home](README.md) · [Development guide](development.md) · [Public API](api.md)

These notes record non-obvious implementation constraints for Minecraft **26.1.2** and NeoForge **26.1.2.126**. Player-facing behavior and numeric balance belong in the linked player guides. Verify current source before relying on these notes after an upgrade.

## Source layout

| Area | Responsibility |
|---|---|
| `FastFurnaceV2` | Common entry point, deferred registries, creative tabs, built-in API initialization, and event listeners |
| `FastFurnaceTier` | Shared tier speed, duration scaling, doubling chance, and JEI smelt-count math |
| `FastFurnaceBlock` / `FastFurnaceBlockEntity` / `FastFurnaceMenu` | Shared furnace/crusher block identity, processing, persistence, automation, and synchronization |
| `AcceleratorBlock` / `AcceleratorBlockEntity` / `AcceleratorMenu` | Exact-work conversions, lava requirement, boosts, automation, and synchronization |
| `ExtremelyFastAlloyerBlock` / `ExtremelyFastAlloyerBlockEntity` / `ExtremelyFastAlloyerMenu` | Dedicated three-input recipes, Extreme Coal fuel, lava-adjusted timing, and automation |
| `ExtremelyFastAlloyerRecipe` / `ExtremelyFastAlloyerRecipes` | Public recipe definition and internal shapeless storage/matching |
| `FastTools` / `ExtremelyFastToolEffects` | Tool materials, modifier-driven durability rolls, and post-loot/damage effects |
| `FastBonusDrops` / `FastOreGeneration` | Mining bonuses and deterministic once-only ore replacement |
| `CrushedOres` / `CrushedOreItem` / `CrushedOreResources` | Raw-ore discovery, result selection, dynamic registration, naming, and item definitions |
| `api/` | Public immutable definitions, registration events, built-in accessors, and queries |
| `internal/FastFurnaceApiImpl` | Definition storage, matching, and ordinary fuel integration; not public API |
| `client/` | Screens, client registration, dynamic resource colors, and tint handling |
| `compat/jei/` | Optional catalysts, categories, transfers, click areas, ingredient information, and GUI handlers |
| `FastFurnaceGameTests` | Registered GameTests and the direct function-instance codec |
| `tools/` | Deterministic texture generators, preserved square sources, and Python tests |

The four furnace and four crusher tiers share one block-entity type and four tier-specific menu types. Crushers synchronize their block identity through menu data. The Accelerator and Alloyer deliberately do not use ordinary smelting recipes or the vanilla recipe book.

## Registration and block resources

- Register every `DeferredRegister` with the mod event bus. Add translations for new registry objects and user-facing text.
- Item model definitions use `assets/<namespace>/items/`. Fast Coal also has a generated model under `models/item/`.
- Explicitly mirror vanilla physical properties for Fast Cobblestone, coal storage blocks, and ore/storage variants. Do not use `ofFullCopy`: it also copies vanilla loot-table keys and description IDs.
- Every mod block using `requiresCorrectToolForDrops()` must be in `data/minecraft/tags/block/mineable/pickaxe.json`, with the appropriate mining-level tag. Otherwise mining speed and self-drops are wrong.
- Furnace and crusher loot preserves only `minecraft:custom_name`, not inventory, XP, progress, fuel, pending output, or boost.
- Furnaces use `minecraft:block/orientable` with all four facings and both `lit` states. Crushers use `cube_bottom_top`; the existing `LIT` state switches between static and animated tops.
- Accelerator and Alloyer models use `minecraft:block/orientable_with_bottom` so the bolt appears only on top. Their blockstates ignore `lit`; `BOLT_LEVEL` ranges from 0–10 and uses ceiling progress.
- Keep common code free of client-only classes. Client entry points use `@Mod(value = MODID, dist = Dist.CLIENT)`. NeoForge 26.1.2 does not provide runtime stripping through `@OnlyIn`.

## Machine timing and persistence

### Furnaces and crushers

- Construct the shared block-entity type with this version's public `BlockEntityType` constructor; `BlockEntityType.Builder` is unavailable. The block codec captures both tier and furnace/crusher identity.
- Use `createTickerHelper` with `FAST_FURNACE_BLOCK_ENTITY` and `FastFurnaceBlockEntity.serverTick`. Vanilla `createFurnaceTicker` bypasses custom timing. Retain the custom container-opening block-entity check.
- Scale processing through `FastFurnaceTier.scaleCookTime`. Furnaces alone roll to double adjusted fuel duration; crushers roll once per completed operation to double the complete result.
- Resolve crusher inputs only through `FastFurnaceApi.findCrusherRecipe`. A zero recipe time inherits ordinary smelting time with a 200-tick fallback. Raw smelting inputs are not implicitly crusher inputs.
- A blocked crusher double persists `fast_crusher_double_output_pending`, pauses fuel, consumes no input, and resumes without rerolling. Clear pending state on successful output or a changed input.
- Decrement fuel only when the required complete result can process. Empty/unprocessable input and blocked output pause without clearing active-fuel state. Preserve cooking progress when fuel expires; changing input still resets it.
- Use the `FastFuel` matched from the stack actually consumed, not the current fuel slot. Persist its ID as `active_fuel`; retain legacy Fast/Extreme flags for old saves. Both Extreme Coal IDs map to menu fuel type 2.
- Persist exact remaining and total burn times as `lit_time_remaining` and `lit_total_time`, mirroring them into vanilla furnace data. The menu uses eleven synchronized values, including split exact times, fuel family, crusher identity, and effective speed.

### Accelerator

- API conversions use exact work units. All four horizontal neighbors must satisfy `FluidTags.LAVA`; source and flowing lava count.
- Normal work advances one unit per tick. Boost work advances by its registered work-per-tick, clamped by remaining recipe and boost work. The machine works without boost.
- Consume boost only when valid work can advance with no banked boost. Missing lava or blocked output preserves progress and boost; empty/unsupported or changed input resets progress without discarding boost.
- Slots and automation check ingredient acceptance, while processing lookup also verifies required input count. Top inserts input, horizontal sides insert boost, and the bottom extracts results.
- Persist progress, recipe work, remaining/total boost, and boost definition ID. Legacy saves without an ID use the extreme flag, falling back to Fast Coal.
- `LIT` means work advanced on that tick, not merely that boost remains banked.

### Extremely Fast Alloyer

- `FastFurnaceApiImpl` registers the built-in recipe and posts `RegisterExtremelyFastAlloyerRecipesEvent` after default item components bind. `ExtremelyFastAlloyerRecipes` stores public definitions and keeps them separate from smelting, crushing, Accelerator, and crafting recipes.
- Shapeless matching assigns each recipe ingredient to a distinct nonempty input slot through backtracking. Missing, duplicate, partial, and unsupported inputs do not match.
- Only the Extreme Coal item is fuel. Each supplies exactly 1,600 valid processing ticks. Work and burn countdown advance only while a valid result fits.
- Horizontal lava reduces the current duration dynamically to its configured minimum without resetting elapsed work. Missing fuel or blocked output preserves progress and inputs; invalid or changed input resets progress while preserving banked fuel.
- Slots 0–2 are inputs, 3 is fuel, and 4 is output. Top exposes inputs, horizontal faces expose fuel, and bottom exposes output. Six menu values expose progress, current duration, current work state, remaining/total burn, and lava count.

### Item transactions

NeoForge stack snapshots do not include custom machine progress or pending output. For Accelerator, Alloyer, and crusher input transfers:

- Detect committed empty/fill transitions in `onTransfer` (`currentAmount == 0 || currentAmount == amountChange`).
- Journal deferred resets with `SnapshotJournal` and apply them only in `onRootCommit`.
- Do not reset progress from `setItem` while `insideTransaction` is true; an aborted extraction must restore both items and machine state.
- Direct non-transactional changes still reset immediately. Retain the furnace superclass's journaled cooking reset.

## Public API lifecycle

- Post fuel, crusher, Accelerator, Alloyer, and tool-modifier registration events once on the **mod bus** from `DefaultDataComponentsBoundEvent` when `shouldUpdateStaticData()` is true. Common setup is too early to construct recipe `ItemStack`s because default components are unbound.
- Post `ModifyFastToolStatsEvent` earlier on the **mod bus** from `ModifyDefaultComponentsEvent`. Each patch reads the current component builder, so multiple listeners compose instead of replacing the item's original defaults.
- Registration is startup-only, not datapack-reloadable. Built-ins register through the same storage before each corresponding third-party event.
- Definition IDs are unique within each content type. Ingredient matching uses the complete `ItemStack`; highest priority wins, then greatest registration sequence. One matching `FastToolModifiers` definition supplies all four proc chances rather than merging definitions.
- `FastFuel.baseBurnTime == 0` preserves ordinary burn behavior. Positive ordinary values integrate through `FurnaceFuelBurnTimeEvent`. Duration scaling uses effective ordinary burn time; speed bonuses add percentage points.
- Accelerator work and work-per-tick must both be zero or both positive. Crusher and Accelerator definitions support arbitrary input/output counts. Return defensive output/display stack copies.
- GameTest-only API examples require `fastfurnacev2.apiTestContent=true` in the GameTest run and must not register in normal gameplay.
- Keep `FastFurnaceApi.API_VERSION` and [the API guide](api.md) synchronized according to the [version policy](development.md#version-management).

## Dynamic crushed ores

### Discovery and selection

- `ModifyRegistriesEvent` captures vanilla items, scans packaged candidates, and installs an item `AddCallback`; inserts occur when the item `RegisterEvent` opens the registry. Guard recursive callbacks and duplicate source IDs.
- Reject every raw item in this mod's namespace except `raw_fast_iron`, `raw_fast_gold`, and `raw_fast_diamond`. Generated IDs may retain `_raw_ore`; unrestricted rediscovery recursively registers items until startup overflows the stack.
- Item-stack components and runtime tags are unbound during discovery. Do not call `getDefaultInstance()` or `Holder.is(TagKey)` there.
- Scan packaged `tags/item/**/*.json` through `ResourcePackLoader`. Start traversal at `c:raw_materials`, `c:dusts`, `c:crushed_ores`, and subtags. Handle nested references, `replace: true`, and optional entries according to tag semantics.
- Reuse only registered, non-`BlockItem` candidates matching conventional crushed/dust IDs or packaged tags. Prefer the raw item's namespace, then tagged candidates, then lexical ID order.
- Re-evaluate selections as items register so preference is independent of registration order. A later external candidate can replace a provisional generated item; exclude that provisional object from creative contents, item definitions, and tint handling even though it cannot be unregistered.

### Generated resources and recipes

- Generated IDs include the source namespace except for the three built-in Fast materials. Resolve occupied readable IDs with a deterministic name-based UUID suffix.
- `CrushedOreItem.getName` uses `Component.translatableWithFallback`. `Component.translatable(key, fallback)` would treat the fallback as a formatting argument.
- `ModifyRecipeJsonsEvent` clones only parsed `minecraft:smelting` JSON after `acceptsItem` matches the raw holder. Components are still unbound; do not create an `ItemStack` here.
- Ignore recipes whose conditions do not match. Preserve every field except `ingredient`, reuse existing external-result recipes, and generate deterministic collision-free IDs from exact result and source-recipe IDs.
- `Item.Properties.finalizeInitializer` overwrites `DataComponents.ITEM_MODEL`; use the always-enabled in-memory client pack to provide one `items/<id>.json` per active generated variant. Keep it below user packs.
- The generated `pack.mcmeta` requires `pack_format`, `min_format`, and `max_format`; current Minecraft rejects packs above legacy format 64 that omit the range fields.
- Client color extraction reads `textures/item/<raw_item_id>.png`, crops animated strips to the first square frame, averages pixels with alpha at least 128, normalizes the maximum channel to 230, and falls back to neutral RGB 230. Refresh on resource reload and keep `NativeImage` out of common code.

## Tools and world events

- Minecraft 26.1.2 uses `ToolMaterial` and `Item.Properties`; `Tier`, `Tiers`, and `DiggerItem` are unavailable in the mapped version. `FastFurnaceApi.createTool` configures vanilla-style components and retains `AxeItem` stripping and `ShovelItem` flattening while adding modifier-driven durability rolls.
- `ModifyDefaultComponentsEvent` runs after item registration and before default components finalize. Its initializer exposes the current `DataComponentMap.Builder`; `FastToolStats` patches durability, finite mining-speed rules, enchantability, and the vanilla base attack attribute IDs. There is no global `ItemDamageEvent` in this NeoForge version.
- Scale sword speed as actual attacks per second: `newBaseline = (4 + vanillaBaseline) * multiplier - 4`. Fast Diamond Sword's damage scales total vanilla damage, not only the material bonus.
- Factory-created tools override NeoForge's `IItemExtension.damageItem` route and query the highest-priority registered modifier on the server. This covers attacks, mining, stripping, and flattening without a separate event handler. Ordinary custom items can use the other modifiers without this item class.
- Extremely Fast built-in Fortune II/Looting II uses `Item.Properties.delayedComponent(DataComponents.ENCHANTMENTS, ...)` so registry lookup occurs after default components bind.
- Registered block-drop modifiers require a noncreative player, block drops enabled, and a correct tool. Copy complete drops after normal loot and Fortune/Looting, preserving components, positions, and motion. Kill-drop and damage rolls require a direct `PLAYER_ATTACK`; arrows, Thorns, and other indirect sources cannot proc them. Damage doubles during `LivingIncomingDamageEvent` before mitigation.
- `FastBonusDrops` appends during server-side `BlockDropsEvent`; never clear vanilla drops. Require a noncreative breaker, enabled drops, a correct tool, and no Silk Touch.
- `FastOreGeneration` uses server-side `ChunkEvent.Load`, source-specific salts, world seed, and position-derived randomness. Skip palettes that cannot contain eligible ore and mutate only the supplied chunk with update flags 0.
- Store Boolean `fast_ore_chunk_processed` and `deepslate_fast_ore_chunk_processed` attachments after independent passes and call `markUnsaved()`. Retain the legacy `extreme_coal_chunk_processed` registration for old saves, but do not let it suppress current passes.

## GUI and JEI integration

- Four menu types carry the furnace tier to the client; all use the same menu and screen classes. Crusher identity arrives through synchronization, so screens select crusher text and click behavior at draw time rather than in constructors.
- Read arbitrary effective speed from synchronized menu data. Format timers with `Locale.ROOT` and `%.1f`.
- Information fly-outs prefer right, then left, then an on-screen fallback. Drawing and `getJeiExtraAreas` must use the same current position calculation and return exclusions only while visible.
- JEI keys transfer handlers by exact container class and recipe type. Register shared smelting transfer once with `menu -> !menu.isFastCrusher()` and crushing transfer once with `FastFurnaceMenu::isFastCrusher`; duplicate registrations overwrite one another.
- Use one shared furnace/crusher screen GUI handler. Its arrow chooses crushing or smelting from the current menu; its flame chooses the current tier's fuel category.
- Register API crusher, Accelerator, and Alloyer definitions directly with their dedicated categories. Accelerator transfers target input slot 0 or boost slot 1, with player inventory starting at slot 3. Alloyer transfers target slots 0–2, with player inventory starting at slot 5.
- Register the Accelerator as catalyst for both dedicated types and the Alloyer only for its dedicated type. Do not route these recipes into ordinary smelting, crushing, or tiered fuel categories.
- Enumerate custom fuel displays from the current level's `FuelValues`, match each stack through `FastFurnaceApi.findFuel`, and calculate smelt counts as `burnTime / tier.scaleCookTime(200, speedBonusPercent)`.
- Do not modify `RecipeTypes.SMELTING_FUEL` to implement Fast Furnace durations; that would alter ordinary furnace displays.

## GameTests and textures

- GameTests use `RegisterGameTestsEvent` plus the registered `fastfurnacev2:function` instance codec. Vanilla's `TestFunctionLoader` bootstraps before mod construction, so registering a loader during mod initialization is too late; `DirectGameTestInstance` invokes mapped functions directly.
- Preserve deterministic random seeds in tests asserting successful/failed rolls and exact durations. Extend tests for timing, synchronization, remainders, transactions, persistence, and active-fuel changes.
- Use square sources under `tools/texture_sources/`; never feed generated animation strips back into a generator. Production seeds and exact commands live in [tools/README.md](../tools/README.md).
- Ordinary lava veins default to eight frames, two ticks per frame, no interpolation, and two primary veins. Shared furnace and tool seeds intentionally align identical silhouettes across tiers.
- Use `generate_extreme_coal_texture.py`, not the ordinary vein generator, for all-lava Extreme Coal, its block, Alloyer body, Extremely Fast Ingot, and Extremely Fast tools.
- The machine-top generator's `--all-lava` option preserves Accelerator bolt geometry while replacing the background. Static crusher/dust generators remove stale animation metadata; idle crusher tops must not have animation metadata.
