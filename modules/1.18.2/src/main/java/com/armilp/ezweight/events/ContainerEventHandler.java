package com.armilp.ezweight.events;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.player.DynamicMaxWeightCalculator;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.inventory.TravelersBackpackContainer;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(modid = EZWeight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ContainerEventHandler {

    private static final boolean TRAVELERS_LOADED = ModList.get().isLoaded(TravelersBackpack.MODID);

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide()) return;

        ServerPlayer player = (ServerPlayer) event.player;
        if (!WeightCommands.isWeightEnabledFor(player)) return;

        double totalWeight = PlayerWeightHandler.getTotalWeight(player);
        double maxWeight = DynamicMaxWeightCalculator.calculate(player);

        if (totalWeight > maxWeight) {
            player.displayClientMessage(
                    new TranslatableComponent("message.ezweight.overburdened",
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
        }
    }

    private static double dropItemsFromInventory(ServerPlayer player, double currentWeight, double maxWeight) {
        if (TRAVELERS_LOADED && CapabilityUtils.isWearingBackpack(player)) {
            TravelersBackpackContainer backpackContainer = CapabilityUtils.getBackpackInv(player);

            if (backpackContainer != null) {
                IItemHandler backpackInventory = backpackContainer.getHandler();
                currentWeight = dropFromHandler(player, backpackInventory, currentWeight, maxWeight, backpackContainer, (byte) 0);

                if (currentWeight > maxWeight) {
                    IItemHandler toolSlots = backpackContainer.getToolSlotsHandler();
                    currentWeight = dropFromHandler(player, toolSlots, currentWeight, maxWeight, backpackContainer, (byte) 1);
                }
            }
        }

        for (int i = player.getInventory().items.size() - 1; i >= 0 && currentWeight > maxWeight; i--) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.isEmpty()) continue;

            if (TRAVELERS_LOADED && stack.getItem() instanceof TravelersBackpackItem) {
                CompoundTag tag = stack.getOrCreateTag();

                if (tag.contains("Inventory")) {
                    currentWeight = dropFromBackpackNBT(player, stack, "Inventory", currentWeight, maxWeight);
                }

                if (currentWeight > maxWeight && tag.contains("ToolsInventory")) {
                    currentWeight = dropFromBackpackNBT(player, stack, "ToolsInventory", currentWeight, maxWeight);
                }

                if (currentWeight > maxWeight && !stack.isEmpty()) {
                    currentWeight = dropSingleItemFromStack(player, stack, currentWeight);
                }

                continue;
            }

            // Dropear el item en sí
            currentWeight = dropSingleItemFromStack(player, stack, currentWeight);
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

    private static double dropFromHandler(ServerPlayer player, IItemHandler handler, double currentWeight,
                                          double maxWeight, TravelersBackpackContainer container, byte dataId) {
        for (int slot = handler.getSlots() - 1; slot >= 0 && currentWeight > maxWeight; slot--) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            while (currentWeight > maxWeight && !stack.isEmpty()) {
                ItemStack extracted = handler.extractItem(slot, 1, false);
                if (!extracted.isEmpty()) {
                    player.drop(extracted, true);
                    currentWeight -= ItemWeightRegistry.getWeight(extracted);

                    container.setDataChanged(dataId);
                } else {
                    break;
                }
            }
        }

        return currentWeight;
    }

    private static double dropFromBackpackNBT(ServerPlayer player, ItemStack backpackStack,
                                              String inventoryKey, double currentWeight, double maxWeight) {
        CompoundTag tag = backpackStack.getOrCreateTag();
        if (!tag.contains(inventoryKey)) return currentWeight;

        CompoundTag inventoryTag = tag.getCompound(inventoryKey);
        if (!inventoryTag.contains("Items", 9)) return currentWeight;

        ListTag items = inventoryTag.getList("Items", 10);
        boolean modified = false;

        for (int i = items.size() - 1; i >= 0 && currentWeight > maxWeight; i--) {
            CompoundTag itemTag = items.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);

            if (stack.isEmpty()) continue;

            double itemWeight = ItemWeightRegistry.getWeight(stack);

            while (currentWeight > maxWeight && stack.getCount() > 0) {
                ItemStack drop = stack.copy();
                drop.setCount(1);
                player.drop(drop, true);

                stack.shrink(1);
                currentWeight -= itemWeight;
                modified = true;
            }

            if (stack.isEmpty()) {
                items.remove(i);
            } else {
                CompoundTag newItemTag = new CompoundTag();
                stack.save(newItemTag);
                newItemTag.putInt("Slot", itemTag.getInt("Slot"));
                items.set(i, newItemTag);
            }
        }

        if (modified) {
            inventoryTag.put("Items", items);
            tag.put(inventoryKey, inventoryTag);
        }

        return currentWeight;
    }
}