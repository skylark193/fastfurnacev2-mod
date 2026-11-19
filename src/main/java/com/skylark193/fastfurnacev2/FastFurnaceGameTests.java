package com.skylark193.fastfurnacev2;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
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
    private static final Map<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> FUNCTIONS = Map.of(
            SPEED_FUNCTION, FastFurnaceGameTests::smeltsAt150Percent,
            FUEL_FUNCTION, FastFurnaceGameTests::rollsAndSynchronizesFuelTime,
            FAST_COAL_FUNCTION, FastFurnaceGameTests::fastCoalAcceleratesAndExtendsBurnTime,
            FAST_COAL_BLOCK_FUNCTION, FastFurnaceGameTests::fastCoalBlockActsAsFastCoalFuel,
            BONUS_DROP_FUNCTION, FastFurnaceGameTests::fortuneIncreasesBonusDropChance,
            TIER_FUNCTION, FastFurnaceGameTests::furnaceTiersScaleSpeedAndFuel);

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
    }

    private static void smeltsAt150Percent(GameTestHelper helper) {
        BlockPos relativePos = BlockPos.ZERO;
        BlockPos pos = helper.absolutePos(relativePos);
        ServerLevel level = helper.getLevel();
        helper.setBlock(relativePos, FastFurnaceV2.FAST_FURNACE.get());
        FastFurnaceBlockEntity furnace = helper.getBlockEntity(relativePos, FastFurnaceBlockEntity.class);
        furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.COBBLESTONE));
        furnace.setItem(FastFurnaceMenu.FUEL_SLOT, new ItemStack(Items.COAL));

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
            level.getRandom().setSeed(findFuelRollSeed(tier.getDoubleBurnChance(), false));
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
            level.getRandom().setSeed(findFuelRollSeed(tier.getDoubleBurnChance(), true));
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
                level.getRandom().setSeed(findFuelRollSeed(tier.getDoubleBurnChance(), false));
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
                level.getRandom().setSeed(findFuelRollSeed(tier.getDoubleBurnChance(), true));
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

    private static FastFurnaceBlockEntity placeFurnace(
            GameTestHelper helper, BlockPos relativePos, ItemStack fuel) {
        return placeFurnace(helper, relativePos, fuel, FastFurnaceTier.FAST);
    }

    private static FastFurnaceBlockEntity placeFurnace(
            GameTestHelper helper, BlockPos relativePos, ItemStack fuel, FastFurnaceTier tier) {
        helper.setBlock(relativePos, FastFurnaceV2.getFurnaceBlock(tier).get());
        FastFurnaceBlockEntity furnace = helper.getBlockEntity(relativePos, FastFurnaceBlockEntity.class);
        furnace.setItem(FastFurnaceMenu.INGREDIENT_SLOT, new ItemStack(Items.COBBLESTONE));
        furnace.setItem(FastFurnaceMenu.FUEL_SLOT, fuel);
        return furnace;
    }

    private static FastFurnaceMenu createMenu(GameTestHelper helper, FastFurnaceBlockEntity furnace) {
        return (FastFurnaceMenu) furnace.createMenu(
                0, helper.makeMockPlayer(GameType.CREATIVE).getInventory());
    }

    private static long findFuelRollSeed(boolean doubled) {
        return findFuelRollSeed(FastFurnaceTier.FAST.getDoubleBurnChance(), doubled);
    }

    private static long findFuelRollSeed(double doubleBurnChance, boolean doubled) {
        for (long seed = 0; ; seed++) {
            boolean rollDoubles = RandomSource.create(seed).nextFloat() < doubleBurnChance;
            if (rollDoubles == doubled) {
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
