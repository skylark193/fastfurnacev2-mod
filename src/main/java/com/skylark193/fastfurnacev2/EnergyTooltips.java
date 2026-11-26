package com.skylark193.fastfurnacev2;

import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

final class EnergyTooltips {
    private EnergyTooltips() {
    }

    static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof BlockItem blockItem)) {
            return;
        }
        switch (blockItem.getBlock()) {
            case FastGeneratorBlock generator -> addGenerator(generator.getTier(), event.getToolTip()::add);
            case FastBatteryBlock battery -> addBattery(battery.getTier(), event.getToolTip()::add);
            case FastPowerConduitBlock conduit -> addConduit(conduit.getTier(), event.getToolTip()::add);
            default -> {
            }
        }
    }

    static void addGenerator(FastFurnaceTier tier, Consumer<Component> tooltip) {
        tooltip.accept(line("generator.generation", tier.getGeneratorOutput()));
        tooltip.accept(line("generator.storage", tier.getGeneratorBufferCapacity()));
        tooltip.accept(line("generator.transfer", tier.getGeneratorExtractionLimit()));
    }

    static void addBattery(FastFurnaceTier tier, Consumer<Component> tooltip) {
        tooltip.accept(line("battery.storage", tier.getBatteryCapacity()));
        tooltip.accept(line("battery.transfer", tier.getBatteryTransferLimit()));
    }

    static void addConduit(FastFurnaceTier tier, Consumer<Component> tooltip) {
        tooltip.accept(line("conduit.transfer", tier.getConduitTransferRate()));
    }

    private static Component line(String key, int amount) {
        return Component.translatable("tooltip.fastfurnacev2." + key, formatEnergy(amount))
                .withStyle(ChatFormatting.GRAY);
    }

    private static String formatEnergy(int amount) {
        return String.format(Locale.ROOT, "%,d", amount);
    }
}
