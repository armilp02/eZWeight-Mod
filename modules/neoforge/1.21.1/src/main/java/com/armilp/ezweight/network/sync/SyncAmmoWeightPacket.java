package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncAmmoWeightPacket implements CustomPacketPayload {

    // 1. Define your unique Type ID constant
    public static final Type<SyncAmmoWeightPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "sync_ammo_weight"));

    // 2. Build the StreamCodec using the corrected static encoder layout
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAmmoWeightPacket> STREAM_CODEC = StreamCodec.of(
            SyncAmmoWeightPacket::encode,
            SyncAmmoWeightPacket::decode
    );

    private final ResourceLocation gunId;
    private final double ammoWeight;

    public SyncAmmoWeightPacket(ResourceLocation gunId, double ammoWeight) {
        this.gunId = gunId;
        this.ammoWeight = ammoWeight;
    }

    // Fixed parameter ordering: RegistryFriendlyByteBuf MUST come first
    public static void encode(RegistryFriendlyByteBuf buffer, SyncAmmoWeightPacket packet) {
        buffer.writeResourceLocation(packet.gunId);
        buffer.writeDouble(packet.ammoWeight);
    }

    public static SyncAmmoWeightPacket decode(RegistryFriendlyByteBuf buffer) {
        return new SyncAmmoWeightPacket(buffer.readResourceLocation(), buffer.readDouble());
    }

    // 3. Updated handler to accept the direct IPayloadContext
    public static void handle(final SyncAmmoWeightPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                ItemWeightRegistry.setGunAmmoWeight(packet.gunId, packet.ammoWeight);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}