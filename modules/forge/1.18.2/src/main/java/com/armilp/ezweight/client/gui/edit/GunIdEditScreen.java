package com.armilp.ezweight.client.gui.edit;

import com.armilp.ezweight.client.gui.WeightMenuScreen;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.network.sync.AmmoWeightUpdatePacket;
import com.armilp.ezweight.network.sync.WeightUpdatePacket;
import com.armilp.ezweight.util.GunIdUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GunIdEditScreen extends Screen {

    private final Screen parent;
    private final ItemStack stack;
    private EditBox weightBox;
    private EditBox idBox;
    private EditBox ammoWeightBox;
    private boolean hasWeightError = false;
    private boolean hasIdError = false;
    private boolean hasAmmoWeightError = false;
    private final GunIdUtils.GunInfo gunInfo;

    public GunIdEditScreen(Screen parent, ItemStack stack) {
        super(new TranslatableComponent("screen.ezweight.edit_gun_id"));
        this.parent = parent;
        this.stack = stack;
        this.gunInfo = GunIdUtils.getGunInfo(stack);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        ResourceLocation effectiveId = gunInfo.getEffectiveId();
        double currentWeight = ItemWeightRegistry.getAllWeights().getOrDefault(effectiveId, 1.0);

        weightBox = new EditBox(this.font, centerX - 90, centerY - 34, 180, 20,
                new TranslatableComponent("gui.ezweight.weight_placeholder"));
        weightBox.setValue(String.format("%.2f", currentWeight));
        weightBox.setResponder(this::onWeightChanged);
        this.addRenderableWidget(weightBox);

        idBox = new EditBox(this.font, centerX - 140, centerY, 280, 20,
                new TranslatableComponent("gui.ezweight.id_placeholder"));
        loadCurrentId();
        idBox.setResponder(this::onIdChanged);
        this.addRenderableWidget(idBox);

        int buttonY = centerY + 28;

        if (gunInfo.hasGunId()) {
            ResourceLocation gunId = gunInfo.getGunId().get();
            Double currentAmmoWeight = ItemWeightRegistry.getGunAmmoWeight(gunId);

            ammoWeightBox = new EditBox(this.font, centerX - 90, centerY + 34, 180, 20,
                    new TranslatableComponent("gui.ezweight.ammo_weight_placeholder"));
            ammoWeightBox.setValue(String.format("%.4f",
                    currentAmmoWeight != null ? currentAmmoWeight : 0.02));
            ammoWeightBox.setResponder(this::onAmmoWeightChanged);
            this.addRenderableWidget(ammoWeightBox);

            buttonY = centerY + 62;
        }

        this.addRenderableWidget(new Button(centerX - 115, buttonY, 75, 20,
                new TranslatableComponent("gui.ezweight.save"), b -> {
            if (saveChanges()) {
                if (this.minecraft != null) this.minecraft.setScreen(parent);
            }
        }));

        this.addRenderableWidget(new Button(centerX - 35, buttonY, 75, 20,
                new TranslatableComponent("gui.ezweight.cancel"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }));

        this.addRenderableWidget(new Button(centerX + 55, buttonY, 95, 20,
                new TranslatableComponent("gui.ezweight.reset_weight"), b -> {
            double estimatedWeight = estimateDefaultWeight();
            if (weightBox != null) weightBox.setValue(String.format("%.2f", estimatedWeight));
            hasWeightError = false;
        }));
    }

    private void loadCurrentId() {
        if (gunInfo.hasGunId()) {
            idBox.setValue(gunInfo.getGunId().get().toString());
        } else if (gunInfo.hasAttachmentId()) {
            idBox.setValue(gunInfo.getAttachmentId().get().toString());
        } else if (gunInfo.hasAmmoId()) {
            idBox.setValue(gunInfo.getAmmoId().get().toString());
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

    private void onAmmoWeightChanged(String value) {
        try {
            double weight = Double.parseDouble(value.replace(",", "."));
            hasAmmoWeightError = weight < 0;
            if (ammoWeightBox != null) {
                ammoWeightBox.setTextColor(hasAmmoWeightError ? 0xFF5555 : 0xFFFFFF);
            }
        } catch (NumberFormatException e) {
            hasAmmoWeightError = true;
            if (ammoWeightBox != null) {
                ammoWeightBox.setTextColor(0xFF5555);
            }
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
        if (hasWeightError || hasIdError || hasAmmoWeightError) {
            return false;
        }

        try {
            double newWeight = Double.parseDouble(weightBox.getValue().replace(",", "."));
            if (newWeight < 0) {
                hasWeightError = true;
                weightBox.setTextColor(0xFF5555);
                return false;
            }

            String idValue = idBox.getValue().trim();
            ResourceLocation targetId;

            if (gunInfo.hasGunId()) {
                GunIdUtils.removeGunId(stack);
            }
            if (gunInfo.hasAttachmentId()) {
                GunIdUtils.removeAttachmentId(stack);
            }
            if (gunInfo.hasAmmoId()) {
                GunIdUtils.removeAmmoId(stack);
            }

            if (!idValue.isEmpty()) {
                ResourceLocation newId = ResourceLocation.tryParse(idValue);
                if (newId == null) {
                    hasIdError = true;
                    updateIdBoxColor();
                    return false;
                }

                if (GunIdUtils.setGunId(stack, newId)) {
                    targetId = newId;
                } else {
                    GunIdUtils.setAttachmentId(stack, newId);
                    targetId = newId;
                }
            } else {
                targetId = gunInfo.getItemId();
            }

            ItemWeightRegistry.setWeight(targetId, newWeight);
            EZWeightNetwork.CHANNEL.sendToServer(new WeightUpdatePacket(targetId, newWeight));

            if (gunInfo.hasGunId() && ammoWeightBox != null) {
                try {
                    double ammoWeight = Double.parseDouble(ammoWeightBox.getValue().replace(",", "."));
                    if (ammoWeight >= 0) {
                        ItemWeightRegistry.setGunAmmoWeight(gunInfo.getGunId().get(), ammoWeight);
                        EZWeightNetwork.CHANNEL.sendToServer(new AmmoWeightUpdatePacket(gunInfo.getGunId().get(), ammoWeight));
                    }
                } catch (Exception e) {
                    hasAmmoWeightError = true;
                }
            }

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

    private double estimateDefaultWeight() {
        if (gunInfo.hasGunId()) {
            String gunType = gunInfo.getGunId().get().getPath().toLowerCase();
            if (gunType.contains("pistol")) return 2.0;
            if (gunType.contains("sniper")) return 7.0;
            if (gunType.contains("shotgun")) return 6.5;
            if (gunType.contains("smg")) return 4.0;
            if (gunType.contains("rifle")) return 5.5;
            return 5.0;
        }
        if (gunInfo.hasAttachmentId()) return 0.8;
        if (gunInfo.hasAmmoId()) return 0.2;
        return 1.0;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        drawCenteredString(poseStack, this.font, this.title, centerX, centerY - 90, 0xFFFFFF);

        this.itemRenderer.renderGuiItem(stack, centerX - 8, centerY - 72);

        String itemName = gunInfo.getDisplayName();
        drawCenteredString(poseStack, this.font, itemName, centerX, centerY - 54, 0xFFFFFF);

        drawString(poseStack, this.font, "Weight:", centerX - 150, centerY - 46, 0xFFFFFF);
        drawString(poseStack, this.font, "Custom ID:", centerX - 150, centerY - 12, 0xFFFFFF);

        if (gunInfo.hasGunId()) {
            drawString(poseStack, this.font, "Ammo Weight/Round:", centerX - 150, centerY + 22, 0xFFFFFF);
        }

        if (!idBox.getValue().trim().isEmpty()) {
            String status = hasIdError ? "✗ Invalid" : "✓ Valid";
            int color = hasIdError ? 0xFF5555 : 0x55FF55;
            drawString(poseStack, this.font, status, centerX + 150, centerY + 4, color);
        }

        renderCurrentIdInfo(poseStack, centerY + 56);

        if (hasWeightError) {
            String errorMsg = new TranslatableComponent("gui.ezweight.invalid_weight").getString();
            drawCenteredString(poseStack, this.font, errorMsg, centerX, centerY - 22, 0xFF5555);
        }

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private void renderCurrentIdInfo(PoseStack poseStack, int startY) {
        int centerX = this.width / 2;
        int y = startY;

        if (gunInfo.hasGunId()) {
            String gunText = "Gun ID: " + gunInfo.getGunId().get();
            drawCenteredString(poseStack, this.font, gunText, centerX, y, 0xFFFF55);
            y += 12;
        }

        if (gunInfo.hasAttachmentId()) {
            String attText = "Attachment ID: " + gunInfo.getAttachmentId().get();
            drawCenteredString(poseStack, this.font, attText, centerX, y, 0xFF55FF);
            y += 12;
        }

        if (gunInfo.hasAmmoId()) {
            String ammoText = "Ammo ID: " + gunInfo.getAmmoId().get();
            drawCenteredString(poseStack, this.font, ammoText, centerX, y, 0x55FF55);
        }
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
        if (weightBox != null) weightBox.tick();
        if (idBox != null) idBox.tick();
        if (ammoWeightBox != null) ammoWeightBox.tick();
    }
}