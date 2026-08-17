package com.armilp.ezweight.registry;

import com.armilp.ezweight.EZWeight;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, EZWeight.MODID);

    public static final RegistryObject<Attribute> WEIGHT = ATTRIBUTES.register("weight", () -> new RangedAttribute("attribute.ezweight.weight", 100.0, 0.0, Double.MAX_VALUE).setSyncable(true));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        if (!event.has(EntityType.PLAYER, WEIGHT.get())) {
            event.add(EntityType.PLAYER, WEIGHT.get());
            EZWeight.LOGGER.info("Weight attribute registered for players!");
        }
    }
}


