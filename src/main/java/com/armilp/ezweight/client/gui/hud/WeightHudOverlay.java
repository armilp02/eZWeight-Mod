package com.armilp.ezweight.client.gui.hud;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.armilp.ezweight.config.WeightConfig.Client.InventoryAnchor.LEFT;
import static com.armilp.ezweight.config.WeightConfig.Client.InventoryAnchor.RIGHT;

@Mod.EventBusSubscriber(modid = "ezweight", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeightHudOverlay {

    private static final ResourceLocation ICON = new ResourceLocation("ezweight", "textures/gui/ezweight_on.png");
    private static final int ICON_SIZE = 16;
    private static final int PADDING = 6;
    private static final int BAR_HEIGHT = 6;
    private static final int MAX_BOX_WIDTH = 140;

    private static final float TITLE_SCALE = 1.0f;
    private static final float TEXT_SCALE = 0.85f;

    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BORDER_COLOR = 0x80FFFFFF;
    private static final int BACKGROUND_COLOR = 0xA0000000;

    private static final String NBT_KEY_HUD_VISIBLE = "ezweight_hud_visible";
    private static float alpha = 0f;
    private static final float FADE_SPEED = 0.05f;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof InventoryScreen) || mc.player == null || mc.options.hideGui) return;

        CompoundTag data = mc.player.getPersistentData();
        boolean visible = data.getBoolean(NBT_KEY_HUD_VISIBLE);
        if (!data.contains(NBT_KEY_HUD_VISIBLE)) {
            data.putBoolean(NBT_KEY_HUD_VISIBLE, true);
            visible = true;
        }

        updateAlpha(visible);

        GuiGraphics graphics = event.getGuiGraphics();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int iconX = 10;
        int iconY = screenHeight - ICON_SIZE - 10;

        renderIcon(graphics, iconX, iconY, visible);

        double mouseX = mc.mouseHandler.xpos() / mc.getWindow().getGuiScale();
        double mouseY = mc.mouseHandler.ypos() / mc.getWindow().getGuiScale();

        handleHoverTooltip(mc, graphics, iconX, iconY, mouseX, mouseY, visible);

        if (visible || alpha > 0f) {
            renderWeightHud(mc, graphics);
        }
    }

    private static void updateAlpha(boolean visible) {
        if (visible) {
            alpha = Math.min(1f, alpha + FADE_SPEED);
        } else {
            alpha = Math.max(0f, alpha - FADE_SPEED);
        }
    }

    private static void renderIcon(GuiGraphics graphics, int x, int y, boolean visible) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ICON);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, visible ? 1f : 0.8f);
        graphics.blit(ICON, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void handleHoverTooltip(Minecraft mc, GuiGraphics graphics, int iconX, int iconY, double mouseX, double mouseY, boolean visible) {
        boolean hovered = mouseX >= iconX && mouseX <= iconX + ICON_SIZE && mouseY >= iconY && mouseY <= iconY + ICON_SIZE;

        if (hovered) {
            graphics.renderOutline(iconX - 1, iconY - 1, ICON_SIZE + 2, ICON_SIZE + 2, 0xFFFFFFFF);
            Component tooltipText = Component.translatable(visible ? "tooltip.ezweight.hide_weight" : "tooltip.ezweight.show_weight");
            graphics.renderTooltip(mc.font, tooltipText, (int) mouseX + 6, (int) mouseY + 6);
        }
    }

    private static void renderWeightHud(Minecraft mc, GuiGraphics graphics) {
        double weight = PlayerWeightHandler.getTotalWeight(mc.player);
        double maxWeight = WeightConfig.COMMON.MAX_WEIGHT.get();
        double pct = Math.max(0.0, Math.min(1.0, weight / maxWeight));

        String title = "ᴇᴢᴡᴇɪɢʜᴛ";
        String weightText = String.format("%.1f / %.1f KG", weight, maxWeight);

        int titlePx = mc.font.width(title);
        int textPx = mc.font.width(weightText);

        float scaledTitleWidth = titlePx * TITLE_SCALE;
        float scaledTextWidth = textPx * TEXT_SCALE;

        int boxWidth = Math.min(MAX_BOX_WIDTH, (int) Math.max(scaledTitleWidth, scaledTextWidth) + PADDING * 2);
        int boxHeight = (int) ((mc.font.lineHeight * TITLE_SCALE) + (mc.font.lineHeight * TEXT_SCALE) + BAR_HEIGHT + PADDING * 4);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int inventoryLeft = (screenWidth - 176) / 2;
        int inventoryTop = (screenHeight - 166) / 2;

        WeightConfig.Client.InventoryAnchor anchor = WeightConfig.CLIENT.MAIN_HUD_ANCHOR.get();
        int offsetX = WeightConfig.CLIENT.MAIN_HUD_OFFSET_X.get();
        int offsetY = WeightConfig.CLIENT.MAIN_HUD_OFFSET_Y.get();

        int x, y;
        switch (anchor) {
            case TOP -> {
                x = inventoryLeft + (176 - boxWidth) / 2 + offsetX;
                y = inventoryTop - boxHeight + offsetY;
            }
            case BOTTOM -> {
                x = inventoryLeft + (176 - boxWidth) / 2 + offsetX;
                y = inventoryTop + 166 + offsetY;
            }
            case LEFT -> {
                x = inventoryLeft - boxWidth + offsetX;
                y = inventoryTop + (166 - boxHeight) / 2 + offsetY;
            }
            case RIGHT -> {
                x = inventoryLeft + 176 + offsetX;
                y = inventoryTop + (166 - boxHeight) / 2 + offsetY;
            }
            default -> {
                x = inventoryLeft - boxWidth + offsetX;
                y = inventoryTop + (166 - boxHeight) / 2 + offsetY;
            }
        }


        int bgAlpha = ((int)(alpha * 255) << 24) | (BACKGROUND_COLOR & 0x00FFFFFF);
        int borderAlpha = ((int)(alpha * 255) << 24) | (BORDER_COLOR & 0x00FFFFFF);
        int textAlpha = ((int)(alpha * 255) << 24) | (TEXT_COLOR & 0x00FFFFFF);

        graphics.fill(x, y, x + boxWidth, y + boxHeight, bgAlpha);
        graphics.renderOutline(x, y, boxWidth, boxHeight, borderAlpha);

        // Title
        graphics.pose().pushPose();
        graphics.pose().translate(x + (boxWidth - scaledTitleWidth) / 2f, y + PADDING, 0);
        graphics.pose().scale(TITLE_SCALE, TITLE_SCALE, 1.0f);
        graphics.drawString(mc.font, Component.literal(title), 0, 0, textAlpha, false);
        graphics.pose().popPose();

        // Text
        float textY = y + PADDING + (mc.font.lineHeight * TITLE_SCALE) + PADDING;
        graphics.pose().pushPose();
        graphics.pose().translate(x + (boxWidth - scaledTextWidth) / 2f, textY, 0);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);

        int color = pct >= 0.8 ? 0xFFFF5555 : pct >= 0.5 ? 0xFFFFFF55 : 0xFF55FF55;
        int colorWithAlpha = ((int)(alpha * 255) << 24) | (color & 0x00FFFFFF);

        graphics.drawString(mc.font, Component.literal(weightText), 0, 0, colorWithAlpha, false);
        graphics.pose().popPose();

        // Progress bar
        int barX = x + PADDING;
        int barY = (int)(textY + mc.font.lineHeight * TEXT_SCALE + PADDING);
        int filled = (int) ((boxWidth - PADDING * 2) * pct);

        graphics.fill(barX, barY, barX + (boxWidth - PADDING * 2), barY + BAR_HEIGHT, ((int)(alpha * 255) << 24) | 0x00333333);
        if (filled > 0) {
            graphics.fill(barX, barY, barX + filled, barY + BAR_HEIGHT, colorWithAlpha);
        }
        graphics.renderOutline(barX, barY, boxWidth - PADDING * 2, BAR_HEIGHT, textAlpha);
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed event) {
        Minecraft mc = Minecraft.getInstance();
        if (!(event.getScreen() instanceof InventoryScreen) || mc.player == null) return;

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        int iconX = 10;
        int iconY = mc.getWindow().getGuiScaledHeight() - ICON_SIZE - 10;

        boolean clicked = mouseX >= iconX && mouseX <= iconX + ICON_SIZE && mouseY >= iconY && mouseY <= iconY + ICON_SIZE;

        if (clicked) {
            CompoundTag data = mc.player.getPersistentData();
            boolean current = data.getBoolean(NBT_KEY_HUD_VISIBLE);
            data.putBoolean(NBT_KEY_HUD_VISIBLE, !current);

            if (mc.level != null) {
                mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.4f, 1.0f);
            }

            event.setCanceled(true);
        }
    }
}
