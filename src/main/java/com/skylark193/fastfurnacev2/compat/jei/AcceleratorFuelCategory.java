package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceV2;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;

final class AcceleratorFuelCategory implements IRecipeCategory<AcceleratorFuelRecipe> {
    private static final IRecipeType<AcceleratorFuelRecipe> RECIPE_TYPE =
            IRecipeType.create(FastFurnaceV2.MODID, "accelerator_fuel", AcceleratorFuelRecipe.class);
    private static final int WIDTH = 190;
    private static final int HEIGHT = 52;

    private final Component title;
    private final IDrawable icon;

    AcceleratorFuelCategory(IGuiHelper guiHelper) {
        this.title = Component.translatable("jei.fastfurnacev2.accelerator_fuel");
        this.icon = guiHelper.createDrawableItemLike(FastFurnaceV2.ACCELERATOR.get());
    }

    static IRecipeType<AcceleratorFuelRecipe> getAcceleratorFuelRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public IRecipeType<AcceleratorFuelRecipe> getRecipeType() {
        return getAcceleratorFuelRecipeType();
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AcceleratorFuelRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 18)
                .setStandardSlotBackground()
                .addItemStacks(List.of(recipe.input()));
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder, AcceleratorFuelRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeFlame(recipe.boostWork()).setPosition(1, 0);
        boolean extreme = FastFurnaceV2.isExtremeCoalFuel(recipe.input());
        addCenteredText(builder, Component.translatable(extreme
                ? "jei.fastfurnacev2.accelerator_extreme_boost_speed"
                : "jei.fastfurnacev2.accelerator_boost_speed"), 0);
        Component capacity = extreme
                ? Component.translatable("jei.fastfurnacev2.accelerator_extreme_coal_capacity")
                : recipe.input().is(FastFurnaceV2.FAST_COAL_BLOCK_ITEM.get())
                        ? Component.translatable("jei.fastfurnacev2.accelerator_fast_coal_block_capacity")
                        : Component.translatable("jei.fastfurnacev2.accelerator_fast_coal_capacity");
        addCenteredText(builder, capacity, 16);
        addCenteredText(builder, Component.translatable(
                "jei.fastfurnacev2.accelerator_boost_work", recipe.boostWork()), 32);
    }

    private static void addCenteredText(IRecipeExtrasBuilder builder, Component text, int y) {
        builder.addText(text, WIDTH - 20, 14)
                .setPosition(20, y)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setTextAlignment(VerticalAlignment.CENTER)
                .setColor(0xFF808080);
    }
}
