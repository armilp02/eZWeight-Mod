package com.armilp.ezweight.events;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.armilp.ezweight.util.BackpackIdUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;


public class ItemTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        if (!WeightConfig.COMMON.ITEM_TOOLTIP_ENABLED.get()) return;

        if (WeightConfig.COMMON.BACKPACK_WEIGHT_REDUCTION_ENABLED.get()
                && WeightConfig.COMMON.BACKPACK_SHOW_TOOLTIP.get()
                && BackpackIdUtils.isBackpackItem(stack)) {
            addBackpackTooltip(event, stack);
        }

        double baseWeight  = ItemWeightRegistry.getWeight(stack);
        double totalWeight = PlayerWeightHandler.getExtendedStackWeightWithContents(stack, event.getEntity());

        if (totalWeight <= 0.0) return;

        int baseColor  = parseColor(WeightConfig.COMMON.ITEM_TOOLTIP_COLOR_BASE.get(),  0xFFD700);
        int totalColor = parseColor(WeightConfig.COMMON.ITEM_TOOLTIP_COLOR_TOTAL.get(), 0xFFFF00);

        if (totalWeight == baseWeight * stack.getCount()) {
            event.getToolTip().add(
                    Component.translatable("tooltip.ezweight.item_weight", fmt(totalWeight))
                            .setStyle(Style.EMPTY.withColor(baseColor)));
        } else {
            if (baseWeight > 0.0) {
                event.getToolTip().add(
                        Component.translatable("tooltip.ezweight.item_weight_base", fmt(baseWeight))
                                .setStyle(Style.EMPTY.withColor(baseColor)));
            }
            event.getToolTip().add(
                    Component.translatable("tooltip.ezweight.item_weight_total", fmt(totalWeight))
                            .setStyle(Style.EMPTY.withColor(totalColor)));
        }
    }

    private static void addBackpackTooltip(ItemTooltipEvent event, ItemStack stack) {
        BackpackIdUtils.BackpackInfo info = BackpackIdUtils.getBackpackInfo(stack);
        int color = parseColor(WeightConfig.COMMON.BACKPACK_TOOLTIP_COLOR.get(), 0x55FF55);

        double reductionPct = info.getWeightReductionPercent();
        if (reductionPct > 0) {
            event.getToolTip().add(
                    Component.literal(String.format("Reduces weight by %.0f%%", reductionPct))
                            .setStyle(Style.EMPTY.withColor(color)));
        }

        Double max = info.getMaxWeight();
        if (max != null && max > 0) {
            event.getToolTip().add(
                    Component.literal(String.format("Capped at %.1f kg", max))
                            .setStyle(Style.EMPTY.withColor(color)));
        }
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }

    private static int parseColor(String hex, int fallback) {
        try {
            return Integer.parseInt(hex.replace("#", "").replace("0x", ""), 16);
        } catch (Exception e) {
            return fallback;
        }
    }
}