package com.armilp.ezweight.network;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.network.gui.OpenWeightGuiPacket;
import com.armilp.ezweight.network.sync.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;


public class EZWeightNetwork {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(
                id++,
                OpenWeightGuiPacket.class,
                OpenWeightGuiPacket::encode,
                OpenWeightGuiPacket::decode,
                OpenWeightGuiPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        CHANNEL.registerMessage(
                id++,
                WeightUpdatePacket.class,
                WeightUpdatePacket::encode,
                WeightUpdatePacket::decode,
                WeightUpdatePacket::handle);

        CHANNEL.registerMessage(
                id++,
                WeightSyncPacket.class,
                WeightSyncPacket::encode,
                WeightSyncPacket::decode,
                WeightSyncPacket::handle
        );

        CHANNEL.registerMessage(id++,
                WeightLevelsSyncPacket.class,
                WeightLevelsSyncPacket::encode,
                WeightLevelsSyncPacket::decode,
                WeightLevelsSyncPacket::handle
        );

        CHANNEL.registerMessage(id++,
                SyncItemsWeightPacket.class,
                SyncItemsWeightPacket::encode,
                SyncItemsWeightPacket::decode,
                SyncItemsWeightPacket::handle
        );

        CHANNEL.registerMessage(id++,
                AmmoWeightUpdatePacket.class,
                AmmoWeightUpdatePacket::encode,
                AmmoWeightUpdatePacket::decode,
                AmmoWeightUpdatePacket::handle
        );

        CHANNEL.registerMessage(id++,
                SyncAmmoWeightPacket.class,
                SyncAmmoWeightPacket::encode,
                SyncAmmoWeightPacket::decode,
                SyncAmmoWeightPacket::handle
        );

        CHANNEL.registerMessage(id++,
                BackpackConfigUpdatePacket.class,
                BackpackConfigUpdatePacket::encode,
                BackpackConfigUpdatePacket::decode,
                BackpackConfigUpdatePacket::handle
        );

        CHANNEL.registerMessage(id++,
                FullWeightSyncPacket.class,
                FullWeightSyncPacket::encode,
                FullWeightSyncPacket::decode,
                FullWeightSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
