package com.skylark193.fastfurnacev2;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jspecify.annotations.Nullable;

public class FastPowerConduitBlock extends PipeBlock implements EntityBlock {
    private final FastFurnaceTier tier;
    private final MapCodec<FastPowerConduitBlock> codec;

    public FastPowerConduitBlock(BlockBehaviour.Properties properties, FastFurnaceTier tier) {
        super(0.3125F, properties);
        this.tier = tier;
        this.codec = simpleCodec(copy -> new FastPowerConduitBlock(copy, tier));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false));
    }

    public FastFurnaceTier getTier() {
        return this.tier;
    }

    @Override
    public MapCodec<FastPowerConduitBlock> codec() {
        return this.codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FastPowerConduitBlockEntity(pos, state);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.withConnections(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(directionToNeighbour),
                this.canConnect(level, neighbourPos, directionToNeighbour));
    }

    private BlockState withConnections(Level level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(PROPERTY_BY_DIRECTION.get(direction),
                    this.canConnect(level, pos.relative(direction), direction));
        }
        return state;
    }

    private boolean canConnect(LevelReader level, BlockPos pos, Direction direction) {
        if (level.getBlockState(pos).getBlock() instanceof FastPowerConduitBlock) {
            return true;
        }
        return level instanceof Level actualLevel
                && actualLevel.getCapability(Capabilities.Energy.BLOCK, pos, direction.getOpposite()) != null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel serverLevel)
                || type != FastFurnaceV2.FAST_POWER_CONDUIT_BLOCK_ENTITY.get()) {
            return null;
        }
        return (innerLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof FastPowerConduitBlockEntity conduit) {
                FastPowerConduitBlockEntity.serverTick(serverLevel, pos, blockState, conduit);
            }
        };
    }
}
