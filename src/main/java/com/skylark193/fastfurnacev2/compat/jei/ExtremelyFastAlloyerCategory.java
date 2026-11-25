package com.skylark193.fastfurnacev2.compat.jei;

import com.skylark193.fastfurnacev2.api.ExtremelyFastAlloyerRecipe;
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

final class ExtremelyFastAlloyerCategory implements IRecipeCategory<ExtremelyFastAlloyerRecipe> {
    private static final IRecipeType<ExtremelyFastAlloyerRecipe> RECIPE_TYPE =
            IRecipeType.create(FastFurnaceV2.MODID, "extremely_fast_alloyer", ExtremelyFastAlloyerRecipe.class);
    private static final int WIDTH = 170;
    private static final int HEIGHT = 82;
    private static final int[] INPUT_X = {12, 36, 24};
    private static final int[] INPUT_Y = {12, 12, 40};

    private final Component title;
    private final IDrawable icon;

    ExtremelyFastAlloyerCategory(IGuiHelper guiHelper) {
        this.title = Component.translatable("jei.fastfurnacev2.alloyer");
        this.icon = guiHelper.createDrawableItemLike(FastFurnaceV2.EXTREMELY_FAST_ALLOYER.get());
    }

    static IRecipeType<ExtremelyFastAlloyerRecipe> getAlloyerRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public IRecipeType<ExtremelyFastAlloyerRecipe> getRecipeType() {
        return getAlloyerRecipeType();
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
    public void setRecipe(
            IRecipeLayoutBuilder builder, ExtremelyFastAlloyerRecipe recipe, IFocusGroup focuses) {
        for (int slot = 0; slot < recipe.ingredients().size(); slot++) {
            builder.addInputSlot(INPUT_X[slot], INPUT_Y[slot])
                    .setStandardSlotBackground()
                    .addItemStacks(recipe.inputItems(slot));
        }
        builder.addOutputSlot(126, 26)
                .setStandardSlotBackground()
                .addItemStacks(List.of(recipe.output()));
    }

    @Override
    public void createRecipeExtras(
            IRecipeExtrasBuilder builder, ExtremelyFastAlloyerRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.processingTicks()).setPosition(66, 27);
        builder.addText(Component.translatable(
                        "jei.fastfurnacev2.alloyer_duration", recipe.processingTicks()), WIDTH, 12)
                .setPosition(0, 66)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080);
    }
}
