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
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Path;

import static com.armilp.ezweight.registry.ModAttributes.WEIGHT;

@Mod(EZWeight.MODID)
public class EZWeight {

    public static final String MODID = "ezweight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EZWeight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEffects.register(modEventBus);
        ModAttributes.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WeightConfig.COMMON_SPEC, "ezweight/config.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, WeightConfig.CLIENT_SPEC, "ezweight/client_config.toml");

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Path configDir = FMLPaths.CONFIGDIR.get().resolve(MODID);
            ItemWeightRegistry.init(configDir);
            WeightLevelManager.init(configDir);
            EZWeightNetwork.register();
        });
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("EZWeight mod loaded on {}", FMLEnvironment.dist);

    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            EZWeightNetwork.sendToPlayer(FullWeightSyncPacket.fromRegistry(), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, WEIGHT.get());
        EZWeight.LOGGER.info("Weight attribute registered for players!");
    }

}
