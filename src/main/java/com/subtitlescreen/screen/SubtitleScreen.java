package com.subtitlescreen.screen;

import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import com.subtitlescreen.network.SubtitleUpdatePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * 字幕方块设置界面。
 *
 * <p>初版继承 AbstractContainerScreen（依赖 ScreenHandler/MenuType 注册）。26.3 把
 * MenuScreens.register 改成了 private，模组无法再注册菜单界面，因此改为普通 Screen：
 * 由服务端在权限校验后发 SubtitleOpenScreenPayload，客户端打开本界面；
 * 保存时通过 SubtitleUpdatePayload 把数据发回服务端。</p>
 */
@Environment(EnvType.CLIENT)
public class SubtitleScreen extends Screen {

    private static final int TEXT_LINE_COUNT = 4;
    private static final int GUI_WIDTH = 340;
    private static final int GUI_HEIGHT = 292;

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

    private final BlockPos pos;
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

    public SubtitleScreen(BlockPos pos) {
        super(Component.translatable("screen.subtitlescreen.subtitle_screen"));
        this.pos = pos;
    }

    private int left() {
        return this.width / 2 - GUI_WIDTH / 2 + 10;
    }

    private int top() {
        return this.height / 2 - GUI_HEIGHT / 2;
    }

    @Override
    protected void init() {
        super.init();
        loadBlockEntity();
        loadCurrentSettings();

        int left = left();
        int top = top();

        // 26.3：addDrawableChild -> addRenderableWidget；EditBox 用 setValue/getValue
        for (int i = 0; i < TEXT_LINE_COUNT; i++) {
            EditBox field = new EditBox(this.font, left, top + 20 + i * 22, 320, 20,
                    Component.literal("字幕内容 " + (i + 1)));
            field.setMaxLength(256);
            subtitleFields[i] = field;
            addRenderableWidget(field);
        }

        if (blockEntity != null) {
            String[] lines = blockEntity.getSubtitleText().split("\\R", -1);
            for (int i = 0; i < subtitleFields.length && i < lines.length; i++) {
                subtitleFields[i].setValue(lines[i]);
            }
        }

        // 26.3：Button.Builder.dimensions(x,y,w,h) -> bounds(x,y,w,h)
        fontButton = Button.builder(Component.empty(), button -> {
            currentFontIndex = (currentFontIndex + 1) % FONT_IDS.length;
            updateFontButton();
        }).bounds(left, top + 113, 150, 20).build();
        updateFontButton();
        addRenderableWidget(fontButton);

        triggerButton = Button.builder(Component.empty(), button -> {
            currentTriggerIndex = (currentTriggerIndex + 1) % TRIGGER_IDS.length;
            updateTriggerButton();
        }).bounds(left + 170, top + 113, 150, 20).build();
        updateTriggerButton();
        addRenderableWidget(triggerButton);

        durationSlider = new DurationSliderWidget(left, top + 140, 240, 20, durationSeconds);
        addRenderableWidget(durationSlider);

        addColorButtons(left, top + 170, false);
        addColorButtons(left, top + 218, true);

        addRenderableWidget(Button.builder(Component.literal("彩虹文本"), button -> textColorHex = "rainbow")
                .bounds(left + 248, top + 140, 72, 20)
                .build());

        addRenderableWidget(Button.builder(Component.literal("保存"), button -> {
            saveSettings();
            onClose();
        }).bounds(left + 116, top + 264, 96, 20).build());

        addRenderableWidget(Button.builder(Component.literal("取消"), button -> onClose())
                .bounds(left + 224, top + 264, 96, 20)
                .build());
    }

    private void loadBlockEntity() {
        if (this.minecraft != null && this.minecraft.level != null
                && this.minecraft.level.getBlockEntity(pos) instanceof SubtitleBlockEntity entity) {
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
            addRenderableWidget(Button.builder(Component.literal(option.label()), button -> {
                if (playerNameColor) {
                    playerNameColorHex = option.hex();
                } else {
                    textColorHex = option.hex();
                }
            }).bounds(buttonX, buttonY, 38, 18).build());
        }
    }

    private void updateFontButton() {
        fontButton.setMessage(Component.literal("字体: " + FONT_LABELS[currentFontIndex]));
    }

    private void updateTriggerButton() {
        triggerButton.setMessage(Component.literal("触发: " + TRIGGER_LABELS[currentTriggerIndex]));
    }

    private void saveSettings() {
        if (this.minecraft == null || this.minecraft.getConnection() == null) {
            return;
        }

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
            String line = field.getValue();
            if (!line.isEmpty() || builder.length() > 0) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(line);
            }
        }

        return builder.isEmpty() ? "" : builder.toString();
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

    // ------------------------------------------------------------------
    // 26.3：Screen.render(...) 改为 extractRenderState(GuiGraphicsExtractor, int, int, float)
    // ------------------------------------------------------------------
    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(extractor, mouseX, mouseY, delta);

        int left = left();
        int top = top();

        extractor.centeredText(this.font, Component.literal("字幕方块设置"), this.width / 2, top + 4, 0xFFFFFF);
        extractor.text(this.font, Component.literal("字幕内容"), left, top + 9, 0xFFFFFF);
        extractor.text(this.font, Component.literal("普通文本颜色: " + colorLabelFor(textColorHex)),
                left, top + 160, 0xFFFFFF);
        extractor.text(this.font, Component.literal("玩家名颜色: " + colorLabelFor(playerNameColorHex)),
                left, top + 208, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
