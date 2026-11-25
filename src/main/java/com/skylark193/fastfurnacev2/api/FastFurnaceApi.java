package com.skylark193.fastfurnacev2.api;

import com.skylark193.fastfurnacev2.ExtremelyFastAlloyerRecipes;
import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.FastTools;
import com.skylark193.fastfurnacev2.internal.FastFurnaceApiImpl;
import java.util.List;
import java.util.Optional;
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
    public static final String API_VERSION = "1.2.0";
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

    public static Item extremeCoal() {
        return FastFurnaceV2.EXTREME_COAL.get();
    }

    public static Item extremeCoalBlock() {
        return FastFurnaceV2.EXTREME_COAL_BLOCK_ITEM.get();
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

    /** Finds a recipe and verifies that the stack contains its required input count. */
    public static Optional<CrusherRecipe> findCrusherRecipe(ItemStack input) {
        return FastFurnaceApiImpl.findCrusherRecipe(input);
    }

    public static List<AcceleratorConversion> acceleratorRecipes() {
        return FastFurnaceApiImpl.acceleratorRecipes();
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
