package com.subtitlescreen.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class SubtitleOverlayRenderer {
    private static final float TEXT_SCALE = 2.0f;
    private static final int LINE_HEIGHT = 12;
    private static final int TEXT_HEIGHT = 9;

    private SubtitleRenderState currentState;
    private long startGameTime;

    public void showSubtitle(String text, String fontType, int duration, String textColorHex, String playerNameColorHex) {
        MinecraftClient client = MinecraftClient.getInstance();
        String playerName = client.player == null ? "" : client.player.getName().getString();

        this.currentState = new SubtitleRenderState(
                text,
                fontType,
                duration,
                textColorHex,
                playerNameColorHex,
                playerName
        );
        this.startGameTime = client.world == null ? 0L : client.world.getTime();
    }

    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (currentState == null || client.player == null || client.world == null) {
            return;
        }

        long elapsedTicks = Math.max(0L, client.world.getTime() - startGameTime);
        if (currentState.isExpired(elapsedTicks)) {
            currentState = null;
            return;
        }

        float alpha = currentState.alphaAt(elapsedTicks);
        if (alpha <= 0.0f) {
            return;
        }

        renderSubtitle(context, client, currentState, alpha);
    }

    public boolean isDisplaying() {
        return currentState != null;
    }

    private void renderSubtitle(DrawContext context, MinecraftClient client, SubtitleRenderState state, float alpha) {
        TextRenderer textRenderer = resolveTextRenderer(client, state.fontType());
        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 4;
        int lineCount = Math.max(1, state.lines().size());
        int textBlockHeight = TEXT_HEIGHT + (lineCount - 1) * LINE_HEIGHT;
        int scaledCenterX = Math.round(centerX / TEXT_SCALE);
        int scaledTextY = Math.round((centerY - textBlockHeight * TEXT_SCALE / 2.0f) / TEXT_SCALE);

        RenderSystem.enableBlend();
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale(TEXT_SCALE, TEXT_SCALE, 1.0f);

        for (int i = 0; i < state.lines().size(); i++) {
            SubtitleRenderState.RenderLine line = state.lines().get(i);
            int lineWidth = textRenderer.getWidth(line.text());
            int x = scaledCenterX - lineWidth / 2;
            renderLine(context, textRenderer, state, line, x, scaledTextY + i * LINE_HEIGHT, alpha);
        }

        matrices.pop();
        RenderSystem.disableBlend();
    }

    private void renderLine(DrawContext context, TextRenderer textRenderer, SubtitleRenderState state,
                            SubtitleRenderState.RenderLine line, int x, int y, float alpha) {
        int currentX = x;
        int rainbowIndex = 0;

        for (SubtitleRenderState.TextSegment segment : line.segments()) {
            if (segment.text().isEmpty()) {
                continue;
            }

            if (segment.playerName()) {
                context.drawText(textRenderer, Text.literal(segment.text()), currentX, y, state.playerNameColor(alpha), true);
                currentX += textRenderer.getWidth(segment.text());
                rainbowIndex += segment.text().length();
                continue;
            }

            if (state.usesRainbowText()) {
                for (int i = 0; i < segment.text().length(); i++) {
                    String character = String.valueOf(segment.text().charAt(i));
                    context.drawText(textRenderer, Text.literal(character), currentX, y,
                            SubtitleRenderState.rainbowColorAt(rainbowIndex, alpha), true);
                    currentX += textRenderer.getWidth(character);
                    rainbowIndex++;
                }
            } else {
                context.drawText(textRenderer, Text.literal(segment.text()), currentX, y, state.normalColor(alpha), true);
                currentX += textRenderer.getWidth(segment.text());
                rainbowIndex += segment.text().length();
            }
        }
    }

    private TextRenderer resolveTextRenderer(MinecraftClient client, String fontType) {
        return client.textRenderer;
    }
}
