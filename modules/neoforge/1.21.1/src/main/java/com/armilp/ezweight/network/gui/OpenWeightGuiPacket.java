package com.armilp.ezweight.network.gui;

import com.armilp.ezweight.EZWeight;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenWeightGuiPacket implements CustomPacketPayload {

    // 1. Define the unique payload Type ID required by 1.21.1
    public static final Type<OpenWeightGuiPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "open_weight_gui"));

    // 2. Align the StreamCodec with static encoder parameter ordering (buffer first)
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWeightGuiPacket> STREAM_CODEC = StreamCodec.of(
            OpenWeightGuiPacket::encode,
            OpenWeightGuiPacket::decode
    );

    public OpenWeightGuiPacket() {
    }

    // Reordered: RegistryFriendlyByteBuf MUST be the first parameter for method references
    public static void encode(RegistryFriendlyByteBuf buf, OpenWeightGuiPacket msg) {
        // Empty as per your original packet implementation
    }

    public static OpenWeightGuiPacket decode(RegistryFriendlyByteBuf buf) {
        return new OpenWeightGuiPacket();
    }

    // 3. Modernized handler using IPayloadContext
    public static void handle(final OpenWeightGuiPacket msg, final IPayloadContext context) {
        context.enqueueWork(OpenWeightGuiHandler::handle);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}