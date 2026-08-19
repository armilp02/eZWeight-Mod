package com.armilp.ezweight.events;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.levels.WeightLevel;
import com.armilp.ezweight.levels.WeightLevelManager;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.armilp.ezweight.registry.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = EZWeight.MODID)
public class WeightEventHandler {

    private static final int EFFECT_DURATION = 6000;
    private static final int CHECK_INTERVAL = 20;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;

        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        if (!WeightCommands.isWeightEnabledFor(serverPlayer)) {
            removeWeightEffects(serverPlayer);
            return;
        }

        double totalWeight = PlayerWeightHandler.getTotalWeight(serverPlayer);
        WeightLevel currentLevel = WeightLevelManager.getLevelForWeight(totalWeight);

        if (currentLevel == null || currentLevel.effects().isEmpty()) {
            removeWeightEffects(serverPlayer);
            return;
        }

        // Updated: map to Holder<MobEffect> instead of raw MobEffect
        List<Holder<MobEffect>> validEffects = currentLevel.effects().stream()
                .map(MobEffectInstance::getEffect)
                .toList();

        List<Holder<MobEffect>> toRemove = new ArrayList<>();
        for (MobEffectInstance active : serverPlayer.getActiveEffects()) {
            if (isWeightEffect(active.getEffect()) && !validEffects.contains(active.getEffect())) {
                toRemove.add(active.getEffect());
            }
        }

        for (Holder<MobEffect> effect : toRemove) {
            serverPlayer.removeEffect(effect);
        }

        for (MobEffectInstance effectInstance : currentLevel.effects()) {
            Holder<MobEffect> effect = effectInstance.getEffect();
            MobEffectInstance currentEffect = serverPlayer.getEffect(effect);

            if (currentEffect == null ||
                    currentEffect.getAmplifier() != effectInstance.getAmplifier() ||
                    currentEffect.getDuration() < 100) {

                serverPlayer.addEffect(new MobEffectInstance(
                        effect,
                        EFFECT_DURATION,
                        effectInstance.getAmplifier(),
                        false,
                        false,
                        true
                ));
            }
        }
    }

    private static void removeWeightEffects(Player player) {
        List<Holder<MobEffect>> toRemove = new ArrayList<>();
        for (MobEffectInstance active : player.getActiveEffects()) {
            if (isWeightEffect(active.getEffect())) {
                toRemove.add(active.getEffect());
            }
        }
        for (Holder<MobEffect> effect : toRemove) {
            player.removeEffect(effect);
        }
    }

    // Updated: Signature and comparison logic updated to accept and handle Holder<MobEffect>
    private static boolean isWeightEffect(Holder<MobEffect> effect) {
        // DeferredHolders implement Holder natively, so we can directly use .equals()
        if (effect.equals(ModEffects.LIGHT_ENCUMBERED) ||
                effect.equals(ModEffects.ENCUMBERED) ||
                effect.equals(ModEffects.HEAVILY_ENCUMBERED) ||
                effect.equals(ModEffects.OVERBURDENED) ||
                effect.equals(ModEffects.CRUSHED)) {
            return true;
        }

        return WeightLevelManager.getLevels().stream()
                .flatMap(level -> level.effects().stream())
                .anyMatch(instance -> instance.getEffect().equals(effect));
    }
}