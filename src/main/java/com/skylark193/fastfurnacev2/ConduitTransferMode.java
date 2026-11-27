package com.skylark193.fastfurnacev2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.RandomSource;

public enum ConduitTransferMode {
    FILL_FIRST_FOUND {
        @Override
        <T> List<T> order(List<T> values, long tick, RandomSource random) {
            return List.copyOf(values);
        }
    },
    ROUND_ROBIN {
        @Override
        <T> List<T> order(List<T> values, long tick, RandomSource random) {
            List<T> ordered = new ArrayList<>(values);
            if (!ordered.isEmpty()) {
                Collections.rotate(ordered, -(int) Math.floorMod(tick, ordered.size()));
            }
            return ordered;
        }
    },
    RANDOM {
        @Override
        <T> List<T> order(List<T> values, long tick, RandomSource random) {
            List<T> ordered = new ArrayList<>(values);
            for (int index = ordered.size() - 1; index > 0; index--) {
                int other = random.nextInt(index + 1);
                Collections.swap(ordered, index, other);
            }
            return ordered;
        }
    };

    abstract <T> List<T> order(List<T> values, long tick, RandomSource random);

    public ConduitTransferMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    public String translationKey() {
        return "gui.fastfurnacev2.transfer_mode." + this.name().toLowerCase(java.util.Locale.ROOT);
    }

    static ConduitTransferMode fromSerialized(int value) {
        return value >= 0 && value < values().length ? values()[value] : FILL_FIRST_FOUND;
    }
}
