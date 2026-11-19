package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.FastFurnaceMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class GoldFastFurnaceScreen extends FastFurnaceScreen {
    public GoldFastFurnaceScreen(FastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
