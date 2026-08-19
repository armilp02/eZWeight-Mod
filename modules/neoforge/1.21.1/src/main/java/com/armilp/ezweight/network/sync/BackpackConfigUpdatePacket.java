package com.armilp.ezweight.network.sync;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.util.BackpackIdUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BackpackConfigUpdatePacket implements CustomPacketPayload {

    // 1. Define the unique payload Type ID
    public static final Type<BackpackConfigUpdatePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "backpack_config_update"));

    // 2. Define the static StreamCodec with the proper parameter order (buffer first)
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackConfigUpdatePacket> STREAM_CODEC = StreamCodec.of(
            BackpackConfigUpdatePacket::encode,
            BackpackConfigUpdatePacket::decode
    );

    public static final int SLOT_OFFHAND    = 200;
    public static final int SLOT_TYPE_WIDE  = -1;
    public static final int ARMOR_SLOT_BASE = 100;

    private final ResourceLocation typeId;
    private final double           weightReduction;
    private final double           maxWeight;
    private final int              slot;

    public BackpackConfigUpdatePacket(ResourceLocation typeId,
                                      double weightReduction,
                                      double maxWeight,
                                      int slot) {
        this.typeId          = typeId;
        this.weightReduction = weightReduction;
        this.maxWeight       = maxWeight;
        this.slot            = slot;
    }

    public BackpackConfigUpdatePacket(ResourceLocation typeId,
                                      double weightReduction,
                                      double maxWeight) {
        this(typeId, weightReduction, maxWeight, SLOT_TYPE_WIDE);
    }

    // Fixed: Reordered to match (Buffer, Packet) type signatures
    public static void encode(RegistryFriendlyByteBuf buf, BackpackConfigUpdatePacket pkt) {
        buf.writeResourceLocation(pkt.typeId);
        buf.writeDouble(pkt.weightReduction);
        buf.writeDouble(pkt.maxWeight);
        buf.writeInt(pkt.slot);
    }

    public static BackpackConfigUpdatePacket decode(RegistryFriendlyByteBuf buf) {
        return new BackpackConfigUpdatePacket(
                buf.readResourceLocation(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readInt()
        );
    }

    // 3. Update the handle method signature to use the modern IPayloadContext
    public static void handle(final BackpackConfigUpdatePacket pkt, final IPayloadContext context) {
        context.enqueueWork(() -> {
            // Natively extract and safely cast the server-bound sending player instance
            if (context.player() instanceof ServerPlayer player) {
                if (pkt.slot == SLOT_TYPE_WIDE) {
                    handleTypeWide(player, pkt);
                } else {
                    handleInstance(player, pkt);
                }
            }
        });
    }

    private static void handleTypeWide(ServerPlayer player, BackpackConfigUpdatePacket pkt) {
        ItemWeightRegistry.setBackpackWeightReduction(pkt.typeId, pkt.weightReduction);
        ItemWeightRegistry.setBackpackMaxWeight(pkt.typeId, pkt.maxWeight);
        ItemWeightRegistry.saveToFile(ItemWeightRegistry.getConfigFile());

        for (ItemStack stack : player.getInventory().items) {
            applyIfMatchesType(stack, pkt.typeId, pkt.weightReduction, pkt.maxWeight);
        }
        for (ItemStack stack : player.getInventory().armor) {
            applyIfMatchesType(stack, pkt.typeId, pkt.weightReduction, pkt.maxWeight);
        }
        applyIfMatchesType(player.getOffhandItem(), pkt.typeId, pkt.weightReduction, pkt.maxWeight);
    }

    private static void handleInstance(ServerPlayer player, BackpackConfigUpdatePacket pkt) {
        ItemStack target = getStackAtSlot(player, pkt.slot);

        if (target.isEmpty()) {
            handleTypeWide(player, pkt);
            return;
        }

        if (!BackpackIdUtils.isBackpackItem(target)) return;

        ResourceLocation actualType = BuiltInRegistries.ITEM.getKey(target.getItem());
        ResourceLocation effectiveType = BackpackIdUtils.getBackpackId(target).orElse(actualType);

        if (!pkt.typeId.equals(effectiveType) && !pkt.typeId.equals(actualType)) return;

        String uuid = BackpackIdUtils.getOrCreateInstanceUUID(target);
        ResourceLocation instanceKey = BackpackIdUtils.uuidToResourceLocation(uuid);

        ItemWeightRegistry.setBackpackWeightReduction(instanceKey, pkt.weightReduction);
        ItemWeightRegistry.setBackpackMaxWeight(instanceKey, pkt.maxWeight);
        ItemWeightRegistry.saveToFile(ItemWeightRegistry.getConfigFile());

        BackpackIdUtils.setInstanceReduction(target, pkt.weightReduction);
        BackpackIdUtils.setInstanceMaxWeight(target, pkt.maxWeight);
    }

    private static ItemStack getStackAtSlot(ServerPlayer player, int slot) {
        if (slot >= 0 && slot < player.getInventory().items.size()) {
            return player.getInventory().items.get(slot);
        }
        if (slot >= ARMOR_SLOT_BASE && slot < ARMOR_SLOT_BASE + player.getInventory().armor.size()) {
            return player.getInventory().armor.get(slot - ARMOR_SLOT_BASE);
        }
        if (slot == SLOT_OFFHAND) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static void applyIfMatchesType(ItemStack stack,
                                           ResourceLocation targetTypeId,
                                           double reduction,
                                           double maxWeight) {
        if (stack.isEmpty() || !BackpackIdUtils.isBackpackItem(stack)) return;
        if (BackpackIdUtils.getInstanceUUID(stack).isPresent()) return;

        ResourceLocation effectiveType = BackpackIdUtils.getBackpackId(stack)
                .orElseGet(() -> BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (!targetTypeId.equals(effectiveType)) return;

        BackpackIdUtils.setInstanceReduction(stack, reduction);
        BackpackIdUtils.setInstanceMaxWeight(stack, maxWeight);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}