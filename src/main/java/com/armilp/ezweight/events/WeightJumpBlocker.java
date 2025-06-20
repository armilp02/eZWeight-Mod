package com.armilp.ezweight.events;

import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.player.PlayerWeightHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class WeightJumpBlocker {

    @SubscribeEvent
    public static void onLivingJump(LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide) return;

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (!WeightCommands.isWeightEnabledFor(serverPlayer)) return;

        double weight = PlayerWeightHandler.getTotalWeight(player);
        if (isJumpDisabled(weight)) {
            player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
            player.hurtMarked = true;

            serverPlayer.displayClientMessage(
                    Component.translatable("message.ezweight.jump_blocked").withStyle(ChatFormatting.RED), true
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
