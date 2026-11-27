package com.skylark193.fastfurnacev2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EnergyConfigurationPayload(BlockPos pos, int action) implements CustomPacketPayload {
    public static final int NETWORK_MODE_ACTION = Direction.values().length;
    public static final Type<EnergyConfigurationPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(FastFurnaceV2.MODID, "configure_energy"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyConfigurationPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeBlockPos(payload.pos);
                        buffer.writeByte(payload.action);
                    },
                    buffer -> new EnergyConfigurationPayload(buffer.readBlockPos(), buffer.readUnsignedByte()));

    public EnergyConfigurationPayload {
        if (action < 0 || action > NETWORK_MODE_ACTION) {
            throw new IllegalArgumentException("Invalid energy configuration action: " + action);
        }
    }

    public static EnergyConfigurationPayload cycleSide(BlockPos pos, Direction direction) {
        return new EnergyConfigurationPayload(pos, direction.ordinal());
    }

    public static EnergyConfigurationPayload cycleNetworkMode(BlockPos pos) {
        return new EnergyConfigurationPayload(pos, NETWORK_MODE_ACTION);
    }

    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, EnergyConfigurationPayload::handle);
    }

    private void handle(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || player.distanceToSqr(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) > 64.0
                || !(player.containerMenu instanceof EnergyConfigurableMenu menu)
                || !menu.getEnergyBlockPos().equals(this.pos)) {
            return;
        }
        if (this.action == NETWORK_MODE_ACTION) {
            if (menu instanceof FastPowerConduitMenu
                    && player.level().getBlockEntity(this.pos) instanceof FastPowerConduitBlockEntity conduit) {
                conduit.cycleNetworkTransferMode();
            }
            return;
        }
        Direction direction = Direction.values()[this.action];
        switch (player.level().getBlockEntity(this.pos)) {
            case FastGeneratorBlockEntity generator when menu instanceof FastGeneratorMenu ->
                    generator.cycleEnergySide(direction);
            case FastBatteryBlockEntity battery when menu instanceof FastBatteryMenu ->
                    battery.cycleEnergySide(direction);
            case FastPowerConduitBlockEntity conduit when menu instanceof FastPowerConduitMenu ->
                    conduit.cycleConnectionMode(direction);
            default -> { }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
