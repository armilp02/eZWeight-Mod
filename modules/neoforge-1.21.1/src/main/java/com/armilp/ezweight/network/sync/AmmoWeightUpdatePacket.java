package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.events.NeoForgeNetworkEvent;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.util.PacketToPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class AmmoWeightUpdatePacket {
    private final ResourceLocation gunId;
    private final double ammoWeight;

    public AmmoWeightUpdatePacket(ResourceLocation gunId, double ammoWeight) {
        this.gunId = gunId;
        this.ammoWeight = ammoWeight;
    }

    public static void encode(AmmoWeightUpdatePacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.gunId);
        buffer.writeDouble(packet.ammoWeight);
    }

    public static AmmoWeightUpdatePacket decode(RegistryFriendlyByteBuf buffer) {
        return new AmmoWeightUpdatePacket(buffer.readResourceLocation(), buffer.readDouble());
    }

    public static void handle(AmmoWeightUpdatePacket packet, Supplier<NeoForgeNetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ItemWeightRegistry.setGunAmmoWeight(packet.gunId, packet.ammoWeight);
            ItemWeightRegistry.saveToFile(ItemWeightRegistry.getConfigFile());
            ItemWeightRegistry.reloadFromFile();

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    EZWeightNetwork.sendToPlayer(SyncAmmoWeightPacket.REGISTRATION,
                            new SyncAmmoWeightPacket(packet.gunId, packet.ammoWeight), player);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static final PacketToPayload.Registration<AmmoWeightUpdatePacket> REGISTRATION =
            PacketToPayload.create(
                    "ammo_weight_update", EZWeight.MODID,
                    AmmoWeightUpdatePacket::encode,
                    AmmoWeightUpdatePacket::decode,
                    AmmoWeightUpdatePacket::handle
            );
}