package com.armilp.ezweight.util;

import com.armilp.ezweight.events.NeoForgeNetworkEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record PacketToPayload<T>(
        T data,
        CustomPacketPayload.Type<PacketToPayload<T>> type,
        BiConsumer<T, RegistryFriendlyByteBuf> encoder,
        Function<RegistryFriendlyByteBuf, T> decoder,
        BiConsumer<T, Supplier<NeoForgeNetworkEvent.Context>> handler
) implements CustomPacketPayload {

    @Override
    public @NotNull Type<PacketToPayload<T>> type() {
        return type;
    }

    public void handle(IPayloadContext context) {
        handler.accept(data, NeoForgeNetworkEvent.Context.wrap(context));
    }

    public static <T> Registration<T> create(
            String id,
            String modId,
            BiConsumer<T, RegistryFriendlyByteBuf> encoder,
            Function<RegistryFriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NeoForgeNetworkEvent.Context>> handler
    ) {
        Type<PacketToPayload<T>> type = new Type<>(ResourceLocation.fromNamespaceAndPath(modId, id));

        StreamCodec<RegistryFriendlyByteBuf, PacketToPayload<T>> codec = StreamCodec.of(
                (buf, payload) -> payload.encoder().accept(payload.data(), buf),
                (buf) -> new PacketToPayload<>(decoder.apply(buf), type, encoder, decoder, handler)
        );

        return new Registration<>(type, codec, encoder, decoder, handler);
    }

    public record Registration<T>(
            Type<PacketToPayload<T>> type,
            StreamCodec<RegistryFriendlyByteBuf, PacketToPayload<T>> codec,
            BiConsumer<T, RegistryFriendlyByteBuf> encoder,
            Function<RegistryFriendlyByteBuf, T> decoder,
            BiConsumer<T, Supplier<NeoForgeNetworkEvent.Context>> handler
    ) {
        public PacketToPayload<T> wrap(T data) {
            return new PacketToPayload<>(data, type, encoder, decoder, handler);
        }
    }
}