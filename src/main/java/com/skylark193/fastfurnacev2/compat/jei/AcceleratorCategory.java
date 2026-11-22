package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceV2;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;

final class AcceleratorCategory implements IRecipeCategory<AcceleratorRecipe> {
    private static final IRecipeType<AcceleratorRecipe> RECIPE_TYPE =
            IRecipeType.create(FastFurnaceV2.MODID, "accelerator", AcceleratorRecipe.class);
    private static final int WIDTH = 170;
    private static final int HEIGHT = 94;

    private final Component title;
    private final IDrawable icon;

    AcceleratorCategory(IGuiHelper guiHelper) {
        this.title = Component.translatable("jei.fastfurnacev2.accelerator");
        this.icon = guiHelper.createDrawableItemLike(FastFurnaceV2.ACCELERATOR.get());
    }

    static IRecipeType<AcceleratorRecipe> getAcceleratorRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public IRecipeType<AcceleratorRecipe> getRecipeType() {
        return getAcceleratorRecipeType();
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
    public void setRecipe(IRecipeLayoutBuilder builder, AcceleratorRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(20, 16)
                .setStandardSlotBackground()
                .addItemStacks(List.of(recipe.input()));
        builder.addOutputSlot(120, 16)
                .setStandardSlotBackground()
                .addItemStacks(List.of(recipe.output()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AcceleratorRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.workTicks()).setPosition(60, 17);
        builder.addText(Component.translatable(
                        "jei.fastfurnacev2.accelerator_duration", recipe.workTicks()), WIDTH, 12)
                .setPosition(0, 42)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
        builder.addText(Component.translatable(
                        "jei.fastfurnacev2.accelerator_fast_duration", recipe.boostedTicks()), WIDTH, 12)
                .setPosition(0, 53)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
        builder.addText(Component.translatable(
                        "jei.fastfurnacev2.accelerator_extreme_duration", recipe.extremeBoostedTicks()), WIDTH, 12)
                .setPosition(0, 64)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
        builder.addText(Component.translatable("jei.fastfurnacev2.accelerator_lava"), WIDTH, 12)
                .setPosition(0, 80)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
    }
}
