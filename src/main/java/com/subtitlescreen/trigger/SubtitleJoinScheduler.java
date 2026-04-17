package com.subtitlescreen.trigger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class SubtitleJoinScheduler {
    private final List<ScheduledJoin> scheduledJoins = new ArrayList<>();

    public void schedule(UUID playerId, int delayTicks) {
        scheduledJoins.add(new ScheduledJoin(playerId, Math.max(1, delayTicks)));
    }

    public List<UUID> tick() {
        List<UUID> duePlayerIds = new ArrayList<>();
        Iterator<ScheduledJoin> iterator = scheduledJoins.iterator();

        while (iterator.hasNext()) {
            ScheduledJoin scheduledJoin = iterator.next();
            scheduledJoin.remainingTicks--;
            if (scheduledJoin.remainingTicks <= 0) {
                duePlayerIds.add(scheduledJoin.playerId);
                iterator.remove();
            }
        }

        return duePlayerIds;
    }

    private static final class ScheduledJoin {
        private final UUID playerId;
        private int remainingTicks;

        private ScheduledJoin(UUID playerId, int remainingTicks) {
            this.playerId = playerId;
            this.remainingTicks = remainingTicks;
        }
    }
}
