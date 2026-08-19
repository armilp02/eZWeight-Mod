package com.armilp.ezweight.player;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.registry.ModAttributes;
import com.armilp.ezweight.util.BackpackIdUtils;
import com.armilp.ezweight.util.DebugLog;
import com.mrcrayfish.backpacked.BackpackHelper;
import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import javax.annotation.Nullable;
import java.util.*;

public class PlayerWeightHandler {

    public static double getExtendedStackWeightWithContents(ItemStack stack, @Nullable Player player) {
        HolderLookup.Provider registries = player != null ? player.level().registryAccess() : null;
        return getStackWeight(stack, player, Collections.newSetFromMap(new IdentityHashMap<>()), registries);
    }

    private static final boolean CURIOS_LOADED = ModList.get().isLoaded(CuriosApi.MODID);
    private static final boolean BACKPACKED_LOADED = ModList.get().isLoaded("backpacked");

    public static double getTotalWeight(Player player) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        HolderLookup.Provider registries = player.level().registryAccess();
        double total = 0.0;

        total += getInventoryWeight(player, visited, registries);

        if (CURIOS_LOADED) {
            total += getCuriosWeight(player, visited, registries);
        }

        if (BACKPACKED_LOADED) {
            total += getBackpackedWeight(player, visited, registries);
        }

        return total;
    }

    private static double getInventoryWeight(Player player, Set<Object> visited, HolderLookup.Provider registries) {
        double total = 0.0;

        for (ItemStack stack : player.getInventory().items) {
            total += getStackWeight(stack, player, visited, registries);
        }

        for (ItemStack stack : player.getInventory().armor) {
            total += getStackWeight(stack, player, visited, registries);
        }

        for (ItemStack stack : player.getInventory().offhand) {
            total += getStackWeight(stack, player, visited, registries);
        }

        return total;
    }

    private static double getCuriosWeight(Player player, Set<Object> visited, HolderLookup.Provider registries) {
        Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);

        return curios.map(handler -> {
            double total = 0.0;

            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                IItemHandler itemHandler = stacksHandler.getStacks();

                if (!visited.add(itemHandler)) {
                    continue;
                }

                total += getHandlerWeight(itemHandler, player, visited, registries);
            }

            return total;
        }).orElse(0.0);
    }

    public static double getMaxWeight(Player player) {
        var attribute = player.getAttribute(ModAttributes.WEIGHT);
        if (attribute != null) {
            return attribute.getValue();
        }
        return WeightConfig.COMMON.MAX_WEIGHT.get();
    }

    private static double getBackpackedWeight(Player player, Set<Object> visited, HolderLookup.Provider registries) {
        NonNullList<ItemStack> backpacks = BackpackHelper.getBackpacks(player);
        double total = 0.0;

        for (ItemStack backpack : backpacks) {
            if (backpack.isEmpty()) continue;

            double backpackWeight = ItemWeightRegistry.getWeight(backpack) * backpack.getCount();
            total += backpackWeight;

            DebugLog.log("Backpack: %s weight=%s", backpack.getItem().getName(backpack).getString(), backpackWeight);
        }

        BackpackedInventoryAccess access = (BackpackedInventoryAccess) player;
        Iterator<BackpackInventory> inventories = access.backpacked$streamNonNullBackpackInventories().iterator();

        while (inventories.hasNext()) {
            BackpackInventory inventory = inventories.next();
            double contentsWeight = getContainerWeight(inventory, player, visited, registries);
            total += contentsWeight;

            DebugLog.log("Backpack contents weight=%s", contentsWeight);
        }

        DebugLog.log("Total backpacked weight: %s", total);
        return total;
    }

    private static double getContainerWeight(Container container, @Nullable Player player, Set<Object> visited, HolderLookup.Provider registries) {
        double total = 0.0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                total += getStackWeight(stack, player, visited, registries);
            }
        }

        return total;
    }

    public static double getStackWeight(ItemStack stack, @Nullable Player player, Set<Object> visited, @Nullable HolderLookup.Provider registries) {
        if (stack.isEmpty()) {
            return 0.0;
        }

        double total = ItemWeightRegistry.getWeight(stack) * stack.getCount();

        if (!visited.add(stack)) {
            return total;
        }

        IItemHandler capability = stack.getCapability(Capabilities.ItemHandler.ITEM);

        if (capability != null) {
            if (visited.add(capability)) {
                double contents = getHandlerWeight(capability, player, visited, registries);

                if (WeightConfig.COMMON.BACKPACK_WEIGHT_REDUCTION_ENABLED.get()
                        && BackpackIdUtils.isBackpackItem(stack)) {
                    contents = BackpackIdUtils.calculateBackpackContentsWeight(stack, contents);
                }

                total += contents;
            }
        } else {
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            CompoundTag tag = (data != null && !data.isEmpty()) ? data.copyTag() : null;
            total += extractWeightFromTag(tag, visited, registries);
        }

        if (BackpackIdUtils.TRAVELERS_BACKPACK_LOADED
                && stack.getItem() instanceof TravelersBackpackItem
                && player != null) {

            BackpackWrapper wrapper = new BackpackWrapper(stack, 1, player, player.level(), -1);
            IItemHandler handler = wrapper.getStorage();

            if (visited.add(handler)) {
                double contents = getHandlerWeight(handler, player, visited, registries);

                if (WeightConfig.COMMON.BACKPACK_WEIGHT_REDUCTION_ENABLED.get()) {
                    contents = BackpackIdUtils.calculateBackpackContentsWeight(stack, contents);
                }

                total += contents;
            }
        }

        return total;
    }

    private static double getHandlerWeight(IItemHandler handler, @Nullable Player player, Set<Object> visited, @Nullable HolderLookup.Provider registries) {
        double total = 0.0;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);

            if (!stack.isEmpty()) {
                total += getStackWeight(stack, player, visited, registries);
            }
        }

        return total;
    }

    public static double extractWeightFromTag(@Nullable CompoundTag tag, Set<Object> visited, @Nullable HolderLookup.Provider registries) {
        if (tag == null || tag.isEmpty() || registries == null) {
            return 0.0;
        }

        double total = 0.0;

        for (String key : tag.getAllKeys()) {
            try {
                if (tag.contains(key, 9)) {
                    ListTag list = tag.getList(key, 10);

                    for (int i = 0; i < list.size(); i++) {
                        ItemStack stack = ItemStack.parseOptional(registries, list.getCompound(i));

                        if (!stack.isEmpty()) {
                            total += getStackWeight(stack, null, visited, registries);
                        }
                    }
                } else if (tag.contains(key, 10)) {
                    CompoundTag inner = tag.getCompound(key);
                    total += extractWeightFromTag(inner, visited, registries);
                }
            } catch (Exception ignored) {
            }
        }

        return total;
    }

}