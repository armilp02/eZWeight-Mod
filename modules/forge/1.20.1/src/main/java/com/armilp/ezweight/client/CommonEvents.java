package com.armilp.ezweight.client;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.levels.WeightLevelManager;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.network.sync.WeightLevelsSyncPacket;
import com.armilp.ezweight.network.sync.WeightSyncPacket;
import com.armilp.ezweight.player.DynamicMaxWeightCalculator;
import com.armilp.ezweight.registry.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = EZWeight.MODID)
public class CommonEvents {

    // Track last synced weight to avoid unnecessary network traffic
    private static final Map<UUID, Double> lastSyncedWeights = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 1. Sync data for weight over the level
            EZWeightNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    WeightLevelsSyncPacket.fromLevels(WeightLevelManager.getLevels())
            );

            // 2. Get the player's current max weight directly from the attribute
            double maxWeight = getPlayerMaxWeight(serverPlayer);

            // 3. Sync to client
            EZWeightNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new WeightSyncPacket(maxWeight)
            );

            // 4. Store initial value to avoid immediate re-sync
            lastSyncedWeights.put(serverPlayer.getUUID(), maxWeight);

            EZWeight.LOGGER.debug("Player {} joined with max weight: {}kg",
                    serverPlayer.getName().getString(), maxWeight);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;

        Player player = event.player;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // Calculate dynamic max weight (this updates the attribute internally)
        double currentMaxWeight = DynamicMaxWeightCalculator.calculate(serverPlayer);

        // Check if weight has changed significantly (avoid spam)
        Double lastSynced = lastSyncedWeights.get(player.getUUID());
        if (lastSynced == null || Math.abs(currentMaxWeight - lastSynced) > 0.001) {
            // Sync to client
            EZWeightNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new WeightSyncPacket(currentMaxWeight)
            );

            // Update tracking
            lastSyncedWeights.put(player.getUUID(), currentMaxWeight);

            EZWeight.LOGGER.debug("Synced weight for {}: {}kg",
                    player.getName().getString(), currentMaxWeight);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Resync weight when changing dimensions
            double maxWeight = getPlayerMaxWeight(serverPlayer);
            EZWeightNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new WeightSyncPacket(maxWeight)
            );
            lastSyncedWeights.put(serverPlayer.getUUID(), maxWeight);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Resync weight on respawn
            double maxWeight = getPlayerMaxWeight(serverPlayer);
            EZWeightNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new WeightSyncPacket(maxWeight)
            );
            lastSyncedWeights.put(serverPlayer.getUUID(), maxWeight);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clean up tracking data
        if (event.getEntity() != null) {
            lastSyncedWeights.remove(event.getEntity().getUUID());
        }
    }

    // Help safely get player's max weight from attribute
    private static double getPlayerMaxWeight(Player player) {
        var attribute = player.getAttribute(ModAttributes.WEIGHT.get());
        if (attribute != null) {
            return attribute.getValue();
        }
        return WeightConfig.COMMON.MAX_WEIGHT.get();
    }
}