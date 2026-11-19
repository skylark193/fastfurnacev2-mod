package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.FastFurnaceV2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = FastFurnaceV2.MODID, dist = Dist.CLIENT)
public class FastFurnaceV2Client {
    public FastFurnaceV2Client(IEventBus modEventBus) {
        modEventBus.addListener(this::registerScreens);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(FastFurnaceV2.FAST_FURNACE_MENU.get(), FastFurnaceScreen::new);
        event.register(FastFurnaceV2.IRON_FAST_FURNACE_MENU.get(), IronFastFurnaceScreen::new);
        event.register(FastFurnaceV2.GOLD_FAST_FURNACE_MENU.get(), GoldFastFurnaceScreen::new);
        event.register(FastFurnaceV2.DIAMOND_FAST_FURNACE_MENU.get(), DiamondFastFurnaceScreen::new);
    }
}
