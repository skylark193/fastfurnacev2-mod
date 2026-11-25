package com.skylark193.fastfurnacev2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FastBatteryBlock extends AbstractFurnaceBlock {
    public static final int MAX_CHARGE_LEVEL = 10;
    public static final IntegerProperty CHARGE_LEVEL = IntegerProperty.create("charge", 0, MAX_CHARGE_LEVEL);
    private final FastFurnaceTier tier;
    private final MapCodec<FastBatteryBlock> codec;

    public FastBatteryBlock(BlockBehaviour.Properties properties, FastFurnaceTier tier) {
        super(properties);
        this.tier = tier;
        this.codec = simpleCodec(copy -> new FastBatteryBlock(copy, tier));
        this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE_LEVEL, 0));
    }

    public FastFurnaceTier getTier() {
        return this.tier;
    }

    @Override
    public MapCodec<FastBatteryBlock> codec() {
        return this.codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, CHARGE_LEVEL);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FastBatteryBlockEntity(pos, state);
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof FastBatteryBlockEntity battery) {
            player.openMenu(battery);
        }
    }

    public static int chargeLevel(int energy, int capacity) {
        if (capacity <= 0 || energy <= 0) {
            return 0;
        }
        return Math.min(MAX_CHARGE_LEVEL,
                (int) ((energy * (long) MAX_CHARGE_LEVEL + capacity - 1L) / capacity));
    }
}
