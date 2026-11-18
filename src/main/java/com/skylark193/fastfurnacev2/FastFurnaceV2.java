package com.skylark193.fastfurnacev2;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Matches vanilla cobblestone's block properties, including hardness, blast resistance, and tool requirement.
    public static final DeferredBlock<Block> FAST_COBBLESTONE = BLOCKS.registerSimpleBlock("fast_cobblestone", properties -> properties
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(2.0F, 6.0F));
    public static final DeferredItem<BlockItem> FAST_COBBLESTONE_ITEM = ITEMS.registerSimpleBlockItem("fast_cobblestone", FAST_COBBLESTONE);

    // Matches the vanilla furnace, including light level 13 while lit.
    public static final DeferredBlock<FastFurnaceBlock> FAST_FURNACE = BLOCKS.registerBlock("fast_furnace", FastFurnaceBlock::new, properties -> properties
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(3.5F)
            .lightLevel(state -> state.getValue(FastFurnaceBlock.LIT) ? 13 : 0));
    public static final DeferredItem<BlockItem> FAST_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("fast_furnace", FAST_FURNACE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FastFurnaceBlockEntity>> FAST_FURNACE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "fast_furnace",
            () -> new BlockEntityType<>(FastFurnaceBlockEntity::new, FAST_FURNACE.get()));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FAST_FURNACE_V2_TAB = CREATIVE_MODE_TABS.register("fast_furnace_v2_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.fastfurnacev2"))
            .icon(() -> FAST_COBBLESTONE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FAST_COBBLESTONE_ITEM.get());
                output.accept(FAST_FURNACE_ITEM.get());
            })
            .build());

    public FastFurnaceV2(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        NeoForge.EVENT_BUS.addListener(FastCobblestoneDrops::onBlockDrops);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(FAST_COBBLESTONE_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(FAST_FURNACE_ITEM);
        }
    }
}
