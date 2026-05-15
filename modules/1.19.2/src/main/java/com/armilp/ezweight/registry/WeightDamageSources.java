package com.armilp.ezweight.registry;

import com.armilp.ezweight.EZWeight;
import net.minecraft.world.damagesource.DamageSource;

public class WeightDamageSources {
    public static final DamageSource OVERWEIGHT = new DamageSource(EZWeight.MODID + ".overweight").bypassArmor();

    public static DamageSource overweight() {
        return OVERWEIGHT;
    }
}