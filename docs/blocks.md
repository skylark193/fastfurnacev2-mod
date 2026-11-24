# Blocks

[Documentation home](README.md) · [Items](items.md) · [Recipes](recipes.md)

Fast Furnace V2 registers **23 blocks**, each with a corresponding block item. All appear in the **`fastfurnacev2:fast_furnace_v2_tab`** creative tab, titled **Fast Furnace V2**, whose icon is Fast Cobblestone. Additional vanilla-tab placement is listed below.

## Complete block index

| Block | Registry ID | Vanilla creative tab | Built-in acquisition |
|---|---|---|---|
| Fast Cobblestone | `fastfurnacev2:fast_cobblestone` | Building Blocks | Bonus drop from mining stone |
| Block of Fast Coal | `fastfurnacev2:fast_coal_block` | Building Blocks | Craft 9 Fast Coal; unpack back into 9 |
| Block of Extreme Coal | `fastfurnacev2:extreme_coal_block` | Building Blocks | Craft 9 Extreme Coal; unpack back into 9 |
| Extreme Coal Ore | `fastfurnacev2:extreme_coal_ore` | Natural Blocks | Replaces eligible regular coal ore during chunk processing |
| Deepslate Extreme Coal Ore | `fastfurnacev2:deepslate_extreme_coal_ore` | Natural Blocks | Replaces eligible deepslate coal ore during chunk processing |
| Fast Iron Ore | `fastfurnacev2:fast_iron_ore` | Natural Blocks | Replaces eligible regular iron ore during chunk processing |
| Deepslate Fast Iron Ore | `fastfurnacev2:deepslate_fast_iron_ore` | Natural Blocks | Replaces eligible deepslate iron ore during chunk processing |
| Fast Gold Ore | `fastfurnacev2:fast_gold_ore` | Natural Blocks | Replaces eligible regular gold ore during chunk processing |
| Deepslate Fast Gold Ore | `fastfurnacev2:deepslate_fast_gold_ore` | Natural Blocks | Replaces eligible deepslate gold ore during chunk processing |
| Fast Diamond Ore | `fastfurnacev2:fast_diamond_ore` | Natural Blocks | Replaces eligible regular diamond ore during chunk processing |
| Deepslate Fast Diamond Ore | `fastfurnacev2:deepslate_fast_diamond_ore` | Natural Blocks | Replaces eligible deepslate diamond ore during chunk processing |
| Block of Fast Iron | `fastfurnacev2:fast_iron_block` | Building Blocks | Craft 9 Fast Iron Ingots or convert an Iron Block |
| Block of Fast Gold | `fastfurnacev2:fast_gold_block` | Building Blocks | Craft 9 Fast Gold Ingots or convert a Gold Block |
| Block of Fast Diamond | `fastfurnacev2:fast_diamond_block` | Building Blocks | Craft 9 Fast Diamonds or convert a Diamond Block |
| Accelerator | `fastfurnacev2:accelerator` | Functional Blocks | Surround an Iron Fast Furnace with 8 Redstone Blocks |
| Fast Furnace | `fastfurnacev2:fast_furnace` | Functional Blocks | Craft from 8 Fast Cobblestone |
| Iron Fast Furnace | `fastfurnacev2:iron_fast_furnace` | Functional Blocks | Upgrade Fast Furnace with 8 Blocks of Fast Iron |
| Gold Fast Furnace | `fastfurnacev2:gold_fast_furnace` | Functional Blocks | Upgrade Iron Fast Furnace with 8 Blocks of Fast Gold |
| Diamond Fast Furnace | `fastfurnacev2:diamond_fast_furnace` | Functional Blocks | Upgrade Gold Fast Furnace with 8 Blocks of Fast Diamond |
| Fast Crusher | `fastfurnacev2:fast_crusher` | Functional Blocks | Craft with 3 Fast Diamonds, 5 Fast Iron Ingots, and a Fast Furnace |
| Iron Fast Crusher | `fastfurnacev2:iron_fast_crusher` | Functional Blocks | Upgrade Fast Crusher with 8 Blocks of Fast Iron |
| Gold Fast Crusher | `fastfurnacev2:gold_fast_crusher` | Functional Blocks | Upgrade Iron Fast Crusher with 8 Blocks of Fast Gold |
| Diamond Fast Crusher | `fastfurnacev2:diamond_fast_crusher` | Functional Blocks | Upgrade Gold Fast Crusher with 8 Blocks of Fast Diamond |

## Fast Cobblestone

**ID:** `fastfurnacev2:fast_cobblestone`

Fast Cobblestone mirrors vanilla cobblestone's physical behavior: stone map color, base-drum instrument, hardness **2.0**, blast resistance **6.0**, and a correct-tool drop requirement. Use a pickaxe; its loot table drops Fast Cobblestone itself.

It has two built-in uses:

- Crafting the base Fast Furnace with eight blocks around an empty center.
- Decorative building with an animated cobblestone-and-lava-vein texture.

Mining stone can append one Fast Cobblestone to normal drops. This is a bonus rather than a replacement for vanilla cobblestone. See [Mining bonuses](world-generation.md#mining-bonuses) for tool, Silk Touch, Fortune, and game-rule requirements.

## Block of Fast Coal

**ID:** `fastfurnacev2:fast_coal_block`

Block of Fast Coal mirrors a vanilla coal block: black map color, base-drum instrument, hardness **5.0**, blast resistance **6.0**, and a correct-tool drop requirement. Use a pickaxe; it drops itself.

It is a reversible storage block and a fuel:

- Craft a full 3×3 grid of Fast Coal into one block.
- Unpack one block into nine Fast Coal.
- Use it as an ordinary 16,000-tick fuel outside Fast Furnace V2's machines.
- Use its Fast Coal speed and tier-duration bonuses in Fast Furnaces and Fast Crushers.
- Use it as a 4,000-work, 3-work-per-tick Accelerator boost.

Its ordinary 16,000-tick value matches a vanilla coal block rather than the 14,400 ticks supplied by nine individual Fast Coal items. See [Fast Furnace fuel durations](furnaces.md#fuel-duration-bonuses) and [Accelerator boosts](accelerator.md#boost-fuels).

## Block of Extreme Coal

**ID:** `fastfurnacev2:extreme_coal_block`

Block of Extreme Coal mirrors a vanilla coal block's black map color, base-drum instrument, hardness **5.0**, blast resistance **6.0**, and correct-tool drop requirement. Use a pickaxe; it drops itself. Nine Extreme Coal craft one block, and one block unpacks into nine Extreme Coal.

Its ordinary burn time is **16,000 ticks**. It receives Extreme Coal's speed and duration bonuses in Fast Furnaces and Fast Crushers, and supplies **8,000 Accelerator work units** at up to **5 work per tick**. Its texture replaces every visible coal-block pixel with animated lava while retaining the source's luminance detail.

## Extreme Coal Ores

**IDs:** `fastfurnacev2:extreme_coal_ore`, `fastfurnacev2:deepslate_extreme_coal_ore`

These blocks mirror regular or deepslate coal ore's physical properties without inheriting vanilla loot tables or translation keys. Regular Extreme Coal Ore has stone map color and hardness/blast resistance **3.0/3.0**; the deepslate variant has deepslate map color and **4.5/3.0**. Both require a pickaxe.

- Without Silk Touch, each drops Extreme Coal using vanilla coal ore's Fortune-scaled ore-drop formula.
- With Silk Touch, each drops the ore block itself.
- Their textures resemble the corresponding vanilla ore with animated lava veins.
- Either block can be crushed into three Extreme Coal.

Each eligible regular or deepslate coal-ore position has an independent deterministic 5% replacement chance. Existing chunks receive separate once-only regular and deepslate passes. See [Fast and Extreme ore generation](world-generation.md#fast-and-extreme-ore-generation).

## Fast Iron, Gold, and Diamond Ores

**IDs:** `fastfurnacev2:fast_iron_ore`, `fastfurnacev2:fast_gold_ore`, `fastfurnacev2:fast_diamond_ore`, and their `deepslate_`-prefixed variants.

These blocks resemble their corresponding vanilla regular or deepslate ores with animated lava veins. Regular variants use stone map color and hardness/blast resistance **3.0/3.0**; deepslate variants use deepslate map color and **4.5/3.0**. All have the same correct-tool requirement as their vanilla counterparts.

| Ore family | Required pickaxe tier | Ordinary drop | Silk Touch drop |
|---|---|---|---|
| Regular or Deepslate Fast Iron Ore | Stone or better | Raw Fast Iron | The mined ore |
| Regular or Deepslate Fast Gold Ore | Iron or better | Raw Fast Gold | The mined ore |
| Regular or Deepslate Fast Diamond Ore | Iron or better | Raw Fast Diamond | The mined ore |

Ordinary drops use vanilla's `minecraft:ore_drops` Fortune formula and explosion decay. Each matching regular or deepslate ore has an independent deterministic 5% replacement chance in newly generated chunks and once in existing chunks. Every Fast ore block can also be crushed into three matching crushed Fast materials. See [Fast and Extreme ore generation](world-generation.md#fast-and-extreme-ore-generation).

## Fast material storage blocks

**IDs:** `fastfurnacev2:fast_iron_block`, `fastfurnacev2:fast_gold_block`, `fastfurnacev2:fast_diamond_block`

Each storage block uses its vanilla counterpart's texture with animated lava veins, drops itself, and can be crafted from nine matching Fast materials or unpacked back into nine. The Accelerator can also convert the corresponding vanilla storage block directly.

| Block | Physical properties | Required pickaxe tier |
|---|---|---|
| Block of Fast Iron | Metal map color, iron-xylophone instrument, hardness **5.0**, blast resistance **6.0** | Stone or better |
| Block of Fast Gold | Gold map color, bell instrument, hardness **3.0**, blast resistance **6.0** | Iron or better |
| Block of Fast Diamond | Diamond map color, hardness **5.0**, blast resistance **6.0** | Iron or better |

These blocks are the required upgrade materials for Iron, Gold, and Diamond Fast Furnaces and Fast Crushers. Vanilla iron, gold, and diamond blocks do not satisfy those recipes.

## Fast Furnace blocks

The four furnace blocks share vanilla-compatible smelting behavior with tiered speed and fuel bonuses. Each has stone map color, base-drum instrument, hardness **3.5**, and blast resistance **3.5**.

| Tier | Speed with ordinary / Fast Coal / Extreme Coal | Fuel-doubling chance |
|---|---:|---:|
| Fast Furnace | 150% / 200% / 250% | 10% |
| Iron Fast Furnace | 200% / 250% / 300% | 15% |
| Gold Fast Furnace | 250% / 300% / 350% | 20% |
| Diamond Fast Furnace | 300% / 350% / 400% | 25% |

Common behavior:

- Require a pickaxe and drop themselves.
- Preserve custom names, but not inventory, experience, progress, or burn state when broken.
- Support directional placement, comparators, the smelting recipe book, and ordinary furnace fuels and remainders.
- Emit light level 13 while lit, with furnace sounds and particles.
- Use distinct front, side, and top textures for each tier, with separate burning fronts.
- Share one animated vein layout across tiers and lit states so adjacent furnace textures tile together.

See [Fast Furnaces](furnaces.md) for exact processing and fuel tables.

## Fast Crusher blocks

The four crusher blocks share the corresponding furnace tier's speed and special-fuel duration scaling, but roll to double completed output rather than fuel duration. They use the same stone map color, base-drum instrument, hardness **3.5**, and blast resistance **3.5** as the furnaces.

| Tier | Speed with ordinary / Fast Coal / Extreme Coal | Output-doubling chance |
|---|---:|---:|
| Fast Crusher | 150% / 200% / 250% | 10% |
| Iron Fast Crusher | 200% / 250% / 300% | 15% |
| Gold Fast Crusher | 250% / 300% / 350% | 20% |
| Diamond Fast Crusher | 300% / 350% / 400% | 25% |

Common behavior:

- Require a pickaxe and drop themselves.
- Preserve custom names, but not inventory, progress, fuel, or pending output when broken.
- Support directional placement, comparators, automation, and light level 13 while lit.
- Use animated Netherrack sides and bottoms.
- Use a static top-down blender while idle and an animated rotor and liquid while lit.
- Add the upgrade tier's metal-block texture to the stationary housing around Iron, Gold, and Diamond blenders.

See [Fast Crushers](crushers.md) for processing, fuel, and pending-output rules.

## Accelerator

**ID:** `fastfurnacev2:accelerator`

The Accelerator is a directional, lava-surrounded converter rather than an ordinary furnace. It has stone map color, base-drum instrument, hardness **3.5**, and blast resistance **3.5**. Craft it by surrounding one Iron Fast Furnace with eight Redstone Blocks. It requires a pickaxe, drops itself, preserves custom names, and supports comparators and automation.

Its body uses animated smooth stone with lava veins. Its top face has an eleven-stage lightning bolt showing conversion progress. The block can be placed without lava, but all four horizontal neighboring positions must contain source or flowing lava before it can process.

Breaking the Accelerator preserves only its custom name, not inventory, conversion progress, or boost work. See the complete [Accelerator guide](accelerator.md).
