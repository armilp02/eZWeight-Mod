package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.WeightSyncData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class WeightSyncPacket implements CustomPacketPayload {

    // 1. Define the unique payload Type ID
    public static final Type<WeightSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "weight_sync"));

    // 2. Wrap encode and decode into a modern StreamCodec instance
    public static final StreamCodec<RegistryFriendlyByteBuf, WeightSyncPacket> STREAM_CODEC = StreamCodec.of(
            WeightSyncPacket::encode,
            WeightSyncPacket::decode
    );

    private final double maxWeight;

    public WeightSyncPacket(double maxWeight) {
        this.maxWeight = maxWeight;
    }

    // Adjusted parameters to match the (buf, packet) codec layout pattern
    public static void encode(RegistryFriendlyByteBuf buf, WeightSyncPacket packet) {
        buf.writeDouble(packet.maxWeight);
    }

    public static WeightSyncPacket decode(RegistryFriendlyByteBuf buf) {
        return new WeightSyncPacket(buf.readDouble());
    }

    // 3. Return your explicit CustomPacketPayload type mapping definition
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 4. Handle work securely on the client game thread using modern IPayloadContext
    public static void handle(final WeightSyncPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                WeightSyncData.setMaxWeight(packet.maxWeight);
            }
        });
    }
}