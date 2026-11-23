package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.api.AcceleratorConversion;
import com.skylark193.fastfurnacev2.api.FastFuel;
import com.skylark193.fastfurnacev2.api.FastFurnaceApi;
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

final class AcceleratorFuelCategory implements IRecipeCategory<FastFuel> {
    private static final IRecipeType<FastFuel> RECIPE_TYPE =
            IRecipeType.create(FastFurnaceV2.MODID, "accelerator_fuel", FastFuel.class);
    private static final int WIDTH = 190;
    private static final int HEIGHT = 52;

    private final Component title;
    private final IDrawable icon;

    AcceleratorFuelCategory(IGuiHelper guiHelper) {
        this.title = Component.translatable("jei.fastfurnacev2.accelerator_fuel");
        this.icon = guiHelper.createDrawableItemLike(FastFurnaceV2.ACCELERATOR.get());
    }

    static IRecipeType<FastFuel> getAcceleratorFuelRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public IRecipeType<FastFuel> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, FastFuel recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 18)
                .setStandardSlotBackground()
                .addItemStacks(recipe.inputItems());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, FastFuel recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeFlame(recipe.acceleratorWork()).setPosition(1, 0);
        addCenteredText(builder, Component.translatable(
                "jei.fastfurnacev2.accelerator_boost_speed", recipe.acceleratorWorkPerTick()), 0);
        addCenteredText(builder, Component.translatable(
                "jei.fastfurnacev2.accelerator_boost_work", recipe.acceleratorWork()), 16);
        addCenteredText(builder, createCapacityText(recipe), 32);
    }

    private static Component createCapacityText(FastFuel fuel) {
        int minimum = Integer.MAX_VALUE;
        int maximum = 0;
        for (AcceleratorConversion conversion : FastFurnaceApi.acceleratorRecipes()) {
            if (conversion.workUnits() <= fuel.acceleratorWork()) {
                int count = fuel.acceleratorWork() / conversion.workUnits();
                minimum = Math.min(minimum, count);
                maximum = Math.max(maximum, count);
            }
        }
        if (maximum == 0) {
            return Component.translatable("jei.fastfurnacev2.accelerator_no_full_conversion");
        }
        return minimum == maximum
                ? Component.translatable("jei.fastfurnacev2.accelerator_conversion_capacity", maximum)
                : Component.translatable(
                        "jei.fastfurnacev2.accelerator_conversion_capacity_range", minimum, maximum);
    }

    private static void addCenteredText(IRecipeExtrasBuilder builder, Component text, int y) {
        builder.addText(text, WIDTH - 20, 14)
                .setPosition(20, y)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setTextAlignment(VerticalAlignment.CENTER)
                .setColor(0xFF808080);
    }
}
