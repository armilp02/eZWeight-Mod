package com.armilp.ezweight.registry;

import com.armilp.ezweight.EZWeight;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, EZWeight.MODID);

    public static final DeferredHolder<Attribute, Attribute> WEIGHT = ATTRIBUTES.register("weight",
            () -> new RangedAttribute("attribute.ezweight.weight", 100.0, 0.0, Double.MAX_VALUE).setSyncable(true));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        if (!event.has(EntityType.PLAYER, WEIGHT)) {
            event.add(EntityType.PLAYER, WEIGHT);
            EZWeight.LOGGER.info("Weight attribute registered for players!");
        }
    }
}
