package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.data.ItemWeightRegistry;
import com.tacz.guns.GunMod;
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
            // Verificar si el ítem pertenece al mod TACZ
            if (packet.itemId.getNamespace().equals(GunMod.MOD_ID)) {
                ItemWeightRegistry.setTACZWeight(packet.itemId, packet.weight);  // Usamos setTACZWeight para TACZ
            } else {
                ItemWeightRegistry.setWeight(packet.itemId, packet.weight);  // Usamos setWeight para otros ítems
            }
        });
        context.get().setPacketHandled(true);
    }
}
