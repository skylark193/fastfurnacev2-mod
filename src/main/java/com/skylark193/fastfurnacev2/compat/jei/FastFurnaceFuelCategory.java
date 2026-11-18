package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.FastFurnaceV2;
import java.text.NumberFormat;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import net.minecraft.network.chat.Component;

final class FastFurnaceFuelCategory implements IRecipeCategory<IJeiFuelingRecipe> {
    static final IRecipeType<IJeiFuelingRecipe> RECIPE_TYPE =
            IRecipeType.create(FastFurnaceV2.MODID, "fast_furnace_fuel", IJeiFuelingRecipe.class);

    private static final int WIDTH = 160;
    private static final int HEIGHT = 34;
    private static final Component TITLE = Component.translatable("jei.fastfurnacev2.fast_furnace_fuel");

    private final IDrawable icon;

    FastFurnaceFuelCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(FastFurnaceV2.FAST_FURNACE.get());
    }

    @Override
    public IRecipeType<IJeiFuelingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return TITLE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, IJeiFuelingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 17)
                .setStandardSlotBackground()
                .addItemStacks(recipe.getInputs());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, IJeiFuelingRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeFlame(recipe.getBurnTime()).setPosition(1, 0);
        builder.addText(createSmeltCountText(recipe.getBurnTime()), WIDTH - 20, HEIGHT)
                .setPosition(20, 0)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setTextAlignment(VerticalAlignment.CENTER)
                .setColor(0xFF808080);
    }

    private static Component createSmeltCountText(int burnTime) {
        if (burnTime == 200) {
            return Component.translatable("gui.jei.category.fuel.smeltCount.single");
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        return Component.translatable(
                "gui.jei.category.fuel.smeltCount",
                numberFormat.format(burnTime / 200.0F));
    }
}
