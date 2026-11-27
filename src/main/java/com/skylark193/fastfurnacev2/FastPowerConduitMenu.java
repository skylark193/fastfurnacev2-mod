package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class FastPowerConduitMenu extends AbstractContainerMenu implements EnergyConfigurableMenu {
    public static final int DATA_COUNT = Direction.values().length + 1;
    private final BlockPos pos;
    private final ContainerData data;
    private final @Nullable FastPowerConduitBlockEntity conduit;

    public FastPowerConduitMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, pos, new SimpleContainerData(DATA_COUNT), null);
        for (Direction direction : Direction.values()) {
            this.data.set(direction.ordinal(), EnergySideMode.BOTH.ordinal());
        }
    }

    public FastPowerConduitMenu(
            int containerId, Inventory inventory, FastPowerConduitBlockEntity conduit) {
        this(containerId, inventory, conduit.getBlockPos(), new ContainerData() {
            @Override
            public int get(int index) {
                return index < Direction.values().length
                        ? conduit.getConnectionMode(Direction.values()[index]).ordinal()
                        : conduit.getTransferMode().ordinal();
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        }, conduit);
    }

    private FastPowerConduitMenu(
            int containerId,
            Inventory inventory,
            BlockPos pos,
            ContainerData data,
            @Nullable FastPowerConduitBlockEntity conduit) {
        super(FastFurnaceV2.FAST_POWER_CONDUIT_MENU.get(), containerId);
        this.pos = pos;
        this.data = data;
        this.conduit = conduit;
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.conduit == null || !this.conduit.isRemoved()
                && player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockPos getEnergyBlockPos() {
        return this.pos;
    }

    @Override
    public EnergySideMode getEnergySideMode(Direction direction) {
        return EnergySideMode.fromSerialized(this.data.get(direction.ordinal()));
    }

    public ConduitTransferMode getTransferMode() {
        return ConduitTransferMode.fromSerialized(this.data.get(Direction.values().length));
    }
}
