package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.FastGeneratorMenu;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class FastGeneratorScreen extends AbstractContainerScreen<FastGeneratorMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier LIT_PROGRESS_SPRITE =
            Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final int ENERGY_X = 151;
    private static final int ENERGY_Y = 19;
    private static final int ENERGY_WIDTH = 16;
    private static final int ENERGY_HEIGHT = 42;
    private static final int INFO_BUTTON_X = 160;
    private static final int INFO_BUTTON_Y = 4;
    private static final int INFO_BUTTON_SIZE = 12;
    private static final int INFO_PANEL_WIDTH = 160;
    private static final int INFO_PANEL_HEIGHT = 68;
    private static final Component INFO_TITLE = Component.translatable("gui.fastfurnacev2.generator.info");
    private static final String STATUS = "gui.fastfurnacev2.generator.status";
    private static final Component IDLE = Component.translatable("gui.fastfurnacev2.generator.status.idle");
    private static final Component GENERATING = Component.translatable("gui.fastfurnacev2.generator.status.generating");
    private static final Component BUFFER_FULL = Component.translatable("gui.fastfurnacev2.generator.status.buffer_full");
    private static final Component CURRENT_BURN = Component.translatable("gui.fastfurnacev2.current_burn");
    private static final Component TOTAL_FUEL = Component.translatable("gui.fastfurnacev2.total_fuel");
    private static final Component SECONDS = Component.translatable("gui.fastfurnacev2.seconds");

    private boolean infoVisible;

    public FastGeneratorScreen(FastGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);
        graphics.fill(x + 8, y + 17, x + 144, y + 77, 0xFFC6C6C6);
        drawSlot(graphics, x + FastGeneratorMenu.FUEL_SLOT_X, y + FastGeneratorMenu.FUEL_SLOT_Y);
        if (this.menu.getBurnTime() > 0) {
            int height = Mth.ceil(this.menu.getBurnProgress() * 13.0F) + 1;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14,
                    0, 14 - height, x + FastGeneratorMenu.FUEL_SLOT_X, y + 36 + 14 - height, 14, height);
        }
        int energyHeight = Mth.ceil(this.menu.getEnergyProgress() * 40.0F);
        graphics.fill(x + ENERGY_X, y + ENERGY_Y,
                x + ENERGY_X + ENERGY_WIDTH, y + ENERGY_Y + ENERGY_HEIGHT, 0xFF202020);
        graphics.fill(x + ENERGY_X + 2, y + 59 - energyHeight, x + ENERGY_X + 14, y + 59, 0xFFE08020);
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
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
        Component output = Component.translatable("gui.fastfurnacev2.generator.output", this.menu.getOutput());
        graphics.text(this.font, output,
                FastGeneratorMenu.FUEL_SLOT_X + 8 - this.font.width(output) / 2,
                FastGeneratorMenu.FUEL_SLOT_Y + 20, 0xFF404040, false);

        boolean infoHovered = this.isHovering(
                INFO_BUTTON_X, INFO_BUTTON_Y, INFO_BUTTON_SIZE, INFO_BUTTON_SIZE, mouseX, mouseY);
        graphics.fill(INFO_BUTTON_X - 1, INFO_BUTTON_Y - 1,
                INFO_BUTTON_X + INFO_BUTTON_SIZE + 1, INFO_BUTTON_Y + INFO_BUTTON_SIZE + 1, 0xFF373737);
        graphics.fill(INFO_BUTTON_X, INFO_BUTTON_Y,
                INFO_BUTTON_X + INFO_BUTTON_SIZE, INFO_BUTTON_Y + INFO_BUTTON_SIZE,
                infoHovered ? 0xFF6F6F6F : 0xFF4F4F4F);
        graphics.text(this.font, "i", INFO_BUTTON_X + 5, INFO_BUTTON_Y + 2, 0xFFFFFFFF, false);

        if (this.isHovering(ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable(
                    "gui.fastfurnacev2.generator.capacity", this.menu.getCapacity()), mouseX, mouseY);
        }
        if (this.infoVisible) {
            int x = this.getInfoPanelX();
            int y = INFO_BUTTON_Y;
            graphics.fill(x - 1, y - 1, x + INFO_PANEL_WIDTH + 1, y + INFO_PANEL_HEIGHT + 1, 0xFFC6C6C6);
            graphics.fill(x, y, x + INFO_PANEL_WIDTH, y + INFO_PANEL_HEIGHT, 0xF0181818);
            graphics.text(this.font, INFO_TITLE, x + 6, y + 6, 0xFFFFFFFF, false);
            graphics.text(this.font, output, x + 6, y + 20, 0xFFE0E0E0, false);
            graphics.text(this.font, Component.translatable(STATUS, this.getStatus()),
                    x + 6, y + 31, 0xFFE0E0E0, false);
            this.drawTime(graphics, CURRENT_BURN, this.menu.getBurnTime(), x, y + 44);
            this.drawTime(graphics, TOTAL_FUEL, this.menu.getTotalBurnTime(), x, y + 55);
        }
    }

    private Component getStatus() {
        if (this.menu.getBurnTime() <= 0) {
            return IDLE;
        }
        return this.menu.getCapacity() - this.menu.getEnergy() < this.menu.getOutput() ? BUFFER_FULL : GENERATING;
    }

    private void drawTime(GuiGraphicsExtractor graphics, Component label, int ticks, int x, int y) {
        String value = String.format(Locale.ROOT, "%.1f", ticks / 20.0);
        int labelX = x + 6;
        int valueX = labelX + this.font.width(label) + this.font.width(" ");
        int secondsX = valueX + this.font.width(value) + this.font.width(" ");
        graphics.text(this.font, label, labelX, y, 0xFFE0E0E0, false);
        graphics.text(this.font, value, valueX, y, 0xFFE0E0E0, false);
        graphics.text(this.font, SECONDS, secondsX, y, 0xFFE0E0E0, false);
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
}
