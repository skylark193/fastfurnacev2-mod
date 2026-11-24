# Fast Furnace V2

Fast Furnace V2 is a NeoForge mod for Minecraft 26.1.2. It adds Fast Cobblestone, Fast Coal, Extreme Coal, Fast Iron/Gold/Diamond materials and their regular and deepslate ores, reversible storage blocks, enhanced Fast and Extremely Fast tools and swords, a lava-powered Accelerator, an Extremely Fast Alloyer and Extremely Fast Ingot, four tiers of accelerated furnaces, four tiers of ore crushers, dynamically generated crushed ores, animated lava-vein textures, and optional JEI integration.

## Documentation

Start at the [documentation home](docs/README.md) for the complete player, modpack, and developer reference. The suite includes dedicated pages for [blocks](docs/blocks.md), [items](docs/items.md), [recipes and fuels](docs/recipes.md), [Fast Furnaces](docs/furnaces.md), [Fast Crushers](docs/crushers.md), the [Accelerator](docs/accelerator.md), the [Extremely Fast Alloyer](docs/alloyer.md), [world generation and dynamic resources](docs/world-generation.md), and [automation, GUI, and JEI](docs/automation-and-jei.md). Mod developers can go directly to the [API guide](docs/api.md).

## Features

### Fast Cobblestone

- Matches vanilla cobblestone's mining requirements and physical properties, but drops Fast Cobblestone.
- Mining stone in survival mode with a correct, non-Silk-Touch tool has a base 10% chance to add one Fast Cobblestone alongside the normal drops. Each Fortune level adds 5 percentage points to this chance. The bonus respects the block-drops game rule.
- Used to craft Fast Furnace with the vanilla furnace pattern: eight Fast Cobblestone around an empty center.
- Appears in Building Blocks and the mod's Fast Furnace V2 creative tab.

### Fast Coal

- Mining coal ore or deepslate coal ore in survival mode with a correct, non-Silk-Touch tool has a base 10% chance to add one Fast Coal alongside the normal drops. Each Fortune level adds 5 percentage points to this chance. The bonus respects the block-drops game rule.
- Burns for 1,600 ticks in ordinary furnaces, exactly like vanilla coal.
- Adds 50 percentage points to the active furnace's smelting speed and receives a tier-dependent burn-time bonus while actively burning.
- Appears in Ingredients and the mod's Fast Furnace V2 creative tab.

### Extreme Coal Ore and Extreme Coal

- Each regular coal-ore block has an independent 5% chance to become Extreme Coal Ore, and each deepslate coal-ore block has an independent 5% chance to become Deepslate Extreme Coal Ore. Newly generated and previously generated chunks are processed once per ore family; persistent per-chunk markers prevent later rerolls.
- Both Extreme Coal Ore variants use their vanilla counterparts' mining properties and require a pickaxe. They drop Extreme Coal, with Fortune increasing the drop quantity through vanilla's ore-drop formula. Silk Touch drops the ore itself.
- Extreme Coal burns for 1,600 ticks in ordinary furnaces, exactly like vanilla coal. Where Fast Coal receives a bonus, Extreme Coal doubles the added effect: it adds 100 percentage points to furnace or crusher speed and receives twice Fast Coal's added tier-dependent burn duration.
- Its Fast / Iron / Gold / Diamond Fast Furnace burn times are 3,200 / 4,000 / 4,800 / 5,600 ticks before the independent fuel-doubling roll, or 6,400 / 8,000 / 9,600 / 11,200 ticks after doubling. Fast Crushers receive its speed and duration bonuses without rolling for doubled fuel duration.
- In an Accelerator, one Extreme Coal supplies 7,200 boosted work units at five work units per tick: exactly 18 coal conversions or two coal-block conversions, taking 80 or 720 ticks each respectively.
- Both ores look like their vanilla counterparts with animated lava veins; Extreme Coal preserves coal's silhouette with every visible pixel colored as animated lava. The ores appear in Natural Blocks, the item appears in Ingredients, and all appear in the mod's creative tab. Either ore can also be crushed into three Extreme Coal.

### Block of Fast Coal

- Crafted from a full 3×3 grid of Fast Coal and can be unpacked back into nine Fast Coal.
- Matches a vanilla coal block's mining requirements and physical properties, drops itself, and has an animated coal-block texture with lava veins.
- Burns for 16,000 ticks in ordinary furnaces, exactly like a vanilla coal block.
- Counts as active Fast Coal fuel in every Fast Furnace tier, adding 50 percentage points to smelting speed and receiving the tier's duration multiplier and independent fuel-doubling roll.
- Burns for 24,000 / 28,000 / 32,000 / 36,000 ticks in Fast / Iron / Gold / Diamond Fast Furnaces before doubling, or 48,000 / 56,000 / 64,000 / 72,000 ticks after doubling.
- Appears in Building Blocks and the mod's Fast Furnace V2 creative tab.

### Block of Extreme Coal

- Crafted from a full 3×3 grid of Extreme Coal and can be unpacked back into nine Extreme Coal. Its coal-block texture is made entirely from animated lava.
- Burns for 16,000 ticks in ordinary furnaces, exactly like a vanilla coal block, and drops itself when mined with a pickaxe.
- Receives Extreme Coal's +100-percentage-point speed bonus and doubled added-duration scaling in Fast Furnaces and Fast Crushers. Its Fast / Iron / Gold / Diamond Fast Furnace burn times are 32,000 / 40,000 / 48,000 / 56,000 ticks before doubling.
- In an Accelerator, supplies 8,000 work units at five work units per tick: exactly 20 item conversions or two block conversions, with 800 work left after the latter.

### Fast Iron, Fast Gold, and Fast Diamond

- Each regular or deepslate iron, gold, or diamond ore has an independent deterministic 5% chance to become its matching Fast variant in newly generated chunks and once in existing chunks.
- Fast ores resemble their vanilla counterparts with animated lava veins. Without Silk Touch they drop Raw Fast Iron, Raw Fast Gold, or Raw Fast Diamond using vanilla Fortune scaling; Silk Touch drops the ore itself.
- Smelt one raw Fast material directly into one Fast Iron Ingot, Fast Gold Ingot, or Fast Diamond. Crushers instead produce two matching crushed Fast items, each of which smelts into one finished material.
- The Accelerator converts ordinary iron ingots, gold ingots, and diamonds into their Fast counterparts in 400 ticks, and their ordinary storage blocks into Fast storage blocks in 3,600 ticks.
- Nine finished Fast materials craft a reversible storage block. Blocks of Fast Iron, Fast Gold, and Fast Diamond replace vanilla metal blocks as the ingredients for furnace and crusher upgrades.

### Fast tools and swords

- Fast Iron pickaxes, shovels, and axes match iron tools but break blocks 20% faster; Fast Iron Sword attacks 20% faster.
- Fast Gold pickaxes, shovels, and axes match golden tools but break blocks 30% faster; Fast Gold Sword attacks 30% faster.
- Fast Diamond pickaxes, shovels, and axes match diamond tools but break blocks 20% faster and have a 5% chance to avoid durability damage. Fast Diamond Sword also attacks 20% faster and deals 20% more damage.
- Durability, enchantability, harvest tiers, and ordinary attack values otherwise match their vanilla counterparts. Repair each set with its corresponding Fast material.
- All twelve use vanilla-shaped stick-and-material crafting recipes, vanilla textures with animated lava veins, and simple JEI bonus descriptions. Tools appear in Tools & Utilities, swords and axes in Combat, and everything in the mod's creative tab.

### Extremely Fast tools and swords

- Craft an Extremely Fast Pickaxe, Shovel, Axe, or Sword with Extremely Fast Ingots using the corresponding vanilla stick-and-material pattern. Every visible texture pixel is animated lava within the normal diamond-tool silhouette.
- Pickaxe, shovel, and axe durability is double diamond, mining is 50% faster, Fortune II is built in, and each break has an independent 5% chance to double all drops after normal Fortune calculations.
- The sword has double diamond-sword durability, 50% faster attacks, unchanged base attack damage, and built-in Looting II. Each kill independently has a 5% chance to double all drops, and each hit independently has a 10% chance to deal double damage.
- Repair all four with Extremely Fast Ingots. Pickaxes, shovels, and axes appear in Tools & Utilities; the sword and axe appear in Combat; all four appear in the mod creative tab.

### Accelerator

- Craft it by surrounding one Iron Fast Furnace with eight Redstone Blocks. It converts coal, iron ingots, gold ingots, and diamonds into Fast variants in 400 ticks (20 seconds), and their storage blocks into Fast storage blocks in 3,600 ticks (180 seconds). Other mods can add conversions through the API.
- Requires lava on all four horizontal sides. Source or flowing lava works, and no lava is required above or below. Missing lava pauses processing without losing progress or stored boost.
- Runs at normal speed without fuel. By default, its fuel slot accepts only Fast Coal, Extreme Coal, and their storage blocks; ordinary furnace fuels are not accepted unless another mod registers them as Accelerator boosts. Fuel is consumed only when valid work can proceed.
- Fast Coal supplies 3,600 boosted work units, exactly nine item conversions or one block conversion. A Block of Fast Coal supplies 4,000 units, exactly ten item conversions or one block conversion with enough left over for one item conversion. Extreme Coal supplies 7,200 units, exactly 18 item conversions or two block conversions. A Block of Extreme Coal supplies 8,000 units, exactly 20 item conversions or two block conversions with 800 work left over.
- Fast Coal and its block advance boosted work three units per tick instead of one. Item conversions therefore take 134 ticks and block conversions take 1,200 ticks (60 seconds). Extreme Coal and its block advance five units per tick, giving 80-tick item and 720-tick block conversions. Work is clamped to the remaining recipe and boost, so partial final ticks do not waste fuel.
- Missing lava and a blocked output pause processing without losing stored boost or recipe progress. Empty or unsupported input resets progress, as does changing between supported inputs; stored boost survives these resets and is saved with the block.
- Uses animated smooth stone with lava veins. Its top face has a lightning bolt that fills from bottom to top through eleven block-state stages as recipe progress advances; the front and other faces retain the smooth-stone body texture.
- Requires a pickaxe, drops itself, preserves custom names, supports directional placement and comparator output, and appears in Functional Blocks and the mod's creative tab. Its furnace-style GUI has no ordinary smelting recipe book and includes an information fly-out for lava, boost, and recipe timing.

### Extremely Fast Alloyer and Extremely Fast Ingot

- Craft the Alloyer by surrounding one Accelerator with eight Blocks of Extreme Coal. Its Accelerator-style body is made entirely from animated lava, and its top retains the eleven-stage lightning-bolt progress animation.
- The Alloyer has three input slots, one Extreme Coal fuel slot, and one output slot. It uses only dedicated Alloyer recipes, never ordinary furnace, crusher, Accelerator, or crafting recipes. No other fuel works.
- Combine one Fast Iron Ingot, one Fast Gold Ingot, and one Fast Diamond in any input-slot order to produce one Extremely Fast Ingot. The base operation takes 800 ticks (40 seconds), reduced by 100 ticks per horizontal lava side to a minimum of 400 ticks.
- Each Extreme Coal supplies 1,600 valid processing ticks. Missing fuel or a blocked output pauses without consuming ingredients or losing progress; empty, incomplete, duplicated, unsupported, or changed inputs reset progress while preserving banked fuel. Changing lava preserves elapsed work and recalculates total duration.
- The Extremely Fast Ingot preserves an ingot silhouette with every visible pixel colored as animated lava. It appears in Ingredients and the mod's creative tab; the Alloyer appears in Functional Blocks and the mod's creative tab.
- Top automation inserts accepted ingredients, horizontal sides insert only Extreme Coal, and the bottom extracts results. The GUI shows fuel and lava-adjusted timing, while the dedicated JEI category shows all three inputs, output, base time, and lava-reduction rule. See the [Extremely Fast Alloyer guide](docs/alloyer.md).

### Crushed Ores

- Discovers vanilla and modded raw ore items during registration, including conventional `raw_...`, `..._raw`, and `..._raw_ore` item IDs and items listed in packaged `c:raw_materials` tags or their subtags.
- Reuses a matching crushed item or dust from another mod instead of generating a duplicate. Detection recognizes conventional IDs such as FTB Materials' `tin_dust` and Create's `crushed_raw_iron`, along with packaged `c:dusts` and `c:crushed_ores` tags. Same-namespace matches and tagged items are preferred.
- When no external match exists, adds a crushed item for the raw ore. Each generated item appears as a dust pile tinted from the visible pixels of its source item's texture and appears in Ingredients and the mod's creative tab. External items keep their own names, models, and creative-tab placement.
- Copies each raw ore's ordinary smelting recipe when the selected crushed item needs one, preserving the output, experience, cooking time, category, and group. Existing external smelting recipes are left unchanged. Crushed ores therefore work in ordinary furnaces and every Fast Furnace tier.
- Without an external match, vanilla raw iron, raw copper, and raw gold become Crushed Iron, Crushed Copper, and Crushed Gold. Raw Fast Iron, Raw Fast Gold, and Raw Fast Diamond similarly produce Crushed Fast Iron, Crushed Fast Gold, and Crushed Fast Diamond. Other generated modded variants use namespace-qualified registry IDs to avoid collisions and derive English material names from their source IDs, removing raw-ore affixes such as `_raw_ore`. For example, a generated crushed item for a modded `tin_raw_ore` item is displayed as Crushed Tin when no material translation is supplied.
- Discovery occurs during mod registration; datapacks cannot introduce new raw item types afterward. Only actually registered external items are reused. If a matching external item registers after a provisional generated result, discovery switches to the external item and removes the generated variant from creative contents and dynamic resources; unresolved tag entries cannot suppress fallback generation. If a generated item's source texture does not use the conventional `textures/item/<raw_item_id>.png` path, its dust falls back to a neutral color.

### Fast Crushers

- Fast Crusher, Iron Fast Crusher, Gold Fast Crusher, and Diamond Fast Crusher process raw ores through the dynamic crushed-ore system instead of performing ordinary smelting. Each raw ore produces two matching crushed ore items; ordinary furnace inputs such as cobblestone do not process unless another mod adds a custom crusher recipe.
- Regular and deepslate Extreme Coal Ore each crush into three Extreme Coal. Each regular or deepslate Fast Iron, Fast Gold, or Fast Diamond Ore crushes into three matching crushed Fast materials.
- A raw ore's ordinary smelting time supplies the operation duration when present, with 200 ticks as the fallback. Every crusher tier uses the corresponding furnace tier's normal, Fast Coal, and Extreme Coal speed, but never doubles fuel duration.

| Crusher | Normal speed | With Fast Coal | With Extreme Coal | Standard operation, normal / Fast / Extreme | Output-doubling chance |
|---|---:|---:|---:|---:|---:|
| Fast Crusher | 150% | 200% | 250% | 133 / 100 / 80 ticks | 10% |
| Iron Fast Crusher | 200% | 250% | 300% | 100 / 80 / 66 ticks | 15% |
| Gold Fast Crusher | 250% | 300% | 350% | 80 / 66 / 57 ticks | 20% |
| Diamond Fast Crusher | 300% | 350% | 400% | 66 / 57 / 50 ticks | 25% |

- Each completed operation independently rolls its tier's chance to double the complete result: two crushed items become four, and an ore block's three-item result becomes six. If a successful doubled result cannot fit, processing and fuel countdown pause without consuming the input until the complete result fits; the crusher never falls back to the smaller result for that operation.
- Every tier uses animated Netherrack textures on its sides and bottom. Its top-down blender has a stationary rotor while idle and animated rotating blades with swirling contents while lit. Iron, gold, and diamond tops show the corresponding block texture in the stationary housing around the blender.
- Every crusher requires a pickaxe, drops itself, preserves custom names, and emits light level 13 while lit. All four appear in Functional Blocks and the mod's Fast Furnace V2 creative tab.
- Craft Fast Crusher with three Fast Diamonds across the top row, a Fast Furnace in the center, and Fast Iron Ingots in the remaining five slots. Surround it with eight Blocks of Fast Iron to craft Iron Fast Crusher, surround that with eight Blocks of Fast Gold to craft Gold Fast Crusher, then surround that with eight Blocks of Fast Diamond to craft Diamond Fast Crusher.

### Fast Furnace Tiers

| Furnace | Normal speed | With Fast Coal | With Extreme Coal | Standard recipe, normal / Fast / Extreme | Fuel doubling chance | Fast / Extreme Coal burn time |
|---|---:|---:|---:|---:|---:|---:|
| Fast Furnace | 150% | 200% | 250% | 133 / 100 / 80 ticks | 10% | 2,400 / 3,200 ticks |
| Iron Fast Furnace | 200% | 250% | 300% | 100 / 80 / 66 ticks | 15% | 2,800 / 4,000 ticks |
| Gold Fast Furnace | 250% | 300% | 350% | 80 / 66 / 57 ticks | 20% | 3,200 / 4,800 ticks |
| Diamond Fast Furnace | 300% | 350% | 400% | 66 / 57 / 50 ticks | 25% | 3,600 / 5,600 ticks |

Standard recipes take 200 ticks in a vanilla furnace. Each consumed fuel item independently rolls the listed chance to double its adjusted burn time. Listed Fast Coal and Extreme Coal burn times are before that roll; doubled Fast Coal values are 4,800, 5,600, 6,400, and 7,200 ticks, while doubled Extreme Coal values are 6,400, 8,000, 9,600, and 11,200 ticks respectively.

Every Fast Furnace and Fast Crusher tier pauses its active fuel countdown when there is no processable work, including empty or unprocessable input and an output slot that cannot accept the required result. For crushers, this includes waiting for a pending doubled result to fit. The countdown resumes when valid work returns, preserving any active Fast Coal or Extreme Coal bonus. If fuel runs out during an operation, recipe progress is preserved instead of cooling down or resetting; adding fuel resumes the same operation from its saved progress.

Craft the base Fast Furnace from eight Fast Cobblestone using the vanilla furnace pattern, then upgrade it in order:

- Surround Fast Furnace with eight Blocks of Fast Iron to craft Iron Fast Furnace.
- Surround Iron Fast Furnace with eight Blocks of Fast Gold to craft Gold Fast Furnace.
- Surround Gold Fast Furnace with eight Blocks of Fast Diamond to craft Diamond Fast Furnace.

Every tier supports vanilla smelting recipes and fuels, the furnace recipe book, inventory and experience handling, comparator output, directional placement, and familiar furnace sounds and particles. All tiers require a pickaxe, drop themselves, preserve custom names, and emit light level 13 while lit.

Every tier uses the same furnace-style GUI. Its information fly-out shows the tier's current speed, fuel doubling chance, remaining burn time, and total current-fuel burn time. Crusher fly-outs instead show their tier's 10% / 15% / 20% / 25% output-doubling chance. Timers use exact tick counts and display seconds with one decimal place. All furnaces and crushers appear in Functional Blocks and the mod's Fast Furnace V2 creative tab.

Fast Cobblestone is the icon of the mod's creative tab. Fast Cobblestone, Fast Coal, Extreme Coal Ore, Extreme Coal, Block of Fast Coal, every Fast ore, raw and finished Fast material, Fast storage block, and all furnace textures use animated lava visuals. Each upgrade has a furnace-shaped front, side, and top made from its tier material, with distinct unlit and burning front models. All furnace textures share one vein layout so adjacent furnace tiers tile together.

## Modding API

The public API is currently version 1.1. Mod-bus registration events support ordinary fuels, per-tier fuel duration and speed bonuses, custom Fast Crusher recipes, Accelerator conversions, and Accelerator boost fuels. The API also exposes machine items, tiers, registered definitions, and recipe/fuel lookup.

Custom content runs in the real machines, including automation, exact timing, output doubling, persistence, GUI synchronization, and optional JEI display. Built-in fuels and recipes use the same implementation. See [the modding API guide](docs/api.md) for dependency setup, lifecycle, examples, priority rules, and query methods.

## Optional JEI Integration

With a compatible JEI release installed:

- Each Fast Furnace tier's progress arrow opens smelting recipes, and its fire icon opens that tier's fuel category. Every crusher's progress arrow opens the dedicated crushing category, and each is a catalyst for crushing and its own tier's fuel category, not smelting.
- The Accelerator's progress arrow opens its built-in and API-registered conversions, while its fire icon opens accepted boosts and explains their processing speed, exact boosted work, and fully boosted conversion capacities. The Accelerator is a catalyst for both categories.
- The Extremely Fast Alloyer's progress arrow opens its dedicated alloying category, which shows all three shapeless inputs, the output, base processing time, and horizontal-lava reduction rule. The Alloyer is the category catalyst and supports three-input recipe transfer.
- Looking up Fast Coal, a Fast material, or its storage block shows its Accelerator conversion recipe alongside its other crafting sources. The conversion category displays normal and registered boost-speed durations and the four-side lava requirement.
- The crushing category contains the discovered raw-ore recipes and API-registered custom recipes, including input counts, output counts, and the tier-dependent 10–25% chance to double the complete result. Looking up a crushed item shows how it is made, while looking up a raw ore shows its crushing use.
- Fuel categories calculate items smelted from each tier's actual cooking speed, including built-in and API-registered fuel speed and duration bonuses.
- Recipe transfer supports smelting for the four Fast Furnace tiers, crushing for all four crusher tiers, Accelerator conversions, three-input Alloyer recipes, and fuel or boost insertion for the machines that use them. Smelting transfers and click areas remain disabled for crushers.
- Every furnace includes a short speed and fuel-doubling description; each crusher instead describes its tier's crushing speed and output-doubling chance. Fast Cobblestone and Fast Coal include acquisition information, Fast ores explain new and retroactive generation and drops, Fast materials explain their processing chains and storage blocks, special fuels explain their bonuses, the Alloyer and Extremely Fast Ingot explain alloying, and every Fast or Extremely Fast tool and sword includes a simple description of its advantages and repair material.
- JEI reserves space around the furnace, Accelerator, and Alloyer information fly-outs.

**Compatibility:** JEI 29.2.0.21 supports Minecraft 26.1 exactly and cannot launch with this mod's Minecraft 26.1.2 environment. The JEI APIs are therefore compile-only by default, and the integration has not been runtime-validated on 26.1.2. A JEI release compatible with both Minecraft 26.1.2 and this integration is required.

## Requirements and Installation

- Minecraft 26.1.2
- NeoForge 26.1.2.126 or newer
- Java 25

Build the mod as described below, then copy the JAR from `build/libs/` into the Minecraft `mods` directory. JEI is optional when a compatible release is available.

## Build and Run

Development requires JDK 25. Use the included Gradle wrapper; no separate Gradle installation is needed.

```bash
# Windows
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runGameTestServer

# macOS / Linux
./gradlew build
./gradlew runClient
./gradlew runServer
./gradlew runGameTestServer
```

The build compiles the mod, runs the checks configured in Gradle, and writes the JAR to `build/libs/`. GameTests run separately with `runGameTestServer`. Use `runData` to generate data resources into `src/generated/resources`.

For a clean rebuild, replace `build` with `clean build`.

## Textures

The reusable Pillow-based generator, preserved vanilla source images, environment setup, regeneration commands, and test instructions are documented in [tools/README.md](tools/README.md). Always regenerate from the square source textures, not from generated animation strips.

## Contributing

Keep changes focused, remove obsolete code and resources, and update relevant documentation. Run the build and the tests applicable to the changed area before submitting changes. See [CLAUDE.md](CLAUDE.md) for architecture, implementation constraints, and detailed validation guidance.

GitHub Actions runs `./gradlew build` on pushes and pull requests using JDK 25.

## AI Assistance Disclosure

This project was developed with assistance from [Claude Code](https://claude.com/claude-code), Anthropic's AI coding assistant. AI-assisted contributions include Java code, tests, documentation, resource files, texture-generation tooling, and generated textures. Development is directed by the project maintainer. Validation commands are documented in this README. This disclosure does not imply endorsement by Anthropic.

## License

This project is licensed under the [MIT License](LICENSE).

The original NeoForge MDK template license is retained in [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt). Minecraft, NeoForge, Mojang mappings, and copied vanilla assets are governed by their respective licenses.
