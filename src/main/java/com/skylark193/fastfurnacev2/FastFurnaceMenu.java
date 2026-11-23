package com.skylark193.fastfurnacev2;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;

public class FastFurnaceMenu extends AbstractFurnaceMenu {
    public static final int MENU_DATA_COUNT = 11;
    private static final int DATA_BURN_TIME_LOW = 4;
    private static final int DATA_BURN_TIME_HIGH = 5;
    private static final int DATA_FUEL_TIME_LOW = 6;
    private static final int DATA_FUEL_TIME_HIGH = 7;
    private static final int DATA_FAST_COAL_ACTIVE = 8;
    private static final int DATA_FAST_CRUSHER = 9;

    private final ContainerData data;
    private final FastFurnaceTier tier;

    public FastFurnaceMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, FastFurnaceTier.FAST);
    }

    public FastFurnaceMenu(int containerId, Inventory inventory, FastFurnaceTier tier) {
        this(containerId, inventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(MENU_DATA_COUNT), tier);
    }

    public FastFurnaceMenu(int containerId, Inventory inventory, Container container, ContainerData data, FastFurnaceTier tier) {
        super(
                FastFurnaceV2.getFurnaceMenu(tier).get(),
                RecipeType.SMELTING,
                RecipePropertySet.FURNACE_INPUT,
                RecipeBookType.FURNACE,
                containerId,
                inventory,
                container,
                data);
        this.data = data;
        this.tier = tier;
    }

    public FastFurnaceTier getTier() {
        return this.tier;
    }

    public int getCurrentBurnTime() {
        return joinInt(this.data.get(DATA_BURN_TIME_LOW), this.data.get(DATA_BURN_TIME_HIGH));
    }

    public int getTotalFuelTime() {
        return joinInt(this.data.get(DATA_FUEL_TIME_LOW), this.data.get(DATA_FUEL_TIME_HIGH));
    }

    public boolean isFastCoalActive() {
        return this.data.get(DATA_FAST_COAL_ACTIVE) != 0;
    }

    public boolean isExtremeCoalActive() {
        return this.data.get(DATA_FAST_COAL_ACTIVE) == 2;
    }

    public boolean isFastCrusher() {
        return this.data.get(DATA_FAST_CRUSHER) != 0;
    }

    /** Returns the tier speed including any synchronized custom or built-in fuel bonus. */
    public int getSpeedPercent() {
        return this.data.get(10);
    }

    private static int joinInt(int low, int high) {
        return (high << 16) | (low & 0xFFFF);
    }
}
