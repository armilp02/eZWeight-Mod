package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.network.EZWeightNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

public class AmmoWeightUpdatePacket {
    private final ResourceLocation gunId;
    private final double ammoWeight;

    public AmmoWeightUpdatePacket(ResourceLocation gunId, double ammoWeight) {
        this.gunId = gunId;
        this.ammoWeight = ammoWeight;
    }

    public static void encode(AmmoWeightUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.gunId);
        buffer.writeDouble(packet.ammoWeight);
    }

    public static AmmoWeightUpdatePacket decode(FriendlyByteBuf buffer) {
        return new AmmoWeightUpdatePacket(buffer.readResourceLocation(), buffer.readDouble());
    }

    public static void handle(AmmoWeightUpdatePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ItemWeightRegistry.setGunAmmoWeight(packet.gunId, packet.ammoWeight);
            ItemWeightRegistry.saveToFile(ItemWeightRegistry.getConfigFile());
            ItemWeightRegistry.reloadFromFile();

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    EZWeightNetwork.CHANNEL.sendTo(new SyncAmmoWeightPacket(packet.gunId, packet.ammoWeight), 
                        player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}