package com.armilp.ezweight.player;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.registry.ModAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMaxWeightOverride {

    private static final Map<UUID, Double> legacyOverrides = new HashMap<>();

    public static void set(Player player, double maxWeight) {
        var attribute = player.getAttribute(ModAttributes.WEIGHT.get());
        if (attribute != null) {
            attribute.setBaseValue(maxWeight);
        }
        legacyOverrides.remove(player.getUUID());
    }

    public static void set(UUID uuid, double maxWeight) {
        Player player = getPlayer(uuid);
        if (player != null) {
            set(player, maxWeight);
        } else {
            legacyOverrides.put(uuid, maxWeight);
        }
    }

    public static void clear(Player player) {
        var attribute = player.getAttribute(ModAttributes.WEIGHT.get());
        if (attribute != null) {
            attribute.setBaseValue(WeightConfig.COMMON.MAX_WEIGHT.get());
        }
        legacyOverrides.remove(player.getUUID());
    }

    public static boolean has(UUID uuid) {
        Player player = getPlayer(uuid);
        if (player != null) {
            var attribute = player.getAttribute(ModAttributes.WEIGHT.get());
            if (attribute != null) {
                double configDefault = WeightConfig.COMMON.MAX_WEIGHT.get();
                return Math.abs(attribute.getBaseValue() - configDefault) > 0.001;
            }
        }
        return legacyOverrides.containsKey(uuid);
    }

    public static double get(UUID uuid) {
        Player player = getPlayer(uuid);
        if (player != null) {
            var attribute = player.getAttribute(ModAttributes.WEIGHT.get());
            if (attribute != null) {
                return attribute.getValue();
            }
        }
        return legacyOverrides.getOrDefault(uuid, -1.0);
    }

    // FIXED: Get player from server
    private static Player getPlayer(UUID uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getPlayerList().getPlayer(uuid);
        }
        return null;
    }
}