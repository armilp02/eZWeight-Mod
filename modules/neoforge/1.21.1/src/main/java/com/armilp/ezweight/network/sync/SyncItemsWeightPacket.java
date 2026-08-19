package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.tacz.guns.GunMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncItemsWeightPacket implements CustomPacketPayload {

    // 1. Define the unique payload Type ID matching your EZWeightNetwork setup
    public static final Type<SyncItemsWeightPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "sync_items_weight"));

    // 2. Wrap your encode and decode operations inside a 1.21 StreamCodec
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncItemsWeightPacket> STREAM_CODEC = StreamCodec.of(
            SyncItemsWeightPacket::encode,
            SyncItemsWeightPacket::decode
    );

    private final ResourceLocation itemId;
    private final double weight;

    public SyncItemsWeightPacket(ResourceLocation itemId, double weight) {
        this.itemId = itemId;
        this.weight = weight;
    }

    // Updated: Changed FriendlyByteBuf to RegistryFriendlyByteBuf to align with modern signatures
    public static void encode(RegistryFriendlyByteBuf buf, SyncItemsWeightPacket packet) {
        buf.writeResourceLocation(packet.itemId);
        buf.writeDouble(packet.weight);
    }

    // Updated: Changed FriendlyByteBuf to RegistryFriendlyByteBuf to align with modern signatures
    public static SyncItemsWeightPacket decode(RegistryFriendlyByteBuf buf) {
        return new SyncItemsWeightPacket(buf.readResourceLocation(), buf.readDouble());
    }

    // 3. Update the handle method to receive NeoForge's modern context injection pipeline
    public void handle(final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Verificar si el ítem pertenece al mod TACZ
            if (this.itemId.getNamespace().equals(GunMod.MOD_ID)) {
                ItemWeightRegistry.setTACZWeight(this.itemId, this.weight);  // Usamos setTACZWeight para TACZ
            } else {
                ItemWeightRegistry.setWeight(this.itemId, this.weight);  // Usamos setWeight para otros ítems
            }
        });
    }

    // 4. Override type() to map this packet instance to its static registry identifier reference
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}