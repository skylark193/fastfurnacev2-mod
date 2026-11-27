package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.EnergyConfigurationPayload;
import com.skylark193.fastfurnacev2.FastPowerConduitMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class FastPowerConduitScreen extends AbstractContainerScreen<FastPowerConduitMenu> {
    private static final int NETWORK_BUTTON_X = 8;
    private static final int NETWORK_BUTTON_Y = 22;
    private static final int NETWORK_BUTTON_WIDTH = 160;
    private static final int NETWORK_BUTTON_HEIGHT = 18;
    private static final int SIDES_X = 6;
    private static final int SIDES_Y = 50;

    public FastPowerConduitScreen(FastPowerConduitMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 148);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x - 1, y - 1, x + this.imageWidth + 1, y + this.imageHeight + 1, 0xFFC6C6C6);
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xF0181818);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFFFF, false);
        boolean hovered = this.isHovering(NETWORK_BUTTON_X, NETWORK_BUTTON_Y,
                NETWORK_BUTTON_WIDTH, NETWORK_BUTTON_HEIGHT, mouseX, mouseY);
        graphics.fill(NETWORK_BUTTON_X - 1, NETWORK_BUTTON_Y - 1,
                NETWORK_BUTTON_X + NETWORK_BUTTON_WIDTH + 1, NETWORK_BUTTON_Y + NETWORK_BUTTON_HEIGHT + 1,
                0xFF373737);
        graphics.fill(NETWORK_BUTTON_X, NETWORK_BUTTON_Y,
                NETWORK_BUTTON_X + NETWORK_BUTTON_WIDTH, NETWORK_BUTTON_Y + NETWORK_BUTTON_HEIGHT,
                hovered ? 0xFF6F6F6F : 0xFF4F4F4F);
        Component mode = Component.translatable("gui.fastfurnacev2.network_transfer",
                Component.translatable(this.menu.getTransferMode().translationKey()));
        graphics.text(this.font, mode, NETWORK_BUTTON_X + (NETWORK_BUTTON_WIDTH - this.font.width(mode)) / 2,
                NETWORK_BUTTON_Y + 5, 0xFFFFFFFF, false);
        EnergySideConfigurationControl.render(graphics, this.font, this.menu,
                SIDES_X, SIDES_Y, mouseX - this.leftPos, mouseY - this.topPos);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int mouseX = (int) event.x() - this.leftPos;
            int mouseY = (int) event.y() - this.topPos;
            if (mouseX >= NETWORK_BUTTON_X && mouseX < NETWORK_BUTTON_X + NETWORK_BUTTON_WIDTH
                    && mouseY >= NETWORK_BUTTON_Y && mouseY < NETWORK_BUTTON_Y + NETWORK_BUTTON_HEIGHT) {
                ClientPacketDistributor.sendToServer(
                        EnergyConfigurationPayload.cycleNetworkMode(this.menu.getEnergyBlockPos()));
                return true;
            }
            if (EnergySideConfigurationControl.mouseClicked(this.menu, SIDES_X, SIDES_Y, mouseX, mouseY)) {
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
