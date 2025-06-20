package com.armilp.ezweight.levels;

import net.minecraft.world.effect.MobEffectInstance;
import java.util.List;

public record WeightLevel(String name, double minWeight, double maxWeight, List<MobEffectInstance> effects) {
    public boolean isInRange(double weight) {
        return weight >= minWeight && weight <= maxWeight;
    }
}

