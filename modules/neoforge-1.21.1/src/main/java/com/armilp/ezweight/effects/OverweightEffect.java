package com.armilp.ezweight.effects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class OverweightEffect extends MobEffect {

    private final double speedMultiplier;

    public OverweightEffect(double speedMultiplier) {
        super(MobEffectCategory.HARMFUL, 0x8B4513);
        this.speedMultiplier = speedMultiplier;

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath("ezweight", "overweight_speed"),
                speedMultiplier,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity p_19467_, int p_19468_) {
        return super.applyEffectTick(p_19467_, p_19468_);
    }

}