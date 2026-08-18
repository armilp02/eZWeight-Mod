package com.armilp.ezweight.events;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.player.DynamicMaxWeightCalculator;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import java.util.Iterator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(modid = EZWeight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ContainerEventHandler {

    private static final boolean TRAVELERS_LOADED = ModList.get().isLoaded(TravelersBackpack.MODID);
    private static final boolean BACKPACKED_LOADED = ModList.get().isLoaded("backpacked");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        ServerPlayer player = (ServerPlayer) event.player;
        if (!WeightCommands.isWeightEnabledFor(player)) return;

        double totalWeight = PlayerWeightHandler.getTotalWeight(player);
        double maxWeight = DynamicMaxWeightCalculator.calculate(player);

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
        currentWeight = dropItemsFromInventory(player, currentWeight, maxWeight);

        if (currentWeight > maxWeight) {
            currentWeight = dropItemsFromBackpacked(player, currentWeight, maxWeight);
        }

        if (currentWeight > maxWeight) {
        }
    }

    private static double dropItemsFromInventory(ServerPlayer player, double currentWeight, double maxWeight) {
        for (int i = player.getInventory().items.size() - 1; i >= 0 && currentWeight > maxWeight; i--) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.isEmpty()) continue;

            if (TRAVELERS_LOADED && stack.getItem() instanceof TravelersBackpackItem) {
                BackpackWrapper wrapper = new BackpackWrapper(stack, 1, player, player.level(), -1);
                IItemHandler backpackInventory = wrapper.getStorage();

                currentWeight = dropFromHandler(player, backpackInventory, currentWeight, maxWeight);

                if (currentWeight > maxWeight && !stack.isEmpty()) {
                    currentWeight = dropSingleItemFromStack(player, stack, currentWeight);
                }

                continue;
            }

            if (stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                IItemHandler handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
                if (handler != null) {
                    currentWeight = dropFromHandler(player, handler, currentWeight, maxWeight);
                }
            }

            currentWeight = dropSingleItemFromStack(player, stack, currentWeight);
        }

        return currentWeight;
    }

    private static double dropItemsFromBackpacked(ServerPlayer player, double currentWeight, double maxWeight) {
        if (!BACKPACKED_LOADED) return currentWeight;

        BackpackedInventoryAccess access = (BackpackedInventoryAccess) player;
        Iterator<BackpackInventory> inventories = access.backpacked$streamNonNullBackpackInventories().iterator();

        while (inventories.hasNext() && currentWeight > maxWeight) {
            BackpackInventory inventory = inventories.next();
            currentWeight = dropFromContainer(player, inventory, currentWeight, maxWeight);
        }

        return currentWeight;
    }

    private static double dropSingleItemFromStack(ServerPlayer player, ItemStack stack, double currentWeight) {
        double itemWeight = ItemWeightRegistry.getWeight(stack);

        while (currentWeight > DynamicMaxWeightCalculator.calculate(player) && stack.getCount() > 0) {
            ItemStack drop = stack.copy();
            drop.setCount(1);
            stack.shrink(1);
            player.drop(drop, true);
            currentWeight -= itemWeight;
        }

        return currentWeight;
    }

    private static double dropFromHandler(ServerPlayer player, IItemHandler handler, double currentWeight, double maxWeight) {
        for (int slot = handler.getSlots() - 1; slot >= 0 && currentWeight > maxWeight; slot--) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            if (stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                IItemHandler nested = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
                if (nested != null) {
                    currentWeight = dropFromHandler(player, nested, currentWeight, maxWeight);
                    stack = handler.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                }
            }

            while (currentWeight > maxWeight && !stack.isEmpty()) {
                ItemStack extracted = handler.extractItem(slot, 1, false);
                if (!extracted.isEmpty()) {
                    player.drop(extracted, true);
                    currentWeight -= ItemWeightRegistry.getWeight(extracted);
                } else {
                    break;
                }
            }
        }

        return currentWeight;
    }

    private static double dropFromContainer(ServerPlayer player, Container container, double currentWeight, double maxWeight) {
        for (int slot = container.getContainerSize() - 1; slot >= 0 && currentWeight > maxWeight; slot--) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) continue;

            if (stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                IItemHandler nested = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
                if (nested != null) {
                    currentWeight = dropFromHandler(player, nested, currentWeight, maxWeight);
                    stack = container.getItem(slot);
                    if (stack.isEmpty()) continue;
                }
            }

            while (currentWeight > maxWeight && !stack.isEmpty()) {
                ItemStack extracted = container.removeItem(slot, 1);
                if (!extracted.isEmpty()) {
                    player.drop(extracted, true);
                    currentWeight -= ItemWeightRegistry.getWeight(extracted);
                    container.setChanged();
                } else {
                    break;
                }
            }
        }

        return currentWeight;
    }
}