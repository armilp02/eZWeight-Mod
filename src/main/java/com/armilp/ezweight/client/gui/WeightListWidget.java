package com.armilp.ezweight.client.gui;

import java.util.HashSet;
import java.util.Set;

import com.armilp.ezweight.client.gui.edit.GunIdEditScreen;
import com.armilp.ezweight.client.gui.edit.WeightEditScreen;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.util.GunIdUtils;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class WeightListWidget extends ObjectSelectionList<WeightListWidget.Entry> {
    public static final Set<ItemEntry> selectedEntries = new HashSet<>();
    private final WeightMenuScreen parent;

    public WeightListWidget(WeightMenuScreen parent, Minecraft mc, int width, int height, int top, int bottom, int itemHeight) {
        super(mc, width, height, top, bottom, itemHeight);
        this.parent = parent;
    }

    public Set<ItemEntry> getSelectedEntries() {
        return selectedEntries;
    }

    public void addCategory(String namespace) {
        this.addEntry(new CategoryEntry(namespace));
    }

    public void addItem(ItemStack stack, double weight) {
        ResourceLocation effectiveId = ItemWeightRegistry.getEffectiveId(stack);
        this.addEntry(new ItemEntry(stack, weight, effectiveId));
    }

    public void addItem(ItemStack stack, double weight, ResourceLocation effectiveId) {
        this.addEntry(new ItemEntry(stack, weight, effectiveId));
    }

    public void addNamespaceSeparator() {
        this.addEntry(new SeparatorEntry());
    }

    public void clear() {
        this.clearEntries();
        selectedEntries.clear();
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
    }

    public static class CategoryEntry extends Entry {
        private final Component title;

        public CategoryEntry(String namespace) {
            this.title = Component.literal("§6[" + namespace + "]");
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            graphics.drawString(Minecraft.getInstance().font, title, left + 5, top + 5, 0xFFFFAA);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public Component getNarration() {
            return title;
        }
    }

    public static class SeparatorEntry extends Entry {
        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            graphics.fill(left + 20, top + height / 2, left + width - 20, top + height / 2 + 1, 0x40FFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal("Separator");
        }
    }

    public class ItemEntry extends Entry {
        public final ItemStack stack;
        private final double weight;
        private final ResourceLocation effectiveId;
        private final boolean hasGunId;

        public ItemEntry(ItemStack stack, double weight, ResourceLocation effectiveId) {
            this.stack = stack;
            this.weight = weight;
            this.effectiveId = effectiveId != null ? effectiveId : ItemWeightRegistry.getEffectiveId(stack);
            this.hasGunId = GunIdUtils.hasGunId(stack) || GunIdUtils.hasAttachmentId(stack) || GunIdUtils.hasAmmoId(stack);
        }

        public ResourceLocation getEffectiveId() {
            return effectiveId;
        }

        public double getWeight() {
            return weight;
        }

        public boolean hasGunId() {
            return hasGunId;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            int color = selectedEntries.contains(this) ? 0xFFAA00 : 0xFFFFFF;

            if (selectedEntries.contains(this)) {
                graphics.fill(left, top, left + width, top + height, 0x8000FF00);
            } else if (hovered) {
                graphics.fill(left, top, left + width, top + height, 0x80808080);
            }

            if (hasGunId) {
                graphics.fill(left + 2, top + 2, left + 6, top + 6, 0xFF00AAFF);
            }

            graphics.renderItem(stack, left + 5, top + 2);
            graphics.renderItemDecorations(Minecraft.getInstance().font, stack, left + 5, top + 2);

            String itemName = stack.getHoverName().getString();

            double totalWeight = GunIdUtils.calculateTotalWeight(stack);
            double baseWeight = GunIdUtils.getBaseWeight(stack);
            double attachmentsWeight = GunIdUtils.getAttachmentsWeight(stack);
            double ammoWeight = GunIdUtils.getAmmoWeight(stack);

            String weightText = Component.translatable("tooltip.ezweight.item_weight", String.format("%.2f", totalWeight)).getString();

            if (attachmentsWeight > 0 || ammoWeight > 0) {
                weightText += " §7(Base: " + String.format("%.2f", baseWeight) + ")";
                if (attachmentsWeight > 0) weightText += " §d(+" + String.format("%.2f", attachmentsWeight) + ")";
                if (ammoWeight > 0) weightText += " §a(+" + String.format("%.2f", ammoWeight) + ")";
            }

            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            String idText = "";
            if (!effectiveId.equals(itemId)) {
                idText = " §7[" + effectiveId.getPath() + "]";
                boolean isGun = GunIdUtils.hasGunId(stack);
                boolean isAttach = GunIdUtils.hasAttachmentId(stack);
                boolean isAmmo = GunIdUtils.hasAmmoId(stack);
                if (isGun) idText += " §b[GUN]";
                if (isAttach) idText += " §d[ATTACH]";
                if (isAmmo) idText += " §a[AMMO]";
            }

            graphics.drawString(Minecraft.getInstance().font, itemName + idText, left + 26, top + 4, color);
            graphics.drawString(Minecraft.getInstance().font, weightText, left + 26, top + 14, 0xAAAAAA);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                long window = Minecraft.getInstance().getWindow().getWindow();
                boolean ctrlDown = InputConstants.isKeyDown(window, 341) || InputConstants.isKeyDown(window, 345);

                if (ctrlDown) {
                    if (selectedEntries.contains(this)) {
                        selectedEntries.remove(this);
                    } else {
                        selectedEntries.add(this);
                    }
                } else {
                    selectedEntries.clear();
                    selectedEntries.add(this);

                    if (hasGunId) {
                        Minecraft.getInstance().setScreen(new GunIdEditScreen(parent, stack));
                    } else if (GunIdUtils.hasAttachmentId(stack) || GunIdUtils.hasAmmoId(stack)) {
                        try {
                            // Intentamos cargar el AttachmentItem solo si está disponible.
                            Minecraft.getInstance().setScreen(new GunIdEditScreen(parent, stack));
                        } catch (Exception e) {
                            System.out.println("Error al cargar AttachmentItem: " + e.getMessage());
                            // Procedemos con un editor de peso general si es necesario.
                            Minecraft.getInstance().setScreen(new WeightEditScreen(parent, stack, effectiveId));
                        }
                    } else {
                        // Este es el caso para todos los ítems que no son de tipo Gun/Attachment/Ammo.
                        Minecraft.getInstance().setScreen(new WeightEditScreen(parent, stack, effectiveId));
                    }
                }
                return true;
            } else if (button == 1 && hasGunId) {
                showGunIdInfo();
                return true;
            }
            return false;
        }

        private void showGunIdInfo() {
            GunIdUtils.GunInfo gunInfo = GunIdUtils.getGunInfo(stack);
            System.out.println("GunId Info: " + gunInfo.toString());
        }

        @Override
        public Component getNarration() {
            String gunIdInfo = hasGunId ? " (Has GunId)" : "";
            return Component.literal(stack.getHoverName().getString() + " - Weight: " + weight + " - ID: " + effectiveId + gunIdInfo);
        }
    }
}
