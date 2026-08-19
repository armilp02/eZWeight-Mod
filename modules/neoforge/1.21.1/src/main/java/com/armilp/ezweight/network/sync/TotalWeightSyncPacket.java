package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.WeightSyncData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TotalWeightSyncPacket implements CustomPacketPayload {

    // 1. Define your unique Type ID constant
    public static final Type<TotalWeightSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "total_weight_sync"));

    // 2. Connect via StreamCodec using correct parameter ordering (Buffer first)
    public static final StreamCodec<RegistryFriendlyByteBuf, TotalWeightSyncPacket> STREAM_CODEC = StreamCodec.of(
            TotalWeightSyncPacket::encode,
            TotalWeightSyncPacket::decode
    );

    private final double totalWeight;

    public TotalWeightSyncPacket(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    // 3. Fixed signature: Buffer must be the first parameter for method references to work cleanly
    public static void encode(RegistryFriendlyByteBuf buf, TotalWeightSyncPacket packet) {
        buf.writeDouble(packet.totalWeight);
    }

    public static TotalWeightSyncPacket decode(RegistryFriendlyByteBuf buf) {
        return new TotalWeightSyncPacket(buf.readDouble());
    }

    // 4. Update your handle method to use modern IPayloadContext context injection
    public static void handle(final TotalWeightSyncPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                WeightSyncData.setTotalWeight(packet.totalWeight);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}