package com.armilp.ezweight.util;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class BackpackIdUtils {

    private static final String NBT_BACKPACK_ID = "BackpackId";
    private static final String NBT_INSTANCE_UUID = "EZWInstanceUUID";
    private static final String NBT_REDUCTION = "EZWReduction";
    private static final String NBT_MAX_WEIGHT = "EZWMaxWeight";

    private static final String INSTANCE_NAMESPACE = "ezweight";
    private static final String INSTANCE_PATH_PREFIX = "instance_";

    public static final boolean TRAVELERS_BACKPACK_LOADED =
            ModList.get().isLoaded("travelersbackpack");

    static final boolean SOPHISTICATED_BACKPACKS_LOADED =
            ModList.get().isLoaded("sophisticatedbackpacks");

    private static final Class<?> TRAVELERS_BACKPACK_ITEM_CLASS =
            loadClass("com.tiviacz.travelersbackpack.items.TravelersBackpackItem");

    private static final Class<?> SOPHISTICATED_BACKPACK_ITEM_CLASS =
            loadClass("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");

    private static final Class<?> BACKPACKED_BACKPACK_ITEM_CLASS =
            loadClass("com.mrcrayfish.backpacked.item.BackpackItem");

    private static Set<ResourceLocation> customBackpackCache = null;

    private static Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void invalidateCustomBackpackCache() {
        customBackpackCache = null;
    }

    private static Set<ResourceLocation> getCustomBackpackIds() {
        if (customBackpackCache == null) {
            customBackpackCache = new HashSet<>();

            for (String entry : WeightConfig.COMMON.CUSTOM_BACKPACK_ITEMS.get()) {
                ResourceLocation rl = ResourceLocation.tryParse(entry.trim());

                if (rl != null) {
                    customBackpackCache.add(rl);
                } else {
                    EZWeight.LOGGER.warn("Invalid resource location in custom_backpack_items: '{}'", entry);
                }
            }
        }

        return customBackpackCache;
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        }
    }

    public static Optional<String> getInstanceUUID(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();

        CompoundTag tag = getCustomData(stack);

        if (!tag.contains(NBT_INSTANCE_UUID, Tag.TAG_STRING)) return Optional.empty();

        String raw = tag.getString(NBT_INSTANCE_UUID);
        return raw.isEmpty() ? Optional.empty() : Optional.of(raw);
    }

    public static String getOrCreateInstanceUUID(ItemStack stack) {
        if (stack.isEmpty()) return "";

        CompoundTag tag = getCustomData(stack);

        if (!tag.contains(NBT_INSTANCE_UUID, Tag.TAG_STRING)
                || tag.getString(NBT_INSTANCE_UUID).isEmpty()) {
            tag.putString(NBT_INSTANCE_UUID, UUID.randomUUID().toString());
            setCustomData(stack, tag);
        }

        return tag.getString(NBT_INSTANCE_UUID);
    }

    public static ResourceLocation uuidToResourceLocation(String uuid) {
        return ResourceLocation.fromNamespaceAndPath(
                INSTANCE_NAMESPACE,
                INSTANCE_PATH_PREFIX + uuid.replace("-", "")
        );
    }

    public static Optional<ResourceLocation> getInstanceRegistryKey(ItemStack stack) {
        return getInstanceUUID(stack).map(BackpackIdUtils::uuidToResourceLocation);
    }

    public static boolean hasBackpackId(ItemStack stack) {
        if (stack.isEmpty()) return false;

        CompoundTag tag = getCustomData(stack);
        return tag.contains(NBT_BACKPACK_ID, Tag.TAG_STRING);
    }

    public static Optional<ResourceLocation> getBackpackId(ItemStack stack) {
        if (!hasBackpackId(stack)) return Optional.empty();

        CompoundTag tag = getCustomData(stack);
        String raw = tag.getString(NBT_BACKPACK_ID);

        ResourceLocation parsed = ResourceLocation.tryParse(raw);

        if (parsed == null) {
            EZWeight.LOGGER.warn("Invalid BackpackId format: {}", raw);
            return Optional.empty();
        }

        return Optional.of(parsed);
    }

    public static boolean setBackpackId(ItemStack stack, ResourceLocation backpackId) {
        if (stack.isEmpty()) return false;

        try {
            CompoundTag tag = getCustomData(stack);
            tag.putString(NBT_BACKPACK_ID, backpackId.toString());
            setCustomData(stack, tag);
            return true;
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to set BackpackId {} on item", backpackId, e);
            return false;
        }
    }

    public static boolean removeBackpackId(ItemStack stack) {
        if (!hasBackpackId(stack)) return false;

        try {
            CompoundTag tag = getCustomData(stack);
            tag.remove(NBT_BACKPACK_ID);
            setCustomData(stack, tag);
            return true;
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to remove BackpackId from item", e);
            return false;
        }
    }

    public static boolean hasInstanceReduction(ItemStack stack) {
        CompoundTag tag = getCustomData(stack);
        return tag.contains(NBT_REDUCTION, Tag.TAG_DOUBLE);
    }

    public static boolean hasInstanceMaxWeight(ItemStack stack) {
        CompoundTag tag = getCustomData(stack);
        return tag.contains(NBT_MAX_WEIGHT, Tag.TAG_DOUBLE);
    }

    public static void setInstanceReduction(ItemStack stack, double reduction) {
        if (stack.isEmpty()) return;

        CompoundTag tag = getCustomData(stack);
        tag.putDouble(NBT_REDUCTION, reduction);
        setCustomData(stack, tag);
    }

    public static void setInstanceMaxWeight(ItemStack stack, double maxWeight) {
        if (stack.isEmpty()) return;

        CompoundTag tag = getCustomData(stack);
        tag.putDouble(NBT_MAX_WEIGHT, maxWeight);
        setCustomData(stack, tag);
    }

    public static void clearInstanceValues(ItemStack stack) {
        if (stack.isEmpty()) return;

        CompoundTag tag = getCustomData(stack);
        tag.remove(NBT_REDUCTION);
        tag.remove(NBT_MAX_WEIGHT);
        tag.remove(NBT_INSTANCE_UUID);
        setCustomData(stack, tag);
    }

    private static Double lookupRegistryValue(
            ItemStack stack,
            String nbtKey,
            Function<ResourceLocation, Double> registryFn
    ) {
        CompoundTag nbt = getCustomData(stack);

        if (nbt.contains(nbtKey, Tag.TAG_DOUBLE)) {
            return nbt.getDouble(nbtKey);
        }

        Optional<String> uuid = getInstanceUUID(stack);
        if (uuid.isPresent()) {
            Double v = registryFn.apply(uuidToResourceLocation(uuid.get()));
            if (v != null) return v;
        }

        Optional<ResourceLocation> backpackId = getBackpackId(stack);
        if (backpackId.isPresent()) {
            Double v = registryFn.apply(backpackId.get());
            if (v != null) return v;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId != null) {
            return registryFn.apply(itemId);
        }

        return null;
    }

    public static double getWeightReductionPercent(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;

        Double found = lookupRegistryValue(
                stack,
                NBT_REDUCTION,
                ItemWeightRegistry::getBackpackWeightReduction
        );

        if (found != null) return found;

        if (isBackpackItem(stack)) {
            return WeightConfig.COMMON.BACKPACK_WEIGHT_REDUCTION_DEFAULT.get();
        }

        return 0.0;
    }

    public static Double getBackpackMaxWeight(ItemStack stack) {
        if (stack.isEmpty()) return null;

        return lookupRegistryValue(
                stack,
                NBT_MAX_WEIGHT,
                ItemWeightRegistry::getBackpackMaxWeight
        );
    }

    public static boolean isBackpackItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (hasBackpackId(stack)) return true;

        Item item = stack.getItem();

        if (TRAVELERS_BACKPACK_LOADED
                && TRAVELERS_BACKPACK_ITEM_CLASS != null
                && TRAVELERS_BACKPACK_ITEM_CLASS.isInstance(item)) {
            return true;
        }

        if (SOPHISTICATED_BACKPACKS_LOADED
                && SOPHISTICATED_BACKPACK_ITEM_CLASS != null
                && SOPHISTICATED_BACKPACK_ITEM_CLASS.isInstance(item)) {
            return true;
        }

        if (BACKPACKED_BACKPACK_ITEM_CLASS != null
                && BACKPACKED_BACKPACK_ITEM_CLASS.isInstance(item)) {
            return true;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        if (itemId != null && itemId.getNamespace().equals("modular_backpacks")) {
            return true;
        }

        return itemId != null && getCustomBackpackIds().contains(itemId);
    }

    public static double calculateBackpackContentsWeight(ItemStack backpack, double originalContentsWeight) {
        if (!isBackpackItem(backpack)) return originalContentsWeight;

        double reduction = getWeightReductionPercent(backpack);
        double reduced = originalContentsWeight * (1.0 - reduction);

        Double max = getBackpackMaxWeight(backpack);
        if (max != null && reduced > max) return max;

        return reduced;
    }

    public static BackpackInfo getBackpackInfo(ItemStack stack) {
        return new BackpackInfo(stack);
    }

    public static class BackpackInfo {
        private final ItemStack stack;
        private final Optional<ResourceLocation> backpackId;
        private final Optional<String> instanceUUID;
        private final ResourceLocation itemId;
        private final boolean hasBackpackId;
        private final boolean isBackpack;
        private final boolean hasInstanceNBT;
        private final double weightReduction;
        private final Double maxWeight;

        public BackpackInfo(ItemStack stack) {
            this.stack = stack;
            this.backpackId = BackpackIdUtils.getBackpackId(stack);
            this.instanceUUID = BackpackIdUtils.getInstanceUUID(stack);
            this.itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            this.hasBackpackId = backpackId.isPresent();
            this.isBackpack = BackpackIdUtils.isBackpackItem(stack);
            this.hasInstanceNBT = BackpackIdUtils.hasInstanceReduction(stack)
                    || BackpackIdUtils.hasInstanceMaxWeight(stack);
            this.weightReduction = BackpackIdUtils.getWeightReductionPercent(stack);
            this.maxWeight = BackpackIdUtils.getBackpackMaxWeight(stack);
        }

        public ItemStack getStack() {
            return stack;
        }

        public Optional<ResourceLocation> getBackpackId() {
            return backpackId;
        }

        public Optional<String> getInstanceUUID() {
            return instanceUUID;
        }

        public ResourceLocation getItemId() {
            return itemId;
        }

        public boolean hasBackpackId() {
            return hasBackpackId;
        }

        public boolean isBackpack() {
            return isBackpack;
        }

        public boolean hasInstanceNBT() {
            return hasInstanceNBT;
        }

        public double getWeightReduction() {
            return weightReduction;
        }

        public double getWeightReductionPercent() {
            return weightReduction * 100.0;
        }

        public Double getMaxWeight() {
            return maxWeight;
        }

        public String getDisplayName() {
            return stack.getHoverName().getString();
        }

        public ResourceLocation getEffectiveId() {
            if (instanceUUID.isPresent()) {
                return BackpackIdUtils.uuidToResourceLocation(instanceUUID.get());
            }

            if (hasBackpackId) {
                return backpackId.get();
            }

            return itemId;
        }

        @Override
        public String toString() {
            return String.format(
                    "BackpackInfo{item=%s, uuid=%s, backpackId=%s, instanceNBT=%b, reduction=%.1f%%, maxWeight=%s}",
                    itemId,
                    instanceUUID.orElse("none"),
                    backpackId.orElse(null),
                    hasInstanceNBT,
                    getWeightReductionPercent(),
                    maxWeight != null ? maxWeight + "kg" : "unlimited"
            );
        }
    }
}