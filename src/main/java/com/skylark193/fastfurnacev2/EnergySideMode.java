package com.skylark193.fastfurnacev2;

/** Energy permissions from the configured block's perspective. */
public enum EnergySideMode {
    INPUT,
    OUTPUT,
    BOTH;

    public boolean allowsInput() {
        return this != OUTPUT;
    }

    public boolean allowsOutput() {
        return this != INPUT;
    }

    public EnergySideMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    public String translationKey() {
        return "gui.fastfurnacev2.energy_side." + this.name().toLowerCase(java.util.Locale.ROOT);
    }

    static EnergySideMode fromSerialized(int value) {
        return value >= 0 && value < values().length ? values()[value] : BOTH;
    }
}
