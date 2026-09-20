package com.subtitlescreen.trigger;

import com.subtitlescreen.block.entity.SubtitleBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public final class SubtitleTriggerManager {
    private static final int JOIN_TRIGGER_DELAY_TICKS = 40;
    private static final SubtitleJoinScheduler JOIN_SCHEDULER = new SubtitleJoinScheduler();

    private SubtitleTriggerManager() {
    }

    public static void scheduleJoinSubtitles(ServerPlayer player) {
        JOIN_SCHEDULER.schedule(player.getUUID(), JOIN_TRIGGER_DELAY_TICKS);
    }

    public static void tick(MinecraftServer server) {
        for (var playerId : JOIN_SCHEDULER.tick()) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                triggerJoinSubtitles(player);
            }
        }
    }

    public static void triggerJoinSubtitles(ServerPlayer player) {
        ServerLevel world = player.serverLevel();

        for (long packedChunkPos : world.getForcedChunks()) {
            LevelChunk chunk = world.getChunk(ChunkPos.getPackedX(packedChunkPos), ChunkPos.getPackedZ(packedChunkPos));

            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof SubtitleBlockEntity subtitleBlockEntity
                        && "on_join".equals(subtitleBlockEntity.getTriggerMode())) {
                    subtitleBlockEntity.triggerForPlayer(player);
                }
            }
        }
    }
}
