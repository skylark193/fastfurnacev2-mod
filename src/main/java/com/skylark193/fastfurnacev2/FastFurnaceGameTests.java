package com.skylark193.fastfurnacev2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

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
    private static final Map<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> FUNCTIONS = Map.of(
            SPEED_FUNCTION, FastFurnaceGameTests::smeltsAt150Percent,
            FUEL_FUNCTION, FastFurnaceGameTests::rollsAndSynchronizesFuelTime,
            FAST_COAL_FUNCTION, FastFurnaceGameTests::fastCoalAcceleratesAndExtendsBurnTime,
            FAST_COAL_BLOCK_FUNCTION, FastFurnaceGameTests::fastCoalBlockActsAsFastCoalFuel,
            BONUS_DROP_FUNCTION, FastFurnaceGameTests::fortuneIncreasesBonusDropChance,
            TIER_FUNCTION, FastFurnaceGameTests::furnaceTiersScaleSpeedAndFuel,
            CRUSHED_ORE_FUNCTION, FastFurnaceGameTests::crushedOresMatchRawOreSmelting,
            FAST_CRUSHER_FUNCTION, FastFurnaceGameTests::fastCrusherCrushesAndDoublesOutput,
            FUEL_PAUSE_FUNCTION, FastFurnaceGameTests::pausesFuelWithoutProcessableWork);

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
            if (tick == 133) {
                level.getRandom().setSeed(findOutputRollSeed(true));
            }
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
        CrushedOreItem crushedIron = CrushedOres.forRawOre(Identifier.withDefaultNamespace("raw_iron"));
        if (crushedIron == null) {
            throw helper.assertionException("Raw iron did not have a crushed variant");
        }

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
        helper.succeed();
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
                FastFurnaceV2.FAST_CRUSHER.get()
        };
        for (boolean fastCoal : new boolean[] {false, true}) {
            for (int index = 0; index < blocks.length; index++) {
                FastFurnaceBlock block = blocks[index];
                String description = BuiltInRegistries.BLOCK.getKey(block)
                        + (fastCoal ? " with Fast Coal" : " with coal");
                BlockPos relativePos = new BlockPos(index * 2, fastCoal ? 8 : 4, 0);
                ItemStack fuel = fastCoal
                        ? new ItemStack(FastFurnaceV2.FAST_COAL.get())
                        : new ItemStack(Items.COAL);
                FastFurnaceBlockEntity furnace = placeFurnace(helper, relativePos, fuel, block);
                FastFurnaceMenu menu = createMenu(helper, furnace);
                int expectedBurnTime = fastCoal
                        ? block.getTier().scaleFastCoalBurnTime(FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME)
                        : FastFurnaceBlockEntity.VANILLA_COAL_BURN_TIME;
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
                assertBurnState(helper, menu, expectedBurnTime, expectedBurnTime, fastCoal,
                        description + " did not start with the correct fuel state");
                tickFurnace(helper, furnace, relativePos);
                assertBurnState(helper, menu, expectedBurnTime - 1, expectedBurnTime, fastCoal,
                        description + " did not count down during valid work");
                if (!furnace.getItem(FastFurnaceMenu.FUEL_SLOT).isEmpty()) {
                    throw helper.assertionException(description + " did not consume exactly one fuel item");
                }

                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, ItemStack.EMPTY);
                for (int tick = 0; tick < 20; tick++) {
                    tickFurnace(helper, furnace, relativePos);
                }
                assertBurnState(helper, menu, expectedBurnTime - 1, expectedBurnTime, fastCoal,
                        description + " wasted fuel with empty input");

                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.DIRT));
                for (int tick = 0; tick < 5; tick++) {
                    tickFurnace(helper, furnace, relativePos);
                }
                assertBurnState(helper, menu, expectedBurnTime - 1, expectedBurnTime, fastCoal,
                        description + " wasted fuel with unsmeltable input");

                furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, processableInput(block));
                furnace.setItem(FastFurnaceMenu.RESULT_SLOT, new ItemStack(Items.DIRT));
                for (int tick = 0; tick < 5; tick++) {
                    tickFurnace(helper, furnace, relativePos);
                }
                assertBurnState(helper, menu, expectedBurnTime - 1, expectedBurnTime, fastCoal,
                        description + " wasted fuel with blocked output");

                furnace.setItem(FastFurnaceMenu.RESULT_SLOT, ItemStack.EMPTY);
                tickFurnace(helper, furnace, relativePos);
                assertBurnState(helper, menu, expectedBurnTime - 2, expectedBurnTime, fastCoal,
                        description + " did not resume its paused fuel countdown");
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

    private static void crushedOresMatchRawOreSmelting(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CrushedOreItem crushedIron = CrushedOres.forRawOre(Identifier.withDefaultNamespace("raw_iron"));
        CrushedOreItem crushedCopper = CrushedOres.forRawOre(Identifier.withDefaultNamespace("raw_copper"));
        CrushedOreItem crushedGold = CrushedOres.forRawOre(Identifier.withDefaultNamespace("raw_gold"));
        if (crushedIron == null || crushedCopper == null || crushedGold == null) {
            throw helper.assertionException("Vanilla raw iron, copper, and gold were not all discovered");
        }
        if (!crushedIron.getDefaultInstance().getHoverName().getString().equals("Crushed Iron")) {
            throw helper.assertionException("Crushed iron did not receive its material-specific display name");
        }

        for (CrushedOreItem crushed : CrushedOres.all()) {
            Item raw = BuiltInRegistries.ITEM.getValue(crushed.getRawOreId());
            SingleRecipeInput rawInput = new SingleRecipeInput(raw.getDefaultInstance());
            SingleRecipeInput crushedInput = new SingleRecipeInput(crushed.getDefaultInstance());
            RecipeHolder<? extends Recipe<SingleRecipeInput>> rawRecipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, rawInput, level)
                    .orElseThrow(() -> helper.assertionException("No raw smelting recipe for " + crushed.getRawOreId()));
            RecipeHolder<? extends Recipe<SingleRecipeInput>> crushedRecipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, crushedInput, level)
                    .orElseThrow(() -> helper.assertionException("No crushed smelting recipe for " + BuiltInRegistries.ITEM.getKey(crushed)));
            ItemStack rawResult = rawRecipe.value().assemble(rawInput);
            ItemStack crushedResult = crushedRecipe.value().assemble(crushedInput);
            if (!ItemStack.isSameItemSameComponents(rawResult, crushedResult)
                    || rawResult.getCount() != crushedResult.getCount()) {
                throw helper.assertionException("Crushed result differs from raw result for " + crushed.getRawOreId());
            }
            if (rawRecipe.value() instanceof AbstractCookingRecipe rawCooking
                    && crushedRecipe.value() instanceof AbstractCookingRecipe crushedCooking
                    && (rawCooking.cookingTime() != crushedCooking.cookingTime()
                    || rawCooking.experience() != crushedCooking.experience()
                    || rawCooking.category() != crushedCooking.category()
                    || !rawCooking.group().equals(crushedCooking.group()))) {
                throw helper.assertionException("Crushed recipe settings differ from raw recipe for " + crushed.getRawOreId());
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

    private static FastFurnaceBlockEntity placeCrusher(GameTestHelper helper, BlockPos relativePos) {
        helper.setBlock(relativePos, FastFurnaceV2.FAST_CRUSHER.get());
        FastFurnaceBlockEntity crusher = helper.getBlockEntity(relativePos, FastFurnaceBlockEntity.class);
        crusher.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.RAW_IRON));
        crusher.setItem(FastFurnaceMenu.FUEL_SLOT, new ItemStack(Items.COAL));
        return crusher;
    }

    private static ItemStack processableInput(FastFurnaceBlock block) {
        return new ItemStack(block == FastFurnaceV2.FAST_CRUSHER.get() ? Items.RAW_IRON : Items.COBBLESTONE);
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
            String context) {
        if (menu.getCurrentBurnTime() != expectedRemaining
                || menu.getTotalFuelTime() != expectedTotal
                || menu.isFastCoalActive() != expectedFastCoal) {
            throw helper.assertionException(context + ": remaining=" + menu.getCurrentBurnTime()
                    + ", total=" + menu.getTotalFuelTime() + ", Fast Coal=" + menu.isFastCoalActive());
        }
    }

    private static long findFuelRollSeed(boolean doubled) {
        return findChanceRollSeed(FastFurnaceTier.FAST.getDoubleBurnChance(), doubled);
    }

    private static long findOutputRollSeed(boolean doubled) {
        return findChanceRollSeed(FastFurnaceBlockEntity.FAST_CRUSHER_DOUBLE_OUTPUT_CHANCE_PERCENT / 100.0, doubled);
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
