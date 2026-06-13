package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.network.LegacyContext;
import com.armilp.ezweight.network.PacketToPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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

    public static void handle(WeightUpdatePacket packet, Supplier<LegacyContext> context) {
        context.get().enqueueWork(() -> {
            // Aquí se verifica si es un ítem TACZ antes de actualizarlo

                ItemWeightRegistry.setWeight(packet.itemId, packet.weight); // Para otros ítems

            ItemWeightRegistry.saveToFile(ItemWeightRegistry.getConfigFile());
            ItemWeightRegistry.reloadFromFile();

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    EZWeightNetwork.sendToPlayer(SyncItemsWeightPacket.REGISTRATION,
                            new SyncItemsWeightPacket(packet.itemId, packet.weight), player);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static final PacketToPayload.Registration<WeightUpdatePacket> REGISTRATION =
            PacketToPayload.create(
                    "weight_update", EZWeight.MODID,
                    WeightUpdatePacket::encode,
                    WeightUpdatePacket::decode,
                    WeightUpdatePacket::handle
            );


}
