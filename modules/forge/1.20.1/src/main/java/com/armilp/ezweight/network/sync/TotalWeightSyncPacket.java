package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.data.WeightSyncData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TotalWeightSyncPacket {
    private final double totalWeight;

    public TotalWeightSyncPacket(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public static void encode(TotalWeightSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.totalWeight);
    }

    public static TotalWeightSyncPacket decode(FriendlyByteBuf buf) {
        return new TotalWeightSyncPacket(buf.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                WeightSyncData.setTotalWeight(this.totalWeight);
            }
        });
        context.setPacketHandled(true);
    }

}