package com.armilp.ezweight.player;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.registry.ModAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DynamicMaxWeightCalculator {

    public static double calculate(Player player) {
        if (player == null) {
            return WeightConfig.COMMON.MAX_WEIGHT.get();
        }

        try {
            var attribute = player.getAttribute(ModAttributes.WEIGHT);

            // If attribute exists and has a base value, use it
            if (attribute != null && attribute.getBaseValue() > 0) {
                return attribute.getValue();
            }
        } catch (Exception e) {
            EZWeight.LOGGER.debug("Weight attribute not yet available, using config values");
        }

        if (!WeightConfig.COMMON.USE_DYNAMIC_WEIGHT.get()) {
            return WeightConfig.COMMON.MAX_WEIGHT.get();
        }

        double baseMaxWeight = WeightConfig.COMMON.MAX_WEIGHT.get();
        double baseWeight = WeightConfig.COMMON.BASE_WEIGHT.get();

        double weightFromFood = baseWeight;
        if (WeightConfig.COMMON.DYN_FOOD_ENABLED.get()) {
            int foodLevel = player.getFoodData().getFoodLevel();
            double foodFactor = (foodLevel / 20.0) * WeightConfig.COMMON.DYN_FOOD_INFLUENCE_MULTIPLIER.get();
            if (foodFactor < 0.0) foodFactor = 0.0;
            weightFromFood = baseWeight + (baseMaxWeight - baseWeight) * Math.min(foodFactor, 1.0);
        }

        double strengthBonus = 0.0;
        if (WeightConfig.COMMON.DYN_STRENGTH_ENABLED.get()) {
            boolean hasStrength = player.hasEffect(MobEffects.DAMAGE_BOOST);
            strengthBonus = hasStrength ? WeightConfig.COMMON.DYN_STRENGTH_BONUS.get() : 0.0;
        }

        double crouchBonus = 0.0;
        if (WeightConfig.COMMON.DYN_CROUCH_ENABLED.get()) {
            crouchBonus = player.isCrouching() ? WeightConfig.COMMON.DYN_CROUCH_BONUS.get() : 0.0;
        }

        double armorWeightPenalty = 0.0;
        if (WeightConfig.COMMON.DYN_ARMOR_PENALTY_ENABLED.get()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    ItemStack stack = player.getItemBySlot(slot);
                    if (!stack.isEmpty()) {
                        armorWeightPenalty += WeightConfig.COMMON.DYN_ARMOR_PENALTY_PER_PIECE.get();
                    }
                }
            }
        }

        double dynamicWeight = weightFromFood + strengthBonus + crouchBonus - armorWeightPenalty;
        double finalWeight = Math.max(baseWeight, Math.min(dynamicWeight, baseMaxWeight));

        // Update the attribute if available
        try {
            var attribute = player.getAttribute(ModAttributes.WEIGHT);
            if (attribute != null) {
                attribute.setBaseValue(finalWeight);
            }
        } catch (Exception e) {
            // Attribute not yet available, skip update
        }

        return finalWeight;
    }
}