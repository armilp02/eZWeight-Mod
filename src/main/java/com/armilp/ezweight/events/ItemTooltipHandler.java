package com.armilp.ezweight.events;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.player.PlayerWeightHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ezweight", value = Dist.CLIENT)
public class ItemTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty()) {
            if (!WeightConfig.COMMON.ITEM_TOOLTIP_ENABLED.get()) return;

            double baseWeight = ItemWeightRegistry.getWeight(stack);
            double totalWeight = PlayerWeightHandler.getExtendedStackWeightWithContents(stack, event.getEntity());

            if (totalWeight > 0.0) {
                String baseColor = WeightConfig.COMMON.ITEM_TOOLTIP_COLOR_BASE.get();
                String totalColor = WeightConfig.COMMON.ITEM_TOOLTIP_COLOR_TOTAL.get();

                int baseColorRgb = Integer.parseInt(baseColor.substring(1), 16);
                int totalColorRgb = Integer.parseInt(totalColor.substring(1), 16);

                if (totalWeight == baseWeight * stack.getCount()) {
                    event.getToolTip().add(Component.translatable("tooltip.ezweight.item_weight", String.format("%.2f", totalWeight))
                            .setStyle(Style.EMPTY.withColor(baseColorRgb)));
                } else {
                    if (baseWeight > 0.0) {
                        event.getToolTip().add(Component.translatable("tooltip.ezweight.item_weight_base", String.format( "%.2f", baseWeight))
                                .setStyle(Style.EMPTY.withColor(baseColorRgb)));
                    }

                    event.getToolTip().add(Component.translatable("tooltip.ezweight.item_weight_total", String.format("%.2f", totalWeight))
                            .setStyle(Style.EMPTY.withColor(totalColorRgb)));
                }
            }
        }
    }
}
