package com.armilp.ezweight.effects;

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
                "7107DE5E-7CE8-4030-940E-514C1F160890",
                speedMultiplier,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}