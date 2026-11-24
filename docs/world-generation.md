# World generation and dynamic resources

[Documentation home](README.md) · [Blocks](blocks.md) · [Items](items.md)

Fast Furnace V2 adds materials through three separate systems:

1. Bonus drops appended while mining stone or coal ore.
2. Deterministic replacement of regular and deepslate coal, iron, gold, and diamond ore with their modded variants.
3. Startup discovery and generation of crushed-ore processing items and smelting recipes.

These systems do not replace ordinary stone or coal-ore drops, add a new ore distribution noise generator, or require a fixed set of crushed items from another mod.

## Mining bonuses

Mining an eligible block can append **one** bonus item after vanilla drops have been calculated:

| Mined block | Bonus item |
|---|---|
| Stone (`minecraft:stone`) | 1 × Fast Cobblestone |
| Any block in `minecraft:coal_ores` | 1 × Fast Coal |

The coal-ores tag includes regular coal ore and deepslate coal ore. Extreme Coal Ore is not in that tag and does not trigger this separate Fast Coal bonus roll. The bonus is independent of the block's normal drops: mining stone still produces its normal result, and coal ore retains its ordinary coal or Silk Touch behavior.

### Eligibility requirements

A bonus roll requires all of the following:

- A non-creative player breaks the block.
- Block drops are enabled.
- The breaker uses a correct tool for the block's drops.
- The tool does **not** have Silk Touch.

Explosions, unsuitable tools, creative breaking, and Silk Touch do not receive these bonuses.

### Fortune chance

For an eligible break:

```text
bonus chance = min(100%, 10% + 5 percentage points × Fortune level)
```

| Fortune level | Bonus chance |
|---|---:|
| None | 10% |
| I | 15% |
| II | 20% |
| III | 25% |
| Nonstandard level 18 or higher | 100% |

Each eligible block makes its own roll. Fortune increases the chance of the one-item bonus; it does not multiply that bonus into multiple Fast Cobblestone or Fast Coal items. Coal ore's separate vanilla Fortune-scaled coal drops are unchanged.

## Fast and Extreme ore generation

Fast and Extreme ores are created by replacing existing regular and deepslate vanilla ore blocks, rather than by adding separate ore-vein distributions.

### Replacement rule

During chunk loading, Fast Furnace V2 examines positions that can contain any eligible ore. Each actual matching ore position receives an independent deterministic roll:

| Vanilla source | 5% replacement result |
|---|---|
| `minecraft:coal_ore` | `fastfurnacev2:extreme_coal_ore` |
| `minecraft:deepslate_coal_ore` | `fastfurnacev2:deepslate_extreme_coal_ore` |
| `minecraft:iron_ore` | `fastfurnacev2:fast_iron_ore` |
| `minecraft:deepslate_iron_ore` | `fastfurnacev2:deepslate_fast_iron_ore` |
| `minecraft:gold_ore` | `fastfurnacev2:fast_gold_ore` |
| `minecraft:deepslate_gold_ore` | `fastfurnacev2:deepslate_fast_gold_ore` |
| `minecraft:diamond_ore` | `fastfurnacev2:fast_diamond_ore` |
| `minecraft:deepslate_diamond_ore` | `fastfurnacev2:deepslate_fast_diamond_ore` |

The other 95% of rolls leave the vanilla ore unchanged.

- Only the eight regular and deepslate ore blocks listed above are eligible.
- Nether Gold Ore and other ore variants are excluded.
- Other blocks in broader ore tags are not replaced.
- No ore is inserted into stone or empty space.
- Each replacement preserves the original block position.

Each source block has its own fixed salt, including separate regular and deepslate salts. A roll is derived from the world seed, block position, and that salt, so it does not depend on chunk-load order or a changing random-number sequence.

### New and existing chunks

The same replacement pass handles:

- Newly generated chunks when they are loaded.
- Chunks generated before Fast Furnace V2 was installed, when those chunks are subsequently loaded.

Persistent Boolean `fast_ore_chunk_processed` and `deepslate_fast_ore_chunk_processed` chunk attachments independently mark the regular and deepslate passes. Existing chunks are processed **once per family**, so unloading and reloading them does not reroll ores or continue converting more blocks over time. A chunk processed by version 1.3.0 can receive the new deepslate pass without rerolling its regular ores. The former coal-only `extreme_coal_chunk_processed` attachment remains readable for old saves but does not suppress either current pass.

This also means that manually placing an eligible vanilla ore in an already processed chunk does not schedule another conversion pass.

### Drops and acquisition

All eight replacement ores follow the corresponding vanilla ore's Silk Touch alternatives and Fortune-scaled `minecraft:ore_drops` formula:

| Replacement ore | Ordinary drop |
|---|---|
| Regular or Deepslate Extreme Coal Ore | Extreme Coal |
| Regular or Deepslate Fast Iron Ore | Raw Fast Iron |
| Regular or Deepslate Fast Gold Ore | Raw Fast Gold |
| Regular or Deepslate Fast Diamond Ore | Raw Fast Diamond |

Without Silk Touch, Fortune can increase the drop quantity. With Silk Touch, each ore drops itself. Fast ore mining tiers match their vanilla equivalents in both regular and deepslate forms: Fast Iron Ore requires a stone pickaxe or better, while Fast Gold and Fast Diamond Ore require an iron pickaxe or better.

See [Extreme Coal Ores](blocks.md#extreme-coal-ores), [Fast material ores](blocks.md#fast-iron-gold-and-diamond-ores), and [Fast material items](items.md#fast-material-items).

## Dynamic crushed ores

Fast Crushers need a crushed result for each supported raw ore. Fast Furnace V2 discovers raw items and selects or generates those results during registry startup.

### Raw-ore discovery

Discovery recognizes:

- Conventional item IDs beginning with `raw_`.
- Conventional item IDs ending with `_raw` or `_raw_ore`.
- Direct and nested entries in packaged `c:raw_materials` item tags and their subtags.

Packaged item-tag JSON from loaded mod files is indexed so nested references can resolve even though runtime item tags and default stack components are not yet bound. A tag entry with `replace: true` replaces earlier values; optional entries are handled according to their tag semantics.

Only registered, non-block raw items participate. Datapacks cannot introduce new registry items after startup.

Items in Fast Furnace V2's own namespace are excluded from raw-ore discovery except for the three explicit built-ins `raw_fast_iron`, `raw_fast_gold`, and `raw_fast_diamond`. Generated crushed IDs can retain source suffixes such as `_raw_ore`; allowing arbitrary mod outputs to rediscover themselves would recursively register more items. The narrow whitelist supports the built-in Fast-material processing chain without reopening that recursion.

### External result selection

Before generating a new item, the mod looks for an already registered, non-block-item result using:

- Conventional IDs such as `crushed_raw_...`, `crushed_...`, `..._crushed`, `..._crushed_raw`, `..._crushed_raw_ore`, `dust_...`, and `..._dust`.
- Their `_ore` variants.
- Direct and nested entries in packaged `c:dusts` and `c:crushed_ores` tags and their subtags.

Examples of supported naming relationships include:

```text
create:crushed_raw_iron  ↔  minecraft:raw_iron
ftbmaterials:tin_dust    ↔  a mod's tin_raw_ore
```

When several candidates match, selection prefers:

1. Candidates in the raw item's namespace.
2. Candidates found in packaged dust or crushed-ore tags.
3. Lexical registry-ID order.

Candidates are re-evaluated as additional items register, so a later registration can replace a provisional generated item or a lower-priority external choice. Selection therefore does not depend on mod registration order.

An external item keeps its own model, display name, creative placement, and other behavior. Fast Furnace V2 uses it as crusher output without replacing its resources.

### Generated fallback items

If no external result is available, the mod registers a generated crushed item.

With vanilla raw materials and no external matches:

| Source | Generated result |
|---|---|
| `minecraft:raw_iron` | `fastfurnacev2:crushed_iron` |
| `minecraft:raw_copper` | `fastfurnacev2:crushed_copper` |
| `minecraft:raw_gold` | `fastfurnacev2:crushed_gold` |
| `fastfurnacev2:raw_fast_iron` | `fastfurnacev2:crushed_fast_iron` |
| `fastfurnacev2:raw_fast_gold` | `fastfurnacev2:crushed_fast_gold` |
| `fastfurnacev2:raw_fast_diamond` | `fastfurnacev2:crushed_fast_diamond` |

Other modded sources include their source namespace in the readable generated ID; the three built-in raw Fast materials use the short IDs shown above. If a readable ID is occupied, a deterministic name-based UUID suffix avoids a collision.

Generated items use a shared dust-pile model, a source-derived tint, and an ID-derived English fallback name. Display normalization removes raw-ore suffixes, so a generated result for `tin_raw_ore` is named **Crushed Tin** even when no language entry exists.

If a better external candidate later replaces a provisional generated result, the unused generated registry object is excluded from creative contents, dynamic item definitions, and tint handling. Registry objects cannot be removed after insertion, but the unused object is no longer selected as machine output.

### Generated smelting recipes

After raw smelting recipes are parsed, Fast Furnace V2 clones eligible `minecraft:smelting` JSON for the selected crushed result.

- The original ingredient is replaced with the crushed result.
- Output, output count, experience, cooking time, category, and group are preserved.
- Existing external smelting recipes are detected and reused instead of duplicated.
- Recipes whose declared conditions do not match are ignored.
- Original raw and external recipes are not replaced.
- Generated recipe IDs include a deterministic UUID derived from the exact result and source-recipe IDs, preventing namespace/path flattening collisions.

Both generated and external crushed results work in ordinary furnaces and Fast Furnaces when an ordinary smelting recipe is available.

### Client appearance

For generated variants only, an always-enabled in-memory client resource pack supplies per-item model definitions. These share the dust-pile model and custom tint source while remaining below user resource packs.

The tint system reads `textures/item/<raw_item_id>.png` from the active resource manager:

1. Crop an animated strip to its first square frame.
2. Average pixels with alpha of at least 128.
3. Normalize the average's maximum color channel to 230.
4. Fall back to neutral RGB 230 if the conventional texture cannot be read.

Colors refresh on client resource reloads. External crushed items do not receive this model or tint override.

See [Items](items.md#crushed-ores), [Fast Crushers](crushers.md), and [Crushed-ore smelting](recipes.md#crushed-ore-smelting).
