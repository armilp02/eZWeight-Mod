package com.armilp.ezweight.client;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.WeightSyncData;
import com.armilp.ezweight.levels.WeightLevelManager;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.network.sync.WeightLevelsSyncPacket;
import com.armilp.ezweight.network.sync.WeightSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = EZWeight.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        double serverMaxWeight = WeightConfig.COMMON.MAX_WEIGHT.get();

        EZWeightNetwork.sendToPlayer(
                WeightSyncPacket.REGISTRATION,
                new WeightSyncPacket(serverMaxWeight),
                serverPlayer
        );

        EZWeightNetwork.sendToPlayer(
                WeightLevelsSyncPacket.REGISTRATION,
                WeightLevelsSyncPacket.fromLevels(WeightLevelManager.getLevels()),
                serverPlayer
        );
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        WeightSyncData.updateDynamicMaxWeight(serverPlayer);

        if (WeightSyncData.consumeUpdatedFlag()) {
            double newMax = WeightSyncData.getMaxWeight();

            EZWeightNetwork.sendToPlayer(
                    WeightSyncPacket.REGISTRATION,
                    new WeightSyncPacket(newMax),
                    serverPlayer
            );
        }
    }
}