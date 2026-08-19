package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;  // <-- Change this import
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FullWeightSyncPacket implements CustomPacketPayload {
    private static final Gson GSON = new Gson();
    private static final java.lang.reflect.Type MAP_TYPE = new TypeToken<Map<String, Double>>() {}.getType();

    public static final Type<FullWeightSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "full_weight_sync"));

    // Change to FriendlyByteBuf instead of RegistryFriendlyByteBuf
    public static final StreamCodec<FriendlyByteBuf, FullWeightSyncPacket> STREAM_CODEC = StreamCodec.of(
            FullWeightSyncPacket::encode,
            FullWeightSyncPacket::decode
    );

    private final Map<String, Double> items;
    private final Map<String, Double> ammoWeights;
    private final Map<String, Double> backpackReductions;
    private final Map<String, Double> backpackMaxWeights;

    public FullWeightSyncPacket(Map<String, Double> items, Map<String, Double> ammoWeights,
                                Map<String, Double> backpackReductions, Map<String, Double> backpackMaxWeights) {
        this.items = items;
        this.ammoWeights = ammoWeights;
        this.backpackReductions = backpackReductions;
        this.backpackMaxWeights = backpackMaxWeights;
    }

    public static FullWeightSyncPacket fromRegistry() {
        return new FullWeightSyncPacket(
                toStringKeyed(ItemWeightRegistry.getAllWeights()),
                toStringKeyed(ItemWeightRegistry.getAllGunAmmoWeights()),
                toStringKeyed(ItemWeightRegistry.getAllBackpackReductions()),
                toStringKeyed(ItemWeightRegistry.getAllBackpackMaxWeights())
        );
    }

    private static Map<String, Double> toStringKeyed(Map<ResourceLocation, Double> src) {
        Map<String, Double> out = new HashMap<>(src.size());
        for (Map.Entry<ResourceLocation, Double> e : src.entrySet()) {
            out.put(e.getKey().toString(), e.getValue());
        }
        return out;
    }

    private static Map<ResourceLocation, Double> toRLKeyed(Map<String, Double> src) {
        Map<ResourceLocation, Double> out = new HashMap<>(src.size());
        for (Map.Entry<String, Double> e : src.entrySet()) {
            ResourceLocation rl = ResourceLocation.tryParse(e.getKey());
            if (rl != null) out.put(rl, e.getValue());
        }
        return out;
    }

    // Change parameter type to FriendlyByteBuf
    public static void encode(FriendlyByteBuf buf, FullWeightSyncPacket pkt) {
        String json = GSON.toJson(new Object[]{pkt.items, pkt.ammoWeights, pkt.backpackReductions, pkt.backpackMaxWeights});
        byte[] raw = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(raw);
        } catch (IOException e) {
            throw new RuntimeException("Failed to gzip weight sync payload", e);
        }
        byte[] compressed = baos.toByteArray();
        buf.writeVarInt(compressed.length);
        buf.writeBytes(compressed);
    }

    // Change parameter type to FriendlyByteBuf
    @SuppressWarnings("unchecked")
    public static FullWeightSyncPacket decode(FriendlyByteBuf buf) {
        int len = buf.readVarInt();
        byte[] compressed = new byte[len];
        buf.readBytes(compressed);

        String json;
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[4096];
            int n;
            while ((n = gzip.read(chunk)) != -1) out.write(chunk, 0, n);
            json = out.toString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to ungzip weight sync payload", e);
        }

        Object[] maps = GSON.fromJson(json, Object[].class);
        Gson mapGson = new Gson();
        Map<String, Double> items = mapGson.fromJson(mapGson.toJson(maps[0]), MAP_TYPE);
        Map<String, Double> ammo = mapGson.fromJson(mapGson.toJson(maps[1]), MAP_TYPE);
        Map<String, Double> backpackRed = mapGson.fromJson(mapGson.toJson(maps[2]), MAP_TYPE);
        Map<String, Double> backpackMax = mapGson.fromJson(mapGson.toJson(maps[3]), MAP_TYPE);

        return new FullWeightSyncPacket(items, ammo, backpackRed, backpackMax);
    }

    public static void handle(final FullWeightSyncPacket pkt, final IPayloadContext context) {
        context.enqueueWork(() -> ItemWeightRegistry.applyServerSync(
                toRLKeyed(pkt.items),
                toRLKeyed(pkt.ammoWeights),
                toRLKeyed(pkt.backpackReductions),
                toRLKeyed(pkt.backpackMaxWeights)
        ));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}