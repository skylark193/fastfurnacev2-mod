package com.skylark193.fastfurnacev2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FastGeneratorBlock extends AbstractFurnaceBlock {
    private final FastFurnaceTier tier;
    private final MapCodec<FastGeneratorBlock> codec;

    public FastGeneratorBlock(BlockBehaviour.Properties properties, FastFurnaceTier tier) {
        super(properties);
        this.tier = tier;
        this.codec = simpleCodec(copy -> new FastGeneratorBlock(copy, tier));
    }

    public FastFurnaceTier getTier() {
        return this.tier;
    }

    @Override
    public MapCodec<FastGeneratorBlock> codec() {
        return this.codec;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FastGeneratorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel serverLevel
                ? createTickerHelper(type, FastFurnaceV2.FAST_GENERATOR_BLOCK_ENTITY.get(),
                        (innerLevel, pos, blockState, generator) ->
                                FastGeneratorBlockEntity.serverTick(serverLevel, pos, blockState, generator))
                : null;
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof FastGeneratorBlockEntity generator) {
            player.openMenu(generator);
        }
    }
}
