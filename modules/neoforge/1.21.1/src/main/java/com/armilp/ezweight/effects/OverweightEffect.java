package com.armilp.ezweight.effects;

import com.armilp.ezweight.EZWeight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

public class OverweightEffect extends MobEffect {

    private static final ResourceLocation OVERWEIGHT_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "effect.overweight.speed");

    public OverweightEffect(double speedMultiplier) {
        super(MobEffectCategory.HARMFUL, 0x8B4513);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                OVERWEIGHT_SPEED_MODIFIER_ID,
                speedMultiplier,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity p_19467_, int p_19468_) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
