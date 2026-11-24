package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

final class MachineLogic {
    private MachineLogic() {
    }

    static int countHorizontalLava(Level level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.LAVA)) {
                count++;
            }
        }
        return count;
    }

    static int boltLevel(int progress, int duration, int maximum) {
        if (duration <= 0) {
            return 0;
        }
        long numerator = (long) Math.max(0, progress) * maximum + duration - 1L;
        return (int) Math.min(maximum, numerator / duration);
    }
}
