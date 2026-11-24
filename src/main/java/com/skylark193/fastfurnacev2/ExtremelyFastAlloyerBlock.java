package com.skylark193.fastfurnacev2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jspecify.annotations.Nullable;

public class ExtremelyFastAlloyerBlock extends AbstractFurnaceBlock {
    public static final int MAX_BOLT_LEVEL = AcceleratorBlock.MAX_BOLT_LEVEL;
    public static final IntegerProperty BOLT_LEVEL = AcceleratorBlock.BOLT_LEVEL;
    public static final MapCodec<ExtremelyFastAlloyerBlock> CODEC = simpleCodec(ExtremelyFastAlloyerBlock::new);

    public ExtremelyFastAlloyerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, net.minecraft.core.Direction.NORTH)
                .setValue(LIT, false)
                .setValue(BOLT_LEVEL, 0));
    }

    @Override
    public MapCodec<ExtremelyFastAlloyerBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, BOLT_LEVEL);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtremelyFastAlloyerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel serverLevel
                ? createTickerHelper(
                        type,
                        FastFurnaceV2.EXTREMELY_FAST_ALLOYER_BLOCK_ENTITY.get(),
                        (innerLevel, pos, blockState, entity) ->
                                ExtremelyFastAlloyerBlockEntity.serverTick(serverLevel, pos, blockState, entity))
                : null;
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof ExtremelyFastAlloyerBlockEntity blockEntity) {
            player.openMenu(blockEntity);
        }
    }
}
