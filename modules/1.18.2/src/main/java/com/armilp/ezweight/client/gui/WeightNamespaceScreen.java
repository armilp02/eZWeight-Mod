package com.armilp.ezweight.client.gui;

import com.armilp.ezweight.data.ItemWeightRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class WeightNamespaceScreen extends Screen {

    private final TreeMap<String, List<ItemStackWithWeight>> categorizedStacks = new TreeMap<>();
    private WeightNamespaceList list;

    public WeightNamespaceScreen() {
        super(new TranslatableComponent("screen.ezweight.select_category"));
    }

    @Override
    protected void init() {
        this.categorizedStacks.clear();

        for (Map.Entry<ResourceLocation, Double> entry : ItemWeightRegistry.getAllWeights().entrySet()) {
            ResourceLocation id = entry.getKey();
            double weight = entry.getValue();

            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null) continue;

            ItemStack stack = new ItemStack(item);
            String namespace = id.getNamespace();
            String path = id.getPath();

            this.categorizedStacks
                    .computeIfAbsent(namespace, k -> new ArrayList<>()) // Utilizas el namespace como clave
                    .add(new ItemStackWithWeight(stack, weight));

        }

        this.list = new WeightNamespaceList(this.minecraft, this.width, this.height - 50, 50, this.height);
        this.addRenderableWidget(this.list);

        List<ItemStackWithWeight> all = new ArrayList<>();
        for (Map.Entry<String, List<ItemStackWithWeight>> e : categorizedStacks.entrySet()) {
            if ("tacz".equals(e.getKey())) continue; // Excluir TACZ del agregado 'all'
            all.addAll(e.getValue());
        }
        this.list.addNamespaceEntry("all", all);

        // Agregar las otras entradas
        for (Map.Entry<String, List<ItemStackWithWeight>> entry : this.categorizedStacks.entrySet()) {
            this.list.addNamespaceEntry(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private static class WeightNamespaceList extends ObjectSelectionList<WeightNamespaceList.Entry> {

        private final Minecraft mc;

        public WeightNamespaceList(Minecraft mc, int width, int height, int top, int bottom) {
            super(mc, width, height, top, bottom, 20);
            this.mc = mc;
        }

        public void addNamespaceEntry(String Namespace, List<ItemStackWithWeight> ItemList) {
            this.addEntry(new Entry(this, Namespace, ItemList));
        }

        static class Entry extends ObjectSelectionList.Entry<Entry> {

            private final String Namespace;
            private final List<ItemStackWithWeight> ItemList;

            private final Button button;

            private final Minecraft mc;

            public Entry(WeightNamespaceList list, String Namespace, List<ItemStackWithWeight> ItemList) {
                this.Namespace = Namespace;
                this.ItemList = ItemList;

                this.mc = list.mc;

                this.button = new Button(0, 0, 200, 20,
                        new TextComponent(this.Namespace), b -> {
                    this.mc.setScreen(new WeightMenuScreen(this.Namespace, this.ItemList));
                });
            }

            @Override
            public void render(PoseStack poseStack, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovered, float partialTick) {

                this.button.y = top;
                this.button.x = left + width / 2 - 100;
                this.button.render(poseStack, mouseX, mouseY, partialTick);

                if (hovered) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(new TranslatableComponent("tooltip.ezweight.view_category"));
                    tooltip.add(new TranslatableComponent("tooltip.ezweight.item_count", this.ItemList.size()));
                    this.mc.screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) { // click izquierdo
                    this.mc.setScreen(new WeightMenuScreen(this.Namespace, this.ItemList));
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return new TextComponent(this.Namespace);
            }
        }
    }
}