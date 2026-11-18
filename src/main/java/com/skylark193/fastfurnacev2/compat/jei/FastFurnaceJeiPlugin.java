package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceMenu;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.client.FastFurnaceScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class FastFurnaceJeiPlugin implements IModPlugin {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addIngredientInfo(
                FastFurnaceV2.FAST_COBBLESTONE.get(),
                Component.translatable("jei.fastfurnacev2.fast_cobblestone"));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(RecipeTypes.SMELTING, FastFurnaceV2.FAST_FURNACE.get());
        registration.addCraftingStation(RecipeTypes.SMELTING_FUEL, FastFurnaceV2.FAST_FURNACE.get());
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
                RecipeTypes.SMELTING_FUEL,
                1,
                1,
                3,
                36);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(FastFurnaceScreen.class, 78, 32, 28, 23, RecipeTypes.SMELTING);
        registration.addRecipeClickArea(FastFurnaceScreen.class, 56, 36, 14, 14, RecipeTypes.SMELTING_FUEL);
        registration.addGuiContainerHandler(FastFurnaceScreen.class, new IGuiContainerHandler<FastFurnaceScreen>() {
            @Override
            public java.util.List<Rect2i> getGuiExtraAreas(FastFurnaceScreen screen) {
                return screen.getJeiExtraAreas();
            }
        });
    }
}
