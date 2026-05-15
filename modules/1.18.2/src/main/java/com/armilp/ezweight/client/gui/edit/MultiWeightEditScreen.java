package com.armilp.ezweight.client.gui.edit;

import com.armilp.ezweight.client.gui.WeightListWidget;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.network.sync.WeightUpdatePacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class MultiWeightEditScreen extends Screen {
    private final Screen parent;
    private final List<WeightListWidget.ItemEntry> items;
    private EditBox weightBox;

    public MultiWeightEditScreen(Screen parent, List<WeightListWidget.ItemEntry> items) {
        super(new TranslatableComponent("screen.ezweight.edit_multiple"));
        this.parent = parent;
        this.items = items;
    }

    @Override
    protected void init() {
        weightBox = new EditBox(this.font, this.width / 2 - 50, this.height / 2 - 10, 100, 20,
                new TranslatableComponent("gui.ezweight.weight_placeholder"));
        this.addRenderableWidget(weightBox);

        this.addRenderableWidget(new Button(
                this.width / 2 - 50, this.height / 2 + 20, 100, 20,
                new TranslatableComponent("gui.ezweight.save"),
                b -> {
                    try {
                        double newWeight = Double.parseDouble(weightBox.getValue());
                        for (WeightListWidget.ItemEntry entry : items) {
                            ResourceLocation id = ForgeRegistries.ITEMS.getKey(entry.stack.getItem());
                            if (id != null) {
                                EZWeightNetwork.CHANNEL.sendToServer(new WeightUpdatePacket(id, newWeight));
                            }
                        }
                        this.minecraft.setScreen(parent);
                    } catch (NumberFormatException e) {
                        weightBox.setTextColor(0xFF5555);
                    }
                }
        ));

        this.addRenderableWidget(new Button(
                this.width / 2 - 50, this.height / 2 + 50, 100, 20,
                new TranslatableComponent("gui.ezweight.cancel"),
                b -> this.minecraft.setScreen(parent)
        ));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, new TranslatableComponent("screen.ezweight.edit_multiple"), this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
        weightBox.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}