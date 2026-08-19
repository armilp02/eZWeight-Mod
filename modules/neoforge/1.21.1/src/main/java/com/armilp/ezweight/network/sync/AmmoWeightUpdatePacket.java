package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class AmmoWeightUpdatePacket implements CustomPacketPayload {

    public static final Type<AmmoWeightUpdatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "ammo_weight_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AmmoWeightUpdatePacket> STREAM_CODEC = StreamCodec.of(
            AmmoWeightUpdatePacket::encode,
            AmmoWeightUpdatePacket::decode
    );

    private final ResourceLocation gunId;
    private final double ammoWeight;

    public AmmoWeightUpdatePacket(ResourceLocation gunId, double ammoWeight) {
        this.gunId = gunId;
        this.ammoWeight = ammoWeight;
    }

    public ResourceLocation gunId() { return this.gunId; }
    public double ammoWeight() { return this.ammoWeight; }

    public static void encode(RegistryFriendlyByteBuf buffer, AmmoWeightUpdatePacket packet) {
        buffer.writeResourceLocation(packet.gunId);
        buffer.writeDouble(packet.ammoWeight);
    }

    public static AmmoWeightUpdatePacket decode(RegistryFriendlyByteBuf buffer) {
        return new AmmoWeightUpdatePacket(buffer.readResourceLocation(), buffer.readDouble());
    }

    public static void handle(final AmmoWeightUpdatePacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemWeightRegistry.setGunAmmoWeight(packet.gunId(), packet.ammoWeight());
            ItemWeightRegistry.saveToFile(ItemWeightRegistry.getConfigFile());
            ItemWeightRegistry.reloadFromFile();

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.connection != null) {
                        player.connection.send(new SyncAmmoWeightPacket(packet.gunId(), packet.ammoWeight()));
                    }
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}