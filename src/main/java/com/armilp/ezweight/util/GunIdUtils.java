package com.armilp.ezweight.util;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class GunIdUtils {

    private static final boolean TACZ_LOADED = ModList.get().isLoaded("tacz");

    public static boolean hasGunId(ItemStack stack) {
        if (stack.isEmpty()) return false;

        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("GunId", Tag.TAG_STRING);
    }

    public static boolean hasAttachmentId(ItemStack stack) {
        if (stack.isEmpty()) return false;

        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("AttachmentId", Tag.TAG_STRING);
    }

    public static boolean hasAmmoId(ItemStack stack) {
        if (stack.isEmpty()) return false;

        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("AmmoId", Tag.TAG_STRING);
    }

    public static Optional<ResourceLocation> getGunId(ItemStack stack) {
        if (!hasGunId(stack)) return Optional.empty();

        try {
            String gunIdString = stack.getTag().getString("GunId");
            return Optional.of(new ResourceLocation(gunIdString));
        } catch (Exception e) {
            EZWeight.LOGGER.warn("Invalid GunId format in item: {}",
                    stack.getTag().getString("GunId"));
            return Optional.empty();
        }
    }

    public static Optional<ResourceLocation> getAttachmentId(ItemStack stack) {
        if (!hasAttachmentId(stack)) return Optional.empty();

        try {
            String id = stack.getTag().getString("AttachmentId");
            return Optional.of(new ResourceLocation(id));
        } catch (Exception e) {
            EZWeight.LOGGER.warn("Invalid AttachmentId format in item: {}", stack.getTag().getString("AttachmentId"));
            return Optional.empty();
        }
    }

    public static Optional<ResourceLocation> getAmmoId(ItemStack stack) {
        if (!hasAmmoId(stack)) return Optional.empty();

        try {
            String id = stack.getTag().getString("AmmoId");
            return Optional.of(new ResourceLocation(id));
        } catch (Exception e) {
            EZWeight.LOGGER.warn("Invalid AmmoId format in item: {}", stack.getTag().getString("AmmoId"));
            return Optional.empty();
        }
    }

    public static boolean setGunId(ItemStack stack, ResourceLocation gunId) {
        if (stack.isEmpty()) return false;

        if (TACZ_LOADED && !(stack.getItem() instanceof AbstractGunItem)) {
            EZWeight.LOGGER.warn("Attempting to set GunId on non-gun item: {}",
                    ForgeRegistries.ITEMS.getKey(stack.getItem()));
            return false;
        }

        try {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("GunId", gunId.toString());
            return true;
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to set GunId {} on item", gunId, e);
            return false;
        }
    }

    public static boolean setAttachmentId(ItemStack stack, ResourceLocation attachmentId) {
        if (stack.isEmpty()) return false;

        try {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("AttachmentId", attachmentId.toString());
            return true;
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to set AttachmentId {} on item", attachmentId, e);
            return false;
        }
    }

    public static boolean setAmmoId(ItemStack stack, ResourceLocation ammoId) {
        if (stack.isEmpty()) return false;

        try {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("AmmoId", ammoId.toString());
            return true;
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to set AmmoId {} on item", ammoId, e);
            return false;
        }
    }

    public static boolean removeGunId(ItemStack stack) {
        if (!hasGunId(stack)) return false;

        try {
            CompoundTag tag = stack.getTag();
            tag.remove("GunId");
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
            return true;
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to remove GunId from item", e);
            return false;
        }
    }

    public static boolean removeAttachmentId(ItemStack stack) {
        if (!hasAttachmentId(stack)) return false;

        try {
            CompoundTag tag = stack.getTag();
            tag.remove("AttachmentId");
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
            return true;
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to remove AttachmentId from item", e);
            return false;
        }
    }

    public static boolean removeAmmoId(ItemStack stack) {
        if (!hasAmmoId(stack)) return false;

        try {
            CompoundTag tag = stack.getTag();
            tag.remove("AmmoId");
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
            return true;
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to remove AmmoId from item", e);
            return false;
        }
    }

    public static ItemStack cloneWithGunId(ItemStack original, ResourceLocation newGunId) {
        ItemStack clone = original.copy();
        setGunId(clone, newGunId);
        return clone;
    }

    public static boolean isValidGunId(ResourceLocation gunId) {
        if (!TACZ_LOADED) return true;

        try {
            return com.tacz.guns.api.TimelessAPI.getAllCommonGunIndex()
                    .stream()
                    .anyMatch(entry -> entry.getKey().equals(gunId));
        } catch (Exception e) {
            EZWeight.LOGGER.error("Error checking GunId validity: {}", gunId, e);
            return false;
        }
    }

    public static List<ItemStack> findItemsWithGunId(List<ItemStack> inventory) {
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : inventory) {
            if (hasGunId(stack)) {
                result.add(stack);
            }
        }

        return result;
    }

    public static List<ItemStack> findItemsByGunId(List<ItemStack> inventory, ResourceLocation targetGunId) {
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : inventory) {
            Optional<ResourceLocation> gunId = getGunId(stack);
            if (gunId.isPresent() && gunId.get().equals(targetGunId)) {
                result.add(stack);
            }
        }

        return result;
    }

    public static int updateGunId(List<ItemStack> inventory, ResourceLocation oldGunId, ResourceLocation newGunId) {
        int updated = 0;

        for (ItemStack stack : inventory) {
            Optional<ResourceLocation> currentGunId = getGunId(stack);
            if (currentGunId.isPresent() && currentGunId.get().equals(oldGunId)) {
                if (setGunId(stack, newGunId)) {
                    updated++;
                }
            }
        }

        EZWeight.LOGGER.info("Updated {} items from GunId {} to {}", updated, oldGunId, newGunId);
        return updated;
    }

    public static GunInfo getGunInfo(ItemStack stack) {
        return new GunInfo(stack);
    }

    public static double calculateTotalWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;

        ResourceLocation baseId = getGunId(stack).orElseGet(() -> ForgeRegistries.ITEMS.getKey(stack.getItem()));
        double baseWeight = baseId != null ? ItemWeightRegistry.getAllWeights().getOrDefault(baseId, 1.0) : 1.0;
        double attachmentsWeight = getAttachmentsWeight(stack);
        double ammoWeight = getAmmoWeight(stack);
        return baseWeight + attachmentsWeight + ammoWeight;
    }

    private static void collectResourceLocations(Tag nbt, List<ResourceLocation> out) {
        if (nbt instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                Tag child = compound.get(key);
                if (child == null) continue;
                if (child.getId() == Tag.TAG_STRING) {
                    try {
                        String val = compound.getString(key);
                        if (val != null && !val.isEmpty()) {
                            out.add(new ResourceLocation(val));
                        }
                    } catch (Exception ignored) {}
                } else if (child.getId() == Tag.TAG_LIST) {
                    collectResourceLocations(child, out);
                } else if (child.getId() == Tag.TAG_COMPOUND) {
                    collectResourceLocations(child, out);
                }
            }
        } else if (nbt instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                Tag elem = list.get(i);
                if (elem.getId() == Tag.TAG_STRING) {
                    try {
                        String val = list.getString(i);
                        if (val != null && !val.isEmpty()) {
                            out.add(new ResourceLocation(val));
                        }
                    } catch (Exception ignored) {}
                } else if (elem.getId() == Tag.TAG_COMPOUND || elem.getId() == Tag.TAG_LIST) {
                    collectResourceLocations(elem, out);
                }
            }
        }
    }

    public static double getBaseWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            return ItemWeightRegistry.getAllWeights().getOrDefault(itemId, 1.0);
        }

        return 1.0;
    }

    public static double getAttachmentsWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;

        double sum = 0.0;
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0.0;

        Set<ResourceLocation> validAttachmentIds = getKnownAttachmentIds();
        List<ResourceLocation> found = new ArrayList<>();
        collectResourceLocations(tag, found);

        Set<ResourceLocation> counted = new HashSet<>();
        for (ResourceLocation rawId : found) {
            if (rawId == null) continue;
            ResourceLocation base = normalizeBaseId(rawId);
            if (base == null) continue;
            if (!validAttachmentIds.isEmpty() && !validAttachmentIds.contains(base)) continue;
            if (counted.contains(base)) continue;

            Double w = resolveWeightForId(rawId);
            if (w != null) {
                sum += w;
                counted.add(base);
            }
        }

        return sum;
    }

    private static ResourceLocation normalizeBaseId(ResourceLocation id) {
        if (id == null) return null;
        String ns = id.getNamespace();
        String path = id.getPath();
        if (path.endsWith("_data")) {
            return new ResourceLocation(ns, path.substring(0, path.length() - 5));
        }
        if (path.endsWith("_display")) {
            return new ResourceLocation(ns, path.substring(0, path.length() - 8));
        }
        return id;
    }

    public static double getAmmoWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0.0;

        List<ResourceLocation> found = new ArrayList<>();
        collectResourceLocations(tag, found);

        Set<ResourceLocation> validAmmoIds = getKnownAmmoIds();
        ResourceLocation selectedAmmoId = null;
        for (ResourceLocation rl : found) {
            if (rl == null) continue;
            ResourceLocation base = normalizeBaseId(rl);
            if (base == null) continue;
            if (!validAmmoIds.isEmpty() && !validAmmoIds.contains(base)) continue;
            selectedAmmoId = rl;
            break;
        }

        double perRoundWeight = 0.02;
        if (selectedAmmoId != null) {
            Double cfg = resolveWeightForId(selectedAmmoId);
            if (cfg != null) perRoundWeight = Math.min(cfg, 0.05);
        } else if (getAmmoId(stack).isPresent()) {
            Double cfg = resolveWeightForId(getAmmoId(stack).get());
            if (cfg != null) perRoundWeight = Math.min(cfg, 0.05);
        }

        int rounds = estimateLoadedRounds(tag, getGunId(stack).orElse(null));
        return Math.max(0.0, rounds * perRoundWeight);
    }

    private static Double resolveWeightForId(ResourceLocation id) {
        // 1) Directo
        Double w = ItemWeightRegistry.getAllWeights().get(id);
        if (w != null) return w;
        // 2) Normalizar sufijos comunes (_data / _display)
        String path = id.getPath();
        if (path.endsWith("_data")) {
            ResourceLocation base = new ResourceLocation(id.getNamespace(), path.substring(0, path.length() - 5));
            w = ItemWeightRegistry.getAllWeights().get(base);
            if (w != null) return w;
        }
        if (path.endsWith("_display")) {
            ResourceLocation base = new ResourceLocation(id.getNamespace(), path.substring(0, path.length() - 8));
            w = ItemWeightRegistry.getAllWeights().get(base);
            if (w != null) return w;
        }
        return null;
    }

    private static Set<ResourceLocation> getKnownAttachmentIds() {
        Set<ResourceLocation> keys = new HashSet<>();
        if (!TACZ_LOADED) return keys;
        try {
            Class<?> api = Class.forName("com.tacz.guns.api.TimelessAPI");
            for (String name : new String[]{"getAllCommonAttachmentIndex", "getAllAttachmentIndex"}) {
                try {
                    java.lang.reflect.Method m = api.getMethod(name);
                    Object res = m.invoke(null);
                    if (res instanceof java.util.Set) {
                        for (Object e : ((java.util.Set<?>) res)) {
                            if (e instanceof java.util.Map.Entry) {
                                Object key = ((java.util.Map.Entry<?, ?>) e).getKey();
                                if (key instanceof ResourceLocation rl) keys.add(rl);
                            }
                        }
                    }
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Exception ignored) {}
        return keys;
    }

    private static Set<ResourceLocation> getKnownAmmoIds() {
        Set<ResourceLocation> keys = new HashSet<>();
        if (!TACZ_LOADED) return keys;
        try {
            Class<?> api = Class.forName("com.tacz.guns.api.TimelessAPI");
            for (String name : new String[]{"getAllCommonAmmoIndex", "getAllAmmoIndex"}) {
                try {
                    java.lang.reflect.Method m = api.getMethod(name);
                    Object res = m.invoke(null);
                    if (res instanceof java.util.Set) {
                        for (Object e : ((java.util.Set<?>) res)) {
                            if (e instanceof java.util.Map.Entry) {
                                Object key = ((java.util.Map.Entry<?, ?>) e).getKey();
                                if (key instanceof ResourceLocation rl) keys.add(rl);
                            }
                        }
                    }
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Exception ignored) {}
        return keys;
    }

    private static int estimateLoadedRounds(CompoundTag tag, ResourceLocation gunId) {
        try {
            int count = 0;
            for (String key : tag.getAllKeys()) {
                Tag child = tag.get(key);
                if (child == null) continue;
                String k = key.toLowerCase();
                if (child.getId() == Tag.TAG_INT || child.getId() == Tag.TAG_SHORT || child.getId() == Tag.TAG_BYTE) {
                    int val = (child.getId() == Tag.TAG_INT) ? tag.getInt(key) : (child.getId() == Tag.TAG_SHORT ? tag.getShort(key) : tag.getByte(key));
                    if (k.contains("ammo") || k.contains("bullet") || k.contains("mag") || k.contains("round")) {
                        count = Math.max(count, val);
                    }
                } else if (child.getId() == Tag.TAG_LIST) {
                    if (k.contains("ammo") || k.contains("bullet") || k.contains("cartridge") || k.contains("mag")) {
                        count = Math.max(count, ((ListTag) child).size());
                    }
                } else if (child.getId() == Tag.TAG_COMPOUND) {
                    count = Math.max(count, estimateLoadedRounds((CompoundTag) child, gunId));
                }
            }
            if (count > 0) return count;
        } catch (Exception ignored) {}

        if (gunId != null) {
            String p = gunId.getPath().toLowerCase();
            if (p.contains("pistol")) return 12;
            if (p.contains("shotgun")) return 8;
            if (p.contains("sniper")) return 5;
            if (p.contains("smg")) return 30;
            if (p.contains("rifle")) return 30;
        }
        return 0;
    }

    public static class GunInfo {
        private final ItemStack stack;
        private final Optional<ResourceLocation> gunId;
        private final Optional<ResourceLocation> attachmentId;
        private final Optional<ResourceLocation> ammoId;
        private final ResourceLocation itemId;
        private final boolean hasGunId;
        private final boolean hasAttachmentId;
        private final boolean hasAmmoId;
        private final boolean isValidGunId;

        public GunInfo(ItemStack stack) {
            this.stack = stack;
            this.gunId = GunIdUtils.getGunId(stack);
            this.attachmentId = GunIdUtils.getAttachmentId(stack);
            this.ammoId = GunIdUtils.getAmmoId(stack);
            this.itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            this.hasGunId = gunId.isPresent();
            this.hasAttachmentId = attachmentId.isPresent();
            this.hasAmmoId = ammoId.isPresent();
            this.isValidGunId = hasGunId && GunIdUtils.isValidGunId(gunId.get());
        }

        public ItemStack getStack() { return stack; }
        public Optional<ResourceLocation> getGunId() { return gunId; }
        public Optional<ResourceLocation> getAttachmentId() { return attachmentId; }
        public Optional<ResourceLocation> getAmmoId() { return ammoId; }
        public ResourceLocation getItemId() { return itemId; }
        public boolean hasGunId() { return hasGunId; }
        public boolean hasAttachmentId() { return hasAttachmentId; }
        public boolean hasAmmoId() { return hasAmmoId; }
        public boolean isValidGunId() { return isValidGunId; }

        public String getDisplayName() {
            return stack.getHoverName().getString();
        }

        public ResourceLocation getEffectiveId() {
            if (hasGunId) return gunId.get();
            if (hasAttachmentId) return attachmentId.get();
            if (hasAmmoId) return ammoId.get();
            return itemId;
        }

        @Override
        public String toString() {
            return String.format("GunInfo{item=%s, gunId=%s, valid=%s}",
                    itemId, gunId.orElse(null), isValidGunId);
        }
    }
}