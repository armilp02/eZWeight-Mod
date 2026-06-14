package com.armilp.ezweight.player;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.util.BackpackIdUtils;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;

public class PlayerWeightHandler {

    private static final boolean CURIOS_LOADED = ModList.get().isLoaded(CuriosApi.MODID);
    private static final boolean BACKPACKED_LOADED = ModList.get().isLoaded("backpacked");

    private static final boolean DEBUG_BACKPACKED_WEIGHT = false;

    private static void debug(String message) {
        if (DEBUG_BACKPACKED_WEIGHT) {
            System.out.println("[eZWeight/BackpackedDebug] " + message);
        }
    }

    public static double getExtendedStackWeightWithContents(ItemStack stack, @Nullable Player player) {
        return getStackWeight(stack, player, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

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
        Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);

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

    private static double getBackpackedWeight(Player player, Set<Object> visited) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0.0;
        }

        if (!(player instanceof com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess access)) {
            debug("Not BackpackedInventoryAccess");
            return 0.0;
        }

        double total = 0.0;
        int max = com.mrcrayfish.backpacked.inventory.ManagementInventory.getMaxEquipable();

        debug("Max equipable: " + max);

        for (int i = 0; i < max; i++) {
            ItemStack backpack = BackpackHelper.getBackpackStack(player, i);

            debug("Slot " + i + ": " + (backpack.isEmpty()
                    ? "EMPTY"
                    : backpack.getItem().getName(backpack).getString()));

            if (backpack.isEmpty()) {
                continue;
            }

            double backpackWeight = ItemWeightRegistry.getWeight(backpack) * backpack.getCount();
            total += backpackWeight;

            debug("Backpack weight: " + backpackWeight);

            com.mrcrayfish.backpacked.inventory.BackpackInventory inventory =
                    access.backpacked$GetBackpackInventory(i);

            debug("Inventory for slot " + i + ": " + (inventory == null
                    ? "NULL"
                    : "FOUND, size: " + inventory.getContainerSize()));

            if (inventory != null) {
                for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                    ItemStack inner = inventory.getItem(slot);

                    if (!inner.isEmpty()) {
                        double innerWeight = ItemWeightRegistry.getWeight(inner) * inner.getCount();

                        debug("  Slot " + slot + ": "
                                + inner.getItem().getName(inner).getString()
                                + " x" + inner.getCount()
                                + " weight: " + ItemWeightRegistry.getWeight(inner));

                        total += innerWeight;
                    }
                }
            } else {
                debug("Using extractWeightFromTag");

                CompoundTag tag = saveStackToTag(backpack, serverPlayer.registryAccess());
                total += extractWeightFromTag(tag, visited, serverPlayer.registryAccess());
            }
        }

        debug("Total backpacked weight: " + total);
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

        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);

        if (handler != null) {
            if (visited.add(handler)) {
                double contents = getHandlerWeight(handler, player, visited);

                if (WeightConfig.COMMON.BACKPACK_WEIGHT_REDUCTION_ENABLED.get()
                        && BackpackIdUtils.isBackpackItem(stack)) {
                    contents = BackpackIdUtils.calculateBackpackContentsWeight(stack, contents);
                }

                total += contents;
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag tag = saveStackToTag(stack, serverPlayer.registryAccess());
            total += extractWeightFromTag(tag, visited, serverPlayer.registryAccess());
        }

        if (BackpackIdUtils.TRAVELERS_BACKPACK_LOADED
                && stack.getItem() instanceof TravelersBackpackItem
                && player != null) {

            BackpackWrapper wrapper = new BackpackWrapper(stack, 1, player, player.level(), -1);
            IItemHandler travelerHandler = wrapper.getStorage();

            if (visited.add(travelerHandler)) {
                double contents = getHandlerWeight(travelerHandler, player, visited);

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

    public static double extractWeightFromTag(
            @Nullable CompoundTag tag,
            Set<Object> visited,
            HolderLookup.Provider registryAccess
    ) {
        if (tag == null || tag.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;

        for (String key : tag.getAllKeys()) {
            try {
                if (tag.contains(key, 9)) {
                    ListTag list = tag.getList(key, 10);

                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag itemTag = list.getCompound(i);
                        ItemStack stack = ItemStack.parseOptional(registryAccess, itemTag);

                        if (!stack.isEmpty()) {
                            total += getStackWeight(stack, null, visited);
                        }
                    }
                } else if (tag.contains(key, 10)) {
                    CompoundTag inner = tag.getCompound(key);
                    total += extractWeightFromTag(inner, visited, registryAccess);
                }
            } catch (Exception ignored) {
            }
        }

        return total;
    }

    private static CompoundTag saveStackToTag(ItemStack stack, HolderLookup.Provider registryAccess) {
        try {
            return (CompoundTag) stack.save(registryAccess);
        } catch (Exception e) {
            return new CompoundTag();
        }
    }
}