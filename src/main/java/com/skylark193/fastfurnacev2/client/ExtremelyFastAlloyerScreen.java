package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.ExtremelyFastAlloyerMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class ExtremelyFastAlloyerScreen extends AbstractContainerScreen<ExtremelyFastAlloyerMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier BURN_PROGRESS_SPRITE =
            Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final int INFO_BUTTON_X = 160;
    private static final int INFO_BUTTON_Y = 4;
    private static final int INFO_BUTTON_SIZE = 12;
    private static final int INFO_PANEL_WIDTH = 176;
    private static final int INFO_PANEL_HEIGHT = 55;
    private static final Component INFO_TITLE = Component.translatable("gui.fastfurnacev2.alloyer.info");
    private static final Component RECIPE_TIME = Component.translatable("gui.fastfurnacev2.alloyer.recipe_time");
    private static final Component OWN_RECIPES = Component.translatable("gui.fastfurnacev2.alloyer.own_recipes");

    private boolean infoVisible;

    public ExtremelyFastAlloyerScreen(ExtremelyFastAlloyerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    public List<Rect2i> getJeiExtraAreas() {
        if (!this.infoVisible) {
            return List.of();
        }
        return List.of(new Rect2i(
                this.leftPos + this.getInfoPanelX() - 1,
                this.topPos + INFO_BUTTON_Y - 1,
                INFO_PANEL_WIDTH + 2,
                INFO_PANEL_HEIGHT + 2));
    }

    private int getInfoPanelX() {
        int rightX = this.imageWidth + 4;
        if (this.leftPos + rightX + INFO_PANEL_WIDTH + 1 <= this.width) {
            return rightX;
        }
        int leftX = -INFO_PANEL_WIDTH - 4;
        if (this.leftPos + leftX - 1 >= 0) {
            return leftX;
        }
        return Math.max(1 - this.leftPos, this.width - this.leftPos - INFO_PANEL_WIDTH - 1);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);
        for (int slot = 0; slot < ExtremelyFastAlloyerMenu.INPUT_SLOT_COUNT; slot++) {
            drawSlot(graphics, x + ExtremelyFastAlloyerMenu.getInputX(slot),
                    y + ExtremelyFastAlloyerMenu.getInputY(slot));
        }
        drawSlot(graphics, x + 116, y + 35);
        int width = Mth.ceil(this.menu.getProgress() * 24.0F);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_SPRITE, 24, 16,
                0, 0, x + 79, y + 34, width, 16);
    }

    private static void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
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
        boolean hovered = this.isHovering(
                INFO_BUTTON_X, INFO_BUTTON_Y, INFO_BUTTON_SIZE, INFO_BUTTON_SIZE, mouseX, mouseY);
        graphics.fill(INFO_BUTTON_X - 1, INFO_BUTTON_Y - 1,
                INFO_BUTTON_X + INFO_BUTTON_SIZE + 1, INFO_BUTTON_Y + INFO_BUTTON_SIZE + 1, 0xFF373737);
        graphics.fill(INFO_BUTTON_X, INFO_BUTTON_Y,
                INFO_BUTTON_X + INFO_BUTTON_SIZE, INFO_BUTTON_Y + INFO_BUTTON_SIZE,
                hovered ? 0xFF6F6F6F : 0xFF4F4F4F);
        graphics.text(this.font, "i", INFO_BUTTON_X + 5, INFO_BUTTON_Y + 2, 0xFFFFFFFF, false);

        if (this.infoVisible) {
            int x = this.getInfoPanelX();
            int y = INFO_BUTTON_Y;
            graphics.fill(x - 1, y - 1, x + INFO_PANEL_WIDTH + 1, y + INFO_PANEL_HEIGHT + 1, 0xFFC6C6C6);
            graphics.fill(x, y, x + INFO_PANEL_WIDTH, y + INFO_PANEL_HEIGHT, 0xF0181818);
            graphics.text(this.font, INFO_TITLE, x + 6, y + 6, 0xFFFFFFFF, false);
            graphics.text(this.font, Component.translatable("gui.fastfurnacev2.speed", 100),
                    x + 6, y + 20, 0xFFE0E0E0, false);
            graphics.text(this.font, RECIPE_TIME, x + 6, y + 31, 0xFFE0E0E0, false);
            graphics.text(this.font, OWN_RECIPES, x + 6, y + 42, 0xFFE0E0E0, false);
        }
    }
}
