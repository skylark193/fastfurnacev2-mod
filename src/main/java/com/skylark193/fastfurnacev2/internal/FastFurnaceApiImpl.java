package com.skylark193.fastfurnacev2.internal;

import com.mojang.logging.LogUtils;
import com.skylark193.fastfurnacev2.AcceleratorBlockEntity;
import com.skylark193.fastfurnacev2.CrushedOres;
import com.skylark193.fastfurnacev2.FastFurnaceBlockEntity;
import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.api.AcceleratorConversion;
import com.skylark193.fastfurnacev2.api.CrusherRecipe;
import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import com.skylark193.fastfurnacev2.api.event.RegisterAcceleratorRecipesEvent;
import com.skylark193.fastfurnacev2.api.event.RegisterFastCrusherRecipesEvent;
import com.skylark193.fastfurnacev2.api.event.RegisterFastFuelsEvent;
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
    private static final Map<Identifier, FastFuel> FUELS_BY_ID = new HashMap<>();
    private static final Map<Identifier, CrusherRecipe> CRUSHER_BY_ID = new HashMap<>();
    private static final Map<Identifier, AcceleratorConversion> ACCELERATOR_BY_ID = new HashMap<>();
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
        initialized = true;
        LOGGER.info("Fast Furnace API {} initialized with {} fuels, {} crusher recipes, and {} Accelerator conversions",
                FastFurnaceApi.API_VERSION, FUELS.size(), CRUSHER_RECIPES.size(), ACCELERATOR_RECIPES.size());
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
    }

    private static void registerDefaultAcceleratorRecipes() {
        registerAcceleratorRecipe(new AcceleratorConversion(
                Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "accelerator/coal"),
                Ingredient.of(Items.COAL),
                new ItemStack(FastFurnaceV2.FAST_COAL.get()),
                AcceleratorBlockEntity.COAL_RECIPE_TICKS));
        registerAcceleratorRecipe(new AcceleratorConversion(
                Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "accelerator/coal_block"),
                Ingredient.of(Items.COAL_BLOCK),
                new ItemStack(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get()),
                AcceleratorBlockEntity.COAL_BLOCK_RECIPE_TICKS));
    }

    private record Entry<T>(T value, int sequence) {
    }

}
