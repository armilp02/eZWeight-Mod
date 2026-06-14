package com.armilp.ezweight.events;

import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.player.DynamicMaxWeightCalculator;
import com.armilp.ezweight.player.PlayerWeightHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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
        Entity entity = event.getEntity();

        // Verificar que sea un LivingEntity primero
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) entity;

        // Verificar que sea un Player
        if (!(livingEntity instanceof Player)) return;
        Player player = (Player) livingEntity;

        if (player.level.isClientSide) return;

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (!WeightCommands.isWeightEnabledFor(serverPlayer)) return;

        double currentWeight = PlayerWeightHandler.getTotalWeight(player);
        double maxWeight = DynamicMaxWeightCalculator.calculate(player);

        if (currentWeight > maxWeight || isJumpDisabled(currentWeight)) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1));
            player.hurtMarked = true;

            serverPlayer.displayClientMessage(
                    new TranslatableComponent("message.ezweight.jump_blocked")
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