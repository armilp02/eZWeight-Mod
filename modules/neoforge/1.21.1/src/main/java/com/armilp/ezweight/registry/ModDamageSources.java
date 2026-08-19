package com.armilp.ezweight.registry;

import com.armilp.ezweight.EZWeight;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public class ModDamageSources {

    public static final ResourceKey<DamageType> OVERWEIGHT = key("overweight");

    private static ResourceKey<DamageType> key (String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, EZWeight.id(name));
    }

    public static DamageSource overweight(RegistryAccess registryAccess)
    {
        return new DamageSource(getType(registryAccess, OVERWEIGHT));
    }

    private static Holder<DamageType> getType(RegistryAccess registryAccess, ResourceKey<DamageType> key){
        return registryAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }

}