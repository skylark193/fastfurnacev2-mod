package com.skylark193.fastfurnacev2.client;

import com.skylark193.fastfurnacev2.FastFurnaceMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class IronFastFurnaceScreen extends FastFurnaceScreen {
    public IronFastFurnaceScreen(FastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
