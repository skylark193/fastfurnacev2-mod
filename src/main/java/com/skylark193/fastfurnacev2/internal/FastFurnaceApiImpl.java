package com.skylark193.fastfurnacev2.internal;

import com.mojang.logging.LogUtils;
import com.skylark193.fastfurnacev2.AcceleratorBlockEntity;
import com.skylark193.fastfurnacev2.CrushedOres;
import com.skylark193.fastfurnacev2.ExtremelyFastAlloyerRecipes;
import com.skylark193.fastfurnacev2.FastFurnaceBlockEntity;
import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.FastTools;
import com.skylark193.fastfurnacev2.api.AcceleratorConversion;
import com.skylark193.fastfurnacev2.api.CrusherRecipe;
import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import com.skylark193.fastfurnacev2.api.FastToolModifiers;
import com.skylark193.fastfurnacev2.api.event.ModifyFastToolStatsEvent;
import com.skylark193.fastfurnacev2.api.event.RegisterAcceleratorRecipesEvent;
import com.skylark193.fastfurnacev2.api.event.RegisterFastCrusherRecipesEvent;
import com.skylark193.fastfurnacev2.api.event.RegisterExtremelyFastAlloyerRecipesEvent;
import com.skylark193.fastfurnacev2.api.event.RegisterFastFuelsEvent;
import com.skylark193.fastfurnacev2.api.event.RegisterFastToolModifiersEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

/** Internal storage and lifecycle for the public API. Not part of the public API surface. */
@ApiStatus.Internal
public final class FastFurnaceApiImpl {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Entry<FastFuel>> FUELS = new ArrayList<>();
    private static final List<Entry<CrusherRecipe>> CRUSHER_RECIPES = new ArrayList<>();
    private static final List<Entry<AcceleratorConversion>> ACCELERATOR_RECIPES = new ArrayList<>();
    private static final List<Entry<FastToolModifiers>> TOOL_MODIFIERS = new ArrayList<>();
    private static final Map<Identifier, FastFuel> FUELS_BY_ID = new HashMap<>();
    private static final Map<Identifier, CrusherRecipe> CRUSHER_BY_ID = new HashMap<>();
    private static final Map<Identifier, AcceleratorConversion> ACCELERATOR_BY_ID = new HashMap<>();
    private static final Map<Identifier, FastToolModifiers> TOOL_MODIFIERS_BY_ID = new HashMap<>();
    private static int nextSequence;
    private static boolean initialized;

    private FastFurnaceApiImpl() {
    }

    public static void initialize(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        registerDefaultFuels();
        modEventBus.post(new RegisterFastFuelsEvent(FastFurnaceApiImpl::registerFuel));
        registerDefaultCrusherRecipes();
        modEventBus.post(new RegisterFastCrusherRecipesEvent(FastFurnaceApiImpl::registerCrusherRecipe));
        registerDefaultAcceleratorRecipes();
        modEventBus.post(new RegisterAcceleratorRecipesEvent(FastFurnaceApiImpl::registerAcceleratorRecipe));
        ExtremelyFastAlloyerRecipes.registerBuiltIn();
        modEventBus.post(new RegisterExtremelyFastAlloyerRecipesEvent(ExtremelyFastAlloyerRecipes::register));
        registerDefaultToolModifiers();
        modEventBus.post(new RegisterFastToolModifiersEvent(FastFurnaceApiImpl::registerToolModifiers));
        initialized = true;
        LOGGER.info("Fast Furnace API {} initialized with {} fuels, {} crusher recipes, {} Accelerator conversions, {} Alloyer recipes, and {} tool modifier definitions",
                FastFurnaceApi.API_VERSION, FUELS.size(), CRUSHER_RECIPES.size(), ACCELERATOR_RECIPES.size(),
                ExtremelyFastAlloyerRecipes.recipes().size(), TOOL_MODIFIERS.size());
    }

    public static void modifyToolStats(IEventBus modEventBus, ModifyDefaultComponentsEvent event) {
        modEventBus.post(new ModifyFastToolStatsEvent(event));
    }

    public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        findFuel(event.getItemStack())
                .filter(fuel -> fuel.baseBurnTime() > 0)
                .ifPresent(fuel -> event.setBurnTime(fuel.baseBurnTime()));
    }

    public static List<FastFuel> fuels() {
        return FUELS.stream().map(Entry::value).toList();
    }

    public static Optional<FastFuel> findFuel(Identifier id) {
        return Optional.ofNullable(FUELS_BY_ID.get(id));
    }

    public static Optional<FastFuel> findFuel(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return find(FUELS, fuel -> fuel.ingredient().test(stack), FastFuel::priority);
    }

    public static List<CrusherRecipe> crusherRecipes() {
        return CRUSHER_RECIPES.stream().map(Entry::value).toList();
    }

    public static Optional<CrusherRecipe> findCrusherRecipe(ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        return find(CRUSHER_RECIPES,
                recipe -> input.getCount() >= recipe.inputCount() && recipe.ingredient().test(input),
                CrusherRecipe::priority);
    }

    public static List<AcceleratorConversion> acceleratorRecipes() {
        return ACCELERATOR_RECIPES.stream().map(Entry::value).toList();
    }

    public static boolean isAcceleratorInput(ItemStack input) {
        return !input.isEmpty() && ACCELERATOR_RECIPES.stream()
                .map(Entry::value)
                .anyMatch(recipe -> recipe.ingredient().test(input));
    }

    public static Optional<AcceleratorConversion> findAcceleratorRecipe(ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        return find(ACCELERATOR_RECIPES,
                recipe -> input.getCount() >= recipe.inputCount() && recipe.ingredient().test(input),
                AcceleratorConversion::priority);
    }

    public static List<FastToolModifiers> toolModifiers() {
        return TOOL_MODIFIERS.stream().map(Entry::value).toList();
    }

    public static Optional<FastToolModifiers> findToolModifiers(Identifier id) {
        return Optional.ofNullable(TOOL_MODIFIERS_BY_ID.get(id));
    }

    public static Optional<FastToolModifiers> findToolModifiers(ItemStack tool) {
        if (tool.isEmpty()) {
            return Optional.empty();
        }
        return find(TOOL_MODIFIERS,
                modifiers -> modifiers.ingredient().test(tool), FastToolModifiers::priority);
    }

    private static void registerFuel(FastFuel fuel) {
        if (FUELS_BY_ID.putIfAbsent(fuel.id(), fuel) != null) {
            throw new IllegalArgumentException("Duplicate Fast Furnace fuel ID: " + fuel.id());
        }
        FUELS.add(new Entry<>(fuel, nextSequence++));
    }

    private static void registerCrusherRecipe(CrusherRecipe recipe) {
        if (CRUSHER_BY_ID.putIfAbsent(recipe.id(), recipe) != null) {
            throw new IllegalArgumentException("Duplicate Fast Crusher recipe ID: " + recipe.id());
        }
        CRUSHER_RECIPES.add(new Entry<>(recipe, nextSequence++));
    }

    private static void registerAcceleratorRecipe(AcceleratorConversion recipe) {
        if (ACCELERATOR_BY_ID.putIfAbsent(recipe.id(), recipe) != null) {
            throw new IllegalArgumentException("Duplicate Accelerator conversion ID: " + recipe.id());
        }
        ACCELERATOR_RECIPES.add(new Entry<>(recipe, nextSequence++));
    }

    private static void registerToolModifiers(FastToolModifiers modifiers) {
        if (TOOL_MODIFIERS_BY_ID.putIfAbsent(modifiers.id(), modifiers) != null) {
            throw new IllegalArgumentException("Duplicate Fast tool modifier ID: " + modifiers.id());
        }
        TOOL_MODIFIERS.add(new Entry<>(modifiers, nextSequence++));
    }

    private static <T> Optional<T> find(
            List<Entry<T>> entries, Predicate<T> predicate, ToIntFunction<T> priority) {
        Entry<T> best = null;
        int bestPriority = Integer.MIN_VALUE;
        for (Entry<T> entry : entries) {
            int candidatePriority = priority.applyAsInt(entry.value());
            if (predicate.test(entry.value())
                    && (best == null || candidatePriority > bestPriority
                    || candidatePriority == bestPriority && entry.sequence() > best.sequence())) {
                best = entry;
                bestPriority = candidatePriority;
            }
        }
        return Optional.ofNullable(best).map(Entry::value);
    }

    private static void registerDefaultFuels() {
        registerFuel(FastFuel.builder(
                        FastFurnaceApi.FAST_COAL_FUEL_ID,
                        Ingredient.of(FastFurnaceV2.FAST_COAL.get()),
                        FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME)
                .burnTimePercent(FastFurnaceTier::getFastCoalBurnTimePercent)
                .speedBonusPercent(FastFurnaceTier.FAST_COAL_SPEED_BONUS_PERCENT)
                .acceleratorBoost(
                        AcceleratorBlockEntity.FAST_COAL_BOOST_WORK,
                        AcceleratorBlockEntity.BOOSTED_WORK_PER_TICK)
                .build());
        registerFuel(FastFuel.builder(
                        FastFurnaceApi.FAST_COAL_BLOCK_FUEL_ID,
                        Ingredient.of(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get()),
                        FastFurnaceBlockEntity.VANILLA_COAL_BLOCK_BURN_TIME)
                .burnTimePercent(FastFurnaceTier::getFastCoalBurnTimePercent)
                .speedBonusPercent(FastFurnaceTier.FAST_COAL_SPEED_BONUS_PERCENT)
                .acceleratorBoost(
                        AcceleratorBlockEntity.FAST_COAL_BLOCK_BOOST_WORK,
                        AcceleratorBlockEntity.BOOSTED_WORK_PER_TICK)
                .build());
        registerFuel(FastFuel.builder(
                        FastFurnaceApi.EXTREME_COAL_FUEL_ID,
                        Ingredient.of(FastFurnaceV2.EXTREME_COAL.get()),
                        FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME)
                .burnTimePercent(tier -> 2 * tier.getFastCoalBurnTimePercent() - 100)
                .speedBonusPercent(FastFurnaceTier.EXTREME_COAL_SPEED_BONUS_PERCENT)
                .acceleratorBoost(
                        AcceleratorBlockEntity.EXTREME_COAL_BOOST_WORK,
                        AcceleratorBlockEntity.EXTREME_COAL_BOOSTED_WORK_PER_TICK)
                .build());
        registerFuel(FastFuel.builder(
                        FastFurnaceApi.EXTREME_COAL_BLOCK_FUEL_ID,
                        Ingredient.of(FastFurnaceV2.EXTREME_COAL_BLOCK_ITEM.get()),
                        FastFurnaceBlockEntity.VANILLA_COAL_BLOCK_BURN_TIME)
                .burnTimePercent(tier -> 2 * tier.getFastCoalBurnTimePercent() - 100)
                .speedBonusPercent(FastFurnaceTier.EXTREME_COAL_SPEED_BONUS_PERCENT)
                .acceleratorBoost(
                        AcceleratorBlockEntity.EXTREME_COAL_BLOCK_BOOST_WORK,
                        AcceleratorBlockEntity.EXTREME_COAL_BOOSTED_WORK_PER_TICK)
                .build());
    }

    private static void registerDefaultCrusherRecipes() {
        for (Identifier rawOreId : CrushedOres.rawOres()) {
            Item crushed = CrushedOres.forRawOre(rawOreId);
            if (crushed == null) {
                continue;
            }
            Identifier id = Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID,
                    "crusher/" + rawOreId.getNamespace() + "/" + rawOreId.getPath());
            registerCrusherRecipe(new CrusherRecipe(
                    id,
                    Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(rawOreId)),
                    new ItemStack(crushed, FastFurnaceBlockEntity.FAST_CRUSHER_OUTPUT_COUNT),
                    0));
        }
        registerOreCrusherRecipe("extreme_coal_ore",
                FastFurnaceV2.EXTREME_COAL_ORE_ITEM.get(), FastFurnaceV2.EXTREME_COAL.get());
        registerOreCrusherRecipe("deepslate_extreme_coal_ore",
                FastFurnaceV2.DEEPSLATE_EXTREME_COAL_ORE_ITEM.get(), FastFurnaceV2.EXTREME_COAL.get());
        registerFastMaterialOreCrusherRecipes("iron",
                FastFurnaceV2.FAST_IRON_ORE_ITEM.get(), FastFurnaceV2.DEEPSLATE_FAST_IRON_ORE_ITEM.get());
        registerFastMaterialOreCrusherRecipes("gold",
                FastFurnaceV2.FAST_GOLD_ORE_ITEM.get(), FastFurnaceV2.DEEPSLATE_FAST_GOLD_ORE_ITEM.get());
        registerFastMaterialOreCrusherRecipes("diamond",
                FastFurnaceV2.FAST_DIAMOND_ORE_ITEM.get(), FastFurnaceV2.DEEPSLATE_FAST_DIAMOND_ORE_ITEM.get());
    }

    private static void registerFastMaterialOreCrusherRecipes(String material, Item regularOre, Item deepslateOre) {
        Item crushed = CrushedOres.forRawOre(Identifier.fromNamespaceAndPath(
                FastFurnaceV2.MODID, "raw_fast_" + material));
        if (crushed == null) {
            throw new IllegalStateException("Missing built-in crushed Fast material: " + material);
        }
        registerOreCrusherRecipe("fast_" + material + "_ore", regularOre, crushed);
        registerOreCrusherRecipe("deepslate_fast_" + material + "_ore", deepslateOre, crushed);
    }

    private static void registerOreCrusherRecipe(String path, Item input, Item output) {
        registerCrusherRecipe(new CrusherRecipe(
                Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "crusher/ore/" + path),
                Ingredient.of(input),
                new ItemStack(output, 3),
                0));
    }

    private static void registerDefaultAcceleratorRecipes() {
        registerAcceleratorConversion("coal", Items.COAL, FastFurnaceV2.FAST_COAL.get(),
                AcceleratorBlockEntity.ITEM_RECIPE_TICKS);
        registerAcceleratorConversion("coal_block", Items.COAL_BLOCK, FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get(),
                AcceleratorBlockEntity.BLOCK_RECIPE_TICKS);
        registerAcceleratorConversion("iron", Items.IRON_INGOT, FastFurnaceV2.FAST_IRON_INGOT.get(),
                AcceleratorBlockEntity.ITEM_RECIPE_TICKS);
        registerAcceleratorConversion("iron_block", Items.IRON_BLOCK, FastFurnaceV2.FAST_IRON_BLOCK_ITEM.get(),
                AcceleratorBlockEntity.BLOCK_RECIPE_TICKS);
        registerAcceleratorConversion("gold", Items.GOLD_INGOT, FastFurnaceV2.FAST_GOLD_INGOT.get(),
                AcceleratorBlockEntity.ITEM_RECIPE_TICKS);
        registerAcceleratorConversion("gold_block", Items.GOLD_BLOCK, FastFurnaceV2.FAST_GOLD_BLOCK_ITEM.get(),
                AcceleratorBlockEntity.BLOCK_RECIPE_TICKS);
        registerAcceleratorConversion("diamond", Items.DIAMOND, FastFurnaceV2.FAST_DIAMOND.get(),
                AcceleratorBlockEntity.ITEM_RECIPE_TICKS);
        registerAcceleratorConversion("diamond_block", Items.DIAMOND_BLOCK, FastFurnaceV2.FAST_DIAMOND_BLOCK_ITEM.get(),
                AcceleratorBlockEntity.BLOCK_RECIPE_TICKS);
    }

    private static void registerAcceleratorConversion(String path, Item input, Item output, int workUnits) {
        registerAcceleratorRecipe(new AcceleratorConversion(
                Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "accelerator/" + path),
                Ingredient.of(input),
                new ItemStack(output),
                workUnits));
    }

    private static void registerDefaultToolModifiers() {
        registerToolModifiers(FastToolModifiers.builder(
                        FastFurnaceApi.FAST_DIAMOND_TOOL_MODIFIERS_ID,
                        Ingredient.of(
                                FastFurnaceV2.FAST_DIAMOND_PICKAXE.get(),
                                FastFurnaceV2.FAST_DIAMOND_SHOVEL.get(),
                                FastFurnaceV2.FAST_DIAMOND_AXE.get(),
                                FastFurnaceV2.FAST_DIAMOND_SWORD.get()))
                .durabilitySaveChance(FastTools.DURABILITY_SAVE_CHANCE)
                .build());
        registerToolModifiers(FastToolModifiers.builder(
                        FastFurnaceApi.EXTREMELY_FAST_HARVESTING_TOOL_MODIFIERS_ID,
                        Ingredient.of(
                                FastFurnaceV2.EXTREMELY_FAST_PICKAXE.get(),
                                FastFurnaceV2.EXTREMELY_FAST_SHOVEL.get(),
                                FastFurnaceV2.EXTREMELY_FAST_AXE.get()))
                .blockDropDoubleChance(FastTools.DROP_DOUBLE_CHANCE)
                .build());
        registerToolModifiers(FastToolModifiers.builder(
                        FastFurnaceApi.EXTREMELY_FAST_SWORD_MODIFIERS_ID,
                        Ingredient.of(FastFurnaceV2.EXTREMELY_FAST_SWORD.get()))
                .killDropDoubleChance(FastTools.DROP_DOUBLE_CHANCE)
                .damageDoubleChance(FastTools.DAMAGE_DOUBLE_CHANCE)
                .build());
    }

    private record Entry<T>(T value, int sequence) {
    }

}
