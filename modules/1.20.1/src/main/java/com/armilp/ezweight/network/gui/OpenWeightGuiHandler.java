// OpenWeightGuiHandler.java
package com.armilp.ezweight.network.gui;

import com.armilp.ezweight.client.gui.WeightNamespaceScreen;
import net.minecraft.client.Minecraft;

public class OpenWeightGuiHandler {
    public static void handle() {
        Minecraft.getInstance().setScreen(new WeightNamespaceScreen());
    }
}
