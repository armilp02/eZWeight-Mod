package com.armilp.ezweight.client;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.client.gui.edit.WeightConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = EZWeight.MODID, value = Dist.CLIENT)
public class WeightKeyBindings {

    public static final String CATEGORY = "key.categories.ezweight";

    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.ezweight.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
    }

    @EventBusSubscriber(modid = EZWeight.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();

            while (OPEN_CONFIG.consumeClick()) {
                if (mc.screen == null) {
                    mc.setScreen(new WeightConfigScreen(null));
                }
            }
        }
    }
}