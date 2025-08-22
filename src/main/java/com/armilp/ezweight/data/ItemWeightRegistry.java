package com.armilp.ezweight.data;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.client.gui.ItemStackWithWeight;
import com.armilp.ezweight.util.GunIdUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.item.AttachmentItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

public class ItemWeightRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, Double> ITEM_WEIGHTS = new HashMap<>();
    private static final String FILE_NAME = "items.json";
    private static final String DEFAULT_ASSET_PATH = "/assets/ezweight/items.json";
    private static File configFile;
    private static boolean taczLoaded = false;

    public static void init(Path configDir) {
        taczLoaded = ModList.get().isLoaded(GunMod.MOD_ID);
        configFile = configDir.resolve(FILE_NAME).toFile();

        if (!configFile.exists()) {
            copyDefaultFromAssets(configFile);
        }

        if (configFile.exists()) {
            loadFromFile(configFile);
        } else {
            generateDefaultFile(configFile);
        }
    }

    private static void copyDefaultFromAssets(File destFile) {
        try (InputStream in = ItemWeightRegistry.class.getResourceAsStream(DEFAULT_ASSET_PATH)) {
            if (in == null) {
                EZWeight.LOGGER.warn("No se encontró el items.json por defecto en assets: {}", DEFAULT_ASSET_PATH);
                return;
            }
            if (destFile.getParentFile() != null) {
                destFile.getParentFile().mkdirs();
            }
            try (OutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
            EZWeight.LOGGER.info("Copiado items.json por defecto desde assets a config.");
        } catch (Exception e) {
            EZWeight.LOGGER.error("No se pudo copiar items.json de assets a config", e);
        }
    }

    private static void loadFromFile(File file) {
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Map<String, Double>>>() {}.getType();
            Map<String, Map<String, Double>> categorizedMap = GSON.fromJson(reader, type);

            ITEM_WEIGHTS.clear();
            boolean updated = false;

            if (categorizedMap != null) {
                for (Map.Entry<String, Map<String, Double>> categoryEntry : categorizedMap.entrySet()) {
                    for (Map.Entry<String, Double> entry : categoryEntry.getValue().entrySet()) {
                        try {
                            ResourceLocation id = new ResourceLocation(entry.getKey());
                            ITEM_WEIGHTS.put(id, entry.getValue());
                        } catch (Exception ex) {
                            EZWeight.LOGGER.warn("Invalid item ID in config: {}", entry.getKey());
                        }
                    }
                }
            }

            updated |= addMissingItemsAndTACZGuns(categorizedMap != null ? categorizedMap : new HashMap<>());

            if (updated) {
                try (FileWriter writer = new FileWriter(file)) {
                    GSON.toJson(categorizedMap, writer);
                    EZWeight.LOGGER.info("Updated item weights file with new items.");
                }
            }

            EZWeight.LOGGER.info("Loaded {} item weights from config.", ITEM_WEIGHTS.size());
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to load item weights!", e);
        }
    }

    public static void saveToFile(File file) {
        try {
            Map<String, Map<String, Double>> categorizedMap = new LinkedHashMap<>();

            for (Map.Entry<ResourceLocation, Double> entry : ITEM_WEIGHTS.entrySet()) {
                ResourceLocation id = entry.getKey();
                String namespace = id.getNamespace();
                categorizedMap
                        .computeIfAbsent(namespace, k -> new LinkedHashMap<>())
                        .put(id.toString(), entry.getValue());
            }

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(categorizedMap, writer);
            }

            EZWeight.LOGGER.info("Item weights saved to file.");
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to save item weights!", e);
        }
    }

    private static void generateDefaultFile(File file) {
        Map<String, Map<String, Double>> categorizedWeights = new HashMap<>();
        ITEM_WEIGHTS.clear();

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null) {
                double weight = estimateWeight(item);
                ITEM_WEIGHTS.put(id, weight);

                String namespace = id.getNamespace();
                categorizedWeights
                        .computeIfAbsent(namespace, k -> new LinkedHashMap<>())
                        .put(id.toString(), weight);
            }
        }
        if (taczLoaded) {
            addTACZGunsToMap(categorizedWeights, ITEM_WEIGHTS);
        }

        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(categorizedWeights, writer);
                EZWeight.LOGGER.info("Generated default item weights with {} categories.", categorizedWeights.size());
            }
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to write default item weights!", e);
        }
    }

    private static double estimateWeight(Item item) {
        if (taczLoaded) {
            if (item instanceof AbstractGunItem) return 5.0;
            if (item instanceof AttachmentItem) return 0.8;
            if (item instanceof AmmoItem) return 0.2;
        }

        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        String itemName = id != null ? id.getPath().toLowerCase() : "";

        double baseWeight = 0.3;

        if (itemName.contains("sword") || itemName.contains("axe") || itemName.contains("pickaxe")) {
            baseWeight += 2.0;
        } else if (itemName.contains("shovel") || itemName.contains("hoe")) {
            baseWeight += 1.0;
        } else if (itemName.contains("helmet") || itemName.contains("chestplate") || itemName.contains("leggings") || itemName.contains("boots")) {
            baseWeight += 3.0;
        } else if (itemName.contains("block")) {
            baseWeight += 4.0;
        } else if (itemName.contains("nugget")) {
            baseWeight += 0.1;
        } else if (itemName.contains("ingot")) {
            baseWeight += 1.0;
        } else if (itemName.contains("stick") || itemName.contains("feather")) {
            baseWeight += 0.05;
        } else if (itemName.contains("food") || itemName.contains("bread") || itemName.contains("meat") || itemName.contains("apple")) {
            baseWeight += 0.2;
        } else if (itemName.contains("bucket")) {
            baseWeight += 1.5;
        } else if (itemName.contains("bow") || itemName.contains("crossbow")) {
            baseWeight += 1.0;
        }

        if (itemName.contains("wood")) {
            baseWeight *= 0.8;
        } else if (itemName.contains("stone")) {
            baseWeight *= 1.2;
        } else if (itemName.contains("iron")) {
            baseWeight *= 1.5;
        } else if (itemName.contains("gold")) {
            baseWeight *= 2.0;
        } else if (itemName.contains("diamond")) {
            baseWeight *= 2.5;
        } else if (itemName.contains("netherite")) {
            baseWeight *= 3.0;
        }

        int stackSize = item.getMaxStackSize();
        if (stackSize > 1) {
            baseWeight /= Math.sqrt(stackSize);
        }

        baseWeight = Math.max(0.01, baseWeight);
        return Math.round(baseWeight * 100.0) / 100.0;
    }

    private static void addTACZGunsToMap(Map<String, Map<String, Double>> categorizedWeights, Map<ResourceLocation, Double> weightsMap) {
        if (!taczLoaded) return;
        Set<Map.Entry<ResourceLocation, CommonGunIndex>> entries = com.tacz.guns.api.TimelessAPI.getAllCommonGunIndex();
        for (Map.Entry<ResourceLocation, CommonGunIndex> entry : entries) {
            ResourceLocation gunId = entry.getKey();
            if (gunId != null && !weightsMap.containsKey(gunId)) {
                double weight = estimateTACZGunWeight(gunId);
                weightsMap.put(gunId, weight);
                categorizedWeights
                        .computeIfAbsent(gunId.getNamespace(), k -> new LinkedHashMap<>())
                        .put(gunId.toString(), weight);
                EZWeight.LOGGER.info("Added TACZ gun '{}' with estimated weight {}", gunId, weight);
            }
        }
        // Añadir índices de attachments (no items) con peso estimado si faltan
        try {
            Set<ResourceLocation> attachmentIndexIds = fetchTimelessIndexKeys("getAllCommonAttachmentIndex", "getAllAttachmentIndex");
            for (ResourceLocation attId : attachmentIndexIds) {
                if (attId != null && !weightsMap.containsKey(attId)) {
                    double weight = 0.8;
                    weightsMap.put(attId, weight);
                    categorizedWeights
                            .computeIfAbsent(attId.getNamespace(), k -> new LinkedHashMap<>())
                            .put(attId.toString(), weight);
                    EZWeight.LOGGER.info("Added TACZ attachment index '{}' with estimated weight {}", attId, weight);
                }
            }
        } catch (Exception e) {
            EZWeight.LOGGER.warn("Failed to add TACZ attachment indexes to weights", e);
        }
        // Añadir índices de munición (no items) con peso estimado si faltan
        try {
            Set<ResourceLocation> ammoIndexIds = fetchTimelessIndexKeys("getAllCommonAmmoIndex", "getAllAmmoIndex");
            for (ResourceLocation ammoId : ammoIndexIds) {
                if (ammoId != null && !weightsMap.containsKey(ammoId)) {
                    double weight = 0.2;
                    weightsMap.put(ammoId, weight);
                    categorizedWeights
                            .computeIfAbsent(ammoId.getNamespace(), k -> new LinkedHashMap<>())
                            .put(ammoId.toString(), weight);
                    EZWeight.LOGGER.info("Added TACZ ammo index '{}' with estimated weight {}", ammoId, weight);
                }
            }
        } catch (Exception e) {
            EZWeight.LOGGER.warn("Failed to add TACZ ammo indexes to weights", e);
        }
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof AttachmentItem) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if (id != null && !weightsMap.containsKey(id)) {
                    double weight = estimateWeight(item);
                    weightsMap.put(id, weight);
                    categorizedWeights
                            .computeIfAbsent(id.getNamespace(), k -> new LinkedHashMap<>())
                            .put(id.toString(), weight);
                    EZWeight.LOGGER.info("Added TACZ attachment '{}' with estimated weight {}", id, weight);
                }
            }
        }
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof AmmoItem) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if (id != null && !weightsMap.containsKey(id)) {
                    double weight = estimateWeight(item);
                    weightsMap.put(id, weight);
                    categorizedWeights
                            .computeIfAbsent(id.getNamespace(), k -> new LinkedHashMap<>())
                            .put(id.toString(), weight);
                    EZWeight.LOGGER.info("Added TACZ ammo '{}' with estimated weight {}", id, weight);
                }
            }
        }
    }

    private static double estimateTACZGunWeight(ResourceLocation gunId) {
        String name = gunId.getPath().toLowerCase();
        if (name.contains("pistol")) return 2.0;
        if (name.contains("sniper")) return 7.0;
        if (name.contains("shotgun")) return 6.5;
        if (name.contains("smg")) return 4.0;
        if (name.contains("rifle")) return 5.5;
        return 5.0;
    }

    private static boolean addMissingItemsAndTACZGuns(Map<String, Map<String, Double>> categorizedMap) {
        boolean updated = false;
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null && !ITEM_WEIGHTS.containsKey(id)) {
                double weight = estimateWeight(item);
                ITEM_WEIGHTS.put(id, weight);
                categorizedMap
                        .computeIfAbsent(id.getNamespace(), k -> new LinkedHashMap<>())
                        .put(id.toString(), weight);
                updated = true;
                EZWeight.LOGGER.info("Added new item '{}' with estimated weight {}", id, weight);
            }
        }
        if (taczLoaded) {
            int oldSize = ITEM_WEIGHTS.size();
            addTACZGunsToMap(categorizedMap, ITEM_WEIGHTS);
            updated |= ITEM_WEIGHTS.size() != oldSize;
        }
        return updated;
    }

    public static ResourceLocation getEffectiveId(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);

        if (taczLoaded) {
            CompoundTag tag = stack.getTag();
            if (item instanceof AbstractGunItem) {
                if (tag != null && tag.contains("GunId", Tag.TAG_STRING)) {
                    try {
                        return new ResourceLocation(tag.getString("GunId"));
                    } catch (Exception e) {
                        EZWeight.LOGGER.warn("Invalid GunId in item: {}", tag.getString("GunId"));
                    }
                }
            } else if (item instanceof AttachmentItem) {
                if (tag != null && tag.contains("AttachmentId", Tag.TAG_STRING)) {
                    try {
                        return new ResourceLocation(tag.getString("AttachmentId"));
                    } catch (Exception e) {
                        EZWeight.LOGGER.warn("Invalid AttachmentId in item: {}", tag.getString("AttachmentId"));
                    }
                }
            } else if (item instanceof AmmoItem) {
                if (tag != null && tag.contains("AmmoId", Tag.TAG_STRING)) {
                    try {
                        return new ResourceLocation(tag.getString("AmmoId"));
                    } catch (Exception e) {
                        EZWeight.LOGGER.warn("Invalid AmmoId in item: {}", tag.getString("AmmoId"));
                    }
                }
            }
        }

        return itemId;
    }

    public static ItemStack createGunItemStack(ResourceLocation gunId) {
        if (!taczLoaded) return ItemStack.EMPTY;

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof AbstractGunItem) {
                ItemStack stack = new ItemStack(item);
                CompoundTag tag = stack.getOrCreateTag();
                tag.putString("GunId", gunId.toString());
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack createAttachmentItemStack(ResourceLocation attachmentId) {
        if (!taczLoaded) return ItemStack.EMPTY;

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof AttachmentItem) {
                ItemStack stack = new ItemStack(item);
                CompoundTag tag = stack.getOrCreateTag();
                tag.putString("AttachmentId", attachmentId.toString());
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack createAmmoItemStack(ResourceLocation ammoId) {
        if (!taczLoaded) return ItemStack.EMPTY;

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof AmmoItem) {
                ItemStack stack = new ItemStack(item);
                CompoundTag tag = stack.getOrCreateTag();
                tag.putString("AmmoId", ammoId.toString());
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static Set<ResourceLocation> fetchTimelessIndexKeys(String... methodNames) {
        Set<ResourceLocation> keys = new HashSet<>();
        if (!taczLoaded) return keys;
        try {
            Class<?> api = Class.forName("com.tacz.guns.api.TimelessAPI");
            for (String name : methodNames) {
                try {
                    java.lang.reflect.Method m = api.getMethod(name);
                    Object res = m.invoke(null);
                    if (res instanceof java.util.Set) {
                        for (Object e : ((java.util.Set<?>) res)) {
                            if (e instanceof java.util.Map.Entry) {
                                Object key = ((java.util.Map.Entry<?, ?>) e).getKey();
                                if (key instanceof ResourceLocation) {
                                    keys.add((ResourceLocation) key);
                                }
                            }
                        }
                    }
                } catch (NoSuchMethodException ignored) {
                }
            }
        } catch (Exception e) {
            EZWeight.LOGGER.warn("Failed to reflect TACZ index methods for attachments/ammo", e);
        }
        return keys;
    }

    public static List<ItemStackWithWeight> getItemsForNamespace(String namespace) {
        List<ItemStackWithWeight> result = new ArrayList<>();

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
            if (itemId != null && itemId.getNamespace().equals(namespace)) {
                if (taczLoaded && (item instanceof AbstractGunItem || item instanceof AttachmentItem || item instanceof AmmoItem)) {
                    continue;
                }
                ItemStack stack = new ItemStack(item);
                double weight = ITEM_WEIGHTS.getOrDefault(itemId, 1.0);
                result.add(new ItemStackWithWeight(stack, weight));
            }
        }

        if (taczLoaded) {
            Set<Map.Entry<ResourceLocation, CommonGunIndex>> gunEntries = com.tacz.guns.api.TimelessAPI.getAllCommonGunIndex();
            for (Map.Entry<ResourceLocation, CommonGunIndex> entry : gunEntries) {
                ResourceLocation gunId = entry.getKey();
                if (gunId != null && gunId.getNamespace().equals(namespace)) {
                    ItemStack gunStack = createGunItemStack(gunId);
                    if (!gunStack.isEmpty()) {
                        double weight = ITEM_WEIGHTS.getOrDefault(gunId, 5.0);
                        result.add(new ItemStackWithWeight(gunStack, weight));
                    }
                }
            }

            // Añadir attachments por índice (si la API está disponible)
            Set<ResourceLocation> attachmentKeys = fetchTimelessIndexKeys("getAllCommonAttachmentIndex", "getAllAttachmentIndex");
            for (ResourceLocation attachmentId : attachmentKeys) {
                if (attachmentId != null && attachmentId.getNamespace().equals(namespace)) {
                    ItemStack attStack = createAttachmentItemStack(attachmentId);
                    if (!attStack.isEmpty()) {
                        double weight = ITEM_WEIGHTS.getOrDefault(attachmentId, 0.8);
                        result.add(new ItemStackWithWeight(attStack, weight));
                    }
                }
            }
            if (attachmentKeys.isEmpty()) {
                for (Item item : ForgeRegistries.ITEMS.getValues()) {
                    if (item instanceof AttachmentItem) {
                        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                        if (id != null && id.getNamespace().equals(namespace)) {
                            ItemStack stack = new ItemStack(item);
                            double weight = ITEM_WEIGHTS.getOrDefault(id, 0.8);
                            result.add(new ItemStackWithWeight(stack, weight));
                        }
                    }
                }
            }

            // Añadir ammo por índice (si la API está disponible)
            Set<ResourceLocation> ammoKeys = fetchTimelessIndexKeys("getAllCommonAmmoIndex", "getAllAmmoIndex");
            for (ResourceLocation ammoId : ammoKeys) {
                if (ammoId != null && ammoId.getNamespace().equals(namespace)) {
                    ItemStack ammoStack = createAmmoItemStack(ammoId);
                    if (!ammoStack.isEmpty()) {
                        double weight = ITEM_WEIGHTS.getOrDefault(ammoId, 0.2);
                        result.add(new ItemStackWithWeight(ammoStack, weight));
                    }
                }
            }
            if (ammoKeys.isEmpty()) {
                for (Item item : ForgeRegistries.ITEMS.getValues()) {
                    if (item instanceof AmmoItem) {
                        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                        if (id != null && id.getNamespace().equals(namespace)) {
                            ItemStack stack = new ItemStack(item);
                            double weight = ITEM_WEIGHTS.getOrDefault(id, 0.2);
                            result.add(new ItemStackWithWeight(stack, weight));
                        }
                    }
                }
            }
        }

        return result;
    }

    public static double getWeight(ItemStack stack) {
        // Solo armas TACZ: sumar dinámicamente attachments y munición instalados
        if (taczLoaded && stack.getItem() instanceof AbstractGunItem) {
            return GunIdUtils.calculateTotalWeight(stack);
        }
        // Resto de ítems: comportamiento previo por id efectivo
        ResourceLocation effectiveId = getEffectiveId(stack);
        return ITEM_WEIGHTS.getOrDefault(effectiveId, 1.0);
    }

    public static Map<ResourceLocation, Double> getAllWeights() {
        return Collections.unmodifiableMap(ITEM_WEIGHTS);
    }

    public static void setWeight(ResourceLocation id, double weight) {
        ITEM_WEIGHTS.put(id, weight);
    }

    public static void setTACZWeight(ResourceLocation id, double weight) {
        ITEM_WEIGHTS.put(id, weight);
        EZWeight.LOGGER.info("TACZ Item weight updated: {} = {}", id, weight);
    }

    public static void setTACZWeight(ItemStack stack, double weight) {
        ResourceLocation effectiveId = getEffectiveId(stack);
        setTACZWeight(effectiveId, weight);
    }

    public static File getConfigFile() {
        return configFile;
    }

    public static void updateMissingItems() {
        if (configFile == null || !configFile.exists()) {
            EZWeight.LOGGER.warn("Config file not found for updating items.");
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<String, Map<String, Double>>>() {}.getType();
            Map<String, Map<String, Double>> categorizedMap = GSON.fromJson(reader, type);

            boolean updated = false;
            updated |= addMissingItemsAndTACZGuns(categorizedMap != null ? categorizedMap : new HashMap<>());

            if (updated) {
                try (FileWriter writer = new FileWriter(configFile)) {
                    GSON.toJson(categorizedMap, writer);
                    EZWeight.LOGGER.info("Updated item weights file with new items.");
                }
            }
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to update missing item weights!", e);
        }
    }

    public static void reloadFromFile() {
        if (configFile != null && configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Type type = new TypeToken<Map<String, Map<String, Double>>>() {}.getType();
                Map<String, Map<String, Double>> categorizedMap = GSON.fromJson(reader, type);

                ITEM_WEIGHTS.clear();

                if (categorizedMap != null) {
                    for (Map.Entry<String, Map<String, Double>> categoryEntry : categorizedMap.entrySet()) {
                        for (Map.Entry<String, Double> entry : categoryEntry.getValue().entrySet()) {
                            try {
                                ResourceLocation id = new ResourceLocation(entry.getKey());
                                double weight = entry.getValue();
                                ITEM_WEIGHTS.put(id, weight);
                            } catch (Exception e) {
                                EZWeight.LOGGER.warn("Invalid entry in item weights config: {}", entry.getKey(), e);
                            }
                        }
                    }
                }

                updateMissingItems();
                EZWeight.LOGGER.info("Reloaded {} item weights from config.", ITEM_WEIGHTS.size());
            } catch (Exception e) {
                EZWeight.LOGGER.error("Failed to reload item weights from file.", e);
            }
        } else {
            EZWeight.LOGGER.warn("No config file found to reload item weights.");
        }
    }
}