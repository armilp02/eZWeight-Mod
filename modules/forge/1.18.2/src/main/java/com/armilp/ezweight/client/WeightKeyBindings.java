package com.armilp.ezweight.client;

import com.armilp.ezweight.client.gui.edit.WeightConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "ezweight", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WeightKeyBindings {

    public static final String CATEGORY = "key.categories.ezweight";
    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.ezweight.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientRegistry.registerKeyBinding(OPEN_CONFIG);
        });
    }

    @Mod.EventBusSubscriber(modid = "ezweight", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();
                if (OPEN_CONFIG.consumeClick() && mc.screen == null) {
                    mc.setScreen(new WeightConfigScreen(null));
                }
            }
        }
    }
}