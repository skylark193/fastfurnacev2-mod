package com.skylark193.fastfurnacev2.api;

import com.skylark193.fastfurnacev2.CrushedOres;
import com.skylark193.fastfurnacev2.ExtremelyFastAlloyerRecipes;
import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.FastTools;
import com.skylark193.fastfurnacev2.internal.FastFurnaceApiImpl;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

/**
 * Public query and content-access API for Fast Furnace V2.
 *
 * <p>Register content through the events in {@code api.event}. Registered-content queries are
 * available after those events fire once default item components are bound. Returned recipes and
 * collections are immutable; returned item stacks are fresh copies.</p>
 */
public final class FastFurnaceApi {
    public static final String API_VERSION = "1.4.0";
    public static final Identifier FAST_COAL_FUEL_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_coal");
    public static final Identifier FAST_COAL_BLOCK_FUEL_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_coal_block");
    public static final Identifier EXTREME_COAL_FUEL_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "extreme_coal");
    public static final Identifier EXTREME_COAL_BLOCK_FUEL_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "extreme_coal_block");
    public static final Identifier EXTREMELY_FAST_INGOT_RECIPE_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "alloyer/extremely_fast_ingot");
    public static final Identifier FAST_DIAMOND_TOOL_MODIFIERS_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "tools/fast_diamond");
    public static final Identifier EXTREMELY_FAST_HARVESTING_TOOL_MODIFIERS_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "tools/extremely_fast_harvesting");
    public static final Identifier EXTREMELY_FAST_SWORD_MODIFIERS_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "tools/extremely_fast_sword");

    private FastFurnaceApi() {
    }

    public static List<FastFurnaceTier> tiers() {
        return List.of(FastFurnaceTier.values());
    }

    public static Item furnace(FastFurnaceTier tier) {
        return FastFurnaceV2.getFurnaceItem(tier).get();
    }

    public static Item crusher(FastFurnaceTier tier) {
        return FastFurnaceV2.getCrusherItem(tier).get();
    }

    public static Item generator(FastFurnaceTier tier) {
        return FastFurnaceV2.getGeneratorItem(tier).get();
    }

    public static Item battery(FastFurnaceTier tier) {
        return FastFurnaceV2.getBatteryItem(tier).get();
    }

    public static Item conduit(FastFurnaceTier tier) {
        return FastFurnaceV2.getConduitItem(tier).get();
    }

    public static Item accelerator() {
        return FastFurnaceV2.ACCELERATOR_ITEM.get();
    }

    public static Item fastCobblestone() {
        return FastFurnaceV2.FAST_COBBLESTONE_ITEM.get();
    }

    public static Item fastCoal() {
        return FastFurnaceV2.FAST_COAL.get();
    }

    public static Item fastCoalBlock() {
        return FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get();
    }

    public static Item extremeCoalOre() {
        return FastFurnaceV2.EXTREME_COAL_ORE_ITEM.get();
    }

    public static Item deepslateExtremeCoalOre() {
        return FastFurnaceV2.DEEPSLATE_EXTREME_COAL_ORE_ITEM.get();
    }

    public static Item extremeCoal() {
        return FastFurnaceV2.EXTREME_COAL.get();
    }

    public static Item extremeCoalBlock() {
        return FastFurnaceV2.EXTREME_COAL_BLOCK_ITEM.get();
    }

    public static Item fastIronOre() {
        return FastFurnaceV2.FAST_IRON_ORE_ITEM.get();
    }

    public static Item deepslateFastIronOre() {
        return FastFurnaceV2.DEEPSLATE_FAST_IRON_ORE_ITEM.get();
    }

    public static Item rawFastIron() {
        return FastFurnaceV2.RAW_FAST_IRON.get();
    }

    public static Item fastIronIngot() {
        return FastFurnaceV2.FAST_IRON_INGOT.get();
    }

    public static Item fastIronBlock() {
        return FastFurnaceV2.FAST_IRON_BLOCK_ITEM.get();
    }

    public static Item fastGoldOre() {
        return FastFurnaceV2.FAST_GOLD_ORE_ITEM.get();
    }

    public static Item deepslateFastGoldOre() {
        return FastFurnaceV2.DEEPSLATE_FAST_GOLD_ORE_ITEM.get();
    }

    public static Item rawFastGold() {
        return FastFurnaceV2.RAW_FAST_GOLD.get();
    }

    public static Item fastGoldIngot() {
        return FastFurnaceV2.FAST_GOLD_INGOT.get();
    }

    public static Item fastGoldBlock() {
        return FastFurnaceV2.FAST_GOLD_BLOCK_ITEM.get();
    }

    public static Item fastDiamondOre() {
        return FastFurnaceV2.FAST_DIAMOND_ORE_ITEM.get();
    }

    public static Item deepslateFastDiamondOre() {
        return FastFurnaceV2.DEEPSLATE_FAST_DIAMOND_ORE_ITEM.get();
    }

    public static Item rawFastDiamond() {
        return FastFurnaceV2.RAW_FAST_DIAMOND.get();
    }

    public static Item fastDiamond() {
        return FastFurnaceV2.FAST_DIAMOND.get();
    }

    public static Item fastDiamondBlock() {
        return FastFurnaceV2.FAST_DIAMOND_BLOCK_ITEM.get();
    }

    public static List<FastFuel> fuels() {
        return FastFurnaceApiImpl.fuels();
    }

    public static Optional<FastFuel> findFuel(Identifier id) {
        return FastFurnaceApiImpl.findFuel(id);
    }

    /** Finds the highest-priority fuel whose ingredient matches the complete stack. */
    public static Optional<FastFuel> findFuel(ItemStack stack) {
        return FastFurnaceApiImpl.findFuel(stack);
    }

    public static List<CrusherRecipe> crusherRecipes() {
        return FastFurnaceApiImpl.crusherRecipes();
    }

    public static Optional<CrusherRecipe> findCrusherRecipe(Identifier id) {
        return FastFurnaceApiImpl.findCrusherRecipe(id);
    }

    /** Returns whether an item can enter a crusher input slot, even before a full recipe count is present. */
    public static boolean isCrusherInput(ItemStack input) {
        return FastFurnaceApiImpl.isCrusherInput(input);
    }

    /** Finds a recipe and verifies that the stack contains its required input count. */
    public static Optional<CrusherRecipe> findCrusherRecipe(ItemStack input) {
        return FastFurnaceApiImpl.findCrusherRecipe(input);
    }

    public static List<AcceleratorConversion> acceleratorRecipes() {
        return FastFurnaceApiImpl.acceleratorRecipes();
    }

    public static Optional<AcceleratorConversion> findAcceleratorRecipe(Identifier id) {
        return FastFurnaceApiImpl.findAcceleratorRecipe(id);
    }

    /** Returns whether an item can enter an Accelerator input slot, even before a full recipe count is present. */
    public static boolean isAcceleratorInput(ItemStack input) {
        return FastFurnaceApiImpl.isAcceleratorInput(input);
    }

    /** Finds a conversion and verifies that the stack contains its required input count. */
    public static Optional<AcceleratorConversion> findAcceleratorRecipe(ItemStack input) {
        return FastFurnaceApiImpl.findAcceleratorRecipe(input);
    }

    public static List<ExtremelyFastAlloyerRecipe> alloyerRecipes() {
        return ExtremelyFastAlloyerRecipes.recipes();
    }

    public static Optional<ExtremelyFastAlloyerRecipe> findAlloyerRecipe(Identifier id) {
        return ExtremelyFastAlloyerRecipes.findRecipe(id);
    }

    /** Returns whether an item can enter any registered Alloyer recipe ingredient slot. */
    public static boolean isAlloyerInput(ItemStack input) {
        return ExtremelyFastAlloyerRecipes.isInput(input);
    }

    /** Finds the highest-priority shapeless match in the first three input stacks. */
    public static Optional<ExtremelyFastAlloyerRecipe> findAlloyerRecipe(List<ItemStack> inputs) {
        return ExtremelyFastAlloyerRecipes.findRecipe(inputs);
    }

    public static Item extremelyFastAlloyer() {
        return FastFurnaceV2.EXTREMELY_FAST_ALLOYER_ITEM.get();
    }

    public static Item extremelyFastIngot() {
        return FastFurnaceV2.EXTREMELY_FAST_INGOT.get();
    }

    /** Returns all discovered raw-ore IDs, including mappings reused from other mods. */
    public static Set<Identifier> rawOreIds() {
        return Set.copyOf(CrushedOres.rawOres());
    }

    /** Returns the generated or reused crushed result for a discovered raw ore. */
    public static Optional<Item> crushedOre(Identifier rawOreId) {
        return Optional.ofNullable(CrushedOres.forRawOre(rawOreId));
    }

    /** Returns an immutable snapshot of all raw-to-crushed result mappings. */
    public static Map<Identifier, Item> crushedOres() {
        Map<Identifier, Item> mappings = new LinkedHashMap<>();
        for (Identifier rawOreId : CrushedOres.rawOres()) {
            Item crushed = CrushedOres.forRawOre(rawOreId);
            if (crushed != null) {
                mappings.put(rawOreId, crushed);
            }
        }
        return Collections.unmodifiableMap(mappings);
    }

    public static Item fastIronPickaxe() {
        return FastFurnaceV2.FAST_IRON_PICKAXE.get();
    }

    public static Item fastIronShovel() {
        return FastFurnaceV2.FAST_IRON_SHOVEL.get();
    }

    public static Item fastIronAxe() {
        return FastFurnaceV2.FAST_IRON_AXE.get();
    }

    public static Item fastIronSword() {
        return FastFurnaceV2.FAST_IRON_SWORD.get();
    }

    public static Item fastGoldPickaxe() {
        return FastFurnaceV2.FAST_GOLD_PICKAXE.get();
    }

    public static Item fastGoldShovel() {
        return FastFurnaceV2.FAST_GOLD_SHOVEL.get();
    }

    public static Item fastGoldAxe() {
        return FastFurnaceV2.FAST_GOLD_AXE.get();
    }

    public static Item fastGoldSword() {
        return FastFurnaceV2.FAST_GOLD_SWORD.get();
    }

    public static Item fastDiamondPickaxe() {
        return FastFurnaceV2.FAST_DIAMOND_PICKAXE.get();
    }

    public static Item fastDiamondShovel() {
        return FastFurnaceV2.FAST_DIAMOND_SHOVEL.get();
    }

    public static Item fastDiamondAxe() {
        return FastFurnaceV2.FAST_DIAMOND_AXE.get();
    }

    public static Item fastDiamondSword() {
        return FastFurnaceV2.FAST_DIAMOND_SWORD.get();
    }

    public static Item extremelyFastPickaxe() {
        return FastFurnaceV2.EXTREMELY_FAST_PICKAXE.get();
    }

    public static Item extremelyFastShovel() {
        return FastFurnaceV2.EXTREMELY_FAST_SHOVEL.get();
    }

    public static Item extremelyFastAxe() {
        return FastFurnaceV2.EXTREMELY_FAST_AXE.get();
    }

    public static Item extremelyFastSword() {
        return FastFurnaceV2.EXTREMELY_FAST_SWORD.get();
    }

    /**
     * Creates a vanilla-style tool whose durability rolls use registered Fast Furnace modifiers.
     * Other registered proc modifiers also work on ordinary modded tool items.
     */
    public static Item createTool(
            FastToolType type,
            ToolMaterial material,
            float attackDamageBaseline,
            float attackSpeedBaseline,
            Item.Properties properties) {
        return FastTools.create(type, material, attackDamageBaseline, attackSpeedBaseline, properties);
    }

    public static List<FastToolModifiers> toolModifiers() {
        return FastFurnaceApiImpl.toolModifiers();
    }

    public static Optional<FastToolModifiers> findToolModifiers(Identifier id) {
        return FastFurnaceApiImpl.findToolModifiers(id);
    }

    /** Finds the highest-priority modifier definition whose ingredient matches the complete stack. */
    public static Optional<FastToolModifiers> findToolModifiers(ItemStack tool) {
        return FastFurnaceApiImpl.findToolModifiers(tool);
    }
}
