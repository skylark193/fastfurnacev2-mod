package com.skylark193.fastfurnacev2.api;

import com.skylark193.fastfurnacev2.FastFurnaceTier;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.internal.FastFurnaceApiImpl;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Public query and content-access API for Fast Furnace V2.
 *
 * <p>Register content through the events in {@code api.event}. Registered-content queries are
 * available after those events fire once default item components are bound. Returned recipes and
 * collections are immutable; returned item stacks are fresh copies.</p>
 */
public final class FastFurnaceApi {
    public static final String API_VERSION = "1.0";
    public static final Identifier FAST_COAL_FUEL_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_coal");
    public static final Identifier FAST_COAL_BLOCK_FUEL_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "fast_coal_block");
    public static final Identifier EXTREME_COAL_FUEL_ID =
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "extreme_coal");

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
}
