package com.subtitlescreen.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * 字幕叠加层渲染器（Minecraft 26.3）。
 *
 * <p>26.3 的 HUD 渲染改成了「状态提取」两阶段模式：
 * {@code HudElement#extractRenderState(GuiGraphicsExtractor, DeltaTracker)} 只负责收集绘制指令，
 * 真正的绘制由 {@code GuiRenderer} 统一完成。初版基于 1.21.1 的
 * {@code HudRenderCallback} + {@code DrawContext} 即时绘制，这两个 API 在 26.3 均已不存在，
 * 因此本类按新模型重写。</p>
 */
@Environment(EnvType.CLIENT)
public class SubtitleOverlayRenderer {

    private static final float TEXT_SCALE = 2.0f;
    private static final int LINE_HEIGHT = 12;
    private static final int TEXT_HEIGHT = 9;

    private SubtitleRenderState currentState;
    private long startGameTime;

    public void showSubtitle(String text, String fontType, int duration, String textColorHex, String playerNameColorHex) {
        Minecraft client = Minecraft.getInstance();
        String playerName = client.player == null ? "" : client.player.getName().getString();

        this.currentState = new SubtitleRenderState(
                text,
                fontType,
                duration,
                textColorHex,
                playerNameColorHex,
                playerName
        );
        this.startGameTime = client.level == null ? 0L : client.level.getGameTime();
    }

    /** HudElement 的入口：收集本帧的字幕绘制指令。 */
    public void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (currentState == null || client.player == null || client.level == null) {
            return;
        }

        long elapsedTicks = Math.max(0L, client.level.getGameTime() - startGameTime);
        if (currentState.isExpired(elapsedTicks)) {
            currentState = null;
            return;
        }

        float alpha = currentState.alphaAt(elapsedTicks);
        if (alpha <= 0.0f) {
            return;
        }

        renderSubtitle(extractor, client, currentState, alpha);
    }

    public boolean isDisplaying() {
        return currentState != null;
    }

    private void renderSubtitle(GuiGraphicsExtractor extractor, Minecraft client, SubtitleRenderState state, float alpha) {
        Font font = client.font;
        int centerX = extractor.guiWidth() / 2;
        int centerY = extractor.guiHeight() / 4;
        int lineCount = Math.max(1, state.lines().size());
        int textBlockHeight = TEXT_HEIGHT + (lineCount - 1) * LINE_HEIGHT;
        float scaledCenterX = centerX / TEXT_SCALE;
        float scaledTextY = (centerY - textBlockHeight * TEXT_SCALE / 2.0f) / TEXT_SCALE;

        Matrix3x2fStack pose = extractor.pose();
        pose.pushMatrix();
        pose.scale(TEXT_SCALE, TEXT_SCALE);

        for (int i = 0; i < state.lines().size(); i++) {
            SubtitleRenderState.RenderLine line = state.lines().get(i);
            int lineWidth = font.width(line.text());
            int x = Math.round(scaledCenterX - lineWidth / 2.0f);
            renderLine(extractor, font, state, line, x, Math.round(scaledTextY) + i * LINE_HEIGHT, alpha);
        }

        pose.popMatrix();
    }

    private void renderLine(GuiGraphicsExtractor extractor, Font font, SubtitleRenderState state,
                            SubtitleRenderState.RenderLine line, int x, int y, float alpha) {
        int currentX = x;
        int rainbowIndex = 0;

        for (SubtitleRenderState.TextSegment segment : line.segments()) {
            if (segment.text().isEmpty()) {
                continue;
            }

            if (segment.playerName()) {
                extractor.text(font, segment.text(), currentX, y, state.playerNameColor(alpha), true);
                currentX += font.width(segment.text());
                rainbowIndex += segment.text().length();
                continue;
            }

            if (state.usesRainbowText()) {
                for (int i = 0; i < segment.text().length(); i++) {
                    String character = String.valueOf(segment.text().charAt(i));
                    extractor.text(font, character, currentX, y,
                            SubtitleRenderState.rainbowColorAt(rainbowIndex, alpha), true);
                    currentX += font.width(character);
                    rainbowIndex++;
                }
            } else {
                extractor.text(font, segment.text(), currentX, y, state.normalColor(alpha), true);
                currentX += font.width(segment.text());
                rainbowIndex += segment.text().length();
            }
        }
    }
}
