package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.AcceleratorBlockEntity;
import com.skylark193.fastfurnacev2.AcceleratorMenu;
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

public class AcceleratorScreen extends AbstractContainerScreen<AcceleratorMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier LIT_PROGRESS_SPRITE =
            Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier BURN_PROGRESS_SPRITE =
            Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final int INFO_BUTTON_X = 160;
    private static final int INFO_BUTTON_Y = 4;
    private static final int INFO_BUTTON_SIZE = 12;
    private static final int INFO_PANEL_WIDTH = 176;
    private static final int INFO_PANEL_HEIGHT = 111;
    private static final Component INFO_TITLE = Component.translatable("gui.fastfurnacev2.accelerator.info");
    private static final Component COAL_RECIPE =
            Component.translatable("gui.fastfurnacev2.accelerator.coal_recipe");
    private static final Component BLOCK_RECIPE =
            Component.translatable("gui.fastfurnacev2.accelerator.block_recipe");
    private static final Component FAST_COAL_CAPACITY =
            Component.translatable("gui.fastfurnacev2.accelerator.fast_coal_capacity");
    private static final Component FAST_COAL_BLOCK_CAPACITY =
            Component.translatable("gui.fastfurnacev2.accelerator.fast_coal_block_capacity");
    private static final Component EXTREME_COAL_CAPACITY =
            Component.translatable("gui.fastfurnacev2.accelerator.extreme_coal_capacity");

    private boolean infoVisible;

    public AcceleratorScreen(AcceleratorMenu menu, Inventory inventory, Component title) {
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
        if (this.menu.isBoosted()) {
            int height = Mth.ceil(this.menu.getBoostProgress() * 13.0F) + 1;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14,
                    0, 14 - height, x + 56, y + 36 + 14 - height, 14, height);
        }
        int width = Mth.ceil(this.menu.getProgress() * 24.0F);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_SPRITE, 24, 16,
                0, 0, x + 79, y + 34, width, 16);
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
            Component lava = Component.translatable(
                    "gui.fastfurnacev2.accelerator.lava_sides", this.menu.getLavaSideCount());
            graphics.text(this.font, lava, x + 6, y + 20,
                    this.menu.getLavaSideCount() == AcceleratorBlockEntity.REQUIRED_LAVA_SIDES
                            ? 0xFF80FF80 : 0xFFFF8080, false);
            Component speed = Component.translatable(
                    "gui.fastfurnacev2.speed", this.menu.getBoostWorkPerTick() * 100);
            graphics.text(this.font, speed, x + 6, y + 31, 0xFFE0E0E0, false);
            graphics.text(this.font, Component.translatable(
                            "gui.fastfurnacev2.accelerator.boost_work", this.menu.getBoostWork()),
                    x + 6, y + 42, 0xFFE0E0E0, false);
            graphics.text(this.font, COAL_RECIPE, x + 6, y + 56, 0xFFE0E0E0, false);
            graphics.text(this.font, BLOCK_RECIPE, x + 6, y + 67, 0xFFE0E0E0, false);
            graphics.text(this.font, FAST_COAL_CAPACITY, x + 6, y + 78, 0xFFE0E0E0, false);
            graphics.text(this.font, FAST_COAL_BLOCK_CAPACITY, x + 6, y + 89, 0xFFE0E0E0, false);
            graphics.text(this.font, EXTREME_COAL_CAPACITY, x + 6, y + 100, 0xFFE0E0E0, false);
        }
    }
}
