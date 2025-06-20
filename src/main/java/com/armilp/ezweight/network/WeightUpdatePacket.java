package com.armilp.ezweight.network;

import com.armilp.ezweight.data.ItemWeightRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WeightUpdatePacket {
    private final ResourceLocation itemId;
    private final double weight;

    public WeightUpdatePacket(ResourceLocation itemId, double weight) {
        this.itemId = itemId;
        this.weight = weight;
    }

    public static void encode(WeightUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.itemId);
        buffer.writeDouble(packet.weight);
    }

    public static WeightUpdatePacket decode(FriendlyByteBuf buffer) {
        return new WeightUpdatePacket(buffer.readResourceLocation(), buffer.readDouble());
    }

    public static void handle(WeightUpdatePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ItemWeightRegistry.setWeight(packet.itemId, packet.weight);
            ItemWeightRegistry.saveToFile(ItemWeightRegistry.getConfigFile());
        });
        context.get().setPacketHandled(true);
    }
}
