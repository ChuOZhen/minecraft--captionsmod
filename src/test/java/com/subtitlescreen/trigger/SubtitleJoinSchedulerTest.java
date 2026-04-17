package com.subtitlescreen.trigger;

import java.util.List;
import java.util.UUID;

public final class SubtitleJoinSchedulerTest {
    public static void main(String[] args) {
        waitsUntilDelayExpires();
        keepsMultiplePlayersIndependent();
    }

    private static void waitsUntilDelayExpires() {
        SubtitleJoinScheduler scheduler = new SubtitleJoinScheduler();
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        scheduler.schedule(playerId, 2);

        assertEquals(List.of(), scheduler.tick(), "first tick should not trigger yet");
        assertEquals(List.of(playerId), scheduler.tick(), "second tick should trigger player");
        assertEquals(List.of(), scheduler.tick(), "trigger should only happen once");
    }

    private static void keepsMultiplePlayersIndependent() {
        SubtitleJoinScheduler scheduler = new SubtitleJoinScheduler();
        UUID firstPlayerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID secondPlayerId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        scheduler.schedule(firstPlayerId, 1);
        scheduler.schedule(secondPlayerId, 3);

        assertEquals(List.of(firstPlayerId), scheduler.tick(), "first player should trigger first");
        assertEquals(List.of(), scheduler.tick(), "second player should still be waiting");
        assertEquals(List.of(secondPlayerId), scheduler.tick(), "second player should trigger after its own delay");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }
}
