package com.subtitlescreen.render;

import java.util.List;

public final class SubtitleRenderStateTest {
    public static void main(String[] args) {
        parsesHexColorsWithOpaqueAlpha();
        fallsBackForInvalidColors();
        fadesInAndOutAcrossDuration();
        splitsLinesAndPlayerNameSegments();
        leavesTextWithoutPlayerNameAsNormalColor();
        cyclesRainbowColorsPerCharacter();
    }

    private static void parsesHexColorsWithOpaqueAlpha() {
        assertEquals(0xFF12AB34, SubtitleRenderState.parseColor("12ab34", "FFFFFF", 1.0f),
                "hex colors should parse case-insensitively and include alpha");
        assertEquals(0x8012AB34, SubtitleRenderState.parseColor("12AB34", "FFFFFF", 0.5f),
                "alpha should be applied to parsed colors");
    }

    private static void fallsBackForInvalidColors() {
        assertEquals(0xFFFFFFFF, SubtitleRenderState.parseColor("bad", "FFFFFF", 1.0f),
                "invalid color values should use fallback");
        assertEquals(0xFF00FF00, SubtitleRenderState.parseColor(null, "00FF00", 1.0f),
                "null color values should use fallback");
    }

    private static void fadesInAndOutAcrossDuration() {
        assertClose(0.1f, SubtitleRenderState.alphaAt(1, 100), "first tick should be partially visible");
        assertClose(1.0f, SubtitleRenderState.alphaAt(20, 100), "middle ticks should be fully visible");
        assertClose(0.5f, SubtitleRenderState.alphaAt(95, 100), "last ten ticks should fade out");
        assertClose(0.0f, SubtitleRenderState.alphaAt(100, 100), "duration boundary should be invisible");
    }

    private static void splitsLinesAndPlayerNameSegments() {
        SubtitleRenderState state = new SubtitleRenderState(
                "Hi Steve\nWelcome Steve",
                "微软雅黑",
                80,
                "FFFFFF",
                "00FF00",
                "Steve"
        );

        List<SubtitleRenderState.RenderLine> lines = state.lines();
        assertEquals(2, lines.size(), "text should split into two render lines");
        assertEquals(2, lines.get(0).segments().size(), "first line should include normal text and player segment");
        assertFalse(lines.get(0).segments().get(0).playerName(), "normal prefix should not be a player segment");
        assertTrue(lines.get(0).segments().get(1).playerName(), "matched player name should use player color");
        assertEquals("Steve", lines.get(0).segments().get(1).text(), "player segment text should be preserved");
    }

    private static void leavesTextWithoutPlayerNameAsNormalColor() {
        SubtitleRenderState state = new SubtitleRenderState(
                "No placeholder here",
                "微软雅黑",
                80,
                "FFFFFF",
                "00FF00",
                "Steve"
        );

        List<SubtitleRenderState.TextSegment> segments = state.lines().get(0).segments();
        assertEquals(1, segments.size(), "line without player name should have one normal segment");
        assertFalse(segments.get(0).playerName(), "line without player name should not use player color");
    }

    private static void cyclesRainbowColorsPerCharacter() {
        int first = SubtitleRenderState.rainbowColorAt(0, 1.0f);
        int eighth = SubtitleRenderState.rainbowColorAt(7, 1.0f);

        assertEquals(first, eighth, "rainbow color should cycle every seven characters");
        assertEquals(0x80FF0000, SubtitleRenderState.rainbowColorAt(0, 0.5f),
                "rainbow color should include alpha");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertClose(float expected, float actual, String message) {
        if (Math.abs(expected - actual) > 0.001f) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean value, String message) {
        if (value) {
            throw new AssertionError(message);
        }
    }
}
