# Blocks

[Documentation home](README.md) · [Items](items.md) · [Recipes](recipes.md)

Fast Furnace V2 registers **36 blocks**, each with a corresponding block item. All appear in the **`fastfurnacev2:fast_furnace_v2_tab`** creative tab, titled **Fast Furnace V2**, whose icon is Lava-Infused Cobblestone. Additional vanilla-tab placement is listed below.

## Complete block index

| Block | Registry ID | Vanilla creative tab | Built-in acquisition |
|---|---|---|---|
| Lava-Infused Cobblestone | `fastfurnacev2:fast_cobblestone` | Building Blocks | Bonus drop from mining stone |
| Block of Lava-Infused Coal | `fastfurnacev2:fast_coal_block` | Building Blocks | Craft 9 Lava-Infused Coal; unpack back into 9 |
| Block of Lava-Infused Coal | `fastfurnacev2:extreme_coal_block` | Building Blocks | Craft 9 Lava-Infused Coal; unpack back into 9 |
| Lava-Infused Coal Ore | `fastfurnacev2:extreme_coal_ore` | Natural Blocks | Replaces eligible regular coal ore during chunk processing |
| Deepslate Lava-Infused Coal Ore | `fastfurnacev2:deepslate_extreme_coal_ore` | Natural Blocks | Replaces eligible deepslate coal ore during chunk processing |
| Lava-Infused Iron Ore | `fastfurnacev2:fast_iron_ore` | Natural Blocks | Replaces eligible regular iron ore during chunk processing |
| Deepslate Lava-Infused Iron Ore | `fastfurnacev2:deepslate_fast_iron_ore` | Natural Blocks | Replaces eligible deepslate iron ore during chunk processing |
| Lava-Infused Gold Ore | `fastfurnacev2:fast_gold_ore` | Natural Blocks | Replaces eligible regular gold ore during chunk processing |
| Deepslate Lava-Infused Gold Ore | `fastfurnacev2:deepslate_fast_gold_ore` | Natural Blocks | Replaces eligible deepslate gold ore during chunk processing |
| Lava-Infused Diamond Ore | `fastfurnacev2:fast_diamond_ore` | Natural Blocks | Replaces eligible regular diamond ore during chunk processing |
| Deepslate Lava-Infused Diamond Ore | `fastfurnacev2:deepslate_fast_diamond_ore` | Natural Blocks | Replaces eligible deepslate diamond ore during chunk processing |
| Block of Lava-Infused Iron | `fastfurnacev2:fast_iron_block` | Building Blocks | Craft 9 Lava-Infused Iron Ingots or convert an Iron Block |
| Block of Lava-Infused Gold | `fastfurnacev2:fast_gold_block` | Building Blocks | Craft 9 Lava-Infused Gold Ingots or convert a Gold Block |
| Block of Lava-Infused Diamond | `fastfurnacev2:fast_diamond_block` | Building Blocks | Craft 9 Lava-Infused Diamonds or convert a Diamond Block |
| Accelerator | `fastfurnacev2:accelerator` | Functional Blocks | Surround an Iron Lava-Infused Furnace with 8 Redstone Blocks |
| Lava-Infused Alloyer | `fastfurnacev2:extremely_fast_alloyer` | Functional Blocks | Surround an Accelerator with 8 Blocks of Lava-Infused Coal (`extreme_coal_block`) |
| Lava-Infused Furnace | `fastfurnacev2:fast_furnace` | Functional Blocks | Craft from 8 Lava-Infused Cobblestone |
| Iron Lava-Infused Furnace | `fastfurnacev2:iron_fast_furnace` | Functional Blocks | Upgrade Lava-Infused Furnace with 8 Blocks of Lava-Infused Iron |
| Gold Lava-Infused Furnace | `fastfurnacev2:gold_fast_furnace` | Functional Blocks | Upgrade Iron Lava-Infused Furnace with 8 Blocks of Lava-Infused Gold |
| Diamond Lava-Infused Furnace | `fastfurnacev2:diamond_fast_furnace` | Functional Blocks | Upgrade Gold Lava-Infused Furnace with 8 Blocks of Lava-Infused Diamond |
| Lava-Infused Crusher | `fastfurnacev2:fast_crusher` | Functional Blocks | Craft with 3 Lava-Infused Diamonds, 5 Lava-Infused Iron Ingots, and a Lava-Infused Furnace |
| Iron Lava-Infused Crusher | `fastfurnacev2:iron_fast_crusher` | Functional Blocks | Upgrade Lava-Infused Crusher with 8 Blocks of Lava-Infused Iron |
| Gold Lava-Infused Crusher | `fastfurnacev2:gold_fast_crusher` | Functional Blocks | Upgrade Iron Lava-Infused Crusher with 8 Blocks of Lava-Infused Gold |
| Diamond Lava-Infused Crusher | `fastfurnacev2:diamond_fast_crusher` | Functional Blocks | Upgrade Gold Lava-Infused Crusher with 8 Blocks of Lava-Infused Diamond |
| Lava-Infused Generator | `fastfurnacev2:fast_generator` | Functional Blocks | No crafting recipe yet |
| Iron Lava-Infused Generator | `fastfurnacev2:iron_fast_generator` | Functional Blocks | No crafting recipe yet |
| Gold Lava-Infused Generator | `fastfurnacev2:gold_fast_generator` | Functional Blocks | No crafting recipe yet |
| Diamond Lava-Infused Generator | `fastfurnacev2:diamond_fast_generator` | Functional Blocks | No crafting recipe yet |
| Lava-Infused Battery | `fastfurnacev2:fast_battery` | Functional Blocks | No crafting recipe yet |
| Iron Lava-Infused Battery | `fastfurnacev2:iron_fast_battery` | Functional Blocks | No crafting recipe yet |
| Gold Lava-Infused Battery | `fastfurnacev2:gold_fast_battery` | Functional Blocks | No crafting recipe yet |
| Diamond Lava-Infused Battery | `fastfurnacev2:diamond_fast_battery` | Functional Blocks | No crafting recipe yet |
| Lava-Infused Power Conduit | `fastfurnacev2:fast_power_conduit` | Functional Blocks | No crafting recipe yet |
| Iron Lava-Infused Power Conduit | `fastfurnacev2:iron_fast_power_conduit` | Functional Blocks | No crafting recipe yet |
| Gold Lava-Infused Power Conduit | `fastfurnacev2:gold_fast_power_conduit` | Functional Blocks | No crafting recipe yet |
| Diamond Lava-Infused Power Conduit | `fastfurnacev2:diamond_fast_power_conduit` | Functional Blocks | No crafting recipe yet |

## Lava-Infused Cobblestone

**ID:** `fastfurnacev2:fast_cobblestone`

Lava-Infused Cobblestone mirrors vanilla cobblestone's physical behavior: stone map color, base-drum instrument, hardness **2.0**, blast resistance **6.0**, and a correct-tool drop requirement. Use a pickaxe; its loot table drops Lava-Infused Cobblestone itself.

It has two built-in uses:

- Crafting the base Lava-Infused Furnace with eight blocks around an empty center.
- Decorative building with an animated cobblestone-and-lava-vein texture.

Mining stone can append one Lava-Infused Cobblestone to normal drops. This is a bonus rather than a replacement for vanilla cobblestone. See [Mining bonuses](world-generation.md#mining-bonuses) for tool, Silk Touch, Fortune, and game-rule requirements.

## Block of Lava-Infused Coal (`fast_coal_block`)

**ID:** `fastfurnacev2:fast_coal_block`

Block of Lava-Infused Coal mirrors a vanilla coal block: black map color, base-drum instrument, hardness **5.0**, blast resistance **6.0**, and a correct-tool drop requirement. Use a pickaxe; it drops itself.

It is a reversible storage block and a fuel:

- Craft a full 3×3 grid of Lava-Infused Coal into one block.
- Unpack one block into nine Lava-Infused Coal.
- Use it as an ordinary 16,000-tick fuel outside Fast Furnace V2's machines.
- Use its Lava-Infused Coal speed and tier-duration bonuses in Lava-Infused Furnaces and Lava-Infused Crushers.
- Use it as a 4,000-work, 3-work-per-tick Accelerator boost.

Its ordinary 16,000-tick value matches a vanilla coal block rather than the 14,400 ticks supplied by nine individual Lava-Infused Coal items. See [Lava-Infused Furnace fuel durations](furnaces.md#fuel-duration-bonuses) and [Accelerator boosts](accelerator.md#boost-fuels).

## Block of Lava-Infused Coal (`extreme_coal_block`)

**ID:** `fastfurnacev2:extreme_coal_block`

Block of Lava-Infused Coal mirrors a vanilla coal block's black map color, base-drum instrument, hardness **5.0**, blast resistance **6.0**, and correct-tool drop requirement. Use a pickaxe; it drops itself. Nine Lava-Infused Coal craft one block, and one block unpacks into nine Lava-Infused Coal.

Its ordinary burn time is **16,000 ticks**. It receives `extreme_coal`'s speed and duration bonuses in Lava-Infused Furnaces and Lava-Infused Crushers, and supplies **8,000 Accelerator work units** at up to **5 work per tick**. Its texture replaces every visible coal-block pixel with animated lava while retaining the source's luminance detail.

## Lava-Infused Coal Ores

**IDs:** `fastfurnacev2:extreme_coal_ore`, `fastfurnacev2:deepslate_extreme_coal_ore`

These blocks mirror regular or deepslate coal ore's physical properties without inheriting vanilla loot tables or translation keys. Regular Lava-Infused Coal Ore has stone map color and hardness/blast resistance **3.0/3.0**; the deepslate variant has deepslate map color and **4.5/3.0**. Both require a pickaxe.

- Without Silk Touch, each drops Lava-Infused Coal (`extreme_coal`) using vanilla coal ore's Fortune-scaled ore-drop formula.
- With Silk Touch, each drops the ore block itself.
- Their textures resemble the corresponding vanilla ore with animated lava veins.
- Either block can be crushed into three Lava-Infused Coal (`extreme_coal`) items.

Each eligible regular or deepslate coal-ore position has an independent deterministic 5% replacement chance. Existing chunks receive separate once-only regular and deepslate passes. See [special ore generation](world-generation.md#special-ore-generation).

## Lava-Infused Iron, Gold, and Diamond Ores

**IDs:** `fastfurnacev2:fast_iron_ore`, `fastfurnacev2:fast_gold_ore`, `fastfurnacev2:fast_diamond_ore`, and their `deepslate_`-prefixed variants.

These blocks resemble their corresponding vanilla regular or deepslate ores with animated lava veins. Regular variants use stone map color and hardness/blast resistance **3.0/3.0**; deepslate variants use deepslate map color and **4.5/3.0**. All have the same correct-tool requirement as their vanilla counterparts.

| Ore family | Required pickaxe tier | Ordinary drop | Silk Touch drop |
|---|---|---|---|
| Regular or Deepslate Lava-Infused Iron Ore | Stone or better | Raw Lava-Infused Iron | The mined ore |
| Regular or Deepslate Lava-Infused Gold Ore | Iron or better | Raw Lava-Infused Gold | The mined ore |
| Regular or Deepslate Lava-Infused Diamond Ore | Iron or better | Raw Lava-Infused Diamond | The mined ore |

Ordinary drops use vanilla's `minecraft:ore_drops` Fortune formula and explosion decay. Each matching regular or deepslate ore has an independent deterministic 5% replacement chance in newly generated chunks and once in existing chunks. Every Lava-Infused ore block can also be crushed into three matching crushed Lava-Infused materials. See [special ore generation](world-generation.md#special-ore-generation).

## Lava-Infused material storage blocks

**IDs:** `fastfurnacev2:fast_iron_block`, `fastfurnacev2:fast_gold_block`, `fastfurnacev2:fast_diamond_block`

Each storage block uses its vanilla counterpart's texture with animated lava veins, drops itself, and can be crafted from nine matching Lava-Infused materials or unpacked back into nine. The Accelerator can also convert the corresponding vanilla storage block directly.

| Block | Physical properties | Required pickaxe tier |
|---|---|---|
| Block of Lava-Infused Iron | Metal map color, iron-xylophone instrument, hardness **5.0**, blast resistance **6.0** | Stone or better |
| Block of Lava-Infused Gold | Gold map color, bell instrument, hardness **3.0**, blast resistance **6.0** | Iron or better |
| Block of Lava-Infused Diamond | Diamond map color, hardness **5.0**, blast resistance **6.0** | Iron or better |

These blocks are the required upgrade materials for Iron, Gold, and Diamond Lava-Infused Furnaces and Lava-Infused Crushers. Vanilla iron, gold, and diamond blocks do not satisfy those recipes.

## Lava-Infused Furnace blocks

The four furnace blocks share vanilla-compatible smelting behavior with tiered speed and fuel bonuses. Each has stone map color, base-drum instrument, hardness **3.5**, and blast resistance **3.5**.

| Tier | Speed with ordinary / `fast_coal` / `extreme_coal` | Fuel-doubling chance |
|---|---:|---:|
| Lava-Infused Furnace | 150% / 200% / 250% | 10% |
| Iron Lava-Infused Furnace | 200% / 250% / 300% | 15% |
| Gold Lava-Infused Furnace | 250% / 300% / 350% | 20% |
| Diamond Lava-Infused Furnace | 300% / 350% / 400% | 25% |

Common behavior:

- Require a pickaxe and drop themselves.
- Preserve custom names, but not inventory, experience, progress, or burn state when broken.
- Support directional placement, comparators, the smelting recipe book, and ordinary furnace fuels and remainders.
- Emit light level 13 while lit, with furnace sounds and particles.
- Use distinct front, side, and top textures for each tier, with separate burning fronts.
- Share one animated vein layout across tiers and lit states so adjacent furnace textures tile together.

See [Lava-Infused Furnaces](furnaces.md) for exact processing and fuel tables.

## Lava-Infused Crusher blocks

The four crusher blocks share the corresponding furnace tier's speed and special-fuel duration scaling, but roll to double completed output rather than fuel duration. They use the same stone map color, base-drum instrument, hardness **3.5**, and blast resistance **3.5** as the furnaces.

| Tier | Speed with ordinary / `fast_coal` / `extreme_coal` | Output-doubling chance |
|---|---:|---:|
| Lava-Infused Crusher | 150% / 200% / 250% | 10% |
| Iron Lava-Infused Crusher | 200% / 250% / 300% | 15% |
| Gold Lava-Infused Crusher | 250% / 300% / 350% | 20% |
| Diamond Lava-Infused Crusher | 300% / 350% / 400% | 25% |

Common behavior:

- Require a pickaxe and drop themselves.
- Preserve custom names, but not inventory, progress, fuel, or pending output when broken.
- Support directional placement, comparators, automation, and light level 13 while lit.
- Use animated Netherrack sides and bottoms.
- Use a static top-down blender while idle and an animated rotor and liquid while lit.
- Add the upgrade tier's metal-block texture to the stationary housing around Iron, Gold, and Diamond blenders.

See [Lava-Infused Crushers](crushers.md) for processing, fuel, and pending-output rules.

## Lava-Infused Generator, Lava-Infused Battery, and Lava-Infused Power Conduit blocks

The four generators and four batteries use stone map color, base-drum instrument, hardness **3.5**, and blast resistance **3.5**. All require a pickaxe and drop themselves. They currently have no crafting recipes.

Generators have directional placement, animated lava-vein housing, a separate active front, and light level 13 while producing energy. Their fuel and active burn determine FE output. Batteries expose bidirectional energy storage and use paired side overlays with eleven charge steps; their animated top contains a lightning bolt. Dropped generators do not preserve energy or other machine state. Dropped batteries preserve exact stored FE and restore it when placed.

The four power conduits use metal map color and sound, hardness **1.5**, blast resistance **6.0**, and non-occluding connected-pipe models. They require a pickaxe, drop themselves without preserving buffered FE, and have no crafting recipes yet. Their animated internal lava channels widen with each tier.

See [Generators, batteries, and conduits](energy.md) for exact rates, capacities, fuel behavior, network rules, and persistence.

## Accelerator

**ID:** `fastfurnacev2:accelerator`

The Accelerator is a directional, lava-surrounded converter rather than an ordinary furnace. It has stone map color, base-drum instrument, hardness **3.5**, and blast resistance **3.5**. Craft it by surrounding one Iron Lava-Infused Furnace with eight Redstone Blocks. It requires a pickaxe, drops itself, preserves custom names, and supports comparators and automation.

Its body uses animated smooth stone with lava veins. Its top face has an eleven-stage lightning bolt showing conversion progress. The block can be placed without lava, but all four horizontal neighboring positions must contain source or flowing lava before it can process.

Breaking the Accelerator preserves only its custom name, not inventory, conversion progress, or boost work. See the complete [Accelerator guide](accelerator.md).

## Lava-Infused Alloyer

**ID:** `fastfurnacev2:extremely_fast_alloyer`

The Lava-Infused Alloyer is a directional, three-input alloying machine rather than a furnace, crusher, or Accelerator conversion target. It has stone map color, base-drum instrument, hardness **3.5**, and blast resistance **3.5**. Craft it by surrounding one Accelerator with eight Blocks of Lava-Infused Coal (`extreme_coal_block`). It requires a pickaxe, drops itself, preserves custom names, and supports comparators and automation. Its `lit` block state tracks whether work advanced on the current tick.

Its body replaces every visible smooth-stone pixel with animated lava. Its top face retains the Accelerator's eleven-stage lightning bolt and fills with recipe progress. It accepts only Lava-Infused Coal (`extreme_coal`) items as fuel; each supplies 1,600 valid processing ticks.

Its built-in recipe combines one Lava-Infused Iron Ingot, one Lava-Infused Gold Ingot, and one Lava-Infused Diamond in any input-slot order into one Lava-Infused Ingot. The base operation takes **800 ticks**, reduced by 100 ticks per horizontal lava side to a minimum of 400. Breaking the Alloyer preserves only its custom name, not inventory, fuel, or progress. See the complete [Lava-Infused Alloyer guide](alloyer.md).
