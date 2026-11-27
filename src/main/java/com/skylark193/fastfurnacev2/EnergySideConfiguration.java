package com.skylark193.fastfurnacev2;

import net.minecraft.core.Direction;

final class EnergySideConfiguration {
    static final int ALL_BOTH = 0b10_10_10_10_10_10;
    static final int ALL_OUTPUT = 0b01_01_01_01_01_01;

    private int modes;

    EnergySideConfiguration(EnergySideMode defaultMode) {
        this.modes = defaultMode == EnergySideMode.OUTPUT ? ALL_OUTPUT : ALL_BOTH;
    }

    EnergySideMode get(Direction direction) {
        return EnergySideMode.fromSerialized(this.modes >>> (direction.ordinal() * 2) & 0b11);
    }

    EnergySideMode cycle(Direction direction) {
        EnergySideMode next = this.get(direction).next();
        int shift = direction.ordinal() * 2;
        this.modes = this.modes & ~(0b11 << shift) | next.ordinal() << shift;
        return next;
    }

    int serialize() {
        return this.modes;
    }

    void deserialize(int modes) {
        this.modes = modes;
        for (Direction direction : Direction.values()) {
            int shift = direction.ordinal() * 2;
            if ((modes >>> shift & 0b11) >= EnergySideMode.values().length) {
                this.modes = this.modes & ~(0b11 << shift) | EnergySideMode.BOTH.ordinal() << shift;
            }
        }
    }
}
