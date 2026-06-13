package com.armilp.ezweight.network;

import com.armilp.ezweight.network.gui.OpenWeightGuiPacket;
import com.armilp.ezweight.network.sync.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class EZWeightNetwork {

    private static final String PROTOCOL_VERSION = "1.0";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(OpenWeightGuiPacket.REGISTRATION.type(), OpenWeightGuiPacket.REGISTRATION.codec(), PacketToPayload::handle);
        registrar.playToServer(WeightUpdatePacket.REGISTRATION.type(), WeightUpdatePacket.REGISTRATION.codec(), PacketToPayload::handle);
        registrar.playToClient(WeightSyncPacket.REGISTRATION.type(), WeightSyncPacket.REGISTRATION.codec(), PacketToPayload::handle);
        registrar.playToClient(WeightLevelsSyncPacket.REGISTRATION.type(), WeightLevelsSyncPacket.REGISTRATION.codec(), PacketToPayload::handle);
        registrar.playToClient(SyncItemsWeightPacket.REGISTRATION.type(), SyncItemsWeightPacket.REGISTRATION.codec(), PacketToPayload::handle);
        registrar.playToServer(AmmoWeightUpdatePacket.REGISTRATION.type(), AmmoWeightUpdatePacket.REGISTRATION.codec(), PacketToPayload::handle);
        registrar.playToClient(SyncAmmoWeightPacket.REGISTRATION.type(), SyncAmmoWeightPacket.REGISTRATION.codec(), PacketToPayload::handle);
        registrar.playToServer(BackpackConfigUpdatePacket.REGISTRATION.type(), BackpackConfigUpdatePacket.REGISTRATION.codec(), PacketToPayload::handle);
    }

    public static <T> void sendToPlayer(PacketToPayload.Registration<T> registration, T message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, registration.wrap(message));
    }

    public static <T> void sendToServer(PacketToPayload.Registration<T> registration, T message) {
        PacketDistributor.sendToServer(registration.wrap(message));
    }
}