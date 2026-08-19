package com.armilp.ezweight;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.levels.WeightLevelManager;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.network.sync.FullWeightSyncPacket;
import com.armilp.ezweight.registry.ModAttributes;
import com.armilp.ezweight.registry.ModEffects;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod(EZWeight.MODID)
public class EZWeight {

    public static final String MODID = "ezweight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EZWeight(IEventBus modEventBus, ModContainer modContainer) {
        ModEffects.register(modEventBus);
        ModAttributes.register(modEventBus);

        modEventBus.register(ModAttributes.class);
        modEventBus.register(EZWeightNetwork.class);

        // Updated: Configuration registration now targets the injected ModContainer instance
        modContainer.registerConfig(ModConfig.Type.COMMON, WeightConfig.COMMON_SPEC, "ezweight/config.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, WeightConfig.CLIENT_SPEC, "ezweight/client_config.toml");

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Path configDir = FMLPaths.CONFIGDIR.get().resolve(MODID);
            ItemWeightRegistry.init(configDir);
            WeightLevelManager.init(configDir);

        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("EZWeight mod loaded on {}", FMLEnvironment.dist);
    }

}