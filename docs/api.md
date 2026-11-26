# Fast Furnace V2 modding API

[Documentation home](README.md)

Fast Furnace V2 1.12.0 exposes API version 1.4.0 for other NeoForge mods. The API supports custom ordinary fuels, tier-dependent fuel bonuses, Fast Crusher recipes, Accelerator conversions, Accelerator boost fuels, Extremely Fast Alloyer recipes, tool-stat overrides, custom tool modifiers, reusable energy storage, recipe and ingredient queries, dynamic crushed-ore mappings, and access to every fixed built-in item. Registered content runs in the actual machines and tool handlers, participates in automation and persistence where applicable, appears in the optional JEI integration where applicable, and uses the same implementation as the built-in content.

The public API is under `com.skylark193.fastfurnacev2.api`. Classes under `com.skylark193.fastfurnacev2.internal` are implementation details and must not be used by other mods.

## Dependency setup

Add Fast Furnace V2 to your compile classpath and development runtime using your mod toolchain. This repository can publish to its ignored project-local Maven repository with `./gradlew publish`; a consuming Gradle project can then use:

```groovy
repositories {
    maven {
        // Replace with the path to the Fast Furnace V2 checkout's repo directory,
        // or with the Maven repository from which your project obtains the mod.
        url = uri("path/to/fastfurnacev2-mod/repo")
    }
}

dependencies {
    compileOnly "com.skylark193.fastfurnacev2:fastfurnacev2:1.12.0"
}
```

There is no public Maven repository implied by this example. A local `compileOnly files("libs/fastfurnacev2-26.1.2.jar")` dependency also works for compilation. Install the mod in the development runtime as required by your toolchain.

Declare the runtime dependency in `neoforge.mods.toml`:

```toml
[[dependencies.examplemod]]
modId="fastfurnacev2"
type="required"
versionRange="[1.12.0,)"
ordering="NONE"
side="BOTH"
```

Replace `examplemod` with your mod ID. Use `type="optional"` instead if your mod works without Fast Furnace V2 and isolates all API references behind an optional integration.

## Registration lifecycle

Definition-registration events are fired once on the **mod event bus** after NeoForge binds default item components, when constructing recipe `ItemStack`s is valid. Fast Furnace V2 listens for `DefaultDataComponentsBoundEvent` and initializes only when `shouldUpdateStaticData()` is true, avoiding duplicate registration on an integrated client. `ModifyFastToolStatsEvent` instead fires earlier from `ModifyDefaultComponentsEvent`, while default components are still mutable. Subscribe from your mod constructor, not from the NeoForge game-event bus:

```java
@Mod("examplemod")
public final class ExampleMod {
    public ExampleMod(IEventBus modEventBus) {
        modEventBus.addListener(this::registerFuels);
        modEventBus.addListener(this::registerCrusherRecipes);
        modEventBus.addListener(this::registerAcceleratorRecipes);
        modEventBus.addListener(this::registerAlloyerRecipes);
        modEventBus.addListener(this::modifyToolStats);
        modEventBus.addListener(this::registerToolModifiers);
    }

    private void registerFuels(RegisterFastFuelsEvent event) {
        // Register fuel definitions here.
    }

    private void registerCrusherRecipes(RegisterFastCrusherRecipesEvent event) {
        // Register crusher recipes here.
    }

    private void registerAcceleratorRecipes(RegisterAcceleratorRecipesEvent event) {
        // Register Accelerator conversions here.
    }

    private void registerAlloyerRecipes(RegisterExtremelyFastAlloyerRecipesEvent event) {
        // Register three-input Alloyer recipes here.
    }

    private void modifyToolStats(ModifyFastToolStatsEvent event) {
        // Patch built-in or other mods' tool defaults here.
    }

    private void registerToolModifiers(RegisterFastToolModifiersEvent event) {
        // Register runtime tool proc chances here.
    }
}
```

Registration occurs once per game process, not on every datapack reload. In Minecraft 26.1.2, common setup is too early to construct `ItemStack`s because their default components are not bound yet. Tool-stat patches must use the earlier component-modification event rather than the definition-registration events. Every definition needs a unique namespaced `Identifier` within its content type. Duplicate IDs fail registration rather than silently replacing another definition.

Ingredients can match items, tags, or other ingredient types supported by `Ingredient.test(ItemStack)`. When multiple definitions of the same type match, higher `priority` wins. Later registrations win equal-priority ties. Built-in definitions use priority zero, so a positive priority can override one without reusing its ID.

## Fuels and custom bonuses

`RegisterFastFuelsEvent` accepts immutable `FastFuel` definitions. A simple ordinary fuel needs only an ID, item, and burn time:

```java
private void registerFuels(RegisterFastFuelsEvent event) {
    event.register(
            Identifier.fromNamespaceAndPath("examplemod", "blaze_rod_fuel"),
            Items.BLAZE_ROD,
            2400);
}
```

Use the builder for tier-dependent duration, speed, or Accelerator behavior:

```java
private void registerFuels(RegisterFastFuelsEvent event) {
    event.register(FastFuel.builder(
                    Identifier.fromNamespaceAndPath("examplemod", "powered_blaze_powder"),
                    Ingredient.of(Items.BLAZE_POWDER),
                    800)
            .burnTimePercent(200)       // 1,600 ticks in every Fast Furnace/Crusher tier
            .speedBonusPercent(25)      // Fast tier: 150% + 25 = 175% processing speed
            .acceleratorBoost(800, 4)  // 800 work units, up to 4 work per tick
            .priority(10)
            .build());
}
```

The parameters are:

- `baseBurnTime`: the ordinary furnace burn time in ticks. Positive values are supplied through NeoForge's fuel burn-time event. Zero preserves the item's existing vanilla or modded burn time, allowing bonuses on an existing fuel or an Accelerator-only definition.
- `burnTimePercent`: a fixed integer percentage or `ToIntFunction<FastFurnaceTier>`. It scales the effective ordinary burn time before a Fast Furnace's independent fuel-doubling roll. Fast Crushers never roll for doubled fuel duration. Use 100 for no scaling. Long arithmetic and clamping prevent overflow.
- `speedBonusPercent`: percentage points added to the machine tier's normal speed. For example, Fast Furnace is normally 150%; a bonus of 25 produces 175%, and its standard 200-tick recipe takes 114 ticks. Zero leaves speed unchanged. The combined speed saturates at `Integer.MAX_VALUE` rather than overflowing.
- `acceleratorBoost(work, workPerTick)`: optional exact boosted work and its maximum work advance per tick. Both values must be positive together or both zero. The Accelerator still runs without boost fuel.
- `priority`: ingredient-match precedence; later registration breaks equal-priority ties.

Active fuel identity is saved by definition ID. The machine continues using the consumed fuel's bonuses even if the fuel slot subsequently contains another item. The built-in Fast Coal, Block of Fast Coal, Extreme Coal, and Block of Extreme Coal definitions use IDs exposed as `FastFurnaceApi.FAST_COAL_FUEL_ID`, `FAST_COAL_BLOCK_FUEL_ID`, `EXTREME_COAL_FUEL_ID`, and `EXTREME_COAL_BLOCK_FUEL_ID`. Block of Extreme Coal has an ordinary burn time of 16,000 ticks, uses Extreme Coal's tier-duration and +100-percentage-point speed bonuses, and supplies 8,000 Accelerator work units at five work per tick. Its item accessor is `FastFurnaceApi.extremeCoalBlock()`.

`FastFuel.inputItems()` returns fresh display stacks for recipe viewers. `scaleBurnTime(...)` and `getSpeedPercent(...)` expose the same calculations used by the machines.

## Fast Crusher recipes

Register a `CrusherRecipe` with an ingredient, input count, output stack, and processing time:

```java
private void registerCrusherRecipes(RegisterFastCrusherRecipesEvent event) {
    event.register(new CrusherRecipe(
            Identifier.fromNamespaceAndPath("examplemod", "cobblestone_to_gravel"),
            Ingredient.of(Items.COBBLESTONE),
            2,                              // consume two cobblestone
            new ItemStack(Items.GRAVEL, 3), // produce three gravel
            200,                            // 200 ticks at vanilla speed
            10));                           // priority
}
```

The full constructor is `(id, ingredient, inputCount, output, processingTime, priority)`. The convenience constructor `(id, ingredient, output, processingTime)` uses one input and priority zero.

- Processing time is scaled by crusher tier and the active fuel's speed bonus.
- A processing time of zero inherits the input's ordinary smelting time, or 200 ticks when no smelting recipe exists.
- The output can have any count that leaves room for the tier's output-doubling roll. For example, three gravel can double to six. A blocked doubled result remains pending without consuming input or rerolling.
- A recipe is processable only when the input stack contains at least `inputCount`. Successful processing consumes exactly that many items.
- Built-in raw-ore recipes use IDs of the form `fastfurnacev2:crusher/<raw namespace>/<raw path>` and priority zero.
- Built-in ore-block recipes use `fastfurnacev2:crusher/ore/<ore path>` for `extreme_coal_ore`, `deepslate_extreme_coal_ore`, `fast_iron_ore`, `deepslate_fast_iron_ore`, `fast_gold_ore`, `deepslate_fast_gold_ore`, `fast_diamond_ore`, and `deepslate_fast_diamond_ore`. Each consumes one ore block and produces three Extreme Coal or three matching crushed Fast materials; the normal output-doubling roll produces six.

The API does not add an ordinary smelting recipe or generate a crushed item for custom crusher output. Register those separately if desired.

## Accelerator conversions and boosts

Register an `AcceleratorConversion` with exact work units:

```java
private void registerAcceleratorRecipes(RegisterAcceleratorRecipesEvent event) {
    event.register(new AcceleratorConversion(
            Identifier.fromNamespaceAndPath("examplemod", "charcoal_to_coal"),
            Ingredient.of(Items.CHARCOAL),
            2,                            // consume two charcoal
            new ItemStack(Items.COAL, 2), // produce two coal
            200,                          // 200 unboosted ticks/work units
            10));                         // priority
}
```

The full constructor is `(id, ingredient, inputCount, output, workUnits, priority)`. The convenience constructor `(id, ingredient, output, workUnits)` uses one input and priority zero.

One work unit equals one unboosted tick. An active boost advances up to its `acceleratorWorkPerTick` each tick, clamped by remaining recipe work and remaining boost. The example takes 200 ticks unboosted or 50 ticks with a four-work-per-tick boost.

The existing four-horizontal-lava requirement, blocked-output pause/resume, input-change reset, transaction handling, persistence, and automation rules apply to custom conversions. Slots and hoppers accept a matching ingredient even before its full required count is present; processing starts only when enough items are available.

Built-in conversion IDs are `fastfurnacev2:accelerator/coal`, `coal_block`, `iron`, `iron_block`, `gold`, `gold_block`, `diamond`, and `diamond_block` (each suffix after `fastfurnacev2:accelerator/`). Item conversions require 400 work units and block conversions require 3,600.

## Extremely Fast Alloyer recipes

Register an `ExtremelyFastAlloyerRecipe` with exactly three ingredients, a nonempty output, and a positive base processing time:

```java
private void registerAlloyerRecipes(RegisterExtremelyFastAlloyerRecipesEvent event) {
    event.register(new ExtremelyFastAlloyerRecipe(
            Identifier.fromNamespaceAndPath("examplemod", "custom_alloy"),
            List.of(
                    Ingredient.of(Items.IRON_INGOT),
                    Ingredient.of(Items.GOLD_INGOT),
                    Ingredient.of(Items.DIAMOND)),
            new ItemStack(Items.NETHERITE_SCRAP),
            800,  // base ticks before horizontal-lava reductions
            10)); // priority
}
```

The full constructor is `(id, ingredients, output, processingTicks, priority)`. The four-argument constructor uses priority zero. Ingredients are shapeless but must match three distinct nonempty input slots; processing consumes one item from each matched slot. Priority applies to the complete recipe match, with later registration breaking equal-priority ties.

Custom recipes use only the Extremely Fast Alloyer and require Extreme Coal. Each horizontal lava side subtracts 100 ticks, with the usual 400-tick floor; lava never increases the duration of a recipe whose base time is already shorter. Existing input reset, output blocking, fuel preservation, transaction, persistence, and automation rules apply. Registered recipes automatically appear in the Alloyer's JEI category.

The built-in Extremely Fast Ingot recipe ID is `FastFurnaceApi.EXTREMELY_FAST_INGOT_RECIPE_ID`.

## Tool statistics and custom tools

### Modify default tool statistics

`ModifyFastToolStatsEvent` patches an item's current defaults. Pass either its registered `Item` or its `Identifier`; patches compose in listener order, so each callback receives earlier changes:

```java
private void modifyToolStats(ModifyFastToolStatsEvent event) {
    event.modify(
            Identifier.fromNamespaceAndPath("fastfurnacev2", "fast_diamond_pickaxe"),
            stats -> stats
                    .withDurability(stats.durability() + 500)
                    .withMiningSpeedMultiplier(1.25F)
                    .withAttackDamage(stats.attackDamage() + 1.0)
                    .withAttackSpeed(stats.attackSpeed() + 0.25)
                    .withEnchantmentValue(15));
}
```

- Durability and enchantment value must be positive.
- The mining-speed multiplier scales every finite speed rule while preserving their relative speeds. It composes with earlier patches and leaves drop-correctness rules unchanged.
- Attack damage includes the player's base damage of one; attack speed includes the player's base four attacks per second. Their defaults are read from the standard main-hand attribute modifiers.
- Built-in Fortune II and Looting II defaults are separate enchantment components and are not changed by `withEnchantmentValue`.

Use this event only for default components. Register runtime proc chances separately through `RegisterFastToolModifiersEvent`.

### Create a custom tool

Create items with `FastFurnaceApi.createTool` during normal item registration. It supports pickaxes, shovels, axes, and swords with the same vanilla-style `ToolMaterial` and property baselines used by Fast Furnace V2:

```java
public static final DeferredItem<Item> CUSTOM_PICKAXE = ITEMS.registerItem(
        "custom_pickaxe",
        properties -> FastFurnaceApi.createTool(
                FastToolType.PICKAXE,
                ToolMaterial.IRON,
                1.0F,   // vanilla-style attack-damage baseline
                -2.8F,  // vanilla-style attack-speed baseline
                properties));
```

The factory preserves axe stripping and shovel flattening. Its item classes route durability damage through registered Fast Furnace modifiers. Block-drop, kill-drop, and melee-damage modifiers also work on ordinary items from other mods; only durability-save rolls require this factory or equivalent `damageItem` integration.

### Register runtime modifiers

Register one immutable `FastToolModifiers` definition after default components bind:

```java
private void registerToolModifiers(RegisterFastToolModifiersEvent event) {
    event.register(FastToolModifiers.builder(
                    Identifier.fromNamespaceAndPath("examplemod", "custom_pickaxe_modifiers"),
                    Ingredient.of(CUSTOM_PICKAXE.get()))
            .durabilitySaveChance(0.05F)
            .blockDropDoubleChance(0.05F)
            .killDropDoubleChance(0.05F)
            .damageDoubleChance(0.10F)
            .priority(10)
            .build());
}
```

Each chance is an independent probability from zero through one; omitted chances default to zero. One highest-priority matching definition supplies all four chances, with later registration breaking equal-priority ties. Chances are not merged across definitions.

- Durability saving rolls on the server for positive damage amounts.
- Block-drop doubling requires a noncreative player, enabled block drops, and a correct tool. Complete drops are copied after normal loot and enchantments.
- Kill-drop doubling and damage doubling require a direct `PLAYER_ATTACK`; arrows, Thorns, and other indirect sources cannot proc them. Damage doubles before mitigation.

Built-in definitions use `FastFurnaceApi.FAST_DIAMOND_TOOL_MODIFIERS_ID`, `EXTREMELY_FAST_HARVESTING_TOOL_MODIFIERS_ID`, and `EXTREMELY_FAST_SWORD_MODIFIERS_ID`. Their priority is zero, so a higher-priority ingredient definition can override their proc chances without reusing an ID.

## Energy storage

`FastEnergyStorage` implements NeoForge's current transactional `EnergyHandler` API by extending `SimpleEnergyHandler`. It adds a change callback suitable for marking a block entity dirty and scheduling client synchronization. The older `IEnergyStorage` API is deprecated; new integrations should use `EnergyHandler` directly.

Create storage with a capacity and either one shared transfer limit or separate insertion/extraction limits:

```java
private final FastEnergyStorage energy =
        new FastEnergyStorage(10_000, 1_000, 500, this::setChanged);

public EnergyHandler energy() {
    return energy;
}
```

All configuration values must be nonnegative. The example accepts at most 1,000 energy and extracts at most 500 energy per operation. A zero insertion or extraction limit disables that direction. Operations clamp to the applicable limit, stored amount, and remaining capacity. Zero-amount operations return zero; negative amounts throw `IllegalArgumentException`.

Persist the storage in its containing block entity using a named child:

```java
@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    energy.deserialize(input.childOrEmpty("energy"));
}

@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    energy.serialize(output.child("energy"));
}
```

Expose that same instance through NeoForge's standard block capability during mod-bus capability registration:

```java
private void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
            Capabilities.Energy.BLOCK,
            EXAMPLE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity.energy());
}
```

Consumers perform insertion and extraction in transactions. Aborting rolls back the amount without firing the callback; a changed root commit fires it once even if several operations occurred:

```java
try (Transaction transaction = Transaction.openRoot()) {
    int extracted = energy.extract(500, transaction);
    // Use the extracted energy, then commit only if the whole operation succeeds.
    transaction.commit();
}
```

To simulate, let the transaction close without committing. Direct `set(int)` changes notify immediately; loading saved energy does not notify. Fast Furnaces, Crushers, the Accelerator, and the Alloyer still do not expose or consume energy. Generators, batteries, and power conduits expose standard block energy capabilities; battery items also expose `Capabilities.Energy.ITEM` with their tier capacity and transfer limit. Battery item charge is stored in `fastfurnacev2:stored_energy` and survives breaking and placement.

## Queries and built-in content access

After the registration events have fired, use `FastFurnaceApi` to access content:

```java
List<FastFurnaceTier> tiers = FastFurnaceApi.tiers();
Item fastFurnace = FastFurnaceApi.furnace(FastFurnaceTier.FAST);
Item diamondCrusher = FastFurnaceApi.crusher(FastFurnaceTier.DIAMOND);
Item generator = FastFurnaceApi.generator(FastFurnaceTier.GOLD);
Item battery = FastFurnaceApi.battery(FastFurnaceTier.IRON);
Item conduit = FastFurnaceApi.conduit(FastFurnaceTier.DIAMOND);
Item accelerator = FastFurnaceApi.accelerator();
Item alloyer = FastFurnaceApi.extremelyFastAlloyer();
Item extremelyFastIngot = FastFurnaceApi.extremelyFastIngot();
Item fastCobblestone = FastFurnaceApi.fastCobblestone();
Item fastCoal = FastFurnaceApi.fastCoal();
Item fastCoalBlock = FastFurnaceApi.fastCoalBlock();
Item extremeCoalOre = FastFurnaceApi.extremeCoalOre();
Item extremeCoal = FastFurnaceApi.extremeCoal();
Item extremeCoalBlock = FastFurnaceApi.extremeCoalBlock();

List<FastFuel> fuels = FastFurnaceApi.fuels();
Optional<FastFuel> fuelById = FastFurnaceApi.findFuel(fuelId);
Optional<FastFuel> fuelByStack = FastFurnaceApi.findFuel(stack);

List<CrusherRecipe> crusherRecipes = FastFurnaceApi.crusherRecipes();
Optional<CrusherRecipe> crusherById = FastFurnaceApi.findCrusherRecipe(crusherRecipeId);
boolean canEnterCrusher = FastFurnaceApi.isCrusherInput(input);
Optional<CrusherRecipe> crusherMatch = FastFurnaceApi.findCrusherRecipe(input);

List<AcceleratorConversion> conversions = FastFurnaceApi.acceleratorRecipes();
Optional<AcceleratorConversion> conversionById = FastFurnaceApi.findAcceleratorRecipe(conversionId);
boolean canEnterInputSlot = FastFurnaceApi.isAcceleratorInput(input);
Optional<AcceleratorConversion> conversion = FastFurnaceApi.findAcceleratorRecipe(input);

List<ExtremelyFastAlloyerRecipe> alloyerRecipes = FastFurnaceApi.alloyerRecipes();
Optional<ExtremelyFastAlloyerRecipe> alloyerById = FastFurnaceApi.findAlloyerRecipe(alloyerRecipeId);
boolean canEnterAlloyer = FastFurnaceApi.isAlloyerInput(input);
Optional<ExtremelyFastAlloyerRecipe> alloyerMatch = FastFurnaceApi.findAlloyerRecipe(threeInputStacks);

Set<Identifier> rawOreIds = FastFurnaceApi.rawOreIds();
Optional<Item> oneCrushedResult = FastFurnaceApi.crushedOre(rawOreId);
Map<Identifier, Item> allCrushedResults = FastFurnaceApi.crushedOres();

List<FastToolModifiers> toolModifiers = FastFurnaceApi.toolModifiers();
Optional<FastToolModifiers> toolModifiersById = FastFurnaceApi.findToolModifiers(toolModifierId);
Optional<FastToolModifiers> toolModifiersByStack = FastFurnaceApi.findToolModifiers(toolStack);
```

Every fixed built-in item has an accessor. In addition to the examples above:

- Regular and deepslate ores: `deepslateExtremeCoalOre()`, `fastIronOre()`, `deepslateFastIronOre()`, `fastGoldOre()`, `deepslateFastGoldOre()`, `fastDiamondOre()`, and `deepslateFastDiamondOre()`.
- Fast materials and storage: `rawFastIron()`, `fastIronIngot()`, `fastIronBlock()`, `rawFastGold()`, `fastGoldIngot()`, `fastGoldBlock()`, `rawFastDiamond()`, `fastDiamond()`, and `fastDiamondBlock()`.
- All sixteen tools: `fastIronPickaxe/Shovel/Axe/Sword()`, `fastGoldPickaxe/Shovel/Axe/Sword()`, `fastDiamondPickaxe/Shovel/Axe/Sword()`, and `extremelyFastPickaxe/Shovel/Axe/Sword()`. Each tool name is a separate method with its complete suffix, such as `fastIronShovel()`.

`findCrusherRecipe(ItemStack)` and `findAcceleratorRecipe(ItemStack)` require enough input items to process. The corresponding `isCrusherInput` and `isAcceleratorInput` methods, plus `isAlloyerInput`, check only ingredient acceptance, making them suitable for slot and automation logic. The list-based Alloyer query examines the first three stacks and applies distinct-slot shapeless matching. All recipe types and fuels also support lookup by exact registered `Identifier`.

`rawOreIds()` includes discovered sources whose results were registered by other mods. `crushedOre` returns the selected generated or reused result for one source, and `crushedOres()` returns an immutable snapshot of the complete mapping. Query collections and maps are immutable, and returned output/display stacks are fresh copies. Do not retain and mutate a definition's internal state; definitions themselves are immutable.

The current API version is available as `FastFurnaceApi.API_VERSION`.

## Recipe viewers and validation

When a compatible JEI is installed, Fast Furnace V2 automatically displays registered crusher recipes, Accelerator conversions, Alloyer recipes, Accelerator boosts, and per-tier custom fuel durations and smelt counts. No separate JEI registration is required for this content.

The mod's GameTest server enables opt-in example definitions with the `fastfurnacev2.apiTestContent=true` system property and tests their actual machine behavior. These examples are not registered in ordinary client or server runs.
