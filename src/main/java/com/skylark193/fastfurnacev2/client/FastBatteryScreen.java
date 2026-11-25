package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.FastBatteryMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class FastBatteryScreen extends AbstractContainerScreen<FastBatteryMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");

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
}
