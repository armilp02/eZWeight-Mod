package com.armilp.ezweight.client.gui.edit;

import com.armilp.ezweight.client.gui.WeightMenuScreen;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.network.sync.WeightUpdatePacket;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class WeightEditScreen extends Screen {

    private final Screen parent;
    private final ItemStack stack;
    private final ResourceLocation effectiveId;
    private EditBox weightBox;
    private boolean hasError = false;

    public WeightEditScreen(Screen parent, ItemStack stack, ResourceLocation effectiveId) {
        super(Component.translatable("screen.ezweight.edit_weight"));
        this.parent = parent;
        this.stack = stack;
        this.effectiveId = effectiveId;
    }

    @Override
    protected void init() {
        super.init();

        double currentWeight = ItemWeightRegistry.getAllWeights().getOrDefault(effectiveId, 1.0);

        weightBox = new EditBox(this.font, this.width / 2 - 50, this.height / 2 - 10, 100, 20,
                Component.translatable("gui.ezweight.weight_placeholder"));
        weightBox.setValue(String.format("%.2f", currentWeight));
        weightBox.setResponder(this::onWeightChanged);
        this.addRenderableWidget(weightBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.ezweight.save"), b -> {
            if (saveWeight()) {
                this.minecraft.setScreen(parent);
            }
        }).bounds(this.width / 2 - 100, this.height / 2 + 30, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.ezweight.cancel"), b -> {
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 + 20, this.height / 2 + 30, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.ezweight.reset"), b -> {
            double estimatedWeight = estimateDefaultWeight();
            weightBox.setValue(String.format("%.2f", estimatedWeight));
            hasError = false;
        }).bounds(this.width / 2 - 40, this.height / 2 + 60, 80, 20).build());
    }

    private void onWeightChanged(String value) {
        try {
            Double.parseDouble(value.replace(",", "."));
            hasError = false;
            weightBox.setTextColor(0xFFFFFF);
        } catch (NumberFormatException e) {
            hasError = true;
            weightBox.setTextColor(0xFF5555);
        }
    }

    private boolean saveWeight() {
        try {
            double newWeight = Double.parseDouble(weightBox.getValue().replace(",", "."));
            if (newWeight < 0) {
                hasError = true;
                weightBox.setTextColor(0xFF5555);
                return false;
            }

            ItemWeightRegistry.setWeight(effectiveId, newWeight);

            EZWeightNetwork.CHANNEL.sendToServer(new WeightUpdatePacket(effectiveId, newWeight));

            if (parent instanceof WeightMenuScreen) {
                ((WeightMenuScreen) parent).onWeightUpdated();
            }

            return true;
        } catch (NumberFormatException e) {
            hasError = true;
            weightBox.setTextColor(0xFF5555);
            return false;
        }
    }

    private double estimateDefaultWeight() {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && !effectiveId.equals(itemId)) {
            String gunType = effectiveId.getPath().toLowerCase();
            if (gunType.contains("pistol")) return 2.0;
            if (gunType.contains("sniper")) return 7.0;
            if (gunType.contains("shotgun")) return 6.5;
            if (gunType.contains("smg")) return 4.0;
            if (gunType.contains("rifle")) return 5.5;
            return 5.0;
        }
        return 1.0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, 50, 0xFFFFFF);

        String itemName = stack.getHoverName().getString();
        graphics.drawCenteredString(this.font, itemName, this.width / 2, this.height / 2 - 60, 0xFFFFFF);

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            String itemIdText = Component.translatable("gui.ezweight.item_id", itemId.toString()).getString();
            graphics.drawCenteredString(this.font, itemIdText, this.width / 2, this.height / 2 - 30, 0x888888);
        }

        if (hasError) {
            String errorMsg = Component.translatable("gui.ezweight.invalid_weight").getString();
            graphics.drawCenteredString(this.font, errorMsg, this.width / 2, this.height / 2 + 10, 0xFF5555);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.renderItem(stack, this.width / 2 - 8, this.height / 2 - 80);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.minecraft.setScreen(parent);
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            if (saveWeight()) {
                this.minecraft.setScreen(parent);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        weightBox.tick();
    }
}