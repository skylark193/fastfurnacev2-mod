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
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

final class FastFurnaceGameTests {
    private static final Identifier SPEED_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_furnace_speed");
    private static final Identifier FUEL_TEST_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_furnace_fuel");
    private static final ResourceKey<Consumer<GameTestHelper>> SPEED_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, SPEED_TEST_ID);
    private static final ResourceKey<Consumer<GameTestHelper>> FUEL_FUNCTION =
            ResourceKey.create(Registries.TEST_FUNCTION, FUEL_TEST_ID);
    private static final Map<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> FUNCTIONS = Map.of(
            SPEED_FUNCTION, FastFurnaceGameTests::smeltsAt150Percent,
            FUEL_FUNCTION, FastFurnaceGameTests::rollsAndSynchronizesFuelTime);

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

    private static FastFurnaceBlockEntity placeFurnace(
            GameTestHelper helper, BlockPos relativePos, ItemStack fuel) {
        helper.setBlock(relativePos, FastFurnaceV2.FAST_FURNACE.get());
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
        for (long seed = 0; ; seed++) {
            boolean rollDoubles = RandomSource.create(seed).nextFloat()
                    < FastFurnaceBlockEntity.DOUBLE_BURN_CHANCE_PERCENT / 100.0F;
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
