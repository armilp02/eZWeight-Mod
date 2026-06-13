package com.armilp.ezweight.player;

import com.armilp.ezweight.config.WeightConfig;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DynamicMaxWeightCalculator {

    public static double calculate(Player player) {
        if (PlayerMaxWeightOverride.has(player.getUUID())) {
            return PlayerMaxWeightOverride.get(player.getUUID());
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

        double dynamicWeight = weightFromFood + strengthBonus + crouchBonus - /*armorWeightPenalty*/ 0.0;

        return Math.max(baseWeight, Math.min(dynamicWeight, baseMaxWeight));
    }
}