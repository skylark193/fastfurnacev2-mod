package com.skylark193.fastfurnacev2.compat.jade;

import com.skylark193.fastfurnacev2.AcceleratorBlock;
import com.skylark193.fastfurnacev2.AcceleratorBlockEntity;
import com.skylark193.fastfurnacev2.ConduitTransferMode;
import com.skylark193.fastfurnacev2.ExtremelyFastAlloyerBlock;
import com.skylark193.fastfurnacev2.ExtremelyFastAlloyerBlockEntity;
import com.skylark193.fastfurnacev2.FastBatteryBlock;
import com.skylark193.fastfurnacev2.FastBatteryBlockEntity;
import com.skylark193.fastfurnacev2.FastFurnaceBlock;
import com.skylark193.fastfurnacev2.FastFurnaceBlockEntity;
import com.skylark193.fastfurnacev2.FastFurnaceV2;
import com.skylark193.fastfurnacev2.FastGeneratorBlock;
import com.skylark193.fastfurnacev2.FastGeneratorBlockEntity;
import com.skylark193.fastfurnacev2.FastPowerConduitBlock;
import com.skylark193.fastfurnacev2.FastPowerConduitBlockEntity;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public final class FastFurnaceJadePlugin implements IWailaPlugin {
    private static final MachineProvider PROVIDER = new MachineProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(PROVIDER, FastFurnaceBlockEntity.class);
        registration.registerBlockDataProvider(PROVIDER, AcceleratorBlockEntity.class);
        registration.registerBlockDataProvider(PROVIDER, ExtremelyFastAlloyerBlockEntity.class);
        registration.registerBlockDataProvider(PROVIDER, FastGeneratorBlockEntity.class);
        registration.registerBlockDataProvider(PROVIDER, FastBatteryBlockEntity.class);
        registration.registerBlockDataProvider(PROVIDER, FastPowerConduitBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PROVIDER, FastFurnaceBlock.class);
        registration.registerBlockComponent(PROVIDER, AcceleratorBlock.class);
        registration.registerBlockComponent(PROVIDER, ExtremelyFastAlloyerBlock.class);
        registration.registerBlockComponent(PROVIDER, FastGeneratorBlock.class);
        registration.registerBlockComponent(PROVIDER, FastBatteryBlock.class);
        registration.registerBlockComponent(PROVIDER, FastPowerConduitBlock.class);
    }

    private static final class MachineProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
        private static final Identifier UID = Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "machine_status");
        private static final String PREFIX = "fastfurnacev2.jade.";
        private static final String TYPE = PREFIX + "type";
        private static final String BURN = PREFIX + "burn";
        private static final String TOTAL_BURN = PREFIX + "total_burn";
        private static final String BURN_PERCENT = PREFIX + "burn_percent";
        private static final String PROGRESS = PREFIX + "progress";
        private static final String TOTAL_PROGRESS = PREFIX + "total_progress";
        private static final String SPEED = PREFIX + "speed";
        private static final String PENDING_DOUBLE = PREFIX + "pending_double";
        private static final String BOOST = PREFIX + "boost";
        private static final String TOTAL_BOOST = PREFIX + "total_boost";
        private static final String LAVA = PREFIX + "lava";
        private static final String ENERGY = PREFIX + "energy";
        private static final String CAPACITY = PREFIX + "capacity";
        private static final String TRANSFER_RATE = PREFIX + "transfer_rate";
        private static final String NETWORK_MODE = PREFIX + "network_mode";

        @Override
        public Identifier getUid() {
            return UID;
        }

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            switch (accessor.getBlockEntity()) {
                case FastFurnaceBlockEntity furnace -> {
                    tag.putString(TYPE, furnace.isFastCrusher() ? "crusher" : "furnace");
                    tag.putInt(BURN, furnace.getCurrentBurnTime());
                    tag.putInt(TOTAL_BURN, furnace.getTotalFuelTime());
                    tag.putInt(PROGRESS, furnace.getCookingProgress());
                    tag.putInt(TOTAL_PROGRESS, furnace.getCookingTotalTime());
                    tag.putInt(SPEED, furnace.getSpeedPercent());
                    tag.putBoolean(PENDING_DOUBLE, furnace.isCrusherDoubleOutputPending());
                }
                case AcceleratorBlockEntity accelerator -> {
                    tag.putString(TYPE, "accelerator");
                    tag.putInt(PROGRESS, accelerator.getWorkProgress());
                    tag.putInt(TOTAL_PROGRESS, accelerator.getRecipeTicks());
                    tag.putInt(BOOST, accelerator.getBoostWork());
                    tag.putInt(TOTAL_BOOST, accelerator.getTotalBoostWork());
                    tag.putInt(SPEED, accelerator.getBoostWork() > 0 ? accelerator.getBoostWorkPerTick() : 1);
                    tag.putInt(LAVA, accelerator.getLavaSideCount());
                }
                case ExtremelyFastAlloyerBlockEntity alloyer -> {
                    tag.putString(TYPE, "alloyer");
                    tag.putInt(BURN, alloyer.getBurnTimeRemaining());
                    tag.putInt(TOTAL_BURN, alloyer.getTotalBurnTime());
                    tag.putInt(PROGRESS, alloyer.getWorkProgress());
                    tag.putInt(TOTAL_PROGRESS, alloyer.getRecipeTicks());
                    tag.putInt(LAVA, alloyer.getLavaSides());
                }
                case FastGeneratorBlockEntity generator -> {
                    tag.putString(TYPE, "generator");
                    putEnergy(tag, generator.energy().getAmountAsInt(), generator.energy().getCapacityAsInt());
                    tag.putInt(TRANSFER_RATE, generator.getTier().getGeneratorExtractionLimit());
                    tag.putInt(BURN, generator.getBurnTime());
                    tag.putInt(TOTAL_BURN, generator.getTotalBurnTime());
                    tag.putInt(SPEED, generator.getOutput());
                    tag.putInt(BURN_PERCENT, generator.getActiveBurnTimePercent());
                    tag.putInt(LAVA, generator.getLavaSides());
                }
                case FastBatteryBlockEntity battery -> {
                    tag.putString(TYPE, "battery");
                    putEnergy(tag, battery.energy().getAmountAsInt(), battery.energy().getCapacityAsInt());
                    tag.putInt(TRANSFER_RATE, battery.getTier().getBatteryTransferLimit());
                }
                case FastPowerConduitBlockEntity conduit -> {
                    tag.putString(TYPE, "conduit");
                    putEnergy(tag, conduit.energy().getAmountAsInt(), conduit.energy().getCapacityAsInt());
                    tag.putInt(TRANSFER_RATE, conduit.getNetworkTransferRate());
                    tag.putInt(NETWORK_MODE, conduit.getNetworkTransferMode().ordinal());
                }
                default -> {
                }
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            switch (data.getStringOr(TYPE, "")) {
                case "furnace", "crusher" -> {
                    addBurn(tooltip, data);
                    addProgress(tooltip, data, "progress");
                    addLine(tooltip, "speed_percent", data.getIntOr(SPEED, 0));
                    if (data.getBooleanOr(PENDING_DOUBLE, false)) {
                        addLine(tooltip, "pending_double");
                    }
                }
                case "accelerator" -> {
                    addProgress(tooltip, data, "work");
                    if (data.getIntOr(BOOST, 0) > 0) {
                        addLine(tooltip, "boost", format(data.getIntOr(BOOST, 0)),
                                format(data.getIntOr(TOTAL_BOOST, 0)));
                    }
                    addLine(tooltip, "work_speed", data.getIntOr(SPEED, 1));
                    addLava(tooltip, data);
                }
                case "alloyer" -> {
                    addBurn(tooltip, data);
                    addProgress(tooltip, data, "progress");
                    addLava(tooltip, data);
                }
                case "generator" -> {
                    addEnergy(tooltip, data);
                    if (data.getIntOr(BURN, 0) > 0) {
                        addLine(tooltip, "generation", format(data.getIntOr(SPEED, 0)));
                        addBurn(tooltip, data);
                        addLine(tooltip, "burn_percent", data.getIntOr(BURN_PERCENT, 100));
                    }
                    addLine(tooltip, "generator_transfer", format(data.getIntOr(TRANSFER_RATE, 0)));
                    addLava(tooltip, data);
                }
                case "battery" -> {
                    addEnergy(tooltip, data);
                    addLine(tooltip, "battery_transfer", format(data.getIntOr(TRANSFER_RATE, 0)));
                }
                case "conduit" -> {
                    addLine(tooltip, "conduit_buffer", format(data.getIntOr(ENERGY, 0)),
                            format(data.getIntOr(CAPACITY, 0)));
                    addLine(tooltip, "network_transfer", format(data.getIntOr(TRANSFER_RATE, 0)));
                    ConduitTransferMode[] modes = ConduitTransferMode.values();
                    int mode = data.getIntOr(NETWORK_MODE, 0);
                    if (mode >= 0 && mode < modes.length) {
                        tooltip.add(Component.translatable("jade.fastfurnacev2.network_mode",
                                Component.translatable(modes[mode].translationKey())));
                    }
                }
                default -> {
                }
            }
        }

        private static void putEnergy(CompoundTag tag, int energy, int capacity) {
            tag.putInt(ENERGY, energy);
            tag.putInt(CAPACITY, capacity);
        }

        private static void addBurn(ITooltip tooltip, CompoundTag data) {
            if (data.getIntOr(BURN, 0) > 0) {
                addLine(tooltip, "burn", format(data.getIntOr(BURN, 0)),
                        format(data.getIntOr(TOTAL_BURN, 0)));
            }
        }

        private static void addProgress(ITooltip tooltip, CompoundTag data, String key) {
            if (data.getIntOr(TOTAL_PROGRESS, 0) > 0) {
                addLine(tooltip, key, format(data.getIntOr(PROGRESS, 0)),
                        format(data.getIntOr(TOTAL_PROGRESS, 0)));
            }
        }

        private static void addEnergy(ITooltip tooltip, CompoundTag data) {
            addLine(tooltip, "energy", format(data.getIntOr(ENERGY, 0)),
                    format(data.getIntOr(CAPACITY, 0)));
        }

        private static void addLava(ITooltip tooltip, CompoundTag data) {
            addLine(tooltip, "lava", data.getIntOr(LAVA, 0));
        }

        private static void addLine(ITooltip tooltip, String key, Object... arguments) {
            tooltip.add(Component.translatable("jade.fastfurnacev2." + key, arguments));
        }

        private static String format(int value) {
            return String.format(Locale.ROOT, "%,d", value);
        }
    }
}
