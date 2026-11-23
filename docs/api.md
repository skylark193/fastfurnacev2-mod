# Fast Furnace V2 modding API

[Documentation home](README.md)

Fast Furnace V2 1.3.0 exposes API version 1.0 for other NeoForge mods. The API supports custom ordinary fuels, tier-dependent fuel bonuses, Fast Crusher recipes, Accelerator conversions, and Accelerator boost fuels. Registered content runs in the actual machines, participates in automation and persistence, appears in the optional JEI integration, and uses the same implementation as the built-in content.

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
    compileOnly "com.skylark193.fastfurnacev2:fastfurnacev2:1.3.0"
}
```

There is no public Maven repository implied by this example. A local `compileOnly files("libs/fastfurnacev2-1.3.0.jar")` dependency also works for compilation. Install the mod in the development runtime as required by your toolchain.

Declare the runtime dependency in `neoforge.mods.toml`:

```toml
[[dependencies.examplemod]]
modId="fastfurnacev2"
type="required"
versionRange="[1.3.0,)"
ordering="NONE"
side="BOTH"
```

Replace `examplemod` with your mod ID. Use `type="optional"` instead if your mod works without Fast Furnace V2 and isolates all API references behind an optional integration.

## Registration lifecycle

Registration events are fired once on the **mod event bus** after NeoForge binds default item components, when constructing recipe `ItemStack`s is valid. Fast Furnace V2 listens for `DefaultDataComponentsBoundEvent` and initializes only when `shouldUpdateStaticData()` is true, avoiding duplicate registration on an integrated client. Subscribe from your mod constructor, not from the NeoForge game-event bus:

```java
@Mod("examplemod")
public final class ExampleMod {
    public ExampleMod(IEventBus modEventBus) {
        modEventBus.addListener(this::registerFuels);
        modEventBus.addListener(this::registerCrusherRecipes);
        modEventBus.addListener(this::registerAcceleratorRecipes);
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
}
```

Registration occurs once per game process, not on every datapack reload. In Minecraft 26.1.2, common setup is too early to construct `ItemStack`s because their default components are not bound yet. Every definition needs a unique namespaced `Identifier` within its content type. Duplicate IDs fail registration rather than silently replacing another definition.

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
- `speedBonusPercent`: percentage points added to the machine tier's normal speed. For example, Fast Furnace is normally 150%; a bonus of 25 produces 175%, and its standard 200-tick recipe takes 114 ticks. Zero leaves speed unchanged.
- `acceleratorBoost(work, workPerTick)`: optional exact boosted work and its maximum work advance per tick. Both values must be positive together or both zero. The Accelerator still runs without boost fuel.
- `priority`: ingredient-match precedence; later registration breaks equal-priority ties.

Active fuel identity is saved by definition ID. The machine continues using the consumed fuel's bonuses even if the fuel slot subsequently contains another item. The built-in Fast Coal, Block of Fast Coal, and Extreme Coal definitions use IDs exposed as `FastFurnaceApi.FAST_COAL_FUEL_ID`, `FAST_COAL_BLOCK_FUEL_ID`, and `EXTREME_COAL_FUEL_ID`.

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

## Queries and built-in content access

After the registration events have fired, use `FastFurnaceApi` to access content:

```java
List<FastFurnaceTier> tiers = FastFurnaceApi.tiers();
Item fastFurnace = FastFurnaceApi.furnace(FastFurnaceTier.FAST);
Item diamondCrusher = FastFurnaceApi.crusher(FastFurnaceTier.DIAMOND);
Item accelerator = FastFurnaceApi.accelerator();
Item fastCobblestone = FastFurnaceApi.fastCobblestone();
Item fastCoal = FastFurnaceApi.fastCoal();
Item fastCoalBlock = FastFurnaceApi.fastCoalBlock();
Item extremeCoalOre = FastFurnaceApi.extremeCoalOre();
Item extremeCoal = FastFurnaceApi.extremeCoal();

List<FastFuel> fuels = FastFurnaceApi.fuels();
Optional<FastFuel> byId = FastFurnaceApi.findFuel(fuelId);
Optional<FastFuel> byStack = FastFurnaceApi.findFuel(stack);

List<CrusherRecipe> crusherRecipes = FastFurnaceApi.crusherRecipes();
Optional<CrusherRecipe> crusherRecipe = FastFurnaceApi.findCrusherRecipe(input);

List<AcceleratorConversion> conversions = FastFurnaceApi.acceleratorRecipes();
boolean canEnterInputSlot = FastFurnaceApi.isAcceleratorInput(input);
Optional<AcceleratorConversion> conversion = FastFurnaceApi.findAcceleratorRecipe(input);
```

`findCrusherRecipe` and `findAcceleratorRecipe` require enough input items to process. `isAcceleratorInput` checks only ingredient acceptance, making it suitable for slot and automation logic. Query collections are immutable, and returned output/display stacks are fresh copies. Do not retain and mutate a definition's internal state; definitions themselves are immutable.

The current API version is available as `FastFurnaceApi.API_VERSION`.

## Recipe viewers and validation

When a compatible JEI is installed, Fast Furnace V2 automatically displays registered crusher recipes, Accelerator conversions, Accelerator boosts, and per-tier custom fuel durations and smelt counts. No separate JEI registration is required for this content.

The mod's GameTest server enables opt-in example definitions with the `fastfurnacev2.apiTestContent=true` system property and tests their actual machine behavior. These examples are not registered in ordinary client or server runs.
