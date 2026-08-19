package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.tacz.guns.GunMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class WeightUpdatePacket implements CustomPacketPayload {

    // 1. Define the unique payload Type ID matching your updated EZWeightNetwork layout
    public static final Type<WeightUpdatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "weight_update"));

    // 2. Connect the encoder/decoder logic straight into modern StreamCodecs
    public static final StreamCodec<RegistryFriendlyByteBuf, WeightUpdatePacket> STREAM_CODEC = StreamCodec.of(
            WeightUpdatePacket::encode,
            WeightUpdatePacket::decode
    );

    private final ResourceLocation itemId;
    private final double weight;

    public WeightUpdatePacket(ResourceLocation itemId, double weight) {
        this.itemId = itemId;
        this.weight = weight;
    }

    public static void encode(RegistryFriendlyByteBuf buffer, WeightUpdatePacket packet) {
        buffer.writeResourceLocation(packet.itemId);
        buffer.writeDouble(packet.weight);
    }

    public static WeightUpdatePacket decode(RegistryFriendlyByteBuf buffer) {
        return new WeightUpdatePacket(buffer.readResourceLocation(), buffer.readDouble());
    }

    // 3. Required by CustomPacketPayload implementation
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 4. Update the logic execution engine to utilize modern IPayloadContext tracking injection
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // Check if it's a TACZ item before updating it
            if (this.itemId.getNamespace().equals(GunMod.MOD_ID)) {
                ItemWeightRegistry.setTACZWeight(this.itemId, this.weight); // Specific setter for TACZ
            } else {
                ItemWeightRegistry.setWeight(this.itemId, this.weight); // Standard items
            }
            ItemWeightRegistry.saveToFile(ItemWeightRegistry.getConfigFile());
            ItemWeightRegistry.reloadFromFile();

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                // Instantiating the client broadcast sync trigger payload
                SyncItemsWeightPacket syncPacket = new SyncItemsWeightPacket(this.itemId, this.weight);

                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    // 5. Native vanilla way to forward packet distributions onto specific network pipes
                    if (player.connection != null) {
                        player.connection.send(syncPacket);
                    }
                }
            }
        });
    }
}