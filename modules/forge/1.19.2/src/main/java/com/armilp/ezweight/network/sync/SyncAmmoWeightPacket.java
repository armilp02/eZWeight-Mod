package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.data.ItemWeightRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncAmmoWeightPacket {
    private final ResourceLocation gunId;
    private final double ammoWeight;

    public SyncAmmoWeightPacket(ResourceLocation gunId, double ammoWeight) {
        this.gunId = gunId;
        this.ammoWeight = ammoWeight;
    }

    public static void encode(SyncAmmoWeightPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.gunId);
        buffer.writeDouble(packet.ammoWeight);
    }

    public static SyncAmmoWeightPacket decode(FriendlyByteBuf buffer) {
        return new SyncAmmoWeightPacket(buffer.readResourceLocation(), buffer.readDouble());
    }

    public static void handle(SyncAmmoWeightPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                ItemWeightRegistry.setGunAmmoWeight(packet.gunId, packet.ammoWeight);
            }
        });
        context.get().setPacketHandled(true);
    }
}