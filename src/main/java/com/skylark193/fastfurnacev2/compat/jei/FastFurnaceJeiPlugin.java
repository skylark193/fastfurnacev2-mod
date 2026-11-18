package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceBlockEntity;
import com.skylark193.fastfurnacev2.FastFurnaceMenu;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.client.FastFurnaceScreen;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;

@JeiPlugin
public class FastFurnaceJeiPlugin implements IModPlugin {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new FastFurnaceFuelCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                FastFurnaceV2.FAST_COBBLESTONE.get(),
                Component.translatable("jei.fastfurnacev2.fast_cobblestone"));
        registration.addIngredientInfo(
                FastFurnaceV2.FAST_COAL.get(),
                Component.translatable("jei.fastfurnacev2.fast_coal"));

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        FuelValues fuelValues = minecraft.level.fuelValues();
        List<IJeiFuelingRecipe> fuels = new ArrayList<>();
        for (ItemStack stack : registration.getIngredientManager().getAllItemStacks()) {
            int burnTime = stack.getBurnTime(RecipeType.SMELTING, fuelValues);
            if (burnTime <= 0) {
                continue;
            }
            if (stack.is(FastFurnaceV2.FAST_COAL.get())) {
                burnTime = FastFurnaceBlockEntity.scaleFastCoalBurnTime(burnTime);
            }
            fuels.add(new FastFurnaceFuelRecipe(List.of(stack), burnTime));
        }
        fuels.sort(Comparator.comparingInt(IJeiFuelingRecipe::getBurnTime));
        registration.addRecipes(FastFurnaceFuelCategory.RECIPE_TYPE, fuels);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(RecipeTypes.SMELTING, FastFurnaceV2.FAST_FURNACE.get());
        registration.addCraftingStation(FastFurnaceFuelCategory.RECIPE_TYPE, FastFurnaceV2.FAST_FURNACE.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                FastFurnaceMenu.class,
                FastFurnaceV2.FAST_FURNACE_MENU.get(),
                RecipeTypes.SMELTING,
                0,
                1,
                3,
                36);
        registration.addRecipeTransferHandler(
                FastFurnaceMenu.class,
                FastFurnaceV2.FAST_FURNACE_MENU.get(),
                FastFurnaceFuelCategory.RECIPE_TYPE,
                1,
                1,
                3,
                36);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(FastFurnaceScreen.class, 78, 32, 28, 23, RecipeTypes.SMELTING);
        registration.addRecipeClickArea(FastFurnaceScreen.class, 56, 36, 14, 14, FastFurnaceFuelCategory.RECIPE_TYPE);
        registration.addGuiContainerHandler(FastFurnaceScreen.class, new IGuiContainerHandler<FastFurnaceScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(FastFurnaceScreen screen) {
                return screen.getJeiExtraAreas();
            }
        });
    }
}
