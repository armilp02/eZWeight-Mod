package com.armilp.ezweight.network.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class OpenWeightGuiPacket {
    public OpenWeightGuiPacket() {}

    public static void encode(OpenWeightGuiPacket msg, FriendlyByteBuf buf) {}

    public static OpenWeightGuiPacket decode(FriendlyByteBuf buf) {
        return new OpenWeightGuiPacket();
    }

    public static void handle(OpenWeightGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(OpenWeightGuiHandler::handle);
        ctx.get().setPacketHandled(true);
    }
}

