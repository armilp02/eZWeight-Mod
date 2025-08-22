package com.armilp.ezweight.client.gui;

import com.armilp.ezweight.client.gui.edit.MultiWeightEditScreen;
import com.armilp.ezweight.data.ItemWeightRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WeightMenuScreen extends Screen {
    private final String namespace;
    private List<ItemStackWithWeight> items;
    private WeightListWidget list;
    private EditBox searchBox;
    private String searchQuery = "";
    private Button clearSearchButton;

    public WeightMenuScreen(String namespace) {
        super(Component.translatable("screen.ezweight.items_title", namespace));
        this.namespace = namespace;
        this.items = ItemWeightRegistry.getItemsForNamespace(namespace);
    }

    @Deprecated
    public WeightMenuScreen(String namespace, List<ItemStackWithWeight> items) {
        super(Component.translatable("screen.ezweight.items_title", namespace));
        this.namespace = namespace;
        this.items = items != null ? items : ItemWeightRegistry.getItemsForNamespace(namespace);
    }

    @Override
    protected void init() {
        super.init();

        this.list = new WeightListWidget(this, this.minecraft, this.width, this.height, 80, this.height - 32, 24);
        this.addWidget(this.list);

        Button backButton = Button.builder(Component.translatable("button.ezweight.back"), b ->
                Minecraft.getInstance().setScreen(new WeightNamespaceScreen())
        ).bounds(10, 30, 60, 20).build();
        this.addRenderableWidget(backButton);

        searchBox = new EditBox(this.font, 10, 55, 200, 16, Component.translatable("textbox.ezweight.search"));
        searchBox.setResponder(this::onSearchChanged);
        searchBox.setMaxLength(50);
        this.addRenderableWidget(searchBox);

        clearSearchButton = Button.builder(Component.literal("✕"), b -> searchBox.setValue(""))
                .bounds(215, 55, 20, 16).build();
        this.addRenderableWidget(clearSearchButton);

        Button editButton = Button.builder(Component.translatable("button.ezweight.edit_selected"), b -> {
            List<WeightListWidget.ItemEntry> selected = new ArrayList<>(list.getSelectedEntries());
            if (!selected.isEmpty()) {
                Minecraft.getInstance().setScreen(new MultiWeightEditScreen(this, selected));
            }
        }).bounds(this.width - 140, 30, 120, 20).build();
        this.addRenderableWidget(editButton);

        Button refreshButton = Button.builder(Component.translatable("button.ezweight.refresh"), b -> {
            refreshItems();
        }).bounds(this.width - 260, 30, 100, 20).build();
        this.addRenderableWidget(refreshButton);

        if ("tacz".equals(this.namespace)) {
            refreshItems();
        } else {
            updateList();
        }
    }

    private void refreshItems() {
        this.items = ItemWeightRegistry.getItemsForNamespace(namespace);
        updateList();
    }

    private void onSearchChanged(String query) {
        this.searchQuery = query.trim().toLowerCase();
        updateList();
    }

    private void updateList() {
        list.clear();
        list.addCategory(namespace);

        List<ItemStackWithWeight> filtered = new ArrayList<>();
        for (ItemStackWithWeight sw : items) {
            String name = sw.stack().getHoverName().getString().toLowerCase();
            ResourceLocation effectiveId = ItemWeightRegistry.getEffectiveId(sw.stack());
            String effectiveName = effectiveId != null ? effectiveId.toString().toLowerCase() : "";

            if (searchQuery.isEmpty() || name.contains(searchQuery) || effectiveName.contains(searchQuery)) {
                filtered.add(sw);
            }
        }

        filtered.sort(Comparator.comparing(sw -> sw.stack().getHoverName().getString()));

        for (ItemStackWithWeight sw : filtered) {
            ResourceLocation effectiveId = ItemWeightRegistry.getEffectiveId(sw.stack());
            double actualWeight = ItemWeightRegistry.getAllWeights().getOrDefault(effectiveId, 1.0);
            list.addItem(sw.stack(), actualWeight, effectiveId);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        String itemCount = Component.translatable("gui.ezweight.item_count", list.children().size() - 1).getString();
        graphics.drawString(this.font, itemCount, this.width - 200, 10, 0xAAAAAA, false);

        super.render(graphics, mouseX, mouseY, partialTick);
        searchBox.render(graphics, mouseX, mouseY, partialTick);

        if (searchBox.getValue().isEmpty() && !searchBox.isFocused()) {
            graphics.drawString(
                    this.font,
                    searchBox.getMessage().getString(),
                    searchBox.getX() + 4,
                    searchBox.getY() + 3,
                    0x888888,
                    false
            );
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (searchBox.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        searchBox.tick();
    }

    public void onWeightUpdated() {
        updateList();
    }
}