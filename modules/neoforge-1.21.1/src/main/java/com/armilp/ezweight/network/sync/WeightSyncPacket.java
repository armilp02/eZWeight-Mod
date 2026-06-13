package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.WeightSyncData;
import com.armilp.ezweight.events.NeoForgeNetworkEvent;
import com.armilp.ezweight.util.PacketToPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class WeightSyncPacket {
    private final double maxWeight;

    public WeightSyncPacket(double maxWeight) {
        this.maxWeight = maxWeight;
    }

    public static void encode(WeightSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.maxWeight);
    }

    public static WeightSyncPacket decode(FriendlyByteBuf buf) {
        return new WeightSyncPacket(buf.readDouble());
    }

    public void handle(Supplier<NeoForgeNetworkEvent.Context> contextSupplier) {
        NeoForgeNetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                WeightSyncData.setMaxWeight(this.maxWeight);
            }
        });
        context.setPacketHandled(true);
    }

    public static final PacketToPayload.Registration<WeightSyncPacket> REGISTRATION =
            PacketToPayload.create(
                    "weight_sync", EZWeight.MODID,
                    WeightSyncPacket::encode,
                    WeightSyncPacket::decode,
                    WeightSyncPacket::handle
            );

}
