package com.armilp.ezweight.events;

import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.player.DynamicMaxWeightCalculator;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.armilp.ezweight.registry.WeightDamageSources;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber
public class WeightDamage {

    private static final String TICK_COUNTER_TAG = "ezweight_damage_tick_counter";
    private static final int TICKS_PER_DAMAGE = 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;

        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!WeightCommands.isWeightEnabledFor(serverPlayer)) return;

        double currentWeight = PlayerWeightHandler.getTotalWeight(player);
        double maxWeight = DynamicMaxWeightCalculator.calculate(player);

        if (!WeightConfig.COMMON.DAMAGE_OVERWEIGHT_ENABLED.get() || maxWeight <= 0.0) {
            resetTickCounter(player);
            return;
        }

        double overweightRatio = currentWeight / maxWeight;
        if (!isOverThreshold(overweightRatio)) {
            resetTickCounter(player);
            return;
        }

        CompoundTag data = player.getPersistentData();
        CompoundTag ezwData = data.contains("ezweight")
                ? data.getCompound("ezweight")
                : new CompoundTag();

        int ticksOverweight = ezwData.getInt(TICK_COUNTER_TAG) + 1;

        if (ticksOverweight >= TICKS_PER_DAMAGE) {
            ticksOverweight = 0;
            float damage = calculateDamage(currentWeight, maxWeight);
            if (damage > 0.0f) {
                DamageSource source = WeightDamageSources.overweight(player.level().registryAccess());
                player.hurt(source, damage);
                serverPlayer.displayClientMessage(
                        Component.translatable("message.ezweight.overweight_damage").withStyle(ChatFormatting.DARK_RED),
                        true
                );
            }
        }

        ezwData.putInt(TICK_COUNTER_TAG, ticksOverweight);
        data.put("ezweight", ezwData);
    }

    private static void resetTickCounter(Player player) {
        CompoundTag data = player.getPersistentData();
        CompoundTag ezwData = data.getCompound("ezweight");

        ezwData.putInt(TICK_COUNTER_TAG, 0);
        data.put("ezweight", ezwData);
    }

    private static boolean isOverThreshold(double overweightRatio) {
        List<? extends String> thresholds = WeightConfig.COMMON.DAMAGE_OVERWEIGHT_THRESHOLDS.get();
        for (String str : thresholds) {
            try {
                double threshold = Double.parseDouble(str);
                if (overweightRatio >= threshold) return true;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    private static float calculateDamage(double currentWeight, double maxWeight) {
        if (!WeightConfig.COMMON.DAMAGE_OVERWEIGHT_ENABLED.get()) {
            return 0.0f;
        }
        float configured = WeightConfig.COMMON.DAMAGE_PER_SECOND.get().floatValue();
        if (configured <= 0.0f) return 0.0f;
        // vanilla ignores damage below 1.0, so enforce a minimum
        return Math.max(configured, 1.0f);
    }
}
