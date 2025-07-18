package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.data.ItemWeightRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncItemsWeightPacket {
    private final ResourceLocation itemId;
    private final double weight;

    public SyncItemsWeightPacket(ResourceLocation itemId, double weight) {
        this.itemId = itemId;
        this.weight = weight;
    }

    public static void encode(SyncItemsWeightPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.itemId);
        buf.writeDouble(packet.weight);
    }

    public static SyncItemsWeightPacket decode(FriendlyByteBuf buf) {
        return new SyncItemsWeightPacket(buf.readResourceLocation(), buf.readDouble());
    }

    public static void handle(SyncItemsWeightPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ItemWeightRegistry.setWeight(packet.itemId, packet.weight);
        });
        context.get().setPacketHandled(true);
    }
}
