package com.subtitlescreen.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SubtitleRenderState {
    private static final int[] RAINBOW_COLORS = {
            0xFF0000,
            0xFF7F00,
            0xFFFF00,
            0x00FF00,
            0x0000FF,
            0x4B0082,
            0x9400D3
    };
    private static final int DEFAULT_FADE_TICKS = 10;

    private final String text;
    private final String fontType;
    private final int durationTicks;
    private final String textColorHex;
    private final String playerNameColorHex;
    private final String playerName;
    private final List<RenderLine> lines;

    public SubtitleRenderState(String text, String fontType, int durationTicks, String textColorHex,
                               String playerNameColorHex, String playerName) {
        this.text = text == null ? "" : text;
        this.fontType = fontType == null ? "" : fontType;
        this.durationTicks = Math.max(1, durationTicks);
        this.textColorHex = normalizeColorValue(textColorHex, "FFFFFF");
        this.playerNameColorHex = normalizeColorValue(playerNameColorHex, "00FF00");
        this.playerName = playerName == null ? "" : playerName;
        this.lines = splitLines(this.text, this.playerName);
    }

    public String text() {
        return text;
    }

    public String fontType() {
        return fontType;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public String textColorHex() {
        return textColorHex;
    }

    public String playerNameColorHex() {
        return playerNameColorHex;
    }

    public List<RenderLine> lines() {
        return lines;
    }

    public boolean usesRainbowText() {
        return "rainbow".equalsIgnoreCase(textColorHex);
    }

    public int normalColor(float alpha) {
        return parseColor(textColorHex, "FFFFFF", alpha);
    }

    public int playerNameColor(float alpha) {
        return parseColor(playerNameColorHex, "00FF00", alpha);
    }

    public float alphaAt(long elapsedTicks) {
        return alphaAt(elapsedTicks, durationTicks);
    }

    public boolean isExpired(long elapsedTicks) {
        return elapsedTicks >= durationTicks;
    }

    public static float alphaAt(long elapsedTicks, int durationTicks) {
        int safeDuration = Math.max(1, durationTicks);
        if (elapsedTicks <= 0 || elapsedTicks >= safeDuration) {
            return 0.0f;
        }

        int fadeTicks = Math.min(DEFAULT_FADE_TICKS, Math.max(1, safeDuration / 2));
        if (elapsedTicks < fadeTicks) {
            return clampAlpha(elapsedTicks / (float) fadeTicks);
        }

        if (elapsedTicks > safeDuration - fadeTicks) {
            return clampAlpha((safeDuration - elapsedTicks) / (float) fadeTicks);
        }

        return 1.0f;
    }

    public static int parseColor(String value, String fallback, float alpha) {
        String normalized = normalizeColorValue(value, fallback);
        if ("rainbow".equalsIgnoreCase(normalized)) {
            normalized = normalizeColorValue(fallback, "FFFFFF");
        }

        int rgb;
        try {
            rgb = Integer.parseInt(normalized, 16) & 0xFFFFFF;
        } catch (NumberFormatException exception) {
            rgb = 0xFFFFFF;
        }

        return withAlpha(rgb, alpha);
    }

    public static int rainbowColorAt(int index, float alpha) {
        int rgb = RAINBOW_COLORS[Math.floorMod(index, RAINBOW_COLORS.length)];
        return withAlpha(rgb, alpha);
    }

    private static List<RenderLine> splitLines(String text, String playerName) {
        String[] rawLines = text.split("\\R", -1);
        List<RenderLine> result = new ArrayList<>();

        for (String line : rawLines) {
            result.add(new RenderLine(line, splitSegments(line, playerName)));
        }

        if (result.isEmpty()) {
            result.add(new RenderLine("", List.of(new TextSegment("", false))));
        }

        return List.copyOf(result);
    }

    private static List<TextSegment> splitSegments(String line, String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return List.of(new TextSegment(line, false));
        }

        List<TextSegment> segments = new ArrayList<>();
        int cursor = 0;
        int matchIndex = line.indexOf(playerName);

        while (matchIndex >= 0) {
            if (matchIndex > cursor) {
                segments.add(new TextSegment(line.substring(cursor, matchIndex), false));
            }

            segments.add(new TextSegment(playerName, true));
            cursor = matchIndex + playerName.length();
            matchIndex = line.indexOf(playerName, cursor);
        }

        if (cursor < line.length()) {
            segments.add(new TextSegment(line.substring(cursor), false));
        }

        if (segments.isEmpty()) {
            segments.add(new TextSegment(line, false));
        }

        return List.copyOf(segments);
    }

    private static String normalizeColorValue(String value, String fallback) {
        if ("rainbow".equalsIgnoreCase(value)) {
            return "rainbow";
        }

        if (value != null && value.matches("(?i)[0-9a-f]{6}")) {
            return value.toUpperCase(Locale.ROOT);
        }

        if (fallback != null && fallback.matches("(?i)[0-9a-f]{6}")) {
            return fallback.toUpperCase(Locale.ROOT);
        }

        return "FFFFFF";
    }

    private static int withAlpha(int rgb, float alpha) {
        int alphaByte = Math.round(clampAlpha(alpha) * 255.0f) & 0xFF;
        return (alphaByte << 24) | (rgb & 0xFFFFFF);
    }

    private static float clampAlpha(float alpha) {
        return Math.max(0.0f, Math.min(1.0f, alpha));
    }

    public record RenderLine(String text, List<TextSegment> segments) {
    }

    public record TextSegment(String text, boolean playerName) {
    }
}
