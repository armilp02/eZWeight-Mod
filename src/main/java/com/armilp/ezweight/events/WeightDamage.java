package com.armilp.ezweight.events;

import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.armilp.ezweight.registry.WeightDamageSources;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class WeightDamage {

    private static final String TICK_COUNTER_TAG = "ezweight_damage_tick_counter";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;
        double weight = PlayerWeightHandler.getTotalWeight(player);

        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (!WeightCommands.isWeightEnabledFor(serverPlayer)) return;

        if (!shouldApplyDamage(weight)) {
            resetTickCounter(player);
            return;
        }

        CompoundTag data = player.getPersistentData();
        int tickCounter = data.getInt(TICK_COUNTER_TAG);
        tickCounter++;

        if (tickCounter >= 20) {
            tickCounter = 0;
            float damage = calculateDamage(weight);
            if (damage > 0.0f) {
                DamageSource damageSource = WeightDamageSources.overweight(player.level().registryAccess());
                player.hurt(damageSource, damage);

                // Mostrar mensaje de advertencia de daño por sobrepeso
                serverPlayer.displayClientMessage(
                        Component.translatable("message.ezweight.overweight_damage").withStyle(ChatFormatting.DARK_RED),
                        true
                );
            }
        }

        data.putInt(TICK_COUNTER_TAG, tickCounter);
    }

    private static void resetTickCounter(Player player) {
        player.getPersistentData().putInt(TICK_COUNTER_TAG, 0);
    }

    private static boolean shouldApplyDamage(double weight) {
        if (!WeightConfig.COMMON.DAMAGE_OVERWEIGHT_ENABLED.get()) return false;

        List<? extends String> ranges = WeightConfig.COMMON.DAMAGE_OVERWEIGHT_RANGES.get();
        for (int i = 0; i < ranges.size() - 1; i += 2) {
            double min = WeightConfig.parseWeightValue(ranges.get(i));
            double max = WeightConfig.parseWeightValue(ranges.get(i + 1));
            if (weight >= min && weight <= max) {
                return true;
            }
        }
        return false;
    }

    private static float calculateDamage(double weight) {
        if (!WeightConfig.COMMON.PROGRESSIVE_DAMAGE_ENABLED.get()) {
            return WeightConfig.COMMON.DAMAGE_PER_SECOND.get().floatValue();
        }

        double startWeight = WeightConfig.getProgressiveStartWeight();
        if (weight <= startWeight) return 0.0f;

        double step = WeightConfig.COMMON.PROGRESSIVE_WEIGHT_STEP.get();
        double damagePerStep = WeightConfig.COMMON.PROGRESSIVE_DAMAGE_PER_STEP.get();

        int stepsOver = (int) ((weight - startWeight) / step);
        return (float) (stepsOver * damagePerStep);
    }
}
