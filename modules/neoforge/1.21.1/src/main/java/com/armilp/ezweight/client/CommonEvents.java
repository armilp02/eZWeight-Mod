package com.armilp.ezweight.client;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.levels.WeightLevelManager;
import com.armilp.ezweight.network.EZWeightNetwork;  // Add this import
import com.armilp.ezweight.network.sync.FullWeightSyncPacket;
import com.armilp.ezweight.network.sync.TotalWeightSyncPacket;
import com.armilp.ezweight.network.sync.WeightLevelsSyncPacket;
import com.armilp.ezweight.network.sync.WeightSyncPacket;
import com.armilp.ezweight.player.DynamicMaxWeightCalculator;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.armilp.ezweight.registry.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = "ezweight")
public class CommonEvents {

    private static final Map<UUID, Double> lastSyncedWeights = new HashMap<>();
    private static final Map<UUID, Double> lastSyncedTotalWeights = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && !event.getLevel().isClientSide()) {

            // Use the helper method for consistency
            EZWeightNetwork.sendToPlayer(FullWeightSyncPacket.fromRegistry(), serverPlayer);
            EZWeightNetwork.sendToPlayer(WeightLevelsSyncPacket.fromLevels(WeightLevelManager.getLevels()), serverPlayer);

            double maxWeight = getPlayerMaxWeight(serverPlayer);
            EZWeightNetwork.sendToPlayer(new WeightSyncPacket(maxWeight), serverPlayer);
            lastSyncedWeights.put(serverPlayer.getUUID(), maxWeight);

            double totalWeight = PlayerWeightHandler.getTotalWeight(serverPlayer);
            EZWeightNetwork.sendToPlayer(new TotalWeightSyncPacket(totalWeight), serverPlayer);
            lastSyncedTotalWeights.put(serverPlayer.getUUID(), totalWeight);

            EZWeight.LOGGER.debug("Player {} joined the world with max weight: {}kg",
                    serverPlayer.getName().getString(), maxWeight);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (player.level().isClientSide) return;

        double currentMaxWeight = DynamicMaxWeightCalculator.calculate(serverPlayer);

        Double lastSynced = lastSyncedWeights.get(player.getUUID());
        if (lastSynced == null || Math.abs(currentMaxWeight - lastSynced) > 0.001) {
            EZWeightNetwork.sendToPlayer(new WeightSyncPacket(currentMaxWeight), serverPlayer);
            lastSyncedWeights.put(player.getUUID(), currentMaxWeight);

            EZWeight.LOGGER.debug("Synced weight for {}: {}kg",
                    player.getName().getString(), currentMaxWeight);
        }

        double currentTotalWeight = PlayerWeightHandler.getTotalWeight(serverPlayer);
        Double lastSyncedTotal = lastSyncedTotalWeights.get(player.getUUID());
        if (lastSyncedTotal == null || Math.abs(currentTotalWeight - lastSyncedTotal) > 0.001) {
            EZWeightNetwork.sendToPlayer(new TotalWeightSyncPacket(currentTotalWeight), serverPlayer);
            lastSyncedTotalWeights.put(player.getUUID(), currentTotalWeight);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            double maxWeight = getPlayerMaxWeight(serverPlayer);
            EZWeightNetwork.sendToPlayer(new WeightSyncPacket(maxWeight), serverPlayer);
            lastSyncedWeights.put(serverPlayer.getUUID(), maxWeight);

            double totalWeight = PlayerWeightHandler.getTotalWeight(serverPlayer);
            EZWeightNetwork.sendToPlayer(new TotalWeightSyncPacket(totalWeight), serverPlayer);
            lastSyncedTotalWeights.put(serverPlayer.getUUID(), totalWeight);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            double maxWeight = getPlayerMaxWeight(serverPlayer);
            EZWeightNetwork.sendToPlayer(new WeightSyncPacket(maxWeight), serverPlayer);
            lastSyncedWeights.put(serverPlayer.getUUID(), maxWeight);

            double totalWeight = PlayerWeightHandler.getTotalWeight(serverPlayer);
            EZWeightNetwork.sendToPlayer(new TotalWeightSyncPacket(totalWeight), serverPlayer);
            lastSyncedTotalWeights.put(serverPlayer.getUUID(), totalWeight);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            lastSyncedWeights.remove(event.getEntity().getUUID());
            lastSyncedTotalWeights.remove(event.getEntity().getUUID());
        }
    }

    private static double getPlayerMaxWeight(Player player) {
        var attribute = player.getAttribute(ModAttributes.WEIGHT);
        if (attribute != null) {
            return attribute.getValue();
        }
        return WeightConfig.COMMON.MAX_WEIGHT.get();
    }
}