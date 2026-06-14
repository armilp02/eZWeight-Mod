package com.armilp.ezweight.client.gui.edit;

import com.armilp.ezweight.client.gui.WeightMenuScreen;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.network.sync.WeightUpdatePacket;
import com.armilp.ezweight.util.BackpackIdUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class BackpackIdEditScreen extends Screen {

    private final Screen parent;
    private final ItemStack stack;
    private EditBox weightBox;
    private EditBox idBox;
    private boolean hasWeightError = false;
    private boolean hasIdError = false;
    private final BackpackIdUtils.BackpackInfo backpackInfo;

    public BackpackIdEditScreen(Screen parent, ItemStack stack) {
        super(Component.translatable("screen.ezweight.edit_backpack"));
        this.parent = parent;
        this.stack = stack;
        this.backpackInfo = BackpackIdUtils.getBackpackInfo(stack);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        ResourceLocation effectiveId = backpackInfo.getEffectiveId();
        double currentWeight = ItemWeightRegistry.getAllWeights().getOrDefault(effectiveId, 1.0);

        weightBox = new EditBox(this.font, centerX - 90, centerY - 30, 180, 20,
                Component.translatable("gui.ezweight.weight_placeholder"));
        weightBox.setValue(String.format("%.2f", currentWeight));
        weightBox.setResponder(this::onWeightChanged);
        this.addRenderableWidget(weightBox);

        idBox = new EditBox(this.font, centerX - 140, centerY + 5, 280, 20,
                Component.translatable("gui.ezweight.backpack_id_placeholder"));
        loadCurrentId();
        idBox.setResponder(this::onIdChanged);
        this.addRenderableWidget(idBox);

        int buttonY = centerY + 40;

        this.addRenderableWidget(Button.builder(Component.translatable("gui.ezweight.save"), b -> {
            if (saveChanges()) {
                if (this.minecraft != null) this.minecraft.setScreen(parent);
            }
        }).bounds(centerX - 115, buttonY, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.ezweight.cancel"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(centerX - 35, buttonY, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.ezweight.reset_weight"), b -> {
            if (weightBox != null) weightBox.setValue("1.0");
            hasWeightError = false;
        }).bounds(centerX + 45, buttonY, 75, 20).build());
    }

    private void loadCurrentId() {
        if (backpackInfo.hasBackpackId()) {
            idBox.setValue(backpackInfo.getBackpackId().get().toString());
        }
        updateIdBoxColor();
    }

    private void onWeightChanged(String value) {
        try {
            double weight = Double.parseDouble(value.replace(",", "."));
            hasWeightError = weight < 0;
            weightBox.setTextColor(hasWeightError ? 0xFF5555 : 0xFFFFFF);
        } catch (NumberFormatException e) {
            hasWeightError = true;
            weightBox.setTextColor(0xFF5555);
        }
    }

    private void onIdChanged(String value) {
        if (value.trim().isEmpty()) {
            hasIdError = false;
            updateIdBoxColor();
            return;
        }
        validateCurrentId();
    }

    private void validateCurrentId() {
        String value = idBox.getValue().trim();
        if (value.isEmpty()) {
            hasIdError = false;
            updateIdBoxColor();
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        hasIdError = (id == null);
        updateIdBoxColor();
    }

    private void updateIdBoxColor() {
        if (hasIdError) {
            idBox.setTextColor(0xFF5555);
        } else if (!idBox.getValue().trim().isEmpty()) {
            idBox.setTextColor(0x55FF55);
        } else {
            idBox.setTextColor(0xFFFFFF);
        }
    }

    private boolean saveChanges() {
        if (hasWeightError || hasIdError) return false;

        try {
            double newWeight = Double.parseDouble(weightBox.getValue().replace(",", "."));
            if (newWeight < 0) {
                hasWeightError = true;
                weightBox.setTextColor(0xFF5555);
                return false;
            }

            String idValue = idBox.getValue().trim();
            ResourceLocation targetId;

            if (backpackInfo.hasBackpackId()) {
                BackpackIdUtils.removeBackpackId(stack);
            }

            if (!idValue.isEmpty()) {
                ResourceLocation newId = ResourceLocation.tryParse(idValue);
                if (newId == null) {
                    hasIdError = true;
                    updateIdBoxColor();
                    return false;
                }
                BackpackIdUtils.setBackpackId(stack, newId);
                targetId = newId;
            } else {
                targetId = backpackInfo.getItemId();
            }

            ItemWeightRegistry.setWeight(targetId, newWeight);
            EZWeightNetwork.sendToServer(WeightUpdatePacket.REGISTRATION, new WeightUpdatePacket(targetId, newWeight));

            if (parent instanceof WeightMenuScreen weightMenuScreen) {
                weightMenuScreen.onWeightUpdated();
            }

            return true;
        } catch (Exception e) {
            hasWeightError = true;
            hasIdError = true;
            weightBox.setTextColor(0xFF5555);
            updateIdBoxColor();
            return false;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        graphics.drawCenteredString(this.font, this.title, centerX, centerY - 90, 0xFFFFFF);
        graphics.renderItem(stack, centerX - 8, centerY - 72);

        String itemName = backpackInfo.getDisplayName();
        graphics.drawCenteredString(this.font, itemName, centerX, centerY - 54, 0xFFFFFF);

        graphics.drawString(this.font, "Weight (kg):", centerX - 150, centerY - 42, 0xFFFFFF);
        graphics.drawString(this.font, "Backpack ID:", centerX - 150, centerY - 7, 0xFFFFFF);

        if (!idBox.getValue().trim().isEmpty()) {
            String status = hasIdError ? "✗ Invalid" : "✓ Valid";
            int color = hasIdError ? 0xFF5555 : 0x55FF55;
            graphics.drawString(this.font, status, centerX + 150, centerY + 7, color);
        }

        if (hasWeightError) {
            String errorMsg = Component.translatable("gui.ezweight.invalid_weight").getString();
            graphics.drawCenteredString(this.font, errorMsg, centerX, centerY - 18, 0xFF5555);
        }

        renderCurrentInfo(graphics, centerY + 75);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCurrentInfo(GuiGraphics graphics, int startY) {
        int centerX = this.width / 2;
        int y = startY;

        if (backpackInfo.hasBackpackId()) {
            graphics.drawCenteredString(this.font,
                    "Current ID: " + backpackInfo.getBackpackId().get(), centerX, y, 0xFFFF55);
            y += 12;
        }

        double reduction = backpackInfo.getWeightReductionPercent();
        graphics.drawCenteredString(this.font,
                String.format("Reduction: %.0f%%  |  Max: %s",
                        reduction,
                        backpackInfo.getMaxWeight() != null
                                ? String.format("%.1f kg", backpackInfo.getMaxWeight())
                                : "unlimited"),
                centerX, y, 0x55FFFF);

        y += 14;
        graphics.drawCenteredString(this.font,
                "Use /ezweight backpack <reduction%> <maxWeight> to change these",
                centerX, y, 0x888888);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (this.minecraft != null) this.minecraft.setScreen(parent);
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            if (saveChanges()) {
                if (this.minecraft != null) this.minecraft.setScreen(parent);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        if (weightBox != null) weightBox.getValue();
        if (idBox != null) idBox.getValue();
    }
}