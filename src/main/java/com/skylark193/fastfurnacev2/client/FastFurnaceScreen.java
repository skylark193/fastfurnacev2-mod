package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.FastFurnaceBlockEntity;
import com.skylark193.fastfurnacev2.FastFurnaceMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeBookCategories;

public class FastFurnaceScreen extends AbstractFurnaceScreen<FastFurnaceMenu> {
    private static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier BURN_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.smeltable");
    private static final List<RecipeBookComponent.TabInfo> TABS = List.of(
            new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.FURNACE),
            new RecipeBookComponent.TabInfo(Items.PORKCHOP, RecipeBookCategories.FURNACE_FOOD),
            new RecipeBookComponent.TabInfo(Items.STONE, RecipeBookCategories.FURNACE_BLOCKS),
            new RecipeBookComponent.TabInfo(Items.LAVA_BUCKET, Items.EMERALD, RecipeBookCategories.FURNACE_MISC));

    private static final int INFO_BUTTON_X = 160;
    private static final int INFO_BUTTON_Y = 4;
    private static final int INFO_BUTTON_SIZE = 12;
    private static final int INFO_PANEL_WIDTH = 152;
    private static final int INFO_PANEL_HEIGHT = 68;
    private static final Component INFO_TITLE = Component.translatable("gui.fastfurnacev2.info");
    private static final Component SPEED = Component.translatable("gui.fastfurnacev2.speed", FastFurnaceBlockEntity.SPEED_PERCENT);
    private static final Component DOUBLE_BURN = Component.translatable(
            "gui.fastfurnacev2.double_burn_chance", FastFurnaceBlockEntity.DOUBLE_BURN_CHANCE_PERCENT);

    private boolean infoVisible;

    public FastFurnaceScreen(FastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, FILTER_NAME, TEXTURE, LIT_PROGRESS_SPRITE, BURN_PROGRESS_SPRITE, TABS);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && this.isHovering(
                INFO_BUTTON_X, INFO_BUTTON_Y, INFO_BUTTON_SIZE, INFO_BUTTON_SIZE, event.x(), event.y())) {
            this.infoVisible = !this.infoVisible;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        boolean hovered = this.isHovering(INFO_BUTTON_X, INFO_BUTTON_Y, INFO_BUTTON_SIZE, INFO_BUTTON_SIZE, mouseX, mouseY);
        graphics.fill(INFO_BUTTON_X - 1, INFO_BUTTON_Y - 1,
                INFO_BUTTON_X + INFO_BUTTON_SIZE + 1, INFO_BUTTON_Y + INFO_BUTTON_SIZE + 1, 0xFF373737);
        graphics.fill(INFO_BUTTON_X, INFO_BUTTON_Y,
                INFO_BUTTON_X + INFO_BUTTON_SIZE, INFO_BUTTON_Y + INFO_BUTTON_SIZE, hovered ? 0xFF6F6F6F : 0xFF4F4F4F);
        graphics.text(this.font, "i", INFO_BUTTON_X + 5, INFO_BUTTON_Y + 2, 0xFFFFFFFF, false);

        if (this.infoVisible) {
            int x = this.imageWidth + 4;
            int y = INFO_BUTTON_Y;
            graphics.fill(x - 1, y - 1, x + INFO_PANEL_WIDTH + 1, y + INFO_PANEL_HEIGHT + 1, 0xFFC6C6C6);
            graphics.fill(x, y, x + INFO_PANEL_WIDTH, y + INFO_PANEL_HEIGHT, 0xF0181818);
            graphics.text(this.font, INFO_TITLE, x + 6, y + 6, 0xFFFFFFFF, false);
            graphics.text(this.font, SPEED, x + 6, y + 20, 0xFFE0E0E0, false);
            graphics.text(this.font, DOUBLE_BURN, x + 6, y + 31, 0xFFE0E0E0, false);
            graphics.text(this.font,
                    Component.translatable("gui.fastfurnacev2.current_burn", this.menu.getCurrentBurnTime()),
                    x + 6, y + 44, 0xFFE0E0E0, false);
            graphics.text(this.font,
                    Component.translatable("gui.fastfurnacev2.total_fuel", this.menu.getTotalFuelTime()),
                    x + 6, y + 55, 0xFFE0E0E0, false);
        }
    }

    public List<Rect2i> getJeiExtraAreas() {
        if (!this.infoVisible) {
            return List.of();
        }
        return List.of(new Rect2i(
                this.leftPos + this.imageWidth + 3,
                this.topPos + INFO_BUTTON_Y - 1,
                INFO_PANEL_WIDTH + 2,
                INFO_PANEL_HEIGHT + 2));
    }
}
