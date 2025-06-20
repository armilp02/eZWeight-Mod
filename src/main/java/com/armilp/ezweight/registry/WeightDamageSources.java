package com.armilp.ezweight.registry;

import com.armilp.ezweight.EZWeight;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.Holder;

public class WeightDamageSources {
    public static DamageSource overweight(RegistryAccess access) {
        Holder<DamageType> holder = access.registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(EZWeight.MODID, "overweight")));
        return new DamageSource(holder);
    }

}

