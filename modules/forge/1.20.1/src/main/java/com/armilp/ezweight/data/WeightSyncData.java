package com.armilp.ezweight.data;

import com.armilp.ezweight.player.DynamicMaxWeightCalculator;
import com.armilp.ezweight.registry.ModAttributes;
import net.minecraft.world.entity.player.Player;

public class WeightSyncData {

    private static double cachedMaxWeight = 100.0;
    private static boolean needsUpdate = false;

    public static void updateDynamicMaxWeight(Player player) {
        double newMax = DynamicMaxWeightCalculator.calculate(player);
        if (Math.abs(newMax - cachedMaxWeight) > 0.001) {
            cachedMaxWeight = newMax;
            needsUpdate = true;

            // Also update the attribute if it's not already set
            var attribute = player.getAttribute(ModAttributes.WEIGHT.get());
            if (attribute != null && Math.abs(attribute.getValue() - newMax) > 0.001) {
                attribute.setBaseValue(newMax);
            }
        }
    }

    public static double getMaxWeight() {
        return cachedMaxWeight;
    }

    public static boolean consumeUpdatedFlag() {
        if (needsUpdate) {
            needsUpdate = false;
            return true;
        }
        return false;
    }

    public static void setMaxWeight(double weight) {
        cachedMaxWeight = weight;
    }
}