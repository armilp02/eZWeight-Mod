package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.levels.WeightLevel;
import com.armilp.ezweight.levels.WeightLevelManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class WeightLevelsSyncPacket implements CustomPacketPayload {

    public static final Type<WeightLevelsSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "weight_levels_sync"));

    // Fixed: Cleaner mapping using static method references
    public static final StreamCodec<RegistryFriendlyByteBuf, WeightLevelsSyncPacket> STREAM_CODEC = StreamCodec.of(
            WeightLevelsSyncPacket::encode,
            WeightLevelsSyncPacket::decode
    );

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final String json;

    public WeightLevelsSyncPacket(String json) {
        this.json = json;
    }

    // Fixed: Changed method to static so it matches the expected signature pattern of StreamCodec.of()
    public static void encode(RegistryFriendlyByteBuf buf, WeightLevelsSyncPacket packet) {
        buf.writeUtf(packet.json);
    }

    public static WeightLevelsSyncPacket decode(RegistryFriendlyByteBuf buf) {
        return new WeightLevelsSyncPacket(buf.readUtf());
    }

    public static void handle(final WeightLevelsSyncPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            WeightLevelManager.loadFromJsonString(packet.json);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static WeightLevelsSyncPacket fromLevels(List<WeightLevel> levels) {
        return new WeightLevelsSyncPacket(WeightLevelManager.toJsonString(levels));
    }
}