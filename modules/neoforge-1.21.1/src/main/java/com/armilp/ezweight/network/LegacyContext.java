package com.armilp.ezweight.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

/**
 * Mimics the old Forge NetworkEvent.Context API so packet handle()
 * methods don't need to change their signature/body.
 */
public class LegacyContext {
    private final IPayloadContext context;

    public LegacyContext(IPayloadContext context) {
        this.context = context;
    }

    public void enqueueWork(Runnable runnable) {
        context.enqueueWork(runnable);
    }

    public void setPacketHandled(boolean handled) {
        // no-op
    }

    public ServerPlayer getSender() {
        if (context.player() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }

    public IPayloadContext raw() {
        return context;
    }

    public static Supplier<LegacyContext> wrap(IPayloadContext context) {
        LegacyContext legacy = new LegacyContext(context);
        return () -> legacy;
    }
}