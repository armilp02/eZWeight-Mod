package com.armilp.ezweight.events;

import com.armilp.ezweight.commands.WeightCommands;
import com.armilp.ezweight.levels.WeightLevel;
import com.armilp.ezweight.levels.WeightLevelManager;
import com.armilp.ezweight.player.PlayerWeightHandler;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class WeightEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        if (!(event.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;

        // Si está deshabilitado para este jugador, eliminar efectos de peso
        if (!WeightCommands.isWeightEnabledFor(serverPlayer)) {
            removeWeightEffects(serverPlayer);
            return;
        }

        double totalWeight = PlayerWeightHandler.getTotalWeight(serverPlayer);
        WeightLevel level = WeightLevelManager.getLevelForWeight(totalWeight);

        if (level == null) return;

        // Quitar efectos anteriores que ya no son válidos
        List<MobEffect> validEffects = level.effects().stream()
                .map(MobEffectInstance::getEffect)
                .toList();

        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance active : serverPlayer.getActiveEffects()) {
            if (isWeightEffect(active.getEffect()) && !validEffects.contains(active.getEffect())) {
                toRemove.add(active.getEffect());
            }
        }

        for (MobEffect effect : toRemove) {
            serverPlayer.removeEffect(effect);
        }

        // Aplicar los efectos necesarios
        for (MobEffectInstance effectInstance : level.effects()) {
            MobEffect effect = effectInstance.getEffect();
            MobEffectInstance current = serverPlayer.getEffect(effect);

            // Aplicar si no existe o si el amplificador cambió
            if (current == null || current.getAmplifier() != effectInstance.getAmplifier()) {
                // Usa duración larga (por ejemplo, 6000 ticks = 5 minutos)
                serverPlayer.addEffect(new MobEffectInstance(
                        effect,
                        6000,
                        effectInstance.getAmplifier(),
                        true, // ambient
                        false, // showParticles
                        true  // showIcon
                ));
            }
        }
    }

    private static void removeWeightEffects(Player player) {
        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance active : player.getActiveEffects()) {
            if (isWeightEffect(active.getEffect())) {
                toRemove.add(active.getEffect());
            }
        }
        for (MobEffect effect : toRemove) {
            player.removeEffect(effect);
        }
    }

    private static boolean isWeightEffect(MobEffect effect) {
        return WeightLevelManager.getLevels().stream()
                .flatMap(level -> level.effects().stream())
                .anyMatch(instance -> instance.getEffect().equals(effect));
    }
}
