package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.events.NeoForgeNetworkEvent;
import com.armilp.ezweight.util.PacketToPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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

    public static void handle(SyncAmmoWeightPacket packet, Supplier<NeoForgeNetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                ItemWeightRegistry.setGunAmmoWeight(packet.gunId, packet.ammoWeight);
            }
        });
        context.get().setPacketHandled(true);
    }

    public static final PacketToPayload.Registration<SyncAmmoWeightPacket> REGISTRATION =
            PacketToPayload.create(
                    "sync_ammo_weight", EZWeight.MODID,
                    SyncAmmoWeightPacket::encode,
                    SyncAmmoWeightPacket::decode,
                    SyncAmmoWeightPacket::handle
            );
}