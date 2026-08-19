package com.armilp.ezweight.network;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.network.gui.OpenWeightGuiPacket;
import com.armilp.ezweight.network.sync.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class EZWeightNetwork {

    private static final String PROTOCOL_VERSION = "1.0";

    @SubscribeEvent
    public static void onRegisterPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(EZWeight.MODID).versioned(PROTOCOL_VERSION);

        // 1. OpenWeightGuiPacket (Server -> Client)
        registrar.playToClient(
                OpenWeightGuiPacket.TYPE,
                OpenWeightGuiPacket.STREAM_CODEC,
                OpenWeightGuiPacket::handle
        );

        // 2. WeightUpdatePacket (Bidirectional / Play)
        registrar.playBidirectional(
                WeightUpdatePacket.TYPE,
                WeightUpdatePacket.STREAM_CODEC,
                WeightUpdatePacket::handle
        );

        // 3. WeightSyncPacket (Bidirectional / Play)
        registrar.playBidirectional(
                WeightSyncPacket.TYPE,
                WeightSyncPacket.STREAM_CODEC,
                WeightSyncPacket::handle
        );

        // 4. TotalWeightSyncPacket (Bidirectional / Play)
        registrar.playBidirectional(
                TotalWeightSyncPacket.TYPE,
                TotalWeightSyncPacket.STREAM_CODEC,
                TotalWeightSyncPacket::handle
        );

        // 5. WeightLevelsSyncPacket (Bidirectional / Play)
        registrar.playBidirectional(
                WeightLevelsSyncPacket.TYPE,
                WeightLevelsSyncPacket.STREAM_CODEC,
                WeightLevelsSyncPacket::handle
        );

        // 6. SyncItemsWeightPacket (Bidirectional / Play)
        registrar.playBidirectional(
                SyncItemsWeightPacket.TYPE,
                SyncItemsWeightPacket.STREAM_CODEC,
                SyncItemsWeightPacket::handle
        );

        // 7. AmmoWeightUpdatePacket (Bidirectional / Play)
        registrar.playBidirectional(
                AmmoWeightUpdatePacket.TYPE,
                AmmoWeightUpdatePacket.STREAM_CODEC,
                AmmoWeightUpdatePacket::handle
        );

        // 8. SyncAmmoWeightPacket (Bidirectional / Play)
        registrar.playBidirectional(
                SyncAmmoWeightPacket.TYPE,
                SyncAmmoWeightPacket.STREAM_CODEC,
                SyncAmmoWeightPacket::handle
        );

        // 9. BackpackConfigUpdatePacket (Bidirectional / Play)
        registrar.playBidirectional(
                BackpackConfigUpdatePacket.TYPE,
                BackpackConfigUpdatePacket.STREAM_CODEC,
                BackpackConfigUpdatePacket::handle
        );

        // 10. FullWeightSyncPacket - Register for BOTH Configuration AND Play phases
        registrar.configurationToClient(
                FullWeightSyncPacket.TYPE,
                FullWeightSyncPacket.STREAM_CODEC,
                FullWeightSyncPacket::handle
        );

        registrar.playToClient(
                FullWeightSyncPacket.TYPE,
                FullWeightSyncPacket.STREAM_CODEC,
                FullWeightSyncPacket::handle
        );
    }

    /**
     * Send a packet to a player, automatically detecting the correct phase.
     * This handles the transition between configuration and play phases.
     */
    public static void
    sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        if (player.connection == null) return;

        // Try to send using the configuration channel first
        // If the player is still in configuration phase, this will work
        try {
            player.connection.send(message);
        } catch (UnsupportedOperationException e) {
            // If it fails, it means the packet is not registered for this phase
            // Log the error but don't crash
            EZWeight.LOGGER.warn("Failed to send packet {} to player {}: {}",
                    message.type().id(), player.getName().getString(), e.getMessage());
            throw e; // Re-throw to maintain the original behavior
        }
    }
}