package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.EnergyConfigurableMenu;
import com.skylark193.fastfurnacev2.EnergyConfigurationPayload;
import com.skylark193.fastfurnacev2.EnergySideMode;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

final class EnergySideConfigurationControl {
    static final int WIDTH = 164;
    static final int HEIGHT = 90;
    private static final int BUTTON_WIDTH = 74;
    private static final int BUTTON_HEIGHT = 18;

    private EnergySideConfigurationControl() {
    }

    static void render(
            GuiGraphicsExtractor graphics,
            Font font,
            EnergyConfigurableMenu menu,
            int x,
            int y,
            int mouseX,
            int mouseY) {
        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, 0xFFC6C6C6);
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xF0181818);
        graphics.text(font, Component.translatable("gui.fastfurnacev2.energy_sides"), x + 6, y + 6, 0xFFFFFFFF, false);
        Direction[] directions = Direction.values();
        for (int index = 0; index < directions.length; index++) {
            Direction direction = directions[index];
            int buttonX = x + 6 + index % 2 * 78;
            int buttonY = y + 20 + index / 2 * 22;
            boolean hovered = isInside(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY);
            graphics.fill(buttonX - 1, buttonY - 1,
                    buttonX + BUTTON_WIDTH + 1, buttonY + BUTTON_HEIGHT + 1, 0xFF373737);
            graphics.fill(buttonX, buttonY, buttonX + BUTTON_WIDTH, buttonY + BUTTON_HEIGHT,
                    hovered ? 0xFF6F6F6F : 0xFF4F4F4F);
            EnergySideMode mode = menu.getEnergySideMode(direction);
            Component label = Component.translatable(
                    "gui.fastfurnacev2.energy_side_button",
                    Component.translatable("gui.fastfurnacev2.direction." + direction.getName()),
                    Component.translatable(mode.translationKey()));
            graphics.text(font, label, buttonX + (BUTTON_WIDTH - font.width(label)) / 2,
                    buttonY + 5, 0xFFFFFFFF, false);
        }
    }

    static boolean mouseClicked(
            EnergyConfigurableMenu menu, int x, int y, int mouseX, int mouseY) {
        Direction[] directions = Direction.values();
        for (int index = 0; index < directions.length; index++) {
            int buttonX = x + 6 + index % 2 * 78;
            int buttonY = y + 20 + index / 2 * 22;
            if (isInside(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, mouseX, mouseY)) {
                ClientPacketDistributor.sendToServer(
                        EnergyConfigurationPayload.cycleSide(menu.getEnergyBlockPos(), directions[index]));
                return true;
            }
        }
        return false;
    }

    private static boolean isInside(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
