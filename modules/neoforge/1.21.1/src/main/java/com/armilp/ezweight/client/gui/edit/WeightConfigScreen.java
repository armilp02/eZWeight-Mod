package com.armilp.ezweight.client.gui.edit;

import com.armilp.ezweight.config.WeightConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeightConfigScreen extends Screen {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ezweight", "textures/gui/ezweight_config_bg.png");
    private static final int BACKGROUND_WIDTH = 248;
    private static final int BACKGROUND_HEIGHT = 166;

    private final Screen parent;
    private int backgroundX, backgroundY;

    public WeightConfigScreen(Screen parent) {
        super(Component.literal("ᴇᴢᴡᴇɪɢʜᴛ ʜᴜᴅ"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.backgroundX = (this.width - BACKGROUND_WIDTH) / 2;
        this.backgroundY = (this.height - BACKGROUND_HEIGHT) / 2;

        int centerX = this.width / 2;
        int buttonWidth = 120;
        int buttonHeight = 18;

        int mainHudButtonY = backgroundY + 50;
        int miniHudButtonY = backgroundY + 78;
        int doneButtonY = backgroundY + 130;

        this.addRenderableWidget(Button.builder(
                        Component.literal("Main HUD Settings"),
                        btn -> this.minecraft.setScreen(new MainHudConfigScreen(this)))
                .bounds(centerX - buttonWidth / 2, mainHudButtonY, buttonWidth, buttonHeight)
                .build()
        );

        this.addRenderableWidget(Button.builder(
                        Component.literal("Mini HUD Settings"),
                        btn -> this.minecraft.setScreen(new MiniHudConfigScreen(this)))
                .bounds(centerX - buttonWidth / 2, miniHudButtonY, buttonWidth, buttonHeight)
                .build()
        );

        this.addRenderableWidget(Button.builder(
                        Component.literal("Done"),
                        btn -> this.minecraft.setScreen(parent))
                .bounds(centerX - 60 / 2, doneButtonY, 60, buttonHeight)
                .build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(BACKGROUND, backgroundX, backgroundY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        int centerX = this.width / 2;
        graphics.drawCenteredString(this.font, this.title, centerX, backgroundY + 20, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

@OnlyIn(Dist.CLIENT)
class MainHudConfigScreen extends Screen {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ezweight", "textures/gui/ezweight_config_bg.png");
    private static final int BACKGROUND_WIDTH = 248;
    private static final int BACKGROUND_HEIGHT = 166;

    private final Screen parent;
    private int backgroundX, backgroundY;
    private WeightConfig.Client.InventoryAnchor currentAnchor;
    private net.minecraft.client.gui.components.EditBox offsetXBox, offsetYBox;

    protected MainHudConfigScreen(Screen parent) {
        super(Component.literal("Main HUD Settings"));
        this.parent = parent;
        this.currentAnchor = WeightConfig.CLIENT.MAIN_HUD_ANCHOR.get();
    }

    @Override
    protected void init() {
        this.backgroundX = (this.width - BACKGROUND_WIDTH) / 2;
        this.backgroundY = (this.height - BACKGROUND_HEIGHT) / 2;

        int centerX = this.width / 2;
        int leftCol = backgroundX + 52;
        int rightCol = backgroundX + 150;

        int buttonWidth = 110;
        int buttonHeight = 16;
        int editBoxWidth = 45;

        int anchorButtonY = backgroundY + 40;
        int offsetBoxesY = backgroundY + 68;
        int resetButtonY = backgroundY + 105;
        int backButtonY = backgroundY + 130;

        this.addRenderableWidget(Button.builder(
                        Component.literal("Anchor: " + currentAnchor.name()),
                        btn -> {
                            WeightConfig.Client.InventoryAnchor[] values = WeightConfig.Client.InventoryAnchor.values();
                            currentAnchor = values[(currentAnchor.ordinal() + 1) % values.length];
                            WeightConfig.CLIENT.MAIN_HUD_ANCHOR.set(currentAnchor);
                            WeightConfig.CLIENT.MAIN_HUD_ANCHOR.save();
                            btn.setMessage(Component.literal("Anchor: " + currentAnchor.name()));
                        })
                .bounds(centerX - buttonWidth / 2, anchorButtonY, buttonWidth, buttonHeight)
                .build()
        );

        offsetXBox = new net.minecraft.client.gui.components.EditBox(this.font, leftCol, offsetBoxesY, editBoxWidth, buttonHeight, Component.literal("Offset X"));
        offsetXBox.setValue(String.valueOf(WeightConfig.CLIENT.MAIN_HUD_OFFSET_X.get()));
        offsetXBox.setMaxLength(4);
        offsetXBox.setResponder(text -> {
            try {
                int val = Integer.parseInt(text);
                if (val >= -500 && val <= 500) {
                    WeightConfig.CLIENT.MAIN_HUD_OFFSET_X.set(val);
                    WeightConfig.CLIENT.MAIN_HUD_OFFSET_X.save();
                }
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(offsetXBox);

        offsetYBox = new net.minecraft.client.gui.components.EditBox(this.font, rightCol, offsetBoxesY, editBoxWidth, buttonHeight, Component.literal("Offset Y"));
        offsetYBox.setValue(String.valueOf(WeightConfig.CLIENT.MAIN_HUD_OFFSET_Y.get()));
        offsetYBox.setMaxLength(4);
        offsetYBox.setResponder(text -> {
            try {
                int val = Integer.parseInt(text);
                if (val >= -500 && val <= 500) {
                    WeightConfig.CLIENT.MAIN_HUD_OFFSET_Y.set(val);
                    WeightConfig.CLIENT.MAIN_HUD_OFFSET_Y.save();
                }
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(offsetYBox);

        this.addRenderableWidget(Button.builder(
                        Component.literal("Reset"),
                        btn -> {
                            currentAnchor = WeightConfig.Client.InventoryAnchor.LEFT;
                            WeightConfig.CLIENT.MAIN_HUD_ANCHOR.set(currentAnchor);
                            WeightConfig.CLIENT.MAIN_HUD_OFFSET_X.set(-10);
                            WeightConfig.CLIENT.MAIN_HUD_OFFSET_Y.set(0);
                            WeightConfig.CLIENT.MAIN_HUD_ANCHOR.save();
                            WeightConfig.CLIENT.MAIN_HUD_OFFSET_X.save();
                            WeightConfig.CLIENT.MAIN_HUD_OFFSET_Y.save();
                            this.clearWidgets();
                            this.init();
                        })
                .bounds(centerX - 60 / 2, resetButtonY, 60, buttonHeight)
                .build()
        );

        this.addRenderableWidget(Button.builder(
                        Component.literal("Back"),
                        btn -> this.minecraft.setScreen(parent))
                .bounds(centerX - 60 / 2, backButtonY, 60, buttonHeight)
                .build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(BACKGROUND, backgroundX, backgroundY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        int centerX = this.width / 2;
        int leftCol = backgroundX + 52;
        int rightCol = backgroundX + 150;

        graphics.drawCenteredString(this.font, this.title, centerX, backgroundY + 15, 0xFFD700);

        int offsetBoxesY = backgroundY + 68;
        graphics.drawString(this.font, "X:", leftCol - 14, offsetBoxesY + 4, 0xFFFFFF, false);
        graphics.drawString(this.font, "Y:", rightCol - 14, offsetBoxesY + 4, 0xFFFFFF, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

@OnlyIn(Dist.CLIENT)
class MiniHudConfigScreen extends Screen {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ezweight", "textures/gui/ezweight_config_bg.png");
    private static final int BACKGROUND_WIDTH = 248;
    private static final int BACKGROUND_HEIGHT = 166;

    private final Screen parent;
    private int backgroundX, backgroundY;
    private net.minecraft.client.gui.components.EditBox miniXBox, miniYBox, miniSizeBox;

    protected MiniHudConfigScreen(Screen parent) {
        super(Component.literal("Mini HUD Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.backgroundX = (this.width - BACKGROUND_WIDTH) / 2;
        this.backgroundY = (this.height - BACKGROUND_HEIGHT) / 2;

        int centerX = this.width / 2;
        int leftCol = backgroundX + 52;
        int rightCol = backgroundX + 150;

        int buttonHeight = 16;
        int editBoxWidth = 45;

        int posBoxesY = backgroundY + 45;
        int sizeBoxY = backgroundY + 73;
        int resetButtonY = backgroundY + 105;
        int backButtonY = backgroundY + 130;

        miniXBox = new net.minecraft.client.gui.components.EditBox(this.font, leftCol, posBoxesY, editBoxWidth, buttonHeight, Component.literal("Mini X"));
        miniXBox.setValue(String.valueOf(WeightConfig.CLIENT.MINI_HUD_X.get()));
        miniXBox.setMaxLength(3);
        miniXBox.setResponder(text -> {
            try {
                int val = Integer.parseInt(text);
                if (val >= 0 && val <= 500) {
                    WeightConfig.CLIENT.MINI_HUD_X.set(val);
                    WeightConfig.CLIENT.MINI_HUD_X.save();
                }
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(miniXBox);

        miniYBox = new net.minecraft.client.gui.components.EditBox(this.font, rightCol, posBoxesY, editBoxWidth, buttonHeight, Component.literal("Mini Y"));
        miniYBox.setValue(String.valueOf(WeightConfig.CLIENT.MINI_HUD_Y.get()));
        miniYBox.setMaxLength(3);
        miniYBox.setResponder(text -> {
            try {
                int val = Integer.parseInt(text);
                if (val >= 0 && val <= 500) {
                    WeightConfig.CLIENT.MINI_HUD_Y.set(val);
                    WeightConfig.CLIENT.MINI_HUD_Y.save();
                }
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(miniYBox);

        miniSizeBox = new net.minecraft.client.gui.components.EditBox(this.font, centerX - editBoxWidth / 2, sizeBoxY, editBoxWidth, buttonHeight, Component.literal("Icon Size"));
        miniSizeBox.setValue(String.valueOf(WeightConfig.CLIENT.MINI_HUD_ICON_SIZE.get()));
        miniSizeBox.setMaxLength(3);
        miniSizeBox.setResponder(text -> {
            try {
                int val = Integer.parseInt(text);
                if (val >= 4 && val <= 256) {
                    WeightConfig.CLIENT.MINI_HUD_ICON_SIZE.set(val);
                    WeightConfig.CLIENT.MINI_HUD_ICON_SIZE.save();
                }
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(miniSizeBox);

        this.addRenderableWidget(Button.builder(
                        Component.literal("Reset"),
                        btn -> {
                            WeightConfig.CLIENT.MINI_HUD_X.set(15);
                            WeightConfig.CLIENT.MINI_HUD_Y.set(65);
                            WeightConfig.CLIENT.MINI_HUD_ICON_SIZE.set(18);
                            WeightConfig.CLIENT.MINI_HUD_X.save();
                            WeightConfig.CLIENT.MINI_HUD_Y.save();
                            WeightConfig.CLIENT.MINI_HUD_ICON_SIZE.save();
                            this.clearWidgets();
                            this.init();
                        })
                .bounds(centerX - 60 / 2, resetButtonY, 60, buttonHeight)
                .build()
        );

        this.addRenderableWidget(Button.builder(
                        Component.literal("Back"),
                        btn -> this.minecraft.setScreen(parent))
                .bounds(centerX - 60 / 2, backButtonY, 60, buttonHeight)
                .build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(BACKGROUND, backgroundX, backgroundY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        int centerX = this.width / 2;
        int leftCol = backgroundX + 52;
        int rightCol = backgroundX + 150;

        graphics.drawCenteredString(this.font, this.title, centerX, backgroundY + 15, 0xFFD700);

        int posBoxesY = backgroundY + 45;
        graphics.drawString(this.font, "X:", leftCol - 14, posBoxesY + 4, 0xFFFFFF, false);
        graphics.drawString(this.font, "Y:", rightCol - 14, posBoxesY + 4, 0xFFFFFF, false);

        int sizeBoxY = backgroundY + 73;
        graphics.drawString(this.font, "Size:", centerX - 42, sizeBoxY + 4, 0xFFFFFF, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}