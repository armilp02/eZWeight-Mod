package com.armilp.ezweight.events;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.player.DynamicMaxWeightCalculator;
import com.armilp.ezweight.player.PlayerWeightHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = EZWeight.MODID)
public class ItemPickupEventHandler {

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!WeightCommands.isWeightEnabledFor(player)) return;

        double currentWeight = PlayerWeightHandler.getTotalWeight(player);
        double maxWeight = DynamicMaxWeightCalculator.calculate(player);

        ItemEntity itemEntity = event.getItemEntity();
        ItemStack stack = itemEntity.getItem();

        if (stack.isEmpty()) return;

        double itemWeight = ItemWeightRegistry.getWeight(stack);
        if (itemWeight <= 0) return;

        int maxPickupCount = (int) Math.floor((maxWeight - currentWeight) / itemWeight);

        if (maxPickupCount <= 0) {
            event.setCanPickup(TriState.FALSE);

            player.displayClientMessage(
                    Component.translatable(
                            "message.ezweight.pickup_blocked",
                            String.format("%.1f", currentWeight),
                            String.format("%.1f", maxWeight)
                    ),
                    true
            );
            return;
        }

        int availableCount = stack.getCount();

        if (maxPickupCount < availableCount) {
            event.setCanPickup(TriState.FALSE);

            ItemStack partial = stack.copy();
            partial.setCount(maxPickupCount);

            boolean added = player.getInventory().add(partial);

            if (added) {
                stack.shrink(maxPickupCount);
                itemEntity.setPickUpDelay(10);
            }
        }
    }
}