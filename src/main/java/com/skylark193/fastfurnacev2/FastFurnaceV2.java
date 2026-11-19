package com.skylark193.fastfurnacev2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(FastFurnaceV2.MODID)
public class FastFurnaceV2 {
    public static final String MODID = "fastfurnacev2";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<MapCodec<? extends GameTestInstance>> GAME_TEST_INSTANCE_TYPES =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Matches vanilla cobblestone's block properties, including hardness, blast resistance, and tool requirement.
    public static final DeferredBlock<Block> FAST_COBBLESTONE = BLOCKS.registerSimpleBlock("fast_cobblestone", properties -> properties
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(2.0F, 6.0F));
    public static final DeferredItem<BlockItem> FAST_COBBLESTONE_ITEM = ITEMS.registerSimpleBlockItem("fast_cobblestone", FAST_COBBLESTONE);
    public static final DeferredItem<Item> FAST_COAL = ITEMS.registerSimpleItem("fast_coal");
    // Matches vanilla coal blocks without copying their loot-table key or description ID.
    public static final DeferredBlock<Block> FAST_COAL_BLOCK = BLOCKS.registerSimpleBlock("fast_coal_block", properties -> properties
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 6.0F));
    public static final DeferredItem<BlockItem> FAST_COAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("fast_coal_block", FAST_COAL_BLOCK);

    public static final DeferredBlock<FastFurnaceBlock> FAST_FURNACE = registerFurnace(FastFurnaceTier.FAST);
    public static final DeferredBlock<FastFurnaceBlock> IRON_FAST_FURNACE = registerFurnace(FastFurnaceTier.IRON);
    public static final DeferredBlock<FastFurnaceBlock> GOLD_FAST_FURNACE = registerFurnace(FastFurnaceTier.GOLD);
    public static final DeferredBlock<FastFurnaceBlock> DIAMOND_FAST_FURNACE = registerFurnace(FastFurnaceTier.DIAMOND);
    public static final DeferredItem<BlockItem> FAST_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("fast_furnace", FAST_FURNACE);
    public static final DeferredItem<BlockItem> IRON_FAST_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("iron_fast_furnace", IRON_FAST_FURNACE);
    public static final DeferredItem<BlockItem> GOLD_FAST_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("gold_fast_furnace", GOLD_FAST_FURNACE);
    public static final DeferredItem<BlockItem> DIAMOND_FAST_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("diamond_fast_furnace", DIAMOND_FAST_FURNACE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FastFurnaceBlockEntity>> FAST_FURNACE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "fast_furnace",
            () -> new BlockEntityType<>(
                    FastFurnaceBlockEntity::new,
                    FAST_FURNACE.get(),
                    IRON_FAST_FURNACE.get(),
                    GOLD_FAST_FURNACE.get(),
                    DIAMOND_FAST_FURNACE.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> FAST_FURNACE_MENU = registerFurnaceMenu(FastFurnaceTier.FAST);
    public static final DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> IRON_FAST_FURNACE_MENU = registerFurnaceMenu(FastFurnaceTier.IRON);
    public static final DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> GOLD_FAST_FURNACE_MENU = registerFurnaceMenu(FastFurnaceTier.GOLD);
    public static final DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> DIAMOND_FAST_FURNACE_MENU = registerFurnaceMenu(FastFurnaceTier.DIAMOND);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FAST_FURNACE_V2_TAB = CREATIVE_MODE_TABS.register("fast_furnace_v2_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.fastfurnacev2"))
            .icon(() -> FAST_COBBLESTONE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FAST_COBBLESTONE_ITEM.get());
                output.accept(FAST_COAL.get());
                output.accept(FAST_COAL_BLOCK_ITEM.get());
                for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                    output.accept(getFurnaceItem(tier).get());
                }
                CrushedOres.addCreative(output);
            })
            .build());

    public static final DeferredHolder<MapCodec<? extends GameTestInstance>, MapCodec<FastFurnaceGameTests.DirectGameTestInstance>>
            GAME_TEST_INSTANCE = GAME_TEST_INSTANCE_TYPES.register(
                    "function", () -> FastFurnaceGameTests.DirectGameTestInstance.CODEC);

    public FastFurnaceV2(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        GAME_TEST_INSTANCE_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(CrushedOres::registerDiscovery);
        modEventBus.addListener(CrushedOres::registerItems);
        modEventBus.addListener(CrushedOreResources::addPackFinder);
        modEventBus.addListener(FastFurnaceGameTests::registerTests);
        NeoForge.EVENT_BUS.addListener(FastBonusDrops::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(CrushedOres::generateRecipes);
    }

    public static boolean isFastCoalFuel(ItemStack stack) {
        return stack.is(FAST_COAL.get()) || stack.is(FAST_COAL_BLOCK_ITEM.get());
    }

    public static DeferredBlock<FastFurnaceBlock> getFurnaceBlock(FastFurnaceTier tier) {
        return switch (tier) {
            case FAST -> FAST_FURNACE;
            case IRON -> IRON_FAST_FURNACE;
            case GOLD -> GOLD_FAST_FURNACE;
            case DIAMOND -> DIAMOND_FAST_FURNACE;
        };
    }

    public static DeferredItem<BlockItem> getFurnaceItem(FastFurnaceTier tier) {
        return switch (tier) {
            case FAST -> FAST_FURNACE_ITEM;
            case IRON -> IRON_FAST_FURNACE_ITEM;
            case GOLD -> GOLD_FAST_FURNACE_ITEM;
            case DIAMOND -> DIAMOND_FAST_FURNACE_ITEM;
        };
    }

    public static DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> getFurnaceMenu(FastFurnaceTier tier) {
        return switch (tier) {
            case FAST -> FAST_FURNACE_MENU;
            case IRON -> IRON_FAST_FURNACE_MENU;
            case GOLD -> GOLD_FAST_FURNACE_MENU;
            case DIAMOND -> DIAMOND_FAST_FURNACE_MENU;
        };
    }

    private static DeferredBlock<FastFurnaceBlock> registerFurnace(FastFurnaceTier tier) {
        return BLOCKS.registerBlock(tier.getId(), properties -> new FastFurnaceBlock(properties, tier), properties -> properties
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(3.5F)
                .lightLevel(state -> state.getValue(FastFurnaceBlock.LIT) ? 13 : 0));
    }

    private static DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> registerFurnaceMenu(FastFurnaceTier tier) {
        return MENUS.register(tier.getId(), () -> new MenuType<>(
                (containerId, inventory) -> new FastFurnaceMenu(containerId, inventory, tier),
                FeatureFlags.VANILLA_SET));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(FAST_COBBLESTONE_ITEM);
            event.accept(FAST_COAL_BLOCK_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(FAST_COAL);
            CrushedOres.addCreative(event);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                event.accept(getFurnaceItem(tier));
            }
        }
    }
}
