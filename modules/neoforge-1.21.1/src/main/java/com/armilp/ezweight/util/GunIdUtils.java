//package com.armilp.ezweight.util;
//
//import com.armilp.ezweight.EZWeight;
//import com.armilp.ezweight.config.WeightConfig;
//import com.armilp.ezweight.data.ItemWeightRegistry;
////import com.tacz.guns.api.DefaultAssets;
////import com.tacz.guns.api.item.IGun;
////import com.tacz.guns.api.item.attachment.AttachmentType;
////import com.tacz.guns.api.item.gun.AbstractGunItem;
//import net.minecraft.core.component.DataComponents;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.ListTag;
//import net.minecraft.nbt.Tag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.component.CustomData;
//import net.neoforged.fml.ModList;
//
//import java.util.*;
//
//public class GunIdUtils {
//
//    private static final boolean TACZ_LOADED = ModList.get().isLoaded("tacz");
//
//    static int rounds = 0;
//
//    private static CompoundTag getCustomData(ItemStack stack) {
//        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
//    }
//
//    private static void setCustomData(ItemStack stack, CompoundTag tag) {
//        if (tag.isEmpty()) {
//            stack.remove(DataComponents.CUSTOM_DATA);
//        } else {
//            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
//        }
//    }
//
//    public static boolean hasGunId(ItemStack stack) {
//        if (stack.isEmpty()) return false;
//        return getCustomData(stack).contains("GunId", Tag.TAG_STRING);
//    }
//
//    public static boolean hasAttachmentId(ItemStack stack) {
//        if (stack.isEmpty()) return false;
//        return getCustomData(stack).contains("AttachmentId", Tag.TAG_STRING);
//    }
//
//    public static boolean hasAmmoId(ItemStack stack) {
//        if (stack.isEmpty()) return false;
//        return getCustomData(stack).contains("AmmoId", Tag.TAG_STRING);
//    }
//
//    public static Optional<ResourceLocation> getGunId(ItemStack stack) {
//        if (!hasGunId(stack)) return Optional.empty();
//
//        String id = getCustomData(stack).getString("GunId");
//        ResourceLocation parsed = ResourceLocation.tryParse(id);
//
//        if (parsed == null) {
//            EZWeight.LOGGER.warn("Invalid GunId format in item: {}", id);
//            return Optional.empty();
//        }
//
//        return Optional.of(parsed);
//    }
//
//    public static Optional<ResourceLocation> getAttachmentId(ItemStack stack) {
//        if (!hasAttachmentId(stack)) return Optional.empty();
//
//        String id = getCustomData(stack).getString("AttachmentId");
//        ResourceLocation parsed = ResourceLocation.tryParse(id);
//
//        if (parsed == null) {
//            EZWeight.LOGGER.warn("Invalid AttachmentId format in item: {}", id);
//            return Optional.empty();
//        }
//
//        return Optional.of(parsed);
//    }
//
//    public static Optional<ResourceLocation> getAmmoId(ItemStack stack) {
//        if (!hasAmmoId(stack)) return Optional.empty();
//
//        String id = getCustomData(stack).getString("AmmoId");
//        ResourceLocation parsed = ResourceLocation.tryParse(id);
//
//        if (parsed == null) {
//            EZWeight.LOGGER.warn("Invalid AmmoId format in item: {}", id);
//            return Optional.empty();
//        }
//
//        return Optional.of(parsed);
//    }
//
//    public static boolean setGunId(ItemStack stack, ResourceLocation gunId) {
//        if (stack.isEmpty()) return false;
//
////        if (TACZ_LOADED && !(stack.getItem() instanceof AbstractGunItem)) {
////            EZWeight.LOGGER.warn(
////                    "Attempting to set GunId on non-gun item: {}",
////                    BuiltInRegistries.ITEM.getKey(stack.getItem())
////            );
////            return false;
////        }
//
//        try {
//            CompoundTag tag = getCustomData(stack);
//            tag.putString("GunId", gunId.toString());
//            setCustomData(stack, tag);
//            return true;
//        } catch (Exception e) {
//            EZWeight.LOGGER.error("Failed to set GunId {} on item", gunId, e);
//            return false;
//        }
//    }
//
//    public static void setAttachmentId(ItemStack stack, ResourceLocation attachmentId) {
//        if (stack.isEmpty()) return;
//
//        try {
//            CompoundTag tag = getCustomData(stack);
//            tag.putString("AttachmentId", attachmentId.toString());
//            setCustomData(stack, tag);
//        } catch (Exception e) {
//            EZWeight.LOGGER.error("Failed to set AttachmentId {} on item", attachmentId, e);
//        }
//    }
//
//    public static void removeGunId(ItemStack stack) {
//        if (!hasGunId(stack)) return;
//
//        try {
//            CompoundTag tag = getCustomData(stack);
//            tag.remove("GunId");
//            setCustomData(stack, tag);
//        } catch (Exception e) {
//            EZWeight.LOGGER.error("Failed to remove GunId from item", e);
//        }
//    }
//
//    public static void removeAttachmentId(ItemStack stack) {
//        if (!hasAttachmentId(stack)) return;
//
//        try {
//            CompoundTag tag = getCustomData(stack);
//            tag.remove("AttachmentId");
//            setCustomData(stack, tag);
//        } catch (Exception e) {
//            EZWeight.LOGGER.error("Failed to remove AttachmentId from item", e);
//        }
//    }
//
//    public static void removeAmmoId(ItemStack stack) {
//        if (!hasAmmoId(stack)) return;
//
//        try {
//            CompoundTag tag = getCustomData(stack);
//            tag.remove("AmmoId");
//            setCustomData(stack, tag);
//        } catch (Exception e) {
//            EZWeight.LOGGER.error("Failed to remove AmmoId from item", e);
//        }
//    }
//
//    public static boolean isValidGunId(ResourceLocation gunId) {
//        if (!TACZ_LOADED) return true;
//
//        try {
//////            Object res = com.tacz.guns.api.TimelessAPI.getAllCommonGunIndex();
////
////            if (containsResourceLocation(res, gunId)) {
////                return true;
////            }
//
//            try {
//                Class<?> api = Class.forName("com.tacz.guns.api.TimelessAPI");
//
//                for (String name : new String[]{"getAllGunIndex"}) {
//                    try {
//                        java.lang.reflect.Method method = api.getMethod(name);
//                        Object result = method.invoke(null);
//
//                        if (containsResourceLocation(result, gunId)) {
//                            return true;
//                        }
//                    } catch (NoSuchMethodException ignored) {
//                    }
//                }
//            } catch (Exception ignored) {
//            }
//
//            return false;
//        } catch (Exception e) {
//            EZWeight.LOGGER.error("Error checking GunId validity: {}", gunId, e);
//            return false;
//        }
//    }
//
//    private static boolean containsResourceLocation(Object object, ResourceLocation id) {
//        if (object instanceof Set<?> set) {
//            for (Object entry : set) {
//                if (entry instanceof Map.Entry<?, ?> mapEntry) {
//                    if (id.equals(mapEntry.getKey())) return true;
//                } else if (id.equals(entry)) {
//                    return true;
//                }
//            }
//        } else if (object instanceof Map<?, ?> map) {
//            return map.containsKey(id);
//        }
//
//        return false;
//    }
//
//    public static GunInfo getGunInfo(ItemStack stack) {
//        return new GunInfo(stack);
//    }
//
//    public static double calculateTotalWeight(ItemStack stack) {
//        if (stack.isEmpty()) return 0.0;
//
//        ResourceLocation baseId = getGunId(stack)
//                .orElseGet(() -> BuiltInRegistries.ITEM.getKey(stack.getItem()));
//
//        double baseWeight = 1.0;
//
//        if (baseId != null) {
//            Double weight = ItemWeightRegistry.getAllWeights().get(baseId);
//            baseWeight = weight != null ? weight : 1.0;
//        }
//
//        return baseWeight + getAttachmentsWeight(stack) + getAmmoWeight(stack);
//    }
//
//    private static void collectResourceLocations(Tag nbt, List<ResourceLocation> out) {
//        if (nbt instanceof CompoundTag compound) {
//            for (String key : compound.getAllKeys()) {
//                Tag child = compound.get(key);
//                if (child == null) continue;
//
//                if (child.getId() == Tag.TAG_STRING) {
//                    String value = compound.getString(key);
//                    ResourceLocation parsed = ResourceLocation.tryParse(value);
//
//                    if (parsed != null) {
//                        out.add(parsed);
//                    }
//                } else if (child.getId() == Tag.TAG_LIST || child.getId() == Tag.TAG_COMPOUND) {
//                    collectResourceLocations(child, out);
//                }
//            }
//        } else if (nbt instanceof ListTag list) {
//            for (int i = 0; i < list.size(); i++) {
//                Tag element = list.get(i);
//
//                if (element.getId() == Tag.TAG_STRING) {
//                    String value = list.getString(i);
//                    ResourceLocation parsed = ResourceLocation.tryParse(value);
//
//                    if (parsed != null) {
//                        out.add(parsed);
//                    }
//                } else if (element.getId() == Tag.TAG_COMPOUND || element.getId() == Tag.TAG_LIST) {
//                    collectResourceLocations(element, out);
//                }
//            }
//        }
//    }
//
//    public static double getBaseWeight(ItemStack stack) {
//        if (stack.isEmpty()) return 0.0;
//
//        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
//
//        if (itemId != null) {
//            return ItemWeightRegistry.getAllWeights().getOrDefault(itemId, 1.0);
//        }
//
//        return 1.0;
//    }
//
//    public static double getAttachmentsWeight(ItemStack stack) {
//        if (stack.isEmpty()) return 0.0;
//        if (!WeightConfig.COMMON.ATTACHMENT_WEIGHT_ENABLED.get()) return 0.0;
//
//        double sum = 0.0;
//        double multiplier = WeightConfig.COMMON.ATTACHMENT_WEIGHT_MULTIPLIER.get();
//
//        if (TACZ_LOADED) {
//            IGun gun = IGun.getIGunOrNull(stack);
//
//            if (gun != null) {
//                for (AttachmentType type : AttachmentType.values()) {
//                    try {
//                        ResourceLocation attachId = gun.getAttachmentId(stack, type);
//
//                        if (DefaultAssets.isEmptyAttachmentId(attachId)) {
//                            continue;
//                        }
//
//                        Double weight = resolveWeightForId(attachId);
//
//                        if (weight != null) {
//                            sum += weight;
//                        }
//                    } catch (Exception ignored) {
//                    }
//                }
//
//                return Math.max(0.0, sum * Math.max(0.0, multiplier));
//            }
//        }
//
//        CompoundTag tag = getCustomData(stack);
//        if (tag.isEmpty()) return 0.0;
//
//        Set<ResourceLocation> validAttachmentIds = getKnownAttachmentIds();
//        List<ResourceLocation> found = new ArrayList<>();
//
//        collectResourceLocations(tag, found);
//
//        Set<ResourceLocation> counted = new HashSet<>();
//
//        for (ResourceLocation rawId : found) {
//            ResourceLocation base = normalizeBaseId(rawId);
//
//            if (base == null) continue;
//            if (!validAttachmentIds.isEmpty() && !validAttachmentIds.contains(base)) continue;
//            if (counted.contains(base)) continue;
//
//            Double weight = resolveWeightForId(rawId);
//
//            if (weight != null) {
//                sum += weight;
//                counted.add(base);
//            }
//        }
//
//        return Math.max(0.0, sum * Math.max(0.0, multiplier));
//    }
//
//    private static ResourceLocation normalizeBaseId(ResourceLocation id) {
//        if (id == null) return null;
//
//        String namespace = id.getNamespace();
//        String path = id.getPath();
//
//        if (path.endsWith("_data")) {
//            return ResourceLocation.fromNamespaceAndPath(namespace, path.substring(0, path.length() - 5));
//        }
//
//        if (path.endsWith("_display")) {
//            return ResourceLocation.fromNamespaceAndPath(namespace, path.substring(0, path.length() - 8));
//        }
//
//        return id;
//    }
//
//    public static double getAmmoWeight(ItemStack stack) {
//        if (stack.isEmpty()) return 0.0;
//        if (!WeightConfig.COMMON.AMMO_WEIGHT_ENABLED.get()) return 0.0;
//
//        CompoundTag tag = getCustomData(stack);
//        if (tag.isEmpty()) return 0.0;
//
//        double perRoundWeight = WeightConfig.COMMON.AMMO_WEIGHT_PER_ROUND.get();
//
//        Optional<ResourceLocation> gunIdOpt = getGunId(stack);
//
//        if (gunIdOpt.isPresent()) {
//            Double gunSpecificWeight = ItemWeightRegistry.getGunAmmoWeight(gunIdOpt.get());
//
//            if (gunSpecificWeight != null) {
//                perRoundWeight = gunSpecificWeight;
//            }
//        }
//
//        Optional<ResourceLocation> ammoIdOpt = getAmmoId(stack);
//
//        if (ammoIdOpt.isPresent()) {
//            Double configured = resolveWeightForId(ammoIdOpt.get());
//
//            if (configured != null) {
//                perRoundWeight = configured;
//            }
//        } else {
//            List<ResourceLocation> found = new ArrayList<>();
//            collectResourceLocations(tag, found);
//
//            Set<ResourceLocation> validAmmoIds = getKnownAmmoIds();
//
//            for (ResourceLocation id : found) {
//                ResourceLocation base = normalizeBaseId(id);
//
//                if (base == null) continue;
//                if (!validAmmoIds.isEmpty() && !validAmmoIds.contains(base)) continue;
//
//                Double configured = resolveWeightForId(id);
//
//                if (configured != null) {
//                    perRoundWeight = configured;
//                    break;
//                }
//            }
//        }
//
//        if (TACZ_LOADED) {
//            IGun gun = IGun.getIGunOrNull(stack);
//
//            if (gun != null) {
//                rounds = gun.getCurrentAmmoCount(stack);
//
//                if (gun.hasBulletInBarrel(stack)) {
//                    rounds += 1;
//                }
//            } else {
//                rounds = estimateLoadedRounds(tag, gunIdOpt.orElse(null));
//            }
//        } else {
//            rounds = estimateLoadedRounds(tag, gunIdOpt.orElse(null));
//        }
//
//        double multiplier = WeightConfig.COMMON.AMMO_WEIGHT_MULTIPLIER.get();
//
//        return Math.max(0.0, rounds * perRoundWeight * Math.max(0.0, multiplier));
//    }
//
//    private static Double resolveWeightForId(ResourceLocation id) {
//        Double weight = ItemWeightRegistry.getAllWeights().get(id);
//
//        if (weight != null) {
//            return weight;
//        }
//
//        String path = id.getPath();
//
//        if (path.endsWith("_data")) {
//            ResourceLocation base = ResourceLocation.fromNamespaceAndPath(
//                    id.getNamespace(),
//                    path.substring(0, path.length() - 5)
//            );
//
//            return ItemWeightRegistry.getAllWeights().get(base);
//        }
//
//        if (path.endsWith("_display")) {
//            ResourceLocation base = ResourceLocation.fromNamespaceAndPath(
//                    id.getNamespace(),
//                    path.substring(0, path.length() - 8)
//            );
//
//            return ItemWeightRegistry.getAllWeights().get(base);
//        }
//
//        return null;
//    }
//
//    private static Set<ResourceLocation> getKnownAttachmentIds() {
//        Set<ResourceLocation> keys = new HashSet<>();
//
//        if (!TACZ_LOADED) return keys;
//
//        try {
//            Object result = com.tacz.guns.api.TimelessAPI.getAllCommonAttachmentIndex();
//            collectKeys(result, keys);
//
//            if (keys.isEmpty()) {
//                Class<?> api = Class.forName("com.tacz.guns.api.TimelessAPI");
//
//                for (String name : new String[]{"getAllAttachmentIndex"}) {
//                    try {
//                        java.lang.reflect.Method method = api.getMethod(name);
//                        collectKeys(method.invoke(null), keys);
//                    } catch (NoSuchMethodException ignored) {
//                    }
//                }
//            }
//        } catch (Throwable ignored) {
//        }
//
//        return keys;
//    }
//
//    private static Set<ResourceLocation> getKnownAmmoIds() {
//        Set<ResourceLocation> keys = new HashSet<>();
//
//        if (!TACZ_LOADED) return keys;
//
//        try {
//            Object result = com.tacz.guns.api.TimelessAPI.getAllCommonAmmoIndex();
//            collectKeys(result, keys);
//
//            if (keys.isEmpty()) {
//                Class<?> api = Class.forName("com.tacz.guns.api.TimelessAPI");
//
//                for (String name : new String[]{"getAllAmmoIndex"}) {
//                    try {
//                        java.lang.reflect.Method method = api.getMethod(name);
//                        collectKeys(method.invoke(null), keys);
//                    } catch (NoSuchMethodException ignored) {
//                    }
//                }
//            }
//        } catch (Throwable ignored) {
//        }
//
//        return keys;
//    }
//
//    private static void collectKeys(Object object, Set<ResourceLocation> keys) {
//        if (object instanceof Set<?> set) {
//            for (Object entry : set) {
//                if (entry instanceof Map.Entry<?, ?> mapEntry) {
//                    if (mapEntry.getKey() instanceof ResourceLocation id) {
//                        keys.add(id);
//                    }
//                } else if (entry instanceof ResourceLocation id) {
//                    keys.add(id);
//                }
//            }
//        } else if (object instanceof Map<?, ?> map) {
//            for (Object key : map.keySet()) {
//                if (key instanceof ResourceLocation id) {
//                    keys.add(id);
//                }
//            }
//        }
//    }
//
//    private static int estimateLoadedRounds(CompoundTag tag, ResourceLocation gunId) {
//        try {
//            int count = 0;
//
//            for (String key : tag.getAllKeys()) {
//                Tag child = tag.get(key);
//                if (child == null) continue;
//
//                String lowerKey = key.toLowerCase();
//
//                if (child.getId() == Tag.TAG_INT || child.getId() == Tag.TAG_SHORT || child.getId() == Tag.TAG_BYTE) {
//                    int value = switch (child.getId()) {
//                        case Tag.TAG_INT -> tag.getInt(key);
//                        case Tag.TAG_SHORT -> tag.getShort(key);
//                        case Tag.TAG_BYTE -> tag.getByte(key);
//                        default -> 0;
//                    };
//
//                    if (lowerKey.contains("ammo")
//                            || lowerKey.contains("bullet")
//                            || lowerKey.contains("mag")
//                            || lowerKey.contains("round")) {
//                        count = Math.max(count, value);
//                    }
//                } else if (child.getId() == Tag.TAG_LIST) {
//                    if (lowerKey.contains("ammo")
//                            || lowerKey.contains("bullet")
//                            || lowerKey.contains("cartridge")
//                            || lowerKey.contains("mag")) {
//                        count = Math.max(count, ((ListTag) child).size());
//                    }
//                } else if (child.getId() == Tag.TAG_COMPOUND) {
//                    count = Math.max(count, estimateLoadedRounds((CompoundTag) child, gunId));
//                }
//            }
//
//            if (count > 0) {
//                return count;
//            }
//        } catch (Exception ignored) {
//        }
//
//        if (gunId != null) {
//            String path = gunId.getPath().toLowerCase();
//
//            if (path.contains("pistol")) return 12;
//            if (path.contains("shotgun")) return 8;
//            if (path.contains("sniper")) return 5;
//            if (path.contains("smg")) return 30;
//            if (path.contains("rifle")) return 30;
//        }
//
//        return 0;
//    }
//
//    public static class GunInfo {
//        private final ItemStack stack;
//        private final Optional<ResourceLocation> gunId;
//        private final Optional<ResourceLocation> attachmentId;
//        private final Optional<ResourceLocation> ammoId;
//        private final ResourceLocation itemId;
//        private final boolean hasGunId;
//        private final boolean hasAttachmentId;
//        private final boolean hasAmmoId;
//        private final boolean isValidGunId;
//
//        public GunInfo(ItemStack stack) {
//            this.stack = stack;
//            this.gunId = GunIdUtils.getGunId(stack);
//            this.attachmentId = GunIdUtils.getAttachmentId(stack);
//            this.ammoId = GunIdUtils.getAmmoId(stack);
//            this.itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
//            this.hasGunId = gunId.isPresent();
//            this.hasAttachmentId = attachmentId.isPresent();
//            this.hasAmmoId = ammoId.isPresent();
//            this.isValidGunId = hasGunId && GunIdUtils.isValidGunId(gunId.get());
//        }
//
//        public ItemStack getStack() {
//            return stack;
//        }
//
//        public Optional<ResourceLocation> getGunId() {
//            return gunId;
//        }
//
//        public Optional<ResourceLocation> getAttachmentId() {
//            return attachmentId;
//        }
//
//        public Optional<ResourceLocation> getAmmoId() {
//            return ammoId;
//        }
//
//        public ResourceLocation getItemId() {
//            return itemId;
//        }
//
//        public boolean hasGunId() {
//            return hasGunId;
//        }
//
//        public boolean hasAttachmentId() {
//            return hasAttachmentId;
//        }
//
//        public boolean hasAmmoId() {
//            return hasAmmoId;
//        }
//
//        public String getDisplayName() {
//            return stack.getHoverName().getString();
//        }
//
//        public ResourceLocation getEffectiveId() {
//            if (hasGunId) return gunId.get();
//            if (hasAttachmentId) return attachmentId.get();
//            if (hasAmmoId) return ammoId.get();
//            return itemId;
//        }
//
//        @Override
//        public String toString() {
//            return String.format(
//                    "GunInfo{item=%s, gunId=%s, valid=%s}",
//                    itemId,
//                    gunId.orElse(null),
//                    isValidGunId
//            );
//        }
//    }
//}