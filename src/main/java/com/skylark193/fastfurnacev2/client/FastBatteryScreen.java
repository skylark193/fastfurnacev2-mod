package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.FastBatteryMenu;
import java.util.ArrayList;
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

public class FastBatteryScreen extends AbstractContainerScreen<FastBatteryMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final int BUTTON_X = 160;
    private static final int INFO_BUTTON_Y = 4;
    private static final int CONFIG_BUTTON_Y = 18;
    private static final int BUTTON_SIZE = 12;
    private static final int INFO_PANEL_WIDTH = 160;
    private static final int INFO_PANEL_HEIGHT = 44;

    private boolean infoVisible;
    private boolean configurationVisible;

    public FastBatteryScreen(FastBatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);
        graphics.fill(x + 8, y + 17, x + 168, y + 76, 0xFF303030);
        int width = Mth.ceil(this.menu.getEnergyProgress() * 144.0F);
        graphics.fill(x + 16, y + 45, x + 16 + width, y + 57, 0xFFE08020);
        graphics.text(this.font, Component.translatable(
                        "gui.fastfurnacev2.energy", this.menu.getEnergy(), this.menu.getCapacity()),
                x + 16, y + 28, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            if (this.isHovering(BUTTON_X, INFO_BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE, event.x(), event.y())) {
                this.infoVisible = !this.infoVisible;
                return true;
            }
            if (this.isHovering(BUTTON_X, CONFIG_BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE, event.x(), event.y())) {
                this.configurationVisible = !this.configurationVisible;
                return true;
            }
            if (this.configurationVisible && EnergySideConfigurationControl.mouseClicked(
                    this.menu, this.getConfigurationPanelX(), this.getConfigurationPanelY(),
                    (int) event.x() - this.leftPos, (int) event.y() - this.topPos)) {
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        this.drawButton(graphics, INFO_BUTTON_Y, "i", mouseX, mouseY);
        this.drawButton(graphics, CONFIG_BUTTON_Y, "E", mouseX, mouseY);
        if (this.isHovering(BUTTON_X, CONFIG_BUTTON_Y, BUTTON_SIZE, BUTTON_SIZE, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(
                    Component.translatable("gui.fastfurnacev2.energy_sides"), mouseX, mouseY);
        }
        if (this.infoVisible) {
            int x = this.getInfoPanelX();
            int y = INFO_BUTTON_Y;
            graphics.fill(x - 1, y - 1, x + INFO_PANEL_WIDTH + 1, y + INFO_PANEL_HEIGHT + 1, 0xFFC6C6C6);
            graphics.fill(x, y, x + INFO_PANEL_WIDTH, y + INFO_PANEL_HEIGHT, 0xF0181818);
            graphics.text(this.font, Component.translatable("gui.fastfurnacev2.battery.info"),
                    x + 6, y + 6, 0xFFFFFFFF, false);
            graphics.text(this.font, Component.translatable(
                            "gui.fastfurnacev2.energy", this.menu.getEnergy(), this.menu.getCapacity()),
                    x + 6, y + 20, 0xFFE0E0E0, false);
            graphics.text(this.font, Component.translatable("tooltip.fastfurnacev2.battery.transfer",
                            this.menu.getTier().getBatteryTransferLimit()),
                    x + 6, y + 31, 0xFFE0E0E0, false);
        }
        if (this.configurationVisible) {
            EnergySideConfigurationControl.render(graphics, this.font, this.menu,
                    this.getConfigurationPanelX(), this.getConfigurationPanelY(),
                    mouseX - this.leftPos, mouseY - this.topPos);
        }
    }

    private void drawButton(GuiGraphicsExtractor graphics, int y, String label, int mouseX, int mouseY) {
        boolean hovered = this.isHovering(BUTTON_X, y, BUTTON_SIZE, BUTTON_SIZE, mouseX, mouseY);
        graphics.fill(BUTTON_X - 1, y - 1, BUTTON_X + BUTTON_SIZE + 1, y + BUTTON_SIZE + 1, 0xFF373737);
        graphics.fill(BUTTON_X, y, BUTTON_X + BUTTON_SIZE, y + BUTTON_SIZE,
                hovered ? 0xFF6F6F6F : 0xFF4F4F4F);
        graphics.text(this.font, label, BUTTON_X + (BUTTON_SIZE - this.font.width(label)) / 2,
                y + 2, 0xFFFFFFFF, false);
    }

    public List<Rect2i> getJeiExtraAreas() {
        List<Rect2i> areas = new ArrayList<>();
        if (this.infoVisible) {
            areas.add(new Rect2i(this.leftPos + this.getInfoPanelX() - 1, this.topPos + INFO_BUTTON_Y - 1,
                    INFO_PANEL_WIDTH + 2, INFO_PANEL_HEIGHT + 2));
        }
        if (this.configurationVisible) {
            areas.add(new Rect2i(this.leftPos + this.getConfigurationPanelX() - 1,
                    this.topPos + this.getConfigurationPanelY() - 1,
                    EnergySideConfigurationControl.WIDTH + 2, EnergySideConfigurationControl.HEIGHT + 2));
        }
        return areas;
    }

    private int getInfoPanelX() {
        return this.getPanelX(INFO_PANEL_WIDTH);
    }

    private int getConfigurationPanelX() {
        return this.getPanelX(EnergySideConfigurationControl.WIDTH);
    }

    private int getConfigurationPanelY() {
        return this.infoVisible ? INFO_BUTTON_Y + INFO_PANEL_HEIGHT + 4 : CONFIG_BUTTON_Y;
    }

    private int getPanelX(int panelWidth) {
        int rightX = this.imageWidth + 4;
        if (this.leftPos + rightX + panelWidth + 1 <= this.width) {
            return rightX;
        }
        int leftX = -panelWidth - 4;
        if (this.leftPos + leftX - 1 >= 0) {
            return leftX;
        }
        return Math.max(1 - this.leftPos, this.width - this.leftPos - panelWidth - 1);
    }
}
