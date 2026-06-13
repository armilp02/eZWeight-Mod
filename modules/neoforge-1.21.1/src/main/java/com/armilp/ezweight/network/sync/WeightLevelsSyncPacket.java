package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.levels.WeightLevel;
import com.armilp.ezweight.levels.WeightLevelManager;
import com.armilp.ezweight.network.LegacyContext;
import com.armilp.ezweight.network.PacketToPayload;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.function.Supplier;

public class WeightLevelsSyncPacket {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final String json;

    public WeightLevelsSyncPacket(String json) {
        this.json = json;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(json);
    }

    public static WeightLevelsSyncPacket decode(FriendlyByteBuf buf) {
        return new WeightLevelsSyncPacket(buf.readUtf());
    }

    public void handle(Supplier<LegacyContext> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            WeightLevelManager.loadFromJsonString(json);
        });
        contextSupplier.get().setPacketHandled(true);
    }

    public static WeightLevelsSyncPacket fromLevels(List<WeightLevel> levels) {
        return new WeightLevelsSyncPacket(WeightLevelManager.toJsonString(levels));
    }

    public static final PacketToPayload.Registration<WeightLevelsSyncPacket> REGISTRATION =
            PacketToPayload.create(
                    "sync_weight_levels", EZWeight.MODID,
                    WeightLevelsSyncPacket::encode,
                    WeightLevelsSyncPacket::decode,
                    WeightLevelsSyncPacket::handle
            );
}
