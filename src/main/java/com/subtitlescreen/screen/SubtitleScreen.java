package com.subtitlescreen.screen;

import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import com.subtitlescreen.network.SubtitleUpdatePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

@Environment(EnvType.CLIENT)
public class SubtitleScreen extends AbstractContainerScreen<SubtitleScreenHandler> {
    private static final int TEXT_LINE_COUNT = 4;

    private static final String[] FONT_IDS = {"微软雅黑", "宋体", "楷体"};
    private static final String[] FONT_LABELS = {"微软雅黑", "宋体", "楷体"};
    private static final String[] TRIGGER_IDS = {"on_join", "redstone"};
    private static final String[] TRIGGER_LABELS = {"玩家进入世界", "红石触发"};
    private static final ColorOption[] COLOR_OPTIONS = {
            new ColorOption("黑", "000000"),
            new ColorOption("深灰", "555555"),
            new ColorOption("灰", "AAAAAA"),
            new ColorOption("白", "FFFFFF"),
            new ColorOption("红", "FF0000"),
            new ColorOption("橙", "FF8000"),
            new ColorOption("黄", "FFFF00"),
            new ColorOption("黄绿", "80FF00"),
            new ColorOption("绿", "00AA00"),
            new ColorOption("青", "00FFFF"),
            new ColorOption("天蓝", "0080FF"),
            new ColorOption("蓝", "0000FF"),
            new ColorOption("紫", "8000FF"),
            new ColorOption("品红", "FF00FF"),
            new ColorOption("粉", "FF80C0"),
            new ColorOption("棕", "8B4513")
    };

    private final EditBox[] subtitleFields = new EditBox[TEXT_LINE_COUNT];
    private Button fontButton;
    private Button triggerButton;
    private DurationSliderWidget durationSlider;

    private int currentFontIndex = 0;
    private int currentTriggerIndex = 0;
    private int durationSeconds = 2;
    private String textColorHex = "FFFFFF";
    private String playerNameColorHex = "00FF00";
    private SubtitleBlockEntity blockEntity;

    public SubtitleScreen(SubtitleScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 340;
        this.imageHeight = 292;
    }

    @Override
    protected void init() {
        super.init();
        loadBlockEntity();
        loadCurrentSettings();

        int left = this.width / 2 - 160;
        int top = this.height / 2 - this.imageHeight / 2;
        int fullWidth = 320;

        for (int i = 0; i < TEXT_LINE_COUNT; i++) {
            EditBox field = new EditBox(textRenderer, left, top + 20 + i * 22, fullWidth, 20,
                    Component.literal("字幕内容 " + (i + 1)));
            field.setMaxLength(256);
            subtitleFields[i] = field;
            addDrawableChild(field);
        }

        if (blockEntity != null) {
            String[] lines = blockEntity.getSubtitleText().split("\\R", -1);
            for (int i = 0; i < subtitleFields.length && i < lines.length; i++) {
                subtitleFields[i].setMessage(lines[i]);
            }
        }

        fontButton = Button.builder(Component.empty(), button -> {
            currentFontIndex = (currentFontIndex + 1) % FONT_IDS.length;
            updateFontButton();
        }).dimensions(left, top + 113, 150, 20).build();
        updateFontButton();
        addDrawableChild(fontButton);

        triggerButton = Button.builder(Component.empty(), button -> {
            currentTriggerIndex = (currentTriggerIndex + 1) % TRIGGER_IDS.length;
            updateTriggerButton();
        }).dimensions(left + 170, top + 113, 150, 20).build();
        updateTriggerButton();
        addDrawableChild(triggerButton);

        durationSlider = new DurationSliderWidget(left, top + 140, 240, 20, durationSeconds);
        addDrawableChild(durationSlider);

        addColorButtons(left, top + 170, false);
        addColorButtons(left, top + 218, true);

        addDrawableChild(Button.builder(Component.literal("彩虹文本"), button -> textColorHex = "rainbow")
                .dimensions(left + 248, top + 140, 72, 20)
                .build());

        addDrawableChild(Button.builder(Component.literal("保存"), button -> {
            saveSettings();
            close();
        }).dimensions(left + 116, top + 264, 96, 20).build());

        addDrawableChild(Button.builder(Component.literal("取消"), button -> close())
                .dimensions(left + 224, top + 264, 96, 20)
                .build());
    }

    private void loadBlockEntity() {
        if (client != null && client.level != null && handler.getBlockPos() != null
                && client.level.getBlockEntity(handler.getBlockPos()) instanceof SubtitleBlockEntity entity) {
            this.blockEntity = entity;
        }
    }

    private void loadCurrentSettings() {
        if (blockEntity == null) {
            return;
        }

        currentFontIndex = findIndex(FONT_IDS, blockEntity.getFontType(), 0);
        currentTriggerIndex = findIndex(TRIGGER_IDS, blockEntity.getTriggerMode(), 0);
        durationSeconds = Math.max(1, Math.min(10, Math.round(blockEntity.getDuration() / 20.0f)));
        textColorHex = normalizeColor(blockEntity.getTextColorHex(), "FFFFFF");
        playerNameColorHex = normalizeColor(blockEntity.getPlayerNameColorHex(), "00FF00");
    }

    private void addColorButtons(int left, int y, boolean playerNameColor) {
        for (int i = 0; i < COLOR_OPTIONS.length; i++) {
            ColorOption option = COLOR_OPTIONS[i];
            int buttonX = left + (i % 8) * 40;
            int buttonY = y + (i / 8) * 20;
            addDrawableChild(Button.builder(Component.literal(option.label()), button -> {
                if (playerNameColor) {
                    playerNameColorHex = option.hex();
                } else {
                    textColorHex = option.hex();
                }
            }).dimensions(buttonX, buttonY, 38, 18).build());
        }
    }

    private void updateFontButton() {
        fontButton.setMessage(Component.literal("字体: " + FONT_LABELS[currentFontIndex]));
    }

    private void updateTriggerButton() {
        triggerButton.setMessage(Component.literal("触发: " + TRIGGER_LABELS[currentTriggerIndex]));
    }

    private void saveSettings() {
        if (client == null || client.getNetworkHandler() == null || handler.getBlockPos() == null) {
            return;
        }

        BlockPos pos = handler.getBlockPos();
        ClientPlayNetworking.send(new SubtitleUpdatePayload(
                pos,
                collectSubtitleText(),
                FONT_IDS[currentFontIndex],
                TRIGGER_IDS[currentTriggerIndex],
                durationSeconds * 20,
                textColorHex,
                playerNameColorHex
        ));
    }

    private String collectSubtitleText() {
        StringBuilder builder = new StringBuilder();

        for (EditBox field : subtitleFields) {
            String line = field.getText();
            if (!line.isEmpty() || builder.length() > 0) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(line);
            }
        }

        return builder.isEmpty() ? "" : builder.toString();
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int left = this.width / 2 - 160;
        int top = this.height / 2 - this.imageHeight / 2;

        context.drawCenteredTextWithShadow(textRenderer, Component.literal("字幕方块设置"), width / 2, top + 4, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Component.literal("字幕内容"), left, top + 9, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Component.literal("普通文本颜色: " + colorLabelFor(textColorHex)), left, top + 160, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Component.literal("玩家名颜色: " + colorLabelFor(playerNameColorHex)), left, top + 208, 0xFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void drawBackground(GuiGraphicsExtractor context, float delta, int mouseX, int mouseY) {
        int left = this.width / 2 - this.imageWidth / 2;
        int top = this.height / 2 - this.imageHeight / 2;
        context.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xC0101010);
    }

    private static int findIndex(String[] values, String value, int fallback) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return i;
            }
        }
        return fallback;
    }

    private static String normalizeColor(String value, String fallback) {
        if ("rainbow".equalsIgnoreCase(value)) {
            return "rainbow";
        }

        if (value != null && value.matches("(?i)[0-9a-f]{6}")) {
            return value.toUpperCase();
        }

        return fallback;
    }

    private static String colorLabelFor(String value) {
        if ("rainbow".equalsIgnoreCase(value)) {
            return "彩虹";
        }

        for (ColorOption option : COLOR_OPTIONS) {
            if (option.hex().equalsIgnoreCase(value)) {
                return option.label();
            }
        }

        return value;
    }

    private record ColorOption(String label, String hex) {
    }

    private class DurationSliderWidget extends AbstractSliderButton {
        DurationSliderWidget(int x, int y, int width, int height, int seconds) {
            super(x, y, width, height, Component.empty(), (Math.max(1, Math.min(10, seconds)) - 1) / 9.0);
            applyValue();
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("持续时间: " + durationSeconds + " 秒"));
        }

        @Override
        protected void applyValue() {
            durationSeconds = 1 + (int) Math.round(value * 9.0);
            durationSeconds = Math.max(1, Math.min(10, durationSeconds));
        }
    }
}
