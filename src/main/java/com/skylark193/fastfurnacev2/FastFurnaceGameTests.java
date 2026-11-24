package com.skylark193.fastfurnacev2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skylark193.fastfurnacev2.api.AcceleratorConversion;
import com.skylark193.fastfurnacev2.api.CrusherRecipe;
import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
import com.skylark193.fastfurnacev2.api.event.RegisterAcceleratorRecipesEvent;
import com.skylark193.fastfurnacev2.api.event.RegisterFastCrusherRecipesEvent;
import com.skylark193.fastfurnacev2.api.event.RegisterFastFuelsEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

final class FastFurnaceGameTests {
    private static final Identifier SPEED_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_furnace_speed");
    private static final Identifier FUEL_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_furnace_fuel");
    private static final Identifier FAST_COAL_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_coal");
    private static final Identifier FAST_COAL_BLOCK_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_coal_block");
    private static final Identifier BONUS_DROP_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_bonus_drops");
    private static final Identifier TIER_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "furnace_tiers");
    private static final Identifier CRUSHED_ORE_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "crushed_ores");
    private static final Identifier FAST_CRUSHER_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_crusher");
    private static final Identifier FUEL_PAUSE_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fuel_pause");
    private static final Identifier ACCELERATOR_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "accelerator");
    private static final Identifier ALLOYER_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "extremely_fast_alloyer");
    private static final Identifier EXTREME_COAL_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "extreme_coal");
    private static final Identifier FAST_ORE_GENERATION_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_ore_generation");
    private static final Identifier MINING_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "mining_tags");
    private static final Identifier FAST_TOOL_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_tools");
    private static final Identifier API_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "api_extensions");
    private static final Identifier API_FUEL_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "test/blaze_powder");
    private static final Identifier API_CRUSHER_RECIPE_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "test/cobblestone_to_gravel");
    private static final Identifier API_ACCELERATOR_RECIPE_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "test/charcoal_to_coal");
    private static final ResourceKey<Consumer<GameTestHelper>> SPEED_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, SPEED_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> FUEL_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, FUEL_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> FAST_COAL_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, FAST_COAL_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> FAST_COAL_BLOCK_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, FAST_COAL_BLOCK_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> BONUS_DROP_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, BONUS_DROP_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> TIER_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, TIER_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> CRUSHED_ORE_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, CRUSHED_ORE_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> FAST_CRUSHER_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, FAST_CRUSHER_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> FUEL_PAUSE_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, FUEL_PAUSE_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> ACCELERATOR_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, ACCELERATOR_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> ALLOYER_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, ALLOYER_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> EXTREME_COAL_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, EXTREME_COAL_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> FAST_ORE_GENERATION_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, FAST_ORE_GENERATION_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> MINING_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, MINING_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> FAST_TOOL_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, FAST_TOOL_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> API_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, API_TEST_ID);
    private static final Map<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> FUNCTIONS = Map.ofEntries(
            Map.entry(SPEED_FUNCTION, FastFurnaceGameTests::smeltsAt150Percent),
            Map.entry(FUEL_FUNCTION, FastFurnaceGameTests::rollsAndSynchronizesFuelTime),
            Map.entry(FAST_COAL_FUNCTION, FastFurnaceGameTests::fastCoalAcceleratesAndExtendsBurnTime),
            Map.entry(FAST_COAL_BLOCK_FUNCTION, FastFurnaceGameTests::fastCoalBlockActsAsFastCoalFuel),
            Map.entry(BONUS_DROP_FUNCTION, FastFurnaceGameTests::fortuneIncreasesBonusDropChance),
            Map.entry(TIER_FUNCTION, FastFurnaceGameTests::furnaceTiersScaleSpeedAndFuel),
            Map.entry(CRUSHED_ORE_FUNCTION, FastFurnaceGameTests::crushedOresMatchRawOreSmelting),
            Map.entry(FAST_CRUSHER_FUNCTION, FastFurnaceGameTests::fastCrusherCrushesAndDoublesOutput),
            Map.entry(FUEL_PAUSE_FUNCTION, FastFurnaceGameTests::pausesFuelWithoutProcessableWork),
            Map.entry(ACCELERATOR_FUNCTION, FastFurnaceGameTests::acceleratorConvertsCoalWithLavaAndBoosts),
            Map.entry(ALLOYER_FUNCTION, FastFurnaceGameTests::extremelyFastAlloyerUsesOnlyShapelessAlloyingRecipes),
            Map.entry(EXTREME_COAL_FUNCTION, FastFurnaceGameTests::extremeCoalDoublesFastCoalBonuses),
            Map.entry(FAST_ORE_GENERATION_FUNCTION, FastFurnaceGameTests::fastOresReplaceRegularOresOnce),
            Map.entry(MINING_FUNCTION, FastFurnaceGameTests::allToolRequiredBlocksArePickaxeMineable),
            Map.entry(FAST_TOOL_FUNCTION, FastFurnaceGameTests::fastToolsApplyVanillaBonuses),
            Map.entry(API_FUNCTION, FastFurnaceGameTests::apiSupportsCustomFuelsAndRecipes));

    private FastFurnaceGameTests() {
    }

    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "default"),
                new TestEnvironmentDefinition.AllOf(List.of()));
        TestData<Holder<TestEnvironmentDefinition<?>>> testData = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                200,
                0,
                true);
        event.registerTest(SPEED_TEST_ID, new DirectGameTestInstance(SPEED_FUNCTION, testData));
        event.registerTest(FUEL_TEST_ID, new DirectGameTestInstance(FUEL_FUNCTION, testData));
        event.registerTest(FAST_COAL_TEST_ID, new DirectGameTestInstance(FAST_COAL_FUNCTION, testData));
        event.registerTest(FAST_COAL_BLOCK_TEST_ID, new DirectGameTestInstance(FAST_COAL_BLOCK_FUNCTION, testData));
        event.registerTest(BONUS_DROP_TEST_ID, new DirectGameTestInstance(BONUS_DROP_FUNCTION, testData));
        event.registerTest(TIER_TEST_ID, new DirectGameTestInstance(TIER_FUNCTION, testData));
        event.registerTest(CRUSHED_ORE_TEST_ID, new DirectGameTestInstance(CRUSHED_ORE_FUNCTION, testData));
        event.registerTest(FAST_CRUSHER_TEST_ID, new DirectGameTestInstance(FAST_CRUSHER_FUNCTION, testData));
        event.registerTest(FUEL_PAUSE_TEST_ID, new DirectGameTestInstance(FUEL_PAUSE_FUNCTION, testData));
        event.registerTest(ACCELERATOR_TEST_ID, new DirectGameTestInstance(ACCELERATOR_FUNCTION, testData));
        event.registerTest(ALLOYER_TEST_ID, new DirectGameTestInstance(ALLOYER_FUNCTION, testData));
        event.registerTest(EXTREME_COAL_TEST_ID, new DirectGameTestInstance(EXTREME_COAL_FUNCTION, testData));
        event.registerTest(FAST_ORE_GENERATION_TEST_ID,
                new DirectGameTestInstance(FAST_ORE_GENERATION_FUNCTION, testData));
        event.registerTest(MINING_TEST_ID, new DirectGameTestInstance(MINING_FUNCTION, testData));
        event.registerTest(FAST_TOOL_TEST_ID, new DirectGameTestInstance(FAST_TOOL_FUNCTION, testData));
        if (Boolean.getBoolean("fastfurnacev2.apiTestContent")) {
            event.registerTest(API_TEST_ID, new DirectGameTestInstance(API_FUNCTION, testData));
        }
    }

    static void registerApiFuels(RegisterFastFuelsEvent event) {
        event.register(FastFuel.builder(API_FUEL_ID, Ingredient.of(Items.BLAZE_POWDER), 800)
                .burnTimePercent(200)
                .speedBonusPercent(25)
                .acceleratorBoost(800, 4)
                .build());
    }

    static void registerApiCrusherRecipes(RegisterFastCrusherRecipesEvent event) {
        event.register(new CrusherRecipe(
                API_CRUSHER_RECIPE_ID,
                Ingredient.of(Items.COBBLESTONE),
                2,
                new ItemStack(Items.GRAVEL, 3),
                200,
                0));
    }

    static void registerApiAcceleratorRecipes(RegisterAcceleratorRecipesEvent event) {
        event.register(new AcceleratorConversion(
                API_ACCELERATOR_RECIPE_ID,
                Ingredient.of(Items.CHARCOAL),
                2,
                new ItemStack(Items.COAL, 2),
                200,
                0));
    }

    private static void smeltsAt150Percent(GameTestHelper helper) {
        BlockPos relativePos = BlockPos.ZERO;
        BlockPos pos = helper.absolutePos(relativePos);
        ServerLevel level = helper.getLevel();
        helper.setBlock(relativePos, FastFurnaceV2.FAST_FURNACE.get());
        FastFurnaceBlockEntity furnace = helper.getBlockEntity(relativePos, FastFurnaceBlockEntity.class);
        furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.COBBLESTONE));
        furnace.setItem(FastFurnaceMenu.FUEL_SLOT, new ItemStack(Items.COAL));
        FastFurnaceMenu menu = createMenu(helper, furnace);
        if (menu.isFastCrusher()) {
            throw helper.assertionException("Fast Furnace incorrectly identified itself as a Fast Crusher");
        }

        for (int tick = 1; tick <= 133; tick++) {
            FastFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), furnace);
            if (tick < 133 && !furnace.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                throw helper.assertionException("Fast Furnace smelted before tick 133");
            }
        }

        ItemStack result = furnace.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!result.is(Items.STONE) || result.getCount() != 1) {
            throw helper.assertionException("Fast Furnace did not smelt one stone on tick 133");
        }
        helper.succeed();
    }

    private static void fastCrusherCrushesAndDoublesOutput(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Item crushedIron = CrushedOres.forRawOre(Identifier.withDefaultNamespace("raw_iron"));
        if (crushedIron == null) {
            throw helper.assertionException("Raw iron did not have a crushed variant");
        }
        assertBuiltInOreCrusherRecipes(helper);

        FastFurnaceBlockEntity crusher = placeCrusher(helper, BlockPos.ZERO);
        FastFurnaceMenu menu = createMenu(helper, crusher);
        if (menu.getTier() != FastFurnaceTier.FAST
                || !menu.isFastCrusher()
                || !crusher.getDisplayName().getString().equals("Fast Crusher")) {
            throw helper.assertionException("Fast Crusher did not synchronize its identity and Fast-tier behavior");
        }

        level.getRandom().setSeed(findFuelRollSeed(true));
        tickFurnace(helper, crusher, BlockPos.ZERO);
        if (menu.getCurrentBurnTime() != FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME
                || menu.getTotalFuelTime() != FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME) {
            throw helper.assertionException("Fast Crusher incorrectly doubled its fuel duration");
        }
        for (int tick = 2; tick <= 132; tick++) {
            tickFurnace(helper, crusher, BlockPos.ZERO);
            if (!crusher.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                throw helper.assertionException("Fast Crusher crushed before tick 133");
            }
        }
        level.getRandom().setSeed(findOutputRollSeed(false));
        tickFurnace(helper, crusher, BlockPos.ZERO);
        ItemStack result = crusher.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!result.is(crushedIron) || result.getCount() != 2
                || !crusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).isEmpty()) {
            throw helper.assertionException("Fast Crusher did not produce two crushed iron on a failed doubling roll");
        }

        BlockPos smeltingPos = new BlockPos(2, 0, 0);
        FastFurnaceBlockEntity smeltingCrusher = placeCrusher(helper, smeltingPos);
        smeltingCrusher.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.COBBLESTONE));
        FastFurnaceMenu smeltingMenu = createMenu(helper, smeltingCrusher);
        tickFurnace(helper, smeltingCrusher, smeltingPos);
        if (smeltingMenu.getCurrentBurnTime() != 0
                || smeltingCrusher.getItem(FastFurnaceMenu.FUEL_SLOT).getCount() != 1
                || !smeltingCrusher.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
            throw helper.assertionException("Fast Crusher processed an ordinary furnace smelting recipe");
        }

        BlockPos doubledPos = new BlockPos(4, 0, 0);
        FastFurnaceBlockEntity doubledCrusher = placeCrusher(helper, doubledPos);
        for (int tick = 1; tick <= 132; tick++) {
            tickFurnace(helper, doubledCrusher, doubledPos);
        }
        level.getRandom().setSeed(findOutputRollSeed(true));
        tickFurnace(helper, doubledCrusher, doubledPos);
        ItemStack doubledResult = doubledCrusher.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!doubledResult.is(crushedIron) || doubledResult.getCount() != 4) {
            throw helper.assertionException("Fast Crusher did not produce four crushed iron on a successful roll");
        }

        BlockPos blockedPos = new BlockPos(6, 0, 0);
        FastFurnaceBlockEntity blockedCrusher = placeCrusher(helper, blockedPos);
        blockedCrusher.setItem(FastFurnaceMenu.RESULT_SLOT, new ItemStack(crushedIron, 61));
        FastFurnaceMenu blockedMenu = createMenu(helper, blockedCrusher);
        for (int tick = 1; tick <= 132; tick++) {
            tickFurnace(helper, blockedCrusher, blockedPos);
        }
        level.getRandom().setSeed(findOutputRollSeed(true));
        tickFurnace(helper, blockedCrusher, blockedPos);
        int pausedBurnTime = blockedMenu.getCurrentBurnTime();
        for (int tick = 0; tick < 20; tick++) {
            tickFurnace(helper, blockedCrusher, blockedPos);
        }
        ItemStack blockedOutput = blockedCrusher.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!blockedOutput.is(crushedIron) || blockedOutput.getCount() != 61
                || blockedCrusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).getCount() != 1
                || blockedMenu.getCurrentBurnTime() != pausedBurnTime) {
            throw helper.assertionException("Fast Crusher fell back to normal output or wasted fuel when double output was blocked");
        }

        level.getRandom().setSeed(findOutputRollSeed(false));
        blockedCrusher.setItem(FastFurnaceMenu.RESULT_SLOT, new ItemStack(crushedIron, 60));
        tickFurnace(helper, blockedCrusher, blockedPos);
        ItemStack resumedOutput = blockedCrusher.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!resumedOutput.is(crushedIron) || resumedOutput.getCount() != 64
                || !blockedCrusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).isEmpty()
                || blockedMenu.getCurrentBurnTime() != pausedBurnTime - 1) {
            throw helper.assertionException("Fast Crusher did not resume the same pending doubled output");
        }

        BlockPos abortedPos = new BlockPos(10, 0, 0);
        FastFurnaceBlockEntity abortedCrusher = createPendingDoubleOutput(helper, abortedPos, crushedIron);
        ResourceHandler<ItemResource> abortedItems = VanillaContainerWrapper.of(abortedCrusher);
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = abortedItems.extract(
                    FastFurnaceMenu.INGREDIENT_SLOT, ItemResource.of(Items.RAW_IRON), 1, transaction);
            if (extracted != 1) {
                throw helper.assertionException("Could not extract crusher input for the aborted-transaction test");
            }
        }
        if (!abortedCrusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).is(Items.RAW_IRON)
                || abortedCrusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).getCount() != 1) {
            throw helper.assertionException("Aborting a transaction did not restore the crusher input");
        }
        abortedCrusher.setItem(FastFurnaceMenu.RESULT_SLOT, new ItemStack(crushedIron, 60));
        level.getRandom().setSeed(findOutputRollSeed(false));
        tickFurnace(helper, abortedCrusher, abortedPos);
        if (abortedCrusher.getItem(FastFurnaceMenu.RESULT_SLOT).getCount() != 64) {
            throw helper.assertionException("An aborted input extraction discarded the pending four-item crusher result");
        }

        BlockPos committedPos = new BlockPos(12, 0, 0);
        FastFurnaceBlockEntity committedCrusher = createPendingDoubleOutput(helper, committedPos, crushedIron);
        ResourceHandler<ItemResource> committedItems = VanillaContainerWrapper.of(committedCrusher);
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = committedItems.extract(
                    FastFurnaceMenu.INGREDIENT_SLOT, ItemResource.of(Items.RAW_IRON), 1, transaction);
            if (extracted != 1) {
                throw helper.assertionException("Could not extract crusher input for the committed-transaction test");
            }
            transaction.commit();
        }
        if (!committedCrusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).isEmpty()) {
            throw helper.assertionException("Committing a transaction did not remove the crusher input");
        }
        committedCrusher.setItem(FastFurnaceMenu.RESULT_SLOT, ItemStack.EMPTY);
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = committedItems.insert(
                    FastFurnaceMenu.INGREDIENT_SLOT, ItemResource.of(Items.RAW_IRON), 1, transaction);
            if (inserted != 1) {
                throw helper.assertionException("Could not restore crusher input transactionally");
            }
            transaction.commit();
        }
        for (int tick = 1; tick <= 133; tick++) {
            if (tick == 133) {
                level.getRandom().setSeed(findOutputRollSeed(false));
            }
            tickFurnace(helper, committedCrusher, committedPos);
        }
        ItemStack committedResult = committedCrusher.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!committedResult.is(crushedIron) || committedResult.getCount() != 2) {
            throw helper.assertionException("A committed empty/fill transition retained stale doubled crusher output");
        }

        BlockPos fastCoalPos = new BlockPos(8, 0, 0);
        FastFurnaceBlockEntity fastCoalCrusher = placeCrusher(helper, fastCoalPos);
        fastCoalCrusher.setItem(FastFurnaceMenu.FUEL_SLOT, new ItemStack(FastFurnaceV2.FAST_COAL.get()));
        for (int tick = 1; tick <= 99; tick++) {
            tickFurnace(helper, fastCoalCrusher, fastCoalPos);
            if (!fastCoalCrusher.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                throw helper.assertionException("Fast Crusher used Fast Coal before tick 100");
            }
        }
        level.getRandom().setSeed(findOutputRollSeed(false));
        tickFurnace(helper, fastCoalCrusher, fastCoalPos);
        ItemStack fastCoalResult = fastCoalCrusher.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!fastCoalResult.is(crushedIron) || fastCoalResult.getCount() != 2) {
            throw helper.assertionException("Fast Crusher did not retain 100-tick Fast Coal processing");
        }

        crusherTiersScaleSpeedAndOutputChance(helper, crushedIron);
        helper.succeed();
    }

    private static FastFurnaceBlockEntity createPendingDoubleOutput(
            GameTestHelper helper, BlockPos relativePos, Item crushedIron) {
        FastFurnaceBlockEntity crusher = placeCrusher(helper, relativePos);
        crusher.setItem(FastFurnaceMenu.RESULT_SLOT, new ItemStack(crushedIron, 61));
        for (int tick = 1; tick <= 132; tick++) {
            tickFurnace(helper, crusher, relativePos);
        }
        helper.getLevel().getRandom().setSeed(findOutputRollSeed(true));
        tickFurnace(helper, crusher, relativePos);
        if (crusher.getItem(FastFurnaceMenu.RESULT_SLOT).getCount() != 61
                || crusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).getCount() != 1) {
            throw helper.assertionException("Could not create a blocked pending crusher double");
        }
        return crusher;
    }

    private static void crusherTiersScaleSpeedAndOutputChance(GameTestHelper helper, Item crushedIron) {
        ServerLevel level = helper.getLevel();
        int[] normalCookTimes = {133, 100, 80, 66};
        int[] fastCoalCookTimes = {100, 80, 66, 57};
        int[] extremeCoalCookTimes = {80, 66, 57, 50};
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            for (int fuelMode = 0; fuelMode < 3; fuelMode++) {
                int expectedCookTime = switch (fuelMode) {
                    case 2 -> extremeCoalCookTimes[tier.ordinal()];
                    case 1 -> fastCoalCookTimes[tier.ordinal()];
                    default -> normalCookTimes[tier.ordinal()];
                };
                int expectedBurnTime = switch (fuelMode) {
                    case 2 -> tier.scaleExtremeCoalBurnTime(FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME);
                    case 1 -> tier.scaleFastCoalBurnTime(FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME);
                    default -> FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME;
                };
                ItemStack fuel = switch (fuelMode) {
                    case 2 -> new ItemStack(FastFurnaceV2.EXTREME_COAL.get());
                    case 1 -> new ItemStack(FastFurnaceV2.FAST_COAL.get());
                    default -> new ItemStack(Items.COAL);
                };
                FastFurnaceBlock block = FastFurnaceV2.getCrusherBlock(tier).get();
                BlockPos relativePos = new BlockPos(tier.ordinal() * 4, 12 + fuelMode * 4, 0);
                FastFurnaceBlockEntity crusher = placeFurnace(helper, relativePos, fuel, block);
                FastFurnaceMenu menu = createMenu(helper, crusher);

                level.getRandom().setSeed(findChanceRollSeed(tier.getDoubleBurnChance(), true));
                tickFurnace(helper, crusher, relativePos);
                if (!block.isFastCrusher() || menu.getTier() != tier || !menu.isFastCrusher()) {
                    throw helper.assertionException(tier + " crusher did not retain its tier and crusher identity");
                }
                assertBurnState(helper, menu, expectedBurnTime, expectedBurnTime, fuelMode != 0, fuelMode == 2,
                        tier + " crusher doubled its fuel or applied the wrong special-coal duration");

                for (int tick = 2; tick <= expectedCookTime; tick++) {
                    if (tick == expectedCookTime) {
                        level.getRandom().setSeed(findTierOutputRollSeed(tier));
                    }
                    tickFurnace(helper, crusher, relativePos);
                    if (tick < expectedCookTime && !crusher.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                        throw helper.assertionException(tier + " crusher finished before tick " + expectedCookTime);
                    }
                }
                ItemStack result = crusher.getItem(FastFurnaceMenu.RESULT_SLOT);
                if (!result.is(crushedIron) || result.getCount() != 4
                        || !crusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).isEmpty()) {
                    throw helper.assertionException(tier + " crusher did not use its tier's output-doubling chance");
                }

                if (fuelMode == 0) {
                    FastFurnaceTier failedTier = tier == FastFurnaceTier.FAST
                            ? FastFurnaceTier.DIAMOND
                            : FastFurnaceTier.values()[tier.ordinal() - 1];
                    long failedSeed = tier == FastFurnaceTier.FAST
                            ? findChanceRollSeed(FastFurnaceTier.DIAMOND.getDoubleBurnChance(), false)
                            : findTierOutputRollSeed(tier);
                    BlockPos failedPos = relativePos.above(2);
                    FastFurnaceBlockEntity failedCrusher = placeFurnace(
                            helper, failedPos, new ItemStack(Items.COAL),
                            FastFurnaceV2.getCrusherBlock(failedTier).get());
                    int failedCookTime = normalCookTimes[failedTier.ordinal()];
                    for (int tick = 1; tick <= failedCookTime; tick++) {
                        if (tick == failedCookTime) {
                            level.getRandom().setSeed(failedSeed);
                        }
                        tickFurnace(helper, failedCrusher, failedPos);
                    }
                    ItemStack failedResult = failedCrusher.getItem(FastFurnaceMenu.RESULT_SLOT);
                    if (!failedResult.is(crushedIron) || failedResult.getCount() != 2) {
                        throw helper.assertionException(failedTier
                                + " crusher doubled output for a roll outside its configured chance");
                    }
                }
            }
        }
    }

    private static void rollsAndSynchronizesFuelTime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FastFurnaceBlockEntity doubledFurnace = placeFurnace(
                helper, BlockPos.ZERO, new ItemStack(Items.LAVA_BUCKET));
        FastFurnaceMenu doubledMenu = createMenu(helper, doubledFurnace);
        level.getRandom().setSeed(findFuelRollSeed(true));
        tickFurnace(helper, doubledFurnace, BlockPos.ZERO);

        if (doubledMenu.getCurrentBurnTime() != 40000 || doubledMenu.getTotalFuelTime() != 40000) {
            throw helper.assertionException("Doubled lava burn time was not synchronized as 40000 ticks");
        }
        if (!doubledFurnace.getItem(FastFurnaceMenu.FUEL_SLOT).is(Items.BUCKET)) {
            throw helper.assertionException("Lava bucket did not leave its vanilla bucket remainder");
        }

        tickFurnace(helper, doubledFurnace, BlockPos.ZERO);
        if (doubledMenu.getCurrentBurnTime() != 39999 || doubledMenu.getTotalFuelTime() != 40000) {
            throw helper.assertionException("Remaining burn time did not decrease by exactly one tick");
        }

        FastFurnaceBlockEntity normalFurnace = placeFurnace(
                helper, new BlockPos(2, 0, 0), new ItemStack(Items.COAL));
        FastFurnaceMenu normalMenu = createMenu(helper, normalFurnace);
        level.getRandom().setSeed(findFuelRollSeed(false));
        tickFurnace(helper, normalFurnace, new BlockPos(2, 0, 0));
        if (normalMenu.getCurrentBurnTime() != 1600 || normalMenu.getTotalFuelTime() != 1600) {
            throw helper.assertionException("Non-doubled coal did not retain its vanilla 1600-tick burn time");
        }
        helper.succeed();
    }

    private static void pausesFuelWithoutProcessableWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FastFurnaceBlock[] blocks = {
                FastFurnaceV2.FAST_FURNACE.get(),
                FastFurnaceV2.IRON_FAST_FURNACE.get(),
                FastFurnaceV2.GOLD_FAST_FURNACE.get(),
                FastFurnaceV2.DIAMOND_FAST_FURNACE.get(),
                FastFurnaceV2.FAST_CRUSHER.get(),
                FastFurnaceV2.IRON_FAST_CRUSHER.get(),
                FastFurnaceV2.GOLD_FAST_CRUSHER.get(),
                FastFurnaceV2.DIAMOND_FAST_CRUSHER.get()
        };
        for (int fuelMode = 0; fuelMode < 3; fuelMode++) {
            for (int index = 0; index < blocks.length; index++) {
                FastFurnaceBlock block = blocks[index];
                String description = BuiltInRegistries.BLOCK.getKey(block) + switch (fuelMode) {
                    case 2 -> " with Extreme Coal";
                    case 1 -> " with Fast Coal";
                    default -> " with coal";
                };
                BlockPos relativePos = new BlockPos(index * 2, 4 + fuelMode * 4, 0);
                Item fuelItem = switch (fuelMode) {
                    case 2 -> FastFurnaceV2.EXTREME_COAL.get();
                    case 1 -> FastFurnaceV2.FAST_COAL.get();
                    default -> Items.COAL;
                };
                FastFurnaceBlockEntity furnace = placeFurnace(
                        helper, relativePos, new ItemStack(fuelItem), block);
                FastFurnaceMenu menu = createMenu(helper, furnace);
                int expectedBurnTime = switch (fuelMode) {
                    case 2 -> block.getTier().scaleExtremeCoalBurnTime(
                            FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME);
                    case 1 -> block.getTier().scaleFastCoalBurnTime(
                            FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME);
                    default -> FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME;
                };
                level.getRandom().setSeed(findChanceRollSeed(block.getTier().getDoubleBurnChance(), false));

                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, ItemStack.EMPTY);
                tickFurnace(helper, furnace, relativePos);
                if (menu.getCurrentBurnTime() != 0
                        || furnace.getItem(FastFurnaceMenu.FUEL_SLOT).getCount() != 1) {
                    throw helper.assertionException(description + " consumed fuel with empty input");
                }

                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.DIRT));
                tickFurnace(helper, furnace, relativePos);
                if (menu.getCurrentBurnTime() != 0
                        || furnace.getItem(FastFurnaceMenu.FUEL_SLOT).getCount() != 1) {
                    throw helper.assertionException(description + " consumed fuel with unsmeltable input");
                }

                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, processableInput(block));
                tickFurnace(helper, furnace, relativePos);
                assertBurnState(helper, menu, expectedBurnTime, expectedBurnTime, fuelMode != 0, fuelMode == 2,
                        description + " did not start with the correct fuel state");
                tickFurnace(helper, furnace, relativePos);
                assertBurnState(helper, menu, expectedBurnTime - 1, expectedBurnTime, fuelMode != 0, fuelMode == 2,
                        description + " did not count down during valid work");
                if (!furnace.getItem(FastFurnaceMenu.FUEL_SLOT).isEmpty()) {
                    throw helper.assertionException(description + " did not consume exactly one fuel item");
                }

                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, ItemStack.EMPTY);
                for (int tick = 0; tick < 20; tick++) {
                    tickFurnace(helper, furnace, relativePos);
                }
                assertBurnState(helper, menu, expectedBurnTime - 1, expectedBurnTime, fuelMode != 0, fuelMode == 2,
                        description + " wasted fuel with empty input");

                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.DIRT));
                for (int tick = 0; tick < 5; tick++) {
                    tickFurnace(helper, furnace, relativePos);
                }
                assertBurnState(helper, menu, expectedBurnTime - 1, expectedBurnTime, fuelMode != 0, fuelMode == 2,
                        description + " wasted fuel with unsmeltable input");

                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, processableInput(block));
                furnace.setItem(FastFurnaceMenu.RESULT_SLOT, new ItemStack(Items.DIRT));
                for (int tick = 0; tick < 5; tick++) {
                    tickFurnace(helper, furnace, relativePos);
                }
                assertBurnState(helper, menu, expectedBurnTime - 1, expectedBurnTime, fuelMode != 0, fuelMode == 2,
                        description + " wasted fuel with blocked output");

                furnace.setItem(FastFurnaceMenu.RESULT_SLOT, ItemStack.EMPTY);
                tickFurnace(helper, furnace, relativePos);
                assertBurnState(helper, menu, expectedBurnTime - 2, expectedBurnTime, fuelMode != 0, fuelMode == 2,
                        description + " did not resume its paused fuel countdown");

                while (menu.getCurrentBurnTime() > 20) {
                    furnace.setItem(
                            FastFurnaceMenu.INGREDIENT_SLOT,
                            alternateProcessableInput(block, furnace.getItem(FastFurnaceMenu.INGREDIENT_SLOT)));
                    tickFurnace(helper, furnace, relativePos);
                }
                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, processableInput(block));
                while (menu.getCurrentBurnTime() > 0) {
                    tickFurnace(helper, furnace, relativePos);
                }
                float savedProgress = menu.getBurnProgress();
                if (savedProgress <= 0.0F || menu.isLit()
                        || !furnace.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                    throw helper.assertionException(description + " lost recipe progress as its fuel expired");
                }
                for (int tick = 0; tick < 20; tick++) {
                    tickFurnace(helper, furnace, relativePos);
                }
                if (menu.getBurnProgress() != savedProgress) {
                    throw helper.assertionException(description + " changed saved recipe progress without fuel");
                }

                furnace.setItem(FastFurnaceMenu.FUEL_SLOT, new ItemStack(fuelItem));
                level.getRandom().setSeed(findChanceRollSeed(block.getTier().getDoubleBurnChance(), false));
                tickFurnace(helper, furnace, relativePos);
                assertBurnState(helper, menu, expectedBurnTime, expectedBurnTime, fuelMode != 0, fuelMode == 2,
                        description + " did not restart with replacement fuel");
                if (menu.getBurnProgress() <= savedProgress) {
                    throw helper.assertionException(description + " restarted instead of resuming saved recipe progress");
                }
            }
        }
        helper.succeed();
    }

    private static void fastCoalAcceleratesAndExtendsBurnTime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack fastCoal = new ItemStack(FastFurnaceV2.FAST_COAL.get());
        int vanillaBurnTime = fastCoal.getBurnTime(RecipeType.SMELTING, level.fuelValues());
        if (vanillaBurnTime != FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME) {
            throw helper.assertionException("Fast Coal did not retain coal's vanilla 1600-tick burn time");
        }

        int fastCoalBurnTime = FastFurnaceTier.FAST.scaleFastCoalBurnTime(vanillaBurnTime);
        FastFurnaceBlockEntity furnace = placeFurnace(helper, BlockPos.ZERO, fastCoal.copy());
        FastFurnaceMenu menu = createMenu(helper, furnace);
        level.getRandom().setSeed(findFuelRollSeed(false));
        tickFurnace(helper, furnace, BlockPos.ZERO);
        if (menu.getCurrentBurnTime() != fastCoalBurnTime
                || menu.getTotalFuelTime() != fastCoalBurnTime
                || !menu.isFastCoalActive()) {
            throw helper.assertionException("Fast Coal did not synchronize its 2400-tick Fast Furnace burn time");
        }

        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        for (int tick = 2; tick <= 100; tick++) {
            FastFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), furnace);
            if (tick < 100 && !furnace.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                throw helper.assertionException("Fast Coal smelted before tick 100");
            }
        }
        ItemStack result = furnace.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!result.is(Items.STONE) || result.getCount() != 1) {
            throw helper.assertionException("Fast Coal did not smelt one stone on tick 100");
        }

        FastFurnaceBlockEntity doubledFurnace = placeFurnace(
                helper, new BlockPos(2, 0, 0), fastCoal.copy());
        FastFurnaceMenu doubledMenu = createMenu(helper, doubledFurnace);
        level.getRandom().setSeed(findFuelRollSeed(true));
        tickFurnace(helper, doubledFurnace, new BlockPos(2, 0, 0));
        if (doubledMenu.getCurrentBurnTime() != fastCoalBurnTime * 2
                || doubledMenu.getTotalFuelTime() != fastCoalBurnTime * 2
                || !doubledMenu.isFastCoalActive()) {
            throw helper.assertionException("Doubled Fast Coal did not synchronize its 4800-tick burn time");
        }
        helper.succeed();
    }

    private static void fastCoalBlockActsAsFastCoalFuel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack fastCoalBlock = new ItemStack(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get());
        int vanillaBurnTime = fastCoalBlock.getBurnTime(RecipeType.SMELTING, level.fuelValues());
        if (vanillaBurnTime != FastFurnaceBlockEntity.VANILLA_COAL_BLOCK_BURN_TIME) {
            throw helper.assertionException("Fast Coal Block did not retain a coal block's vanilla 16000-tick burn time");
        }

        int[] expectedCookTimes = {100, 80, 66, 57};
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            int tierIndex = tier.ordinal();
            int expectedBurnTime = tier.scaleFastCoalBurnTime(vanillaBurnTime);
            BlockPos relativePos = new BlockPos(tierIndex * 4, 4, 0);
            FastFurnaceBlockEntity furnace = placeFurnace(
                    helper, relativePos, new ItemStack(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get()), tier);
            FastFurnaceMenu menu = createMenu(helper, furnace);
            level.getRandom().setSeed(findChanceRollSeed(tier.getDoubleBurnChance(), false));
            tickFurnace(helper, furnace, relativePos);

            if (!menu.isFastCoalActive()
                    || menu.getCurrentBurnTime() != expectedBurnTime
                    || menu.getTotalFuelTime() != expectedBurnTime) {
                throw helper.assertionException("Incorrect Fast Coal Block fuel synchronization for " + tier);
            }

            int expectedCookTime = expectedCookTimes[tierIndex];
            BlockPos pos = helper.absolutePos(relativePos);
            for (int tick = 2; tick <= expectedCookTime; tick++) {
                FastFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), furnace);
                if (tick < expectedCookTime && !furnace.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                    throw helper.assertionException(tier + " smelted before Fast Coal Block tick " + expectedCookTime);
                }
            }
            ItemStack result = furnace.getItem(FastFurnaceMenu.RESULT_SLOT);
            if (!result.is(Items.STONE) || result.getCount() != 1) {
                throw helper.assertionException(tier + " did not smelt with Fast Coal Block on tick " + expectedCookTime);
            }

            float expectedSmelts = expectedBurnTime / (float) expectedCookTime;
            if (Math.abs(tier.getSmeltCount(expectedBurnTime, true) - expectedSmelts) > 0.0001F) {
                throw helper.assertionException("Incorrect Fast Coal Block JEI smelt count for " + tier);
            }

            BlockPos doubledPos = relativePos.above(4);
            FastFurnaceBlockEntity doubledFurnace = placeFurnace(
                    helper, doubledPos, new ItemStack(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get()), tier);
            FastFurnaceMenu doubledMenu = createMenu(helper, doubledFurnace);
            level.getRandom().setSeed(findChanceRollSeed(tier.getDoubleBurnChance(), true));
            tickFurnace(helper, doubledFurnace, doubledPos);
            if (!doubledMenu.isFastCoalActive()
                    || doubledMenu.getCurrentBurnTime() != expectedBurnTime * 2
                    || doubledMenu.getTotalFuelTime() != expectedBurnTime * 2) {
                throw helper.assertionException("Incorrect doubled Fast Coal Block burn time for " + tier);
            }
        }
        helper.succeed();
    }

    private static void furnaceTiersScaleSpeedAndFuel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int[] normalCookTimes = {133, 100, 80, 66};
        int[] fastCoalCookTimes = {100, 80, 66, 57};
        int[] normalSpeeds = {150, 200, 250, 300};
        int[] fastCoalSpeeds = {200, 250, 300, 350};
        int[] doubleBurnChances = {10, 15, 20, 25};
        int[] fastCoalBurnTimes = {2400, 2800, 3200, 3600};

        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            int tierIndex = tier.ordinal();
            if (tier.getSpeedPercent() != normalSpeeds[tierIndex]
                    || tier.getSpeedPercent(true) != fastCoalSpeeds[tierIndex]
                    || tier.getDoubleBurnChancePercent() != doubleBurnChances[tierIndex]
                    || tier.scaleFastCoalBurnTime(FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME)
                            != fastCoalBurnTimes[tierIndex]) {
                throw helper.assertionException("Incorrect configured stats for " + tier);
            }

            for (boolean fastCoalActive : new boolean[] {false, true}) {
                int expectedCookTime = fastCoalActive
                        ? fastCoalCookTimes[tierIndex]
                        : normalCookTimes[tierIndex];
                int expectedBurnTime = fastCoalActive
                        ? fastCoalBurnTimes[tierIndex]
                        : FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME;
                ItemStack fuel = fastCoalActive
                        ? new ItemStack(FastFurnaceV2.FAST_COAL.get())
                        : new ItemStack(Items.COAL);
                BlockPos relativePos = new BlockPos(tierIndex * 4 + (fastCoalActive ? 2 : 0), 2, 0);
                FastFurnaceBlockEntity furnace = placeFurnace(helper, relativePos, fuel, tier);
                FastFurnaceMenu menu = createMenu(helper, furnace);
                level.getRandom().setSeed(findChanceRollSeed(tier.getDoubleBurnChance(), false));
                tickFurnace(helper, furnace, relativePos);

                if (menu.getTier() != tier
                        || menu.isFastCoalActive() != fastCoalActive
                        || menu.getCurrentBurnTime() != expectedBurnTime
                        || menu.getTotalFuelTime() != expectedBurnTime) {
                    throw helper.assertionException("Incorrect fuel synchronization for " + tier);
                }

                BlockPos pos = helper.absolutePos(relativePos);
                for (int tick = 2; tick <= expectedCookTime; tick++) {
                    FastFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), furnace);
                    if (tick < expectedCookTime && !furnace.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                        throw helper.assertionException(tier + " smelted before tick " + expectedCookTime);
                    }
                }
                ItemStack result = furnace.getItem(FastFurnaceMenu.RESULT_SLOT);
                if (!result.is(Items.STONE) || result.getCount() != 1) {
                    throw helper.assertionException(tier + " did not smelt on tick " + expectedCookTime);
                }

                float expectedSmelts = expectedBurnTime / (float) expectedCookTime;
                if (Math.abs(tier.getSmeltCount(expectedBurnTime, fastCoalActive) - expectedSmelts) > 0.0001F) {
                    throw helper.assertionException("Incorrect JEI smelt count for " + tier);
                }

                ItemStack doubledFuel = fastCoalActive
                        ? new ItemStack(FastFurnaceV2.FAST_COAL.get())
                        : new ItemStack(Items.COAL);
                BlockPos doubledPos = relativePos.above(4);
                FastFurnaceBlockEntity doubledFurnace = placeFurnace(helper, doubledPos, doubledFuel, tier);
                FastFurnaceMenu doubledMenu = createMenu(helper, doubledFurnace);
                level.getRandom().setSeed(findChanceRollSeed(tier.getDoubleBurnChance(), true));
                tickFurnace(helper, doubledFurnace, doubledPos);
                if (doubledMenu.getCurrentBurnTime() != expectedBurnTime * 2
                        || doubledMenu.getTotalFuelTime() != expectedBurnTime * 2
                        || doubledMenu.isFastCoalActive() != fastCoalActive) {
                    throw helper.assertionException("Incorrect doubled fuel synchronization for " + tier);
                }
            }
        }
        helper.succeed();
    }

    private static void extremeCoalDoublesFastCoalBonuses(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack extremeCoal = new ItemStack(FastFurnaceV2.EXTREME_COAL.get());
        if (extremeCoal.getBurnTime(RecipeType.SMELTING, level.fuelValues())
                != FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME) {
            throw helper.assertionException("Extreme Coal did not retain coal's ordinary 1600-tick fuel value");
        }

        int[] expectedCookTimes = {80, 66, 57, 50};
        int[] expectedSpeeds = {250, 300, 350, 400};
        int[] expectedBurnTimes = {3200, 4000, 4800, 5600};
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            int tierIndex = tier.ordinal();
            int expectedCookTime = expectedCookTimes[tierIndex];
            int expectedBurnTime = expectedBurnTimes[tierIndex];
            if (tier.getSpeedPercent(false, true) != expectedSpeeds[tierIndex]
                    || tier.scaleCookTime(200, false, true) != expectedCookTime
                    || tier.scaleExtremeCoalBurnTime(FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME)
                            != expectedBurnTime) {
                throw helper.assertionException("Incorrect configured Extreme Coal stats for " + tier);
            }

            BlockPos relativePos = new BlockPos(tierIndex * 4, 0, 0);
            FastFurnaceBlockEntity furnace = placeFurnace(helper, relativePos, extremeCoal.copy(), tier);
            FastFurnaceMenu menu = createMenu(helper, furnace);
            level.getRandom().setSeed(findChanceRollSeed(tier.getDoubleBurnChance(), false));
            tickFurnace(helper, furnace, relativePos);
            assertBurnState(helper, menu, expectedBurnTime, expectedBurnTime, true, true,
                    tier + " did not synchronize Extreme Coal");

            for (int tick = 2; tick <= expectedCookTime; tick++) {
                tickFurnace(helper, furnace, relativePos);
                if (tick < expectedCookTime && !furnace.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                    throw helper.assertionException(tier + " smelted before Extreme Coal tick " + expectedCookTime);
                }
            }
            ItemStack result = furnace.getItem(FastFurnaceMenu.RESULT_SLOT);
            if (!result.is(Items.STONE) || result.getCount() != 1) {
                throw helper.assertionException(tier + " did not smelt on Extreme Coal tick " + expectedCookTime);
            }
            float expectedSmelts = expectedBurnTime / (float) expectedCookTime;
            if (Math.abs(tier.getSmeltCount(expectedBurnTime, false, true) - expectedSmelts) > 0.0001F) {
                throw helper.assertionException("Incorrect Extreme Coal JEI smelt count for " + tier);
            }

            BlockPos doubledPos = relativePos.above(4);
            FastFurnaceBlockEntity doubledFurnace = placeFurnace(helper, doubledPos, extremeCoal.copy(), tier);
            FastFurnaceMenu doubledMenu = createMenu(helper, doubledFurnace);
            level.getRandom().setSeed(findChanceRollSeed(tier.getDoubleBurnChance(), true));
            tickFurnace(helper, doubledFurnace, doubledPos);
            assertBurnState(helper, doubledMenu, expectedBurnTime * 2, expectedBurnTime * 2, true, true,
                    tier + " did not independently double Extreme Coal duration");
        }
        assertExtremeCoalBlockFuel(helper);
        helper.succeed();
    }

    private static void assertExtremeCoalBlockFuel(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack extremeCoalBlock = new ItemStack(FastFurnaceV2.EXTREME_COAL_BLOCK_ITEM.get());
        if (extremeCoalBlock.getBurnTime(RecipeType.SMELTING, level.fuelValues())
                != FastFurnaceBlockEntity.VANILLA_COAL_BLOCK_BURN_TIME) {
            throw helper.assertionException("Extreme Coal Block did not retain a coal block's ordinary 16000-tick fuel value");
        }
        FastFuel definition = FastFurnaceApi.findFuel(FastFurnaceApi.EXTREME_COAL_BLOCK_FUEL_ID)
                .orElseThrow(() -> helper.assertionException("Extreme Coal Block was not registered through the fuel API"));
        if (!FastFurnaceV2.isExtremeCoalFuel(extremeCoalBlock)
                || definition.speedBonusPercent() != FastFurnaceTier.EXTREME_COAL_SPEED_BONUS_PERCENT
                || definition.acceleratorWork() != AcceleratorBlockEntity.EXTREME_COAL_BLOCK_BOOST_WORK
                || definition.acceleratorWorkPerTick() != AcceleratorBlockEntity.EXTREME_COAL_BOOSTED_WORK_PER_TICK
                || !AcceleratorBlockEntity.isAcceleratorFuel(extremeCoalBlock)) {
            throw helper.assertionException("Extreme Coal Block has incorrect Extreme Coal or Accelerator bonuses");
        }

        int[] expectedCookTimes = {80, 66, 57, 50};
        for (FastFurnaceTier tier : FastFurnaceTier.values()) {
            int tierIndex = tier.ordinal();
            int expectedBurnTime = tier.scaleExtremeCoalBurnTime(FastFurnaceBlockEntity.VANILLA_COAL_BLOCK_BURN_TIME);
            BlockPos relativePos = new BlockPos(tierIndex * 4, 8, 0);
            FastFurnaceBlockEntity furnace = placeFurnace(helper, relativePos, extremeCoalBlock.copy(), tier);
            FastFurnaceMenu menu = createMenu(helper, furnace);
            level.getRandom().setSeed(findChanceRollSeed(tier.getDoubleBurnChance(), false));
            tickFurnace(helper, furnace, relativePos);
            assertBurnState(helper, menu, expectedBurnTime, expectedBurnTime, true, true,
                    tier + " did not synchronize Extreme Coal Block");
            if (menu.getSpeedPercent() != tier.getSpeedPercent(false, true)) {
                throw helper.assertionException(tier + " did not apply Extreme Coal Block's speed bonus");
            }

            int expectedCookTime = expectedCookTimes[tierIndex];
            for (int tick = 2; tick <= expectedCookTime; tick++) {
                tickFurnace(helper, furnace, relativePos);
                if (tick < expectedCookTime && !furnace.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                    throw helper.assertionException(tier + " smelted before Extreme Coal Block tick " + expectedCookTime);
                }
            }
            if (!furnace.getItem(FastFurnaceMenu.RESULT_SLOT).is(Items.STONE)) {
                throw helper.assertionException(tier + " did not smelt with Extreme Coal Block");
            }

            BlockPos doubledPos = relativePos.above(4);
            FastFurnaceBlockEntity doubledFurnace = placeFurnace(helper, doubledPos, extremeCoalBlock.copy(), tier);
            FastFurnaceMenu doubledMenu = createMenu(helper, doubledFurnace);
            level.getRandom().setSeed(findChanceRollSeed(tier.getDoubleBurnChance(), true));
            tickFurnace(helper, doubledFurnace, doubledPos);
            assertBurnState(helper, doubledMenu, expectedBurnTime * 2, expectedBurnTime * 2, true, true,
                    tier + " did not independently double Extreme Coal Block duration");
        }
    }

    private static void fastOresReplaceRegularOresOnce(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        var chunkPos = new net.minecraft.world.level.ChunkPos(origin.getX() >> 4, origin.getZ() >> 4);
        List<Map.Entry<Block, Block>> orePairs = List.of(
                Map.entry(Blocks.COAL_ORE, FastFurnaceV2.EXTREME_COAL_ORE.get()),
                Map.entry(Blocks.DEEPSLATE_COAL_ORE, FastFurnaceV2.DEEPSLATE_EXTREME_COAL_ORE.get()),
                Map.entry(Blocks.IRON_ORE, FastFurnaceV2.FAST_IRON_ORE.get()),
                Map.entry(Blocks.DEEPSLATE_IRON_ORE, FastFurnaceV2.DEEPSLATE_FAST_IRON_ORE.get()),
                Map.entry(Blocks.GOLD_ORE, FastFurnaceV2.FAST_GOLD_ORE.get()),
                Map.entry(Blocks.DEEPSLATE_GOLD_ORE, FastFurnaceV2.DEEPSLATE_FAST_GOLD_ORE.get()),
                Map.entry(Blocks.DIAMOND_ORE, FastFurnaceV2.FAST_DIAMOND_ORE.get()),
                Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, FastFurnaceV2.DEEPSLATE_FAST_DIAMOND_ORE.get()));
        BlockPos[] replacedPositions = new BlockPos[orePairs.size()];
        BlockPos[] retainedPositions = new BlockPos[orePairs.size()];

        for (int oreIndex = 0; oreIndex < orePairs.size(); oreIndex++) {
            Block source = orePairs.get(oreIndex).getKey();
            int y = origin.getY() + oreIndex * 2;
            for (int x = 0; x < 16 && (replacedPositions[oreIndex] == null || retainedPositions[oreIndex] == null); x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z);
                    if (FastOreGeneration.shouldReplace(level.getSeed(), pos, source)
                            && replacedPositions[oreIndex] == null) {
                        replacedPositions[oreIndex] = pos;
                    } else if (!FastOreGeneration.shouldReplace(level.getSeed(), pos, source)
                            && retainedPositions[oreIndex] == null) {
                        retainedPositions[oreIndex] = pos;
                    }
                    if (replacedPositions[oreIndex] != null && retainedPositions[oreIndex] != null) {
                        break;
                    }
                }
            }
            if (replacedPositions[oreIndex] == null || retainedPositions[oreIndex] == null) {
                throw helper.assertionException("Could not establish deterministic 5% replacement positions for " + source);
            }
            level.setBlock(replacedPositions[oreIndex], source.defaultBlockState(), 0);
            level.setBlock(retainedPositions[oreIndex], source.defaultBlockState(), 0);
        }

        net.minecraft.world.level.chunk.ChunkAccess chunk = level.getChunk(chunkPos.x(), chunkPos.z());
        chunk.setData(FastFurnaceV2.EXTREME_COAL_CHUNK_PROCESSED, true);
        chunk.setData(FastFurnaceV2.FAST_ORE_CHUNK_PROCESSED, false);
        chunk.setData(FastFurnaceV2.DEEPSLATE_FAST_ORE_CHUNK_PROCESSED, false);
        int replacements = FastOreGeneration.processChunk(level.getSeed(), chunk);
        if (replacements < orePairs.size()
                || !chunk.getData(FastFurnaceV2.FAST_ORE_CHUNK_PROCESSED)
                || !chunk.getData(FastFurnaceV2.DEEPSLATE_FAST_ORE_CHUNK_PROCESSED)) {
            throw helper.assertionException("Fast ore generation did not process regular and deepslate ores once");
        }
        assertOreReplacementStates(helper, level, orePairs, replacedPositions, retainedPositions);
        for (int oreIndex = 0; oreIndex < orePairs.size(); oreIndex++) {
            level.setBlock(replacedPositions[oreIndex], orePairs.get(oreIndex).getKey().defaultBlockState(), 0);
        }

        int repeatedReplacements = FastOreGeneration.processChunk(level.getSeed(), chunk);
        if (repeatedReplacements != 0) {
            throw helper.assertionException("A processed chunk rolled for Fast ores more than once");
        }
        for (Map.Entry<Block, Block> orePair : orePairs) {
            int oreIndex = orePairs.indexOf(orePair);
            if (!level.getBlockState(replacedPositions[oreIndex]).is(orePair.getKey())) {
                throw helper.assertionException("A processed chunk replaced newly placed ore");
            }
        }

        // A 1.3.0 chunk has only the regular marker. Its deepslate pass must not reroll regular ores.
        chunk.setData(FastFurnaceV2.FAST_ORE_CHUNK_PROCESSED, true);
        chunk.setData(FastFurnaceV2.DEEPSLATE_FAST_ORE_CHUNK_PROCESSED, false);
        int deepslateReplacements = FastOreGeneration.processChunk(level.getSeed(), chunk);
        if (deepslateReplacements < 4) {
            throw helper.assertionException("A legacy chunk did not receive its deepslate-only Fast ore pass");
        }
        for (int oreIndex = 0; oreIndex < orePairs.size(); oreIndex += 2) {
            if (!level.getBlockState(replacedPositions[oreIndex]).is(orePairs.get(oreIndex).getKey())
                    || !level.getBlockState(replacedPositions[oreIndex + 1]).is(orePairs.get(oreIndex + 1).getValue())) {
                throw helper.assertionException("The deepslate migration pass rerolled a regular ore or missed deepslate");
            }
        }
        helper.succeed();
    }

    private static void assertOreReplacementStates(
            GameTestHelper helper,
            ServerLevel level,
            List<Map.Entry<Block, Block>> orePairs,
            BlockPos[] replacedPositions,
            BlockPos[] retainedPositions) {
        for (int oreIndex = 0; oreIndex < orePairs.size(); oreIndex++) {
            if (!level.getBlockState(replacedPositions[oreIndex]).is(orePairs.get(oreIndex).getValue())
                    || !level.getBlockState(retainedPositions[oreIndex]).is(orePairs.get(oreIndex).getKey())) {
                throw helper.assertionException("Fast ore generation replaced the wrong blocks for "
                        + orePairs.get(oreIndex).getKey());
            }
        }
    }

    private static void crushedOresMatchRawOreSmelting(GameTestHelper helper) {
        assertFastMaterialProcessing(helper);
        ServerLevel level = helper.getLevel();
        Item crushedIron = CrushedOres.forRawOre(Identifier.withDefaultNamespace("raw_iron"));
        Item crushedCopper = CrushedOres.forRawOre(Identifier.withDefaultNamespace("raw_copper"));
        Item crushedGold = CrushedOres.forRawOre(Identifier.withDefaultNamespace("raw_gold"));
        if (crushedIron == null || crushedCopper == null || crushedGold == null) {
            throw helper.assertionException("Vanilla raw iron, copper, and gold were not all discovered");
        }
        if (crushedIron instanceof CrushedOreItem
                && !crushedIron.getDefaultInstance().getHoverName().getString().equals("Crushed Iron")) {
            throw helper.assertionException("Crushed iron did not receive its material-specific display name");
        }
        Component moddedTinName = Component.translatable("item.fastfurnacev2.crushed_ore",
                CrushedOreItem.createMaterialName(
                        Identifier.fromNamespaceAndPath("examplemod", "tin_raw_ore"), "tin_raw_ore"));
        if (!moddedTinName.getString().equals("Crushed Tin")) {
            throw helper.assertionException("A modded raw-ore suffix did not receive its English fallback name");
        }

        Identifier generatedRawSuffixedId = Identifier.fromNamespaceAndPath(
                FastFurnaceV2.MODID, "crushed_examplemod_tin_raw_ore");
        if (!CrushedOres.isDiscoverableRawOre(
                        Identifier.fromNamespaceAndPath("examplemod", "tin_raw_ore"), Items.IRON_INGOT)
                || CrushedOres.isDiscoverableRawOre(generatedRawSuffixedId, Items.IRON_INGOT)) {
            throw helper.assertionException(
                    "Raw-ore discovery accepted a generated item or rejected an external raw ore");
        }

        if (!CrushedOres.isCrushedOreForMaterial(
                        Identifier.fromNamespaceAndPath("ftbmaterials", "tin_dust"), "tin_raw_ore")
                || !CrushedOres.isCrushedOreForMaterial(
                        Identifier.fromNamespaceAndPath("create", "crushed_raw_iron"), "iron")
                || !CrushedOres.isCrushedOreForMaterial(
                        Identifier.fromNamespaceAndPath("examplemod", "tin_crushed_raw_ore"), "tin_raw_ore")
                || CrushedOres.isCrushedOreForMaterial(
                        Identifier.fromNamespaceAndPath("ftbmaterials", "tin_ore"), "tin_raw_ore")) {
            throw helper.assertionException("Conventional external crushed ores were not matched to their raw materials");
        }

        Identifier firstCollision = CrushedOres.generatedRecipeId(
                Identifier.fromNamespaceAndPath("foo_bar", "baz"), Identifier.withDefaultNamespace("smelting"));
        Identifier secondCollision = CrushedOres.generatedRecipeId(
                Identifier.fromNamespaceAndPath("foo", "bar_baz"), Identifier.withDefaultNamespace("smelting"));
        if (firstCollision.equals(secondCollision)
                || !firstCollision.equals(CrushedOres.generatedRecipeId(
                        Identifier.fromNamespaceAndPath("foo_bar", "baz"),
                        Identifier.withDefaultNamespace("smelting")))) {
            throw helper.assertionException("Generated crushed recipe IDs collided or were not deterministic");
        }

        for (Identifier rawOreId : CrushedOres.rawOres()) {
            Item crushed = CrushedOres.forRawOre(rawOreId);
            if (crushed == null) {
                throw helper.assertionException("No registered crushed result for " + rawOreId);
            }
            Item raw = BuiltInRegistries.ITEM.getValue(rawOreId);
            SingleRecipeInput rawInput = new SingleRecipeInput(raw.getDefaultInstance());
            SingleRecipeInput crushedInput = new SingleRecipeInput(crushed.getDefaultInstance());
            RecipeHolder<? extends Recipe<SingleRecipeInput>> rawRecipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, rawInput, level)
                    .orElseThrow(() -> helper.assertionException("No raw smelting recipe for " + rawOreId));
            RecipeHolder<? extends Recipe<SingleRecipeInput>> crushedRecipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, crushedInput, level)
                    .orElseThrow(() -> helper.assertionException("No crushed smelting recipe for " + BuiltInRegistries.ITEM.getKey(crushed)));
            if (crushed instanceof CrushedOreItem) {
                ItemStack rawResult = rawRecipe.value().assemble(rawInput);
                ItemStack crushedResult = crushedRecipe.value().assemble(crushedInput);
                if (!ItemStack.isSameItemSameComponents(rawResult, crushedResult)
                        || rawResult.getCount() != crushedResult.getCount()) {
                    throw helper.assertionException("Crushed result differs from raw result for " + rawOreId);
                }
                if (rawRecipe.value() instanceof AbstractCookingRecipe rawCooking
                        && crushedRecipe.value() instanceof AbstractCookingRecipe crushedCooking
                        && (rawCooking.cookingTime() != crushedCooking.cookingTime()
                        || rawCooking.experience() != crushedCooking.experience()
                        || rawCooking.category() != crushedCooking.category()
                        || !rawCooking.group().equals(crushedCooking.group()))) {
                    throw helper.assertionException("Crushed recipe settings differ from raw recipe for " + rawOreId);
                }
            }
        }

        BlockPos vanillaPos = BlockPos.ZERO;
        helper.setBlock(vanillaPos, Blocks.FURNACE);
        FurnaceBlockEntity vanillaFurnace = helper.getBlockEntity(vanillaPos, FurnaceBlockEntity.class);
        vanillaFurnace.setItem(0, crushedIron.getDefaultInstance());
        vanillaFurnace.setItem(1, new ItemStack(Items.COAL));
        FastFurnaceBlockEntity fastFurnace = placeFurnace(
                helper, new BlockPos(2, 0, 0), new ItemStack(Items.COAL));
        fastFurnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, crushedIron.getDefaultInstance());
        for (int tick = 1; tick <= 200; tick++) {
            AbstractFurnaceBlockEntity.serverTick(level, helper.absolutePos(vanillaPos),
                    level.getBlockState(helper.absolutePos(vanillaPos)), vanillaFurnace);
            tickFurnace(helper, fastFurnace, new BlockPos(2, 0, 0));
        }
        if (!vanillaFurnace.getItem(2).is(Items.IRON_INGOT) || vanillaFurnace.getItem(2).getCount() != 1) {
            throw helper.assertionException("An ordinary furnace did not smelt crushed iron into iron");
        }
        if (!fastFurnace.getItem(FastFurnaceMenu.RESULT_SLOT).is(Items.IRON_INGOT)
                || fastFurnace.getItem(FastFurnaceMenu.RESULT_SLOT).getCount() != 1) {
            throw helper.assertionException("A Fast Furnace did not smelt crushed iron into iron");
        }
        helper.succeed();
    }

    private static void acceleratorConvertsCoalWithLavaAndBoosts(GameTestHelper helper) {
        assertAcceleratorConversion(helper, Items.IRON_INGOT, FastFurnaceV2.FAST_IRON_INGOT.get(),
                AcceleratorBlockEntity.ITEM_RECIPE_TICKS);
        assertAcceleratorConversion(helper, Items.IRON_BLOCK, FastFurnaceV2.FAST_IRON_BLOCK_ITEM.get(),
                AcceleratorBlockEntity.BLOCK_RECIPE_TICKS);
        assertAcceleratorConversion(helper, Items.GOLD_INGOT, FastFurnaceV2.FAST_GOLD_INGOT.get(),
                AcceleratorBlockEntity.ITEM_RECIPE_TICKS);
        assertAcceleratorConversion(helper, Items.GOLD_BLOCK, FastFurnaceV2.FAST_GOLD_BLOCK_ITEM.get(),
                AcceleratorBlockEntity.BLOCK_RECIPE_TICKS);
        assertAcceleratorConversion(helper, Items.DIAMOND, FastFurnaceV2.FAST_DIAMOND.get(),
                AcceleratorBlockEntity.ITEM_RECIPE_TICKS);
        assertAcceleratorConversion(helper, Items.DIAMOND_BLOCK, FastFurnaceV2.FAST_DIAMOND_BLOCK_ITEM.get(),
                AcceleratorBlockEntity.BLOCK_RECIPE_TICKS);
        ServerLevel level = helper.getLevel();
        AcceleratorBlockEntity normal = placeAccelerator(
                helper, BlockPos.ZERO, new ItemStack(Items.COAL), ItemStack.EMPTY);
        AcceleratorMenu normalMenu = createAcceleratorMenu(helper, normal);
        if (!normal.getDisplayName().getString().equals("Accelerator")
                || normal.canPlaceItem(AcceleratorMenu.INPUT_SLOT, new ItemStack(FastFurnaceV2.FAST_COAL.get()))
                || normal.canPlaceItem(AcceleratorMenu.FUEL_SLOT, new ItemStack(Items.COAL))
                || !normal.canPlaceItem(AcceleratorMenu.FUEL_SLOT, new ItemStack(FastFurnaceV2.FAST_COAL.get()))
                || !normal.canPlaceItem(AcceleratorMenu.FUEL_SLOT, new ItemStack(FastFurnaceV2.EXTREME_COAL.get()))
                || !normalMenu.getSlot(AcceleratorMenu.FUEL_SLOT).mayPlace(
                        new ItemStack(FastFurnaceV2.EXTREME_COAL.get()))
                || normalMenu.getSlot(AcceleratorMenu.FUEL_SLOT).mayPlace(new ItemStack(Items.COAL))
                || normalMenu.getSlot(AcceleratorMenu.RESULT_SLOT).mayPlace(new ItemStack(Items.COAL))
                || normal.getSlotsForFace(Direction.UP)[0] != AcceleratorMenu.INPUT_SLOT
                || normal.getSlotsForFace(Direction.DOWN)[0] != AcceleratorMenu.RESULT_SLOT) {
            throw helper.assertionException("Accelerator slots or automation accepted unsupported items");
        }

        for (int tick = 1; tick <= AcceleratorBlockEntity.ITEM_RECIPE_TICKS; tick++) {
            tickAccelerator(helper, normal, BlockPos.ZERO);
            if (tick < AcceleratorBlockEntity.ITEM_RECIPE_TICKS
                    && !normal.getItem(AcceleratorMenu.RESULT_SLOT).isEmpty()) {
                throw helper.assertionException("Accelerator converted coal before 400 ticks");
            }
            if (tick == 200 && level.getBlockState(helper.absolutePos(BlockPos.ZERO))
                    .getValue(AcceleratorBlock.BOLT_LEVEL) != 5) {
                throw helper.assertionException("Accelerator lightning bolt was not half full at 50% progress");
            }
            if (tick == 399 && level.getBlockState(helper.absolutePos(BlockPos.ZERO))
                    .getValue(AcceleratorBlock.BOLT_LEVEL) != 10) {
                throw helper.assertionException("Accelerator lightning bolt was not full near completion");
            }
        }
        if (!normal.getItem(AcceleratorMenu.RESULT_SLOT).is(FastFurnaceV2.FAST_COAL.get())
                || normal.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 1
                || !normal.getItem(AcceleratorMenu.INPUT_SLOT).isEmpty()
                || normalMenu.getLavaSideCount() != AcceleratorBlockEntity.REQUIRED_LAVA_SIDES
                || level.getBlockState(helper.absolutePos(BlockPos.ZERO))
                        .getValue(AcceleratorBlock.BOLT_LEVEL) != 0) {
            throw helper.assertionException("Accelerator did not convert coal in twenty seconds without fuel");
        }

        BlockPos coalBlockPos = new BlockPos(4, 0, 0);
        AcceleratorBlockEntity coalBlock = placeAccelerator(
                helper, coalBlockPos, new ItemStack(Items.COAL_BLOCK), ItemStack.EMPTY);
        tickAccelerator(helper, coalBlock, coalBlockPos, AcceleratorBlockEntity.BLOCK_RECIPE_TICKS - 1);
        if (!coalBlock.getItem(AcceleratorMenu.RESULT_SLOT).isEmpty()) {
            throw helper.assertionException("Accelerator converted a coal block before 3600 ticks");
        }
        tickAccelerator(helper, coalBlock, coalBlockPos);
        if (!coalBlock.getItem(AcceleratorMenu.RESULT_SLOT).is(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get())
                || coalBlock.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 1) {
            throw helper.assertionException("Accelerator did not convert a coal block in 180 seconds");
        }

        BlockPos missingLavaPos = new BlockPos(8, 0, 0);
        AcceleratorBlockEntity missingLava = placeAccelerator(
                helper, missingLavaPos, new ItemStack(Items.COAL),
                new ItemStack(FastFurnaceV2.FAST_COAL.get()));
        helper.setBlock(missingLavaPos.north(), Blocks.AIR);
        AcceleratorMenu missingLavaMenu = createAcceleratorMenu(helper, missingLava);
        tickAccelerator(helper, missingLava, missingLavaPos, 20);
        if (missingLavaMenu.getWorkProgress() != 0 || missingLavaMenu.getBoostWork() != 0
                || missingLava.getItem(AcceleratorMenu.FUEL_SLOT).getCount() != 1
                || missingLavaMenu.getLavaSideCount() != 3) {
            throw helper.assertionException("Accelerator processed or consumed boost without all four lava sides");
        }
        helper.setBlock(missingLavaPos.north(), Blocks.LAVA);
        tickAccelerator(helper, missingLava, missingLavaPos, 134);
        if (!missingLava.getItem(AcceleratorMenu.RESULT_SLOT).is(FastFurnaceV2.FAST_COAL.get())
                || missingLavaMenu.getBoostWork() != 3200) {
            throw helper.assertionException("Accelerator did not resume at triple speed after lava was restored");
        }

        BlockPos pausedPos = new BlockPos(0, 4, 0);
        AcceleratorBlockEntity paused = placeAccelerator(
                helper, pausedPos, new ItemStack(Items.COAL),
                new ItemStack(FastFurnaceV2.FAST_COAL.get()));
        AcceleratorMenu pausedMenu = createAcceleratorMenu(helper, paused);
        tickAccelerator(helper, paused, pausedPos, 10);
        int savedWork = pausedMenu.getWorkProgress();
        int savedBoost = pausedMenu.getBoostWork();
        helper.setBlock(pausedPos.east(), Blocks.AIR);
        tickAccelerator(helper, paused, pausedPos, 20);
        if (pausedMenu.getWorkProgress() != savedWork || pausedMenu.getBoostWork() != savedBoost) {
            throw helper.assertionException("Accelerator lost progress or boost while lava was missing");
        }
        helper.setBlock(pausedPos.east(), Blocks.LAVA);
        paused.setItem(AcceleratorMenu.RESULT_SLOT, new ItemStack(Items.DIRT));
        tickAccelerator(helper, paused, pausedPos, 20);
        if (pausedMenu.getWorkProgress() != savedWork || pausedMenu.getBoostWork() != savedBoost) {
            throw helper.assertionException("Accelerator lost progress or boost while its output was blocked");
        }
        paused.setItem(AcceleratorMenu.RESULT_SLOT, ItemStack.EMPTY);
        tickAccelerator(helper, paused, pausedPos, 124);
        if (!paused.getItem(AcceleratorMenu.RESULT_SLOT).is(FastFurnaceV2.FAST_COAL.get())
                || pausedMenu.getBoostWork() != 3200) {
            throw helper.assertionException("Accelerator did not resume its saved boosted operation");
        }
        paused.setItem(AcceleratorMenu.INPUT_SLOT, new ItemStack(Items.COAL));
        tickAccelerator(helper, paused, pausedPos, 5);
        paused.setItem(AcceleratorMenu.INPUT_SLOT, new ItemStack(Items.COAL_BLOCK));
        if (pausedMenu.getWorkProgress() != 0
                || pausedMenu.getRecipeTicks() != AcceleratorBlockEntity.BLOCK_RECIPE_TICKS) {
            throw helper.assertionException("Accelerator did not reset progress when its input type changed");
        }

        BlockPos invalidPos = new BlockPos(4, 4, 0);
        AcceleratorBlockEntity invalid = placeAccelerator(
                helper, invalidPos, new ItemStack(Items.DIRT), new ItemStack(Items.COAL));
        AcceleratorMenu invalidMenu = createAcceleratorMenu(helper, invalid);
        tickAccelerator(helper, invalid, invalidPos, 20);
        if (invalidMenu.getWorkProgress() != 0 || invalidMenu.getBoostWork() != 0
                || invalid.getItem(AcceleratorMenu.FUEL_SLOT).getCount() != 1
                || !invalid.getItem(AcceleratorMenu.RESULT_SLOT).isEmpty()) {
            throw helper.assertionException("Accelerator processed an unsupported input or consumed ordinary fuel");
        }

        BlockPos abortedTransactionPos = new BlockPos(8, 4, 0);
        AcceleratorBlockEntity aborted = placeAccelerator(
                helper, abortedTransactionPos, new ItemStack(Items.COAL), ItemStack.EMPTY);
        AcceleratorMenu abortedMenu = createAcceleratorMenu(helper, aborted);
        tickAccelerator(helper, aborted, abortedTransactionPos, 25);
        ResourceHandler<ItemResource> abortedItems = VanillaContainerWrapper.of(aborted);
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = abortedItems.extract(
                    AcceleratorMenu.INPUT_SLOT, ItemResource.of(Items.COAL), 1, transaction);
            if (extracted != 1) {
                throw helper.assertionException("Could not extract Accelerator input for the abort test");
            }
        }
        if (!aborted.getItem(AcceleratorMenu.INPUT_SLOT).is(Items.COAL)
                || abortedMenu.getWorkProgress() != 25
                || abortedMenu.getRecipeTicks() != AcceleratorBlockEntity.ITEM_RECIPE_TICKS) {
            throw helper.assertionException("An aborted Accelerator extraction erased progress or failed to restore coal");
        }

        BlockPos committedTransactionPos = new BlockPos(12, 4, 0);
        AcceleratorBlockEntity committed = placeAccelerator(
                helper, committedTransactionPos, new ItemStack(Items.COAL), ItemStack.EMPTY);
        AcceleratorMenu committedMenu = createAcceleratorMenu(helper, committed);
        tickAccelerator(helper, committed, committedTransactionPos, 25);
        ResourceHandler<ItemResource> committedItems = VanillaContainerWrapper.of(committed);
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = committedItems.extract(
                    AcceleratorMenu.INPUT_SLOT, ItemResource.of(Items.COAL), 1, transaction);
            if (extracted != 1) {
                throw helper.assertionException("Could not extract Accelerator input for the commit test");
            }
            transaction.commit();
        }
        if (!committed.getItem(AcceleratorMenu.INPUT_SLOT).isEmpty()
                || committedMenu.getWorkProgress() != 0 || committedMenu.getRecipeTicks() != 0) {
            throw helper.assertionException("A committed Accelerator extraction did not reset its completed input state");
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = committedItems.insert(
                    AcceleratorMenu.INPUT_SLOT, ItemResource.of(Items.COAL_BLOCK), 1, transaction);
            if (inserted != 1) {
                throw helper.assertionException("Could not insert a coal block transactionally");
            }
            transaction.commit();
        }
        if (committedMenu.getWorkProgress() != 0
                || committedMenu.getRecipeTicks() != AcceleratorBlockEntity.BLOCK_RECIPE_TICKS) {
            throw helper.assertionException("A committed Accelerator refill did not select the new conversion duration");
        }

        acceleratorBoostCapacities(helper);
        helper.succeed();
    }

    private static void extremelyFastAlloyerUsesOnlyShapelessAlloyingRecipes(GameTestHelper helper) {
        if (ExtremelyFastAlloyerRecipes.recipes().size() != 1
                || ExtremelyFastAlloyerRecipes.recipes().get(0).processingTicks() != 800) {
            throw helper.assertionException("Expected only the built-in 800-tick Extremely Fast Ingot recipe");
        }
        ExtremelyFastAlloyerBlockEntity alloyer = placeAlloyer(
                helper, BlockPos.ZERO,
                new ItemStack(FastFurnaceV2.FAST_DIAMOND.get()),
                new ItemStack(FastFurnaceV2.FAST_IRON_INGOT.get()),
                new ItemStack(FastFurnaceV2.FAST_GOLD_INGOT.get()));
        ExtremelyFastAlloyerMenu menu = createAlloyerMenu(helper, alloyer);
        if (!alloyer.getDisplayName().getString().equals("Extremely Fast Alloyer")
                || menu.getRecipeTicks() != 800
                || !menu.getSlot(0).mayPlace(new ItemStack(FastFurnaceV2.FAST_IRON_INGOT.get()))
                || !menu.getSlot(ExtremelyFastAlloyerMenu.FUEL_SLOT).mayPlace(
                        new ItemStack(FastFurnaceV2.EXTREME_COAL.get()))
                || menu.getSlot(ExtremelyFastAlloyerMenu.FUEL_SLOT).mayPlace(
                        new ItemStack(FastFurnaceV2.EXTREME_COAL_BLOCK_ITEM.get()))
                || menu.getSlot(ExtremelyFastAlloyerMenu.FUEL_SLOT).mayPlace(new ItemStack(Items.COAL))
                || menu.getSlot(ExtremelyFastAlloyerMenu.RESULT_SLOT).mayPlace(
                        new ItemStack(FastFurnaceV2.EXTREMELY_FAST_INGOT.get()))
                || !alloyer.canPlaceItem(0, new ItemStack(FastFurnaceV2.FAST_GOLD_INGOT.get()))
                || alloyer.canPlaceItem(0, new ItemStack(Items.RAW_IRON))
                || alloyer.getSlotsForFace(Direction.UP).length != 3
                || alloyer.getSlotsForFace(Direction.NORTH).length != 1
                || alloyer.getSlotsForFace(Direction.NORTH)[0] != ExtremelyFastAlloyerMenu.FUEL_SLOT
                || alloyer.getSlotsForFace(Direction.DOWN)[0] != ExtremelyFastAlloyerMenu.RESULT_SLOT) {
            throw helper.assertionException("Alloyer menu, identity, fuel, ingredient, or automation restrictions are incorrect");
        }
        tickAlloyer(helper, alloyer, BlockPos.ZERO, 399);
        if (!alloyer.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).isEmpty()
                || menu.getBurnTimeRemaining() != 1201
                || menu.getTotalBurnTime() != 1600) {
            throw helper.assertionException("Alloyer completed early or did not consume exactly one Extreme Coal");
        }
        tickAlloyer(helper, alloyer, BlockPos.ZERO);
        if (helper.getBlockState(BlockPos.ZERO).getValue(ExtremelyFastAlloyerBlock.BOLT_LEVEL) != 5) {
            throw helper.assertionException("Alloyer bolt did not show half progress at tick 400: progress="
                    + menu.getWorkProgress() + ", duration=" + menu.getRecipeTicks()
                    + ", lava=" + menu.getLavaSides() + ", bolt="
                    + helper.getBlockState(BlockPos.ZERO).getValue(ExtremelyFastAlloyerBlock.BOLT_LEVEL));
        }
        tickAlloyer(helper, alloyer, BlockPos.ZERO, 399);
        if (menu.getWorkProgress() != 799 || !menu.isProcessing()
                || helper.getBlockState(BlockPos.ZERO).getValue(ExtremelyFastAlloyerBlock.BOLT_LEVEL) != 10) {
            throw helper.assertionException("Alloyer did not synchronize tick-799 progress and its full bolt");
        }
        tickAlloyer(helper, alloyer, BlockPos.ZERO);
        if (!alloyer.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).is(FastFurnaceV2.EXTREMELY_FAST_INGOT.get())
                || alloyer.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).getCount() != 1
                || !alloyer.getItem(0).isEmpty() || !alloyer.getItem(1).isEmpty() || !alloyer.getItem(2).isEmpty()
                || menu.getWorkProgress() != 0 || menu.getRecipeTicks() != 0 || menu.getBurnTimeRemaining() != 800
                || helper.getBlockState(BlockPos.ZERO).getValue(ExtremelyFastAlloyerBlock.BOLT_LEVEL) != 0) {
            throw helper.assertionException("Alloyer did not consume one of each shapeless ingredient on tick 800");
        }

        BlockPos duplicatePos = new BlockPos(4, 0, 0);
        ExtremelyFastAlloyerBlockEntity duplicate = placeAlloyer(
                helper, duplicatePos,
                new ItemStack(FastFurnaceV2.FAST_IRON_INGOT.get()),
                new ItemStack(FastFurnaceV2.FAST_GOLD_INGOT.get()),
                new ItemStack(FastFurnaceV2.FAST_GOLD_INGOT.get()));
        tickAlloyer(helper, duplicate, duplicatePos, 800);
        if (!duplicate.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).isEmpty()) {
            throw helper.assertionException("Alloyer accepted duplicate ingredients");
        }

        BlockPos foreignPos = new BlockPos(8, 0, 0);
        ExtremelyFastAlloyerBlockEntity foreign = placeAlloyer(
                helper, foreignPos, new ItemStack(Items.COBBLESTONE), new ItemStack(Items.RAW_IRON),
                new ItemStack(Items.COAL));
        ExtremelyFastAlloyerMenu foreignMenu = createAlloyerMenu(helper, foreign);
        tickAlloyer(helper, foreign, foreignPos, 800);
        if (foreignMenu.getWorkProgress() != 0
                || !foreign.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).isEmpty()) {
            throw helper.assertionException("Alloyer accepted furnace, crusher, or Accelerator inputs");
        }

        BlockPos blockedPos = new BlockPos(12, 0, 0);
        ExtremelyFastAlloyerBlockEntity blocked = placeAlloyer(helper, blockedPos);
        ExtremelyFastAlloyerMenu blockedMenu = createAlloyerMenu(helper, blocked);
        tickAlloyer(helper, blocked, blockedPos, 25);
        blocked.setItem(ExtremelyFastAlloyerMenu.RESULT_SLOT, new ItemStack(Items.DIRT));
        tickAlloyer(helper, blocked, blockedPos, 20);
        if (blockedMenu.getWorkProgress() != 25 || blockedMenu.isProcessing()
                || blockedMenu.getBurnTimeRemaining() != 1575) {
            throw helper.assertionException("Blocked Alloyer output did not pause and preserve progress");
        }
        blocked.setItem(ExtremelyFastAlloyerMenu.RESULT_SLOT, ItemStack.EMPTY);
        tickAlloyer(helper, blocked, blockedPos, 775);
        if (blocked.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).getCount() != 1) {
            throw helper.assertionException("Alloyer did not resume its saved operation after output space returned");
        }

        BlockPos changedPos = new BlockPos(16, 0, 0);
        ExtremelyFastAlloyerBlockEntity changed = placeAlloyer(helper, changedPos);
        ExtremelyFastAlloyerMenu changedMenu = createAlloyerMenu(helper, changed);
        tickAlloyer(helper, changed, changedPos, 25);
        changed.setItem(1, new ItemStack(FastFurnaceV2.FAST_IRON_INGOT.get()));
        if (changedMenu.getWorkProgress() != 0 || changedMenu.getRecipeTicks() != 0) {
            throw helper.assertionException("Changing Alloyer input did not reset invalid progress");
        }

        BlockPos repeatPos = new BlockPos(20, 0, 0);
        ExtremelyFastAlloyerBlockEntity repeated = placeAlloyer(
                helper, repeatPos,
                new ItemStack(FastFurnaceV2.FAST_IRON_INGOT.get(), 2),
                new ItemStack(FastFurnaceV2.FAST_GOLD_INGOT.get(), 2),
                new ItemStack(FastFurnaceV2.FAST_DIAMOND.get(), 2));
        tickAlloyer(helper, repeated, repeatPos, 800);
        if (repeated.getItem(0).getCount() != 1 || repeated.getItem(1).getCount() != 1
                || repeated.getItem(2).getCount() != 1
                || repeated.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).getCount() != 1) {
            throw helper.assertionException("Alloyer did not retain stacked ingredients for a second operation");
        }
        tickAlloyer(helper, repeated, repeatPos, 800);
        if (repeated.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).getCount() != 2) {
            throw helper.assertionException("Alloyer did not continuously process stacked ingredients");
        }

        BlockPos abortedPos = new BlockPos(24, 0, 0);
        ExtremelyFastAlloyerBlockEntity aborted = placeAlloyer(helper, abortedPos);
        ExtremelyFastAlloyerMenu abortedMenu = createAlloyerMenu(helper, aborted);
        tickAlloyer(helper, aborted, abortedPos, 25);
        ResourceHandler<ItemResource> abortedItems = VanillaContainerWrapper.of(aborted);
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = abortedItems.extract(
                    0, ItemResource.of(FastFurnaceV2.FAST_IRON_INGOT.get()), 1, transaction);
            if (extracted != 1) {
                throw helper.assertionException("Could not extract Alloyer input for the abort test");
            }
        }
        if (!aborted.getItem(0).is(FastFurnaceV2.FAST_IRON_INGOT.get())
                || abortedMenu.getWorkProgress() != 25 || abortedMenu.getRecipeTicks() != 800) {
            throw helper.assertionException("An aborted Alloyer extraction erased progress or failed to restore its input");
        }

        BlockPos committedPos = new BlockPos(28, 0, 0);
        ExtremelyFastAlloyerBlockEntity committed = placeAlloyer(helper, committedPos);
        ExtremelyFastAlloyerMenu committedMenu = createAlloyerMenu(helper, committed);
        tickAlloyer(helper, committed, committedPos, 25);
        ResourceHandler<ItemResource> committedItems = VanillaContainerWrapper.of(committed);
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = committedItems.extract(
                    0, ItemResource.of(FastFurnaceV2.FAST_IRON_INGOT.get()), 1, transaction);
            if (extracted != 1) {
                throw helper.assertionException("Could not extract Alloyer input for the commit test");
            }
            transaction.commit();
        }
        if (!committed.getItem(0).isEmpty()
                || committedMenu.getWorkProgress() != 0 || committedMenu.getRecipeTicks() != 0) {
            throw helper.assertionException("A committed Alloyer extraction did not reset its completed input state");
        }

        int[] expectedTimes = {800, 700, 600, 500, 400};
        for (int lavaSides = 0; lavaSides <= 4; lavaSides++) {
            BlockPos lavaPos = new BlockPos(32 + lavaSides * 4, 0, 0);
            ExtremelyFastAlloyerBlockEntity lavaAlloyer = placeAlloyer(helper, lavaPos);
            int placedLava = 0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (placedLava++ < lavaSides) {
                    helper.setBlock(lavaPos.relative(direction), Blocks.LAVA);
                }
            }
            int expectedTime = expectedTimes[lavaSides];
            tickAlloyer(helper, lavaAlloyer, lavaPos, expectedTime - 1);
            ExtremelyFastAlloyerMenu lavaMenu = createAlloyerMenu(helper, lavaAlloyer);
            if (lavaMenu.getRecipeTicks() != expectedTime || lavaMenu.getLavaSides() != lavaSides
                    || !lavaAlloyer.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).isEmpty()) {
                throw helper.assertionException("Alloyer did not pause before its " + expectedTime + "-tick lava-scaled operation");
            }
            tickAlloyer(helper, lavaAlloyer, lavaPos);
            if (lavaAlloyer.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).getCount() != 1) {
                throw helper.assertionException("Alloyer did not finish on its " + expectedTime + "-tick lava-scaled operation");
            }
        }

        BlockPos exhaustionPos = new BlockPos(52, 0, 0);
        ExtremelyFastAlloyerBlockEntity exhausted = placeAlloyer(
                helper, exhaustionPos,
                new ItemStack(FastFurnaceV2.FAST_IRON_INGOT.get(), 4),
                new ItemStack(FastFurnaceV2.FAST_GOLD_INGOT.get(), 4),
                new ItemStack(FastFurnaceV2.FAST_DIAMOND.get(), 4));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (direction != Direction.SOUTH) {
                helper.setBlock(exhaustionPos.relative(direction), Blocks.LAVA);
            }
        }
        ExtremelyFastAlloyerMenu exhaustedMenu = createAlloyerMenu(helper, exhausted);
        tickAlloyer(helper, exhausted, exhaustionPos, 1600);
        if (exhaustedMenu.getBurnTimeRemaining() != 0 || exhaustedMenu.getWorkProgress() != 100
                || exhausted.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).getCount() != 3) {
            throw helper.assertionException("Alloyer did not exhaust Extreme Coal after exactly 1,600 valid work ticks");
        }
        tickAlloyer(helper, exhausted, exhaustionPos, 25);
        if (exhaustedMenu.getWorkProgress() != 100) {
            throw helper.assertionException("Missing Alloyer fuel did not preserve saved progress");
        }
        exhausted.setItem(ExtremelyFastAlloyerMenu.FUEL_SLOT, new ItemStack(FastFurnaceV2.EXTREME_COAL.get()));
        tickAlloyer(helper, exhausted, exhaustionPos, 399);
        if (exhausted.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).getCount() != 3) {
            throw helper.assertionException("Refueled Alloyer completed before restoring the remaining work");
        }
        tickAlloyer(helper, exhausted, exhaustionPos);
        if (exhausted.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).getCount() != 4) {
            throw helper.assertionException("Refueled Alloyer did not resume its saved operation");
        }

        BlockPos wrongFuelPos = new BlockPos(56, 0, 0);
        ExtremelyFastAlloyerBlockEntity wrongFuel = placeAlloyer(helper, wrongFuelPos);
        wrongFuel.setItem(ExtremelyFastAlloyerMenu.FUEL_SLOT, new ItemStack(Items.COAL));
        tickAlloyer(helper, wrongFuel, wrongFuelPos, 800);
        if (!wrongFuel.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).isEmpty()
                || wrongFuel.getItem(ExtremelyFastAlloyerMenu.FUEL_SLOT).getCount() != 1) {
            throw helper.assertionException("Alloyer processed with or consumed a non-Extreme-Coal fuel");
        }

        BlockPos changedLavaPos = new BlockPos(60, 0, 0);
        ExtremelyFastAlloyerBlockEntity changedLava = placeAlloyer(helper, changedLavaPos);
        tickAlloyer(helper, changedLava, changedLavaPos, 100);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.setBlock(changedLavaPos.relative(direction), Blocks.LAVA);
        }
        tickAlloyer(helper, changedLava, changedLavaPos, 299);
        if (!changedLava.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).isEmpty()) {
            throw helper.assertionException("Alloyer mishandled elapsed work when its lava count changed");
        }
        tickAlloyer(helper, changedLava, changedLavaPos);
        if (changedLava.getItem(ExtremelyFastAlloyerMenu.RESULT_SLOT).getCount() != 1) {
            throw helper.assertionException("Alloyer did not preserve elapsed work while recalculating lava-scaled time");
        }
        helper.succeed();
    }

    private static void acceleratorBoostCapacities(GameTestHelper helper) {
        BlockPos fastCoalPos = new BlockPos(0, 8, 0);
        AcceleratorBlockEntity fastCoal = placeAccelerator(
                helper, fastCoalPos, new ItemStack(Items.COAL, 10),
                new ItemStack(FastFurnaceV2.FAST_COAL.get()));
        AcceleratorMenu fastCoalMenu = createAcceleratorMenu(helper, fastCoal);
        tickAccelerator(helper, fastCoal, fastCoalPos, 1205);
        if (fastCoal.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 8) {
            throw helper.assertionException("Fast Coal completed its ninth coal conversion too early");
        }
        tickAccelerator(helper, fastCoal, fastCoalPos);
        if (fastCoal.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 9
                || fastCoal.getItem(AcceleratorMenu.INPUT_SLOT).getCount() != 1
                || fastCoalMenu.getBoostWork() != 0
                || !fastCoal.getItem(AcceleratorMenu.FUEL_SLOT).isEmpty()) {
            throw helper.assertionException("Fast Coal did not boost exactly nine coal conversions");
        }
        tickAccelerator(helper, fastCoal, fastCoalPos);
        if (fastCoal.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 9
                || fastCoalMenu.getWorkProgress() != 1) {
            throw helper.assertionException("Accelerator did not return to normal speed after Fast Coal expired");
        }

        BlockPos fastCoalBlockPos = new BlockPos(4, 8, 0);
        AcceleratorBlockEntity fastCoalBlock = placeAccelerator(
                helper, fastCoalBlockPos, new ItemStack(Items.COAL, 11),
                new ItemStack(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get()));
        AcceleratorMenu fastCoalBlockMenu = createAcceleratorMenu(helper, fastCoalBlock);
        tickAccelerator(helper, fastCoalBlock, fastCoalBlockPos, 1340);
        if (fastCoalBlock.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 10
                || fastCoalBlock.getItem(AcceleratorMenu.INPUT_SLOT).getCount() != 1
                || fastCoalBlockMenu.getBoostWork() != 0) {
            throw helper.assertionException("Block of Fast Coal did not boost exactly ten coal conversions");
        }

        BlockPos boostedBlockPos = new BlockPos(8, 8, 0);
        AcceleratorBlockEntity boostedBlock = placeAccelerator(
                helper, boostedBlockPos, new ItemStack(Items.COAL_BLOCK),
                new ItemStack(FastFurnaceV2.FAST_COAL.get()));
        AcceleratorMenu boostedBlockMenu = createAcceleratorMenu(helper, boostedBlock);
        tickAccelerator(helper, boostedBlock, boostedBlockPos, 1199);
        if (!boostedBlock.getItem(AcceleratorMenu.RESULT_SLOT).isEmpty()) {
            throw helper.assertionException("Fast Coal converted a coal block before 1200 boosted ticks");
        }
        tickAccelerator(helper, boostedBlock, boostedBlockPos);
        if (!boostedBlock.getItem(AcceleratorMenu.RESULT_SLOT).is(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get())
                || boostedBlockMenu.getBoostWork() != 0) {
            throw helper.assertionException("Fast Coal did not complete exactly one boosted coal-block conversion");
        }

        BlockPos boostedByBlockPos = new BlockPos(12, 8, 0);
        AcceleratorBlockEntity boostedByBlock = placeAccelerator(
                helper, boostedByBlockPos, new ItemStack(Items.COAL_BLOCK),
                new ItemStack(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get()));
        AcceleratorMenu boostedByBlockMenu = createAcceleratorMenu(helper, boostedByBlock);
        tickAccelerator(helper, boostedByBlock, boostedByBlockPos, 1200);
        if (!boostedByBlock.getItem(AcceleratorMenu.RESULT_SLOT).is(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get())
                || boostedByBlockMenu.getBoostWork() != 400) {
            throw helper.assertionException("Block of Fast Coal did not boost one coal-block conversion");
        }
        boostedByBlock.setItem(AcceleratorMenu.INPUT_SLOT, new ItemStack(Items.COAL));
        boostedByBlock.setItem(AcceleratorMenu.RESULT_SLOT, ItemStack.EMPTY);
        tickAccelerator(helper, boostedByBlock, boostedByBlockPos, 134);
        if (!boostedByBlock.getItem(AcceleratorMenu.RESULT_SLOT).is(FastFurnaceV2.FAST_COAL.get())
                || boostedByBlockMenu.getBoostWork() != 0) {
            throw helper.assertionException("Block of Fast Coal did not retain its remaining boosted work");
        }

        BlockPos extremePos = new BlockPos(0, 12, 0);
        AcceleratorBlockEntity extreme = placeAccelerator(
                helper, extremePos, new ItemStack(Items.COAL, 19),
                new ItemStack(FastFurnaceV2.EXTREME_COAL.get()));
        AcceleratorMenu extremeMenu = createAcceleratorMenu(helper, extreme);
        tickAccelerator(helper, extreme, extremePos);
        if (!extremeMenu.isExtremeBoosted()
                || extremeMenu.getBoostWork() != AcceleratorBlockEntity.EXTREME_COAL_BOOST_WORK
                        - AcceleratorBlockEntity.EXTREME_COAL_BOOSTED_WORK_PER_TICK
                || AcceleratorBlockEntity.getBoostWorkPerTick(new ItemStack(FastFurnaceV2.EXTREME_COAL.get())) != 5) {
            throw helper.assertionException("Accelerator did not synchronize its 5x Extreme Coal boost");
        }
        tickAccelerator(helper, extreme, extremePos, 1438);
        if (extreme.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 17) {
            throw helper.assertionException("Extreme Coal completed its eighteenth coal conversion too early");
        }
        tickAccelerator(helper, extreme, extremePos);
        if (extreme.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 18
                || extreme.getItem(AcceleratorMenu.INPUT_SLOT).getCount() != 1
                || extremeMenu.getBoostWork() != 0 || extremeMenu.isExtremeBoosted()
                || !extreme.getItem(AcceleratorMenu.FUEL_SLOT).isEmpty()) {
            throw helper.assertionException("Extreme Coal did not boost exactly eighteen coal conversions");
        }
        tickAccelerator(helper, extreme, extremePos);
        if (extreme.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 18
                || extremeMenu.getWorkProgress() != 1) {
            throw helper.assertionException("Accelerator did not return to normal speed after Extreme Coal expired");
        }

        BlockPos extremeBlocksPos = new BlockPos(4, 12, 0);
        AcceleratorBlockEntity extremeBlocks = placeAccelerator(
                helper, extremeBlocksPos, new ItemStack(Items.COAL_BLOCK, 2),
                new ItemStack(FastFurnaceV2.EXTREME_COAL.get()));
        AcceleratorMenu extremeBlocksMenu = createAcceleratorMenu(helper, extremeBlocks);
        tickAccelerator(helper, extremeBlocks, extremeBlocksPos, 719);
        if (!extremeBlocks.getItem(AcceleratorMenu.RESULT_SLOT).isEmpty()) {
            throw helper.assertionException("Extreme Coal converted a coal block before 720 boosted ticks");
        }
        tickAccelerator(helper, extremeBlocks, extremeBlocksPos);
        if (extremeBlocks.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 1
                || extremeBlocksMenu.getBoostWork() != 3600) {
            throw helper.assertionException("Extreme Coal did not complete its first 720-tick coal-block conversion");
        }
        tickAccelerator(helper, extremeBlocks, extremeBlocksPos, 720);
        if (extremeBlocks.getItem(AcceleratorMenu.RESULT_SLOT).getCount() != 2
                || extremeBlocksMenu.getBoostWork() != 0) {
            throw helper.assertionException("Extreme Coal did not boost exactly two coal-block conversions");
        }
    }

    private static void apiSupportsCustomFuelsAndRecipes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        FastFuel customFuel = FastFurnaceApi.findFuel(API_FUEL_ID)
                .orElseThrow(() -> helper.assertionException("API fuel was not registered"));
        ItemStack blazePowder = new ItemStack(Items.BLAZE_POWDER);
        if (customFuel.scaleBurnTime(FastFurnaceTier.FAST, 800) != 1600
                || customFuel.getSpeedPercent(FastFurnaceTier.FAST) != 175
                || customFuel.acceleratorWork() != 800
                || customFuel.acceleratorWorkPerTick() != 4
                || blazePowder.getBurnTime(RecipeType.SMELTING, level.fuelValues()) != 800
                || FastFurnaceApi.findFuel(blazePowder).orElse(null) != customFuel
                || FastFurnaceApi.tiers().size() != 4
                || FastFurnaceApi.furnace(FastFurnaceTier.FAST) != FastFurnaceV2.FAST_FURNACE_ITEM.get()) {
            throw helper.assertionException("API fuel queries or ordinary burn-time registration were incorrect");
        }
        if (FastFurnaceApi.findCrusherRecipe(new ItemStack(Items.COBBLESTONE)).isPresent()
                || FastFurnaceApi.findCrusherRecipe(new ItemStack(Items.COBBLESTONE, 2))
                        .filter(recipe -> recipe.id().equals(API_CRUSHER_RECIPE_ID)).isEmpty()
                || FastFurnaceApi.findAcceleratorRecipe(new ItemStack(Items.CHARCOAL)).isPresent()
                || FastFurnaceApi.findAcceleratorRecipe(new ItemStack(Items.CHARCOAL, 2))
                        .filter(recipe -> recipe.id().equals(API_ACCELERATOR_RECIPE_ID)).isEmpty()) {
            throw helper.assertionException("API recipe lookup did not enforce input counts or registered IDs");
        }

        helper.setBlock(BlockPos.ZERO, FastFurnaceV2.FAST_CRUSHER.get());
        FastFurnaceBlockEntity crusher = helper.getBlockEntity(BlockPos.ZERO, FastFurnaceBlockEntity.class);
        crusher.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.COBBLESTONE, 3));
        crusher.setItem(FastFurnaceMenu.FUEL_SLOT, blazePowder.copy());
        FastFurnaceMenu crusherMenu = createMenu(helper, crusher);
        tickFurnace(helper, crusher, BlockPos.ZERO);
        if (crusherMenu.getTotalFuelTime() != 1600 || crusherMenu.getCurrentBurnTime() != 1600
                || crusherMenu.getSpeedPercent() != 175 || !crusherMenu.isFastCoalActive()
                || crusherMenu.isExtremeCoalActive()
                || crusher.getItem(FastFurnaceMenu.FUEL_SLOT).getCount() != 0) {
            throw helper.assertionException("Custom fuel did not synchronize its burn duration and +25 speed bonus");
        }
        level.getRandom().setSeed(findOutputRollSeed(false));
        for (int tick = 2; tick < 114; tick++) {
            tickFurnace(helper, crusher, BlockPos.ZERO);
            if (!crusher.getItem(FastFurnaceMenu.RESULT_SLOT).isEmpty()) {
                throw helper.assertionException("Custom crusher recipe completed before tick 114");
            }
        }
        tickFurnace(helper, crusher, BlockPos.ZERO);
        ItemStack crushed = crusher.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!crushed.is(Items.GRAVEL) || crushed.getCount() != 3
                || crusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).getCount() != 1) {
            throw helper.assertionException("Custom crusher recipe did not consume two inputs and produce three gravel");
        }

        BlockPos doubledPos = new BlockPos(2, 0, 0);
        helper.setBlock(doubledPos, FastFurnaceV2.FAST_CRUSHER.get());
        FastFurnaceBlockEntity doubledCrusher = helper.getBlockEntity(doubledPos, FastFurnaceBlockEntity.class);
        doubledCrusher.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.COBBLESTONE, 2));
        doubledCrusher.setItem(FastFurnaceMenu.FUEL_SLOT, blazePowder.copy());
        for (int tick = 1; tick < 114; tick++) {
            tickFurnace(helper, doubledCrusher, doubledPos);
        }
        level.getRandom().setSeed(findOutputRollSeed(true));
        tickFurnace(helper, doubledCrusher, doubledPos);
        ItemStack doubled = doubledCrusher.getItem(FastFurnaceMenu.RESULT_SLOT);
        if (!doubled.is(Items.GRAVEL) || doubled.getCount() != 6
                || !doubledCrusher.getItem(FastFurnaceMenu.INGREDIENT_SLOT).isEmpty()) {
            throw helper.assertionException("Crusher output doubling did not double the complete custom result");
        }

        BlockPos acceleratorPos = new BlockPos(0, 4, 0);
        AcceleratorBlockEntity accelerator = placeAccelerator(
                helper, acceleratorPos, new ItemStack(Items.CHARCOAL, 3), blazePowder.copy());
        AcceleratorMenu acceleratorMenu = createAcceleratorMenu(helper, accelerator);
        if (!accelerator.canPlaceItem(AcceleratorMenu.INPUT_SLOT, new ItemStack(Items.CHARCOAL))
                || !accelerator.canPlaceItem(AcceleratorMenu.FUEL_SLOT, blazePowder)
                || !accelerator.canPlaceItemThroughFace(
                        AcceleratorMenu.INPUT_SLOT, new ItemStack(Items.CHARCOAL), Direction.UP)
                || !acceleratorMenu.getSlot(AcceleratorMenu.FUEL_SLOT).mayPlace(blazePowder)) {
            throw helper.assertionException("API content was rejected by Accelerator slots or automation");
        }
        tickAccelerator(helper, accelerator, acceleratorPos, 49);
        if (!accelerator.getItem(AcceleratorMenu.RESULT_SLOT).isEmpty()
                || acceleratorMenu.getBoostWorkPerTick() != 4) {
            throw helper.assertionException("Custom Accelerator conversion completed before 50 boosted ticks");
        }
        tickAccelerator(helper, accelerator, acceleratorPos);
        ItemStack converted = accelerator.getItem(AcceleratorMenu.RESULT_SLOT);
        if (!converted.is(Items.COAL) || converted.getCount() != 2
                || accelerator.getItem(AcceleratorMenu.INPUT_SLOT).getCount() != 1
                || acceleratorMenu.getBoostWork() != 600
                || acceleratorMenu.getBoostWorkPerTick() != 4) {
            throw helper.assertionException("Custom Accelerator conversion or exact-work boost was incorrect");
        }
        helper.succeed();
    }

    private static void assertFastMaterialProcessing(GameTestHelper helper) {
        Item[] rawMaterials = {
            FastFurnaceV2.RAW_FAST_IRON.get(),
            FastFurnaceV2.RAW_FAST_GOLD.get(),
            FastFurnaceV2.RAW_FAST_DIAMOND.get()
        };
        Item[] results = {
            FastFurnaceV2.FAST_IRON_INGOT.get(),
            FastFurnaceV2.FAST_GOLD_INGOT.get(),
            FastFurnaceV2.FAST_DIAMOND.get()
        };
        for (int index = 0; index < rawMaterials.length; index++) {
            Item raw = rawMaterials[index];
            Identifier rawId = BuiltInRegistries.ITEM.getKey(raw);
            Item crushed = CrushedOres.forRawOre(rawId);
            if (crushed == null || !CrushedOres.isDiscoverableRawOre(rawId, raw)) {
                throw helper.assertionException("Built-in raw Fast material was not discovered: " + rawId);
            }
            CrusherRecipe crusherRecipe = FastFurnaceApi.findCrusherRecipe(raw.getDefaultInstance())
                    .orElseThrow(() -> helper.assertionException("No crusher recipe for " + rawId));
            ItemStack crusherOutput = crusherRecipe.output();
            if (!crusherOutput.is(crushed) || crusherOutput.getCount() != 2) {
                throw helper.assertionException("Crusher recipe did not produce two crushed results for " + rawId);
            }
            assertSmeltingResult(helper, raw, results[index]);
            assertSmeltingResult(helper, crushed, results[index]);
        }
    }

    private static void assertSmeltingResult(GameTestHelper helper, Item input, Item expectedResult) {
        ServerLevel level = helper.getLevel();
        SingleRecipeInput recipeInput = new SingleRecipeInput(input.getDefaultInstance());
        ItemStack result = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, recipeInput, level)
                .orElseThrow(() -> helper.assertionException("No smelting recipe for " + BuiltInRegistries.ITEM.getKey(input)))
                .value().assemble(recipeInput);
        if (!result.is(expectedResult) || result.getCount() != 1) {
            throw helper.assertionException("Incorrect smelting result for " + BuiltInRegistries.ITEM.getKey(input));
        }
    }

    private static void assertBuiltInOreCrusherRecipes(GameTestHelper helper) {
        assertOreCrusherRecipe(helper, FastFurnaceV2.EXTREME_COAL_ORE_ITEM.get(), FastFurnaceV2.EXTREME_COAL.get());
        assertOreCrusherRecipe(helper,
                FastFurnaceV2.DEEPSLATE_EXTREME_COAL_ORE_ITEM.get(), FastFurnaceV2.EXTREME_COAL.get());
        String[] materials = {"iron", "gold", "diamond"};
        Item[] regularOres = {
            FastFurnaceV2.FAST_IRON_ORE_ITEM.get(),
            FastFurnaceV2.FAST_GOLD_ORE_ITEM.get(),
            FastFurnaceV2.FAST_DIAMOND_ORE_ITEM.get()
        };
        Item[] deepslateOres = {
            FastFurnaceV2.DEEPSLATE_FAST_IRON_ORE_ITEM.get(),
            FastFurnaceV2.DEEPSLATE_FAST_GOLD_ORE_ITEM.get(),
            FastFurnaceV2.DEEPSLATE_FAST_DIAMOND_ORE_ITEM.get()
        };
        for (int index = 0; index < materials.length; index++) {
            Item crushed = CrushedOres.forRawOre(Identifier.fromNamespaceAndPath(
                    FastFurnaceV2.MODID, "raw_fast_" + materials[index]));
            if (crushed == null) {
                throw helper.assertionException("Missing crushed Fast material for " + materials[index]);
            }
            assertOreCrusherRecipe(helper, regularOres[index], crushed);
            assertOreCrusherRecipe(helper, deepslateOres[index], crushed);
        }
    }

    private static void assertOreCrusherRecipe(GameTestHelper helper, Item ore, Item expectedOutput) {
        CrusherRecipe recipe = FastFurnaceApi.findCrusherRecipe(ore.getDefaultInstance())
                .orElseThrow(() -> helper.assertionException("No ore-block crusher recipe for " + ore));
        if (!recipe.output().is(expectedOutput) || recipe.output().getCount() != 3) {
            throw helper.assertionException("Ore-block crusher recipe did not produce three matching outputs for " + ore);
        }
    }

    private static void assertAcceleratorConversion(
            GameTestHelper helper, Item input, Item expectedOutput, int expectedWork) {
        AcceleratorConversion conversion = FastFurnaceApi.findAcceleratorRecipe(input.getDefaultInstance())
                .orElseThrow(() -> helper.assertionException("No Accelerator conversion for " + input));
        ItemStack output = conversion.output();
        if (!output.is(expectedOutput) || output.getCount() != 1 || conversion.workUnits() != expectedWork) {
            throw helper.assertionException("Incorrect Accelerator conversion for " + input);
        }
    }

    private static void fastToolsApplyVanillaBonuses(GameTestHelper helper) {
        assertMiningTool(helper, Items.IRON_PICKAXE, FastFurnaceV2.FAST_IRON_PICKAXE.get(),
                Blocks.STONE, 1.20F, ItemTags.PICKAXES, FastTools.FAST_IRON.repairItems(),
                FastFurnaceV2.FAST_IRON_INGOT.get());
        assertMiningTool(helper, Items.IRON_SHOVEL, FastFurnaceV2.FAST_IRON_SHOVEL.get(),
                Blocks.DIRT, 1.20F, ItemTags.SHOVELS, FastTools.FAST_IRON.repairItems(),
                FastFurnaceV2.FAST_IRON_INGOT.get());
        assertMiningTool(helper, Items.IRON_AXE, FastFurnaceV2.FAST_IRON_AXE.get(),
                Blocks.OAK_LOG, 1.20F, ItemTags.AXES, FastTools.FAST_IRON.repairItems(),
                FastFurnaceV2.FAST_IRON_INGOT.get());
        assertMiningTool(helper, Items.GOLDEN_PICKAXE, FastFurnaceV2.FAST_GOLD_PICKAXE.get(),
                Blocks.STONE, 1.30F, ItemTags.PICKAXES, FastTools.FAST_GOLD.repairItems(),
                FastFurnaceV2.FAST_GOLD_INGOT.get());
        assertMiningTool(helper, Items.GOLDEN_SHOVEL, FastFurnaceV2.FAST_GOLD_SHOVEL.get(),
                Blocks.DIRT, 1.30F, ItemTags.SHOVELS, FastTools.FAST_GOLD.repairItems(),
                FastFurnaceV2.FAST_GOLD_INGOT.get());
        assertMiningTool(helper, Items.GOLDEN_AXE, FastFurnaceV2.FAST_GOLD_AXE.get(),
                Blocks.OAK_LOG, 1.30F, ItemTags.AXES, FastTools.FAST_GOLD.repairItems(),
                FastFurnaceV2.FAST_GOLD_INGOT.get());
        assertMiningTool(helper, Items.DIAMOND_PICKAXE, FastFurnaceV2.FAST_DIAMOND_PICKAXE.get(),
                Blocks.STONE, 1.20F, ItemTags.PICKAXES, FastTools.FAST_DIAMOND.repairItems(),
                FastFurnaceV2.FAST_DIAMOND.get());
        assertMiningTool(helper, Items.DIAMOND_SHOVEL, FastFurnaceV2.FAST_DIAMOND_SHOVEL.get(),
                Blocks.DIRT, 1.20F, ItemTags.SHOVELS, FastTools.FAST_DIAMOND.repairItems(),
                FastFurnaceV2.FAST_DIAMOND.get());
        assertMiningTool(helper, Items.DIAMOND_AXE, FastFurnaceV2.FAST_DIAMOND_AXE.get(),
                Blocks.OAK_LOG, 1.20F, ItemTags.AXES, FastTools.FAST_DIAMOND.repairItems(),
                FastFurnaceV2.FAST_DIAMOND.get());

        assertSword(helper, Items.IRON_SWORD, FastFurnaceV2.FAST_IRON_SWORD.get(),
                1.20F, 1.0F, FastTools.FAST_IRON.repairItems(), FastFurnaceV2.FAST_IRON_INGOT.get());
        assertSword(helper, Items.GOLDEN_SWORD, FastFurnaceV2.FAST_GOLD_SWORD.get(),
                1.30F, 1.0F, FastTools.FAST_GOLD.repairItems(), FastFurnaceV2.FAST_GOLD_INGOT.get());
        assertSword(helper, Items.DIAMOND_SWORD, FastFurnaceV2.FAST_DIAMOND_SWORD.get(),
                1.20F, 1.20F, FastTools.FAST_DIAMOND.repairItems(), FastFurnaceV2.FAST_DIAMOND.get());
        assertMiningTool(helper, Items.DIAMOND_PICKAXE, FastFurnaceV2.EXTREMELY_FAST_PICKAXE.get(),
                Blocks.STONE, 1.50F, ItemTags.PICKAXES, FastTools.EXTREMELY_FAST.repairItems(),
                FastFurnaceV2.EXTREMELY_FAST_INGOT.get(), 2.0F);
        assertMiningTool(helper, Items.DIAMOND_SHOVEL, FastFurnaceV2.EXTREMELY_FAST_SHOVEL.get(),
                Blocks.DIRT, 1.50F, ItemTags.SHOVELS, FastTools.EXTREMELY_FAST.repairItems(),
                FastFurnaceV2.EXTREMELY_FAST_INGOT.get(), 2.0F);
        assertMiningTool(helper, Items.DIAMOND_AXE, FastFurnaceV2.EXTREMELY_FAST_AXE.get(),
                Blocks.OAK_LOG, 1.50F, ItemTags.AXES, FastTools.EXTREMELY_FAST.repairItems(),
                FastFurnaceV2.EXTREMELY_FAST_INGOT.get(), 2.0F);
        assertSword(helper, Items.DIAMOND_SWORD, FastFurnaceV2.EXTREMELY_FAST_SWORD.get(),
                1.50F, 1.0F, FastTools.EXTREMELY_FAST.repairItems(),
                FastFurnaceV2.EXTREMELY_FAST_INGOT.get(), 2.0F);
        for (Item tool : List.of(
                FastFurnaceV2.EXTREMELY_FAST_PICKAXE.get(),
                FastFurnaceV2.EXTREMELY_FAST_SHOVEL.get(),
                FastFurnaceV2.EXTREMELY_FAST_AXE.get())) {
            if (tool.getDefaultInstance().getEnchantmentLevel(
                    helper.getLevel().registryAccess().getOrThrow(Enchantments.FORTUNE)) != 2) {
                throw helper.assertionException(tool + " is missing its built-in Fortune II");
            }
        }
        if (FastFurnaceV2.EXTREMELY_FAST_SWORD.get().getDefaultInstance().getEnchantmentLevel(
                helper.getLevel().registryAccess().getOrThrow(Enchantments.LOOTING)) != 2) {
            throw helper.assertionException("Extremely Fast Sword is missing its built-in Looting II");
        }

        Item[] diamondTools = {
            FastFurnaceV2.FAST_DIAMOND_PICKAXE.get(),
            FastFurnaceV2.FAST_DIAMOND_SHOVEL.get(),
            FastFurnaceV2.FAST_DIAMOND_AXE.get(),
            FastFurnaceV2.FAST_DIAMOND_SWORD.get()
        };
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        long savedSeed = findChanceRollSeed(FastTools.DURABILITY_SAVE_CHANCE, true);
        long damagedSeed = findChanceRollSeed(FastTools.DURABILITY_SAVE_CHANCE, false);
        for (Item tool : diamondTools) {
            helper.getLevel().getRandom().setSeed(savedSeed);
            int savedDamage = tool.damageItem(tool.getDefaultInstance(), 1, player, broken -> { });
            helper.getLevel().getRandom().setSeed(damagedSeed);
            int ordinaryDamage = tool.damageItem(tool.getDefaultInstance(), 1, player, broken -> { });
            if (savedDamage != 0 || ordinaryDamage != 1) {
                throw helper.assertionException(tool + " did not apply its deterministic 5% durability-saving roll");
            }
        }

        long doubledDropSeed = findChanceRollSeed(ExtremelyFastToolEffects.DROP_DOUBLE_CHANCE, true);
        long ordinaryDropSeed = findChanceRollSeed(ExtremelyFastToolEffects.DROP_DOUBLE_CHANCE, false);
        Item[] extremelyFastTools = {
            FastFurnaceV2.EXTREMELY_FAST_PICKAXE.get(),
            FastFurnaceV2.EXTREMELY_FAST_SHOVEL.get(),
            FastFurnaceV2.EXTREMELY_FAST_AXE.get()
        };
        Block[] brokenBlocks = {Blocks.STONE, Blocks.DIRT, Blocks.OAK_LOG};
        for (int index = 0; index < extremelyFastTools.length; index++) {
            player.setItemSlot(EquipmentSlot.MAINHAND, extremelyFastTools[index].getDefaultInstance());
            for (boolean doubled : new boolean[] {false, true}) {
                helper.getLevel().getRandom().setSeed(doubled ? doubledDropSeed : ordinaryDropSeed);
                List<ItemEntity> drops = new ArrayList<>();
                drops.add(new ItemEntity(helper.getLevel(), 0, 0, 0, new ItemStack(Items.COBBLESTONE, 3)));
                BlockDropsEvent event = new BlockDropsEvent(
                        helper.getLevel(), helper.absolutePos(BlockPos.ZERO), brokenBlocks[index].defaultBlockState(),
                        null, drops, player, extremelyFastTools[index].getDefaultInstance());
                ExtremelyFastToolEffects.onBlockDrops(event);
                if (drops.size() != (doubled ? 2 : 1)
                        || doubled && !ItemStack.isSameItemSameComponents(drops.get(0).getItem(), drops.get(1).getItem())) {
                    throw helper.assertionException(extremelyFastTools[index] + " did not apply its independent 5% drop roll");
                }
            }
        }

        player.setItemSlot(EquipmentSlot.MAINHAND, FastFurnaceV2.EXTREMELY_FAST_SWORD.get().getDefaultInstance());
        var damageSource = player.damageSources().playerAttack(player);
        for (boolean doubled : new boolean[] {false, true}) {
            helper.getLevel().getRandom().setSeed(doubled ? doubledDropSeed : ordinaryDropSeed);
            List<ItemEntity> drops = new ArrayList<>();
            drops.add(new ItemEntity(helper.getLevel(), 0, 0, 0, new ItemStack(Items.BONE, 2)));
            LivingDropsEvent event = new LivingDropsEvent(player, damageSource, drops, true);
            ExtremelyFastToolEffects.onLivingDrops(event);
            if (drops.size() != (doubled ? 2 : 1)) {
                throw helper.assertionException("Extremely Fast Sword did not apply its independent 5% kill-drop roll");
            }

            helper.getLevel().getRandom().setSeed(findChanceRollSeed(
                    ExtremelyFastToolEffects.DAMAGE_DOUBLE_CHANCE, doubled));
            LivingIncomingDamageEvent damageEvent = new LivingIncomingDamageEvent(
                    player, new DamageContainer(damageSource, 7.0F));
            ExtremelyFastToolEffects.onIncomingDamage(damageEvent);
            if (damageEvent.getAmount() != (doubled ? 14.0F : 7.0F)) {
                throw helper.assertionException("Extremely Fast Sword did not apply its independent 10% damage roll");
            }
        }
        helper.succeed();
    }

    private static void assertMiningTool(
            GameTestHelper helper,
            Item vanilla,
            Item fast,
            Block efficientBlock,
            float speedMultiplier,
            TagKey<Item> toolTag,
            TagKey<Item> repairTag,
            Item repairItem) {
        assertMiningTool(helper, vanilla, fast, efficientBlock, speedMultiplier, toolTag, repairTag, repairItem, 1.0F);
    }

    private static void assertMiningTool(
            GameTestHelper helper,
            Item vanilla,
            Item fast,
            Block efficientBlock,
            float speedMultiplier,
            TagKey<Item> toolTag,
            TagKey<Item> repairTag,
            Item repairItem,
            float durabilityMultiplier) {
        ItemStack vanillaStack = vanilla.getDefaultInstance();
        ItemStack fastStack = fast.getDefaultInstance();
        var vanillaTool = vanillaStack.get(DataComponents.TOOL);
        var fastTool = fastStack.get(DataComponents.TOOL);
        if (vanillaTool == null || fastTool == null) {
            throw helper.assertionException(fast + " is missing its vanilla-style tool component");
        }
        float vanillaSpeed = vanillaTool.getMiningSpeed(efficientBlock.defaultBlockState());
        float fastSpeed = fastTool.getMiningSpeed(efficientBlock.defaultBlockState());
        if (Math.abs(fastSpeed - vanillaSpeed * speedMultiplier) > 0.0001F
                || fastStack.getMaxDamage() != vanillaStack.getMaxDamage() * durabilityMultiplier
                || !fastStack.is(toolTag)
                || !repairItem.getDefaultInstance().is(repairTag)) {
            throw helper.assertionException(fast + " has incorrect mining speed, durability, tags, or repair material");
        }
    }

    private static void assertSword(
            GameTestHelper helper,
            Item vanilla,
            Item fast,
            float speedMultiplier,
            float damageMultiplier,
            TagKey<Item> repairTag,
            Item repairItem) {
        assertSword(helper, vanilla, fast, speedMultiplier, damageMultiplier, repairTag, repairItem, 1.0F);
    }

    private static void assertSword(
            GameTestHelper helper,
            Item vanilla,
            Item fast,
            float speedMultiplier,
            float damageMultiplier,
            TagKey<Item> repairTag,
            Item repairItem,
            float durabilityMultiplier) {
        ItemStack vanillaStack = vanilla.getDefaultInstance();
        ItemStack fastStack = fast.getDefaultInstance();
        var vanillaModifiers = vanillaStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        var fastModifiers = fastStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (vanillaModifiers == null || fastModifiers == null) {
            throw helper.assertionException(fast + " is missing its vanilla-style weapon attributes");
        }
        double vanillaSpeed = vanillaModifiers.compute(Attributes.ATTACK_SPEED, 4.0, EquipmentSlot.MAINHAND);
        double fastSpeed = fastModifiers.compute(Attributes.ATTACK_SPEED, 4.0, EquipmentSlot.MAINHAND);
        double vanillaDamage = vanillaModifiers.compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND);
        double fastDamage = fastModifiers.compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND);
        if (Math.abs(fastSpeed - vanillaSpeed * speedMultiplier) > 0.0001
                || Math.abs(fastDamage - vanillaDamage * damageMultiplier) > 0.0001
                || fastStack.getMaxDamage() != vanillaStack.getMaxDamage() * durabilityMultiplier
                || !fastStack.is(ItemTags.SWORDS)
                || !repairItem.getDefaultInstance().is(repairTag)) {
            throw helper.assertionException(fast + " has incorrect attack speed, damage, durability, tags, or repair material");
        }
    }

    private static void allToolRequiredBlocksArePickaxeMineable(GameTestHelper helper) {
        int registeredBlocks = 0;
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (!FastFurnaceV2.MODID.equals(id.getNamespace())) {
                continue;
            }
            registeredBlocks++;
            var state = entry.getValue().defaultBlockState();
            if (state.requiresCorrectToolForDrops() && !state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                throw helper.assertionException(id + " requires a tool but is not pickaxe-mineable");
            }
        }
        if (registeredBlocks != 24) {
            throw helper.assertionException("Expected to audit all 24 registered mod blocks, found " + registeredBlocks);
        }
        helper.succeed();
    }

    private static void fortuneIncreasesBonusDropChance(GameTestHelper helper) {
        float[] expectedChances = {0.10F, 0.15F, 0.20F, 0.25F};
        for (int fortuneLevel = 0; fortuneLevel < expectedChances.length; fortuneLevel++) {
            float chance = FastBonusDrops.getDropChance(fortuneLevel);
            if (Math.abs(chance - expectedChances[fortuneLevel]) > 0.0001F) {
                throw helper.assertionException(
                        "Unexpected bonus-drop chance at Fortune " + fortuneLevel + ": " + chance);
            }
        }
        if (FastBonusDrops.getDropChance(20) != 1.0F) {
            throw helper.assertionException("Nonstandard Fortune levels did not cap the bonus-drop chance at 100%");
        }
        helper.succeed();
    }

    private static ExtremelyFastAlloyerBlockEntity placeAlloyer(GameTestHelper helper, BlockPos relativePos) {
        return placeAlloyer(
                helper, relativePos,
                new ItemStack(FastFurnaceV2.FAST_IRON_INGOT.get()),
                new ItemStack(FastFurnaceV2.FAST_GOLD_INGOT.get()),
                new ItemStack(FastFurnaceV2.FAST_DIAMOND.get()));
    }

    private static ExtremelyFastAlloyerBlockEntity placeAlloyer(
            GameTestHelper helper, BlockPos relativePos, ItemStack first, ItemStack second, ItemStack third) {
        helper.setBlock(relativePos, FastFurnaceV2.EXTREMELY_FAST_ALLOYER.get());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.setBlock(relativePos.relative(direction), Blocks.STONE);
        }
        ExtremelyFastAlloyerBlockEntity alloyer =
                helper.getBlockEntity(relativePos, ExtremelyFastAlloyerBlockEntity.class);
        alloyer.setItem(0, first);
        alloyer.setItem(1, second);
        alloyer.setItem(2, third);
        alloyer.setItem(ExtremelyFastAlloyerMenu.FUEL_SLOT, new ItemStack(FastFurnaceV2.EXTREME_COAL.get()));
        return alloyer;
    }

    private static ExtremelyFastAlloyerMenu createAlloyerMenu(
            GameTestHelper helper, ExtremelyFastAlloyerBlockEntity alloyer) {
        return (ExtremelyFastAlloyerMenu) alloyer.createMenu(
                0, helper.makeMockPlayer(GameType.CREATIVE).getInventory());
    }

    private static void tickAlloyer(
            GameTestHelper helper, ExtremelyFastAlloyerBlockEntity alloyer, BlockPos relativePos) {
        BlockPos pos = helper.absolutePos(relativePos);
        ExtremelyFastAlloyerBlockEntity.serverTick(
                helper.getLevel(), pos, helper.getLevel().getBlockState(pos), alloyer);
    }

    private static void tickAlloyer(
            GameTestHelper helper, ExtremelyFastAlloyerBlockEntity alloyer, BlockPos relativePos, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            tickAlloyer(helper, alloyer, relativePos);
        }
    }

    private static AcceleratorBlockEntity placeAccelerator(
            GameTestHelper helper, BlockPos relativePos, ItemStack input, ItemStack fuel) {
        helper.setBlock(relativePos, FastFurnaceV2.ACCELERATOR.get());
        helper.setBlock(relativePos.above(), Blocks.STONE);
        helper.setBlock(relativePos.below(), Blocks.STONE);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.setBlock(relativePos.relative(direction), Blocks.LAVA);
        }
        AcceleratorBlockEntity accelerator = helper.getBlockEntity(relativePos, AcceleratorBlockEntity.class);
        accelerator.setItem(AcceleratorMenu.INPUT_SLOT, input);
        accelerator.setItem(AcceleratorMenu.FUEL_SLOT, fuel);
        return accelerator;
    }

    private static AcceleratorMenu createAcceleratorMenu(
            GameTestHelper helper, AcceleratorBlockEntity accelerator) {
        return (AcceleratorMenu) accelerator.createMenu(
                0, helper.makeMockPlayer(GameType.CREATIVE).getInventory());
    }

    private static void tickAccelerator(
            GameTestHelper helper, AcceleratorBlockEntity accelerator, BlockPos relativePos) {
        BlockPos pos = helper.absolutePos(relativePos);
        AcceleratorBlockEntity.serverTick(
                helper.getLevel(), pos, helper.getLevel().getBlockState(pos), accelerator);
    }

    private static void tickAccelerator(
            GameTestHelper helper, AcceleratorBlockEntity accelerator, BlockPos relativePos, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            tickAccelerator(helper, accelerator, relativePos);
        }
    }

    private static FastFurnaceBlockEntity placeCrusher(GameTestHelper helper, BlockPos relativePos) {
        helper.setBlock(relativePos, FastFurnaceV2.FAST_CRUSHER.get());
        FastFurnaceBlockEntity crusher = helper.getBlockEntity(relativePos, FastFurnaceBlockEntity.class);
        crusher.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.RAW_IRON));
        crusher.setItem(FastFurnaceMenu.FUEL_SLOT, new ItemStack(Items.COAL));
        return crusher;
    }

    private static ItemStack processableInput(FastFurnaceBlock block) {
        return new ItemStack(block.isFastCrusher() ? Items.RAW_IRON : Items.COBBLESTONE);
    }

    private static ItemStack alternateProcessableInput(FastFurnaceBlock block, ItemStack current) {
        if (block.isFastCrusher()) {
            return new ItemStack(current.is(Items.RAW_IRON) ? Items.RAW_COPPER : Items.RAW_IRON);
        }
        return new ItemStack(current.is(Items.COBBLESTONE) ? Items.STONE : Items.COBBLESTONE);
    }

    private static FastFurnaceBlockEntity placeFurnace(
            GameTestHelper helper, BlockPos relativePos, ItemStack fuel) {
        return placeFurnace(helper, relativePos, fuel, FastFurnaceTier.FAST);
    }

    private static FastFurnaceBlockEntity placeFurnace(
            GameTestHelper helper, BlockPos relativePos, ItemStack fuel, FastFurnaceTier tier) {
        return placeFurnace(helper, relativePos, fuel, FastFurnaceV2.getFurnaceBlock(tier).get());
    }

    private static FastFurnaceBlockEntity placeFurnace(
            GameTestHelper helper, BlockPos relativePos, ItemStack fuel, FastFurnaceBlock block) {
        helper.setBlock(relativePos, block);
        FastFurnaceBlockEntity furnace = helper.getBlockEntity(relativePos, FastFurnaceBlockEntity.class);
        furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, processableInput(block));
        furnace.setItem(FastFurnaceMenu.FUEL_SLOT, fuel);
        return furnace;
    }

    private static FastFurnaceMenu createMenu(GameTestHelper helper, FastFurnaceBlockEntity furnace) {
        return (FastFurnaceMenu) furnace.createMenu(
                0, helper.makeMockPlayer(GameType.CREATIVE).getInventory());
    }

    private static void assertBurnState(
            GameTestHelper helper,
            FastFurnaceMenu menu,
            int expectedRemaining,
            int expectedTotal,
            boolean expectedFastCoal,
            boolean expectedExtremeCoal,
            String context) {
        if (menu.getCurrentBurnTime() != expectedRemaining
                || menu.getTotalFuelTime() != expectedTotal
                || menu.isFastCoalActive() != expectedFastCoal
                || menu.isExtremeCoalActive() != expectedExtremeCoal) {
            throw helper.assertionException(context + ": remaining=" + menu.getCurrentBurnTime()
                    + ", total=" + menu.getTotalFuelTime() + ", Fast Coal=" + menu.isFastCoalActive()
                    + ", Extreme Coal=" + menu.isExtremeCoalActive());
        }
    }

    private static long findFuelRollSeed(boolean doubled) {
        return findChanceRollSeed(FastFurnaceTier.FAST.getDoubleBurnChance(), doubled);
    }

    private static long findOutputRollSeed(boolean doubled) {
        return findChanceRollSeed(FastFurnaceTier.FAST.getDoubleBurnChance(), doubled);
    }

    private static long findTierOutputRollSeed(FastFurnaceTier tier) {
        double previousTierChance = tier == FastFurnaceTier.FAST
                ? 0.0
                : FastFurnaceTier.values()[tier.ordinal() - 1].getDoubleBurnChance();
        for (long seed = 0; ; seed++) {
            float roll = RandomSource.create(seed).nextFloat();
            if (roll >= previousTierChance && roll < tier.getDoubleBurnChance()) {
                return seed;
            }
        }
    }

    private static long findChanceRollSeed(double chance, boolean success) {
        for (long seed = 0; ; seed++) {
            boolean rollSucceeds = RandomSource.create(seed).nextFloat() < chance;
            if (rollSucceeds == success) {
                return seed;
            }
        }
    }

    private static void tickFurnace(
            GameTestHelper helper, FastFurnaceBlockEntity furnace, BlockPos relativePos) {
        BlockPos pos = helper.absolutePos(relativePos);
        FastFurnaceBlockEntity.serverTick(
                helper.getLevel(), pos, helper.getLevel().getBlockState(pos), furnace);
    }

    static final class DirectGameTestInstance extends GameTestInstance {
        static final MapCodec<DirectGameTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceKey.codec(Registries.TEST_FUNCTION).fieldOf("function").forGetter(test -> test.function),
                TestData.CODEC.forGetter(test -> test.info()))
                .apply(instance, DirectGameTestInstance::new));

        private final ResourceKey<Consumer<GameTestHelper>> function;

        private DirectGameTestInstance(
                ResourceKey<Consumer<GameTestHelper>> function,
                TestData<Holder<TestEnvironmentDefinition<?>>> testData) {
            super(testData);
            this.function = function;
        }

        @Override
        public void run(GameTestHelper helper) {
            Consumer<GameTestHelper> function = FUNCTIONS.get(this.function);
            if (function == null) {
                throw helper.assertionException("Unknown test function: " + this.function.identifier());
            }
            function.accept(helper);
        }

        @Override
        public MapCodec<DirectGameTestInstance> codec() {
            return CODEC;
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.translatable("test_instance.type.function");
        }
    }
}
