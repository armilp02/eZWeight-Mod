package com.armilp.ezweight.network.gui;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.network.LegacyContext;
import com.armilp.ezweight.network.PacketToPayload;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;


public class OpenWeightGuiPacket {
    public OpenWeightGuiPacket() {
    }

    public static void encode(OpenWeightGuiPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenWeightGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenWeightGuiPacket();
    }

    public static void handle(OpenWeightGuiPacket msg, Supplier<LegacyContext> ctx) {
        ctx.get().enqueueWork(OpenWeightGuiHandler::handle);
        ctx.get().setPacketHandled(true);
    }

    public static final PacketToPayload.Registration<OpenWeightGuiPacket> REGISTRATION =
            PacketToPayload.create(
                    "open_weight_gui", EZWeight.MODID,
                    OpenWeightGuiPacket::encode,
                    OpenWeightGuiPacket::decode,
                    OpenWeightGuiPacket::handle
            );

}

