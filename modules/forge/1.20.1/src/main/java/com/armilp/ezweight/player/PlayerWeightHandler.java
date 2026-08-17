package com.armilp.ezweight.player;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.registry.ModAttributes;
import com.armilp.ezweight.util.BackpackIdUtils;
import com.armilp.ezweight.util.DebugLog;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class PlayerWeightHandler {

    public static double getExtendedStackWeightWithContents(ItemStack stack, @Nullable Player player) {
        return getStackWeight(stack, player, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static final boolean CURIOS_LOADED = ModList.get().isLoaded(CuriosApi.MODID);
    private static final boolean BACKPACKED_LOADED = ModList.get().isLoaded("backpacked");

    public static double getTotalWeight(Player player) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        double total = 0.0;

        total += getInventoryWeight(player, visited);

        if (CURIOS_LOADED) {
            total += getCuriosWeight(player, visited);
        }

        if (BACKPACKED_LOADED) {
            total += getBackpackedWeight(player, visited);
        }

        return total;
    }

    private static double getInventoryWeight(Player player, Set<Object> visited) {
        double total = 0.0;

        for (ItemStack stack : player.getInventory().items) {
            total += getStackWeight(stack, player, visited);
        }

        for (ItemStack stack : player.getInventory().armor) {
            total += getStackWeight(stack, player, visited);
        }

        for (ItemStack stack : player.getInventory().offhand) {
            total += getStackWeight(stack, player, visited);
        }

        return total;
    }

    private static double getCuriosWeight(Player player, Set<Object> visited) {
        LazyOptional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);

        return curios.map(handler -> {
            double total = 0.0;

            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                IItemHandler itemHandler = stacksHandler.getStacks();

                if (!visited.add(itemHandler)) {
                    continue;
                }

                total += getHandlerWeight(itemHandler, player, visited);
            }

            return total;
        }).orElse(0.0);
    }

    public static double getMaxWeight(Player player) {
        var attribute = player.getAttribute(ModAttributes.WEIGHT.get());
        if (attribute != null) {
            return attribute.getValue();
        }
        return WeightConfig.COMMON.MAX_WEIGHT.get();
    }

    private static double getBackpackedWeight(Player player, Set<Object> visited) {
        if (!(player instanceof ServerPlayer)) {
            return 0.0;
        }

        if (!(player instanceof com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess access)) {
            DebugLog.log("Not BackpackedInventoryAccess");
            return 0.0;
        }

        double total = 0.0;
        int max = com.mrcrayfish.backpacked.inventory.ManagementInventory.getMaxEquipable();
        DebugLog.log("Max equipable: %d", max);

        for (int i = 0; i < max; i++) {
            ItemStack backpack = BackpackHelper.getBackpackStack(player, i);
            DebugLog.log("Slot %d: %s", i, backpack.isEmpty() ? "EMPTY" : backpack.getItem().getName(backpack).getString());

            if (backpack.isEmpty()) {
                continue;
            }

            double backpackWeight = ItemWeightRegistry.getWeight(backpack) * backpack.getCount();
            total += backpackWeight;
            DebugLog.log("Backpack weight: %s", backpackWeight);

            com.mrcrayfish.backpacked.inventory.BackpackInventory inventory = access.backpacked$GetBackpackInventory(i);
            DebugLog.log("Inventory for slot %d: %s", i, inventory == null ? "NULL" : "FOUND, size: " + inventory.getContainerSize());

            if (inventory != null) {
                for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                    ItemStack inner = inventory.getItem(slot);

                    if (!inner.isEmpty()) {
                        DebugLog.log("  Slot %d: %s x%d weight: %s", slot, inner.getItem().getName(inner).getString(), inner.getCount(), ItemWeightRegistry.getWeight(inner));
                        total += ItemWeightRegistry.getWeight(inner) * inner.getCount();
                    }
                }
            } else {
                DebugLog.log("Using extractWeightFromTag");
                total += extractWeightFromTag(backpack.getTag(), visited);
            }
        }

        DebugLog.log("Total backpacked weight: %s", total);
        return total;
    }

    public static double getStackWeight(ItemStack stack, @Nullable Player player, Set<Object> visited) {
        if (stack.isEmpty()) {
            return 0.0;
        }

        double total = ItemWeightRegistry.getWeight(stack) * stack.getCount();

        if (!visited.add(stack)) {
            return total;
        }

        LazyOptional<IItemHandler> capability = stack.getCapability(ForgeCapabilities.ITEM_HANDLER);

        if (capability.isPresent()) {
            IItemHandler handler = capability.orElseThrow(IllegalStateException::new);

            if (visited.add(handler)) {
                double contents = getHandlerWeight(handler, player, visited);

                if (WeightConfig.COMMON.BACKPACK_WEIGHT_REDUCTION_ENABLED.get()
                        && BackpackIdUtils.isBackpackItem(stack)) {
                    contents = BackpackIdUtils.calculateBackpackContentsWeight(stack, contents);
                }

                total += contents;
            }
        } else {
            total += extractWeightFromTag(stack.getTag(), visited);
        }

        if (BackpackIdUtils.TRAVELERS_BACKPACK_LOADED
                && stack.getItem() instanceof TravelersBackpackItem
                && player != null) {

            BackpackWrapper wrapper = new BackpackWrapper(stack, 1, player, player.level(), -1);
            IItemHandler handler = wrapper.getStorage();

            if (visited.add(handler)) {
                double contents = getHandlerWeight(handler, player, visited);

                if (WeightConfig.COMMON.BACKPACK_WEIGHT_REDUCTION_ENABLED.get()) {
                    contents = BackpackIdUtils.calculateBackpackContentsWeight(stack, contents);
                }

                total += contents;
            }
        }

        return total;
    }

    private static double getHandlerWeight(IItemHandler handler, @Nullable Player player, Set<Object> visited) {
        double total = 0.0;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);

            if (!stack.isEmpty()) {
                total += getStackWeight(stack, player, visited);
            }
        }

        return total;
    }

    public static double extractWeightFromTag(@Nullable CompoundTag tag, Set<Object> visited) {
        if (tag == null || tag.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;

        for (String key : tag.getAllKeys()) {
            try {
                if (tag.contains(key, 9)) {
                    ListTag list = tag.getList(key, 10);

                    for (int i = 0; i < list.size(); i++) {
                        ItemStack stack = ItemStack.of(list.getCompound(i));

                        if (!stack.isEmpty()) {
                            total += getStackWeight(stack, null, visited);
                        }
                    }
                } else if (tag.contains(key, 10)) {
                    CompoundTag inner = tag.getCompound(key);
                    total += extractWeightFromTag(inner, visited);
                }
            } catch (Exception ignored) {
            }
        }

        return total;
    }


}