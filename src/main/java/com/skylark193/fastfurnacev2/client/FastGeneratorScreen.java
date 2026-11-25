package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.FastGeneratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class FastGeneratorScreen extends AbstractContainerScreen<FastGeneratorMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier LIT_PROGRESS_SPRITE =
            Identifier.withDefaultNamespace("container/furnace/lit_progress");

    public FastGeneratorScreen(FastGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.getBurnTime() > 0) {
            int height = Mth.ceil(this.menu.getBurnProgress() * 13.0F) + 1;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14,
                    0, 14 - height, x + 80, y + 36 + 14 - height, 14, height);
        }
        int energyHeight = Mth.ceil(this.menu.getEnergyProgress() * 40.0F);
        graphics.fill(x + 151, y + 19, x + 167, y + 61, 0xFF202020);
        graphics.fill(x + 153, y + 59 - energyHeight, x + 165, y + 59, 0xFFE08020);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(this.font, Component.translatable(
                        "gui.fastfurnacev2.generator.output", this.menu.getOutput()),
                8, 21, 0xFF404040, false);
        graphics.text(this.font, Component.translatable(
                        "gui.fastfurnacev2.energy", this.menu.getEnergy(), this.menu.getCapacity()),
                8, 68, 0xFF404040, false);
    }
}
