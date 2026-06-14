package com.armilp.ezweight.client.gui.hud;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.WeightSyncData;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "ezweight", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeightHudOverlay {

    private static final ResourceLocation ICON = new ResourceLocation("ezweight", "textures/gui/ezweight_on.png");
    private static final ResourceLocation BACKGROUND = new ResourceLocation("ezweight", "textures/gui/ezweight_backg.png");
    private static final ResourceLocation BACKGROUND_BROKEN = new ResourceLocation("ezweight", "textures/gui/ezweight_backg_broken.png");
    private static final ResourceLocation WEIGHT_BAR = new ResourceLocation("ezweight", "textures/gui/ezweight_bar.png");
    private static final ResourceLocation WEIGHT_BAR_FILL = new ResourceLocation("ezweight", "textures/gui/ezweight_bar1.png");

    private static final int ICON_SIZE = 16;
    private static final int TEXTURE_SIZE = 80;
    private static final int RENDER_SIZE = 80;
    private static final int BAR_WIDTH = 51;
    private static final int BAR_HEIGHT = 11;
    private static final int BAR_TEXTURE_WIDTH = 51;
    private static final int BAR_TEXTURE_HEIGHT = 10;

    private static final float ELEMENT_SPACING_RATIO = 0.0625f;
    private static final float TITLE_SCALE = 0.95f;
    private static final float TEXT_SCALE = 0.8f;
    private static final int TEXT_COLOR = 0xFFFFFF;

    private static final String NBT_KEY_HUD_VISIBLE = "ezweight_hud_visible";
    private static final String NBT_KEY_DISPLAY_MODE = "ezweight_display_mode";
    private static final float FADE_SPEED = 0.05f;
    private static final double KG_TO_LB = 2.20462;

    private static int hudX, hudY, iconX, iconY;
    private static float alpha = 0f;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderScreen(ScreenEvent.DrawScreenEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getScreen() instanceof InventoryScreen) || mc.player == null || mc.options.hideGui) return;

        CompoundTag data = mc.player.getPersistentData();
        ensureNBTDefaults(data);

        boolean visible = data.getBoolean(NBT_KEY_HUD_VISIBLE);
        updateAlpha(visible);
        calculateHudPosition(mc);

        PoseStack poseStack = event.getPoseStack();
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        renderIcon(poseStack, iconX, iconY, visible);
        handleHoverTooltip(mc, poseStack, iconX, iconY, mouseX, mouseY, data);

        if (visible || alpha > 0f) {
            renderWeightHud(mc, poseStack, data);
        }
    }

    private static void ensureNBTDefaults(CompoundTag data) {
        if (!data.contains(NBT_KEY_HUD_VISIBLE)) {
            data.putBoolean(NBT_KEY_HUD_VISIBLE, true);
        }
        if (!data.contains(NBT_KEY_DISPLAY_MODE)) {
            data.putInt(NBT_KEY_DISPLAY_MODE, 0);
        }
    }

    private static void updateAlpha(boolean visible) {
        alpha = visible ? Math.min(1f, alpha + FADE_SPEED) : Math.max(0f, alpha - FADE_SPEED);
    }

    private static void renderIcon(PoseStack poseStack, int x, int y, boolean visible) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ICON);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, visible ? 1f : 0.8f);
        Screen.blit(poseStack, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void handleHoverTooltip(Minecraft mc, PoseStack poseStack, int iconX, int iconY,
                                           double mouseX, double mouseY, CompoundTag data) {
        if (!isMouseOver(mouseX, mouseY, iconX, iconY, ICON_SIZE, ICON_SIZE)) return;

        renderOutline(poseStack, iconX - 1, iconY - 1, ICON_SIZE + 2, ICON_SIZE + 2, 0xFFFFFFFF);

        int displayMode = data.getInt(NBT_KEY_DISPLAY_MODE);
        String tooltipText = switch (displayMode) {
            case 1 -> "Hide Weight";
            case 2 -> "Show Weight (KG)";
            default -> "Show Weight (lb)";
        };

        renderTooltip(mc, poseStack, new TextComponent(tooltipText), (int) mouseX + 6, (int) mouseY + 6);
    }

    private static void renderWeightHud(Minecraft mc, PoseStack poseStack, CompoundTag data) {
        assert mc.player != null;
        double weight = PlayerWeightHandler.getTotalWeight(mc.player);
        double maxWeight = WeightSyncData.getMaxWeight();
        int displayMode = data.getInt(NBT_KEY_DISPLAY_MODE);

        if (displayMode == 1) {
            weight *= KG_TO_LB;
            maxWeight *= KG_TO_LB;
        }

        double weightPercentage = Math.max(0.0, Math.min(1.0, weight / maxWeight));
        boolean isOverweight = isOverweightThreshold(weightPercentage);

        String title = "ᴇᴢᴡᴇɪɢʜᴛ";
        String unit = displayMode == 1 ? "lb" : "KG";
        String weightText = String.format("%.1f / %.1f %s", weight, maxWeight, unit);

        int elementSpacing = Math.round(RENDER_SIZE * ELEMENT_SPACING_RATIO);

        renderBackground(poseStack, hudX, hudY, isOverweight);
        renderTexts(mc, poseStack, hudX, hudY, title, weightText, weightPercentage, elementSpacing);
        renderWeightBar(poseStack, hudX, hudY, weightPercentage, elementSpacing);
    }

    private static boolean isOverweightThreshold(double weightPercentage) {
        if (!WeightConfig.COMMON.DAMAGE_OVERWEIGHT_ENABLED.get()) return false;

        List<? extends String> thresholds = WeightConfig.COMMON.DAMAGE_OVERWEIGHT_THRESHOLDS.get();
        if (thresholds.isEmpty()) return false;

        try {
            double threshold = Double.parseDouble(thresholds.get(0));
            return weightPercentage >= threshold;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void renderBackground(PoseStack poseStack, int x, int y, boolean isOverweight) {
        ResourceLocation background = isOverweight ? BACKGROUND_BROKEN : BACKGROUND;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, background);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        Screen.blit(poseStack, x, y, 0, 0, RENDER_SIZE, RENDER_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void renderTexts(Minecraft mc, PoseStack poseStack, int x, int y,
                                    String title, String weightText, double pct, int spacing) {
        Font font = mc.font;
        int titleWidth = font.width(title);
        int textWidth = font.width(weightText);
        float scaledTitleWidth = titleWidth * TITLE_SCALE;
        float scaledTextWidth = textWidth * TEXT_SCALE;

        int textAlpha = ((int) (alpha * 255) << 24) | (TEXT_COLOR & 0x00FFFFFF);

        float titleHeight = font.lineHeight * TITLE_SCALE;
        float textHeight = font.lineHeight * TEXT_SCALE;
        float totalContentHeight = titleHeight + spacing + textHeight + spacing + BAR_HEIGHT;
        float startY = y + (RENDER_SIZE - totalContentHeight) / 2f - (RENDER_SIZE * 0.12f);

        // Render title
        poseStack.pushPose();
        poseStack.translate(x + (RENDER_SIZE - scaledTitleWidth) / 2f, startY, 0);
        poseStack.scale(TITLE_SCALE, TITLE_SCALE, 1.0f);
        font.draw(poseStack, new TextComponent(title), 0, 0, textAlpha);
        poseStack.popPose();

        // Render weight text
        float textY = startY + titleHeight + spacing;
        poseStack.pushPose();
        poseStack.translate(x + (RENDER_SIZE - scaledTextWidth) / 2f, textY, 0);
        poseStack.scale(TEXT_SCALE, TEXT_SCALE, 1.0f);

        int color = getWeightColor(pct);
        int colorWithAlpha = ((int) (alpha * 255) << 24) | (color & 0x00FFFFFF);
        font.draw(poseStack, new TextComponent(weightText), 0, 0, colorWithAlpha);
        poseStack.popPose();
    }

    private static void renderWeightBar(PoseStack poseStack, int x, int y, double pct, int spacing) {
        Font font = Minecraft.getInstance().font;
        float titleHeight = font.lineHeight * TITLE_SCALE;
        float textHeight = font.lineHeight * TEXT_SCALE;
        float totalContentHeight = titleHeight + spacing + textHeight + spacing + BAR_HEIGHT;
        float startY = y + (RENDER_SIZE - totalContentHeight) / 2f - (RENDER_SIZE * 0.12f);

        int barY = Math.round(startY + titleHeight + spacing + textHeight + spacing);
        int barX = x + (RENDER_SIZE - BAR_WIDTH) / 2;
        int filledWidth = (int) (pct * BAR_WIDTH);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Render bar background
        RenderSystem.setShaderTexture(0, WEIGHT_BAR);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        Screen.blit(poseStack, barX, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_TEXTURE_WIDTH, BAR_TEXTURE_HEIGHT);

        // Render bar fill
        if (filledWidth > 0) {
            float[] rgb = getWeightBarColor(pct);
            RenderSystem.setShaderTexture(0, WEIGHT_BAR_FILL);
            RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], alpha);
            Screen.blit(poseStack, barX, barY, 0, 0, filledWidth, BAR_HEIGHT, BAR_TEXTURE_WIDTH, BAR_TEXTURE_HEIGHT);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static int getWeightColor(double pct) {
        if (pct >= 0.8) return 0xFFFF5555;
        if (pct >= 0.5) return 0xFFFFFF55;
        return 0xFF55FF55;
    }

    private static float[] getWeightBarColor(double pct) {
        if (pct >= 0.8) return new float[]{1f, 0.2f, 0.2f};
        if (pct >= 0.5) return new float[]{1f, 1f, 0.2f};
        return new float[]{0.2f, 1f, 0.2f};
    }

    private static void calculateHudPosition(Minecraft mc) {
        if (!(mc.screen instanceof InventoryScreen screen)) return;

        int inventoryLeft = screen.getGuiLeft();
        int inventoryTop = screen.getGuiTop();
        int inventoryWidth = screen.getXSize();
        int inventoryHeight = screen.getYSize();

        WeightConfig.Client.InventoryAnchor anchor = WeightConfig.CLIENT.MAIN_HUD_ANCHOR.get();
        int offsetX = WeightConfig.CLIENT.MAIN_HUD_OFFSET_X.get();
        int offsetY = WeightConfig.CLIENT.MAIN_HUD_OFFSET_Y.get();

        switch (anchor) {
            case TOP -> {
                hudX = inventoryLeft + (inventoryWidth - RENDER_SIZE) / 2 + offsetX;
                hudY = inventoryTop - RENDER_SIZE + offsetY;
                iconX = hudX + RENDER_SIZE + 5;
                iconY = hudY;
            }
            case BOTTOM -> {
                hudX = inventoryLeft + (inventoryWidth - RENDER_SIZE) / 2 + offsetX;
                hudY = inventoryTop + inventoryHeight + offsetY;
                iconX = hudX + RENDER_SIZE + 5;
                iconY = hudY;
            }
            case LEFT -> {
                hudX = inventoryLeft - RENDER_SIZE + offsetX;
                hudY = inventoryTop + (inventoryHeight - RENDER_SIZE) / 2 + offsetY;
                iconX = hudX - ICON_SIZE - 5;
                iconY = hudY;
            }
            case RIGHT -> {
                hudX = inventoryLeft + inventoryWidth + offsetX;
                hudY = inventoryTop + (inventoryHeight - RENDER_SIZE) / 2 + offsetY;
                iconX = hudX + RENDER_SIZE + 5;
                iconY = hudY;
            }
            default -> {
                hudX = inventoryLeft - RENDER_SIZE + offsetX;
                hudY = inventoryTop + (inventoryHeight - RENDER_SIZE) / 2 + offsetY;
                iconX = hudX - ICON_SIZE - 5;
                iconY = hudY;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseClickedEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getScreen() instanceof InventoryScreen) || mc.player == null) return;

        calculateHudPosition(mc);

        if (!isMouseOver(event.getMouseX(), event.getMouseY(), iconX, iconY, ICON_SIZE, ICON_SIZE)) return;

        CompoundTag data = mc.player.getPersistentData();
        int currentMode = data.getInt(NBT_KEY_DISPLAY_MODE);
        int nextMode = (currentMode + 1) % 3;

        data.putInt(NBT_KEY_DISPLAY_MODE, nextMode);
        data.putBoolean(NBT_KEY_HUD_VISIBLE, nextMode != 2);

        assert mc.level != null;
        mc.level.playSound(mc.player, mc.player.blockPosition(),
                SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 0.4f, 1.0f);
        event.setCanceled(true);
    }

    private static boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static void renderOutline(PoseStack poseStack, int x, int y, int width, int height, int color) {
        fill(poseStack, x, y, x + width, y + 1, color);
        fill(poseStack, x, y + height - 1, x + width, y + height, color);
        fill(poseStack, x, y + 1, x + 1, y + height - 1, color);
        fill(poseStack, x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    private static void fill(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        Screen.fill(poseStack, x1, y1, x2, y2, color);
    }

    private static void renderTooltip(Minecraft mc, PoseStack poseStack, Component text, int x, int y) {
        mc.screen.renderTooltip(poseStack, text, x, y);
    }
}