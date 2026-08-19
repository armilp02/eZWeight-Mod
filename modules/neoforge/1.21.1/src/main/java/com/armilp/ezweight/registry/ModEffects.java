package com.armilp.ezweight.registry;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.effects.OverweightEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, EZWeight.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> LIGHT_ENCUMBERED = MOB_EFFECTS.register(
            "light_encumbered",
            () -> new OverweightEffect(-0.15)
    );

    public static final DeferredHolder<MobEffect, MobEffect> ENCUMBERED = MOB_EFFECTS.register(
            "encumbered",
            () -> new OverweightEffect(-0.25)
    );

    public static final DeferredHolder<MobEffect, MobEffect> HEAVILY_ENCUMBERED = MOB_EFFECTS.register(
            "heavily_encumbered",
            () -> new OverweightEffect(-0.40)
    );

    public static final DeferredHolder<MobEffect, MobEffect> OVERBURDENED = MOB_EFFECTS.register(
            "overburdened",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x654321) {}
                    .addAttributeModifier(
                            Attributes.MOVEMENT_SPEED,
                            ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "effect.overburdened.speed"),
                            -0.60,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
                    .addAttributeModifier(
                            Attributes.ATTACK_SPEED,
                            ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "effect.overburdened.attack_speed"),
                            -0.30,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
    );

    public static final DeferredHolder<MobEffect, MobEffect> CRUSHED = MOB_EFFECTS.register(
            "crushed",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4A0000) {}
                    .addAttributeModifier(
                            Attributes.MOVEMENT_SPEED,
                            ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "effect.crushed.speed"),
                            -0.80,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
                    .addAttributeModifier(
                            Attributes.ATTACK_DAMAGE,
                            ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "effect.crushed.attack_damage"),
                            -0.50,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
                    .addAttributeModifier(
                            Attributes.ATTACK_SPEED,
                            ResourceLocation.fromNamespaceAndPath(EZWeight.MODID, "effect.crushed.attack_speed"),
                            -0.50,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    )
    );

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
