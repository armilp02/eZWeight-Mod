package com.armilp.ezweight.player;

import com.armilp.ezweight.data.ItemWeightRegistry;
import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.inventory.TravelersBackpackContainer;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nullable;

public class PlayerWeightHandler {

    public static double getTotalWeight(Player player) {
        double total = 0.0;

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
                total += getExtendedStackWeightWithContents(stack, player);
            }
        }

        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty()) {
                total += getExtendedStackWeightWithContents(stack, player);
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            total += getExtendedStackWeightWithContents(offhand, player);
        }

        if (ModList.get().isLoaded(CuriosApi.MODID)) {
            LazyOptional<ICuriosItemHandler> curiosCap = player.getCapability(CuriosCapability.INVENTORY);
            total += curiosCap.map(handler -> getCuriosInventoryWeight(handler, player)).orElse(0.0);
        }

        if (ModList.get().isLoaded(TravelersBackpack.MODID) && CapabilityUtils.isWearingBackpack(player)) {
            TravelersBackpackContainer backpackContainer = CapabilityUtils.getBackpackInv(player);
            if (backpackContainer != null) {
                IItemHandler backpackInventory = backpackContainer.getHandler();
                total += getHandlerContentsWeight(backpackInventory);

                IItemHandler toolSlots = backpackContainer.getToolSlotsHandler();
                total += getHandlerContentsWeight(toolSlots);
            }
        }

        return total;
    }

    private static double getCuriosInventoryWeight(ICuriosItemHandler handler, Player player) {
        double sum = 0.0;
        if (ModList.get().isLoaded(CuriosApi.MODID)) {
            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                IItemHandlerModifiable itemHandler = stacksHandler.getStacks();
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        sum += getExtendedStackWeightWithContents(stack, player);
                    }
                }
            }
        }

        return sum;
    }

    public static double getExtendedStackWeightWithContents(ItemStack stack, @Nullable Player player) {
        double total = getStackWeightWithContents(stack);
        total += extractWeightFromTag(stack.getTag());

        if (ModList.get().isLoaded(TravelersBackpack.MODID) && stack.getItem() instanceof TravelersBackpackItem) {
            CompoundTag tag = stack.getOrCreateTag();

            if (tag.contains("Inventory")) {
                CompoundTag inventoryTag = tag.getCompound("Inventory");
                total += extractWeightFromInventoryNBT(inventoryTag);
            }

            if (tag.contains("ToolsInventory")) {
                CompoundTag toolsTag = tag.getCompound("ToolsInventory");
                total += extractWeightFromInventoryNBT(toolsTag);
            }
        }

        return total;
    }

    private static double getStackWeightWithContents(ItemStack stack) {
        double per = ItemWeightRegistry.getWeight(stack);
        double tot = per * stack.getCount();

        boolean isContainerItem = stack.hasTag() && stack.getTag().contains("BlockEntityTag");

        double capWeight = 0.0;
        if (!isContainerItem) {
            capWeight = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                    .map(PlayerWeightHandler::getHandlerContentsWeight)
                    .orElse(0.0);
        }
        return tot + capWeight;
    }

    public static double extractWeightFromTag(CompoundTag tag) {
        if (tag == null) return 0.0;
        double sum = 0.0;

        for (String key : tag.getAllKeys()) {
            String lk = key.toLowerCase();

            if (lk.equals("items") || lk.contains("inv") || lk.equals("toolsinventory") || lk.equals("upgrades") || lk.equals("blockentitytag")) {
                try {
                    if (lk.equals("blockentitytag")) {
                        CompoundTag be = tag.getCompound(key);
                        if (be.contains("Items", 9)) {
                            ListTag list = be.getList("Items", 10);
                            for (int i = 0; i < list.size(); i++) {
                                ItemStack inner = ItemStack.of(list.getCompound(i));
                                if (!inner.isEmpty()) {
                                    sum += getExtendedStackWeightWithContents(inner, null);
                                }
                            }
                            continue;
                        }
                        sum += extractWeightFromTag(be);
                        continue;
                    }
                    ListTag list = tag.getList(key, 10);
                    for (int i = 0; i < list.size(); i++) {
                        ItemStack inner = ItemStack.of(list.getCompound(i));
                        if (!inner.isEmpty()) {
                            sum += getExtendedStackWeightWithContents(inner, null);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return sum;
    }

    private static double extractWeightFromInventoryNBT(CompoundTag nbt) {
        double sum = 0.0;
        if (nbt.contains("Items", 9)) {
            ListTag items = nbt.getList("Items", 10);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemTag = items.getCompound(i);
                ItemStack stack = ItemStack.of(itemTag);
                if (!stack.isEmpty()) {
                    sum += getExtendedStackWeightWithContents(stack, null);
                }
            }
        }
        return sum;
    }

    private static double getHandlerContentsWeight(IItemHandler handler) {
        double sum = 0.0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (!slot.isEmpty()) {
                sum += getExtendedStackWeightWithContents(slot, null);
            }
        }
        return sum;
    }
}