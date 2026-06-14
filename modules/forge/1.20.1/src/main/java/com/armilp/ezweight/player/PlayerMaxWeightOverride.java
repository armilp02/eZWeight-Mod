package com.armilp.ezweight.player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMaxWeightOverride {

    private static final Map<UUID, Double> overrides = new HashMap<>();

    public static void set(UUID uuid, double maxWeight) {
        overrides.put(uuid, maxWeight);
    }

    public static void clear(UUID uuid) {
        overrides.remove(uuid);
    }

    public static boolean has(UUID uuid) {
        return overrides.containsKey(uuid);
    }

    public static double get(UUID uuid) {
        return overrides.getOrDefault(uuid, -1.0);
    }
}