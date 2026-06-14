package com.armilp.ezweight.data;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.client.gui.ItemStackWithWeight;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

public class ItemWeightRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, Double> ITEM_WEIGHTS = new HashMap<>();
    private static final Map<ResourceLocation, Double> GUN_AMMO_WEIGHTS = new HashMap<>();
    private static final Map<ResourceLocation, Double> BACKPACK_WEIGHT_REDUCTIONS = new HashMap<>();
    private static final Map<ResourceLocation, Double> BACKPACK_MAX_WEIGHTS = new HashMap<>();
    private static final String FILE_NAME = "items.json";
    private static final String DEFAULT_ASSET_PATH = "/assets/ezweight/items.json";
    private static File configFile;

    public static void init(Path configDir) {
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
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> categorizedMap = GSON.fromJson(reader, type);

            ITEM_WEIGHTS.clear();
            GUN_AMMO_WEIGHTS.clear();
            BACKPACK_WEIGHT_REDUCTIONS.clear();
            BACKPACK_MAX_WEIGHTS.clear();
            boolean updated = false;

            if (categorizedMap != null) {
                for (Map.Entry<String, Map<String, Object>> categoryEntry : categorizedMap.entrySet()) {
                    for (Map.Entry<String, Object> entry : categoryEntry.getValue().entrySet()) {
                        try {
                            ResourceLocation id = ResourceLocation.parse(entry.getKey());
                            Object value = entry.getValue();

                            if (value instanceof Map<?, ?> itemData) {
                                if (itemData.containsKey("weight")) {
                                    Object weightObj = itemData.get("weight");
                                    double weight = (weightObj instanceof Number n) ? n.doubleValue() : 1.0;
                                    ITEM_WEIGHTS.put(id, weight);
                                }
                                if (itemData.containsKey("ammo_weight")) {
                                    Object ammoWeightObj = itemData.get("ammo_weight");
                                    double ammoWeight = (ammoWeightObj instanceof Number n) ? n.doubleValue() : 0.02;
                                    GUN_AMMO_WEIGHTS.put(id, ammoWeight);
                                }
                                if (itemData.containsKey("backpack_weight_reduction")) {
                                    Object reductionObj = itemData.get("backpack_weight_reduction");
                                    double reduction = (reductionObj instanceof Number n) ? n.doubleValue() : 0.20;
                                    BACKPACK_WEIGHT_REDUCTIONS.put(id, reduction);
                                }
                                if (itemData.containsKey("backpack_max_weight")) {
                                    Object maxWeightObj = itemData.get("backpack_max_weight");
                                    double maxWeight = (maxWeightObj instanceof Number n) ? n.doubleValue() : 250.0;
                                    BACKPACK_MAX_WEIGHTS.put(id, maxWeight);
                                }
                            } else if (value instanceof Number n) {
                                ITEM_WEIGHTS.put(id, n.doubleValue());
                            }
                        } catch (Exception ex) {
                            EZWeight.LOGGER.warn("Invalid item ID in config: {}", entry.getKey());
                        }
                    }
                }
            }

            updated |= addMissingItems(categorizedMap != null ? categorizedMap : new HashMap<>());

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
            Map<String, Map<String, Object>> categorizedMap = new LinkedHashMap<>();

            for (Map.Entry<ResourceLocation, Double> entry : ITEM_WEIGHTS.entrySet()) {
                ResourceLocation id = entry.getKey();
                String namespace = id.getNamespace();

                Double ammoWeight = GUN_AMMO_WEIGHTS.get(id);
                Double backpackReduction = BACKPACK_WEIGHT_REDUCTIONS.get(id);
                Double backpackMaxWeight = BACKPACK_MAX_WEIGHTS.get(id);

                if (ammoWeight != null || backpackReduction != null || backpackMaxWeight != null) {
                    Map<String, Object> itemData = new LinkedHashMap<>();
                    itemData.put("weight", entry.getValue());
                    if (ammoWeight != null) itemData.put("ammo_weight", ammoWeight);
                    if (backpackReduction != null) itemData.put("backpack_weight_reduction", backpackReduction);
                    if (backpackMaxWeight != null) itemData.put("backpack_max_weight", backpackMaxWeight);
                    categorizedMap.computeIfAbsent(namespace, k -> new LinkedHashMap<>())
                            .put(id.toString(), itemData);
                } else {
                    categorizedMap.computeIfAbsent(namespace, k -> new LinkedHashMap<>())
                            .put(id.toString(), entry.getValue());
                }
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
        Map<String, Map<String, Object>> categorizedWeights = new HashMap<>();
        ITEM_WEIGHTS.clear();

        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
                double weight = estimateWeight(item);
                ITEM_WEIGHTS.put(id, weight);
                categorizedWeights.computeIfAbsent(id.getNamespace(), k -> new LinkedHashMap<>())
                        .put(id.toString(), weight);
            }
        }

        try {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(categorizedWeights, writer);
                EZWeight.LOGGER.info("Generated default item weights with {} categories.", categorizedWeights.size());
            }
        } catch (Exception e) {
            EZWeight.LOGGER.error("Failed to write default item weights!", e);
        }
    }

    private static double estimateWeight(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
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

        if (itemName.contains("wood")) baseWeight *= 0.8;
        else if (itemName.contains("stone")) baseWeight *= 1.2;
        else if (itemName.contains("iron")) baseWeight *= 1.5;
        else if (itemName.contains("gold")) baseWeight *= 2.0;
        else if (itemName.contains("diamond")) baseWeight *= 2.5;
        else if (itemName.contains("netherite")) baseWeight *= 3.0;

        int stackSize = item.getDefaultMaxStackSize();
        if (stackSize > 1) baseWeight /= Math.sqrt(stackSize);

        baseWeight = Math.max(0.01, baseWeight);
        return Math.round(baseWeight * 100.0) / 100.0;
    }

    private static boolean addMissingItems(Map<String, Map<String, Object>> categorizedMap) {
        boolean updated = false;
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null && !ITEM_WEIGHTS.containsKey(id)) {
                double weight = estimateWeight(item);
                ITEM_WEIGHTS.put(id, weight);
                categorizedMap.computeIfAbsent(id.getNamespace(), k -> new LinkedHashMap<>())
                        .put(id.toString(), weight);
                updated = true;
                EZWeight.LOGGER.info("Added new item '{}' with estimated weight {}", id, weight);
            }
        }
        return updated;
    }

    public static ResourceLocation getEffectiveId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public static List<ItemStackWithWeight> getItemsForNamespace(String namespace) {
        List<ItemStackWithWeight> result = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if (itemId != null && itemId.getNamespace().equals(namespace)) {
                ItemStack stack = new ItemStack(item);
                double weight = ITEM_WEIGHTS.getOrDefault(itemId, 1.0);
                result.add(new ItemStackWithWeight(stack, weight));
            }
        }
        return result;
    }

    public static double getWeight(ItemStack stack) {
        ResourceLocation effectiveId = getEffectiveId(stack);
        return ITEM_WEIGHTS.getOrDefault(effectiveId, 1.0);
    }

    public static double getStackTotalWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;
        ResourceLocation effectiveId = getEffectiveId(stack);
        double weight = ITEM_WEIGHTS.getOrDefault(effectiveId, 1.0);
        return weight * stack.getCount();
    }

    public static Map<ResourceLocation, Double> getAllWeights() {
        return Collections.unmodifiableMap(ITEM_WEIGHTS);
    }

    public static void setWeight(ResourceLocation id, double weight) {
        ITEM_WEIGHTS.put(id, weight);
    }

    public static Double getGunAmmoWeight(ResourceLocation gunId) {
        return GUN_AMMO_WEIGHTS.get(gunId);
    }

    public static void setGunAmmoWeight(ResourceLocation gunId, double ammoWeight) {
        GUN_AMMO_WEIGHTS.put(gunId, ammoWeight);
        EZWeight.LOGGER.info("Gun ammo weight set: {} = {}", gunId, ammoWeight);
    }

    public static Double getBackpackWeightReduction(ResourceLocation backpackId) {
        return BACKPACK_WEIGHT_REDUCTIONS.get(backpackId);
    }

    public static void setBackpackWeightReduction(ResourceLocation backpackId, double reduction) {
        BACKPACK_WEIGHT_REDUCTIONS.put(backpackId, reduction);
        EZWeight.LOGGER.info("Backpack weight reduction set: {} = {}%", backpackId, reduction * 100);
    }

    public static Double getBackpackMaxWeight(ResourceLocation backpackId) {
        return BACKPACK_MAX_WEIGHTS.get(backpackId);
    }

    public static void setBackpackMaxWeight(ResourceLocation backpackId, double maxWeight) {
        BACKPACK_MAX_WEIGHTS.put(backpackId, maxWeight);
        EZWeight.LOGGER.info("Backpack max weight set: {} = {} kg", backpackId, maxWeight);
    }

    public static Map<ResourceLocation, Double> getAllBackpackReductions() {
        return Collections.unmodifiableMap(BACKPACK_WEIGHT_REDUCTIONS);
    }

    public static Map<ResourceLocation, Double> getAllBackpackMaxWeights() {
        return Collections.unmodifiableMap(BACKPACK_MAX_WEIGHTS);
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
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> categorizedMap = GSON.fromJson(reader, type);
            boolean updated = addMissingItems(categorizedMap != null ? categorizedMap : new HashMap<>());
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
        if (configFile == null || !configFile.exists()) {
            EZWeight.LOGGER.warn("No config file found to reload item weights.");
            return;
        }
        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> categorizedMap = GSON.fromJson(reader, type);

            ITEM_WEIGHTS.clear();
            GUN_AMMO_WEIGHTS.clear();
            BACKPACK_WEIGHT_REDUCTIONS.clear();
            BACKPACK_MAX_WEIGHTS.clear();

            if (categorizedMap != null) {
                for (Map.Entry<String, Map<String, Object>> categoryEntry : categorizedMap.entrySet()) {
                    for (Map.Entry<String, Object> entry : categoryEntry.getValue().entrySet()) {
                        try {
                            ResourceLocation id = ResourceLocation.parse(entry.getKey());
                            Object value = entry.getValue();
                            if (value instanceof Map<?, ?> itemData) {
                                if (itemData.containsKey("weight")) {
                                    Object w = itemData.get("weight");
                                    ITEM_WEIGHTS.put(id, (w instanceof Number n) ? n.doubleValue() : 1.0);
                                }
                                if (itemData.containsKey("ammo_weight")) {
                                    Object w = itemData.get("ammo_weight");
                                    GUN_AMMO_WEIGHTS.put(id, (w instanceof Number n) ? n.doubleValue() : 0.02);
                                }
                                if (itemData.containsKey("backpack_weight_reduction")) {
                                    Object w = itemData.get("backpack_weight_reduction");
                                    BACKPACK_WEIGHT_REDUCTIONS.put(id, (w instanceof Number n) ? n.doubleValue() : 0.20);
                                }
                                if (itemData.containsKey("backpack_max_weight")) {
                                    Object w = itemData.get("backpack_max_weight");
                                    BACKPACK_MAX_WEIGHTS.put(id, (w instanceof Number n) ? n.doubleValue() : 250.0);
                                }
                            } else if (value instanceof Number n) {
                                ITEM_WEIGHTS.put(id, n.doubleValue());
                            }
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
    }
}