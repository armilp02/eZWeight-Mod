package com.armilp.ezweight.events;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.player.PlayerWeightHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EZWeight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemPickupEventHandler {

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        var player = event.getEntity();

        double currentWeight = PlayerWeightHandler.getTotalWeight(player);
        ItemStack stack = event.getItem().getItem();

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (!WeightCommands.isWeightEnabledFor(serverPlayer)) return;

        double itemWeight = ItemWeightRegistry.getWeight(stack.getItem()) * stack.getCount();
        double maxWeight = WeightConfig.COMMON.MAX_WEIGHT.get();

        if (currentWeight + itemWeight > maxWeight) {
            event.setCanceled(true);
            player.displayClientMessage(
                    Component.translatable("message.ezweight.pickup_blocked",
                            String.format("%.1f", currentWeight),
                            String.format("%.1f", maxWeight)
                    ),
                    true
            );
        }
    }
}
