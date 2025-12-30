package com.armilp.ezweight.registry;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.effects.OverweightEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = 
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EZWeight.MODID);

    public static final RegistryObject<MobEffect> LIGHT_ENCUMBERED = MOB_EFFECTS.register(
        "light_encumbered",
        () -> new OverweightEffect(-0.15)
    );

    public static final RegistryObject<MobEffect> ENCUMBERED = MOB_EFFECTS.register(
        "encumbered",
        () -> new OverweightEffect(-0.25)
    );

    public static final RegistryObject<MobEffect> HEAVILY_ENCUMBERED = MOB_EFFECTS.register(
        "heavily_encumbered",
        () -> new OverweightEffect(-0.40)
    );

    public static final RegistryObject<MobEffect> OVERBURDENED = MOB_EFFECTS.register(
        "overburdened",
        () -> new MobEffect(MobEffectCategory.HARMFUL, 0x654321) {
            {
                this.addAttributeModifier(
                    Attributes.MOVEMENT_SPEED,
                    "8107DE5E-7CE8-4030-940E-514C1F160891",
                    -0.60,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                this.addAttributeModifier(
                    Attributes.ATTACK_SPEED,
                    "8107DE5E-7CE8-4030-940E-514C1F160892",
                    -0.30,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
                );
            }
        }
    );

    public static final RegistryObject<MobEffect> CRUSHED = MOB_EFFECTS.register(
        "crushed",
        () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4A0000) {
            {
                this.addAttributeModifier(
                    Attributes.MOVEMENT_SPEED,
                    "9107DE5E-7CE8-4030-940E-514C1F160893",
                    -0.80,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                this.addAttributeModifier(
                    Attributes.ATTACK_DAMAGE,
                    "9107DE5E-7CE8-4030-940E-514C1F160894",
                    -0.50,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                this.addAttributeModifier(
                    Attributes.ATTACK_SPEED,
                    "9107DE5E-7CE8-4030-940E-514C1F160895",
                    -0.50,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
                );
            }
        }
    );
    
    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}