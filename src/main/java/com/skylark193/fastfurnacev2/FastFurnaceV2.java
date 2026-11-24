package com.skylark193.fastfurnacev2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.skylark193.fastfurnacev2.internal.FastFurnaceApiImpl;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.DefaultDataComponentsBoundEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

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
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);

    // Matches vanilla cobblestone's block properties, including hardness, blast resistance, and tool requirement.
    public static final DeferredBlock<Block> FAST_COBBLESTONE = BLOCKS.registerSimpleBlock("fast_cobblestone", properties -> properties
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(2.0F, 6.0F));
    public static final DeferredItem<BlockItem> FAST_COBBLESTONE_ITEM = ITEMS.registerSimpleBlockItem("fast_cobblestone", FAST_COBBLESTONE);
    public static final DeferredItem<Item> FAST_COAL = ITEMS.registerSimpleItem("fast_coal");
    // Match each vanilla ore or storage block without copying its loot-table key or description ID.
    public static final DeferredBlock<Block> FAST_IRON_ORE = registerOre("fast_iron_ore");
    public static final DeferredItem<BlockItem> FAST_IRON_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("fast_iron_ore", FAST_IRON_ORE);
    public static final DeferredBlock<Block> DEEPSLATE_FAST_IRON_ORE = registerDeepslateOre("deepslate_fast_iron_ore");
    public static final DeferredItem<BlockItem> DEEPSLATE_FAST_IRON_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("deepslate_fast_iron_ore", DEEPSLATE_FAST_IRON_ORE);
    public static final DeferredItem<Item> RAW_FAST_IRON = ITEMS.registerSimpleItem("raw_fast_iron");
    public static final DeferredItem<Item> FAST_IRON_INGOT = ITEMS.registerSimpleItem("fast_iron_ingot");
    public static final DeferredBlock<Block> FAST_IRON_BLOCK = BLOCKS.registerSimpleBlock(
            "fast_iron_block", properties -> properties
                    .mapColor(MapColor.METAL)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .sound(SoundType.IRON)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F));
    public static final DeferredItem<BlockItem> FAST_IRON_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("fast_iron_block", FAST_IRON_BLOCK);
    public static final DeferredBlock<Block> FAST_GOLD_ORE = registerOre("fast_gold_ore");
    public static final DeferredItem<BlockItem> FAST_GOLD_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("fast_gold_ore", FAST_GOLD_ORE);
    public static final DeferredBlock<Block> DEEPSLATE_FAST_GOLD_ORE = registerDeepslateOre("deepslate_fast_gold_ore");
    public static final DeferredItem<BlockItem> DEEPSLATE_FAST_GOLD_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("deepslate_fast_gold_ore", DEEPSLATE_FAST_GOLD_ORE);
    public static final DeferredItem<Item> RAW_FAST_GOLD = ITEMS.registerSimpleItem("raw_fast_gold");
    public static final DeferredItem<Item> FAST_GOLD_INGOT = ITEMS.registerSimpleItem("fast_gold_ingot");
    public static final DeferredBlock<Block> FAST_GOLD_BLOCK = BLOCKS.registerSimpleBlock(
            "fast_gold_block", properties -> properties
                    .mapColor(MapColor.GOLD)
                    .instrument(NoteBlockInstrument.BELL)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F));
    public static final DeferredItem<BlockItem> FAST_GOLD_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("fast_gold_block", FAST_GOLD_BLOCK);
    public static final DeferredBlock<Block> FAST_DIAMOND_ORE = registerOre("fast_diamond_ore");
    public static final DeferredItem<BlockItem> FAST_DIAMOND_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("fast_diamond_ore", FAST_DIAMOND_ORE);
    public static final DeferredBlock<Block> DEEPSLATE_FAST_DIAMOND_ORE = registerDeepslateOre("deepslate_fast_diamond_ore");
    public static final DeferredItem<BlockItem> DEEPSLATE_FAST_DIAMOND_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("deepslate_fast_diamond_ore", DEEPSLATE_FAST_DIAMOND_ORE);
    public static final DeferredItem<Item> RAW_FAST_DIAMOND = ITEMS.registerSimpleItem("raw_fast_diamond");
    public static final DeferredItem<Item> FAST_DIAMOND = ITEMS.registerSimpleItem("fast_diamond");
    public static final DeferredItem<Item> EXTREMELY_FAST_INGOT = ITEMS.registerSimpleItem("extremely_fast_ingot");
    public static final DeferredBlock<Block> FAST_DIAMOND_BLOCK = BLOCKS.registerSimpleBlock(
            "fast_diamond_block", properties -> properties
                    .mapColor(MapColor.DIAMOND)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F));
    public static final DeferredItem<BlockItem> FAST_DIAMOND_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("fast_diamond_block", FAST_DIAMOND_BLOCK);
    // Matches vanilla coal ore without copying its loot-table key or description ID.
    public static final DeferredBlock<Block> EXTREME_COAL_ORE = BLOCKS.registerSimpleBlock(
            "extreme_coal_ore", properties -> properties
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 3.0F));
    public static final DeferredItem<BlockItem> EXTREME_COAL_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("extreme_coal_ore", EXTREME_COAL_ORE);
    public static final DeferredBlock<Block> DEEPSLATE_EXTREME_COAL_ORE = registerDeepslateOre("deepslate_extreme_coal_ore");
    public static final DeferredItem<BlockItem> DEEPSLATE_EXTREME_COAL_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("deepslate_extreme_coal_ore", DEEPSLATE_EXTREME_COAL_ORE);
    public static final DeferredItem<Item> EXTREME_COAL = ITEMS.registerSimpleItem("extreme_coal");
    // Matches vanilla coal blocks without copying their loot-table key or description ID.
    public static final DeferredBlock<Block> FAST_COAL_BLOCK = BLOCKS.registerSimpleBlock("fast_coal_block", properties -> properties
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 6.0F));
    public static final DeferredItem<BlockItem> FAST_COAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("fast_coal_block", FAST_COAL_BLOCK);
    public static final DeferredBlock<Block> EXTREME_COAL_BLOCK = BLOCKS.registerSimpleBlock(
            "extreme_coal_block", properties -> properties
                    .mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F));
    public static final DeferredItem<BlockItem> EXTREME_COAL_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("extreme_coal_block", EXTREME_COAL_BLOCK);
    public static final DeferredBlock<AcceleratorBlock> ACCELERATOR = BLOCKS.registerBlock(
            "accelerator", AcceleratorBlock::new, properties -> properties
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F));
    public static final DeferredItem<BlockItem> ACCELERATOR_ITEM =
            ITEMS.registerSimpleBlockItem("accelerator", ACCELERATOR);
    public static final DeferredBlock<ExtremelyFastAlloyerBlock> EXTREMELY_FAST_ALLOYER = BLOCKS.registerBlock(
            "extremely_fast_alloyer", ExtremelyFastAlloyerBlock::new, properties -> properties
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F));
    public static final DeferredItem<BlockItem> EXTREMELY_FAST_ALLOYER_ITEM =
            ITEMS.registerSimpleBlockItem("extremely_fast_alloyer", EXTREMELY_FAST_ALLOYER);

    public static final DeferredBlock<FastFurnaceBlock> FAST_FURNACE = registerFurnace(FastFurnaceTier.FAST);
    public static final DeferredBlock<FastFurnaceBlock> IRON_FAST_FURNACE = registerFurnace(FastFurnaceTier.IRON);
    public static final DeferredBlock<FastFurnaceBlock> GOLD_FAST_FURNACE = registerFurnace(FastFurnaceTier.GOLD);
    public static final DeferredBlock<FastFurnaceBlock> DIAMOND_FAST_FURNACE = registerFurnace(FastFurnaceTier.DIAMOND);
    public static final DeferredBlock<FastFurnaceBlock> FAST_CRUSHER = registerCrusher(FastFurnaceTier.FAST);
    public static final DeferredBlock<FastFurnaceBlock> IRON_FAST_CRUSHER = registerCrusher(FastFurnaceTier.IRON);
    public static final DeferredBlock<FastFurnaceBlock> GOLD_FAST_CRUSHER = registerCrusher(FastFurnaceTier.GOLD);
    public static final DeferredBlock<FastFurnaceBlock> DIAMOND_FAST_CRUSHER = registerCrusher(FastFurnaceTier.DIAMOND);
    public static final DeferredItem<BlockItem> FAST_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("fast_furnace", FAST_FURNACE);
    public static final DeferredItem<BlockItem> IRON_FAST_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("iron_fast_furnace", IRON_FAST_FURNACE);
    public static final DeferredItem<BlockItem> GOLD_FAST_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("gold_fast_furnace", GOLD_FAST_FURNACE);
    public static final DeferredItem<BlockItem> DIAMOND_FAST_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("diamond_fast_furnace", DIAMOND_FAST_FURNACE);
    public static final DeferredItem<BlockItem> FAST_CRUSHER_ITEM = ITEMS.registerSimpleBlockItem("fast_crusher", FAST_CRUSHER);
    public static final DeferredItem<BlockItem> IRON_FAST_CRUSHER_ITEM = ITEMS.registerSimpleBlockItem("iron_fast_crusher", IRON_FAST_CRUSHER);
    public static final DeferredItem<BlockItem> GOLD_FAST_CRUSHER_ITEM = ITEMS.registerSimpleBlockItem("gold_fast_crusher", GOLD_FAST_CRUSHER);
    public static final DeferredItem<BlockItem> DIAMOND_FAST_CRUSHER_ITEM = ITEMS.registerSimpleBlockItem("diamond_fast_crusher", DIAMOND_FAST_CRUSHER);

    public static final DeferredItem<Item> FAST_IRON_PICKAXE = ITEMS.registerSimpleItem(
            "fast_iron_pickaxe", properties -> properties.pickaxe(FastTools.FAST_IRON, 1.0F, -2.8F));
    public static final DeferredItem<ShovelItem> FAST_IRON_SHOVEL = ITEMS.registerItem(
            "fast_iron_shovel", properties -> new ShovelItem(FastTools.FAST_IRON, 1.5F, -3.0F, properties));
    public static final DeferredItem<AxeItem> FAST_IRON_AXE = ITEMS.registerItem(
            "fast_iron_axe", properties -> new AxeItem(FastTools.FAST_IRON, 6.0F, -3.1F, properties));
    public static final DeferredItem<Item> FAST_IRON_SWORD = ITEMS.registerSimpleItem(
            "fast_iron_sword", properties -> properties.sword(
                    FastTools.FAST_IRON, 3.0F, FastTools.scaleSwordAttackSpeed(-2.4F, 1.20F)));
    public static final DeferredItem<Item> FAST_GOLD_PICKAXE = ITEMS.registerSimpleItem(
            "fast_gold_pickaxe", properties -> properties.pickaxe(FastTools.FAST_GOLD, 1.0F, -2.8F));
    public static final DeferredItem<ShovelItem> FAST_GOLD_SHOVEL = ITEMS.registerItem(
            "fast_gold_shovel", properties -> new ShovelItem(FastTools.FAST_GOLD, 1.5F, -3.0F, properties));
    public static final DeferredItem<AxeItem> FAST_GOLD_AXE = ITEMS.registerItem(
            "fast_gold_axe", properties -> new AxeItem(FastTools.FAST_GOLD, 6.0F, -3.0F, properties));
    public static final DeferredItem<Item> FAST_GOLD_SWORD = ITEMS.registerSimpleItem(
            "fast_gold_sword", properties -> properties.sword(
                    FastTools.FAST_GOLD, 3.0F, FastTools.scaleSwordAttackSpeed(-2.4F, 1.30F)));
    public static final DeferredItem<FastTools.Standard> FAST_DIAMOND_PICKAXE = ITEMS.registerItem(
            "fast_diamond_pickaxe", FastTools.Standard::new,
            properties -> properties.pickaxe(FastTools.FAST_DIAMOND, 1.0F, -2.8F));
    public static final DeferredItem<FastTools.Shovel> FAST_DIAMOND_SHOVEL = ITEMS.registerItem(
            "fast_diamond_shovel",
            properties -> new FastTools.Shovel(FastTools.FAST_DIAMOND, 1.5F, -3.0F, properties));
    public static final DeferredItem<FastTools.Axe> FAST_DIAMOND_AXE = ITEMS.registerItem(
            "fast_diamond_axe",
            properties -> new FastTools.Axe(FastTools.FAST_DIAMOND, 5.0F, -3.0F, properties));
    public static final DeferredItem<FastTools.Standard> FAST_DIAMOND_SWORD = ITEMS.registerItem(
            "fast_diamond_sword", FastTools.Standard::new,
            properties -> properties.sword(
                    FastTools.FAST_DIAMOND,
                    FastTools.scaleSwordDamage(ToolMaterial.DIAMOND, FastTools.FAST_DIAMOND, 3.0F, 1.20F),
                    FastTools.scaleSwordAttackSpeed(-2.4F, 1.20F)));
    public static final DeferredItem<Item> EXTREMELY_FAST_PICKAXE = ITEMS.registerSimpleItem(
            "extremely_fast_pickaxe", properties -> withDefaultEnchantment(
                    properties.pickaxe(FastTools.EXTREMELY_FAST, 1.0F, -2.8F), Enchantments.FORTUNE));
    public static final DeferredItem<ShovelItem> EXTREMELY_FAST_SHOVEL = ITEMS.registerItem(
            "extremely_fast_shovel", properties -> new ShovelItem(
                    FastTools.EXTREMELY_FAST, 1.5F, -3.0F,
                    withDefaultEnchantment(properties, Enchantments.FORTUNE)));
    public static final DeferredItem<AxeItem> EXTREMELY_FAST_AXE = ITEMS.registerItem(
            "extremely_fast_axe", properties -> new AxeItem(
                    FastTools.EXTREMELY_FAST, 5.0F, -3.0F,
                    withDefaultEnchantment(properties, Enchantments.FORTUNE)));
    public static final DeferredItem<Item> EXTREMELY_FAST_SWORD = ITEMS.registerSimpleItem(
            "extremely_fast_sword", properties -> withDefaultEnchantment(
                    properties.sword(
                            FastTools.EXTREMELY_FAST, 3.0F, FastTools.scaleSwordAttackSpeed(-2.4F, 1.50F)),
                    Enchantments.LOOTING));

    private static Item.Properties withDefaultEnchantment(
            Item.Properties properties, ResourceKey<Enchantment> enchantment) {
        return properties.delayedComponent(DataComponents.ENCHANTMENTS, context -> {
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchantments.set(context.getOrThrow(enchantment), 2);
            return enchantments.toImmutable();
        });
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FastFurnaceBlockEntity>> FAST_FURNACE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "fast_furnace",
            () -> new BlockEntityType<>(
                    FastFurnaceBlockEntity::new,
                    FAST_FURNACE.get(),
                    IRON_FAST_FURNACE.get(),
                    GOLD_FAST_FURNACE.get(),
                    DIAMOND_FAST_FURNACE.get(),
                    FAST_CRUSHER.get(),
                    IRON_FAST_CRUSHER.get(),
                    GOLD_FAST_CRUSHER.get(),
                    DIAMOND_FAST_CRUSHER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AcceleratorBlockEntity>> ACCELERATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("accelerator", () -> new BlockEntityType<>(
                    AcceleratorBlockEntity::new, ACCELERATOR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtremelyFastAlloyerBlockEntity>>
            EXTREMELY_FAST_ALLOYER_BLOCK_ENTITY = BLOCK_ENTITIES.register("extremely_fast_alloyer",
                    () -> new BlockEntityType<>(ExtremelyFastAlloyerBlockEntity::new, EXTREMELY_FAST_ALLOYER.get()));

    public static final DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> FAST_FURNACE_MENU = registerFurnaceMenu(FastFurnaceTier.FAST);
    public static final DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> IRON_FAST_FURNACE_MENU = registerFurnaceMenu(FastFurnaceTier.IRON);
    public static final DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> GOLD_FAST_FURNACE_MENU = registerFurnaceMenu(FastFurnaceTier.GOLD);
    public static final DeferredHolder<MenuType<?>, MenuType<FastFurnaceMenu>> DIAMOND_FAST_FURNACE_MENU = registerFurnaceMenu(FastFurnaceTier.DIAMOND);
    public static final DeferredHolder<MenuType<?>, MenuType<AcceleratorMenu>> ACCELERATOR_MENU = MENUS.register(
            "accelerator", () -> new MenuType<>(AcceleratorMenu::new, FeatureFlags.VANILLA_SET));
    public static final DeferredHolder<MenuType<?>, MenuType<ExtremelyFastAlloyerMenu>> EXTREMELY_FAST_ALLOYER_MENU =
            MENUS.register("extremely_fast_alloyer",
                    () -> new MenuType<>(ExtremelyFastAlloyerMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FAST_FURNACE_V2_TAB = CREATIVE_MODE_TABS.register("fast_furnace_v2_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.fastfurnacev2"))
            .icon(() -> FAST_COBBLESTONE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FAST_COBBLESTONE_ITEM.get());
                output.accept(FAST_COAL.get());
                output.accept(EXTREME_COAL_ORE_ITEM.get());
                output.accept(DEEPSLATE_EXTREME_COAL_ORE_ITEM.get());
                output.accept(EXTREME_COAL.get());
                output.accept(FAST_COAL_BLOCK_ITEM.get());
                output.accept(EXTREME_COAL_BLOCK_ITEM.get());
                output.accept(FAST_IRON_ORE_ITEM.get());
                output.accept(DEEPSLATE_FAST_IRON_ORE_ITEM.get());
                output.accept(RAW_FAST_IRON.get());
                output.accept(FAST_IRON_INGOT.get());
                output.accept(FAST_IRON_BLOCK_ITEM.get());
                output.accept(FAST_GOLD_ORE_ITEM.get());
                output.accept(DEEPSLATE_FAST_GOLD_ORE_ITEM.get());
                output.accept(RAW_FAST_GOLD.get());
                output.accept(FAST_GOLD_INGOT.get());
                output.accept(FAST_GOLD_BLOCK_ITEM.get());
                output.accept(FAST_DIAMOND_ORE_ITEM.get());
                output.accept(DEEPSLATE_FAST_DIAMOND_ORE_ITEM.get());
                output.accept(RAW_FAST_DIAMOND.get());
                output.accept(FAST_DIAMOND.get());
                output.accept(FAST_DIAMOND_BLOCK_ITEM.get());
                output.accept(EXTREMELY_FAST_INGOT.get());
                output.accept(FAST_IRON_PICKAXE.get());
                output.accept(FAST_IRON_SHOVEL.get());
                output.accept(FAST_IRON_AXE.get());
                output.accept(FAST_IRON_SWORD.get());
                output.accept(FAST_GOLD_PICKAXE.get());
                output.accept(FAST_GOLD_SHOVEL.get());
                output.accept(FAST_GOLD_AXE.get());
                output.accept(FAST_GOLD_SWORD.get());
                output.accept(FAST_DIAMOND_PICKAXE.get());
                output.accept(FAST_DIAMOND_SHOVEL.get());
                output.accept(FAST_DIAMOND_AXE.get());
                output.accept(FAST_DIAMOND_SWORD.get());
                output.accept(EXTREMELY_FAST_PICKAXE.get());
                output.accept(EXTREMELY_FAST_SHOVEL.get());
                output.accept(EXTREMELY_FAST_AXE.get());
                output.accept(EXTREMELY_FAST_SWORD.get());
                output.accept(ACCELERATOR_ITEM.get());
                output.accept(EXTREMELY_FAST_ALLOYER_ITEM.get());
                for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                    output.accept(getFurnaceItem(tier).get());
                }
                for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                    output.accept(getCrusherItem(tier).get());
                }
                CrushedOres.addCreative(output);
            })
            .build());

    public static final DeferredHolder<MapCodec<? extends GameTestInstance>, MapCodec<FastFurnaceGameTests.DirectGameTestInstance>>
            GAME_TEST_INSTANCE = GAME_TEST_INSTANCE_TYPES.register(
                    "function", () -> FastFurnaceGameTests.DirectGameTestInstance.CODEC);
    // Retain the legacy attachment ID so saves from older versions can still resolve their chunk data.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> EXTREME_COAL_CHUNK_PROCESSED =
            ATTACHMENT_TYPES.register("extreme_coal_chunk_processed", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL.fieldOf("processed"))
                    .build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> FAST_ORE_CHUNK_PROCESSED =
            ATTACHMENT_TYPES.register("fast_ore_chunk_processed", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL.fieldOf("processed"))
                    .build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> DEEPSLATE_FAST_ORE_CHUNK_PROCESSED =
            ATTACHMENT_TYPES.register("deepslate_fast_ore_chunk_processed", () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL.fieldOf("processed"))
                    .build());

    public FastFurnaceV2(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        GAME_TEST_INSTANCE_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        if (Boolean.getBoolean("fastfurnacev2.apiTestContent")) {
            modEventBus.addListener(FastFurnaceGameTests::registerApiFuels);
            modEventBus.addListener(FastFurnaceGameTests::registerApiCrusherRecipes);
            modEventBus.addListener(FastFurnaceGameTests::registerApiAcceleratorRecipes);
        }
        modEventBus.addListener(CrushedOres::registerDiscovery);
        modEventBus.addListener(CrushedOres::registerItems);
        modEventBus.addListener(CrushedOreResources::addPackFinder);
        modEventBus.addListener(FastFurnaceGameTests::registerTests);
        NeoForge.EVENT_BUS.addListener(DefaultDataComponentsBoundEvent.class, event -> {
            if (event.shouldUpdateStaticData()) {
                ExtremelyFastAlloyerRecipes.initialize();
                FastFurnaceApiImpl.initialize(modEventBus);
            }
        });
        NeoForge.EVENT_BUS.addListener(FastBonusDrops::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(ExtremelyFastToolEffects::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(ExtremelyFastToolEffects::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(ExtremelyFastToolEffects::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(FastFurnaceApiImpl::onFuelBurnTime);
        NeoForge.EVENT_BUS.addListener(CrushedOres::generateRecipes);
        NeoForge.EVENT_BUS.addListener(FastOreGeneration::onChunkLoad);
    }

    public static boolean isFastCoalFuel(ItemStack stack) {
        return stack.is(FAST_COAL.get()) || stack.is(FAST_COAL_BLOCK_ITEM.get());
    }

    public static boolean isExtremeCoalFuel(ItemStack stack) {
        return stack.is(EXTREME_COAL.get()) || stack.is(EXTREME_COAL_BLOCK_ITEM.get());
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

    public static DeferredBlock<FastFurnaceBlock> getCrusherBlock(FastFurnaceTier tier) {
        return switch (tier) {
            case FAST -> FAST_CRUSHER;
            case IRON -> IRON_FAST_CRUSHER;
            case GOLD -> GOLD_FAST_CRUSHER;
            case DIAMOND -> DIAMOND_FAST_CRUSHER;
        };
    }

    public static DeferredItem<BlockItem> getCrusherItem(FastFurnaceTier tier) {
        return switch (tier) {
            case FAST -> FAST_CRUSHER_ITEM;
            case IRON -> IRON_FAST_CRUSHER_ITEM;
            case GOLD -> GOLD_FAST_CRUSHER_ITEM;
            case DIAMOND -> DIAMOND_FAST_CRUSHER_ITEM;
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

    private static DeferredBlock<Block> registerOre(String id) {
        return BLOCKS.registerSimpleBlock(id, properties -> properties
                .mapColor(MapColor.STONE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 3.0F));
    }

    private static DeferredBlock<Block> registerDeepslateOre(String id) {
        return BLOCKS.registerSimpleBlock(id, properties -> properties
                .mapColor(MapColor.DEEPSLATE)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops()
                .strength(4.5F, 3.0F));
    }

    private static DeferredBlock<FastFurnaceBlock> registerFurnace(FastFurnaceTier tier) {
        return registerMachine(tier.getId(), tier, false);
    }

    private static DeferredBlock<FastFurnaceBlock> registerCrusher(FastFurnaceTier tier) {
        return registerMachine(tier.getCrusherId(), tier, true);
    }

    private static DeferredBlock<FastFurnaceBlock> registerMachine(
            String id, FastFurnaceTier tier, boolean fastCrusher) {
        return BLOCKS.registerBlock(id, properties -> new FastFurnaceBlock(properties, tier, fastCrusher), properties -> properties
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
            event.accept(EXTREME_COAL_BLOCK_ITEM);
            event.accept(FAST_IRON_BLOCK_ITEM);
            event.accept(FAST_GOLD_BLOCK_ITEM);
            event.accept(FAST_DIAMOND_BLOCK_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(EXTREME_COAL_ORE_ITEM);
            event.accept(DEEPSLATE_EXTREME_COAL_ORE_ITEM);
            event.accept(FAST_IRON_ORE_ITEM);
            event.accept(DEEPSLATE_FAST_IRON_ORE_ITEM);
            event.accept(FAST_GOLD_ORE_ITEM);
            event.accept(DEEPSLATE_FAST_GOLD_ORE_ITEM);
            event.accept(FAST_DIAMOND_ORE_ITEM);
            event.accept(DEEPSLATE_FAST_DIAMOND_ORE_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(FAST_COAL);
            event.accept(EXTREME_COAL);
            event.accept(RAW_FAST_IRON);
            event.accept(FAST_IRON_INGOT);
            event.accept(RAW_FAST_GOLD);
            event.accept(FAST_GOLD_INGOT);
            event.accept(RAW_FAST_DIAMOND);
            event.accept(FAST_DIAMOND);
            event.accept(EXTREMELY_FAST_INGOT);
            CrushedOres.addCreative(event);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(FAST_IRON_PICKAXE);
            event.accept(FAST_IRON_SHOVEL);
            event.accept(FAST_IRON_AXE);
            event.accept(FAST_GOLD_PICKAXE);
            event.accept(FAST_GOLD_SHOVEL);
            event.accept(FAST_GOLD_AXE);
            event.accept(FAST_DIAMOND_PICKAXE);
            event.accept(FAST_DIAMOND_SHOVEL);
            event.accept(FAST_DIAMOND_AXE);
            event.accept(EXTREMELY_FAST_PICKAXE);
            event.accept(EXTREMELY_FAST_SHOVEL);
            event.accept(EXTREMELY_FAST_AXE);
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(FAST_IRON_SWORD);
            event.accept(FAST_IRON_AXE);
            event.accept(FAST_GOLD_SWORD);
            event.accept(FAST_GOLD_AXE);
            event.accept(FAST_DIAMOND_SWORD);
            event.accept(FAST_DIAMOND_AXE);
            event.accept(EXTREMELY_FAST_SWORD);
            event.accept(EXTREMELY_FAST_AXE);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ACCELERATOR_ITEM);
            event.accept(EXTREMELY_FAST_ALLOYER_ITEM);
            for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                event.accept(getFurnaceItem(tier));
            }
            for (FastFurnaceTier tier : FastFurnaceTier.values()) {
                event.accept(getCrusherItem(tier));
            }
        }
    }
}
