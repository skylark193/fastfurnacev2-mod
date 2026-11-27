package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.FastFurnaceV2;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = FastFurnaceV2.MODID, dist = Dist.CLIENT)
public class FastFurnaceV2Client {
    public FastFurnaceV2Client(IEventBus modEventBus) {
        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerTintSources);
        modEventBus.addListener(this::addReloadListeners);
    }

    private void registerTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "crushed_ore"), CrushedOreTintSource.CODEC);
    }

    private void addReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "crushed_ore_colors"), new CrushedOreColors());
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(FastFurnaceV2.FAST_FURNACE_MENU.get(), FastFurnaceScreen::new);
        event.register(FastFurnaceV2.IRON_FAST_FURNACE_MENU.get(), FastFurnaceScreen::new);
        event.register(FastFurnaceV2.GOLD_FAST_FURNACE_MENU.get(), FastFurnaceScreen::new);
        event.register(FastFurnaceV2.DIAMOND_FAST_FURNACE_MENU.get(), FastFurnaceScreen::new);
        event.register(FastFurnaceV2.ACCELERATOR_MENU.get(), AcceleratorScreen::new);
        event.register(FastFurnaceV2.EXTREMELY_FAST_ALLOYER_MENU.get(), ExtremelyFastAlloyerScreen::new);
        event.register(FastFurnaceV2.FAST_GENERATOR_MENU.get(), FastGeneratorScreen::new);
        event.register(FastFurnaceV2.FAST_BATTERY_MENU.get(), FastBatteryScreen::new);
        event.register(FastFurnaceV2.FAST_POWER_CONDUIT_MENU.get(), FastPowerConduitScreen::new);
    }
}
