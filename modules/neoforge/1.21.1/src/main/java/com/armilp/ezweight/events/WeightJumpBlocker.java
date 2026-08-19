package com.armilp.ezweight.events;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.player.DynamicMaxWeightCalculator;
import com.armilp.ezweight.player.PlayerWeightHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;


import java.util.List;


@EventBusSubscriber(modid = EZWeight.MODID)
public class WeightJumpBlocker {

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide) return;

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (!WeightCommands.isWeightEnabledFor(serverPlayer)) return;

        double currentWeight = PlayerWeightHandler.getTotalWeight(player);
        double maxWeight = DynamicMaxWeightCalculator.calculate(player);

        if (currentWeight > maxWeight || isJumpDisabled(currentWeight)) {
            player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
            player.hurtMarked = true;

            serverPlayer.displayClientMessage(
                    Component.translatable("message.ezweight.jump_blocked")
                            .withStyle(ChatFormatting.RED), true
            );
        }
    }

    public static boolean isJumpDisabled(double weight) {
        if (!WeightConfig.COMMON.NO_JUMP_WEIGHT_ENABLED.get()) return false;

        List<? extends String> ranges = WeightConfig.COMMON.NO_JUMP_WEIGHT_RANGES.get();
        for (int i = 0; i < ranges.size() - 1; i += 2) {
            double min = WeightConfig.parseWeightValue(ranges.get(i));
            double max = WeightConfig.parseWeightValue(ranges.get(i + 1));
            if (weight >= min && weight <= max) {
                return true;
            }
        }
        return false;
    }
}
