package com.armilp.ezweight.events;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.player.PlayerWeightHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(modid = EZWeight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ContainerEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        ServerPlayer player = (ServerPlayer) event.player;
        if (!WeightCommands.isWeightEnabledFor(player)) return;

        double totalWeight = PlayerWeightHandler.getTotalWeight(player);
        double maxWeight = WeightConfig.COMMON.MAX_WEIGHT.get();

        if (totalWeight > maxWeight) {
            player.displayClientMessage(
                    Component.translatable("message.ezweight.overburdened",
                            String.format("%.1f", totalWeight),
                            String.format("%.1f", maxWeight)
                    ),
                    true
            );

            dropExcessItems(player, totalWeight, maxWeight);
        }
    }

    private static void dropExcessItems(ServerPlayer player, double currentWeight, double maxWeight) {
        for (int i = player.getInventory().items.size() - 1; i >= 0 && currentWeight > maxWeight; i--) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.isEmpty()) continue;

            if (stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                IItemHandler handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
                if (handler != null) {
                    currentWeight = tryDropFromHandler(player, handler, currentWeight, maxWeight);
                }
            }

            while (currentWeight > maxWeight && stack.getCount() > 0) {
                ItemStack drop = stack.copy();
                drop.setCount(1);
                stack.shrink(1);
                player.drop(drop, true);
                currentWeight -= ItemWeightRegistry.getWeight(drop.getItem());
            }
        }
    }

    private static double tryDropFromHandler(ServerPlayer player, IItemHandler handler, double currentWeight, double maxWeight) {
        for (int slot = handler.getSlots() - 1; slot >= 0 && currentWeight > maxWeight; slot--) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            if (stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                IItemHandler nested = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
                if (nested != null) {
                    currentWeight = tryDropFromHandler(player, nested, currentWeight, maxWeight);
                    stack = handler.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                }
            }

            while (currentWeight > maxWeight && !stack.isEmpty()) {
                ItemStack extracted = handler.extractItem(slot, 1, false);
                if (!extracted.isEmpty()) {
                    player.drop(extracted, true);
                    currentWeight -= ItemWeightRegistry.getWeight(extracted.getItem());
                } else {
                    break;
                }
            }
        }
        return currentWeight;
    }



}
