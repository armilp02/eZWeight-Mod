package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.events.NeoForgeNetworkEvent;
import com.armilp.ezweight.util.PacketToPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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

    public static void handle(SyncItemsWeightPacket packet, Supplier<NeoForgeNetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
                ItemWeightRegistry.setWeight(packet.itemId, packet.weight);  // Usamos setWeight para otros ítems

        });
        context.get().setPacketHandled(true);
    }
    public static final PacketToPayload.Registration<SyncItemsWeightPacket> REGISTRATION =
            PacketToPayload.create(
                    "sync_items_weight", EZWeight.MODID,
                    SyncItemsWeightPacket::encode,
                    SyncItemsWeightPacket::decode,
                    SyncItemsWeightPacket::handle
            );
}
