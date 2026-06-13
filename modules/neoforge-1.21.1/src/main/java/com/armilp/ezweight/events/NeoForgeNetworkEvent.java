package com.armilp.ezweight.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

/**
 * Mimics the old Forge NetworkEvent.Context API so packet handle()
 * methods don't need to change their signature/body.
 */
public class NeoForgeNetworkEvent {

    public static class Context {
        private final IPayloadContext context;

        public Context(IPayloadContext context) {
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

        public static Supplier<Context> wrap(IPayloadContext context) {
            Context legacy = new Context(context);
            return () -> legacy;
        }
    }


}